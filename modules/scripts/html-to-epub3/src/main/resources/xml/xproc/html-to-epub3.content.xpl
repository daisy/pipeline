<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                type="pxi:html-to-epub3-content" name="main">

    <p:input port="html" sequence="true" primary="true"/>
    <p:input port="fileset.in.resources"/>
    <p:output port="docs" sequence="true" primary="true">
        <p:pipe port="result" step="docs"/>
    </p:output>
    <p:output port="resources" sequence="true">
        <p:pipe port="in-memory.out" step="resources"/>
    </p:output>
    <p:output port="fileset.out.docs" primary="false">
        <p:pipe port="result" step="fileset.docs"/>
    </p:output>
    <p:output port="fileset.out.resources" primary="false">
        <p:pipe port="fileset.out" step="resources"/>
    </p:output>


    <p:option name="publication-dir" required="true"/>
    <p:option name="content-dir" required="true"/>

    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
        <p:documentation>
            px:set-base-uri
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    <p:import href="convert-diagram-descriptions.xpl"/>


    <!--TODO if single doc, chunk; else keep original chunking-->


    <!--=========================================================================-->
    <!-- FILESET CLEANUP                                                         -->
    <!--=========================================================================-->

    <p:group name="resources">
        <p:output port="fileset.out" primary="true"/>
        <p:output port="in-memory.out">
            <p:pipe port="in-memory.out" step="diagram-descriptions"/>
        </p:output>
        <p:xslt>
            <p:input port="source">
                <p:pipe port="fileset.in.resources" step="main"/>
            </p:input>
            <p:input port="stylesheet">
                <p:document href="../xslt/fileset-clean-resources.xsl"/>
            </p:input>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
        </p:xslt>
        <pxi:convert-diagram-descriptions name="diagram-descriptions">
            <p:with-option name="content-dir" select="$content-dir"/>
        </pxi:convert-diagram-descriptions>
        <px:set-base-uri>
            <p:with-option name="base-uri" select="$content-dir"/>
        </px:set-base-uri>
        <p:add-xml-base/>
    </p:group>
    <p:sink/>

    <!--=========================================================================-->
    <!-- XHTML CLEANUP                                                           -->
    <!--=========================================================================-->
    <p:group name="docs">
        <p:output port="result" sequence="true"/>
        <p:for-each>
            <p:iteration-source>
                <p:pipe port="html" step="main"/>
            </p:iteration-source>

            <!--TODO remove http-equiv='content-type'-->

            <!--–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––>
             |   UPGRADE TO XHTML5                                                         |
            <|–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––-->
            <p:xslt name="html-upgrade">
                <p:input port="stylesheet">
                    <p:document href="http://www.daisy.org/pipeline/modules/html-utils/html5-upgrade.xsl"/>
                </p:input>
                <p:input port="parameters">
                    <p:empty/>
                </p:input>
            </p:xslt>

            <!--–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––>
             |   CLEAN RESOURCE REFERENCES                                                        |
            <|–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––-->
            <p:xslt>
                <p:input port="source">
                    <p:pipe port="result" step="html-upgrade"/>
                    <p:pipe port="fileset.out" step="resources"/>
                </p:input>
                <p:input port="stylesheet">
                    <p:document href="../xslt/html-clean-resources.xsl"/>
                </p:input>
                <p:input port="parameters">
                    <p:empty/>
                </p:input>
            </p:xslt>

            <!--–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––>
             |   CLEAN HTTP-EQUIV                                                          |
            <|–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––-->

            <p:delete match="/h:html/h:head/h:meta[matches(@http-equiv,'Content-Type','i')]"
                xmlns:h="http://www.w3.org/1999/xhtml"/>

            <!--–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––>
             |   SET LANGUAGE                                                              |
            <|–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––-->
            <p:group>
                <p:variable name="lang"
                    select="/*/(if (@lang|@xml:lang) then (@lang|@xml:lang)
                                                    else p:system-property('p:language'))"/>
                <p:add-attribute match="/*" attribute-name="lang">
                    <p:with-option name="attribute-value" select="$lang"/>
                </p:add-attribute>
                <p:add-attribute match="/*" attribute-name="xml:lang">
                    <p:with-option name="attribute-value" select="$lang"/>
                </p:add-attribute>
            </p:group>

            <!--–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––>
             |   FIX CONTENT MODELS                                                        |
            <|–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––-->

            <p:xslt>
                <p:input port="stylesheet">
                    <p:document href="http://www.daisy.org/pipeline/modules/html-utils/html-fixer.xsl"/>
                </p:input>
                <p:input port="parameters">
                    <p:empty/>
                </p:input>
            </p:xslt>

            <!--–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––>
             |   ADD MISSING IDS                                                           |
            <|–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––-->
            <p:xslt>
                <p:input port="stylesheet">
                    <p:document href="http://www.daisy.org/pipeline/modules/html-utils/html-id-fixer.xsl"/>
                </p:input>
                <p:input port="parameters">
                    <p:empty/>
                </p:input>
            </p:xslt>

            <!--–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––>
             |   CLEAN OUTLINE                                                        |
            <|–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––-->

            <!--TODO: try to add sections where missing -->

            <!--–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––>
             |   CHANGE THE BASE URI TO A SAFE FILE NAME                                   |
            <|–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––-->
            <p:identity name="html-final"/>
            <p:xslt name="safe-uri">
                <p:input port="stylesheet">
                    <p:inline>
                        <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                            xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions"
                            xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                            xmlns:xs="http://www.w3.org/2001/XMLSchema" version="2.0">
                            <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/uri-functions.xsl"/>
                            <xsl:template match="/">
                                <c:result><xsl:value-of
                                        select="pf:replace-path(base-uri(/*),escape-html-uri(replace(pf:unescape-uri(pf:get-path(base-uri(/*))),'[^\p{L}\p{N}\-/_.]','_')))"
                                    /></c:result>
                            </xsl:template>
                        </xsl:stylesheet>
                    </p:inline>
                </p:input>
                <p:input port="parameters">
                    <p:empty/>
                </p:input>
            </p:xslt>
            <p:identity>
                <p:input port="source">
                    <p:pipe port="result" step="html-final"/>
                </p:input>
            </p:identity>
            <px:set-base-uri>
                <p:with-option name="base-uri"
                    select="resolve-uri(replace(normalize-space(.),'^.*?([^/]+)\.[^/]*$','$1.xhtml'), $content-dir)">
                    <p:pipe port="result" step="safe-uri"/>
                </p:with-option>
            </px:set-base-uri>
            <!--<px:message>
                <p:with-option name="message"
                    select="concat('upgraded HTML document to the EPUB3 content document ',substring($result-uri,string-length($publication-dir)+1))"
                />
            </px:message>-->
        </p:for-each>
    </p:group>


    <!--=========================================================================-->
    <!-- RESULT FILESETS                                                         -->
    <!--=========================================================================-->
    <p:group name="fileset.docs">
        <p:output port="result"/>
        <p:for-each>
            <p:variable name="result-uri" select="base-uri(/*)"/>
            <px:fileset-create>
                <p:with-option name="base" select="$content-dir"/>
            </px:fileset-create>
            <px:fileset-add-entry media-type="application/xhtml+xml">
                <p:with-option name="href" select="$result-uri"/>
            </px:fileset-add-entry>
        </p:for-each>
        <px:fileset-join/>
    </p:group>


</p:declare-step>
