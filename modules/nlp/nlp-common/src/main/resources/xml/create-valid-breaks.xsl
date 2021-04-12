<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:f="functions"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                exclude-result-prefixes="xs">

  <xsl:param name="output-ns"/>
  <xsl:param name="output-sentence-tag"/>
  <xsl:param name="sentence-attr" select="''"/>
  <xsl:param name="sentence-attr-val" select="''"/>

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

  <xsl:key name="sentence-for-element" match="d:sentence" use="@element"/>

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

  <xsl:template match="*[local-name()=local-name-from-QName($tmp-sentence-tag)
		         and namespace-uri()=namespace-uri-from-QName($tmp-sentence-tag)]"
		mode="sentence-ids" priority="2">
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
      match="*[@pxi:special-sentence or
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

  <xsl:template match="node()" priority="1">
    <xsl:variable name="entry" select="key('sentence-for-element', generate-id(.), $sentence-ids-tree)"/>
    <xsl:choose>
      <xsl:when test="$entry and $entry/@recycled">
  	<xsl:copy copy-namespaces="no">
  	  <xsl:call-template name="copy-namespaces"/>
  	  <xsl:apply-templates select="@*" mode="inside-sentence"/>
  	  <xsl:if test="not(@id)">
  	    <xsl:attribute name="id">
  	      <xsl:value-of select="$entry/@id"/>
  	    </xsl:attribute>
  	  </xsl:if>
  	  <xsl:apply-templates select="node()" mode="inside-sentence">
  	    <xsl:with-param name="can-contain-sentences" select="exists(@pxi:can-contain-sentences)"/>
  	  </xsl:apply-templates>
  	</xsl:copy>
      </xsl:when>
      <xsl:when test="$entry">
  	<xsl:element name="{$output-sentence-tag}" namespace="{$output-ns}">
  	  <xsl:attribute name="id">
  	    <xsl:value-of select="$entry/@id"/>
  	  </xsl:attribute>
	  <xsl:if test="$sentence-attr != ''">
	    <xsl:attribute name="{$sentence-attr}">
	      <xsl:value-of select="$sentence-attr-val"/>
	    </xsl:attribute>
	  </xsl:if>
	  <xsl:copy-of select="@xml:lang"/> <!-- doesn't always exist -->
  	  <xsl:apply-templates select="node()" mode="inside-sentence">
	    <xsl:with-param name="can-contain-sentences" select="true()"/>
  	  </xsl:apply-templates>
  	</xsl:element>
      </xsl:when>
      <xsl:when test="(local-name()=local-name-from-QName($tmp-word-tag)
		       and namespace-uri()=namespace-uri-from-QName($tmp-word-tag))
		      or (local-name()=local-name-from-QName($tmp-sentence-tag)
		          and namespace-uri()=namespace-uri-from-QName($tmp-sentence-tag))">
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

  <xsl:template match="*[local-name()=local-name-from-QName($tmp-sentence-tag)
		         and namespace-uri()=namespace-uri-from-QName($tmp-sentence-tag)]"
		mode="inside-sentence" priority="2">
    <xsl:param name="can-contain-sentences" as="xs:boolean"/>
    <!-- Ignore the node: since we are already inside a sentence,
         it means that a parent node has been recycled to contain the
         current sentence (e.g. a pagenum or an existing sentence) -->
    <xsl:apply-templates select="node()" mode="#current">
      <xsl:with-param name="can-contain-sentences" select="$can-contain-sentences"/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="*[local-name()=local-name-from-QName($tmp-word-tag)
		         and namespace-uri()=namespace-uri-from-QName($tmp-word-tag)]"
		mode="inside-sentence" priority="2">
    <xsl:param name="can-contain-sentences" as="xs:boolean"/>
    <xsl:choose>
      <xsl:when test="$can-contain-sentences">
	<xsl:element name="{$output-word-tag}" namespace="{$output-ns}">
	  <xsl:if test="$word-attr != ''">
	    <xsl:attribute name="{$word-attr}">
	      <xsl:value-of select="$word-attr-val"/>
	    </xsl:attribute>
	  </xsl:if>
	  <xsl:apply-templates select="node()" mode="inside-word">
	    <xsl:with-param name="can-contain-sentences" select="exists(@pxi:can-contain-sentences)"/>
	  </xsl:apply-templates>
	</xsl:element>
      </xsl:when>
      <xsl:otherwise>
	<!-- The word is ignored. -->
	<xsl:apply-templates select="node()" mode="#current">
	  <xsl:with-param name="can-contain-sentences" select="exists(@pxi:can-contain-sentences)"/>
	</xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="*[$exclusive-sentence-tag='true' and local-name()=$output-sentence-tag]"
		mode="inside-sentence" priority="3">
    <!-- The existing sentence is ignored. Warning: the attributes are lost. -->
    <xsl:apply-templates select="node()" mode="#current">
      <xsl:with-param name="can-contain-sentences" select="true()"/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="*[$exclusive-word-tag='true' and local-name()=$output-word-tag]"
		mode="inside-sentence" priority="2">
    <xsl:copy copy-namespaces="no">
      <xsl:call-template name="copy-namespaces"/>
      <xsl:apply-templates select="@*|node()" mode="inside-word"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="node()" mode="inside-sentence" priority="1">
    <xsl:copy copy-namespaces="no">
      <xsl:call-template name="copy-namespaces"/>
      <xsl:apply-templates select="@*" mode="#current"/>
      <xsl:apply-templates select="node()" mode="#current">
	<xsl:with-param name="can-contain-sentences" select="exists(@pxi:can-contain-sentences)"/>
      </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="*[local-name()=local-name-from-QName($tmp-word-tag)
		         and namespace-uri()=namespace-uri-from-QName($tmp-word-tag)]"
		mode="inside-word" priority="2">
    <!-- the temporary word is ignored.-->
    <xsl:apply-templates select="node()" mode="#current"/>
  </xsl:template>

  <xsl:template match="*[$exclusive-word-tag='true' and local-name()=$output-word-tag]"
		mode="inside-word" priority="2">
    <!-- the word is ignored -->
    <xsl:choose>
      <xsl:when test="count(@*) > 0">
	<xsl:element name="{$output-subsentence-tag}" namespace="{$output-ns}">
	  <xsl:apply-templates select="@*|node()" mode="#current"/>
	</xsl:element>
      </xsl:when>
      <xsl:otherwise>
	<xsl:apply-templates select="node()" mode="#current"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="node()" mode="inside-word" priority="1">
    <xsl:copy copy-namespaces="no">
      <xsl:call-template name="copy-namespaces"/>
      <xsl:apply-templates select="@*|node()" mode="#current"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template mode="#default inside-sentence inside-word" match="@*">
    <xsl:sequence select="."/>
  </xsl:template>

  <xsl:template mode="#default inside-sentence inside-word"
		match="@pxi:special-sentence|
		       @pxi:can-contain-sentences|
		       @pxi:cannot-be-sentence-child"/>

  <!-- UTILS -->

  <xsl:variable name="remove-ns" as="xs:string*"
		select="('http://www.daisy.org/ns/pipeline/xproc/internal',
			 distinct-values(
			   for $tag in ($tmp-word-tag,$tmp-sentence-tag) return namespace-uri-from-QName($tag)))"/>

  <xsl:template name="copy-namespaces">
    <xsl:for-each select="namespace::*[not(.=$remove-ns)]">
      <xsl:sequence select="."/>
    </xsl:for-each>
  </xsl:template>

</xsl:stylesheet>

