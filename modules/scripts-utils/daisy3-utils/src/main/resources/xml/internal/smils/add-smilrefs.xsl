<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
                exclude-result-prefixes="#all">

  <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>

  <!-- This script adds @smilref attributes to the input document, but
       not all the nodes and attributes are copied, because the output
       document is used only as a lightweight structure. The script
       copy-smilref.xsl can be called to get the full original
       document with the @smilrefs. -->

  <xsl:param name="mo-dir"/>

  <xsl:variable name="mo-dir-rel" select="pf:relativize-uri($mo-dir, base-uri(/*))"/>

  <xsl:key name="struct" match="*[@node]" use="@node"/>
  <xsl:key name="clips" match="d:clip" use="@textref"/>

  <xsl:variable name="top-elements" select="//dtb:level|
					    //dtb:level1|
					    //dtb:level2|
					    //dtb:level3|
					    //dtb:level4|
					    //dtb:level5|
					    //dtb:level6"/>

  <!-- This variable maps levels to numbers. They will be used for
       determining the smil names.-->
  <xsl:variable name="top-element-positions">
    <d:structs>
      <!-- Note: this might create position not referred anywhere,
           which should be harmless. -->
      <xsl:for-each select="$top-elements">
	<d:s node="{generate-id(.)}" pos="{position()}"/>
      </xsl:for-each>
    </d:structs>
  </xsl:variable>

  <xsl:template match="*[@id and empty(@pxi:no-smilref)]">
    <xsl:copy>
      <xsl:variable name="prev" select="preceding::*[self::dtb:level|
					             self::dtb:level1|
					             self::dtb:level2|
					             self::dtb:level3|
					             self::dtb:level4|
					             self::dtb:level5|
					             self::dtb:level6][1]"/>
      <xsl:variable name="smil-id" select="if ($prev)
					   then key('struct',generate-id($prev),$top-element-positions)/@pos
					   else '0'"/>
      <xsl:attribute name="smilref">
	<xsl:value-of select="concat($mo-dir-rel, 'mo', $smil-id, '.smil#s', @id)"/>
      </xsl:attribute>
      <xsl:choose>
	<xsl:when test="exists(child::dtb:w) or key('clips',concat('#',@id),collection()[/d:audio-clips])">
	  <!-- note: if the clip contains a skippable structure, it won't pass the
	       validation because every skippable structure must hold a @smilref -->
	  <xsl:copy-of select="@*|node()"/> <!-- children are not allowed to hold a @smilref -->
	</xsl:when>
	<xsl:otherwise>
	  <xsl:apply-templates select="@*|node()"/>
	</xsl:otherwise>
      </xsl:choose>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="node()|@*">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
