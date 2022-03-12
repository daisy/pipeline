<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                type="px:dtbook-load" name="main">

    <p:documentation> Loads the DTBook XML fileset. </p:documentation>

    <p:input port="source" primary="true" sequence="true" px:media-type="application/x-dtbook+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>One or more DTBook files to be loaded. Any auxilliary resources referenced from the
            DTBook documents will be resolved based on these files.</p>
        </p:documentation>
    </p:input>

    <p:output port="fileset.out" primary="true"/>
    <p:output port="in-memory.out" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Fileset containing all the DTBook files and any resources they reference (images
            etc.). Only contains resources that actually exist on disk. The DTBooks are loaded into
            memory. The <code>original-href</code> attributes reflects which files are stored on
            disk.</p>
        </p:documentation>
        <p:pipe step="dtbooks" port="result"/>
    </p:output>

    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
        <p:documentation>
            px:message
            px:parse-xml-stylesheet-instructions
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-add-entry
            px:fileset-join
            px:fileset-load
            px:fileset-purge
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

    <!--
        fileset containing the input DTBooks (with normalized base URIs)
    -->
    <p:for-each>
        <p:identity name="dtbook"/>
        <p:sink/>
        <px:fileset-add-entry media-type="application/x-dtbook+xml">
            <p:input port="entry">
                <p:pipe step="dtbook" port="result"/>
            </p:input>
        </px:fileset-add-entry>
    </p:for-each>
    <px:fileset-join/>
    <px:fileset-load name="dtbooks">
        <p:input port="in-memory">
            <p:pipe step="main" port="source"/>
        </p:input>
    </px:fileset-load>

    <p:for-each>
        <p:xslt>
            <p:input port="stylesheet">
                <p:document href="dtbook-fileset.xsl"/>
            </p:input>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
        </p:xslt>
    </p:for-each>
    <px:fileset-join/>
    <px:mediatype-detect/>
    <p:identity name="resources-mathml"/>
    <p:sink/>
    
    <!-- add any CSS stylesheets from xml-stylesheet instructions  -->
    <p:for-each>
        <p:iteration-source>
            <p:pipe step="main" port="source"/>
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
    <px:fileset-purge>
        <p:input port="source.in-memory">
            <p:pipe step="dtbooks" port="result"/>
        </p:input>
    </px:fileset-purge>

</p:declare-step>
