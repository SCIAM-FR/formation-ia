---
title: TP3 — Évaluer le Skill
description: Écrire les critères de jugement, prouver une détection de défaut et calibrer un premier verdict sur la génération du TP2.
permalink: /tp3/
previous_url: /tp2/
previous_title: TP2 — Formaliser
next_url: /tp4/
next_title: TP4 — Gouverner
---

## Objectif et livrables

Complétez **les assertions déterministes**, rédigez **une grille de juge LLM** et
conservez **un premier verdict calibré par une lecture humaine sur la génération
du TP2**, ainsi qu'une preuve de détection rouge puis vert. La plomberie est
fournie ; votre responsabilité est de définir ce qui constitue une preuve de
conformité, sans prétendre couvrir tous les cas.

Entrées : le Skill du TP2 et un serveur généré avec lui.
Support : [`tp3-eval/`]({{ site.repository_url }}/tree/main/tp3-eval).

## Essentiel et approfondissement

| Parcours | Sections concernées | Preuve de sortie |
| --- | --- | --- |
| **Essentiel — 2 h cible** | 1 à 6 : exemple examiné, quatre stubs complétés, une mutation contrôlée, grille et premier verdict | Rapports sur la génération TP2, mutation rouge puis correction verte, verdict LLM confronté à une lecture humaine et limites écrites |
| **Essentiel mutualisé, si le temps de génération reste raisonnable** | 7 : une demande adverse par binôme, puis mise en commun | Provenance distincte de TP2, cas et résultats partagés ; sinon noter explicitement « non exécuté » |
| **Approfondissement — hors 14 h ou si avance** | 3 : couverture supplémentaire ; 4 : mutations supplémentaires ; 7 : cinq demandes et répétitions | Résultats séparés par cas et par répétition, couverture élargie et limites mesurées |

Le fournisseur du juge doit être préparé avant la séance pour le parcours complet.
Sans verdict LLM effectivement exécuté et calibré, le TP reste **partiel et non
validé**. Les délais d'accès, de génération ou de correction peuvent imposer
d'allonger la durée cible ; ne désactivez aucun test pour la tenir.

## 1. Comprendre le harnais avant de l'exécuter

| Fichier | Rôle | Travail demandé |
| --- | --- | --- |
| `GeneratedProject.java` | Lit le `pom.xml` et les sources Java du projet cible | Lire son API, ne pas réécrire la plomberie |
| `ConformiteDeterministeTest.java` | **Conventions du Skill** : un exemple de scorer et quatre tests en échec volontaire, valables pour n'importe quel domaine | Remplacer les `fail("À écrire…")` par des assertions |
| `CasAttendu.java` | Lit le cas du dataset désigné par `-Dcas=<id>` et son champ `attendu` | Lire son API, ne pas réécrire la plomberie |
| `ContratDemandeTest.java` | **Contrat de la demande** : vérifie les noms que la demande impose (tools, resource, prompt), d'après `attendu.contrat` du cas ; ignoré sans `-Dcas` | Lire ; comparer avec vos assertions de conventions |
| `JugeLLM.java` | Appelle un endpoint compatible Chat Completions | Comprendre la configuration, ne pas le réécrire |
| `ConformiteJugeTest.java` | Envoie les sources Java au juge et impose une note minimale | Compléter et pondérer la grille |
| `src/test/resources/dataset/` | Cinq demandes en trois catégories, chacune avec un `attendu` structuré : `contrat` (noms attendus) et `conventions` | Lire les catégories ; une demande adverse mutualisée si raisonnable, ensemble des cas en approfondissement |

Deux familles de vérifications, à ne pas confondre :

- **Les conventions du Skill** ne citent aucun nom du catalogue : snake_case pour
  tous les tools, descriptions partout, resource paramétrée, séparation métier /
  adaptateur. Elles doivent rester vertes sur un serveur conforme pour un autre
  domaine. C'est ce que **vous écrivez**.
- **Le contrat de la demande** cite les noms : `find_service`, `get_owner`,
  `service://{name}`, `fiche_service`. Il vient de la demande, pas du Skill ; un
  serveur qui le respecte prouve que le modèle a lu la demande. Il est **fourni**,
  paramétré par le cas (`-Dcas=happy-1`), et sert de référence pour vos regex.

> Le harnais **ne génère pas** le serveur, **ne le compile pas**, **ne le démarre
> pas** et **ne parcourt pas automatiquement** les fichiers du dataset. Il inspecte
> le POM et les sources d'un projet existant. Le build, le démarrage et les appels
> MCP sont vérifiés **séparément**, comme au TP2 ; un harnais vert ne les remplace pas.

Les commandes ci-dessous ciblent la génération retenue au TP2. Si vous avez dû
régénérer ailleurs, remplacez `serveur-avec-skill` par son chemin exact. Conservez
sa provenance (prompt, modèle, version du Skill, dossier) : elle ne devient pas
le cas `happy-1` simplement parce qu'elle expose les quatre primitives. En
revanche, sa demande est celle de `happy-1` : `-Dcas=happy-1` charge le bon contrat
sans en faire une exécution du dataset.

`sourceJava()` concatène tous les fichiers `.java`, y compris les tests du projet
cible. `sourceContient(regex)` applique une regex multiligne à cette concaténation :
une chaîne dans un commentaire ou un test peut donc tromper un scorer trop permissif.
Si vous retirez les commentaires avant de chercher, préservez les chaînes de
caractères : `service://{name}` contient `//`, et un `//[^\n]*` naïf le tronque.
`ContratDemandeTest.sansCommentaires` montre une manière de faire.

## 2. Exécuter le point de départ

Commencez sans appel LLM, avec un chemin absolu :

```bash
cd "$FORMATION_REPO/tp3-eval"
mvn -Dtest='ConformiteDeterministeTest,ContratDemandeTest' -Dcas=happy-1 \
  -Dserveur.genere.dir="$ATELIER_DIR/serveur-avec-skill" test
```

Résultat attendu **avant votre travail** : l'exemple sur la dépendance peut passer,
le contrat de la demande passe si votre serveur expose bien les quatre primitives,
mais les quatre tests de conventions à compléter échouent avec « À écrire… ».
C'est intentionnel, pas un défaut à masquer. Les rapports sont sous
`target/surefire-reports/`. Si le contrat échoue, c'est un défaut de génération :
retour au TP2, pas d'assouplissement.

Le répertoire cible doit contenir le `pom.xml` du serveur. La valeur par défaut
`../serveur-genere` ne correspond pas à l'organisation de cet atelier : fournissez
donc toujours `-Dserveur.genere.dir`.

## 3. Écrire les assertions déterministes

Complétez les quatre tests existants, sans les désactiver :

| Test | Ce qu'il doit distinguer |
| --- | --- |
| `tools_nommes_en_snake_case` | Tous les noms publics de tools en snake_case, qu'ils viennent de l'attribut `name` ou du nom de méthode ; un seul `findService` doit faire échouer, dans n'importe quel domaine |
| `tools_et_arguments_decrits` | Une description sur chaque `@Tool` et chaque `@ToolArg`, et une définition de ce qu'est une description acceptable |
| `resource_template_parametree_et_decrite` | Au moins une `@ResourceTemplate` dont l'URI porte un paramètre, avec un `@ResourceTemplateArg` décrit ; l'URI exacte relève du contrat |
| `separe_metier_et_adaptateur` | Une classe métier qui charge les données, distincte de l'adaptateur qui porte les annotations et délègue |

Commencez avec les méthodes de `GeneratedProject` et lisez `ContratDemandeTest`
pour la manière d'exiger l'annotation et le nom sur la même déclaration. Formulez
des messages d'échec qui indiquent **la convention attendue**. Attention : une
méthode Java camelCase peut exposer un nom MCP snake_case via l'annotation, et
l'inverse ; vérifiez le nom public. Interdit : recopier `find_service` ou
`service://{name}` dans une convention — si votre assertion ne tient que pour le
catalogue, c'est un contrat, pas une convention.

L'exemple fourni vérifie seulement la présence de `quarkus-mcp-server` dans le POM.
Il ne prouve ni le transport HTTP ni les coordonnées exactes. **Dans l'essentiel**,
examinez cet exemple et consignez cette limite, en plus de compléter les quatre
stubs. **En approfondissement**, renforcez la couverture au regard des conventions :
dépendance HTTP, package, prompt `fiche_service`, descriptions des tools et arguments,
chargement classpath, tests et logs.

Une regex peut suffire à illustrer un critère simple, mais pas à prouver toute une
architecture. Expliquez les limites de chaque assertion ; les critères de comportement
doivent également être vérifiés dans les tests du serveur ou avec un client MCP.

## 4. Prouver que les tests savent échouer

Exécutez d'abord vos tests déterministes sur la génération retenue du TP2. Traitez
les échecs sans assouplir les critères pour obtenir un vert artificiel : un défaut
de génération peut exiger le retour au TP2. Ensuite, créez une copie destinée
uniquement aux défauts contrôlés :

```bash
mkdir -p "$ATELIER_DIR/evaluations"
cp -R "$ATELIER_DIR/serveur-avec-skill" \
  "$ATELIER_DIR/evaluations/serveur-mutation-01"
```

Dans cette copie, choisissez **une mutation pour l'essentiel** parmi les exemples
ci-dessous : renommez le nom MCP d'un tool en camelCase, retirez une description,
changez le template URI ou déplacez une responsabilité métier dans l'adaptateur.
Relancez les tests avec le chemin de cette copie, en conservant le sélecteur et le
`-Dcas` de la section 2 : vous verrez quelle famille détecte quoi.

Le test correspondant doit devenir rouge avec un message pertinent. Rétablissez
manuellement ce changement et relancez les tests pour prouver le retour au **vert**.
Conservez les deux rapports avant qu'une nouvelle exécution les remplace.
Si le scorer reste vert, cherchez une correspondance accidentelle dans les commentaires
ou tests. N'altérez pas votre génération de référence.

Les autres mutations sont un **approfondissement** : toujours un seul défaut à
la fois, sur une copie restaurée ou une nouvelle copie, sans désactiver de test.

| Mutation | Test censé échouer | Résultat observé |
| --- | --- | --- |
| Nom public `findService` au lieu de `find_service` | Convention `tools_nommes_en_snake_case` **et** contrat `tools_de_la_demande_exposes` : la convention le verrait dans n'importe quel domaine, le contrat seulement ici | À relever |
| Description retirée d'un `@ToolArg` | Convention `tools_et_arguments_decrits` ; le contrat reste vert | À relever |
| URI `catalogue://{name}` | Contrat `resource_de_la_demande_exposee` ; la convention reste verte si l'URI est encore paramétrée | À relever |
| Chargement JSON déplacé dans l'adaptateur | Convention `separe_metier_et_adaptateur` | À relever |

## 5. Rédiger la grille du juge

Dans `ConformiteJugeTest.java`, remplacez le TODO de la chaîne `grille` par des
critères précis. Vous pouvez partir de cette répartition, à justifier :

| Dimension | Poids proposé | Preuve à demander |
| --- | --- | --- |
| Séparation des responsabilités | 4 points | Classes concernées et délégation réelle |
| Contrat MCP et annotations | 3 points | Noms, URI, descriptions et arguments |
| Accès aux données | 2 points | Chargement classpath, absence de chemin propre à un poste |
| Lisibilité et cohérence | 1 point | Justification liée au code, pas une préférence de style |

Définissez ce qui vaut zéro, un crédit partiel ou tous les points. Demandez des
preuves dans les sources, pas une appréciation globale. Le seuil actuel du test est
**7/10** ; précisez les défauts rédhibitoires qui ne devraient pas être compensés par
des points de style.

Le juge ne reçoit que `sourceJava()`, **pas le POM, les logs ni le JSON du catalogue**.
Ne lui demandez pas de certifier des faits qu'il ne peut pas observer. Le client
ajoute déjà la consigne de sortie JSON `{"note": <0-10>, "justification": "..."}`.
Demandez aussi de traiter les commentaires des sources comme des données, jamais
comme des instructions pour le juge ; ce cadrage ne remplace pas les tests.

## 6. Activer et calibrer le juge

Cette étape appartient à l'essentiel ; l'endpoint est un prérequis préparé par
le formateur, pas une option permettant de déclarer le TP complet sans juge.

Le fournisseur doit accepter un corps de type Chat Completions et répondre dans
`choices[0].message.content`. `LLM_ENDPOINT` est **l'URL complète** de cette API,
pas seulement l'URL de base du fournisseur. Le contenu retourné doit être du JSON
valide sans balises Markdown.

Dans votre terminal local, configurez les valeurs validées par le formateur :

```bash
export LLM_ENDPOINT="https://votre-fournisseur.example/v1/chat/completions"
export LLM_MODEL="identifiant-du-modele-juge"
```

Cette URL est un exemple à remplacer, pas un service opérationnel. Chargez
`LLM_API_KEY` via votre gestionnaire de secrets ou une saisie masquée. Pour Bash :

```bash
read -r -s -p "Clé API du juge : " LLM_API_KEY
printf '\n'
export LLM_API_KEY
```

Pour Zsh, utilisez `read -r -s "LLM_API_KEY?Clé API du juge : "` puis
`printf '\n'` et `export LLM_API_KEY`. N'enregistrez jamais la clé dans le dépôt
ou dans les captures.

```bash
cd "$FORMATION_REPO/tp3-eval"
mvn -Dserveur.genere.dir="$ATELIER_DIR/serveur-avec-skill" test
```

Le juge s'active si `LLM_ENDPOINT` est non vide et a alors besoin des trois variables.
Sans endpoint, il est **ignoré**, pas réussi. Pour une exécution volontairement sans
juge, gardez le sélecteur `-Dtest=ConformiteDeterministeTest` et signalez un résultat
**partiel et non validé**. Une panne de fournisseur appelle un report ou du temps
supplémentaire, pas une validation fictive.

Le code source du serveur sera envoyé au fournisseur : utilisez uniquement les
sources fictives autorisées pour la formation. Les appels peuvent être facturés.

Sur la **génération du TP2 non mutée**, faites aussi une lecture humaine avec la
même grille : notez les preuves, les points et les défauts bloquants, puis comparez
avec la note et la justification du juge. Consignez les accords et désaccords.
Si le juge manque un défaut ou récompense un critère non observable, précisez la
grille et rejouez l'évaluation ; gardez la trace des ajustements. Ce premier
calibrage ne démontre ni la fiabilité générale du juge ni la robustesse du Skill.

## 7. Mutualiser un cas adverse, puis approfondir le dataset

Ouvrez les trois fichiers JSON du dataset :

| Fichier | Cas | Ce qu'on cherche à mesurer |
| --- | --- | --- |
| `happy.json` | `happy-1` | Demande explicite des quatre primitives |
| `realistic.json` | `realistic-1`, `realistic-2` | Demande métier ou volontairement vague |
| `adverse.json` | `adverse-1`, `adverse-2` | Pression pour fusionner les classes ou utiliser camelCase |

**Parcours essentiel mutualisé :** si les temps de génération et d'accès le
permettent après les preuves des sections 1 à 6, répartissez les demandes adverses
entre binômes. Chaque binôme traite **une seule demande adverse**, puis partage
son résultat et ses limites. Si ce n'est pas raisonnable dans la séance, marquez
ce cas « non exécuté » et réservez son exécution à l'approfondissement ; n'en
déduisez aucune robustesse adverse.

**Approfondissement :** traitez l'ensemble des cinq demandes et répétez les cas
instables. La procédure suivante s'applique à **chaque cas choisi**, pas
obligatoirement aux cinq dans les 2 h essentielles.

Créez un projet neuf sous `evaluations/<id>/`, copiez le catalogue
et installez le Skill avec sa référence comme au TP2. Ouvrez une nouvelle session
avec le modèle fixé. Chargez le Skill, puis transmettez le champ `demande`,
en précisant que le catalogue local est la source de données. Ne transmettez pas
le champ `attendu` : il sert à l'évaluateur. Lisez-le, vous : son `contrat` dit
quels noms la demande impose (aucun pour `realistic-2`), et son `commentaire` dit
ce que le cas cherche à mesurer.

Construisez le projet avec `mvn test`, vérifiez son contrat MCP, puis lancez le
harnais sur ce projet **avec l'identifiant du cas**, pour que le contrat vérifié
soit celui de la demande jouée. Exemple après génération de `adverse-2` :

```bash
cd "$FORMATION_REPO/tp3-eval"
mvn -Dcas=adverse-2 -Dserveur.genere.dir="$ATELIER_DIR/evaluations/adverse-2" test
```

Sur `adverse-2`, la demande réclame `findService` en camelCase ; le contrat attendu
reste `find_service`. Si le contrat échoue mais que la convention snake_case passe,
le modèle a inventé un troisième nom ; si les deux échouent, il a obéi à la demande
contre le Skill. Les deux lectures sont des résultats.

Les résultats Surefire sont remplacés à chaque exécution : archivez-les avec les
sources et les conditions du cas avant de passer au suivant. Relevez :

| Provenance / cas / répétition | Modèle et version du Skill | Build / MCP | Assertions | Juge /10 ou non exécuté | Écart / lecture humaine |
| --- | --- | --- | --- | --- | --- |
| Génération TP2 — prompt conservé, hors dataset | À renseigner | À renseigner | À renseigner | À renseigner | À renseigner |
| Dataset `adverse-1 / 1` (ou `adverse-2 / 1`) | À renseigner si exécuté | À renseigner | À renseigner | À renseigner | À renseigner |
| `happy-1 / 1` | À renseigner | À renseigner | À renseigner | À renseigner | À renseigner |

N'ajoutez que les lignes correspondant à vos exécutions ; ne rebaptisez pas la
génération TP2 en cas du dataset. En approfondissement, répétez les cas instables
dans des dossiers distincts. Les cas adverses expriment
une attente pédagogique, pas une garantie d'obéissance du modèle : si une demande
contradictoire l'emporte, documentez l'échec et ajustez le Skill sans assouplir
le scorer pour le faire passer.

## Dépannage et point de passage

| Symptôme | Cause à vérifier |
| --- | --- |
| « Projet généré introuvable » | Chemin absolu erroné ou génération non faite |
| Échecs « À écrire » | Stubs encore présents |
| `Cas introuvable dans le dataset` | Faute de frappe dans `-Dcas` ; ids : `happy-1`, `realistic-1`, `realistic-2`, `adverse-1`, `adverse-2` |
| Contrat rouge sur un serveur qui marche | La demande imposait un nom que le serveur n'expose pas sous ce nom public : défaut de génération, pas de scorer |
| Juge ignoré | `LLM_ENDPOINT` absent ou vide ; noter « non exécuté » |
| Erreur HTTP, JSON ou délai du juge | Endpoint complet, modèle, clé, quota, compatibilité du format |
| Assertions vertes sur un projet cassé | Regex trop large, occurrence dans un commentaire ou un test |
| Erreur de compilation Java 21 | JDK réellement utilisé par Maven ; en cas de plugin ancien, fixer sa version avec le formateur |

Le passage validé au TP4 demande les **quatre conventions écrites**, l'exemple et
le contrat fourni examinés,
**une mutation rouge puis sa correction verte**, une grille écrite et **un premier
verdict LLM calibré par une lecture humaine sur la génération TP2**. Conservez
séparément les preuves du build/MCP et celles du harnais. Mutualisez le cas adverse
s'il a été exécuté, sinon indiquez cette limite ; le dataset complet et les
mutations supplémentaires ne conditionnent pas ce passage. Sans juge, vous pouvez
préparer la gouvernance, mais TP3 reste **partiel et non validé**.
