# Formation IA — modifications restantes

État au 21 septembre 2026, après la seconde passe : corrections techniques du deck,
alignement de la plaquette et ajout des supports pédagogiques. Il reste la
préparation opérationnelle de la session, qui ne se fait pas dans le dépôt.

Supports concernés : [slide deck](slides/Support_formation_SCIAM.pptx) (désormais
**58 diapositives**), [plaquette](Plaquette_IA-PLT-USINE.pdf) (source dans
[`plaquette/`](plaquette/)), [guides des TP](docs/index.md), squelettes du dépôt et
[références formateur](formateur/README.md).

Ce fichier rassemble les suites de la revue, pas une demande de compléter les
exercices à la place des participants.

## Déjà réalisé — ne pas refaire

### Première passe

- Fiches TP (désormais diapos 28, 34, 43, 52) : consignes alignées sur les TP,
  durées cibles, preuves attendues, distinction essentiel / approfondissement.
- Diapos M6 (50–51) : séparation GitLab / OpenCode / accès MCP, conditions de revue
  et de blocage, statut de squelette de la CI explicité.
- Diapo finale (58) : quatre livrables et limites avant production.
- Diapos 2 et 13 : cadence ajustée ; programme détaillé de 14 h dans les guides,
  dont 6 h 30 de TP essentiels.
- Guides et README : parcours, points de passage et préparation formateur.

### Seconde passe — deck (P1)

- [x] **Diapo 7 — Sidecars.** Formulation sur le cycle de vie (démarrage, arrêt,
  coordination) ; notes : les sidecars natifs ne remplacent pas l'injection.
- [x] **Diapo 8 — « 18 mois, pas 5 ans ».** Présenté comme un ordre de grandeur
  illustratif, sources et dates à vérifier dans les notes.
- [x] **Diapo 9 — Sorties structurées et contexte long.** Conformité au schéma
  distinguée de l'exactitude métier ; contexte long et cache ne suppriment pas la
  sélection de l'information.
- [x] **Diapo 12 — « Les responsabilités qui restent à l'organisation ».** Titre,
  sous-titre, six cartes et notes harmonisés : mécanismes fournis, politiques,
  critères et décisions conservés.
- [x] **Diapo 14 — Frontier et open-weight.** Deux axes : capacité × distribution ;
  exemples datés et sourcés dans les notes.
- [x] **Diapo 15 — Coût des MoE.** Calcul par token, mémoire des poids et
  communication distingués ; référence Hugging Face dans les notes.
- [x] **Diapo 20 — AP2.** Case « paiements » réduite à AP2 ; les deux homonymes ACP
  développés dans les notes.
- [x] **Diapo 46 (ex-42) — Évaluation et guardrails.** « Évaluer mesure ; un
  guardrail intervient pour contraindre » ; évaluation en ligne mentionnée.
- [x] **Diapo 47 (ex-43) — Gateway MCP.** Pattern possible, pas une garantie ;
  authentification, autorisation et audit distingués ; OAuth 2.1 ≠ clé API.
- [x] **Notes M0–M1.** Exemples datés « à date sept. 2026 », sources à citer,
  rafraîchissement avant chaque session.

### Seconde passe — plaquette (P1)

- [x] Prérequis précisés (test Java simple, Git/Maven, poste préparé).
- [x] Ligne « Conditions techniques » ajoutée (fournisseur LLM, réseau, GitLab).
- [x] TP1 : « comparaison des générations et mesure des écarts éventuels ».
- [x] Portée et durée : 14 h = parcours essentiel avec environnement préparé ;
  approfondissements complémentaires ; « poser les fondations ».
- [x] Évaluation des acquis : grille avant/après sur quatre compétences, distincte
  de l'évaluation du Skill.
- La plaquette se régénère avec `python3 plaquette/build.py` (PyMuPDF).

### Seconde passe — supports pédagogiques (P2)

- [x] **Diapo 26 — Schéma du fil rouge « méta »** : chaîne de fabrication et
  chaîne d'utilisation, avant TP1.
- [x] **Diapo 27 — Accès aux guides** : lien et QR code par TP ; lien cliquable
  ajouté sur chaque fiche TP.
- [x] **Diapo 41 — Scorer trompeur** : démonstration du faux vert ; matériel dans
  [`formateur/demo-scorer-trompeur/`](formateur/demo-scorer-trompeur/).
- [x] **Diapo 42 — Lecture des résultats** : build/tests, contrat MCP, assertions,
  juge ; « non exécuté » visible ; cinq cas = signal exploratoire.
- [x] **Diapo 48 — Mini-exercice M5** (~15 min) : trois incidents fictifs ;
  trace et corrigé dans [`formateur/m5-trace-fictive/`](formateur/m5-trace-fictive/).
- [x] **Diapos 56–57 — Fiche de sortie M7** : livrable remplacé par la fiche
  remplie ; canevas sur la diapo 57 et dans
  [`docs/fiche-de-sortie.md`](docs/fiche-de-sortie.md).
- [x] **Permissions OpenCode** : exemple aligné sur `permission.skill`.
- [x] **Références formateur** : corrigé possible des assertions dans
  [`formateur/corrige-assertions/`](formateur/corrige-assertions/).

**À vérifier à l'ouverture du deck :** le rendu n'a pas pu être contrôlé
visuellement (conversion PDF indisponible sur le poste). Ouvrir dans PowerPoint et
vérifier les diapos 26 (schéma dessiné), 27 (QR codes), 42 et 57 (tableaux), ainsi
que les fiches TP 28, 34, 43, 52 dont la colonne droite a gagné une ligne.

## P1 — Préparer effectivement la prochaine session

Ces points relèvent de l'environnement d'atelier et de la répétition, pas du dépôt.

- [ ] **Figer et éprouver l'environnement.** Consigner les versions retenues de
  Java, Maven, Quarkus/MCP et OpenCode, le modèle de génération et le modèle juge.
  Exécuter le parcours essentiel sur un poste représentatif avant la formation.

- [ ] **Prévalider les appels LLM.** Vérifier génération, compatibilité du endpoint
  juge, format de réponse, quotas et budget. Prévoir un repli explicitement partiel
  si le juge est indisponible ; ne pas compter un test ignoré comme réussi.

- [ ] **Préparer la vraie chaîne GitLab du TP4.** Dans l'environnement d'atelier,
  remplacer la génération fictive, installer OpenCode, vérifier les variables
  autorisées et générer à partir du Skill de la MR. Démontrer le blocage rouge puis
  le retour au vert avant la séance. Le
  [YAML du dépôt](tp4-gouvernance/.gitlab-ci.yml) reste un squelette, pas une CI clé en main.

- [ ] **Vérifier les droits et l'offre GitLab.** Préparer les comptes, groupes,
  runners, protections et règles d'approbation nécessaires. Si l'approbation
  obligatoire des propriétaires n'est pas disponible, documenter ce qui sera
  montré manuellement sans le présenter comme un verrou automatique.

- [ ] **Vérifier l'exemple de permissions OpenCode** avec la version retenue :
  le [JSON fourni](tp4-gouvernance/opencode-permissions.example.json) suit le schéma
  `permission.skill` ; contrôler le chargement autorisé et refusé avec les agents
  réellement utilisés (`build`, `plan` ou un agent de revue personnalisé).

- [ ] **Produire les sorties de référence.** Générer et archiver hors dépôt des
  serveurs conformes et non conformes avec le modèle de la session, pour le débrief
  et le dépannage. Le corrigé des assertions et la démonstration du scorer sont
  déjà dans `formateur/`.

- [ ] **Chronométrer une répétition.** Mesurer temps de génération, corrections,
  appels juge et pipelines. Ajuster les durées ou les prérequis sur ces observations ;
  les durées actuelles sont des cibles, pas des mesures. Rafraîchir au passage les
  exemples datés des notes M0–M1.

**Critère de fin :** les participants consacrent le temps essentiel à apprendre,
comparer et prouver, pas à découvrir des problèmes d'accès ou d'installation.
