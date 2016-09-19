<p:declare-step type="pxi:apply-lexicons" version="1.0" name="main"
		xmlns:p="http://www.w3.org/ns/xproc"
		xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
		xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
		xmlns:pls="http://www.w3.org/2005/01/pronunciation-lexicon"
		exclude-inline-prefixes="#all">

  <p:input port="fileset" />
  <p:input port="user-lexicons"/>
  <p:input port="source" primary="true"/>
  <p:output port="result" primary="true"/>

  <p:option name="lang" required="true"/>

  <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>

  <!-- iterate over the fileset to extract the lexicons URI, then load them -->
  <!-- from the disk -->
  <p:for-each name="doc-lexicons">
    <p:iteration-source select="//*[@media-type = 'application/pls+xml']">
      <p:pipe port="fileset" step="main"/>
    </p:iteration-source>
    <p:output port="result" sequence="true"/>
    <p:variable name="lexicon-loc" select="resolve-uri(/*/@href, base-uri(.))"/>
    <p:load>
      <p:with-option name="href" select="$lexicon-loc"/>
    </p:load>
    <px:message>
      <p:with-option name="message" select="concat('load lexicon ', $lexicon-loc)"/>
    </px:message>
  </p:for-each>

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
	<px:message>
	  <p:with-option name="message" select="concat('loaded lexicon for language: ', $lang)"/>
	</px:message>
      </p:group>
      <p:catch>
	<p:identity>
	  <p:input port="source">
	    <p:empty/>
	  </p:input>
	</p:identity>
	<px:message>
	  <p:with-option name="message" select="concat('could not find the builtin lexicon for language: ', $lang)"/>
	</px:message>
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
      <p:pipe port="user-lexicons" step="main"/>
      <p:pipe port="result" step="doc-lexicons"/>
      <p:pipe port="result" step="builtin-lexicons"/>
      <p:pipe port="result" step="empty-lexicon"/>
    </p:input>
    <p:input port="stylesheet">
      <p:document href="../xslt/reorganize-lexicons.xsl"/>
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
      <p:document href="../xslt/pls-to-ssml.xsl"/>
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
      <p:document href="../xslt/regex-pls-to-ssml.xsl"/>
    </p:input>
    <p:input port="parameters">
      <p:empty/>
    </p:input>
  </p:xslt>

</p:declare-step>
