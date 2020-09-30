<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:daisy3-create-ncx" name="main">

    <p:input port="content" primary="true">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
	<p>DTBook document with the smilref attributes.</p>
      </p:documentation>
    </p:input>

    <p:input port="page-list" sequence="true">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p><code>d:fileset</code> document from which the <code>pageList</code> element is to be
        constructed.</p>
        <p>If not provided, the <code>pageList</code> element will reference all the
        <code>pagenum</code> elements in the DTBook.</p>
      </p:documentation>
      <p:empty/>
    </p:input>

    <p:input port="audio-map">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p><code>d:audio-clips</code> document with the locations of the audio files.</p>
      </p:documentation>
    </p:input>

    <p:option name="ncx-dir">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Directory URI which the URI of the output NCX file will be based on.</p>
      </p:documentation>
    </p:option>

    <p:option name="uid">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
	<p>UID of the DTBook (in the meta elements)</p>
      </p:documentation>
    </p:option>

    <p:option name="fail-if-missing-smilref" select="'false'">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>If this option is set, <code>h1</code>, <code>h2</code>, <code>h3</code>,
        <code>h4</code>, <code>h5</code>, <code>h6</code>, <code>levelhd</code>, <code>hd</code> and
        <code>pagenum</code> elements with a missing <code>smilref</code> attribute will result in
        an error.</p>
      </p:documentation>
    </p:option>

    <p:output port="result" primary="true">
      <p:pipe step="ncx" port="result"/>
    </p:output>
    <p:output port="result.fileset">
      <p:pipe step="fileset" port="result"/>
    </p:output>

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

    <p:xslt>
      <p:input port="source">
	<p:pipe step="main" port="content"/>
	<p:pipe step="main" port="audio-map"/>
	<p:pipe step="main" port="page-list"/>
      </p:input>
      <p:input port="stylesheet">
	<p:document href="create-ncx.xsl"/>
      </p:input>
      <p:with-param name="ncx-dir" select="$ncx-dir"/>
      <p:with-param name="uid" select="$uid"/>
      <p:with-param name="fail-if-missing-smilref" select="$fail-if-missing-smilref"/>
    </p:xslt>

    <px:set-base-uri>
      <p:with-option name="base-uri" select="concat($ncx-dir, 'navigation.ncx')"/>
    </px:set-base-uri>
    <p:identity name="ncx"/>
    <p:sink/>

    <px:fileset-create>
      <p:with-option name="base" select="$ncx-dir"/>
    </px:fileset-create>
    <px:fileset-add-entry media-type="application/x-dtbncx+xml" name="fileset">
      <p:input port="entry">
        <p:pipe step="ncx" port="result"/>
      </p:input>
      <p:with-param port="file-attributes" name="indent" select="'true'"/>
      <p:with-param port="file-attributes" name="doctype-public" select="'-//NISO//DTD ncx 2005-1//EN'"/>
      <p:with-param port="file-attributes" name="doctype-system" select="'http://www.daisy.org/z3986/2005/ncx-2005-1.dtd'"/>
    </px:fileset-add-entry>
    <p:sink/>

</p:declare-step>
