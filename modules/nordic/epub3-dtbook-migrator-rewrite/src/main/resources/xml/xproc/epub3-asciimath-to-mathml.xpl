<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:px="http://www.daisy.org/ns/pipeline/xproc" xmlns:d="http://www.daisy.org/ns/pipeline/data"
    type="px:nordic-epub3-asciimath-to-mathml" name="main" version="1.0" xmlns:epub="http://www.idpf.org/2007/ops" xmlns:pxp="http://exproc.org/proposed/steps" xpath-version="2.0"
    xmlns:cx="http://xmlcalabash.com/ns/extensions" xmlns:html="http://www.w3.org/1999/xhtml">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">Nordic EPUB3 ASCIIMath to MathML</h1>
        <p px:role="desc">Creates MathML for all HTML elements with the "asciimath" class. (experimental)</p>
    </p:documentation>

    <p:option name="epub" required="true" px:type="anyFileURI" px:media-type="application/epub+zip">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">EPUB3 Publication</h2>
        </p:documentation>
    </p:option>

    <p:option name="temp-dir" required="true" px:output="temp" px:type="anyDirURI">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Temporary directory</h2>
            <p px:role="desc">Temporary directory for use by the script.</p>
        </p:documentation>
    </p:option>

    <p:option name="output-dir" required="true" px:output="result" px:type="anyDirURI">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Output directory</h2>
            <p px:role="desc">Output directory for the EPUB publication.</p>
        </p:documentation>
    </p:option>

    <p:import href="step/epub3-asciimath-to-mathml.step.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/epub3-ocf-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/mediatype-utils/library.xpl"/>

    <px:message message="$1">
        <p:with-option name="param1" select="/*">
            <p:document href="../version-description.xml"/>
        </p:with-option>
    </px:message>
    
    <px:normalize-uri name="epub">
        <p:with-option name="href" select="resolve-uri($epub,static-base-uri())"/>
    </px:normalize-uri>
    <px:normalize-uri name="temp-dir">
        <p:with-option name="href" select="resolve-uri($temp-dir,static-base-uri())"/>
    </px:normalize-uri>
    <px:normalize-uri name="output-dir">
        <p:with-option name="href" select="resolve-uri($output-dir,static-base-uri())"/>
    </px:normalize-uri>
    
    <px:message message="Unzipping EPUB" name="epub3-asciimath-to-mathml.message.epub-unzipped"/>
    <p:sink/>
    
    <px:fileset-unzip name="epub3-asciimath-to-mathml.unzip" cx:depends-on="epub3-asciimath-to-mathml.message.epub-unzipped" load-to-memory="false" store-to-disk="true">
        <p:with-option name="href" select="/*/text()">
            <p:pipe port="normalized" step="epub"/>
        </p:with-option>
        <p:with-option name="unzipped-basedir" select="concat(/*/text(),'epub/')">
            <p:pipe port="normalized" step="temp-dir"/>
        </p:with-option>
    </px:fileset-unzip>
    <p:sink/>
    <px:mediatype-detect name="epub3-asciimath-to-mathml.mediatype-detect">
        <p:input port="source">
            <p:pipe port="fileset" step="epub3-asciimath-to-mathml.unzip"/>
        </p:input>
    </px:mediatype-detect>

    <px:message message="Converting from ASCIIMath to MathML"/>
    <px:nordic-epub3-asciimath-to-mathml.step name="epub3-asciimath-to-mathml.convert" fail-on-error="true"/>

    <px:message message="Zipping EPUB"/>
    <px:epub3-store name="epub3-asciimath-to-mathml.store">
        <p:with-option name="href" select="concat(/*/text(),replace($epub,'.*/',''))">
            <p:pipe port="normalized" step="output-dir"/>
        </p:with-option>
        <p:input port="in-memory.in">
            <p:pipe port="in-memory.out" step="epub3-asciimath-to-mathml.convert"/>
        </p:input>
    </px:epub3-store>

</p:declare-step>
