<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                name="main" type="px:dtbook-to-epub3.script"
                px:input-filesets="dtbook"
                px:output-filesets="epub3"
                exclude-inline-prefixes="#all">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">DTBook to EPUB 3</h1>
        <p px:role="desc">Transforms multiple DTBooks into EPUB 3 format</p>
        <a px:role="homepage" href="http://daisy.github.io/pipeline/Get-Help/User-Guide/Scripts/dtbook-to-epub3/">
            Online documentation
        </a>
    </p:documentation>

    <p:input port="source" primary="true" sequence="true" px:media-type="application/x-dtbook+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">DTBook file(s)</h2>
            <p px:role="desc">One or more DTBook files to be transformed. In the case of multiple files, a merge will be performed.</p>
        </p:documentation>
    </p:input>

    <p:output port="tts-log" sequence="true">
      <!-- defined in ../../../../../common-options.xpl -->
      <p:pipe step="result" port="tts-log"/>
    </p:output>
    <p:serialization port="tts-log" indent="true" omit-xml-declaration="false"/>

    <p:option xmlns:_="tts" name="_:stylesheet" select="''">
      <!-- defined in ../../../../../common-options.xpl -->
    </p:option>

    <p:option name="stylesheet-parameters" select="'()'">
      <!-- defined in ../../../../../common-options.xpl -->
    </p:option>

    <p:option name="language" select="''">
      <!-- defined in ../../../../../common-options.xpl -->
    </p:option>

    <p:option name="result" required="true" px:output="result" px:type="anyDirURI">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">EPUB</h2>
            <p px:role="desc">The resulting EPUB 3 publication.</p>
        </p:documentation>
    </p:option>

    <p:option name="temp-dir" required="true" px:output="temp" px:type="anyDirURI">
        <!-- directory used for temporary files -->
    </p:option>

    <p:option name="validation" select="'abort'">
      <!-- defined in ../../../../../common-options.xpl -->
    </p:option>

    <p:output port="validation-report" sequence="true">
        <!-- defined in ../../../../../common-options.xpl -->
        <p:pipe step="load" port="validation-report"/>
    </p:output>

    <p:output port="status" px:media-type="application/vnd.pipeline.status+xml" primary="true">
      <!-- whether the conversion was aborted due to validation errors or text-to-speech errors -->
      <!-- when the conversion fails because of text-to-speech errors it may still output a
           (incomplete) EPUB 3 publication-->
    </p:output>

    <p:option name="nimas" select="'false'">
      <!-- defined in ../../../../../common-options.xpl -->
    </p:option>

    <p:input port="tts-config">
      <!-- defined in ../../../../../common-options.xpl -->
      <p:inline><d:config/></p:inline>
    </p:input>
    <p:option name="lexicon" select="p:system-property('d:org.daisy.pipeline.tts.default-lexicon')">
      <!-- defined in ../../../../../common-options.xpl -->
    </p:option>

    <p:option xmlns:_="dtbook" name="_:chunk-size" select="'-1'">
      <!-- defined in ../../../../../common-options.xpl -->
    </p:option>

    <p:option name="audio" select="'false'">
      <!-- defined in ../../../../../common-options.xpl -->
    </p:option>
    <p:option name="audio-file-type" select="'audio/mpeg'" px:hidden="true">
        <!-- the desired file type of the generated audio files -->
    </p:option>

    <p:import href="convert.xpl">
        <p:documentation>
            px:dtbook-to-epub3
        </p:documentation>
    </p:import>

    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
      <p:documentation>
        px:fileset-add-entries
      </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/dtbook-utils/library.xpl">
      <p:documentation>
        px:dtbook-load
      </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/epub-utils/library.xpl">
      <p:documentation>
        px:epub3-store
      </p:documentation>
    </p:import>
    <cx:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl" type="application/xslt+xml">
      <p:documentation>
        pf:normalize-uri
      </p:documentation>
    </cx:import>

    <p:sink/>
    <px:fileset-add-entries media-type="application/x-dtbook+xml" name="dtbook">
      <p:input port="entries">
	<p:pipe step="main" port="source"/>
      </p:input>
    </px:fileset-add-entries>
    <px:dtbook-load name="load" px:progress=".1" px:message="Loading DTBook">
      <p:input port="source.in-memory">
	<p:pipe step="dtbook" port="result.in-memory"/>
      </p:input>
      <p:with-option name="validation" select="not($validation='off')"/>
      <p:with-option name="nimas" select="$nimas='true'"/>
      <!-- assume MathML 3.0 -->
    </px:dtbook-load>

    <p:identity>
      <p:input port="source">
	<p:pipe step="load" port="validation-status"/>
      </p:input>
    </p:identity>
    <p:choose>
      <p:when test="/d:validation-status[@result='error']">
	<p:choose>
	  <p:when test="$validation='abort'">
	    <p:identity px:message="The input contains an invalid DTBook file. See validation report for more info."
			px:message-severity="ERROR"/>
	  </p:when>
	  <p:otherwise>
	    <p:identity px:message="The input contains an invalid DTBook file. See validation report for more info."
			px:message-severity="WARN"/>
	  </p:otherwise>
	</p:choose>
      </p:when>
      <p:otherwise>
	<p:identity/>
      </p:otherwise>
    </p:choose>
    <p:choose name="result" px:progress=".9">
      <p:when test="/d:validation-status[@result='error'] and $validation='abort'">
	<p:output port="status" primary="true"/>
        <p:output port="tts-log" sequence="true">
	  <p:empty/>
	</p:output>
	<p:identity/>
      </p:when>
      <p:otherwise>
	<p:output port="status" primary="true"/>
	<p:output port="tts-log" sequence="true">
	  <p:pipe step="convert-and-store" port="tts-log"/>
	</p:output>
	<p:variable name="dtbook-is-valid" cx:as="xs:boolean"
		    select="not($validation='off') and exists(/d:validation-status[@result='ok'])"/>

	<!-- get the EPUB filename from the first DTBook -->
	<p:sink/>
	<p:split-sequence test="position()=1" initial-only="true">
	  <p:input port="source">
	    <p:pipe step="main" port="source"/>
	  </p:input>
	</p:split-sequence>
	<p:group name="convert-and-store" px:progress="1">
	  <p:output port="status" primary="true"/>
	  <p:output port="tts-log" sequence="true">
	    <p:pipe step="convert" port="tts-log"/>
	  </p:output>
	  <p:variable name="dtbook-uri" select="base-uri(/)"/>
	  <p:variable name="output-name" select="replace(replace($dtbook-uri,'^.*/([^/]+)$','$1'),'\.[^\.]*$','')"/>
	  <p:variable name="output-dir-uri" select="pf:normalize-uri(concat($result,'/'))"/>
	  <p:variable name="epub-file-uri" select="concat($output-dir-uri,$output-name,'.epub')"/>
	  <p:sink/>

	  <px:dtbook-to-epub3 name="convert" px:progress="8/9">
	    <p:input port="source.fileset">
	      <p:pipe step="load" port="result.fileset"/>
	    </p:input>
	    <p:input port="source.in-memory">
	      <p:pipe step="load" port="result.in-memory"/>
	    </p:input>
	    <p:input port="tts-config">
	      <p:pipe step="main" port="tts-config"/>
	    </p:input>
	    <p:with-option name="stylesheet" xmlns:_="tts" select="string-join(
	                                                             for $s in tokenize($_:stylesheet,'\s+')[not(.='')] return
	                                                               resolve-uri($s,$dtbook-uri),
	                                                             ' ')"/>
	    <p:with-option name="stylesheet-parameters" select="$stylesheet-parameters"/>
	    <p:with-option name="lexicon" select="for $l in tokenize($lexicon,'\s+')[not(.='')] return
	                                            resolve-uri($l,$dtbook-uri)"/>
	    <p:with-option name="audio" select="$audio"/>
	    <p:with-option name="audio-file-type" select="$audio-file-type"/>
	    <p:with-option name="language" select="$language"/>
	    <p:with-option name="validation" select="$validation"/>
	    <p:with-option name="dtbook-is-valid" select="$dtbook-is-valid"/>
	    <p:with-option name="nimas" select="$nimas='true'"/>
	    <p:with-option name="chunk-size" xmlns:_="dtbook" select="$_:chunk-size"/>
	    <p:with-option name="output-name" select="$output-name"/>
	    <p:with-option name="output-dir" select="concat($temp-dir,'epub3-unzipped/')"/>
	    <p:with-option name="temp-dir" select="concat($temp-dir,'temp/')"/>
	  </px:dtbook-to-epub3>

	  <px:epub3-store name="store" px:progress="1/9" px:message="Storing EPUB 3">
	    <p:input port="in-memory.in">
	      <p:pipe step="convert" port="result.in-memory"/>
	    </p:input>
	    <p:with-option name="href" select="$epub-file-uri"/>
	  </px:epub3-store>

	  <p:identity cx:depends-on="store">
	    <p:input port="source">
	      <p:pipe step="convert" port="status"/>
	    </p:input>
	  </p:identity>
	</p:group>
      </p:otherwise>
    </p:choose>

</p:declare-step>
