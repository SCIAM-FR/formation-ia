#!/usr/bin/env bash
# Supprime le cluster kind de la démonstration (et tout ce qu'il contient). Les clés distribuées deviennent inertes.
set -euo pipefail
DEMO_DIR="$(cd "$(dirname "$0")/.." && pwd)"; cd "$DEMO_DIR"
[ -f .env ] && { set -a; . ./.env; set +a; }
CLUSTER="${KIND_CLUSTER:-formation-ia}"
kind delete cluster --name "$CLUSTER"
rm -f cles-demo.txt
echo "cluster $CLUSTER supprimé, cles-demo.txt effacé"
