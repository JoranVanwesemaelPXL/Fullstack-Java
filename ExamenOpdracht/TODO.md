# TODO – Maaltijdboxen (Angular-frontend + Spring Cloud-backend)

Vink af met `[x]` zodra iets af én getest is.
Legende: 🔴 dringend · 🟡 deze/volgende week · ⚪ later/extra

---

## 📅 Deadlines

| Wat | Wanneer |
|---|---|
| Frontend verdediging 1 (min. 1 US af) | 🔴 week van **23/11** |
| Frontend verdediging 2 | week van 7/12 |
| Frontend verdediging 3 | week van 4/1 |
| Backend: week 9 → 14 | zie planning hieronder (nu: **week 9**) |

---

## 🔗 API-afspraken (contract frontend ↔ backend)

> Leg dit vast in `openapi.yaml` (bevroren in week 10). Alle paden lopen via de gateway: `http://localhost:8080/api`.
> Headers bij elk request: `X-User` (gebruikersnaam) en `X-Role` (`KLANT` | `MEDEWERKER` | `CHAUFFEUR`).
> Fout-body altijd: `{ "code": "OUT_OF_STOCK", "message": "leesbare tekst" }`

| US | Methode + pad | Service | Antwoord |
|---|---|---|---|
| US1 | `GET /api/recipes` (huidige week, optioneel `?week=2026-W48`) | recipe | 200 `RecipeDto[]` |
| US1 | `GET /api/recipes/{id}` | recipe | 200 / 404 |
| US2 | `GET /api/delivery-slots` | delivery | 200 `DeliverySlotDto[]` (met vrije plaatsen) |
| US2 | `POST /api/orders` | order | 201 / 400 / **409** (voorraad of slot) |
| US3 | `GET /api/orders` (eigen bestellingen via `X-User`) | order | 200 `OrderDto[]` |
| US6 | `POST /api/orders/{id}/cancel` | order | 200 / 404 / 409 (al onderweg) |
| US4 | `GET /api/stock?low=true` | inventory | 200 `StockItemDto[]` |
| US4 | `PATCH /api/stock/{id}` (aanvullen) | inventory | 200 / 400 / 404 |
| US5 | `GET /api/delivery-runs/mine` | delivery | 200 `DeliveryRunDto[]` |
| US5 | `POST /api/delivery-runs/{runId}/orders/{orderId}/delivered` | delivery | 200 / 404 |

**RecipeDto** (al gebruikt in de frontend, zie `core/models/recipe.model.ts`):
```json
{ "id": 1, "name": "...", "description": "...", "prepTimeMinutes": 35, "servings": 2,
  "allergens": ["melk"], "ingredients": [{ "ingredientId": 1, "name": "arborio", "quantity": 90, "unit": "g" }],
  "imageUrl": null, "week": "2026-W48" }
```

---

# 🅰️ FRONTEND (Angular)

## Basis / infrastructuur
- [x] `environments/` (`environment.ts` + `environment.development.ts`) + `fileReplacements` in `angular.json`
- [x] `provideHttpClient(withInterceptors([authInterceptor]))` in `app.config.ts`
- [ ] `npm install @ngrx/signals` uitvoeren 🔴
- [x] Mock-backend (`npm run mock`, poort 3000) – US1 endpoints
- [ ] Mock-backend uitbreiden voor US2, US3, US6
- [x] Modellen: `Recipe`, `User/Role`, `ApiError`
- [ ] Modellen: `Order`, `OrderLine`, `OrderStatus`, `DeliverySlot`
- [x] Bugfix: link `/orders/new` → `/new-order` in orders.html
- [x] Bugfix: ongeldige `/* */` comment in `<img>` op welkompagina
- [ ] Hero-afbeelding lokaal zetten in `public/` (externe URL kan verdwijnen) ⚪
- [ ] Gedeelde navbar-component (nu enkel nav op welkompagina) ⚪

## Authenticatie
- [x] `AuthService` met signals (gebruiker + rol, bewaard in localStorage)
- [x] Loginscherm: naam + rol kiezen (KLANT / MEDEWERKER / CHAUFFEUR)
- [x] `authGuard` (→ `/login?returnUrl=...`) en `roleGuard(...)`
- [x] `authInterceptor` stuurt `X-User` en `X-Role` mee
- [x] Uitloggen + naam/rol tonen in de navbar

## User stories – Deel 1 (klant)
- [x] **US1 – Weekmenu**: `RecipeService` + `RecipeStore` (SignalStore) + menupagina
      (kaarten met bereidingstijd, personen, allergenen, ingrediënten; allergenenfilter; laad-/fout-/leegstatus) 🔴 *testen tegen mock*
- [ ] **US2 – Box bestellen**: `DeliverySlotService`/`OrderService` + stores; recepten + personen + slot kiezen; `POST /orders`; 409 meteen tonen
- [ ] **US3 – Mijn bestellingen**: lijst met statusbadge (PLACED, CONFIRMED, IN_DELIVERY, DELIVERED, CANCELLED)
- [ ] **US6 – Annuleren**: knop enkel bij PLACED/CONFIRMED; store bijwerken na annuleren

## Deel 2 – Tests
- [x] Eerste unittests: `AuthService`, `RecipeService`, `App`
- [ ] Unittests: `RecipeStore`, guards, interceptor, componenten (Login, Menu, …)
- [ ] Code-coverage tonen (`ng test --coverage`)
- [ ] Playwright e2e-tests (login → menu → bestellen → annuleren)
- [ ] e2e-coverage via monocart

## Deel 3 – Extra's (elk 1pt) ⚪
- [ ] US4 (medewerker: voorraad) en US5 (chauffeur: leverronde) in de frontend
- [ ] US1 + US2 responsive (laptop + gsm) met Tailwind
- [ ] Optimistic update (bv. annuleren) + tekstdocument met rollback-stappen
- [ ] Access-token + refresh-token (rotation)
- [ ] Angular Material (minstens US1 + US2)
- [ ] Docker-container met HTTPS
- [ ] i18n (US1 + US2)
- [ ] Eigen technologie voorstellen + document

---

# ☕ BACKEND (Spring Cloud)

Spring Boot 4.0.8 · Spring Cloud 2025.1.3 · Java 21 · eigen GitHub-repo, regelmatig committen

## Week 8 – Analyse
- [ ] `README.md` (services, waarom die grenzen, opstarten, vereenvoudigingen) – *startversie staat er, in eigen woorden herschrijven*
- [ ] `BESLISSINGEN.md` met eerste beslissing: de servicegrenzen – *startversie staat er, in eigen woorden herschrijven*
- [ ] ⚠️ Bestanden staan in `SpringCloud-Backend/`, niet in de repo-root → melden op het GitHub-issue van het controlescript
- [ ] Domeinmodel per service + schets API-paden (zie tabel hierboven)
- [ ] Beslissen: aparte customer-service ja/nee (en wat dupliceer je in order-service?)

## Week 9 – De services (US1 + US4 werken) 🔴 ← **nu**
- [x] Multi-module `pom.xml` in de root (groupId `be.foodexpress`)
- [x] `docker-compose.yml` met 4 PostgreSQL-databases (poorten 5433–5436)
- [ ] 5 modules genereren in IntelliJ (Spring Initializr)
- [ ] `recipe-service` – Recipe, Ingredient – eigen DB – **US1**: `GET /api/recipes`
- [ ] `order-service` – Order, OrderLine (+ status-enum)
- [ ] `inventory-service` – StockItem – **US4**: lage voorraad + aanvullen
- [ ] `delivery-service` – DeliverySlot, DeliveryRun
- [ ] `notification-service` – geen DB, enkel loggen
- [ ] CRUD per service met **DTO's** (geen entities over de lijn) + mappers
- [ ] `@RestControllerAdvice` met vast foutobject + statuscodes (201/200/404/400/409)
- [ ] `X-User`/`X-Role` header uitlezen
- [ ] Seed-data (bv. `data.sql`) zodat het weekmenu niet leeg is
- [ ] `BESLISSINGEN.md` bijwerken

## Week 10 – Testen & contract
- [ ] Testklassen per service, Testcontainers in pom, nergens `@Disabled`
- [ ] `mvn test` groen
- [ ] `openapi.yaml` in de root (bevroren)
- [ ] git tag `week10-spec`

## Week 11 – Discovery & Feign (US2)
- [ ] `discovery-service` (Eureka server) + Eureka-client in elke service
- [ ] `@FeignClient` order → inventory (voorraad check, **synchroon**), zonder localhost-URL
- [ ] Feign order → delivery (slot vrij?) 
- [ ] US2: bestelling geweigerd met 409 + duidelijke fout bij te weinig voorraad / vol slot
- [ ] US3: eigen bestellingen ophalen
- [ ] US6: annuleren zolang niet IN_DELIVERY

## Week 12 – Config & gateway
- [ ] `config-service` (centrale configuratie)
- [ ] `api-gateway` – alle routes op één adres, onbekend pad → 404
- [ ] CORS op de gateway (`http://localhost:4200`)
- [ ] Frontend `environment.development.ts` → `http://localhost:8080/api`
- [ ] git tag `week12-api`

## Week 13 – Observability & messaging
- [ ] RabbitMQ (AMQP) in pom
- [ ] Event `order.confirmed` → 3 luisteraars: inventory (reserveren), delivery (inplannen), notification (loggen)
- [ ] Bestelling slaagt ook als notification-service plat ligt (testen!)
- [ ] Event bij annuleren → voorraad komt vrij (US6)
- [ ] US5: chauffeur markeert geleverd → status DELIVERED
- [ ] Resilience4j circuit breaker + fallback op minstens één Feign-call
- [ ] Micrometer Tracing (+ Zipkin): één traceId door de keten, ook door de queue

## Week 14 – Afwerken
- [ ] Dockerfile per service + `docker-compose.yml` (`docker compose up -d`)
- [ ] springdoc-openapi; Swagger UI op de gateway toont alle services
- [ ] JaCoCo, coverage ≥ 70%
- [ ] README + BESLISSINGEN af
- [ ] git tag `week14-final`

## ✅ Eindcontrole backend ("Wanneer is het af?")
- [ ] 5 services, 5 databases, allemaal in Eureka
- [ ] Alles via één gateway-adres; onbekend pad = 404
- [ ] Bestelling met te weinig voorraad wordt geweigerd met duidelijke fout
- [ ] Bevestigde bestelling triggert 3 consumers via de broker
- [ ] notification-service stoppen → bestelling slaagt nog
- [ ] Eén traceId door de hele keten (ook queue)
- [ ] `mvn test` groen zonder `@Disabled`
- [ ] `docker compose up -d` brengt alles omhoog
- [ ] Swagger UI op gateway toont alle services
- [ ] Testcoverage ≥ 70%
