package com.sciam.formation.eval;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * SCORERS DÉTERMINISTES — À COMPLÉTER (TP3).
 * Un exemple est fourni ; écrivez les suivants. Réf : domaine/conventions.md
 */
class ConformiteDeterministeTest {

    private final GeneratedProject projet = GeneratedProject.charger();

    // --- Exemple fourni ---------------------------------------------------
    @Test
    void utilise_extension_quarkus_mcp() {
        assertTrue(projet.pom().contains("quarkus-mcp-server"),
            "Le pom doit déclarer l'extension quarkus-mcp-server");
    }

    // --- À COMPLÉTER ------------------------------------------------------
    @Test
    void expose_tool_find_service() {
        // TODO : vérifier un @Tool nommé find_service
        // Indice : projet.sourceContient("@Tool[\\s\\S]*find_service")
        fail("À écrire : tool find_service");
    }

    @Test
    void expose_tool_get_owner() {
        // TODO : idem pour get_owner
        fail("À écrire : tool get_owner");
    }

    @Test
    void resource_suit_le_gabarit_uri() {
        // TODO : @ResourceTemplate(uriTemplate = "service://{name}")
        fail("À écrire : gabarit d'URI service://{name}");
    }

    @Test
    void separe_metier_et_adaptateur() {
        // TODO : un CatalogueService distinct de l'adaptateur MCP
        fail("À écrire : séparation métier / adaptateur");
    }
}
