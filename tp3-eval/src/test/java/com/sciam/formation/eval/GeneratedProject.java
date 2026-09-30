package com.sciam.formation.eval;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * FOURNI — le serveur généré, vu comme du texte.
 * Chemin : -Dserveur.genere.dir=/chemin/vers/le/serveur (le dossier qui contient pom.xml).
 */
public final class GeneratedProject {

    private final Path racine;

    private GeneratedProject(Path racine) { this.racine = racine; }

    public static GeneratedProject charger() {
        String dir = System.getProperty("serveur.genere.dir", "");
        Path p = Path.of(dir.isBlank() ? "." : dir);
        if (dir.isBlank() || !Files.exists(p.resolve("pom.xml"))) {
            throw new IllegalStateException("Projet généré introuvable : passez -Dserveur.genere.dir=/chemin/vers/le/serveur (dossier contenant pom.xml)");
        }
        return new GeneratedProject(p);
    }

    /** Le pom.xml, tel quel. */
    public String pom() {
        return lire(racine.resolve("pom.xml"));
    }

    /** Toutes les sources de src/main/java, concaténées, commentaires retirés, chaînes préservées.
     *  Les tests du serveur (src/test) sont volontairement exclus : un scorer y trouverait n'importe quoi. */
    public String source() {
        Path main = racine.resolve("src/main/java");
        if (!Files.isDirectory(main)) return "";
        try (Stream<Path> s = Files.walk(main)) {
            StringBuilder sb = new StringBuilder();
            s.filter(f -> f.toString().endsWith(".java")).sorted().forEach(f -> sb.append(lire(f)).append('\n'));
            return sansCommentaires(sb.toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Les sources de src/main/java fichier par fichier (nom simple → contenu sans commentaires).
     *  Pour un juge « System One » : un état JSON nommé plutôt qu'un bloc de texte concaténé. */
    public java.util.Map<String, String> sources() {
        Path main = racine.resolve("src/main/java");
        java.util.Map<String, String> m = new java.util.LinkedHashMap<>();
        if (!Files.isDirectory(main)) return m;
        try (Stream<Path> s = Files.walk(main)) {
            s.filter(f -> f.toString().endsWith(".java")).sorted()
             .forEach(f -> m.put(f.getFileName().toString(), sansCommentaires(lire(f))));
            return m;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Vrai si source() contient une occurrence de la regex (mode DOTALL : « . » traverse les lignes). */
    public boolean contient(String regex) {
        return Pattern.compile(regex, Pattern.DOTALL).matcher(source()).find();
    }

    /** Retire les commentaires bloc et ligne sans toucher aux chaînes : « service://{name} » contient « // ». */
    static String sansCommentaires(String src) {
        Matcher m = Pattern.compile("\"(?:\\\\.|[^\"\\\\])*\"|/\\*.*?\\*/|//[^\\n]*", Pattern.DOTALL).matcher(src);
        StringBuilder sb = new StringBuilder();
        while (m.find()) m.appendReplacement(sb, Matcher.quoteReplacement(m.group().startsWith("\"") ? m.group() : " "));
        m.appendTail(sb);
        return sb.toString();
    }

    private static String lire(Path f) {
        try { return Files.readString(f); } catch (IOException e) { throw new UncheckedIOException(e); }
    }
}
