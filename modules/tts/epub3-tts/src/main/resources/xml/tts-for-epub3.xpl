<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:epub="http://www.idpf.org/2007/ops"
                xmlns:tts="http://www.daisy.org/ns/pipeline/tts"
                xmlns:ssml="http://www.w3.org/2001/10/synthesis"
                exclude-inline-prefixes="#all"
                type="px:tts-for-epub3" name="main">

  <p:input port="source.fileset" primary="true"/>
  <p:input port="source.in-memory" sequence="true">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>The source fileset with HTML documents, lexicons and CSS stylesheets.</p>
    </p:documentation>
  </p:input>

  <p:input port="config">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <h2>TTS configuration file</h2>
      <p>Configuration file with voice mappings, PLS lexicons and annotations.</p>
    </p:documentation>
  </p:input>

  <p:output port="audio-map">
    <p:pipe port="audio-map" step="synthesize"/>
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>List of audio clips mapped to fragments in the HTML document set.</p>
    </p:documentation>
  </p:output>

  <p:output port="result.fileset" primary="true"/>
  <p:output port="result.in-memory" sequence="true">
    <p:pipe step="update-fileset" port="result.in-memory"/>
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
       <p>The result fileset.</p>
       <p>HTML documents are enriched with IDs, words and sentences. Inlined aural CSS is
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

  <p:option name="audio" required="false" cx:type="xs:boolean" cx:as="xs:string" select="'true'">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <h2>Enable Text-To-Speech</h2>
      <p>Whether to use a speech synthesizer to produce
      audio files.</p>
    </p:documentation>
  </p:option>

  <p:option name="audio-file-type" select="'audio/mpeg'">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>The desired file type of the generated audio files, specified as a MIME type.</p>
      <p>Examples:</p>
      <ul>
        <li>"audio/mpeg"</li>
        <li>"audio/x-wav" (but note that this is not a core media type)</li>
      </ul>
    </p:documentation>
  </p:option>

  <p:option name="process-css" required="false" select="'true'">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>Set to false to bypass aural CSS processing.</p>
    </p:documentation>
  </p:option>

  <p:option name="stylesheet" select="''">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>CSS style sheets as space separated list of absolute URIs.</p>
    </p:documentation>
  </p:option>

  <p:option name="stylesheet-parameters" cx:as="xs:string" select="'()'">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>Parameters that are passed to SCSS style sheets.</p>
    </p:documentation>
  </p:option>

  <p:option name="lexicon" cx:as="xs:anyURI*" select="()">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>PLS lexicons as list of absolute URIs.</p>
    </p:documentation>
  </p:option>

  <p:option name="sentence-class" required="false" select="''">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>Class attribute to mark sentences with.</p>
    </p:documentation>
  </p:option>

  <p:option name="temp-dir" select="''">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>Empty directory dedicated to this conversion. May be left empty in which case a temporary
      directory will be automatically created.</p>
    </p:documentation>
  </p:option>

  <p:import href="epub3-to-ssml.xpl">
    <p:documentation>
      px:epub3-to-ssml
    </p:documentation>
  </p:import>
  <p:import href="http://www.daisy.org/pipeline/modules/tts-common/library.xpl">
    <p:documentation>
      px:ssml-to-audio
      px:isolate-skippable
    </p:documentation>
  </p:import>
  <p:import href="http://www.daisy.org/pipeline/modules/html-break-detection/library.xpl">
    <p:documentation>
      px:html-break-detect
      px:html-unwrap-words
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

  <p:variable name="fileset-base" select="base-uri(/*)">
    <p:pipe step="main" port="source.fileset"/>
  </p:variable>

  <p:choose name="process-css" px:progress="1/10">
    <p:when test="$audio='true' and $process-css='true'">
      <p:output port="fileset" primary="true"/>
      <p:output port="in-memory" sequence="true">
        <p:pipe step="cascade" port="result.in-memory"/>
      </p:output>
      <px:css-speech-cascade include-user-agent-stylesheet="true" content-type="application/xhtml+xml" name="cascade">
        <p:input port="source.in-memory">
          <p:pipe step="main" port="source.in-memory"/>
        </p:input>
        <p:with-option name="user-stylesheet" select="$stylesheet"/>
        <p:with-option name="parameters" select="$stylesheet-parameters"/>
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

  <px:fileset-load media-types="application/xhtml+xml" name="html">
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
      <p:output port="html" primary="true" sequence="true"/>
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
      <p:identity/>
    </p:when>

    <!-- ====== TTS ON ====== -->
    <p:otherwise>
      <p:output port="html" primary="true" sequence="true">
        <p:pipe step="for-each" port="html"/>
      </p:output>
      <p:output port="audio-map">
        <p:pipe port="result" step="to-audio"/>
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
        <p:output port="html">
          <p:pipe step="clean-html" port="result"/>
        </p:output>
        <p:group px:progress="1/5">
          <p:documentation>
            Insert "speech-only" spans from @tts:before and @tts:after attributes
          </p:documentation>
          <p:insert match="*[@tts:before]" position="first-child">
            <p:input port="insertion">
              <p:inline><tts:before>[CONTENT]</tts:before></p:inline>
            </p:input>
          </p:insert>
          <p:string-replace match="tts:before/text()" replace="parent::*/parent::*/@tts:before"/>
          <p:insert match="*[@tts:after]" position="last-child">
            <p:input port="insertion">
              <p:inline><tts:after>[CONTENT]</tts:after></p:inline>
            </p:input>
          </p:insert>
          <p:string-replace match="tts:after/text()" replace="parent::*/parent::*/@tts:after"/>
          <p:add-attribute match="tts:before|tts:after"
                           attribute-name="tts:speech-only" attribute-value=""/>
          <p:rename match="tts:before|tts:after"
                    new-name="span" new-namespace="http://www.w3.org/1999/xhtml"/>
        </p:group>
        <px:html-break-detect name="break" px:progress="1/5" px:message="Performing sentence detection">
          <p:with-option name="sentence-attr" select="if ($sentence-class!='') then 'class' else ''"/>
          <p:with-option name="sentence-attr-val" select="$sentence-class"/>
        </px:html-break-detect>
        <px:isolate-skippable name="isolate-skippable" px:progress="1/5"
                              match="*[@epub:type/tokenize(.,'\s+')=('pagebreak','noteref')]|
                                     *[@role='doc-pagebreak']|
                                     *[@role='doc-noteref']">
          <!-- noterefs don't actually need to be skippable (only the notes), but they are isolated
               to not disturb the flow of the surrounding text -->
          <p:input port="sentence-ids">
            <p:pipe step="break" port="sentence-ids"/>
          </p:input>
        </px:isolate-skippable>
        <px:epub3-to-ssml px:progress="1/5" px:message="Generating SSML from HTML">
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
          <p:with-option name="user-lexicons" select="$lexicon"/>
        </px:epub3-to-ssml>
        <px:add-ids match="ssml:s" name="ssml">
          <p:documentation>px:ssml-to-audio requires that all sentences have an id attribute</p:documentation>
        </px:add-ids>
        <p:sink/>
        <p:group px:progress="1/10">
          <p:documentation>
            Unwrap elements with @tts:speech-only attribute and remove text content.
          </p:documentation>
          <p:delete match="*[@tts:speech-only]//text()">
            <p:input port="source">
              <p:pipe step="isolate-skippable" port="result"/>
            </p:input>
          </p:delete>
          <p:unwrap match="*[@tts:speech-only][not(@id)]"/>
          <p:documentation>Remove @tts:* attributes and tts namespace nodes</p:documentation>
          <px:css-speech-clean/>
        </p:group>
        <px:html-unwrap-words px:progress="1/10">
          <p:documentation>
            Remove the word tags because it results in invalid EPUB. (The info is used in the
            synthesize step, but not for synchronization on word level.)
          </p:documentation>
        </px:html-unwrap-words>
        <p:identity name="clean-html"/>
        <p:sink/>
      </p:for-each>
      <px:ssml-to-audio name="to-audio" px:progress="7/9" px:message="Processing SSML">
        <p:input port="config">
          <p:pipe port="config" step="main"/>
        </p:input>
        <p:with-option name="audio-file-type" select="$audio-file-type">
          <p:empty/>
        </p:with-option>
        <p:with-option name="include-log" select="$include-log">
          <p:empty/>
        </p:with-option>
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
      <p:pipe step="html" port="result.fileset"/>
    </p:input>
    <p:input port="update.in-memory">
      <p:pipe step="synthesize" port="html"/>
    </p:input>
  </px:fileset-update>

</p:declare-step>
