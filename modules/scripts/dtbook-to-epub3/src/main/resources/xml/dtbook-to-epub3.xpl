<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                name="dtbook-to-epub3" type="px:dtbook-to-epub3.script"
                px:input-filesets="dtbook"
                px:output-filesets="epub3"
                exclude-inline-prefixes="#all">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">DTBook to EPUB 3</h1>
        <p px:role="desc">Converts multiple dtbooks to EPUB 3 format</p>
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

    <p:output port="validation-status" px:media-type="application/vnd.pipeline.status+xml">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h2 px:role="name">Status</h2>
        <p px:role="desc" xml:space="preserve">Whether or not the conversion was successful.

When text-to-speech is enabled, the conversion may output a (incomplete) EPUB 3 publication even if the text-to-speech process has errors.</p>
      </p:documentation>
      <p:pipe step="convert-and-store" port="validation-status"/>
    </p:output>

    <p:option name="language" required="false" px:type="string" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Language code</h2>
            <p px:role="desc">Language code of the input document.</p>
        </p:documentation>
    </p:option>

    <p:option name="output-dir" required="true" px:output="result" px:type="anyDirURI">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">EPUB</h2>
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
	<p px:role="desc" xml:space="preserve">Configuration file for the Text-To-Speech.

[More details on the configuration file format](http://daisy.github.io/pipeline/modules/tts-common/doc/tts-config.html).</p>
      </p:documentation>
    </p:option>

    <p:option name="chunk-size" required="false" px:type="integer" select="'-1'">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h2 px:role="name">Chunk size</h2>
        <p px:role="desc" xml:space="preserve">The maximum size of HTML files in kB. Specify "-1" for no maximum.

Top-level sections in the DTBook become separate HTML files in the resulting EPUB, and are further
split up if they exceed the given maximum size.</p>
      </p:documentation>
    </p:option>

    <p:option name="audio" required="false" px:type="boolean" select="'false'">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
	<h2 px:role="name">Enable Text-To-Speech</h2>
	<p px:role="desc">Whether to use a speech synthesizer to produce audio files.</p>
      </p:documentation>
    </p:option>

    <p:import href="convert.xpl"/>

    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/dtbook-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/epub3-ocf-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>

    <px:normalize-uri name="output-dir-uri">
        <p:with-option name="href" select="concat($output-dir,'/')"/>
    </px:normalize-uri>

    <p:split-sequence name="first-dtbook" test="position()=1" initial-only="true"/>
    <p:sink/>

    <p:group name="convert-and-store">
        <p:output port="validation-status">
          <p:pipe step="convert" port="validation-status"/>
        </p:output>

        <p:variable name="output-name" select="replace(replace(base-uri(/),'^.*/([^/]+)$','$1'),'\.[^\.]*$','')">
            <p:pipe port="matched" step="first-dtbook"/>
        </p:variable>
        <p:variable name="output-dir-uri" select="/c:result/string()">
            <p:pipe step="output-dir-uri" port="normalized"/>
        </p:variable>
        <p:variable name="epub-file-uri" select="concat($output-dir-uri,$output-name,'.epub')"/>

	<px:dtbook-load name="load">
            <p:input port="source">
                <p:pipe port="source" step="dtbook-to-epub3"/>
            </p:input>
        </px:dtbook-load>

	<p:choose name="tts-config">
	  <p:when test="$tts-config != ''">
	    <p:xpath-context>
	      <p:empty/>
	    </p:xpath-context>
	    <p:output port="result"/>
	    <p:load>
	      <p:with-option name="href" select="$tts-config"/>
	    </p:load>
	  </p:when>
	  <p:otherwise>
	    <p:output port="result">
	      <p:inline>
		<d:config/>
	      </p:inline>
	    </p:output>
	    <p:sink/>
	  </p:otherwise>
	</p:choose>

	<px:dtbook-to-epub3 name="convert">
	  <p:input port="source.fileset">
	    <p:pipe step="load" port="fileset.out"/>
	  </p:input>
	  <p:input port="source.in-memory">
	    <p:pipe step="load" port="in-memory.out"/>
	  </p:input>
	  <p:input port="tts-config">
	    <p:pipe step="tts-config" port="result"/>
	  </p:input>
	  <p:with-option name="audio" select="$audio"/>
	  <p:with-option name="language" select="$language"/>
	  <p:with-option name="assert-valid" select="$assert-valid"/>
	  <p:with-option name="chunk-size" select="$chunk-size"/>
	  <p:with-option name="output-name" select="$output-name"/>
	  <p:with-option name="output-dir" select="$output-dir-uri"/>
	  <p:with-option name="temp-dir" select="$temp-dir"/>
	</px:dtbook-to-epub3>

        <px:epub3-store name="store">
            <p:input port="in-memory.in">
                <p:pipe step="convert" port="result.in-memory"/>
            </p:input>
            <p:with-option name="href" select="$epub-file-uri"/>
        </px:epub3-store>

    </p:group>

</p:declare-step>
