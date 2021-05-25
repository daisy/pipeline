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
        <a px:role="homepage" href="http://daisy.github.io/pipeline/Get-Help/User-Guide/Scripts/zedai-to-epub3/">
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

    <p:option name="include-tts-log" select="'false'">
      <!-- defined in common-options.xpl -->
    </p:option>
    <p:output port="tts-log" sequence="true">
      <!-- defined in common-options.xpl -->
      <p:pipe step="load-convert-store" port="tts-log"/>
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

    <p:input port="tts-config">
      <!-- defined in common-options.xpl -->
      <p:inline><d:config/></p:inline>
    </p:input>

    <p:option name="audio"  select="'false'">
      <!-- defined in common-options.xpl -->
    </p:option>

    <p:option xmlns:_="zedai" name="_:chunk-size" select="'-1'">
      <!-- defined in common-options.xpl -->
    </p:option>

    <p:import href="zedai-to-epub3.convert.xpl"/>

    <p:import href="http://www.daisy.org/pipeline/modules/epub-utils/library.xpl">
      <p:documentation>
        px:epub3-store
      </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
      <p:documentation>
        px:fileset-delete
      </p:documentation>
    </p:import>
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
        <p:output port="tts-log">
          <p:pipe step="convert" port="tts-log"/>
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

        <px:zedai-to-epub3 name="convert">
            <p:input port="in-memory.in">
                <p:pipe step="load" port="in-memory.out"/>
            </p:input>
            <p:input port="tts-config">
              <p:pipe step="main" port="tts-config"/>
            </p:input>
            <p:with-option name="output-dir" select="$temp-dir"/>
	    <p:with-option name="audio" select="$audio"/>
            <p:with-option name="include-tts-log" select="$include-tts-log"/>
        </px:zedai-to-epub3>

        <px:epub3-store>
            <p:with-option name="href" select="$epub-file-uri"/>
            <p:input port="in-memory.in">
                <p:pipe port="in-memory.out" step="convert"/>
            </p:input>
        </px:epub3-store>

        <px:fileset-delete>
          <p:input port="source">
            <p:pipe step="convert" port="temp-audio-files"/>
          </p:input>
        </px:fileset-delete>
    </p:group>

</p:declare-step>
