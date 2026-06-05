<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:ssml="http://www.w3.org/2001/10/synthesis"
		xmlns:tts="http://www.daisy.org/ns/pipeline/tts"
		exclude-result-prefixes="#all"
		version="2.0">

  <xsl:import href="flatten-css.xsl"/>

  <xsl:param name="lang"/>
  <xsl:param name="style-ns"/>

  <xsl:key name="bindings" match="*[@sent]" use="@sent"/>
  <xsl:key name="parent-to-child" match="*[@ancestor]" use="@ancestor"/>

  <!-- Map the sentences to their ancestors if they are attached to cues or pauses -->
  <xsl:variable name="sent-ancestors">
    <tts:parents>
      <xsl:for-each select="//ssml:s">
	<xsl:variable name="sent-id" select="@id"/>
	<xsl:for-each select="ancestor-or-self::*[@tts:cue-before or
			      @tts:cue-after or @tts:pause-before or @tts:pause-after]">
	  <tts:parenting ancestor="{generate-id(.)}" sent="{$sent-id}"/>
	</xsl:for-each>
      </xsl:for-each>
    </tts:parents>
  </xsl:variable>

  <xsl:variable name="bindings">
    <tts:bindings>
      <xsl:apply-templates mode="build-bindings" select="/*"/>
    </tts:bindings>
  </xsl:variable>

  <xsl:template name="bind-one-sent">
    <xsl:param name="sent-id"/>
    <xsl:param name="pause"/>
    <xsl:param name="cue"/>
    <xsl:if test="$pause != '' or $cue != ''">
      <bind sent="{$sent-id}">
	<xsl:if test="$pause != ''">
	  <xsl:attribute name="pause"><xsl:value-of select="$pause"/></xsl:attribute>
	</xsl:if>
	<xsl:if test="$cue != ''">
	  <xsl:attribute name="cue"><xsl:value-of select="$cue"/></xsl:attribute>
	</xsl:if>
      </bind>
    </xsl:if>
  </xsl:template>

  <xsl:template match="*" mode="bind-all-the-way-through">
    <xsl:param name="sent-id"/>
    <xsl:call-template name="bind-one-sent">
      <xsl:with-param name="sent-id" select="$sent-id"/>
      <xsl:with-param name="pause" select="@tts:pause-before"/>
      <xsl:with-param name="cue" select="@tts:cue-before"/>
    </xsl:call-template>
    <xsl:apply-templates select="*" mode="bind-all-the-way-through">
      <xsl:with-param name="sent-id" select="$sent-id"/>
    </xsl:apply-templates>
    <xsl:call-template name="bind-one-sent">
      <xsl:with-param name="sent-id" select="$sent-id"/>
      <xsl:with-param name="pause" select="@tts:pause-after"/>
      <xsl:with-param name="cue" select="@tts:cue-after"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="ssml:s[@id]" mode="build-bindings" priority="2">
    <xsl:call-template name="bind-one-sent">
      <xsl:with-param name="sent-id" select="@id"/>
      <xsl:with-param name="pause" select="@tts:pause-before"/>
      <xsl:with-param name="cue" select="@tts:cue-before"/>
    </xsl:call-template>
    <content sent="{@id}"/> <!-- we'll deal later with the inner pauses and cues -->
    <xsl:call-template name="bind-one-sent">
      <xsl:with-param name="sent-id" select="@id"/>
      <xsl:with-param name="pause" select="@tts:pause-after"/>
      <xsl:with-param name="cue" select="@tts:cue-after"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="*" mode="build-bindings" priority="1">
    <xsl:variable name="descendants" select="key('parent-to-child', generate-id(), $sent-ancestors)"/>
    <xsl:choose>
      <xsl:when test="$descendants">
	<xsl:call-template name="bind-one-sent">
	  <xsl:with-param name="sent-id" select="$descendants[1]/@sent"/>
	  <xsl:with-param name="pause" select="@tts:pause-before"/>
	  <xsl:with-param name="cue" select="@tts:cue-before"/>
	</xsl:call-template>
	<xsl:apply-templates select="*" mode="build-bindings"/>
	<xsl:call-template name="bind-one-sent">
	  <xsl:with-param name="sent-id" select="$descendants[last()]/@sent"/>
	  <xsl:with-param name="pause" select="@tts:pause-after"/>
	  <xsl:with-param name="cue" select="@tts:cue-after"/>
	</xsl:call-template>
      </xsl:when>
      <xsl:when test="@tts:pause-before or @tts:cue-before or @tts:pause-after or @tts:cue-after">
	<!-- We have a pause or a cue but no descendant sentence available. So we decide
	     to bind them arbitrairly to the next sentence or the previous one. It
	     shouldn't make any difference which one. -->
	<xsl:variable name="neighbor" select="preceding::ssml:s[@id][1]|following::ssml:s[@id][1]"/>
	<xsl:if test="$neighbor">
	  <xsl:apply-templates select="." mode="bind-all-the-way-through">
	    <xsl:with-param name="sent-id" select="$neighbor[1]/@id"/>
	  </xsl:apply-templates>
	</xsl:if>
      </xsl:when>
      <xsl:otherwise>
	<xsl:apply-templates select="*" mode="build-bindings"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- ========= iterate over the sentences ========= -->
  <xsl:template match="/">
    <ssml:speak version="1.1"> <!-- version 1.0 has no <ssml:token>, nor <ssml:w>. -->
      <xsl:for-each select="//ssml:s">
	<ssml:s>
	  <xsl:variable name="sentence" select="current()"/>
	  <xsl:copy-of select="$sentence/@*"/>
	  <xsl:apply-templates select="$sentence" mode="flatten-css-properties">
	    <xsl:with-param name="style-ns" select="$style-ns"/>
	    <xsl:with-param name="lang" select="$lang"/>
	  </xsl:apply-templates>
	  <xsl:for-each select="key('bindings', $sentence/@id, $bindings)">
	    <xsl:choose>
	      <xsl:when test="current()/@pause or current()/@cue">
		<xsl:apply-templates select="current()/@pause" mode="pause"/>
		<xsl:apply-templates select="current()/@cue" mode="cue"/>
	      </xsl:when>
	      <xsl:otherwise>
		<!-- sentence content -->
		<xsl:apply-templates select="$sentence/node()" mode="inside-sentence"/>

		<!-- Capture the next punctuation marks to produce the right prosody. -->
		<xsl:variable name="next-text-node"
			      select="$sentence/following-sibling::text()[normalize-space(.) != ''][1]"/>

		<xsl:if test="$next-text-node">
		  <xsl:analyze-string select="$next-text-node" regex="^[\p{{Z}}]*([.!?…])">
		    <xsl:matching-substring>
		      <xsl:value-of select="regex-group(1)"/>
		    </xsl:matching-substring>
		    <xsl:non-matching-substring/>
		  </xsl:analyze-string>
		</xsl:if>

		<!-- Legacy code (too CPU-intensive, but more accurate): -->
		<!-- <xsl:variable name="next-node" select="following-sibling::node()[preceding-sibling::ssml:s = current()]"/> -->
		<!-- <xsl:variable name="next-node-content" select="if ($next-node/self::text()) -->
		<!-- 						   then $next-node -->
		<!-- 						   else string-join($next-node//text(),'')"/> -->

		<!-- <xsl:value-of select="replace($next-node-content, '[^.!?…]', '')"/> -->


		<!-- No ssml:break is added here as some TTS processors may already add
		     the silent fragments after the final punctuation marks. -->
	      </xsl:otherwise>
	    </xsl:choose>
	  </xsl:for-each>
	</ssml:s>
      </xsl:for-each>
    </ssml:speak>
  </xsl:template>

  <!-- === copy the sentences' content and add the cues and pauses if necessary === -->
  <xsl:template match="*" mode="inside-sentence">
    <xsl:apply-templates select="@tts:pause-before" mode="pause"/>
    <xsl:apply-templates select="@tts:cue-before" mode="cue"/>

    <xsl:element name="{name()}" namespace="{namespace-uri()}">
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="node()" mode="inside-sentence"/>
    </xsl:element>

    <xsl:apply-templates select="@tts:cue-after" mode="cue"/>
    <xsl:apply-templates select="@tts:pause-after" mode="pause"/>
  </xsl:template>

  <xsl:template match="text()" mode="inside-sentence">
    <xsl:value-of select="."/>
  </xsl:template>

  <xsl:template match="@*" mode="pause">
    <ssml:break time="{.}"/>
  </xsl:template>

  <xsl:template match="@*" mode="cue">
    <xsl:choose>
      <xsl:when test="starts-with(., 'file:///')">
	<ssml:audio src="{substring-after(., 'file://')}"/>
      </xsl:when>
      <xsl:when test="starts-with(., 'file:/')">
	<ssml:audio src="{substring-after(., 'file:')}"/>
      </xsl:when>
      <xsl:when test="starts-with(., '/')">
	<ssml:audio src="{.}"/>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>

