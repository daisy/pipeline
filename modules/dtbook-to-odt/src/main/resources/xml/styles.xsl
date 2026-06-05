<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0"
		xmlns:dc="http://purl.org/dc/elements/1.1/"
		xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0"
		xmlns:xforms="http://www.w3.org/2002/xforms"
		xmlns:svg="urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0"
		xmlns:form="urn:oasis:names:tc:opendocument:xmlns:form:1.0"
		xmlns:dom="http://www.w3.org/2001/xml-events"
		xmlns:number="urn:oasis:names:tc:opendocument:xmlns:datastyle:1.0"
		xmlns:table="urn:oasis:names:tc:opendocument:xmlns:table:1.0"
		xmlns:script="urn:oasis:names:tc:opendocument:xmlns:script:1.0"
		xmlns:meta="urn:oasis:names:tc:opendocument:xmlns:meta:1.0"
		xmlns:draw="urn:oasis:names:tc:opendocument:xmlns:drawing:1.0"
		xmlns:math="http://www.w3.org/1998/Math/MathML"
		xmlns:dr3d="urn:oasis:names:tc:opendocument:xmlns:dr3d:1.0"
		xmlns:style="urn:oasis:names:tc:opendocument:xmlns:style:1.0"
		xmlns:xs="http://www.w3.org/2001/XMLSchema"
		xmlns:xlink="http://www.w3.org/1999/xlink"
		xmlns:chart="urn:oasis:names:tc:opendocument:xmlns:chart:1.0"
		xmlns:config="urn:oasis:names:tc:opendocument:xmlns:config:1.0"
		xmlns:fo="urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0"
		xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
		exclude-result-prefixes="#all">
	
	<xsl:include href="utilities.xsl"/>
	
	<!-- ======== -->
	<!-- TEMPLATE -->
	<!-- ======== -->
	
	<xsl:template match="/">
		<xsl:apply-templates select="/*" mode="template"/>
	</xsl:template>
	
	<xsl:template match="@*|node()" mode="template">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" mode="template"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="/office:document-styles/office:styles" mode="template">
		<xsl:copy>
			<xsl:apply-templates mode="template"/>
			<xsl:call-template name="missing-default-style"/>
			<xsl:call-template name="missing-paragraph-styles"/>
			<xsl:call-template name="missing-text-styles"/>
			<xsl:call-template name="missing-list-styles"/>
			<xsl:call-template name="missing-graphic-styles"/>
			<xsl:call-template name="error-styles"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="style:default-style" mode="template">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" mode="template"/>
			<xsl:if test="not(style:text-properties)">
				<xsl:element name="style:text-properties">
					<xsl:call-template name="default-language-properties"/>
				</xsl:element>
			</xsl:if>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="style:default-style/style:text-properties" mode="template">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" mode="template"/>
			<xsl:call-template name="default-language-properties"/>
		</xsl:copy>
	</xsl:template>
	
	<!-- drop all language properties -->
	<xsl:template match="@fo:language|@fo:country|
	                     @style:language-asian|@style:country-asian|
	                     @style:language-complex|@style:country-complex"
	              mode="template"/>
	
	<xsl:variable name="all-styles" select="(collection()[1]//office:styles |
	                                         collection()[1]//office:automatic-styles |
	                                         collection()[2]//office:automatic-styles)"/>
	
	<!-- ============= -->
	<!-- DEFAULT STYLE -->
	<!-- ============= -->
	
	<xsl:template name="missing-default-style">
		<xsl:if test="not(style:default-style[@style:family='paragraph'])">
			<xsl:element name="style:default-style">
				<xsl:attribute name="style:family" select="'paragraph'"/>
				<xsl:element name="style:text-properties">
					<xsl:call-template name="default-language-properties"/>
				</xsl:element>
			</xsl:element>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="default-language-properties">
		<xsl:variable name="lang" select="collection()[3]/dtb:dtbook/@xml:lang"/>
		<xsl:if test="exists($lang)">
			<xsl:call-template name="language-properties">
				<xsl:with-param name="lang" select="$lang"/>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>
	
	<!-- ================ -->
	<!-- PARAGRAPH STYLES -->
	<!-- ================ -->
	
	<xsl:template name="missing-paragraph-styles">
		<xsl:variable name="paragraph_styles" select="$all-styles/style:style[@style:family='paragraph']/@style:name"/>
		<xsl:if test="not('Standard'=$paragraph_styles)">
			<xsl:call-template name="paragraph-style">
				<xsl:with-param name="style:name" select="'Standard'"/>
			</xsl:call-template>
		</xsl:if>
		<xsl:for-each select="distinct-values(collection()[2]//*[self::text:p or self::text:h]/@text:style-name)">
			<xsl:if test="not(.=('Standard', $paragraph_styles))">
				<xsl:call-template name="paragraph-style">
					<xsl:with-param name="style:name" select="."/>
					<xsl:with-param name="style:parent-style-name" select="'Standard'"/>
				</xsl:call-template>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template name="paragraph-style">
		<xsl:param name="style:name" as="xs:string"/>
		<xsl:param name="style:parent-style-name" as="xs:string?"/>
		<xsl:element name="style:style">
			<xsl:attribute name="style:name" select="$style:name"/>
			<xsl:attribute name="style:display-name" select="style:display-name($style:name)"/>
			<xsl:attribute name="style:family" select="'paragraph'"/>
			<xsl:attribute name="style:class" select="'text'"/>
			<xsl:if test="exists($style:parent-style-name)">
				<xsl:attribute name="style:parent-style-name" select="$style:parent-style-name"/>
			</xsl:if>
		</xsl:element>
	</xsl:template>
	
	<!-- =========== -->
	<!-- TEXT STYLES -->
	<!-- =========== -->
	
	<xsl:template name="missing-text-styles">
		<xsl:variable name="text_styles" select="$all-styles/style:style[@style:family='text']/@style:name"/>
		<xsl:for-each select="distinct-values(collection()[2]//*[self::text:span or self::text:a]/@text:style-name)">
			<xsl:if test="not(.=$text_styles)">
				<xsl:call-template name="text-style">
					<xsl:with-param name="style:name" select="."/>
				</xsl:call-template>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template name="text-style">
		<xsl:param name="style:name" as="xs:string"/>
		<xsl:element name="style:style">
			<xsl:attribute name="style:name" select="$style:name"/>
			<xsl:attribute name="style:display-name" select="style:display-name($style:name)"/>
			<xsl:attribute name="style:family" select="'text'"/>
		</xsl:element>
	</xsl:template>
	
	<!-- =========== -->
	<!-- LIST STYLES -->
	<!-- =========== -->
	
	<xsl:template name="missing-list-styles">
		<xsl:variable name="list_styles" select="$all-styles/text:list-style/@style:name"/>
		<xsl:for-each select="distinct-values(collection()[2]//text:list/@text:style-name)">
			<xsl:if test="not(.=$list_styles)">
				<xsl:element name="text:list-style">
					<xsl:attribute name="style:name" select="."/>
					<xsl:attribute name="style:display-name" select="style:display-name(.)"/>
					<xsl:for-each select="1 to 10">
						<xsl:call-template name="text:list-level-style-bullet">
							<xsl:with-param name="text:level" select="."/>
							<xsl:with-param name="fo:margin-left" select=". * 0.1575"/>
							<xsl:with-param name="fo:text-indent" select="-0.1575"/>
						</xsl:call-template>
					</xsl:for-each>
				</xsl:element>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template name="text:list-level-style-bullet">
		<xsl:param name="text:level" as="xs:double"/>
		<xsl:param name="fo:margin-left" as="xs:double"/>
		<xsl:param name="fo:text-indent" as="xs:double"/>
		<xsl:choose>
			<!--
			    MS Word default
			-->
			<xsl:when test="true()">
				<xsl:element name="text:list-level-style-bullet">
					<xsl:attribute name="text:level" select="$text:level"/>
					<xsl:attribute name="text:bullet-char" select="'•'"/>
					<xsl:element name="style:list-level-properties">
						<xsl:attribute name="text:list-level-position-and-space-mode" select="'label-width-and-position'"/>
						<xsl:attribute name="text:min-label-width" select="format-number(max((0, - $fo:text-indent)), '0.0000in')"/>
						<xsl:attribute name="text:min-label-distance" select="'0'"/>
						<xsl:attribute name="text:space-before" select="format-number($fo:margin-left + $fo:text-indent, '0.0000in')"/>
					</xsl:element>
				</xsl:element>
			</xsl:when>
			<!--
			    LibreOffice default
			-->
			<xsl:otherwise>
				<xsl:element name="text:list-level-style-bullet">
					<xsl:attribute name="text:level" select="$text:level"/>
					<xsl:attribute name="text:bullet-char" select="'•'"/>
					<xsl:element name="style:list-level-properties">
						<xsl:attribute name="text:list-level-position-and-space-mode" select="'label-alignment'"/>
						<xsl:element name="style:list-level-label-alignment">
							<xsl:attribute name="text:label-followed-by" select="'listtab'"/>
							<xsl:attribute name="text:list-tab-stop-position" select="format-number($fo:margin-left, '0.0000in')"/>
							<xsl:attribute name="fo:margin-left" select="format-number($fo:margin-left, '0.0000in')"/>
							<xsl:attribute name="fo:text-indent" select="format-number($fo:text-indent, '0.0000in')"/>
						</xsl:element>
					</xsl:element>
					<xsl:element name="style:text-properties">
						<xsl:attribute name="style:font-name" select="'OpenSymbol'"/>
					</xsl:element>
				</xsl:element>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!-- ============== -->
	<!-- GRAPHIC STYLES -->
	<!-- ============== -->
	
	<xsl:template name="missing-graphic-styles">
		<xsl:variable name="graphic_styles" select="$all-styles/style:style[@style:family='graphic']/@style:name"/>
		<xsl:for-each select="distinct-values(collection()[2]//draw:frame/@draw:style-name)">
			<xsl:if test="not(.=$graphic_styles)">
				<xsl:call-template name="graphic-style">
					<xsl:with-param name="style:name" select="."/>
				</xsl:call-template>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template name="graphic-style">
		<xsl:param name="style:name" as="xs:string"/>
		<xsl:param name="style:parent-style-name" as="xs:string?"/>
		<xsl:element name="style:style">
			<xsl:attribute name="style:name" select="$style:name"/>
			<xsl:attribute name="style:display-name" select="style:display-name($style:name)"/>
			<xsl:attribute name="style:family" select="'graphic'"/>
			<xsl:if test="exists($style:parent-style-name)">
				<xsl:attribute name="style:parent-style-name" select="$style:parent-style-name"/>
			</xsl:if>
		</xsl:element>
	</xsl:template>
	
	<!-- ============ -->
	<!-- ERROR STYLES -->
	<!-- ============ -->
	
	<xsl:template name="error-styles">
		<xsl:if test="not($all-styles/style:style[@style:family='paragraph' and @style:name='ERROR']) and collection()[2]//text:p[@text:style-name='ERROR']">
			<xsl:element name="style:style">
				<xsl:attribute name="style:name" select="'ERROR'"/>
				<xsl:attribute name="style:display-name" select="'ERROR'"/>
				<xsl:attribute name="style:family" select="'paragraph'"/>
				<xsl:attribute name="style:class" select="'text'"/>
				<xsl:attribute name="style:parent-style-name" select="'Standard'"/>
				<xsl:element name="style:paragraph-properties">
					<xsl:attribute name="style:border-line-width" select="'0.0035in 0.0138in 0.0035in'"/>
					<xsl:attribute name="fo:padding" select="'0.0201in'"/>
					<xsl:attribute name="fo:border" select="'0.26pt double #ff0000'"/>
					<xsl:attribute name="style:shadow" select="'none'"/>
				</xsl:element>
				<xsl:element name="style:text-properties">
					<xsl:attribute name="fo:color" select="'#ff0000'"/>
					<xsl:attribute name="fo:font-weight" select="'bold'"/>
				</xsl:element>
			</xsl:element>
		</xsl:if>
		<xsl:if test="not($all-styles/style:style[@style:family='text' and @style:name='ERROR']) and collection()[2]//text:span[@text:style-name='ERROR']">
			<xsl:element name="style:style">
				<xsl:attribute name="style:name" select="'ERROR'"/>
				<xsl:attribute name="style:display-name" select="'ERROR'"/>
				<xsl:attribute name="style:family" select="'text'"/>
				<xsl:element name="style:text-properties">
					<xsl:attribute name="fo:color" select="'#ffffff'"/>
					<xsl:attribute name="fo:background-color" select="'#ff0000'"/>
					<xsl:attribute name="fo:font-weight" select="'bold'"/>
				</xsl:element>
			</xsl:element>
		</xsl:if>
	</xsl:template>
	
</xsl:stylesheet>
