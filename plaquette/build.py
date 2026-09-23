#!/usr/bin/env python3
"""Génère ../Plaquette_IA-PLT-USINE.pdf à partir de plaquette.html.

Dépendance : PyMuPDF (pip install pymupdf). Usage : python3 build.py
"""
from pathlib import Path

import fitz  # PyMuPDF

HERE = Path(__file__).parent
SRC = HERE / "plaquette.html"
OUT = HERE.parent / "Plaquette_IA-PLT-USINE.pdf"

CSS = """
body { font-family: sans-serif; font-size: 10pt; color: #000000; line-height: 1.2; }
p { margin: 0 0 5pt 0; }
.eyebrow { color: #0f766e; font-weight: bold; font-size: 9.5pt; margin: 0 0 2pt 0; }
h1 { color: #1b1f3b; font-size: 19pt; margin: 0 0 3pt 0; }
.subtitle { color: #475569; font-style: italic; font-size: 12.5pt; margin: 0 0 8pt 0; }
h2 { color: #1b1f3b; font-size: 12.5pt; margin: 9pt 0 4pt 0; }
ul { margin: 0 0 3pt 0; }
li { margin: 0 0 1pt 0; }
table { border: 0.5pt solid #d6dee8; margin: 1pt 0 4pt 0; }
td, th { padding: 3pt 6pt; border: 0.5pt solid #d6dee8; font-size: 10pt; vertical-align: top; }
th { color: #1b1f3b; text-align: left; font-weight: bold; border-bottom: 1pt solid #1b1f3b; }
td.k { color: #1b1f3b; font-weight: bold; width: 118pt; }
.jour { color: #0f766e; font-weight: bold; font-size: 11pt; margin: 5pt 0 3pt 0; }
.mod { margin: 0 0 3pt 6pt; }
.mod b { color: #1b1f3b; }
.mod .d { color: #475569; }
.code { font-family: monospace; font-size: 9pt; }
hr { border: 0; border-top: 0.75pt solid #d6dee8; margin: 10pt 0 6pt 0; }
.footer { color: #0f766e; font-weight: bold; font-size: 9pt; }
"""


def main() -> None:
    story = fitz.Story(html=SRC.read_text(encoding="utf-8"), user_css=CSS)
    writer = fitz.DocumentWriter(str(OUT))
    mediabox = fitz.paper_rect("a4")
    where = mediabox + (60, 50, -60, -50)
    more = True
    while more:
        device = writer.begin_page(mediabox)
        more, _ = story.place(where)
        story.draw(device)
        writer.end_page()
    writer.close()

    doc = fitz.open(str(OUT))
    doc.set_metadata({
        "title": "Industrialiser et gouverner l'IA générative — IA-PLT-USINE",
        "author": "SCIAM",
        "subject": "Plaquette de formation",
        "creator": "plaquette/build.py",
    })
    doc.saveIncr()
    print(f"{OUT} — {len(doc)} page(s)")


if __name__ == "__main__":
    main()
