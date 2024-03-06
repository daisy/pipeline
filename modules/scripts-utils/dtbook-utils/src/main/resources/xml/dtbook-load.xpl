<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                type="px:dtbook-load" name="main">

    <p:documentation> Loads the DTBook XML fileset. </p:documentation>

    <p:input port="source.fileset" primary="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The input fileset containing the DTBook files (marked with
            <code>media-type="application/x-dtbook+xml"</code>.</p>
            <p>Will also be used for loading other resources. If files are present in memory, they
            are expected to be <code>c:data</code> documents. Only when files are not present in
            this fileset, it will be attempted to load them from disk.</p>
        </p:documentation>
    </p:input>
    <p:input port="source.in-memory" sequence="true">
        <p:empty/>
    </p:input>

    <p:option name="validation" cx:as="xs:boolean" select="false()">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Whether to perform validation of the input.</p>
        </p:documentation>
    </p:option>

    <p:option name="mathml-version" select="'3.0'" cx:type="3.0|2.0">
        <p:documentation>
            <p>Version of MathML in the DTBook file</p>
        </p:documentation>
    </p:option>
    <p:option name="check-images" select="false()" cx:as="xs:boolean">
        <p:documentation>
            <p>Check to see that referenced images exist on disk</p>
        </p:documentation>
    </p:option>
    <p:option name="nimas" cx:as="xs:boolean" select="false()">
        <p:documentation>
            <p>Validate against NIMAS 1.1</p>
        </p:documentation>
    </p:option>

    <p:output port="result.fileset" primary="true">
        <p:pipe step="fileset" port="result.fileset"/>
    </p:output>
    <p:output port="result.in-memory" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Fileset containing all the DTBook files and any resources they reference (images
            etc.). Only contains resources that actually exist on disk. The DTBooks are loaded into
            memory. The <code>original-href</code> attributes reflects which files are stored on
            disk.</p>
        </p:documentation>
        <p:pipe step="dtbooks" port="result"/>
    </p:output>

    <p:output port="validation-report" sequence="true" px:media-type="application/vnd.pipeline.report+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The validation report</p>
            <p>Can contain as many report documents as there are DTBook files in the input.</p>
            <p>The port is empty if the <code>validation</code> option is set to false or if the
            input contains no invalid DTBook files.</p>
        </p:documentation>
        <p:pipe step="validate" port="report"/>
    </p:output>

    <p:output port="validation-status" px:media-type="application/vnd.pipeline.status+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The <a href="http://daisy.github.io/pipeline/StatusXML">validation status</a>
            document</p>
            <p>'ok' if the input does not contain invalid DTBook files, 'error' otherwise.</p>
        </p:documentation>
        <p:pipe step="validate" port="status"/>
    </p:output>

    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
        <p:documentation>
            px:assert
            px:parse-xml-stylesheet-instructions
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-join
            px:fileset-load
            px:fileset-purge
            px:fileset-filter
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/mediatype-utils/library.xpl">
        <p:documentation>
            px:mediatype-detect
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/css-utils/library.xpl">
        <p:documentation>
            px:css-to-fileset
        </p:documentation>
    </p:import>
    <p:import href="validate-dtbook/validate-dtbook.xpl">
        <p:documentation>
            px:dtbook-validate
        </p:documentation>
    </p:import>

    <!--
        fileset containing the input DTBooks (with normalized base URIs)
    -->
    <px:fileset-load media-types="application/x-dtbook+xml" name="dtbooks">
        <p:input port="in-memory">
            <p:pipe step="main" port="source.in-memory"/>
        </p:input>
    </px:fileset-load>

    <p:for-each>
        <p:xslt>
            <p:input port="stylesheet">
                <p:document href="dtbook-fileset.xsl"/>
            </p:input>
            <p:with-param port="parameters" name="context.fileset" select="/">
                <p:pipe step="main" port="source.fileset"/>
            </p:with-param>
            <p:with-param port="parameters" name="context.in-memory" select="collection()">
                <p:pipe step="main" port="source.in-memory"/>
            </p:with-param>
        </p:xslt>
    </p:for-each>
    <px:fileset-join/>
    <px:mediatype-detect/>
    <p:identity name="resources-mathml"/>
    <p:sink/>
    
    <!-- add any CSS stylesheets from xml-stylesheet instructions  -->
    <p:for-each>
        <p:iteration-source>
            <p:pipe step="dtbooks" port="result"/>
        </p:iteration-source>
        <px:parse-xml-stylesheet-instructions name="parse-pi"/>
        <p:sink/>
        <p:delete match="d:file[not(@media-type='text/css')]">
            <p:input port="source">
                <p:pipe step="parse-pi" port="fileset"/>
            </p:input>
        </p:delete>
    </p:for-each>
    <p:identity name="css-from-pi"/>
    <p:sink/>

    <p:group name="referenced-from-css">
        <p:output port="result"/>
        <px:fileset-join>
            <p:input port="source">
                <p:pipe step="resources-mathml" port="result"/>
                <p:pipe step="css-from-pi" port="result"/>
            </p:input>
        </px:fileset-join>
        <px:css-to-fileset/>
    </p:group>
    <p:sink/>

    <px:fileset-join>
        <p:input port="source">
            <p:pipe step="dtbooks" port="result.fileset"/>
            <p:pipe step="resources-mathml" port="result"/>
            <p:pipe step="css-from-pi" port="result"/>
            <p:pipe step="referenced-from-css" port="result"/>
        </p:input>
    </px:fileset-join>

    <!--
        remove files that are not on disk or in memory, and make @original-href reflect which files are in memory
    -->
    <px:fileset-purge name="fileset">
        <p:input port="source.in-memory">
            <p:pipe step="dtbooks" port="result"/>
        </p:input>
    </px:fileset-purge>

    <p:choose name="validate">
        <p:when test="not($validation)">
            <p:output port="status" primary="true">
                <p:inline>
                    <d:validation-status result="ok"/>
                </p:inline>
            </p:output>
            <p:output port="report" sequence="true">
                <p:empty/>
            </p:output>
            <p:sink/>
        </p:when>
        <p:otherwise px:message="Validating DTBook">
            <p:output port="status" primary="true"/>
            <p:output port="report" sequence="true">
                <p:pipe step="each" port="report"/>
            </p:output>
            <!--
                px:dtbook-validate expects single DTBook as input and no resources
            -->
            <px:fileset-load media-types="application/x-dtbook+xml" name="load">
                <p:input port="in-memory">
                    <p:pipe step="dtbooks" port="result"/>
                </p:input>
            </px:fileset-load>
            <p:for-each name="each">
                <p:output port="report" sequence="true">
                    <p:pipe step="status-and-report" port="report"/>
                </p:output>
                <p:output port="status" primary="true"/>
                <p:identity name="single-dtbook"/>
                <p:sink/>
                <px:fileset-add-entry media-type="application/x-dtbook+xml" name="single-dtbook-fileset">
                    <p:input port="entry">
                        <p:pipe step="single-dtbook" port="result"/>
                    </p:input>
                </px:fileset-add-entry>
                <px:dtbook-validate name="dtbook-validate">
                    <p:input port="source.in-memory">
                        <p:pipe step="single-dtbook-fileset" port="result.in-memory"/>
                    </p:input>
                    <p:with-option name="nimas" select="$nimas"/>
                    <p:with-option name="mathml-version" select="$mathml-version"/>
                    <p:with-option name="check-images" select="$check-images"/>
                </px:dtbook-validate>
                <p:sink/>
                <p:identity>
                    <p:input port="source">
                        <p:pipe step="dtbook-validate" port="validation-status"/>
                    </p:input>
                </p:identity>
                <p:choose name="status-and-report">
                    <p:when test="/d:validation-status[@result='ok']">
                        <p:output port="status" primary="true"/>
                        <p:output port="report" sequence="true">
                            <p:empty/>
                        </p:output>
                        <p:identity/>
                    </p:when>
                    <p:otherwise>
                        <p:output port="status" primary="true"/>
                        <p:output port="report">
                            <p:pipe step="dtbook-validate" port="html-report"/>
                        </p:output>
                        <p:identity/>
                    </p:otherwise>
                </p:choose>
            </p:for-each>
            <p:template>
                <p:input port="template">
                    <p:inline><d:validation-status result="{$status}"/></p:inline>
                </p:input>
                <p:input port="source">
                    <p:empty/>
                </p:input>
                <p:with-param port="parameters" name="status"
                              select="if (collection()//d:validation-status[@result='error']) then 'error' else 'ok'"/>
            </p:template>
        </p:otherwise>
    </p:choose>

</p:declare-step>
