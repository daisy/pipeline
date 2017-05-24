<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pom="http://maven.apache.org/POM/4.0.0"
                xmlns:exsl="http://exslt.org/common"
                xmlns:exslf="http://exslt.org/functions"
                xmlns:str="http://exslt.org/strings"
                xmlns:fn="my-functions"
                extension-element-prefixes="exslf">
	
	<xsl:param name="CURDIR"/>
	<xsl:param name="MODULE"/>
	<xsl:param name="SRC_DIR"/>
	<xsl:param name="MAIN_DIR"/>
	<xsl:param name="OUTPUT_FILENAME"/>
	
	<xsl:output method="text"/>
	
	<xsl:variable name="effective-pom" select="/*"/>
	
	<xsl:variable name="src-dirs" select="str:split($SRC_DIR, ' ')"/>
	<xsl:variable name="main-dirs" select="str:split($MAIN_DIR, ' ')"/>
	
	<xsl:template match="/">
		<exsl:document href="{concat($MODULE,'/',$OUTPUT_FILENAME)}" method="text">
			<xsl:call-template name="main">
				<xsl:with-param name="module" select="$MODULE"/>
				<xsl:with-param name="module-pom" select="document(concat($CURDIR,'/',$MODULE,'/pom.xml'))"/>
			</xsl:call-template>
		</exsl:document>
	</xsl:template>
	
	<xsl:variable name="internal-runtime-dependencies-rtf">
		<pom:projects>
			<xsl:for-each select="/*/pom:project">
				<xsl:copy>
					<xsl:copy-of select="pom:groupId"/>
					<xsl:copy-of select="pom:artifactId"/>
					<pom:dependencies>
						<xsl:for-each select="pom:dependencies/pom:dependency">
							<xsl:if test="not(pom:scope='test') and not(pom:scope='provided')">
								<xsl:if test="$effective-pom/pom:project[pom:groupId=current()/pom:groupId and
								                                         pom:artifactId=current()/pom:artifactId]">
									<xsl:copy-of select="."/>
								</xsl:if>
							</xsl:if>
						</xsl:for-each>
					</pom:dependencies>
				</xsl:copy>
			</xsl:for-each>
		</pom:projects>
	</xsl:variable>
	<xsl:variable name="internal-runtime-dependencies" select="exsl:node-set($internal-runtime-dependencies-rtf)"/>
	
	<xsl:template name="main">
		<xsl:param name="module"/>
		<xsl:param name="module-pom"/>
		<xsl:variable name="groupId">
			<xsl:choose>
				<xsl:when test="$module-pom/pom:project/pom:groupId">
					<xsl:copy-of select="$module-pom/pom:project/pom:groupId"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:copy-of select="$module-pom/pom:project/pom:parent/pom:groupId"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="artifactId" select="$module-pom/pom:project/pom:artifactId"/>
		<xsl:variable name="version" select="$module-pom/pom:project/pom:version"/>
		<xsl:variable name="packaging" select="$module-pom/pom:project/pom:packaging"/>
		<xsl:variable name="dirname">
			<xsl:choose>
				<xsl:when test="$module='.'">
					<xsl:value-of select="''"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="concat($module,'/')"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:choose>
			<xsl:when test="$module-pom/pom:project/pom:modules/pom:module">
				<xsl:text>.SECONDARY : </xsl:text>
				<xsl:call-template name="install-command">
					<xsl:with-param name="dirname" select="$dirname"/>
				</xsl:call-template>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:for-each select="$module-pom/pom:project/pom:modules/pom:module">
					<xsl:call-template name="install-command">
						<xsl:with-param name="dirname" select="$dirname"/>
					</xsl:call-template>
					<xsl:text> : </xsl:text>
					<xsl:variable name="submodule" select="concat($dirname,.)"/>
					<xsl:variable name="submodule-pom" select="document(concat($CURDIR,'/',$submodule,'/pom.xml'))"/>
					<xsl:choose>
						<xsl:when test="$submodule-pom/pom:project/pom:modules/pom:module">
							<xsl:call-template name="install-command">
								<xsl:with-param name="module" select="$submodule"/>
							</xsl:call-template>
						</xsl:when>
						<xsl:otherwise>
							<xsl:variable name="submodule-groupId">
								<xsl:choose>
									<xsl:when test="$submodule-pom/pom:project/pom:groupId">
										<xsl:copy-of select="$submodule-pom/pom:project/pom:groupId"/>
									</xsl:when>
									<xsl:otherwise>
										<xsl:copy-of select="$submodule-pom/pom:project/pom:parent/pom:groupId"/>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:variable>
							<xsl:call-template name="location-in-repo">
								<xsl:with-param name="groupId" select="$submodule-groupId"/>
								<xsl:with-param name="artifactId" select="$submodule-pom/pom:project/pom:artifactId"/>
								<xsl:with-param name="version" select="$submodule-pom/pom:project/pom:version"/>
								<xsl:with-param name="type" select="$submodule-pom/pom:project/pom:packaging"/>
							</xsl:call-template>
						</xsl:otherwise>
					</xsl:choose>
					<xsl:text>&#x0A;</xsl:text>
					<exsl:document href="{concat($submodule,'/',$OUTPUT_FILENAME)}" method="text">
						<xsl:text>&#x0A;</xsl:text>
						<xsl:call-template name="main">
							<xsl:with-param name="module" select="$submodule"/>
							<xsl:with-param name="module-pom" select="$submodule-pom"/>
						</xsl:call-template>
						<xsl:text>&#x0A;</xsl:text>
					</exsl:document>
				</xsl:for-each>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:text>.PHONY : </xsl:text>
				<xsl:value-of select="concat($dirname,'.last-tested')"/>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:for-each select="$module-pom/pom:project/pom:modules/pom:module">
					<xsl:value-of select="concat($dirname,'.last-tested : ',$dirname,.,'/.last-tested')"/>
					<xsl:text>&#x0A;</xsl:text>
				</xsl:for-each>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="concat($dirname,'.last-tested : %/.last-tested : %/.test')"/>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:text>ifndef DUMP_DEPENDENCIES</xsl:text>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:text>&#x09;</xsl:text>
				<xsl:text>touch $@</xsl:text>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:text>endif</xsl:text>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:text>.SECONDARY : </xsl:text>
				<xsl:value-of select="concat($dirname,'.test')"/>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:value-of select="concat($dirname,'.test : | $(MVN_WORKSPACE)')"/>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:text>ifndef DUMP_DEPENDENCIES</xsl:text>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:text>&#x09;</xsl:text>
				<xsl:text>cd $(dir $@) &amp;&amp; \</xsl:text>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:text>&#x09;</xsl:text>
				<xsl:text>$(MVN) clean verify | $(MVN_LOG)</xsl:text>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:text>endif</xsl:text>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:value-of select="concat($dirname,'.test : %/.test : %/pom.xml')"/>
				<xsl:text> %/.dependencies</xsl:text>
				<xsl:for-each select="$src-dirs">
					<xsl:if test="fn:starts-with(.,$dirname)">
						<!-- %/src/**/* does not work -->
						<xsl:value-of select="concat(' $(call rwildcard,',.,'/,*)')"/>
					</xsl:if>
				</xsl:for-each>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:call-template name="location-in-repo">
					<xsl:with-param name="groupId" select="$groupId"/>
					<xsl:with-param name="artifactId" select="$artifactId"/>
					<xsl:with-param name="version" select="$version"/>
					<xsl:with-param name="type" select="'pom'"/>
				</xsl:call-template>
				<xsl:value-of select="concat(' : %/',$artifactId,'-',$version,'.pom : %/maven-metadata-local.xml')"/>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:value-of select="concat('$(MVN_WORKSPACE)/',translate($groupId,'.','/'),'/',$artifactId,'/',$version,'/maven-metadata-local.xml ')"/>
				<xsl:call-template name="location-in-repo">
					<xsl:with-param name="groupId" select="$groupId"/>
					<xsl:with-param name="artifactId" select="$artifactId"/>
					<xsl:with-param name="version" select="$version"/>
					<xsl:with-param name="type" select="'pom'"/>
				</xsl:call-template>
				<xsl:text> : </xsl:text>
				<xsl:call-template name="install-command">
					<xsl:with-param name="dirname" select="$dirname"/>
				</xsl:call-template>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:text>ifndef DUMP_DEPENDENCIES</xsl:text>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:text>&#x09;</xsl:text>
				<xsl:text>touch $@</xsl:text>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:text>endif</xsl:text>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:value-of select="concat('$(MVN_WORKSPACE)/',translate($groupId,'.','/'),'/',$artifactId,'/',$version,'/',$artifactId,'-',$version,'%')"/>
				<xsl:text> : </xsl:text>
				<xsl:value-of select="concat($dirname,'.install%')"/>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:text>ifdef DUMP_DEPENDENCIES</xsl:text>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:text>&#x09;</xsl:text>
				<xsl:text>true</xsl:text>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:text>else</xsl:text>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:text>&#x09;</xsl:text>
				<xsl:text>test -e $@</xsl:text>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:text>endif</xsl:text>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:if test="$packaging='bundle' or $packaging='jar' or $packaging='maven-plugin'">
					<xsl:text>&#x0A;</xsl:text>
					<xsl:text>.SECONDARY : </xsl:text>
					<xsl:call-template name="install-command">
						<xsl:with-param name="dirname" select="$dirname"/>
						<xsl:with-param name="type" select="'jar'"/>
					</xsl:call-template>
					<xsl:text>&#x0A;</xsl:text>
					<xsl:call-template name="install-command">
						<xsl:with-param name="dirname" select="$dirname"/>
						<xsl:with-param name="type" select="'jar'"/>
					</xsl:call-template>
					<xsl:text> : %/.install.jar : %/.install </xsl:text>
					<xsl:text>&#x0A;</xsl:text>
				</xsl:if>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:text>.SECONDARY : </xsl:text>
				<xsl:call-template name="install-command">
					<xsl:with-param name="dirname" select="$dirname"/>
				</xsl:call-template>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:call-template name="install-command">
					<xsl:with-param name="dirname" select="$dirname"/>
				</xsl:call-template>
				<xsl:text> : | $(MVN_WORKSPACE)</xsl:text>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:text>ifdef DUMP_DEPENDENCIES</xsl:text>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:text>&#x09;</xsl:text>
				<xsl:text>+dirname $@</xsl:text>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:text>else</xsl:text>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:text>&#x09;</xsl:text>
				<xsl:text>cd $(dir $@) &amp;&amp; \</xsl:text>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:text>&#x09;</xsl:text>
				<xsl:text>$(MVN) clean install -DskipTests -Dinvoker.skip=true | $(MVN_LOG)</xsl:text>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:text>endif</xsl:text>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:call-template name="install-command">
					<xsl:with-param name="dirname" select="$dirname"/>
				</xsl:call-template>
				<xsl:text> : %/.install : %/pom.xml</xsl:text>
				<xsl:text> %/.dependencies</xsl:text>
				<xsl:for-each select="$main-dirs">
					<xsl:if test="fn:starts-with(.,$dirname)">
						<!-- %/src/**/* does not work -->
						<xsl:value-of select="concat(' $(call rwildcard,',.,'/,*)')"/>
					</xsl:if>
				</xsl:for-each>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:value-of select="concat('.SECONDARY : ',$dirname,'.dependencies')"/>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:value-of select="concat($dirname,'.dependencies :')"/>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:for-each select="$module-pom/pom:project/pom:dependencyManagement/pom:dependencies/pom:dependency[pom:scope='import']">
					<xsl:variable name="dependency-version" select="pom:version"/>
					<xsl:if test="fn:ends-with($dependency-version, '-SNAPSHOT')">
						<xsl:value-of select="concat($dirname,'.dependencies : ')"/>
						<xsl:call-template name="location-in-repo">
							<xsl:with-param name="groupId" select="pom:groupId"/>
							<xsl:with-param name="artifactId" select="pom:artifactId"/>
							<xsl:with-param name="version" select="$dependency-version"/>
							<xsl:with-param name="type" select="pom:type"/>
							<xsl:with-param name="classifier" select="pom:classifier"/>
						</xsl:call-template>
						<xsl:text>&#x0A;</xsl:text>
					</xsl:if>
				</xsl:for-each>
				<xsl:for-each select="$effective-pom/pom:project[pom:groupId=$groupId and
				                                                 pom:artifactId=$artifactId and
				                                                 pom:version=$version]">
					<xsl:variable name="managed-internal-runtime-dependencies">
						<xsl:variable name="dependencyManagement" select="pom:dependencyManagement"/>
						<pom:projects>
							<xsl:for-each select="$internal-runtime-dependencies/pom:projects/pom:project">
								<xsl:copy>
									<xsl:copy-of select="pom:groupId"/>
									<xsl:copy-of select="pom:artifactId"/>
									<pom:dependencies>
										<xsl:for-each select="pom:dependencies/pom:dependency">
											<xsl:copy>
												<xsl:copy-of select="pom:groupId"/>
												<xsl:copy-of select="pom:artifactId"/>
												<xsl:copy-of select="pom:type"/>
												<xsl:copy-of select="pom:classifier"/>
												<xsl:variable name="managed-version"
												              select="$dependencyManagement
												                      /pom:dependencies
												                      /pom:dependency[string(pom:groupId)=string(current()/pom:groupId) and
												                                      string(pom:artifactId)=string(current()/pom:artifactId) and
												                                      string(pom:type)=string(current()/pom:type)]
												                      /pom:version"/>
												<xsl:choose>
													<xsl:when test="$managed-version">
														<pom:version>
															<xsl:value-of select="$managed-version"/>
														</pom:version>
													</xsl:when>
													<xsl:otherwise>
														<xsl:copy-of select="pom:version"/>
													</xsl:otherwise>
												</xsl:choose>
											</xsl:copy>
										</xsl:for-each>
									</pom:dependencies>
								</xsl:copy>
							</xsl:for-each>
						</pom:projects>
					</xsl:variable>
					<xsl:apply-templates select=".">
						<xsl:with-param name="dirname" select="$dirname"/>
						<xsl:with-param name="managed-internal-runtime-dependencies" select="exsl:node-set($managed-internal-runtime-dependencies)"/>
					</xsl:apply-templates>
				</xsl:for-each>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="pom:project">
		<xsl:param name="dirname"/>
		<xsl:param name="managed-internal-runtime-dependencies"/>
		<xsl:variable name="project" select="."/>
		<!--
		    only automatically selected profiles supported
		-->
		<xsl:for-each select="pom:parent|
		                      pom:dependencies/pom:dependency|
		                      pom:build/pom:plugins/pom:plugin|
		                      pom:build/pom:plugins/pom:plugin[(not(pom:groupId) or pom:groupId='org.apache.maven.plugins')
		                                                       and pom:artifactId='maven-dependency-plugin']
		                                                      /pom:executions/pom:execution[pom:goals[pom:goal='copy' or
		                                                                                              pom:goal='unpack']]
		                                                      /pom:configuration/pom:artifactItems/pom:artifactItem">
			<xsl:variable name="groupId" select="pom:groupId"/>
			<xsl:variable name="artifactId" select="pom:artifactId"/>
			<xsl:variable name="version">
				<xsl:choose>
					<xsl:when test="pom:version">
						<xsl:value-of select="pom:version"/>
					</xsl:when>
					<xsl:when test="self::pom:artifactItem">
						<xsl:choose>
							<xsl:when test="$project/pom:dependencies/pom:dependency
							                [string(pom:groupId)=string(current()/pom:groupId) and
							                 string(pom:artifactId)=string(current()/pom:artifactId) and
							                 string(pom:type)=string(current()/pom:type) and
							                 string(pom:classifier)=string(current()/pom:classifier)]">
								<!-- already handled -->
							</xsl:when>
							<xsl:when test="$project/pom:dependencies/pom:dependency
							                [string(pom:groupId)=string(current()/pom:groupId) and
							                 string(pom:artifactId)=string(current()/pom:artifactId)]">
								<xsl:value-of select="$project/pom:dependencies/pom:dependency
								                      [string(pom:groupId)=string(current()/pom:groupId) and
								                       string(pom:artifactId)=string(current()/pom:artifactId)]
								                      /pom:version"/>
							</xsl:when>
							<xsl:when test="$project/pom:dependencyManagement/pom:dependencies/pom:dependency
							                [string(pom:groupId)=string(current()/pom:groupId) and
							                 string(pom:artifactId)=string(current()/pom:artifactId)]">
								<xsl:value-of select="$project/pom:dependencyManagement/pom:dependencies/pom:dependency
								                      [string(pom:groupId)=string(current()/pom:groupId) and
								                       string(pom:artifactId)=string(current()/pom:artifactId)]
								                      /pom:version"/>
							</xsl:when>
							<xsl:otherwise>
								<!-- might be transitive dependency, but not supported here -->
								<xsl:message terminate="yes">error</xsl:message>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:when>
					<xsl:when test="self::pom:plugin">
						<!-- a plugin may have no version defined -->
					</xsl:when>
					<xsl:otherwise>
						<xsl:message terminate="yes">coding error</xsl:message>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:variable>
			<xsl:choose>
				<xsl:when test="fn:ends-with($version, '-SNAPSHOT')">
					<xsl:value-of select="concat($dirname,'.dependencies : ')"/>
					<xsl:variable name="type">
						<xsl:choose>
							<xsl:when test="self::pom:parent">
								<xsl:value-of select="'pom'"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="pom:type"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:variable>
					<xsl:call-template name="location-in-repo">
						<xsl:with-param name="groupId" select="pom:groupId"/>
						<xsl:with-param name="artifactId" select="pom:artifactId"/>
						<xsl:with-param name="version" select="$version"/>
						<xsl:with-param name="type" select="$type"/>
						<xsl:with-param name="classifier" select="pom:classifier"/>
					</xsl:call-template>
					<xsl:text>&#x0A;</xsl:text>
				</xsl:when>
				<!--
				    A non-snapshot dependency B of A may have a transitive dependency C who's
				    version is overwritten by Maven to a snapshot based on the
				    dependencyManagement of A.
				    
				    We try to support this use case here. We are making the assumption that
				    module B is present in the Maven reactor and that the transitive
				    dependencies of B as defined in the module (the snapshot) and the transitive
				    dependencies of the non-snapshot version of B are the same.
				-->
				<xsl:when test="self::pom:dependency and not(pom:scope='test') and not(pom:scope='provided')">
					<xsl:apply-templates select="$managed-internal-runtime-dependencies/pom:projects/pom:project[pom:groupId=$groupId and
					                                                                                             pom:artifactId=$artifactId]">
						<xsl:with-param name="dirname" select="$dirname"/>
						<xsl:with-param name="managed-internal-runtime-dependencies" select="$managed-internal-runtime-dependencies"/>
					</xsl:apply-templates>
				</xsl:when>
			</xsl:choose>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template name="location-in-repo">
		<xsl:param name="groupId"/>
		<xsl:param name="artifactId"/>
		<xsl:param name="version"/>
		<xsl:param name="type"/>
		<xsl:param name="classifier"/>
		<xsl:value-of select="concat('$(MVN_WORKSPACE)/',translate($groupId,'.','/'),'/',$artifactId,'/',$version,'/',$artifactId,'-',$version)"/>
		<xsl:if test="$classifier">
			<xsl:value-of select="concat('-',$classifier)"/>
		</xsl:if>
		<xsl:choose>
			<xsl:when test="string($type)='' or $type='bundle' or $type='maven-plugin'">
				<xsl:text>.jar</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="concat('.',$type)"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template name="install-command">
		<xsl:param name="dirname"/>
		<xsl:param name="type"/>
		<xsl:param name="classifier"/>
		<xsl:value-of select="concat($dirname,'.install')"/>
		<xsl:if test="$classifier">
			<xsl:value-of select="concat('-',$classifier)"/>
		</xsl:if>
		<xsl:if test="$type">
			<xsl:value-of select="concat('.',$type)"/>
		</xsl:if>
	</xsl:template>
	
	<exslf:function name="fn:starts-with">
		<xsl:param name="arg1"/>
		<xsl:param name="arg2"/>
		<exslf:result select="substring($arg1, 1, string-length($arg2)) = $arg2"/>
	</exslf:function>
	
	<exslf:function name="fn:ends-with">
		<xsl:param name="arg1"/>
		<xsl:param name="arg2"/>
		<exslf:result select="substring($arg1, string-length($arg1) - string-length($arg2) + 1) = $arg2"/>
	</exslf:function>
	
	<xsl:include href="lib/str.split.function.xsl"/>
	
</xsl:stylesheet>
