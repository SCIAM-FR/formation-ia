# Mini-exercice M5 — lire une trace de production (~15 min)

Trois incidents fictifs dans une trace d'agent ; pour chacun, les binômes nomment
**le contrôle**, **son emplacement** et **son responsable**. Ce n'est pas un
cinquième TP : pas de code, une grille à remplir. Réservez environ 15 minutes sur
les 45 de M5, après observabilité, guardrails et gateway.

## Déroulé

| Temps | Action |
| --- | --- |
| 2 min | Projeter ou distribuer `trace.jsonl` et la grille vide ci-dessous |
| 8 min | En binôme : repérer les trois incidents, remplir la grille |
| 5 min | Restitution : un binôme par incident, compléments du formateur |

Toutes les valeurs (identifiants, clé, horodatages) sont fictives.

## Grille à remplir

| Incident (lignes de la trace) | Contrôle | Emplacement | Responsable |
| --- | --- | --- | --- |
| A — action non autorisée | | | |
| B — boucle trop longue | | | |
| C — donnée sensible dans les logs | | | |

## Corrigé formateur

| Incident | Contrôle attendu | Où il s'applique réellement | Qui en répond |
| --- | --- | --- | --- |
| **A** — `delete_service` appelé et exécuté par un client qui ne doit voir que `find_service` et `get_owner` | **Autorisation par outil** (liste des tools exposés par client, portée du jeton) | Au **serveur MCP** et au **SI** derrière lui ; le gateway peut filtrer la liste, mais un accès direct le contourne | Propriétaire du serveur MCP, avec la sécurité pour la politique |
| **B** — 42 appels `find_service` identiques en 90 s, aucune réponse finale, coût ×20 | **Guardrail coupe-circuit** (nombre d'appels ou budget par session) + **alerte** d'observabilité sur le coût | Côté **client/agent** ou gateway pour couper ; plateforme d'observabilité pour alerter | Équipe qui exploite l'agent ; seuil fixé avec le métier |
| **C** — clé API en clair dans l'argument d'un appel de tool, trace conservée 90 jours | **Masquage à l'émission** des traces, **rétention** adaptée, **révocation** immédiate de la clé | Dans l'instrumentation de l'agent et du serveur (avant l'export), puis dans la plateforme de logs | Sécurité + propriétaire du serveur ; l'auteur du prompt qui a collé la clé pour la révocation |

Ce que l'exercice fait dire : un contrôle se définit par les trois colonnes.
« On a un gateway » ne répond ni au **où** ni au **qui**. Lien avec les diapos
précédentes : mesurer (éval) n'est pas contraindre (guardrail) ; le gateway est un
pattern possible, pas une garantie ; ni secrets ni données sensibles dans le
contexte partagé (M6).
