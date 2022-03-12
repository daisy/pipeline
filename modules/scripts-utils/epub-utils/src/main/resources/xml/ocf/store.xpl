<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc-internal"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                type="px:epub3-store" name="main">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Store a EPUB 3 fileset in a ZIP container</p>
    </p:documentation>

    <p:input port="fileset.in" primary="true"/>
    <p:input port="in-memory.in" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The EPUB 3 fileset</p>
        </p:documentation>
    </p:input>

    <p:output port="result" primary="false">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>A <code>c:result</code> document containing the URI of the ZIP file.</p>
        </p:documentation>
        <p:pipe port="result" step="zip"/>
    </p:output>

    <p:option name="href" required="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The URI of the ZIP file</p>
        </p:documentation>
    </p:option>

    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-store
        </p:documentation>
    </p:import>
    <p:import href="ocf-zip.xpl">
        <p:documentation>
            pxi:epub3-ocf-zip
        </p:documentation>
    </p:import>

    <px:fileset-store name="fileset-store">
        <p:input port="fileset.in">
            <p:pipe port="fileset.in" step="main"/>
        </p:input>
        <p:input port="in-memory.in">
            <p:pipe port="in-memory.in" step="main"/>
        </p:input>
    </px:fileset-store>

    <pxi:epub3-ocf-zip name="zip" cx:depends-on="fileset-store">
        <p:with-option name="target" select="$href"/>
        <p:input port="source">
            <p:pipe port="fileset.out" step="fileset-store"/>
        </p:input>
    </pxi:epub3-ocf-zip>

</p:declare-step>
