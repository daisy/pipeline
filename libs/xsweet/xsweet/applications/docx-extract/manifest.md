

#### EXTRACT-docx.xsl

XSLT stylesheet version 3.0 (5 templates)

XSweet: 'wrapper' XSLT for docx extraction with cleanup; using this XSLT in a 3.0 processor replaces five calls to distinct XSLTs. [1]

Input: a WordML document.xml file as extracted from .docx input, with its related (neighbor) files in place

Output: HTML Typescript - fairly clean and regular HTML

Declared dependency: `docx-html-extract.xsl`

Declared dependency: `handle-notes.xsl`

Declared dependency: `scrub.xsl`

Declared dependency: `join-elements.xsl`

Declared dependency: `collapse-paragraphs.xsl`

#### docx-html-extract.xsl

XSLT stylesheet version 3.0 (72 templates)

XSweet: step 1 of docx extraction - pulling the main text, notes and styles.... [3a]

Input: a WordML document.xml file as extracted from .docx input, with its related (neighbor) files in place

Output: Spammy HTML, pretty cruddy, expect to perform cleanup ...

Compile-time dependency (xsl:include) `docx-table-extract.xsl`

#### handle-notes.xsl

XSLT stylesheet version 3.0 (8 templates)

XSweet: notes cleanup, step 2 of regular docx extraction .... [3b]

Input: A messy noisy HTML document straight out of docx-extract.xsl

Output: A copy, with some regularization with respect specifically to footnotes and endnotes ...

Runtime parameter ``footnote-format`` as xs:string

Runtime parameter ``endnote-format`` as xs:string

#### scrub.xsl

XSLT stylesheet version 3.0 (9 templates)

XSweet: "Scrub" cleanup in service of docx-extraction, usually step 3 .... [3c]

Input: A messy noisy HTML document needing streamlining and cleanup.

Output: A copy, with improvements.

Note: the rule in the extraction XSLT is "make an element for anything" even if it hasn't been mapped - this step has a chance to wipe this up, and does so for certain elements known to be innocuous. Occasionally new such elements may need to be matched in this XSLT (detect them by invalid HTML downstream, with unknown element types).

#### join-elements.xsl

XSLT stylesheet version 3.0 (8 templates)

XSweet: Further reduces haphazard redundancy in markup by joining adjacent elements with similar properties .... [3d]

Input: A messy noisy HTML document needing (yet more) streamlining and cleanup.

Output: A copy, with improvements.

#### collapse-paragraphs.xsl

XSLT stylesheet version 3.0 (9 templates)

XSweet: Further removal of redundant expression of formatting properties, especially in service of subsequent heuristics (where we need to see properties on paragraphs, not only their contents objects) .... [3e]

Input: A messy noisy HTML document needing (yet more and even more) streamlining and cleanup.

Output: A copy, with improvements.

#### docx-html-extract-mini.xsl

XSLT stylesheet version 3.0 (61 templates)

XSweet: an EXPERIMENTAL single-pass reduced docx extraction for further development. [ZZ]

Compile-time dependency (xsl:include) `docx-table-extract.xsl`

#### docx-html-extract-old.xsl

XSLT stylesheet version 2.0 (55 templates)

#### docx-html-extract-saxon-shell.xsl

XSLT stylesheet version 2.0 (1 template)

Compile-time dependency (xsl:import) `docx-html-extract.xsl`

Runtime parameter ``docx-file-uri`` as xs:string

Runtime parameter ``show-css`` as xs:string

#### docx-table-extract.xsl

XSLT stylesheet version 3.0 (14 templates)

#### quickndirty2.xsl

XSLT stylesheet version 2.0 (21 templates)

XSweet: one of the earliest docx extraction XSLTs, kept here for historical reasons. It's standalone!

Runtime parameter ``show-css`` as xs:string

#### docx-document-production.xpl

XProc pipeline version 1.0 (6 steps)

Runtime dependency: `docx-html-extract.xsl`

Runtime dependency: `handle-notes.xsl`

Runtime dependency: `scrub.xsl`

Runtime dependency: `join-elements.xsl`

Runtime dependency: `collapse-paragraphs.xsl`