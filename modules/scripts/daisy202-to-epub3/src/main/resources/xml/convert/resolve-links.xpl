<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:html="http://www.w3.org/1999/xhtml"
                type="pxi:daisy202-to-epub3-resolve-links" name="resolve-links">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p px:role="desc">De-references links in content documents.</p>
        <p>In DAISY 2.02, links point to other locations in the book via the SMIL files. For instance, a link in ncc.html might point to content.smil#fragment, and the SMIL clip at
            content.smil#fragment might point to content.html#id. This step would change the original link from content.smil#fragment to content.html#id.</p>
    </p:documentation>

    <p:input port="source" primary="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p px:role="desc">A DAISY 2.02 content document.</p>
            <pre><code class="example">
                <html xmlns="http://www.w3.org/1999/xhtml" xml:base="file:/home/user/daisy202/ncc.html">
                    <head>...</head>
                    <body> ... <a href="a.smil#fragment"/> ... </body>
                </html>
            </code></pre>
        </p:documentation>
    </p:input>
    <p:input port="resolve-links-mapping">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p px:role="desc">A map of all the links in the SMIL files.</p>
            <pre><code class="example">
                <di:mapping xmlns:di="http://www.daisy.org/ns/pipeline/tmp">
                    <di:smil xml:base="file:/home/user/a.smil">
                        <di:text par-id="fragment1" text-id="frg1" src="a.html#txt1"/>
                        <di:text par-id="fragment2" text-id="frg2" src="a.html#txt2"/>
                    </di:smil>
                    <di:smil xml:base="file:/home/user/b.smil">
                        <di:text par-id="fragment1" text-id="frg1" src="b.html#txt1"/>
                        <di:text par-id="fragment2" text-id="frg2" src="b.html#txt2"/>
                    </di:smil>
                </di:mapping>
            </code></pre>
        </p:documentation>
    </p:input>
    <p:output port="result">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p px:role="desc">The resulting DAISY 2.02 content document.</p>
            <pre><code class="example">
                <html xmlns="http://www.w3.org/1999/xhtml" xml:base="file:/home/user/daisy202/ncc.html">
                    <head>...</head>
                    <body> ... <a href="a.html#id"/> ... </body>
                </html>
            </code></pre>
        </p:documentation>
    </p:output>

    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>

    <p:variable name="content-base" select="base-uri(/*)"/>

    <p:xslt>
        <p:with-param name="content-base" select="$content-base"/>
        <p:input port="source">
            <p:pipe step="resolve-links" port="source"/>
            <p:pipe step="resolve-links" port="resolve-links-mapping"/>
        </p:input>
        <p:input port="stylesheet">
            <p:inline>
                <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                                xmlns:pf="http://www.daisy.org/ns/pipeline/functions">
                    <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/uri-functions.xsl"/>
                    <xsl:param name="content-base" required="yes"/>
                    <xsl:variable name="content-filename" select="replace(replace($content-base,'^.*/([^/]+)$','$1'),'^([^#]+)#.*$','$1')"/>
                    <xsl:variable name="content-filename-position" select="string-length($content-filename)+1"/>
                    <xsl:variable name="resolve-links-mapping" select="collection()[2]"/>
                    <xsl:template match="*">
                        <xsl:copy>
                            <xsl:copy-of select="@*"/>
                            <xsl:apply-templates/>
                        </xsl:copy>
                    </xsl:template>
                    <xsl:template match="html:a">
                        <xsl:variable name="original-uri" select="resolve-uri(tokenize(@href,'#')[1],$content-base)"/>
                        <xsl:variable name="original-fragment"
                                      select="if (contains(@href,'#')) then tokenize(@href,'#')[last()] else ''"/>
                        <xsl:variable name="result"
                                      select="$resolve-links-mapping/*/*[base-uri(.)=$original-uri]
                                                                    /*[(@par-id,@text-id)=$original-fragment]/@src"/>
                        <xsl:copy>
                            <xsl:copy-of select="@* except @xml:base"/>
                            <xsl:if test="exists($result)">
                                <xsl:variable name="href" select="pf:relativize-uri($result,$content-base)"/>
                                <!--
                                    pf:relativize-uri('foo.html#bar', 'foo.html') != '#bar'
                                -->
                                <xsl:attribute name="href" select="if (starts-with($href,concat($content-filename,'#')))
                                                                   then substring($href,$content-filename-position)
                                                                   else $href"/>
                            </xsl:if>
                            <xsl:apply-templates/>
                        </xsl:copy>
                    </xsl:template>
                </xsl:stylesheet>
            </p:inline>
        </p:input>
    </p:xslt>
    <px:message message="dereferenced all links in $1">
        <p:with-option name="param1" select="$content-base"/>
    </px:message>
    <p:xslt>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="resolve-links.xsl"/>
        </p:input>
    </p:xslt>

</p:declare-step>
