package com.sciam.formation.eval;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * PLOMBIER FOURNI — à ne pas modifier.
 * Charge le cas du dataset désigné par -Dcas=&lt;id&gt; (ex. -Dcas=happy-1) et expose
 * son champ « attendu » : le CONTRAT propre à la demande (noms de tools, resource,
 * prompt) et l'exigence de CONVENTIONS. Sans -Dcas, aucun contrat n'est chargé :
 * seules les conventions du Skill sont évaluées.
 */
public final class CasAttendu {

    private static final ObjectMapper M = new ObjectMapper();
    private static final String[] FICHIERS = {"dataset/happy.json", "dataset/realistic.json", "dataset/adverse.json"};

    /** Le contrat attendu pour une demande : noms publics à retrouver dans le serveur généré. */
    public record Contrat(List<String> tools, Optional<String> resource, Optional<String> prompt) {}

    private final String id;
    private final String demande;
    private final Contrat contrat;
    private final String commentaire;

    private CasAttendu(String id, String demande, Contrat contrat, String commentaire) {
        this.id = id; this.demande = demande; this.contrat = contrat; this.commentaire = commentaire;
    }

    /** Le cas désigné par -Dcas, ou vide si la propriété est absente. */
    public static Optional<CasAttendu> depuisPropriete() {
        String id = System.getProperty("cas", "").trim();
        return id.isEmpty() ? Optional.empty() : Optional.of(charger(id));
    }

    public static CasAttendu charger(String id) {
        for (String fichier : FICHIERS) {
            try (InputStream in = CasAttendu.class.getClassLoader().getResourceAsStream(fichier)) {
                if (in == null) continue;
                for (JsonNode cas : M.readTree(in)) {
                    if (id.equals(cas.path("id").asText())) return depuisJson(cas);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        throw new IllegalStateException("Cas introuvable dans le dataset : " + id
            + " (ids connus : happy-1, realistic-1, realistic-2, adverse-1, adverse-2)");
    }

    private static CasAttendu depuisJson(JsonNode cas) {
        JsonNode attendu = cas.path("attendu");
        JsonNode c = attendu.path("contrat");
        List<String> tools = new ArrayList<>();
        c.path("tools").forEach(t -> tools.add(t.asText()));
        return new CasAttendu(
            cas.path("id").asText(),
            cas.path("demande").asText(),
            new Contrat(List.copyOf(tools), texte(c, "resource"), texte(c, "prompt")),
            attendu.path("commentaire").asText(""));
    }

    private static Optional<String> texte(JsonNode n, String champ) {
        JsonNode v = n.path(champ);
        return v.isMissingNode() || v.isNull() || v.asText().isBlank() ? Optional.empty() : Optional.of(v.asText());
    }

    public String id() { return id; }
    public String demande() { return demande; }
    public Contrat contrat() { return contrat; }
    public String commentaire() { return commentaire; }
}
