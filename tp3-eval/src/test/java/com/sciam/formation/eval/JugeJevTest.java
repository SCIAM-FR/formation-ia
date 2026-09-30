package com.sciam.formation.eval;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static com.sciam.formation.eval.JugeJev.*;

/**
 * Juge « System One » (TypeSafe Jev) — la rubric devient des QUESTIONS TYPÉES et des POIDS en code.
 * Actif seulement si TYPESAFE_API_KEY est défini (sinon skipped, pas réussi).
 *
 * Règles du jeu, tirées de la doc Jev (model-jaggedness) :
 *  - ne pas lui demander ce que le code calcule exactement : snake_case, présence d'annotations,
 *    longueur des descriptions → ConformiteTest. Le juge ne couvre que ce qu'une regex ne sait pas juger ;
 *  - une question = un seul jugement ; les niveaux décrivent des situations, pas des degrés ;
 *  - lecture littérale : écrire la condition exacte, les cas limites dans les critères ;
 *  - l'état est de la donnée, pas des instructions : les sources sont envoyées sans commentaires ;
 *  - l'anglais est la langue d'entraînement principale : les questions sont en anglais.
 */
@EnabledIfEnvironmentVariable(named = "TYPESAFE_API_KEY", matches = ".+")
class JugeJevTest {

    /** Les questions : l'identifiant est à vous, il n'est pas envoyé au modèle. */
    static final Map<String, Question> RUBRIC = Map.of(
        "descriptions_utiles", score(
            "How useful are the description strings of the @Tool and @ToolArg annotations to an AI agent "
            + "that must decide which tool to call and how to fill its arguments?",
            "Descriptions are missing, empty, or only repeat the method or parameter name",
            "Descriptions say what the tool does, but not what the argument expects nor what the tool returns",
            "Descriptions say what the tool does, what each argument expects (format or example), and what the tool returns"),

        "delegation_propre", noul(
            "Do the methods carrying MCP annotations (@Tool, @ResourceTemplate, @Prompt) only call an injected "
            + "service and format its result, without themselves loading data, parsing JSON, searching or filtering?",
            "Every annotated method delegates the lookup to an injected service; loops, filtering and JSON parsing live in the non-annotated class",
            "At least one annotated method loads the catalogue, parses JSON, or searches or filters the data itself"),

        "absence_geree", noul(
            "When the requested service name does not exist in the catalogue, do the annotated methods return a "
            + "readable message to the caller instead of throwing an exception or returning null?",
            "Each annotated method handles the not-found case with an explicit message",
            "At least one annotated method throws, returns null, or ignores the not-found case"),

        "defaut_principal", choice(
            "Which of these is the most serious defect of this MCP server, if any?",
            Map.of(
                "none", "No notable defect against the conventions",
                "logic_in_adapter", "Business logic or data loading inside the MCP-annotated class",
                "poor_descriptions", "Tool or argument descriptions too vague for an agent to use",
                "missing_primitive", "A tool, resource template or prompt from the conventions is absent",
                "unhandled_errors", "Not-found or invalid input cases are not handled"))
    );

    @Test
    void conformite_notee_par_le_juge() {
        GeneratedProject projet = GeneratedProject.charger();
        // Un état JSON nommé : un champ par fichier source, sans commentaires (« state is data »).
        Map<String, Object> etat = Map.of("java_sources", projet.sources());

        Verdict v = new JugeJev().evaluer(etat, RUBRIC);

        ReponseScore descriptions = v.score("descriptions_utiles");
        ReponseNoul delegation = v.noul("delegation_propre");
        ReponseNoul absence = v.noul("absence_geree");
        ReponseChoice defaut = v.choice("defaut_principal");

        // Confiance basse = « je ne sais pas » : on ne tranche pas à la machine, on renvoie à un humain (skipped).
        assumeTrue(descriptions.confiance() >= 0.5,
            "Juge indécis sur les descriptions (confiance " + descriptions.confiance() + ") : à relire par un humain, ou reformuler les niveaux.\n" + v.resume());

        // La note : des poids que VOUS choisissez, lisibles, modifiables sans toucher au modèle.
        double note = 10 * (0.5 * descriptions.normalise() + 0.3 * delegation.noul() + 0.2 * absence.noul());

        // Toujours visible : dans la console (mvn sans -q) et dans target/surefire-reports/*JugeJevTest-output.txt.
        System.out.printf("%n=== Verdict du juge Jev : note %.1f/10, défaut principal « %s » (%d tokens)%n%s%n",
            note, defaut.choix(), v.tokensEntree(), v.resume());
        sauver(v, note);

        assertTrue(note >= 7, String.format(
            "Note insuffisante (%.1f/10). Défaut principal selon le juge : %s.%n%s", note, defaut.choix(), v.resume()));
    }

    /** Trace brute dans target/juge-jev.json : réponses, probabilités, note. Utile pour comparer deux runs ou deux serveurs. */
    private static void sauver(Verdict v, double note) {
        try {
            java.nio.file.Path f = java.nio.file.Path.of("target", "juge-jev.json");
            java.nio.file.Files.createDirectories(f.getParent());
            new com.fasterxml.jackson.databind.ObjectMapper().writerWithDefaultPrettyPrinter()
                .writeValue(f.toFile(), java.util.Map.of("modele", v.modele(), "note", note, "reponses", v.reponses()));
        } catch (java.io.IOException e) {
            System.err.println("Trace non écrite : " + e.getMessage());
        }
    }
}
