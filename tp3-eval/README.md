# TP3 — Évaluer le Skill `create-quarkus-mcp-server`

**[Guide pas à pas](https://sciam-fr.github.io/formation-ia/tp3/)** · [Source](../docs/tp3.md)

Harnais d'éval JUnit, quatre fichiers. La plomberie est fournie ; **vous écrivez les scorers** :
quatre assertions déterministes et la rubric du LLM-as-a-judge. Durée : 2 h.

| Fichier | Statut |
| --- | --- |
| `GeneratedProject` | fourni : `pom()`, `source()` (src/main sans commentaires), `contient(regex)` |
| `ConformiteTest` | **à écrire** : deux exemples fournis, quatre stubs |
| `JugeTest` | **à écrire** : la rubric ; seuil 7/10 ; skipped sans `LLM_ENDPOINT` |
| `JugeLLM` | fourni : client Chat Completions (`LLM_ENDPOINT`, `LLM_API_KEY`, `LLM_MODEL`) |

## Lancer

```bash
export SERVEUR="$ATELIER_DIR/serveur-avec-skill"
mvn -q -Dtest=ConformiteTest -Dserveur.genere.dir="$SERVEUR" test     # sans juge
export LLM_ENDPOINT=... LLM_MODEL=... LLM_API_KEY=...
mvn -q -Dserveur.genere.dir="$SERVEUR" test                           # avec juge
```

Le harnais ne génère, ne compile et ne démarre pas le serveur. Rapports : `target/surefire-reports/`.

## Étapes
1. Lancer tel quel : deux exemples verts, quatre stubs rouges.
2. Écrire les assertions : tout vert sur le serveur du TP2.
3. Mutation : une copie, `findService` en camelCase, rouge, correction, vert.
4. Rubric et juge : verdict LLM comparé à votre propre note.

Corrigé formateur : `formateur/corrige-assertions/`.
