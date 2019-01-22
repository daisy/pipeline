<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                px:input-filesets="zedai"
                px:output-filesets="epub3 mp3"
                type="px:zedai-to-epub3.script" name="main">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">ZedAI to EPUB 3</h1>
        <p px:role="desc">Transforms a ZedAI (DAISY 4 XML) document into an EPUB 3 publication.</p>
        <a px:role="homepage" href="http://daisy.github.io/pipeline/modules/zedai-to-epub3">
            Online documentation
        </a>
    </p:documentation>

    <p:input port="source" primary="true" px:name="source" px:media-type="application/z3998-auth+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">ZedAI document</h2>
            <p px:role="desc">Input ZedAI.</p>
        </p:documentation>
    </p:input>

    <p:output port="validation-status" px:media-type="application/vnd.pipeline.status+xml">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h2 px:role="name">Status</h2>
        <p px:role="desc" xml:space="preserve">Whether or not the conversion was successful.

When text-to-speech is enabled, the conversion may output a (incomplete) EPUB 3 publication even if the text-to-speech process has errors.</p>
      </p:documentation>
      <p:pipe step="load-convert-store" port="validation-status"/>
    </p:output>

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

    <p:option name="tts-config" required="false" px:type="anyFileURI" select="''">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
	<h2 px:role="name">Text-To-Speech configuration file</h2>
	<p px:role="desc" xml:space="preserve">Configuration file for the Text-To-Speech.

[More details on the configuration file format](http://daisy.github.io/pipeline/modules/tts-common/doc/tts-config.html).</p>
      </p:documentation>
    </p:option>

    <p:option name="audio" required="false" px:type="boolean" select="'false'">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
	<h2 px:role="name">Enable Text-To-Speech</h2>
	<p px:role="desc">Whether to use a speech synthesizer to produce audio files.</p>
      </p:documentation>
    </p:option>

    <p:option name="chunk-size" required="false" px:type="integer" select="'-1'">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h2 px:role="name">Chunk size</h2>
        <p px:role="desc" xml:space="preserve">The maximum size of HTML files in kB. Specify "-1" for no maximum.

Top-level sections in the ZedAI become separate HTML files in the resulting EPUB, and are further
split up if they exceed the given maximum size.</p>
      </p:documentation>
    </p:option>

    <p:import href="zedai-to-epub3.convert.xpl"/>

    <p:import href="http://www.daisy.org/pipeline/modules/epub3-nav-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/epub3-ocf-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/epub3-pub-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/zedai-utils/library.xpl"/>

    <p:variable name="input-uri" select="base-uri(/)"/>

    <px:normalize-uri name="output-dir-uri">
      <p:with-option name="href" select="concat($output-dir,'/')"/>
    </px:normalize-uri>
    <p:sink/>

    <p:group name="load-convert-store">
        <p:output port="validation-status">
          <p:pipe step="convert" port="validation-status"/>
        </p:output>
        <p:variable name="output-dir-uri" select="/c:result/string()">
          <p:pipe step="output-dir-uri" port="normalized"/>
        </p:variable>
        <p:variable name="epub-file-uri" select="concat($output-dir-uri,replace($input-uri,'^.*/([^/]*?)(\.[^/\.]*)?$','$1'),'.epub')"/>

        <px:zedai-load name="load">
            <p:input port="source">
                <p:pipe port="source" step="main"/>
            </p:input>
        </px:zedai-load>

	<p:choose name="tts-config">
	  <p:when test="$tts-config != ''">
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
	
        <px:zedai-to-epub3 name="convert">
	    <p:input port="fileset.in">
	        <p:pipe step="load" port="fileset.out"/>
	    </p:input>
            <p:input port="in-memory.in">
                <p:pipe step="load" port="in-memory.out"/>
            </p:input>
	    <p:input port="tts-config">
	      <p:pipe step="tts-config" port="result"/>
	    </p:input>
            <p:with-option name="output-dir" select="$temp-dir"/>
	    <p:with-option name="audio" select="$audio"/>
        </px:zedai-to-epub3>

        <px:epub3-store>
            <p:with-option name="href" select="$epub-file-uri"/>
            <p:input port="in-memory.in">
                <p:pipe port="in-memory.out" step="convert"/>
            </p:input>
        </px:epub3-store>
    </p:group>

</p:declare-step>
