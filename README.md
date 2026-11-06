# Product Catalogue PoC

A product catalogue where customers browse, search and submit enquiries. Built as the foundation for a real product, not a throwaway prototype.

| Layer | Choice | Why |
|---|---|---|
| Client | **Android** (Kotlin, Jetpack Compose) | Native mobile; Compose keeps UI code small and the theme swappable |
| Backend | **Node.js + TypeScript** (Express) | Thin API; strong typing end to end; easy to containerise for AWS (ECS/Lambda) |

```
android/   Kotlin app          ->  talks only to ->  backend/   Node API  ->  Fake Store API
                                                                      \->  SMTP (Mailpit locally)
```

## Run it (about 5 minutes)

### Prerequisites
- Docker (for the backend), **or** Node 20+
- Android Studio (Koala or newer) with JDK 17 and an Android Emulator (API 26+)

### 1. Start the backend
```bash
docker compose up --build
```
- API: http://localhost:3000 (try http://localhost:3000/api/home)
- Enquiry emails land in Mailpit: http://localhost:8025

Without Docker:
```bash
cd backend
cp .env.example .env
npm install
npm run dev        # emails are printed to the console when SMTP_HOST is empty
```

### 2. Run the Android app
1. Open the `android/` folder in Android Studio and let Gradle sync. Android Studio downloads Gradle 8.7 from `gradle/wrapper/gradle-wrapper.properties`.
2. Start an emulator and press Run.

The emulator reaches your machine at `10.0.2.2`, which is the default `apiBaseUrl` in `android/gradle.properties`. For a physical device, use your computer's LAN IP:
```bash
cd android && ./gradlew installDebug -PapiBaseUrl=http://192.168.1.20:3000/
```
If `./gradlew` is missing, run `gradle wrapper --gradle-version 8.7` once in `android/`, or just use Android Studio.

### 3. Try the acceptance criteria
| Criterion | How to see it |
|---|---|
| Home shows 5 top rated products | Launch the app |
| Category page: top rated + more | Tap a category chip |
| Search | Type a term, tap **Search** |
| Product details | Tap any product tile |
| Enquiry | Product page -> **Enquire** -> submit -> "Thank you" screen; open http://localhost:8025 to see the email |

## Tests
```bash
cd backend && npm test             # 36 tests: service logic, HTTP API, upstream adapter
cd android && ./gradlew testDebugUnitTest   # ViewModels, validation, error mapping
```

## Configuration
All backend config is environment-driven; see `backend/.env.example` (no secrets are committed).

| Variable | Purpose |
|---|---|
| `FEATURED_PRODUCT_IDS` | The 5 curated "top rated" home page products |
| `SALES_INBOX_EMAIL` | Where enquiries are sent |
| `SMTP_HOST` / `SMTP_PORT` / `SMTP_USER` / `SMTP_PASS` | Email provider (empty host = log to console) |
| `FAKE_STORE_BASE_URL`, `PRODUCT_CACHE_TTL_SECONDS` | Product source |

## API
| Endpoint | Purpose |
|---|---|
| `GET /api/home` | Featured products |
| `GET /api/categories` | Category names |
| `GET /api/categories/:name` | `{ topRated, others }` for a category |
| `GET /api/products?q=` | Search |
| `GET /api/products/:id` | Product details |
| `POST /api/enquiries` | Send an enquiry to the sales inbox |

## Docs
- [DECISIONS.md](DECISIONS.md): assumptions, cuts, growth path, next steps
- [AI_USAGE.md](AI_USAGE.md): how AI tools were used
- [CLAUDE.md](CLAUDE.md): agent instructions used while building
