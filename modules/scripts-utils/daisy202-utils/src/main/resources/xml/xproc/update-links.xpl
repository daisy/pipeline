<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                exclude-inline-prefixes="#all"
                type="px:daisy202-update-links" name="main">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Update cross-references in HTML and SMIL documents</p>
    </p:documentation>

    <p:input port="source.fileset" primary="true"/>
    <p:input port="source.in-memory" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>DAISY 2.02 fileset before the relocation of files.</p>
        </p:documentation>
        <p:empty/>
    </p:input>
    <p:input port="mapping">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>A <code>d:fileset</code> document that defines the relocation of files in the DAISY
            2.02 fileset.</p>
        </p:documentation>
    </p:input>

    <p:output port="result.fileset" primary="true"/>
    <p:output port="result.in-memory" sequence="true">
        <p:pipe step="updated-links-in-smil" port="result.in-memory"/>
    </p:output>

    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-filter
            px:fileset-load
            px:fileset-update
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/html-utils/library.xpl">
        <p:documentation>
            px:html-update-links
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/smil-utils/library.xpl">
        <p:documentation>
            px:smil-update-links
        </p:documentation>
    </p:import>
    
    <p:documentation>Update cross-references in HTML documents</p:documentation>
    <px:fileset-filter media-types="application/xhtml+xml" name="html">
        <p:input port="source">
            <p:pipe step="main" port="source.fileset"/>
        </p:input>
        <p:input port="source.in-memory">
            <p:pipe step="main" port="source.in-memory"/>
        </p:input>
    </px:fileset-filter>
    <px:fileset-load>
        <p:input port="in-memory">
            <p:pipe step="main" port="source.in-memory"/>
        </p:input>
    </px:fileset-load>
    <p:for-each name="updated-links-in-html.in-memory">
        <p:output port="result"/>
        <px:html-update-links>
            <p:input port="mapping">
                <p:pipe step="main" port="mapping"/>
            </p:input>
        </px:html-update-links>
    </p:for-each>
    <p:sink/>
    <px:fileset-update name="updated-links-in-html">
        <p:input port="source.fileset">
            <p:pipe step="main" port="source.fileset"/>
        </p:input>
        <p:input port="source.in-memory">
            <p:pipe step="main" port="source.in-memory"/>
        </p:input>
        <p:input port="update.fileset">
            <p:pipe step="html" port="result"/>
        </p:input>
        <p:input port="update.in-memory">
            <p:pipe step="updated-links-in-html.in-memory" port="result"/>
        </p:input>
    </px:fileset-update>
    <p:sink/>

    <p:documentation>Update cross-references in SMIL documents</p:documentation>
    <px:fileset-filter media-types="application/smil+xml" name="smil">
        <p:input port="source">
            <p:pipe step="html" port="not-matched"/>
        </p:input>
        <p:input port="source.in-memory">
            <p:pipe step="html" port="not-matched.in-memory"/>
        </p:input>
    </px:fileset-filter>
    <px:fileset-load>
        <p:input port="in-memory">
            <p:pipe step="html" port="not-matched.in-memory"/>
        </p:input>
    </px:fileset-load>
    <p:for-each name="updated-links-in-smil.in-memory">
        <p:output port="result"/>
        <px:smil-update-links>
            <p:input port="mapping">
                <p:pipe step="main" port="mapping"/>
            </p:input>
        </px:smil-update-links>
    </p:for-each>
    <p:sink/>
    <px:fileset-update name="updated-links-in-smil">
        <p:input port="source.fileset">
            <p:pipe step="updated-links-in-html" port="result.fileset"/>
        </p:input>
        <p:input port="source.in-memory">
            <p:pipe step="updated-links-in-html" port="result.in-memory"/>
        </p:input>
        <p:input port="update.fileset">
            <p:pipe step="smil" port="result"/>
        </p:input>
        <p:input port="update.in-memory">
            <p:pipe step="updated-links-in-smil.in-memory" port="result"/>
        </p:input>
    </px:fileset-update>

</p:declare-step>
