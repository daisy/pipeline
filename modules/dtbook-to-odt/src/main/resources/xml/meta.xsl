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
	
	<!-- ======== -->
	<!-- TEMPLATE -->
	<!-- ======== -->
	
	<xsl:template match="/">
		<xsl:apply-templates select="collection()[1]/*" mode="template"/>
	</xsl:template>
	
	<xsl:template match="@*|node()" mode="template">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" mode="template"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="/office:document-meta/office:meta" mode="template">
		<xsl:copy>
			<xsl:apply-templates mode="template"/>
			<xsl:call-template name="dtb:creator"/>
			<xsl:call-template name="dtb:date"/>
			<xsl:call-template name="dtb:title"/>
			<xsl:call-template name="dtb:subject"/>
		</xsl:copy>
	</xsl:template>
	
	<!--
	    Merge metadata from template with generated metadata. If there are two meta tags with the
	    same name, only keep the first occurence.
	-->
	
	<xsl:template match="/office:document-meta/office:meta/*" mode="template" priority="1">
		<xsl:variable name="node-name" as="xs:QName" select="node-name(.)"/>
		<xsl:if test="not(preceding-sibling::*[node-name(.)=$node-name])">
			<xsl:copy>
				<xsl:apply-templates select="@*|node()" mode="template"/>
			</xsl:copy>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="/office:document-meta/office:meta/meta:user-defined" mode="template" priority="1.1">
		<xsl:variable name="name" select="string(@meta:name)"/>
		<xsl:if test="not(preceding-sibling::meta:user-defined[@meta:name=$name])">
			<xsl:copy>
				<xsl:apply-templates select="@*|node()" mode="template"/>
			</xsl:copy>
		</xsl:if>
	</xsl:template>
	
	<!-- =============== -->
	<!-- DTBOOK METADATA -->
	<!-- =============== -->
	
	<xsl:template name="dtb:creator">
		<xsl:variable name="creator" select="dtb:meta('dc:creator')"/>
		<xsl:if test="not(dc:creator)">
			<xsl:element name="dc:creator">
				<xsl:sequence select="$creator"/>
			</xsl:element>
		</xsl:if>
		<xsl:if test="not(meta:initial-creator)">
			<xsl:element name="meta:initial-creator">
				<xsl:sequence select="$creator"/>
			</xsl:element>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="dtb:date">
		<xsl:variable name="date" select="dtb:meta('dc:date')"/>
		<xsl:if test="not(dc:date)">
			<xsl:element name="dc:date">
				<xsl:sequence select="$date"/>
			</xsl:element>
		</xsl:if>
		<xsl:if test="not(meta:creation-date)">
			<xsl:element name="meta:creation-date">
				<xsl:sequence select="$date"/>
			</xsl:element>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="dtb:title">
		<xsl:variable name="title" select="dtb:meta('dc:title')"/>
		<!--<xsl:variable name="title" select="string(collection()[2]/dtb:dtbook//dtb:doctitle[1])"/>-->
		<xsl:if test="not(dc:title)">
			<xsl:element name="dc:title">
				<xsl:sequence select="$title"/>
			</xsl:element>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="dtb:subject">
		<xsl:variable name="subject" select="dtb:meta('dc:subject')"/>
		<xsl:if test="not(dc:subject) and $subject!=''">
			<xsl:element name="dc:subject">
				<xsl:sequence select="$subject"/>
			</xsl:element>
		</xsl:if>
	</xsl:template>
	
	<!-- ========= -->
	<!-- UTILITIES -->
	<!-- ========= -->
	
	<xsl:function name="dtb:meta">
		<xsl:param name="name" as="xs:string"/>
		<xsl:sequence select="collection()[2]/dtb:dtbook/dtb:head/dtb:meta[lower-case(@name)=$name]/@content"/>
	</xsl:function>
	
</xsl:stylesheet>
