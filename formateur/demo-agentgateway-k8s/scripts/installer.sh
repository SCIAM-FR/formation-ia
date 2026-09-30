#!/usr/bin/env bash
# Installe la démonstration agentgateway sur un cluster kind dédié (poste formateur) :
#   1. cluster kind « formation-ia » (kind/cluster.yaml : ports 4000, 4001 et 8180 publiés sur le poste) ;
#   2. CRD Gateway API 1.6.0, charts Helm agentgateway-crds et agentgateway v1.5.0 (API AgentgatewayModel activée) ;
#   3. Secret de la clé Zen, ConfigMaps (catalogue de prix, realm Keycloak), mot de passe admin Keycloak ;
#   4. manifestes 10 → 60, puis attente de la gateway.
# Usage : ./scripts/installer.sh        (rejouable : chaque étape est idempotente)
# Ensuite : ./scripts/preparer-demo.sh  (clés virtuelles des équipes)
set -euo pipefail
DEMO_DIR="$(cd "$(dirname "$0")/.." && pwd)"; cd "$DEMO_DIR"
AGW_VERSION="${AGW_VERSION:-v1.5.0}"
GWAPI_VERSION="${GWAPI_VERSION:-1.6.0}"
for outil in kind kubectl helm docker openssl; do command -v "$outil" >/dev/null || { echo "$outil requis" >&2; exit 1; }; done
[ -f .env ] || { echo "Fichier .env absent : cp .env.example .env, puis renseigner OPENCODE_API_KEY et KEYCLOAK_ADMIN_PASSWORD." >&2; exit 1; }
set -a; . ./.env; set +a
case "${OPENCODE_API_KEY:-}" in ""|cle-opencode-zen) echo "OPENCODE_API_KEY n'est pas renseignée dans .env" >&2; exit 1 ;; esac
case "${KEYCLOAK_ADMIN_PASSWORD:-}" in ""|remplacer) echo "KEYCLOAK_ADMIN_PASSWORD n'est pas renseigné dans .env" >&2; exit 1 ;; esac
CLUSTER="${KIND_CLUSTER:-formation-ia}"

# 1. Cluster kind
if kind get clusters 2>/dev/null | grep -qx "$CLUSTER"; then
  echo "cluster kind $CLUSTER : déjà présent"
else
  kind create cluster --config kind/cluster.yaml --name "$CLUSTER" --wait 120s
fi
kubectl config use-context "kind-$CLUSTER" >/dev/null
kubectl get nodes

# 2. Gateway API + control plane agentgateway
kubectl apply --server-side --force-conflicts -f "https://github.com/kubernetes-sigs/gateway-api/releases/download/v${GWAPI_VERSION}/standard-install.yaml" >/dev/null
helm upgrade -i agentgateway-crds oci://cr.agentgateway.dev/charts/agentgateway-crds --create-namespace --namespace agentgateway-system --version "$AGW_VERSION" >/dev/null
helm upgrade -i agentgateway oci://cr.agentgateway.dev/charts/agentgateway --namespace agentgateway-system --version "$AGW_VERSION" \
  --set agentgatewayModels.enabled=true --wait --timeout 5m >/dev/null
echo "control plane agentgateway $AGW_VERSION : $(kubectl get gatewayclass agentgateway -o jsonpath='{.status.conditions[?(@.type=="Accepted")].status}' | sed 's/True/accepté/')"

# 3. Secrets et ConfigMaps (idempotents)
kubectl create namespace keycloak --dry-run=client -o yaml | kubectl apply -f - >/dev/null
kubectl -n keycloak create secret generic keycloak-admin --from-literal=password="$KEYCLOAK_ADMIN_PASSWORD" --dry-run=client -o yaml | kubectl apply -f - >/dev/null
kubectl -n keycloak create configmap keycloak-realm --from-file=realm-formation-ia.json=keycloak/realm-formation-ia.json --dry-run=client -o yaml | kubectl apply -f - >/dev/null
kubectl -n agentgateway-system create secret generic zen-secret --from-literal=Authorization="$OPENCODE_API_KEY" --dry-run=client -o yaml | kubectl apply -f - >/dev/null
kubectl -n agentgateway-system create configmap catalogue-prix-zen --from-file=zen.json=costs/zen.json --dry-run=client -o yaml | kubectl apply -f - >/dev/null
echo "secret zen-secret, ConfigMaps catalogue-prix-zen et keycloak-realm : publiés"

# 4. Manifestes, dans l'ordre
for f in manifests/*.yaml; do kubectl apply -f "$f" >/dev/null && echo "appliqué : $f"; done
kubectl -n keycloak rollout status deploy/keycloak --timeout=300s
kubectl -n agentgateway-system rollout status deploy/agentgateway-proxy --timeout=180s
kubectl -n ratelimit rollout status deploy/ratelimit --timeout=120s
echo
kubectl get gateway -n agentgateway-system
kubectl get agentgatewaypolicy -n agentgateway-system -o custom-columns='POLITIQUE:.metadata.name,ACCEPTEE:.status.ancestors[0].conditions[?(@.type=="Accepted")].status,ATTACHEE:.status.ancestors[0].conditions[?(@.type=="Attached")].status'
echo
echo "Gateway LLM : http://localhost:4000  —  MCP : http://localhost:4001/mcp  —  Keycloak : http://localhost:8180"
echo "Suite : ./scripts/preparer-demo.sh"
