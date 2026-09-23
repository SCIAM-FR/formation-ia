---
title: Fiche de sortie — une capacité, neuf lignes
description: Le canevas à remplir pendant l'atelier M7 pour transformer une capacité IA bricolée en capacité cataloguée, évaluée et gouvernée.
eyebrow: Atelier M7 · Modèle opératoire
permalink: /fiche-de-sortie/
previous_url: /tp4/
previous_title: TP4 — Gouverner
---

## À quoi sert cette fiche

L'atelier de clôture (M7, 1 h) ne produit pas une esquisse mais **une fiche remplie**
pour **une** capacité réelle de votre organisation : un serveur MCP, un Skill, un
scorer ou un prompt partagé, déjà utilisé quelque part, même de façon artisanale.

Chaque ligne appelle un nom, une date ou un critère observable. Une intention
(« à définir », « l'équipe plateforme », « bientôt ») n'est pas une réponse.

## Le canevas

Copiez ce tableau dans vos notes ou imprimez la page. La colonne d'exemple reprend
le fil rouge de la formation ; remplacez-la par votre capacité.

| Ligne | La question à trancher | Exemple — fil rouge | Votre capacité |
| --- | --- | --- | --- |
| **Capacité** | Quoi, en une phrase ; ce qu'elle ne fait pas | Skill `create-quarkus-mcp-server` : génère un serveur MCP Quarkus conforme aux conventions maison ; ne déploie rien | |
| **Consommateurs** | Qui l'utilise, par quel client, à quelle fréquence | Équipes produit via OpenCode ; agent CI de la platform team à chaque MR | |
| **Propriétaire** | Une personne nommée et un suppléant, pas une équipe | Nom, adresse, suppléant | |
| **Contrat** | Ce qui est garanti, versionné et observable de l'extérieur | Tools `find_service` / `get_owner`, resource `service://{name}`, prompt `fiche_service` ; conventions v1 | |
| **Critères d'évaluation** | Assertions, grille, seuil ; quand on rejoue | `tp3-eval` : cinq assertions, juge ≥ 7/10 calibré par lecture humaine ; à chaque MR et à chaque changement de modèle | |
| **Distribution / version** | Où on la trouve, comment on la référence | Dépôt GitLab, tag semver, entrée du catalogue interne | |
| **Contrôles d'accès** | Qui peut charger, modifier, appeler ; où le contrôle s'applique | CODEOWNERS sur le Skill et le harnais ; `permission.skill` par agent ; OAuth 2.1 sur le serveur MCP | |
| **Dépréciation** | Le test de suppression : critère, cadence, qui décide | Rejouer les cas sans une règle à chaque nouveau modèle ; retirer si les preuves tiennent ; décision du propriétaire | |
| **Première action datée** | Un verbe, un nom, une date | Ouvrir la MR d'entrée au catalogue — nom, date | |

## Comment la remplir en atelier

| Temps | Étape |
| --- | --- |
| 10 min | Cartographier les capacités IA déjà présentes dans les équipes, même bricolées |
| 10 min | Choisir la première à cataloguer, en binôme |
| 25 min | Remplir les neuf lignes ; chaque ligne contient un nom, une date ou un critère observable |
| 15 min | Restitution : chaque binôme lit sa première action datée |

Les lignes suivent le cycle de vie présenté en M7 : contribution, revue,
distribution, dépréciation. Les contrôles d'accès reprennent les trois couches vues
en M5 et M6 : GitLab pour les changements, OpenCode pour le chargement du Skill,
l'authentification et l'autorisation MCP pour l'accès au serveur.

## Après la formation

La fiche est le premier pas, pas la cible. Avant toute mise en production,
relisez la dernière diapositive du support : élargir les cas et répéter les
évaluations, valider identité, accès, secrets, observabilité et budgets, nommer
un propriétaire, versionner et prévoir le retour arrière.
