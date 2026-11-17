# Maaltijdboxenaanhuis: plan van aanpak

Backend (Spring Boot 4.0.8, Spring Cloud 2025.1.3, Java 21) voor een dienst die wekelijkse maaltijdboxen samenstelt en levert, met een eigen Angular-frontend als afnemer.

> **Status:** week 8, analyse. Er is nog geen code; dit document is het plan.
> Beslissingen en hun reden staan in [BESLISSINGEN.md](BESLISSINGEN.md).

```
examenopdracht/
├── architectuur/workspace.dsl   <- Structurizr-model (bron van alle diagrammen)
├── frontend/                    <- Angular-applicatie
├── backend/                     <- Spring Cloud-backend (multi-module)
├── README.md                    <- dit plan
└── BESLISSINGEN.md
```

---

## 1. Architectuurschets

Het diagram staat als code in [`architectuur/workspace.dsl`](architectuur/workspace.dsl) (Structurizr DSL). Het bevat vier views:

| View | Wat je ziet |
|---|---|
| `Architectuur-volledig` | Alle containers, inclusief discovery en config |
| `Architectuur-functioneel` | Zonder discovery en config: wie praat functioneel met wie |
| `US2-bestellen` | Dynamic view van de belangrijkste flow |
| `US5-leveren` | Dynamic view van de afwerking van een leverronde |

**Bekijken met Structurizr Lite** (poort 8089, want 8080 is voor de gateway):

```bash
docker run -it --rm -p 8089:8080 -v "$(pwd)/architectuur:/usr/local/structurizr" structurizr/lite
# open http://localhost:8089
```

Exporteer de views als PNG vanuit de Lite-interface (Export > PNG) en zet ze in `architectuur/img/`, zodat ze ook zonder Docker leesbaar zijn:

![Functioneel overzicht](architectuur/img/Architectuur-functioneel.png)

**Legende**

| Pijl | Betekenis |
|---|---|
| Doorgetrokken, label `Feign (sync)` | Synchrone call, we hebben het antwoord nodig |
| Label `AMQP (async)` | Asynchroon via RabbitMQ, mag nooit een bestelling doen mislukken |
| Gestippeld grijs | Infrastructuur (Eureka-registratie, config ophalen) |

**Principes**
1. Het frontend praat **alleen** met de gateway. Enkel paden onder `/api/**` worden gerouteerd; al het andere geeft 404.
2. Elke service heeft zijn eigen database. Services verwijzen naar elkaar **enkel via id's**, nooit via foreign keys of gedeelde tabellen.
3. Een vraag waarvan we het antwoord nodig hebben om verder te kunnen is synchroon (Feign). Alles wat na de bevestiging gebeurt is asynchroon.
4. Interne endpoints (`/internal/**`) zijn bedoeld voor service-naar-service calls en worden niet door de gateway doorgegeven.

---

## 2. Services en grenzen

| Service | Bezit (eigen DB) | Waarom deze grens |
|---|---|---|
| **recipe-service** | `Recipe`, `Ingredient`, `RecipeIngredient` (hoeveelheid per basisaantal personen) | Het aanbod verandert wekelijks en door andere mensen dan voorraad of bestellingen. Het is ook de enige bron van ingrediënt-id's. |
| **order-service** | `Order`, `OrderLine` | De kern: levenscyclus en status van een bestelling. Kent van de anderen enkel id's en snapshots. |
| **inventory-service** | `StockItem`, `StockReservation` | Voorraad heeft een eigen ritme (aanvullen, opraken) en moet atomair reserveren. `StockReservation` (orderId, ingredientId, hoeveelheid) komt erbij om bij annuleren precies te weten wat vrijkomt. |
| **delivery-service** | `DeliverySlot`, `DeliveryRun`, `DeliveryStop` | Capaciteit van tijdsloten en ritten is een eigen probleem. `DeliveryStop` (orderId, adres, status) komt erbij om bestellingen in een rit te kunnen afvinken. |
| **notification-service** | niets | Pure consument. Als hij plat ligt mag niets anders merken. |

Infrastructuur: **discovery-service** (Eureka), **config-service**, **gateway**, **RabbitMQ**.

### Wat order-service dupliceert (en waarom)

Er is **geen customer-service**. Een klant is in deze case niet meer dan een id en wat naam- en adresgegevens, en er is geen eigen beheer van klanten.

| Gedupliceerd in `Order` | Reden |
|---|---|
| `customerId` (uit header `X-User-Id`) | Is de sleutel om "mijn bestellingen" op te halen |
| `customerName`, `deliveryAddress` (snapshot op moment van bestellen) | Een bestelling moet later nog kloppen, ook als een adres wijzigt. Delivery krijgt het adres via het event, niet via een call. |
| `recipeName` in `OrderLine` (snapshot) | De bestellijst moet tonen wat er besteld is, ook als het recept volgende week verdwijnt, en zonder dat een call naar recipe-service nodig is |

Wat order-service **niet** dupliceert: voorraad, slotcapaciteit, receptdetails (die worden opgehaald op het moment van bestellen).

### Statusmodel van een bestelling

```
PLACED ──(checks ok)──> CONFIRMED ──(delivery.started)──> IN_DELIVERY ──(order.delivered)──> DELIVERED
   │                        │
   └──────────── annuleren (PLACED of CONFIRMED) ──────────> CANCELLED
```

`PLACED` bestaat enkel binnen de transactie van `POST /api/orders`. Falen de synchrone checks, dan wordt alles teruggedraaid en krijgt de klant een 409. Slagen ze, dan gaat de order naar `CONFIRMED` en wordt `order.confirmed` gepubliceerd.

### Events (RabbitMQ)

| Exchange | Routing key | Producer | Consumers |
|---|---|---|---|
| `order.events` | `order.confirmed` | order-service | inventory, delivery, notification |
| `order.events` | `order.cancelled` | order-service | inventory, delivery, notification |
| `delivery.events` | `delivery.started` | delivery-service | order, notification |
| `delivery.events` | `order.delivered` | delivery-service | order, notification |

Het event `order.confirmed` bevat alles wat consumers nodig hebben (orderId, klantId, naam, adres, slotId, personen, lijst van ingrediënt-id + hoeveelheid), zodat ze **geen terugvraag** hoeven te doen. Elk event draagt een `traceId` mee in de headers.

---

## 3. Flows per user story

### US1: Het weekmenu bekijken
1. Frontend → gateway → recipe-service: `GET /api/recipes?week=2026-W47`.
2. recipe-service leest uit zijn eigen database en geeft een lijst DTO's terug (naam, bereidingstijd, basisaantal personen, allergenen).
3. Volledig **synchroon** en zonder andere services: een leesvraag heeft het antwoord direct nodig en er is niets om op te wachten.

### US2: Een box samenstellen en bestellen
1. Klant plaatst de bestelling in de frontend → gateway → order-service: `POST /api/orders` (recepten, aantal personen, leverslot). Klant en rol komen uit de headers.
2. order-service haalt per recept de ingrediënten op bij recipe-service. **Sync (Feign)**: zonder die lijst weet hij niet wat er nodig is. Onbekend recept → 404.
3. order-service rekent de totalen uit (hoeveelheid × personen / basisaantal) en vraagt aan inventory-service: `POST /internal/stock/check`. **Sync (Feign)**: zonder antwoord kan de bestelling niet aanvaard worden. Te weinig → **409** `INSUFFICIENT_STOCK` met de ontbrekende ingrediënten.
4. order-service vraagt aan delivery-service: `GET /internal/delivery-slots/{id}`. **Sync (Feign)**. Slot vol of onbekend → **409** `SLOT_FULL`.
5. Slagen beide checks: order wordt opgeslagen als `CONFIRMED`, antwoord **201** met de order.
6. Pas dan publiceert order-service `order.confirmed` **async**. Drie consumers pikken het op:
   - inventory-service reserveert de ingrediënten (`StockReservation`) en trekt ze af van de voorraad.
   - delivery-service plant de bestelling in het slot in (`DeliveryStop`) en verhoogt de bezetting.
   - notification-service logt "bestelling bevestigd".
7. Ligt één van de drie plat, dan slaagt de bestelling toch. De consument verwerkt het bericht zodra hij terug is (durable queue).

### US3: Mijn bestellingen volgen
1. Frontend → gateway → order-service: `GET /api/orders` (gefilterd op `X-User-Id`) en `GET /api/orders/{id}`.
2. order-service leest uit zijn eigen database. **Sync, geen andere services.**
3. De status blijft actueel omdat order-service zelf `delivery.started` en `order.delivered` consumeert (**async**) en zijn eigen status bijwerkt. Hij hoeft dus nooit bij delivery-service te vragen waar een bestelling staat.

### US4: Voorraad beheren
1. Medewerker → gateway → inventory-service: `GET /api/stock` en `GET /api/stock?lowOnly=true` (onder drempel).
2. Aanvullen: `PATCH /api/stock/{ingredientId}/replenish` met `{ "quantity": 50 }`.
3. **Sync, enkel inventory-service.** Alleen rol `MEDEWERKER` mag dit; anders **403**.

### US5: Een leverronde afwerken
1. Chauffeur → gateway → delivery-service: `GET /api/delivery-runs?date=...` toont zijn rit met de stops.
2. `PATCH /api/delivery-runs/{id}/start` zet de rit op onderweg. delivery-service publiceert `delivery.started` (**async**). order-service zet de bestellingen op `IN_DELIVERY`.
3. `PATCH /api/delivery-runs/{id}/orders/{orderId}/delivered` markeert een stop als geleverd. delivery-service publiceert `order.delivered` (**async**).
4. order-service zet de order op `DELIVERED`; notification-service verwittigt de klant.
5. De chauffeur wacht niet op die gevolgen: het afvinken slaagt zodra delivery-service het bewaard heeft.

### US6: Een bestelling annuleren
1. Klant → gateway → order-service: `PATCH /api/orders/{id}/cancel`.
2. order-service controleert de status (enkel `PLACED` of `CONFIRMED`, en enkel eigen bestellingen). Is de order al `IN_DELIVERY` of `DELIVERED` → **409** `ORDER_NOT_CANCELLABLE`.
3. Status wordt `CANCELLED`, antwoord **200**.
4. order-service publiceert `order.cancelled` (**async**):
   - inventory-service geeft de `StockReservation` vrij.
   - delivery-service haalt de stop uit de rit en geeft de plaats in het slot terug.
   - notification-service verwittigt de klant.
5. Het antwoord aan de klant hangt niet af van het vrijgeven: de bestelling is geannuleerd, de rest volgt.

---

## 4. API-paden (eerste schets)

Alles via de gateway op `http://localhost:8080`. Headers: `X-User-Id`, `X-User-Role` (`KLANT` | `MEDEWERKER` | `CHAUFFEUR`).

| Service | Methode + pad | Rol | Doel | Succes |
|---|---|---|---|---|
| recipe | `GET /api/recipes?week=` | iedereen | Weekmenu (US1) | 200 |
| recipe | `GET /api/recipes/{id}` | iedereen | Detail met ingrediënten | 200 / 404 |
| recipe | `POST /api/recipes` | MEDEWERKER | Recept aanmaken (CRUD, week 9) | 201 / 400 |
| recipe | `PUT /api/recipes/{id}` | MEDEWERKER | Recept wijzigen | 200 / 404 |
| recipe | `DELETE /api/recipes/{id}` | MEDEWERKER | Recept verwijderen | 204 / 404 |
| order | `POST /api/orders` | KLANT | Bestellen (US2) | 201 / 400 / 409 |
| order | `GET /api/orders` | KLANT | Mijn bestellingen (US3) | 200 |
| order | `GET /api/orders/{id}` | KLANT | Detail + status | 200 / 404 |
| order | `PATCH /api/orders/{id}/cancel` | KLANT | Annuleren (US6) | 200 / 404 / 409 |
| inventory | `GET /api/stock` | MEDEWERKER | Voorraad (US4); `?lowOnly=true` | 200 |
| inventory | `PATCH /api/stock/{ingredientId}/replenish` | MEDEWERKER | Aanvullen (US4) | 200 / 404 / 400 |
| delivery | `GET /api/delivery-slots?date=` | KLANT | Beschikbare slots met vrije plaatsen | 200 |
| delivery | `GET /api/delivery-runs?date=` | CHAUFFEUR | Mijn ritten (US5) | 200 |
| delivery | `GET /api/delivery-runs/{id}` | CHAUFFEUR | Stops in een rit | 200 / 404 |
| delivery | `PATCH /api/delivery-runs/{id}/start` | CHAUFFEUR | Rit starten | 200 / 404 |
| delivery | `PATCH /api/delivery-runs/{id}/orders/{orderId}/delivered` | CHAUFFEUR | Stop afvinken (US5) | 200 / 404 |

**Intern, niet via de gateway:**

| Service | Pad | Gebruikt door |
|---|---|---|
| recipe | `GET /internal/recipes/{id}` | order-service |
| inventory | `POST /internal/stock/check` | order-service |
| delivery | `GET /internal/delivery-slots/{id}` | order-service |

**Afspraken**
- Paden altijd in het **meervoud** en onder `/api`. Acties die geen CRUD zijn (annuleren, starten) zijn een `PATCH` op een subpad.
- Statuscodes: `201` aanmaken, `200` ophalen/wijzigen, `204` verwijderen, `400` ongeldige body, `403` verkeerde rol, `404` bestaat niet, `409` voorraad/slot/status laat het niet toe.
- Nooit entities in de response; altijd DTO's.
- Eén foutobject voor alle services:

```json
{
  "code": "INSUFFICIENT_STOCK",
  "message": "Onvoldoende voorraad voor: arborio (nodig 600 g, beschikbaar 450 g)",
  "timestamp": "2026-11-17T10:15:30Z",
  "path": "/api/orders"
}
```

Foutcodes: `VALIDATION_ERROR`, `NOT_FOUND`, `FORBIDDEN`, `INSUFFICIENT_STOCK`, `SLOT_FULL`, `ORDER_NOT_CANCELLABLE`, `SERVICE_UNAVAILABLE`.

---

## 5. Bewuste vereenvoudigingen

| Vereenvoudiging | Gevolg |
|---|---|
| Geen echte authenticatie: rol en gebruiker komen uit headers | De backend vertrouwt de headers. Geen tokens, geen login. |
| Geen customer-service; klantgegevens zijn een snapshot in `Order` | Een adreswijziging geldt niet voor lopende bestellingen. |
| Geen routeoptimalisatie: een `DeliveryRun` is gewoon de lijst bestellingen van een slot | Volgorde van stops is invoervolgorde. |
| Geen echte mails: notification-service logt enkel | Geen retry, geen opslag. |
| Geen betalingen, geen prijzen | De bestelling is gratis. |
| Voorraadcontrole bij het bestellen is een **controle**, geen reservatie; de echte reservatie volgt async | Twee klanten kunnen tegelijk de laatste voorraad "zien". Zie open punt in BESLISSINGEN.md. |
| Event wordt gepubliceerd na het opslaan, zonder outbox | Crasht de service tussen opslaan en publiceren, dan gaat het event verloren. Bewust aanvaard in deze case. |
| Alles draait lokaal via Docker Compose | Geen productie-infrastructuur. |

---

## 6. Opstarten

*Wordt aangevuld in week 14.* Doel: `docker compose up -d` vanuit `backend/` brengt alles omhoog; Swagger UI via de gateway.

## 7. Weekplanning

| Week | Doel |
|---|---|
| 8 | Servicegrenzen, domeinmodel, API-schets (dit document) |
| 9 | Vijf services met CRUD en eigen DB; US1 en US4 |
| 10 | Tests, `openapi.yaml` bevroren, tag `week10-spec` |
| 11 | Eureka + Feign; US2 werkt |
| 12 | Config-service, gateway, CORS, tag `week12-api` |
| 13 | RabbitMQ (3 consumers), Resilience4j, Micrometer Tracing |
| 14 | Docker Compose, JaCoCo ≥ 70%, springdoc, tag `week14-final` |
