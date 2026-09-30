# Démonstration M5 — la gateway de modèles en mode Kubernetes, avec agentgateway sur kind (~20 min)

Troisième variante de la démonstration M5, après [`demo-litellm/`](../demo-litellm/README.md)
(LiteLLM dans Docker) et [`demo-agentgateway/`](../demo-agentgateway/README.md) (agentgateway
en binaire, un fichier YAML). Ici, **tout est un objet Kubernetes** : la gateway est une
`Gateway` de la Gateway API, chaque alias de modèle un `AgentgatewayModel`, chaque règle une
`AgentgatewayPolicy`, les clés des équipes une `ConfigMap` d'empreintes. Le control plane
agentgateway traduit ces objets en configuration de proxy. Le tout tourne dans un **cluster
kind dédié** sur le poste du formateur, sans rien héberger chez le client, avec
**OpenCode Zen** comme fournisseur de modèles, et c'est le **fil rouge** qui passe à travers :
le juge du TP3, une génération OpenCode, le serveur MCP généré au TP2.

L'objectif pédagogique reste celui de la diapo M0 « Les responsabilités qui restent à
l'organisation » : identité des appels, coût et routage, règles d'accès, audit. Ce que cette
variante ajoute : la politique de la gateway est **du code Kubernetes**, revue et déployée
comme le reste de la plateforme. « Le budget de l'équipe A, c'est la ligne 20 de
`50-politiques-llm.yaml` et la ConfigMap du service de comptage ; la MR qui l'a changé est
dans GitLab, comme le Skill du TP4. » C'est la même boucle que M6, appliquée à la gateway.

> **Une seule variante par séance.** Choisissez LiteLLM, agentgateway en binaire ou
> agentgateway sur kind, et répétez celle-là. Les diapos M5 du support décrivent LiteLLM ;
> pour cette variante, adaptez les diapos 60 à 63 (vocabulaire : « objet Kubernetes »,
> « kubectl apply », « GitOps » au lieu d'« interface d'administration »).

> **État de cette variante (29 septembre 2026).** Exécutée de bout en bout sur ce poste
> (kind v0.31, Kubernetes 1.35, agentgateway v1.5.0, Gateway API 1.6.0) : clés et
> visibilité des modèles par équipe, appels réels à Zen (GLM et Claude) avec coût au
> journal, refus 401 / 403 / 429 (modèle réservé, budget de tokens, 30 requêtes par minute),
> OpenCode en mode non interactif (réponse en streaming : tokens et coût non remontés par Zen, voir Dépannage), serveur MCP du fil rouge appelé à travers la gateway avec
> un jeton Keycloak, outils filtrés par groupe. Ce qui n'a pas été rejoué est listé en fin de
> document. La répétition de la veille reste obligatoire.

## Ce qui est dans ce dossier

| Fichier | Rôle |
| --- | --- |
| `kind/cluster.yaml` | Cluster kind à un nœud, avec trois ports publiés sur le poste : 4000 (LLM), 4001 (MCP), 8180 (Keycloak). Pas de port-forward pendant la démo. |
| `manifests/10-ratelimit.yaml` | Service de comptage Envoy `ratelimit` + Redis, et **les seuils** : requêtes par minute par équipe, budget de tokens de la clé de démonstration. |
| `manifests/20-keycloak.yaml` | Keycloak de démonstration (realm importé : groupes = équipes, utilisateurs `formateur`, `alice`, `bob`). |
| `manifests/30-gateway.yaml` | `AgentgatewayParameters` (catalogue de prix, base SQLite du tableau de bord, exposition NodePort) et la `Gateway` avec deux écouteurs, `llm` et `mcp`. |
| `manifests/40-modeles.yaml` | Les trois alias, un `AgentgatewayModel` chacun : `modele-generation`, `modele-juge`, `modele-reserve` (ce dernier réservé à `platform-team`). |
| `manifests/50-politiques-llm.yaml` | **La politique de l'écouteur LLM** : clés virtuelles, débit par équipe et budget de tokens, étiquettes « équipe » sur les métriques et le journal. |
| `manifests/60-mcp.yaml` | Le serveur MCP du fil rouge derrière la gateway : backend, route, jeton Keycloak exigé, outils visibles par `platform-team` seulement. |
| `costs/zen.json` | Prix des modèles Zen en dollars par million de tokens (ConfigMap `catalogue-prix-zen`). |
| `keycloak/realm-formation-ia.json` | Realm importé au démarrage (ConfigMap `keycloak-realm`). |
| `scripts/installer.sh` | Crée le cluster, installe Gateway API et agentgateway, publie secrets et ConfigMaps, applique les manifestes. Rejouable. |
| `scripts/preparer-demo.sh` | Génère les clés des équipes dans `cles-demo.txt`, publie leurs **empreintes SHA-256** dans la ConfigMap `cles-equipes`, vérifie chaque clé et le refus budget. |
| `scripts/nettoyer.sh` | Supprime le cluster kind. |
| `.env.example` | Clé OpenCode Zen et mot de passe admin Keycloak, à copier en `.env` (ignoré par git). |
| `opencode.gateway.example.json` | Extrait de configuration OpenCode pointant vers la gateway. |

## Ce qui change par rapport aux deux autres variantes

| Sujet | LiteLLM (Docker) | agentgateway binaire | agentgateway sur kind |
| --- | --- | --- | --- |
| Où vit la politique | Postgres, alimenté par l'API | `config.yaml`, relu à chaud | Objets Kubernetes (`Gateway`, `AgentgatewayModel`, `AgentgatewayPolicy`, `ConfigMap`), `kubectl apply` ou GitOps |
| Alias de modèle | `model_list` | `llm.models` | Un `AgentgatewayModel` par alias, avec sa transformation `model`, son secret, sa règle d'accès |
| Clé d'équipe | Objet team + clé en base | `apiKey.keys` dans le YAML, lues dans l'environnement | Entrées d'une `ConfigMap` : **empreinte SHA-256** + métadonnées `user_id`, `group` ; la clé en clair ne vit jamais dans le cluster |
| Modèle réservé | `access_groups` | `allowedModels` par clé | `policies.authorization` sur le modèle (CEL sur `apiKey.group`) ; `/v1/models` est filtré en conséquence |
| Budget | Dollars, en base | Dollars par clé, SQLite | **Tokens** par jour par clé, comptés par `ratelimit` + Redis (`unit: Tokens`) ; les dollars se lisent au journal et dans Analytics |
| Débit | `rpm_limit` par équipe | `remoteRateLimit` | `rateLimit.global` par équipe, même service `ratelimit` |
| Prix | `model_info` | catalogue JSON | ConfigMap + `AgentgatewayParameters.modelCatalog` |
| Journal, tableau de bord | Onglet Usage | UI Analytics / Logs, SQLite | UI **en lecture seule** sur le pod (`kubectl port-forward 15000`), Analytics / Logs sur SQLite éphémère ; `kubectl logs` porte coût et équipe |
| SSO | OIDC sur l'interface | OIDC sur la gateway UI | Jeton Keycloak exigé sur la route **MCP** ; l'UI Kubernetes n'a pas de connexion |
| MCP gateway | API `/v1/mcp/server` | section `mcp` | `AgentgatewayBackend` + `HTTPRoute`, autorisation CEL sur `jwt.groups` |
| Administration | Clé maître | API sur `localhost:15000` | `kubectl` et RBAC Kubernetes : qui peut modifier une `AgentgatewayPolicy` est une décision du cluster |

## Préparer, la veille

1. Prérequis sur le poste : Docker Desktop, `kind` (0.31 testé), `kubectl`, `helm`, `openssl`,
   `curl`, `python3`. Le cluster est créé par le script ; les autres clusters kind du poste
   ne sont pas touchés (contexte `kind-formation-ia`).
2. `cp .env.example .env` ; coller la clé OpenCode Zen dans `OPENCODE_API_KEY` et choisir
   `KEYCLOAK_ADMIN_PASSWORD`. Vérifier que la clé Zen répond (même commande qu'en
   `demo-litellm`, étape 2). Les modèles de la démo sont ceux de Zen, pas ceux des TP, et
   c'est sans importance : l'alias isole le code du modèle.
3. Installer :

   ```bash
   cd formateur/demo-agentgateway-k8s
   ./scripts/installer.sh
   ```

   Compter trois à cinq minutes la première fois (images à télécharger : agentgateway,
   Keycloak, ratelimit, Redis). Si le réseau de la salle est incertain, faire l'installation
   la veille et garder le cluster ; `kind` le redémarre avec Docker. Le script affiche l'état
   de la `Gateway` (`PROGRAMMED True`) et des politiques (`ACCEPTEE True`, `ATTACHEE True`).
   La politique `mcp-jeton-keycloak` peut rester `ATTACHEE <none>` une minute, le temps que
   le proxy récupère les clés de signature de Keycloak.
4. Peupler et vérifier :

   ```bash
   ./scripts/preparer-demo.sh
   ```

   Le script écrit une clé par équipe dans `cles-demo.txt` (ignoré par git), publie leurs
   empreintes dans la ConfigMap `cles-equipes`, puis vérifie : chaque clé obtient `200` sur
   `/v1/models` et ne voit **que ses modèles** (`modele-reserve` n'apparaît que pour
   `platform-team`), une clé inconnue reçoit `401`, et la clé `demo-budget-epuise` passe son
   premier appel puis reçoit `429` (le budget est débité après la réponse). Rejouable ;
   `--regenerer` remplace toutes les clés, ce qui révoque celles distribuées. Le budget de
   la clé de démonstration est un compteur journalier dans Redis : pour le réarmer,
   `kubectl -n ratelimit rollout restart deploy/redis`.
5. Interface en lecture seule : dans un terminal dédié,
   `kubectl port-forward deploy/agentgateway-proxy -n agentgateway-system 15000`, puis
   `http://localhost:15000/ui/`. Vérifier Traffic → Routes et Policies (les objets sont bien
   arrivés au proxy), LLM → Analytics (Group by : Group, Measure : Cost) et Logs. Le tableau de
   bord lit une base SQLite dans `/tmp` du pod : elle repart de zéro si le pod redémarre.
6. **Faire tourner un peu de trafic** pour que le journal ne soit pas vide le jour J : même
   recette qu'avec LiteLLM (juge du TP3 avec la clé `platform-team`, quelques appels par équipe
   produit, un appel à `modele-reserve` avec `platform-team`). Commandes dans « Commandes
   prêtes ». Ne lancez pas encore la salve de quarante appels : ce refus se joue en direct.
7. MCP gateway : démarrer le serveur MCP du fil rouge sur le poste (`mvn quarkus:dev` dans un
   serveur généré à la répétition ou celui du TP2), relever son point d'accès (souvent
   `http://localhost:8080/mcp`) ; depuis kind, le poste s'appelle `host.docker.internal`.
   Ajuster `host`, `port` ou `path` dans `manifests/60-mcp.yaml` si besoin, puis `kubectl apply`.
   Jouer les appels du bloc MCP de « Commandes prêtes ».
8. **Répéter la démo une fois en entier, chronomètre en main**, et enregistrer une capture
   vidéo de secours.

## Déroulé (20 min)

| Temps | Écran | Ce que vous montrez, ce que vous dites |
| --- | --- | --- |
| 1 min | Diapo « Une gateway de modèles » | Le retour d'expérience en deux phrases. « Tout ce que vous avez appelé depuis hier aurait pu passer par là. » |
| 3 min | Éditeur sur `manifests/`, puis `kubectl get agentgatewaymodel,agentgatewaypolicy -n agentgateway-system` | **La politique est du code Kubernetes.** Trois `AgentgatewayModel` ; Zen et sa clé n'apparaissent que comme `zen-secret`. Une `ConfigMap` d'empreintes pour les clés des équipes, une politique pour le débit et le budget, une ConfigMap de seuils. « Qui a fixé 30 requêtes par minute ? C'est écrit là ; chez vous, c'est une MR sur le dépôt de la plateforme, revue comme le Skill de demain. » |
| 5 min | Terminal | **Le fil rouge traverse la gateway.** (1) Le juge du TP3 : même commande Maven qu'au TP3, seules les variables changent (`LLM_ENDPOINT`, `LLM_MODEL`, `LLM_API_KEY`). (2) OpenCode en mode non interactif avec la clé `equipe-produit-a`. Puis `kubectl logs` : deux lignes, deux équipes, un coût chacune. Le code n'a pas changé ; une URL et un alias ont été fournis. |
| 4 min | Terminal | **Trois refus.** `modele-reserve` avec la clé produit → `403 model_authorization_denied` avant tout appel au fournisseur ; la même clé ne voit même pas ce modèle dans `/v1/models`. Clé `demo-budget-epuise` → `429`, le compteur de tokens vit dans Redis. Quarante appels en parallèle pour `equipe-produit-b` → trente `200` puis dix `429`, en-tête `x-ratelimit-remaining: 0`. À chaque fois : où le contrôle s'est appliqué, dans quel objet la valeur est écrite. |
| 3 min | `/ui/llm/analytics` et `/ui/llm/logs` (port-forward) | **Le journal.** Group by → Group, Measure → Cost : « le juge du TP3 a coûté combien ? ». Une ligne : modèle, fournisseur, tokens, coût, équipe, pas le contenu. « Bon réglage pour vos données réelles ? Qui décide de la rétention ? » Dire que l'UI est en lecture seule : la source de vérité est le cluster. |
| 2 min | Terminal | **Identité par l'annuaire.** Un jeton Keycloak pour `formateur` (groupe `platform-team`) ouvre la route MCP ; sans jeton, `401`. Le jeton d'`alice` est valide mais ne voit **aucun outil**, celui de `formateur` voit `find_service` et `get_owner`. « L'identité vient de Keycloak ; le groupe aussi ; la règle qui décide est une ligne CEL dans une `AgentgatewayPolicy`. » |
| 1 min | `manifests/60-mcp.yaml` | **La gateway devant un serveur MCP.** Un backend vers le serveur des participants, une route, deux politiques : le jeton exigé, les outils réservés à `platform-team`. Même mécanique que pour les modèles, autre identité. |
| 1 min | Diapo « Sécuriser les accès MCP » | Réglé : qui voit quel outil, avec l'identité de l'annuaire, et un journal. Pas réglé : un client qui joint `localhost:8080` directement contourne tout ; le serveur reste responsable de ses contrôles. Montrer le `curl` direct si le temps le permet. |
| 1 min | Diapo « Ce que la gateway rend gouvernable » | Fermer : le mécanisme est là, et il est **dans le dépôt de la plateforme** ; la politique reste à écrire, à relire, à décider. Enchaîner sur la trace fictive. |

## Comment c'est câblé

- **Modèles.** L'API `AgentgatewayModel` (expérimentale, activée par `agentgatewayModels.enabled=true`
  dans le chart) sert les chemins OpenAI standard sur l'écouteur `llm` : `/v1/chat/completions`,
  `/v1/models`. Chaque alias déclare son fournisseur (`OpenAI` pour GLM via
  `/zen/v1/chat/completions`, `Anthropic` pour Claude via `/zen/v1/messages`, les deux avec
  `baseURL: https://opencode.ai/zen/v1`), réécrit le nom du modèle par une transformation et
  lit la clé Zen dans le Secret `zen-secret`. agentgateway convertit à la volée une requête
  `chat/completions` vers le format Anthropic pour `modele-reserve`.
- **Clés virtuelles.** `traffic.apiKeyAuthentication` en mode `Strict` sur l'écouteur `llm`,
  ConfigMap sélectionnée par étiquette, entrées `keyHash` + `metadata`. Les métadonnées
  alimentent le débit (`apiKey.group`), le budget (`apiKey.user_id`), les métriques et le journal.
- **Débit et budget.** Une seule `AgentgatewayPolicy` porte l'authentification et
  `rateLimit.global` : deux politiques sur la même cible s'écraseraient en silence. Les seuils
  sont dans la ConfigMap du service `ratelimit` (domaine `formation-ia`) : `team` en requêtes
  par minute, `user_id` en tokens par jour pour `demo-budget-epuise` seulement.
- **Coût.** `AgentgatewayParameters.modelCatalog` référence la ConfigMap de prix ; la
  recherche se fait par fournisseur (`openai`, `anthropic`) et nom de modèle **après**
  transformation (`glm-5.3`, `claude-opus-5-5`). Vérifié : `cost_catalog_lookups_total{status="Exact"}`,
  et `agw.ai.usage.cost.total` sur chaque ligne de journal.
- **MCP.** `AgentgatewayBackend` avec une cible statique `host.docker.internal:8080/mcp`,
  `HTTPRoute /mcp` sur l'écouteur `mcp`, `jwtAuthentication` Strict (émetteur
  `http://localhost:8180/realms/formation-ia`, clés lues sur le Service `keycloak` dans le
  cluster, audience `formation-ia-mcp`), autorisation `Require has(jwt.groups)` sur la route,
  puis `backend.mcp.authorization` : outils autorisés si `jwt.groups` contient `platform-team`.
  Pour les autres, `tools/list` revient vide et `tools/call` répond `Unknown tool`.
- **Exposition.** Le Service généré par le control plane est passé en `NodePort` par un overlay
  dans `AgentgatewayParameters` (`30080` et `30081`), publiés par kind sur `localhost:4000`
  et `localhost:4001`. Keycloak de même sur `30180` → `localhost:8180`.

## Commandes prêtes

À lancer depuis `formateur/demo-agentgateway-k8s`. Les clés viennent de `cles-demo.txt`,
le dépôt est retrouvé par git. La commande OpenCode se lance dans un répertoire temporaire
pour ne pas toucher à votre `opencode.json` habituel. Le serveur à juger est celui de votre
répétition (`$ATELIER_DIR/serveur-avec-skill`) ; à défaut, le projet piège du dossier formateur.

```bash
cd formateur/demo-agentgateway-k8s
export G=http://localhost:4000 M=http://localhost:4001 NS=agentgateway-system
export CLE_PLATFORM=$(awk '$1=="platform-team"{print $2}' cles-demo.txt)
export CLE_PRODUIT=$(awk '$1=="equipe-produit-a"{print $2}' cles-demo.txt)
export CLE_PRODUIT_B=$(awk '$1=="equipe-produit-b"{print $2}' cles-demo.txt)
export CLE_DEMO=$(awk '$1=="demo-budget-epuise"{print $2}' cles-demo.txt)
export FORMATION_REPO=$(git rev-parse --show-toplevel)
export SERVEUR_A_JUGER="${ATELIER_DIR:-$FORMATION_REPO/formateur/demo-scorer-trompeur}/${ATELIER_DIR:+serveur-avec-skill}${ATELIER_DIR:-projet-piege}"
echo "clés : ${#CLE_PLATFORM}/${#CLE_PRODUIT}/${#CLE_DEMO} caractères — serveur jugé : $SERVEUR_A_JUGER"
```

```bash
# Ce que chaque clé voit : /v1/models est filtré par les règles d'accès des modèles
curl -sS "$G/v1/models" -H "Authorization: Bearer $CLE_PRODUIT" | python3 -m json.tool

# Le juge du TP3 à travers la gateway (code inchangé)
LLM_ENDPOINT="$G/v1/chat/completions" LLM_MODEL=modele-juge LLM_API_KEY="$CLE_PLATFORM" \
  mvn -q -f "$FORMATION_REPO/tp3-eval/pom.xml" -Dtest=JugeTest \
  -Dserveur.genere.dir="$SERVEUR_A_JUGER" test

# OpenCode en mode non interactif, à travers la gateway, avec la clé de l'équipe produit A
D=$(mktemp -d) && sed 's#"_note": "[^"]*",##' opencode.gateway.example.json > "$D/opencode.json"
( cd "$D" && AGW_KEY="$CLE_PRODUIT" opencode run --model gateway-demo/modele-generation \
    "Réponds en une phrase : qu'est-ce que MCP ?" )

# Un appel simple, réponse complète (usage : tokens)
curl -sS "$G/v1/chat/completions" -H "Authorization: Bearer $CLE_PRODUIT" -H 'Content-Type: application/json' \
  -d '{"model":"modele-generation","max_tokens":40,"messages":[{"role":"user","content":"Réponds : prêt."}]}'

# Les dernières lignes du journal : coût, modèle, fournisseur, équipe
kubectl logs deploy/agentgateway-proxy -n $NS | grep protocol=llm | tail -3 \
  | grep -o -E 'http.status=[0-9]+|gen_ai.request.model=[^ ]+|agw.ai.usage.cost.total=[^ ]+|team="[^"]+"' | paste - - - -

# Modèle non autorisé pour l'équipe (403 model_authorization_denied, le fournisseur n'est pas appelé)
curl -sS -w '\nHTTP %{http_code}\n' "$G/v1/chat/completions" -H "Authorization: Bearer $CLE_PRODUIT" \
  -H 'Content-Type: application/json' -d '{"model":"modele-reserve","messages":[{"role":"user","content":"bonjour"}]}'

# Budget de tokens épuisé (clé de démonstration, déjà amorcée par le script)
curl -sS -D - -o /dev/null "$G/v1/chat/completions" -H "Authorization: Bearer $CLE_DEMO" \
  -H 'Content-Type: application/json' -d '{"model":"modele-juge","messages":[{"role":"user","content":"bonjour"}]}' | grep -i -E "^HTTP|ratelimit"

# Limite de débit : 40 appels en parallèle pour equipe-produit-b (30 par minute) → trente 200, dix 429
export CLE_B="$CLE_PRODUIT_B"
seq 1 40 | xargs -P 10 -I{} sh -c 'curl -sS -o /dev/null -w "%{http_code}\n" "$G/v1/chat/completions" \
  -H "Authorization: Bearer $CLE_B" -H "Content-Type: application/json" \
  -d "{\"model\":\"modele-juge\",\"max_tokens\":1,\"messages\":[{\"role\":\"user\",\"content\":\"ok\"}]}"' | sort | uniq -c
kubectl logs deploy/ratelimit -n ratelimit --tail 100 | grep -c OVER_LIMIT   # décisions du service de comptage

# Métriques et interface (port-forward dans un autre terminal : kubectl port-forward deploy/agentgateway-proxy -n $NS 15000 15020)
curl -s http://localhost:15020/metrics | grep -E 'gen_ai_client_token_usage_sum|cost_catalog_lookups_total' | cut -c1-200
open http://localhost:15000/ui/llm/analytics

# ---- MCP gateway : le serveur Quarkus du fil rouge doit tourner sur le poste (localhost:8080/mcp)
H=(-H 'Content-Type: application/json' -H 'Accept: application/json, text/event-stream')
jeton()   { curl -sS -X POST http://localhost:8180/realms/formation-ia/protocol/openid-connect/token \
              -d grant_type=password -d client_id=agentgateway-mcp -d "username=$1" -d "password=$1" -d scope=openid \
              | python3 -c 'import sys,json;print(json.load(sys.stdin)["access_token"])'; }
session() { curl -sS -i -X POST "$M/mcp" -H "Authorization: Bearer $1" "${H[@]}" \
              -d '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-03-26","capabilities":{},"clientInfo":{"name":"curl","version":"1"}}}' \
              | awk 'tolower($1)=="mcp-session-id:"{print $2}' | tr -d '\r'; }

# Sans jeton : 401
curl -sS -o /dev/null -w 'HTTP %{http_code}\n' -X POST "$M/mcp" "${H[@]}" -d '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-03-26","capabilities":{},"clientInfo":{"name":"curl","version":"1"}}}'

# formateur (platform-team) : les outils du TP
T=$(jeton formateur); S=$(session "$T")
curl -sS -X POST "$M/mcp" -H "Authorization: Bearer $T" -H "mcp-session-id: $S" "${H[@]}" -d '{"jsonrpc":"2.0","id":2,"method":"tools/list"}' | sed -n 's/^data: //p'
curl -sS -X POST "$M/mcp" -H "Authorization: Bearer $T" -H "mcp-session-id: $S" "${H[@]}" \
  -d '{"jsonrpc":"2.0","id":3,"method":"tools/call","params":{"name":"find_service","arguments":{"query":"auth"}}}' | sed -n 's/^data: //p'

# alice (équipe produit A) : jeton valide, aucun outil
T=$(jeton alice); S=$(session "$T")
curl -sS -X POST "$M/mcp" -H "Authorization: Bearer $T" -H "mcp-session-id: $S" "${H[@]}" -d '{"jsonrpc":"2.0","id":2,"method":"tools/list"}' | sed -n 's/^data: //p'

# Le contournement : le serveur Quarkus en direct, sans jeton, sans journal
curl -sS -o /dev/null -w 'direct : HTTP %{http_code}\n' -X POST http://localhost:8080/mcp "${H[@]}" -d '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-03-26","capabilities":{},"clientInfo":{"name":"curl","version":"1"}}}'
```

## Dépannage

| Symptôme | Vérification |
| --- | --- |
| `preparer-demo.sh` : « la gateway ne répond pas 200 » | `kubectl get gateway -n agentgateway-system` (PROGRAMMED), `kubectl logs deploy/agentgateway-proxy -n agentgateway-system` ; ports 4000/4001 libres sur le poste avant `kind create` |
| `model_not_found` sur un alias | API `AgentgatewayModel` non activée (`agentgatewayModels.enabled`), écouteur `llm` sans `kinds: AgentgatewayModel`, ou `parentRefs.sectionName` différent de `llm` : `kubectl get gateway … -o jsonpath='{.status.listeners[*].attachedRoutes}'` doit donner 3 |
| Tous les appels LLM en `500` | Service `ratelimit` indisponible et `failureMode: FailClosed` : `kubectl get pods -n ratelimit`, domaine ou noms de descripteurs différents entre la politique et la ConfigMap |
| Aucun `429` sur la salve | Appels trop lents pour tenir dans la même minute : lancer en parallèle (`xargs -P 10`) ; `LOG_LEVEL: debug` du service montre `got descriptor: (team=…)` |
| Coût à 0 au journal | `cost_catalog_lookups_total{status="Missing"}` : le couple `gen_ai_system` / `gen_ai_request_model` du journal n'est pas dans `costs/zen.json` ; corriger la ConfigMap et `kubectl rollout restart deploy/agentgateway-proxy` |
| `mcp-jeton-keycloak` reste `ATTACHEE <none>` | Le proxy n'a pas encore les clés Keycloak : attendre que le pod `keycloak` soit prêt ; `kubectl get agentgatewaypolicy mcp-jeton-keycloak -n agentgateway-system -o yaml` donne le message |
| Jeton refusé (`401`) alors qu'il vient de Keycloak | `iss` du jeton ≠ `issuer` de la politique : `KC_HOSTNAME` doit rester `http://localhost:8180` ; audience `formation-ia-mcp` absente (mapper du client `agentgateway-mcp`) |
| `tools/list` vide pour `formateur` | Serveur Quarkus arrêté ou autre point d'accès ; `host.docker.internal` ; vérifier `kubectl get agentgatewaybackend catalogue -o yaml` |
| UI vide | Le port-forward 15000 doit être actif ; la base SQLite est dans `/tmp` du pod et repart de zéro à chaque redémarrage |
| Coût à 0 sur l'appel OpenCode alors que les `curl` sont chiffrés | OpenCode demande une réponse en **streaming** et Zen n'a renvoyé aucun `usage` dans le flux : sans tokens, pas de coût ni de débit de budget pour cet appel. Constaté le 29 sept. 2026 avec `glm-5.3` ; à montrer tel quel (« ce que le fournisseur ne compte pas, la gateway ne peut pas le facturer ») ou à contourner en demandant à OpenCode un modèle qui remonte l'usage en streaming |

## Ce qui n'a pas été rejoué ici

- Le juge du TP3 à travers la gateway : le chemin `/v1/chat/completions` est celui vérifié avec
  `curl` et OpenCode, la commande Maven est identique à celle des autres variantes.
- Le tableau de bord Analytics groupé par équipe : la base et le catalogue sont en place
  (coût présent dans `/api/logs/search`), le rendu et le « Group by : Group » sont à confirmer
  dans l'UI lors de la répétition.
- Le comportement après redémarrage du poste : kind redémarre le cluster avec Docker, mais
  la base SQLite du tableau de bord est éphémère, et le compteur de budget vit dans Redis.

Après la session : `./scripts/preparer-demo.sh --regenerer` révoque les clés distribuées ;
`./scripts/nettoyer.sh` supprime le cluster.
