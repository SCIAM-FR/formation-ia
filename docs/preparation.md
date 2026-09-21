---
title: Préparer son poste
description: Vérifier les outils, connecter un modèle et séparer les générations des supports de formation.
permalink: /preparation/
previous_url: /
previous_title: Le parcours
next_url: /tp1/
next_title: TP1 — Générer
---

## 1. Vérifier les prérequis

| Outil ou accès | Vérification | Résultat attendu |
| --- | --- | --- |
| Git | `git --version` | Une version installée |
| JDK 21 ou supérieur | `java -version` | Une version au moins égale à 21 |
| Maven | `mvn -version` | Maven disponible et utilisant le bon JDK |
| OpenCode | `opencode --version` | Le CLI répond |
| Modèle LLM | Un message dans OpenCode | Une vraie réponse, sans erreur d'authentification |
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
git clone https://github.com/sebastienblanc/formation-ia.git
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
Un accès GitLab peut être préparé plus tard, mais il sera indispensable au TP4.
