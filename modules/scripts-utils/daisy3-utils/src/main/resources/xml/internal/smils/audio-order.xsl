<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                exclude-result-prefixes="#all">

  <xsl:variable name="link-nodes" select="('noteref', 'annoref')"/>
  <xsl:variable name="target-nodes" select="('note', 'annotation')"/>

  <xsl:key name="normalized-links" match="*[@idref]" use="substring-after(@idref, '#')"/>
  <xsl:key name="links" match="*[@idref]" use="@idref"/>
  <xsl:key name="targets" match="*[@id]" use="@id"/>

  <xsl:template match="/" priority="5">
    <d:audio-order>
      <xsl:apply-templates select="*"/>
    </d:audio-order>
  </xsl:template>

  <xsl:template match="*" priority="1">
    <xsl:apply-templates select="*"/>
  </xsl:template>

  <xsl:template match="*[@id or starts-with(local-name(), 'level')]" priority="2">
    <xsl:copy>
      <xsl:copy-of select="@id|@pxi:no-smilref"/>
      <xsl:apply-templates select="*"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="*[$link-nodes = local-name()]" priority="3">
    <xsl:copy>
      <xsl:copy-of select="@id|@idref|@pxi:no-smilref"/>
      <xsl:apply-templates select="*"/>
    </xsl:copy>

    <!-- TODO: do not create the target here every time. If the link is inside an audio
         clip -which should not happen- it is safer to move the target after the clip as
         to make sure the target can hold a @smilref -->
    <xsl:if test="key('links', @idref)[1]/@id = @id">
      <!-- Add the note/annotation only if it is the first noteref/annoref that points -->
      <!-- to the note/annotation. -->
      <xsl:variable name="target" select="key('targets', substring-after(@idref, '#'))"/>
      <xsl:element name="{local-name($target)}" namespace="{namespace-uri($target)}">
	<xsl:copy-of select="$target/(@id|@pxi:no-smilref)"/>
	<xsl:apply-templates select="$target/*"/>
      </xsl:element>
    </xsl:if>
  </xsl:template>

  <xsl:template match="*[$target-nodes = local-name()]" priority="3">
    <!-- The note/annotation is added only if it is not associated to any noteref/annoref -->
    <xsl:if test="not(key('normalized-links', @id))">
      <xsl:copy>
	<xsl:copy-of select="@id|@pxi:no-smilref"/>
	<xsl:apply-templates select="*"/>
      </xsl:copy>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
