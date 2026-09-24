---
title: Préparer son poste
description: Vérifier les outils, connecter un modèle et séparer les générations des supports de formation.
permalink: /preparation/
previous_url: /
previous_title: Le parcours
next_url: /tp1/
next_title: TP1 — Générer
---

## Avant la formation — checklist du formateur

La cible de **14 h hors pauses**, dont **6 h 30 de TP essentiels**, suppose cette
préparation faite **avant la séance**, et non pendant les 15 minutes d'accueil.
Les participants y vérifient leurs accès ; ils n'y installent pas toute la chaîne.

| À prévalider par le formateur | Preuve attendue avant la séance |
| --- | --- |
| Postes, JDK/Maven, dépendances et inspection MCP | Build, démarrage et appels MCP sur l'environnement d'atelier ; dépendances accessibles |
| OpenCode et modèle de génération autorisé | Version fixée, authentification et réponse réelle ; profil vanilla sans Skill préchargé |
| Fournisseur du juge TP3 | Endpoint Chat Completions, modèle, clé et quota autorisés ; appel réel avec JSON exploitable par le client Java |
| Projet GitLab, comptes et offre | Accès participants, droits de protection de `main`, possibilités de revue CODEOWNERS identifiées |
| Runner et environnement OpenCode | Job de MR exécuté dans un environnement isolé ; Java/Maven et OpenCode disponibles |
| Modèles et secrets CI autorisés | Génération et juge accessibles dans le contexte de MR de confiance retenu, sans exposer les clés aux contributions non fiables |
| Démonstration LiteLLM (M5), sur le poste formateur | Gateway locale démarrée, alias et équipes créés, juge du TP3 et serveur MCP du fil rouge passés à travers, refus et journal vérifiés, capture vidéo de secours ([`formateur/demo-litellm/`]({{ site.repository_url }}/tree/main/formateur/demo-litellm)) |
| Chaîne de génération TP4 | Génération neuve depuis le Skill du checkout de la MR, preuve de chargement, build et évaluation réels ; échec bloquant et rapports conservés |

Le dossier [`formateur/`]({{ site.repository_url }}/tree/main/formateur) du dépôt
regroupe la démonstration du scorer trompeur (M4), un corrigé possible des
assertions du TP3 et la trace fictive du mini-exercice M5. Les sorties de
génération conformes et non conformes utiles au débrief se produisent lors de la
répétition, avec le modèle et les versions retenus ; archivez-les hors du dépôt.

Le dépôt livre un **template de CI avec un `echo` TODO**, pas une CI clé en main.
Le formateur adapte et éprouve la plomberie des
[sections 3 et 4 du TP4]({{ '/tp4/' | relative_url }})
avant la formation. Il prépare l'infrastructure, sans faire à la place des
participants la rédaction du Skill, des assertions ou de la grille. La CI de
séance devra utiliser **leurs versions** issues de la MR, pas un serveur préfabriqué.

**Si un accès manque :** rétablissez-le et allongez l'atelier, ou organisez une
démonstration sur un environnement autorisé en indiquant ce que les participants
n'ont pas pu vérifier. Sans juge, les assertions peuvent avancer, mais TP3 reste
**partiel et non validé** tant que le verdict n'a pas été calibré avec une lecture
humaine. Sans génération réelle ou sans preuve de blocage GitLab, TP4 reste partiel.
Ne remplacez pas ces preuves par un statut vert, un `echo` ou des tests désactivés.

## Périmètre de préparation

| Parcours | Sections | Preuve de sortie |
| --- | --- | --- |
| Essentiel, avant séance puis vérification à l'accueil | Checklist formateur ; sections 1 à 4 | Accès opérationnels, appel modèle réel, chemins isolés, juge et CI prévalidés |
| Approfondissement / parcours autonome, hors 14 h | Installation et configuration depuis zéro, notamment TP4 sections 3 et 4 | Même chaîne réellement exécutée ; temps supplémentaire à prévoir |

## 1. Vérifier les prérequis

| Outil ou accès | Vérification | Résultat attendu |
| --- | --- | --- |
| Git | `git --version` | Une version installée |
| JDK 21 ou supérieur | `java -version` | Une version au moins égale à 21 |
| Maven | `mvn -version` | Maven disponible et utilisant le bon JDK |
| OpenCode | `opencode --version` | Le CLI répond |
| Modèle LLM | Un message dans OpenCode | Une vraie réponse, sans erreur d'authentification |
| Juge LLM, pour TP3 | Configuration validée avec le formateur | Endpoint compatible, modèle et secret autorisés disponibles |
| GitLab, pour TP4 | Accès au projet d'atelier | Droits suffisants pour gérer branche protégée, CI et revue |

Installez les outils manquants avec les instructions de votre environnement.
La [documentation OpenCode](https://opencode.ai/docs/) décrit l'installation et
la connexion aux fournisseurs. Le CLI Quarkus est facultatif : les guides utilisent
Maven.

Vérifiez particulièrement la ligne `Java version` de `mvn -version`. Un JDK récent
dans le terminal ne garantit pas que Maven utilise ce même JDK ; ajustez `JAVA_HOME`
si nécessaire.

## 2. Récupérer les supports

Si vous n'avez pas encore de clone :

```bash
git clone https://github.com/SCIAM-FR/formation-ia.git
cd formation-ia
```

Depuis la racine de votre clone, définissez deux chemins :

```bash
export FORMATION_REPO="$(pwd)"
export ATELIER_DIR="$HOME/formation-ia-atelier"
mkdir -p "$ATELIER_DIR"
```

`FORMATION_REPO` désigne les supports, `ATELIER_DIR` les productions. Choisissez un
répertoire d'atelier **hors du clone** et inutilisé pour cette session de formation.
Vous éviterez que la génération vanilla lise les conventions et le Skill du dépôt.

> Ces variables ne persistent que dans le terminal courant. Dans chaque nouveau
> terminal, redéfinissez-les avec les mêmes chemins absolus. Ne relancez pas
> `FORMATION_REPO="$(pwd)"` depuis un projet généré.

L'organisation obtenue au fil des TP sera :

```text
formation-ia/                       # supports clonés
  domaine/
  tp1-serveur-vanilla/
  tp2-skill/
  tp3-eval/
  tp4-gouvernance/
formation-ia-atelier/               # productions, hors du clone
  serveur-vanilla/                  # TP1 : baseline à conserver
  serveur-avec-skill/               # TP2 : nouvelle génération
  evaluations/                     # TP3 : projets et résultats par cas
```

## 3. Connecter et essayer le modèle

Configurez le fournisseur autorisé par votre organisation :

```bash
opencode auth login
opencode models
```

Ouvrez ensuite OpenCode dans l'espace d'atelier :

```bash
cd "$ATELIER_DIR"
opencode
```

Sélectionnez le modèle prévu pour la formation et demandez :

```text
Réponds simplement « prêt pour le TP ». Ne crée et ne modifie aucun fichier.
```

Une liste de modèles ou une connexion enregistrée ne suffit pas : attendez une
réponse réelle. Relevez le modèle exact (`fournisseur/modèle`) et la version
d'OpenCode. Gardez-les identiques pour comparer TP1 et TP2.

## 4. Préparer une comparaison honnête

Avant TP1, vérifiez vos instructions et Skills globaux OpenCode, Claude ou agents :
un Skill Quarkus/MCP déjà installé fausserait la baseline. Utilisez un profil
d'atelier sans ce Skill si nécessaire, sans supprimer votre configuration habituelle.
Ne transmettez pas encore les conventions maison à l'agent.

Gardez une trace du prompt, du modèle, des relances et des corrections demandées.
Ne copiez jamais de clé API dans les prompts, les sources ou les captures.
N'envoyez que les données fictives de cet atelier au fournisseur autorisé.

## Prêt à commencer

Vous pouvez passer au TP1 si Java et Maven utilisent un JDK compatible, si OpenCode
répond avec le modèle retenu et si les deux chemins de travail sont définis.
Les accès au juge et à GitLab ainsi que le runner doivent déjà avoir été prévalidés
par le formateur pour tenir le parcours complet. Signalez dès l'accueil tout écart
avec cette préparation ; n'attendez pas TP3 ou TP4 pour découvrir un accès manquant.
