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
     -d '{"model":"glm-5.3-flash","max_tokens":10,"messages":[{"role":"user","content":"ok"}]}'
   ```

3. `cp litellm-config.example.yaml litellm-config.yaml` ; garder ou changer les
   modèles Zen derrière les trois alias (voir « Fournisseur : OpenCode Zen »).
   Les modèles de la démo sont ceux de Zen, pas ceux que le client mettra à
   disposition pour les TP : c'est sans importance, la démo montre que le même
   code (juge, OpenCode, serveur MCP) traverse la gateway quel que soit le modèle
   derrière l'alias — et c'est justement l'argument.
4. `docker compose up -d` ; `curl -fsS http://localhost:4000/health/liveliness`.
5. Interface : `http://localhost:4000/ui`, première connexion avec `UI_USERNAME` /
   `UI_PASSWORD` (ce compte générique n'est que la clé maître déguisée). Créer
   ensuite un administrateur à votre nom : Internal Users → Invite User, votre
   adresse, rôle `proxy_admin`. Les actions dans l'interface seront attribuées à une
   personne, pas à « admin » — c'est l'argument de la démo. Avec le SSO Keycloak,
   le compte `formateur` du realm joue ce rôle ; ce compte local reste un filet de
   sécurité tant que le SSO n'est pas prouvé. Ne désactivez la connexion par
   variables (`disable_env_credential_login: true`) qu'une fois l'un des deux vérifié.
6. Peupler la gateway :

   ```bash
   cd formateur/demo-litellm
   ./scripts/preparer-demo.sh
   ```

   Le script lit la clé maître dans `.env` (ou dans `LITELLM_MASTER_KEY` si elle
   est déjà exportée), vise `http://localhost:4000` par défaut — passez une autre
   URL en argument si besoin — et s'arrête avec un message clair si la clé manque
   ou si la gateway ne répond pas.

   Le script crée les équipes `equipe-produit-a`, `equipe-produit-b`,
   `platform-team`, `ci-usine`, une clé par équipe et la clé `demo-budget-epuise`,
   dont le budget est dérisoire (un millionième de dollar) et que le script
   **amorce** par un appel d'un token : LiteLLM compare le budget à la dépense déjà
   enregistrée, une clé neuve passerait donc son premier appel. Le script attend
   quelques secondes que la dépense soit écrite et confirme le `429`. Les clés sont
   écrites dans `cles-demo.txt` (ignoré par git). Il est
   **rejouable** : une équipe déjà présente est conservée, une clé dont l'alias
   existe est supprimée puis régénérée — LiteLLM ne restitue jamais la valeur d'une
   clé après sa création, c'est donc le seul moyen de la retrouver si le fichier
   est perdu. Relancer le script juste avant la démo régénère et ré-amorce aussi la
   clé `demo-budget-epuise`. Une erreur de la gateway s'affiche avec son message
   (`Erreur 400 sur POST /team/new : …`) et arrête le script sans écrire le fichier.
7. **Faire tourner un peu de trafic** pour que le journal ne soit pas vide le
   jour J : l'onglet Usage doit montrer plusieurs équipes, plusieurs modèles et
   un coût qui se compare. Récupérez d'abord les clés écrites par le script :

   ```bash
   cd formateur/demo-litellm
   export CLE_PLATFORM=$(awk '$1=="platform-team"{print $2}' cles-demo.txt)
   export CLE_PRODUIT_A=$(awk '$1=="equipe-produit-a"{print $2}' cles-demo.txt)
   export CLE_PRODUIT_B=$(awk '$1=="equipe-produit-b"{print $2}' cles-demo.txt)
   export CLE_CI=$(awk '$1=="ci-usine"{print $2}' cles-demo.txt)
   export GATEWAY=http://localhost:4000
   ```

   Puis générez trois sortes de trafic, chacune visible différemment au journal :

   - **Le juge du TP3, avec la clé `platform-team`**, sur un serveur généré lors de
     votre répétition (ou, à défaut, sur `formateur/demo-scorer-trompeur/projet-piege`,
     dont les sources suffisent au juge) :

     ```bash
     LLM_ENDPOINT="$GATEWAY/v1/chat/completions" LLM_MODEL=modele-juge LLM_API_KEY="$CLE_PLATFORM" \
       mvn -q -f "$FORMATION_REPO/tp3-eval/pom.xml" -Dtest=JugeTest \
       -Dserveur.genere.dir="$ATELIER_DIR/serveur-avec-skill" test
     ```

     C'est l'appel le plus cher de la démo (les sources entières partent au modèle) :
     il donne la réponse à « le juge a coûté combien ? ».

   - **Quelques appels par équipe produit**, courts, pour que `equipe-produit-a` et
     `equipe-produit-b` apparaissent avec des dépenses différentes :

     ```bash
     for i in 1 2 3; do
       curl -sS -o /dev/null "$GATEWAY/v1/chat/completions" -H "Authorization: Bearer $CLE_PRODUIT_A" \
         -H 'Content-Type: application/json' \
         -d '{"model":"modele-generation","max_tokens":40,"messages":[{"role":"user","content":"Explique MCP en une phrase."}]}'
     done
     curl -sS -o /dev/null "$GATEWAY/v1/chat/completions" -H "Authorization: Bearer $CLE_PRODUIT_B" \
       -H 'Content-Type: application/json' \
       -d '{"model":"modele-juge","max_tokens":40,"messages":[{"role":"user","content":"Explique MCP en une phrase."}]}'
     ```

   - **Un appel au modèle réservé avec la clé `platform-team`**, seule équipe du
     groupe `direction`, pour que `modele-reserve` figure au journal avec son prix
     bien plus élevé — le contraste avec le refus de la démo n'en sera que plus net :

     ```bash
     curl -sS -o /dev/null "$GATEWAY/v1/chat/completions" -H "Authorization: Bearer $CLE_PLATFORM" \
       -H 'Content-Type: application/json' \
       -d '{"model":"modele-reserve","max_tokens":40,"messages":[{"role":"user","content":"Explique MCP en une phrase."}]}'
     ```

   Vérifiez ensuite dans `/ui` → Usage que chaque équipe a une dépense non nulle
   et que les coûts correspondent aux prix de `model_info` (sinon, les prix ne
   sont pas pris en compte : vérifier l'indentation de `model_info` et redémarrer
   la gateway). Comptez une dizaine de centimes pour l'ensemble. Ne lancez pas
   encore la boucle des quarante appels ni la clé au budget minuscule : ces deux
   refus se jouent en direct pendant la démo, et un budget épuisé ne se réarme pas.
8. SSO : Keycloak démarre avec le compose et importe le realm ; voir la section
   « SSO avec Keycloak » ci-dessous pour le secret, la vérification et le scénario.
   Gratuit jusqu'à cinq utilisateurs depuis la version 1.76 de LiteLLM, licence
   Enterprise au-delà : trois comptes de démo suffisent.
9. MCP gateway. Quatre gestes, tous dans « Commandes prêtes », bloc MCP :
   - démarrer le serveur MCP du fil rouge sur le poste (`mvn quarkus:dev` dans un
     serveur généré à la répétition, ou celui du TP2) et relever son point d'accès
     HTTP (souvent `http://localhost:8080/mcp` avec `quarkus-mcp-server-http` ;
     vérifier dans les logs ou la Dev UI). Depuis le conteneur LiteLLM, ce poste
     s'appelle `host.docker.internal` ;
   - déclarer le serveur dans la gateway sous l'alias `catalogue`, par l'API
     (`POST /v1/mcp/server`) ou dans l'interface (MCP Servers → Add New MCP
     Server). Passer par l'API ou l'interface plutôt que par le YAML : les
     permissions par équipe référencent l'identifiant que la gateway attribue au
     serveur ;
   - autoriser **seulement** `platform-team` (`POST /team/update` avec
     `object_permission.mcp_servers`, ou Teams → platform-team → MCP servers) ;
   - vérifier `tools/list` puis `tools/call` de `find_service` à travers
     `http://localhost:4000/mcp/` avec la clé `platform-team` : les outils
     apparaissent préfixés par l'alias (`catalogue-find_service`). Avec la clé
     `equipe-produit-b`, la liste doit revenir **vide** : la gateway ne refuse pas
     bruyamment, elle ne montre pas.
10. **Répéter la démo une fois en entier, chronomètre en main**, et enregistrer
    une capture vidéo de secours : si Docker ou le réseau lâche en salle, on
    projette la vidéo et on commente.

## Déroulé (20 min)

| Temps | Écran | Ce que vous montrez, ce que vous dites |
| --- | --- | --- |
| 1 min | Diapo « Une gateway de modèles » | Le retour d'expérience en deux phrases. « Tout ce que vous avez appelé depuis hier aurait pu passer par là. » |
| 3 min | `/ui` → Models, Teams, Keys | Trois alias ; Zen et sa clé n'apparaissent pas. Quatre équipes, chacune avec budget, limite de débit, modèles autorisés. « Qui a fixé 5 $ et 30 requêtes par minute ? Moi, hier soir. Chez vous, qui ? » |
| 5 min | Terminal | **Le fil rouge traverse la gateway.** Deux consommateurs des TP, inchangés, pointés vers la gateway. (1) Le juge du TP3 : même commande Maven qu'au TP3, seules les variables changent (`LLM_ENDPOINT` = la gateway, `LLM_MODEL` = l'alias `modele-juge`, `LLM_API_KEY` = la clé `platform-team`) ; le verdict tombe et l'appel apparaît dans le journal avec son coût et son équipe. (2) OpenCode en mode non interactif : depuis un répertoire vide contenant `opencode.gateway.example.json` renommé en `opencode.json`, `opencode run` avec une question d'une phrase et la clé `equipe-produit-a` dans `LITELLM_KEY` ; la réponse arrive, un second appel apparaît au journal, cette fois pour l'équipe produit. Commandes exactes dans « Commandes prêtes ». Ce qu'on dit : le code du juge et OpenCode n'ont pas changé, seuls une URL et un alias ont été fournis ; et derrière l'alias tourne un modèle Zen, pas celui des TP — l'alias isole le code du modèle. |
| 4 min | Terminal | **Trois refus.** `modele-reserve` avec une clé d'équipe produit → `403 team_model_access_denied`. Clé `demo-budget-epuise` → `429 budget_exceeded` avec le montant dépensé et le plafond dans le message, le fournisseur n'a pas été appelé. Boucle de 40 appels courts → `429`. À chaque fois : où le contrôle s'est appliqué, qui a fixé la valeur. |
| 3 min | `/ui` → Usage / Logs | Dépense par équipe et par tag : « le juge du TP3 a coûté combien ? ». Un appel journalisé montre modèle, coût, clé, équipe, mais pas le contenu (`turn_off_message_logging`). « Bon réglage pour vos données réelles ? Qui décide de la rétention ? » |
| 2 min | Navigation privée → `/ui` → « Login with SSO » | **SSO par Keycloak.** Connexion `alice` / `alice` : Keycloak authentifie, LiteLLM la crée à la volée avec le rôle `internal_user` (attribut `litellm_role`) et la rattache à `equipe-produit-a` (revendication `groups`). Elle ne voit que les clés et la dépense de son équipe, pas celles de `platform-team`. « L'identité vient de votre annuaire ; le droit de voir quoi, c'est encore une décision : l'attribut et le groupe, quelqu'un les a posés. » Prenez ces 2 min sur la colonne Tracer ou sur le MCP gateway si vous êtes en retard. |
| 1 min | `/ui` → MCP Servers, puis Teams → platform-team | **La gateway devant un serveur MCP.** Le serveur du fil rouge, celui que les participants ont fait générer, est déclaré sous l'alias `catalogue` avec son URL sur votre poste. Dans l'équipe `platform-team`, la section MCP servers le liste ; dans `equipe-produit-b`, rien. « Même objet que pour les modèles : un alias, des équipes, un droit posé par quelqu'un. » |
| 2 min | Terminal | **Trois appels JSON-RPC.** (1) `tools/list` sur `/mcp/` avec la clé `platform-team` : les quatre primitives du TP apparaissent, préfixées par l'alias — `catalogue-find_service`, `catalogue-get_owner`… « Le préfixe, c'est la gateway qui range plusieurs serveurs dans un même espace. » (2) `tools/call` de `catalogue-find_service` avec `query = "auth"` : la réponse vient du serveur Quarkus, à travers la gateway, avec la même clé virtuelle que pour les modèles. (3) `tools/list` avec la clé `equipe-produit-b` : liste vide, code 200. « Pas de refus, pas d'erreur : l'outil n'existe pas pour cette équipe. C'est confortable pour l'agent, et c'est exactement ce qui rend le journal indispensable. » |
| 1 min | Diapo « Sécuriser les accès MCP » | **Ce que ça règle, ce que ça ne règle pas.** Réglé : qui voit quel serveur, avec la même identité que pour les modèles ; cette version sait aussi restreindre outil par outil (`mcp_tool_permissions`), à montrer si vous l'avez testé. Pas réglé : un client qui joint le serveur Quarkus directement, sur `localhost:8080`, contourne tout — le montrer en un `curl` si le temps le permet. Le serveur reste responsable de ses propres contrôles, et l'incident A de la trace fictive tient toujours. |
| 1 min | Diapo « Ce que la gateway rend gouvernable » | Fermer : le mécanisme est là ; la politique — budget, modèles, rétention, qui approuve — reste à écrire. Enchaîner sur le mini-exercice : la trace fictive. |

## Fournisseur : OpenCode Zen

Zen est le fournisseur de modèles d'OpenCode (opencode.ai/zen) : une clé, une
facturation, plusieurs familles de modèles. Il expose **trois formats d'API selon
le modèle**, et c'est ce qui dicte la configuration LiteLLM :

| Famille Zen | Endpoint Zen | Déclaration LiteLLM | Exemple dans la config |
| --- | --- | --- | --- |
| GLM, MiniMax, DeepSeek V4.1 Flash | `/zen/v1/chat/completions` | `model: openai/<id>`, `api_base: https://opencode.ai/zen/v1` | `modele-generation` → `glm-5.3`, `modele-juge` → `glm-5.3-flash` |
| Claude, Qwen (sauf Qwen Max) | `/zen/v1/messages` (format Anthropic) | `model: anthropic/<id>`, `api_base: https://opencode.ai/zen` — LiteLLM ajoute `/v1/messages` | `modele-reserve` → `claude-opus-5-5` |
| GPT, Grok, Muse | `/zen/v1/responses` (Responses API) | Non retenu pour la démo : à tester avec la route Responses de LiteLLM si vous y tenez | — |

Trois choix à assumer devant la salle :

- **La doc Zen et son API divergent.** Le 27 septembre 2026, `deepseek-v4-pro` et
  `kimi-k2.7-code` répondaient 404 sur `/chat/completions`, `deepseek-v4-flash`
  n'existait plus, et les modèles gratuits refusent tout client autre qu'OpenCode.
  D'où le choix de GLM 5.3 / GLM 5.3 Flash, vérifiés. Avant chaque session, listez
  les modèles (`/zen/v1/models`) et testez chaque alias par un appel réel : un alias
  cassé met le modèle en « cooldown » côté LiteLLM et la démo répond 429 pour une
  mauvaise raison.
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
| `KC_HOSTNAME` (côté Keycloak) | `http://localhost:8180` | Fige l'émetteur (`iss`) des jetons sur l'URL du navigateur. Sans cela, Keycloak refuse au userinfo, appelé via `keycloak:8080`, un jeton émis via `localhost:8180` (« Invalid token issuer ») |
| `GENERIC_USER_ID_ATTRIBUTE` | `preferred_username` | Identifiant stable de l'utilisateur LiteLLM |
| `GENERIC_USER_ROLE_ATTRIBUTE` | `litellm_role` | Revendication ajoutée par le mapper Keycloak depuis l'attribut utilisateur du même nom : `proxy_admin`, `internal_user`, `internal_user_viewer` |
| `litellm_jwtauth.team_ids_jwt_field: groups` (config YAML) | revendication `groups` | À chaque connexion, LiteLLM ajoute l'utilisateur aux équipes dont le `team_id` égale un nom de groupe ; le script crée les équipes avec `team_id` = nom du groupe |

**Mise en place, la veille :**

1. Générer un secret (`openssl rand -hex 24`) et le mettre dans `.env`
   (`KEYCLOAK_CLIENT_SECRET`), **avant le premier démarrage de Keycloak**. Le
   realm le lit à l'import via le placeholder `${KEYCLOAK_CLIENT_SECRET}` et
   LiteLLM le reçoit en `GENERIC_CLIENT_SECRET` : une seule source. Renseigner
   aussi `KEYCLOAK_ADMIN_PASSWORD`.
2. `docker compose up -d` ; attendre `http://localhost:8180/realms/formation-ia`
   (le premier démarrage de Keycloak prend une à deux minutes). **Le realm n'est
   importé qu'une fois** : si Keycloak a déjà démarré avec un autre secret ou un
   placeholder, soit vous repartez de zéro (`docker compose rm -sf keycloak`, puis
   `up -d`), soit vous alignez le secret dans la console : Clients → litellm →
   Credentials → Regenerate ou saisie, puis la même valeur dans `.env` et
   `docker compose up -d litellm` pour qu'il la relise.
3. Console Keycloak : `http://localhost:8180/admin`, compte `admin`. Vérifier le
   realm `formation-ia`, le client `litellm` (URL de redirection
   `http://localhost:4000/sso/callback`) et les trois utilisateurs. Changer les mots
   de passe de démo si la salle est filmée.
4. Lancer `scripts/preparer-demo.sh` (étape 6 de la préparation) **après** le
   démarrage de Keycloak : les équipes portent le `team_id` attendu par la
   revendication `groups`.
5. Tester en navigation privée : `http://localhost:4000/ui` → « Login with SSO »
   → `formateur` / `formateur` doit arriver `proxy_admin` ; `alice` / `alice` doit
   arriver `internal_user` dans `equipe-produit-a`. Vérifier dans Internal Users.
6. Garder un compte `proxy_admin` local (étape 5 de la préparation) tant que le SSO
   n'est pas prouvé ; `disable_env_credential_login` seulement après.

Le realm importé n'est **pas** réimporté au redémarrage si la base Keycloak existe
déjà (mode `start-dev`, stockage éphémère dans le conteneur) : pour repartir de zéro,
`docker compose rm -sf keycloak` puis `up -d`.

**Si la connexion échoue** : « Invalid redirect_uri » → l'URL de retour dans le
client Keycloak et `PROXY_BASE_URL` ne correspondent pas ; page LiteLLM
« Internal server error » après la saisie du mot de passe → lire
`docker compose logs litellm` : `unauthorized_client / Invalid client
credentials` signifie que le secret connu de Keycloak n'est pas celui de `.env`
(cas typique : realm importé avant que `.env` soit rempli, voir l'étape 2),
une erreur de connexion signifie que le conteneur LiteLLM ne joint pas
`keycloak:8080` (réseau compose) ; utilisateur créé sans rôle → le mapper `litellm_role` n'est pas dans la
réponse userinfo (vérifier dans Keycloak : Clients → litellm → Client scopes →
Evaluate → User info) ; utilisateur sans équipe → `team_ids_jwt_field` absent de la
configuration ou `team_id` différent du nom de groupe ; dans les journaux Keycloak,
`USER_INFO_REQUEST_ERROR … Invalid token issuer. Expected 'http://keycloak:8080/…'`
→ `KC_HOSTNAME` n'est pas à `http://localhost:8180` (LiteLLM retente alors le code,
d'où un second message `Code not valid`) ; dans les journaux LiteLLM,
`value is not a valid email address … special-use or reserved name` → LiteLLM
valide l'e-mail des utilisateurs SSO et refuse les domaines réservés (`.local`,
`.test`, `.example`) : les comptes du realm utilisent `@formation-ia.fr`, gardez un
domaine de forme valide si vous les changez. Pour retester après une correction,
ouvrez **une nouvelle fenêtre privée** : le code d'autorisation d'une tentative
échouée est à usage unique.

**Changer d'utilisateur pendant la démo.** Le bouton « Logout » de LiteLLM ne
ferme que la session LiteLLM ; Keycloak garde la sienne dans le navigateur et, au
clic suivant sur « Login with SSO », reconnecte le même utilisateur sans mot de
passe — c'est le « single » de single sign-on, et un bon point à faire remarquer.
Le compose règle donc `PROXY_LOGOUT_URL` sur l'endpoint de déconnexion Keycloak :
après « Logout », Keycloak affiche une page de confirmation, ferme sa session et
renvoie sur la page de connexion LiteLLM, d'où `alice` peut se connecter. Autre
option, plus rapide en séance : une fenêtre privée par utilisateur.

Ce qu'il faut dire en salle : Keycloak joue ici le rôle de l'annuaire du client
(Entra, Okta, Keycloak d'entreprise…). Ce que la démo montre, c'est que l'identité
vient de l'annuaire, mais que le **rôle** et l'**équipe** sont posés par quelqu'un :
un attribut, un groupe. La gateway les lit ; elle ne les décide pas.

## Commandes prêtes

À lancer depuis `formateur/demo-litellm`, dans un terminal préparé une fois pour
toutes : les clés viennent de `cles-demo.txt`, la clé maître de `.env`, le dépôt
est retrouvé par git. La commande OpenCode se lance dans un répertoire temporaire
pour ne pas toucher à votre `opencode.json` habituel, qui parle à Zen en direct. Le
serveur à juger est celui de votre répétition
(`$ATELIER_DIR/serveur-avec-skill`) ; à défaut, le projet piège du dossier formateur
suffit — le juge lui donne alors une note basse en expliquant le piège, ce qui est
une démonstration en soi, pour une fraction de centime.

```bash
cd formateur/demo-litellm
set -a; . ./.env; set +a
export GATEWAY=http://localhost:4000
export CLE_PLATFORM=$(awk '$1=="platform-team"{print $2}' cles-demo.txt)
export CLE_PRODUIT=$(awk '$1=="equipe-produit-a"{print $2}' cles-demo.txt)
export CLE_DEMO=$(awk '$1=="demo-budget-epuise"{print $2}' cles-demo.txt)
export FORMATION_REPO=$(git rev-parse --show-toplevel)
export SERVEUR_A_JUGER="${ATELIER_DIR:-$FORMATION_REPO/formateur/demo-scorer-trompeur}/${ATELIER_DIR:+serveur-avec-skill}${ATELIER_DIR:-projet-piege}"
echo "clés : ${#CLE_PLATFORM}/${#CLE_PRODUIT}/${#CLE_DEMO} caractères — serveur jugé : $SERVEUR_A_JUGER"
```

```bash
# Le juge du TP3 à travers la gateway (code inchangé)
LLM_ENDPOINT="$GATEWAY/v1/chat/completions" LLM_MODEL=modele-juge LLM_API_KEY="$CLE_PLATFORM" \
  mvn -q -f "$FORMATION_REPO/tp3-eval/pom.xml" -Dtest=JugeTest \
  -Dserveur.genere.dir="$SERVEUR_A_JUGER" test

# OpenCode en mode non interactif, à travers la gateway, avec la clé de l'équipe produit A
D=$(mktemp -d) && sed 's#"_note": "[^"]*",##' opencode.gateway.example.json > "$D/opencode.json"
( cd "$D" && LITELLM_KEY="$CLE_PRODUIT" opencode run --model gateway-demo/modele-generation \
    "Réponds en une phrase : qu'est-ce que MCP ?" )

# Un appel simple, en-têtes visibles (coût de l'appel, dépense de la clé)
curl -sS -D - "$GATEWAY/v1/chat/completions" -H "Authorization: Bearer $CLE_PRODUIT" \
  -H 'Content-Type: application/json' \
  -d '{"model":"modele-generation","max_tokens":40,"messages":[{"role":"user","content":"Réponds : prêt."}]}'

# Modèle non autorisé pour l'équipe (403 team_model_access_denied)
curl -sS -w '\n%{http_code}\n' "$GATEWAY/v1/chat/completions" -H "Authorization: Bearer $CLE_PRODUIT" \
  -H 'Content-Type: application/json' \
  -d '{"model":"modele-reserve","messages":[{"role":"user","content":"bonjour"}]}'

# Budget épuisé (clé au budget dérisoire, déjà amorcée par le script)
curl -sS -w '\n%{http_code}\n' "$GATEWAY/v1/chat/completions" -H "Authorization: Bearer $CLE_DEMO" \
  -H 'Content-Type: application/json' \
  -d '{"model":"modele-generation","messages":[{"role":"user","content":"bonjour"}]}'

# Limite de débit (30 requêtes par minute pour equipe-produit-a → des 429 apparaissent)
for i in $(seq 1 40); do
  curl -sS -o /dev/null -w '%{http_code} ' "$GATEWAY/v1/chat/completions" -H "Authorization: Bearer $CLE_PRODUIT" \
    -H 'Content-Type: application/json' \
    -d '{"model":"modele-generation","max_tokens":5,"messages":[{"role":"user","content":"ok"}]}'
done; echo

# ---- MCP gateway : le serveur Quarkus du fil rouge doit tourner sur le poste (relever son point d'accès)
export MCP_SERVEUR_URL="http://host.docker.internal:8080/mcp"   # vu depuis le conteneur LiteLLM

# Déclarer le serveur sous l'alias « catalogue » et récupérer l'identifiant attribué par la gateway
export MCP_ID=$(curl -sS -X POST "$GATEWAY/v1/mcp/server" -H "Authorization: Bearer $LITELLM_MASTER_KEY" \
  -H 'Content-Type: application/json' \
  -d "{\"server_name\":\"catalogue\",\"alias\":\"catalogue\",\"url\":\"$MCP_SERVEUR_URL\",\"transport\":\"http\",\"description\":\"Serveur MCP du fil rouge (catalogue de services)\"}" \
  | python3 -c "import sys,json;print(json.load(sys.stdin)['server_id'])"); echo "MCP_ID=$MCP_ID"

# Autoriser seulement platform-team
curl -sS -o /dev/null -w 'platform-team autorisée : %{http_code}\n' -X POST "$GATEWAY/team/update" \
  -H "Authorization: Bearer $LITELLM_MASTER_KEY" -H 'Content-Type: application/json' \
  -d "{\"team_id\":\"platform-team\",\"object_permission\":{\"mcp_servers\":[\"$MCP_ID\"]}}"

# tools/list avec platform-team : les primitives du TP, préfixées « catalogue- »
curl -sS -X POST "$GATEWAY/mcp/" -H "x-litellm-api-key: Bearer $CLE_PLATFORM" \
  -H 'Content-Type: application/json' -H 'Accept: application/json, text/event-stream' \
  -d '{"jsonrpc":"2.0","id":1,"method":"tools/list"}'

# tools/call de find_service à travers la gateway
curl -sS -X POST "$GATEWAY/mcp/" -H "x-litellm-api-key: Bearer $CLE_PLATFORM" \
  -H 'Content-Type: application/json' -H 'Accept: application/json, text/event-stream' \
  -d '{"jsonrpc":"2.0","id":2,"method":"tools/call","params":{"name":"catalogue-find_service","arguments":{"query":"auth"}}}'

# tools/list avec equipe-produit-b : liste vide (200), pas de refus
export CLE_PRODUIT_B=$(awk '$1=="equipe-produit-b"{print $2}' cles-demo.txt)
curl -sS -X POST "$GATEWAY/mcp/" -H "x-litellm-api-key: Bearer $CLE_PRODUIT_B" \
  -H 'Content-Type: application/json' -H 'Accept: application/json, text/event-stream' \
  -d '{"jsonrpc":"2.0","id":3,"method":"tools/list"}'

# Contournement : le serveur joint en direct, sans gateway ni clé (à montrer si le temps le permet)
curl -sS -X POST http://localhost:8080/mcp -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' -d '{"jsonrpc":"2.0","id":4,"method":"tools/list"}'

# Retirer la déclaration après la démo
# curl -sS -X DELETE "$GATEWAY/v1/mcp/server/$MCP_ID" -H "Authorization: Bearer $LITELLM_MASTER_KEY"

# Dépense d'une équipe (clé maître) : equipe-produit-a, equipe-produit-b, platform-team ou ci-usine
curl -sS "$GATEWAY/team/info?team_id=platform-team" -H "Authorization: Bearer $LITELLM_MASTER_KEY" \
  | python3 -c "import sys,json;t=json.load(sys.stdin)['team_info'];print(t['team_id'],'dépense',t['spend'],'$ sur',t['max_budget'])"
```

Codes d'erreur observés sur l'image `main-stable` du 27 septembre 2026 : `403`
pour un modèle hors équipe, `429` pour le débit comme pour le budget (le type
d'erreur les distingue : `budget_exceeded`). D'autres versions renvoient `400` ou
`422` pour le budget : vérifiez sur l'image figée et adaptez ce que vous annoncez.

## Ce que la démonstration ne doit pas laisser croire

- Que la gateway **sécurise** les serveurs MCP : elle gouverne qui voit quel
  serveur, et sur cette version quel outil, mais seulement pour ce qui passe par
  elle. Un client qui joint le serveur directement n'en sait rien. L'autorisation
  effective et la protection du SI restent au serveur.
- Que le quota **remplace** l'évaluation : un appel bon marché peut être non conforme.
- Que le SSO est acquis : le Keycloak de la démo est un jouet local ; licence LiteLLM
  au-delà de cinq utilisateurs et raccordement à l'annuaire réel sont des décisions
  du client.
- Que l'installation est faite : ici, c'est un poste de formateur. Chez le client,
  hébergement, haute disponibilité, secrets, sauvegarde de Postgres et TLS sont à
  traiter ; c'est précisément une ligne de la fiche de sortie de M7.
- Que les chiffres et les onglets sont définitifs : ils varient selon la version.
