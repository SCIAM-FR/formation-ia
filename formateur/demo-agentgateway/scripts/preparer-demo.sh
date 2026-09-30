#!/usr/bin/env bash
# Prépare la gateway agentgateway de DÉMONSTRATION (poste formateur) :
#   1. génère les clés virtuelles des équipes dans .env (celles qui sont vides, ou toutes avec --regenerer) ;
#   2. recrée le conteneur agentgateway pour qu'il relise .env, et attend qu'il réponde ;
#   3. écrit les clés dans cles-demo.txt (même format que demo-litellm, ignoré par git) ;
#   4. vérifie chaque clé sur /v1/models ;
#   5. amorce la clé demo-budget-epuise par un appel d'un token et confirme le 429 budget_exceeded.
#
# Usage : ./scripts/preparer-demo.sh [--regenerer]
# Rejouable : sans --regenerer, les clés existantes sont conservées. Avec --regenerer, la valeur des clés change mais
# la dépense déjà comptée reste attachée au NOM de la clé (metadata.name) dans data/agentgateway.db : pour réarmer
# demo-budget-epuise, arrêter la gateway et supprimer data/agentgateway.db* (le journal Analytics repart de zéro aussi).
set -euo pipefail

DEMO_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$DEMO_DIR"
ADMIN="http://127.0.0.1:15000"
GATEWAY="http://localhost:4000"
REGEN=0; [ "${1:-}" = "--regenerer" ] && REGEN=1

[ -f .env ] || { echo "Fichier .env absent : cp .env.example .env, puis renseigner OPENCODE_API_KEY et les secrets." >&2; exit 1; }
[ -f config.yaml ] || { echo "Fichier config.yaml absent : cp config.example.yaml config.yaml, puis adapter." >&2; exit 1; }
command -v openssl >/dev/null || { echo "openssl requis pour générer les clés" >&2; exit 1; }

# 1. Clés virtuelles : une variable par équipe dans .env, référencée par config.yaml ($CLE_…)
for var in CLE_PLATFORM CLE_PRODUIT_A CLE_PRODUIT_B CLE_CI CLE_DEMO; do
  current="$(grep -E "^${var}=" .env | head -1 | cut -d= -f2- || true)"
  if [ -z "$current" ] || [ "$REGEN" = 1 ]; then
    new="sk-agw-$(openssl rand -hex 24)"
    if grep -qE "^${var}=" .env; then
      sed -i.bak "s|^${var}=.*|${var}=${new}|" .env && rm -f .env.bak
    else
      echo "${var}=${new}" >> .env
    fi
    echo "$var : clé générée"
  else
    echo "$var : conservée"
  fi
done
set -a; . ./.env; set +a
for var in OPENCODE_API_KEY KEYCLOAK_CLIENT_SECRET OIDC_COOKIE_SECRET; do
  case "${!var:-}" in ""|remplacer*|cle-opencode-zen) echo "$var n'est pas renseigné dans .env" >&2; exit 1 ;; esac
done

# 2. Recréer le conteneur (il lit les clés dans son environnement) et attendre l'API d'administration
mkdir -p data
docker compose up -d --force-recreate agentgateway >/dev/null
for i in $(seq 1 30); do
  curl -fsS "$ADMIN/api/runtime" >/dev/null 2>&1 && break
  [ "$i" = 30 ] && { echo "agentgateway ne répond pas sur $ADMIN : docker compose logs agentgateway" >&2; exit 1; }
  sleep 2
done
echo "gateway prête : $(curl -fsS "$ADMIN/api/runtime" | python3 -c 'import sys,json;d=json.load(sys.stdin);print("stockage", d.get("ui",{}).get("configStoreMode"))' 2>/dev/null || echo ok)"

# 3. Clés dans cles-demo.txt (à garder hors du dépôt, à révoquer après la session en les régénérant)
OUT="$DEMO_DIR/cles-demo.txt"
printf 'platform-team  %s\nequipe-produit-a  %s\nequipe-produit-b  %s\nci-usine  %s\ndemo-budget-epuise  %s\n' \
  "$CLE_PLATFORM" "$CLE_PRODUIT_A" "$CLE_PRODUIT_B" "$CLE_CI" "$CLE_DEMO" > "$OUT"

# 4. Chaque clé doit être acceptée et ne voir que ses modèles autorisés
verif_cle() { # verif_cle <nom> <clé>
  local code modeles
  code="$(curl -sS -o /tmp/agw-models.json -w '%{http_code}' "$GATEWAY/v1/models" -H "Authorization: Bearer $2")"
  modeles="$(python3 -c 'import sys,json;print(",".join(m["id"] for m in json.load(sys.stdin).get("data",[])))' < /tmp/agw-models.json 2>/dev/null || true)"
  echo "  $1 : HTTP $code — modèles visibles : ${modeles:-aucun}"
  [ "$code" = 200 ]
}
echo "vérification des clés (GET /v1/models) :"
verif_cle platform-team "$CLE_PLATFORM" || exit 1
verif_cle equipe-produit-a "$CLE_PRODUIT_A" || exit 1
verif_cle equipe-produit-b "$CLE_PRODUIT_B" || exit 1
verif_cle ci-usine "$CLE_CI" || exit 1
verif_cle demo-budget-epuise "$CLE_DEMO" || exit 1
code="$(curl -sS -o /dev/null -w '%{http_code}' "$GATEWAY/v1/models" -H "Authorization: Bearer sk-agw-invalide")"
echo "  clé inconnue : HTTP $code (attendu 401)"

# 5. Amorcer la clé au budget dérisoire : le budget est débité APRÈS la réponse, une clé neuve passe donc son premier appel
appel_demo() {
  curl -sS -o /dev/null -w '%{http_code}' "$GATEWAY/v1/chat/completions" -H "Authorization: Bearer $CLE_DEMO" \
    -H 'Content-Type: application/json' \
    -d '{"model":"modele-juge","max_tokens":1,"messages":[{"role":"user","content":"ok"}]}'
}
etat_budget() {
  curl -fsS "$ADMIN/api/budgets/status?apiKeyName=demo-budget-epuise" \
    | python3 -c 'import sys,json;b=json.load(sys.stdin).get("budgets",[]);print(b[0]["usage"]["exceeded"], b[0]["usage"]["used"]) if b else print("none")' 2>/dev/null || echo "none"
}
premier="$(appel_demo)"
echo "clé demo-budget-epuise : premier appel HTTP $premier (200 attendu si le budget est encore vierge, 429 s'il l'est déjà)"
for i in $(seq 1 10); do
  etat="$(etat_budget)"
  case "$etat" in True*) break ;; esac
  sleep 2
done
echo "  budget « minuscule » : dépassé=${etat%% *} dépensé=${etat#* } \$"
second="$(appel_demo)"
case "$second" in
  429) echo "  second appel : HTTP 429 — la gateway refuse désormais cette clé (budget_exceeded)" ;;
  *)   echo "  attention : second appel HTTP $second ; si la dépense est 0, le catalogue ne chiffre pas ce modèle (README, « Vérifier que la dépense est chiffrée »)" >&2 ;;
esac

echo "Gateway : $GATEWAY — admin : $ADMIN/ui — clés écrites dans $OUT"
