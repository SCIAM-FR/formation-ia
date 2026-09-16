# Conventions maison — serveur MCP Quarkus (catalogue de services)

Ce document est la **référence de conformité**. Le Skill de TP2 doit l'encoder ; les
scorers de TP3 doivent le vérifier.

## Stack & structure
- Extension : `io.quarkiverse.mcp:quarkus-mcp-server-http` (dev), `-stdio` en option.
- Package racine : `com.sciam.formation.mcp`.
- **Séparation stricte** métier / adaptateur MCP :
  - `CatalogueService` : charge `catalogue-services.json` (classpath) et expose la logique.
  - `CatalogueMcpServer` : ne contient QUE les annotations MCP, délègue au service.

## Primitives (domaine catalogue de services)
- **Tools** (verbes, snake_case) :
  - `find_service(query)` — recherche par nom/équipe.
  - `get_owner(service)` — équipe propriétaire + criticité.
- **Resource** : `service://{name}` → fiche complète (équipe, criticité, dépendances).
- **Prompt** : `fiche_service` — gabarit de synthèse pour l'astreinte.

## Annotations
- `@Tool(description = "…")` sur chaque tool, `@ToolArg(description = "…")` sur chaque argument.
- `@ResourceTemplate(uriTemplate = "service://{name}")` + `@ResourceTemplateArg`.
- `@Prompt` avec un `name` explicite.

## Tests & observabilité
- Au moins un test vérifiant que le serveur démarre et que les primitives s'enregistrent au build.
- Logs applicatifs activés (niveau INFO).
