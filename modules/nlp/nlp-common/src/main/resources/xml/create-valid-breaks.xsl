<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                exclude-result-prefixes="xs">

  <xsl:param name="tmp-word-tag"/>
  <xsl:param name="tmp-sentence-tag"/>
  <xsl:param name="can-contain-words"/>
  <xsl:param name="special-sentences" select="''"/>
  <xsl:param name="output-ns"/>
  <xsl:param name="output-sentence-tag"/>
  <xsl:param name="exclusive-sentence-tag" select="'true'"/> <!-- false if the element can be used for another purpose -->
  <xsl:param name="exclusive-word-tag" select="'true'"/><!-- false if the element can be used for another purpose -->
  <xsl:param name="output-subsentence-tag" />
  <xsl:param name="id-prefix" select="''"/>

  <!-- The words need an additional pair (attr, val), otherwise they
       could not be identified later on, unlike the sentences which
       are identified thanks to their id. -->
  <xsl:param name="output-word-tag"/>
  <xsl:param name="word-attr" select="''"/>
  <xsl:param name="word-attr-val" select="''"/>

  <xsl:key name="sentence-for-element" match="d:sentence" use="@element"/>

  <xsl:variable name="special-list" select="concat(',', $special-sentences, ',')"/>

  <!--========================================================= -->
  <!-- FIND ALL THE SENTENCES' ID                               -->
  <!--========================================================= -->

  <xsl:variable name="sentence-ids-tree">
    <xsl:variable name="sentence-ids-tree">
      <d:sentences>
        <xsl:apply-templates select="/*" mode="sentence-ids"/>
      </d:sentences>
    </xsl:variable>
    <xsl:for-each select="$sentence-ids-tree/*">
      <xsl:copy>
        <xsl:for-each select="*">
          <xsl:copy>
            <xsl:attribute name="id" select="concat('st', $id-prefix, position())"/>
            <xsl:sequence select="@*"/>
          </xsl:copy>
        </xsl:for-each>
      </xsl:copy>
    </xsl:for-each>
  </xsl:variable>

  <xsl:template match="*" mode="sentence-ids" priority="1">
    <xsl:apply-templates select="*" mode="#current"/>
  </xsl:template>

  <xsl:template match="*[local-name() = $tmp-sentence-tag]" mode="sentence-ids" priority="2">
    <d:sentence element="{generate-id(.)}">
      <xsl:copy-of select="@xml:lang"/> <!-- doesn't always exist -->
    </d:sentence>
  </xsl:template>

  <!-- If an existing sentence is the parent of detected temporary
       sentence(s), it will be used instead of them. That is, the
       temporary sentence(s) will be ignored. Likewise, if a temporary
       sentence is the parent of existing sentence(s), the existing
       sentences will be discarded. -->
  <xsl:template mode="sentence-ids" priority="3"
      match="*[contains($special-list, concat(',', local-name(), ',')) or
	     ($exclusive-sentence-tag = 'true' and local-name() = $output-sentence-tag)]">
    <!-- TODO: copy the @xml:lang -->
    <d:sentence element="{generate-id(.)}" recycled="1"/>
    <!-- Warning: a 'special-sentence', such as noteref, is unlikely
         to be stamped as 'recycled' because it is usually the child
         of a tmp:sentence (not the other way around). -->
  </xsl:template>

  <!--======================================================== -->
  <!-- INSERT THE SENTENCES USING VALID ELEMENTS COMPLIANT     -->
  <!-- WITH THE FORMAT (e.g. Zedai, DTBook)                    -->
  <!--======================================================== -->

  <xsl:template match="/" priority="2">
    <xsl:apply-templates select="node()"/>
    <!-- Write the list of sentences on the secondary port. -->
    <xsl:result-document href="{concat('sids', $id-prefix, generate-id(), '.xml')}" method="xml">
      <xsl:for-each select="$sentence-ids-tree/*">
        <xsl:copy>
          <xsl:for-each select="*">
            <xsl:copy>
              <xsl:sequence select="@* except @element"/>
            </xsl:copy>
          </xsl:for-each>
        </xsl:copy>
      </xsl:for-each>
    </xsl:result-document>
  </xsl:template>

  <xsl:template match="@*|node()" priority="1">
    <xsl:variable name="entry" select="key('sentence-for-element', generate-id(.), $sentence-ids-tree)"/>
    <xsl:choose>
      <xsl:when test="$entry and $entry/@recycled">
  	<xsl:copy copy-namespaces="no">
  	  <xsl:call-template name="copy-namespaces"/>
  	  <xsl:copy-of select="@*"/>
  	  <xsl:if test="not(@id)">
  	    <xsl:attribute name="id">
  	      <xsl:value-of select="$entry/@id"/>
  	    </xsl:attribute>
  	  </xsl:if>
  	  <xsl:apply-templates select="node()" mode="inside-sentence">
  	    <xsl:with-param name="parent-name" select="local-name()"/>
  	  </xsl:apply-templates>
  	</xsl:copy>
      </xsl:when>
      <xsl:when test="$entry">
  	<xsl:element name="{$output-sentence-tag}" namespace="{$output-ns}">
  	  <xsl:attribute name="id">
  	    <xsl:value-of select="$entry/@id"/>
  	  </xsl:attribute>
	  <xsl:copy-of select="@xml:lang"/> <!-- doesn't always exist -->
  	  <xsl:apply-templates select="node()" mode="inside-sentence">
  	    <xsl:with-param name="parent-name" select="$output-sentence-tag"/>
  	  </xsl:apply-templates>
  	</xsl:element>
      </xsl:when>
      <xsl:when test="local-name() = $tmp-word-tag or local-name() = $tmp-sentence-tag">
  	<!-- The node is ignored. This shouldn't happen though, because the -->
  	<!-- sentences have been properly distributed by the previous -->
  	<!-- script. -->
  	<xsl:apply-templates select="node()"/>
      </xsl:when>
      <xsl:otherwise>
  	<xsl:copy copy-namespaces="no">
  	  <xsl:call-template name="copy-namespaces"/>
  	  <xsl:apply-templates select="@*|node()"/>
  	</xsl:copy>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--======================================================== -->
  <!-- INSIDE THE SENTENCES (ONCE THEY HAVE BEEN ADDED)        -->
  <!--======================================================== -->

  <xsl:template match="*[local-name() = $tmp-sentence-tag]" mode="inside-sentence" priority="2">
    <xsl:param name="parent-name"/>
    <!-- Ignore the node: since we are already inside a sentence,
         it means that a parent node has been recycled to contain the
         current sentence (e.g. a pagenum or an existing sentence) -->
    <xsl:apply-templates select="node()" mode="inside-sentence">
      <xsl:with-param name="parent-name" select="$parent-name"/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:variable name="ok-parent-list" select="concat(',', $can-contain-words, ',', $output-sentence-tag, ',')" />
  <xsl:template match="*[local-name() = $tmp-word-tag]" mode="inside-sentence" priority="2">
    <xsl:param name="parent-name"/>
    <xsl:choose>
      <xsl:when test="contains($ok-parent-list, concat(',', $parent-name, ','))">
	<xsl:element name="{$output-word-tag}" namespace="{$output-ns}">
	  <xsl:if test="$word-attr != ''">
	    <xsl:attribute name="{$word-attr}">
	      <xsl:value-of select="$word-attr-val"/>
	    </xsl:attribute>
	  </xsl:if>
	  <xsl:apply-templates select="node()" mode="inside-word">
	    <xsl:with-param name="parent-name" select="local-name()"/>
	  </xsl:apply-templates>
	</xsl:element>
      </xsl:when>
      <xsl:otherwise>
	<!-- The word is ignored. -->
	<xsl:apply-templates select="node()" mode="inside-sentence">
	  <xsl:with-param name="parent-name" select="local-name()"/>
	</xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="*[$exclusive-sentence-tag='true' and local-name()=$output-sentence-tag]"
		mode="inside-sentence" priority="3">
    <!-- The existing sentence is ignored. Warning: the attributes are lost. -->
    <xsl:apply-templates select="node()" mode="inside-sentence">
      <xsl:with-param name="parent-name" select="local-name()"/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="*[$exclusive-word-tag='true' and local-name()=$output-word-tag]"
		mode="inside-sentence" priority="2">
    <xsl:copy copy-namespaces="no">
      <xsl:call-template name="copy-namespaces"/>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="node()" mode="inside-word"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="node()" mode="inside-sentence" priority="1">
    <xsl:copy copy-namespaces="no">
      <xsl:call-template name="copy-namespaces"/>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="node()" mode="inside-sentence">
	<xsl:with-param name="parent-name" select="local-name()"/>
      </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="*[local-name() = $tmp-word-tag]" mode="inside-word" priority="2">
    <!-- the temporary word is ignored.-->
    <xsl:apply-templates select="node()" mode="inside-word"/>
  </xsl:template>

  <xsl:template match="*[$exclusive-word-tag='true' and local-name()=$output-word-tag]"
		mode="inside-word" priority="2">
    <!-- the word is ignored -->
    <xsl:choose>
      <xsl:when test="count(@*) > 0">
	<xsl:element name="{$output-subsentence-tag}" namespace="{$output-ns}">
	  <xsl:copy-of select="@*"/>
	  <xsl:apply-templates select="node()" mode="inside-word"/>
	</xsl:element>
      </xsl:when>
      <xsl:otherwise>
	<xsl:apply-templates select="node()" mode="inside-word"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="node()" mode="inside-word" priority="1">
    <xsl:copy copy-namespaces="no">
      <xsl:call-template name="copy-namespaces"/>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="node()" mode="inside-word"/>
    </xsl:copy>
  </xsl:template>

  <!-- UTILS -->
  <xsl:template name="copy-namespaces">
    <xsl:for-each select="namespace::* except namespace::tmp">
      <xsl:namespace name="{name(.)}" select="string(.)"/>
    </xsl:for-each>
  </xsl:template>

</xsl:stylesheet>

