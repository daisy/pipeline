# Various XSweet applications live here

Each directory contains one or several XSLT transformations. Some are experimental and will not provide satisfactory results, so do not be dismayed if not everything makes sense. Usually the way forward will be evident by clues left in the set of XSLTs themselves, in their comments or in accompanying readme docs.

Typically you will be constructing a chain of these transformations calling XSLTs from several different subdirectories. So, for example:

(starting with the extracted `document.xml`, transform with)

* docx-extract/EXTRACT-docx.xsl
* list-promote/PROMOTE-lists.xsl
* local-fixup/hyperlink-inferencer.xsl
* html-polish/final-rinse.xsl
* XSweet/XSweet/applications/html-polish/xhtml-serialize.xsl
* XSweet/HTMLevator/applications/header-promote/header-promotion-CHOOSE.xsl

Every step but the first, consumes (reads) an HTML-tagged document (albeit in XML syntax for convenience) and produces an (xml-well-formed) HTML document. (A tag-abbreviated HTML5 output may be produced as a terminal step, if wanted.)

Note: it's a convention in this project to name stylesheets with components of their filenames in ALL CAPS when these stylesheets use the XPath 3.0 function transform(), thus achieving "meta-stylesheet" status (inasmuch as they do not merely transform, they also orchestrate and execute transformations). For example, the 

## `css-abstract`

Does its best to rewrite style properties into CSS. YMMV.

## `docx-extract`

Pulls HTML out of .docx. Assumes `document.xml` as the (primary) source document. Several XSLTs here may also make reference to other XML documents in the (docx, unzipped) repository, such as `styles.xml`, `footnotes.xml` and the like.

See the readme for more info.

## `html-polish`

Steps expected to be final or near-final.

## `list-promote`

Make HTML `ol` and `ul` from WordML inputs (marked as lists items).

## `local-fixup`

## `produce-analysis`

## `produce-plaintext`
