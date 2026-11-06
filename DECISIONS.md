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

## Requirements dropped or simplified
- **No persistence of enquiries.** They only go to email. Cheap to add via a repository, but not required by the criteria, and a database adds setup for the reviewer. If SMTP is down the enquiry is lost, which is the biggest known gap (see next steps).
- **No pagination.** Fake Store has 20 products. The service and API are shaped so it can be added.
- **No instrumented/UI tests on Android.** Time-box. Logic lives in ViewModels and a validator, which are unit-tested; composables are thin.
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
