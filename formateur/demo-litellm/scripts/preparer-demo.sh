#!/usr/bin/env bash
# Peuple la gateway LiteLLM de DÉMONSTRATION (poste formateur) : quatre équipes,
# une clé par équipe, une clé au budget minuscule pour montrer un refus.
#
# Usage : ./scripts/preparer-demo.sh [URL de la gateway]
#   - l'URL vaut http://localhost:4000 par défaut ;
#   - LITELLM_MASTER_KEY est lue dans l'environnement, ou dans le fichier .env du dossier de la démo.
#
# Rejouable : une équipe déjà présente est conservée telle quelle ; une clé dont
# l'alias existe déjà est SUPPRIMÉE puis régénérée (LiteLLM ne restitue jamais la
# valeur d'une clé après sa création). Les clés sont écrites dans cles-demo.txt,
# à côté de .env (ignoré par git), seulement si tout s'est bien passé.
set -euo pipefail
# NB : sous bash 3 (macOS), set -e ne s'applique pas dans les $(…) : chaque appel est donc vérifié explicitement.

DEMO_DIR="$(cd "$(dirname "$0")/.." && pwd)"
GATEWAY="${1:-http://localhost:4000}"

if [ -z "${LITELLM_MASTER_KEY:-}" ] && [ -f "$DEMO_DIR/.env" ]; then
  set -a; . "$DEMO_DIR/.env"; set +a
fi
if [ -z "${LITELLM_MASTER_KEY:-}" ]; then
  echo "Clé maître introuvable : définissez LITELLM_MASTER_KEY ou renseignez $DEMO_DIR/.env" >&2
  exit 1
fi
if ! curl -fsS "$GATEWAY/health/liveliness" >/dev/null 2>&1; then
  echo "La gateway ne répond pas sur $GATEWAY (docker compose up -d ?)" >&2
  exit 1
fi

OUT="$DEMO_DIR/cles-demo.txt"
TMP="$(mktemp)"
trap 'rm -f "$TMP"' EXIT

# api <méthode> <chemin> [json] : affiche la réponse ; échoue avec le message de LiteLLM si le code n'est pas 2xx.
api() {
  local method="$1" path="$2" body="${3:-}" out code
  out=$(curl -sS -X "$method" "$GATEWAY$path" -w '\n%{http_code}' \
        -H "Authorization: Bearer $LITELLM_MASTER_KEY" -H 'Content-Type: application/json' \
        ${body:+--data-raw "$body"})
  code="${out##*$'\n'}"
  out="${out%$'\n'*}"
  if [ "${code:0:1}" != "2" ]; then
    echo "Erreur $code sur $method $path : $out" >&2
    return 1
  fi
  printf '%s' "$out"
}
champ() { python3 -c "import sys,json;print(json.load(sys.stdin)[\"$1\"])"; }

team_existe() { api GET "/team/info?team_id=$1" >/dev/null 2>&1; }

# Supprime la clé portant cet alias, si elle existe (obligatoire avant de la régénérer).
supprimer_cle_par_alias() {
  local alias="$1" tokens
  tokens=$(api GET "/key/list?return_full_object=true&size=100" \
    | python3 -c "import sys,json;print(' '.join(k['token'] for k in json.load(sys.stdin).get('keys',[]) if k.get('key_alias')=='$alias'))") || return 1
  for t in $tokens; do
    api POST /key/delete "{\"keys\":[\"$t\"]}" >/dev/null || return 1
    echo "  clé « $alias » existante supprimée, elle sera régénérée" >&2
  done
}

creer_cle() { # creer_cle <alias> <json des autres champs>
  local alias="$1" extra="$2"
  supprimer_cle_par_alias "$alias" || return 1
  api POST /key/generate "{\"key_alias\":\"$alias\",$extra}" | champ key || return 1
}

# nom | modèles ou groupe d'accès | budget USD | requêtes par minute
while IFS='|' read -r team models budget rpm; do
  if team_existe "$team"; then
    echo "équipe $team : déjà présente, conservée"
  else
    # team_id explicite = nom du groupe Keycloak, pour que la connexion SSO rattache l'utilisateur à son équipe
    api POST /team/new "{\"team_id\":\"$team\",\"team_alias\":\"$team\",\"models\":$models,\"max_budget\":$budget,\"budget_duration\":\"30d\",\"rpm_limit\":$rpm}" >/dev/null || exit 1
    echo "équipe $team : créée (budget $budget \$, $rpm req/min, modèles $models)"
  fi
  key=$(creer_cle "$team-cle" "\"team_id\":\"$team\",\"models\":[\"all-team-models\"],\"metadata\":{\"tags\":[\"$team\",\"formation-ia\"]}") || exit 1
  echo "$team  $key" >> "$TMP"
done <<'TEAMS'
equipe-produit-a|["atelier"]|5|30
equipe-produit-b|["atelier"]|5|30
platform-team|["atelier","direction"]|20|60
ci-usine|["modele-generation","modele-juge"]|10|20
TEAMS

# Clé au budget minuscule : montre un refus explicite sans attendre. Régénérée à chaque passage,
# donc le budget est de nouveau intact pour la démo.
demo_key=$(creer_cle "demo-budget-epuise" "\"models\":[\"modele-juge\"],\"max_budget\":0.000001") || exit 1
echo "demo-budget-epuise  $demo_key" >> "$TMP"
# LiteLLM vérifie le budget sur la dépense DÉJÀ enregistrée : une clé neuve passe toujours son premier appel.
# On « amorce » donc la clé par un appel minuscule (quelques tokens sur le modèle le moins cher) pour que
# sa dépense (≈ 3 µ$ pour 15 tokens) dépasse le budget (1 µ$) avant la démo. Délai le temps que la dépense soit écrite.
if curl -sS -o /dev/null "$GATEWAY/v1/chat/completions" -H "Authorization: Bearer $demo_key" \
     -H 'Content-Type: application/json' \
     -d '{"model":"modele-juge","max_tokens":1,"messages":[{"role":"user","content":"ok"}]}'; then
  sleep 12   # LiteLLM écrit la dépense en base par lots
  code=$(curl -sS -o /dev/null -w '%{http_code}' "$GATEWAY/v1/chat/completions" -H "Authorization: Bearer $demo_key" \
     -H 'Content-Type: application/json' \
     -d '{"model":"modele-juge","max_tokens":1,"messages":[{"role":"user","content":"ok"}]}')
  case "$code" in
    4*) echo "clé demo-budget-epuise amorcée : la gateway répond désormais $code (budget dépassé)" >&2 ;;
    *)  echo "attention : la clé demo-budget-epuise répond encore $code ; relancer le script ou attendre l'écriture de la dépense" >&2 ;;
  esac
else
  echo "attention : impossible d'amorcer la clé demo-budget-epuise (modele-juge injoignable ?)" >&2
fi

cp "$TMP" "$OUT"
echo "Gateway : $GATEWAY — clés écrites dans $OUT (à garder hors du dépôt, à révoquer après la session)."
