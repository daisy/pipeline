

#### header-promotion-CHOOSE.xsl

XSLT stylesheet version 3.0 (7 templates)

XSweet: top level 'macro XSLT' stylesheet for dynamic dispatch of header promotion logic [1]

Input: an HTML Typescript document (wf)

Output: a copy, with headers promoted according to the logic selected

Note: runtime parameter `method` may be "ranked-format", "outline-level", or the name of an (XML) configuration file; if the method is not designated the XSLT falls back to "outline-level" (when such headers are detected) or "ranked-format" (when they are not)

Runtime parameter ``method`` as xs:string

Declared dependency: `make-header-mapper-xslt.xsl`

Declared dependency: `outline-headers.xsl`

Declared dependency: `digest-paragraphs.xsl`

Declared dependency: `make-header-escalator-xslt.xsl`

Declared dependency: `outline-headers.xsl`

Declared dependency: `digest-paragraphs.xsl`

Declared dependency: `make-header-escalator-xslt.xsl`

#### outline-headers.xsl

XSLT stylesheet version 2.0 (2 templates)

XSweet: Performs header promotion based on outline level [2]

Input: an HTML Typescript document (wf)

Output: a copy, with headers promoted according to outline levels detected on paragraphs

#### digest-paragraphs.xsl

XSLT stylesheet version 2.0 (11 templates)

XSweet: paragraph property analysis in support of header promotion: header promotion step 1 [3a]

Input: an HTML typescript file

Output: an XML file showing the results of analysis, for input to `make-header-escalator.xsl`

Note: not sorting yet these are in arbitrary order.

#### make-header-escalator-xslt.xsl

XSLT stylesheet version 2.0 (2 templates)

XSweet: produces header promotion XSLT from analyzed (crunched) inputs, for the 'property-based' header promotion pathway [3b]

Input: results of running `digest-paragraphs.xsl` on (wf) HTML input

Output: an XSLT suitable for running on the same (original) input to produce a copy with headers promoted

Runtime parameter ``debug-mode`` as xs:string

#### make-header-mapper-xslt.xsl

XSLT stylesheet version 2.0 (4 templates)

XSweet: produces header promotion/mapping XSLT from a configuration file (XML), for the 'mapping-based' header promotion pathway [4]

Input: A configuration file such as the included `config-mockup.xml`

Output: a copy, with headers promoted according to the mapping

#### html-header-promote.xpl

XProc pipeline version 1.0 (5 steps)

XSweet: Apply header promotion XSLT chain into a single XProc call

Input: an HTML typescript file with no headers

Output: a copy, with headers promoted

Note: unlike the 'chooser' header promotion macro XSLT, this XProc is wired up to 'property-based' header promotion.

Runtime dependency: `digest-paragraphs.xsl`

Runtime dependency: `make-header-escalator-xslt.xsl`

#### config-mockup.xml