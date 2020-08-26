<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                exclude-inline-prefixes="#all"
                type="px:epub-rename-files" name="main">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Rename files in an EPUB fileset</p>
    </p:documentation>

    <p:input port="source.fileset" primary="true"/>
    <p:input port="source.in-memory" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The input fileset</p>
        </p:documentation>
        <p:empty/>
    </p:input>
    <p:input port="mapping">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>A <code>d:fileset</code> document that maps files in the source fileset
            (<code>@original-href</code>) to files in the result fileset (<code>@href</code>).</p>
        </p:documentation>
    </p:input>

    <p:output port="result.fileset" primary="true"/>
    <p:output port="result.in-memory" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The output fileset</p>
            <p>The files are renamed in the fileset manifest and the base URIs of the in-memory
            documents are updated accordingly. Cross-references in OPF, HTML and SMIL documents are
            updated too.</p>
        </p:documentation>
        <p:pipe step="rename" port="result.in-memory"/>
    </p:output>

    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-apply
        </p:documentation>
    </p:import>
    <p:import href="epub-update-links.xpl">
        <p:documentation>
            px:epub-update-links
        </p:documentation>
    </p:import>

    <p:documentation>Update cross-references</p:documentation>
    <px:epub-update-links name="update-links">
        <p:input port="source.in-memory">
            <p:pipe step="main" port="source.in-memory"/>
        </p:input>
        <p:input port="mapping">
            <p:pipe step="main" port="mapping"/>
        </p:input>
    </px:epub-update-links>

    <p:documentation>Perform the renaming</p:documentation>
    <px:fileset-apply name="rename">
        <p:input port="source.in-memory">
            <p:pipe step="update-links" port="result.in-memory"/>
        </p:input>
        <p:input port="mapping">
            <p:pipe step="main" port="mapping"/>
        </p:input>
    </px:fileset-apply>

</p:declare-step>
