<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:map="http://www.w3.org/2005/xpath-functions/map"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:s="org.daisy.pipeline.braille.css.xpath.Style"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                exclude-result-prefixes="#all"
                version="3.0">
	
	<xsl:param name="attribute-name" required="yes"/>
	
	<xsl:include href="library.xsl"/>
	
	<xsl:template match="/">
		<xsl:apply-templates select="/*"/>
		<xsl:result-document href="irrelevant">
			<c:result content-type="text/plain">
				<xsl:variable name="id-to-style" as="map(xs:string,xs:string)">
					<xsl:map>
						<xsl:for-each select="//*/@*[local-name()=$attribute-name and namespace-uri()='']">
							<xsl:variable name="id" select="(parent::*/@id/string(.),parent::*/@xml:id/string(.),parent::*/generate-id(.))[1]"/>
							<xsl:map-entry key="$id" select="string(.)"/>
						</xsl:for-each>
					</xsl:map>
				</xsl:variable>
				<xsl:variable name="selector-to-style" as="map(xs:string,item())">
					<xsl:iterate select="map:keys($id-to-style)">
						<xsl:param name="styles" as="map(xs:string,item())" select="map{}"/>
						<xsl:param name="selectors" as="map(xs:string,xs:string*)" select="map{}"/>
						<xsl:on-completion>
							<xsl:map>
								<xsl:for-each select="map:keys($styles)">
									<xsl:variable name="style" as="item()" select="map:get($styles,.)"/>
									<xsl:variable name="selectors" as="xs:string*" select="map:get($selectors,.)"/>
									<xsl:map-entry key="string-join($selectors,', ')" select="$style"/>
								</xsl:for-each>
							</xsl:map>
						</xsl:on-completion>
						<xsl:variable name="selector" as="xs:string" select="concat('#',.)"/>
						<xsl:variable name="style" as="xs:string" select="map:get($id-to-style,.)"/>
						<xsl:variable name="selectors" as="map(xs:string,xs:string*)"
						              select="map:put($selectors,$style,(map:get($selectors,$style),$selector))"/>
						<xsl:choose>
							<xsl:when test="map:contains($styles,$style)">
								<xsl:next-iteration>
									<xsl:with-param name="selectors" select="$selectors"/>
								</xsl:next-iteration>
							</xsl:when>
							<xsl:otherwise>
								<xsl:variable name="serialized" as="xs:string" select="$style"/>
								<xsl:variable name="style" as="item()?" select="css:parse-stylesheet($style)"/>
								<xsl:choose>
									<xsl:when test="empty($style)">
										<xsl:next-iteration>
											<xsl:with-param name="selectors" select="$selectors"/>
										</xsl:next-iteration>
									</xsl:when>
									<xsl:otherwise>
										<xsl:next-iteration>
											<xsl:with-param name="styles" select="map:put($styles,$serialized,$style)"/>
											<xsl:with-param name="selectors" select="$selectors"/>
										</xsl:next-iteration>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:iterate>
				</xsl:variable>
				<xsl:variable name="page-styles" as="map(xs:string,item())">
					<xsl:map>
						<xsl:for-each select="map:keys($selector-to-style)">
							<xsl:variable name="style" as="item()" select="map:get($selector-to-style,.)"/>
							<xsl:call-template name="extract-page-styles">
								<xsl:with-param name="style" select="$style"/>
							</xsl:call-template>
						</xsl:for-each>
					</xsl:map>
				</xsl:variable>
				<xsl:variable name="page-style-names" as="map(xs:string,xs:string)">
					<xsl:map>
						<xsl:for-each select="map:keys($page-styles)">
							<xsl:variable name="style" as="xs:string" select="."/>
							<xsl:variable name="name" as="xs:string" select="concat('page-',position())"/>
							<xsl:map-entry key="$style" select="$name"/>
						</xsl:for-each>
					</xsl:map>
				</xsl:variable>
				<xsl:variable name="style" as="item()*">
					<xsl:for-each select="map:keys($page-styles)">
						<xsl:sequence select="s:of(concat('@page ',map:get($page-style-names,.)),
						                           map:get($page-styles,.))"/>
					</xsl:for-each>
					<xsl:for-each select="map:keys($selector-to-style)">
						<xsl:variable name="style" as="item()">
							<xsl:call-template name="substitute-named-pages">
								<xsl:with-param name="style" select="map:get($selector-to-style,.)"/>
								<xsl:with-param name="page-style-names" tunnel="yes" select="$page-style-names"/>
							</xsl:call-template>
						</xsl:variable>
						<xsl:sequence select="s:of(.,$style)"/>
					</xsl:for-each>
				</xsl:variable>
				<xsl:value-of select="css:serialize-stylesheet-pretty($style,'&#x9;')"/>
				<xsl:text>&#xa;</xsl:text>
			</c:result>
		</xsl:result-document>
	</xsl:template>
	
	<xsl:template name="extract-page-styles" as="map(xs:string,item())">
		<xsl:param name="style" as="item()" required="true"/>
		<xsl:map>
			<xsl:for-each select="s:iterate($style)">
				<xsl:variable name="selector" as="xs:string?" select="s:selector(.)"/>
				<xsl:if test="$selector">
					<xsl:variable name="style" as="item()" select="s:get(.,$selector)"/>
					<xsl:choose>
						<xsl:when test="$selector='@page'">
							<xsl:variable name="serialized" as="xs:string" select="string(.)"/>
							<xsl:map-entry key="$serialized" select="$style"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:call-template name="extract-page-styles">
								<xsl:with-param name="style" select="$style"/>
							</xsl:call-template>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:if>
			</xsl:for-each>
		</xsl:map>
	</xsl:template>
	
	<xsl:template name="substitute-named-pages" as="item()">
		<xsl:param name="style" as="item()" required="true"/>
		<xsl:param name="page-style-names" tunnel="yes" as="map(xs:string,xs:string)" required="true"/>
		<xsl:variable name="style" as="item()*">
			<xsl:for-each select="s:iterate($style)">
				<xsl:variable name="selector" as="xs:string?" select="s:selector(.)"/>
				<xsl:choose>
					<xsl:when test="$selector">
						<xsl:choose>
							<xsl:when test="$selector='@page'">
								<xsl:variable name="serialized" as="xs:string" select="string(.)"/>
								<xsl:sequence select="css:parse-stylesheet(concat('page: ',map:get($page-style-names,$serialized)))"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:variable name="style" as="item()">
									<xsl:call-template name="substitute-named-pages">
										<xsl:with-param name="style" select="s:get(.,$selector)"/>
									</xsl:call-template>
								</xsl:variable>
								<xsl:sequence select="s:of($selector,$style)"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:when>
					<xsl:otherwise>
						<xsl:sequence select="."/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
		</xsl:variable>
		<xsl:sequence select="s:merge($style)"/>
	</xsl:template>
	
	<xsl:template match="*[not(@id)][@*[local-name()=$attribute-name and namespace-uri()='']]">
		<xsl:copy>
			<xsl:attribute name="id" select="(@xml:id/string(.),generate-id(.))[1]"/>
			<!--
			    XML elements may only have a single attribute of type ID
			-->
			<xsl:apply-templates mode="#current" select="@* except @xml:id"/>
			<xsl:apply-templates mode="#current"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="@*|node()" mode="#default substitute-named-pages">
		<xsl:copy>
			<xsl:apply-templates mode="#current" select="@*|node()"/>
		</xsl:copy>
	</xsl:template>
	
</xsl:stylesheet>
