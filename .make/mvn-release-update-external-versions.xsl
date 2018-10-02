<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pom="http://maven.apache.org/POM/4.0.0"
                xmlns="http://maven.apache.org/POM/4.0.0"
                xpath-default-namespace="http://maven.apache.org/POM/4.0.0"
                exclude-result-prefixes="pom">
	
	<xsl:param name="OUTPUT_FILENAME"/>
	
	<xsl:variable name="BASE" select="base-uri(/*)"/>
	
	<xsl:output method="xml"/>
	
	<xsl:template match="/">
		<xsl:variable name="internal-artifacts" as="element()*">
			<xsl:apply-templates mode="internal-artifacts" select="*">
				<xsl:with-param name="module" tunnel="yes" select="'.'"/>
			</xsl:apply-templates>
		</xsl:variable>
		<xsl:result-document href="{$OUTPUT_FILENAME}" method="xml">
			<xsl:text>&#x0A;</xsl:text>
			<xsl:apply-templates select="*">
				<xsl:with-param name="module" tunnel="yes" select="'.'"/>
				<xsl:with-param name="internal-artifacts" tunnel="yes" select="$internal-artifacts"/>
			</xsl:apply-templates>
		</xsl:result-document>
	</xsl:template>
	
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="/project/dependencyManagement/dependencies/dependency/version[ends-with(.,'-SNAPSHOT')]|
	                     /project/parent/version[ends-with(.,'-SNAPSHOT')]">
		<xsl:param name="internal-artifacts" tunnel="yes"/>
		<xsl:choose>
			<xsl:when test="not($internal-artifacts[string(groupId)=string(current()/parent::*/groupId) and
			                                        string(artifactId)=string(current()/parent::*/artifactId)])">
				<xsl:copy>
					<xsl:value-of select="replace(.,'-SNAPSHOT','')"/>
				</xsl:copy>
			</xsl:when>
			<xsl:otherwise>
				<xsl:next-match/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="/project/modules/module">
		<xsl:param name="module" tunnel="yes" required="yes"/>
		<xsl:variable name="submodule" select="string-join((
		                                         if ($module='.') then () else $module,
		                                         string(.)),'/')"/>
		<xsl:variable name="submodule-pom" select="document(resolve-uri(concat($submodule,'/pom.xml'),$BASE))"/>
		<xsl:result-document href="{concat($submodule,'/',$OUTPUT_FILENAME)}" method="xml">
			<xsl:text>&#x0A;</xsl:text>
			<xsl:apply-templates select="$submodule-pom/*">
				<xsl:with-param name="module" tunnel="yes" select="$submodule"/>
			</xsl:apply-templates>
		</xsl:result-document>
		<xsl:next-match/>
	</xsl:template>
	
	<xsl:template mode="internal-artifacts" match="/project">
		<artifactItem>
			<xsl:sequence select="(groupId,parent/groupId)[1],artifactId,version"/>
		</artifactItem>
		<xsl:apply-templates mode="#current" select="modules/module"/>
	</xsl:template>
	
	<xsl:template mode="internal-artifacts" match="/project/modules/module">
		<xsl:param name="module" tunnel="yes" required="yes"/>
		<xsl:variable name="submodule" select="string-join((
		                                         if ($module='.') then () else $module,
		                                         string(.)),'/')"/>
		<xsl:variable name="submodule-pom" select="document(resolve-uri(concat($submodule,'/pom.xml'),$BASE))"/>
		<xsl:apply-templates mode="#current" select="$submodule-pom/*">
			<xsl:with-param name="module" tunnel="yes" select="$submodule"/>
		</xsl:apply-templates>
	</xsl:template>
	
</xsl:stylesheet>
