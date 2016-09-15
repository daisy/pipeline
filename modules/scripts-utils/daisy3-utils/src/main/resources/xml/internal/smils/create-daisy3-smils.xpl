<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step name="main" type="px:create-daisy3-smils" xmlns:p="http://www.w3.org/ns/xproc"
    xmlns:d="http://www.daisy.org/ns/pipeline/data"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    xmlns:smil="http://www.w3.org/2001/SMIL20/"
    version="1.0">

    <p:input port="content" primary="true" sequence="false">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
	 <p>DTBook file. Its URI does no matter.</p>
      </p:documentation>
    </p:input>

    <p:input port="audio-map">
       <p:documentation xmlns="http://www.w3.org/1999/xhtml">
	 <p>The clipBegin and clipEnd attributes must be compliant
	 with the XML time data type and not be greater than 24
	 hours. See pipeline-mod-tts's documentation for more
	 details.</p>
       </p:documentation>
    </p:input>

    <p:output port="fileset.out">
      <p:pipe port="result" step="smil-in-fileset"/>
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
	 <p>Fileset entries for the SMIL files.</p>
      </p:documentation>
    </p:output>

    <p:output port="smil.out" sequence="true">
      <p:pipe port="result" step="smil-with-durations"/>
    </p:output>

    <p:output port="updated-content" primary="true">
      <p:pipe port="result" step="copy-smilrefs"/>
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
	 <p>Content document with smilref attributes.</p>
      </p:documentation>
    </p:output>

    <p:output port="duration">
      <p:pipe port="result" step="total-duration"/>
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
	<p>Total duration.</p>
      </p:documentation>
    </p:output>

    <p:option name="root-dir">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
	<p>Root directory of the DAISY 3 files.</p>
      </p:documentation>
    </p:option>

    <p:option name="audio-dir">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
	<p>Parent directory URI of the audio files.</p>
      </p:documentation>
    </p:option>

    <p:option name="smil-dir">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
	<p>Directory URI which the URI of the output SMIL files will be based on.</p>
      </p:documentation>
    </p:option>

    <p:option name="daisy3-dtbook-uri">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
	<p>Expected URI of the final DTBook document.</p>
      </p:documentation>
    </p:option>

    <p:option name="uid">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
	<p>UID of the DTBook (in the meta elements)</p>
      </p:documentation>
    </p:option>

    <p:option name="audio-only" required="false" select="'false'">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
	<p>No reference to DTBook in SMIL files</p>
      </p:documentation>
    </p:option>

    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>

    <!-- They cannot hold a smilref attribute or they can contain
         levels (which would make them wrongly dispatched over
         multiple smils) -->
    <p:variable name="no-smilref"
		select="' level level1 level2 level3 level4 level5 level6 dtbook frontmatter bodymatter rearmatter br head title meta style book bdo hr w '"/>

    <p:delete match="@smilref"/>

    <p:xslt name="add-ids">
      <p:input port="stylesheet">
	<p:document href="add-ids.xsl"/>
      </p:input>
      <p:with-param name="no-smilref" select="$no-smilref"/>
      <p:with-param name="stop-recursion" select="' math '"/>
    </p:xslt>
    <px:message message="Smil-needed IDs generated"/>

    <p:xslt name="audio-order">
      <p:input port="stylesheet">
    	<p:document href="audio-order.xsl"/>
      </p:input>
      <p:input port="parameters">
    	<p:empty/>
      </p:input>
    </p:xslt>
    <px:message message="SMIL audio order generated"/>
    <p:sink/>

    <p:load name="add-smilrefs-xsl">
      <p:with-option name="href"
      		     select="if ($audio-only='true') then 'add-smilrefs-audio-only.xsl' else 'add-smilrefs.xsl'"/>
    </p:load>

    <p:xslt name="add-smilrefs">
      <p:input port="source">
	<p:pipe port="result" step="audio-order"/>
	<p:pipe port="audio-map" step="main"/>
      </p:input>
      <p:input port="stylesheet">
	<p:pipe port="result" step="add-smilrefs-xsl"/>
      </p:input>
      <p:with-param name="no-smilref" select="$no-smilref"/>
      <p:with-param name="mo-dir" select="$smil-dir"/>
      <p:with-param name="output-dir" select="$root-dir"/>
    </p:xslt>
    <px:message message="Smilref generated"/>
    <p:sink/>

    <p:xslt name="copy-smilrefs">
      <p:input port="source">
	<p:pipe port="result" step="add-ids"/>
	<p:pipe port="result" step="add-smilrefs"/>
      </p:input>
      <p:input port="stylesheet">
	<p:document href="copy-smilrefs.xsl"/>
      </p:input>
      <p:input port="parameters">
	<p:empty/>
      </p:input>
    </p:xslt>
    <px:message message="Smilrefs copied to the original document"/>
    <p:sink/>

    <p:xslt name="create-smils">
      <p:input port="source">
	<p:pipe port="result" step="add-smilrefs"/>
	<p:pipe port="audio-map" step="main"/>
      </p:input>
      <p:input port="stylesheet">
	<p:document href="create-smils.xsl"/>
      </p:input>
      <p:with-param name="uid" select="$uid"/>
      <p:with-param name="mo-dir" select="$smil-dir"/>
      <p:with-param name="audio-dir" select="$audio-dir"/>
      <p:with-param name="content-uri" select="$daisy3-dtbook-uri"/>
      <p:with-param name="content-dir" select="$root-dir"/>
      <p:with-param name="audio-only" select="$audio-only"/>
    </p:xslt>
    <px:message message="SMIL files generated."/><p:sink/>
    <p:for-each name="all-smils">
      <p:iteration-source>
	<p:pipe port="secondary" step="create-smils"/>
      </p:iteration-source>
      <p:output port="result"/>
      <p:xslt>
	<p:input port="stylesheet">
	  <p:document href="fill-end-attrs.xsl"/>
	</p:input>
	<p:input port="parameters">
	  <p:empty/>
	</p:input>
      </p:xslt>
    </p:for-each>

    <p:xslt name="compute-durations">
      <p:input port="source">
	<p:pipe port="result" step="all-smils"/>
      </p:input>
      <p:input port="stylesheet">
	<p:document href="compute-durations.xsl"/>
      </p:input>
      <p:input port="parameters">
	<p:empty/>
      </p:input>
    </p:xslt>
    <p:delete match="d:duration" name="total-duration"/>
    <px:message message="Durations computed."/><p:sink/>

    <p:for-each name="smil-with-durations">
      <p:output port="result"/>
      <p:iteration-source>
	<p:pipe port="result" step="all-smils"/>
      </p:iteration-source>
      <p:variable name="doc-uri" select="base-uri(/*)"/>
      <p:viewport match="smil:head/smil:meta[@name='dtb:totalElapsedTime']">
	<p:add-attribute attribute-name="content" match="/*">
	  <p:with-option name="attribute-value" select="//*[@doc=$doc-uri]/@duration">
	    <p:pipe port="result" step="compute-durations"/>
	  </p:with-option>
	</p:add-attribute>
      </p:viewport>
      <p:choose>
	<p:when test="$audio-only = 'true'">
	  <p:delete match="smil:text"/>
	</p:when>
	<p:otherwise>
	  <p:identity/>
	</p:otherwise>
      </p:choose>
    </p:for-each>

    <p:for-each>
      <p:iteration-source>
	<p:pipe port="result" step="all-smils"/>
      </p:iteration-source>
      <p:output port="result" sequence="true"/>
      <p:variable name="mo-uri" select="base-uri(/*)"/>
      <px:fileset-create>
	<p:with-option name="base" select="$smil-dir"/>
      </px:fileset-create>
      <px:fileset-add-entry media-type="application/smil">
	<p:with-option name="href" select="$mo-uri"/>
      </px:fileset-add-entry>
    </p:for-each>
    <px:fileset-join name="smil-in-fileset"/>

    <px:message message="SMIL fileset created."/><p:sink/>

</p:declare-step>
