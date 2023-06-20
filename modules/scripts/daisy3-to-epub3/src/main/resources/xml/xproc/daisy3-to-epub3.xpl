<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                px:input-filesets="daisy3"
                px:output-filesets="epub3"
                type="px:daisy3-to-epub3.script" version="1.0" name="main">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">DAISY 3 to EPUB 3</h1>
        <p px:role="desc">Transforms a DAISY 3 publication into an EPUB 3 publication.</p>
        <a px:role="homepage" href="http://daisy.github.io/pipeline/Get-Help/User-Guide/Scripts/daisy3-to-epub3/">
            Online documentation
        </a>
        <address>
            Authors:
            <dl px:role="author">
                <dt>Name:</dt>
                <dd px:role="name">Romain Deltour</dd>
                <dt>E-mail:</dt>
                <dd><a px:role="contact" href="mailto:rdeltour@gmail.com">rdeltour@gmail.com</a></dd>
                <dt>Organization:</dt>
                <dd px:role="organization">DAISY Consortium</dd>
            </dl>
        </address>
    </p:documentation>


    <!--=========================================================================-->
    <!-- STEP SIGNATURE                                                          -->
    <!--=========================================================================-->

    <p:input port="source" primary="true" sequence="false" px:media-type="application/oebps-package+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">OPF</h2>
            <p px:role="desc">The package file of the input DTB.</p>
        </p:documentation>
    </p:input>

    <p:option name="result" required="true" px:output="result" px:type="anyDirURI">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">EPUB</h2>
            <p px:role="desc">The produced EPUB.</p>
        </p:documentation>
    </p:option>
    <p:option name="temp-dir" required="true" px:output="temp" px:type="anyDirURI">
        <!-- directory used for temporary files -->
    </p:option>
    <p:option name="mediaoverlays" required="false" select="'true'" px:type="boolean">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Include Media Overlays</h2>
            <p px:role="desc">Whether or not to include media overlays and associated audio files
                (true or false).</p>
        </p:documentation>
    </p:option>
    <p:option name="validation" select="'off'">
        <!-- defined in ../../../../../../common-options.xpl -->
    </p:option>
    <p:option xmlns:_="dtbook" name="_:chunk-size" select="'-1'">
        <!-- defined in ../../../../../../common-options.xpl -->
    </p:option>
    
    <!--<p:option name="compatibility-mode" required="false" select="'true'" px:type="boolean">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Compatibility Mode</h2>
            <p px:role="desc">Whether or not to include NCX-file, OPF guide element and ASCII
                filenames (true or false).</p>
        </p:documentation>
    </p:option>-->

    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-create
            px:fileset-add-entry
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/daisy3-utils/library.xpl">
        <p:documentation>
            px:daisy3-load
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/epub-utils/library.xpl">
        <p:documentation>
            px:epub3-store
        </p:documentation>
    </p:import>
    <p:import href="convert.xpl"/>

    <p:variable name="output-dir-checked" select="resolve-uri(replace($result,'(.+?)/?$','$1/'))"/>
    <p:variable name="epub-file" select="concat($output-dir-checked,'result.epub')"/>

    <px:fileset-create>
        <p:with-option name="base" select="resolve-uri('./',base-uri(/*))"/>
    </px:fileset-create>
    <px:fileset-add-entry media-type="application/oebps-package+xml">
        <p:input port="entry">
            <p:pipe step="main" port="source"/>
        </p:input>
    </px:fileset-add-entry>
    <px:daisy3-load name="load" px:progress="1/10" px:message="Loading DAISY 3"/>

    <px:daisy3-to-epub3 name="convert" px:progress="8/10">
        <p:input port="source.in-memory">
            <p:pipe step="load" port="result.in-memory"/>
        </p:input>
        <p:with-option name="mediaoverlays" select="$mediaoverlays"/>
        <p:with-option name="validation" select="$validation"/>
        <p:with-option name="chunk-size" xmlns:_="dtbook" select="$_:chunk-size"/>
        <p:with-option name="temp-dir" select="$temp-dir"/>
    </px:daisy3-to-epub3>

    <px:epub3-store px:progress="1/10" px:message="Storing EPUB 3">
        <p:with-option name="href" select="$epub-file"/>
        <p:input port="in-memory.in">
            <p:pipe step="convert" port="result.in-memory"/>
        </p:input>
    </px:epub3-store>

</p:declare-step>
