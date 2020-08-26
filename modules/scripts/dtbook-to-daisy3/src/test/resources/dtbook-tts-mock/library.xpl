<p:library version="1.0" xmlns:p="http://www.w3.org/ns/xproc">
  <p:declare-step version="1.0" name="main" type="px:tts-for-dtbook"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                exclude-inline-prefixes="#all">

    <p:input port="source.fileset" primary="true"/>
    <p:input port="source.in-memory" sequence="true"/>
    <p:input port="config"/>

    <p:output port="audio-map">
      <p:pipe port="clips" step="build-audio-map"/>
    </p:output>

    <p:output port="result.fileset" primary="true"/>
    <p:output port="result.in-memory" sequence="true">
      <p:pipe step="update-fileset" port="result.in-memory"/>
    </p:output>

    <p:output port="sentence-ids" sequence="true">
      <p:empty/> <!-- not used anywhere so far -->
    </p:output>

    <p:output port="status" sequence="false">
      <p:inline>
        <d:validation-status result="ok"/>
      </p:inline>
    </p:output>
	
    <p:output port="log" sequence="true">
      <p:empty/>
    </p:output>

    <p:option name="audio" required="false" px:type="boolean" select="'true'"/>
    <p:option name="process-css" required="false" px:type="boolean" select="'true'">
      <!-- ignored -->
    </p:option>

    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/dtbook-break-detection/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/daisy3-utils/library.xpl"/>

    <px:fileset-load media-types="application/x-dtbook+xml" name="dtbook">
      <p:input port="in-memory">
	<p:pipe step="main" port="source.in-memory"/>
      </p:input>
    </px:fileset-load>

    <p:choose name="build-audio-map">
      <p:when test="$audio = 'false'">
	<p:output port="clips" primary="true">
	  <p:inline>
	    <d:audio-clips/>
	  </p:inline>
	</p:output>
	<p:output port="content">
	  <p:pipe step="dtbook" port="result"/>
	</p:output>
	<p:sink/>
      </p:when>
      <p:otherwise>
	<p:output port="clips" primary="true"/>
	<p:output port="content">
	  <p:pipe port="result" step="isolate"/>
	</p:output>
	<px:copy-resource name="copy-mp3">
	  <p:with-option name="href" select="resolve-uri('30sec.mp3')">
	    <p:inline>
	      <irrelevant/>
	    </p:inline>
	  </p:with-option>
	  <p:with-option name="target" select="'file:${java.io.tmpdir}/30sec.mp3'"/>
	</px:copy-resource>
	<p:sink/>
	<!-- It is necessary to apply NLP and daisy3-utils to split the content around the
	     skippable elements (pagenums and noterefs) so they can be attached to a
	     smilref attribute that won't be the descendant of any audio clip. Otherwise
	     we risk having pagenums without @smilref, which is not allowed by the
	     specs. -->
	<px:dtbook-break-detect name="break">
	  <p:input port="source">
	    <!-- we are assuming that there is only the DTBook -->
	    <p:pipe step="dtbook" port="result"/>
	  </p:input>
	</px:dtbook-break-detect>
	<px:daisy3-isolate-skippable name="isolate">
	  <p:input port="sentence-ids">
	    <p:pipe port="sentence-ids" step="break"/>
	  </p:input>
	</px:daisy3-isolate-skippable>
	<p:xslt name="audio-map">
	  <p:with-param port="parameters" name="mp3-path" select="string(.)">
	    <p:pipe step="copy-mp3" port="result"/>
	  </p:with-param>
	  <p:input port="stylesheet">
	    <p:document href="generate-audio-map.xsl"/>
	  </p:input>
	</p:xslt>
      </p:otherwise>
    </p:choose>
    <p:sink/>

    <px:fileset-update name="update-fileset">
      <p:input port="source.fileset">
	<p:pipe step="main" port="source.fileset"/>
      </p:input>
      <p:input port="source.in-memory">
	<p:pipe step="main" port="source.in-memory"/>
      </p:input>
      <p:input port="update.fileset">
	<p:pipe step="dtbook" port="result.fileset"/>
      </p:input>
      <p:input port="update.in-memory">
	<p:pipe step="build-audio-map" port="content"/>
      </p:input>
    </px:fileset-update>

  </p:declare-step>

</p:library>
