<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                type="px:diagram-to-html" name="main">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Converts any DIAGRAM descriptions in the input fileset into HTML.</p>
    </p:documentation>

    <p:input port="source.fileset" primary="true"/>
    <p:input port="source.in-memory" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Input fileset</p>
        </p:documentation>
        <p:empty/>
    </p:input>

    <p:output port="result.fileset" primary="true"/>
    <p:output port="result.in-memory" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>A fileset where old DIAGRAM entries have been replaced by entries representing the
            newly produced HTML documents.</p>
            <p>Note that the order between DIAGRAM and other entries is not preserved.</p>
            <p>The HTML documents have the same location of the DIAGRAM files and have the file
            extension ".xhtml".</p>
            <p>Cross-references in HTML are updated.</p>
        </p:documentation>
        <p:pipe step="update-links-in-html" port="result.in-memory"/>
    </p:output>
    <p:output port="mapping">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Mapping document that expresses relation between DIAGRAM input and HTML output files.</p>
        </p:documentation>
        <p:pipe step="diagram-to-html" port="mapping"/>
    </p:output>

    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-filter
            px:fileset-diff
            px:fileset-load
            px:fileset-join
            px:fileset-update
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/html-utils/library.xpl">
        <p:documentation>
            px:html-update-links
        </p:documentation>
    </p:import>

    <p:group name="diagram">
        <p:output port="result.fileset" primary="true">
            <p:pipe step="load" port="result.fileset"/>
        </p:output>
        <p:output port="result.in-memory" sequence="true">
            <p:pipe step="load" port="result"/>
        </p:output>
        <p:output port="not-matched.fileset">
            <p:pipe step="not-matched.fileset" port="result"/>
        </p:output>
        <p:delete match="d:file[not(tokenize(@kind,'\s+')='description')]"/>
        <px:fileset-load media-types="application/xml application/z3998-auth-diagram+xml" name="load">
            <p:input port="in-memory">
                <p:pipe step="main" port="source.in-memory"/>
            </p:input>
        </px:fileset-load>
        <p:sink/>
        <px:fileset-diff name="not-matched.fileset">
            <p:input port="source">
                <p:pipe step="main" port="source.fileset"/>
            </p:input>
            <p:input port="secondary">
                <p:pipe step="load" port="result.fileset"/>
            </p:input>
        </px:fileset-diff>
        <p:sink/>
    </p:group>

    <p:documentation>Convert DIAGRAM to HTML</p:documentation>
    <p:group name="diagram-to-html">
        <p:output port="fileset" primary="true"/>
        <p:output port="in-memory" sequence="true">
            <p:pipe step="convert" port="secondary"/>
        </p:output>
        <p:output port="mapping">
            <p:pipe step="mapping" port="result"/>
        </p:output>
        <p:sink/>
        <p:xslt name="convert" initial-mode="fileset">
            <p:input port="source">
                <p:pipe step="diagram" port="result.fileset"/>
                <p:pipe step="diagram" port="result.in-memory"/>
            </p:input>
            <p:input port="stylesheet">
                <p:document href="../xslt/fileset-convert-diagram.xsl"/>
            </p:input>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
        </p:xslt>
        <p:label-elements match="d:file" attribute="original-href" label="@href-before-move" replace="true"/>
        <p:delete match="/*/*[not(self::d:file)]|
                         d:file/@*[not(name()=('href','original-href'))]"
                  name="mapping"/>
        <p:sink/>
        <p:delete match="@href-before-move">
            <p:input port="source">
                <p:pipe step="convert" port="result"/>
            </p:input>
        </p:delete>
    </p:group>

    <p:documentation>Add HTML to fileset</p:documentation>
    <px:fileset-join name="replace-diagram-with-html.fileset">
        <p:input port="source">
            <p:pipe step="diagram" port="not-matched.fileset"/>
            <p:pipe step="diagram-to-html" port="fileset"/>
        </p:input>
    </px:fileset-join>
    <p:sink/>

    <p:documentation>Update links from other HTML</p:documentation>
    <px:fileset-filter media-types="application/xhtml+xml" name="html">
        <p:input port="source">
            <p:pipe step="diagram" port="not-matched.fileset"/>
        </p:input>
    </px:fileset-filter>
    <px:fileset-load>
        <p:input port="in-memory">
            <p:pipe step="main" port="source.in-memory"/>
        </p:input>
    </px:fileset-load>
    <p:for-each name="update-links-in-html.in-memory">
        <p:output port="result"/>
        <px:html-update-links>
            <p:input port="mapping">
                <p:pipe step="diagram-to-html" port="mapping"/>
            </p:input>
        </px:html-update-links>
    </p:for-each>
    <p:sink/>
    <px:fileset-update name="update-links-in-html">
        <p:input port="source.fileset">
            <p:pipe step="replace-diagram-with-html.fileset" port="result"/>
        </p:input>
        <p:input port="source.in-memory">
            <p:pipe step="main" port="source.in-memory"/>
            <p:pipe step="diagram-to-html" port="in-memory"/>
        </p:input>
        <p:input port="update.fileset">
            <p:pipe step="html" port="result"/>
        </p:input>
        <p:input port="update.in-memory">
            <p:pipe step="update-links-in-html.in-memory" port="result"/>
        </p:input>
    </px:fileset-update>

</p:declare-step>
