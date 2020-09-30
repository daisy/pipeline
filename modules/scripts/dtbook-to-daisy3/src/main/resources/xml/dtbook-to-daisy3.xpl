<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:dtbook="http://www.daisy.org/z3986/2005/dtbook/"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                type="px:dtbook-to-daisy3.script" name="main"
                px:input-filesets="dtbook"
                px:output-filesets="daisy3 mp3"
                exclude-inline-prefixes="#all">

  <p:documentation xmlns="http://www.w3.org/1999/xhtml">
    <h1 px:role="name">DTBook to DAISY 3</h1>
    <p px:role="desc">Converts multiple DTBooks to DAISY 3 format</p>
    <a px:role="homepage" href="http://daisy.github.io/pipeline/modules/dtbook-to-daisy3">
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
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <h2 px:role="name">Status</h2>
      <p px:role="desc" xml:space="preserve">Whether or not the conversion was successful.

When text-to-speech is enabled, the conversion may output a (incomplete) DAISY 3 even if the text-to-speech process has errors.</p>
    </p:documentation>
    <p:pipe step="convert" port="validation-status"/>
  </p:output>

  <p:output port="tts-log" sequence="true">
    <!-- defined in common-options.xpl -->
    <p:pipe step="convert" port="tts-log"/>
  </p:output>

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

  <p:input port="tts-config">
    <!-- defined in common-options.xpl -->
    <p:inline><d:config/></p:inline>
  </p:input>

  <p:option name="audio" select="'false'">
    <!-- defined in common-options.xpl -->
  </p:option>

  <p:option name="with-text" required="false" px:type="boolean" select="'true'">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <h2 px:role="name">With text</h2>
      <p px:role="desc">Includes DTBook in output, as opposed to audio only.</p>
    </p:documentation>
  </p:option>

  <!-- TODO: throw an error if both 'audio' and 'with-text' are false -->

  <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
  <p:import href="http://www.daisy.org/pipeline/modules/dtbook-utils/library.xpl"/>
  <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl"/>
  <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
    <p:documentation>
      px:fileset-store
      px:fileset-delete
    </p:documentation>
  </p:import>
  <p:import href="dtbook-to-daisy3.convert.xpl"/>

  <px:normalize-uri name="output-dir-uri">
    <p:with-option name="href" select="concat($output-dir,'/')"/>
  </px:normalize-uri>
  
  <px:dtbook-load name="load"/>

  <px:dtbook-to-daisy3 name="convert" px:progress="1">
    <p:input port="fileset.in">
      <p:pipe step="load" port="fileset.out"/>
    </p:input>
    <p:input port="in-memory.in">
      <p:pipe step="load" port="in-memory.out"/>
    </p:input>
    <p:input port="tts-config">
      <p:pipe step="main" port="tts-config"/>
    </p:input>
    <p:with-option name="publisher" select="$publisher"/>
    <p:with-option name="output-fileset-base" select="/c:result/string()">
      <p:pipe step="output-dir-uri" port="normalized"/>
    </p:with-option>
    <p:with-option name="audio" select="$audio"/>
    <p:with-option name="audio-only" select="$with-text = 'false'"/>
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
