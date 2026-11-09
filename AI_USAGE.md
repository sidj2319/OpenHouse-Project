# AI usage notes

## Tools and models
- Claude (Sonnet 5.5) in the Claude app, using its file and shell tools to generate and run the project.
- Android Studio assistants, used for the Android side (editing, build errors, Gradle sync issues).

## Plan / spec
The brief (PDF) was the spec. The plan followed from it:
1. Pick Android + Node/TypeScript. The backend is needed because the client must not call Fake Store directly.
2. Define the two boundaries that must be swappable first (`ProductSource`, `EmailSender`), then build services and routes on top.
3. Backend tests before moving to Android.
4. Android: data layer -> ViewModels (tested) -> thin Compose screens -> navigation.
5. Docs last: assumptions, cuts, growth path.

## Key prompts / moments that shaped the solution

### Project setup: backend and Android in one repo
**1. Prompt:** "Create a project repo as per the brief. Put the Node.js/TypeScript backend in `backend/` and the Android app in `android/`, both in the same Git repo, with one README, a `docker-compose.yml` to run the backend locally, a `.gitignore` and a `.env.example`. The backend wraps the Fake Store API behind a `ProductSource` interface, and sends enquiry emails through an `EmailSender` interface so the data source and email provider can change."
- **Shaped:** the single-repo layout, the two swappable seams the brief calls out, and the rule that the Android app talks only to our backend.
- **Reviewed:** I checked that routes and services import only the interfaces, and that the concrete implementations are wired in one place (`backend/src/server.ts`).

### Android: Compose screens and ViewModels
**2. Prompt:** "Create the Android screens in Jetpack Compose and a ViewModel for each, as the acceptance criteria require: Home (5 featured products), Category (top rated, then more), Search results, Product details, and the Enquiry form with a Thank-you message. Each ViewModel exposes a `StateFlow` of `Loading` / `Error` / `Success` and talks to a `CatalogueRepository` interface, not to Retrofit. Keep colours and typography in one theme file."
- **Shaped:** thin composables, one consistent UI state pattern with a Retry button, and a single place to restyle the app when the creative designs arrive (`ui/theme/Theme.kt`).
- **Reviewed:** I checked that no screen touches the network layer directly, and that the enquiry form shows "Thank you" only after the backend confirms success.

### Unit tests
**3. Prompt:** "Write JUnit tests for the Android ViewModels with a fake `CatalogueRepository` and a test dispatcher. Cover loading, success, error and retry for Home; the enquiry form's validation, trimmed values, failed submission keeping the typed form, and error clearing on edit; and the repository error mapping, including that coroutine cancellation is rethrown."
- **Shaped:** tests depend only on the repository interface, so changing the backend client does not break them.
- **Note:** the backend has 36 vitest tests that pass. The Android module was generated in an environment without the Android SDK, so its tests are run from Android Studio with `./gradlew testDebugUnitTest`.

### Debugging
**4. Prompt:** "npm : The term 'npm' is not recognized as the name of a cmdlet, function, script file, or operable program..."
- **Cause:** Node.js was not installed on my Windows machine. The AI explained how to install it without Chocolatey (official installer, winget, or portable zip). Node 20+ is a prerequisite for running the backend without Docker.

**5. Prompt:** "I'm seeing it on the link but it's not working on android app"
- **Cause:** the backend worked in the browser, so the problem was the Android side reaching it. The emulator must use `10.0.2.2`, a real phone needs the PC's LAN IP added to both `gradle.properties` and `network_security_config.xml`, and Gradle must be re-synced after changing the URL.

**6. Prompt:** "I ran npm install and got '8 vulnerabilities (3 moderate, 3 high, 2 critical)' and an esbuild install-scripts warning. What does this mean?"
- **Outcome:** the AI ran `npm audit` rather than guessing. The findings were in dev tooling (`vitest`, `vite`, `esbuild`, `tinypool`) plus runtime packages `nodemailer` and `zod`. I decided **not** to run `npm audit fix --force` because it would upgrade across major versions and could break the code and tests, and I recorded dependency upgrades as a next step instead.

## Where the AI got something wrong and how it was caught
- **Wrong import for the Retrofit serialization converter.** The first draft of `AppContainer.kt` imported the old `com.jakewharton...` package while `build.gradle.kts` depends on the official `com.squareup.retrofit2:converter-kotlinx-serialization`. Caught on review of the generated file; fixed to `retrofit2.converter.kotlinx.serialization.asConverterFactory`.
- **Missing `dp` import in `HomeScreen.kt`.** Caught on review; fixed.
- **Invalid catch parameter syntax in a test** (`catch (_: ...)`). Replaced with a named parameter.
- **Android code was never compiled where it was generated** (no Android SDK there), so compile errors were expected on the first build in Android Studio and fixed there.

## Time spent
| Phase | Time |
|---|---|
| Planning | 3 hours |
| Building | 18 hours |
| Testing | 4 hours |
| Reviewing | 3 hours |
| **Total** | **28 hours** |

This is well over the suggested 2-3 hour time-box. The extra time went mostly on building and learning the Android and Node setup, and getting the app to build and talk to the backend.