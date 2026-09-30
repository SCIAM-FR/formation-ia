package com.sciam.formation.eval;

import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * SCORERS DÉTERMINISTES — deux exemples fournis, quatre tests À ÉCRIRE (TP3, étape 2).
 *
 * SRC contient les sources du serveur généré (src/main/java), sans commentaires.
 * Règle du jeu : une assertion vérifie une convention de domaine/conventions.md ; elle doit rester
 * verte sur un serveur conforme et devenir rouge sur une mutation (étape 3).
 */
class ConformiteTest {

    static final GeneratedProject projet = GeneratedProject.charger();
    static final String SRC = projet.source();

    // ---- Exemple 1 : une vérification sur le POM ---------------------------------------------
    @Test
    void utilise_extension_quarkus_mcp() {
        assertTrue(projet.pom().contains("quarkus-mcp-server"),
            "Le pom.xml doit déclarer l'extension quarkus-mcp-server");
    }

    // ---- Exemple 2 : une regex qui exige l'annotation ET le nom sur la même déclaration ---------
    // Chercher juste la chaîne « find_service » ne suffit pas : un log ou un nom de variable la contient aussi.
    @Test
    void expose_les_tools_de_la_demande() {
        for (String nom : List.of("find_service", "get_owner")) {
            assertTrue(exposeTool(nom), "Aucun @Tool nommé « " + nom + " » : attribut name = \"" + nom
                + "\", ou @Tool sans name sur une méthode Java nommée " + nom);
        }
    }

    /** Vrai si un @Tool expose publiquement ce nom : @Tool(name = "x"), ou @Tool sans name sur une méthode x(...). */
    static boolean exposeTool(String nom) {
        String q = Pattern.quote(nom);
        boolean avecName = Pattern.compile("@Tool\\s*\\([^)]*\\bname\\s*=\\s*\"" + q + "\"", Pattern.DOTALL).matcher(SRC).find();
        boolean methode = Pattern.compile("@Tool\\b(?:\\s*\\((?![^)]*\\bname\\s*=)[^)]*\\))?\\s*(?:@\\w+(?:\\([^)]*\\))?\\s*)*"
            + "(?:public\\s+|protected\\s+)?[\\w<>\\[\\],\\s]+?\\s+" + q + "\\s*\\(", Pattern.DOTALL).matcher(SRC).find();
        return avecName || methode;
    }

    // ---- À ÉCRIRE ---------------------------------------------------------------------------
    @Test
    void tools_nommes_en_snake_case() {
        // TODO : pour chaque @Tool, le nom public (attribut name, sinon nom de la méthode) matche ^[a-z][a-z0-9_]*$.
        //        Un seul « findService » doit faire échouer le test.
        fail("À écrire : tools en snake_case");
    }

    @Test
    void tools_et_arguments_decrits() {
        // TODO : chaque @Tool et chaque @ToolArg porte une description non vide. Fixez un minimum de caractères.
        fail("À écrire : descriptions sur @Tool et @ToolArg");
    }

    @Test
    void resource_template_parametree_et_decrite() {
        // TODO : au moins une @ResourceTemplate dont l'uriTemplate contient {…}, avec un @ResourceTemplateArg décrit.
        fail("À écrire : resource template paramétrée et décrite");
    }

    @Test
    void separe_metier_et_adaptateur() {
        // TODO : les classes qui portent @Tool / @Resource / @Prompt ne chargent pas le JSON (ObjectMapper, getResourceAsStream…)
        //        et délèguent à une classe métier injectée ; une classe sans annotation MCP charge les données.
        fail("À écrire : séparation métier / adaptateur");
    }
}
