<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step name="main" type="px:create-ncx" xmlns:p="http://www.w3.org/ns/xproc"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    version="1.0">

    <p:input port="content" primary="true">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
	<p>DTBook document with the smilref attributes.</p>
      </p:documentation>
    </p:input>

    <p:input port="audio-map">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
	<p>List of audio clips (see ssml-to-audio documentation)</p>
      </p:documentation>
    </p:input>

    <p:option name="audio-dir">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
	<p>Parent directory URI of the audio files.</p>
      </p:documentation>
    </p:option>

    <p:option name="smil-dir">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
	<p>Parent directory URI of the smil files.</p>
      </p:documentation>
    </p:option>

    <p:option name="ncx-dir">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
	<p>Output directory URI if the NCX file were to be stored or refered by a fileset.</p>
      </p:documentation>
    </p:option>

    <p:option name="uid">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
	<p>UID of the DTBook (in the meta elements)</p>
      </p:documentation>
    </p:option>

    <p:output port="result" primary="true">
      <p:pipe port="result" step="create-ncx"/>
    </p:output>

    <p:xslt>
      <p:input port="source">
	<p:pipe port="content" step="main"/>
	<p:pipe port="audio-map" step="main"/>
      </p:input>
      <p:input port="stylesheet">
	<p:document href="create-ncx.xsl"/>
      </p:input>
      <p:with-param name="mo-dir" select="$smil-dir"/>
      <p:with-param name="audio-dir" select="$audio-dir"/>
      <p:with-param name="ncx-dir" select="$ncx-dir"/>
      <p:with-param name="uid" select="$uid"/>
    </p:xslt>

    <p:add-attribute name="create-ncx" match="/*" attribute-name="xml:base">
      <p:with-option name="attribute-value" select="concat($ncx-dir, 'navigation.ncx')"/>
    </p:add-attribute>

</p:declare-step>
