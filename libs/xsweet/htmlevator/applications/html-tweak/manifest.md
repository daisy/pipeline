

#### APPLY-html-tweaks.xsl

XSLT stylesheet version 3.0 (7 templates)

XSweet: A generalized HTML modifier with a configurable driver. Use to clean up and improve HTML. [1]

Input: an HTML typescript file.

Output: a copy, except with tweaks to the HTML as specified by the configuration.

Note: runtime parameter `config` enables naming a config file. Its name must be suffixed `xml`. See the file `html-tweak-map.xml` for an example. The configuration provides for matching elements in the HTML based on regularities in 'style' or 'class' assignment.

Runtime parameter ``config`` as xs:string

Declared dependency: `make-html-tweak-xslt.xsl`

#### html-tweak-demo.xsl

XSLT stylesheet version 2.0 (3 templates)

XSweet: An *example* of an XSLT produced for the generalized HTML Tweak operation.

Input: (presumably) HTML Typescript

Output: a copy, with (demo) tweaks

Note: This XSLT was produced from (a version of) html-tweak-map.xml as an example of HTML Tweak logic. It is saved here as a demonstration.

Compile-time dependency (xsl:include) `html-tweak-lib.xsl`

#### html-tweak-lib.xsl

XSLT stylesheet version 2.0 (4 templates)

XSweet: Library for HTML Tweak XSLTs (must be available at runtime)

Note: this XSLT isn't run on its own: it is, however, included as a module. HTML Tweak depends on its being available.

#### make-html-tweak-xslt.xsl

XSLT stylesheet version 2.0 (9 templates)

XSweet: Dynamic XSLT production from configuration XML

Input: An HTML Tweak configuration file such as `html-tweak-map.xml`

Output: An XSLT to be applied to HTML Typescript (to achieve the HTML Tweak)

Runtime parameter ``debug-mode`` as xs:string

#### html-tweak.xpl

XProc pipeline version 1.0 (3 steps)

Runtime dependency: `make-html-tweak-xslt.xsl`

#### html-tweak-map.xml