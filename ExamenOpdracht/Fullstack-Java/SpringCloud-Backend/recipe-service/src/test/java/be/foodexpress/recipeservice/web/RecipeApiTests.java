package be.foodexpress.recipeservice.web;

import be.foodexpress.recipeservice.TestcontainersConfiguration;
import be.foodexpress.recipeservice.repository.IngredientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integratietest: start de volledige service op een willekeurige poort, met een echte
 * PostgreSQL in een Testcontainer (Docker moet draaien). De DemoDataSeeder vult het menu.
 * We gebruiken bewust de gewone Java HttpClient: zo testen we exact wat de frontend ziet.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RecipeApiTests {

    @Value("${local.server.port}")
    int port;

    @Autowired
    IngredientRepository ingredientRepository;

    private final HttpClient http = HttpClient.newHttpClient();

    @Test
    void us1_weekmenuGeeftDeRecepten() throws Exception {
        HttpResponse<String> res = get("/api/recipes");

        assertThat(res.statusCode()).isEqualTo(200);
        assertThat(res.body())
                .contains("Romige champignonrisotto")
                .contains("\"prepTimeMinutes\":35")
                .contains("\"allergens\":[\"melk\",\"selderij\"]");
    }

    @Test
    void onbestaandReceptGeeft404MetFoutobject() throws Exception {
        HttpResponse<String> res = get("/api/recipes/999999");

        assertThat(res.statusCode()).isEqualTo(404);
        assertThat(res.body()).contains("\"code\":\"RECIPE_NOT_FOUND\"").doesNotContain("Exception");
    }

    @Test
    void ongeldigeWeekGeeft400() throws Exception {
        HttpResponse<String> res = get("/api/recipes?week=volgende");

        assertThat(res.statusCode()).isEqualTo(400);
        assertThat(res.body()).contains("\"code\":\"INVALID_WEEK\"");
    }

    @Test
    void onbekendPadGeeft404() throws Exception {
        assertThat(get("/api/bestaat-niet").statusCode()).isEqualTo(404);
    }

    @Test
    void aanmakenZonderMedewerkerRolGeeft403() throws Exception {
        HttpResponse<String> res = post("/api/recipes", "KLANT", validBody());

        assertThat(res.statusCode()).isEqualTo(403);
        assertThat(res.body()).contains("\"code\":\"FORBIDDEN\"");
    }

    @Test
    void ongeldigeBodyGeeft400MetDetails() throws Exception {
        String body = """
                { "name": "", "prepTimeMinutes": -5, "servings": 2, "week": "2026-W48", "ingredients": [] }
                """;
        HttpResponse<String> res = post("/api/recipes", "MEDEWERKER", body);

        assertThat(res.statusCode()).isEqualTo(400);
        assertThat(res.body())
                .contains("\"code\":\"VALIDATION_FAILED\"")
                .contains("name")
                .contains("prepTimeMinutes")
                .contains("ingredients");
    }

    @Test
    void medewerkerKanEenReceptAanmaken() throws Exception {
        HttpResponse<String> res = post("/api/recipes", "MEDEWERKER", validBody());

        assertThat(res.statusCode()).isEqualTo(201);
        assertThat(res.headers().firstValue("Location")).isPresent();
        assertThat(res.body()).contains("\"name\":\"Testsoep\"").contains("\"week\":\"2030-W01\"");
    }

    // ---------- hulpmethodes ----------

    private String validBody() {
        Long ingredientId = ingredientRepository.findAll().getFirst().getId();
        return """
                {
                  "name": "Testsoep",
                  "description": "Enkel voor de test",
                  "prepTimeMinutes": 15,
                  "servings": 2,
                  "week": "2030-W01",
                  "ingredients": [ { "ingredientId": %d, "quantityPerPerson": 50 } ]
                }
                """.formatted(ingredientId);
    }

    private HttpResponse<String> get(String path) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(uri(path)).GET().build();
        return http.send(req, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String path, String role, String json) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(uri(path))
                .header("Content-Type", "application/json")
                .header(UserHeaders.USER, "test")
                .header(UserHeaders.ROLE, role)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return http.send(req, HttpResponse.BodyHandlers.ofString());
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }
}
