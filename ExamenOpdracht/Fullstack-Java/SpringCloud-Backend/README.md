# Maaltijdboxen – Spring Cloud-backend

Backend voor een dienst die wekelijkse maaltijdboxen samenstelt en levert.
Afnemer: de Angular-frontend in `../Angular-Frontend`.

**Versies:** Spring Boot 4.0.8 · Spring Cloud 2025.1.3 · Java 21 · PostgreSQL 17 · Maven

> ✏️ Dit is een startversie. Pas de motivering aan in je eigen woorden: op de verdediging moet je elke grens zelf kunnen verantwoorden.

---

## Services

| Service | Poort | Database (host-poort) | Eigenaar van | Verantwoordelijkheid |
|---|---|---|---|---|
| `recipe-service` | 8081 | `recipe_db` (5433) | Recipe, Ingredient | Het weekmenu: recepten, bereidingstijd, personen, allergenen |
| `order-service` | 8082 | `order_db` (5434) | Order, OrderLine | Bestellingen plaatsen, opvolgen en annuleren. Het hart van de applicatie |
| `inventory-service` | 8083 | `inventory_db` (5435) | StockItem | Voorraad per ingrediënt: controleren, reserveren, vrijgeven, aanvullen |
| `delivery-service` | 8084 | `delivery_db` (5436) | DeliverySlot, DeliveryRun | Leverslots (capaciteit) en leverrondes per chauffeur |
| `notification-service` | 8085 | – | – | Meldingen naar de klant (enkel loggen) |

Later:

| Service | Poort | Week |
|---|---|---|
| `discovery-service` (Eureka) | 8761 | 11 |
| `config-service` | 8888 | 12 |
| `api-gateway` | 8080 | 12 |

## Waarom deze grenzen?

- **Elke service is eigenaar van zijn eigen data** en heeft een eigen database. Geen enkele service leest rechtstreeks de tabellen van een andere.
- **recipe-service** verandert één keer per week (nieuw menu) en wordt vooral gelezen. Dat is een ander ritme dan bestellingen.
- **inventory-service** staat los van recepten: een recept zegt *wat* er nodig is, de voorraad zegt *hoeveel er is*. Twintig klanten die dezelfde risotto kiezen, raken de voorraad, niet het recept.
- **delivery-service** beheert capaciteit (plaatsen per slot) en ritten. Dat is logistiek, geen bestelling.
- **notification-service** heeft geen eigen data. Ze reageert enkel op events, zodat een storing daar nooit een bestelling kan doen mislukken.
- **Geen aparte customer-service**: de klant wordt geïdentificeerd via de header `X-User`. order-service bewaart de gebruikersnaam en het leveradres als kopie bij de bestelling (zie BESLISSINGEN.md).

## Synchroon vs. asynchroon

| Stap | Hoe | Waarom |
|---|---|---|
| Voorraad controleren bij bestellen | **Synchroon** (Feign: order → inventory) | We hebben het antwoord nodig om de bestelling te aanvaarden of te weigeren (409) |
| Leverslot controleren bij bestellen | **Synchroon** (Feign: order → delivery) | Idem: een vol slot moet de klant meteen horen |
| Voorraad reserveren, levering inplannen, klant verwittigen | **Asynchroon** (event `order.confirmed` via RabbitMQ) | Dit mag een bevestigde bestelling nooit doen mislukken |

## Lokaal opstarten

```bash
# 1. Databases starten
docker compose up -d

# 2. Alles bouwen en testen (vanuit deze map)
mvn clean verify

# 3. Een service starten (of via IntelliJ)
mvn -pl recipe-service spring-boot:run
```

Database-login (lokaal): gebruiker `mealbox`, wachtwoord `mealbox`.

## Authenticatie

Er is geen echt inlogsysteem. De frontend stuurt bij elk request de headers
`X-User` (gebruikersnaam) en `X-Role` (`KLANT`, `MEDEWERKER` of `CHAUFFEUR`) mee.
De backend leest die headers uit en gebruikt ze in de logica.

## Bewuste vereenvoudigingen

- Geen echte authenticatie: rol en gebruiker komen uit headers.
- Geen aparte customer-service: klantgegevens (gebruikersnaam, adres) worden bij de bestelling bewaard.
- Leverslots zijn niet gekoppeld aan een regio of postcode. Een rit heeft geen route-optimalisatie.
- notification-service verstuurt geen mails, ze logt enkel.
- Geen betalingen.
