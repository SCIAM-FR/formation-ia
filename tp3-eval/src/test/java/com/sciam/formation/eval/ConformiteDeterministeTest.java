package com.sciam.formation.eval;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * CONVENTIONS DU SKILL — SCORERS DÉTERMINISTES À COMPLÉTER (TP3).
 *
 * Ici on vérifie ce que le SKILL impose, quel que soit le domaine du serveur :
 * nommage, descriptions, structure. Les noms propres au catalogue (find_service,
 * get_owner, service://{name}) relèvent de la DEMANDE : ils sont vérifiés par
 * ContratDemandeTest, fourni et piloté par -Dcas=&lt;id&gt;.
 *
 * Un exemple est fourni ; écrivez les quatre suivants. Réf : domaine/conventions.md.
 * Indices : projet.sourceJava() concatène tous les .java, tests compris ;
 * projet.sourceContient(regex) applique une regex DOTALL. Retirez les commentaires
 * avant de chercher (attention : « service://{name} » contient « // », ne cassez pas
 * les chaînes), et exigez l'annotation ET le nom sur la même déclaration.
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
    void tools_nommes_en_snake_case() {
        // TODO : chaque @Tool expose un nom public en snake_case (^[a-z][a-z0-9_]*$),
        //        qu'il vienne de l'attribut name ou du nom de la méthode Java.
        //        Un seul tool en camelCase doit faire échouer le test, quel que soit le domaine.
        fail("À écrire : tools en snake_case");
    }

    @Test
    void tools_et_arguments_decrits() {
        // TODO : chaque @Tool porte une description ; chaque @ToolArg porte une description.
        //        Indice : compter les @Tool( et les @Tool(...description = ...) ne suffit pas
        //        si une description vide passe ; définissez ce qu'est une description acceptable.
        fail("À écrire : descriptions sur @Tool et @ToolArg");
    }

    @Test
    void resource_template_parametree_et_decrite() {
        // TODO : au moins une @ResourceTemplate dont l'uriTemplate contient un paramètre {…},
        //        avec un @ResourceTemplateArg décrit. L'URI exacte est l'affaire du contrat, pas d'ici.
        fail("À écrire : resource template paramétrée et décrite");
    }

    @Test
    void separe_metier_et_adaptateur() {
        // TODO : une classe métier (charge les données, porte la logique) distincte de l'adaptateur
        //        MCP (annotations, délégation). Indices : aucune annotation MCP dans la classe métier ;
        //        aucun chargement de JSON dans l'adaptateur.
        fail("À écrire : séparation métier / adaptateur");
    }
}
