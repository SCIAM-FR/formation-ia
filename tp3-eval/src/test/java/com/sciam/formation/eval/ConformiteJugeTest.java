package com.sciam.formation.eval;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import static org.junit.jupiter.api.Assertions.*;

/**
 * SCORER LLM-as-a-Judge — À COMPLÉTER (TP3).
 * Le plombier (JugeLLM) est fourni ; VOUS écrivez la GRILLE.
 * Ne s'active que si le modèle est câblé (LLM_ENDPOINT défini).
 */
@EnabledIfEnvironmentVariable(named = "LLM_ENDPOINT", matches = ".+")
class ConformiteJugeTest {

    private final GeneratedProject projet = GeneratedProject.charger();
    private final JugeLLM juge = new JugeLLM();

    @Test
    void conformite_nuancee_du_serveur() {
        // TODO : rédigez et affinez la grille. Exigez une note /10.
        String grille = """
            Tu es relecteur d'architecture. Évalue ce serveur MCP Quarkus selon les
            conventions maison SCIAM :
            - séparation métier / adaptateur MCP,
            - tools en snake_case (find_service, get_owner),
            - resource au gabarit service://{name},
            - descriptions présentes sur @Tool et @ToolArg.
            TODO : complétez / pondérez ces critères.
            """;

        JugeLLM.Verdict v = juge.noter(grille, projet.sourceJava());
        assertTrue(v.note() >= 7,
            "Conformité insuffisante (" + v.note() + "/10) : " + v.justification());
    }
}
