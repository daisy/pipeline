<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal/daisy3-to-epub3"
                type="pxi:ncx-to-nav" name="main">

    <p:input port="source" primary="true" sequence="false">
        <p:documentation>The NCX document.</p:documentation>
    </p:input>
    <p:input port="smils" sequence="true">
        <p:documentation>The DAISY 3 SMIL documents.</p:documentation>
    </p:input>
    <p:input port="dtbooks" sequence="true">
        <p:documentation>The DAISY 3 DTBook documents.</p:documentation>
    </p:input>
    <p:input port="dtbook-html-mapping">
        <p:documentation>d:fileset with mapping from DTBook to XHTML files.</p:documentation>
    </p:input>
    
    <p:option name="result-uri" required="true"/>

    <p:output port="fileset.out" primary="true" sequence="false">
        <p:pipe step="fileset" port="result"/>
    </p:output>
    <p:output port="in-memory.out" sequence="false">
        <p:pipe step="nav" port="result"/>
    </p:output>

    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-create
            px:fileset-add-entry
            px:fileset-compose
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
        <p:documentation>
            px:set-base-uri
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/html-utils/library.xpl">
        <p:documentation>
            px:html-update-links
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/epub-utils/library.xpl">
        <p:documentation>
            px:epub3-nav-from-ncx
        </p:documentation>
    </p:import>
    
    <p:group name="id-map">
        <p:output port="result"/>
        <p:xslt template-name="create-id-map">
            <p:input port="source">
                <p:pipe step="main" port="smils"/>
                <p:pipe step="main" port="dtbooks"/>
            </p:input>
            <p:input port="stylesheet">
                <p:document href="smil-to-dtbook-ids.xsl"/>
            </p:input>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
        </p:xslt>
        <p:identity name="smil-dtbook-mapping"/>
        <p:sink/>
        <!-- add the mapping from dtbook to html -->
        <px:fileset-compose>
            <p:input port="source">
                <p:pipe step="smil-dtbook-mapping" port="result"/>
                <p:pipe step="main" port="dtbook-html-mapping"/>
            </p:input>
        </px:fileset-compose>
        <!-- add the mapping from ncx to nav -->
        <p:insert position="first-child">
            <p:input port="insertion">
                <p:inline><d:file/></p:inline>
            </p:input>
        </p:insert>
        <p:add-attribute match="/*/d:file[1]" attribute-name="href">
            <p:with-option name="attribute-value" select="$result-uri"/>
        </p:add-attribute>
        <p:add-attribute match="/*/d:file[1]" attribute-name="original-href">
            <p:with-option name="attribute-value" select="base-uri(/*)">
                <p:pipe step="main" port="source"/>
            </p:with-option>
        </p:add-attribute>
    </p:group>
    <p:sink/>

    <p:group name="nav">
        <p:output port="result"/>
        <px:epub3-nav-from-ncx>
            <p:input port="source">
                <p:pipe step="main" port="source"/>
            </p:input>
        </px:epub3-nav-from-ncx>
        <px:set-base-uri>
            <p:with-option name="base-uri" select="$result-uri"/>
        </px:set-base-uri>
        <px:html-update-links source-renamed="true">
            <p:input port="mapping">
                <p:pipe step="id-map" port="result"/>
            </p:input>
        </px:html-update-links>
    </p:group>

    <p:group name="fileset">
        <p:output port="result"/>
        <px:fileset-create>
            <p:with-option name="base" select="replace(base-uri(/*),'/[^/]*$','/')"/>
        </px:fileset-create>
        <px:fileset-add-entry media-type="application/xhtml+xml">
            <p:input port="entry">
                <p:pipe step="nav" port="result"/>
            </p:input>
        </px:fileset-add-entry>
    </p:group>

</p:declare-step>
