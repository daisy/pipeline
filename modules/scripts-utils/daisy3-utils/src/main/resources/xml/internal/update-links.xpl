<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc-internal"
                exclude-inline-prefixes="#all"
                type="px:daisy3-update-links" name="main">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Update cross-references in OPF, NCX and SMIL documents</p>
    </p:documentation>

    <p:input port="source.fileset" primary="true"/>
    <p:input port="source.in-memory" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>DAISY 3 fileset before the relocation of files.</p>
        </p:documentation>
        <p:empty/>
    </p:input>
    <p:input port="mapping">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>A <code>d:fileset</code> document that defines the relocation of files in the DAISY 3
            fileset.</p>
        </p:documentation>
    </p:input>

    <p:output port="result.fileset" primary="true"/>
    <p:output port="result.in-memory" sequence="true">
        <p:pipe step="updated-links-in-res" port="result.in-memory"/>
    </p:output>

    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-filter
            px:fileset-load
            px:fileset-update
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/smil-utils/library.xpl">
        <p:documentation>
            px:smil-update-links
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/dtbook-utils/library.xpl">
        <p:documentation>
            px:dtbook-update-links
        </p:documentation>
    </p:import>
    <p:import href="ncx/update-links.xpl">
        <p:documentation>
            px:ncx-update-links
        </p:documentation>
    </p:import>
    <p:import href="opf/update-links.xpl">
        <p:documentation>
            pxi:daisy3-opf-update-links
        </p:documentation>
    </p:import>
    <p:import href="resources/update-links.xpl">
        <p:documentation>
            pxi:daisy3-res-update-links
        </p:documentation>
    </p:import>

    <p:documentation>Update cross-references in package document</p:documentation>
    <px:fileset-filter media-types="application/oebps-package+xml" name="opf">
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
    <p:for-each name="updated-links-in-opf.in-memory">
        <p:output port="result"/>
        <pxi:daisy3-opf-update-links>
            <p:input port="mapping">
                <p:pipe step="main" port="mapping"/>
            </p:input>
        </pxi:daisy3-opf-update-links>
    </p:for-each>
    <p:sink/>
    <px:fileset-update name="updated-links-in-opf">
        <p:input port="source.fileset">
            <p:pipe step="main" port="source.fileset"/>
        </p:input>
        <p:input port="source.in-memory">
            <p:pipe step="main" port="source.in-memory"/>
        </p:input>
        <p:input port="update.fileset">
            <p:pipe step="opf" port="result"/>
        </p:input>
        <p:input port="update.in-memory">
            <p:pipe step="updated-links-in-opf.in-memory" port="result"/>
        </p:input>
    </px:fileset-update>
    <p:sink/>

    <p:documentation>Update cross-references in NCX document</p:documentation>
    <px:fileset-filter media-types="application/x-dtbncx+xml" name="ncx">
        <p:input port="source">
            <p:pipe step="opf" port="not-matched"/>
        </p:input>
        <p:input port="source.in-memory">
            <p:pipe step="opf" port="not-matched.in-memory"/>
        </p:input>
    </px:fileset-filter>
    <px:fileset-load>
        <p:input port="in-memory">
            <p:pipe step="opf" port="not-matched.in-memory"/>
        </p:input>
    </px:fileset-load>
    <p:for-each name="updated-links-in-ncx.in-memory">
        <p:output port="result"/>
        <px:ncx-update-links>
            <p:input port="mapping">
                <p:pipe step="main" port="mapping"/>
            </p:input>
        </px:ncx-update-links>
    </p:for-each>
    <p:sink/>
    <px:fileset-update name="updated-links-in-ncx">
        <p:input port="source.fileset">
            <p:pipe step="updated-links-in-opf" port="result.fileset"/>
        </p:input>
        <p:input port="source.in-memory">
            <p:pipe step="updated-links-in-opf" port="result.in-memory"/>
        </p:input>
        <p:input port="update.fileset">
            <p:pipe step="ncx" port="result"/>
        </p:input>
        <p:input port="update.in-memory">
            <p:pipe step="updated-links-in-ncx.in-memory" port="result"/>
        </p:input>
    </px:fileset-update>
    <p:sink/>

    <p:documentation>Update cross-references in SMIL documents</p:documentation>
    <px:fileset-filter media-types="application/smil+xml" name="smil">
        <p:input port="source">
            <p:pipe step="ncx" port="not-matched"/>
        </p:input>
        <p:input port="source.in-memory">
            <p:pipe step="ncx" port="not-matched.in-memory"/>
        </p:input>
    </px:fileset-filter>
    <px:fileset-load>
        <p:input port="in-memory">
            <p:pipe step="ncx" port="not-matched.in-memory"/>
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
            <p:pipe step="updated-links-in-ncx" port="result.fileset"/>
        </p:input>
        <p:input port="source.in-memory">
            <p:pipe step="updated-links-in-ncx" port="result.in-memory"/>
        </p:input>
        <p:input port="update.fileset">
            <p:pipe step="smil" port="result"/>
        </p:input>
        <p:input port="update.in-memory">
            <p:pipe step="updated-links-in-smil.in-memory" port="result"/>
        </p:input>
    </px:fileset-update>

    <p:documentation>Update cross-references in DTBook documents</p:documentation>
    <px:fileset-filter media-types="application/x-dtbook+xml" name="dtbook">
        <p:input port="source">
            <p:pipe step="smil" port="not-matched"/>
        </p:input>
        <p:input port="source.in-memory">
            <p:pipe step="smil" port="not-matched.in-memory"/>
        </p:input>
    </px:fileset-filter>
    <px:fileset-load>
        <p:input port="in-memory">
            <p:pipe step="smil" port="not-matched.in-memory"/>
        </p:input>
    </px:fileset-load>
    <p:for-each name="updated-links-in-dtbook.in-memory">
        <p:output port="result"/>
        <px:dtbook-update-links>
            <p:input port="mapping">
                <p:pipe step="main" port="mapping"/>
            </p:input>
        </px:dtbook-update-links>
    </p:for-each>
    <p:sink/>
    <px:fileset-update name="updated-links-in-dtbook">
        <p:input port="source.fileset">
            <p:pipe step="updated-links-in-smil" port="result.fileset"/>
        </p:input>
        <p:input port="source.in-memory">
            <p:pipe step="updated-links-in-smil" port="result.in-memory"/>
        </p:input>
        <p:input port="update.fileset">
            <p:pipe step="dtbook" port="result"/>
        </p:input>
        <p:input port="update.in-memory">
            <p:pipe step="updated-links-in-dtbook.in-memory" port="result"/>
        </p:input>
    </px:fileset-update>

    <p:documentation>Update cross-references in resources document</p:documentation>
    <px:fileset-filter media-types="application/x-dtbresource+xml" name="res">
        <p:input port="source">
            <p:pipe step="dtbook" port="not-matched"/>
        </p:input>
        <p:input port="source.in-memory">
            <p:pipe step="dtbook" port="not-matched.in-memory"/>
        </p:input>
    </px:fileset-filter>
    <px:fileset-load>
        <p:input port="in-memory">
            <p:pipe step="dtbook" port="not-matched.in-memory"/>
        </p:input>
    </px:fileset-load>
    <p:for-each name="updated-links-in-res.in-memory">
        <p:output port="result"/>
        <pxi:daisy3-res-update-links>
            <p:input port="mapping">
                <p:pipe step="main" port="mapping"/>
            </p:input>
        </pxi:daisy3-res-update-links>
    </p:for-each>
    <p:sink/>
    <px:fileset-update name="updated-links-in-res">
        <p:input port="source.fileset">
            <p:pipe step="updated-links-in-dtbook" port="result.fileset"/>
        </p:input>
        <p:input port="source.in-memory">
            <p:pipe step="updated-links-in-dtbook" port="result.in-memory"/>
        </p:input>
        <p:input port="update.fileset">
            <p:pipe step="res" port="result"/>
        </p:input>
        <p:input port="update.in-memory">
            <p:pipe step="updated-links-in-res.in-memory" port="result"/>
        </p:input>
    </px:fileset-update>

</p:declare-step>
