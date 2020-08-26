<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc-internal"
                type="pxi:epub3-ocf-zip" name="main">

    <p:input port="source"/>
    <p:output port="result">
        <p:pipe step="store" port="result"/>
    </p:output>
    <p:option name="target" required="true"/>

    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-add-entry
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/odf-utils/library.xpl">
        <p:documentation>
            px:odf-store
        </p:documentation>
    </p:import>

    <px:fileset-add-entry media-type="application/epub+zip" href="."/>

    <!-- manifest already created (or not) in epub3-ocf-finalize -->
    <px:odf-store skip-manifest="true" name="store">
        <!-- assume px:fileset-store was previously called -->
        <p:input port="source.in-memory">
            <p:empty/>
        </p:input>
        <p:with-option name="href" select="$target"/>
    </px:odf-store>

</p:declare-step>
