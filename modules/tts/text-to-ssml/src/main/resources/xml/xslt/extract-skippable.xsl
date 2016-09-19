<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:ssml="http://www.w3.org/2001/10/synthesis"
		xmlns:xs="http://www.w3.org/2001/XMLSchema"
		exclude-result-prefixes="#all"
		version="2.0">

  <!-- must have the same value as in the Java part -->
  <xsl:variable name="mark-delimiter" select="'___'"/>
  <xsl:variable name="main-doc" select="collection()[1]"/>
  <xsl:variable name="skippable-ids" select="collection()[2]"/>

  <xsl:key name="skippable" match="*[@id]" use="@id"/>

  <xsl:template match="/" priority="2">
    <!-- Primary output port: -->
    <ssml:speak version="1.1">
      <xsl:for-each select="$main-doc/*/ssml:s">
	<xsl:variable name="has-skippable" select="ssml:contains-skippable(current())"/>
	<xsl:variable name="clips" select="*[@id and not(key('skippable', @id, $skippable-ids))]"/>
	<xsl:choose>
	  <xsl:when test="$has-skippable and  count($clips) = 1">
	    <!-- If there is only one clip in the sentence, it would make sense to create
	         partial mark names (e.g. '___id1' or 'id2___') but it is safer to
	         transform those kind of clips into regular sentences. -->
	    <xsl:copy>
	      <xsl:copy-of select="@* except @id"/>
	      <xsl:copy-of select="$clips[1]/@id"/>
	      <xsl:for-each select="node()">
		<xsl:if test="not(ssml:contains-skippable(current()) or ssml:is-skippable(current()))">
		  <xsl:apply-templates select="current()" mode="copy-without-id"/>
		</xsl:if>
	      </xsl:for-each>
	    </xsl:copy>
	  </xsl:when>
	  <xsl:when test="$has-skippable and count($clips) = 0">
	    <!-- Sentences that contain only a skippable elements are not copied because
	         it entails creating floating SSML marks referring to no surrounding
	         elements, and no surrounding marks would link to such skippable elts. So
	         we wouldn't be able to use any mark to map these skippable elements to
	         audio clips and ssml-to-audio will end up creating a clip for the whole
	         sentence, thereby making it impossible to create a SMIL entry for the
	         skippable elts inside the sentence's audio clip. -->
	    <!-- Nonetheless, these skippable elements are not lost: they will be copied
	         to the skippable-only document. -->
	  </xsl:when>
	  <xsl:when test="not($has-skippable)">
	    <xsl:copy>
	      <xsl:copy-of select="@*"/>
	      <xsl:apply-templates select="current()/node()" mode="copy-without-id"/>
	    </xsl:copy>
	  </xsl:when>
	  <xsl:otherwise>
	    <xsl:copy>
	      <xsl:copy-of select="@*"/>
	      <xsl:for-each select="node()">
		<xsl:variable name="under-sent" select="current()"/>
		<xsl:choose>
		  <xsl:when test="not(ssml:contains-skippable(current()) or ssml:is-skippable(current()))">
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
		    <ssml:mark name="{concat($clip-before, $mark-delimiter, $clip-after)}"/>
		  </xsl:when>
		</xsl:choose>
	      </xsl:for-each>
	    </xsl:copy>
	  </xsl:otherwise>
	</xsl:choose>
      </xsl:for-each>
    </ssml:speak>

    <!-- Secondary output port: -->
    <xsl:result-document href="skippable-only.xml">
      <ssml:speak version="1.1">
	<xsl:for-each select="$skippable-ids/*/*">
	  <xsl:variable name="in-doc" select="key('skippable', @id, collection()[1])"/>
	  <xsl:variable name="under-sent" select="$in-doc/ancestor-or-self::*[parent::ssml:s][1]"/>
	  <xsl:if test="$under-sent">
	    <ssml:s id="{concat('holder-of-', @id)}">
	      <xsl:copy-of select="$under-sent/../@*[name() != 'id']"/> <!-- including CSS if any -->
	      <xsl:copy-of select="$under-sent"/> <!-- here we must keep the @ids of the skippable elts (required for MathML) -->
	      <!-- we are copying the whole tree because there might be annotations
	           inserted around the skippable element. -->
	    </ssml:s>
	  </xsl:if>
	</xsl:for-each>
      </ssml:speak>
    </xsl:result-document>
  </xsl:template>

  <xsl:function name="ssml:contains-skippable" as="xs:boolean">
    <xsl:param name="n" as="node()"/>
    <xsl:value-of select="count($n/descendant::*[ssml:is-skippable(.)]) > 0"/>
  </xsl:function>

  <xsl:function name="ssml:is-skippable" as="xs:boolean">
    <xsl:param name="n" as="node()"/>
    <xsl:value-of select="$n/@id and key('skippable', $n/@id, $skippable-ids)"/>
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
