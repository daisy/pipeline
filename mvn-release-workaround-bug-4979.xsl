<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pom="http://maven.apache.org/POM/4.0.0"
                xmlns:fn="my-functions"
                xmlns="http://maven.apache.org/POM/4.0.0"
                xpath-default-namespace="http://maven.apache.org/POM/4.0.0"
                exclude-result-prefixes="pom fn">
	
	<!--
	    This is a workaround for the fact that Maven does not allow you to override a configuration
	    parameter from the command line. See this bug report: https://issues.apache.org/jira/browse/MNG-4979.
	    
	    This XSLT adds the following configuration for the maven-release-plugin to the POM (or
	    replaces the existing configuration):
	    
	    <configuration>
	      <goals>${goals}</goals>
	      <arguments>${arguments}</arguments>
	    </configuration>
	    
	    which makes it possible to override the configuration with -Dgoals=... -Darguments=...
	-->
	
	<xsl:output method="xml" indent="no"/>
	
	<xsl:variable name="has-maven-release-plugin" select="//plugin[artifactId='maven-release-plugin']"/>
	
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="/">
		<xsl:text>&#x0A;</xsl:text>
		<xsl:apply-templates/>
	</xsl:template>
	
	<xsl:template match="/project">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
			<xsl:if test="not(build)">
				<xsl:value-of select="fn:indent(1)"/>
				<xsl:variable name="empty-build">
					<build>
						<xsl:text>&#x0A;</xsl:text>
						<xsl:value-of select="fn:indent(1)"/>
					</build>
				</xsl:variable>
				<xsl:apply-templates select="$empty-build"/>
				<xsl:text>&#x0A;</xsl:text>
			</xsl:if>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="build">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
			<xsl:if test="not($has-maven-release-plugin) and not(pluginManagement)">
				<xsl:value-of select="fn:indent(1)"/>
				<xsl:variable name="empty-pluginManagement" as="element()">
					<pluginManagement>
						<xsl:text>&#x0A;</xsl:text>
						<xsl:value-of select="fn:indent(3)"/>
						<plugins>
							<xsl:text>&#x0A;</xsl:text>
							<xsl:value-of select="fn:indent(3)"/>
						</plugins>
						<xsl:text>&#x0A;</xsl:text>
						<xsl:value-of select="fn:indent(2)"/>
					</pluginManagement>
				</xsl:variable>
				<xsl:apply-templates select="$empty-pluginManagement"/>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:value-of select="fn:indent(1)"/>
			</xsl:if>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="pluginManagement/plugins">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
			<xsl:if test="not($has-maven-release-plugin)">
				<xsl:value-of select="fn:indent(1)"/>
				<plugin>
					<xsl:text>&#x0A;</xsl:text>
					<xsl:value-of select="fn:indent(5)"/>
					<groupId>org.apache.maven.plugins</groupId>
					<xsl:text>&#x0A;</xsl:text>
					<xsl:value-of select="fn:indent(5)"/>
					<artifactId>maven-release-plugin</artifactId>
					<xsl:text>&#x0A;</xsl:text>
					<xsl:value-of select="fn:indent(5)"/>
					<version>2.5.3</version>
					<xsl:text>&#x0A;</xsl:text>
					<xsl:value-of select="fn:indent(5)"/>
					<configuration>
						<xsl:text>&#x0A;</xsl:text>
						<xsl:value-of select="fn:indent(6)"/>
						<goals>${goals}</goals>
						<xsl:text>&#x0A;</xsl:text>
						<xsl:value-of select="fn:indent(6)"/>
						<arguments>${arguments}</arguments>
						<xsl:text>&#x0A;</xsl:text>
						<xsl:value-of select="fn:indent(5)"/>
					</configuration>
					<xsl:text>&#x0A;</xsl:text>
					<xsl:value-of select="fn:indent(4)"/>
				</plugin>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:value-of select="fn:indent(3)"/>
			</xsl:if>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="plugin[artifactId='maven-release-plugin']">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
			<xsl:if test="not(configuration)">
				<xsl:value-of select="fn:indent(1)"/>
				<configuration>
					<xsl:text>&#x0A;</xsl:text>
					<xsl:value-of select="fn:indent(count(ancestor::*) + 2)"/>
					<goals>${goals}</goals>
					<xsl:text>&#x0A;</xsl:text>
					<xsl:value-of select="fn:indent(count(ancestor::*) + 2)"/>
					<arguments>${arguments}</arguments>
					<xsl:text>&#x0A;</xsl:text>
					<xsl:value-of select="fn:indent(count(ancestor::*) + 1)"/>
				</configuration>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:value-of select="fn:indent(count(ancestor::*))"/>
			</xsl:if>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="plugin[artifactId='maven-release-plugin']/configuration">
		<xsl:copy>
			<xsl:apply-templates select="@*|(node() except (goals|arguments))"/>
			<xsl:value-of select="fn:indent(1)"/>
			<goals>${goals}</goals>
			<xsl:text>&#x0A;</xsl:text>
			<xsl:value-of select="fn:indent(count(ancestor::*) + 1)"/>
			<arguments>
				<xsl:text>${arguments}</xsl:text>
				<xsl:if test="arguments">
					<xsl:text> </xsl:text>
					<xsl:value-of select="arguments"/>
				</xsl:if>
			</arguments>
			<xsl:text>&#x0A;</xsl:text>
			<xsl:value-of select="fn:indent(count(ancestor::*))"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:variable name="child-indent" select="tokenize((/project/artifactId/preceding-sibling::text())[last()],'\n\r?')[last()]"/>
	
	<xsl:function name="fn:indent">
		<xsl:param name="times"/>
		<xsl:sequence select="fn:repeat($child-indent,$times)"/>
	</xsl:function>
	
	<xsl:function name="fn:repeat">
		<xsl:param name="string"/>
		<xsl:param name="times"/>
		<xsl:sequence select="string-join(for $i in 1 to $times return $string,'')"/>
	</xsl:function>
	
</xsl:stylesheet>
