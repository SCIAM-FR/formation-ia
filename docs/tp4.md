---
title: TP4 — Gouverner le Skill (GitLab ou GitHub)
description: Choisir la variante du TP4 selon la forge préparée pour l'atelier ; les preuves exigées sont identiques.
permalink: /tp4/
previous_url: /tp3/
previous_title: TP3 — Évaluer
next_url: /fiche-de-sortie/
next_title: Fiche de sortie (M7)
---

## Deux variantes, une seule exigence

Le TP4 rend le Skill **gouvernable** : une proposition de changement relue, une CI
qui régénère et évalue la version proposée, des protections vérifiées, des
permissions de chargement observées. Ces preuves ne dépendent pas de la forge.
Le guide existe en deux variantes ; suivez celle que le formateur a préparée :

| Variante | Quand la suivre | Guide |
| --- | --- | --- |
| **GitLab** | Projet GitLab d'atelier, runner GitLab, pipeline de merge request | [TP4 — GitLab]({{ '/tp4-gitlab/' | relative_url }}) |
| **GitHub** | Dépôt dans une organisation GitHub, GitHub Actions, workflow de pull request | [TP4 — GitHub]({{ '/tp4-github/' | relative_url }}) |

Ne mélangez pas les deux : les fichiers de CI, l'emplacement des secrets et les
réglages de protection diffèrent. Les entrées sont les mêmes dans les deux cas :
Skill et références du TP2, harnais complété du TP3, fichiers d'exemple de
[`tp4-gouvernance/`]({{ site.repository_url }}/tree/main/tp4-gouvernance).

## Correspondance des termes

| Contrôle | GitLab | GitHub |
| --- | --- | --- |
| Proposer un changement | Merge request (MR) | Pull request (PR) |
| Protéger `main` | Protected branches / Branch rules | Branch protection rules ou Rulesets |
| Revue obligatoire | Règles d'approbation et CODEOWNERS, selon l'offre | « Require review from Code Owners », avec des équipes d'organisation |
| CI | `.gitlab-ci.yml`, pipeline de MR, runner GitLab | `.github/workflows/*.yml`, événement `pull_request`, runner Actions |
| Secrets | Variables CI protégées et masquées | Secrets and variables → Actions ; Environments avec approbation |
| Bloquer la fusion | Pipeline réussie exigée | Required status checks, par nom de job |
| Contributions non fiables | Variables protégées absentes des MR de fork | Secrets absents des PR de fork ; jamais `pull_request_target` avec exécution du code proposé |
| Fichier de propriété | `CODEOWNERS` à la racine, groupes `@groupe` | `CODEOWNERS` à la racine ou dans `.github/`, équipes `@organisation/equipe` |

Dans les deux cas, un fichier `CODEOWNERS` seul n'impose rien : la règle qui rend
la revue bloquante se configure sur la branche protégée et se vérifie sur une
vraie MR ou PR. Certaines protections dépendent de l'offre : GitLab Premium pour
les approbations par propriétaires, GitHub Team ou Enterprise pour protéger les
branches d'un dépôt privé. Le formateur indique ce qui est disponible ; ce qui ne
l'est pas se documente comme une limite, jamais comme une protection acquise.

## Ce que les deux variantes exigent

- `main` protégé et push direct refusé, preuve conservée.
- Une génération **réelle** depuis le Skill du checkout de la MR ou de la PR, pas
  un serveur préfabriqué ni un cache.
- Un blocage rouge sur une mutation contrôlée, puis un retour au vert par correction,
  sans retirer de test.
- Le juge du TP3 exécuté, ou une démonstration explicitement partielle.
- Le chargement du Skill autorisé pour un agent et refusé pour un autre.
- Un protocole de test de suppression défini avec le binôme.

La fiche du TP4 dans le support et le squelette de CI du dépôt valent pour les
deux variantes : `tp4-gouvernance/.gitlab-ci.yml` pour GitLab,
`tp4-gouvernance/.github/workflows/evaluer-le-skill.yml` pour GitHub. Les deux
contiennent un `echo` TODO : aucune génération n'a lieu en l'état.
