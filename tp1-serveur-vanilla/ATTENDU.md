# Référence formateur — à quoi ressemble un serveur *conforme*

Sert à pointer les divergences en fin de TP1. La cible complète est
`domaine/conventions.md`. Points d'attention fréquents où les générations divergent :

- **Structure** : métier (`CatalogueService`) mêlé à l'adaptateur MCP au lieu d'être séparé.
- **Transport** : stdio codé en dur vs `quarkus-mcp-server-http` ; parfois les deux.
- **Nommage** : `findService` / `searchService` au lieu de `find_service`.
- **Resource** : URI `service/{name}` ou `catalogue://{name}` au lieu de `service://{name}`.
- **Annotations** : `@Tool` sans `description`, arguments sans `@ToolArg`.
- **Données** : chemin en dur vers le JSON au lieu d'un chargement classpath.

Ce sont exactement les écarts que le Skill de TP2 devra fermer, et que les scorers de TP3
devront détecter.
