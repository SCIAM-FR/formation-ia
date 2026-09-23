package com.sciam.formation.mcp;

import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

/**
 * PROJET PIÈGE — démonstration formateur.
 *
 * Ce serveur est censé exposer le tool find_service, mais il ne le fait pas :
 * la seule méthode annotée @Tool s'appelle « chercher ». Le nom find_service
 * n'apparaît que dans ce commentaire, dans un log et dans un nom de test.
 */
public class CatalogueMcpServer {

    private static final Logger LOG = Logger.getLogger(CatalogueMcpServer.class);

    @Inject
    CatalogueService catalogue;

    @Tool(description = "Recherche un service par nom ou par équipe")
    public String chercher(@ToolArg(description = "Nom ou équipe") String query) {
        LOG.infof("appel de find_service (alias chercher) avec %s", query);
        return catalogue.rechercher(query);
    }
}
