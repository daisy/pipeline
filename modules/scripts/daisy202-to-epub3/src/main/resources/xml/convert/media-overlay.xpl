<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:mo="http://www.w3.org/ns/SMIL"
                xmlns:epub="http://www.idpf.org/2007/ops"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                type="pxi:daisy202-to-epub3-mediaoverlay" name="mediaoverlay">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p px:role="desc">For processing the SMILs.</p>
    </p:documentation>

    <p:input port="daisy-smil" sequence="true" primary="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">The DAISY 2.02 SMIL-files.</p:documentation>
    </p:input>
    <p:input port="content" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">The EPUB3 Content Documents.</p:documentation>
    </p:input>

    <p:output port="mediaoverlay" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">The EPUB3 Media Overlays.</p:documentation>
    </p:output>

    <p:option name="daisy-dir" required="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p px:role="name">URI to the DAISY 2.02 files.</p>
            <pre><code class="example">file:/home/user/daisy202/</code></pre>
        </p:documentation>
    </p:option>
    <p:option name="publication-dir" required="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p px:role="desc">URI to the EPUB3 Publication directory.</p>
            <pre><code class="example">file:/home/user/epub3/epub/Publication/</code></pre>
        </p:documentation>
    </p:option>
    <p:option name="content-dir" required="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p px:role="name">URI to the EPUB3 Content directory.</p>
            <pre><code class="example">file:/home/user/epub3/epub/Publication/Content/</code></pre>
        </p:documentation>
    </p:option>
    <p:option name="include-mediaoverlay" required="true" cx:as="xs:string">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p px:role="desc">Whether or not to include media overlays. Can be either 'true' or 'false'.</p>
        </p:documentation>
    </p:option>

    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
        <p:documentation>
            px:message
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">For manipulating filesets.</p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/smil-utils/library.xpl">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>For manipulating media overlays.</p>
            <ol>
                <li>px:smil-upgrade</li>
                <li>px:mediaoverlay-join</li>
                <li>px:mediaoverlay-rearrange</li>
            </ol>
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
        <p:documentation>
            px:set-base-uri
        </p:documentation>
    </p:import>

    <p:for-each name="daisy-smil-iterate">
        <p:iteration-source>
            <p:pipe port="daisy-smil" step="mediaoverlay"/>
        </p:iteration-source>
        <p:variable name="original-uri" select="base-uri(/*)"/>
        <px:smil-upgrade version="3.0"/>
        <px:message message="upgraded the SMIL file $1">
            <p:with-option name="param1" select="$original-uri"/>
        </px:message>
        <px:set-base-uri>
            <p:with-option name="base-uri" select="$original-uri"/>
        </px:set-base-uri>
        <p:add-xml-base/>
    </p:for-each>
    <px:mediaoverlay-join name="mediaoverlay-joined"/>
    <px:message message="joined all the media overlays"/>
    <p:sink/>

    <p:choose>
        <p:when test="$include-mediaoverlay='true'">
            <p:xpath-context>
                <p:empty/>
            </p:xpath-context>

            <p:for-each>
                <p:iteration-source>
                    <p:pipe port="content" step="mediaoverlay"/>
                </p:iteration-source>
                <p:variable name="content-result-uri" select="base-uri(/*)"/>
                <p:variable name="result-uri" select="replace($content-result-uri,'xhtml$','smil')"/>
                <px:set-base-uri>
                    <p:with-option name="base-uri" select="/*/@original-href"/>
                </px:set-base-uri>
                <p:add-xml-base/>
                <p:add-attribute match="/*" attribute-name="original-href">
                    <p:with-option name="attribute-value" select="$result-uri"/>
                </p:add-attribute>
            </p:for-each>
            <p:identity name="content"/>

            <px:mediaoverlay-rearrange>
                <p:input port="mediaoverlay">
                    <p:pipe port="result" step="mediaoverlay-joined"/>
                </p:input>
                <p:input port="content">
                    <p:pipe port="result" step="content"/>
                </p:input>
            </px:mediaoverlay-rearrange>
            <px:message message="SMIL fragments have been rearranged according to the content order"/>

            <p:for-each>
                <px:set-base-uri>
                    <p:with-option name="base-uri" select="/*/@original-href"/>
                </px:set-base-uri>
                <p:add-xml-base/>
                <p:xslt name="rearrange.mediaoverlay-annotated">
                    <p:input port="parameters">
                        <p:empty/>
                    </p:input>
                    <p:input port="stylesheet">
                        <p:inline>
                            <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" xmlns:mo="http://www.w3.org/ns/SMIL">
                                <xsl:template match="@*|node()">
                                    <xsl:copy>
                                        <xsl:apply-templates select="@*|node()"/>
                                    </xsl:copy>
                                </xsl:template>
                                <xsl:template match="mo:text[@src]">
                                    <xsl:copy>
                                        <xsl:copy-of select="@*"/>
                                        <xsl:attribute name="src" select="replace(@src,'^(.+)\.[^\.]*#(.*)$','$1.xhtml#$2')"/>
                                        <xsl:apply-templates select="node()"/>
                                    </xsl:copy>
                                </xsl:template>
                                <xsl:template match="*[@epub:textref]">
                                    <xsl:copy>
                                        <xsl:apply-templates select="@*"/>
                                        <xsl:attribute name="epub:textref" select="replace(@epub:textref,'^(.+)\.[^\.]*#(.*)$','$1.xhtml#$2')"/>
                                        <xsl:apply-templates select="node()"/>
                                    </xsl:copy>
                                </xsl:template>
                            </xsl:stylesheet>
                        </p:inline>
                    </p:input>
                </p:xslt>
                <px:message message="updated text references in $1">
                    <p:with-option name="param1" select="replace(base-uri(/*),'.*/','')"/>
                </px:message>
            </p:for-each>

        </p:when>
        <p:otherwise>
            <px:message message="No SMIL files will be included in result fileset"/>
            <p:identity>
                <p:input port="source">
                    <p:empty/>
                </p:input>
            </p:identity>
        </p:otherwise>
    </p:choose>

</p:declare-step>
