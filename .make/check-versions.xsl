<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pom="http://maven.apache.org/POM/4.0.0"
                xpath-default-namespace="http://maven.apache.org/POM/4.0.0">
	
	<xsl:output method="text"/>
	
	<xsl:param name="modules" required="yes"/>
	
	<xsl:variable name="development-versions" select="/*/dependency"/>
	<xsl:variable name="effective-pom" select="document('../.effective-pom.xml')/*"/>
	
	<xsl:template match="/">
		<xsl:for-each select="for $p in tokenize($modules,'\s+')
		                      return document(concat('../',$p,'/pom.xml'))/project">
			<xsl:variable name="project" select="."/>
			<xsl:variable name="groupId" as="xs:string" select="(groupId,parent/groupId)[1]"/>
			<xsl:variable name="artifactId" as="xs:string" select="artifactId"/>
			<xsl:for-each select="parent|
			                      dependencies/dependency[version]|
			                      dependencyManagement/dependencies/dependency">
				<xsl:if test="not(exists(
				                (preceding-sibling::node() except preceding-sibling::*[1]/preceding-sibling::node())
				                [self::processing-instruction('fixed-dependency')]))">
					<xsl:variable name="development-version" as="element()?"
					              select="$development-versions[string(artifactId)=string(current()/artifactId)
					                                            and string(groupId)=string(current()/groupId)]"/>
					<xsl:if test="exists($development-version)">
						<xsl:variable name="version" as="xs:string">
							<xsl:choose>
								<xsl:when test="not(matches(version,'.*\$\{[^\}]+\}.*'))">
									<xsl:sequence select="version"/>
								</xsl:when>
								<!--
								    substitute properties
								-->
								<xsl:otherwise>
									<xsl:variable name="properties" as="xs:string*">
										<xsl:analyze-string regex="\$\{{([^\}}]+)\}}" select="version">
											<xsl:matching-substring>
												<xsl:sequence select="regex-group(1)"/>
											</xsl:matching-substring>
										</xsl:analyze-string>
									</xsl:variable>
									<xsl:choose>
										<xsl:when test="every $p in properties satisfies $project/properties/*[local-name()=$p]">
											<xsl:variable name="substituted" as="xs:string*">
												<xsl:analyze-string regex="\$\{{([^\}}]+)\}}" select="version">
													<xsl:matching-substring>
														<xsl:variable name="p" as="xs:string" select="regex-group(1)"/>
														<xsl:sequence select="string($project/properties/*[local-name()=$p])"/>
													</xsl:matching-substring>
													<xsl:non-matching-substring>
														<xsl:sequence select="."/>
													</xsl:non-matching-substring>
												</xsl:analyze-string>
											</xsl:variable>
											<xsl:sequence select="string-join($substituted,'')"/>
										</xsl:when>
										<xsl:when test="not(scope='import')">
											<!--
											    read version in effective pom where properties have been substituted
											-->
											<xsl:sequence select="$effective-pom/project[groupId=$groupId and artifactId=$artifactId]
									                                    /(parent|
									                                      dependencies/dependency|
									                                      dependencyManagement/dependencies/dependency)
									                                    [groupId=current()/groupId and artifactId=current()/artifactId][1]
									                                    /version"/>
										</xsl:when>
										<xsl:otherwise>
											<xsl:message terminate="yes"/>
										</xsl:otherwise>
									</xsl:choose>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:variable>
						<xsl:if test="not(string($development-version/version)=$version)">
							<xsl:text>Dependency </xsl:text>
							<xsl:value-of select="groupId"/>
							<xsl:text>:</xsl:text>
							<xsl:value-of select="artifactId"/>
							<xsl:text> of </xsl:text>
							<xsl:value-of select="$groupId"/>
							<xsl:text>:</xsl:text>
							<xsl:value-of select="$artifactId"/>
							<xsl:text> does not match development version: </xsl:text>
							<xsl:value-of select="$version"/>
							<xsl:text> &lt;-&gt; </xsl:text>
							<xsl:value-of select="$development-version/version"/>
							<xsl:text>&#x0A;</xsl:text>
						</xsl:if>
					</xsl:if>
				</xsl:if>
			</xsl:for-each>
		</xsl:for-each>
	</xsl:template>
	
</xsl:stylesheet>
