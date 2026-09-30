#!/usr/bin/env bash
# Prépare la gateway agentgateway sur Kubernetes (poste formateur) :
#   1. génère les clés virtuelles des équipes (une par équipe) dans cles-demo.txt, ignoré par git ;
#   2. publie leurs EMPREINTES SHA-256 et leurs métadonnées (user_id, group) dans la ConfigMap « cles-equipes » ;
#   3. vérifie chaque clé sur /v1/models et l'amorçage de la clé demo-budget-epuise (429 au second appel).
# Usage : ./scripts/preparer-demo.sh [--regenerer]
# Rejouable : sans --regenerer, les clés de cles-demo.txt sont conservées. Le budget de demo-budget-epuise est un
# compteur journalier dans Redis : pour le réarmer, redémarrer le déploiement redis (namespace ratelimit).
set -euo pipefail
DEMO_DIR="$(cd "$(dirname "$0")/.." && pwd)"; cd "$DEMO_DIR"
GATEWAY="${GATEWAY:-http://localhost:4000}"
NS=agentgateway-system
OUT="$DEMO_DIR/cles-demo.txt"
REGEN=0; [ "${1:-}" = "--regenerer" ] && REGEN=1
command -v openssl >/dev/null || { echo "openssl requis" >&2; exit 1; }
kubectl get gateway agentgateway-proxy -n "$NS" >/dev/null 2>&1 || { echo "Gateway absente : lancer scripts/installer.sh d'abord" >&2; exit 1; }

# 1. Clés : une par équipe ; le groupe (= équipe) sert au débit et au tableau de bord. Bash 3 (macOS) : pas de tableaux associatifs.
NOMS="platform-team equipe-produit-a equipe-produit-b ci-usine demo-budget-epuise"
groupe() { case "$1" in demo-budget-epuise) echo demo ;; *) echo "$1" ;; esac; }
cle_de() { [ -f "$OUT" ] && awk -v n="$1" '$1==n{print $2}' "$OUT" || true; }
TMP="$(mktemp)"; trap 'rm -f "$TMP"' EXIT
for nom in $NOMS; do
  valeur="$(cle_de "$nom")"
  if [ -z "$valeur" ] || [ "$REGEN" = 1 ]; then valeur="sk-agw-$(openssl rand -hex 24)"; echo "$nom : clé générée"; else echo "$nom : conservée"; fi
  printf '%s  %s\n' "$nom" "$valeur" >> "$TMP"
done
cp "$TMP" "$OUT"

# 2. ConfigMap des empreintes : la clé en clair ne vit que dans cles-demo.txt (et chez l'équipe qui la reçoit)
empreinte() { printf '%s' "$1" | openssl dgst -sha256 -hex | awk '{print "sha256:"$NF}'; }
{
  echo "apiVersion: v1"; echo "kind: ConfigMap"
  echo "metadata:"; echo "  name: cles-equipes"; echo "  namespace: $NS"
  echo "  labels: { formation-ia/cles-virtuelles: \"true\" }"
  echo "data:"
  for nom in $NOMS; do
    printf '  %s: |\n    {"keyHash": "%s", "metadata": {"user_id": "%s", "group": "%s"}}\n' "$nom" "$(empreinte "$(cle_de "$nom")")" "$nom" "$(groupe "$nom")"
  done
} | kubectl apply -f - >/dev/null
echo "ConfigMap cles-equipes publiée (empreintes SHA-256, métadonnées user_id / group)"

# 3. Vérifications : la gateway doit répondre sur le poste (NodePort 30080 -> localhost:4000 via kind)
for i in $(seq 1 30); do
  code="$(curl -sS -o /dev/null -w '%{http_code}' "$GATEWAY/v1/models" -H "Authorization: Bearer $(cle_de platform-team)" 2>/dev/null || true)"
  [ "$code" = 200 ] && break
  [ "$i" = 30 ] && { echo "la gateway ne répond pas 200 sur $GATEWAY/v1/models (dernier code : ${code:-aucun}) ; kubectl logs deploy/agentgateway-proxy -n $NS" >&2; exit 1; }
  sleep 2
done
echo "vérification des clés (GET /v1/models) :"
for nom in $NOMS; do
  code="$(curl -sS -o /tmp/agw-models.json -w '%{http_code}' "$GATEWAY/v1/models" -H "Authorization: Bearer $(cle_de "$nom")")"
  modeles="$(python3 -c 'import sys,json;print(",".join(m["id"] for m in json.load(sys.stdin).get("data",[])))' < /tmp/agw-models.json 2>/dev/null || true)"
  echo "  $nom : HTTP $code — modèles visibles : ${modeles:-aucun}"
done
echo "  clé inconnue : HTTP $(curl -sS -o /dev/null -w '%{http_code}' "$GATEWAY/v1/models" -H 'Authorization: Bearer sk-agw-invalide') (attendu 401)"

appel_demo() {
  curl -sS -o /dev/null -w '%{http_code}' "$GATEWAY/v1/chat/completions" -H "Authorization: Bearer $(cle_de demo-budget-epuise)" \
    -H 'Content-Type: application/json' \
    -d '{"model":"modele-juge","max_tokens":5,"messages":[{"role":"user","content":"Réponds : ok"}]}'
}
premier="$(appel_demo)"; sleep 1; second="$(appel_demo)"
echo "clé demo-budget-epuise : premier appel HTTP $premier, second HTTP $second (attendu 200 puis 429 : le budget est débité après la réponse)"
echo "Gateway : $GATEWAY — clés écrites dans $OUT (à garder hors du dépôt ; --regenerer les révoque)."
