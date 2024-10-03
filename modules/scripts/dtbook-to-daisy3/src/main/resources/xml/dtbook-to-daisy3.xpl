<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:dtbook="http://www.daisy.org/z3986/2005/dtbook/"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                type="px:dtbook-to-daisy3.script" name="main"
                px:input-filesets="dtbook"
                px:output-filesets="daisy3 mp3"
                exclude-inline-prefixes="#all">

  <p:documentation xmlns="http://www.w3.org/1999/xhtml">
    <h1 px:role="name">DTBook to DAISY 3</h1>
    <p px:role="desc">Converts multiple DTBooks to DAISY 3 format</p>
    <a px:role="homepage" href="http://daisy.github.io/pipeline/Get-Help/User-Guide/Scripts/dtbook-to-daisy3/">
      Online documentation
    </a>
  </p:documentation>

  <p:input port="source" primary="true" px:media-type="application/x-dtbook+xml">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <h2 px:role="name">DTBook file</h2>
      <p px:role="desc">The 2005-3 DTBook file to be transformed.</p>
    </p:documentation>
  </p:input>

  <p:output port="validation-status" px:media-type="application/vnd.pipeline.status+xml">
    <!-- when text-to-speech is enabled, the conversion may output a (incomplete) DAISY 3 even if
         the text-to-speech process has errors -->
    <p:pipe step="convert" port="validation-status"/>
  </p:output>

  <p:output port="tts-log" sequence="true">
    <!-- defined in ../../../../../common-options.xpl -->
    <p:pipe step="convert" port="tts-log"/>
  </p:output>
  <p:serialization port="tts-log" indent="true" omit-xml-declaration="false"/>

  <p:option name="publisher" required="false" px:type="string" select="''">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <h2 px:role="name">Publisher</h2>
      <p px:role="desc">The agency responsible for making the Digital
      Talking Book available. If left blank, it will be retrieved from
      the DTBook meta-data.</p>
    </p:documentation>
  </p:option>

  <p:option name="output-dir" required="true" px:output="result" px:type="anyDirURI">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <h2 px:role="name">DAISY 3</h2>
      <p px:role="desc">The resulting DAISY 3 publication.</p>
    </p:documentation>
  </p:option>

  <p:option name="audio" select="'false'">
    <!-- defined in ../../../../../common-options.xpl -->
  </p:option>

  <p:option name="include-tts-log" select="p:system-property('d:org.daisy.pipeline.tts.log')">
    <!-- defined in ../../../../../common-options.xpl -->
  </p:option>

  <p:input port="tts-config">
    <!-- defined in ../../../../../common-options.xpl -->
    <p:inline><d:config/></p:inline>
  </p:input>

  <p:option xmlns:_="tts" name="_:stylesheet" select="''">
    <!-- defined in ../../../../../common-options.xpl -->
  </p:option>
  
  <p:option name="stylesheet-parameters" cx:as="xs:string*" select="'()'">
      <!-- defined in ../../../../../common-options.xpl -->
  </p:option>

  <p:option name="lexicon" select="p:system-property('d:org.daisy.pipeline.tts.default-lexicon')">
    <!-- defined in ../../../../../common-options.xpl -->
  </p:option>

  <p:option name="audio-file-type" select="'audio/mpeg'" px:hidden="true">
    <!-- the desired file type of the generated audio files -->
  </p:option>

  <p:option name="with-text" required="false" px:type="boolean" select="'true'">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <h2 px:role="name">With text</h2>
      <p px:role="desc">Includes DTBook in output, as opposed to audio only.</p>
    </p:documentation>
  </p:option>

  <p:option name="word-detection" required="false" px:type="boolean" select="'true'">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <h2 px:role="name">Detect words</h2>
      <p px:role="desc" xml:space="preserve">Whether to detect and mark up words with `&lt;w&gt;` tags.

By default word detection is performed but an option is provided to disable it because some DAISY 3
reading systems can't handle the word tags.</p>
    </p:documentation>
  </p:option>

  <!-- TODO: throw an error if both 'audio' and 'with-text' are false -->

  <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
  <p:import href="http://www.daisy.org/pipeline/modules/dtbook-utils/library.xpl"/>
  <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl"/>
  <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
    <p:documentation>
      px:fileset-add-entry
      px:fileset-store
      px:fileset-delete
    </p:documentation>
  </p:import>
  <p:import href="dtbook-to-daisy3.convert.xpl"/>

  <px:normalize-uri name="output-dir-uri">
    <p:with-option name="href" select="concat($output-dir,'/')"/>
  </px:normalize-uri>
  <p:sink/>
  
  <px:fileset-add-entry media-type="application/x-dtbook+xml" name="dtbook">
      <p:input port="entry">
          <p:pipe step="main" port="source"/>
      </p:input>
  </px:fileset-add-entry>
  <px:dtbook-load name="load">
      <p:input port="source.in-memory">
          <p:pipe step="dtbook" port="result.in-memory"/>
      </p:input>
  </px:dtbook-load>

  <px:dtbook-to-daisy3 name="convert" px:progress="1">
    <p:input port="fileset.in">
      <p:pipe step="load" port="result.fileset"/>
    </p:input>
    <p:input port="in-memory.in">
      <p:pipe step="load" port="result.in-memory"/>
    </p:input>
    <p:input port="tts-config">
      <p:pipe step="main" port="tts-config"/>
    </p:input>
    <p:with-option name="stylesheet" xmlns:_="tts" select="string-join(
                                                             for $output-fileset-base in string(/c:result) return
                                                             for $s in tokenize($_:stylesheet,'\s+')[not(.='')] return
                                                               resolve-uri($s,$output-fileset-base),
                                                             ' ')">
      <p:pipe step="output-dir-uri" port="normalized"/>
    </p:with-option>
    <p:with-option name="stylesheet-parameters" select="$stylesheet-parameters"/>
    <p:with-option name="lexicon" select="for $output-fileset-base in string(/c:result) return
                                          for $l in tokenize($lexicon,'\s+')[not(.='')] return
                                            resolve-uri($l,$output-fileset-base)">
      <p:pipe step="output-dir-uri" port="normalized"/>
    </p:with-option>
    <p:with-option name="publisher" select="$publisher"/>
    <p:with-option name="output-fileset-base" select="/c:result/string()">
      <p:pipe step="output-dir-uri" port="normalized"/>
    </p:with-option>
    <p:with-option name="audio" select="$audio='true'"/>
    <p:with-option name="audio-only" select="$with-text='false'"/>
    <p:with-option name="audio-file-type" select="$audio-file-type"/>
    <p:with-option name="word-detection" select="$word-detection='true'"/>
    <p:with-option name="include-tts-log" select="$include-tts-log"/>
  </px:dtbook-to-daisy3>

  <px:fileset-store name="store">
    <p:input port="fileset.in">
      <p:pipe port="fileset.out" step="convert"/>
    </p:input>
    <p:input port="in-memory.in">
      <p:pipe port="in-memory.out" step="convert"/>
    </p:input>
  </px:fileset-store>

  <px:fileset-delete cx:depends-on="store">
    <p:input port="source">
      <p:pipe step="convert" port="temp-audio-files"/>
    </p:input>
  </px:fileset-delete>

</p:declare-step>
