#!/usr/bin/env bash
# Génère une clé virtuelle par équipe dans cles.txt (ignoré par git) et AFFICHE la ConfigMap d'empreintes à appliquer.
# Lisez-la avant de l'appliquer : c'est l'objet que vous devrez expliquer (section 3).
# Usage : ./scripts/generer-cles.sh > cles-equipes.yaml && kubectl apply -f cles-equipes.yaml
set -euo pipefail
TP_DIR="$(cd "$(dirname "$0")/.." && pwd)"; cd "$TP_DIR"
OUT="$TP_DIR/cles.txt"; NS=agentgateway-system
NOMS="platform-team equipe-produit-a equipe-produit-b demo-budget-epuise"
groupe() { case "$1" in demo-budget-epuise) echo demo ;; *) echo "$1" ;; esac; }
cle_de() { [ -f "$OUT" ] && awk -v n="$1" '$1==n{print $2}' "$OUT" || true; }
TMP="$(mktemp)"; trap 'rm -f "$TMP"' EXIT
for nom in $NOMS; do v="$(cle_de "$nom")"; [ -n "$v" ] || v="sk-tp5-$(openssl rand -hex 24)"; printf '%s  %s\n' "$nom" "$v" >> "$TMP"; done
cp "$TMP" "$OUT"
empreinte() { printf '%s' "$1" | openssl dgst -sha256 -hex | awk '{print "sha256:"$NF}'; }
echo "apiVersion: v1"; echo "kind: ConfigMap"; echo "metadata:"; echo "  name: cles-equipes"; echo "  namespace: $NS"
echo "  labels: { tp5/cles-virtuelles: \"true\" }"; echo "data:"
for nom in $NOMS; do
  printf '  %s: |\n    {"keyHash": "%s", "metadata": {"user_id": "%s", "group": "%s"}}\n' "$nom" "$(empreinte "$(cle_de "$nom")")" "$nom" "$(groupe "$nom")"
done
echo "# clés en clair écrites dans $OUT (hors dépôt)" >&2
