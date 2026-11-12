# ORION — Persian AI Assistant

A Persian RTL browser app with two modes:
- **ORION Lite** (`assistant04.html`): lightweight local utilities and notes.
- **ORION AI** (`webllm_assistant.html`): WebLLM-powered in-browser model interface.

## GitHub Pages
The GitHub Actions workflow at `.github/workflows/pages.yml` deploys the repository root on every push to `main`. In repository Settings → Pages, select **GitHub Actions** as the build and deployment source.

After deployment, open the Pages URL on your phone. Use the browser menu's **Add to Home screen** option if available.

## Offline and AI limitations
The service worker caches the app shell and same-origin pages. ORION Lite uses browser local storage and works offline after the page has been cached. The WebLLM page currently loads its library from jsDelivr and fetches the model separately; first-time setup needs internet and compatible hardware/browser support. This workflow does not bundle the model or guarantee offline AI on every phone.

## Data
Chat state in the current WebLLM implementation is held in memory for the active page session. ORION Lite notes are stored in localStorage on the current browser/device. Clearing browser storage can erase local data. There is no cloud sync or multi-device account system.

## Current scope
This is a web/PWA shell around the existing assistant pages, not a hosted cloud AI service. The Lite assistant is rule-based rather than a general-purpose LLM.
