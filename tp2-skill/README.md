# TP2 — Écrire le Skill `create-quarkus-mcp-server`

**[Guide détaillé sur GitHub Pages](https://sciam-fr.github.io/formation-ia/tp2/)**
· [Source du guide](../docs/tp2.md)

**Objectif** : distiller le savoir-faire de TP1 dans un Skill, pour qu'OpenCode produise
un serveur MCP Quarkus **conforme**, de façon **répétable**.

## Étapes

1. **Repérez les écarts** observés en TP1 (structure, transport, nommage, annotations) —
   voir `../tp1-serveur-vanilla/ATTENDU.md`.
2. **Complétez** `skills/create-quarkus-mcp-server/SKILL.md` (c'est un squelette à remplir).
   Distillez uniquement le *delta* : ce que le modèle se plante à faire seul. N'écrivez pas
   ce qu'il sait déjà (anti-pattern « le harnais qui devient plafond »).
3. **Déposez** le Skill dans un chemin de découverte OpenCode
   (ex. `.opencode/skills/` ou `.claude/skills/` à la racine de votre projet).
4. **Dogfood** : relancez OpenCode + Skill, régénérez un serveur, et **comparez au vanilla
   de TP1**. Le résultat doit être conforme et reproductible.
5. **Poussez** le Skill sur GitLab (amorce TP4).

## Référence

`references/conventions-quarkus-mcp.md` (copie des conventions maison à encoder).
