package com.sciam.formation.eval;

import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * CORRIGÉ FORMATEUR — une réponse possible aux quatre stubs du TP3.
 * Réf : domaine/conventions.md. À utiliser au débrief, pas à distribuer avant.
 *
 * Choix de conception : on ne cherche que dans les sources de production, on
 * retire les commentaires avant d'appliquer une regex, et on exige à chaque fois
 * l'annotation MCP ET le nom public, sur la même déclaration.
 */
class ConformiteDeterministeCorrige {

    private final GeneratedProject projet = GeneratedProject.charger();

    private static final Pattern COMMENTAIRES =
        Pattern.compile("/\\*.*?\\*/|//[^\\n]*", Pattern.DOTALL);

    /** Sources sans commentaires ; les tests du projet cible restent inclus
     *  (GeneratedProject ne distingue pas src/main de src/test), d'où les
     *  assertions qui exigent une annotation et non une simple occurrence. */
    private String source() {
        return COMMENTAIRES.matcher(projet.sourceJava()).replaceAll(" ");
    }

    private static boolean exposeTool(String src, String nom) {
        String q = Pattern.quote(nom);
        boolean avecName = Pattern.compile(
            "@Tool\\s*\\([^)]*\\bname\\s*=\\s*\"" + q + "\"", Pattern.DOTALL)
            .matcher(src).find();
        boolean methodeNommee = Pattern.compile(
            "@Tool\\b(?:\\s*\\((?![^)]*\\bname\\s*=)[^)]*\\))?\\s*(?:@\\w+(?:\\([^)]*\\))?\\s*)*"
            + "(?:public\\s+|protected\\s+)?[\\w<>\\[\\],\\s]+?\\s+" + q + "\\s*\\(",
            Pattern.DOTALL).matcher(src).find();
        return avecName || methodeNommee;
    }

    // --- Exemple fourni dans le squelette, conservé --------------------------
    @Test
    void utilise_extension_quarkus_mcp() {
        assertTrue(projet.pom().contains("quarkus-mcp-server"),
            "Le pom doit déclarer l'extension quarkus-mcp-server");
    }

    // --- Les quatre stubs -----------------------------------------------------
    @Test
    void expose_tool_find_service() {
        assertTrue(exposeTool(source(), "find_service"),
            "Convention : un @Tool dont le nom public est find_service "
            + "(attribut name, ou méthode Java nommée find_service). "
            + "Une occurrence dans un commentaire, un log ou un test ne compte pas.");
    }

    @Test
    void expose_tool_get_owner() {
        assertTrue(exposeTool(source(), "get_owner"),
            "Convention : un @Tool dont le nom public est get_owner.");
    }

    @Test
    void resource_suit_le_gabarit_uri() {
        Pattern gabarit = Pattern.compile(
            "@ResourceTemplate\\s*\\([^)]*\\buriTemplate\\s*=\\s*\"service://\\{name\\}\"",
            Pattern.DOTALL);
        assertTrue(gabarit.matcher(source()).find(),
            "Convention : @ResourceTemplate(uriTemplate = \"service://{name}\") ; "
            + "catalogue://{name} ou service/{name} ne sont pas conformes.");
    }

    @Test
    void separe_metier_et_adaptateur() {
        String src = source();
        assertTrue(src.contains("class CatalogueService"),
            "Convention : une classe métier CatalogueService");
        assertTrue(src.contains("class CatalogueMcpServer"),
            "Convention : un adaptateur CatalogueMcpServer distinct");

        String metier = corps(src, "CatalogueService");
        String adaptateur = corps(src, "CatalogueMcpServer");
        assertFalse(Pattern.compile("@(Tool|Resource|ResourceTemplate|Prompt)\\b")
                .matcher(metier).find(),
            "CatalogueService ne doit porter aucune annotation MCP");
        assertFalse(Pattern.compile("catalogue-services\\.json|ObjectMapper|Files\\.read|getResourceAsStream")
                .matcher(adaptateur).find(),
            "CatalogueMcpServer ne doit ni lire le JSON ni porter la logique métier : il délègue");
    }

    /** Corps approximatif d'une classe : du mot-clé class jusqu'à la prochaine
     *  déclaration de classe ou la fin du fichier. Suffisant pour un scorer,
     *  pas pour un analyseur : à dire aux participants. */
    private static String corps(String src, String classe) {
        int debut = src.indexOf("class " + classe);
        if (debut < 0) return "";
        int fin = src.indexOf("\nclass ", debut + 1);
        int finPublic = src.indexOf("\npublic class ", debut + 1);
        if (fin < 0 || (finPublic >= 0 && finPublic < fin)) fin = finPublic;
        return fin < 0 ? src.substring(debut) : src.substring(debut, fin);
    }
}
