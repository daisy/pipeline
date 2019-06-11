<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:docx2hub="http://transpect.io/docx2hub"
                type="px:docx-to-html.script" name="main"
                px:input-filesets="docx"
                px:output-filesets="html">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">Office OpenXML to HTML</h1>
        <p px:role="desc">Transforms a Microsoft Office OpenXML document into HTML using thedocx2hub module of the transpect pipeline.</p>
        <a px:role="homepage" href="http://daisy.github.io/pipeline/modules/docx-to-html">
            Online documentation
        </a>
    </p:documentation>
    <!--   
    <p:input port="source" primary="true" sequence="true" px:media-type="application/vnd.openxmlformats-officedocument.wordprocessingml.document">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Docx OpenXML file</h2>
        </p:documentation>
    </p:input>-->

    <p:option name="docx" required="true" px:type="anyFileURI" px:media-type="application/vnd.openxmlformats-officedocument.wordprocessingml.document">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">OpenXML docx file</h2>
            <p px:role="desc">a *.docx file</p>
        </p:documentation>
    </p:option>

    <p:option name="language" required="false" px:type="string" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Language code</h2>
            <p px:role="desc">Language code of the input document.</p>
        </p:documentation>
    </p:option>

    <p:option name="output-dir" required="true" px:output="result" px:type="anyDirURI">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">HTML</h2>
            <p px:role="desc">The resulting HTML document.</p>
        </p:documentation>
    </p:option>

    <p:option name="chunk-size" required="false" px:type="integer" select="'-1'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Chunk size</h2>
            <p px:role="desc" xml:space="preserve">The maximum size of HTML files in kB. Specify "-1" for no maximum.

Top-level sections in the DTBook become separate HTML files, and are further split up if they exceed
the given maximum size.</p>
        </p:documentation>
    </p:option>


    <!-- DEPENDENCIES -->
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl"/>
    <p:import href="http://transpect.io/docx2hub/xpl/docx2html.xpl"/>


    <!-- Starting script -->
    <px:normalize-uri name="output-dir-uri">
        <p:with-option name="href" select="concat($output-dir,'/')"/>
    </px:normalize-uri>
    
    <p:sink/>
    
    <p:group>
        <p:variable name="encoded-title"
            select="replace(replace(base-uri(/),'^.*/([^/]+)$','$1'),'\.[^\.]*$','')">
            <p:pipe port="matched" step="first-docx"/>
        </p:variable>
        <p:variable name="output-dir-uri" select="/c:result/string()">
            <p:pipe step="output-dir-uri" port="normalized"/>
        </p:variable>
        <p:variable name="html-file-uri" select="concat($output-dir,$encoded-title,'.html')"/>

        <docx2hub:docx2html>
            <p:with-option name="docx" select="$docx" />
        </docx2hub:docx2html>

    </p:group>

</p:declare-step>
