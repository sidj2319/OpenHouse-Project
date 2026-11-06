# Product catalogue PoC: Android client + Node/TypeScript backend

Paste this as the PR description (feature branch -> `main`). Leave the PR open.

## What this does
Customers can see 5 featured products, browse categories (top rated first, then the rest), search, view product details and submit an enquiry that emails the sales inbox and shows a Thank-you screen.

## Approach
- **Android (Kotlin/Compose)** talks only to our backend. **Node/TypeScript backend** wraps the Fake Store API.
- Two seams for the things the brief says will change: `ProductSource` (data) and `EmailSender` (email). Theme lives in one file.
- Screens -> ViewModels -> `CatalogueRepository` interface, so UI is unit-testable without a network.

## Assumptions and cuts
See [DECISIONS.md](DECISIONS.md). Highlights: featured products are curated via config with a rating fallback; search is AND across title/description/category; enquiries are email-only (not persisted); no pagination or UI tests.

## How to run
See [README.md](README.md): `docker compose up --build`, then run `android/` on an emulator. Enquiry emails are visible at http://localhost:8025.

## Testing
- Backend: 36 vitest tests (service logic, HTTP API incl. validation and failure paths, upstream adapter caching and stale fallback), all passing.
- Android: JUnit tests for ViewModels, validation and error mapping.
- TODO: state honestly whether you built and ran the Android tests and the app on an emulator, and what you found.

## AI usage
See [AI_USAGE.md](AI_USAGE.md).

## Known gaps / risks
- Enquiries are lost if SMTP is down (no queue or persistence yet).
- Android module needs a first build on your machine; see AI_USAGE.md.
