# TP3 — Évaluer le Skill `create-quarkus-mcp-server`

**[Guide détaillé sur GitHub Pages](https://sciam-fr.github.io/formation-ia/tp3/)**
· [Source du guide](../docs/tp3.md)

Harnais d'éval 100 % Java. **Le squelette fournit la plomberie ; vous écrivez ce qui porte
du jugement** : les assertions de conformité et la grille du juge.

## Deux parcours

| Parcours | Sections du guide | Preuve de sortie |
| --- | --- | --- |
| **Essentiel — 2 h cible** | 1 à 6 ; 7 si raisonnable | Quatre stubs complétés, exemple examiné, une mutation rouge puis corrigée verte, grille écrite et premier verdict LLM calibré avec lecture humaine sur la génération TP2 |
| **Approfondissement** | 3, 4 et 7 | Couverture élargie, mutations supplémentaires, dataset complet et répétitions |

Une demande adverse par binôme est mutualisée si le temps de génération le permet ;
sinon, notez « non exécuté ». Distinguez cette sortie du serveur issu du TP2 :
évaluer ce dernier ne signifie pas avoir exécuté un cas du dataset.
Les approfondissements sont hors des 14 h ou si avance, sans supprimer l'essentiel.
Le fournisseur du juge doit être préparé avant séance pour le parcours complet.

## Fourni (ne pas réécrire)
- `GeneratedProject` — lit le POM et les sources du serveur généré.
- `JugeLLM` — client LLM-as-a-Judge agnostique (endpoint/clé/modèle via env).
- `ConformiteDeterministeTest` — un scorer exemple + des stubs `@Test` à compléter.
- `ConformiteJugeTest` — l'appel au juge est câblé ; **la grille est à écrire**.
- `dataset/` — cas happy / realistic / adverse.

## À écrire (vous)
- Les quatre stubs d'assertions déterministes (`// TODO`) : deux tools, resource,
  séparation. Ne désactivez aucun test ; examinez aussi le scorer exemple.
- La grille (rubric) du LLM-as-a-Judge.

## Lancer
    export LLM_ENDPOINT=...  LLM_API_KEY=...  LLM_MODEL=...
    mvn -Dserveur.genere.dir=/chemin/vers/le/serveur/genere test

Le juge (`ConformiteJugeTest`) ne s'active que si `LLM_ENDPOINT` est défini.
Sans juge exécuté et calibré, le TP est **partiel et non validé**.
Le harnais **ne génère, ne compile ni ne démarre** le serveur et **ne boucle pas**
automatiquement sur le dataset. Vérifiez séparément build, démarrage et appels MCP ;
archivez les résultats en indiquant la provenance exacte du projet évalué.

## L'idée
En TP2 vous avez écrit la conformité *en prose* (le Skill). Ici vous l'écrivez *en
assertions exécutables*. Prose → test : c'est ce qui rend le Skill vérifiable, donc
gouvernable (TP4). **On délègue la création, on garde la vérification.**
