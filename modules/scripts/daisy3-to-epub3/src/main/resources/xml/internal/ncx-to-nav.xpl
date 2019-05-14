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
    <p:input port="htmls" sequence="true">
        <p:documentation>The EPUB 3 XHTML content documents.</p:documentation>
    </p:input>
    
    <p:option name="result-uri" required="true"/>

    <p:output port="fileset.out" primary="true" sequence="false">
        <p:pipe port="result" step="create-fileset"/>
    </p:output>
    <p:output port="in-memory.out" sequence="false">
        <p:pipe port="result" step="create-nav"/>
    </p:output>


    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">For manipulating
            filesets.</p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
        <p:documentation>
            px:set-base-uri
        </p:documentation>
    </p:import>
    
    <p:group name="id-map">
        <p:output port="result"/>
        <p:xslt name="smil-to-dtbook-id" template-name="create-id-map">
            <p:input port="source">
                <p:pipe port="smils" step="main"/>
                <p:pipe port="dtbooks" step="main"/>
                <p:pipe port="htmls" step="main"/>
            </p:input>
            <p:input port="stylesheet">
                <p:document href="smil-to-html-ids.xsl"/>
            </p:input>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
        </p:xslt>
    </p:group>
    <p:sink/>

    <p:group name="create-nav">
        <p:output port="result"/>
        <p:xslt>
            <p:input port="source">
                <p:pipe port="source" step="main"/>
            </p:input>
            <p:input port="stylesheet">
                <p:document href="ncx-to-nav.xsl"/>
            </p:input>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
        </p:xslt>
        <px:set-base-uri name="ncx-with-smilrefs">
            <p:with-option name="base-uri" select="$result-uri"/>
        </px:set-base-uri>
        <p:xslt>
            <p:input port="source">
                <p:pipe port="result" step="ncx-with-smilrefs"/>
                <p:pipe port="result" step="id-map"/>
            </p:input>
            <p:input port="stylesheet">
                <p:document href="ncx-id-fixer.xsl"/>
            </p:input>
            <p:with-param name="ncx-uri" select="base-uri(/*)">
                <p:pipe port="source" step="main"/>
            </p:with-param>
        </p:xslt>
    </p:group>
    
    <p:group name="create-fileset">
        <p:output port="result"/>
        <px:fileset-create>
            <p:with-option name="base" select="replace($result-uri,'/[^/]*$','/')"/>
        </px:fileset-create>
        <px:fileset-add-entry media-type="application/xhtml+xml">
            <p:with-option name="href" select="$result-uri"/>
        </px:fileset-add-entry>
    </p:group>

</p:declare-step>
