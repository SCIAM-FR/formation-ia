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

La publication de ces guides se fait sur **GitHub Pages**. La gouvernance du Skill
en TP4 se pratique sur **GitLab** : ce sont deux usages distincts.
