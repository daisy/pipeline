<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:ssml="http://www.w3.org/2001/10/synthesis"
		exclude-result-prefixes="#all"
		version="2.0">

  <xsl:param name="lang"/>
  <xsl:param name="style-ns"/>

  <!-- must have the same value as in the Java part -->
  <xsl:variable name="mark-delimiter" select="'___'"/>

  <xsl:variable name="voice-attr" select="'voice-family'"/>

  <xsl:template match="/">
    <ssml:speak version="1.1">
      <!-- Group skippable elements with the same CSS properties. -->
      <xsl:for-each-group select="/*/ssml:s" group-by="string-join(@xml:lang|ssml:get-voice(.),'_')">
	<!-- Group by packets of 10 instances -->
	<xsl:for-each-group select="current-group()" group-by="(position() - 1) idiv 10">
	  <xsl:variable name="packet" select="current-group()"/>
	  <xsl:choose>
	    <xsl:when test="count($packet) = 1">
	      <!-- It can be hard to handle a mark between a skippable element and
	           nothing. Instead, we keep standlone skippable elts as regular,
	           mark-free sentences. -->
	      <ssml:s id="{ssml:get-skippable($packet[1])/@id}">
		<xsl:copy-of select="$packet[1]/@* except @*[local-name()=$voice-attr] except @id"/>
		<xsl:copy-of select="ssml:get-voice($packet[1])"/>
		<xsl:copy-of select="$packet[1]/node()"/>
	      </ssml:s>
	    </xsl:when>
	    <xsl:otherwise>
	      <ssml:s id="{concat('internal-holder-of-', ssml:get-skippable($packet[1])/@id)}">
		<!-- Copy CSS and xml:lang: -->
		<xsl:copy-of select="$packet[1]/@* except @id except @*[local-name()=$voice-attr]"/>
		<xsl:copy-of select="ssml:get-voice($packet[1])"/>

		<xsl:for-each select="$packet">
		  <xsl:variable name="foreach-pos" select="position()"/>
		  <xsl:variable name="current-skippable" select="ssml:get-skippable(current())"/>
		  <xsl:variable name="next-skippable" select="ssml:get-skippable($packet[$foreach-pos + 1])"/>

		  <!-- we are keeping all the intermediate nodes to as to keep
		       intermediate CSS and annotations -->
		  <xsl:copy-of select="current()/node()"/>

		  <!-- force the processor to end the pronunciation by a neutral prosody. -->
		  <xsl:value-of select="','"/>

		  <xsl:if test="$next-skippable">
		    <ssml:mark name="{concat($current-skippable/@id, $mark-delimiter, $next-skippable/@id)}"/>

		    <!-- force the processor to reinitialize the prosody state. -->
		    <xsl:value-of select="' . '"/>
		  </xsl:if>
		</xsl:for-each>
	      </ssml:s>
	    </xsl:otherwise>
	  </xsl:choose>
	</xsl:for-each-group>
      </xsl:for-each-group>
    </ssml:speak>
  </xsl:template>

  <xsl:function name="ssml:get-skippable">
    <xsl:param name="sentence"/>
    <xsl:sequence select="$sentence/descendant::*[@id][last()]"/>
    <!-- We have to be extra careful with this technique because a skippable can be
         surrounded by many nodes (such as annotations), thus we have to make sure that
         annotations have no @id attached. To address this problem, it would be ok to pass
         the list of skippable @ids as a parameter of this stylesheet. -->
  </xsl:function>

  <xsl:function name="ssml:get-voice">
    <xsl:param name="sentence"/>
    <xsl:sequence select="ssml:get-skippable($sentence)/ancestor-or-self::*[@*[local-name()=$voice-attr]]/@*[local-name()=$voice-attr]"/>
  </xsl:function>

</xsl:stylesheet>
