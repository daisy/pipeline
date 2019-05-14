<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step name="main" type="px:create-daisy3-opf" xmlns:p="http://www.w3.org/ns/xproc"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    version="1.0">

    <p:input port="source" primary="true">
      <p:documentation>The fileset.</p:documentation>
    </p:input>

    <p:output port="result" primary="true">
      <p:documentation>The OPF file.</p:documentation>
    </p:output>

    <p:option name="output-dir">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
	<p>Root directory URI common to all the files to package (NCX, smil etc.)</p>
      </p:documentation>
    </p:option>

    <p:option name="title">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
	<p>Title of the DTBook document.</p>
      </p:documentation>
    </p:option>

    <p:option name="uid">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
	<p>UID of the DTBook (in the meta elements)</p>
      </p:documentation>
    </p:option>

    <p:option name="total-time">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
	<p>Total duration as returned by px:create-smil-files</p>
      </p:documentation>
    </p:option>

    <p:option name="opf-uri">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
	<p>Output directory URI if the OPF file were to be stored or refered by a fileset.</p>
      </p:documentation>
    </p:option>

    <p:option name="lang">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
	<p>Main language of the DTBook file(s).</p>
      </p:documentation>
    </p:option>

    <p:option name="audio-only" required="false" select="'false'">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>No reference to DTBook in SMIL files</p>
      </p:documentation>
    </p:option>

    <p:option name="publisher"/>

    <p:option name="mathml-xslt-fallback" select="''">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Fallback stylesheet for DAISY players (see DAISY specifications). Empty if no MathML</p>
      </p:documentation>
    </p:option>

    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
        <p:documentation>
            px:set-base-uri
        </p:documentation>
    </p:import>

    <p:xslt>
      <p:input port="stylesheet">
	<p:document href="create-opf.xsl"/>
      </p:input>
      <p:with-param name="lang" select="$lang"/>
      <p:with-param name="publisher" select="$publisher"/>
      <p:with-param name="output-dir" select="$output-dir"/>
      <p:with-param name="uid" select="$uid"/>
      <p:with-param name="title" select="$title"/>
      <p:with-param name="total-time" select="$total-time"/>
      <p:with-param name="audio-only" select="$audio-only"/>
      <p:with-param name="mathml-xslt-fallback" select="$mathml-xslt-fallback"/>
    </p:xslt>

    <px:set-base-uri>
      <p:with-option name="base-uri" select="$opf-uri"/>
    </px:set-base-uri>
    <p:add-xml-base/>

</p:declare-step>
