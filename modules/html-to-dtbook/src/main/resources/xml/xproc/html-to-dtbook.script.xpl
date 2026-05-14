<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                type="px:html-to-dtbook.script">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">HTML to DTBook</h1>
        <p px:role="desc">Transforms a (X)HTML document into a DTBook document.</p>
        <a px:role="homepage" href="http://daisy.github.io/pipeline/Get-Help/User-Guide/Scripts/html-to-dtbook/">
            Online documentation
        </a>
    </p:documentation>

    <p:option name="source" required="true" px:type="anyFileURI" px:media-type="application/xhtml+xml text/html">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">HTML document</h2>
            <p px:role="desc">The (X)HTML document to convert</p>
        </p:documentation>
    </p:option>

    <p:option name="result" required="true" px:output="result" px:type="anyDirURI">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">DTBook</h2>
            <p px:role="desc">The directory that will contain the resulting DTBook. The name of the
            DTBook is the name of the HTML file with file extension ".xml".</p>
        </p:documentation>
    </p:option>

    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-create
            px:fileset-add-entry
            px:fileset-copy
            px:fileset-store
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/html-utils/library.xpl">
        <p:documentation>
            px:html-load
        </p:documentation>
    </p:import>
    <p:import href="html-to-dtbook.xpl">
        <p:documentation>
            px:html-to-dtbook
        </p:documentation>
    </p:import>

    <p:documentation>
        Create fileset of HTML and referenced resources. The fileset base is the parent directory of
        the HTML file.
    </p:documentation>
    <p:group name="html-and-resources" px:progress="1/10" px:message="Loading HTML">
        <p:output port="fileset" primary="true"/>
        <p:output port="in-memory" sequence="true">
            <p:pipe step="html" port="result.in-memory"/>
        </p:output>
        <px:fileset-create>
            <p:with-option name="base" select="resolve-uri('./',$source)"/>
        </px:fileset-create>
        <px:fileset-add-entry media-type="application/xhtml+xml">
            <p:with-option name="href" select="$source"/>
        </px:fileset-add-entry>
        <px:html-load name="html"/>
    </p:group>

    <p:documentation>
        Copy to new location. This will fail if there are resources outside the parent directory of
        the HTML file.
    </p:documentation>
    <px:fileset-copy name="copy">
        <p:with-option name="target" select="$result"/>
        <p:input port="source.in-memory">
            <p:pipe step="html-and-resources" port="in-memory"/>
        </p:input>
    </px:fileset-copy>

    <p:documentation>
        Convert HTML to DTBook. This will fail if there are multiple HTML files in the fileset.
    </p:documentation>
    <px:html-to-dtbook name="dtbook-and-resources" px:message="Converting HTML to DTBook" px:progress="8/10"
                       imply-headings="true">
        <p:input port="source.in-memory">
            <p:pipe step="copy" port="result.in-memory"/>
        </p:input>
    </px:html-to-dtbook>

    <p:documentation>
        Store DTBook
    </p:documentation>
    <px:fileset-store px:message="Storing DTBook" px:progress="1/10">
        <p:input port="in-memory.in">
            <p:pipe step="dtbook-and-resources" port="result.in-memory"/>
        </p:input>
    </px:fileset-store>

</p:declare-step>
