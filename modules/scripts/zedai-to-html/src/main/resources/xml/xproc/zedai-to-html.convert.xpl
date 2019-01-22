<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                type="px:zedai-to-html"
                name="main"
                exclude-inline-prefixes="#all">

    <p:documentation>
        Transforms a ZedAI (DAISY 4 XML) document into an EPUB 3 publication.
    </p:documentation>

    <p:input port="fileset.in" primary="true"/>
    <p:input port="in-memory.in" sequence="true"/>

    <p:output port="fileset.out" primary="true">
        <p:pipe port="result" step="fileset.result"/>
    </p:output>
    <p:output port="in-memory.out" sequence="true">
        <p:pipe step="zedai-to-html" port="html-files"/>
    </p:output>

    <p:option name="output-dir" required="true"/>
    <p:option name="chunk" select="'false'"/>
    <p:option name="chunk-size" required="false" select="'-1'"/>

    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/html-utils/library.xpl"/>

    <!--=========================================================================-->
    <!-- GET ZEDAI FROM FILESET                                                  -->
    <!--=========================================================================-->

    <p:documentation>Retreive the ZedAI document from the input fileset.</p:documentation>
    <p:group name="zedai-input">
        <p:output port="result"/>
        <px:fileset-load media-types="application/z3998-auth+xml">
            <p:input port="in-memory">
                <p:pipe step="main" port="in-memory.in"/>
            </p:input>
        </px:fileset-load>
        <!-- TODO: describe the error on the wiki and insert correct error code -->
        <px:assert message="No XML documents with the ZedAI media type ('application/z3998-auth+xml') found in the fileset."
                   test-count-min="1" error-code="PEZE00"/>
        <px:assert message="More than one XML document with the ZedAI media type ('application/z3998-auth+xml') found in the fileset; there can only be one ZedAI document."
                   test-count-max="1" error-code="PEZE00"/>
    </p:group>

    <!--=========================================================================-->
    <!-- CONVERT TO XHTML                                                        -->
    <!--=========================================================================-->

    <p:documentation>Convert the ZedAI Document into several XHTML Documents</p:documentation>
    <p:group name="zedai-to-html">
        <p:output port="result" primary="true" sequence="true"/>
        <p:output port="html-files" sequence="true">
            <p:pipe port="html-files" step="zedai-to-html.iterate"/>
        </p:output>
        <p:variable name="zedai-basename"
            select="replace(replace(//*[@media-type='application/z3998-auth+xml']/@href,'^.+/([^/]+)$','$1'),'^(.+)\.[^\.]+$','$1')">
            <p:pipe port="fileset.in" step="main"/>
        </p:variable>
        <p:variable name="result-basename" select="concat($output-dir,$zedai-basename,'.xhtml')"/>
        <p:xslt name="zedai-to-html.html-single">
            <p:input port="source">
                <p:pipe port="result" step="zedai-input"/>
            </p:input>
            <p:input port="stylesheet">
                <p:document href="http://www.daisy.org/pipeline/modules/zedai-to-html/xslt/zedai-to-html.xsl"/>
            </p:input>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
        </p:xslt>
        <px:set-base-uri>
            <p:with-option name="base-uri" select="$result-basename"/>
        </px:set-base-uri>
        <p:choose>
            <p:when test="$chunk='true'">
                <p:output port="result" sequence="true"/>
                <px:html-chunker>
                    <p:with-option name="max-chunk-size" select="$chunk-size"/>
                </px:html-chunker>
            </p:when>
            <p:otherwise>
                <p:output port="result" sequence="true"/>
                <p:identity/>
            </p:otherwise>
        </p:choose>
        <p:for-each name="zedai-to-html.iterate">
            <p:output port="fileset" primary="true"/>
            <p:output port="html-files" sequence="true">
                <p:pipe port="result" step="zedai-to-html.iterate.html"/>
            </p:output>
            <p:variable name="result-uri" select="base-uri(/*)"/>
            <p:identity name="zedai-to-html.iterate.html"/>
            <px:fileset-create>
                <p:with-option name="base" select="$output-dir"/>
            </px:fileset-create>
            <px:fileset-add-entry media-type="application/xhtml+xml">
                <p:with-option name="href" select="$result-uri"/>
            </px:fileset-add-entry>
        </p:for-each>
    </p:group>
    
    <!--=========================================================================-->
    <!-- CONSOLIDATE THE OUTPUT FILESET                                          -->
    <!--=========================================================================-->

    <!--Construct the resources fileset-->
    <p:identity>
        <p:input port="source">
            <p:pipe step="main" port="fileset.in"/>
        </p:input>
    </p:identity>
    <p:group name="resources">
        <p:output port="result"/>
        <p:variable name="fileset-base" select="base-uri(/*)"/>
        <p:variable name="zedai-uri" select="resolve-uri(//d:file[@media-type='application/z3998-auth+xml']/@href,$fileset-base)"/>
        <p:delete match="d:file[@media-type='application/z3998-auth+xml']"/>
        <p:viewport match="/*/*">
            <p:documentation>Make sure that the files in the fileset is relative to the ZedAI file.</p:documentation>
            <p:variable name="original-uri" select="(/*/@original-href, resolve-uri(/*/@href,$fileset-base))[1]"/>
            <p:variable name="uri" select="resolve-uri(/*/@href,$fileset-base)"/>
            <p:variable name="base" select="$zedai-uri"/>
            <px:assert message="The URI to be made relative should not be empty (original URI: $1 | URI: $2 | ZedAI base: $3)">
                <p:with-option name="param1" select="$original-uri"/>
                <p:with-option name="param2" select="$uri"/>
                <p:with-option name="param3" select="$base"/>
                <p:with-option name="test" select="not($uri='')"/>
            </px:assert>
            <px:assert message="The base URI that the resource hrefs should be relative against should not be empty (original URI: $1 | URI: $2 | ZedAI base: $3)">
                <p:with-option name="param1" select="$original-uri"/>
                <p:with-option name="param2" select="$uri"/>
                <p:with-option name="param3" select="$base"/>
                <p:with-option name="test" select="not($base='')"/>
            </px:assert>
            <p:xslt>
                <p:with-param name="uri" select="$uri"/>
                <p:with-param name="base" select="$base"/>
                <p:input port="stylesheet">
                    <p:inline>
                        <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:pf="http://www.daisy.org/ns/pipeline/functions" version="2.0">
                            <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/uri-functions.xsl"/>
                            <xsl:param name="uri" required="yes"/>
                            <xsl:param name="base" required="yes"/>
                            <xsl:template match="/*">
                                <xsl:copy>
                                    <xsl:copy-of select="@*"/>
                                    <xsl:attribute name="href" select="pf:relativize-uri($uri,$base)"/>
                                </xsl:copy>
                            </xsl:template>
                        </xsl:stylesheet>
                    </p:inline>
                </p:input>
            </p:xslt>
            <p:identity/>
        </p:viewport>
        <p:add-attribute match="/*" attribute-name="xml:base">
            <p:with-option name="attribute-value" select="$output-dir"/>
        </p:add-attribute>
        <!-- TODO: remove resources from fileset that are not referenced from any of the in-memory files -->
    </p:group>
    
    <px:fileset-join name="package-doc.join-filesets">
        <p:input port="source">
            <p:pipe port="result" step="zedai-to-html"/>
            <p:pipe port="result" step="resources"/>
        </p:input>
    </px:fileset-join>
    
    <p:group name="fileset.result">
        <p:output port="result"/>
        <p:variable name="fileset-base" select="base-uri(/*)"/>
        <p:identity name="fileset.dirty"/>
        <p:wrap-sequence wrapper="wrapper">
            <p:input port="source">
                <p:pipe step="zedai-to-html" port="html-files"/>
            </p:input>
        </p:wrap-sequence>
        <p:delete match="/*/*/*" name="wrapped-in-memory"/>
        <p:identity>
            <p:input port="source">
                <p:pipe port="result" step="fileset.dirty"/>
            </p:input>
        </p:identity>
        <p:viewport match="//d:file">
            <p:variable name="file-href" select="resolve-uri(/*/@href,$fileset-base)"/>
            <p:variable name="file-original" select="if (/*/@original-href) then resolve-uri(/*/@original-href) else ''"/>
            <p:choose>
                <p:xpath-context>
                    <p:pipe port="result" step="wrapped-in-memory"/>
                </p:xpath-context>
                <p:when test="not($file-original) and not(/*/*[resolve-uri(base-uri(.)) = $file-href])">
                    <!-- Fileset contains file reference to a file that is neither stored on disk nor in memory; discard it -->
                    <p:sink/>
                    <p:identity>
                        <p:input port="source">
                            <p:empty/>
                        </p:input>
                    </p:identity>
                    <px:message>
                        <p:with-option name="message" select="concat('skipping ',$file-href)"/>
                    </px:message>
                </p:when>
                <p:otherwise>
                    <!-- File refers to a document on disk or in memory; keep it -->
                    <p:identity/>
                </p:otherwise>
            </p:choose>
        </p:viewport>
    </p:group>

</p:declare-step>
