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
	
	<!-- Generate automatic-styles for:
	     * paragraphs and spans with a @xml:lang attribute
	     * tables
	     Other automatic-styles are taken care of by LibreOffice or MS Word:
		 * for paragraphs inside a list, automatic styles are generated that inherit from the specified style
		 * for image frames, automatic styles are generated that inherit from the specified style
		 * section styles are renamed Sect1, Sect2, etc.
		 * section styles that are not declared yet are generated
		 * ...
	-->
	<xsl:variable name="automatic-language-styles" as="element()*">
		<xsl:call-template name="automatic-language-styles">
			<xsl:with-param name="elements" as="element()*">
				<xsl:for-each-group select="//*[self::text:p or self::text:h or self::text:span or self::text:a][@xml:lang]" group-by="@xml:lang">
					<xsl:for-each-group select="current-group()" group-by="string(@text:style-name)">
						<xsl:for-each-group select="current-group()" group-by="style:family(.)">
							<xsl:sequence select="current-group()[1]"/>
						</xsl:for-each-group>
					</xsl:for-each-group>
				</xsl:for-each-group>
			</xsl:with-param>
			<xsl:with-param name="existing-styles" select="//office:automatic-styles/style:style[@style:family=('paragraph','text')]"/>
		</xsl:call-template>
	</xsl:variable>
	
	<xsl:template match="/">
		<xsl:apply-templates select="/*"/>
	</xsl:template>
	
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="/office:document-content/office:automatic-styles">
		<xsl:copy>
			<xsl:apply-templates/>
			<xsl:for-each select="$automatic-language-styles">
				<xsl:sequence select="style:style"/>
			</xsl:for-each>
			<xsl:call-template name="automatic-table-styles"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="text:p|text:h|text:span|text:a">
		<xsl:choose>
			<xsl:when test="@xml:lang">
				<xsl:variable name="parent-style-name" select="string(@text:style-name)"/>
				<xsl:variable name="family" select="style:family(.)"/>
				<xsl:variable name="lang" select="@xml:lang"/>
				<xsl:copy>
					<xsl:apply-templates select="@*[not(name(.)='xml:lang')]"/>
					<xsl:attribute name="text:style-name"
					               select="$automatic-language-styles[@xml:lang=$lang]
					                         /style:style[@style:family=$family and string(@style:parent-style-name)=$parent-style-name]
					                         /@style:name"/>
					<xsl:apply-templates select="node()"/>
				</xsl:copy>
			</xsl:when>
			<xsl:otherwise>
				<xsl:next-match/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="table:table">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:attribute name="table:style-name" select="style:name(@table:name)"/>
			<xsl:apply-templates select="node()"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="table:table-column">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:attribute name="table:style-name" select="concat(style:name(parent::table:table/@table:name), '.A')"/>
			<xsl:apply-templates select="node()"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template name="automatic-language-styles" as="element()*">
		<xsl:param name="elements" as="element()*"/>
		<xsl:param name="existing-styles" as="element()*"/>
		<xsl:if test="exists($elements)">
			<xsl:variable name="element" select="$elements[1]"/>
			<xsl:variable name="family" select="style:family($element)"/>
			<xsl:variable name="style" as="element()?">
				<xsl:element name="language-style">
					<xsl:attribute name="xml:lang" select="$element/@xml:lang"/>
					<xsl:element name="style:style">
						<xsl:attribute name="style:name">
							<xsl:call-template name="generate-automatic-style-name">
								<xsl:with-param name="existing-style-names" select="$existing-styles/style:style[@style:family=$family]/@style:name"/>
								<xsl:with-param name="prefix" select="if ($family='paragraph') then 'P' else 'T'"/>
							</xsl:call-template>
						</xsl:attribute>
						<xsl:attribute name="style:family" select="$family"/>
						<xsl:if test="$element/@text:style-name">
							<xsl:attribute name="style:parent-style-name" select="$element/@text:style-name"/>
						</xsl:if>
						<xsl:element name="style:text-properties">
							<xsl:call-template name="language-properties">
								<xsl:with-param name="lang" select="$element/@xml:lang"/>
							</xsl:call-template>
						</xsl:element>
					</xsl:element>
				</xsl:element>
			</xsl:variable>
			<xsl:sequence select="$style"/>
			<xsl:call-template name="automatic-language-styles">
				<xsl:with-param name="elements" select="$elements[position() &gt; 1]"/>
				<xsl:with-param name="existing-styles" select="($existing-styles, $style)"/>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="automatic-table-styles">
		<xsl:for-each select="//table:table">
			<xsl:element name="style:style">
				<xsl:attribute name="style:name" select="style:name(@table:name)"/>
				<xsl:attribute name="style:display-name" select="@table:name"/>
				<xsl:attribute name="style:family" select="'table'"/>
			</xsl:element>
			<xsl:element name="style:style">
				<xsl:attribute name="style:name" select="concat(style:name(@table:name), '.A')"/>
				<xsl:attribute name="style:display-name" select="concat(@table:name, '.A')"/>
				<xsl:attribute name="style:family" select="'table-column'"/>
			</xsl:element>
		</xsl:for-each>
	</xsl:template>
	
</xsl:stylesheet>
