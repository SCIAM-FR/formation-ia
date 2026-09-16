# TP1 — Bâtir un serveur MCP Quarkus avec OpenCode

**Objectif** : piloter OpenCode pour produire un serveur MCP en Quarkus. Vous **n'écrivez
pas de Java à la main** : vous promptez, vous relisez, vous validez.

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

1. Le serveur **démarre** (`quarkus dev`).
2. Les primitives **s'enregistrent au build** (atout Quarkus : une erreur de signature
   explose à la compilation).
3. Vous inspectez la sortie avec l'inspecteur MCP du dev mode.

## Le vrai livrable : le constat

Mettez vos serveurs côte à côte avec vos voisins. **Ça marche — mais chacun a un résultat
différent** : structure, choix de transport, nommage, annotations qui divergent.

> Le modèle donne de la **capacité**, pas de la **conformité** ni de l'**uniformité**.

C'est le déclencheur de TP2 : on va distiller la conformité dans un Skill.

## Note formateur

Ne vendez pas TP1 comme « ça échoue » (un bon modèle produira un serveur correct). Cadrez
sur la **non-conformité** : divergence entre participants, écarts aux conventions maison
(`domaine/conventions.md`). La divergence est le point pédagogique, pas l'échec.
