# Démonstration — le faux vert d'un scorer (M4)

Objectif : montrer en cinq minutes qu'une regex qui trouve `find_service` dans les
sources peut passer au vert alors qu'**aucun tool** de ce nom n'existe, puis montrer
la vérification qui détecte le défaut. À faire **avant** que les participants écrivent
leurs assertions (TP3, section 3).

## Le projet piège

`projet-piege/` imite ce que le harnais `tp3-eval` lit : un `pom.xml` qui déclare
`quarkus-mcp-server-http` et des sources Java. Il ne compile pas et ne démarre pas ;
ce n'est pas nécessaire, **le harnais ne lit que du texte**.

- `CatalogueMcpServer.java` : un commentaire et une chaîne de log mentionnent
  `find_service`, mais la seule méthode annotée `@Tool` expose `chercher`.
- `CatalogueMcpServerTest.java` : le nom d'une méthode de test contient
  `find_service`. `GeneratedProject.sourceJava()` concatène **aussi** `src/test`.

## Déroulé

1. **Le scorer naïf.** Dans un projet de démonstration séparé (jamais dans
   `tp3-eval/` du dépôt), écrivez ou montrez :

   ```java
   @Test
   void contrat_find_service_naif() {
       assertTrue(projet.sourceContient("find_service"));
   }
   ```

   Lancez-le sur le piège. Avec le scorer naïf, le test est **vert**. Demandez à la
   salle si le serveur expose le tool. Non. Puis lancez le contrat fourni :

   ```bash
   cd tp3-eval
   mvn -Dtest=ContratDemandeTest -Dcas=happy-1 \
     -Dserveur.genere.dir="$FORMATION_REPO/formateur/demo-scorer-trompeur/projet-piege" test
   ```

   Rouge sur `find_service` et `get_owner` : le contrat fourni exige l'annotation et
   le nom public sur la même déclaration.

2. **Pourquoi.** Ouvrez `CatalogueMcpServer.java` et le test : le nom apparaît
   dans un commentaire, dans un log et dans un nom de méthode de test.
   `sourceContient` applique une regex `DOTALL` sur la concaténation de tous les
   `.java`.

3. **La parade.** Exiger l'annotation **et** le nom public sur la même déclaration,
   après suppression des commentaires, en ignorant `src/test`. Voir
   [`ScorerRobusteExemple.java`](ScorerRobusteExemple.java) et, dans le harnais,
   `ContratDemandeTest.exposeTool`. Relancez : **rouge** sur le piège, **vert** sur
   une génération conforme. Faites remarquer au passage que le piège passe la
   convention snake_case (« chercher » est un nom valide) : c'est le contrat de la
   demande qui le détecte, pas une convention du Skill — les deux familles servent.

4. **Le message.** Un scorer n'est crédible qu'après avoir prouvé qu'il sait
   échouer (section 4 du TP3, mutation contrôlée). Un vert qui ne peut pas devenir
   rouge ne mesure rien.

## Variante : `@Tool` sans `name`

Dans l'extension Quarkus MCP, `@Tool` sans attribut `name` expose le **nom de la
méthode Java**. Une méthode `findService()` annotée `@Tool` produit donc un tool
`findService`, pas `find_service`. Le scorer robuste doit accepter soit
`@Tool(name = "find_service")`, soit une méthode **nommée** `find_service` ; c'est
la seconde mutation intéressante à montrer si le temps le permet.
