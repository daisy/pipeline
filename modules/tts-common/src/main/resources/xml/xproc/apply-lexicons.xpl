<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
		xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
		xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
		xmlns:pls="http://www.w3.org/2005/01/pronunciation-lexicon"
		xmlns:d="http://www.daisy.org/ns/pipeline/data"
		xmlns:cx="http://xmlcalabash.com/ns/extensions"
		xmlns:xs="http://www.w3.org/2001/XMLSchema"
		type="pxi:apply-lexicons" name="main"
		exclude-inline-prefixes="#all">

  <p:input port="source" primary="true"/>
  <p:option name="user-lexicons" cx:as="xs:anyURI*" select="()">
    <p:documentation>
      All URIs in this sequence will be loaded and applied. The URIs can be absolute or relative to
      the source. The files must exist on disk.
    </p:documentation>
  </p:option>
  <p:input port="doc-lexicons.fileset">
    <p:documentation>
      All files in this fileset with media-type "application/pls+xml" will be loaded and
      applied. These are the lexicons associated with the source document (detected by
      px:dtbook-load and px:epub-load).
    </p:documentation>
    <p:inline><d:fileset/></p:inline>
  </p:input>
  <p:input port="doc-lexicons.in-memory" sequence="true">
    <p:empty/>
  </p:input>
  
  <p:output port="result" primary="true"/>

  <p:option name="lang" required="true"/>

  <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
    <p:documentation>
      px:fileset-create
      px:fileset-add-entries
      px:fileset-load
    </p:documentation>
  </p:import>

  <p:sink/>
  <p:group name="user-lexicons">
    <p:output port="result" sequence="true"/>
    <px:fileset-create>
      <p:with-option name="base" select="base-uri(/*)">
        <p:pipe step="main" port="source"/>
      </p:with-option>
    </px:fileset-create>
    <px:fileset-add-entries media-type="application/pls+xml">
      <p:with-option name="href" select="$user-lexicons"/>
    </px:fileset-add-entries>
    <px:fileset-load/>
    <p:for-each>
      <p:variable name="base" select="base-uri(/*)"/>
      <p:identity px:message="load lexicon {$base}"/>
    </p:for-each>
  </p:group>
  <p:sink/>

  <p:group name="doc-lexicons">
    <p:output port="result" sequence="true"/>
    <px:fileset-load media-types="application/pls+xml">
      <p:input port="fileset">
	<p:pipe step="main" port="doc-lexicons.fileset"/>
      </p:input>
      <p:input port="in-memory">
	<p:pipe step="main" port="doc-lexicons.in-memory"/>
      </p:input>
    </px:fileset-load>
    <p:for-each>
      <p:variable name="base" select="base-uri(/*)"/>
      <p:identity px:message="load lexicon {$base}"/>
    </p:for-each>
  </p:group>

  <!-- find all the languages actually used -->
  <p:xslt name="list-lang">
    <p:input port="source">
      <p:pipe port="source" step="main"/>
    </p:input>
    <p:input port="parameters">
      <p:empty/>
    </p:input>
    <p:input port="stylesheet">
      <p:inline>
	<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
	  <xsl:output method="xml" encoding="UTF-8" />
	  <xsl:template match="/">
	    <root>
	      <xsl:for-each-group select="//node()[@xml:lang]" group-by="@xml:lang">
		<lang><xsl:attribute name="lang">
		  <xsl:value-of select="@xml:lang"/>
		</xsl:attribute></lang>
	      </xsl:for-each-group>
	    </root>
	  </xsl:template>
	</xsl:stylesheet>
      </p:inline>
    </p:input>
  </p:xslt>

  <!-- read the corresponding lexicons from the disk -->
  <p:for-each name="builtin-lexicons">
    <p:output port="result" sequence="true"/>
    <p:iteration-source select="//*[@lang]">
      <p:pipe port="result" step="list-lang"/>
    </p:iteration-source>
    <p:variable name="lang" select="/*/@lang">
      <p:pipe port="current" step="builtin-lexicons"/>
    </p:variable>
    <p:try>
      <p:group>
	<p:load>
	  <p:with-option name="href" select="concat('../lexicons/lexicon_', $lang,'.pls')"/>
	</p:load>
	<p:identity px:message="loaded lexicon for language: {$lang}" px:message-severity="DEBUG"/>
      </p:group>
      <p:catch>
	<p:identity>
	  <p:input port="source">
	    <p:empty/>
	  </p:input>
	</p:identity>
	<p:identity px:message="could not find the builtin lexicon for language: {$lang}" px:message-severity="DEBUG"/>
      </p:catch>
    </p:try>
  </p:for-each>

  <p:identity name="empty-lexicon">
    <p:input port="source">
      <p:inline>
	<lexicon version="1.0" xmlns="http://www.w3.org/2005/01/pronunciation-lexicon"/>
      </p:inline>
    </p:input>
  </p:identity>

  <p:xslt name="separate-regex-lexicons">
    <p:input port="source">
      <p:pipe step="user-lexicons" port="result"/>
      <p:pipe step="doc-lexicons" port="result"/>
      <p:pipe step="builtin-lexicons" port="result"/>
      <p:pipe step="empty-lexicon" port="result"/>
    </p:input>
    <p:input port="stylesheet">
      <p:document href="reorganize-lexicons.xsl"/>
    </p:input>
    <p:input port="parameters">
      <p:empty/>
    </p:input>
  </p:xslt>

  <p:xslt name="pls">
    <p:input port="source">
      <p:pipe port="source" step="main"/>
      <p:pipe port="result" step="separate-regex-lexicons"/>
    </p:input>
    <p:input port="stylesheet">
      <p:document href="pls-to-ssml.xsl"/>
    </p:input>
    <p:input port="parameters">
      <p:empty/>
    </p:input>
  </p:xslt>

  <p:xslt name="regex-pls">
    <p:input port="source">
      <p:pipe port="result" step="pls"/>
      <p:pipe port="secondary" step="separate-regex-lexicons"/>
    </p:input>
    <p:input port="stylesheet">
      <p:document href="regex-pls-to-ssml.xsl"/>
    </p:input>
    <p:input port="parameters">
      <p:empty/>
    </p:input>
  </p:xslt>

</p:declare-step>
