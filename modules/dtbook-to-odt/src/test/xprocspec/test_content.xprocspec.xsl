<?xml version="1.0" encoding="utf-8" standalone="yes"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:x="http://www.daisy.org/ns/xprocspec"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0"
                xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0"
                xmlns:table="urn:oasis:names:tc:opendocument:xmlns:table:1.0"
                xmlns:draw="urn:oasis:names:tc:opendocument:xmlns:drawing:1.0"
                xmlns:svg="urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:math="http://www.w3.org/1998/Math/MathML"
                xmlns="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="#all"
                version="2.0">
	
	<xsl:include href="serialize.xsl"/>
	
	<xsl:output method="html" indent="no"/>
	
	<xsl:template match="/x:description">
		<html>
			<head>
				<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
				<link rel="stylesheet" type="text/css" href="../test_content.xprocspec.css"/>
			</head>
			<body>
				<xsl:apply-templates select="x:scenario"/>
			</body>
		</html>
	</xsl:template>
	
	<xsl:template match="x:scenarion[@pending='true']" priority="1"/>
	
	<xsl:template match="x:scenario">
		<xsl:if test="parent::x:scenario">
			<xsl:element name="h{count(ancestor-or-self::x:scenario)}">
				<xsl:value-of select="@label"/>
			</xsl:element>
			<xsl:sequence select="x:documentation/child::node()"/>
		</xsl:if>
		<xsl:variable name="input" select="x:call/x:input[@port='source']/x:document[@type='inline']"/>
		<xsl:if test="$input">
			<div class="xml-document">
				<div class="code">
					<xsl:variable name="indent" select="((if ($input/wrapper) then $input/wrapper/node() else $input/node())[1]
					                                     /self::text()/string-length(tokenize(if (matches(.,'^\s'))
					                                                                            then replace(.,'^(\s+).*$','$1')
					                                                                            else '',
					                                                                          '\n')[last()]),
					                                     0)[1]"/>
					<xsl:for-each select="if ($input/wrapper) then $input/wrapper/* else $input/*">
						<xsl:apply-templates mode="serialize" select=".">
							<xsl:with-param name="indent" tunnel="yes" select="- $indent"/>
						</xsl:apply-templates>
						<xsl:text>&#x0A;</xsl:text>
					</xsl:for-each>
				</div>
			</div>
		</xsl:if>
		<xsl:variable name="output" select="x:expect/x:document[@type='inline']"/>
		<xsl:if test="$output">
			<div class="odt-document">
				<xsl:apply-templates mode="htmlize-odt"
				                     select="$output/(node()[not(self::wrapper)]|wrapper/node())"/>
				<xsl:if test="$output//text:note[@text:note-class='footnote']">
					<div class="footnotes">
						<xsl:apply-templates mode="htmlize-odt-footnotes"
						                     select="$output//text:note[@text:note-class='footnote']"/>
					</div>
				</xsl:if>
			</div>
		</xsl:if>
		<xsl:apply-templates select="x:scenario"/>
	</xsl:template>
	
	<xsl:template mode="htmlize-odt" match="text:*|draw:*|svg:*|table:*">
		<div>
			<xsl:call-template name="htmlize-odt-classes"/>
			<xsl:apply-templates mode="#current" select="@*|node()"/>
		</div>
	</xsl:template>
	
	<xsl:template mode="htmlize-odt" match="text:span">
		<span>
			<xsl:call-template name="htmlize-odt-classes"/>
			<xsl:apply-templates mode="#current" select="@*|node()"/>
		</span>
	</xsl:template>
	
	<xsl:template mode="htmlize-odt" match="table:table">
		<table>
			<xsl:call-template name="htmlize-odt-classes"/>
			<xsl:apply-templates mode="#current" select="@*|node()"/>
		</table>
	</xsl:template>
	
	<xsl:template mode="htmlize-odt" match="table:table-header-rows">
		<thead>
			<xsl:call-template name="htmlize-odt-classes"/>
			<xsl:apply-templates mode="#current" select="@*|node()"/>
		</thead>
	</xsl:template>
	
	<xsl:template mode="htmlize-odt" match="table:table-row">
		<tr>
			<xsl:call-template name="htmlize-odt-classes"/>
			<xsl:apply-templates mode="#current" select="@*|node()"/>
		</tr>
	</xsl:template>
	
	<xsl:template mode="htmlize-odt" match="table:table-cell">
		<td>
			<xsl:call-template name="htmlize-odt-classes"/>
			<xsl:apply-templates mode="#current" select="@*|node()"/>
		</td>
	</xsl:template>
	
	<xsl:template mode="htmlize-odt" match="table:table-cell/@table:number-columns-spanned">
		<xsl:attribute name="colspan" select="."/>
	</xsl:template>
	
	<xsl:template mode="htmlize-odt" match="table:table-cell/@table:number-rows-spanned">
		<xsl:attribute name="rowspan" select="."/>
	</xsl:template>
	
	<xsl:template mode="htmlize-odt" match="text:note[@text:note-class='footnote']">
		<span class="footnote-ref"/>
	</xsl:template>
	
	<xsl:template mode="htmlize-odt" match="draw:frame[draw:image]">
		<img src="../{draw:image/@xlink:href}" title="{svg:title}" style="width: {@svg:width}"/>
	</xsl:template>
	
	<xsl:template mode="htmlize-odt" match="text:s">
		<span>
			<xsl:call-template name="htmlize-odt-classes"/>
			<xsl:value-of select="string-join(for $i in 1 to xs:integer(number((@text:c,'1')[1])) return ' ','')"/>
		</span>
	</xsl:template>
	
	<xsl:template mode="htmlize-odt" match="math:*">
		<xsl:sequence select="."/>
	</xsl:template>
	
	<xsl:template mode="htmlize-odt" match="@*"/>
	
	<xsl:template mode="htmlize-odt" match="text()">
		<xsl:value-of select="."/>
	</xsl:template>
	
	<xsl:template name="htmlize-odt-classes">
		<xsl:variable name="classes" as="xs:string*">
			<xsl:apply-templates mode="htmlize-odt-classes" select=".|@*"/>
		</xsl:variable>
		<xsl:if test="exists($classes)">
			<xsl:attribute name="class" select="string-join($classes,' ')"/>
		</xsl:if>
	</xsl:template>
	
	<xsl:template mode="htmlize-odt-classes" match="text:*">
		<xsl:sequence select="concat('text-',local-name(.))"/>
	</xsl:template>
	
	<xsl:template mode="htmlize-odt-classes" match="draw:*">
		<xsl:sequence select="concat('draw-',local-name(.))"/>
	</xsl:template>
	
	<xsl:template mode="htmlize-odt-classes" match="svg:*">
		<xsl:sequence select="concat('svg-',local-name(.))"/>
	</xsl:template>
	
	<xsl:template mode="htmlize-odt-classes" match="table:*">
		<xsl:sequence select="concat('table-',local-name(.))"/>
	</xsl:template>
	
	<xsl:template mode="htmlize-odt-classes" match="@office:*">
		<xsl:sequence select="concat('office-',local-name(.),'-',string(.))"/>
	</xsl:template>
	
	<xsl:template mode="htmlize-odt-classes" match="@text:*">
		<xsl:sequence select="concat('text-',local-name(.),'-',string(.))"/>
	</xsl:template>
	
	<xsl:template mode="htmlize-odt-classes" match="@draw:*">
		<xsl:sequence select="concat('draw-',local-name(.),'-',string(.))"/>
	</xsl:template>
	
	<xsl:template mode="htmlize-odt-classes" match="@svg:*">
		<xsl:sequence select="concat('svg-',local-name(.),'-',string(.))"/>
	</xsl:template>
	
	<xsl:template mode="htmlize-odt-classes" match="@xlink:*">
		<xsl:sequence select="concat('xlink-',local-name(.),'-',string(.))"/>
	</xsl:template>
	
	<xsl:template mode="htmlize-odt-classes" match="@table:*">
		<xsl:sequence select="concat('table-',local-name(.),'-',string(.))"/>
	</xsl:template>
	
	<xsl:template mode="htmlize-odt-footnotes" match="text:note[@text:note-class='footnote']">
		<div>
			<xsl:call-template name="htmlize-odt-classes"/>
			<xsl:apply-templates mode="htmlize-odt" select="@*|node()"/>
		</div>
	</xsl:template>
	
</xsl:stylesheet>
