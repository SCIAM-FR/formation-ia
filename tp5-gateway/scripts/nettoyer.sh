#!/usr/bin/env bash
set -euo pipefail
TP_DIR="$(cd "$(dirname "$0")/.." && pwd)"; cd "$TP_DIR"
[ -f .env ] && { set -a; . ./.env; set +a; }
kind delete cluster --name "${KIND_CLUSTER:-tp5-gateway}"; rm -f cles.txt; echo "cluster supprimé, cles.txt effacé"
