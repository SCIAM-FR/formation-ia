---
title: TP4 — Gouverner le Skill sur GitLab
description: Faire du Skill un actif revu, évalué et protégé, puis organiser sa réduction lorsque le modèle progresse.
permalink: /tp4/
previous_url: /tp3/
previous_title: TP3 — Évaluer
---

## Objectif et livrables

Obtenez une **merge request relue**, une **CI qui évalue la génération de cette MR**
et des **règles de protection vérifiées**. Définissez aussi qui peut charger le Skill
et comment le faire évoluer.

Entrées : Skill et références du TP2, harnais complété du TP3, accès à un projet
GitLab d'atelier et à un runner. Certaines règles d'approbation dépendent de
l'offre et de la version de GitLab ; vérifiez les possibilités avec le formateur.

## Essentiel et approfondissement

| Parcours | Sections concernées | Preuve de sortie |
| --- | --- | --- |
| **Préparation formateur — avant séance, hors 14 h** | 1, 3 et 4 : projet, runner, environnement OpenCode, modèle, fournisseur du juge et secrets autorisés prévalidés | Pipeline de MR réellement génératrice et évaluatrice dans un contexte de confiance ; infrastructure prête pour les versions des participants |
| **Essentiel — 1 h 30 cible** | 1 à 6 : import des travaux, protections, inspection de la CI/secrets préparés, permissions et MR ; 7 : définir le protocole de suppression | `main` protégé, revue CODEOWNERS selon l'offre, vraie génération depuis la MR avec évaluation, blocage rouge puis retour vert, chargement Skill allow/deny observé |
| **Approfondissement — hors 14 h ou si avance** | 3 et 4 : installation CI depuis zéro / parcours autonome ; 3 : dataset multi-cas ; 6 : diagnostics supplémentaires ; 7 : test de suppression répété | Chaîne autonome éprouvée, résultats par cas, comparaison répétée avec/sans instruction |

Les participants **configurent et prouvent les protections et la revue** ; la
préparation du runner ne fait pas cet exercice à leur place. Si l'infrastructure
n'est pas prête, allongez l'atelier ou annoncez une démonstration **partielle et non
validée**. Le temps cible ne justifie ni génération fictive ni suppression de test.

> Les fichiers livrés dans `tp4-gouvernance/` sont des **exemples à adapter**.
> La CI contient un `echo "TODO atelier…"` : elle ne génère aucun serveur en l'état.
> Les permissions OpenCode sont elles aussi un exemple, pas une configuration
> à recopier aveuglément.

## 1. Préparer le dépôt de gouvernance

**Essentiel :** utilisez le projet GitLab d'atelier prévalidé par le formateur.
Sa création depuis zéro relève de la préparation ou du parcours autonome.
Pour garder les chemins fournis cohérents, conservez cette organisation à sa racine :

```text
.gitlab-ci.yml
CODEOWNERS
domaine/
tp2-skill/
  skills/create-quarkus-mcp-server/SKILL.md
  references/conventions-quarkus-mcp.md
tp3-eval/
  pom.xml
  src/test/...
```

Dans un clone de ce projet GitLab, copiez vos versions **complétées** des supports,
pas les squelettes initiaux. Copiez aussi le `.gitignore` des supports, puis ajoutez
les exclusions nécessaires pour les sorties de génération et les secrets.
Utilisez à la racine la `.gitlab-ci.yml` **adaptée et prévalidée par le formateur**,
sans l'écraser par le template contenant l'`echo`. Adaptez le `CODEOWNERS` fourni.
En préparation ou en parcours autonome, partez des fichiers de `tp4-gouvernance/`
et effectuez les adaptations des sections 3 et 4 avant d'en attendre une validation.
Vérifiez `git status` avant tout commit.

Si vous reprenez le clone de formation plutôt qu'un projet séparé, ajoutez un remote
distinct, sans remplacer `origin` :

```bash
git remote add atelier-gitlab <URL_DU_PROJET_GITLAB>
git remote -v
```

Remplacez `<URL_DU_PROJET_GITLAB>` avant exécution. Faites valider l'import initial
de `main` selon les droits du projet, puis travaillez uniquement sur des branches de
contribution. Ne poussez pas de clés, de configuration personnelle ou de sorties
du modèle contenant des données non autorisées.

## 2. Protéger `main` et rendre la revue effective

Dans les paramètres GitLab, cherchez **Repository / Branch rules** ou
**Protected branches**, selon votre version :

1. Protégez `main` et interdisez les pushes directs pour les contributeurs.
2. Limitez les droits de merge aux rôles convenus.
3. Exigez une pipeline réussie avant fusion.
4. Configurez les approbations requises et la revue par les propriétaires du code,
   si votre offre GitLab le permet.

Le fichier fourni contient :

```text
/tp2-skill/skills/   @platform-team
```

Remplacez `@platform-team` par le groupe GitLab réel disposant de l'accès nécessaire.
Ajoutez la propriété des références et du harnais : changer la convention ou
affaiblir le test peut contourner une protection portant seulement sur le Skill.
Si vous avez déplacé le Skill sous `/skills/`, adaptez aussi le motif.

**Un fichier CODEOWNERS n'impose pas à lui seul une approbation bloquante.**
Vérifiez la règle sur la branche protégée et sur une MR réelle. Si l'offre ne permet
pas l'approbation obligatoire des propriétaires, documentez cette limite et
organisez une revue manuelle ; ne la présentez pas comme une protection automatique.

## 3. Remplacer la génération fictive de la CI

**Préparation formateur / parcours autonome :** les opérations d'installation et
de câblage de cette section se font avant la séance, ou en approfondissement hors
14 h. **Dans l'essentiel**, inspectez la configuration préparée, repérez chacune
des étapes ci-dessous et prouvez leur exécution sur votre MR en section 6.
Le dépôt ne livre pas cette chaîne opérationnelle clé en main.

Ouvrez `.gitlab-ci.yml`. Le job fourni utilise `maven:3.9-eclipse-temurin-21`
et s'exécute sur les événements de merge request. Cette image n'installe pas
OpenCode : prévoyez une image d'atelier avec une version fixée, ou une installation
explicite conforme aux pratiques de votre organisation.

Remplacez le `echo` TODO par une vraie séquence :

1. Créer un dossier `serveur-genere/` vide dans le checkout du job.
2. Y copier le catalogue et le Skill **issu de la MR**, avec sa référence.
3. Lancer OpenCode en mode non interactif, avec modèle et permissions maîtrisés.
4. Vérifier qu'un POM a été produit, compiler et tester le serveur.
5. Vérifier séparément le démarrage et les appels MCP ; le harnais ne le fait pas.
6. Exécuter le harnais complété du TP3, juge inclus pour le parcours complet,
   sur cette sortie et conserver les rapports.

Voici un point de départ pour la **commande de génération**, à intégrer après
installation d'OpenCode et configuration du fournisseur :

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
  -Dserveur.genere.dir="$CI_PROJECT_DIR/serveur-genere" test
```

Cet extrait illustre la génération, le build et l'appel au harnais : il ne constitue
pas un job complet et ne vérifie pas le démarrage ni les appels MCP. Le formateur
prépare aussi ce contrôle séparé, la collecte des traces et la vérification de
présence du juge de la section 4. Les participants en inspectent les preuves.

Ces commandes supposent que le répertoire courant est la racine du checkout GitLab.
`GENERATION_MODEL` contient un identifiant `fournisseur/modèle`. Vérifiez les options
de votre version avec `opencode run --help` ; le `--skill` figurant dans le texte
TODO du squelette ne doit pas être présumé disponible. Le Skill se découvre dans
le projet et se charge via l'outil `skill`.

Le modèle peut ne pas charger le Skill malgré le prompt : conservez les traces
de génération et vérifiez ce chargement. Ne remplacez pas cette étape par un serveur
préfabriqué ou mis en cache : la CI doit évaluer **la modification proposée**.
Une génération réellement issue de la MR **par exécution de la pipeline** suffit
au parcours essentiel : relevez son prompt et sa provenance sans la confondre
avec un cas du dataset.
**En approfondissement**, pour couvrir tous les scénarios du TP3, étendez le job
à un répertoire de génération distinct par cas ; le harnais ne boucle pas
automatiquement sur le dataset.

Le code de sortie d'OpenCode seul ne suffit pas. Les étapes Maven doivent bloquer
le job en cas d'échec : pas de `|| true`, pas de `allow_failure` pour le contrôle
de conformité, pas de suppression des tests rouges.

## 4. Configurer les secrets et les preuves

**Préparation formateur / parcours autonome :** configurez et testez ces accès avant
séance. **Essentiel participants :** inspectez les noms, les portées et la politique
d'accès sans révéler les valeurs, puis constatez que génération et juge ont été
réellement exécutés sur la MR. Ne passez pas les 1 h 30 à installer un fournisseur.

Définissez les variables CI dans GitLab, jamais dans le YAML :

| Variable | Usage |
| --- | --- |
| `GENERATION_MODEL` | Modèle utilisé par OpenCode |
| Identifiants du fournisseur OpenCode | Génération ; nom des variables selon le fournisseur |
| `LLM_ENDPOINT`, `LLM_API_KEY`, `LLM_MODEL` | Juge du TP3, requis pour le parcours complet |

Les variables du juge ne configurent pas automatiquement le fournisseur OpenCode.
Masquez les secrets et restreignez leur disponibilité. Les variables protégées
peuvent ne pas être accessibles à une pipeline de MR, selon les branches, les
réglages et l'origine de la contribution. **Ne le résolvez pas en exposant vos clés
aux MR non fiables.**

Choisissez une politique explicite :

- pipeline d'évaluation complète uniquement dans un contexte de confiance avec
  secrets autorisés ;
- ou démonstration partielle sans juge, explicitement **non validée pour le parcours
  complet**, avec génération réalisée dans un environnement de confiance et
  évaluation complète à reprendre lorsque les accès seront disponibles.

Pour la validation complète de l'atelier, faites échouer le job lorsque les
variables du juge sont absentes : le test JUnit est sinon ignoré lorsque
`LLM_ENDPOINT` est vide. Vérifiez aussi dans le rapport qu'il a été exécuté.
Une pipeline verte ne doit pas cacher cette absence ni autoriser une fusion
présentée comme entièrement évaluée.

Ajoutez des artefacts GitLab avec `when: always` et le rapport JUnit
`tp3-eval/target/surefire-reports/TEST-*.xml`. Conservez aussi le commit du Skill,
le modèle, les cas utilisés et les sorties utiles, après suppression des secrets.
Les sources produites par l'agent sont du code à exécuter dans un runner isolé,
avec des droits minimaux, pas sur une machine contenant des accès de production.

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
droits GitLab ; ne confondez pas ces couches de contrôle :

- **GitLab** contrôle contributions, secrets CI, revue et fusion.
- **OpenCode** contrôle notamment le chargement du Skill via `permission.skill` ;
  ses identifiants fournisseur permettent les appels au modèle.
- **L'authentification et l'autorisation MCP** contrôlent l'accès à un serveur et
  à ses capacités ; elles ne sont ni configurées ni prouvées par un `allow/deny`
  de Skill. N'extrapolez pas ce test à la sécurité d'accès du serveur MCP.

## 6. Ouvrir une MR et tester le blocage

Créez une branche, modifiez une règle du Skill et ouvrez une merge request vers
`main`. Dans sa description, indiquez l'écart ciblé, les cas d'évaluation concernés
et les résultats avant/après.

Contrôlez que le bon propriétaire est sollicité et que la pipeline de MR démarre.
Vérifiez le commit évalué, le chargement du Skill, la génération neuve et les rapports
du harnais, y compris le juge. Avec un rôle contributeur, constatez aussi
l'interdiction du push direct sur `main` (contrôle des droits ou tentative sans
modification de `main`) et conservez la preuve.

**Essentiel :** utilisez une mutation contrôlée déjà éprouvée au TP3 pour démontrer
le blocage. Sur **cette branche d'exercice, jamais sur `main`**, ajoutez
temporairement une étape qui injecte ce défaut dans la sortie **après la vraie
génération et avant l'évaluation**. Gardez tous les tests actifs. La pipeline doit
devenir rouge et la fusion impossible. Conservez le rapport et l'état de la MR.
Retirez l'injection dans un nouveau commit et relancez la chaîne complète :
génération neuve, build/MCP et évaluation doivent revenir au vert. Cette correction
ne consiste pas à retirer le test qui détectait le défaut.

**Approfondissement / diagnostic :** une assertion JUnit temporaire explicitement
en échec permet aussi d'isoler la mécanique de blocage GitLab. Si vous l'utilisez,
retirez uniquement cette assertion ajoutée pour le diagnostic, jamais les tests
du harnais ; cette preuve seule ne démontre pas la détection d'une non-conformité.
Obtenez l'approbation prévue avant toute fusion ; si l'offre ne la rend pas
bloquante, conservez la preuve de revue manuelle et indiquez cette limite.

## 7. Organiser le test de suppression

**Essentiel :** définissez avec le binôme quand lancer ce test, qui relit les
résultats, quels cas comparer et quels critères imposeraient de conserver la règle.
L'exécution répétée n'est pas requise pour terminer les 1 h 30.

**Approfondissement :** quand le modèle change, créez une MR retirant une instruction
devenue peut-être inutile. Comparez avec et sans cette instruction, sur les mêmes
cas et plusieurs générations, en consignant le nouveau modèle.

Supprimez la règle seulement si les résultats restent acceptables. En cas de
régression, conservez-la et joignez les preuves. Le but n'est pas de réduire le
Skill à tout prix : c'est d'éviter qu'il accumule des instructions devenues sans effet.

## Bilan et dépannage

| Symptôme | Vérification |
| --- | --- |
| Job absent | Pipeline de MR, emplacement racine de `.gitlab-ci.yml`, règles `rules` |
| `opencode: command not found` | Installation ou image du runner |
| Projet généré introuvable | TODO remplacé, commande réellement exécutée, bon dossier de sortie |
| Juge ignoré en CI | Disponibilité des variables et politique de validation choisie |
| CODEOWNERS sans effet bloquant | Motif de chemin, groupe valide, offre GitLab, règle d'approbation |
| Mauvaise version du Skill évaluée | Copie depuis le checkout de la MR, pas depuis `main` ou un cache |

Le passage essentiel réunit la MR, `main` protégé, la preuve d'un blocage rouge
puis d'un retour vert après correction, une génération réelle du checkout de
la MR, son build/MCP et son évaluation (juge exécuté), la revue selon l'offre et
les permissions allow/deny constatées. Indiquez le périmètre exact et le protocole
de suppression prévu ; ni le dataset complet ni les suppressions répétées ne sont
requis ici. Sans infrastructure préparée, génération réelle ou juge, le résultat
reste partiel et non validé. La boucle complète est :
**générer → formaliser → évaluer → gouverner**.
