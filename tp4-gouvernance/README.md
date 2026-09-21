# TP4 — Gouverner le Skill sur GitLab

**[Guide détaillé sur GitHub Pages](https://sciam-fr.github.io/formation-ia/tp4/)**
· [Source du guide](../docs/tp4.md)

Le Skill est un actif partagé : il se versionne, se revoit, se protège.

## Mise sous gouvernance
1. Poussez `tp2-skill/skills/create-quarkus-mcp-server` sur un projet GitLab.
2. **Branch protection** sur `main` : pas de push direct, tout passe par merge request.
3. **CODEOWNERS** : la platform team possède `skills/` → revue obligatoire (voir `CODEOWNERS`).
4. **CI qui évalue** : `.gitlab-ci.yml` rejoue l'éval de TP3 sur chaque MR. Une modif du
   Skill qui casse la conformité est bloquée avant le merge.
5. **Permissions par agent** : `opencode-permissions.example.json` — qui a le droit
   d'utiliser le Skill (allow/deny).

## Drift
Le Skill dérive quand le modèle progresse : des instructions deviennent inutiles (le socle
les a absorbées). Appliquez le **test de suppression** en revue : « si le modèle sait déjà
le faire, on retire ces lignes ». Le Skill doit **rétrécir** avec le temps, pas grossir.

## Boucle complète
TP1 (vanilla) → TP2 (Skill) → TP3 (éval) → **TP4 : le versioning, la CI et la revue rendent
le Skill sûr à faire évoluer.** La ligne de flottaison, en pratique.
