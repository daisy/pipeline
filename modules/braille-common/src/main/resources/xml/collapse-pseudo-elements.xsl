<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="3.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:s="org.daisy.pipeline.braille.css.xpath.Style"
                exclude-result-prefixes="#all">

	<xsl:import href="http://www.daisy.org/pipeline/modules/braille/css-utils/library.xsl"/>

	<xsl:template match="*[css:before|css:after]">
		<xsl:copy>
			<xsl:sequence select="@* except @style"/>
			<xsl:variable name="style" as="item()?">
				<xsl:call-template name="collapse-pseudo-elements"/>
			</xsl:variable>
			<xsl:sequence select="css:style-attribute($style)"/>
			<xsl:apply-templates select="node() except (css:before|css:after)"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template name="collapse-pseudo-elements" as="item()?">
		<xsl:variable name="style" as="item()?" select="css:parse-stylesheet(@style)"/>
		<xsl:variable name="content" as="item()*">
			<xsl:if test="self::css:before|self::css:after">
				<xsl:for-each-group select="text()|* except (css:before|css:after)"
				                    group-adjacent="(@style/string(s:remove(css:parse-stylesheet(.),'content')),'')[1]">
					<xsl:variable name="content" as="item()*">
						<xsl:for-each-group select="current-group()"
						                    group-adjacent="boolean(exists(@style/s:get(css:parse-stylesheet(.),'content')))">
							<xsl:choose>
								<xsl:when test="current-grouping-key()">
									<xsl:sequence select="current-group()/s:get(css:parse-stylesheet(@style),'content')"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:sequence select="string-join(current-group()/string(.),'')"/>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:for-each-group>
					</xsl:variable>
					<xsl:variable name="content" as="item()?" select="s:merge($content)"/>
					<xsl:sequence select="s:put(@style/s:remove(css:parse-stylesheet(.),'content'),'content',$content)"/>
				</xsl:for-each-group>
			</xsl:if>
		</xsl:variable>
		<xsl:variable name="before-style" as="item()?">
			<xsl:for-each select="css:before">
				<xsl:call-template name="collapse-pseudo-elements"/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="style" as="item()?"
		              select="if (exists($before-style)) then s:put($style,'&amp;::before',$before-style) else $style"/>
		<xsl:variable name="style" as="item()?" select="s:merge(($style,$content[1]))"/>
		<xsl:variable name="after-style" as="item()*">
			<xsl:sequence select="$content[position()&gt;1]"/>
			<xsl:for-each select="css:after">
				<xsl:call-template name="collapse-pseudo-elements"/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="after-style" as="item()?">
			<!-- nest pseudo-elements if needed -->
			<xsl:iterate select="reverse($after-style)">
				<xsl:param name="nested" as="item()?" select="()"/>
				<xsl:on-completion>
					<xsl:sequence select="$nested"/>
				</xsl:on-completion>
				<xsl:variable name="style" as="item()" select="."/>
				<xsl:next-iteration>
					<xsl:with-param name="nested" select="if (exists($nested))
					                                      then s:put($nested,'&amp;::after',$style)
					                                      else $style"/>
				</xsl:next-iteration>
			</xsl:iterate>
		</xsl:variable>
		<xsl:variable name="style" as="item()?"
		              select="if (exists($after-style)) then s:put($style,'&amp;::after',$after-style) else $style"/>
		<xsl:sequence select="$style"/>
	</xsl:template>

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>
