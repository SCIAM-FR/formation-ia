package com.sciam.formation.eval;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * PLOMBIER FOURNI — à ne pas modifier.
 * Charge le projet serveur généré (OpenCode + Skill) pour l'inspecter.
 * Chemin via -Dserveur.genere.dir=... (défaut : ../serveur-genere).
 */
public final class GeneratedProject {

    private final Path racine;

    private GeneratedProject(Path racine) { this.racine = racine; }

    public static GeneratedProject charger() {
        Path p = Path.of(System.getProperty("serveur.genere.dir", "../serveur-genere"));
        if (!Files.isDirectory(p)) {
            throw new IllegalStateException("Projet généré introuvable : " + p.toAbsolutePath()
                + " (passez -Dserveur.genere.dir=/chemin/vers/le/serveur)");
        }
        return new GeneratedProject(p);
    }

    /** Concatène tout le code source Java du projet généré. */
    public String sourceJava() {
        try (Stream<Path> s = Files.walk(racine)) {
            StringBuilder sb = new StringBuilder();
            s.filter(f -> f.toString().endsWith(".java")).forEach(f -> sb.append(lire(f)).append('\n'));
            return sb.toString();
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }

    public String pom() {
        Path pom = racine.resolve("pom.xml");
        return Files.exists(pom) ? lire(pom) : "";
    }

    /** true si le code source matche l'expression régulière (multiligne). */
    public boolean sourceContient(String regex) {
        return Pattern.compile(regex, Pattern.DOTALL).matcher(sourceJava()).find();
    }

    private static String lire(Path f) {
        try { return Files.readString(f); } catch (IOException e) { throw new UncheckedIOException(e); }
    }
}
