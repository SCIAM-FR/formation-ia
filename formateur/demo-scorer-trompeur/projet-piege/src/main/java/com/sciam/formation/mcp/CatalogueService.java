package com.sciam.formation.mcp;

import jakarta.enterprise.context.ApplicationScoped;

/** PROJET PIÈGE — métier minimal, sans lien avec la démonstration. */
@ApplicationScoped
public class CatalogueService {

    public String rechercher(String query) {
        return "[]";
    }
}
