<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
                xmlns:math="http://www.w3.org/1998/Math/MathML"
                xmlns:ssml="http://www.w3.org/2001/10/synthesis"
                type="px:tts-for-dtbook" name="main"
                exclude-inline-prefixes="#all">

  <p:input port="source.fileset" primary="true">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>The source fileset with DTBook documents, lexicons and CSS style sheets.</p>
    </p:documentation>
  </p:input>
  <p:input port="source.in-memory" sequence="true"/>

  <p:input port="config">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
       <p>Configuration file with voice mappings, PLS lexicons and annotations.</p>
    </p:documentation>
  </p:input>

  <p:output port="audio-map">
    <p:pipe step="synthesize" port="audio-map"/>
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
       <p>List of audio clips mapped to fragments in the DTBook document set.</p>
    </p:documentation>
  </p:output>

  <p:output port="result.fileset" primary="true"/>
  <p:output port="result.in-memory" sequence="true">
    <p:pipe step="update-fileset" port="result.in-memory"/>
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>The result fileset.</p>
      <p>DTBook documents are enriched with IDs, words and sentences. Inlined aural CSS is
      removed.</p>
    </p:documentation>
  </p:output>

  <p:output port="status">
    <p:pipe step="synthesize" port="status"/>
  </p:output>

  <p:option name="include-log" select="'false'">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>Whether or not to make the TTS log available on the "log" port.</p>
    </p:documentation>
  </p:option>
  <p:output port="log" sequence="true">
    <p:pipe step="synthesize" port="log"/>
  </p:output>

  <p:option name="audio" required="false" cx:type="xs:boolean" select="'true'" cx:as="xs:string">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <h2>Enable Text-To-Speech</h2>
      <p>Whether to use a speech synthesizer to produce audio files.</p>
    </p:documentation>
  </p:option>

  <p:option name="audio-file-type" select="'audio/mpeg'">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>The desired file type of the generated audio files, specified as a MIME type.</p>
      <p>Examples:</p>
      <ul>
        <li>"audio/mpeg"</li>
        <li>"audio/x-wav"</li>
      </ul>
    </p:documentation>
  </p:option>

  <p:option name="process-css" required="false" select="'true'" cx:as="xs:string">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>Set to false to bypass aural CSS processing.</p>
    </p:documentation>
  </p:option>

  <p:option name="stylesheet" select="''">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>CSS user style sheets as space separated list of absolute URIs.</p>
    </p:documentation>
  </p:option>

  <p:option name="word-detection" required="false" select="'true'" cx:as="xs:string">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>Whether to detect and mark up words with <code>&lt;w&gt;</code> tags.</p>
    </p:documentation>
  </p:option>

  <p:option name="temp-dir" select="''">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>Empty directory dedicated to this conversion. May be left empty in which case a temporary
      directory will be automatically created.</p>
    </p:documentation>
  </p:option>

  <p:import href="dtbook-to-ssml.xpl">
    <p:documentation>
      px:dtbook-to-ssml
    </p:documentation>
  </p:import>
  <p:import href="http://www.daisy.org/pipeline/modules/dtbook-break-detection/library.xpl">
    <p:documentation>
      px:dtbook-break-detect
      px:dtbook-unwrap-words
    </p:documentation>
  </p:import>
  <p:import href="http://www.daisy.org/pipeline/modules/tts-common/library.xpl">
    <p:documentation>
      px:ssml-to-audio
      px:isolate-skippable
    </p:documentation>
  </p:import>
  <p:import href="http://www.daisy.org/pipeline/modules/css-utils/library.xpl">
    <p:documentation>
      px:css-speech-cascade
      px:css-speech-clean
    </p:documentation>
  </p:import>
  <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
    <p:documentation>
      px:fileset-load
      px:fileset-update
    </p:documentation>
  </p:import>
  <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
    <p:documentation>
      px:add-ids
    </p:documentation>
  </p:import>

  <p:choose name="process-css" px:progress="1/10">
    <p:when test="$audio='true' and $process-css='true'">
      <p:output port="fileset" primary="true"/>
      <p:output port="in-memory" sequence="true">
        <p:pipe step="cascade" port="result.in-memory"/>
      </p:output>
      <px:css-speech-cascade content-type="application/x-dtbook+xml" name="cascade">
        <p:input port="source.in-memory">
          <p:pipe step="main" port="source.in-memory"/>
        </p:input>
        <p:with-option name="user-stylesheet" select="$stylesheet"/>
      </px:css-speech-cascade>
    </p:when>
    <p:otherwise>
      <p:output port="fileset" primary="true"/>
      <p:output port="in-memory" sequence="true">
        <p:pipe step="main" port="source.in-memory"/>
      </p:output>
      <p:identity/>
    </p:otherwise>
  </p:choose>

  <px:fileset-load media-types="application/x-dtbook+xml" name="dtbook">
    <p:input port="in-memory">
      <p:pipe step="process-css" port="in-memory"/>
    </p:input>
  </px:fileset-load>

  <p:choose name="synthesize" px:progress="9/10">
    <!-- ====== TTS OFF ====== -->
    <p:when test="$audio = 'false'">
      <p:xpath-context>
        <p:empty/>
      </p:xpath-context>
      <p:output port="dtbook" primary="true" sequence="true"/>
      <p:output port="audio-map">
	<p:inline>
	  <d:audio-clips/>
	</p:inline>
      </p:output>
      <p:output port="status">
	<p:inline>
	  <d:status result="ok"/>
	</p:inline>
      </p:output>
      <p:output port="log" sequence="true">
	<p:empty/>
      </p:output>
      <!-- Find the sentences and the words, even if the Text-To-Speech is off. -->
      <p:for-each px:progress="1">
	<px:dtbook-break-detect name="break" px:progress="1/3"/>
	<px:isolate-skippable match="dtb:pagenum|dtb:noteref|dtb:annoref|dtb:linenum|math:math" px:progress="1/3">
	  <p:input port="sentence-ids">
	    <p:pipe step="break" port="sentence-ids"/>
	  </p:input>
	</px:isolate-skippable>
	<p:choose px:progress="1/3">
	  <p:when test="$word-detection='false'">
	    <px:dtbook-unwrap-words px:progress="1"/>
	  </p:when>
	  <p:otherwise>
	    <p:identity/>
	  </p:otherwise>
	</p:choose>
      </p:for-each>
    </p:when>

    <!-- ====== TTS ON ====== -->
    <p:otherwise>
      <p:output port="dtbook" primary="true" sequence="true">
	<p:pipe step="for-each" port="dtbook"/>
      </p:output>
      <p:output port="audio-map">
	<p:pipe step="to-audio" port="result"/>
      </p:output>
      <p:output port="status">
	<p:pipe step="to-audio" port="status"/>
      </p:output>
      <p:output port="log" sequence="true">
	<p:pipe step="to-audio" port="log"/>
      </p:output>
      <p:for-each name="for-each" px:progress="0.2">
	<p:output port="ssml" primary="true" sequence="true">
	  <p:pipe step="ssml" port="result"/>
	</p:output>
	<p:output port="dtbook">
	  <p:pipe step="clean-dtbook" port="result"/>
	</p:output>
	<!-- It is necessary to apply px:dtbook-break-detect and px:isolate-skippable to
	     split the content around the skippable elements (pagenums and noterefs) so
	     they can be attached to a smilref attribute that won't be the descendant of
	     any audio clip. Otherwise we risk having pagenums without @smilref, which
	     is not allowed by the specs. -->
	<px:dtbook-break-detect name="break" px:progress="1/5"/>
	<px:isolate-skippable name="isolate-skippable"
			      match="dtb:pagenum|dtb:noteref|dtb:annoref|dtb:linenum|math:math"
			      px:progress="1/5">
	  <p:input port="sentence-ids">
	    <p:pipe step="break" port="sentence-ids"/>
	  </p:input>
	</px:isolate-skippable>
	<px:dtbook-to-ssml px:message="SSML generation for DTBook" px:progress="1/5">
	  <p:input port="sentence-ids">
	    <p:pipe step="break" port="sentence-ids"/>
	  </p:input>
	  <p:input port="skippable-ids">
	    <p:pipe step="isolate-skippable" port="skippable-ids"/>
	  </p:input>
	  <p:input port="fileset.in">
	    <p:pipe step="process-css" port="fileset"/>
	  </p:input>
	  <p:input port="config">
	    <p:pipe port="config" step="main"/>
	  </p:input>
	</px:dtbook-to-ssml>
	<px:add-ids match="ssml:s" name="ssml">
	  <p:documentation>px:ssml-to-audio requires that all sentences have an id attribute</p:documentation>
	</px:add-ids>
	<p:sink/>
	<px:css-speech-clean px:progress="1/5">
	  <p:input port="source">
	    <p:pipe step="isolate-skippable" port="result"/>
	  </p:input>
	</px:css-speech-clean>
	<p:choose px:progress="1/5">
	  <p:when test="$word-detection='false'">
	    <px:dtbook-unwrap-words px:progress="1"/>
	  </p:when>
	  <p:otherwise>
	    <p:identity/>
	  </p:otherwise>
	</p:choose>
	<p:identity name="clean-dtbook"/>
	<p:sink/>
      </p:for-each>
      <px:ssml-to-audio name="to-audio" px:progress="0.8">
	<p:with-option name="audio-file-type" select="$audio-file-type">
	  <p:empty/>
	</p:with-option>
	<p:with-option name="include-log" select="$include-log">
	  <p:empty/>
	</p:with-option>
	<p:input port="config">
	  <p:pipe port="config" step="main"/>
	</p:input>
	<p:with-option name="temp-dir" select="if ($temp-dir!='') then concat($temp-dir,'audio/') else ''">
	  <p:empty/>
	</p:with-option>
      </px:ssml-to-audio>
    </p:otherwise>
  </p:choose>
  <p:sink/>

  <px:fileset-update name="update-fileset">
    <p:input port="source.fileset">
      <p:pipe step="process-css" port="fileset"/>
    </p:input>
    <p:input port="source.in-memory">
      <p:pipe step="process-css" port="in-memory"/>
    </p:input>
    <p:input port="update.fileset">
      <p:pipe step="dtbook" port="result.fileset"/>
    </p:input>
    <p:input port="update.in-memory">
      <p:pipe step="synthesize" port="dtbook"/>
    </p:input>
  </px:fileset-update>

</p:declare-step>
