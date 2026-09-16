package com.sciam.formation.eval;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * PLOMBIER FOURNI — LLM-as-a-Judge, agnostique du fournisseur.
 * Config via l'environnement : LLM_ENDPOINT, LLM_API_KEY, LLM_MODEL.
 * Vous n'écrivez PAS ce fichier : vous écrivez la GRILLE (rubric) dans le test.
 */
public final class JugeLLM {

    public record Verdict(int note, String justification) {}

    private static final ObjectMapper M = new ObjectMapper();
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build();

    /** @param grille la rubrique (CE QUE VOUS ÉCRIVEZ) ; @param artefact la sortie à juger. */
    public Verdict noter(String grille, String artefact) {
        try {
            String prompt = grille + "\n\n=== ARTEFACT ===\n" + artefact
                + "\n\nRéponds UNIQUEMENT en JSON : {\"note\": <0-10>, \"justification\": \"...\"}";
            var body = M.createObjectNode();
            body.put("model", env("LLM_MODEL"));
            body.putArray("messages").addObject().put("role", "user").put("content", prompt);

            HttpRequest req = HttpRequest.newBuilder(URI.create(env("LLM_ENDPOINT")))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + env("LLM_API_KEY"))
                .timeout(Duration.ofSeconds(60))
                .POST(HttpRequest.BodyPublishers.ofString(M.writeValueAsString(body)))
                .build();

            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            String contenu = M.readTree(res.body()).at("/choices/0/message/content").asText();
            JsonNode v = M.readTree(contenu);
            return new Verdict(v.get("note").asInt(), v.get("justification").asText());
        } catch (Exception e) {
            throw new RuntimeException("Appel au juge LLM échoué : " + e.getMessage(), e);
        }
    }

    private static String env(String k) {
        String v = System.getenv(k);
        if (v == null || v.isBlank()) throw new IllegalStateException("Variable manquante : " + k);
        return v;
    }
}
