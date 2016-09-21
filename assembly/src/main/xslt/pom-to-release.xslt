<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pom="http://maven.apache.org/POM/4.0.0" 
                version="2.0"
                exclude-result-prefixes="#all"
                >
        <xsl:param name="time"/>

        <xsl:template match="/">
                <xsl:variable name="version" select="/pom:project/pom:version/text()"></xsl:variable> 
                <releaseDescriptor href="http://daisy.github.io/pipeline-assembly/releases/{$version}" version="{$version}" time="{$time}">
                        <xsl:apply-templates select="/pom:project/pom:build/pom:plugins/pom:plugin/pom:executions/pom:execution[./pom:id/text()='copy-felix-launcher']/pom:configuration/pom:artifactItems">
                                <xsl:with-param name="deployPath">/system/bootstrap</xsl:with-param>
                        </xsl:apply-templates>
                        <xsl:apply-templates select="/pom:project/pom:build/pom:plugins/pom:plugin/pom:executions/pom:execution[./pom:id/text()='copy-felix-bundles']/pom:configuration/pom:artifactItems">
                                <xsl:with-param name="deployPath">/system/felix</xsl:with-param>
                        </xsl:apply-templates>
                        <xsl:apply-templates select="/pom:project/pom:build/pom:plugins/pom:plugin/pom:executions/pom:execution[./pom:id/text()='copy-libs-bundles']/pom:configuration/pom:artifactItems">
                                <xsl:with-param name="deployPath">/system/framework</xsl:with-param>
                        </xsl:apply-templates>                        
                        <xsl:apply-templates select="/pom:project/pom:build/pom:plugins/pom:plugin/pom:executions/pom:execution[./pom:id/text()='copy-pipeline-bundles']/pom:configuration/pom:artifactItems">
                                <xsl:with-param name="deployPath">/system/framework</xsl:with-param>
                        </xsl:apply-templates>                        
                        <xsl:apply-templates select="/pom:project/pom:build/pom:plugins/pom:plugin/pom:executions/pom:execution[./pom:id/text()='copy-frontend']/pom:configuration/pom:artifactItems">
                                <xsl:with-param name="deployPath">/system/frontend</xsl:with-param>
                        </xsl:apply-templates>                        
                        <xsl:apply-templates select="/pom:project/pom:build/pom:plugins/pom:plugin/pom:executions/pom:execution[./pom:id/text()='copy-pipeline-modules']/pom:configuration/pom:artifactItems">
                                <xsl:with-param name="deployPath">/modules</xsl:with-param>
                        </xsl:apply-templates>
                        <xsl:apply-templates mode="zip" select="/pom:project/pom:profiles/pom:profile/pom:build/pom:plugins/pom:plugin/pom:executions/pom:execution[./pom:id/text()='unpack-cli-win']/pom:configuration/pom:artifactItems/pom:artifactItem">
                                <xsl:with-param name="deployPath">/cli</xsl:with-param>
                                <xsl:with-param name="classifier">windows_386</xsl:with-param>
                        </xsl:apply-templates>                        
                </releaseDescriptor>
        </xsl:template>
        <xsl:template match="pom:artifactItem">
                <xsl:param name="deployPath" />
                <xsl:variable name="artifactId" select="./pom:artifactId/text()"/>
            
                <xsl:variable name="groupId" select="./pom:groupId/text()"/>
                <xsl:variable name="version">
                        <xsl:choose >
                                <xsl:when test="./pom:version/text()!=''"><xsl:value-of select="./pom:version/text()"></xsl:value-of></xsl:when>
                                <xsl:when test="/pom:project/pom:dependencyManagement/pom:dependencies/pom:dependency[./pom:artifactId/text()=$artifactId and ./pom:groupId/text()=$groupId]/pom:version/text()!=''">
                                        <xsl:value-of select="/pom:project/pom:dependencyManagement/pom:dependencies/pom:dependency[./pom:artifactId/text()=$artifactId and ./pom:groupId/text()=$groupId]/pom:version/text()"></xsl:value-of>
                                </xsl:when>
                                <xsl:when test="/pom:project/pom:dependencies/pom:dependency[./pom:artifactId/text()=$artifactId and ./pom:groupId/text()=$groupId]/pom:version/text()!=''">
                                        <xsl:value-of select="/pom:project/pom:dependencies/pom:dependency[./pom:artifactId/text()=$artifactId and ./pom:groupId/text()=$groupId]/pom:version/text()"></xsl:value-of>
                                </xsl:when>                         
                        </xsl:choose >
                        
                </xsl:variable> 
                <xsl:variable name="repository">
                        <xsl:choose >
                                <xsl:when test="contains($artifactId,'restlet')">http://maven.restlet.com/</xsl:when>
                                <xsl:when test="contains($artifactId,'mysql')">http://repository.springsource.com/maven/bundles/external/</xsl:when>
                                <xsl:when test="$groupId='org.eclipse.gemini'">http://download.eclipse.org/gemini/dbaccess/mvn/</xsl:when>
                                <xsl:when test="$groupId='org.eclipse.persistence'">http://download.eclipse.org/rt/eclipselink/maven.repo/</xsl:when>
                                <xsl:when test="$groupId='org.eclipse'">http://download.eclipse.org/gemini/mvn/</xsl:when>
                                
                                <xsl:otherwise>http://central.maven.org/maven2/</xsl:otherwise>
                        </xsl:choose>
                        
                </xsl:variable>
                <xsl:variable name="href" select="concat($repository, replace($groupId,'\.','/'),
                        '/',$artifactId,'/',$version,'/',$artifactId,'-',$version,'.jar')"></xsl:variable>
                 
                <xsl:variable name="id" select="string-join(($groupId,$artifactId),'/')"/>

                <xsl:variable name="finalPath" select="concat($deployPath,'/',$groupId,'.',$artifactId,'-',$version,'.jar')"></xsl:variable>
                <artifact href="{$href}" id="{$id}" extract="false" deployPath="{$finalPath}" version="{$version}"/>
        </xsl:template>
        <xsl:template match="pom:artifactItem" mode="zip">
                <xsl:param name="deployPath" />
                <xsl:param name="classifier" />
                <xsl:variable name="artifactId" select="./pom:artifactId/text()"/>
                <xsl:variable name="groupId" select="./pom:groupId/text()"/>
                <xsl:variable name="version" select="./pom:version/text()"/> 
                
                <xsl:variable name="href" select="concat('http://search.maven.org/remotecontent?filepath=', replace($groupId,'\.','/'),
                        '/',$artifactId,'/',$version,'/',$artifactId,'-',$version,'-',$classifier,'.zip')"></xsl:variable>
                
                <xsl:variable name="finalPath" select="concat($deployPath,'/',$groupId,'.',$artifactId,'-',$version,'-',$classifier,'.zip')"></xsl:variable>
                <xsl:variable name="id" select="string-join(($groupId,$artifactId),'/')"/>
                <artifact href="{$href}" id="{$id}" extract="true" deployPath="{$finalPath}" version="{$version}"/>
        </xsl:template>
</xsl:stylesheet>

