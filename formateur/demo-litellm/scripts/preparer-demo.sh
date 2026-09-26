#!/usr/bin/env bash
# Peuple la gateway LiteLLM de DÉMONSTRATION (poste formateur) : quatre équipes,
# une clé par équipe, une clé au budget minuscule pour montrer un refus.
#
# Usage : LITELLM_MASTER_KEY=sk-... ./preparer-demo.sh http://localhost:4000
# Les clés générées sont écrites dans cles-demo.txt (ignoré par git).
set -euo pipefail

GATEWAY="${1:?URL de la gateway}"
: "${LITELLM_MASTER_KEY:?LITELLM_MASTER_KEY manquante}"
OUT="cles-demo.txt"

api() { # api <chemin> <json>
  curl -fsS "$GATEWAY$1" -H "Authorization: Bearer $LITELLM_MASTER_KEY" \
       -H 'Content-Type: application/json' --data-raw "$2"
}
champ() { python3 -c "import sys,json;print(json.load(sys.stdin)[\"$1\"])"; }

: > "$OUT"
# nom | modèles ou groupe | budget USD | rpm
while IFS='|' read -r team models budget rpm; do
  # team_id explicite = nom du groupe Keycloak, pour que la connexion SSO rattache l'utilisateur à son équipe
  team_id=$(api /team/new "{\"team_id\":\"$team\",\"team_alias\":\"$team\",\"models\":$models,
      \"max_budget\":$budget,\"budget_duration\":\"30d\",\"rpm_limit\":$rpm}" | champ team_id)
  key=$(api /key/generate "{\"team_id\":\"$team_id\",\"key_alias\":\"$team-cle\",
      \"models\":[\"all-team-models\"],\"metadata\":{\"tags\":[\"$team\",\"formation-ia\"]}}" | champ key)
  echo "$team  $key" >> "$OUT"
done <<'TEAMS'
equipe-produit-a|["atelier"]|5|30
equipe-produit-b|["atelier"]|5|30
platform-team|["atelier","direction"]|20|60
ci-usine|["modele-generation","modele-juge"]|10|20
TEAMS

# Clé au budget minuscule : montre un refus explicite sans attendre.
demo_key=$(api /key/generate '{"key_alias":"demo-budget-epuise","models":["modele-generation"],"max_budget":0.01}' | champ key)
echo "demo-budget-epuise  $demo_key" >> "$OUT"

echo "Clés écrites dans $OUT — à garder hors du dépôt et à révoquer après la session."
