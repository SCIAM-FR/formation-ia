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

## Essentiel et approfondissement

| Parcours | Sections concernées | Preuve de sortie |
| --- | --- | --- |
| **Essentiel — 1 h 30 cible** | 1 à 5, puis 6 pour sauvegarder et corriger si nécessaire | Skill et référence installés, chargement prouvé, **un plan relu et approuvé dans Plannotator**, **une génération neuve** conforme au plan et sans copie de TP1, build/MCP vérifiés, comparaison et limites écrites, sources sauvegardées dans le repo |
| **Approfondissement — hors 14 h ou si avance** | 6 : répétitions supplémentaires de robustesse | Sorties indépendantes à prompt, modèle et version du Skill identiques ; stabilité mesurée sans promesse de reproductibilité |

Une régénération pour vérifier une règle corrigée après échec reste **nécessaire**,
pas facultative. Elle peut dépasser la durée cible ; ne la confondez pas avec
les répétitions supplémentaires d'un premier résultat satisfaisant.

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

## 4. Vérifier le chargement, planifier, puis générer

Cette section ajoute une étape entre le Skill et le code : **un plan, relu et
annoté avant toute génération**. OpenCode a deux agents primaires, `plan`, qui lit
et propose sans écrire de fichier, et `build`, qui exécute ; on passe de l'un à
l'autre avec la touche Tab. [Plannotator](https://docs.plannotator.ai/open-source/agents/opencode)
ouvre le plan de l'agent `plan` dans le navigateur, vous l'annotez, et vos remarques
reviennent à l'agent jusqu'à ce que vous l'approuviez. Comptez 15 minutes.

### 4.1 Installer Plannotator dans le projet

Dans `serveur-avec-skill`, créez `opencode.json` avec le plugin :

```json
{
  "$schema": "https://opencode.ai/config.json",
  "plugin": ["@plannotator/opencode@latest"]
}
```

Le plugin se télécharge au premier lancement d'OpenCode dans ce répertoire (accès
réseau nécessaire, à prévalider par le formateur). Il expose à l'agent `plan` un
outil `submit_plan` ; c'est cet appel qui ouvre le navigateur. Les commandes
`/plannotator-review`, `/plannotator-annotate` et `/plannotator-last`, installées
par `curl -fsSL https://plannotator.ai/install.sh | bash`, sont facultatives.

### 4.2 Charger le Skill, en mode plan

Lancez OpenCode directement sur l'agent `plan`, avec le même modèle qu'au TP1 :

```bash
opencode --agent plan
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

### 4.3 Demander le plan

Reprenez la demande métier du TP1, sans la consigne « aucun Skill », et demandez un
plan plutôt que du code :

```text
Utilise le Skill create-quarkus-mcp-server que tu viens de charger.
Prépare le plan de génération du serveur MCP Quarkus pour catalogue-services.json :
la liste des fichiers à créer avec le rôle de chacun, le package et les classes,
la dépendance MCP et le transport retenus, les quatre primitives avec leurs noms
publics et leurs annotations, le chargement des données et les tests prévus.
Pour chaque choix, cite la règle du Skill ou de sa référence qui l'impose.
Ne consulte et ne recopie pas le serveur vanilla. Soumets le plan avec submit_plan.
```

L'agent `plan` n'écrit aucun fichier : si des fichiers apparaissent, vous n'êtes pas
sur le bon agent. Quand il appelle `submit_plan`, Plannotator ouvre le plan dans le
navigateur.

### 4.4 Relire et annoter le plan

Lisez le plan **avec les conventions sous les yeux**, ligne par ligne. Chaque point
de la liste ci-dessous doit être présent, exact et justifié par une règle ; sinon,
sélectionnez le passage dans Plannotator et annotez-le :

| À vérifier dans le plan | Ce qui doit y figurer |
| --- | --- |
| Dépendance et transport | `quarkus-mcp-server-http` ; stdio seulement en option |
| Package et classes | `com.sciam.formation.mcp` ; `CatalogueService` (données, logique) distinct de `CatalogueMcpServer` (annotations, délégation) |
| Tools | `find_service(query)` et `get_owner(service)`, noms publics en snake_case, `@Tool(description)` et `@ToolArg(description)` |
| Resource | `@ResourceTemplate(uriTemplate = "service://{name}")` |
| Prompt | `fiche_service`, nom explicite |
| Données | `catalogue-services.json` lu depuis le classpath, pas un chemin de poste |
| Tests et logs | Un test de démarrage vérifiant l'enregistrement des primitives ; logs INFO |
| Justification | Chaque choix renvoie à une règle du Skill ; un choix sans règle révèle un trou du Skill |

Trois situations, trois réactions :

- **Le plan viole une règle** (nom camelCase, classe unique, URI différente) :
  annotez, demandez la correction, et notez que le Skill contient bien la règle.
  Si le Skill ne la contient pas, c'est le Skill qu'il faut corriger, en section 2,
  avant de recommencer.
- **Le plan fait un choix que le Skill ne cadre pas** (transport, gestion du
  service inconnu, format des retours) : c'est un écart à traiter en section 6,
  pas une faute du modèle. Notez-le.
- **Le plan est conforme** : approuvez. Ne demandez pas de changements pour le
  plaisir ; un plan conforme du premier coup est un résultat, comme au TP1.

Approuvez seulement quand la liste est complète. Plannotator renvoie vos annotations
à l'agent et, à l'approbation, bascule sur l'agent `build`. Conservez le plan
approuvé (export ou copie du texte) avec ses annotations : c'est une preuve de sortie
du TP, au même titre que la génération.

### 4.5 Générer selon le plan

Sur l'agent `build`, demandez l'exécution du plan approuvé, sans le reformuler :

```text
Génère le serveur dans le répertoire courant en suivant exactement le plan approuvé
et les conventions du Skill. Ne consulte et ne recopie pas le serveur vanilla.
Compile et exécute les tests.
```

Tout écart entre le plan approuvé et le code produit se relève en section 5 : c'est
une information sur le modèle, pas sur le Skill.

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

**Essentiel en cas d'échec :** si une règle échoue, modifiez le **Skill source du
clone** et sa référence si nécessaire, puis réinstallez les deux comme en section 3
dans un **nouveau répertoire**. Régénérez avec une nouvelle session et revérifiez
le chargement, le build et les appels MCP. Corriger seulement le Java généré ne
corrige pas le savoir-faire. Conservez aussi la sortie en échec et les changements
de version du Skill.

**Approfondissement :** après un premier résultat satisfaisant, refaites des
générations indépendantes avec le même prompt, le même modèle et la même version
du Skill, puis comparez les critères. Deux succès ne prouvent pas une garantie
universelle, mais permettent déjà de repérer une règle instable. Conservez chaque
sortie séparément. Ces répétitions ne sont pas requises dans les 1 h 30 essentielles.

Le `.gitignore` des supports exclut `.opencode/` : versionnez bien la source sous
`tp2-skill/skills/` et sa référence sous `tp2-skill/references/`, pas uniquement la
copie installée. Cette sauvegarde dans votre repo local fait partie de l'essentiel.
La publication et les protections sur la forge (GitLab ou GitHub) seront réalisées
**au TP4, pas au TP2**.

## Dépannage

| Symptôme (plan) | Action |
| --- | --- |
| Le navigateur ne s'ouvre pas à `submit_plan` | Plugin absent de `opencode.json`, OpenCode non relancé après l'ajout, ou premier téléchargement bloqué par le réseau |
| L'agent écrit des fichiers pendant le plan | Vous êtes sur `build` : Tab pour revenir sur `plan`, ou relancer avec `opencode --agent plan` |
| L'agent ne répond qu'en prose, sans `submit_plan` | Redemander explicitement « soumets le plan avec submit_plan » |
| Le plan cite des règles absentes du Skill | Le modèle comble avec ses habitudes : bon signal pour la section 6, pas une faute à annoter |


| Symptôme | Vérification |
| --- | --- |
| Skill absent de la liste | Nom exact `SKILL.md`, frontmatter valide, chemin de découverte |
| Skill refusé | Permission `skill`, éventuel `deny` global ou propre à l'agent |
| Référence introuvable | Présence de `references/` à côté du `SKILL.md` installé |
| Anciennes instructions encore appliquées | Nouvelle session et nouvelle copie du Skill source |
| Serveur conforme seulement après retouches | Transformer les retouches récurrentes en règles puis régénérer |

## Point de passage

Le Skill se charge réellement, sa référence est accessible, un plan a été relu,
annoté si nécessaire et approuvé avant tout code, une génération neuve conforme à
ce plan a été construite, inspectée et comparée à TP1, et les sources du Skill et de sa
référence sont sauvegardées dans le repo. Les écarts restants et les limites de
cette unique observation sont explicites : ne concluez pas à la répétabilité.
Si une règle a dû être corrigée, sa vérification sur une nouvelle génération est
requise. Conservez le chemin exact de la génération retenue pour TP3 ; si ce n'est
plus `serveur-avec-skill`, adaptez les commandes du guide suivant.

**Bonus expérimental, hors 14 h :** le Skill partage un savoir-faire, pas l'état
d'un travail en cours. Le [TP2 Bonus]({{ '/tp2-bonus/' | relative_url }}) explore
trois manières de transmettre le contexte d'une session OpenCode à un binôme, de
l'export manuel à un Skill qui le publie sous une clé Redis.
