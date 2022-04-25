<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:daisy3-create-res-file" name="main"
                exclude-inline-prefixes="#all">

  <!-- TODO: use different words depending on the document
	   language to localize the file, and generate the
	   corresponding audio with the available TTS. To do so, it
	   could return a list of ssml:s with custom ids and call
	   ssml-to-audio. -->

  <p:input port="source">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
	  <p>Contents of the DAISY 3 resource file. If not specified a default document is used.</p>
    </p:documentation>
	<p:document href="resources.res"/>
  </p:input>

  <p:output port="result.fileset" primary="true">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
	  <p>Fileset with as single file the provided or default resource file.</p>
    </p:documentation>

	<p:pipe step="fileset" port="result.fileset"/>
  </p:output>
  <p:output port="result.in-memory" sequence="true">
	<p:pipe step="res-file" port="result"/>
  </p:output>

  <p:option name="output-base-uri">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
	  <p>Base URI of the DAISY 3 resource file.</p>
    </p:documentation>
  </p:option>

  <!--<p:option name="lang">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>Main language of the DTBook document which the resource file refers to.</p>
      <p>Used for localization of the resource file.</p>
    </p:documentation>
  </p:option>-->

  <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
    <p:documentation>
      px:set-base-uri
    </p:documentation>
  </p:import>
  <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
    <p:documentation>
      px:fileset-create
      px:fileset-add-entry
    </p:documentation>
  </p:import>

  <px:set-base-uri>
    <p:input port="source">
      <p:pipe step="main" port="source"/>
    </p:input>
    <p:with-option name="base-uri" select="$output-base-uri"/>
  </px:set-base-uri>
  <p:identity name="res-file"/>
  <p:sink/>

  <px:fileset-create>
    <p:with-option name="base" select="resolve-uri('./',$output-base-uri)"/>
  </px:fileset-create>
  <px:fileset-add-entry media-type="application/x-dtbresource+xml" name="fileset">
    <p:input port="entry">
      <p:pipe step="res-file" port="result"/>
    </p:input>
    <p:with-param port="file-attributes" name="doctype-public" select="'-//NISO//DTD resource 2005-1//EN'"/>
    <p:with-param port="file-attributes" name="doctype-system" select="'http://www.daisy.org/z3986/2005/resource-2005-1.dtd'"/>
  </px:fileset-add-entry>

</p:declare-step>
