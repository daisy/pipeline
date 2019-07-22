<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:docx2hub="http://transpect.io/docx2hub"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                name="main"
                type="px:docx-to-html.script"
                px:input-filesets="docx"
                px:output-filesets="xhtml"
                exclude-inline-prefixes="#all">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">Docx to HTML</h1>
        <p px:role="desc">Transforms a Microsoft Office OpenXML document into HTML using thedocx2hub module of the transpect pipeline.</p>
        <a px:role="homepage" href="http://daisy.github.io/pipeline/modules/docx-to-html">
            Online documentation
        </a>
    </p:documentation>
    <!--
    <p:input port="source" primary="true" sequence="true" px:media-type="application/vnd.openxmlformats-officedocument.wordprocessingml.document">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Docx OpenXML file</h2>
        </p:documentation>
    </p:input>-->
    
    <p:option name="docx" required="true" px:type="anyFileURI" px:media-type="application/vnd.openxmlformats-officedocument.wordprocessingml.document" px:sequence="false">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">OpenXML docx file</h2>
            <p px:role="desc">a *.docx file</p>
        </p:documentation>
    </p:option>

    


    <p:option name="language" required="false" px:type="string" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Language code</h2>
            <p px:role="desc">Language code of the input document.</p>
        </p:documentation>
    </p:option>

    <p:option name="output-dir" required="true" px:output="result" px:type="anyDirURI">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">HTML</h2>
            <p px:role="desc">The resulting HTML document.</p>
        </p:documentation>
    </p:option>

    <p:option name="temp-dir" required="true" px:type="anyFileURI">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Transpect temp dir</h2>
            <p px:role="desc">Transpect temporary directory URI (for status and debug)</p>
        </p:documentation>
    </p:option>


    <!-- DEPENDENCIES -->
    <p:import href="http://transpect.io/docx2hub/xpl/docx2html.xpl">
        <p:documentation>
            docx2hub:docx2html
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
        <p:documentation>
            px:normalize-uri
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-store
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/html-utils/library.xpl">
        <p:documentation>
            px:html-to-fileset
        </p:documentation>
    </p:import>

    <px:normalize-uri name="output-dir-uri">
        <p:with-option name="href" select="concat($output-dir,'/')"/>
    </px:normalize-uri>
    <px:normalize-uri name="temp-dir-uri">
        <p:with-option name="href" select="concat($temp-dir,'/')"/>
    </px:normalize-uri>
    <p:sink/>
    
    <p:group>
        <p:variable name="output-dir-uri" select="/c:result/string()">
            <p:pipe port="normalized" step="output-dir-uri"/>
        </p:variable>
        <p:variable name="temp-dir-uri" select="/c:result/string()">
            <p:pipe port="normalized" step="temp-dir-uri"/>
        </p:variable>
        <p:variable name="html-file-uri"
                    select="concat($output-dir-uri, replace($docx,'^.*/([^/]*)\.[^/\.]*$','$1.xhtml'))"/>
        
        <p:group name="html">
            <p:output port="result">
                <p:pipe port="result" step="rebase"/>
            </p:output>

            <docx2hub:docx2html name="convert">
                <!-- <p:input port="source"><p:empty/></p:input> -->
                <p:with-option name="docx" select="$docx" />
                <p:with-option name="extract-dir" select="$temp-dir-uri" />
                <p:with-option name="debug-dir-uri" select="$temp-dir-uri"/>
                <p:with-option name="status-dir-uri" select="$temp-dir-uri"/>
            </docx2hub:docx2html>
            <p:xslt>
                <p:documentation>
                    Clean the html : 
                    - Replace images absolute src URIs by relative ones
                </p:documentation>
                <p:input port="stylesheet">
                    <p:inline>
                      <xsl:stylesheet version="2.0">
                        <xsl:template match="@*|node()">
                            <xsl:copy>
                                <xsl:apply-templates select="@*|node()"/>
                            </xsl:copy>
                        </xsl:template>
                        <xsl:template match="@src">
                            <xsl:attribute name="src">
                                <xsl:value-of select="replace(.,base-uri(/*),'')"/>
                            </xsl:attribute>
                        </xsl:template>
                      </xsl:stylesheet>
                    </p:inline>
                </p:input>
                <p:input port="parameters">
                    <p:empty/>
                </p:input>
            </p:xslt>
            <px:set-base-uri name="rebase">
                <p:documentation>
                    docx2html does not set a file at the end of the base-uri, which is required for the html to fileset step.
                </p:documentation>
                <p:with-option name="base-uri" select="concat(base-uri(/*),'document.xhtml')"/>
            </px:set-base-uri>
            <p:store>
                <p:with-option name="href" select="$html-file-uri"/>
            </p:store>
        </p:group>
        <px:html-to-fileset name="html-fileset"/>
        
        <px:set-base-uri name="rebase-fileset">
            <p:documentation>
                fileset-store use the base uri as target directory
            </p:documentation>
            <p:with-option name="base-uri" select="$output-dir-uri"/>
        </px:set-base-uri>
        <px:fileset-store name="test">
            <p:input port="fileset.in">
                <p:pipe port="result" step="rebase-fileset" />
            </p:input>
            <p:input port="in-memory.in">
                <p:pipe step="html" port="result"/>
            </p:input>
        </px:fileset-store>
        
    </p:group>
    
</p:declare-step>
