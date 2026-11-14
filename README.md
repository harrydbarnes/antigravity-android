# Counterweight: native Antigravity Android companion

Counterweight uses **native Jetpack Compose chat** connected to the official
Antigravity CLI's **Agent Client Protocol (ACP)**. The phone pairs with a small
companion on your computer. It does not embed the Antigravity website, scrape
Google cookies, or claim that selecting a Google account authenticates an
Antigravity workspace.

## How Google login works

1. Sign in once to the official Antigravity CLI on your computer.
2. Start the Counterweight companion in your chosen project directory.
3. Pair Android with the companion's HTTPS address and token once.
4. On later launches, Android reconnects using its saved pairing.

`agy acp` reuses the CLI's cached Google credentials. Google tokens stay on the
computer. Android stores only the companion token, encrypted using Android
Keystore, excluded from backup and device transfer. There is no phone website
login and no Firebase or Google Cloud OAuth project to configure.

**A signed-in, reachable computer is required.** This is a desktop companion,
not a direct client for Google's hosted cloud projects or an existing desktop
conversation. The bridge starts a new ACP session within the configured directory.
It keeps that session while the companion process is running, including when the
phone disconnects. Restarting the companion starts a fresh conversation.

## Start the desktop companion

Requirements: Python 3.10+, the official Antigravity CLI with ACP support, and a
project folder. The companion has no third-party Python dependencies.

```bash
agy acp --help
# If you are not signed in, complete the official desktop login:
agy acp --login
# Exit the interactive process after sign-in, then start the companion:
python3 companion/server.py --workspace /absolute/path/to/project
```

The companion prints a pairing token and listens on `127.0.0.1:8765`. Treat this
token as remote access to the agent on your computer. It is saved in
`~/.counterweight-token`; keep it private. To revoke it, stop the server, delete
that file and restart, then pair Android again. No Google credentials are printed
or sent to Android.

### Give Android a trusted HTTPS address

Android requires HTTPS with a certificate trusted by the device. It never bypasses
certificate checks or follows HTTP redirects with the bearer token.

One private-network option is Tailscale on both devices. With HTTPS enabled in
your tailnet, publish the loopback companion to your own tailnet:

```bash
tailscale serve --bg http://127.0.0.1:8765
```

Use the HTTPS address printed by Tailscale in Android. Use **Serve**, not Funnel;
no public exposure is needed. Alternatively, put your existing HTTPS reverse proxy
in front of the loopback listener, or supply your own trusted certificate directly:

```bash
python3 companion/server.py --workspace /absolute/path/to/project \
  --host 0.0.0.0 --cert /path/to/fullchain.pem --key /path/to/privkey.pem
```

A non-loopback HTTP listener is rejected. Windows users can run the companion
with `py -3 companion/server.py ...` where Python and `agy` are available in PATH.

## On Android

- Open Counterweight, paste the HTTPS address and full pairing token, then tap
  **Pair computer**. Pairing is saved only after ACP creates a session.
- Send a prompt. Agent text appears in the native conversation.
- Approvals show the agent's tool details and exact permission choices.
  Nothing is automatically approved by this client. Your desktop agent's existing
  policies still determine which actions require approval.
- **Stop** sends ACP cancellation and cancels outstanding approval requests.
- Unanswered approvals expire after two minutes, including when Android is offline.
- If disconnected, tap **Reconnect**. Prompts are never automatically replayed.
- **Forget computer** removes the local pairing. It does not sign Google out of
  your desktop or revoke a token copied elsewhere.

The live UI deliberately does not expose the repository's old sample cloud
projects, workstations, artifacts or fake-success tool actions. Those legacy
components remain in source, outside the launched native path. The client does
not yet support attachments, session browsing, cloud agents, model selection or
background notifications. ACP client-side filesystem and terminal capabilities
are not advertised; the CLI owns its own tools.

## Build and verification

JDK 17, Android SDK 34, and network access to Gradle/Maven repositories are required.

```bash
python3 -m unittest discover -s companion/tests -v
./gradlew testDebugUnitTest assembleDebug
```

The companion tests run a deterministic **ACP subprocess fixture**, exercising
initialisation, session creation, streaming, permissions, cancellation, process
failure, pairing authentication and reconnection. They do not contact Google or
prove that a particular installed CLI release behaves correctly. GitHub Actions
runs the companion tests plus the Android build and existing unit tests.

Live acceptance check: run the real signed-in CLI, pair a physical phone over
trusted HTTPS, ask for a response, exercise an approval and rejection, then
restart Android and confirm it reconnects to the same companion session. Also
check cancellation, revoked tokens, offline recovery and companion restart.

## Official references

- [Antigravity ACP and cached authentication](https://antigravity.google/docs/cli/acp)
- [ACP v1 protocol](https://agentclientprotocol.com/protocol/v1/initialization)
- [Tailscale Serve](https://tailscale.com/kb/1242/tailscale-serve)
