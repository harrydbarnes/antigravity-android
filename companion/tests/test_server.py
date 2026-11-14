import http.client
import json
from pathlib import Path
import sys
import tempfile
import threading
import time
import unittest
from http.server import ThreadingHTTPServer

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from server import Bridge, make_handler


class CompanionTest(unittest.TestCase):
    def setUp(self):
        self.directory = tempfile.TemporaryDirectory()
        fixture = Path(__file__).with_name("fake_acp.py")
        self.bridge = Bridge([sys.executable, str(fixture)], self.directory.name)
        self.server = ThreadingHTTPServer(("127.0.0.1", 0), make_handler(self.bridge, "a" * 43))
        self.thread = threading.Thread(target=self.server.serve_forever)
        self.thread.start()

    def tearDown(self):
        self.bridge.closed.set()
        self.bridge.close()
        self.server.shutdown()
        self.server.server_close()
        self.thread.join()
        self.directory.cleanup()

    def request(self, path, data=None, token="a" * 43, origin=None):
        connection = http.client.HTTPConnection(*self.server.server_address, timeout=5)
        headers = {"Authorization": "Bearer " + token}
        if origin:
            headers["Origin"] = origin
        connection.request("GET" if data is None else "POST", path,
                           None if data is None else json.dumps(data), headers)
        response = connection.getresponse()
        result = response.status, json.loads(response.read())
        connection.close()
        return result

    def wait_for(self, condition):
        deadline = time.monotonic() + 3
        while time.monotonic() < deadline:
            state = self.bridge.snapshot()
            if condition(state):
                return state
            time.sleep(0.01)
        self.fail("Condition not reached: " + str(self.bridge.snapshot()))

    def test_rejects_missing_token_and_browser_origins(self):
        self.assertEqual(401, self.request("/state", token="wrong")[0])
        self.assertEqual(401, self.request("/state", origin="https://evil.example")[0])
        self.assertIsNone(self.bridge.process)

    def test_real_subprocess_round_trip_and_explicit_permission(self):
        status, state = self.request("/connect", {})
        self.assertEqual(200, status)
        self.assertEqual("fixture-session", state["sessionId"])
        self.assertEqual(200, self.request("/prompt", {"text": "hello"})[0])
        state = self.wait_for(lambda s: s["permissions"])
        self.assertTrue(state["busy"])
        permission_id = state["permissions"][0]["id"]
        self.assertIn("rawInput", state["permissions"][0]["details"])
        self.assertEqual(400, self.request("/permission", {"id": permission_id, "optionId": "bogus"})[0])
        self.assertTrue(self.bridge.snapshot()["permissions"])
        self.assertEqual(200, self.request("/permission", {"id": permission_id, "optionId": "allow"})[0])
        state = self.wait_for(lambda s: not s["busy"])
        self.assertIn('"optionId": "allow"', state["messages"][-1]["text"])
        self.assertEqual(400, self.request("/permission", {"id": permission_id, "optionId": "allow"})[0])

    def test_reconnect_is_idempotent(self):
        self.request("/connect", {})
        process = self.bridge.process
        self.request("/connect", {})
        self.assertIs(process, self.bridge.process)

    def test_rejects_parallel_prompts(self):
        self.request("/connect", {})
        self.request("/prompt", {"text": "hello"})
        self.wait_for(lambda s: s["permissions"])
        self.assertEqual(400, self.request("/prompt", {"text": "duplicate"})[0])
        self.request("/cancel", {})
        self.wait_for(lambda s: not s["busy"] and not s["permissions"])

    def test_expired_approval_is_cancelled_without_phone_polling(self):
        self.request("/connect", {})
        self.request("/prompt", {"text": "hello"})
        self.wait_for(lambda s: s["permissions"])
        with self.bridge.lock:
            permission_id = self.bridge.state["permissions"][0]["id"]
            rpc_id, params, _ = self.bridge.permission_requests[permission_id]
            self.bridge.permission_requests[permission_id] = rpc_id, params, time.monotonic() - 121
        state = self.wait_for(lambda s: not s["busy"])
        self.assertIn('"cancelled"', state["messages"][-1]["text"])
        self.assertFalse(state["permissions"])

    def test_process_exit_invalidates_session(self):
        self.request("/connect", {})
        self.request("/prompt", {"text": "crash"})
        self.wait_for(lambda s: not s["sessionId"] and not s["busy"])
        self.assertEqual(400, self.request("/prompt", {"text": "hello"})[0])
        self.assertEqual(200, self.request("/connect", {})[0])

    def test_prompt_validation_and_unknown_route(self):
        self.assertEqual(400, self.request("/prompt", {"text": ""})[0])
        self.assertEqual(400, self.request("/prompt", {"text": "x" * 32001})[0])
        self.assertEqual(404, self.request("/unknown", {})[0])
        self.assertEqual(404, self.request("/unknown")[0])

    def test_state_is_bounded(self):
        with self.bridge.lock:
            for _ in range(120):
                self.bridge.append("user", "x" * 10000)
        state = self.bridge.snapshot()
        self.assertLessEqual(len(state["messages"]), 100)
        self.assertLessEqual(sum(len(m["text"]) for m in state["messages"]), 262144)


if __name__ == "__main__":
    unittest.main()
