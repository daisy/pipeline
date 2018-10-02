<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:d="http://www.daisy.org/ns/pipeline/data"
		xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
		exclude-result-prefixes="#all" version="2.0">

  <!-- Prerequesite: SMIL nodes must have @ids -->

  <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>

  <xsl:param name="no-smilref"/>
  <xsl:param name="mo-dir"/>
  <xsl:param name="output-dir"/>

  <xsl:variable name="mo-dir-rel" select="pf:relativize-uri($mo-dir, $output-dir)"/>

  <!-- pagenums and noterefs are also linked by the NCX but they can't
       be SMIL seq (they have no children), so there is no need to
       make a special case of them.-->
  <xsl:variable name="ncx-linked" select="('levelhd', 'hd', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'note')"/>

  <xsl:key name="clips" match="*[@idref]" use="@idref"/>

  <xsl:template match="/*">
    <xsl:copy>
      <xsl:for-each-group group-by="(position() - 1) idiv 200"
			  select="//*[@id and key('clips', @id, collection()[/d:audio-clips])]">
	<xsl:variable name="smilfile" select="concat($mo-dir-rel, 'mo', position(), '.smil')"/>
	<xsl:for-each select="current-group()">
	  <!-- We take last() to handle cases when audio-order.xsl has moved notes into headings.  -->
	  <!-- It is indeed the toppest ancestor that must be copied first, to mirror the original doc -->
	  <xsl:variable name="ncx-parent" select="ancestor-or-self::*[local-name()=$ncx-linked][last()]"/>
	  <xsl:choose>
	    <xsl:when test="not($ncx-parent)">
	      <xsl:copy>
		<xsl:copy-of select="@* except @smilref"/>
		<xsl:attribute name="smilref">
		  <xsl:value-of select="concat($smilfile, '#s', current()/@id)"/>
		</xsl:attribute>
	      </xsl:copy>
	    </xsl:when>
	    <xsl:otherwise>
	      <!-- hd, h1, h2 etc. must have a @smilref because the NCX must refer to them and
	           yet they can't arbitrarily refer to one of their children because headings
	           can be composed of multiple sentence children. Therefore they must represent
	           themselves in the SMIL files as <seq> elements.-->
	      <!-- Warning: this script won't work well with pagenums inside headings! -->
	      <xsl:variable name="children"
	      		    select="$ncx-parent/descendant::*[@id and (key('clips', @id, collection()[/d:audio-clips]) or local-name()=$ncx-linked)]"/>
	      <xsl:if test="not($children) or $children[1] is current()">
		<!-- This way of dealing with NCX parents is slow. We could do a lot better. -->
		<xsl:apply-templates select="$ncx-parent" mode="in-same-smil">
		  <xsl:with-param name="smilfile" select="$smilfile"/>
		  <xsl:with-param name="to-be-copied" select="$ncx-parent|$children"/>
		</xsl:apply-templates>
	      </xsl:if>
	    </xsl:otherwise>
	  </xsl:choose>
	</xsl:for-each>
      </xsl:for-each-group>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="*" mode="in-same-smil">
    <xsl:param name="to-be-copied"/>
    <xsl:param name="smilfile"/>
    <xsl:choose>
      <xsl:when test="count($to-be-copied intersect .) = 0">
    	<xsl:apply-templates select="*" mode="in-same-smil">
    	  <xsl:with-param name="smilfile" select="$smilfile"/>
    	  <xsl:with-param name="to-be-copied" select="$to-be-copied"/>
    	</xsl:apply-templates>
      </xsl:when>
      <xsl:otherwise>
    	<xsl:copy>
    	  <xsl:copy-of select="@* except @smilref"/>
    	  <xsl:attribute name="smilref">
    	    <xsl:value-of select="concat($smilfile, '#s', @id)"/>
    	  </xsl:attribute>
    	  <xsl:apply-templates select="*" mode="in-same-smil">
    	    <xsl:with-param name="smilfile" select="$smilfile"/>
    	    <xsl:with-param name="to-be-copied" select="$to-be-copied"/>
    	  </xsl:apply-templates>
    	</xsl:copy>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
