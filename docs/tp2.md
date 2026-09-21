---
title: TP2 — Distiller les conventions dans un Skill
description: Transformer les écarts du TP1 en instructions réutilisables, puis comparer une génération neuve à la baseline.
permalink: /tp2/
previous_url: /tp1/
previous_title: TP1 — Générer
next_url: /tp3/
next_title: TP3 — Évaluer
---

## Objectif et livrables

Produisez un **Skill découvrable par OpenCode**, une **génération indépendante**
et une **comparaison argumentée avec TP1**. Il ne s'agit pas de recopier un cours
Java : gardez le *delta*, c'est-à-dire les instructions nécessaires pour combler
les écarts observés.

Entrées : votre baseline et son tableau d'écarts.
Fichier à compléter :
[`tp2-skill/skills/create-quarkus-mcp-server/SKILL.md`]({{ site.repository_url }}/blob/main/tp2-skill/skills/create-quarkus-mcp-server/SKILL.md).

## 1. Relier chaque écart à une règle

Relisez les [conventions maison]({{ site.repository_url }}/blob/main/domaine/conventions.md).
Pour chaque écart du TP1, écrivez une instruction observable plutôt qu'une intention
vague :

| Écart observé | Question à résoudre dans le Skill |
| --- | --- |
| Métier et MCP mélangés | Quelle classe charge le catalogue ? Quelle classe délègue ? |
| Transport différent | Quelle dépendance HTTP faut-il utiliser ? |
| Noms publics variables | Quels noms exacts l'inspecteur doit-il afficher ? |
| Arguments mal décrits | Quelles annotations et descriptions exiger ? |
| Chemin local codé en dur | Où placer le JSON et comment le charger ? |

Évitez « fais du code propre ». Une règle doit permettre au participant du TP3 de
concevoir une vérification. N'imposez pas de choix supplémentaires absents des
conventions sans les discuter avec le formateur.

## 2. Compléter le squelette

Dans votre clone des supports, ouvrez `SKILL.md` et complétez ses rubriques :

1. **Structure imposée** : package racine, responsabilités de `CatalogueService`
   et de `CatalogueMcpServer`, données sur le classpath.
2. **Extension et transport** : dépendance MCP HTTP, stdio seulement en option ;
   versions compatibles avec la stack retenue.
3. **Primitives et annotations** : noms publics exacts, paramètres et descriptions,
   template de resource et nom explicite du prompt.
4. **Checklist** : ce que l'agent doit construire, lancer et inspecter avant
   d'annoncer un résultat.

Conservez le frontmatter YAML avec `name: create-quarkus-mcp-server` et une
`description` expliquant **quand** charger le Skill. Le nom doit correspondre au
répertoire. La description sert au choix du Skill ; le corps porte les instructions.

Relisez votre texte à voix haute : chaque règle réduit-elle un écart, ou répète-t-elle
une capacité déjà acquise par le modèle ? Gardez les détails de référence dans
`references/conventions-quarkus-mcp.md` plutôt que de multiplier les répétitions.

## 3. Installer le Skill et sa référence

Créez un nouveau projet, sans recopier le code du TP1 :

```bash
mkdir "$ATELIER_DIR/serveur-avec-skill"
cd "$ATELIER_DIR/serveur-avec-skill"
git init
cp "$FORMATION_REPO/domaine/catalogue-services.json" .
mkdir -p .opencode/skills/create-quarkus-mcp-server/references
cp "$FORMATION_REPO/tp2-skill/skills/create-quarkus-mcp-server/SKILL.md" \
  .opencode/skills/create-quarkus-mcp-server/SKILL.md
cp "$FORMATION_REPO/tp2-skill/references/conventions-quarkus-mcp.md" \
  .opencode/skills/create-quarkus-mcp-server/references/
```

La référence est livrée à côté de `skills/` dans les supports, mais le lien du
`SKILL.md` suppose un sous-répertoire `references/` dans le Skill installé.
La copie ci-dessus rend donc le paquet autonome :

```text
.opencode/skills/create-quarkus-mcp-server/
  SKILL.md
  references/
    conventions-quarkus-mcp.md
```

Le dossier `tp2-skill/skills/` n'est **pas** à lui seul un chemin de découverte
OpenCode. Utilisez l'emplacement ci-dessus, documenté dans
[Agent Skills](https://opencode.ai/docs/skills/).

## 4. Vérifier le chargement, puis générer

Lancez une nouvelle session OpenCode dans `serveur-avec-skill`, avec le même modèle
qu'au TP1 :

```bash
opencode
```

Demandez d'abord :

```text
Charge le Skill create-quarkus-mcp-server avec l'outil skill.
Lis sa référence de conventions. Indique les règles que tu vas appliquer,
mais ne génère pas encore de code.
```

Vérifiez l'appel effectif à l'outil `skill` dans la session et l'accès à la référence.
Une réponse « je connais ce Skill » ne suffit pas. Si le Skill n'est pas disponible,
corrigez l'installation avant de continuer.

Reprenez ensuite la demande métier du TP1, en remplaçant la consigne de ne pas
utiliser de Skill par :

```text
Utilise le Skill create-quarkus-mcp-server que tu viens de charger.
Génère le serveur dans le répertoire courant, selon ses conventions.
Ne consulte et ne recopie pas le serveur vanilla.
```

## 5. Comparer sur des preuves

Exécutez `mvn test`, puis `mvn quarkus:dev` dans le nouveau projet. Rejouez
**les mêmes appels MCP qu'au TP1**, y compris la recherche par équipe et le
service inconnu. Arrêtez le dev mode après inspection.

Complétez votre comparaison :

| Critère | Vanilla | Avec Skill | Preuve |
| --- | --- | --- | --- |
| Démarrage et appels métier | À relever | À relever | Sorties des commandes et de l'inspecteur |
| Extension HTTP et package | À relever | À relever | `pom.xml`, packages Java |
| Séparation des responsabilités | À relever | À relever | Classes et délégation |
| Noms, descriptions et URI | À relever | À relever | Annotations et contrat MCP observé |
| Chargement classpath et logs | À relever | À relever | Ressources, code et logs |
| Corrections manuelles demandées | À compter | À compter | Historique des prompts |

Une absence d'amélioration est un résultat utile : cherchez une règle ambiguë,
un Skill non chargé ou une baseline déjà conforme. Ne modifiez pas la baseline
pour rendre la comparaison plus flatteuse.

## 6. Itérer sans tricher

Si une règle échoue, modifiez le **Skill source du clone**, recopiez-le dans le projet
d'essai et régénérez dans un **nouveau répertoire** avec une nouvelle session.
Corriger seulement le Java généré ne corrige pas le savoir-faire.

Refaites au moins une génération indépendante avec le même prompt, le même modèle
et la même version du Skill, puis comparez les critères. Deux succès ne prouvent
pas une garantie universelle, mais permettent déjà de repérer une règle instable.
Conservez chaque sortie séparément.

Le `.gitignore` des supports exclut `.opencode/` : versionnez bien la source sous
`tp2-skill/skills/` et sa référence, pas uniquement la copie installée.
La publication et les protections GitLab seront réalisées au TP4.

## Dépannage

| Symptôme | Vérification |
| --- | --- |
| Skill absent de la liste | Nom exact `SKILL.md`, frontmatter valide, chemin de découverte |
| Skill refusé | Permission `skill`, éventuel `deny` global ou propre à l'agent |
| Référence introuvable | Présence de `references/` à côté du `SKILL.md` installé |
| Anciennes instructions encore appliquées | Nouvelle session et nouvelle copie du Skill source |
| Serveur conforme seulement après retouches | Transformer les retouches récurrentes en règles puis régénérer |

## Point de passage

Le Skill se charge réellement, sa référence est accessible, une génération neuve
a été comparée à TP1 et les écarts restants sont explicites. Vous pouvez maintenant
transformer cette comparaison en évaluation exécutable.
