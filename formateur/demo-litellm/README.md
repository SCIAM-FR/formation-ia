# Démonstration M5 — une gateway de modèles avec LiteLLM (~20 min)

Une démonstration **sur le poste du formateur**, sans rien héberger chez le client :
LiteLLM tourne en local dans Docker, avec **OpenCode Zen** comme fournisseur de
modèles (la clé Zen du formateur), et c'est le **fil rouge de la formation** qui
passe à travers — le juge du TP3, une
génération OpenCode, le serveur MCP généré au TP2. Objectif : rendre concrètes,
sur un cas que les participants connaissent, quatre responsabilités de la diapo M0
« Les responsabilités qui restent à l'organisation » : identité des appels, coût et
routage, règles d'accès, audit.

Retour d'expérience à partager : chez un grand client, la gateway de modèles a été la
première brique de plateforme IA qui a tenu, parce qu'elle répond à la question posée
en premier par la direction : **qui dépense quoi, sur quel modèle, et qui l'a décidé ?**

## Ce qui est dans ce dossier

| Fichier | Rôle |
| --- | --- |
| `docker-compose.yml` | LiteLLM + Postgres sur le poste formateur. Environnement de démonstration, pas de production. |
| `.env.example` | Clés à générer et clé OpenCode Zen, à copier en `.env` (ignoré par git). |
| `litellm-config.example.yaml` | Alias `modele-generation`, `modele-juge`, `modele-reserve` branchés sur Zen avec leurs prix ; réglages ; déclaration du serveur MCP du fil rouge (commentée). À copier en `litellm-config.yaml` (ignoré par git). |
| `scripts/preparer-demo.sh` | Crée les équipes, les clés et la clé au budget minuscule utilisées pendant la démo. |
| `opencode.gateway.example.json` | Extrait de configuration OpenCode pointant vers la gateway locale. |
| `keycloak/realm-formation-ia.json` | Realm Keycloak importé au démarrage : client `litellm`, groupes = équipes LiteLLM, utilisateurs `formateur` (proxy_admin), `alice` (internal_user, équipe produit A), `bob` (lecture seule, équipe produit B). |

## Préparer, la veille

1. Récupérer l'image et la figer :
   `docker pull docker.litellm.ai/berriai/litellm:main-stable`, puis noter le tag
   ou le digest dans la fiche de versions. Si le réseau de la salle est incertain,
   embarquer l'image (`docker save … | gzip > litellm.tar.gz`) et Postgres.
2. `cp .env.example .env` ; générer `LITELLM_MASTER_KEY` et `LITELLM_SALT_KEY`
   (`openssl rand -hex 32`) ; coller la clé OpenCode Zen dans `OPENCODE_API_KEY`
   (console Zen : opencode.ai/zen, facturation, clé API). Vérifier qu'elle répond :

   ```bash
   curl -sS https://opencode.ai/zen/v1/chat/completions \
     -H "Authorization: Bearer $OPENCODE_API_KEY" -H 'Content-Type: application/json' \
     -d '{"model":"deepseek-v4-flash","max_tokens":10,"messages":[{"role":"user","content":"ok"}]}'
   ```

3. `cp litellm-config.example.yaml litellm-config.yaml` ; garder ou changer les
   modèles Zen derrière les trois alias (voir « Fournisseur : OpenCode Zen »).
   Les modèles de la démo sont ceux de Zen, pas ceux que le client mettra à
   disposition pour les TP : c'est sans importance, la démo montre que le même
   code (juge, OpenCode, serveur MCP) traverse la gateway quel que soit le modèle
   derrière l'alias — et c'est justement l'argument.
4. `docker compose up -d` ; `curl -fsS http://localhost:4000/health/liveliness`.
5. Interface : `http://localhost:4000/ui`, connexion avec `UI_USERNAME` /
   `UI_PASSWORD`, création d'un compte `proxy_admin` nominatif.
6. `LITELLM_MASTER_KEY=… ./scripts/preparer-demo.sh http://localhost:4000` :
   crée les équipes `equipe-produit-a`, `equipe-produit-b`, `platform-team`,
   `ci-usine`, une clé par équipe et la clé `demo-budget-epuise` (0,01 $).
   Les clés sont dans `cles-demo.txt` (ignoré par git).
7. **Faire tourner un peu de trafic** pour que le journal ne soit pas vide :
   le juge du TP3 sur une génération, quelques appels curl avec chaque équipe.
8. SSO : Keycloak démarre avec le compose et importe le realm ; voir la section
   « SSO avec Keycloak » ci-dessous pour le secret, la vérification et le scénario.
   Gratuit jusqu'à cinq utilisateurs depuis la version 1.76 de LiteLLM, licence
   Enterprise au-delà : trois comptes de démo suffisent.
9. MCP gateway : démarrer le serveur Quarkus du fil rouge (`mvn quarkus:dev`),
   décommenter `mcp_servers` dans `litellm-config.yaml` avec le point d'accès
   réel, redémarrer la gateway, vérifier un appel de `find_service` à travers
   `http://localhost:4000/mcp/catalogue`.
10. **Répéter la démo une fois en entier, chronomètre en main**, et enregistrer
    une capture vidéo de secours : si Docker ou le réseau lâche en salle, on
    projette la vidéo et on commente.

## Déroulé (20 min)

| Temps | Écran | Ce que vous montrez, ce que vous dites |
| --- | --- | --- |
| 1 min | Diapo « Une gateway de modèles » | Le retour d'expérience en deux phrases. « Tout ce que vous avez appelé depuis hier aurait pu passer par là. » |
| 3 min | `/ui` → Models, Teams, Keys | Trois alias ; Zen et sa clé n'apparaissent pas. Quatre équipes, chacune avec budget, limite de débit, modèles autorisés. « Qui a fixé 5 $ et 30 requêtes par minute ? Moi, hier soir. Chez vous, qui ? » |
| 5 min | Terminal | **Le fil rouge traverse la gateway.** Relancer le juge du TP3 sur une génération avec `LLM_ENDPOINT=http://localhost:4000/v1/chat/completions`, `LLM_MODEL=modele-juge`, la clé `platform-team` : le test passe, l'appel apparaît dans le journal avec son coût et son équipe. Puis un `opencode run` d'une phrase avec la clé `equipe-produit-a` : même chose. « Le code des TP n'a pas changé : une URL et un alias — et derrière l'alias, un modèle différent de celui de vos TP, sans que rien ne bouge côté code. » |
| 4 min | Terminal | **Trois refus.** `modele-reserve` avec une clé d'équipe produit → modèle non autorisé. Clé `demo-budget-epuise` → erreur `4xx` explicite, le fournisseur n'a pas été appelé. Boucle de 40 appels courts → `429`. À chaque fois : où le contrôle s'est appliqué, qui a fixé la valeur. |
| 3 min | `/ui` → Usage / Logs | Dépense par équipe et par tag : « le juge du TP3 a coûté combien ? ». Un appel journalisé montre modèle, coût, clé, équipe, mais pas le contenu (`turn_off_message_logging`). « Bon réglage pour vos données réelles ? Qui décide de la rétention ? » |
| 2 min | Navigation privée → `/ui` → « Login with SSO » | **SSO par Keycloak.** Connexion `alice` / `alice` : Keycloak authentifie, LiteLLM la crée à la volée avec le rôle `internal_user` (attribut `litellm_role`) et la rattache à `equipe-produit-a` (revendication `groups`). Elle ne voit que les clés et la dépense de son équipe, pas celles de `platform-team`. « L'identité vient de votre annuaire ; le droit de voir quoi, c'est encore une décision : l'attribut et le groupe, quelqu'un les a posés. » Prenez ces 2 min sur la colonne Tracer ou sur le MCP gateway si vous êtes en retard. |
| 3 min | `/ui` → MCP, puis terminal | Le serveur MCP du fil rouge déclaré derrière la gateway. `find_service` appelé à travers `/mcp/catalogue` avec la clé `platform-team` ; refus avec la clé `equipe-produit-b`. Pont vers la diapo « Sécuriser les accès MCP » : l'accès est réglé **par serveur**, pas par outil ; le serveur Quarkus garde ses propres contrôles. |
| 1 min | Diapo « Ce que la gateway rend gouvernable » | Fermer : le mécanisme est là ; la politique — budget, modèles, rétention, qui approuve — reste à écrire. Enchaîner sur le mini-exercice : la trace fictive. |

## Fournisseur : OpenCode Zen

Zen est le fournisseur de modèles d'OpenCode (opencode.ai/zen) : une clé, une
facturation, plusieurs familles de modèles. Il expose **trois formats d'API selon
le modèle**, et c'est ce qui dicte la configuration LiteLLM :

| Famille Zen | Endpoint Zen | Déclaration LiteLLM | Exemple dans la config |
| --- | --- | --- | --- |
| DeepSeek, GLM, Kimi, MiniMax, modèles gratuits | `/zen/v1/chat/completions` | `model: openai/<id>`, `api_base: https://opencode.ai/zen/v1` | `modele-generation` → `deepseek-v4-pro`, `modele-juge` → `deepseek-v4-flash` |
| Claude, Qwen (sauf Qwen Max) | `/zen/v1/messages` (format Anthropic) | `model: anthropic/<id>`, `api_base: https://opencode.ai/zen` — LiteLLM ajoute `/v1/messages` | `modele-reserve` → `claude-opus-5-5` |
| GPT, Grok, Muse | `/zen/v1/responses` (Responses API) | Non retenu pour la démo : à tester avec la route Responses de LiteLLM si vous y tenez | — |

Trois choix à assumer devant la salle :

- **Les prix sont renseignés à la main** dans `model_info` (`input_cost_per_token`,
  `output_cost_per_token`) : LiteLLM ne connaît pas la grille Zen, et sans cela
  le journal des dépenses afficherait zéro. Relevés le 25 septembre 2026 ;
  à revérifier sur opencode.ai/docs/zen avant la démo. C'est un bon point à dire :
  « la dépense affichée est celle que vous avez déclarée à la gateway ».
- **Le modèle réservé est le plus cher** (Claude Opus 5.5, 4 $ / 20 $ par million)
  et il n'est ouvert qu'au groupe `direction`, donc à `platform-team` : le refus de
  la démo a un sens économique, pas seulement technique.
- **Les modèles de la démo ne sont pas ceux des TP**, et ce n'est pas grave : le
  fournisseur retenu par le client pour les TP n'est pas connu à l'avance, et la
  démo prouve précisément qu'un alias isole le code du modèle. Dites-le en salle
  plutôt que de le cacher.
- **Votre OpenCode parle à Zen en direct** via `/connect` (fournisseur `opencode`)
  si vous l'utilisez ainsi pendant les TP. Pour la démo, il parle à la gateway, qui
  parle à Zen :
  `opencode.gateway.example.json`, avec la clé virtuelle d'une équipe dans
  `LITELLM_KEY`. Ne mélangez pas les deux dans le même `opencode.json` sous
  peine de ne plus savoir quel appel est passé par où.

La clé Zen reste dans `.env` sur votre poste ; la salle ne voit que les alias.
`curl -sS https://opencode.ai/zen/v1/models -H "Authorization: Bearer $OPENCODE_API_KEY"`
liste ce que Zen propose si vous voulez changer un modèle.

## SSO avec Keycloak (local, dans le même compose)

Le compose démarre un Keycloak de démonstration (`quay.io/keycloak/keycloak`, mode
`start-dev`, port `8180` côté navigateur) qui importe `keycloak/realm-formation-ia.json`
au premier démarrage. LiteLLM le consomme comme fournisseur **OIDC générique** :

| Variable LiteLLM | Valeur dans le compose | Pourquoi |
| --- | --- | --- |
| `PROXY_BASE_URL` | `http://localhost:4000` | Construit l'URL de retour `http://localhost:4000/sso/callback`, déclarée dans le client Keycloak |
| `GENERIC_AUTHORIZATION_ENDPOINT` | `http://localhost:8180/realms/formation-ia/protocol/openid-connect/auth` | Appelée par **le navigateur**, donc `localhost:8180` |
| `GENERIC_TOKEN_ENDPOINT`, `GENERIC_USERINFO_ENDPOINT` | `http://keycloak:8080/realms/formation-ia/protocol/openid-connect/…` | Appelées par **le conteneur LiteLLM**, donc le nom du service compose |
| `GENERIC_USER_ID_ATTRIBUTE` | `preferred_username` | Identifiant stable de l'utilisateur LiteLLM |
| `GENERIC_USER_ROLE_ATTRIBUTE` | `litellm_role` | Revendication ajoutée par le mapper Keycloak depuis l'attribut utilisateur du même nom : `proxy_admin`, `internal_user`, `internal_user_viewer` |
| `litellm_jwtauth.team_ids_jwt_field: groups` (config YAML) | revendication `groups` | À chaque connexion, LiteLLM ajoute l'utilisateur aux équipes dont le `team_id` égale un nom de groupe ; le script crée les équipes avec `team_id` = nom du groupe |

**Mise en place, la veille :**

1. Générer un secret (`openssl rand -hex 24`), le mettre dans `.env`
   (`KEYCLOAK_CLIENT_SECRET`) **et** dans le champ `secret` du client `litellm` de
   `keycloak/realm-formation-ia.json`. Renseigner `KEYCLOAK_ADMIN_PASSWORD`.
2. `docker compose up -d` ; attendre `http://localhost:8180/realms/formation-ia`
   (le premier démarrage de Keycloak prend une à deux minutes).
3. Console Keycloak : `http://localhost:8180/admin`, compte `admin`. Vérifier le
   realm `formation-ia`, le client `litellm` (URL de redirection
   `http://localhost:4000/sso/callback`) et les trois utilisateurs. Changer les mots
   de passe de démo si la salle est filmée.
4. Lancer `scripts/preparer-demo.sh` **après** Keycloak : les équipes portent le
   `team_id` attendu par la revendication `groups`.
5. Tester en navigation privée : `http://localhost:4000/ui` → « Login with SSO »
   → `formateur` / `formateur` doit arriver `proxy_admin` ; `alice` / `alice` doit
   arriver `internal_user` dans `equipe-produit-a`. Vérifier dans Internal Users.
6. Garder un compte `proxy_admin` local (étape 5 de la préparation) tant que le SSO
   n'est pas prouvé ; `disable_env_credential_login` seulement après.

Le realm importé n'est **pas** réimporté au redémarrage si la base Keycloak existe
déjà (mode `start-dev`, stockage éphémère dans le conteneur) : pour repartir de zéro,
`docker compose rm -sf keycloak` puis `up -d`.

**Si la connexion échoue** : « Invalid redirect_uri » → l'URL de retour dans le
client Keycloak et `PROXY_BASE_URL` ne correspondent pas ; erreur au moment de
l'échange du code → le conteneur LiteLLM ne joint pas `keycloak:8080` (réseau
compose) ; utilisateur créé sans rôle → le mapper `litellm_role` n'est pas dans la
réponse userinfo (vérifier dans Keycloak : Clients → litellm → Client scopes →
Evaluate → User info) ; utilisateur sans équipe → `team_ids_jwt_field` absent de la
configuration ou `team_id` différent du nom de groupe. Si l'échange de jeton refuse
l'émetteur (`issuer`) à cause des deux hôtes (`localhost` côté navigateur,
`keycloak` côté conteneur), ajoutez `127.0.0.1 keycloak` dans `/etc/hosts` du poste,
publiez Keycloak en `8080:8080` et utilisez `http://keycloak:8080/…` pour les trois
endpoints.

Ce qu'il faut dire en salle : Keycloak joue ici le rôle de l'annuaire du client
(Entra, Okta, Keycloak d'entreprise…). Ce que la démo montre, c'est que l'identité
vient de l'annuaire, mais que le **rôle** et l'**équipe** sont posés par quelqu'un :
un attribut, un groupe. La gateway les lit ; elle ne les décide pas.

## Commandes prêtes

```bash
export GATEWAY=http://localhost:4000
export CLE_PLATFORM=sk-…      # equipe platform-team
export CLE_PRODUIT=sk-…       # equipe equipe-produit-a
export CLE_DEMO=sk-…          # demo-budget-epuise

# Le juge du TP3 à travers la gateway (code inchangé)
LLM_ENDPOINT="$GATEWAY/v1/chat/completions" LLM_MODEL=modele-juge LLM_API_KEY="$CLE_PLATFORM" \
  mvn -q -f "$FORMATION_REPO/tp3-eval/pom.xml" -Dtest=ConformiteJugeTest \
  -Dserveur.genere.dir="$ATELIER_DIR/serveur-avec-skill" test

# Un appel simple, en-têtes visibles
curl -sS -D - "$GATEWAY/v1/chat/completions" -H "Authorization: Bearer $CLE_PRODUIT" \
  -H 'Content-Type: application/json' \
  -d '{"model":"modele-generation","messages":[{"role":"user","content":"Réponds : prêt."}]}'

# Modèle non autorisé pour l'équipe
curl -sS -w '\n%{http_code}\n' "$GATEWAY/v1/chat/completions" -H "Authorization: Bearer $CLE_PRODUIT" \
  -H 'Content-Type: application/json' \
  -d '{"model":"modele-reserve","messages":[{"role":"user","content":"bonjour"}]}'

# Budget épuisé
curl -sS -w '\n%{http_code}\n' "$GATEWAY/v1/chat/completions" -H "Authorization: Bearer $CLE_DEMO" \
  -H 'Content-Type: application/json' \
  -d '{"model":"modele-generation","messages":[{"role":"user","content":"bonjour"}]}'

# Limite de débit
for i in $(seq 1 40); do
  curl -sS -o /dev/null -w '%{http_code} ' "$GATEWAY/v1/chat/completions" -H "Authorization: Bearer $CLE_PRODUIT" \
    -H 'Content-Type: application/json' \
    -d '{"model":"modele-generation","max_tokens":5,"messages":[{"role":"user","content":"ok"}]}'
done; echo

# Dépense d'une équipe (clé maître)
curl -sS "$GATEWAY/team/info?team_id=<id>" -H "Authorization: Bearer $LITELLM_MASTER_KEY"
```

Codes d'erreur : `429` pour le débit ; `400` ou `422` pour le budget selon la
version. Vérifiez sur l'image figée et adaptez ce que vous annoncez.

## Ce que la démonstration ne doit pas laisser croire

- Que la gateway **sécurise** les serveurs MCP : elle gouverne l'accès aux modèles
  et, via le MCP gateway, l'accès à un serveur MCP **entier**. L'autorisation par
  outil et la protection du SI restent au serveur.
- Que le quota **remplace** l'évaluation : un appel bon marché peut être non conforme.
- Que le SSO est acquis : le Keycloak de la démo est un jouet local ; licence LiteLLM
  au-delà de cinq utilisateurs et raccordement à l'annuaire réel sont des décisions
  du client.
- Que l'installation est faite : ici, c'est un poste de formateur. Chez le client,
  hébergement, haute disponibilité, secrets, sauvegarde de Postgres et TLS sont à
  traiter ; c'est précisément une ligne de la fiche de sortie de M7.
- Que les chiffres et les onglets sont définitifs : ils varient selon la version.
