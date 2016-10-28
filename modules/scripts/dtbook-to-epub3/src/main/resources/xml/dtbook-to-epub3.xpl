<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step version="1.0" name="dtbook-to-epub3" type="px:dtbook-to-epub3"
    px:input-filesets="dtbook"
    px:output-filesets="epub3"
    xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc" xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal" xmlns:tmp="http://www.daisy.org/ns/pipeline/tmp"
    xmlns:z="http://www.daisy.org/ns/z3986/authoring/" xmlns:dtbook="http://www.daisy.org/z3986/2005/dtbook/" xmlns:html="http://www.w3.org/1999/xhtml" xmlns:d="http://www.daisy.org/ns/pipeline/data" exclude-inline-prefixes="#all">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">DTBook to EPUB3</h1>
        <p px:role="desc">Converts multiple dtbooks to epub3 format</p>
        <a px:role="homepage" href="http://daisy.github.io/pipeline/modules/dtbook-to-epub3">
            Online documentation
        </a>
    </p:documentation>

    <p:input port="source" primary="true" sequence="true" px:media-type="application/x-dtbook+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">DTBook file(s)</h2>
            <p px:role="desc">One or more DTBook files to be transformed. In the case of multiple files, a merge will be performed.</p>
        </p:documentation>
    </p:input>

    <p:option name="language" required="false" px:type="string" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Language code</h2>
            <p px:role="desc">Language code of the input document.</p>
        </p:documentation>
    </p:option>

    <p:option name="output-dir" required="true" px:output="result" px:type="anyDirURI">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">EPUB 3</h2>
            <p px:role="desc">The resulting EPUB 3 publication.</p>
        </p:documentation>
    </p:option>

    <p:option name="temp-dir" required="true" px:output="temp" px:type="anyDirURI">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Temporary directory</h2>
            <p px:role="desc">Directory used for temporary files.</p>
        </p:documentation>
    </p:option>

    <p:option name="assert-valid" required="false" px:type="boolean" select="'true'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Assert validity</h2>
            <p px:role="desc">Whether to stop processing and raise an error on validation issues.</p>
        </p:documentation>
    </p:option>

    <p:option name="tts-config" required="false" px:type="anyFileURI" select="''">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
	<h2 px:role="name">Text-To-Speech configuration file</h2>
	<p px:role="desc">Configuration file for the Text-To-Speech.</p>
      </p:documentation>
    </p:option>

    <p:option name="audio" required="false" px:type="boolean" select="'false'">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
	<h2 px:role="name">Enable Text-To-Speech</h2>
	<p px:role="desc">Whether to use a speech synthesizer to produce audio files.</p>
      </p:documentation>
    </p:option>

    <p:import href="http://www.daisy.org/pipeline/modules/dtbook-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/dtbook-to-zedai/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/zedai-to-epub3/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/epub3-ocf-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/css-speech/library.xpl"/>

    <p:split-sequence name="first-dtbook" test="position()=1" initial-only="true"/>
    <p:sink/>

    <p:xslt name="output-dir-uri">
        <p:with-param name="href" select="concat($output-dir,'/')"/>
        <p:input port="source">
            <p:inline>
                <d:file/>
            </p:inline>
        </p:input>
        <p:input port="stylesheet">
            <p:inline>
                <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:pf="http://www.daisy.org/ns/pipeline/functions" version="2.0">
                    <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/uri-functions.xsl"/>
                    <xsl:param name="href" required="yes"/>
                    <xsl:template match="/*">
                        <xsl:copy>
                            <xsl:attribute name="href" select="pf:normalize-uri($href)"/>
                        </xsl:copy>
                    </xsl:template>
                </xsl:stylesheet>
            </p:inline>
        </p:input>
    </p:xslt>
    <p:sink/>

    <p:group>
        <p:variable name="output-name" select="replace(replace(base-uri(/),'^.*/([^/]+)$','$1'),'\.[^\.]*$','')">
            <p:pipe port="matched" step="first-dtbook"/>
        </p:variable>

        <p:variable name="output-dir-uri" select="/*/@href">
            <p:pipe port="result" step="output-dir-uri"/>
        </p:variable>
        <p:variable name="epub-file-uri" select="concat($output-dir-uri,$output-name,'.epub')"/>

	<px:dtbook-load name="load">
            <p:input port="source">
                <p:pipe port="source" step="dtbook-to-epub3"/>
            </p:input>
        </px:dtbook-load>

	<p:choose name="load-tts-config">
	  <p:when test="$tts-config != ''">
	    <p:xpath-context>
	      <p:empty/>
	    </p:xpath-context>
	    <p:output port="result" primary="true"/>
	    <p:load>
	      <p:with-option name="href" select="$tts-config"/>
	    </p:load>
	  </p:when>
	  <p:otherwise>
	    <p:output port="result" primary="true">
	      <p:inline>
		<d:config/>
	      </p:inline>
	    </p:output>
	    <p:sink/>
	  </p:otherwise>
	</p:choose>
	<p:choose name="css-inlining">
	  <p:when test="$audio = 'true'">
	    <p:output port="result" primary="true"/>
	    <px:inline-css-speech>
	      <p:input port="source">
	    	<p:pipe port="in-memory.out" step="load"/>
	      </p:input>
	      <p:input port="fileset.in">
	    	<p:pipe port="fileset.out" step="load"/>
	      </p:input>
	      <p:input port="config">
	    	<p:pipe port="result" step="load-tts-config"/>
	      </p:input>
	      <p:with-option name="content-type" select="'application/x-dtbook+xml'"/>
	    </px:inline-css-speech>
	  </p:when>
	  <p:otherwise>
	    <p:output port="result" primary="true"/>
	    <p:identity>
	      <p:input port="source">
		<p:pipe port="in-memory.out" step="load"/>
	      </p:input>
	    </p:identity>
	  </p:otherwise>
	</p:choose>

        <px:dtbook-to-zedai-convert name="convert.dtbook-to-zedai">
	    <p:input port="fileset.in">
	        <p:pipe port="fileset.out" step="load"/>
	    </p:input>
            <p:input port="in-memory.in">
	      <p:pipe port="result" step="css-inlining"/>
            </p:input>
            <p:with-option name="opt-output-dir" select="concat($output-dir-uri,'zedai/')"/>
            <p:with-option name="opt-zedai-filename" select="concat($output-name,'.xml')"/>
            <p:with-option name="opt-lang" select="$language"/>
            <p:with-option name="opt-assert-valid" select="$assert-valid"/>
        </px:dtbook-to-zedai-convert>

        <!--TODO better handle core media type filtering-->
        <!--TODO copy/translate CSS ?-->
        <p:delete name="filtered-zedai-fileset"
            match="d:file[not(@media-type=('application/z3998-auth+xml',
            'image/gif','image/jpeg','image/png','image/svg+xml',
            'application/pls+xml',
            'audio/mpeg','audio/mp4','text/javascript'))]"/>

        <px:zedai-to-epub3-convert name="convert.zedai-to-epub3">
            <p:input port="in-memory.in">
                <p:pipe port="in-memory.out" step="convert.dtbook-to-zedai"/>
            </p:input>
            <p:input port="tts-config">
	      <p:pipe port="result" step="load-tts-config"/>
            </p:input>
            <p:with-option name="output-dir" select="$temp-dir"/>
            <p:with-option name="audio" select="$audio"/>
        </px:zedai-to-epub3-convert>

        <px:epub3-store name="store">
            <p:input port="in-memory.in">
                <p:pipe port="in-memory.out" step="convert.zedai-to-epub3"/>
            </p:input>
            <p:with-option name="href" select="$epub-file-uri"/>
        </px:epub3-store>

    </p:group>

</p:declare-step>
