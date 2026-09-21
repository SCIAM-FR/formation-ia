# TP3 — Évaluer le Skill `create-quarkus-mcp-server`

**[Guide détaillé sur GitHub Pages](https://sebastienblanc.github.io/formation-ia/tp3/)**
· [Source du guide](../docs/tp3.md)

Harnais d'éval 100 % Java. **Le squelette fournit la plomberie ; vous écrivez ce qui porte
du jugement** : les assertions de conformité et la grille du juge.

## Fourni (ne pas réécrire)
- `GeneratedProject` — charge et inspecte le serveur généré.
- `JugeLLM` — client LLM-as-a-Judge agnostique (endpoint/clé/modèle via env).
- `ConformiteDeterministeTest` — un scorer exemple + des stubs `@Test` à compléter.
- `ConformiteJugeTest` — l'appel au juge est câblé ; **la grille est à écrire**.
- `dataset/` — cas happy / realistic / adverse.

## À écrire (vous)
- Les assertions déterministes (`// TODO`) : tools, resource, séparation, nommage.
- La grille (rubric) du LLM-as-a-Judge.

## Lancer
    export LLM_ENDPOINT=...  LLM_API_KEY=...  LLM_MODEL=...
    mvn -Dserveur.genere.dir=/chemin/vers/le/serveur/genere test

Le juge (`ConformiteJugeTest`) ne s'active que si `LLM_ENDPOINT` est défini.

## L'idée
En TP2 vous avez écrit la conformité *en prose* (le Skill). Ici vous l'écrivez *en
assertions exécutables*. Prose → test : c'est ce qui rend le Skill vérifiable, donc
gouvernable (TP4). **On délègue la création, on garde la vérification.**
