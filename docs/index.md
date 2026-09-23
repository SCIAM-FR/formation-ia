---
title: Du premier prompt au Skill gouverné
description: Quatre TP pour transformer une génération ponctuelle en un savoir-faire partagé, mesurable et maintenable.
eyebrow: Formation · Du protocole MCP à l'usine logicielle IA
permalink: /
next_url: /preparation/
next_title: Préparer son poste
---

## Le scénario

Votre équipe veut rendre son **catalogue de services interne** accessible à un agent.
Vous allez faire produire un serveur MCP Java/Quarkus par OpenCode, observer les
différences entre générations, puis rendre cette production conforme aux conventions
de l'équipe.

Le catalogue contient cinq services fictifs : `auth`, `facturation`,
`catalogue-produits`, `recherche` et `notifications`. Les données métier sont locales :
aucune API métier ni base de données n'est nécessaire. Les appels au modèle et le
téléchargement des dépendances nécessitent, eux, un accès réseau.

## Le parcours

| Étape | Question à résoudre | Livrable |
| --- | --- | --- |
| [Préparation]({{ '/preparation/' | relative_url }}) | Mon poste et mon modèle sont-ils prêts ? | Un espace de travail et un premier appel au modèle |
| [TP1 — Générer]({{ '/tp1/' | relative_url }}) | Le modèle sait-il produire un serveur fonctionnel ? | Un serveur vanilla et un constat des écarts |
| [TP2 — Formaliser]({{ '/tp2/' | relative_url }}) | Comment rendre la génération conforme ? | Un Skill et une nouvelle génération comparée au TP1 |
| [TP3 — Évaluer]({{ '/tp3/' | relative_url }}) | Comment prouver que le Skill aide réellement ? | Des assertions, une grille de juge et des résultats |
| [TP4 — Gouverner]({{ '/tp4/' | relative_url }}) | Comment faire évoluer ce savoir-faire sans régression ? | Une merge request, une CI et des règles de revue |

Suivez les TP dans cet ordre : chaque livrable sert d'entrée au suivant. Les pages
précisent les fichiers à ouvrir, les commandes à lancer, les résultats à observer et
les erreurs fréquentes. Les blocs de commandes supposent un shell Bash/Zsh ou WSL.

## Programme sur deux jours

Proposition de cadence : **14 h d'enseignement hors pauses**, réparties en deux
journées de 7 h. Les durées sont des **cibles pédagogiques**, dépendantes d'un
environnement prêt, et non des temps d'exécution garantis.

| Jour 1 | Durée |
| --- | --- |
| Accueil et vérification de la préparation | 15 min |
| M0 | 30 min |
| M1 | 1 h |
| M2 | 1 h |
| TP1 — Essentiel | 1 h 30 |
| M3 | 45 min |
| TP2 — Essentiel | 1 h 30 |
| Débriefing | 30 min |
| **Total J1** | **7 h** |

| Jour 2 | Durée |
| --- | --- |
| M4 | 1 h |
| TP3 — Essentiel | 2 h |
| M5 | 45 min |
| M6 | 30 min |
| TP4 — Essentiel | 1 h 30 |
| M7 | 1 h |
| Bilan | 15 min |
| **Total J2** | **7 h** |

Les modules M0 à M7 correspondent au support de présentation. Les quatre TP
occupent **6 h 30**. Leurs guides identifient le périmètre **essentiel** et les
preuves nécessaires pour passer à la suite. Les **approfondissements** conservent
les exercices plus longs : seconde génération individuelle au TP1, répétitions
de robustesse au TP2, dataset complet et mutations supplémentaires au TP3,
installation CI de zéro et test de suppression répété au TP4.

Ils sont prévus **hors des 14 h**, ou si vous avez de l'avance, **sans supprimer
les étapes essentielles**. Une régénération nécessaire pour corriger un échec
n'est pas une répétition facultative : elle peut imposer d'allonger l'atelier.
Le formateur doit vérifier les accès et préparer l'environnement CI ainsi que
le fournisseur du juge **avant la séance** :
voir la [checklist de préparation]({{ '/preparation/' | relative_url }}).
Sans ces prérequis, prévoyez du temps supplémentaire ou annoncez une démonstration
partielle ; ni un juge ignoré ni une CI fictive ne valent validation.

## La règle du jeu

**Vous pilotez la création, vous gardez la vérification.** En TP1, faites écrire le
serveur par l'agent. En TP2, écrivez les instructions qui manquent au modèle. En TP3,
écrivez vous-mêmes les critères de jugement. En TP4, rendez ces critères opposables
en revue et en CI.

> Un serveur qui fonctionne n'est pas nécessairement conforme. Un test vert n'est
> utile que s'il sait aussi détecter un défaut. Un Skill n'est pas une garantie :
> c'est une hypothèse à vérifier.

Les squelettes du dépôt restent volontairement incomplets. Ces guides ne constituent
pas un corrigé : ils vous accompagnent pour produire et défendre vos propres choix.

## Les supports

- [Dépôt de formation]({{ site.repository_url }}) : sources, données et squelettes.
- [Catalogue métier]({{ site.repository_url }}/blob/main/domaine/catalogue-services.json) :
  les données communes à tous les TP.
- [Conventions maison]({{ site.repository_url }}/blob/main/domaine/conventions.md) :
  la cible de conformité, à étudier **après la première génération vanilla**.
- [Fiche de sortie]({{ '/fiche-de-sortie/' | relative_url }}) : le canevas de
  l'atelier M7, une capacité de votre organisation en neuf lignes.

La publication de ces guides se fait sur **GitHub Pages**. La gouvernance du Skill
en TP4 se pratique sur **GitLab** : ce sont deux usages distincts.
