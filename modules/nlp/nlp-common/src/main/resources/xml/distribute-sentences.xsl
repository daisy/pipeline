<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
		xmlns:xs="http://www.w3.org/2001/XMLSchema"
		xmlns:d="http://www.daisy.org/ns/pipeline/data"
		xmlns:c="http://www.w3.org/ns/xproc-step"
		xmlns:f="functions"
		xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
		xmlns:xml="http://www.w3.org/XML/1998/namespace"
		exclude-result-prefixes="xs">

  <xsl:variable name="options" as="element(c:param-set)" select="collection()[2]/*"/>

  <!--
      This relies on p:in-scope-names adding the namespaces that travel with an option
      (https://www.w3.org/TR/xproc/#opt-param-bindings) to the c:param element. This is true for our
      version of XMLCalabash.
  -->
  <xsl:variable name="tmp-word-tag" as="xs:QName"
		select="f:param-value-as-QName($options/c:param[@name='tmp-word-tag'])"/>
  <xsl:variable name="tmp-sentence-tag" as="xs:QName"
		select="f:param-value-as-QName($options/c:param[@name='tmp-sentence-tag'])"/>

  <xsl:function name="f:param-value-as-QName" as="xs:QName">
    <xsl:param name="param" as="element(c:param)"/>
    <xsl:sequence select="resolve-QName($param/@value,$param)"/>
  </xsl:function>

  <!-- Copy the document until a sentence is found. -->

  <xsl:template match="@*|node()" priority="1">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="*[local-name()=local-name-from-QName($tmp-sentence-tag)
		         and namespace-uri()=namespace-uri-from-QName($tmp-sentence-tag)]"
		priority="2">
    <xsl:choose>
      <xsl:when test="../@pxi:can-contain-sentences">
	<xsl:call-template name="new-sent-on-top-of-children">
	  <!-- @xml:lang comes from the Java detection -->
	  <xsl:with-param name="lang" select="@xml:lang"/>
	</xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
	<xsl:apply-templates select="node()" mode="split-sentence">
	  <xsl:with-param name="lang" select="@xml:lang"/>
	</xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="*[@pxi:can-contain-sentences]"
		mode="split-sentence" priority="3">
    <xsl:param name="lang" select="''"/>
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:call-template name="new-sent-on-top-of-children">
	<xsl:with-param name="lang" select="if (@xml:lang) then @xml:lang else $lang"/>
      </xsl:call-template>
    </xsl:copy>
  </xsl:template>

  <!-- Sentences forbidden here: let us try the insertion inside the children. -->
  <xsl:template match="node()" mode="split-sentence" priority="1">
    <xsl:param name="lang" select="''"/>
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="node()" mode="split-sentence">
	<xsl:with-param name="lang" select="if (@xml:lang) then @xml:lang else $lang"/>
      </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="*[local-name()=local-name-from-QName($tmp-word-tag)
		         and namespace-uri()=namespace-uri-from-QName($tmp-word-tag)]"
		mode="split-sentence" priority="2">
    <xsl:param name="lang" select="''"/>
    <xsl:copy-of select="node()"/> <!-- ignore the tmp:word -->
  </xsl:template>

  <xsl:template match="*[local-name()=local-name-from-QName($tmp-sentence-tag)
		         and namespace-uri()=namespace-uri-from-QName($tmp-sentence-tag)]"
		mode="split-sentence" priority="2">
    <xsl:param name="lang" select="''"/>
    <!-- Should not happen. -->
    <xsl:copy-of select="node()"/>
  </xsl:template>

  <xsl:template name="new-sent-on-top-of-children">
    <xsl:param name="lang" select="''"/>
    <xsl:for-each-group select="node()"
			group-adjacent="self::text() or not(@pxi:cannot-be-sentence-child)">
      <xsl:choose>
	<xsl:when test="current-grouping-key()">
	  <xsl:choose>
	    <xsl:when test="empty(current-group()[self::* or normalize-space()])">
	      <xsl:sequence select="current-group()"/>
	    </xsl:when>
	    <xsl:otherwise>
	      <!-- assuming the tmp words are inserted at the lowest possible level. -->
	      <xsl:element name="{local-name-from-QName($tmp-sentence-tag)}"
			   namespace="{namespace-uri-from-QName($tmp-sentence-tag)}">
		<xsl:if test="$lang != ''">
		  <xsl:attribute namespace="http://www.w3.org/XML/1998/namespace" name="lang">
		    <xsl:value-of select="$lang"/>
		  </xsl:attribute>
		</xsl:if>
		<xsl:copy-of select="current-group()"/> <!-- including the <tmp:word> nodes. -->
	      </xsl:element>
	    </xsl:otherwise>
	  </xsl:choose>
	</xsl:when>
	<xsl:otherwise>
	  <xsl:apply-templates select="current-group()" mode="split-sentence">
	    <xsl:with-param name="lang" select="$lang"/>
	  </xsl:apply-templates>
	</xsl:otherwise>
      </xsl:choose>
    </xsl:for-each-group>
  </xsl:template>


</xsl:stylesheet>

