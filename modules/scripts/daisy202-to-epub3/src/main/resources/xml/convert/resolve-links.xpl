<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal" xmlns:html="http://www.w3.org/1999/xhtml"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc" type="pxi:daisy202-to-epub3-resolve-links" name="resolve-links" version="1.0">

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
    <p:variable name="content-filename" select="replace(replace($content-base,'^.*/([^/]+)$','$1'),'^([^#]+)#.*$','$1')"/>
    <p:variable name="content-filename-position" select="string-length($content-filename)+1"/>

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">For each 'a'-link</p:documentation>
    <p:viewport match="//html:a">
        <p:variable name="original-uri" select="resolve-uri(tokenize(/*/@href,'#')[1],$content-base)"/>
        <p:variable name="original-fragment" select="if (contains(/*/@href,'#')) then tokenize(/*/@href,'#')[last()] else ''"/>
        <p:variable name="result" select="/*/*[base-uri(.)=$original-uri]/*[(@par-id,@text-id)=$original-fragment]/@src">
            <p:pipe port="resolve-links-mapping" step="resolve-links"/>
        </p:variable>

        <p:choose>
            <p:when test="$result != ''">
                <p:xslt>
                    <p:with-param name="from" select="$content-base"/>
                    <p:with-param name="to" select="$result"/>
                    <p:with-param name="filename" select="$content-filename"/>
                    <p:with-param name="filename-position" select="$content-filename-position"/>
                    <p:input port="stylesheet">
                        <p:inline>
                            <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:pf="http://www.daisy.org/ns/pipeline/functions" version="2.0">
                                <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/uri-functions.xsl"/>
                                <xsl:param name="from" required="yes"/>
                                <xsl:param name="to" required="yes"/>
                                <xsl:param name="filename" required="yes"/>
                                <xsl:param name="filename-position" required="yes"/>
                                <xsl:template match="/*">
                                    <xsl:copy>
                                        <xsl:copy-of select="@*"/>
                                        <xsl:attribute name="href" select="pf:relativize-uri($to,$from)"/>
                                        <xsl:if test="starts-with(/*/@href,$filename)">
                                            <xsl:attribute name="href" select="substring(/*/@href,$filename-position)"/>
                                        </xsl:if>
                                        <xsl:copy-of select="node()"/>
                                    </xsl:copy>
                                </xsl:template>
                            </xsl:stylesheet>
                        </p:inline>
                    </p:input>
                </p:xslt>
            </p:when>
            <p:otherwise>
                <p:identity/>
            </p:otherwise>
        </p:choose>
        <p:delete match="/*/@xml:base"/>
    </p:viewport>
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
