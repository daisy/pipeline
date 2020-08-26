<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                exclude-inline-prefixes="#all"
                type="px:epub3-safe-uris" name="main">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Change all URIs in a fileset to EPUB-safe URIs.</p>
        <p>See <a href="http://idpf.org/epub/30/spec/epub30-ocf.html#sec-container-filenames">Open
        Container Format 3.0 specification</a>.</p>
    </p:documentation>

    <p:input port="source.fileset" primary="true"/>
    <p:input port="source.in-memory" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The input fileset</p>
        </p:documentation>
        <p:empty/>
    </p:input>

    <p:output port="result.fileset" primary="true"/>
    <p:output port="result.in-memory" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The output fileset</p>
            <p>The xml:base, href and original-href attributes in the fileset manifest or changed to
            EPUB-safe URIs. The base URIs of the in-memory documents are updated
            accordingly. Cross-references in HTML and SMIL documents are updated too.</p>
        </p:documentation>
        <p:pipe step="rename" port="result.in-memory"/>
    </p:output>
    <p:output port="mapping">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>A <code>d:fileset</code> document that contains the mapping from the source files
            (<code>@original-href</code>) to the copied files (<code>@href</code>).</p>
        </p:documentation>
        <p:pipe step="mapping" port="result"/>
    </p:output>

    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-apply
        </p:documentation>
    </p:import>
    <p:import href="../epub-rename-files.xpl">
        <p:documentation>
            px:epub-rename-files
        </p:documentation>
    </p:import>

    <p:add-xml-base/>
    <p:xslt>
        <p:input port="stylesheet">
            <p:document href="epub3-safe-uris.xsl"/>
        </p:input>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
    </p:xslt>
    <p:label-elements match="d:file[@href=preceding-sibling::d:file/@href]" attribute="href" replace="true"
                      label="for $href in @href
                             return replace($href,
                                            '^(.+?)(\.[^\.]+)?$',
                                            concat('$1_',1+count(preceding-sibling::d:file[@href=$href]),'$2'))">
        <p:documentation>Because the renaming may have resulted in duplicate file names</p:documentation>
    </p:label-elements>
    <p:label-elements match="d:file" attribute="original-href" label="@href-before-move" replace="true"/>
    <p:delete match="/*/*[not(self::d:file)]|
                     d:file/@*[not(name()=('href','original-href'))]"
              name="mapping"/>
    <p:sink/>

    <px:epub-rename-files name="rename">
        <p:input port="source.fileset">
            <p:pipe step="main" port="source.fileset"/>
        </p:input>
        <p:input port="source.in-memory">
            <p:pipe step="main" port="source.in-memory"/>
        </p:input>
        <p:input port="mapping">
            <p:pipe step="mapping" port="result"/>
        </p:input>
    </px:epub-rename-files>

</p:declare-step>
