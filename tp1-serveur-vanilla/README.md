# TP1 — Bâtir un serveur MCP Quarkus avec OpenCode

**[Guide détaillé sur GitHub Pages](https://sciam-fr.github.io/formation-ia/tp1/)**
· [Source du guide](../docs/tp1.md)

**Objectif** : piloter OpenCode pour produire un serveur MCP en Quarkus. Vous **n'écrivez
pas de Java à la main** : vous promptez, vous relisez, vous validez.

## Deux parcours

| Parcours | Sections du guide | Preuve de sortie |
| --- | --- | --- |
| **Essentiel — 1 h 30 cible** | 1 à 6 | Un serveur vanilla construit et démarré, appels MCP vérifiés, comparaison avec un voisin, baseline et écarts éventuels conservés |
| **Approfondissement** | 5 | Une seconde génération individuelle indépendante comparée à la première |

Les approfondissements se font hors des 14 h de formation ou si vous avez de
l'avance, sans supprimer l'essentiel. La cible suppose un environnement prêt.

## Consigne

Faites générer par OpenCode un serveur MCP Quarkus qui, sur le domaine
`catalogue-services.json`, expose :

- un **tool** `find_service`,
- un **tool** `get_owner`,
- une **resource** `service://{name}`,
- un **prompt** `fiche_service`.

> À ce stade, **aucun Skill** : OpenCode ne s'appuie que sur le modèle de base et vos
> prompts. C'est voulu.

## Ce que vous validez

1. Le build et les tests passent (`mvn test`), puis le serveur **démarre**
   (`mvn quarkus:dev`).
2. Les deux tools, la resource paramétrée et le prompt sont exposés avec le contrat
   attendu ; un build vert ne suffit pas à le prouver.
3. Vous vérifiez les appels avec l'inspecteur MCP du dev mode ou un client compatible.

## Le vrai livrable : le constat

Mettez vos serveurs côte à côte avec vos voisins. Comparez structure, choix de
transport, nommage et annotations : des divergences **peuvent** apparaître.
Consignez les écarts prouvés, ou leur absence ; conservez votre baseline.

> Le modèle donne de la **capacité**, pas une garantie de **conformité** ni
> d'**uniformité**.

C'est le déclencheur de TP2 : on va distiller la conformité dans un Skill.

## Note formateur

Ne vendez pas TP1 comme « ça échoue » ni comme une non-conformité assurée : le modèle
peut déjà respecter les conventions maison (`domaine/conventions.md`).
Le point pédagogique est de **mesurer**, pas de provoquer l'échec ou la divergence.
