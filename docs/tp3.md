---
title: TP3 — Évaluer le Skill
description: Écrire les scorers du harnais d'éval, prouver qu'ils détectent un défaut, calibrer un LLM-as-a-judge.
permalink: /tp3/
previous_url: /tp2/
previous_title: TP2 — Formaliser
next_url: /tp4/
next_title: TP4 — Gouverner
---

## Objectif

Au TP2 vous avez écrit la conformité en prose (le Skill). Ici vous l'écrivez en **tests
exécutables**. Le harnais JUnit est fourni ; vous écrivez ce qui porte du jugement :
quatre assertions déterministes et la rubric d'un LLM-as-a-judge.

**Livrables**

- Les quatre tests de `ConformiteTest` complétés, verts sur votre serveur du TP2.
- Un rapport Surefire rouge sur une mutation, puis vert après correction.
- La rubric de `JugeTest` écrite, un verdict du juge, et votre propre note sur la même grille.

**Durée** : 2 h. **Prérequis** : le serveur généré au TP2 (`$ATELIER_DIR/serveur-avec-skill`,
build vert), JDK 21, Maven, et pour l'étape 4 l'endpoint du juge fourni par le formateur.

## Le harnais en 30 secondes

Dossier [`tp3-eval/`]({{ site.repository_url }}/tree/main/tp3-eval), projet Maven autonome,
quatre fichiers Java, moins de 200 lignes.

| Fichier | Statut | Rôle |
| --- | --- | --- |
| `GeneratedProject.java` | fourni | Le serveur cible vu comme du texte : `pom()`, `source()` (les `.java` de `src/main`, commentaires retirés), `contient(regex)` |
| `ConformiteTest.java` | **à écrire** | Deux exemples fournis, quatre stubs `fail("À écrire…")` |
| `JugeTest.java` | **à écrire** | Envoie `source()` au juge avec votre rubric ; seuil 7/10 ; skipped sans `LLM_ENDPOINT` |
| `JugeLLM.java` | fourni | Client Chat Completions : `LLM_ENDPOINT`, `LLM_API_KEY`, `LLM_MODEL` |

Le harnais **ne génère, ne compile et ne démarre pas** le serveur : il lit des sources. Build
et appels MCP se vérifient comme au TP2.

## Étape 1 — Lancer le harnais tel quel

```bash
cd "$FORMATION_REPO/tp3-eval"
export SERVEUR="$ATELIER_DIR/serveur-avec-skill"
mvn -q -Dtest=ConformiteTest -Dserveur.genere.dir="$SERVEUR" test
```

Attendu : les deux exemples verts, les quatre stubs rouges avec « À écrire ». Rapports dans
`target/surefire-reports/`.

Lisez les deux exemples avant d'écrire. Le premier lit le POM. Le second,
`expose_les_tools_de_la_demande`, montre la technique à réutiliser : une regex qui exige
l'annotation `@Tool` **et** le nom sur la même déclaration. Chercher la chaîne `find_service`
ne suffirait pas, un log ou une variable la contient aussi.

Si cet exemple est rouge, votre serveur n'expose pas les tools demandés : défaut de
génération, retour au TP2.

## Étape 2 — Écrire les quatre assertions

Remplacez chaque `fail(...)` par des assertions sur `SRC`. Une convention ne cite aucun nom du
catalogue : elle doit rester vraie pour un serveur MCP d'un autre domaine.

| Test | Règle | Indice |
| --- | --- | --- |
| `tools_nommes_en_snake_case` | Chaque `@Tool` expose un nom public `^[a-z][a-z0-9_]*$` : `name = "..."` sinon le nom de méthode | Un seul `findService` doit faire échouer le test |
| `tools_et_arguments_decrits` | Chaque `@Tool` et chaque `@ToolArg` porte une `description` non triviale | Fixez un minimum de caractères ; `description = ""` doit échouer |
| `resource_template_parametree_et_decrite` | Au moins une `@ResourceTemplate` dont l'`uriTemplate` contient `{…}`, avec un `@ResourceTemplateArg` décrit | L'URI exacte n'est pas une convention |
| `separe_metier_et_adaptateur` | Les classes annotées MCP ne chargent pas le JSON et délèguent ; une classe non annotée charge les données | Cherchez `ObjectMapper` / `getResourceAsStream` classe par classe |

Chaque message d'échec doit dire la convention attendue et ce qui a été trouvé.

```bash
mvn -q -Dtest=ConformiteTest -Dserveur.genere.dir="$SERVEUR" test
```

Attendu : tout vert. Si un test est rouge sur votre serveur du TP2, c'est soit votre regex,
soit un vrai écart du serveur : tranchez, ne desserrez pas la règle pour passer au vert.

## Étape 3 — Prouver qu'un test détecte un défaut

Mutation testing manuel : une copie, un défaut, un test rouge, correction, vert.

```bash
cp -R "$SERVEUR" "$ATELIER_DIR/serveur-mutation-01"
# dans la copie : renommer le nom public d'un tool en camelCase (name = "findService")
mvn -q -Dtest=ConformiteTest -Dserveur.genere.dir="$ATELIER_DIR/serveur-mutation-01" test
cp -R target/surefire-reports "$ATELIER_DIR/serveur-mutation-01-rouge"
# rétablir le nom, relancer : vert
```

Attendu : `tools_nommes_en_snake_case` rouge, et l'exemple `expose_les_tools_de_la_demande`
aussi. Si votre test reste vert, votre regex matche autre chose.

Autres mutations si vous avez le temps, une à la fois :

| Mutation | Test qui doit devenir rouge |
| --- | --- |
| Retirer la `description` d'un `@ToolArg` | `tools_et_arguments_decrits` |
| Charger le JSON directement dans l'adaptateur | `separe_metier_et_adaptateur` |
| Renommer l'URI en `catalogue://{name}` | Aucune convention : c'est voulu, l'URI relève de la demande |

## Étape 4 — Écrire la rubric et calibrer le juge

Dans `JugeTest.java`, remplacez le `TODO` de la chaîne `rubric`. Le juge ne voit que
`source()` : pas le POM, pas les logs, pas le catalogue. Ne lui demandez que ce qu'il peut
observer, et exigez des preuves dans le code.

| Dimension | Points | Preuve demandée |
| --- | --- | --- |
| Séparation métier / adaptateur | 4 | Classes concernées, délégation réelle |
| Contrat MCP et annotations | 3 | Noms, URI, descriptions, arguments |
| Accès aux données | 2 | Chargement classpath, pas de chemin absolu |
| Lisibilité | 1 | Justifiée par le code, pas par le style |

Précisez ce qui vaut 0, un crédit partiel ou tout, et les défauts rédhibitoires.

Endpoint et modèle sont fournis par le formateur ; la clé ne va ni dans le dépôt ni dans une
capture :

```bash
export LLM_ENDPOINT="https://.../v1/chat/completions"   # URL complète de l'API Chat Completions
export LLM_MODEL="..."
read -r -s -p "LLM_API_KEY : " LLM_API_KEY; echo; export LLM_API_KEY
mvn -q -Dserveur.genere.dir="$SERVEUR" test
```

Le juge répond `{"note": <0-10>, "justification": "..."}`. Notez ensuite **vous-même** le
serveur avec la même rubric et comparez : accords, désaccords, ce que le juge a manqué. Si un
critère non observable a été récompensé, précisez la rubric et relancez. Sans `LLM_ENDPOINT`,
le test est skipped, pas réussi : le TP est alors partiel.

## Dépannage

| Symptôme | Cause |
| --- | --- |
| `Projet généré introuvable` | `-Dserveur.genere.dir` absent ou faux ; le dossier doit contenir `pom.xml` |
| `expose_les_tools_de_la_demande` rouge sur un serveur qui marche | Le nom public n'est pas celui demandé : défaut de génération, pas de scorer |
| Test vert sur une mutation | Regex trop large : elle matche une chaîne ou une autre déclaration |
| `JugeTest` skipped | `LLM_ENDPOINT` non défini dans ce terminal |
| `Appel au juge LLM échoué` | URL incomplète, modèle inconnu, clé, quota, ou réponse non JSON (balises Markdown) |
| Erreur de compilation | Maven n'utilise pas le JDK 21 : `mvn -version` |

## Point de passage

- Quatre conventions écrites, vertes sur le serveur du TP2.
- Une mutation rouge puis verte, rapports conservés.
- Rubric écrite, verdict du juge obtenu et comparé à votre lecture.
- Vous savez dire ce que chaque assertion prouve et ne prouve pas.

Les mutations supplémentaires ne conditionnent pas le passage au TP4.
