package com.sciam.formation.eval;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * EXEMPLE FORMATEUR — une vérification qui résiste au projet piège.
 *
 * À montrer en démonstration (M4), pas à copier dans tp3-eval/ : les participants
 * écrivent leurs propres assertions. Le principe : ne chercher que dans src/main,
 * retirer les commentaires, puis exiger l'annotation @Tool ET le nom public sur la
 * même déclaration.
 */
final class ScorerRobusteExemple {

    private static final Pattern COMMENTAIRES =
        Pattern.compile("/\\*.*?\\*/|//[^\\n]*", Pattern.DOTALL);

    /** Sources de production seulement, commentaires retirés. */
    static String sourceMainSansCommentaires(Path racine) {
        try (Stream<Path> s = Files.walk(racine.resolve("src/main/java"))) {
            StringBuilder sb = new StringBuilder();
            s.filter(f -> f.toString().endsWith(".java"))
             .forEach(f -> sb.append(lire(f)).append('\n'));
            return COMMENTAIRES.matcher(sb).replaceAll(" ");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Vrai si un tool MCP porte publiquement le nom demandé :
     * soit @Tool(name = "find_service"), soit @Tool sans name sur une méthode
     * Java nommée find_service (l'extension expose alors le nom de la méthode).
     */
    static boolean exposeTool(String source, String nom) {
        String q = Pattern.quote(nom);
        Pattern avecName = Pattern.compile(
            "@Tool\\s*\\([^)]*\\bname\\s*=\\s*\"" + q + "\"", Pattern.DOTALL);
        Pattern sansName = Pattern.compile(
            "@Tool\\b(?:\\s*\\((?![^)]*\\bname\\s*=)[^)]*\\))?\\s*(?:@\\w+(?:\\([^)]*\\))?\\s*)*"
            + "(?:public\\s+|protected\\s+)?[\\w<>\\[\\],\\s]+?\\s+" + q + "\\s*\\(",
            Pattern.DOTALL);
        return avecName.matcher(source).find() || sansName.matcher(source).find();
    }

    private static String lire(Path f) {
        try {
            return Files.readString(f);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
