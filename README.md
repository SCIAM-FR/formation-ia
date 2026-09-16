# Formation — Du protocole MCP à l'usine logicielle IA

Repo support des travaux pratiques. Fil rouge unique : on **fait produire** un serveur
MCP par un agent, on **distille** ce savoir-faire dans un Skill, on **évalue** le Skill,
puis on le **gouverne**.

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
