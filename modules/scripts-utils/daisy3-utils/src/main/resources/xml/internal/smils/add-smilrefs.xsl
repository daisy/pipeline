<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:d="http://www.daisy.org/ns/pipeline/data"
		xmlns:dt="http://www.daisy.org/z3986/2005/dtbook/"
		xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
		exclude-result-prefixes="#all" version="2.0">

  <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>

  <!-- This script adds @smilref attributes to the input document, but
       not all the nodes and attributes are copied, because the output
       document is used only as a lightweight structure. The script
       copy-smilref.xsl can be called to get the full original
       document with the @smilrefs. -->

  <xsl:param name="no-smilref"/>
  <xsl:param name="mo-dir"/>
  <xsl:param name="output-dir"/>

  <xsl:variable name="mo-dir-rel" select="pf:relativize-uri($mo-dir, $output-dir)"/>

  <xsl:key name="struct" match="*[@node]" use="@node"/>
  <xsl:key name="clips" match="*[@idref]" use="@idref"/>

  <!-- This variable maps levels to numbers. They will be used for
       determining the smil names.-->
  <xsl:variable name="top-elements">
    <d:structs>
      <!-- Note: this might create position not referred anywhere,
           which should be harmless. -->
      <xsl:for-each select="//*[starts-with(local-name(), 'level')]">
	<d:s pos="{position()}" node="{generate-id(current())}"/>
      </xsl:for-each>
    </d:structs>
  </xsl:variable>

  <xsl:template match="node()|@*" priority="1">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="*[@id and not(contains($no-smilref, concat(' ', local-name(), ' ')))]"
		priority="4">
    <xsl:copy>
      <xsl:variable name="prev" select="preceding::*[key('struct', generate-id(), $top-elements)][1]"/>
      <xsl:variable name="smil-id">
	<xsl:choose>
	  <xsl:when test="$prev">
	    <xsl:value-of select="key('struct', generate-id($prev), $top-elements)/@pos"/>
	  </xsl:when>
	  <xsl:otherwise>
	    <xsl:value-of select="'0'"/>
	  </xsl:otherwise>
	</xsl:choose>
      </xsl:variable>
      <xsl:attribute name="smilref">
	<xsl:value-of select="concat($mo-dir-rel, 'mo', $smil-id, '.smil#s', @id)"/>
      </xsl:attribute>
      <xsl:choose>
	<xsl:when test="key('clips', @id, collection()[/d:audio-clips]) or count(dt:w) > 0">
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

</xsl:stylesheet>
