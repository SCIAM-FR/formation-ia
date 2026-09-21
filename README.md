# Formation — Du protocole MCP à l'usine logicielle IA

Repo support des travaux pratiques. Fil rouge unique : on **fait produire** un serveur
MCP par un agent, on **distille** ce savoir-faire dans un Skill, on **évalue** le Skill,
puis on le **gouverne**.

## Guides pas à pas

Le site **[Guides des TP](https://sciam-fr.github.io/formation-ia/)** propose
une préparation du poste et des instructions détaillées pour chaque exercice :
commandes, résultats attendus, points de contrôle, livrables et dépannage.
Il sera accessible après activation de GitHub Pages et premier déploiement.

Les guides sont aussi consultables dans le dépôt :
[accueil](docs/index.md), [préparation](docs/preparation.md),
[TP1](docs/tp1.md), [TP2](docs/tp2.md), [TP3](docs/tp3.md), [TP4](docs/tp4.md).

## Fil rouge

| TP  | On fait…                                   | Artefact produit                         |
|-----|--------------------------------------------|------------------------------------------|
| TP1 | Piloter OpenCode → serveur MCP « vanilla » | Un serveur Quarkus (divergent, non conforme) |
| TP2 | Distiller le savoir-faire dans un Skill    | `skills/create-quarkus-mcp-server`       |
| TP3 | Évaluer le Skill                           | Un harnais JUnit (assertions + grille de juge) |
| TP4 | Gouverner le Skill                         | Repo GitLab : MR, protection, permissions |

Le domaine métier commun aux TP est un **catalogue de services interne**
(voir `domaine/`). Tout est auto-contenu : aucune dépendance réseau côté domaine.

## Prérequis

- Lecture de Java / familiarité JVM (on relit et complète, on n'écrit pas tout à la main)
- JDK 21+, Maven
- OpenCode installé et **un modèle LLM câblé** (cf. note formateur — prérequis n°1 du Jour 1)

## Principe directeur (rappel M0)

> Écrire du code qu'on veut supprimer. Le Skill = le *delta* résiduel entre ce que le
> modèle de base sait déjà faire et la conformité maison. Le jour où le socle absorbe
> Quarkus+MCP, le Skill rétrécit — c'est le **test de suppression**.

## Publier et maintenir le site

Le site Jekyll est dans `docs/`. Pour activer sa publication sur ce dépôt :

1. Dans **Settings → Pages → Build and deployment**, choisir **GitHub Actions**
   comme source (droits d'administration requis).
2. Fusionner les fichiers du site et `.github/workflows/pages.yml` sur `main`.
3. Dans **Actions**, vérifier le workflow **Publier les guides des TP**.
   Il peut aussi être lancé manuellement depuis `main` avec **Run workflow**.

Les pull requests modifiant le site déclenchent sa construction, sans déploiement.
Les changements dans `docs/` ou dans le workflow, poussés sur `main`, publient le
site à l'adresse ci-dessus. Le workflow ne déploie que `docs/`, pas les projets
Java ni les fichiers d'atelier. Pour un fork ou un domaine personnalisé, adapter
`url`, `baseurl` et `repository_url` dans `docs/_config.yml`.

Pour prévisualiser localement, avec Ruby 3.3 et Bundler disponibles :

```bash
cd docs
bundle install
bundle exec jekyll serve --host 127.0.0.1
```

Ouvrir `http://127.0.0.1:4000/formation-ia/`. Pour vérifier seulement la génération :
`bundle exec jekyll build --strict_front_matter`.
Les guides sont rédigés en Markdown ; la navigation commune se configure dans
`docs/_config.yml` et l'habillage dans `docs/_layouts/` et `docs/assets/`.
