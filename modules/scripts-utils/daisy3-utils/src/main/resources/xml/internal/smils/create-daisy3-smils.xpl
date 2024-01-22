<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
                xmlns:smil="http://www.w3.org/2001/SMIL20/"
                xmlns:mathml="http://www.w3.org/1998/Math/MathML"
                type="px:daisy3-create-smils" name="main">

    <p:input port="source.fileset" primary="true"/>
    <p:input port="source.in-memory" sequence="false">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>DAISY 3 fileset with the DTBook</p>
        <p>May contain other files but exactly one document must be loaded into memory: the
        DTBook.</p>
      </p:documentation>
    </p:input>

    <p:input port="audio-map">
       <p:documentation xmlns="http://www.w3.org/1999/xhtml">
         <p><code>d:audio-clips</code> document with the locations of the audio files.</p>
         <p>The clipBegin and clipEnd attributes must be compliant with the XML time data type and
         not be greater than 24 hours. See pipeline-mod-tts's documentation for more details.</p>
       </p:documentation>
    </p:input>

    <p:output port="result.fileset">
      <p:pipe step="daisy3-fileset" port="result"/>
    </p:output>
    <p:output port="result.in-memory" sequence="true">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Copy of the input fileset with the SMIL files added, and the modified DTBook with smilref
        attributes added, and possibly an updated DOCTYPE.</p>
        <!-- This step does not ensure that *all* heading and pagenum elements get a smilref. For
             instance, if a pagenum element is contained in another element with an associated audio
             fragment, the pagenum element does not get a smilref attribute. -->
      </p:documentation>
      <p:pipe step="copy-smilrefs" port="result"/>
      <p:pipe step="smil-maybe-without-text" port="result"/>
    </p:output>

    <p:output port="dtbook.fileset">
      <p:pipe step="dtbook-fileset" port="result"/>
    </p:output>
    <p:output port="dtbook.in-memory" sequence="false">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Fileset with only the modified DTBook (loaded into memory).</p>
      </p:documentation>
      <p:pipe step="copy-smilrefs" port="result"/>
    </p:output>

    <p:output port="smil.fileset" primary="true">
      <p:pipe step="smil-fileset" port="result"/>
    </p:output>
    <p:output port="smil.in-memory" sequence="true">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Fileset with only the SMIL files.</p>
      </p:documentation>
      <p:pipe step="smil-maybe-without-text" port="result"/>
    </p:output>

    <p:option name="smil-dir">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
	<p>Directory URI which the URI of the output SMIL files will be based on.</p>
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

    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
      <p:documentation>
        px:message
      </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
      <p:documentation>
        px:set-base-uri
      </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
      <p:documentation>
        px:fileset-create
        px:fileset-add-entries
        px:fileset-join
      </p:documentation>
    </p:import>
    <p:import href="add-elapsed-time.xpl">
      <p:documentation>
        px:daisy3-smil-add-elapsed-time
      </p:documentation>
    </p:import>
    <cx:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl" type="application/xslt+xml">
      <p:documentation>
        pf:normalize-uri
        pf:relativize-uri
      </p:documentation>
    </cx:import>

    <!-- They cannot hold a smilref attribute or they can contain
         levels (which would make them wrongly dispatched over
         multiple smils) -->
    <p:variable name="no-smilref"
		select="' level level1 level2 level3 level4 level5 level6 dtbook frontmatter bodymatter rearmatter br head title meta style book bdo hr w '"/>

    <p:identity>
      <p:input port="source">
        <p:pipe step="main" port="source.in-memory"/>
      </p:input>
    </p:identity>
    <p:delete match="@smilref"/>

    <p:xslt name="add-ids" px:progress="1/6">
      <p:input port="stylesheet">
	<p:document href="add-ids.xsl"/>
      </p:input>
      <p:with-param name="no-smilref" select="$no-smilref"/>
      <p:with-param name="stop-recursion" select="' math '"/>
    </p:xslt>
    <px:message severity="DEBUG" message="Smil-needed IDs generated"/>

    <p:xslt name="audio-order" px:progress="1/6">
      <p:input port="stylesheet">
    	<p:document href="audio-order.xsl"/>
      </p:input>
      <p:input port="parameters">
    	<p:empty/>
      </p:input>
    </p:xslt>
    <px:message severity="DEBUG" message="SMIL audio order generated"/>
    <p:sink/>

    <p:group name="audio-map">
      <p:documentation>Relativize and normalize src and textref attributes in d:audio-clips document.</p:documentation>
      <p:output port="result"/>
      <p:variable name="dtbook-base-uri" select="pf:normalize-uri(base-uri(/*))">
	<p:pipe step="main" port="source.in-memory"/>
      </p:variable>
      <p:label-elements match="d:clip" attribute="src" px:message="dtbook-base-uri: {$dtbook-base-uri}">
	<p:input port="source">
	  <p:pipe step="main" port="audio-map"/>
	</p:input>
	  <p:with-option name="label"
			 select="concat('
				   pf:relativize-uri(pf:normalize-uri(resolve-uri(@src,base-uri(.))),
				   &quot;',$dtbook-base-uri,'&quot;
				   )
				 ')"/>
      </p:label-elements>
      <p:label-elements match="d:clip" attribute="textref">
	<p:input port="source">
	  <p:pipe step="main" port="audio-map"/>
	</p:input>
	  <p:with-option name="label"
			 select="concat('
				   pf:relativize-uri(pf:normalize-uri(resolve-uri(@textref,base-uri(.))),
				   &quot;',$dtbook-base-uri,'&quot;
				   )
				 ')"/>
      </p:label-elements>
      <px:set-base-uri>
	<p:with-option name="base-uri" select="$dtbook-base-uri"/>
      </px:set-base-uri>
    </p:group>
    <p:sink/>

    <p:load name="add-smilrefs-xsl">
      <p:with-option name="href"
      		     select="if ($audio-only='true') then 'add-smilrefs-audio-only.xsl' else 'add-smilrefs.xsl'"/>
    </p:load>

    <p:xslt name="add-smilrefs" px:progress="1/6">
      <p:input port="source">
	<p:pipe port="result" step="audio-order"/>
	<p:pipe step="audio-map" port="result"/>
      </p:input>
      <p:input port="stylesheet">
	<p:pipe port="result" step="add-smilrefs-xsl"/>
      </p:input>
      <p:with-param name="no-smilref" select="$no-smilref"/>
      <p:with-param name="mo-dir" select="$smil-dir"/>
    </p:xslt>
    <px:message severity="DEBUG" message="Smilref generated"/>
    <p:sink/>

    <p:xslt name="copy-smilrefs" px:progress="1/6">
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
    <px:message severity="DEBUG" message="Smilrefs copied to the original document"/>
    <p:sink/>

    <p:xslt name="create-smils" px:progress="1/6">
      <p:input port="source">
	<p:pipe port="result" step="add-smilrefs"/>
	<p:pipe step="audio-map" port="result"/>
      </p:input>
      <p:input port="stylesheet">
	<p:document href="create-smils.xsl"/>
      </p:input>
      <p:with-param name="uid" select="$uid"/>
      <p:with-param name="mo-dir" select="$smil-dir"/>
      <p:with-param name="audio-only" select="$audio-only"/>
    </p:xslt>
    <px:message severity="DEBUG" message="SMIL files generated."/><p:sink/>
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

    <px:daisy3-smil-add-elapsed-time px:progress="1/6"/>
    <px:message severity="DEBUG" message="Durations computed."/>

    <p:choose>
      <p:xpath-context>
	<p:empty/>
      </p:xpath-context>
      <p:when test="$audio-only='true'">
        <p:for-each>
          <p:delete match="smil:text"/>
        </p:for-each>
      </p:when>
      <p:otherwise>
        <p:identity/>
      </p:otherwise>
    </p:choose>
    <p:identity name="smil-maybe-without-text"/>

    <p:sink/>
    <px:fileset-create>
      <p:with-option name="base" select="$smil-dir"/>
    </px:fileset-create>
    <px:fileset-add-entries media-type="application/smil">
      <p:input port="entries">
	<p:pipe step="smil-maybe-without-text" port="result"/>
      </p:input>
      <p:with-param port="file-attributes" name="indent" select="'true'"/>
      <p:with-param port="file-attributes" name="doctype-public" select="'-//NISO//DTD dtbsmil 2005-2//EN'"/>
      <p:with-param port="file-attributes" name="doctype-system" select="'http://www.daisy.org/z3986/2005/dtbsmil-2005-2.dtd'"/>
    </px:fileset-add-entries>
    <px:fileset-join px:message="SMIL fileset created." px:message-severity="DEBUG"
                     name="smil-fileset"/>
    <p:sink/>

    <!--
        update DTBook DOCTYPE if needed
    -->
    <p:identity>
      <p:input port="source">
        <p:pipe step="main" port="source.fileset"/>
      </p:input>
    </p:identity>
    <p:viewport match="d:file[@media-type='application/x-dtbook+xml']">
      <p:variable name="math-prefix" select="substring-before((//mathml:math)[1]/name(), ':')">
        <!-- Hopefully, the MathML namespace prefixes are all the same. -->
        <p:pipe step="copy-smilrefs" port="result"/>
      </p:variable>
      <p:choose>
        <p:when test="$math-prefix">
          <p:variable name="smilref-prefix" select="substring-before(name((//mathml:*[@dtb:smilref])[1]/@dtb:smilref), ':')">
            <!-- Hopefully, the DTBook namespace prefixes are all the same for MathML elements. -->
            <p:pipe step="copy-smilrefs" port="result"/>
          </p:variable>
          <p:variable name="dtbook-prefix" select="if ($smilref-prefix) then $smilref-prefix else 'dtbook'"/>
          <!-- FIXME: use MathML2 DTD instead of MathML3 DTD when MathML2 detected -->
          <p:variable name="math-extension" select="concat(' [
						    &lt;!ENTITY % MATHML.prefixed &quot;INCLUDE&quot;&gt;
						    &lt;!ENTITY % MATHML.prefix &quot;', $math-prefix, '&quot;&gt;
						    &lt;!ENTITY % MATHML.Common.attrib
						    &quot;xlink:href    CDATA       #IMPLIED
						    xlink:type     CDATA       #IMPLIED
						    class          CDATA       #IMPLIED
						    style          CDATA       #IMPLIED
						    id             ID          #IMPLIED
						    xref           IDREF       #IMPLIED
						    other          CDATA       #IMPLIED
						    xmlns:', $dtbook-prefix, '   CDATA       #FIXED ''http://www.daisy.org/z3986/2005/dtbook/''
						    ',$dtbook-prefix,':smilref CDATA       #IMPLIED&quot;&gt;
						    &lt;!ENTITY % mathML3 PUBLIC &quot;-//W3C//DTD MathML 3.0//EN&quot;
						    &quot;http://www.w3.org/Math/DTD/mathml3/mathml3.dtd&quot;&gt;
						    %mathML3;
						    &lt;!ENTITY % externalFlow &quot;| ', $math-prefix, ':math&quot;&gt;
						    &lt;!ENTITY % externalNamespaces &quot;xmlns:', $math-prefix, ' CDATA #FIXED
						    ''http://www.w3.org/1998/Math/MathML''&quot;&gt;]')"/>
          <!-- assuming doctype-public and doctype-system attributes are present -->
          <p:add-attribute match="d:file" attribute-name="doctype">
            <p:with-option name="attribute-value" select="concat('&lt;!DOCTYPE dtbook PUBLIC &quot;',
				   //d:file/@doctype-public, '&quot; &quot;', //d:file/@doctype-system,
				   '&quot;', $math-extension, '&gt;')"/>
          </p:add-attribute>
          <p:delete match="@doctype-public|@doctype-system"/>
        </p:when>
        <p:otherwise>
          <p:identity/>
        </p:otherwise>
      </p:choose>
    </p:viewport>
    <p:identity name="dtbook-fileset"/>
    <p:sink/>

    <px:fileset-join name="daisy3-fileset">
      <p:input port="source">
        <p:pipe step="dtbook-fileset" port="result"/>
        <p:pipe step="smil-fileset" port="result"/>
      </p:input>
    </px:fileset-join>
    <p:sink/>

</p:declare-step>
