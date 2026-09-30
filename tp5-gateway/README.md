# TP5 — Bâtir une gateway de modèles et de serveurs MCP avec agentgateway

**[Guide détaillé sur GitHub Pages](https://sciam-fr.github.io/formation-ia/tp5/)**
· [Source du guide](../docs/tp5.md)

**Objectif** : construire vous-même, sur un cluster kind local, la gateway que le formateur
a montrée en M5 : alias de modèles, clés virtuelles par équipe, modèle réservé, coût par
appel, débit et budget, puis votre serveur MCP du TP2 derrière la même gateway, protégé par
un jeton Keycloak. Tout est un objet Kubernetes : ce que vous écrivez ici se relit en merge
request comme le Skill du TP4.

Ce TP est **optionnel et long** (3 h en essentiel). Il se joue hors des 14 h, en journée
complémentaire, ou à la place du TP4 pour un groupe qui n'a pas d'accès GitLab/GitHub.

## Deux parcours

| Parcours | Sections du guide | Preuve de sortie |
| --- | --- | --- |
| **Essentiel — 3 h cible** | 1 à 8 | Trois alias servis, clés par équipe avec 401 / 403 / 429 prouvés, coût chiffré au journal, fil rouge (juge, OpenCode) passé à travers, serveur MCP du TP2 appelé avec un jeton Keycloak et outils filtrés par groupe |
| **Approfondissement** | 9 | Manifestes mis sous revue dans le dépôt du TP4, modèle virtuel de repli, tableau de bord Analytics |

## Prérequis

Docker Desktop, `kind`, `kubectl`, `helm`, `openssl`, `curl`, `python3`, et une clé du
fournisseur de modèles retenu pour la formation (API compatible Chat Completions). Votre
serveur MCP du TP2 (`mvn quarkus:dev`) pour la section 8. Environ 3 Go de RAM libres pour kind.

## Ce que le dossier fournit et ce que vous écrivez

| Fourni (`fournis/`, `scripts/`) | À écrire (`squelettes/`) |
| --- | --- |
| Cluster kind avec ports publiés, Gateway API, control plane agentgateway (`scripts/socle.sh`) | La `Gateway` et ses paramètres (`30-gateway.yaml`) |
| Service de comptage `ratelimit` + Redis, sans seuils utiles | Les alias `AgentgatewayModel` et la règle du modèle réservé (`40-modeles.yaml`) |
| Keycloak avec le realm `formation-ia` (formateur / alice / bob, groupes = équipes) | La politique de l'écouteur LLM : clés, débit, budget, étiquettes (`50-politiques-llm.yaml`) |
| Générateur de clés et d'empreintes (`scripts/generer-cles.sh`) | Les seuils du service de comptage et le catalogue de prix |
| Extrait OpenCode | Le backend, la route et les deux politiques du serveur MCP (`60-mcp.yaml`) |

Chaque squelette porte des `TODO` numérotés par section du guide. Ne copiez pas le corrigé :
il est dans `formateur/demo-agentgateway-k8s/`, pour le formateur et pour le débrief.

## Note formateur

Le corrigé est la démonstration M5 en mode Kubernetes, exécutée de bout en bout le
29 septembre 2026. Les pièges connus sont dans son README (streaming sans usage, seuils par
minute, JWKS à attendre). Prévoir les images en cache local si le réseau de la salle est lent.
