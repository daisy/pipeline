<?xml version="1.0" encoding="UTF-8"?>
<!-- ======================================================================= -->
<!-- There are 2 copies of this file:                                        -->
<!-- * scripts/dtbook-to-pef/src/main/resources/xml/xslt/volume-breaking.xsl -->
<!-- * scripts/html-to-pef/src/main/resources/xml/xslt/volume-breaking.xsl   -->
<!-- Whenever you update this file, also update the other copies.            -->
<!-- ======================================================================= -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns:f="#"
                exclude-result-prefixes="#all"
                version="2.0">
	
	<xsl:param name="duplex" as="xs:boolean" select="true()"/>
	<xsl:param name="maximum-number-of-sheets" as="xs:integer" select="70"/>
	<xsl:param name="allow-volume-break-inside-leaf-section-factor" as="xs:integer" select="10"/>
	<xsl:param name="prefer-volume-break-before-higher-level-factor" as="xs:double" select="0"/>
	<!--
	    for testing
	-->
	<xsl:param name="minimum-number-of-leaf-section-pages" as="xs:integer" select="3"/>
	
	<xsl:variable name="maximum-number-of-pages" as="xs:integer"
	              select="if ($duplex) then $maximum-number-of-sheets * 2 else $maximum-number-of-sheets"/>
	<xsl:variable name="maximum-number-of-leaf-section-pages" as="xs:integer"
	              select="xs:integer(round($maximum-number-of-pages div 3 + 0.5))"/>
		
	<xsl:template match="/*">
		<xsl:choose>
			<xsl:when test="$allow-volume-break-inside-leaf-section-factor=10">
				<xsl:sequence select="."/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:variable name="leaf-sections" as="element()">
					<xsl:apply-templates mode="leaf-sections" select="."/>
				</xsl:variable>
				<xsl:apply-templates mode="keep-with-next-section" select="$leaf-sections"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template mode="leaf-sections"
	              match="dtb:level1|  html:body|
	                     dtb:level2|  html:section|
	                     dtb:level3|
	                     dtb:level4|
	                     dtb:level5|
	                     dtb:level6">
		<xsl:copy>
			<xsl:apply-templates mode="#current" select="@*"/>
			<xsl:variable name="pages-estimate" as="xs:double" select="f:pages-estimate(.)"/>
			<xsl:choose>
				<xsl:when test="exists(html:section|
				                       dtb:level2|dtb:level3|dtb:level4|dtb:level5|dtb:level6)
				                or $pages-estimate gt $maximum-number-of-leaf-section-pages">
					<xsl:for-each-group select="node()" group-adjacent="self::html:section or
					                                                    self::dtb:level2 or
					                                                    self::dtb:level3 or
					                                                    self::dtb:level4 or
					                                                    self::dtb:level5 or
					                                                    self::dtb:level6">
						<xsl:choose>
							<xsl:when test="current-grouping-key() or not(current-group()[self::*])">
								<xsl:apply-templates mode="#current" select="current-group()"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:call-template name="create-div-leaf-sections">
									<xsl:with-param name="content" select="current-group()"/>
								</xsl:call-template>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:for-each-group>
				</xsl:when>
				<xsl:otherwise>
					<xsl:apply-templates mode="#current" select="node()"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template name="create-div-leaf-sections">
		<xsl:param name="content" as="node()*" required="yes"/>
		<xsl:call-template name="create-div-leaf-section">
			<xsl:with-param name="content" as="node()*" select="$content"/>
			<xsl:with-param name="pages-estimates" as="xs:double*" select="for $n in $content return f:pages-estimate($n)"/>
			<xsl:with-param name="namespace" as="xs:string" select="$content[self::*][1]/namespace-uri()"/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template name="create-div-leaf-section">
		<xsl:param name="content" as="node()*" required="yes"/>
		<xsl:param name="pages-estimates" as="xs:double*" required="yes"/>
		<xsl:param name="namespace" as="xs:string" required="yes"/>
		<xsl:choose>
			<xsl:when test="sum($pages-estimates) le $maximum-number-of-leaf-section-pages">
				<xsl:element name="div" namespace="{$namespace}">
					<xsl:choose>
						<xsl:when test="sum($pages-estimates) le $minimum-number-of-leaf-section-pages">
							<xsl:attribute name="class" select="'leaf-section keep-with-next-section'"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:attribute name="class" select="'leaf-section'"/>
						</xsl:otherwise>
					</xsl:choose>
					<xsl:apply-templates mode="#current" select="$content"/>
				</xsl:element>
			</xsl:when>
			<xsl:otherwise>
				<!-- section too big; recursively split it in half until it is small enough -->
				<xsl:variable name="content-nodes-split-position" as="xs:boolean*">
					<xsl:for-each select="$pages-estimates">
						<xsl:variable name="position" select="position()"/>
						<xsl:value-of select="sum($pages-estimates[position() le $position]) gt sum($pages-estimates) div 2"/>
					</xsl:for-each>
				</xsl:variable>
				<xsl:variable name="content-nodes-split-position" select="count($content-nodes-split-position[.=false()]) + 1"/>
				<xsl:choose>
					<xsl:when test="$content-nodes-split-position=count($pages-estimates)">
						<!--
						    FIXME: can not be split in small enough parts; don't wrap in a leaf section
						-->
						<xsl:apply-templates mode="#current" select="$content"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:call-template name="create-div-leaf-section">
							<xsl:with-param name="content" as="node()*" select="$content[position() le $content-nodes-split-position]"/>
							<xsl:with-param name="pages-estimates" as="xs:double*"
							                select="$pages-estimates[position() le $content-nodes-split-position]"/>
							<xsl:with-param name="namespace" select="$namespace"/>
						</xsl:call-template>
						<xsl:call-template name="create-div-leaf-section">
							<xsl:with-param name="content" as="node()*" select="$content[position() gt $content-nodes-split-position]"/>
							<xsl:with-param name="pages-estimates" as="xs:double*"
							                select="$pages-estimates[position() gt $content-nodes-split-position]"/>
							<xsl:with-param name="namespace" select="$namespace"/>
						</xsl:call-template>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template mode="keep-with-next-section"
	              match="dtb:level1|  html:body|
	                     dtb:level2|  html:section|
	                     dtb:level3|
	                     dtb:level4|
	                     dtb:level5|
	                     dtb:level6|
	                     *[local-name()='div' and f:classes(.)='leaf-section']">
		<xsl:choose>
			<xsl:when test="not(f:classes(.)='keep-with-next-section')">
				<xsl:next-match/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy exclude-result-prefixes="#all">
					<xsl:variable name="result-classes"
					              select="if (local-name()='div') then f:classes(.) else f:classes(.)[not(.='leaf-section')]"/>
					<xsl:choose>
						<xsl:when test="exists(following-sibling::*[self::html:body or
						                                            self::html:section or
						                                            self::dtb:level1 or
						                                            self::dtb:level2 or
						                                            self::dtb:level3 or
						                                            self::dtb:level4 or
						                                            self::dtb:level5 or
						                                            self::dtb:level6 or
						                                            local-name()='div' and f:classes(.)='leaf-section'])">
							<!-- leaf node has another sectioning element or leaf node as following
							     sibling: keep the "keep-with-next-section" class -->
							<xsl:apply-templates mode="#current" select="@* except @class"/>
							<xsl:if test="count($result-classes)">
								<xsl:attribute name="class" select="string-join($result-classes, ' ')"/>
							</xsl:if>
						</xsl:when>
						<xsl:otherwise>
							<!-- leaf node has no following sibling sectioning element or leaf node:
							     remove the "keep-with-next-section" class -->
							<xsl:apply-templates mode="#current" select="@* except @class"/>
							<xsl:variable name="result-classes" select="$result-classes[not(.='keep-with-next-section')]"/>
							<xsl:if test="count($result-classes)">
								<xsl:attribute name="class" select="string-join($result-classes,' ')"/>
							</xsl:if>
						</xsl:otherwise>
					</xsl:choose>
					<xsl:apply-templates mode="#current" select="node()"/>
				</xsl:copy>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template mode="leaf-sections
	                    keep-with-next-section"
	              match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates mode="#current" select="@*|node()"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:function name="f:classes" as="xs:string*">
		<xsl:param name="element" as="element()"/>
		<xsl:sequence select="tokenize($element/@class,'\s+')"/>
	</xsl:function>
	
	<xsl:function name="f:pages-estimate" as="xs:double">
		<xsl:param name="element" as="node()*"/>
		<xsl:value-of select="string-length(normalize-space(string-join($element//text(),' '))) div 650"/>
	</xsl:function>
	
</xsl:stylesheet>
