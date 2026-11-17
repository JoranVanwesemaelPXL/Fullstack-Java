workspace "Maaltijdboxenaanhuis" "Plan van aanpak - architectuur (week 8)" {

    model {
        klant       = person "Klant" "Bekijkt het weekmenu, bestelt en volgt bestellingen (US1, US2, US3, US6)"
        medewerker  = person "Medewerker" "Beheert de voorraad (US4)"
        chauffeur   = person "Chauffeur" "Werkt een leverronde af (US5)"

        maaltijdboxen = softwareSystem "Maaltijdboxenaanhuis" "Stelt wekelijkse maaltijdboxen samen en levert ze" {

            frontend = container "Angular frontend" "AuthService bewaart rol + gebruiker; stuurt X-User-Id en X-User-Role mee" "Angular, :4200" "Frontend"

            gateway = container "gateway" "Enig toegangspunt, routering, CORS, aggregatie Swagger UI. Onbekend pad = 404" "Spring Cloud Gateway, :8080" "Infra-service"

            discovery = container "discovery-service" "Service registry" "Eureka Server, :8761" "Infra-service"
            config    = container "config-service" "Centrale configuratie" "Spring Cloud Config Server, :8888" "Infra-service"
            broker    = container "Message broker" "Topic exchanges order.events en delivery.events" "RabbitMQ, :5672" "Broker"

            recipeService = container "recipe-service" "Het aanbod: Recipe, Ingredient" "Spring Boot, :8081" "Service"
            recipeDb      = container "recipe-db" "" "PostgreSQL" "Database"

            orderService  = container "order-service" "De bestelling: Order, OrderLine" "Spring Boot, :8082" "Service"
            orderDb       = container "order-db" "" "PostgreSQL" "Database"

            inventoryService = container "inventory-service" "Voorraad: StockItem, StockReservation" "Spring Boot, :8083" "Service"
            inventoryDb      = container "inventory-db" "" "PostgreSQL" "Database"

            deliveryService = container "delivery-service" "Levering: DeliverySlot, DeliveryRun, DeliveryStop" "Spring Boot, :8084" "Service"
            deliveryDb      = container "delivery-db" "" "PostgreSQL" "Database"

            notificationService = container "notification-service" "Meldingen naar de klant (enkel loggen, geen eigen data)" "Spring Boot, :8085" "Service"
        }

        # --- Personen -> frontend
        klant      -> frontend "Gebruikt"
        medewerker -> frontend "Gebruikt"
        chauffeur  -> frontend "Gebruikt"

        # --- Frontend -> gateway -> services (REST, synchroon)
        frontend -> gateway "Alle calls via /api/**" "HTTPS/JSON"
        gateway -> recipeService       "/api/recipes/**"                       "REST"
        gateway -> orderService        "/api/orders/**"                        "REST"
        gateway -> inventoryService    "/api/stock/**"                         "REST"
        gateway -> deliveryService     "/api/delivery-slots/**, /api/delivery-runs/**" "REST"

        # --- Synchrone service-naar-service calls (Feign + Resilience4j)
        orderService -> recipeService    "Haalt receptdetails + ingredienten op"      "Feign (sync)"
        orderService -> inventoryService "Controleert voorraad (POST /internal/stock/check)" "Feign (sync)"
        orderService -> deliveryService  "Controleert leverslot (GET /internal/delivery-slots/{id})" "Feign (sync)"

        # --- Asynchroon via broker
        orderService        -> broker "Publiceert order.confirmed, order.cancelled" "AMQP (async)"
        deliveryService     -> broker "Publiceert delivery.started, order.delivered" "AMQP (async)"
        broker -> inventoryService    "order.confirmed -> reserveren; order.cancelled -> vrijgeven" "AMQP (async)"
        broker -> deliveryService     "order.confirmed -> inplannen; order.cancelled -> slot vrijgeven" "AMQP (async)"
        broker -> notificationService "order.confirmed, order.cancelled, delivery.started, order.delivered -> melding" "AMQP (async)"
        broker -> orderService        "delivery.started -> IN_DELIVERY; order.delivered -> DELIVERED" "AMQP (async)"

        # --- Eigen databases
        recipeService    -> recipeDb    "Leest/schrijft" "JPA"
        orderService     -> orderDb     "Leest/schrijft" "JPA"
        inventoryService -> inventoryDb "Leest/schrijft" "JPA"
        deliveryService  -> deliveryDb  "Leest/schrijft" "JPA"

        # --- Infrastructuur (aparte tag zodat ze weggefilterd kunnen worden)
        gateway             -> discovery "Zoekt services op" "Eureka" "Infra"
        recipeService       -> discovery "Registreert" "Eureka" "Infra"
        orderService        -> discovery "Registreert" "Eureka" "Infra"
        inventoryService    -> discovery "Registreert" "Eureka" "Infra"
        deliveryService     -> discovery "Registreert" "Eureka" "Infra"
        notificationService -> discovery "Registreert" "Eureka" "Infra"

        gateway             -> config "Haalt config op" "HTTP" "Infra"
        recipeService       -> config "Haalt config op" "HTTP" "Infra"
        orderService        -> config "Haalt config op" "HTTP" "Infra"
        inventoryService    -> config "Haalt config op" "HTTP" "Infra"
        deliveryService     -> config "Haalt config op" "HTTP" "Infra"
        notificationService -> config "Haalt config op" "HTTP" "Infra"
    }

    views {

        container maaltijdboxen "Architectuur-volledig" "Alles, inclusief discovery en config" {
            include *
            autolayout lr 300 150
        }

        container maaltijdboxen "Architectuur-functioneel" "Zonder discovery en config: wie praat functioneel met wie" {
            include *
            exclude discovery config
            exclude "relationship.tag==Infra"
            autolayout lr 300 150
        }

        dynamic maaltijdboxen "US2-bestellen" "US2 - Een box samenstellen en bestellen" {
            frontend     -> gateway          "1. POST /api/orders"
            gateway      -> orderService     "2. Route naar order-service"
            orderService -> recipeService    "3. Receptdetails + ingredienten (sync)"
            orderService -> inventoryService "4. Voorraad controleren (sync) - tekort = 409"
            orderService -> deliveryService  "5. Leverslot controleren (sync) - vol = 409"
            orderService -> orderDb          "6. Order opslaan als CONFIRMED"
            orderService -> broker           "7. Publiceer order.confirmed (async)"
            broker       -> inventoryService "8a. Voorraad reserveren"
            broker       -> deliveryService  "8b. Levering inplannen"
            broker       -> notificationService "8c. Klant verwittigen"
            autolayout lr 300 150
        }

        dynamic maaltijdboxen "US5-leveren" "US5 - Een leverronde afwerken" {
            frontend        -> gateway         "1. PATCH /api/delivery-runs/{id}/orders/{orderId}/delivered"
            gateway         -> deliveryService "2. Route naar delivery-service"
            deliveryService -> deliveryDb      "3. Stop op DELIVERED"
            deliveryService -> broker          "4. Publiceer order.delivered (async)"
            broker          -> orderService    "5a. Order op DELIVERED"
            broker          -> notificationService "5b. Klant verwittigen"
            autolayout lr 300 150
        }

        styles {
            element "Person" {
                shape person
                background #08427b
                color #ffffff
            }
            element "Frontend" {
                shape webBrowser
                background #dd0031
                color #ffffff
            }
            element "Service" {
                shape roundedBox
                background #1168bd
                color #ffffff
            }
            element "Infra-service" {
                shape hexagon
                background #6b7280
                color #ffffff
            }
            element "Broker" {
                shape pipe
                background #ff6600
                color #ffffff
            }
            element "Database" {
                shape cylinder
                background #2a9d8f
                color #ffffff
            }
            relationship "Relationship" {
                thickness 2
            }
            relationship "Infra" {
                dashed true
                color #9ca3af
            }
        }
    }
}
