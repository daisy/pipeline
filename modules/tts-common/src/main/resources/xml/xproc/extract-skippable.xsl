<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:xs="http://www.w3.org/2001/XMLSchema"
		xmlns:ssml="http://www.w3.org/2001/10/synthesis"
		exclude-result-prefixes="#all"
		version="2.0">

  <xsl:param name="sentence-ids" required="yes"/> <!-- xs:string* or document-node() -->
  <xsl:param name="skippable-ids" required="yes"/> <!-- xs:string* or document-node() -->

  <!-- must have the same value as in ../../../java/org/daisy/pipeline/tts/calabash/impl/FormatSpecifications.java -->
  <xsl:variable name="mark-delimiter" select="'___'"/>
  <xsl:variable name="main-doc" select="collection()[1]"/>
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
  <xsl:variable name="skippable-ids-doc" as="document-node()">
    <xsl:choose>
      <xsl:when test="$skippable-ids instance of document-node()">
	<xsl:sequence select="$skippable-ids"/>
      </xsl:when>
      <xsl:otherwise>
	<xsl:document>
	  <d:skippables xmlns:d="http://www.daisy.org/ns/pipeline/data">
	    <xsl:for-each select="$skippable-ids">
	      <d:skippable id="{.}"/>
	    </xsl:for-each>
	  </d:skippables>
	</xsl:document>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:key name="sentence" match="*[@id]" use="@id"/>
  <xsl:key name="skippable" match="*[@id]" use="@id"/>

  <xsl:template match="/">
    <!-- primary output port -->
    <xsl:apply-templates select="/*"/>
    <!-- secondary output port -->
    <xsl:result-document href="skippable-only.xml">
      <xsl:variable name="sentences" as="element()*">
	<xsl:for-each select="$sentence-ids-doc//@id">
	  <xsl:sequence select="key('sentence',.,$main-doc)"/>
	</xsl:for-each>
      </xsl:variable>
      <ssml:speak version="1.1">
	<xsl:for-each select="$skippable-ids-doc//@id">
	  <xsl:variable name="skippable-id" as="xs:string" select="."/>
	  <xsl:variable name="skippable" select="key('skippable',.,$main-doc)"/>
	  <!-- we are copying the whole tree because there might be annotations
	       inserted around the skippable element. -->
	  <xsl:variable name="under-sent" as="element()?" select="$skippable/ancestor-or-self::* intersect $sentences/*"/>
	  <xsl:for-each select="$under-sent">
	    <xsl:variable name="sent" as="element()" select="$under-sent/parent::*"/>
	    <ssml:s>
	      <xsl:copy-of select="$sent/(@* except @id)"/> <!-- copy xml:lang and CSS -->
	      <xsl:copy>
		<xsl:copy-of select="@*|node()"/>
	      </xsl:copy>
	    </ssml:s>
	  </xsl:for-each>
	</xsl:for-each>
      </ssml:speak>
    </xsl:result-document>
  </xsl:template>

  <xsl:template match="*[@id][exists(key('sentence',@id,$sentence-ids-doc))]">
    <xsl:variable name="has-skippable" select="ssml:contains-skippable(.)"/>
    <xsl:variable name="clips" select="*[@id and not(key('skippable',@id,$skippable-ids-doc))]"/>
    <xsl:choose>
      <xsl:when test="$has-skippable and count($clips)=1">
	<!-- If there is only one clip in the sentence, it would make sense to create
	     partial mark names (e.g. '___id1' or 'id2___') but it is safer to
	     transform those kind of clips into regular sentences. -->
	<xsl:copy>
	  <xsl:copy-of select="@* except @id"/>
	  <xsl:copy-of select="$clips[1]/@id"/>
	  <xsl:for-each select="node()">
	    <xsl:if test="not(ssml:contains-skippable(.) or ssml:is-skippable(.))">
	      <xsl:apply-templates select="." mode="copy-without-id"/>
	    </xsl:if>
	  </xsl:for-each>
	</xsl:copy>
      </xsl:when>
      <xsl:when test="$has-skippable and count($clips) = 0">
	<!-- Sentences that contain only a skippable elements are not copied because
	     it entails creating floating SSML marks referring to no surrounding
	     elements, and no surrounding marks would link to such skippable elements. So
	     we wouldn't be able to use any mark to map these skippable elements to
	     audio clips and ssml-to-audio will end up creating a clip for the whole
	     sentence, thereby making it impossible to create a SMIL entry for the
	     skippable elements inside the sentence's audio clip. -->
	<!-- Nonetheless, these skippable elements are not lost: they will be copied
	     to the skippable-only document. -->
      </xsl:when>
      <xsl:when test="not($has-skippable)">
	<xsl:copy>
	  <xsl:copy-of select="@*"/>
	  <xsl:apply-templates select="node()" mode="copy-without-id"/>
	</xsl:copy>
      </xsl:when>
      <xsl:otherwise>
	<xsl:copy>
	  <xsl:copy-of select="@*"/>
	  <xsl:for-each select="node()">
	    <xsl:variable name="under-sent" select="."/>
	    <xsl:choose>
	      <xsl:when test="not(ssml:contains-skippable(.) or ssml:is-skippable(.))">
		<xsl:apply-templates select="$under-sent" mode="copy-without-id"/>
	      </xsl:when>
	      <xsl:when test="not(ssml:is-skippable($under-sent/preceding-sibling::*[1]))">
		<xsl:variable name="clip-before"
			      select="$under-sent/preceding-sibling::*[@id and not(ssml:is-skippable(.))][1]/@id"/>
		<xsl:variable name="clip-after"
			      select="$under-sent/following-sibling::*[@id and not(ssml:is-skippable(.))][1]/@id"/>

		<!-- The skippable element is replaced with a mark delimiting the end of
		     the previous element and the beginning of the next one. The
		     skippable elements are copied to a separate document. -->
		<ssml:mark name="{concat($clip-before,$mark-delimiter,$clip-after)}"/>
	      </xsl:when>
	    </xsl:choose>
	  </xsl:for-each>
	</xsl:copy>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:function name="ssml:contains-skippable" as="xs:boolean">
    <xsl:param name="n" as="node()"/>
    <xsl:value-of select="count($n/descendant::*[ssml:is-skippable(.)]) > 0"/>
  </xsl:function>

  <xsl:function name="ssml:is-skippable" as="xs:boolean">
    <xsl:param name="n" as="node()"/>
    <xsl:value-of select="$n/@id and key('skippable',$n/@id,$skippable-ids-doc)"/>
  </xsl:function>

  <!-- Once the marks have been inserted, we don't need the inner @ids anymore. Besides,
       keeping @ids would create duplicate @ids when clips are transformed into regular
       sentences. -->
  <xsl:template match="node()" mode="copy-without-id">
    <xsl:copy>
      <xsl:copy-of select="@* except @id"/>
      <xsl:apply-templates select="node()" mode="copy-without-id"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
