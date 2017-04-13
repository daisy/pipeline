<p:library version="1.0" xmlns:p="http://www.w3.org/ns/xproc">
  <p:declare-step version="1.0" name="main" type="px:tts-for-dtbook"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                exclude-inline-prefixes="#all">

    <p:input port="content.in" primary="true" sequence="true"/>
    <p:input port="fileset.in"/>
    <p:input port="config"/>

    <p:output port="audio-map">
      <p:pipe port="clips" step="build-audio-map"/>
    </p:output>

    <p:output port="content.out" primary="true" sequence="true">
      <p:pipe port="content" step="build-audio-map"/>
    </p:output>

    <p:output port="sentence-ids" sequence="true">
      <p:empty/> <!-- not used anywhere so far -->
    </p:output>

    <p:option name="audio" required="false" px:type="boolean" select="'true'"/>
    <p:option name="output-dir" required="false" select="''"/>

    <p:import href="http://www.daisy.org/pipeline/modules/dtbook-break-detection/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/daisy3-utils/library.xpl"/>

    <p:choose name="build-audio-map">
      <p:when test="$audio = 'false'">
	<p:output port="clips" primary="true">
	  <p:inline>
	    <d:audio-clips/>
	  </p:inline>
	</p:output>
	<p:output port="content">
	  <p:pipe port="content.in" step="main"/>
	</p:output>
	<p:sink/>
      </p:when>
      <p:otherwise>
	<p:output port="clips" primary="true"/>
	<p:output port="content">
	  <p:pipe port="result" step="isolate"/>
	</p:output>
	<!-- It is necessary to apply NLP and daisy3-utils to split the content around the
	     skippable elements (pagenums and noterefs) so they can be attached to a
	     smilref attribute that won't be the descendant of any audio clip. Otherwise
	     we risk having pagenums without @smilref, which is not allowed by the
	     specs. -->
	<px:dtbook-break-detect name="break">
	  <p:input port="source">
	    <!-- we are assuming that there is only the DTBook in content.in -->
	    <p:pipe port="content.in" step="main"/>
	  </p:input>
	</px:dtbook-break-detect>
	<px:isolate-daisy3-skippable name="isolate">
	  <p:input port="sentence-ids">
	    <p:pipe port="sentence-ids" step="break"/>
	  </p:input>
	</px:isolate-daisy3-skippable>
	<p:xslt name="audio-map">
	  <p:input port="parameters">
	    <p:empty/>
	  </p:input>
	  <p:input port="stylesheet">
	    <p:document href="generate-audio-map.xsl"/>
	  </p:input>
	</p:xslt>
      </p:otherwise>
    </p:choose>

  </p:declare-step>

</p:library>
