<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:s="org.daisy.pipeline.braille.css.xpath.Style"
                exclude-result-prefixes="#all">

	<xsl:import href="http://www.daisy.org/pipeline/modules/braille/css-utils/library.xsl"/>

	<xsl:template match="*[@style]">
		<xsl:copy>
			<xsl:sequence select="@* except @style"/>
			<xsl:call-template name="expand-pseudo-elements">
				<xsl:with-param name="style" select="css:parse-stylesheet(@style)"/>
				<xsl:with-param name="content" select="node()"/>
			</xsl:call-template>
		</xsl:copy>
	</xsl:template>

	<xsl:template name="expand-pseudo-elements">
		<xsl:param name="style" as="item()?" required="yes"/>
		<xsl:param name="content" as="node()*" select="()"/>
		<xsl:variable name="before-style" as="item()?" select="s:get($style,'&amp;::before')"/>
		<xsl:variable name="after-style" as="item()?" select="s:get($style,'&amp;::after')"/>
		<xsl:choose>
			<xsl:when test="exists($before-style) or exists($after-style)">
				<xsl:variable name="style" as="item()?" select="s:remove($style,('&amp;::before','&amp;::after'))"/>
				<xsl:sequence select="css:style-attribute($style)"/>
				<xsl:if test="exists($before-style)">
					<css:before>
						<xsl:call-template name="expand-pseudo-elements">
							<xsl:with-param name="style" select="s:remove($before-style,'content')"/>
							<xsl:with-param name="content" as="node()*">
								<xsl:call-template name="expand-content-property">
									<xsl:with-param name="content" select="s:get($before-style,'content')"/>
								</xsl:call-template>
							</xsl:with-param>
						</xsl:call-template>
					</css:before>
				</xsl:if>
				<xsl:apply-templates select="$content"/>
				<xsl:if test="exists($after-style)">
					<css:after>
						<xsl:call-template name="expand-pseudo-elements">
							<xsl:with-param name="style" select="s:remove($after-style,'content')"/>
							<xsl:with-param name="content" as="node()*">
								<xsl:call-template name="expand-content-property">
									<xsl:with-param name="content" select="s:get($after-style,'content')"/>
								</xsl:call-template>
							</xsl:with-param>
						</xsl:call-template>
					</css:after>
				</xsl:if>
			</xsl:when>
			<xsl:otherwise>
				<xsl:sequence select="css:style-attribute($style)"/>
				<xsl:apply-templates select="$content"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="expand-content-property" as="node()*">
		<xsl:param name="content" as="item()?" required="yes"/>
		<xsl:if test="exists($content)">
			<xsl:for-each-group select="s:iterate($content)" group-adjacent=". instance of xs:string">
				<xsl:choose>
					<xsl:when test="current-grouping-key()">
						<xsl:value-of select="string-join(current-group(),'')"/>
					</xsl:when>
					<xsl:otherwise>
						<_>
							<xsl:sequence select="css:style-attribute(s:of('content',s:merge(current-group())))"/>
							<xsl:text> </xsl:text>
						</_>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each-group>
		</xsl:if>
	</xsl:template>

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>
