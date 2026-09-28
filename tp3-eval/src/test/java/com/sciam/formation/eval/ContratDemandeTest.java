package com.sciam.formation.eval;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CONTRAT DE LA DEMANDE — FOURNI, paramétré par le cas du dataset (-Dcas=&lt;id&gt;).
 *
 * Ces tests vérifient ce que la DEMANDE impose (les noms publics du catalogue :
 * find_service, get_owner, service://{name}, fiche_service), pas ce que le SKILL
 * impose. Les conventions du Skill, valables pour n'importe quel domaine, sont dans
 * ConformiteDeterministeTest : c'est là que vous écrivez.
 *
 * Sans -Dcas, cette classe est ignorée : le harnais n'évalue alors que les conventions.
 * Les regex ignorent les commentaires ; elles exigent l'annotation ET le nom public sur
 * la même déclaration, ce qu'une simple recherche de chaîne ne fait pas (voir la
 * démonstration du scorer trompeur, M4).
 */
@EnabledIfSystemProperty(named = "cas", matches = ".+")
class ContratDemandeTest {

    private final GeneratedProject projet = GeneratedProject.charger();
    private final CasAttendu cas = CasAttendu.depuisPropriete().orElseThrow();

    /** Retire les commentaires sans toucher aux chaînes : « service://{name} » contient « // ». */
    static String sansCommentaires(String src) {
        Matcher m = Pattern.compile("\"(?:\\\\.|[^\"\\\\])*\"|/\\*.*?\\*/|//[^\\n]*", Pattern.DOTALL).matcher(src);
        StringBuilder sb = new StringBuilder();
        while (m.find()) m.appendReplacement(sb, Matcher.quoteReplacement(m.group().startsWith("\"") ? m.group() : " "));
        m.appendTail(sb);
        return sb.toString();
    }

    private String source() {
        return sansCommentaires(projet.sourceJava());
    }

    /** Vrai si un @Tool porte publiquement ce nom : attribut name, ou méthode Java de ce nom sans attribut name. */
    static boolean exposeTool(String src, String nom) {
        String q = Pattern.quote(nom);
        boolean avecName = Pattern.compile("@Tool\\s*\\([^)]*\\bname\\s*=\\s*\"" + q + "\"", Pattern.DOTALL)
            .matcher(src).find();
        boolean methode = Pattern.compile(
            "@Tool\\b(?:\\s*\\((?![^)]*\\bname\\s*=)[^)]*\\))?\\s*(?:@\\w+(?:\\([^)]*\\))?\\s*)*"
            + "(?:public\\s+|protected\\s+)?[\\w<>\\[\\],\\s]+?\\s+" + q + "\\s*\\(", Pattern.DOTALL)
            .matcher(src).find();
        return avecName || methode;
    }

    @Test
    void tools_de_la_demande_exposes() {
        String src = source();
        for (String tool : cas.contrat().tools()) {
            assertTrue(exposeTool(src, tool),
                "Cas " + cas.id() + " : la demande attend un tool nommé « " + tool
                + " » (attribut name de @Tool, ou méthode de ce nom). " + cas.commentaire());
        }
        if (cas.contrat().tools().isEmpty()) {
            assertTrue(Pattern.compile("@Tool\\b").matcher(src).find(),
                "Cas " + cas.id() + " : aucun nom imposé, mais au moins un @Tool est attendu. " + cas.commentaire());
        }
    }

    @Test
    void resource_de_la_demande_exposee() {
        cas.contrat().resource().ifPresent(uri ->
            assertTrue(Pattern.compile("@ResourceTemplate\\s*\\([^)]*\\buriTemplate\\s*=\\s*\""
                    + Pattern.quote(uri) + "\"", Pattern.DOTALL).matcher(source()).find(),
                "Cas " + cas.id() + " : la demande attend une @ResourceTemplate dont l'uriTemplate vaut « " + uri + " »."));
    }

    @Test
    void prompt_de_la_demande_expose() {
        cas.contrat().prompt().ifPresent(nom -> {
            String q = Pattern.quote(nom);
            boolean avecName = Pattern.compile("@Prompt\\s*\\([^)]*\\bname\\s*=\\s*\"" + q + "\"", Pattern.DOTALL)
                .matcher(source()).find();
            boolean methode = Pattern.compile("@Prompt\\b(?:\\s*\\([^)]*\\))?\\s*(?:@\\w+(?:\\([^)]*\\))?\\s*)*"
                + "(?:public\\s+|protected\\s+)?[\\w<>\\[\\],\\s]+?\\s+" + q + "\\s*\\(", Pattern.DOTALL)
                .matcher(source()).find();
            assertTrue(avecName || methode,
                "Cas " + cas.id() + " : la demande attend un @Prompt nommé « " + nom + " ».");
        });
    }
}
