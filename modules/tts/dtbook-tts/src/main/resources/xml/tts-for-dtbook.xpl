<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step version="1.0" name="main" type="px:tts-for-dtbook"
		xmlns:p="http://www.w3.org/ns/xproc"
		xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
		xmlns:d="http://www.daisy.org/ns/pipeline/data"
		exclude-inline-prefixes="#all">

  <p:input port="content.in" primary="true" sequence="true">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>List of DTBook documents.</p>
    </p:documentation>
  </p:input>

  <p:input port="fileset.in">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
       <p>Input fileset including DTBook documents, lexicons and CSS stylesheets.</p>
    </p:documentation>
  </p:input>

  <p:input port="config">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
       <p>Configuration file with lexicons, voices declaration and various properties.</p>
    </p:documentation>
  </p:input>

  <p:output port="audio-map">
    <p:pipe port="audio-map" step="synthesize"/>
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
       <p>List of audio clips (see pipeline-mod-tts documentation).</p>
    </p:documentation>
  </p:output>

  <p:output port="content.out" primary="true" sequence="true">
    <p:pipe port="result" step="remove-css"/>
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
       <p>Copy of the DTBook documents enriched with ids, words and
       sentences, and without inlined aural CSS.</p>
    </p:documentation>
  </p:output>

  <p:output port="sentence-ids" sequence="true">
    <p:pipe port="sentence-ids" step="lexing"/>
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>Every document of this port is a list of nodes whose id
      attribute refers to elements of the 'content.out'
      documents. Grammatically speaking, the referred elements are
      sentences even if the underlying XML elements are not meant to
      be so. Documents are listed in the same order as in
      'content.out'.</p>
    </p:documentation>
  </p:output>

  <p:output port="status">
    <p:pipe step="synthesize" port="status"/>
  </p:output>

  <p:option name="audio" required="false" px:type="boolean" select="'true'">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <h2 px:role="name">Enable Text-To-Speech</h2>
      <p px:role="desc">Whether to use a speech synthesizer to produce audio files.</p>
    </p:documentation>
  </p:option>

  <p:option name="output-dir" required="true">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <h2 px:role="name">Output directory</h2>
      <p px:role="desc">Output directory for log files or other things.</p>
    </p:documentation>
  </p:option>

  <p:import href="http://www.daisy.org/pipeline/modules/ssml-to-audio/library.xpl" />
  <p:import href="http://www.daisy.org/pipeline/modules/dtbook-to-ssml/library.xpl" />
  <p:import href="http://www.daisy.org/pipeline/modules/dtbook-break-detection/library.xpl"/>
  <p:import href="http://www.daisy.org/pipeline/modules/daisy3-utils/library.xpl"/>
  <p:import href="http://www.daisy.org/pipeline/modules/tts-helpers/library.xpl"/>
  <p:import href="http://www.daisy.org/pipeline/modules/css-speech/library.xpl"/>

  <!-- Find the sentences and the words, even if the Text-To-Speech is off. -->
  <p:for-each name="lexing">
    <p:iteration-source>
      <!-- For now, the for-each is actually not needed since there is
           only one DTBook. -->
      <p:pipe port="content.in" step="main"/>
    </p:iteration-source>
    <p:output port="result" primary="true"/>
    <p:output port="sentence-ids">
      <p:pipe port="sentence-ids" step="break"/>
    </p:output>
    <p:output port="skippable-ids">
      <p:pipe port="skippable-ids" step="isolate-skippable"/>
    </p:output>
    <px:dtbook-break-detect name="break"/>
    <px:isolate-daisy3-skippable name="isolate-skippable">
      <p:input port="sentence-ids">
	<p:pipe port="sentence-ids" step="break"/>
      </p:input>
      <p:with-option name="id-prefix" select="concat('i', p:iteration-position())"/>
    </px:isolate-daisy3-skippable>
  </p:for-each>

  <p:choose name="synthesize">
    <p:when test="$audio = 'false'">
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
      <p:sink/>
    </p:when>
    <p:otherwise>
      <p:output port="audio-map">
	<p:pipe step="to-audio" port="result"/>
      </p:output>
      <p:output port="status">
	<p:pipe step="to-audio" port="status"/>
      </p:output>
      <p:for-each name="for-each.content">
	<p:iteration-source>
	  <p:pipe port="result" step="lexing"/>
	</p:iteration-source>
	<p:output port="ssml.out" primary="true" sequence="true">
	  <p:pipe port="result" step="ssml-gen"/>
	</p:output>
	<p:split-sequence name="sentence-ids">
	  <p:input port="source">
	    <p:pipe port="sentence-ids" step="lexing"/>
	  </p:input>
	  <p:with-option name="test" select="concat('position()=', p:iteration-position())"/>
	</p:split-sequence>
	<p:split-sequence name="skippable-ids">
	  <p:input port="source">
	    <p:pipe port="skippable-ids" step="lexing"/>
	  </p:input>
	  <p:with-option name="test" select="concat('position()=', p:iteration-position())"/>
	</p:split-sequence>
	<px:dtbook-to-ssml name="ssml-gen">
	  <p:input port="content.in">
	    <p:pipe port="current" step="for-each.content"/>
	  </p:input>
	  <p:input port="sentence-ids">
	    <p:pipe port="matched" step="sentence-ids"/>
	  </p:input>
	  <p:input port="skippable-ids">
	    <p:pipe port="matched" step="skippable-ids"/>
	  </p:input>
	  <p:input port="fileset.in">
	    <p:pipe port="fileset.in" step="main"/>
	  </p:input>
	  <p:input port="config">
	    <p:pipe port="config" step="main"/>
	  </p:input>
	</px:dtbook-to-ssml>
      </p:for-each>
      <px:ssml-to-audio name="to-audio">
	<p:input port="config">
	  <p:pipe port="config" step="main"/>
	</p:input>
	<p:with-option name="output-dir" select="$output-dir">
	  <p:empty/>
	</p:with-option>
      </px:ssml-to-audio>
    </p:otherwise>
  </p:choose>

  <p:for-each name="remove-css">
    <p:iteration-source>
      <p:pipe port="result" step="lexing"/>
    </p:iteration-source>
    <p:output port="result" primary="true"/>
    <px:remove-inline-css-speech/>
  </p:for-each>

</p:declare-step>
