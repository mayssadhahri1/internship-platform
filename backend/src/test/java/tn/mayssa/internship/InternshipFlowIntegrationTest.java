package tn.mayssa.internship;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Parcours complet : inscription, publication d'une offre, candidature, décision. */
@SpringBootTest
@AutoConfigureMockMvc
class InternshipFlowIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    private String register(String role) throws Exception {
        String email = role.toLowerCase() + "-" + UUID.randomUUID() + "@test.tn";
        var body = Map.of("fullName", "Test " + role, "email", email, "password", "Password123",
                "role", role, "organization", role.equals("COMPANY") ? "ACME" : "Polytechnique Sousse");
        String res = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(res).get("token").asText();
    }

    private MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder req, String token) {
        return req.header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON);
    }

    private long createOffer(String companyToken, String title) throws Exception {
        var body = Map.of("title", title, "description", "Développement d'une API REST sécurisée",
                "location", "Sousse", "durationMonths", 6, "skills", "Spring Boot, Angular");
        String res = mvc.perform(auth(post("/api/offers"), companyToken).content(json.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(res).get("id").asLong();
    }

    private String motivation() throws Exception {
        return json.writeValueAsString(Map.of("motivation", "Je suis très motivée par ce sujet de stage."));
    }

    @Test
    void fullApplicationFlow() throws Exception {
        String company = register("COMPANY");
        String student = register("STUDENT");
        long offerId = createOffer(company, "Plateforme de suivi des stages");

        // L'offre est visible publiquement et trouvable par mot-clé
        mvc.perform(get("/api/offers").param("keyword", "suivi des stages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Plateforme de suivi des stages"));

        // L'étudiant postule une seule fois
        String res = mvc.perform(auth(post("/api/offers/" + offerId + "/applications"), student).content(motivation()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn().getResponse().getContentAsString();
        long applicationId = json.readTree(res).get("id").asLong();

        mvc.perform(auth(post("/api/offers/" + offerId + "/applications"), student).content(motivation()))
                .andExpect(status().isConflict());

        // L'entreprise voit la candidature et l'accepte
        mvc.perform(auth(get("/api/offers/" + offerId + "/applications"), company))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mvc.perform(auth(patch("/api/applications/" + applicationId + "/status"), company)
                        .content("{\"status\":\"ACCEPTED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        // L'étudiant voit la décision
        mvc.perform(auth(get("/api/applications/mine"), student))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("ACCEPTED"));
    }

    @Test
    void studentCannotPublishOffer() throws Exception {
        String student = register("STUDENT");
        var body = Map.of("title", "X", "description", "Y", "location", "Tunis", "durationMonths", 4);
        mvc.perform(auth(post("/api/offers"), student).content(json.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    @Test
    void companyCannotSeeApplicationsOfAnotherCompany() throws Exception {
        String owner = register("COMPANY");
        String other = register("COMPANY");
        long offerId = createOffer(owner, "Offre privée");

        mvc.perform(auth(get("/api/offers/" + offerId + "/applications"), other))
                .andExpect(status().isForbidden());
        mvc.perform(auth(delete("/api/offers/" + offerId), other))
                .andExpect(status().isForbidden());
    }

    @Test
    void protectedEndpointsRequireToken() throws Exception {
        mvc.perform(get("/api/applications/mine")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/stats")).andExpect(status().isUnauthorized());
    }

    @Test
    void loginWithWrongPasswordIsRejected() throws Exception {
        register("STUDENT");
        var body = Map.of("email", "inconnu@test.tn", "password", "mauvais");
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidOfferIsRejectedWithFieldErrors() throws Exception {
        String company = register("COMPANY");
        var body = Map.of("title", "", "description", "Y", "location", "Tunis", "durationMonths", 20);
        String res = mvc.perform(auth(post("/api/offers"), company).content(json.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();
        JsonNode errors = json.readTree(res).get("errors");
        org.junit.jupiter.api.Assertions.assertTrue(errors.has("title"));
        org.junit.jupiter.api.Assertions.assertTrue(errors.has("durationMonths"));
    }
}
