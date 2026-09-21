# TP4 — Gouverner le Skill sur GitLab

**[Guide détaillé sur GitHub Pages](https://sciam-fr.github.io/formation-ia/tp4/)**
· [Source du guide](../docs/tp4.md)

Le Skill est un actif partagé : il se versionne, se revoit, se protège.

## Deux parcours

| Parcours | Sections du guide | Preuve de sortie |
| --- | --- | --- |
| **Préparation formateur, avant séance** | 1, 3 et 4 | Projet GitLab, runner, environnement OpenCode, modèles et secrets autorisés prévalidés ; vraie génération et évaluation en CI |
| **Essentiel — 1 h 30 cible** | 1 à 6 ; protocole en 7 | `main` protégé, MR et revue selon l'offre, génération réelle de la MR évaluée, blocage rouge puis retour vert, chargement Skill allow/deny prouvé |
| **Approfondissement** | 3 et 4 depuis zéro ; 3, 6 et 7 pour aller plus loin | Installation CI autonome, dataset multi-cas, diagnostics et test de suppression répété |

Les approfondissements sont hors des 14 h ou si avance, sans supprimer l'essentiel.
La plomberie CI et les secrets se préparent avant la séance ; les participants
les inspectent puis en prouvent l'exécution. Sans cette préparation, allongez
l'atelier ou annoncez une démonstration partielle, pas une validation fictive.

**Le dépôt ne fournit pas de CI clé en main :** `.gitlab-ci.yml` contient un
`echo` TODO, à remplacer par le formateur (ou en parcours autonome) par une
génération réelle depuis le Skill de la MR, suivie du build/MCP et du harnais
complété, juge inclus pour le parcours complet.

## Mise sous gouvernance
1. Publiez le Skill, ses références et le harnais complété sur le projet GitLab préparé.
2. **Branch protection** sur `main` : pas de push direct, tout passe par merge request.
3. **CODEOWNERS** : adaptez les propriétaires et les chemins du Skill, des références
   et du harnais. Prouvez la revue obligatoire si l'offre le permet ; sinon,
   documentez la limite et la revue manuelle.
4. **CI qui génère et évalue** : inspectez la chaîne adaptée, prouvez qu'elle utilise
   le checkout de la MR et qu'une mutation contrôlée bloque la fusion, puis que sa
   correction rétablit le vert. Ne désactivez aucun test.
5. **Permissions par agent** : adaptez `opencode-permissions.example.json` au schéma
   courant et prouvez le chargement du Skill en allow/deny. Cela ne remplace ni
   les droits GitLab ni l'authentification et l'autorisation MCP.

## Drift
Le Skill dérive quand le modèle progresse : des instructions deviennent inutiles (le socle
les a absorbées). Définissez le protocole du **test de suppression** dans l'essentiel ;
en approfondissement, comparez avec/sans règle sur plusieurs générations. Ne retirez
une règle que si les preuves le justifient ; le Skill ne doit pas rétrécir à tout prix.

## Boucle complète
TP1 (vanilla) → TP2 (Skill) → TP3 (éval) → **TP4 : le versioning, la CI et la revue rendent
le Skill vérifiable à chaque évolution.** La ligne de flottaison, en pratique.
