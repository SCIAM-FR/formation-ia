# TP4 — Gouverner le Skill sur GitLab ou GitHub

**[Page d'entrée du TP4 sur GitHub Pages](https://sciam-fr.github.io/formation-ia/tp4/)**
· variante [GitLab](https://sciam-fr.github.io/formation-ia/tp4-gitlab/)
· variante [GitHub](https://sciam-fr.github.io/formation-ia/tp4-github/)
· sources : [`docs/tp4.md`](../docs/tp4.md), [`docs/TP4-GITLAB.md`](../docs/TP4-GITLAB.md),
[`docs/TP4-GITHUB.md`](../docs/TP4-GITHUB.md)

Le Skill est un actif partagé : il se versionne, se revoit, se protège. Le TP existe
en **deux variantes** selon la forge préparée par le formateur ; les preuves exigées
sont les mêmes. Suivez une seule variante.

## Fichiers du dossier

| Fichier | Forge | Rôle |
| --- | --- | --- |
| `.gitlab-ci.yml` | GitLab | Pipeline de merge request : squelette avec un `echo` TODO |
| `.github/workflows/evaluer-le-skill.yml` | GitHub | Workflow `pull_request` : squelette avec un `echo` TODO |
| `CODEOWNERS` | les deux | Propriété du Skill ; syntaxe des propriétaires selon la forge |
| `opencode-permissions.example.json` | les deux | Exemple de `permission.skill` par agent, à adapter |

## Deux parcours

| Parcours | Sections du guide | Preuve de sortie |
| --- | --- | --- |
| **Préparation formateur, avant séance** | 1, 3 et 4 | Projet ou dépôt, runner, environnement OpenCode, modèles et secrets autorisés prévalidés ; vraie génération et évaluation en CI |
| **Essentiel — 1 h 30 cible** | 1 à 6 ; protocole en 7 | `main` protégé, MR ou PR et revue selon l'offre, génération réelle de la MR/PR évaluée, blocage rouge puis retour vert, chargement Skill allow/deny prouvé |
| **Approfondissement** | 3 et 4 depuis zéro ; 3, 6 et 7 pour aller plus loin | Installation CI autonome, plusieurs générations, diagnostics et test de suppression répété |

Les approfondissements sont hors des 14 h ou si avance, sans supprimer l'essentiel.
La plomberie CI et les secrets se préparent avant la séance ; les participants
les inspectent puis en prouvent l'exécution. Sans cette préparation, allongez
l'atelier ou annoncez une démonstration partielle, pas une validation fictive.

**Le dépôt ne fournit pas de CI clé en main :** les deux squelettes contiennent un
`echo` TODO, à remplacer par le formateur (ou en parcours autonome) par une
génération réelle depuis le Skill de la MR ou de la PR, suivie du build/MCP et du
harnais complété, juge inclus pour le parcours complet.

## Mise sous gouvernance
1. Publiez le Skill, ses références et le harnais complété sur le projet ou dépôt préparé.
2. **Protection de `main`** : pas de push direct, tout passe par merge request ou pull request.
3. **CODEOWNERS** : adaptez les propriétaires et les chemins du Skill, des références
   et du harnais. Prouvez la revue obligatoire si l'offre le permet ; sinon,
   documentez la limite et la revue manuelle.
4. **CI qui génère et évalue** : inspectez la chaîne adaptée, prouvez qu'elle utilise
   le checkout de la MR ou de la PR et qu'une mutation contrôlée bloque la fusion,
   puis que sa correction rétablit le vert. Ne désactivez aucun test.
5. **Permissions par agent** : adaptez `opencode-permissions.example.json` au schéma
   courant et prouvez le chargement du Skill en allow/deny. Cela ne remplace ni
   les droits de la forge ni l'authentification et l'autorisation MCP.

## Drift
Le Skill dérive quand le modèle progresse : des instructions deviennent inutiles (le socle
les a absorbées). Définissez le protocole du **test de suppression** dans l'essentiel ;
en approfondissement, comparez avec/sans règle sur plusieurs générations. Ne retirez
une règle que si les preuves le justifient ; le Skill ne doit pas rétrécir à tout prix.

## Boucle complète
TP1 (vanilla) → TP2 (Skill) → TP3 (éval) → **TP4 : le versioning, la CI et la revue rendent
le Skill vérifiable à chaque évolution.** La ligne de flottaison, en pratique.
