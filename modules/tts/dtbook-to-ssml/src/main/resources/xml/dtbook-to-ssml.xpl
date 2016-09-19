<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:dtbook-to-ssml" version="1.0"
		xmlns:p="http://www.w3.org/ns/xproc"
		xmlns:c="http://www.w3.org/ns/xproc-step"
		xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
		xmlns:dt="http://www.daisy.org/z3986/2005/dtbook/"
		exclude-inline-prefixes="#all"
		name="main">

    <p:documentation>
      <p>Specialization of the SSML generation for DTBook</p>
    </p:documentation>

    <p:input port="fileset.in" sequence="false"/>
    <p:input port="content.in" primary="true" sequence="false"/>
    <p:input port="sentence-ids" sequence="false"/>
    <p:input port="skippable-ids"/>
    <p:input port="config"/>

    <p:output port="result" primary="true" sequence="true">
      <p:pipe port="result" step="ssml-gen" />
    </p:output>

    <p:option name="separate-skippable" required="false" select="'true'"/>

    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/text-to-ssml/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/tts-helpers/library.xpl"/>

    <p:variable name="dc-lang" select="//meta[@name='dc:Language']/@content"/>

    <px:get-tts-lexicons name="user-lexicons">
      <p:input port="config">
	<p:pipe port="config" step="main"/>
      </p:input>
    </px:get-tts-lexicons>

    <px:get-tts-annotations content-type="application/x-dtbook+xml" name="get-config-annot">
      <p:input port="config">
	<p:pipe port="config" step="main"/>
      </p:input>
    </px:get-tts-annotations>
    <p:count limit="1"/>
    <p:choose name="get-annotations">
      <p:when test=". &gt; 0">
	<p:output port="result" primary="true" sequence="true"/>
	<p:identity>
	  <p:input port="source">
	    <p:pipe port="result" step="get-config-annot"/>
	  </p:input>
	</p:identity>
      </p:when>
      <p:otherwise>
	<p:output port="result" primary="true" sequence="true"/>
	<p:load href="http://www.daisy.org/pipeline/modules/text-to-ssml/dtbook-annotating.xsl"/>
      </p:otherwise>
    </p:choose>

    <p:xslt name="semantic">
      <p:input port="source">
	<p:pipe port="content.in" step="main"/>
      </p:input>
      <p:input port="stylesheet">
	<p:document href="dtbook-semantic-transform.xsl"/>
      </p:input>
      <p:input port="parameters">
	<p:empty/>
      </p:input>
    </p:xslt>

    <px:text-to-ssml name="ssml-gen">
      <!-- output ssml.out and content.out -->
      <p:input port="fileset.in">
	<p:pipe port="fileset.in" step="main"/>
      </p:input>
      <p:input port="content.in">
	<p:pipe port="result" step="semantic"/>
      </p:input>
      <p:input port="sentence-ids">
	<p:pipe port="sentence-ids" step="main"/>
      </p:input>
      <p:input port="skippable-ids">
	<p:pipe port="skippable-ids" step="main"/>
      </p:input>
      <p:input port="user-lexicons">
	<p:pipe port="result" step="user-lexicons"/>
      </p:input>
      <p:input port="annotations">
	<p:pipe port="result" step="get-annotations"/>
      </p:input>
      <p:with-option name="word-element" select="'w'"/>
      <p:with-option name="lang" select="if ($dc-lang) then $dc-lang else 'en'"/>
    </px:text-to-ssml>

    <px:message message="End SSML generation for DTBook"/><p:sink/>

</p:declare-step>
