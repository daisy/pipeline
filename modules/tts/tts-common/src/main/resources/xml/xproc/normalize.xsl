<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
		xmlns:ssml="http://www.w3.org/2001/10/synthesis"
		exclude-result-prefixes="#all">

  <xsl:param name="word-element"/>
  <xsl:param name="word-attr" select="''"/>
  <xsl:param name="word-attr-val" select="''"/>

  <xsl:param name="sentence-ids" required="yes"/> <!-- xs:string* or document-node() -->
  <xsl:variable name="sentence-ids-doc" as="document-node()">
    <xsl:choose>
      <xsl:when test="$sentence-ids instance of document-node()">
	<xsl:sequence select="$sentence-ids"/>
      </xsl:when>
      <xsl:otherwise>
	<xsl:document>
	  <d:sentences xmlns:d="http://www.daisy.org/ns/pipeline/data">
	    <xsl:for-each select="$sentence-ids">
	      <d:sentence id="{.}"/>
	    </xsl:for-each>
	  </d:sentences>
	</xsl:document>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:key name="sentences" match="*[@id]" use="@id"/>

  <xsl:template match="*[@id][key('sentences',@id,$sentence-ids-doc)]" priority="1">
    <ssml:s>
      <xsl:choose>
        <xsl:when test="self::ssml:speak">
          <xsl:sequence select="@* except @version"/>
          <!-- in order to preserve pre-translated SSML islands we wrap it inside a temporary token -->
          <ssml:token role="preserve">
            <xsl:sequence select="node()"/>
          </ssml:token>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates mode="inside-sentence" select="@*|node()"/>
        </xsl:otherwise>
      </xsl:choose>
    </ssml:s>
  </xsl:template>

  <!-- in order to preserve pre-translated SSML islands we wrap it inside a temporary token -->
  <xsl:template mode="inside-sentence" match="ssml:speak">
    <ssml:token role="preserve">
      <xsl:sequence select="@* except @version"/>
      <xsl:sequence select="node()"/>
    </ssml:token>
  </xsl:template>

  <xsl:template mode="inside-sentence" match="*[local-name()=$word-element and string(@*[local-name()=$word-attr])=$word-attr-val]">
    <ssml:token>
      <xsl:apply-templates mode="#current" select="@*|node()"/>
    </ssml:token>
  </xsl:template>

  <xsl:template mode="#default inside-sentence" match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates mode="#current" select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
