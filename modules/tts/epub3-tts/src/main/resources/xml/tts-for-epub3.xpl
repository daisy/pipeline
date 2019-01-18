<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step version="1.0" name="main" type="px:tts-for-epub3"
		xmlns:p="http://www.w3.org/ns/xproc"
		xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
		xmlns:d="http://www.daisy.org/ns/pipeline/data"
		exclude-inline-prefixes="#all">

  <p:input port="in-memory.in" primary="true" sequence="true">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>List of documents in memory, including one or more HTML
      documents and lexicons.</p>
    </p:documentation>
  </p:input>

  <p:input port="fileset.in">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
       <p>Input filesets including HTML documents, lexicons and CSS
       stylesheets.</p>
    </p:documentation>
  </p:input>

  <p:input port="config">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <h2 px:role="name">Text-To-Speech configuration file</h2>
      <p px:role="desc">Configuration file that contains Text-To-Speech
      properties, links to aural CSS stylesheets and links to PLS
      lexicons.</p>
    </p:documentation>
  </p:input>

  <p:output port="audio-map">
    <p:pipe port="audio-map" step="synthesize"/>
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
       <p>List of audio clips (see pipeline-mod-tts
       documentation).</p>
    </p:documentation>
  </p:output>

  <p:output port="content.out" primary="true" sequence="true">
    <p:pipe port="content.out" step="synthesize"/>
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
       <p>Copy of HTML documents of in-memory.in enriched with IDs,
       words and sentences.</p>
    </p:documentation>
  </p:output>

  <p:output port="in-memory.out" sequence="true">
    <p:pipe port="non-html" step="html-filter"/>
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
       <p>Copy of in-memory.out without the HTML documents of
       content.out</p>
    </p:documentation>
  </p:output>

  <p:output port="sentence-ids" sequence="true">
    <p:pipe port="sentence-ids" step="synthesize"/>
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
      <p px:role="desc">Whether to use a speech synthesizer to produce
      audio files.</p>
    </p:documentation>
  </p:option>

  <!-- Might be useful some day: -->
  <!-- <p:option name="segmentation" required="false" px:type="boolean" select="'true'"> -->
  <!--   <p:documentation xmlns="http://www.w3.org/1999/xhtml"> -->
  <!--     <h2 px:role="name">Enable segmentation</h2> -->
  <!--     <p px:role="desc">Whether to segment the text or not, i.e. word and sentence boundary detection.</p> -->
  <!--   </p:documentation> -->
  <!-- </p:option> -->

  <p:option name="ssml-of-lexicons-uris" required="false" px:type="anyURI" select="''">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <h2 px:role="name">Lexicons SSML pointers</h2>
      <p px:role="desc">URI of an SSML file which contains a list of
      lexicon elements with their URI. The lexicons will be provided
      to the Text-To-Speech processors.</p>
    </p:documentation>
  </p:option>

  <p:option name="anti-conflict-prefix" required="false"  select="''">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <h2 px:role="name">Prefix for IDs</h2>
      <p px:role="desc">The IDs will be prefixed so as to prevent conflicts.</p>
    </p:documentation>
  </p:option>

  <p:option name="output-dir" required="true">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <h2 px:role="name">Output directory</h2>
      <p px:role="desc">Output directory for log files or other things.</p>
    </p:documentation>
  </p:option>

  <p:option name="temp-dir" select="''">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>Empty directory dedicated to this conversion. May be left empty in which case a temporary
      directory will be automaticall created.</p>
    </p:documentation>
  </p:option>

  <p:import href="http://www.daisy.org/pipeline/modules/ssml-to-audio/library.xpl" />
  <p:import href="http://www.daisy.org/pipeline/modules/epub3-to-ssml/library.xpl" />
  <p:import href="http://www.daisy.org/pipeline/modules/html-break-detection/library.xpl"/>
  <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
  <p:import href="http://www.daisy.org/pipeline/modules/css-speech/library.xpl"/>

  <p:variable name="fileset-base" select="base-uri(/*)">
    <p:pipe port="fileset.in" step="main"/>
  </p:variable>

  <p:for-each name="html-filter">
    <p:output port="html" sequence="true" primary="true">
      <p:pipe port="html" step="is.html"/>
    </p:output>
    <p:output port="non-html" sequence="true">
      <p:pipe port="non-html" step="is.html"/>
    </p:output>
    <p:iteration-source>
      <p:pipe port="in-memory.in" step="main"/>
    </p:iteration-source>
    <p:variable name="doc-uri" select="base-uri(/*)"/>
    <p:choose name="is.html">
      <p:xpath-context>
	<p:pipe port="fileset.in" step="main"/>
      </p:xpath-context>
      <p:when test="//*[@media-type='application/xhtml+xml']/resolve-uri(@href, $fileset-base)=$doc-uri">
	<p:output port="html">
	  <p:pipe port="result" step="id"/>
	</p:output>
	<p:output port="non-html">
	  <p:empty/>
	</p:output>
	<p:identity name="id"/>
      </p:when>
      <p:otherwise>
	<p:output port="html">
	  <p:empty/>
	</p:output>
	<p:output port="non-html">
	  <p:pipe port="result" step="id"/>
	</p:output>
	<p:identity name="id"/>
      </p:otherwise>
    </p:choose>
  </p:for-each>

  <p:choose name="synthesize">
    <!-- ====== TTS OFF ====== -->
    <p:when test="$audio = 'false'">
      <p:xpath-context>
	<p:empty/>
      </p:xpath-context>
      <p:output port="audio-map">
	<p:inline>
	  <d:audio-clips/>
	</p:inline>
      </p:output>
      <p:output port="content.out" primary="true" sequence="true">
	<p:pipe port="html" step="html-filter"/>
      </p:output>
      <p:output port="sentence-ids" sequence="true">
	<p:empty/>
      </p:output>
      <p:output port="status">
	<p:inline>
	  <d:status result="ok"/>
	</p:inline>
      </p:output>
      <p:sink/>
    </p:when>

    <!-- ====== TTS ON ====== -->
    <p:otherwise>
      <p:output port="audio-map">
	<p:pipe port="result" step="to-audio"/>
      </p:output>
      <p:output port="content.out" primary="true" sequence="true">
	<p:pipe port="content.out" step="loop"/>
      </p:output>
      <p:output port="sentence-ids" sequence="true">
	<p:pipe port="sentence-ids" step="loop"/>
      </p:output>
      <p:output port="status">
	<p:pipe step="to-audio" port="status"/>
      </p:output>
      <p:for-each name="loop">
	<p:output port="ssml.out" primary="true" sequence="true">
	  <p:pipe port="result" step="ssml-gen"/>
	</p:output>
	<p:output port="content.out">
	  <p:pipe port="result" step="rm-css"/>
	</p:output>
	<p:output port="sentence-ids">
	  <p:pipe port="sentence-ids" step="lexing"/>
	</p:output>
	<px:html-break-detect name="lexing">
	  <p:with-option name="id-prefix" select="concat($anti-conflict-prefix, p:iteration-position(), '-')"/>
	</px:html-break-detect>
	<px:epub3-to-ssml name="ssml-gen">
	  <p:input port="content.in">
	    <p:pipe port="result" step="lexing"/>
	  </p:input>
	  <p:input port="sentence-ids">
	    <p:pipe port="sentence-ids" step="lexing"/>
	  </p:input>
	  <p:input port="fileset.in">
	    <p:pipe port="fileset.in" step="main"/>
	  </p:input>
	  <p:input port="config">
	    <p:pipe port="config" step="main"/>
	  </p:input>
	</px:epub3-to-ssml>
	<px:remove-inline-css-speech name="rm-css">
	  <p:input port="source">
	    <p:pipe port="result" step="lexing"/>
	  </p:input>
	</px:remove-inline-css-speech>
      </p:for-each>
      <px:ssml-to-audio name="to-audio">
	<p:input port="config">
	  <p:pipe port="config" step="main"/>
	</p:input>
	<p:with-option name="output-dir" select="$output-dir">
	  <p:empty/>
	</p:with-option>
	<p:with-option name="temp-dir" select="if ($temp-dir!='') then concat($temp-dir,'audio/') else ''">
	  <p:empty/>
	</p:with-option>
      </px:ssml-to-audio>
    </p:otherwise>
  </p:choose>

</p:declare-step>
