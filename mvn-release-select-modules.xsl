<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pom="http://maven.apache.org/POM/4.0.0"
                xmlns="http://maven.apache.org/POM/4.0.0"
                xpath-default-namespace="http://maven.apache.org/POM/4.0.0"
                exclude-result-prefixes="pom">
	
	<xsl:param name="ENABLED_MODULES"/>
	<xsl:param name="OUTPUT_FILENAME"/>
	
	<xsl:variable name="BASE" select="base-uri(/*)"/>
	
	<xsl:variable name="enabled-modules" select="tokenize($ENABLED_MODULES, '\s+')"/>
	
	<xsl:output method="xml"/>
	
	<xsl:template match="/">
		<xsl:result-document href="{$OUTPUT_FILENAME}" method="xml">
			<xsl:text>&#x0A;</xsl:text>
			<xsl:apply-templates select="*">
				<xsl:with-param name="module" tunnel="yes" select="'.'"/>
			</xsl:apply-templates>
		</xsl:result-document>
	</xsl:template>
	
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="/project/modules/module">
		<xsl:param name="module" tunnel="yes" required="yes"/>
		<xsl:variable name="submodule" select="string-join((
		                                         if ($module='.') then () else $module,
		                                         string(.)),'/')"/>
		<xsl:variable name="submodule-pom" select="document(resolve-uri(concat($submodule,'/pom.xml'),$BASE))"/>
		<xsl:choose>
			<xsl:when test="$submodule-pom/project/modules/module">
				<xsl:next-match/>
				<xsl:result-document href="{concat($submodule,'/',$OUTPUT_FILENAME)}" method="xml">
					<xsl:text>&#x0A;</xsl:text>
					<xsl:apply-templates select="$submodule-pom/*">
						<xsl:with-param name="module" tunnel="yes" select="$submodule"/>
					</xsl:apply-templates>
				</xsl:result-document>
			</xsl:when>
			<xsl:when test="$submodule=$enabled-modules">
				<xsl:next-match/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:comment>
					<xsl:text>&lt;module&gt;</xsl:text>
					<xsl:value-of select="string(.)"/>
					<xsl:text>&lt;/module&gt;</xsl:text>
				</xsl:comment>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
</xsl:stylesheet>
