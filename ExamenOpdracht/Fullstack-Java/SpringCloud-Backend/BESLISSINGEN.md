# Beslissingen

Per week drie tot vijf regels: wat ik gedaan heb, wat nog openstaat, en welke keuze ik gemaakt heb en waarom.

> ✏️ Startversie: herschrijf in je eigen woorden en vul elke week aan.

---

## Week 8 – Analyse

- **Gedaan:** servicegrenzen vastgelegd: recipe, order, inventory, delivery en notification, elk met een eigen database (notification zonder). API-paden afgestemd met de frontend (zie `../TODO.md`, later `openapi.yaml`).
- **Keuze – geen customer-service:** de klant is de `X-User` uit de header. order-service bewaart bij elke bestelling een *kopie* van de klantnaam en het leveradres. Zo blijft een oude bestelling correct, ook als de klant later verhuist, en heeft order-service geen extra synchrone call nodig.
- **Keuze – voorraad apart van recepten:** een recept beschrijft wat er nodig is, inventory beheert hoeveel er is. Ze veranderen om verschillende redenen en op een ander ritme.
- **Keuze – synchroon vs. asynchroon:** voorraad- en slotcontrole zijn synchroon (Feign), want de klant moet meteen weten of het lukt. Alles na de bevestiging (reserveren, inplannen, melden) gaat via het event `order.confirmed`.
- **Open:** hoe order-service weet welke ingrediënten een recept nodig heeft (kopie van de ingrediëntenlijst in de bestelling, of opvragen bij recipe-service?).

## Week 9 – De services

- **Gedaan:** multi-module `pom.xml` in de root; `docker-compose.yml` met vier PostgreSQL-databases.
- **Keuze – PostgreSQL:** dezelfde database lokaal, in tests (Testcontainers) en in Docker. Zo test ik tegen wat er echt draait.
- **Keuze – root-pom als aggregator:** elke service houdt `spring-boot-starter-parent` als parent; de root-pom bundelt enkel de modules. Elke service blijft zo zelfstandig te bouwen en te deployen.
- **Gedaan:** recipe-service met CRUD voor recepten en ingrediënten, DTO's, één foutobject (`ApiError`) en demodata. US1 werkt: `GET /api/recipes`.
- **Keuze – Spring Boot 4.0.8 i.p.v. 4.2.0:** IntelliJ stelde 4.2.0 voor, maar de case en Spring Cloud 2025.1.x horen bij Boot 4.0.x.
- **Keuze – allergenen op het ingrediënt:** een recept *berekent* zijn allergenen uit zijn ingrediënten. Zo kan een recept nooit een allergeen "vergeten" als je een ingrediënt toevoegt.
- **Keuze – Ingredient is een catalogus met vaste id's:** inventory-service gebruikt hetzelfde `ingredientId` als sleutel voor de voorraad, zonder de tabellen van recipe-service te delen.
- **Keuze – schrijven enkel als MEDEWERKER:** de rol komt uit de header `X-Role`; een andere rol krijgt 403.
- **Open:** order-, inventory- en delivery-service (CRUD), US4.
