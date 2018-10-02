<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:d="http://www.daisy.org/ns/pipeline/data"
		xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
		xmlns:xs="http://www.w3.org/2001/XMLSchema"
		xmlns="http://openebook.org/namespaces/oeb-package/1.0/"
		exclude-result-prefixes="xsl d pf xs" version="2.0">

  <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>


  <!-- input: the fileset -->
  <!-- output: the the opf file -->

  <xsl:param name="output-dir"/>
  <xsl:param name="title"/>
  <xsl:param name="uid"/>
  <xsl:param name="total-time"/>
  <xsl:param name="lang"/>
  <xsl:param name="publisher"/>
  <xsl:param name="audio-only"/>
  <xsl:param name="mathml-xslt-fallback" select="''"/>

  <xsl:template match="/">
    <xsl:variable name="has-audio" select="boolean(//d:file[contains(@media-type, 'audio')][1])"/>
    <xsl:variable name="has-image" select="boolean(//d:file[contains(@media-type, 'image')][1])"/>

    <package unique-identifier="uid">
      <metadata>
	<dc-metadata xmlns:oebpackage="http://openebook.org/namespaces/oeb-package/1.0/" xmlns:dc="http://purl.org/dc/elements/1.1/">
	  <dc:Format>ANSI/NISO Z39.86-2005</dc:Format>
	  <dc:Language><xsl:value-of select="$lang"/></dc:Language>
	  <dc:Date><xsl:value-of select="format-date(current-date(), '[Y0001]-[M01]-[D01]')"/></dc:Date>
	  <dc:Publisher><xsl:value-of select="$publisher"/></dc:Publisher>
	  <dc:Title><xsl:value-of select="$title"/></dc:Title>
	  <dc:Identifier id="uid"><xsl:value-of select="$uid"/></dc:Identifier>
	</dc-metadata>
	<x-metadata>
	  <meta name="dtb:multimediaType"
		content="{if ($audio-only='true') then 'audioOnly' else
			 (if ($has-audio) then 'audioFullText' else 'textNCX')}"/>
	  <meta name="dtb:totalTime" content="{$total-time}"/>
	  <meta name="dtb:multimediaContent"
		content="{concat(
			 if ($audio-only='true') then 'audio' else (if ($has-audio) then 'audio,text' else 'text'),
			 if ($has-image) then ',image' else '')}"/>
	  <xsl:if test="$mathml-xslt-fallback != ''">
	    <meta name="z39-86-extension-version"
		  scheme="http://www.w3.org/1998/Math/MathML"
		  content="1.0" />
	    <meta name="DTBook-XSLTFallback"
		  scheme="http://www.w3.org/1998/Math/MathML"
		  content="{pf:relativize-uri($mathml-xslt-fallback, $output-dir)}" />
	  </xsl:if>
	</x-metadata>
      </metadata>
      <manifest>
	<xsl:call-template name="manifest"/>
      </manifest>
      <spine>
	<xsl:call-template name="spine"/>
      </spine>
    </package>
  </xsl:template>

  <xsl:function name="d:getIdRef">
    <xsl:param name="s"/>
    <xsl:value-of select="substring-before(tokenize($s, '[/\\]')[last()], '.')"/>
  </xsl:function>

  <xsl:template name="manifest">
    <xsl:for-each select="//d:file">
      <xsl:variable name="id">
	<xsl:choose>
	  <xsl:when test="contains(@media-type, 'smil')">
	    <xsl:value-of select="d:getIdRef(@href)"/>
	  </xsl:when>
	  <xsl:when test="contains(@media-type, 'ncx')">
	    <xsl:value-of select="'ncx'"/>
	  </xsl:when>
	  <xsl:when test="contains(@media-type, 'res')">
	    <xsl:value-of select="'resource'"/>
	  </xsl:when>
	  <xsl:otherwise>
	    <xsl:value-of select="concat('opf-', position())"/>
	  </xsl:otherwise>
	</xsl:choose>
      </xsl:variable>
      <item href="{pf:relativize-uri(resolve-uri(@href, base-uri(.)), $output-dir)}"
	    id="{$id}" media-type="{@media-type}"/>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="spine">
    <xsl:for-each select="//d:file[contains(@media-type, 'smil')]">
      <itemref idref="{d:getIdRef(@href)}"/>
    </xsl:for-each>
  </xsl:template>

</xsl:stylesheet>
