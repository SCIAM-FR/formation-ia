#!/usr/bin/env bash
# Socle du TP5 : ce que vous n'écrivez pas. Cluster kind, Gateway API, control plane agentgateway, service de comptage,
# Keycloak, Secret du fournisseur et ConfigMaps. Rejouable.
# Usage : ./scripts/socle.sh   (après cp .env.example .env)
set -euo pipefail
TP_DIR="$(cd "$(dirname "$0")/.." && pwd)"; cd "$TP_DIR"
AGW_VERSION="${AGW_VERSION:-v1.5.0}"; GWAPI_VERSION="${GWAPI_VERSION:-1.6.0}"
for outil in kind kubectl helm docker openssl; do command -v "$outil" >/dev/null || { echo "$outil requis" >&2; exit 1; }; done
[ -f .env ] || { echo "cp .env.example .env, puis renseigner FOURNISSEUR_API_KEY et KEYCLOAK_ADMIN_PASSWORD" >&2; exit 1; }
set -a; . ./.env; set +a
case "${FOURNISSEUR_API_KEY:-}" in ""|remplacer*) echo "FOURNISSEUR_API_KEY manquante dans .env" >&2; exit 1 ;; esac
case "${KEYCLOAK_ADMIN_PASSWORD:-}" in ""|remplacer*) echo "KEYCLOAK_ADMIN_PASSWORD manquant dans .env" >&2; exit 1 ;; esac
CLUSTER="${KIND_CLUSTER:-tp5-gateway}"

kind get clusters 2>/dev/null | grep -qx "$CLUSTER" && echo "cluster kind $CLUSTER : déjà présent" || kind create cluster --config kind/cluster.yaml --name "$CLUSTER" --wait 120s
kubectl config use-context "kind-$CLUSTER" >/dev/null
kubectl apply --server-side --force-conflicts -f "https://github.com/kubernetes-sigs/gateway-api/releases/download/v${GWAPI_VERSION}/standard-install.yaml" >/dev/null
helm upgrade -i agentgateway-crds oci://cr.agentgateway.dev/charts/agentgateway-crds --create-namespace --namespace agentgateway-system --version "$AGW_VERSION" >/dev/null
helm upgrade -i agentgateway oci://cr.agentgateway.dev/charts/agentgateway --namespace agentgateway-system --version "$AGW_VERSION" \
  --set agentgatewayModels.enabled=true --wait --timeout 5m >/dev/null
echo "control plane agentgateway $AGW_VERSION installé, GatewayClass : $(kubectl get gatewayclass agentgateway -o jsonpath='{.status.conditions[?(@.type=="Accepted")].status}')"

kubectl create namespace keycloak --dry-run=client -o yaml | kubectl apply -f - >/dev/null
kubectl -n keycloak create secret generic keycloak-admin --from-literal=password="$KEYCLOAK_ADMIN_PASSWORD" --dry-run=client -o yaml | kubectl apply -f - >/dev/null
kubectl -n keycloak create configmap keycloak-realm --from-file=realm-formation-ia.json=fournis/keycloak/realm-formation-ia.json --dry-run=client -o yaml | kubectl apply -f - >/dev/null
kubectl -n agentgateway-system create secret generic fournisseur-secret --from-literal=Authorization="$FOURNISSEUR_API_KEY" --dry-run=client -o yaml | kubectl apply -f - >/dev/null
[ -f costs/catalogue.json ] && kubectl -n agentgateway-system create configmap catalogue-prix --from-file=catalogue.json=costs/catalogue.json --dry-run=client -o yaml | kubectl apply -f - >/dev/null && echo "ConfigMap catalogue-prix publiée depuis costs/catalogue.json" || echo "pas de costs/catalogue.json : la ConfigMap catalogue-prix sera créée en section 5"
kubectl apply -f fournis/10-ratelimit.yaml >/dev/null && kubectl apply -f fournis/20-keycloak.yaml >/dev/null
kubectl -n ratelimit rollout status deploy/ratelimit --timeout=120s
kubectl -n keycloak rollout status deploy/keycloak --timeout=300s
echo "Socle prêt. Secret fournisseur-secret et ConfigMap keycloak-realm publiés. Suite : section 2 du guide (squelettes/30-gateway.yaml)."
