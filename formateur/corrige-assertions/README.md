# Corrigé possible des assertions déterministes (TP3)

`ConformiteCorrige.java` propose **une** manière de compléter les quatre stubs de
`tp3-eval/src/test/java/com/sciam/formation/eval/ConformiteTest.java`. Les noms du
catalogue n'y figurent pas : ils relèvent de l'exemple fourni `expose_les_tools_de_la_demande`.
Il sert au débrief et au dépannage ; il ne remplace pas le travail des participants
et ne doit pas être copié dans le squelette du dépôt.

Pour l'essayer sur une génération, copiez-le **hors du dépôt** dans une copie de
`tp3-eval`, à côté de `GeneratedProject.java`, puis :

```bash
mvn -Dtest=ConformiteCorrige -Dserveur.genere.dir="$ATELIER_DIR/serveur-avec-skill" test
```

## Limites à faire nommer par les participants

| Assertion | Ce qu'elle prouve | Ce qu'elle ne prouve pas |
| --- | --- | --- |
| `tools_nommes_en_snake_case` | Chaque `@Tool` expose un nom public en snake_case (attribut `name`, sinon nom de méthode), commentaires retirés | Que le nom est celui que la demande voulait : c'est le contrat |
| `tools_et_arguments_decrits` | Une description d'au moins dix caractères sur chaque `@Tool`, d'au moins trois sur chaque `@ToolArg` | Qu'une description est utile à un agent |
| `resource_template_parametree_et_decrite` | Une `@ResourceTemplate` dont l'URI contient `{…}`, avec un `@ResourceTemplateArg` décrit | L'URI exacte (`service://{name}`) : contrat |
| `separe_metier_et_adaptateur` | Les classes annotées MCP ne chargent pas de JSON et injectent un service ; une classe non annotée charge les données | Une séparation réellement propre : une regex ne juge pas une architecture |
| `expose_les_tools_de_la_demande` (exemple fourni) | `find_service` et `get_owner` exposés, annotation et nom sur la même déclaration | Que le tool répond correctement ; que l'inspecteur l'affiche (build/MCP à vérifier séparément) |

Ces limites sont exactement ce que la rubric du juge (étape 4 du guide TP3) et les vérifications
build/MCP doivent couvrir. Les tests restent verts sur une génération conforme aux
[conventions](../../domaine/conventions.md) et rouges sur les mutations de
l'étape 3 du guide TP3 : `findService` au lieu de `find_service` (convention snake_case
et exemple fourni), description retirée, chargement JSON déplacé dans l'adaptateur.
La mutation `catalogue://{name}` n'est détectée par aucune convention : c'est voulu,
l'URI n'est pas une convention du Skill.

Vérifié le 28 septembre 2026 sur un projet conforme minimal et sur le projet piège :
corrigé vert sur le conforme, `findService` détecté, piège rouge sur la resource et la
séparation. Harnais réduit à quatre fichiers le 29 septembre 2026, corrigé recompilé
et rejoué sur le projet piège ce jour-là.
