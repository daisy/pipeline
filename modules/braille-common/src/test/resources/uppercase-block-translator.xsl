<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:s="org.daisy.pipeline.braille.css.xpath.Style"
                xmlns:t="org.daisy.pipeline.braille.css.xpath.StyledText"
                exclude-result-prefixes="#all">

	<xsl:import href="../../main/resources/xml/abstract-block-translator.xsl"/>

	<xsl:template match="css:block">
		<xsl:variable name="text" as="item()*">
			<xsl:apply-templates mode="text-items"/>
		</xsl:variable>
		<xsl:variable name="uppercase-text" as="item()*">
			<xsl:for-each select="$text">
				<xsl:variable name="text" as="xs:string" select="t:getText(.)"/>
				<xsl:variable name="style" as="item()?" select="t:getStyle(.)"/>
				<xsl:choose>
					<xsl:when test="$text='busstopp' and s:get($style,'hyphens')[string(.)='auto']">
						<xsl:sequence select="."/>
					</xsl:when>
					<xsl:when test="s:get($style,'text-transform')[string(.)='none']">
						<xsl:choose>
							<xsl:when test="not(s:get($style,'braille-charset')[not(string(.)='unicode')])">
								<xsl:sequence select="t:of(
								                        translate($text,'⠁⠃⠉⠙⠑⠋⠛⠓⠊⠚⠅⠇⠍⠝⠕⠏⠟⠗⠎⠞⠥⠧⠺⠭⠽⠵','ABCDEFGHIJKLMNOPQRSTUVWXYZ'),
								                        s:merge(($style,$BRAILLE_CHARSET_DECLARATION)))"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:sequence select="."/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:when>
					<xsl:otherwise>
						<xsl:variable name="uppercase" as="xs:string" select="upper-case($text)"/>
						<xsl:variable name="normalised" as="xs:string"
						              select="if (s:get($style,'white-space')[not(string(.)='normal')])
						                      then $uppercase
						                      else normalize-space($uppercase)"/>
						<xsl:variable name="hyphenated" as="xs:string"
						              select="if (s:get($style,'hyphens')[string(.)='auto'])
						                      then replace($normalised, 'FOOBAR', 'FOO=BAR')
						                      else $normalised"/>
						<xsl:sequence select="t:of(
						                        $hyphenated,
						                        s:merge((s:remove($style,'hyphens'),
						                                 $TEXT_TRANSFORM_NONE,
						                                 $BRAILLE_CHARSET_DECLARATION)))"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
		</xsl:variable>
		<xsl:apply-templates select="node()[1]" mode="treewalk">
			<xsl:with-param name="new-text-nodes" select="$uppercase-text"/>
		</xsl:apply-templates>
	</xsl:template>

</xsl:stylesheet>
