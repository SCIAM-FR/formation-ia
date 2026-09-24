# Références formateur

Matériel **réservé au formateur** : démonstrations, corrigés et traces fictives
utilisés pendant les modules M4, M5 et M7 et au débrief des TP. Rien ici ne doit
être copié dans les squelettes participants (`tp1-…`, `tp2-…`, `tp3-…`, `tp4-…`),
qui restent volontairement incomplets.

| Dossier | Usage | Moment |
| --- | --- | --- |
| [`demo-scorer-trompeur/`](demo-scorer-trompeur/) | Un projet « piège » sur lequel un scorer naïf passe au vert alors que le tool n'existe pas, et l'assertion robuste qui le détecte | M4, avant la section 3 du TP3 |
| [`corrige-assertions/`](corrige-assertions/) | Un corrigé possible des quatre stubs de `ConformiteDeterministeTest`, avec ses limites | Débrief et dépannage du TP3 |
| [`m5-trace-fictive/`](m5-trace-fictive/) | Une trace JSONL fictive avec trois incidents et la grille de correction du mini-exercice | M5, ~15 min |
| [`demo-litellm/`](demo-litellm/) | Une gateway de modèles LiteLLM sur le poste formateur : compose, configuration, script de peuplement et déroulé minuté ; le fil rouge (juge TP3, OpenCode, serveur MCP) passe à travers | M5, ~20 min |

Le canevas de la fiche de sortie (M7) est publié avec les guides :
[`docs/fiche-de-sortie.md`](../docs/fiche-de-sortie.md).

## Ce que ce dossier ne contient pas

- **Des serveurs générés conformes ou non conformes.** Ils dépendent du modèle,
  de la version d'OpenCode et de l'extension MCP retenus pour la session : générez
  et archivez les vôtres lors de la répétition chronométrée (voir
  [`docs/preparation.md`](../docs/preparation.md)), en dehors du dépôt.
- **Une CI GitLab prête à l'emploi.** Le YAML du TP4 reste un squelette à câbler
  dans l'environnement d'atelier.
- **Une gateway hébergée chez le client.** `demo-litellm/` tourne sur le poste du
  formateur pour une démonstration ; l'hébergement, les secrets et le SSO en
  production relèvent du client — c'est une ligne de la fiche de sortie M7.
