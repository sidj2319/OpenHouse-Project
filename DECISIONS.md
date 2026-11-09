# Decisions and assumptions

## Assumptions (where the acceptance criteria were unclear)

| Gap | Assumption |
|---|---|
| "I have chosen 5 products to be top rated" | The entrepreneur curates them via `FEATURED_PRODUCT_IDS` (config, in display order). If fewer than 5 valid IDs are given, the gap is filled with the highest-rated products so the home page is never short. A real admin UI would replace the config later. |
| "Top rated products for that category" | Fake Store has no curation per category, so the top 3 by rating (then review count) lead the page; the rest follow as "More in ...". |
| Search "text matching that search term" | Case-insensitive, matches title, description and category. Multiple words must all match (AND), so "red backpack" narrows rather than widens. Blank searches are rejected (400); the Search button is disabled when blank. |
| Search is submitted by a button | The Search button and the keyboard Search action both submit. No search-as-you-type. |
| Enquiry form fields | Name and email required; phone and message optional. Limits: name 100, message 1000 chars. |
| "Sales inbox receives an email" | One plain-text email to `SALES_INBOX_EMAIL`, `Reply-To` set to the customer so sales can answer directly. No confirmation email to the customer. |
| "Thank you" only after success | The app shows it only after the backend confirms the email was handed to the provider. If sending fails the customer sees an error and keeps their form. |
| Authentication | None. Browsing and enquiring are public. |
| Currency | The Fake Store API gives bare numbers; shown as USD. |
| Platforms | Android only, minSdk 26, portrait phone layout. Tablet and landscape polish not attempted. |

## Android client: decisions and assumptions

### Stack choices
| Area | Choice | Why |
|---|---|---|
| Language / UI | Kotlin, Jetpack Compose, Material 3 | Less boilerplate than XML views, and the theme is a single swappable file. |
| Architecture | Screen (composable) -> ViewModel -> `CatalogueRepository` interface -> Retrofit API | Each layer has one job. Screens hold no logic, ViewModels hold state and rules, the repository hides the network. |
| State | `StateFlow` per ViewModel, exposed as `UiState` (`Loading` / `Error` / `Success`) | One small, consistent pattern on every screen, so loading, error and retry behave the same everywhere. |
| Networking | Retrofit + OkHttp + kotlinx.serialization | Standard, small, and `ignoreUnknownKeys` lets the backend add fields without breaking the app. |
| Images | Coil | Compose-native; handles caching and loading without custom code. |
| Navigation | Navigation Compose, one route per screen (home, category, search, product, enquiry) | Back stack and the system back button work without extra code. |
| Dependency injection | Hand-written `AppContainer` | Five screens do not justify Hilt. It can be swapped in later without touching screens. |
| SDK levels | minSdk 26, target/compile 34 | Covers the vast majority of active devices and avoids legacy workarounds. |

### Assumptions
| Gap | Assumption |
|---|---|
| Screen set | Five screens map one-to-one to the acceptance criteria: Home, Category, Search results, Product details, Enquiry (the form turns into the Thank-you view in place). |
| Home layout | Category chips in a row, then the 5 featured products ("Top rated") in a 2-column grid. The search bar sits above, so search is reachable from home. |
| Category page | Two sections: "Top rated in X" then "More in X". The "More" section is hidden when the category has no extra products. |
| Search | Results open on their own screen. The search bar stays visible there so the customer can refine the term. An empty result shows "No products match ...". |
| Product tile | Image, title (2 lines max), price and rating. Tapping anywhere on the tile opens details. |
| Enquire flow | The Enquire button is on the product page. The form shows the product title so the customer knows what they are enquiring about. If the title fails to load, the form still works. |
| Validation | The app validates name, email, phone and message length for fast feedback, but the backend re-validates everything and is the source of truth. Rules are kept in sync by hand (noted in `EnquiryValidator`). |
| Submit behaviour | The button is disabled while sending to prevent double-submits. The Thank-you view appears only after the backend confirms success. On failure the customer sees an error and keeps what they typed. |
| Rotation / process death | ViewModels survive rotation, so form input and loaded data are kept. Full recovery after the system kills the app is not handled: the user returns to the home screen. |
| Language / formatting | English only, prices shown as USD (`$12.99`), category names title-cased for display (`men's clothing` -> `Men's Clothing`). |
| Server address | Set at build time through `apiBaseUrl` in `android/gradle.properties`, defaulting to `10.0.2.2:3000` (the emulator's alias for the host PC). A real phone needs the PC's LAN IP. |
| Offline | Not supported. A failed request shows a clear message and a "Try again" button. |

### Android-specific trade-offs
- **ViewModel per navigation entry, no shared cache.** Going back to home refetches. This costs an extra request but avoids stale data and cache-invalidation logic. The backend caches the upstream, so it is cheap.
- **Customer-facing errors are mapped in one place** (`safeCall` in the repository): network failure, 400/404/429/5xx and unknown errors each get a friendly message. Raw exception text is never shown, so no internals leak. Coroutine cancellation is deliberately re-thrown, not swallowed.
- **Cleartext HTTP is allowed only for `10.0.2.2` and `localhost`** through the network security config. All other traffic must be HTTPS. For a LAN IP during device testing the address must be added to that file, and the exception removed once the backend has TLS.
- **No design system beyond Material 3 defaults.** The creative work is not available yet, so I kept visual decisions minimal and isolated in `ui/theme/Theme.kt` and `ui/common`.
- **Search is a plain submit, not as-you-type.** It matches the acceptance criteria ("click the search button") and avoids debouncing and request cancellation logic.

### Testing approach on Android
- Unit tests (JUnit, fake repository, no emulator) cover: Home, Category and Search ViewModels, the enquiry ViewModel (validation, trimming, success, failure keeping the form, error clearing on edit), the validator, and the repository's error mapping.
- Composables are kept thin on purpose so most behaviour is testable without a UI framework.
- **Honest status:** I generated this module in an environment without the Android SDK, so I did not compile it or run its tests there. Treat the first Android Studio build as part of the review and see `AI_USAGE.md` for any fixes made.

## Requirements dropped or simplified
- **No persistence of enquiries.** They only go to email. Cheap to add via a repository, but not required by the criteria, and a database adds setup for the reviewer. If SMTP is down the enquiry is lost, which is the biggest known gap (see next steps).
- **No pagination.** Fake Store has 20 products. The service and API are shaped so it can be added.
- **No instrumented/UI tests on Android.** Time-box. Logic lives in ViewModels and a validator, which are unit-tested; composables are thin.
- **No state restoration after process death, no tablet/landscape layouts, no localisation, no dark-theme polish** on Android. None are in the acceptance criteria.
- **No CI, no deployment config.** The Dockerfile is AWS-compatible (runs as-is on ECS/Fargate), but I did not write Terraform/CDK.
- **No image caching tuning or offline mode.** Coil's defaults apply.
- **Basic accessibility only** (content descriptions on images). No full audit.

## How the design supports growth beyond the PoC
- **Product data source will change** -> `ProductSource` interface; `FakeStoreProductSource` is the only class that knows the upstream shape and validates it with zod (an anti-corruption layer). Swap in a database or ERP by implementing two methods and changing one line in `server.ts`. The service does filtering and ranking in memory, which is fine for tens of products; with a real store that logic moves behind the interface.
- **Email provider will change** -> `EmailSender` interface; SMTP and a console logger exist today. SES/SendGrid is one new class.
- **Look and feel will change** -> all colours and typography live in `ui/theme/Theme.kt`; screens use `MaterialTheme` tokens. Shared pieces (`ProductCard`, `SearchBar`, `UiStateContent`) are in `ui/common`.
- **Backend can change under the app** -> screens depend on ViewModels, which depend on a `CatalogueRepository` interface. Retrofit is an implementation detail, and ViewModel tests use a fake repository.
- **API contract** -> every response is a JSON object (e.g. `{ featured: [...] }`, not a bare array) so fields can be added without breaking the app.
- **Resilience** -> the Fake Store source caches (TTL configurable), serves stale data if a refresh fails, and times out after 5 seconds.
- **Security basics** -> zod validation on every input, 10kb body limit, `helmet`, rate limiting on enquiries, plain-text emails only, newlines stripped from subject lines, friendly errors with no internals leaked, cleartext HTTP allowed on Android only for `10.0.2.2`/`localhost`.

## Trade-offs worth knowing
- **Backend wraps the full product list per request** (cached) instead of calling Fake Store's category endpoint. Simpler and consistent; wasteful at scale.
- **Manual DI on Android** (`AppContainer`) rather than Hilt. Fewer moving parts for 5 screens; easy to migrate later.
- **Express 4**, chosen for familiarity and stability.

## What I would do next with another day
1. Persist enquiries (Postgres or DynamoDB) and send email from a retry queue so no enquiry is lost if SMTP fails.
2. Compose UI tests for the enquiry flow and a backend contract test against the real Fake Store API.
3. Pagination and server-side filtering behind `ProductSource`.
4. Apply the creative team's design: tokens in `Theme.kt`, then component polish.
5. CI (backend tests + `testDebugUnitTest`), a deployed backend on AWS with TLS, and removal of the cleartext exception.
6. Structured logging with request IDs, and a customer confirmation email.
7. An admin way to choose featured products instead of an env var.
8. Android: restore state after process death (`SavedStateHandle`), a short-lived in-memory cache so back navigation is instant, Hilt if the app grows past a handful of screens, accessibility pass (TalkBack, touch targets), and a release build config with minification and signing.