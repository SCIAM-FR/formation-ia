# Corrigé possible des assertions déterministes (TP3)

`ConformiteDeterministeCorrige.java` propose **une** manière de compléter les quatre
stubs de **conventions** de `tp3-eval/src/test/java/com/sciam/formation/eval/ConformiteDeterministeTest.java`.
Les noms du catalogue n'y figurent pas : ils relèvent du contrat de la demande,
vérifié par `ContratDemandeTest` (fourni, `-Dcas=<id>`).
Il sert au débrief et au dépannage ; il ne remplace pas le travail des participants
et ne doit pas être copié dans le squelette du dépôt.

Pour l'essayer sur une génération, copiez-le **hors du dépôt** dans une copie de
`tp3-eval`, à côté de `GeneratedProject.java`, puis :

```bash
mvn -Dtest='ConformiteDeterministeCorrige,ContratDemandeTest' -Dcas=happy-1 \
  -Dserveur.genere.dir="$ATELIER_DIR/serveur-avec-skill" test
```

## Limites à faire nommer par les participants

| Assertion | Ce qu'elle prouve | Ce qu'elle ne prouve pas |
| --- | --- | --- |
| `tools_nommes_en_snake_case` | Chaque `@Tool` expose un nom public en snake_case (attribut `name`, sinon nom de méthode), commentaires retirés | Que le nom est celui que la demande voulait : c'est le contrat |
| `tools_et_arguments_decrits` | Une description d'au moins dix caractères sur chaque `@Tool`, d'au moins trois sur chaque `@ToolArg` | Qu'une description est utile à un agent |
| `resource_template_parametree_et_decrite` | Une `@ResourceTemplate` dont l'URI contient `{…}`, avec un `@ResourceTemplateArg` décrit | L'URI exacte (`service://{name}`) : contrat |
| `separe_metier_et_adaptateur` | Les classes annotées MCP ne chargent pas de JSON et injectent un service ; une classe non annotée charge les données | Une séparation réellement propre : une regex ne juge pas une architecture |
| `ContratDemandeTest` (fourni) | Les noms de `attendu.contrat` du cas, avec annotation et nom sur la même déclaration | Que le tool répond correctement ; que l'inspecteur l'affiche (build/MCP à vérifier séparément) |

Ces limites sont exactement ce que la grille du juge (section 5) et les vérifications
build/MCP doivent couvrir. Les tests restent verts sur une génération conforme aux
[conventions](../../domaine/conventions.md) et rouges sur les mutations de la
section 4 du guide TP3 : `findService` au lieu de `find_service` (convention snake_case
et contrat), description retirée (convention), chargement JSON déplacé dans
l'adaptateur (convention). La mutation `catalogue://{name}` n'est détectée que par le
contrat : c'est voulu, l'URI n'est pas une convention du Skill.

Vérifié le 28 septembre 2026 sur un projet conforme minimal et sur le projet piège :
corrigé et contrat verts sur le conforme, `findService` détecté par les deux familles,
`realistic-2` (aucun nom imposé) vert sur le conforme, piège rouge sur la resource et
la séparation.
