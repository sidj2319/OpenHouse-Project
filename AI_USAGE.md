# AI usage notes

> **Draft for you to review and finish.** The scaffold was generated with Claude. Everything marked **TODO** needs your own input or confirmation before submitting. You will be asked to defend every decision in the interview, so read the code, run it, and correct anything here that isn't true.

## Tools and models
- Claude (Sonnet 5.5) in the Claude app, using its file and shell tools to generate and run the project.
- TODO: list any other tools you used (Android Studio assistants, Copilot, etc.).

## Agent instruction files
- [`CLAUDE.md`](CLAUDE.md) is committed at the repo root. It encodes the architecture boundaries (routes -> services -> `ProductSource`/`EmailSender`; Screen -> ViewModel -> `CatalogueRepository`), validation, no-secrets and test rules.

## Plan / spec
The brief (PDF) was the spec. The plan followed from it:
1. Pick Android + Node/TypeScript. The backend is needed because the client must not call Fake Store directly.
2. Define the two boundaries that must be swappable first (`ProductSource`, `EmailSender`), then build services and routes on top.
3. Backend tests before moving to Android.
4. Android: data layer -> ViewModels (tested) -> thin Compose screens -> navigation.
5. Docs last: assumptions, cuts, growth path.

TODO: if you wrote your own plan or prompt, paste or link it here.

## Key prompts / moments that shaped the solution
TODO: replace with the 3-5 prompts you actually used. Candidates from this session:
1. "Create a project repo as per the pdf and generate only android code for this", which chose Android as the client. Backend scope was interpreted as required by the brief's "all product data must go through your backend" constraint. TODO: confirm that's what you intended.
2. Decision to put an interface in front of the product source and email, driven by the brief's line that data source, look and feel and email provider will change.
3. Decision to run backend tests (36 passing) before writing the Android client.
4. Decision to treat "top rated" as curated config with a rating-based fallback.

## Where the AI got something wrong and how it was caught
Real issues found while building (verified in this session):
- **Wrong import for the Retrofit serialization converter.** The first draft of `AppContainer.kt` imported the old `com.jakewharton...` package while `build.gradle.kts` depends on the official `com.squareup.retrofit2:converter-kotlinx-serialization`. Caught on self-review of the generated file; fixed to `retrofit2.converter.kotlinx.serialization.asConverterFactory`.
- **Missing `dp` import in `HomeScreen.kt`.** Caught on review; fixed.
- **Invalid catch parameter syntax in a test** (`catch (_: ...)`). Replaced with a named parameter.

TODO: add anything you find yourself. The Android module has **not** been compiled in the environment that generated it (no Android SDK), so expect to fix some compile errors on first build and record them here honestly.

## Time spent
TODO: record your real time, roughly split across planning / building / testing / reviewing. Do not copy generation time. Include your own time reading, running and fixing the code.

| Phase | Time |
|---|---|
| Planning | TODO |
| Building | TODO |
| Testing | TODO |
| Reviewing | TODO |
