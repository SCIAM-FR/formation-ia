---
title: Fiche de sortie — une capacité, neuf lignes
description: Le canevas vierge de l'atelier M7 et un exemple complet rempli pour le Skill du fil rouge.
eyebrow: Atelier M7 · Modèle opératoire
permalink: /fiche-de-sortie/
previous_url: /tp4/
previous_title: TP4 — Gouverner
---

## À quoi sert cette fiche

L'atelier de clôture ne produit pas une esquisse mais **une fiche remplie** pour
**une** capacité réelle de votre organisation : un serveur MCP, un Skill, un scorer
ou un prompt partagé, déjà utilisé quelque part, même de façon artisanale.

Neuf lignes en trois groupes : ce qu'elle est et pour qui, ce qu'on garantit,
comment elle vit ; puis une première action datée. Chaque ligne appelle un nom,
une date ou un critère observable. Une case « à définir », « l'équipe plateforme »
ou « bientôt » ne compte pas.

## Le canevas vierge

Copiez ce tableau dans vos notes ou imprimez la page.

**Quoi et pour qui**

| # | Ligne | La question | Votre réponse |
| --- | --- | --- | --- |
| 1 | **Capacité** | Ce qu'elle fait en une phrase, et ce qu'elle ne fait pas | |
| 2 | **Utilisateurs** | Qui s'en sert, et avec quel outil | |
| 3 | **Responsable** | Une personne nommée et un suppléant, pas une équipe | |

**Ce qu'on garantit**

| # | Ligne | La question | Votre réponse |
| --- | --- | --- | --- |
| 4 | **Contrat** | Ce qui est promis, versionné et observable | |
| 5 | **Évaluation** | Les tests, le seuil, et quand on les rejoue | |
| 6 | **Accès** | Qui peut la charger, la modifier, l'appeler | |

**Comment elle vit**

| # | Ligne | La question | Votre réponse |
| --- | --- | --- | --- |
| 7 | **Distribution** | Où on la trouve et quelle version on utilise | |
| 8 | **Retrait** | À quel signal on la supprime, et qui décide | |

**Et maintenant**

| # | Ligne | La question | Votre réponse |
| --- | --- | --- | --- |
| 9 | **Première action datée** | Un verbe, un nom, une date | |

## Exemple complet — le Skill `create-quarkus-mcp-server`

La capacité du fil rouge, telle qu'elle pourrait entrer au catalogue d'une platform
team le lendemain de la formation. Les noms et les dates sont fictifs ; ce qui
compte est leur forme : à chaque ligne, quelque chose qu'on peut vérifier.

**Quoi et pour qui**

| # | Ligne | Réponse |
| --- | --- | --- |
| 1 | **Capacité** | Génère un serveur MCP Java/Quarkus conforme aux conventions maison (structure, transport HTTP, nommage snake_case, resource `service://{name}`, données sur le classpath) à partir d'une description de domaine. Ne déploie rien, ne gère pas l'authentification du serveur produit, ne couvre que Quarkus. |
| 2 | **Utilisateurs** | Les développeurs des équipes produit, depuis OpenCode (Skill chargé à la demande) ; l'agent CI de la platform team, qui régénère un serveur à chaque merge request touchant le Skill. |
| 3 | **Responsable** | Marie Dupont (platform team), suppléant Karim Benali. Sollicités par CODEOWNERS sur `tp2-skill/` et `tp3-eval/`. |

**Ce qu'on garantit**

| # | Ligne | Réponse |
| --- | --- | --- |
| 4 | **Contrat** | Un serveur généré expose deux tools `find_service` et `get_owner`, la resource `service://{name}` et le prompt `fiche_service`, avec descriptions sur chaque tool et argument. Conventions v1 (`domaine/conventions.md`), Quarkus 3.x, `quarkus-mcp-server-http`. Vérifiable avec l'inspecteur MCP du dev mode. |
| 5 | **Évaluation** | Harnais `tp3-eval` : cinq assertions déterministes (extension, deux tools, gabarit d'URI, séparation métier/adaptateur) et un juge LLM avec seuil 7/10, calibré une fois par lecture humaine. Rejoué par la CI à chaque MR sur le Skill, et manuellement à chaque changement de modèle de génération. |
| 6 | **Accès** | Charger : agents `build` seulement, via `permission.skill` dans `opencode.json` ; l'agent `plan` est refusé. Modifier : merge request obligatoire, `main` protégé, approbation du responsable ou du suppléant. Appeler un serveur généré : OAuth 2.1 côté serveur MCP, hors périmètre du Skill. |

**Comment elle vit**

| # | Ligne | Réponse |
| --- | --- | --- |
| 7 | **Distribution** | Dépôt GitLab `platform/skills`, tag `create-quarkus-mcp-server/v1.0.0`, entrée du catalogue interne avec le lien du guide. Les équipes installent la version taguée dans `.opencode/skills/`, jamais `main`. |
| 8 | **Retrait** | Test de suppression à chaque nouveau modèle de génération : rejouer les cinq cas du dataset sans la règle candidate ; si assertions et juge restent verts sur trois générations, la règle est retirée par MR. Le Skill entier est retiré quand il ne contient plus que des rappels que le modèle respecte seul. Décision : la responsable, preuves jointes à la MR. |

**Et maintenant**

| # | Ligne | Réponse |
| --- | --- | --- |
| 9 | **Première action datée** | Ouvrir la merge request d'entrée au catalogue avec la fiche et le tag v1.0.0 — Marie, 15 octobre. |

## Comment la remplir en atelier

| Temps | Étape |
| --- | --- |
| 5 min | Cartographier les capacités IA déjà présentes dans les équipes, même bricolées |
| 5 min | Choisir la première à cataloguer, en binôme |
| 25 min | Remplir les neuf lignes ; chaque ligne contient un nom, une date ou un critère observable |
| 10 min | Restitution : chaque binôme lit sa première action datée |

Les groupes suivent le cycle de vie présenté en M7 : contribution, revue,
distribution, retrait. La ligne « Accès » reprend les trois couches vues en M5 et
M6 : la forge pour les changements, OpenCode pour le chargement du Skill,
l'authentification et l'autorisation MCP pour l'accès au serveur.

## Après la formation

La fiche est le premier pas, pas la cible. Avant toute mise en production,
relisez la dernière diapositive du support : élargir les cas et répéter les
évaluations, valider identité, accès, secrets, observabilité et budgets, nommer
un responsable, versionner et prévoir le retour arrière.
