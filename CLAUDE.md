# Project instructions for AI agents

Product catalogue PoC: Android (Kotlin/Compose) client + Node/TypeScript backend.
It must grow beyond a PoC, so keep the boundaries below intact.

## Layout
- `backend/` Express + TypeScript. `npm test`, `npm run typecheck`, `npm run dev`.
- `android/` Kotlin + Jetpack Compose. `./gradlew testDebugUnitTest`, `./gradlew installDebug`.

## Architecture rules
- The Android app talks ONLY to our backend, never to the Fake Store API.
- Backend: routes -> services -> `ProductSource` / `EmailSender` interfaces.
  Concrete implementations (Fake Store, SMTP) are wired in `backend/src/server.ts` only.
  Services and routes must not import a concrete provider.
- Backend returns the catalogue's own `Product` model; upstream shapes stop in the provider.
- Android: Screen -> ViewModel -> `CatalogueRepository` interface. Screens never touch Retrofit.
  Look and feel lives in `ui/theme`; screens use MaterialTheme tokens, not hard-coded colours.
- Wire models on Android mirror the backend JSON; change both sides together.

## Conventions
- Validate all external input with zod on the backend; the client validates only for UX.
- User-facing errors are friendly messages; never leak internals or stack traces.
- No secrets in the repo. New config goes in `backend/.env.example`.
- No paid services. Everything must run locally (docker compose + emulator).
- Every behaviour change needs a test. Backend: vitest. Android: JUnit with fake repositories.
- Keep it small: no new dependency without a stated reason in DECISIONS.md.
