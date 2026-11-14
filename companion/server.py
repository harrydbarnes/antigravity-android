"""Single-user HTTPS/loopback bridge to the official `agy acp` stdio protocol."""
import argparse
import hmac
import json
import os
from pathlib import Path
import secrets
import ssl
import subprocess
import threading
import time
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer


class Bridge:
    def __init__(self, command, workspace):
        self.command = command
        self.workspace = str(Path(workspace).resolve(strict=True))
        self.lock = threading.RLock()
        self.write_lock = threading.Lock()
        self.connect_lock = threading.Lock()
        self.process = None
        self.waiters = {}
        self.sequence = 0
        self.state = {"sessionId": "", "messages": [], "permissions": [],
                      "busy": False, "error": "", "workspace": self.workspace}
        self.permission_requests = {}
        self.reader = None
        self.closed = threading.Event()
        threading.Thread(target=self.expire_permissions, daemon=True).start()

    def expire_permissions(self):
        while not self.closed.wait(1):
            self.cancel_permissions(expired_only=True)

    def snapshot(self):
        with self.lock:
            return json.loads(json.dumps(self.state))

    def write(self, message):
        with self.write_lock:
            if not self.process or self.process.poll() is not None:
                raise RuntimeError("Antigravity is not running. Reconnect after checking the desktop CLI.")
            self.process.stdin.write(json.dumps(message) + "\n")
            self.process.stdin.flush()

    def rpc(self, method, params, timeout=60):
        with self.lock:
            self.sequence += 1
            request_id = self.sequence
            event = threading.Event()
            result = {}
            self.waiters[request_id] = (event, result)
        try:
            self.write({"jsonrpc": "2.0", "id": request_id, "method": method, "params": params})
            if not event.wait(timeout):
                raise RuntimeError("Antigravity timed out. Check desktop sign-in and reconnect.")
            if "error" in result:
                raise RuntimeError(str(result["error"]))
            return result.get("result", {})
        finally:
            with self.lock:
                self.waiters.pop(request_id, None)

    def read(self):
        try:
            for line in self.process.stdout:
                message = json.loads(line)
                with self.lock:
                    if "method" not in message:
                        waiter = self.waiters.get(message.get("id"))
                        if waiter:
                            waiter[1].update(message)
                            waiter[0].set()
                    elif message["method"] == "session/update":
                        update = message.get("params", {}).get("update", {})
                        if update.get("sessionUpdate") == "agent_message_chunk":
                            content = update.get("content", {})
                            if content.get("type") == "text":
                                self.append("assistant", content.get("text", ""), chunk=True)
                    elif message["method"] == "session/request_permission" and "id" in message:
                        params = message["params"]
                        key = secrets.token_urlsafe(24)
                        self.permission_requests[key] = (message["id"], params, time.monotonic())
                        self.state["permissions"].append({
                            "id": key, "title": params.get("toolCall", {}).get("title", "Tool approval"),
                            "options": params.get("options", []),
                            "details": json.dumps(params.get("toolCall", {}), indent=2)[:32768]})
                    elif "id" in message:
                        self.write({"jsonrpc": "2.0", "id": message["id"],
                                    "error": {"code": -32601, "message": "Client capability not supported"}})
        except (ValueError, OSError, RuntimeError) as exc:
            with self.lock:
                self.state["error"] = f"ACP connection failed: {exc}"
        finally:
            with self.lock:
                self.state["sessionId"] = ""
                self.state["busy"] = False
                self.state["permissions"] = []
                self.permission_requests.clear()
                for event, result in self.waiters.values():
                    result["error"] = "Antigravity disconnected. Check the CLI on your computer."
                    event.set()

    def append(self, role, text, chunk=False):
        messages = self.state["messages"]
        if chunk and messages and messages[-1]["role"] == role:
            messages[-1]["text"] = (messages[-1]["text"] + text)[-131072:]
        else:
            messages.append({"role": role, "text": text[-131072:]})
        # Bound polling payload and retained conversation data.
        while len(messages) > 100 or sum(len(m["text"]) for m in messages) > 262144:
            messages.pop(0)

    def connect(self):
        with self.connect_lock:
            if self.snapshot()["sessionId"]:
                return self.snapshot()
            if self.process:
                self.close()
            with self.lock:
                self.state.update(messages=[], permissions=[], busy=False, error="")
            self.process = subprocess.Popen(self.command, cwd=self.workspace, stdin=subprocess.PIPE,
                                            stdout=subprocess.PIPE, text=True, encoding="utf-8", bufsize=1)
            self.reader = threading.Thread(target=self.read, daemon=True)
            self.reader.start()
            result = self.rpc("initialize", {"protocolVersion": 1,
                              "clientInfo": {"name": "counterweight", "version": "1.0"},
                              "clientCapabilities": {}})
            if result.get("protocolVersion") != 1:
                self.close()
                raise RuntimeError("The desktop agent does not support ACP version 1.")
            result = self.rpc("session/new", {"cwd": self.workspace, "mcpServers": []})
            session_id = result.get("sessionId")
            if not isinstance(session_id, str) or not session_id:
                raise RuntimeError("The agent did not create a session. Run agy on the desktop and sign in first.")
            with self.lock:
                self.state["sessionId"] = session_id
            return self.snapshot()

    def prompt(self, text):
        if not isinstance(text, str) or not text.strip() or len(text) > 32000:
            raise ValueError("Enter a prompt of 1 to 32,000 characters.")
        with self.lock:
            session_id = self.state["sessionId"]
            if not session_id or self.state["busy"]:
                raise ValueError("Connect first and wait for the current turn to finish.")
            process = self.process
            self.state.update(busy=True, error="")
            self.append("user", text)
        def run():
            try:
                self.rpc("session/prompt", {"sessionId": session_id,
                         "prompt": [{"type": "text", "text": text}]}, timeout=3600)
            except (RuntimeError, OSError) as exc:
                with self.lock:
                    if self.process is process:
                        self.state["error"] = str(exc)
                # Do not let a previous turn tear down a newly reconnected process.
                self.close(expected_process=process)
            finally:
                with self.lock:
                    if self.process is process:
                        self.state["busy"] = False
                        self.cancel_permissions()
        threading.Thread(target=run, daemon=True).start()

    def permission(self, request_id, option_id=None):
        with self.lock:
            entry = self.permission_requests.get(request_id)
            if not entry:
                raise ValueError("Approval expired or has already been answered.")
            rpc_id, params, _ = entry
            if option_id is not None and option_id not in [o["optionId"] for o in params.get("options", [])]:
                raise ValueError("Unknown permission option.")
            result = {"outcome": "cancelled"} if option_id is None else {
                "outcome": "selected", "optionId": option_id}
            self.write({"jsonrpc": "2.0", "id": rpc_id, "result": {"outcome": result}})
            del self.permission_requests[request_id]
            self.state["permissions"] = [p for p in self.state["permissions"] if p["id"] != request_id]

    def cancel_permissions(self, expired_only=False):
        with self.lock:
            for key, (_, _, created) in list(self.permission_requests.items()):
                if not expired_only or time.monotonic() - created > 120:
                    try:
                        self.permission(key)
                    except (RuntimeError, OSError):
                        pass

    def cancel(self):
        self.cancel_permissions()
        session_id = self.snapshot()["sessionId"]
        if session_id:
            self.write({"jsonrpc": "2.0", "method": "session/cancel", "params": {"sessionId": session_id}})

    def close(self, expected_process=None):
        process = self.process
        if expected_process is not None and process is not expected_process:
            return
        if process and process.poll() is None:
            process.terminate()
            try:
                process.wait(timeout=5)
            except subprocess.TimeoutExpired:
                process.kill()
                process.wait()
        if self.reader and self.reader is not threading.current_thread():
            self.reader.join(timeout=5)
        if process:
            for stream in (process.stdin, process.stdout):
                if stream:
                    stream.close()


def make_handler(bridge, token):
    class Handler(BaseHTTPRequestHandler):
        def setup(self):
            super().setup()
            self.connection.settimeout(90)

        def log_message(self, *_):
            pass  # Never log bearer tokens, prompts or approval data.

        def respond(self, code, data):
            body = json.dumps(data).encode()
            self.send_response(code)
            self.send_header("Content-Type", "application/json")
            self.send_header("Cache-Control", "no-store")
            self.send_header("Content-Length", str(len(body)))
            self.end_headers()
            self.wfile.write(body)

        def authorised(self):
            if self.headers.get("Origin") or not hmac.compare_digest(
                    self.headers.get("Authorization", ""), "Bearer " + token):
                self.respond(401, {"error": "Pairing token rejected."})
                return False
            return True

        def do_GET(self):
            if not self.authorised():
                return
            if self.path != "/state":
                return self.respond(404, {"error": "Unknown endpoint."})
            bridge.cancel_permissions(expired_only=True)
            self.respond(200, bridge.snapshot())

        def do_POST(self):
            if not self.authorised():
                return
            try:
                length = int(self.headers.get("Content-Length", "0"))
                if not 0 < length <= 131072:
                    raise ValueError("Invalid request size.")
                data = json.loads(self.rfile.read(length))
                if not isinstance(data, dict):
                    raise ValueError("Expected an object.")
                if self.path == "/connect":
                    bridge.connect()
                elif self.path == "/prompt":
                    bridge.prompt(data.get("text"))
                elif self.path == "/permission":
                    bridge.permission(data.get("id"), data.get("optionId"))
                elif self.path == "/cancel":
                    bridge.cancel()
                else:
                    return self.respond(404, {"error": "Unknown endpoint."})
                self.respond(200, bridge.snapshot())
            except (ValueError, TypeError) as exc:
                self.respond(400, {"error": str(exc)})
            except (RuntimeError, OSError) as exc:
                self.respond(502, {"error": str(exc)})
    return Handler


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--workspace", required=True)
    parser.add_argument("--host", default="127.0.0.1")
    parser.add_argument("--port", type=int, default=8765)
    parser.add_argument("--cert")
    parser.add_argument("--key")
    parser.add_argument("--token-file", default=str(Path.home() / ".counterweight-token"))
    args = parser.parse_args()
    if args.host not in ("127.0.0.1", "::1", "localhost") and not (args.cert and args.key):
        parser.error("Non-loopback listeners require --cert and --key. Alternatively use an HTTPS reverse proxy.")
    path = Path(args.token_file)
    if not path.exists():
        fd = os.open(path, os.O_WRONLY | os.O_CREAT | os.O_EXCL, 0o600)
        with os.fdopen(fd, "w") as handle:
            handle.write(secrets.token_urlsafe(32))
    token = path.read_text().strip()
    if len(token) < 32:
        parser.error("Pairing token must contain at least 32 characters.")
    bridge = Bridge(["agy", "acp"], args.workspace)
    server = ThreadingHTTPServer((args.host, args.port), make_handler(bridge, token))
    if args.cert and args.key:
        context = ssl.SSLContext(ssl.PROTOCOL_TLS_SERVER)
        context.minimum_version = ssl.TLSVersion.TLSv1_2
        context.load_cert_chain(args.cert, args.key)
        server.socket = context.wrap_socket(server.socket, server_side=True)
    print(f"Companion listening on {args.host}:{args.port}. Pair with your HTTPS address.")
    print(f"Pairing token: {token}")
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        pass
    finally:
        bridge.closed.set()
        bridge.close()
        server.server_close()


if __name__ == "__main__":
    main()
