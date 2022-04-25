<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                px:input-filesets="html"
                px:output-filesets="epub3"
                type="px:html-to-epub3.script" name="main"
                exclude-inline-prefixes="px c">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">HTML to EPUB 3</h1>
        <p px:role="desc">Transforms (X)HTML documents into an EPUB 3 publication.</p>
        <a px:role="homepage" href="http://daisy.github.io/pipeline/Get-Help/User-Guide/Scripts/html-to-epub3/">
            Online documentation
        </a>
    </p:documentation>
    
    <p:input port="metadata" primary="false" sequence="true" px:media-type="application/xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Metadata</h2>
            <p px:role="desc" xml:space="preserve">Metadata to be included in the EPUB.

If specified, the document must be a [`metadata`](http://www.idpf.org/epub/301/spec/epub-publications.html#sec-metadata-elem)
element in the OPF namespace. If not specified, metadata is extracted from the HTML documents.</p>
        </p:documentation>
        <p:empty/>
    </p:input>

    <p:option name="html" required="true" px:type="anyFileURI"
        px:media-type="application/xhtml+xml text/html" px:sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">HTML document(s)</h2>
            <p px:role="desc">List of the HTML documents to convert.</p>
        </p:documentation>
    </p:option>

    <p:option name="output-dir" required="true" px:output="result" px:type="anyDirURI">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">EPUB</h2>
            <p px:role="desc">The resulting EPUB 3 publication.</p>
        </p:documentation>
    </p:option>

    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
        <p:documentation>
            px:normalize-uri
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-add-entries
            px:fileset-load
            px:fileset-join
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/epub-utils/library.xpl">
        <p:documentation>
            px:epub3-store
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/html-utils/library.xpl">
        <p:documentation>
            px:html-load
        </p:documentation>
    </p:import>
    <p:import href="html-to-epub3.convert.xpl">
        <p:documentation>
            px:html-to-epub3
        </p:documentation>
    </p:import>

    <px:normalize-uri name="output-dir-uri">
        <p:with-option name="href" select="concat($output-dir,'/')"/>
    </px:normalize-uri>
    <p:sink/>

    <p:group>
        <p:variable name="output-dir-uri"  select="/c:result/string()">
            <p:pipe step="output-dir-uri" port="normalized"/>
        </p:variable>
        <p:variable name="epub-file-uri"
            select="concat($output-dir-uri,if (ends-with($html,'/')) then 'result' else replace($html,'^.*/([^/]*)\.[^/\.]*$','$1'),'.epub')"/>

        <p:group name="load">
            <p:output port="fileset" primary="true"/>
            <p:output port="in-memory" sequence="true">
                <p:pipe step="html-load" port="result.in-memory"/>
            </p:output>
            <px:fileset-add-entries media-type="application/xhtml+xml">
                <p:input port="source.fileset">
                    <p:inline><d:fileset/></p:inline>
                </p:input>
                <p:with-option name="href" select="tokenize($html,'\s+')"/>
            </px:fileset-add-entries>
            <px:fileset-join/>
            <px:html-load name="html-load"/>
        </p:group>

        <px:html-to-epub3 name="convert">
            <p:input port="input.in-memory">
                <p:pipe step="load" port="in-memory"/>
            </p:input>
            <p:with-option name="output-dir" select="concat($output-dir-uri,'epub/')">
                <p:empty/>
            </p:with-option>
            <p:input port="metadata">
                <p:pipe port="metadata" step="main"/>
            </p:input>
        </px:html-to-epub3>

        <px:epub3-store>
            <p:with-option name="href" select="$epub-file-uri">
                <p:empty/>
            </p:with-option>
            <p:input port="in-memory.in">
                <p:pipe port="in-memory.out" step="convert"/>
            </p:input>
        </px:epub3-store>

    </p:group>

</p:declare-step>
