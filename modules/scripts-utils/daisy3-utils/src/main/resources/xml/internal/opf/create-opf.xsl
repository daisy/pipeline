<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
                xmlns="http://openebook.org/namespaces/oeb-package/1.0/"
                exclude-result-prefixes="xsl d pf xs c dc dtb">

  <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>

  <!--
      input:
       - the fileset
       - Dublin Core metadata (c:param-set)
       - DTBook
       output: the opf file
  -->

  <xsl:param name="output-base-uri"/>
  <xsl:param name="uid"/>
  <xsl:param name="total-time"/>

  <xsl:template match="/">
    <xsl:variable name="has-audio" as="xs:boolean" select="exists(//d:file[contains(@media-type, 'audio')])"/>
    <xsl:variable name="has-image" as="xs:boolean" select="exists(//d:file[contains(@media-type, 'image')])"/>
    <xsl:variable name="audio-only" as="xs:boolean" select="not(exists(//d:file[@media-type='application/x-dtbook+xml']))"/>

    <xsl:variable name="dtbook" as="document-node(element(dtb:dtbook))?" select="collection()[3]"/>

    <xsl:variable name="dc-metadata-from-params" as="element(c:param)*"
                  select="collection()[2]//c:param[@namespace='http://purl.org/dc/elements/1.1/'][@value[not(.='')]]"/>
    <xsl:variable name="dc-metadata-from-dtbook" as="element(dtb:meta)*"
                  select="$dtbook//dtb:meta[matches(@name,'^dc:')]"/>
    <xsl:variable name="dc-metadata" as="element()*"> <!-- element(dc:*)* -->
      <xsl:for-each select="distinct-values(($dc-metadata-from-params/@name/lower-case(.),
                                             $dc-metadata-from-dtbook/@name/lower-case(substring(.,4))))">
        <xsl:element name="dc:{concat(upper-case(substring(.,1,1)),lower-case(substring(.,2)))}">
          <xsl:variable name="name" select="."/>
          <xsl:value-of select="($dc-metadata-from-params[lower-case(@name)=lower-case($name)]/@value,
                                 $dc-metadata-from-dtbook[lower-case(substring(@name,4))=lower-case($name)]/@content)[1]"/>
        </xsl:element>
      </xsl:for-each>
    </xsl:variable>

    <package unique-identifier="uid">
      <metadata>
	<dc-metadata xmlns:oebpackage="http://openebook.org/namespaces/oeb-package/1.0/" xmlns:dc="http://purl.org/dc/elements/1.1/">
	  <dc:Format>ANSI/NISO Z39.86-2005</dc:Format>
          <!--
              Main language of the content of the publication.
              Defaults to first xml:lang attribute of DTBook, or "und" (undetermined).
          -->
          <xsl:choose>
            <xsl:when test="exists($dc-metadata/self::dc:Language)">
              <xsl:sequence select="$dc-metadata/self::dc:Language"/>
            </xsl:when>
            <xsl:otherwise>
              <dc:Language><xsl:value-of select="($dtbook//@xml:lang,'und')[1]"/></dc:Language>
            </xsl:otherwise>
          </xsl:choose>
          <!--
              Date of publication of the DTB.
              Defaults to the current date.
          -->
          <xsl:choose>
            <xsl:when test="exists($dc-metadata/self::dc:Date)">
              <xsl:sequence select="$dc-metadata/self::dc:Date"/>
            </xsl:when>
            <xsl:otherwise>
              <dc:Date><xsl:value-of select="format-date(current-date(), '[Y0001]-[M01]-[D01]')"/></dc:Date>
            </xsl:otherwise>
          </xsl:choose>
          <!--
              The agency responsible for making the DTB available.
              Defaults to "unknown"
          -->
          <xsl:choose>
            <xsl:when test="exists($dc-metadata/self::dc:Publisher)">
              <xsl:sequence select="$dc-metadata/self::dc:Publisher"/>
            </xsl:when>
            <xsl:otherwise>
              <dc:Publisher>unknown</dc:Publisher>
            </xsl:otherwise>
          </xsl:choose>
          <!--
              The title of the DTB, including any subtitles.
              Defaults to "unknown"
          -->
          <xsl:choose>
            <xsl:when test="exists($dc-metadata/self::dc:Title)">
              <xsl:for-each select="$dc-metadata/self::dc:Title">
                <xsl:copy>
                  <xsl:sequence select="@*"/>
                  <xsl:value-of select="normalize-space(string(.))"/>
                </xsl:copy>
              </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
              <dc:Title>unknown</dc:Title>
            </xsl:otherwise>
          </xsl:choose>
          <!--
              Globally unique identifier of the DTB.
          -->
	  <dc:Identifier id="uid"><xsl:value-of select="$uid"/></dc:Identifier>
          <xsl:sequence select="$dc-metadata[not(self::dc:Format|
                                                 self::dc:Language|
                                                 self::dc:Date|
                                                 self::dc:Publisher|
                                                 self::dc:Title|
                                                 self::dc:Identifier)]"/>
	</dc-metadata>
	<x-metadata>
	  <meta name="dtb:multimediaType"
		content="{if ($audio-only) then 'audioOnly' else
			 (if ($has-audio) then 'audioFullText' else 'textNCX')}"/>
	  <meta name="dtb:totalTime" content="{$total-time}"/>
	  <meta name="dtb:multimediaContent"
		content="{concat(
			 if ($audio-only) then 'audio' else (if ($has-audio) then 'audio,text' else 'text'),
			 if ($has-image) then ',image' else '')}"/>
	  <xsl:if test="//d:file[@role='mathml-xslt-fallback']">
	    <meta name="z39-86-extension-version"
		  scheme="http://www.w3.org/1998/Math/MathML"
		  content="1.0" />
	    <meta name="DTBook-XSLTFallback"
		  scheme="http://www.w3.org/1998/Math/MathML"
		  content="{pf:relativize-uri(//d:file[@role='mathml-xslt-fallback']
                                      /resolve-uri(@href,base-uri(.)), $output-base-uri)}" />
	  </xsl:if>
	</x-metadata>
      </metadata>
      <manifest>
	<!-- list OPF itself -->
	<item href="{pf:relativize-uri($output-base-uri, $output-base-uri)}" id="opf" media-type="text/xml"/>
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
      <item href="{pf:relativize-uri(resolve-uri(@href, base-uri(.)), $output-base-uri)}"
	    id="{$id}" media-type="{@media-type}"/>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="spine">
    <xsl:for-each select="//d:file[contains(@media-type, 'smil')]">
      <itemref idref="{d:getIdRef(@href)}"/>
    </xsl:for-each>
  </xsl:template>

</xsl:stylesheet>
