# Démonstration M5 — la même gateway de modèles avec agentgateway (~20 min)

Variante de [`demo-litellm/`](../demo-litellm/README.md) : **même démonstration, autre
gateway**. agentgateway est un projet open source (Apache 2.0) donné à la Linux Foundation
et rattaché à l'Agentic AI Foundation ; c'est un data plane écrit en Rust, configuré par
**un fichier YAML déclaratif**, qui sert dans le même binaire le trafic LLM
(API OpenAI et Anthropic), le trafic MCP et l'HTTP ordinaire. Il tourne en local dans
Docker sur le poste du formateur, avec **OpenCode Zen** comme fournisseur de modèles, et
c'est le **fil rouge de la formation** qui passe à travers : le juge du TP3, une
génération OpenCode, le serveur MCP généré au TP2.

L'objectif pédagogique ne change pas : rendre concrètes quatre responsabilités de la
diapo M0 « Les responsabilités qui restent à l'organisation », identité des appels, coût
et routage, règles d'accès, audit. Ce qui change, et qui vaut d'être dit en salle : chez
LiteLLM, équipes, clés et budgets se créent **par l'API ou l'interface** et vivent dans
Postgres ; chez agentgateway, tout cela est **dans le fichier de configuration**, donc
versionnable, relisible, revoyable en merge request, exactement comme le Skill du TP4.
« Qui a fixé 5 $ et 30 requêtes par minute ? C'est écrit là, à la ligne 90, et le commit
dit qui et quand. »

> **Une seule variante par séance.** Choisissez LiteLLM ou agentgateway, répétez celle-là.
> Les diapos M5 du support décrivent LiteLLM ; si vous retenez agentgateway, adaptez les
> diapos 60 à 63 (vocabulaire : « clé virtuelle » au lieu d'« équipe », « fichier de
> configuration » au lieu d'« interface d'administration »).

> **État de cette variante (29 septembre 2026).** Écrite à partir de la documentation
> agentgateway v1.5.0 ; `config.example.yaml` passe `agentgateway --validate-only` ; le
> compose est validé par `docker compose config`. Elle n'a **pas encore été exécutée de
> bout en bout** : la répétition de la veille n'est pas facultative, et la section
> « À confirmer lors de la répétition » liste ce qui doit être vérifié en premier.

## Ce qui est dans ce dossier

| Fichier | Rôle |
| --- | --- |
| `docker-compose.yml` | agentgateway, le service de comptage Envoy `ratelimit` avec Redis (débit par équipe), Keycloak. Environnement de démonstration, pas de production. |
| `config.example.yaml` | **Toute la démo** : fournisseur Zen, trois alias, cinq clés virtuelles avec budgets et modèles autorisés, débit par équipe, SSO de l'interface, serveur MCP du fil rouge et ses droits. À copier en `config.yaml` (ignoré par git). |
| `.env.example` | Clé OpenCode Zen, secrets Keycloak et cookie, clés virtuelles (remplies par le script). À copier en `.env` (ignoré par git). |
| `costs/zen.json` | Catalogue de prix des modèles Zen en dollars par million de tokens : sans lui, le journal affiche une dépense nulle et le budget en dollars ne bloque jamais. |
| `ratelimit-config/formation-ia.yaml` | Seuils de requêtes par minute par équipe, appliqués par le service `ratelimit`. |
| `scripts/preparer-demo.sh` | Génère les clés virtuelles dans `.env`, recrée la gateway, écrit `cles-demo.txt`, vérifie chaque clé et amorce la clé au budget épuisé. |
| `opencode.gateway.example.json` | Extrait de configuration OpenCode pointant vers la gateway locale. |
| `keycloak/realm-formation-ia.json` | Le realm de `demo-litellm` (groupes = équipes, utilisateurs `formateur`, `alice`, `bob`) plus un client `agentgateway-ui` pour l'interface. |

## Ce qui change par rapport à LiteLLM

| Sujet | LiteLLM | agentgateway |
| --- | --- | --- |
| Où vit la politique | Postgres, alimenté par l'API et l'interface (`/team/new`, `/key/generate`) | `config.yaml`, relu à chaud ; l'interface est en lecture seule (`storage.mode: readOnly`) |
| Équipe | Objet « team » avec budget, débit, modèles ; clés rattachées | Pas d'objet équipe : une **clé virtuelle** par équipe, avec `metadata.group`, `allowedModels`, `budgets` |
| Budget | Par équipe et par clé, en dollars, sur la dépense enregistrée | Par clé, `USD` ou `Tokens`, fenêtre glissante alignée sur l'epoch, `Block` ou `Audit` ; compté en base **après** la réponse |
| Prix des modèles | `model_info` dans le YAML | Catalogue JSON séparé (`config.modelCatalog`), importable depuis models.dev avec `agctl catalog import` |
| Débit | `rpm_limit` par équipe, en base | `localRateLimit` = toute la gateway ; **par équipe** = `remoteRateLimit` vers un service Envoy `ratelimit` + Redis |
| Journal | Onglet Usage / Logs, coût par clé et par tag | Onglets Analytics / Logs sur SQLite ou Postgres ; groupement par modèle, fournisseur, utilisateur, groupe, agent client ; métriques Prometheus et traces OTel |
| Contenu des prompts | `turn_off_message_logging` | Non conservé par défaut ; `accessLog.database.llm: full` pour l'activer |
| SSO | OIDC générique, rôles et équipes LiteLLM créés à la connexion | Politique `oidc` sur la gateway qui sert l'interface, règle d'autorisation CEL ; **pas de rôles ni de vue par équipe** dans l'interface |
| Administration | Clé maître, comptes `proxy_admin` | API d'administration **sans authentification** sur `localhost:15000` : isolation réseau, jamais publiée |
| MCP gateway | Serveur déclaré par l'API, droits par `object_permission` d'équipe, outils préfixés `alias-` | Cible dans la section `mcp`, droits par règles CEL `mcpAuthorization` (filtrées de `tools/list`), outils préfixés `alias_` |

## Préparer, la veille

1. Récupérer les images et les figer : `docker compose pull`, puis noter les tags dans la
   fiche de versions (`cr.agentgateway.dev/agentgateway:v1.5.0`, `envoyproxy/ratelimit`,
   `redis:7-alpine`, `quay.io/keycloak/keycloak:26.3`). Si le réseau de la salle est
   incertain, embarquer les images (`docker save … | gzip`).
2. `cp .env.example .env` ; coller la clé OpenCode Zen dans `OPENCODE_API_KEY` (console
   Zen : opencode.ai/zen, facturation, clé API) ; générer `OIDC_COOKIE_SECRET`
   (`openssl rand -hex 32`, exactement 64 caractères hexadécimaux), `KEYCLOAK_CLIENT_SECRET`
   (`openssl rand -hex 24`) et `KEYCLOAK_ADMIN_PASSWORD` ; mettre votre `id -u` et `id -g`
   dans `DEMO_UID` et `DEMO_GID` (le conteneur écrit la base SQLite dans `./data`).
   Laisser les `CLE_*` vides : le script les remplit. Vérifier que la clé Zen répond :

   ```bash
   curl -sS https://opencode.ai/zen/v1/chat/completions \
     -H "Authorization: Bearer $OPENCODE_API_KEY" -H 'Content-Type: application/json' \
     -d '{"model":"glm-5.3-flash","max_tokens":10,"messages":[{"role":"user","content":"ok"}]}'
   ```

3. `cp config.example.yaml config.yaml` ; garder ou changer les modèles Zen derrière les
   trois alias (voir « Fournisseur : OpenCode Zen »). Les modèles de la démo sont ceux de
   Zen, pas ceux que le client mettra à disposition pour les TP : c'est sans importance,
   la démo montre que le même code traverse la gateway quel que soit le modèle derrière
   l'alias, et c'est justement l'argument. Si vous changez un modèle, mettez à jour son
   prix dans `costs/zen.json`.
4. `docker compose up -d`. Keycloak met une à deux minutes à devenir sain ; agentgateway
   attend ce moment parce qu'il charge les clés de signature Keycloak au démarrage.
   Suivre avec `docker compose ps` puis `docker compose logs -f agentgateway` jusqu'à
   `serving UI at http://localhost:4001/ui` et `started bind bind="bind/4000"`.
5. Peupler et vérifier :

   ```bash
   cd formateur/demo-agentgateway
   ./scripts/preparer-demo.sh
   ```

   Le script génère une clé par équipe dans `.env` (`sk-agw-…`), recrée le conteneur
   pour qu'il les lise, écrit `cles-demo.txt`, vérifie que chaque clé obtient `200` sur
   `/v1/models` en ne voyant **que ses modèles autorisés**, qu'une clé inconnue reçoit
   `401`, puis **amorce** la clé `demo-budget-epuise` : agentgateway débite le budget
   après la réponse, une clé neuve passe donc son premier appel ; le script fait cet
   appel d'un token, attend que la dépense soit écrite et confirme le `429`. Il est
   **rejouable** : les clés existantes sont conservées ; `--regenerer` les remplace toutes
   (à faire après la session pour les révoquer). La dépense reste attachée au **nom** de
   la clé dans `data/agentgateway.db` : pour réarmer `demo-budget-epuise`, arrêter la
   gateway et supprimer `data/agentgateway.db*`, ce qui vide aussi le journal.
6. **Vérifier que la dépense est chiffrée.** C'est le point le plus fragile de cette
   variante. Après un appel, lire le journal et chercher le coût :

   ```bash
   curl -s -X POST http://127.0.0.1:15000/api/logs/search -H 'Content-Type: application/json' -d '{}' \
     | python3 -c 'import sys,json;[print(l["genAi"].get("providerName"), l["genAi"].get("requestModel"), l.get("usage"), l.get("cost")) for l in json.load(sys.stdin)["logs"][:5]]'
   curl -s http://127.0.0.1:15020/metrics | grep cost_catalog_lookups_total
   ```

   Le compteur `agentgateway_cost_catalog_lookups_total` doit être en `status="Exact"`.
   S'il est en `Missing`, la clé de recherche du catalogue n'est pas celle attendue :
   relevez `providerName` et `requestModel` tels qu'ils apparaissent dans le journal, et
   ajoutez cette combinaison dans `costs/zen.json` (le fichier duplique déjà les prix
   sous `custom`, sous le nom du fournisseur et sous les alias, faute d'avoir pu
   trancher sans exécution). Le catalogue est relu à chaud. Sans coût chiffré, le
   budget en dollars ne bloque jamais : le script le signale au second appel.
7. Interface : `http://localhost:4001/ui` passe par le SSO Keycloak (compte `formateur`),
   `http://127.0.0.1:15000/ui` est la copie sans authentification, réservée au poste.
   Vérifier Analytics (Group by : Group), Logs, LLM → Virtual API Keys (les cinq clés et
   leurs budgets), LLM → Costs (le catalogue), LLM → Client Setup (qui génère l'extrait
   OpenCode et Claude Code).
8. **Faire tourner un peu de trafic** pour que le journal ne soit pas vide le jour J :
   même recette qu'avec LiteLLM (juge du TP3 avec la clé `platform-team`, quelques appels
   par équipe produit, un appel à `modele-reserve` avec `platform-team`). Commandes dans
   « Commandes prêtes ». Ne lancez pas encore la boucle des quarante appels : ce refus
   se joue en direct.
9. MCP gateway : démarrer le serveur MCP du fil rouge sur le poste (`mvn quarkus:dev`
   dans un serveur généré à la répétition ou celui du TP2), relever son point d'accès
   (souvent `http://localhost:8080/mcp`), l'ajuster si besoin dans `config.yaml`
   (`mcp.targets[0].mcp.host`, vu du conteneur sous `host.docker.internal`), puis
   jouer les trois appels JSON-RPC de « Commandes prêtes ». Contrairement à LiteLLM, rien à
   déclarer par l'API : la cible et la règle de droits sont déjà dans le fichier.
10. **Répéter la démo une fois en entier, chronomètre en main**, et enregistrer une
    capture vidéo de secours.

## Déroulé (20 min)

| Temps | Écran | Ce que vous montrez, ce que vous dites |
| --- | --- | --- |
| 1 min | Diapo « Une gateway de modèles » | Le retour d'expérience en deux phrases. « Tout ce que vous avez appelé depuis hier aurait pu passer par là. » |
| 3 min | Éditeur sur `config.yaml`, puis `/ui` → Virtual API Keys | **La politique est un fichier.** Trois alias ; Zen et sa clé n'apparaissent que comme `$OPENCODE_API_KEY`. Cinq clés virtuelles, chacune avec son groupe, ses modèles autorisés, son budget en dollars sur 30 jours. « Qui a fixé 5 $ et 30 requêtes par minute ? Moi, hier soir, dans ce fichier, et vous pourriez le relire en merge request comme le Skill de demain. » |
| 5 min | Terminal | **Le fil rouge traverse la gateway.** (1) Le juge du TP3 : même commande Maven qu'au TP3, seules les variables changent. (2) OpenCode en mode non interactif avec la clé `equipe-produit-a`. Puis `/ui` → Logs : deux appels, deux groupes, un coût chacun. Le code n'a pas changé, une URL et un alias ont été fournis. |
| 4 min | Terminal | **Trois refus.** `modele-reserve` avec la clé produit → `403 model_not_allowed`, avant tout appel au fournisseur. Clé `demo-budget-epuise` → `429 budget_exceeded`, et `/api/budgets/status` montre le dépensé et le plafond. Boucle de 40 appels → `429` à partir du 31ᵉ : le compteur vit dans Redis, la règle dans `ratelimit-config/`. À chaque fois : où le contrôle s'est appliqué, qui a fixé la valeur. |
| 3 min | `/ui` → Analytics, Logs | **Le journal.** Group by → Group, Measure → Cost : « le juge du TP3 a coûté combien ? ». Un appel : modèle, fournisseur, tokens, coût, groupe, agent client (`user_agent`), mais pas le contenu. « Bon réglage pour vos données réelles ? Qui décide de la rétention ? » |
| 2 min | Navigation privée → `http://localhost:4001/ui` | **SSO par Keycloak.** `formateur` / `formateur` entre : son jeton porte le groupe `platform-team`. `alice` / `alice` s'authentifie puis reçoit `403` : la règle CEL `"platform-team" in jwt.groups` la refuse. « L'identité vient de votre annuaire ; le droit d'entrer, c'est une ligne de ce fichier. Ici l'interface est un outil de plateforme, pas un portail par équipe : c'est un choix, pas une limite technique. » |
| 1 min | `config.yaml`, section `mcp` | **La gateway devant un serveur MCP.** Une cible `catalogue` vers le serveur des participants, une règle : seule `platform-team` voit ses outils. Même fichier, mêmes clés que pour les modèles. |
| 2 min | Terminal | **Trois appels JSON-RPC** sur `http://localhost:4000/mcp`. `tools/list` avec `platform-team` : `catalogue_find_service`, `catalogue_get_owner`… « Le préfixe, c'est la gateway qui range plusieurs serveurs dans un même espace. » `tools/call` de `catalogue_find_service` avec `query = "auth"`. `tools/list` avec `equipe-produit-b` : liste vide, code 200. « Pas de refus, pas d'erreur : l'outil n'existe pas pour cette équipe. » |
| 1 min | Diapo « Sécuriser les accès MCP » | Réglé : qui voit quel serveur et quel outil, avec la même identité que pour les modèles. Pas réglé : un client qui joint `localhost:8080` directement contourne tout ; le serveur reste responsable de ses contrôles. Montrer le `curl` direct si le temps le permet. |
| 1 min | Diapo « Ce que la gateway rend gouvernable » | Fermer : le mécanisme est là, et cette fois il est **dans un fichier** ; la politique reste à écrire, à relire, à décider. Enchaîner sur la trace fictive. |

## Fournisseur : OpenCode Zen

Zen expose trois formats d'API selon le modèle. agentgateway n'a pas de fournisseur Zen
natif : il est déclaré deux fois comme fournisseur **`custom`**, une fois par format,
et les alias y font référence (`provider.reference`). agentgateway convertit à la
volée une requête `chat/completions` vers le format `messages` quand le modèle visé ne
parle que celui-là : c'est le cas de `modele-reserve`.

| Famille Zen | Endpoint Zen | Déclaration dans `config.yaml` | Alias |
| --- | --- | --- | --- |
| GLM, MiniMax, DeepSeek V4.1 Flash | `/zen/v1/chat/completions` | provider `zen-chat` : `custom.formats: [{type: completions, path: /zen/v1/chat/completions}]` | `modele-generation` → `glm-5.3`, `modele-juge` → `glm-5.3-flash` |
| Claude, Qwen (sauf Qwen Max) | `/zen/v1/messages` (format Anthropic) | provider `zen-messages` : `custom.formats: [{type: messages, path: /zen/v1/messages}]` | `modele-reserve` → `claude-opus-5-5` |
| GPT, Grok, Muse | `/zen/v1/responses` | Non retenu ; possible avec `type: responses` si vous y tenez | — |

Les remarques de `demo-litellm` valent ici : la doc Zen et son API divergent (tester
chaque alias par un appel réel la veille), les prix sont ceux que **vous** avez déclarés
dans `costs/zen.json`, le modèle réservé est le plus cher, et votre OpenCode personnel
parle à Zen en direct alors que celui de la démo parle à la gateway : ne mélangez pas
les deux configurations.

## SSO avec Keycloak (local, dans le même compose)

Le compose démarre le même Keycloak que `demo-litellm` (mode `start-dev`, port `8180`
côté navigateur) et importe `keycloak/realm-formation-ia.json` au premier démarrage. Le
realm contient un client supplémentaire, `agentgateway-ui`, avec l'URL de retour
`http://localhost:4001/oauth/callback` et le mapper `groups`. agentgateway le consomme
par une politique `oidc` posée sur la gateway `ui-gateway` qui sert l'interface :

| Champ `config.yaml` | Valeur | Pourquoi |
| --- | --- | --- |
| `issuer` | `http://localhost:8180/realms/formation-ia` | Doit égaler le `iss` des jetons, fixé par `KC_HOSTNAME` sur l'URL vue du navigateur |
| `authorizationEndpoint` | `http://localhost:8180/…/auth` | Suivi par **le navigateur** |
| `tokenEndpoint`, `jwks.url` | `http://keycloak:8080/…/token`, `…/certs` | Appelés par **le conteneur**, donc le nom du service compose. `discovery` doit rester absent quand ces trois points sont explicites |
| `clientSecret` | `$KEYCLOAK_CLIENT_SECRET` | Une seule source, `.env`, injectée aussi dans le realm à l'import |
| `authorization.rules` | `require: '"platform-team" in jwt.groups'` | Qui entre : une ligne, relisible |
| `OIDC_COOKIE_SECRET` (environnement) | 64 caractères hexadécimaux | Chiffre le cookie de session ; agentgateway refuse de démarrer sans |

**Mise en place, la veille :** fixer `KEYCLOAK_CLIENT_SECRET` **avant le premier démarrage**
(le realm n'est importé qu'une fois ; sinon `docker compose rm -sf keycloak` puis `up -d`),
vérifier la console `http://localhost:8180/admin`, puis tester en navigation privée :
`formateur` entre, `alice` reçoit `403`. Pour changer d'utilisateur, une fenêtre privée par
utilisateur est le plus simple : la session Keycloak survit à la fermeture de celle de la
gateway.

**Si la connexion échoue :** `docker compose logs agentgateway`. `failed to load oidc jwks`
au démarrage → Keycloak n'était pas prêt ou `keycloak:8080` injoignable (le conteneur
redémarre seul grâce à `restart: unless-stopped`) ; `Invalid redirect_uri` côté Keycloak →
l'URL de retour du client et `redirectURI` diffèrent ; `unauthorized_client` → le secret
connu de Keycloak n'est pas celui de `.env` (realm importé avant que `.env` soit rempli) ;
`iss` refusé → `KC_HOSTNAME` n'est pas `http://localhost:8180`. Pour démarrer sans SSO le
temps de diagnostiquer, commentez le bloc `ui.policies` : l'interface sur 4001 devient
alors ouverte, comme celle de 15000.

Ce qu'il faut dire en salle : Keycloak joue le rôle de l'annuaire du client. L'identité
vient de l'annuaire, le groupe aussi ; **le droit d'entrer est une règle écrite dans la
configuration de la gateway**, et il n'y a ici ni rôle ni vue par équipe : l'interface
est celle de la platform team. Si l'organisation veut un portail par équipe, c'est un
produit à construire au-dessus du journal, pas un réglage.

## Commandes prêtes

À lancer depuis `formateur/demo-agentgateway`, dans un terminal préparé une fois pour
toutes : les clés viennent de `cles-demo.txt`, le dépôt est retrouvé par git. La commande
OpenCode se lance dans un répertoire temporaire pour ne pas toucher à votre
`opencode.json` habituel. Le serveur à juger est celui de votre répétition
(`$ATELIER_DIR/serveur-avec-skill`) ; à défaut, le projet piège du dossier formateur suffit.

```bash
cd formateur/demo-agentgateway
export GATEWAY=http://localhost:4000
export ADMIN=http://127.0.0.1:15000
export CLE_PLATFORM=$(awk '$1=="platform-team"{print $2}' cles-demo.txt)
export CLE_PRODUIT=$(awk '$1=="equipe-produit-a"{print $2}' cles-demo.txt)
export CLE_PRODUIT_B=$(awk '$1=="equipe-produit-b"{print $2}' cles-demo.txt)
export CLE_DEMO=$(awk '$1=="demo-budget-epuise"{print $2}' cles-demo.txt)
export FORMATION_REPO=$(git rev-parse --show-toplevel)
export SERVEUR_A_JUGER="${ATELIER_DIR:-$FORMATION_REPO/formateur/demo-scorer-trompeur}/${ATELIER_DIR:+serveur-avec-skill}${ATELIER_DIR:-projet-piege}"
echo "clés : ${#CLE_PLATFORM}/${#CLE_PRODUIT}/${#CLE_DEMO} caractères — serveur jugé : $SERVEUR_A_JUGER"
```

```bash
# Ce que chaque clé voit : /v1/models est filtré par allowedModels
curl -sS "$GATEWAY/v1/models" -H "Authorization: Bearer $CLE_PRODUIT" | python3 -m json.tool

# Le juge du TP3 à travers la gateway (code inchangé)
LLM_ENDPOINT="$GATEWAY/v1/chat/completions" LLM_MODEL=modele-juge LLM_API_KEY="$CLE_PLATFORM" \
  mvn -q -f "$FORMATION_REPO/tp3-eval/pom.xml" -Dtest=JugeTest \
  -Dserveur.genere.dir="$SERVEUR_A_JUGER" test

# OpenCode en mode non interactif, à travers la gateway, avec la clé de l'équipe produit A
D=$(mktemp -d) && sed 's#"_note": "[^"]*",##' opencode.gateway.example.json > "$D/opencode.json"
( cd "$D" && AGW_KEY="$CLE_PRODUIT" opencode run --model gateway-demo/modele-generation \
    "Réponds en une phrase : qu'est-ce que MCP ?" )

# Un appel simple, réponse complète (usage : tokens)
curl -sS "$GATEWAY/v1/chat/completions" -H "Authorization: Bearer $CLE_PRODUIT" \
  -H 'Content-Type: application/json' \
  -d '{"model":"modele-generation","max_tokens":40,"messages":[{"role":"user","content":"Réponds : prêt."}]}'

# Le dernier appel dans le journal, avec son coût et son groupe
curl -s -X POST "$ADMIN/api/logs/search" -H 'Content-Type: application/json' -d '{}' \
  | python3 -c 'import sys,json;l=json.load(sys.stdin)["logs"][0];print(json.dumps(l,indent=1,ensure_ascii=False))'

# Modèle non autorisé pour l'équipe (403 model_not_allowed, le fournisseur n'est pas appelé)
curl -sS -w '\n%{http_code}\n' "$GATEWAY/v1/chat/completions" -H "Authorization: Bearer $CLE_PRODUIT" \
  -H 'Content-Type: application/json' \
  -d '{"model":"modele-reserve","messages":[{"role":"user","content":"bonjour"}]}'

# Budget épuisé (clé au budget dérisoire, déjà amorcée par le script) et l'état du budget côté gateway
curl -sS -w '\n%{http_code}\n' "$GATEWAY/v1/chat/completions" -H "Authorization: Bearer $CLE_DEMO" \
  -H 'Content-Type: application/json' \
  -d '{"model":"modele-juge","messages":[{"role":"user","content":"bonjour"}]}'
curl -s "$ADMIN/api/budgets/status?apiKeyName=demo-budget-epuise" | python3 -m json.tool

# Limite de débit (30 requêtes par minute pour equipe-produit-a → des 429 apparaissent)
for i in $(seq 1 40); do
  curl -sS -o /dev/null -w '%{http_code} ' "$GATEWAY/v1/chat/completions" -H "Authorization: Bearer $CLE_PRODUIT" \
    -H 'Content-Type: application/json' \
    -d '{"model":"modele-generation","max_tokens":5,"messages":[{"role":"user","content":"ok"}]}'
done; echo
docker compose logs --tail 5 ratelimit     # le descripteur team=equipe-produit-a et la décision OVER_LIMIT

# Métriques : tokens par groupe, et l'état des recherches dans le catalogue de prix
curl -s http://127.0.0.1:15020/metrics | grep -E 'gen_ai_client_token_usage_sum|cost_catalog_lookups_total'

# ---- MCP gateway : le serveur Quarkus du fil rouge doit tourner sur le poste (cible « catalogue » dans config.yaml)
MCP_HDR=(-H 'Content-Type: application/json' -H 'Accept: application/json, text/event-stream')

# tools/list avec platform-team : les primitives du TP, préfixées « catalogue_ »
curl -sS -X POST "$GATEWAY/mcp" -H "Authorization: Bearer $CLE_PLATFORM" "${MCP_HDR[@]}" \
  -d '{"jsonrpc":"2.0","id":1,"method":"tools/list"}'

# tools/call de find_service à travers la gateway
curl -sS -X POST "$GATEWAY/mcp" -H "Authorization: Bearer $CLE_PLATFORM" "${MCP_HDR[@]}" \
  -d '{"jsonrpc":"2.0","id":2,"method":"tools/call","params":{"name":"catalogue_find_service","arguments":{"query":"auth"}}}'

# tools/list avec equipe-produit-b : liste vide (200), pas de refus
curl -sS -X POST "$GATEWAY/mcp" -H "Authorization: Bearer $CLE_PRODUIT_B" "${MCP_HDR[@]}" \
  -d '{"jsonrpc":"2.0","id":3,"method":"tools/list"}'

# Le contournement : le serveur Quarkus en direct, sans clé, sans journal
curl -sS -X POST http://localhost:8080/mcp "${MCP_HDR[@]}" -d '{"jsonrpc":"2.0","id":4,"method":"tools/list"}'
```

Si un client MCP exige une session (`initialize` avant `tools/list`), utilisez le
Tool Playground de l'interface (MCP → Tool Playground → Initialize) ou l'Inspector MCP
pointé sur `http://localhost:4000/mcp` avec la clé en en-tête `Authorization`.

## Dépannage

| Symptôme | Vérification |
| --- | --- |
| `error looking key '…' up: environment variable not found` au démarrage | Un `$NOM` non défini dans `config.yaml`, **commentaires compris** : agentgateway développe toutes les variables du fichier. Le script remplit les `CLE_*` ; `OPENCODE_API_KEY` et `KEYCLOAK_CLIENT_SECRET` viennent de `.env` |
| `failed to load oidc jwks` puis arrêt | Keycloak pas encore prêt ou secret absent ; le conteneur redémarre seul. Sinon commenter `ui.policies` |
| `failed to initialize request log database` | `./data` absent ou non inscriptible : `mkdir data`, `DEMO_UID`/`DEMO_GID` égaux à `id -u`/`id -g` |
| Le refus budget ne vient jamais, dépense à 0 | Le catalogue ne chiffre pas ce fournisseur ou ce modèle : `cost_catalog_lookups_total{status="Missing"}` ; ajouter la combinaison `providerName`/`requestModel` du journal dans `costs/zen.json` |
| `500` sur tous les appels LLM | Service `ratelimit` indisponible et `failureMode: failClosed` : `docker compose logs ratelimit` (domaine ou clé de descripteur différents de `config.yaml`) |
| Aucun `429` sur la boucle de 40 | Le descripteur `team` n'arrive pas (`LOG_LEVEL: debug` du service `ratelimit` le montre) ; `metadata.group` de la clé ; `domain` identique des deux côtés |
| `modele-reserve` répond `4xx` du fournisseur pour `platform-team` | Conversion `completions` → `messages` ou identifiant de modèle Zen : tester `/v1/messages` directement sur la gateway, puis `curl` Zen en direct |
| `tools/list` vide pour `platform-team` | Serveur Quarkus arrêté ou point d'accès différent ; `host.docker.internal` ; la variable `apiKey.group` dans la règle `mcpAuthorization` (voir ci-dessous) |
| `/ui` sur 4001 boucle ou refuse | Voir « SSO avec Keycloak » ; en attendant, `http://127.0.0.1:15000/ui` |

## À confirmer lors de la répétition

Ces points viennent de la documentation, pas d'une exécution. Les vérifier dans cet ordre,
la veille, et corriger `config.yaml` ou ce README en conséquence :

1. **Clé de recherche du catalogue de prix** pour un fournisseur `custom` : `providerName`
   et `requestModel` tels qu'ils apparaissent dans `/api/logs/search`, puis `status="Exact"`
   sur `cost_catalog_lookups_total`. Sans cela, ni coût au journal ni refus budget.
2. **Débit du budget sur des clés lues dans l'environnement** : `/api/budgets/status`
   doit montrer la dépense de `demo-budget-epuise` après l'amorçage du script.
3. **`apiKey.group` dans une règle `mcpAuthorization`** : la documentation montre des règles
   sur `jwt.*` ; si `apiKey.*` n'y est pas disponible, remplacer par une règle sur le
   nom du client, ou passer les clés MCP en `jwtAuth`. Vérifier que `tools/list` est bien
   vide, et non en erreur, pour `equipe-produit-b`.
4. **Conversion `chat/completions` → `messages`** pour `modele-reserve` (Claude via Zen), et
   la validité des identifiants de modèles Zen du jour.
5. **SSO** : `formateur` entre, `alice` reçoit `403`, avec les points d'accès mixtes
   (`localhost:8180` pour le navigateur, `keycloak:8080` pour le conteneur).
6. **Débit par équipe** : `OVER_LIMIT` dans les journaux du service `ratelimit` à partir du
   31ᵉ appel de la minute pour `equipe-produit-a`.
7. **OpenCode** : le fournisseur `@ai-sdk/openai-compatible` envoie bien `Authorization:
   Bearer` avec la clé virtuelle ; l'interface LLM → Client Setup génère la même chose.

Après la session : `./scripts/preparer-demo.sh --regenerer` révoque les clés distribuées
(les anciennes valeurs ne sont plus dans l'environnement du conteneur), et
`docker compose down` puis suppression de `data/` efface le journal.
