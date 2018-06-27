<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:xs="http://www.w3.org/2001/XMLSchema"
        xmlns:pom="http://maven.apache.org/POM/4.0.0"
        version="2.0"
        exclude-result-prefixes="#all"
        >
        <xsl:param name="time"/>
        <xsl:param name="relativeHrefs" select="false()"/>
        
        <!-- don't include unmatched text nodes in the result -->
        <xsl:template match="text()"/>
        
        <xsl:template match="/*">
                <xsl:variable name="version" select="/pom:project/pom:version/text()"/>
                <xsl:variable name="artifacts" as="element()*">
                        <xsl:for-each select="/pom:project/pom:build/pom:plugins/pom:plugin[pom:artifactId='maven-dependency-plugin']/pom:executions/pom:execution[starts-with(pom:id/text(),'copy-')]">
                                <xsl:variable name="deployPath">
                                        <xsl:choose>
                                                <xsl:when test="pom:id = 'copy-felix-launcher'">
                                                        <xsl:value-of select="'system/bootstrap'"/>
                                                </xsl:when>
                                                <xsl:when test="pom:id = 'copy-felix-bundles'">
                                                        <xsl:value-of select="'system/felix'"/>
                                                </xsl:when>
                                                <xsl:when test="pom:id = 'copy-logging-bundles'">
                                                        <xsl:value-of select="'system/felix'"/>
                                                </xsl:when>
                                                <xsl:when test="pom:id = 'copy-framework-libs-bundles'">
                                                        <xsl:value-of select="'system/felix'"/>
                                                </xsl:when>
                                                <xsl:when test="pom:id = 'copy-framework-pipeline-bundles'">
                                                        <xsl:value-of select="'system/felix'"/>
                                                </xsl:when>
                                                <xsl:when test="pom:id = 'copy-webservice-bundles'">
                                                        <xsl:value-of select="'system/frontend'"/>
                                                </xsl:when>
                                                <xsl:when test="pom:id = 'copy-modules-libs-bundles'">
                                                        <xsl:value-of select="'system/felix'"/>
                                                </xsl:when>
                                                <xsl:when test="pom:id = 'copy-modules-pipeline-bundles'">
                                                        <xsl:value-of select="'system/felix'"/>
                                                </xsl:when>
                                                <xsl:when test="pom:id = 'copy-modules-linux-bundles'">
                                                        <xsl:value-of select="'system/felix'"/>
                                                </xsl:when>
                                                <xsl:when test="pom:id = 'copy-modules-mac-bundles'">
                                                        <xsl:value-of select="'system/felix'"/>
                                                </xsl:when>
                                                <xsl:when test="pom:id = 'copy-modules-win-bundles'">
                                                        <xsl:value-of select="'system/felix'"/>
                                                </xsl:when>
                                                <xsl:when test="pom:id = 'copy-persistence-libs-bundles'">
                                                        <xsl:value-of select="'system/framework/persistence'"/>
                                                </xsl:when>
                                                <xsl:when test="pom:id = 'copy-persistence-pipeline-bundles'">
                                                        <xsl:value-of select="'system/framework/persistence'"/>
                                                </xsl:when>
                                                <xsl:when test="pom:id = 'copy-volatile-bundles'">
                                                        <xsl:value-of select="'system/framework/volatile'"/>
                                                </xsl:when>
                                                <xsl:when test="pom:id = 'copy-gui-bundles'">
                                                        <xsl:value-of select="'system/felix'"/>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                        <xsl:message terminate="yes" select="concat('the build plugin maven-dependency-plugin has an an execution without an associated deployPath in ',replace(base-uri(),'^.*/',''),': ',pom:id/text())"/>
                                                </xsl:otherwise>
                                        </xsl:choose>
                                </xsl:variable>
                                <xsl:apply-templates select="pom:configuration/pom:artifactItems">
                                        <xsl:with-param name="deployPath" select="$deployPath"/>
                                </xsl:apply-templates>
                        </xsl:for-each>
                        <xsl:apply-templates mode="zip" select="/pom:project/pom:profiles/pom:profile[pom:id='linux']/pom:build/pom:plugins/pom:plugin/pom:executions/pom:execution[./pom:id/text()='unpack-cli-linux']/pom:configuration/pom:artifactItems/pom:artifactItem">
                                <xsl:with-param name="deployPath">cli</xsl:with-param>
                                <xsl:with-param name="classifier">linux_386</xsl:with-param>
                        </xsl:apply-templates>
                        <xsl:apply-templates mode="zip" select="/pom:project/pom:profiles/pom:profile[pom:id='mac']/pom:build/pom:plugins/pom:plugin/pom:executions/pom:execution[./pom:id/text()='unpack-cli-mac']/pom:configuration/pom:artifactItems/pom:artifactItem">
                                <xsl:with-param name="deployPath">cli</xsl:with-param>
                                <xsl:with-param name="classifier">darwin_386</xsl:with-param>
                        </xsl:apply-templates>
                        <xsl:apply-templates mode="zip" select="/pom:project/pom:profiles/pom:profile[pom:id='win']/pom:build/pom:plugins/pom:plugin/pom:executions/pom:execution[./pom:id/text()='unpack-cli-win']/pom:configuration/pom:artifactItems/pom:artifactItem">
                                <xsl:with-param name="deployPath">cli</xsl:with-param>
                                <xsl:with-param name="classifier">windows_386</xsl:with-param>
                        </xsl:apply-templates>
                </xsl:variable>
                
                <xsl:text>
</xsl:text>
                <releaseDescriptor href="http://daisy.github.io/pipeline-assembly/releases/{$version}" version="{$version}" time="{$time}">
                        <xsl:for-each select="$artifacts">
                                <xsl:sort select="xs:boolean(@extract)"/>
                                <xsl:sort select="@id"/>
                                <xsl:text>
    </xsl:text>
                                <xsl:copy-of select="."/>
                        </xsl:for-each>
                </releaseDescriptor>
                <xsl:text>
</xsl:text>
        </xsl:template>
        <xsl:template match="pom:artifactItem">
                <xsl:param name="deployPath" />
                <xsl:variable name="artifactId" select="./pom:artifactId/text()"/>
                <xsl:variable name="groupId" select="./pom:groupId/text()"/>
                <xsl:variable name="version" select="(
                                                        ./pom:version/text(),
                                                        /pom:project/pom:dependencyManagement/pom:dependencies/pom:dependency[./pom:artifactId/text()=$artifactId and ./pom:groupId/text()=$groupId]/pom:version/text(),
                                                        /pom:project/pom:dependencies/pom:dependency[./pom:artifactId/text()=$artifactId and ./pom:groupId/text()=$groupId]/pom:version/text()
                                                      )[.!=''][1]"/>
                <xsl:variable name="classifier" select="(
                                                        ./pom:classifier/text(),
                                                        /pom:project/pom:dependencyManagement/pom:dependencies/pom:dependency[./pom:artifactId/text()=$artifactId and ./pom:groupId/text()=$groupId]/pom:classifier/text(),
                                                        /pom:project/pom:dependencies/pom:dependency[./pom:artifactId/text()=$artifactId and ./pom:groupId/text()=$groupId]/pom:classifier/text()
                                                      )[.!=''][1]"/>
                <xsl:variable name="repository">
                        <xsl:choose>
                                <xsl:when test="string($relativeHrefs) = 'true'"/>
                                <xsl:when test="contains($artifactId,'restlet')">http://maven.restlet.com/</xsl:when>
                                <xsl:when test="contains($artifactId,'mysql')">http://repository.springsource.com/maven/bundles/external/</xsl:when>
                                <xsl:when test="$groupId='org.eclipse.gemini'">http://download.eclipse.org/gemini/dbaccess/mvn/</xsl:when>
                                <xsl:when test="$groupId='org.eclipse.persistence'">http://download.eclipse.org/rt/eclipselink/maven.repo/</xsl:when>
                                <xsl:when test="$groupId='org.eclipse'">http://download.eclipse.org/gemini/mvn/</xsl:when>
                                
                                <xsl:otherwise>http://central.maven.org/maven2/</xsl:otherwise>
                        </xsl:choose>
                </xsl:variable>
                <xsl:variable name="href" select="concat($repository, replace($groupId,'\.','/'), '/',$artifactId,'/',$version,'/',$artifactId,'-',$version,
                                                        if ($classifier) then concat('-',$classifier) else '',
                                                        '.jar')"/>
                <xsl:variable name="id" select="string-join(($groupId,$artifactId,$version,$classifier),'/')"/>
                <!--
                    This is a hack, real solution is to do this in the POM
                -->
                <xsl:variable name="deployPath" select="if (starts-with($artifactId,'org.apache.felix.gogo.'))
                                                        then concat($deployPath,'/gogo')
                                                        else $deployPath"/>
                <xsl:variable name="finalPath" select="if (pom:destFileName) then concat($deployPath,'/',string(pom:destFileName)) else
                                                       concat($deployPath,'/',$groupId,'.',$artifactId,'-',$version,
                                                        if ($classifier) then concat('-',$classifier) else '',
                                                        '.jar')"/>
                <artifact href="{$href}" id="{$id}" extract="false" deployPath="{$finalPath}" version="{$version}" artifactId="{$artifactId}" groupId="{$groupId}" classifier="{$classifier}" overwrite-path="true"/>
        </xsl:template>
        <xsl:template match="pom:artifactItem" mode="zip">
                <xsl:param name="deployPath" />
                <xsl:param name="classifier" select="()" />
                <xsl:variable name="artifactId" select="./pom:artifactId/text()"/>
                <xsl:variable name="groupId" select="./pom:groupId/text()"/>
                <xsl:variable name="version" select="(
                                                        ./pom:version/text(),
                                                        /pom:project/pom:dependencyManagement/pom:dependencies/pom:dependency[./pom:artifactId/text()=$artifactId and ./pom:groupId/text()=$groupId]/pom:version/text(),
                                                        /pom:project/pom:dependencies/pom:dependency[./pom:artifactId/text()=$artifactId and ./pom:groupId/text()=$groupId]/pom:version/text()
                                                      )[.!=''][1]"/>
                
                <xsl:variable name="classifier" select="(
                                                        $classifier,
                                                        ./pom:classifier/text(),
                                                        /pom:project/pom:dependencyManagement/pom:dependencies/pom:dependency[./pom:artifactId/text()=$artifactId and ./pom:groupId/text()=$groupId]/pom:classifier/text(),
                                                        /pom:project/pom:dependencies/pom:dependency[./pom:artifactId/text()=$artifactId and ./pom:groupId/text()=$groupId]/pom:classifier/text()
                                                      )[.!=''][1]"/>
                <xsl:variable name="href" select="concat(if (string($relativeHrefs) = 'true') then '' else 'http://search.maven.org/remotecontent?filepath=',
                        replace($groupId,'\.','/'), '/',$artifactId,'/',$version,'/',$artifactId,'-',$version,
                        if ($classifier) then concat('-',$classifier) else '',
                        '.zip')"/>
                <xsl:variable name="overwrite-path" select="if ($groupId = 'org.daisy.pipeline' and $artifactId = 'cli') then 'false' else 'true'"/>
                
                <xsl:variable name="id" select="string-join(($groupId,$artifactId,$version,$classifier),'/')"/>
                <artifact href="{$href}" id="{$id}" extract="true" deployPath="{$deployPath}" version="{$version}" artifactId="{$artifactId}" groupId="{$groupId}" classifier="{$classifier}" overwrite-path="{$overwrite-path}"/>
        </xsl:template>
</xsl:stylesheet>

