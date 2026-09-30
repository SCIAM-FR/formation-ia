package com.sciam.formation.eval;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * CORRIGÉ FORMATEUR — une réponse possible aux quatre stubs de ConformiteTest (TP3, étape 2).
 * Réf : domaine/conventions.md. À utiliser au débrief, pas à distribuer avant.
 *
 * Ces assertions ne citent aucun nom du catalogue : elles restent vertes sur un serveur
 * conforme de n'importe quel domaine. Choix de conception : raisonner déclaration par
 * déclaration (une annotation et ce qui la suit) sur GeneratedProject.src, qui ne
 * contient que src/main/java, sans commentaires.
 */
class ConformiteCorrige {

    private final GeneratedProject projet = GeneratedProject.charger();
    private final String src = projet.source();

    private static final Pattern SNAKE = Pattern.compile("^[a-z][a-z0-9_]*$");


    /** Une annotation @Tool et la déclaration de méthode qui la suit : (attributs, nom de méthode). */
    private static final Pattern TOOL_DECL = Pattern.compile(
        "@Tool\\b(?:\\s*\\(([^)]*)\\))?\\s*(?:@\\w+(?:\\([^)]*\\))?\\s*)*"
        + "(?:public\\s+|protected\\s+)?[\\w<>\\[\\],\\s]+?\\s+(\\w+)\\s*\\(", Pattern.DOTALL);

    private static String nomPublic(Matcher tool) {
        String attributs = tool.group(1) == null ? "" : tool.group(1);
        Matcher name = Pattern.compile("\\bname\\s*=\\s*\"([^\"]*)\"").matcher(attributs);
        return name.find() ? name.group(1) : tool.group(2);
    }

    // --- Les quatre stubs -----------------------------------------------------
    @Test
    void tools_nommes_en_snake_case() {
        Matcher m = TOOL_DECL.matcher(src);
        int n = 0;
        while (m.find()) {
            n++;
            String nom = nomPublic(m);
            assertTrue(SNAKE.matcher(nom).matches(),
                "Convention : les noms publics de tools sont en snake_case ; trouvé « " + nom
                + " » (attribut name de @Tool, ou nom de la méthode sans attribut name).");
        }
        assertTrue(n > 0, "Aucun @Tool trouvé : rien à vérifier, ce n'est pas un serveur MCP conforme.");
    }

    @Test
    void tools_et_arguments_decrits() {
        Matcher tool = Pattern.compile("@Tool\\b(?:\\s*\\(([^)]*)\\))?").matcher(src);
        int n = 0;
        while (tool.find()) {
            n++;
            String attributs = tool.group(1) == null ? "" : tool.group(1);
            assertTrue(Pattern.compile("\\bdescription\\s*=\\s*\"[^\"]{10,}\"").matcher(attributs).find(),
                "Convention : chaque @Tool porte une description d'au moins dix caractères ; manquante ou vide sur « @Tool(" + attributs.trim() + ") ».");
        }
        assertTrue(n > 0, "Aucun @Tool trouvé.");
        Matcher arg = Pattern.compile("@ToolArg\\b(?:\\s*\\(([^)]*)\\))?").matcher(src);
        while (arg.find()) {
            String attributs = arg.group(1) == null ? "" : arg.group(1);
            assertTrue(Pattern.compile("\\bdescription\\s*=\\s*\"[^\"]{3,}\"").matcher(attributs).find(),
                "Convention : chaque @ToolArg porte une description ; manquante sur « @ToolArg(" + attributs.trim() + ") ».");
        }
    }

    @Test
    void resource_template_parametree_et_decrite() {
        Matcher rt = Pattern.compile("@ResourceTemplate\\s*\\(([^)]*)\\)", Pattern.DOTALL).matcher(src);
        assertTrue(rt.find(), "Convention : au moins une @ResourceTemplate.");
        Matcher uri = Pattern.compile("\\buriTemplate\\s*=\\s*\"([^\"]*)\"").matcher(rt.group(1));
        assertTrue(uri.find() && uri.group(1).matches(".*\\{\\w+\\}.*"),
            "Convention : l'uriTemplate d'une @ResourceTemplate contient un paramètre {…} ; trouvé « " + rt.group(1).trim() + " ».");
        assertTrue(Pattern.compile("@ResourceTemplateArg\\s*\\([^)]*\\bdescription\\s*=\\s*\"[^\"]{3,}\"", Pattern.DOTALL).matcher(src).find(),
            "Convention : l'argument de la resource est annoté @ResourceTemplateArg avec une description.");
    }

    @Test
    void separe_metier_et_adaptateur() {
        // L'adaptateur : la ou les classes qui portent des annotations MCP.
        Matcher classe = Pattern.compile("\\bclass\\s+(\\w+)\\b").matcher(src);
        int adaptateurs = 0, metiers = 0;
        while (classe.find()) {
            String corps = corps(src, classe.start());
            boolean mcp = Pattern.compile("@(Tool|Resource|ResourceTemplate|Prompt)\\b").matcher(corps).find();
            boolean chargeJson = Pattern.compile("\\.json\"|ObjectMapper|Files\\.read|getResourceAsStream|Json\\w*\\.parse").matcher(corps).find();
            if (mcp) {
                adaptateurs++;
                assertFalse(chargeJson,
                    "Convention : la classe MCP « " + classe.group(1) + " » ne doit pas charger les données ni porter la logique : elle délègue.");
                assertTrue(Pattern.compile("@Inject\\b|private\\s+final\\s+\\w+Service\\b").matcher(corps).find(),
                    "Convention : l'adaptateur « " + classe.group(1) + " » délègue à un service métier injecté.");
            } else if (chargeJson) {
                metiers++;
                assertFalse(Pattern.compile("@(Tool|Resource|ResourceTemplate|Prompt)\\b").matcher(corps).find(),
                    "Convention : la classe métier « " + classe.group(1) + " » ne porte aucune annotation MCP.");
            }
        }
        assertTrue(adaptateurs >= 1, "Convention : au moins une classe adaptateur MCP.");
        assertTrue(metiers >= 1, "Convention : une classe métier distincte charge les données (aucune trouvée).");
    }

    /** Corps approximatif d'une classe : de « class X » jusqu'à la prochaine déclaration de classe
     *  ou la fin. Suffisant pour un scorer, pas pour un analyseur : à dire aux participants. */
    private static String corps(String src, int debut) {
        Matcher suivante = Pattern.compile("\\bclass\\s+\\w+\\b").matcher(src);
        int fin = src.length();
        if (suivante.find(debut + 6)) fin = suivante.start();
        return src.substring(debut, fin);
    }
}
