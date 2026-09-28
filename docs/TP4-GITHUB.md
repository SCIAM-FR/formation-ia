---
title: TP4 — Gouverner le Skill sur GitHub
description: Variante GitHub — faire du Skill un actif revu, évalué et protégé, puis organiser sa réduction lorsque le modèle progresse.
permalink: /tp4-github/
previous_url: /tp4/
previous_title: TP4 — Choisir la forge
next_url: /fiche-de-sortie/
next_title: Fiche de sortie (M7)
---

> **Variante GitHub.** Le même TP existe pour [GitLab]({{ '/tp4-gitlab/' | relative_url }}) ;
> la [page d'entrée du TP4]({{ '/tp4/' | relative_url }}) donne la correspondance des
> termes. Suivez la variante de la forge préparée par le formateur, sans mélanger.

## Objectif et livrables

Obtenez une **pull request relue**, un **workflow GitHub Actions qui évalue la
génération de cette PR** et des **règles de protection vérifiées**. Définissez aussi
qui peut charger le Skill et comment le faire évoluer.

Entrées : Skill et références du TP2, harnais complété du TP3, accès à un dépôt
GitHub d'atelier dans une **organisation**, et à un runner Actions, hébergé par
GitHub ou auto-hébergé. Certaines protections dépendent de l'offre : sur
github.com, les règles de branche d'un dépôt privé et la revue obligatoire par les
propriétaires demandent GitHub Team ou Enterprise, et les propriétaires doivent
être des équipes de l'organisation. Vérifiez les possibilités avec le formateur.

## Essentiel et approfondissement

| Parcours | Sections concernées | Preuve de sortie |
| --- | --- | --- |
| **Préparation formateur — avant séance, hors 14 h** | 1, 3 et 4 : dépôt, runner, environnement OpenCode, modèle, fournisseur du juge et secrets autorisés prévalidés | Workflow de PR réellement générateur et évaluateur dans un contexte de confiance ; infrastructure prête pour les versions des participants |
| **Essentiel — 1 h 30 cible** | 1 à 6 : import des travaux, protections, inspection du workflow et des secrets préparés, permissions et PR ; 7 : définir le protocole de suppression | `main` protégé, revue Code Owners selon l'offre, vraie génération depuis la PR avec évaluation, blocage rouge puis retour vert, chargement Skill allow/deny observé |
| **Approfondissement — hors 14 h ou si avance** | 3 et 4 : installation CI depuis zéro / parcours autonome ; 3 : dataset multi-cas ; 6 : diagnostics supplémentaires ; 7 : test de suppression répété | Chaîne autonome éprouvée, résultats par cas, comparaison répétée avec/sans instruction |

Les participants **configurent et prouvent les protections et la revue** ; la
préparation du runner ne fait pas cet exercice à leur place. Si l'infrastructure
n'est pas prête, allongez l'atelier ou annoncez une démonstration **partielle et non
validée**. Le temps cible ne justifie ni génération fictive ni suppression de test.

> Les fichiers livrés dans `tp4-gouvernance/` sont des **exemples à adapter**.
> Le workflow contient un `echo "TODO atelier…"` : il ne génère aucun serveur en l'état.
> Les permissions OpenCode sont elles aussi un exemple, pas une configuration
> à recopier aveuglément.

## 1. Préparer le dépôt de gouvernance

**Essentiel :** utilisez le dépôt GitHub d'atelier prévalidé par le formateur, dans
l'organisation prévue. Sa création depuis zéro relève de la préparation ou du
parcours autonome. Pour garder les chemins fournis cohérents, conservez cette
organisation à sa racine :

```text
.github/
  workflows/evaluer-le-skill.yml
CODEOWNERS
domaine/
tp2-skill/
  skills/create-quarkus-mcp-server/SKILL.md
  references/conventions-quarkus-mcp.md
tp3-eval/
  pom.xml
  src/test/...
```

Dans un clone de ce dépôt, copiez vos versions **complétées** des supports,
pas les squelettes initiaux. Copiez aussi le `.gitignore` des supports, puis ajoutez
les exclusions nécessaires pour les sorties de génération et les secrets.
Utilisez le workflow **adapté et prévalidé par le formateur**, sans l'écraser par le
template contenant l'`echo`. Adaptez le `CODEOWNERS` fourni.
En préparation ou en parcours autonome, partez des fichiers de `tp4-gouvernance/`
et effectuez les adaptations des sections 3 et 4 avant d'en attendre une validation.
Vérifiez `git status` avant tout commit.

Si vous reprenez le clone de formation plutôt qu'un dépôt séparé, ajoutez un remote
distinct, sans remplacer `origin` :

```bash
git remote add atelier-github <URL_DU_DEPOT_GITHUB>
git remote -v
```

Remplacez `<URL_DU_DEPOT_GITHUB>` avant exécution. Faites valider l'import initial
de `main` selon les droits du dépôt, puis travaillez uniquement sur des branches de
contribution. Ne poussez pas de clés, de configuration personnelle ou de sorties
du modèle contenant des données non autorisées.

## 2. Protéger `main` et rendre la revue effective

Dans les paramètres du dépôt, ouvrez **Rules / Rulesets**, ou **Branches / Branch
protection rules** sur les dépôts qui utilisent encore l'ancien modèle. Créez une
règle qui cible `main` :

1. **Require a pull request before merging** : le push direct est refusé.
2. **Require approvals** avec au moins une approbation, **Require review from Code
   Owners**, et rejet des approbations obsolètes après un nouveau commit.
3. **Require status checks to pass before merging** : ajoutez le check du job
   d'évaluation, sous le nom exact qu'il porte dans le workflow.
4. **Block force pushes** et **Restrict deletions**.
5. N'autorisez **aucun contournement**, administrateurs compris. Une règle que les
   propriétaires du dépôt peuvent ignorer ne prouve rien pendant l'exercice.

Le fichier fourni contient :

```text
/tp2-skill/skills/   @platform-team
```

Sur GitHub, un propriétaire est un utilisateur ou une **équipe d'organisation**
notée `@organisation/nom-de-l-equipe`, avec un accès en écriture au dépôt.
Remplacez `@platform-team` par l'équipe réelle. Ajoutez la propriété des
références et du harnais : changer la convention ou affaiblir le test peut
contourner une protection portant seulement sur le Skill. Si vous avez déplacé le
Skill sous `/skills/`, adaptez aussi le motif. GitHub signale les erreurs de
syntaxe et les propriétaires inconnus dans l'affichage du fichier : vérifiez-le.

**Un fichier CODEOWNERS n'impose pas à lui seul une approbation bloquante.**
C'est la case « Require review from Code Owners » de la règle de branche qui la rend
obligatoire. Vérifiez sur une PR réelle que les propriétaires sont sollicités et
que la fusion reste bloquée sans leur approbation. Si l'offre ne le permet pas,
documentez cette limite et organisez une revue manuelle ; ne la présentez pas
comme une protection automatique.

## 3. Remplacer la génération fictive du workflow

**Préparation formateur / parcours autonome :** les opérations d'installation et
de câblage de cette section se font avant la séance, ou en approfondissement hors
14 h. **Dans l'essentiel**, inspectez le workflow préparé, repérez chacune des
étapes ci-dessous et prouvez leur exécution sur votre PR en section 6.
Le dépôt ne livre pas cette chaîne opérationnelle clé en main.

Ouvrez `.github/workflows/evaluer-le-skill.yml`. Le job fourni s'exécute sur
l'événement `pull_request` vers `main`, sur `ubuntu-latest`, installe un JDK 21
Temurin avec le cache Maven, puis appelle le harnais. Il n'installe pas OpenCode :
prévoyez une installation explicite à version fixée, par exemple avec
`npm install -g opencode-ai@<version>`, ou un runner auto-hébergé préparé selon
les pratiques de votre organisation.

Remplacez le `echo` TODO par une vraie séquence :

1. Créer un dossier `serveur-genere/` vide dans le checkout du job.
2. Y copier le catalogue et le Skill **issu de la PR**, avec sa référence.
3. Lancer OpenCode en mode non interactif, avec modèle et permissions maîtrisés.
4. Vérifier qu'un POM a été produit, compiler et tester le serveur.
5. Vérifier séparément le démarrage et les appels MCP ; le harnais ne le fait pas.
6. Exécuter le harnais complété du TP3, juge inclus pour le parcours complet,
   sur cette sortie et conserver les rapports.

Voici un point de départ pour la **commande de génération**, à intégrer dans une
étape `run` après installation d'OpenCode et configuration du fournisseur :

```bash
mkdir -p serveur-genere/.opencode/skills/create-quarkus-mcp-server/references
cp domaine/catalogue-services.json serveur-genere/
cp tp2-skill/skills/create-quarkus-mcp-server/SKILL.md \
  serveur-genere/.opencode/skills/create-quarkus-mcp-server/
cp tp2-skill/references/conventions-quarkus-mcp.md \
  serveur-genere/.opencode/skills/create-quarkus-mcp-server/references/
(
  cd serveur-genere
  opencode run --model "$GENERATION_MODEL" \
    "Charge le Skill create-quarkus-mcp-server et sa référence. Crée ici un serveur MCP Quarkus pour catalogue-services.json : tools find_service et get_owner, resource service://{name}, prompt fiche_service. Compile et teste le projet."
)
test -f serveur-genere/pom.xml
mvn -B -f serveur-genere/pom.xml test
mvn -B -f tp3-eval/pom.xml \
  -Dcas=happy-1 -Dserveur.genere.dir="$GITHUB_WORKSPACE/serveur-genere" test
```

`-Dcas=happy-1` charge le **contrat attendu** du cas `happy-1` : les noms de tools,
la resource et le prompt que la demande impose. Le prompt de génération ci-dessus
est précisément cette demande ; si vous changez le prompt de la CI, changez le cas,
ou le harnais vérifiera un contrat qui n'a pas été demandé. Sans `-Dcas`, seules
les conventions du Skill et le juge s'exécutent.

Cet extrait illustre la génération, le build et l'appel au harnais : il ne constitue
pas un job complet et ne vérifie pas le démarrage ni les appels MCP. Le formateur
prépare aussi ce contrôle séparé, la collecte des traces et la vérification de
présence du juge de la section 4. Les participants en inspectent les preuves.

Ces commandes supposent que le répertoire courant est la racine du checkout,
`GITHUB_WORKSPACE`. `GENERATION_MODEL` contient un identifiant `fournisseur/modèle`.
Vérifiez les options de votre version avec `opencode run --help` ; le `--skill`
figurant dans le texte TODO du squelette ne doit pas être présumé disponible. Le
Skill se découvre dans le projet et se charge via l'outil `skill`.

Sur l'événement `pull_request`, `actions/checkout` récupère par défaut le **commit
de fusion** de la PR sur `main`, pas le dernier commit de la branche. C'est la
version qui serait fusionnée : c'est bien elle qu'il faut évaluer. Relevez le SHA
évalué dans le journal du job pour le compte rendu.

Le modèle peut ne pas charger le Skill malgré le prompt : conservez les traces
de génération et vérifiez ce chargement. Ne remplacez pas cette étape par un serveur
préfabriqué ou mis en cache : le workflow doit évaluer **la modification proposée**.
Une génération réellement issue de la PR **par exécution du workflow** suffit
au parcours essentiel : relevez son prompt et sa provenance sans la confondre
avec un cas du dataset.
**En approfondissement**, pour couvrir tous les scénarios du TP3, étendez le job
à un répertoire de génération distinct par cas ; le harnais ne boucle pas
automatiquement sur le dataset.

Le code de sortie d'OpenCode seul ne suffit pas. Les étapes Maven doivent bloquer
le job en cas d'échec : pas de `|| true`, pas de `continue-on-error: true` pour le
contrôle de conformité, pas de suppression des tests rouges.

## 4. Configurer les secrets et les preuves

**Préparation formateur / parcours autonome :** configurez et testez ces accès avant
séance. **Essentiel participants :** inspectez les noms, les portées et la politique
d'accès sans révéler les valeurs, puis constatez que génération et juge ont été
réellement exécutés sur la PR. Ne passez pas les 1 h 30 à installer un fournisseur.

Définissez secrets et variables dans **Settings / Secrets and variables / Actions**,
jamais dans le YAML. Le workflow les lit avec `secrets.NOM` et `vars.NOM` :

| Nom | Type | Usage |
| --- | --- | --- |
| `GENERATION_MODEL` | Variable | Modèle utilisé par OpenCode |
| Identifiants du fournisseur OpenCode | Secret | Génération ; nom selon le fournisseur |
| `LLM_ENDPOINT`, `LLM_MODEL` | Variables | Juge du TP3 |
| `LLM_API_KEY` | Secret | Juge du TP3, requis pour le parcours complet |

Les variables du juge ne configurent pas automatiquement le fournisseur OpenCode.
GitHub masque les secrets dans les journaux, mais un `echo` volontaire ou un fichier
d'artefact peut les exposer : ne les écrivez nulle part.

**Les secrets ne sont pas transmis aux workflows `pull_request` déclenchés depuis
un fork.** C'est une protection, pas un bug. Ne la contournez pas avec
`pull_request_target` : cet événement s'exécute avec les secrets du dépôt de base
et, combiné à un checkout puis une exécution du code proposé, livre vos clés à
n'importe quel contributeur. **Ne le résolvez pas en exposant vos clés aux PR non
fiables.**

Choisissez une politique explicite :

- workflow d'évaluation complète uniquement pour les PR internes, ou derrière un
  **Environment** GitHub à approbation requise : le job attend qu'un relecteur
  désigné l'autorise avant d'accéder aux secrets de cet environnement ;
- ou démonstration partielle sans juge, explicitement **non validée pour le parcours
  complet**, avec génération réalisée dans un environnement de confiance et
  évaluation complète à reprendre lorsque les accès seront disponibles.

Pour la validation complète de l'atelier, faites échouer le job lorsque les
variables du juge sont absentes : le test JUnit est sinon ignoré lorsque
`LLM_ENDPOINT` est vide. Vérifiez aussi dans le rapport qu'il a été exécuté.
Un workflow vert ne doit pas cacher cette absence ni autoriser une fusion
présentée comme entièrement évaluée.

Conservez les rapports avec `actions/upload-artifact` et la condition
`if: always()`, sur `tp3-eval/target/surefire-reports/`. Conservez aussi le commit
du Skill, le modèle, les cas utilisés et les sorties utiles, après suppression des
secrets. Limitez le jeton du workflow avec `permissions: contents: read`.
Les sources produites par l'agent sont du code à exécuter dans un runner isolé,
avec des droits minimaux : les runners hébergés par GitHub sont éphémères ; un
runner auto-hébergé doit l'être aussi, jamais sur une machine contenant des accès
de production, et jamais sur un dépôt public.

## 5. Contrôler les permissions de Skills

Lisez `opencode-permissions.example.json` : il illustre une intention, mais précise
qu'il doit être adapté au schéma courant. Dans la
[documentation OpenCode](https://opencode.ai/docs/skills/), le réglage passe par
`permission.skill`, au singulier.

Exemple à intégrer à un `opencode.json` d'atelier, en conservant les autres réglages :

```json
{
  "$schema": "https://opencode.ai/config.json",
  "agent": {
    "build": {
      "permission": {
        "skill": {
          "create-quarkus-mcp-server": "allow"
        }
      }
    },
    "plan": {
      "permission": {
        "skill": {
          "create-quarkus-mcp-server": "deny"
        }
      }
    }
  }
}
```

Ici, `build` peut charger le Skill et `plan` ne le peut pas. Pour un agent de revue
personnalisé, utilisez son nom réel et sa configuration. Dans deux nouvelles
sessions, essayez le chargement avec chacun des agents et conservez le résultat.

Un refus d'accès au Skill n'interdit pas à lui seul de lire un fichier, d'exécuter
une commande ou de pousser du code. Complétez par les permissions d'outils et les
droits GitHub ; ne confondez pas ces couches de contrôle :

- **GitHub** contrôle contributions, secrets Actions, revue et fusion.
- **OpenCode** contrôle notamment le chargement du Skill via `permission.skill` ;
  ses identifiants fournisseur permettent les appels au modèle.
- **L'authentification et l'autorisation MCP** contrôlent l'accès à un serveur et
  à ses capacités ; elles ne sont ni configurées ni prouvées par un `allow/deny`
  de Skill. N'extrapolez pas ce test à la sécurité d'accès du serveur MCP.

## 6. Ouvrir une PR et tester le blocage

Créez une branche, modifiez une règle du Skill et ouvrez une pull request vers
`main`. Dans sa description, indiquez l'écart ciblé, les cas d'évaluation concernés
et les résultats avant/après.

Contrôlez que les propriétaires sont demandés en relecture automatiquement et que
le workflow démarre. Vérifiez le commit évalué, le chargement du Skill, la
génération neuve et les rapports du harnais, y compris le juge. Le bouton de fusion
doit indiquer les conditions manquantes : check requis en attente ou en échec,
revue des propriétaires absente. Avec un rôle contributeur, constatez aussi le refus
du push direct sur `main` et conservez la preuve :

```bash
git push atelier-github HEAD:main
# attendu : refus de la branche protégée (message GH006 ou règle de ruleset)
```

**Essentiel :** utilisez une mutation contrôlée déjà éprouvée au TP3 pour démontrer
le blocage. Sur **cette branche d'exercice, jamais sur `main`**, ajoutez
temporairement une étape qui injecte ce défaut dans la sortie **après la vraie
génération et avant l'évaluation**. Gardez tous les tests actifs. Le check doit
devenir rouge et la fusion impossible. Conservez le rapport et l'état de la PR.
Retirez l'injection dans un nouveau commit et relancez la chaîne complète :
génération neuve, build/MCP et évaluation doivent revenir au vert. Cette correction
ne consiste pas à retirer le test qui détectait le défaut.

**Approfondissement / diagnostic :** une assertion JUnit temporaire explicitement
en échec permet aussi d'isoler la mécanique de blocage GitHub. Si vous l'utilisez,
retirez uniquement cette assertion ajoutée pour le diagnostic, jamais les tests
du harnais ; cette preuve seule ne démontre pas la détection d'une non-conformité.
Obtenez l'approbation prévue avant toute fusion ; si l'offre ne la rend pas
bloquante, conservez la preuve de revue manuelle et indiquez cette limite.

## 7. Organiser le test de suppression

**Essentiel :** définissez avec le binôme quand lancer ce test, qui relit les
résultats, quels cas comparer et quels critères imposeraient de conserver la règle.
L'exécution répétée n'est pas requise pour terminer les 1 h 30.

**Approfondissement :** quand le modèle change, créez une PR retirant une instruction
devenue peut-être inutile. Comparez avec et sans cette instruction, sur les mêmes
cas et plusieurs générations, en consignant le nouveau modèle.

Supprimez la règle seulement si les résultats restent acceptables. En cas de
régression, conservez-la et joignez les preuves. Le but n'est pas de réduire le
Skill à tout prix : c'est d'éviter qu'il accumule des instructions devenues sans effet.

## Bilan et dépannage

| Symptôme | Vérification |
| --- | --- |
| Workflow absent de la PR | Emplacement `.github/workflows/`, déclencheur `pull_request` et filtre de branches, YAML valide dans l'onglet Actions |
| Check requis « Expected — Waiting for status » | Le nom du check requis doit être exactement le nom du job ; le workflow doit se déclencher sur la PR |
| `opencode: command not found` | Étape d'installation à version fixée, ou runner auto-hébergé préparé |
| Projet généré introuvable | TODO remplacé, commande réellement exécutée, bon dossier de sortie |
| Secrets vides dans le job | PR ouverte depuis un fork, Environment non approuvé, nom du secret |
| Juge ignoré en CI | Disponibilité des variables et politique de validation choisie |
| CODEOWNERS sans effet bloquant | Case « Require review from Code Owners », équipe avec accès en écriture, syntaxe `@organisation/equipe`, offre du dépôt |
| Fusion possible malgré le rouge | Contournement autorisé pour les administrateurs, ou check non déclaré requis |
| Mauvaise version du Skill évaluée | Checkout de la PR, pas de `main` ni d'un cache |

Le passage essentiel réunit la PR, `main` protégé, la preuve d'un blocage rouge
puis d'un retour vert après correction, une génération réelle du checkout de
la PR, son build/MCP et son évaluation (juge exécuté), la revue selon l'offre et
les permissions allow/deny constatées. Indiquez le périmètre exact et le protocole
de suppression prévu ; ni le dataset complet ni les suppressions répétées ne sont
requis ici. Sans infrastructure préparée, génération réelle ou juge, le résultat
reste partiel et non validé. La boucle complète est :
**générer → formaliser → évaluer → gouverner**.
