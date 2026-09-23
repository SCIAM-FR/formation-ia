# Corrigé possible des assertions déterministes (TP3)

`ConformiteDeterministeCorrige.java` propose **une** manière de compléter les quatre
stubs de `tp3-eval/src/test/java/com/sciam/formation/eval/ConformiteDeterministeTest.java`.
Il sert au débrief et au dépannage ; il ne remplace pas le travail des participants
et ne doit pas être copié dans le squelette du dépôt.

Pour l'essayer sur une génération, copiez-le **hors du dépôt** dans une copie de
`tp3-eval`, à côté de `GeneratedProject.java`, puis :

```bash
mvn -Dtest=ConformiteDeterministeCorrige \
  -Dserveur.genere.dir="$ATELIER_DIR/serveur-avec-skill" test
```

## Limites à faire nommer par les participants

| Assertion | Ce qu'elle prouve | Ce qu'elle ne prouve pas |
| --- | --- | --- |
| `expose_tool_find_service` / `get_owner` | Une annotation `@Tool` avec ce nom public, hors commentaires et hors tests | Que le tool répond correctement ; que l'inspecteur l'affiche (build/MCP à vérifier séparément) |
| `resource_suit_le_gabarit_uri` | Un `@ResourceTemplate` dont l'`uriTemplate` vaut exactement `service://{name}` | Que la resource renvoie la fiche complète |
| `separe_metier_et_adaptateur` | Deux classes distinctes ; les annotations MCP hors de `CatalogueService` ; la lecture JSON hors de l'adaptateur | Une séparation réellement propre : une regex ne juge pas une architecture |

Ces limites sont exactement ce que la grille du juge (section 5) et les vérifications
build/MCP doivent couvrir. Les tests restent verts sur une génération conforme aux
[conventions](../../domaine/conventions.md) et rouges sur les mutations de la
section 4 du guide TP3 : `findService` au lieu de `find_service`,
`catalogue://{name}`, chargement JSON déplacé dans l'adaptateur.
