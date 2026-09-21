---
title: TP1 — Générer un serveur MCP vanilla
description: Piloter OpenCode pour obtenir un serveur fonctionnel, puis observer ce que le modèle ne normalise pas tout seul.
permalink: /tp1/
previous_url: /preparation/
previous_title: Préparation
next_url: /tp2/
next_title: TP2 — Formaliser
---

## Objectif et livrables

Faites produire le Java par l'agent : votre travail est de cadrer la demande,
relire les changements et vérifier le résultat. À la fin, conservez **un serveur
qui démarre**, **les preuves de ses appels MCP** et **un tableau des écarts**.

Prérequis : la [préparation]({{ '/preparation/' | relative_url }}) est terminée.
Support : [consigne du TP1]({{ site.repository_url }}/blob/main/tp1-serveur-vanilla/README.md).

## Essentiel et approfondissement

| Parcours | Sections concernées | Preuve de sortie |
| --- | --- | --- |
| **Essentiel — 1 h 30 cible** | 1 à 6 : une génération vanilla, build, démarrage, inspecteur, comparaison avec un voisin, baseline | Deux tools `find_service` / `get_owner`, resource `service://{name}`, prompt `fiche_service` observés ; appels vérifiés ; écarts éventuels et conditions conservés |
| **Approfondissement — hors 14 h ou si avance** | 5 : seconde génération individuelle indépendante et nouvelle comparaison | Deux sorties séparées et variabilité documentée, sans supposer un échec |

La durée suppose la préparation vérifiée ; les corrections nécessaires au
fonctionnement restent essentielles. L'approfondissement ne remplace aucune preuve.

## 1. Créer un projet isolé

Dans votre terminal préparé :

```bash
mkdir "$ATELIER_DIR/serveur-vanilla"
cd "$ATELIER_DIR/serveur-vanilla"
git init
cp "$FORMATION_REPO/domaine/catalogue-services.json" .
opencode
```

Si le dossier existe déjà, choisissez un nouveau nom et utilisez-le dans les
commandes suivantes. Ne mélangez pas deux générations.
Ne copiez ni Skill ni conventions dans ce projet.

## 2. Donner la consigne à l'agent

Utilisez ce point de départ et conservez le texte exact :

```text
Crée dans le répertoire courant un serveur MCP Java avec Quarkus.
Le domaine est le catalogue local catalogue-services.json.
Il faut exposer :
- un tool find_service(query), pour chercher un service par nom ou équipe ;
- un tool get_owner(service), pour obtenir son équipe et sa criticité ;
- une resource service://{name}, pour lire sa fiche complète ;
- un prompt fiche_service, pour préparer une synthèse d'astreinte.
Ne fais aucun appel à une API métier externe.
Génère le projet, les tests et les commandes pour le lancer.
Compile et exécute les tests, puis explique comment inspecter les primitives MCP.
N'utilise aucun Skill pour cet exercice.
```

Laissez l'agent proposer une structure et un transport. Relisez les commandes avant
de les autoriser. S'il pose une question, répondez et notez votre décision : cela
fait partie des conditions de génération.

Vous devez obtenir au minimum un `pom.xml`, du code sous `src/main/java`,
les données nécessaires à l'exécution et des tests. Lisez le `pom.xml` pour relever
les versions de Java, de Quarkus et de l'extension MCP utilisées.

## 3. Construire et lancer réellement

Depuis `serveur-vanilla`, dans un autre terminal si OpenCode est encore ouvert :

```bash
mvn test
mvn quarkus:dev
```

Le premier appel doit terminer sans erreur ; vérifiez qu'il a effectivement exécuté
des tests. Le second reste actif : attendez le message de démarrage Quarkus.
Si le projet fournit un wrapper Maven, `./mvnw` peut remplacer `mvn`.

Une erreur de résolution de dépendance n'est pas une erreur de logique métier.
Une annotation ou une signature MCP invalide peut empêcher le build ou le
démarrage. Transmettez à l'agent **la commande et le message d'erreur exacts**,
demandez une correction ciblée, puis rejouez les mêmes commandes.

```text
Voici la commande exécutée et son erreur : [coller la sortie utile].
Identifie la cause, corrige le projet sans retirer les primitives demandées,
puis relance la validation. Explique les fichiers modifiés.
```

Ne contournez pas un échec en supprimant un test ou une primitive.

## 4. Inspecter le contrat MCP

Pour un serveur HTTP, ouvrez la Dev UI à l'adresse indiquée dans les logs
(souvent `http://localhost:8080/q/dev-ui/`) et cherchez l'outil d'inspection MCP
fourni par l'extension. Selon sa version, les intitulés et l'accès à l'inspecteur
peuvent varier : suivez le lien exposé par votre serveur.

Si le modèle a choisi stdio ou si aucun inspecteur n'est intégré, utilisez un client
MCP compatible avec ce transport. Consultez la
[documentation de l'extension](https://docs.quarkiverse.io/quarkus-mcp-server/dev/index.html)
et, si nécessaire, le [MCP Inspector](https://modelcontextprotocol.io/docs/tools/inspector).
Ne déduisez pas une URL MCP du seul port HTTP : relevez le point d'accès documenté
par le projet.

Listez séparément les tools, les templates de resources et les prompts. Une resource
paramétrée apparaît dans les **templates**, pas forcément dans la liste des resources
statiques. Vérifiez les arguments réellement exposés, puis exécutez :

| Appel | Résultat métier à retrouver |
| --- | --- |
| `find_service` avec `query = "auth"` | Le service `auth` |
| `find_service` avec `query = "Team Catalog"` | `catalogue-produits` et `recherche` |
| `get_owner` avec `service = "facturation"` | `Team Billing`, criticité `haute` |
| Lecture de `service://facturation` | Dépendances `auth` et `catalogue-produits`, équipe et criticité |
| Récupération du prompt `fiche_service` | Un gabarit de synthèse exploitable pour l'astreinte |

Pour le prompt, renseignez les arguments indiqués par son schéma : la consigne
n'en fixe pas la signature. Récupérer un prompt MCP ne lance pas automatiquement
une génération LLM.

Essayez aussi un service inconnu. Notez le comportement sans inventer une convention
qui n'existe pas encore : liste vide, absence ou erreur explicite doivent être
discutées. Aucun résultat ne doit inventer un propriétaire.

## 5. Faire le constat de conformité

**Seulement après la génération**, ouvrez les
[conventions maison]({{ site.repository_url }}/blob/main/domaine/conventions.md)
et la [référence formateur]({{ site.repository_url }}/blob/main/tp1-serveur-vanilla/ATTENDU.md).
Dans le parcours essentiel, comparez votre projet à celui d'un voisin.
La seconde génération individuelle indépendante est un **approfondissement** :
conservez-la séparément si vous la réalisez.

Complétez un tableau dans vos notes :

| Critère | Observation dans mon projet | Écart à traiter en TP2 |
| --- | --- | --- |
| Extension et transport | Dépendance exacte du `pom.xml` | HTTP attendu, stdio seulement en option |
| Package | Package racine généré | Cible : `com.sciam.formation.mcp` |
| Métier et adaptateur | Qui charge les données ? Qui porte les annotations ? | Deux responsabilités séparées |
| Noms publics | Noms vus dans l'inspecteur, pas seulement les méthodes Java | `find_service`, `get_owner`, `fiche_service` |
| Resource | URI et argument exposés | `service://{name}` |
| Descriptions | Annotations des tools et arguments | Descriptions explicites |
| Données et démarrage | Chargement JSON, tests, logs | Classpath, test de démarrage, logs INFO |

Ne déclarez pas un écart sans preuve dans un fichier ou dans l'inspecteur.
Si votre modèle respecte déjà tout, notez-le : **le TP ne cherche pas à provoquer
un échec**. La conformité d'une génération ne prouve pas encore sa répétabilité.

## 6. Figer la baseline

Arrêtez le dev mode avec `Ctrl+C`. Gardez ce projet intact pour la comparaison.
Vous pouvez le versionner localement après avoir vérifié `.gitignore` et
`git status` : ni `target/`, ni secrets, ni configuration personnelle ne doivent
être ajoutés. Ne publiez rien avant cette vérification.

Votre compte rendu doit contenir le prompt, les versions, les corrections demandées,
les résultats des appels et le tableau des écarts.

## Dépannage

| Symptôme | Action |
| --- | --- |
| Port 8080 occupé | Arrêter votre autre serveur ou utiliser `mvn quarkus:dev -Dquarkus.http.port=8081` |
| JSON introuvable au lancement | Vérifier où le code cherche le fichier et si la ressource est empaquetée |
| Build vert, primitives absentes | Vérifier l'extension, les annotations et les listes de l'inspecteur |
| Méthode Java présente mais nom MCP incorrect | Comparer le nom public enregistré, pas seulement le nom de méthode |
| Modèle bloqué sur une dépendance | Lui fournir l'erreur Maven et la documentation de la version choisie |

## Point de passage

Passez au TP2 lorsque les quatre primitives ont été observées, que les appels métier
sont vérifiés, que la baseline et la comparaison avec un voisin sont conservées
et que vous savez distinguer **fonctionnement** et **conformité**. Un tableau sans
écart est recevable s'il est étayé ; une seconde génération individuelle n'est pas
requise pour ce passage.
