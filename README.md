# ORION — Persian personal assistant

ORION is being rebuilt as an offline-first personal assistant, starting with a browser/PWA interface and targeting a dedicated Android agent later.

## Current web foundation (v0.2)
The root page is the new ORION workspace. It currently includes:
- Persian RTL responsive interface and navigation.
- Local command-based chat (not a general-purpose LLM).
- Conversation history persisted in this browser.
- User-managed local memory entries.
- Task list with completion/deletion.
- Local notes, calculator, device date/time.
- JSON export/import backup.
- Optional OpenAI-compatible chat-completions endpoint configuration. No provider is enabled by default; users must supply a compatible endpoint/model and, if required, a key. API key is not persisted. Browser CORS restrictions may prevent direct connections.
- Optional speech recognition where supported by the browser.
- PWA shell caching for same-origin files.

## Architecture roadmap
1. **Web core:** UI, local data store, command router, task/memory modules, backup/restore.
2. **AI adapter:** pluggable local model (when device/browser supports it) and optional online provider adapter; clear source labels and explicit failure states.
3. **Agent runtime:** permission-scoped tools, action plans, confirmation gate for sensitive/irreversible actions, audit log, cancellation and recovery.
4. **Android host:** dedicated Android app wrapper/native bridge for notifications, share intents, files and approved device capabilities. A web page alone cannot safely or reliably control other Android apps.
5. **Security and reliability:** encrypted local vault, import validation, tests, accessibility and performance.

## Honest capability boundary
Not implemented yet: autonomous multi-step agent execution, native Android integration/APK, background service, push notifications, automatic semantic memory, cloud sync/accounts, bundled offline LLM, encrypted vault, or full voice assistant. The current local chat uses deterministic commands and must not be represented as a general AI model.

## Run / deploy
GitHub Actions workflow `.github/workflows/pages.yml` deploys the repository root to GitHub Pages. Open the Pages URL on Android Chrome and use **Add to Home screen** if available. First visit needs network; same-origin shell assets can then be cached. This does not make external model libraries or model weights available offline.

## Data and privacy
Current records are stored in localStorage on this browser/device. Clearing site data may erase them; export a JSON backup regularly. Do not enter secrets or sensitive personal data. API keys are held only in the active page field and are not written to localStorage, but a direct browser-to-provider connection is not a production-grade secret-handling architecture.
