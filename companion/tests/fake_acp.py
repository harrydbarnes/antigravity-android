"""Deterministic protocol fixture, not an Antigravity implementation."""
import json
import sys


def send(message):
    print(json.dumps({"jsonrpc": "2.0", **message}), flush=True)


pending = None
for line in sys.stdin:
    request = json.loads(line)
    method = request.get("method")
    request_id = request.get("id")
    if method == "initialize":
        send({"id": request_id, "result": {"protocolVersion": 1}})
    elif method == "session/new":
        send({"id": request_id, "result": {"sessionId": "fixture-session"}})
    elif method == "session/prompt":
        text = request["params"]["prompt"][0]["text"]
        if text == "crash":
            sys.exit(1)
        pending = request_id
        send({"method": "session/update", "params": {"sessionId": "fixture-session", "update": {
            "sessionUpdate": "agent_message_chunk", "content": {"type": "text", "text": "Fixture response"}}}})
        send({"id": "permission-1", "method": "session/request_permission", "params": {
            "sessionId": "fixture-session", "toolCall": {"title": "Fixture tool", "rawInput": {"command": "test"}},
            "options": [{"optionId": "allow", "kind": "allow_once", "name": "Allow once"},
                        {"optionId": "reject", "kind": "reject_once", "name": "Reject"}]}})
    elif method == "session/cancel":
        if pending is not None:
            send({"id": pending, "result": {"stopReason": "cancelled"}})
            pending = None
    elif method is None and request_id == "permission-1":
        outcome = request["result"]["outcome"]
        send({"method": "session/update", "params": {"sessionId": "fixture-session", "update": {
            "sessionUpdate": "agent_message_chunk", "content": {"type": "text", "text": json.dumps(outcome)}}}})
        if pending is not None:
            send({"id": pending, "result": {"stopReason": "end_turn"}})
            pending = None
