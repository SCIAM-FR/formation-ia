package com.sciam.formation.eval;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * FOURNI — juge « System One » : TypeSafe Jev, endpoint POST /v1/systemone (https://docs.typesafe.ai/api).
 *
 * Remplace JugeLLM. Différence de nature : Jev ne génère pas de texte. On lui envoie un état (les sources)
 * et des questions typées ; il renvoie des probabilités calibrées, une par question, en un seul appel :
 *  - Noul   : question oui/non → probabilité que la réponse soit oui (0..1) ;
 *  - Score  : niveaux ordonnés décrits en mots → position sur l'échelle + confiance ;
 *  - Choice : options nommées → option la plus probable + distribution + confiance.
 * La note sur 10 n'est plus demandée au modèle : elle se calcule en code à partir des réponses
 * (pattern « composite scoring »). Il n'y a donc plus de JSON libre à parser ni de justification à lire :
 * la justification, ce sont les réponses elles-mêmes.
 *
 * Config via l'environnement : TYPESAFE_API_KEY (obligatoire), TYPESAFE_MODEL (défaut jev-latest ;
 * épingler jev-1.13.0 si des seuils ont été calibrés), TYPESAFE_ENDPOINT (défaut : l'API publique).
 * Trace : la requête et la réponse brutes sont toujours écrites dans target/juge-jev-request.json et
 * target/juge-jev-response.json ; TYPESAFE_TRACE=1 les affiche aussi en console.
 * Vous n'écrivez pas ce fichier : vous écrivez les questions et les poids dans le test.
 */
public final class JugeJev {

    public static final String ENDPOINT_DEFAUT = "https://api.typesafe.ai/v1/systemone";
    public static final String MODELE_DEFAUT = "jev-latest";

    // --- Questions ---------------------------------------------------------------------------
    /** instructions : une String, ou une Map pour une instruction structurée (la question dans un champ,
     *  les données qu'elle cite dans les autres, référencées entre accents graves : `champ`). */
    public sealed interface Question permits Noul, Score, Choice {}
    public record Noul(Object instructions, Object oui, Object non) implements Question {}
    public record Score(Object instructions, List<?> niveaux) implements Question {}
    public record Choice(Object instructions, Map<String, ?> options) implements Question {}

    public static Noul noul(Object instructions) { return new Noul(instructions, null, null); }
    /** oui / non : ce que veut dire une réponse proche de 1 et une réponse proche de 0. */
    public static Noul noul(Object instructions, Object oui, Object non) { return new Noul(instructions, oui, non); }
    /** niveaux : du plus bas au plus haut, 2 à 10 niveaux, chacun décrivant une situation (pas un degré). */
    public static Score score(Object instructions, Object... niveaux) { return new Score(instructions, List.of(niveaux)); }
    public static Choice choice(Object instructions, Map<String, ?> options) { return new Choice(instructions, options); }

    // --- Réponses ----------------------------------------------------------------------------
    public sealed interface Reponse permits ReponseNoul, ReponseScore, ReponseChoice {}
    public record ReponseNoul(double noul) implements Reponse {
        public boolean oui(double seuil) { return noul >= seuil; }
    }
    public record ReponseScore(double score, double confiance, Map<Integer, Double> probabilites, Map<Integer, String> legende) implements Reponse {
        /** Le score ramené sur 0..1 : score / (nombre de niveaux − 1). Indispensable avant de pondérer
         *  des échelles de longueurs différentes. */
        public double normalise() { return legende.size() <= 1 ? 0 : score / (legende.size() - 1); }
        /** Le niveau le plus probable, en clair. */
        public String niveauProbable() {
            return probabilites.entrySet().stream().max(Map.Entry.comparingByValue())
                .map(e -> e.getKey() + " : " + legende.get(e.getKey())).orElse("?");
        }
    }
    public record ReponseChoice(String choix, double confiance, Map<String, Double> probabilites) implements Reponse {}

    /** Les réponses d'un appel, sous les identifiants choisis pour les questions. */
    public record Verdict(String modele, Map<String, Reponse> reponses, int tokensEntree) {
        public ReponseNoul noul(String id) { return (ReponseNoul) reponse(id, ReponseNoul.class); }
        public ReponseScore score(String id) { return (ReponseScore) reponse(id, ReponseScore.class); }
        public ReponseChoice choice(String id) { return (ReponseChoice) reponse(id, ReponseChoice.class); }
        private Reponse reponse(String id, Class<? extends Reponse> type) {
            Reponse r = reponses.get(id);
            if (r == null) throw new IllegalArgumentException("Pas de réponse « " + id + " » ; reçues : " + reponses.keySet());
            if (!type.isInstance(r)) throw new IllegalArgumentException("« " + id + " » est " + r.getClass().getSimpleName() + ", pas " + type.getSimpleName());
            return r;
        }
        /** Résumé lisible pour un message d'échec : une ligne par question. */
        public String resume() {
            StringBuilder sb = new StringBuilder("modèle ").append(modele).append('\n');
            reponses.forEach((id, r) -> sb.append("  ").append(id).append(" = ").append(switch (r) {
                case ReponseNoul n -> String.format("noul %.2f", n.noul());
                case ReponseScore s -> String.format("score %.2f sur %d, soit %.2f normalisé (confiance %.2f, niveau probable %s)",
                    s.score(), s.legende().size() - 1, s.normalise(), s.confiance(), s.niveauProbable());
                case ReponseChoice c -> String.format("choix %s (confiance %.2f, %s)", c.choix(), c.confiance(), c.probabilites());
            }).append('\n'));
            return sb.toString();
        }
    }

    // --- Appel -------------------------------------------------------------------------------
    private static final ObjectMapper M = new ObjectMapper();
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build();
    private final String endpoint = envOu("TYPESAFE_ENDPOINT", ENDPOINT_DEFAUT);
    private final String modele = envOu("TYPESAFE_MODEL", MODELE_DEFAUT);

    /** @param etat ce qui est jugé : une String, ou une Map/List (objet JSON nommé, recommandé) ;
     *  @param questions toutes les questions d'un coup : elles sont évaluées en parallèle sur le même état. */
    public Verdict evaluer(Object etat, Map<String, ? extends Question> questions) {
        try {
            ObjectNode body = M.createObjectNode();
            body.set("state", M.valueToTree(etat));
            body.put("model", modele);
            ObjectNode qs = body.putObject("questions");
            questions.forEach((id, q) -> qs.set(id, json(q)));
            tracer("request", body);
            JsonNode reponse = M.readTree(envoyer(M.writeValueAsString(body)));
            tracer("response", reponse);
            return lire(reponse);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Appel au juge Jev échoué : " + e.getMessage(), e);
        }
    }

    private ObjectNode json(Question q) {
        ObjectNode n = M.createObjectNode();
        switch (q) {
            case Noul x -> {
                n.put("type", "noul");
                n.set("instructions", M.valueToTree(x.instructions()));
                if (x.oui() != null || x.non() != null) {
                    ObjectNode c = n.putObject("criteria");
                    if (x.oui() != null) c.set("true", M.valueToTree(x.oui()));
                    if (x.non() != null) c.set("false", M.valueToTree(x.non()));
                }
            }
            case Score x -> {
                if (x.niveaux().size() < 2 || x.niveaux().size() > 10)
                    throw new IllegalArgumentException("Un Score a de 2 à 10 niveaux ; reçu " + x.niveaux().size());
                n.put("type", "score");
                n.set("instructions", M.valueToTree(x.instructions()));
                n.set("criteria", M.valueToTree(x.niveaux()));
            }
            case Choice x -> {
                n.put("type", "choice");
                n.set("instructions", M.valueToTree(x.instructions()));
                n.set("criteria", M.valueToTree(x.options()));
            }
        }
        return n;
    }

    /** POST avec reprise sur 429 / 529 (backoff exponentiel, en-tête retry-after honoré), comme les SDK officiels. */
    private String envoyer(String body) throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(endpoint))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + env("TYPESAFE_API_KEY"))
            .timeout(Duration.ofSeconds(60))
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
        long attente = 1000;
        for (int essai = 1; ; essai++) {
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            int code = res.statusCode();
            if (code == 200) return res.body();
            boolean reprise = (code == 429 || code == 529) && essai < 4;
            if (!reprise) throw new IllegalStateException("HTTP " + code + " de " + endpoint + " : " + res.body());
            long delai = res.headers().firstValueAsLong("retry-after").stream().map(s -> s * 1000).findFirst().orElse(attente);
            Thread.sleep(delai);
            attente *= 2;
        }
    }

    private static Verdict lire(JsonNode racine) {
        Map<String, Reponse> reponses = new LinkedHashMap<>();
        for (Iterator<Map.Entry<String, JsonNode>> it = racine.path("answers").fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> e = it.next();
            JsonNode a = e.getValue();
            reponses.put(e.getKey(), switch (a.path("type").asText()) {
                case "noul" -> new ReponseNoul(a.path("noul").asDouble());
                case "score" -> {
                    Map<Integer, Double> p = new LinkedHashMap<>();
                    a.path("probabilities").fields().forEachRemaining(x -> p.put(Integer.parseInt(x.getKey()), x.getValue().asDouble()));
                    Map<Integer, String> l = new LinkedHashMap<>();
                    a.path("legend").fields().forEachRemaining(x -> l.put(Integer.parseInt(x.getKey()), x.getValue().isTextual() ? x.getValue().asText() : x.getValue().toString()));
                    yield new ReponseScore(a.path("score").asDouble(), a.path("confidence").asDouble(), p, l);
                }
                case "choice" -> {
                    Map<String, Double> p = new LinkedHashMap<>();
                    a.path("probabilities").fields().forEachRemaining(x -> p.put(x.getKey(), x.getValue().asDouble()));
                    yield new ReponseChoice(a.path("choice").asText(), a.path("confidence").asDouble(), p);
                }
                default -> throw new IllegalStateException("Type de réponse inconnu pour « " + e.getKey() + " » : " + a);
            });
        }
        return new Verdict(racine.path("model").asText(), reponses, racine.path("usage").path("input_tokens").asInt());
    }

    /** Écrit le JSON tel qu'il part (ou arrive) dans target/juge-jev-<nom>.json ; en console si TYPESAFE_TRACE est défini. */
    private static void tracer(String nom, JsonNode json) {
        try {
            String joli = M.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            java.nio.file.Path f = java.nio.file.Path.of("target", "juge-jev-" + nom + ".json");
            java.nio.file.Files.createDirectories(f.getParent());
            java.nio.file.Files.writeString(f, joli);
            if (!envOu("TYPESAFE_TRACE", "").isBlank()) System.out.println("=== Jev " + nom + " (" + f + ")\n" + joli);
        } catch (java.io.IOException e) {
            System.err.println("Trace " + nom + " non écrite : " + e.getMessage());
        }
    }

    private static String env(String k) {
        String v = System.getenv(k);
        if (v == null || v.isBlank()) throw new IllegalStateException("Variable manquante : " + k);
        return v;
    }

    private static String envOu(String k, String defaut) {
        String v = System.getenv(k);
        return v == null || v.isBlank() ? defaut : v;
    }
}
