<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pom="http://maven.apache.org/POM/4.0.0"
                xmlns="http://maven.apache.org/POM/4.0.0"
                xpath-default-namespace="http://maven.apache.org/POM/4.0.0"
                exclude-result-prefixes="pom">

	<xsl:param name="unchanged-deps" as="xs:string*" required="yes"/>
	<xsl:param name="optimized-modules" as="xs:string*" required="yes"/>
	<xsl:param name="relative-path-to-original-dir" as="xs:string" required="yes"/>

	<xsl:template match="/*">
		<xsl:choose>
			<xsl:when test="tokenize($relative-path-to-original-dir,'/')[last()]='bom'">
				<xsl:apply-templates mode="bom" select="."/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates mode="aggregator" select="."/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template mode="bom aggregator #default" match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates mode="#current" select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template mode="bom" match="/project/dependencyManagement/dependencies/dependency/version[ends-with(.,'-SNAPSHOT')]">
		<xsl:choose>
			<xsl:when test="$unchanged-deps=concat(../groupId/string(.),':',../artifactId/string(.))">
				<xsl:variable name="version" select="tokenize(replace(string(),'-SNAPSHOT$',''),'\.')"/>
				<xsl:variable name="patch-number" select="$version[last()]"/>
				<xsl:choose>
					<xsl:when test="$patch-number='0'">
						<!-- a major or minor version update was done, which means it was no automatic bump -->
						<xsl:next-match/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:variable name="patch-number" select="xs:integer(number($patch-number))"/>
						<xsl:copy>
							<xsl:value-of select="string-join(($version[position()&lt;last()],format-number($patch-number - 1,'0')),'.')"/>
						</xsl:copy>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:next-match/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template mode="aggregator" match="/project/modules/module">
		<xsl:choose>
			<xsl:when test="string(.)=$optimized-modules">
				<!-- assume that relative path stays the same -->
				<xsl:next-match/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy>
					<xsl:value-of select="concat($relative-path-to-original-dir,'/',string(.))"/>
				</xsl:copy>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

</xsl:stylesheet>
