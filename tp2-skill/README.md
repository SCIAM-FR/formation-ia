# TP2 — Écrire le Skill `create-quarkus-mcp-server`

**[Guide détaillé sur GitHub Pages](https://sciam-fr.github.io/formation-ia/tp2/)**
· [Source du guide](../docs/tp2.md)

**Objectif** : distiller le savoir-faire de TP1 dans un Skill et vérifier son apport
à la **conformité**, sans déduire la répétabilité d'une seule génération.

## Deux parcours

| Parcours | Sections du guide | Preuve de sortie |
| --- | --- | --- |
| **Essentiel — 1 h 30 cible** | 1 à 6, corrections si nécessaires | Skill + référence installés et sauvegardés dans le repo, chargement prouvé, plan relu et approuvé dans Plannotator, une génération neuve vérifiée et comparée à TP1 |
| **Approfondissement** | 6, répétitions de robustesse | Plusieurs sorties indépendantes et stabilité documentée |

Les répétitions supplémentaires se font hors des 14 h ou si vous avez de l'avance,
sans supprimer l'essentiel. Une nouvelle génération pour vérifier la correction
d'une règle en échec reste nécessaire, quitte à dépasser la durée cible.

## Étapes

1. **Repérez les écarts** observés en TP1 (structure, transport, nommage, annotations) —
   voir `../tp1-serveur-vanilla/ATTENDU.md`.
2. **Complétez** `skills/create-quarkus-mcp-server/SKILL.md` (c'est un squelette à remplir).
   Distillez uniquement le *delta* : ce que le modèle ne fait pas encore seul. N'écrivez pas
   ce qu'il sait déjà (anti-pattern « le harnais qui devient plafond »).
3. **Installez** le Skill dans `.opencode/skills/create-quarkus-mcp-server/`
   d'un projet neuf, avec la référence dans son sous-répertoire `references/`.
   Vérifiez l'appel réel à l'outil `skill` et la lecture de cette référence.
4. **Planifiez avant de générer** : sur l'agent `plan` d'OpenCode, demandez le plan
   de génération et relisez-le dans [Plannotator](https://docs.plannotator.ai/open-source/agents/opencode)
   (plugin `@plannotator/opencode` dans `opencode.json`), conventions sous les yeux ;
   annotez ce qui manque ou dévie, approuvez quand tout renvoie à une règle.
5. **Générez une fois** selon le plan approuvé, sans consulter ni recopier le serveur TP1 ; vérifiez le build,
   le démarrage et les appels MCP, puis **comparez au vanilla de TP1**. Explicitez
   les écarts et limites. En cas d'échec d'une règle, corrigez le Skill source et
   vérifiez-le sur une nouvelle génération, pas seulement sur du Java retouché.
6. **Sauvegardez** les versions sources du Skill et de sa référence dans le repo
   local. La **publication sur la forge (GitLab ou GitHub) appartient au TP4**, pas à ce TP.

## Bonus expérimental

Hors des 14 h : [partager le contexte d'une session OpenCode](../docs/TP-2-Bonus.md)
avec un binôme, par export manuel, par Skill de passation, puis par Skill publiant
le contexte sous une clé Redis. Rien n'en rentre dans ce dossier.

## Référence

`references/conventions-quarkus-mcp.md` (copie des conventions maison à encoder).
