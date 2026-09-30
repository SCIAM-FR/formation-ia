---
title: TP5 — Bâtir une gateway de modèles et de serveurs MCP
description: Construire sur un cluster kind la gateway vue en M5 avec agentgateway — alias, clés par équipe, coût, débit, budget — puis y placer son serveur MCP derrière un jeton Keycloak.
permalink: /tp5/
previous_url: /tp4/
previous_title: TP4 — Gouverner
next_url: /fiche-de-sortie/
next_title: Fiche de sortie (M7)
---

> **TP optionnel et long.** Il reprend, pour que vous le construisiez vous-mêmes, ce que
> la démonstration M5 a montré sur le poste du formateur. Il se joue hors des 14 h, en
> journée complémentaire, ou remplace le TP4 pour un groupe sans accès GitLab ou GitHub.
> Prérequis : Docker Desktop, `kind`, `kubectl`, `helm`, une clé du fournisseur de modèles
> retenu pour la formation, votre serveur MCP du TP2.

## Objectif et livrables

À la fin, vous avez **une gateway qui tourne sur votre poste**, dont toute la politique est
écrite en objets Kubernetes que vous pourriez relire en merge request : trois alias de
modèles, une clé par équipe, un modèle réservé, un coût par appel, un débit et un budget par
équipe, et votre serveur MCP du TP2 derrière la même gateway, accessible avec un jeton
Keycloak et des outils visibles selon le groupe. Le fil rouge des TP précédents la
traverse sans changer une ligne de code : le juge du TP3 et OpenCode.

Livrables : les quatre manifestes complétés dans `tp5-gateway/squelettes/`, la ConfigMap
des seuils et le catalogue de prix, et un compte rendu avec les preuves de chaque section
(commandes et codes HTTP observés). Les clés en clair ne rentrent jamais dans le dépôt.

Support : [consigne du TP5]({{ site.repository_url }}/blob/main/tp5-gateway/README.md).
Corrigé formateur : la démonstration
[`formateur/demo-agentgateway-k8s/`]({{ site.repository_url }}/tree/main/formateur/demo-agentgateway-k8s).
Ne l'ouvrez qu'au débrief.

## Essentiel et approfondissement

| Parcours | Sections | Preuve de sortie |
| --- | --- | --- |
| **Essentiel — 3 h cible** | 1 à 8 | `/v1/models` filtré par équipe, `401` / `403` / `429` observés et expliqués, coût chiffré au journal, juge et OpenCode passés par la gateway, `tools/list` plein pour `formateur` et vide pour `alice` |
| **Approfondissement — hors 14 h** | 9 | Manifestes sous revue dans le dépôt du TP4, modèle virtuel de repli, tableau de bord Analytics |

Durées indicatives : 20 min de socle, 25 min par section 2 et 6, 15 à 20 min pour les
autres, 40 min pour la section MCP. Un poste sans les images en cache ajoute cinq à dix
minutes de téléchargement en section 1.

## 1. Poser le socle

Vous n'écrivez pas le cluster ni le control plane : le script les installe. Vous lisez ce
qu'il fait avant de le lancer.

```bash
cd "$FORMATION_REPO/tp5-gateway"
cp .env.example .env            # clé du fournisseur, mot de passe admin Keycloak
cat scripts/socle.sh            # cluster kind, Gateway API 1.6, charts agentgateway 1.5, ratelimit, Keycloak
./scripts/socle.sh
```

Le script crée le cluster `tp5-gateway` avec trois ports publiés sur votre poste (4000,
4001, 8180), installe les CRD de la Gateway API et le control plane agentgateway avec l'API
`AgentgatewayModel` activée, met votre clé fournisseur dans le Secret `fournisseur-secret`,
puis déploie le service de comptage et Keycloak.

Vérifiez et notez :

```bash
kubectl get gatewayclass agentgateway          # ACCEPTED True
kubectl get pods -n agentgateway-system -n ratelimit -n keycloak
kubectl get crd | grep agentgateway.dev        # les types que vous allez écrire
```

Ouvrez `kubectl explain agentgatewaymodel.spec` et `kubectl explain agentgatewaypolicy.spec.traffic`
: ce sont vos références pour tout le TP, à préférer aux exemples copiés.

## 2. Une gateway et un premier alias

Complétez `squelettes/30-gateway.yaml` : l'écouteur `llm` doit autoriser le kind
`AgentgatewayModel`, c'est ce qui active les chemins `/v1/chat/completions` et `/v1/models`.
Laissez l'écouteur `mcp` pour la section 8. Puis `squelettes/40-modeles.yaml` : l'alias
`modele-generation`, avec l'URL de base du fournisseur, le modèle réel dans la transformation
`model`, et le Secret. Ajoutez `modele-juge` sur un modèle moins cher.

```bash
kubectl apply -f squelettes/30-gateway.yaml
kubectl apply -f squelettes/40-modeles.yaml
kubectl get gateway -n agentgateway-system                     # PROGRAMMED True
kubectl get gateway agentgateway-proxy -n agentgateway-system -o jsonpath='{.status.listeners[*].attachedRoutes}'
```

Le nombre de routes attachées à l'écouteur `llm` doit être le nombre d'alias. Appelez :

```bash
export G=http://localhost:4000
curl -sS "$G/v1/models"
curl -sS "$G/v1/chat/completions" -H 'Content-Type: application/json' \
  -d '{"model":"modele-generation","max_tokens":20,"messages":[{"role":"user","content":"Réponds : prêt."}]}'
kubectl logs deploy/agentgateway-proxy -n agentgateway-system | grep protocol=llm | tail -1
```

Relevez dans la ligne de journal : `gen_ai.provider.name`, `gen_ai.request.model` (le
modèle **réel**, pas l'alias), les tokens, et `agw.ai.usage.cost.total`, qui vaut `0` pour
l'instant. Notez qu'à ce stade **n'importe qui** peut appeler : c'est l'objet de la section 3.

Demandez un modèle qui n'existe pas : la gateway répond `model_not_found` sans toucher au
fournisseur.

## 3. Des clés par équipe

Les clés virtuelles vivent dans une ConfigMap d'**empreintes SHA-256** : la clé en clair
n'existe que chez l'équipe qui la reçoit. Générez-les et lisez l'objet avant de l'appliquer :

```bash
./scripts/generer-cles.sh > cles-equipes.yaml
cat cles-equipes.yaml           # une entrée par équipe : keyHash + metadata (user_id, group)
kubectl apply -f cles-equipes.yaml
cat cles.txt                    # les clés en clair, hors dépôt
```

Complétez `squelettes/50-politiques-llm.yaml` : `apiKeyAuthentication` avec le sélecteur
d'étiquette de la ConfigMap et le mode que vous jugez juste. Justifiez dans le compte rendu
pourquoi `Optional` serait une erreur ici.

```bash
kubectl apply -f squelettes/50-politiques-llm.yaml
kubectl get agentgatewaypolicy -n agentgateway-system -o custom-columns='NOM:.metadata.name,ACCEPTEE:.status.ancestors[0].conditions[?(@.type=="Accepted")].status,ATTACHEE:.status.ancestors[0].conditions[?(@.type=="Attached")].status'
export CLE_A=$(awk '$1=="equipe-produit-a"{print $2}' cles.txt)
curl -sS -o /dev/null -w '%{http_code}\n' "$G/v1/models"                                   # 401
curl -sS -o /dev/null -w '%{http_code}\n' "$G/v1/models" -H "Authorization: Bearer $CLE_A"  # 200
curl -sS -o /dev/null -w '%{http_code}\n' "$G/v1/models" -H "Authorization: Bearer sk-tp5-invalide"
```

Question à trancher dans le compte rendu : que se passe-t-il si l'équipe perd sa clé ? Qui
la régénère, où, et comment l'ancienne cesse-t-elle de fonctionner ? Relancez
`generer-cles.sh` après avoir supprimé une ligne de `cles.txt` pour le vérifier.

## 4. Réserver un modèle

Ajoutez `modele-reserve` dans `40-modeles.yaml`, sur le modèle le plus cher dont vous
disposez, avec une règle d'autorisation inline : seul `apiKey.group == "platform-team"` peut
le demander. Appliquez, puis :

```bash
export CLE_P=$(awk '$1=="platform-team"{print $2}' cles.txt)
curl -sS "$G/v1/models" -H "Authorization: Bearer $CLE_A" | python3 -m json.tool   # modele-reserve absent
curl -sS "$G/v1/models" -H "Authorization: Bearer $CLE_P" | python3 -m json.tool   # présent
curl -sS -w '\n%{http_code}\n' "$G/v1/chat/completions" -H "Authorization: Bearer $CLE_A" -H 'Content-Type: application/json' \
  -d '{"model":"modele-reserve","messages":[{"role":"user","content":"bonjour"}]}'      # 403 model_authorization_denied
```

Relevez que le refus intervient **avant** tout appel au fournisseur : rien au journal côté
`endpoint=`, rien de facturé. Comparez avec le TP4 : la même idée, une règle lisible qui
décide, mais ici sur un appel de modèle plutôt que sur une fusion.

## 5. Chiffrer chaque appel

Écrivez `costs/catalogue.json` à partir de `fournis/costs/catalogue.example.json`, avec les
prix réels de vos modèles en dollars par million de tokens. Les clés de recherche sont le
**fournisseur** et le **modèle réel** tels qu'ils apparaissent dans le journal de la section 2.
Publiez-le et référencez-le dans les paramètres de la gateway :

```bash
kubectl -n agentgateway-system create configmap catalogue-prix --from-file=catalogue.json=costs/catalogue.json \
  --dry-run=client -o yaml | kubectl apply -f -
```

Dans `30-gateway.yaml`, complétez `modelCatalog` et, pour les pages Analytics et Logs de
l'interface, `rawConfig.config.database` avec une base SQLite dans `/tmp`. Dans
`50-politiques-llm.yaml`, ajoutez les étiquettes `team` sur les métriques et `team` / `cle`
sur le journal. Appliquez les deux, attendez le redémarrage du proxy, puis :

```bash
kubectl rollout status deploy/agentgateway-proxy -n agentgateway-system
curl -sS -o /dev/null "$G/v1/chat/completions" -H "Authorization: Bearer $CLE_A" -H 'Content-Type: application/json' \
  -d '{"model":"modele-juge","max_tokens":20,"messages":[{"role":"user","content":"Réponds : prêt."}]}'
kubectl logs deploy/agentgateway-proxy -n agentgateway-system | grep protocol=llm | tail -1 \
  | grep -o -E 'agw.ai.usage.cost.total=[^ ]+|team="[^"]+"'
kubectl port-forward deploy/agentgateway-proxy -n agentgateway-system 15020 15000 &
curl -s http://localhost:15020/metrics | grep cost_catalog_lookups_total
```

Le coût doit être non nul et le compteur en `status="Exact"`. S'il est en `Missing`,
c'est la clé de recherche : corrigez le catalogue, réappliquez la ConfigMap et redémarrez le
proxy. Ouvrez ensuite `http://localhost:15000/ui/llm/analytics` : l'interface est en lecture
seule en mode Kubernetes, la source de vérité est le cluster.

## 6. Limiter le débit et poser un budget

Les compteurs partagés vivent dans le service `ratelimit` fourni ; **vous écrivez les
seuils**. Dans `fournis/10-ratelimit.yaml`, remplacez le seuil unique par : 30 requêtes par
minute pour chaque équipe produit, 60 pour les autres, et un budget de 20 **tokens** par jour
pour la clé `demo-budget-epuise`. Le nom des clés de descripteurs (`team`, `user_id`) doit
être exactement celui que la gateway enverra.

Dans `50-politiques-llm.yaml`, ajoutez `rateLimit.global` : domaine `tp5`, référence au
Service `ratelimit`, un descripteur `team` en requêtes et un descripteur `user_id` en tokens.
Décidez de `failureMode` et écrivez pourquoi.

```bash
kubectl apply -f fournis/10-ratelimit.yaml && kubectl -n ratelimit rollout restart deploy/ratelimit
kubectl apply -f squelettes/50-politiques-llm.yaml
export CLE_B=$(awk '$1=="equipe-produit-b"{print $2}' cles.txt) CLE_D=$(awk '$1=="demo-budget-epuise"{print $2}' cles.txt)
# Budget de tokens : le premier appel passe (débité après la réponse), le second est refusé
for i in 1 2; do curl -sS -o /dev/null -w '%{http_code}\n' "$G/v1/chat/completions" -H "Authorization: Bearer $CLE_D" \
  -H 'Content-Type: application/json' -d '{"model":"modele-juge","max_tokens":5,"messages":[{"role":"user","content":"ok"}]}'; done
# Débit : 40 appels en parallèle pour l'équipe B → 30 fois 200, 10 fois 429
seq 1 40 | xargs -P 10 -I{} sh -c 'curl -sS -o /dev/null -w "%{http_code}\n" "$G/v1/chat/completions" \
  -H "Authorization: Bearer $CLE_B" -H "Content-Type: application/json" \
  -d "{\"model\":\"modele-juge\",\"max_tokens\":1,\"messages\":[{\"role\":\"user\",\"content\":\"ok\"}]}"' | sort | uniq -c
kubectl logs deploy/ratelimit -n ratelimit --tail 50 | grep -E "got descriptor|OVER_LIMIT" | tail -5
```

Deux pièges à observer et à consigner : une salve trop lente s'étale sur deux fenêtres d'une
minute et ne déclenche rien ; le budget de tokens est débité **après** la réponse, l'appel
qui franchit la limite passe. Que faudrait-il pour un vrai plafond en dollars ? Comparez avec
la variante binaire de la démo, qui sait le faire avec une base de données.

## 7. Faire passer le fil rouge

Rien ne change dans le code : une URL, un alias, une clé.

```bash
LLM_ENDPOINT="$G/v1/chat/completions" LLM_MODEL=modele-juge LLM_API_KEY="$CLE_P" \
  mvn -q -f "$FORMATION_REPO/tp3-eval/pom.xml" -Dtest=JugeTest \
  -Dserveur.genere.dir="$ATELIER_DIR/serveur-avec-skill" test
D=$(mktemp -d) && sed 's#"_note": "[^"]*",##' opencode.gateway.example.json > "$D/opencode.json"
( cd "$D" && AGW_KEY="$CLE_A" opencode run --model gateway-demo/modele-generation "Réponds en une phrase : qu'est-ce que MCP ?" )
kubectl logs deploy/agentgateway-proxy -n agentgateway-system | grep protocol=llm | tail -2
```

Répondez à la question de la direction : **le juge du TP3 a coûté combien, pour quelle
équipe ?** Relevez aussi le coût de l'appel OpenCode : s'il est nul, regardez si le
fournisseur a renvoyé les tokens dans une réponse en streaming. Ce que le fournisseur ne
compte pas, la gateway ne peut pas le facturer : c'est une limite à connaître.

## 8. Votre serveur MCP derrière la gateway, avec un jeton Keycloak

Démarrez votre serveur du TP2 sur le poste (`mvn quarkus:dev`) et relevez son point
d'accès. Complétez `30-gateway.yaml` avec l'écouteur `mcp` (port 3000, kind `HTTPRoute`),
puis `60-mcp.yaml` : le backend vers `host.docker.internal`, la route, et les deux
politiques : jeton Keycloak exigé sur la route (émetteur `http://localhost:8180/realms/formation-ia`,
clés lues sur le Service `keycloak`, audience `formation-ia-mcp`, puis `Require has(jwt.groups)`),
et outils du backend réservés au groupe `platform-team`.

```bash
kubectl apply -f squelettes/30-gateway.yaml -f squelettes/60-mcp.yaml
kubectl get agentgatewaypolicy -n agentgateway-system    # mcp-jeton-keycloak peut rester non attachée une minute (JWKS)
export M=http://localhost:4001
H=(-H 'Content-Type: application/json' -H 'Accept: application/json, text/event-stream')
jeton()   { curl -sS -X POST http://localhost:8180/realms/formation-ia/protocol/openid-connect/token \
              -d grant_type=password -d client_id=agentgateway-mcp -d "username=$1" -d "password=$1" -d scope=openid \
              | python3 -c 'import sys,json;print(json.load(sys.stdin)["access_token"])'; }
session() { curl -sS -i -X POST "$M/mcp" -H "Authorization: Bearer $1" "${H[@]}" \
              -d '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-03-26","capabilities":{},"clientInfo":{"name":"curl","version":"1"}}}' \
              | awk 'tolower($1)=="mcp-session-id:"{print $2}' | tr -d '\r'; }
curl -sS -o /dev/null -w 'sans jeton : %{http_code}\n' -X POST "$M/mcp" "${H[@]}" -d '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-03-26","capabilities":{},"clientInfo":{"name":"curl","version":"1"}}}'
for u in formateur alice; do T=$(jeton $u); S=$(session "$T"); echo "== $u"; \
  curl -sS -X POST "$M/mcp" -H "Authorization: Bearer $T" -H "mcp-session-id: $S" "${H[@]}" -d '{"jsonrpc":"2.0","id":2,"method":"tools/list"}' | sed -n 's/^data: //p'; done
```

Décodez le jeton de `formateur` (`python3 -c` sur la seconde partie du JWT) et relevez
`iss`, `aud`, `groups`. Puis, avec son jeton, appelez `find_service` avec `query = "auth"` à
travers la gateway, et la même chose avec le jeton d'`alice` : notez la réponse exacte.
Terminez par le contournement : le même `initialize` directement sur votre serveur,
`localhost:8080/mcp`, sans jeton. Concluez dans le compte rendu : ce que la gateway règle,
ce que le serveur doit régler lui-même.

## 9. Approfondissements

- **Mettre la gateway sous revue.** Copiez vos quatre manifestes et la ConfigMap des seuils
  dans le dépôt du TP4, ajoutez-les au `CODEOWNERS`, et faites relire en MR ou PR une
  modification de budget. En CI, `kubectl apply --dry-run=server` contre votre kind ne sera
  pas possible depuis un runner distant : discutez ce qui le serait chez vous.
- **Un modèle virtuel de repli.** Un `AgentgatewayModel` avec `virtualModel.failover` entre
  deux modèles internes, dont un pointé sur un Service sans pod ; observez l'éviction puis le
  repli, et la première requête qui échoue quand même.
- **Analytics.** Avec la base SQLite de la section 5, comparez le « Group by : Group,
  Measure : Cost » de l'interface avec vos relevés manuels. Que perd-on au redémarrage du pod ?

## Dépannage

| Symptôme | Vérification |
| --- | --- |
| `model_not_found` sur un alias | Kind `AgentgatewayModel` absent de l'écouteur, `sectionName` différent de `llm`, `attachedRoutes` à 0 ; l'API doit être activée dans le chart |
| Politique `ACCEPTEE True` mais sans effet | Deux politiques posent le même champ sur la même cible : fusionnez-les ; `kubectl get agentgatewaypolicy … -o yaml` |
| Tous les appels en `500` | `failureMode: FailClosed` et service `ratelimit` injoignable, ou domaine différent entre politique et ConfigMap |
| Aucun `429` sur la salve | Appels trop lents pour une seule fenêtre : parallélisez ; `LOG_LEVEL: debug` du service montre `got descriptor` |
| Coût à `0` | `cost_catalog_lookups_total{status="Missing"}` : fournisseur ou modèle réel absent du catalogue ; redémarrer le proxy après correction |
| Jeton refusé `401` | `iss` du jeton ≠ `issuer` de la politique (`KC_HOSTNAME`), audience absente, ou JWKS pas encore chargé |
| `tools/list` vide pour `formateur` | Serveur Quarkus arrêté, port ou chemin différent, `host.docker.internal` ; `kubectl get agentgatewaybackend catalogue -o yaml` |
| Ports 4000/4001/8180 occupés | Un autre cluster ou service sur le poste : `lsof -i :4000` avant `socle.sh` |

## Point de passage

Le TP est atteint quand les trois alias répondent, que `/v1/models` diffère selon la clé,
que vous avez provoqué et expliqué un `401`, un `403` et un `429`, que le coût est chiffré
au journal, que le juge et OpenCode sont passés par la gateway, et que votre serveur MCP
répond à `formateur` et rien à `alice` à travers elle. Le compte rendu dit, pour chaque
contrôle, **dans quel objet la valeur est écrite et qui devrait avoir le droit de la
changer** : c'est la question de M6, posée à la gateway.

Après le TP : `./scripts/nettoyer.sh` supprime le cluster ; supprimez `cles.txt`.
