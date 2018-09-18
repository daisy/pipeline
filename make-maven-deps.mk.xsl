<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pom="http://maven.apache.org/POM/4.0.0"
                exclude-result-prefixes="xs pom"
                xmlns="http://maven.apache.org/POM/4.0.0">
	
	<xsl:param name="ROOT_DIR"/>
	<xsl:param name="MY_DIR"/>
	<xsl:param name="GRADLE_POM"/>
	<xsl:param name="MODULE"/>
	<xsl:param name="SRC_DIRS"/>
	<xsl:param name="DOC_DIRS"/>
	<xsl:param name="INDEX_FILES"/>
	<xsl:param name="MAIN_DIRS"/>
	<!--
	    directories from which multiple modules are released at once
	-->
	<xsl:param name="RELEASE_DIRS"/>
	<xsl:param name="OUTPUT_FILENAME"/>
	
	<xsl:output method="xml" indent="yes"/>
	
	<xsl:variable name="effective-pom" select="/*"/>
	<xsl:variable name="gradle-pom" select="document(concat($ROOT_DIR,'/',$GRADLE_POM))/*"/>
	
	<xsl:variable name="src-dirs" select="tokenize($SRC_DIRS, '\s+')"/>
	<xsl:variable name="main-dirs" select="tokenize($MAIN_DIRS, '\s+')"/>
	<xsl:variable name="doc-dirs" select="tokenize($DOC_DIRS, '\s+')"/>
	<xsl:variable name="index-files" select="tokenize($INDEX_FILES, '\s+')"/>
	<xsl:variable name="release-dirs" select="tokenize($RELEASE_DIRS, '\s+')"/>
	
	<xsl:template match="/">
		<xsl:call-template name="main">
			<xsl:with-param name="module" select="$MODULE"/>
			<xsl:with-param name="module-pom" select="document(concat($ROOT_DIR,'/',$MODULE,'/pom.xml'))"/>
			<xsl:with-param name="release-dir" select="()"/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:variable name="internal-runtime-dependencies" as="element()">
		<pom:projects>
			<xsl:for-each select="/*/pom:project">
				<xsl:copy>
					<xsl:copy-of select="pom:groupId"/>
					<xsl:copy-of select="pom:artifactId"/>
					<xsl:copy-of select="pom:version"/>
					<pom:dependencies>
						<xsl:for-each select="pom:dependencies/pom:dependency">
							<xsl:if test="not(pom:scope='test') and not(pom:scope='provided')">
								<xsl:if test="$effective-pom/pom:project[pom:groupId=current()/pom:groupId and
								                                         pom:artifactId=current()/pom:artifactId]
								              or $gradle-pom/pom:project[pom:groupId=current()/pom:groupId and
								                                         pom:artifactId=current()/pom:artifactId]">
									<xsl:copy-of select="."/>
								</xsl:if>
							</xsl:if>
						</xsl:for-each>
					</pom:dependencies>
				</xsl:copy>
			</xsl:for-each>
			<xsl:sequence select="$gradle-pom/pom:project"/>
		</pom:projects>
	</xsl:variable>
	
	<xsl:template name="main">
		<xsl:param name="module"/>
		<xsl:param name="module-pom"/>
		<xsl:param name="release-dir"/>
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
		<xsl:variable name="type" select="if (string($module-pom/pom:project/pom:packaging)=('','bundle','maven-plugin'))
		                                  then 'jar'
		                                  else $module-pom/pom:project/pom:packaging"/>
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
		<xsl:variable name="is-aggregator" as="xs:boolean" select="exists($module-pom/pom:project/pom:modules/pom:module)"/>
		<xsl:variable name="is-release-dir" as="xs:boolean" select="not($release-dir) and $module=$release-dirs"/>
		<xsl:variable name="artifacts-and-dependencies" as="element()*"> <!-- (artifactItem | dependency)* -->
			<xsl:choose>
				<xsl:when test="$module-pom/pom:project/pom:modules/pom:module">
					<xsl:for-each select="$module-pom/pom:project/pom:modules/pom:module">
						<xsl:variable name="submodule" select="concat($dirname,.)"/>
						<xsl:variable name="submodule-pom" select="document(concat($ROOT_DIR,'/',$submodule,'/pom.xml'))"/>
						<xsl:call-template name="main">
							<xsl:with-param name="module" select="$submodule"/>
							<xsl:with-param name="module-pom" select="$submodule-pom"/>
							<xsl:with-param name="release-dir" select="if ($is-release-dir) then $module else $release-dir"/>
						</xsl:call-template>
					</xsl:for-each>
				</xsl:when>
				<xsl:otherwise>
					<xsl:if test="ends-with($version,'-SNAPSHOT')">
						<artifactItem>
							<groupId>
								<xsl:value-of select="$groupId"/>
							</groupId>
							<artifactId>
								<xsl:value-of select="$artifactId"/>
							</artifactId>
							<version>
								<xsl:value-of select="$version"/>
							</version>
							<type>
								<xsl:value-of select="$type"/>
							</type>
						</artifactItem>
					</xsl:if>
					<xsl:for-each select="$module-pom/pom:project/pom:dependencyManagement/pom:dependencies/pom:dependency">
						<xsl:if test="ends-with(pom:version, '-SNAPSHOT')">
							<dependency fromDependencyManagement="true">
								<xsl:if test="pom:scope='import'">
									<xsl:attribute name="scope" select="'import'"/>
								</xsl:if>
								<groupId>
									<xsl:value-of select="pom:groupId"/>
								</groupId>
								<artifactId>
									<xsl:value-of select="pom:artifactId"/>
								</artifactId>
								<version>
									<xsl:value-of select="pom:version"/>
								</version>
								<type>
									<xsl:value-of select="(pom:type,'jar')[1]"/>
								</type>
								<xsl:if test="pom:classifier">
									<classifier>
										<xsl:value-of select="pom:classifier"/>
									</classifier>
								</xsl:if>
							</dependency>
						</xsl:if>
					</xsl:for-each>
					<xsl:for-each select="$effective-pom/pom:project[pom:groupId=$groupId and
					                                                 pom:artifactId=$artifactId and
					                                                 pom:version=$version]">
						<xsl:variable name="managed-internal-runtime-dependencies" as="element()">
							<xsl:variable name="dependencyManagement" select="pom:dependencyManagement"/>
							<pom:projects>
								<xsl:for-each select="$internal-runtime-dependencies/pom:project">
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
													                                      string(pom:type)=string(current()/pom:type) and
													                                      string(pom:classifier)=string(current()/pom:classifier)]
													                      /pom:version"/>
													<pom:version>
														<xsl:choose>
															<xsl:when test="$managed-version">
																<xsl:value-of select="$managed-version"/>
															</xsl:when>
															<xsl:otherwise>
																<xsl:text>DUMMY</xsl:text>
															</xsl:otherwise>
														</xsl:choose>
													</pom:version>
												</xsl:copy>
											</xsl:for-each>
										</pom:dependencies>
									</xsl:copy>
								</xsl:for-each>
							</pom:projects>
						</xsl:variable>
						<xsl:apply-templates select=".">
							<xsl:with-param name="managed-internal-runtime-dependencies" select="$managed-internal-runtime-dependencies"/>
						</xsl:apply-templates>
					</xsl:for-each>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="artifacts-and-dependencies" as="element()*">
			<xsl:choose>
				<xsl:when test="$is-aggregator">
					<xsl:sequence select="$artifacts-and-dependencies/self::pom:artifactItem"/>
					<xsl:for-each select="$artifacts-and-dependencies/self::pom:dependency">
						<xsl:if test="not($artifacts-and-dependencies/self::pom:artifactItem[
						              string(pom:groupId)=string(current()/pom:groupId) and
						              string(pom:artifactId)=string(current()/pom:artifactId) and
						              string(pom:version)=string(current()/pom:version) and
						              string(pom:type)=string(current()/pom:type) and
						              string(pom:classifier)=string(current()/pom:classifier)])">
							<xsl:sequence select="."/>
						</xsl:if>
					</xsl:for-each>
				</xsl:when>
				<xsl:otherwise>
					<xsl:sequence select="$artifacts-and-dependencies"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:result-document href="{concat($module,'/',$OUTPUT_FILENAME)}" method="text">
			<xsl:choose>
				<xsl:when test="$is-aggregator">
					<xsl:text>.SECONDARY : </xsl:text>
					<xsl:value-of select="concat($dirname,'.install')"/>
					<xsl:text>&#x0A;</xsl:text>
					<xsl:value-of select="concat($dirname,'.install')"/>
					<xsl:text> :</xsl:text>
					<xsl:for-each select="$artifacts-and-dependencies/self::pom:artifactItem">
						<xsl:text> \&#x0A;&#x09;</xsl:text>
						<xsl:call-template name="location-in-repo">
							<xsl:with-param name="groupId" select="pom:groupId"/>
							<xsl:with-param name="artifactId" select="pom:artifactId"/>
							<xsl:with-param name="version" select="pom:version"/>
							<xsl:with-param name="type" select="pom:type"/>
							<xsl:with-param name="classifier" select="pom:classifier"/>
						</xsl:call-template>
					</xsl:for-each>
					<xsl:text>&#x0A;</xsl:text>
					<xsl:text>&#x0A;</xsl:text>
					<xsl:value-of select="concat('check : ',$dirname,'.last-tested')"/>
					<xsl:text>&#x0A;</xsl:text>
					<xsl:text>.PHONY : </xsl:text>
					<xsl:value-of select="concat($dirname,'.last-tested')"/>
					<xsl:text>&#x0A;</xsl:text>
					<xsl:value-of select="concat($dirname,'.last-tested')"/>
					<xsl:text> :</xsl:text>
					<xsl:for-each select="$module-pom/pom:project/pom:modules/pom:module">
						<xsl:text> \&#x0A;&#x09;</xsl:text>
						<xsl:value-of select="concat($dirname,.,'/.last-tested')"/>
					</xsl:for-each>
					<xsl:text>&#x0A;</xsl:text>
					<xsl:if test="not($module='.')">
						<xsl:text>&#x0A;</xsl:text>
						<xsl:text>.PHONY : </xsl:text>
						<xsl:value-of select="concat('eclipse-',$module)"/>
						<xsl:text>&#x0A;</xsl:text>
						<xsl:value-of select="concat('eclipse-',$module)"/>
						<xsl:text> :</xsl:text>
						<xsl:for-each select="$module-pom/pom:project/pom:modules/pom:module">
							<xsl:text> \&#x0A;&#x09;</xsl:text>
							<xsl:value-of select="concat('eclipse-',$dirname,.)"/>
						</xsl:for-each>
						<xsl:text>&#x0A;</xsl:text>
					</xsl:if>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="concat($dirname,'.last-tested : %/.last-tested : %/.test | .group-eval')"/>
					<xsl:text>&#x0A;</xsl:text>
					<xsl:text>&#x09;</xsl:text>
					<xsl:text>+$(EVAL) touch $@</xsl:text>
					<xsl:text>&#x0A;</xsl:text>
					<xsl:text>&#x0A;</xsl:text>
					<xsl:text>.SECONDARY : </xsl:text>
					<xsl:value-of select="concat($dirname,'.test')"/>
					<xsl:text>&#x0A;</xsl:text>
					<xsl:value-of select="concat($dirname,'.test : | .maven-init .group-eval')"/>
					<xsl:text>&#x0A;</xsl:text>
					<xsl:text>&#x09;</xsl:text>
					<xsl:text>+$(EVAL) </xsl:text>
					<xsl:value-of select="$MY_DIR"/>
					<xsl:text>/mvn-test.sh $$(dirname $@)</xsl:text>
					<xsl:text>&#x0A;</xsl:text>
					<xsl:text>&#x0A;</xsl:text>
					<xsl:value-of select="concat($dirname,'.test : %/.test : %/pom.xml')"/>
					<xsl:text> %/.dependencies</xsl:text>
					<xsl:for-each select="$src-dirs">
						<xsl:if test="starts-with(.,$dirname)">
							<!-- %/src/**/* does not work -->
							<xsl:value-of select="concat(' $(call rwildcard,',.,'/,*)')"/>
						</xsl:if>
					</xsl:for-each>
					<xsl:text>&#x0A;</xsl:text>
					<xsl:text>&#x0A;</xsl:text>
					<xsl:if test="ends-with($version,'-SNAPSHOT')">
						<xsl:call-template name="location-in-repo">
							<xsl:with-param name="groupId" select="$groupId"/>
							<xsl:with-param name="artifactId" select="$artifactId"/>
							<xsl:with-param name="version" select="$version"/>
							<xsl:with-param name="type" select="'pom'"/>
						</xsl:call-template>
						<xsl:text> : </xsl:text>
						<xsl:value-of select="concat($dirname,'.install.pom | .group-eval')"/>
						<xsl:text>&#x0A;</xsl:text>
						<xsl:text>&#x09;</xsl:text>
						<xsl:text>+$(call eval-if-unix,'test -e' $@)</xsl:text>
						<xsl:text>&#x0A;</xsl:text>
						<xsl:text>&#x09;</xsl:text>
						<xsl:text>+$(call eval-if-unix,touch $@)</xsl:text>
						<xsl:text>&#x0A;</xsl:text>
						<xsl:text>&#x0A;</xsl:text>
						<xsl:value-of select="concat('$(MVN_LOCAL_REPOSITORY)/',
						                             translate($groupId,'.','/'),
						                             '/',$artifactId,
						                             '/',$version,
						                             '/',$artifactId,
						                             '-',$version,'%')"/>
						<xsl:text> : </xsl:text>
						<xsl:value-of select="concat($dirname,'.install% | .group-eval')"/>
						<xsl:text>&#x0A;</xsl:text>
						<xsl:text>&#x09;</xsl:text>
						<xsl:text>+$(call eval-if-unix,'test -e' $@)</xsl:text>
						<xsl:text>&#x0A;</xsl:text>
						<xsl:text>&#x09;</xsl:text>
						<xsl:text>+$(call eval-if-unix,'touch' $@)</xsl:text>
						<xsl:text>&#x0A;</xsl:text>
						<xsl:text>&#x0A;</xsl:text>
						<xsl:text>.SECONDARY : </xsl:text>
						<xsl:value-of select="concat($dirname,'.install.pom')"/>
						<xsl:text>&#x0A;</xsl:text>
						<xsl:value-of select="concat($dirname,'.install.pom')"/>
						<xsl:text> : %/.install.pom : %/pom.xml %/.dependencies | .maven-init .group-eval</xsl:text>
						<xsl:text>&#x0A;</xsl:text>
						<xsl:text>&#x09;</xsl:text>
						<xsl:text>+$(call eval-for-host-platform,</xsl:text>
						<xsl:value-of select="$MY_DIR"/>
						<xsl:text>/mvn-install-pom.sh,$$(dirname $@))</xsl:text>
						<xsl:text>&#x0A;</xsl:text>
						<xsl:if test="$type='jar'">
							<xsl:text>&#x0A;</xsl:text>
							<xsl:text>.SECONDARY : </xsl:text>
							<xsl:value-of select="concat($dirname,'.install.jar')"/>
							<xsl:text>&#x0A;</xsl:text>
							<xsl:value-of select="concat($dirname,'.install.jar')"/>
							<xsl:text> : %/.install.jar : %/.install </xsl:text>
							<xsl:text>&#x0A;</xsl:text>
							<xsl:if test="$effective-pom
							              /pom:project[pom:groupId=$groupId and
							                           pom:artifactId=$artifactId and
							                           pom:version=$version]
							              //pom:build/pom:plugins/pom:plugin[(not(pom:groupId) or pom:groupId='org.apache.maven.plugins')
							                                                 and pom:artifactId='maven-jar-plugin']
							              /pom:executions/pom:execution[pom:goals[pom:goal='jar']]
							              /pom:configuration/pom:classifier[string(.)='doc']">
								<xsl:text>&#x0A;</xsl:text>
								<xsl:text>.SECONDARY : </xsl:text>
								<xsl:value-of select="concat($dirname,'.install-doc.jar')"/>
								<xsl:text>&#x0A;</xsl:text>
								<xsl:value-of select="concat($dirname,'.install-doc.jar')"/>
								<xsl:text> : %/.install-doc.jar : %/.install</xsl:text>
								<xsl:text>&#x0A;</xsl:text>
								<xsl:for-each select="$doc-dirs">
									<xsl:if test="starts-with(.,$dirname)">
										<xsl:text>&#x0A;</xsl:text>
										<xsl:value-of select="concat($dirname,'.install-doc.jar :  $(call rwildcard,',.,'/,*)')"/>
										<xsl:text>&#x0A;</xsl:text>
									</xsl:if>
								</xsl:for-each>
								<xsl:for-each select="$index-files">
									<xsl:if test="starts-with(.,$dirname)">
										<xsl:text>&#x0A;</xsl:text>
										<xsl:value-of select="concat($dirname,'.install-doc.jar : ',.)"/>
										<xsl:text>&#x0A;</xsl:text>
									</xsl:if>
								</xsl:for-each>
							</xsl:if>
							<xsl:if test="$effective-pom
							              /pom:project[pom:groupId=$groupId and
							                           pom:artifactId=$artifactId and
							                           pom:version=$version]
							              //pom:build/pom:plugins/pom:plugin[(not(pom:groupId) or pom:groupId='org.apache.maven.plugins')
							                                                 and pom:artifactId='maven-jar-plugin']
							              /pom:executions/pom:execution[pom:goals[pom:goal='jar']]
							              /pom:configuration/pom:classifier[string(.)='xprocdoc']">
								<xsl:text>&#x0A;</xsl:text>
								<xsl:text>.SECONDARY : </xsl:text>
								<xsl:value-of select="concat($dirname,'.install-xprocdoc.jar')"/>
								<xsl:text>&#x0A;</xsl:text>
								<xsl:value-of select="concat($dirname,'.install-xprocdoc.jar')"/>
								<xsl:text> : %/.install-xprocdoc.jar : %/.install</xsl:text>
								<xsl:text>&#x0A;</xsl:text>
							</xsl:if>
							<xsl:if test="$effective-pom
							              /pom:project[pom:groupId=$groupId and
							                           pom:artifactId=$artifactId and
							                           pom:version=$version]
							              /pom:build/pom:plugins/pom:plugin[(not(pom:groupId) or pom:groupId='org.apache.maven.plugins')
							                                                and pom:artifactId='maven-javadoc-plugin']
							              /pom:executions/pom:execution[pom:goals[pom:goal='jar']]">
								<xsl:text>&#x0A;</xsl:text>
								<xsl:text>.SECONDARY : </xsl:text>
								<xsl:value-of select="concat($dirname,'.install-javadoc.jar')"/>
								<xsl:text>&#x0A;</xsl:text>
								<xsl:value-of select="concat($dirname,'.install-javadoc.jar')"/>
								<xsl:text> : %/.install-javadoc.jar : %/.install </xsl:text>
								<xsl:text>&#x0A;</xsl:text>
							</xsl:if>
						</xsl:if>
						<xsl:text>&#x0A;</xsl:text>
						<xsl:text>.SECONDARY : </xsl:text>
						<xsl:value-of select="concat($dirname,'.install')"/>
						<xsl:text>&#x0A;</xsl:text>
						<xsl:value-of select="concat($dirname,'.install')"/>
						<xsl:text> : | .maven-init .group-eval</xsl:text>
						<xsl:text>&#x0A;</xsl:text>
						<xsl:text>&#x09;</xsl:text>
						<xsl:text>+$(call eval-for-host-platform,</xsl:text>
						<xsl:value-of select="$MY_DIR"/>
						<xsl:text>/mvn-install.sh,$$(dirname $@))</xsl:text>
						<xsl:text>&#x0A;</xsl:text>
						<xsl:text>&#x0A;</xsl:text>
						<xsl:value-of select="concat($dirname,'.install')"/>
						<xsl:text> : %/.install : %/pom.xml</xsl:text>
						<xsl:text> %/.dependencies</xsl:text>
						<xsl:for-each select="$main-dirs">
							<xsl:if test="starts-with(.,$dirname)">
								<!-- %/src/**/* does not work -->
								<xsl:value-of select="concat(' $(call rwildcard,',.,'/,*)')"/>
							</xsl:if>
						</xsl:for-each>
						<xsl:text>&#x0A;</xsl:text>
					</xsl:if>
					<xsl:text>&#x0A;</xsl:text>
					<xsl:value-of select="concat('.SECONDARY : ',$dirname,'.dependencies')"/>
					<xsl:text>&#x0A;</xsl:text>
					<xsl:value-of select="concat($dirname,'.dependencies :')"/>
					<xsl:if test="ends-with($version,'-SNAPSHOT')">
						<xsl:variable name="dependencies" as="xs:string*">
							<xsl:for-each select="$artifacts-and-dependencies/self::pom:dependency[not(@fromDependencyManagement)
							                                                                       or @scope='import']">
								<xsl:call-template name="location-in-repo">
									<xsl:with-param name="groupId" select="pom:groupId"/>
									<xsl:with-param name="artifactId" select="pom:artifactId"/>
									<xsl:with-param name="version" select="pom:version"/>
									<xsl:with-param name="type" select="pom:type"/>
									<xsl:with-param name="classifier" select="pom:classifier"/>
								</xsl:call-template>
							</xsl:for-each>
						</xsl:variable>
						<xsl:variable name="dependencies" as="xs:string*" select="distinct-values($dependencies)"/>
						<xsl:if test="count($dependencies) &gt; 0">
							<xsl:text> </xsl:text>
							<xsl:if test="count($dependencies) &gt; 1">
								<xsl:text>\&#x0A;&#x09;</xsl:text>
							</xsl:if>
							<xsl:sequence select="string-join($dependencies, ' \&#x0A;&#x09;')"/>
						</xsl:if>
					</xsl:if>
					<xsl:text>&#x0A;</xsl:text>
					<xsl:text>&#x0A;</xsl:text>
					<xsl:variable name="version-without-snapshot" select="replace($version,'-SNAPSHOT$','')"/>
					<xsl:value-of select="concat('$(MVN_LOCAL_REPOSITORY)/',
					                             translate($groupId,'.','/'),
					                             '/',$artifactId,
					                             '/',$version-without-snapshot,
					                             '/',$artifactId,
					                             '-',$version-without-snapshot,'.%')"/>
					<xsl:text> \&#x0A;</xsl:text>
					<xsl:value-of select="concat('$(MVN_LOCAL_REPOSITORY)/',
					                             translate($groupId,'.','/'),
					                             '/',$artifactId,
					                             '/',$version-without-snapshot,
					                             '/',$artifactId,
					                             '-',$version-without-snapshot,'-%')"/>
					<xsl:text> : </xsl:text>
					<xsl:value-of select="concat($dirname,'.release')"/>
					<xsl:text>&#x0A;</xsl:text>
					<xsl:text>&#x09;</xsl:text>
					<xsl:text>+:</xsl:text>
					<xsl:text>&#x0A;</xsl:text>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:if test="$is-release-dir or not($is-aggregator)">
				<xsl:text>&#x0A;</xsl:text>
				<xsl:value-of select="concat('.SECONDARY : ',$dirname,'.release')"/>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:choose>
					<xsl:when test="not(ends-with($version, '-SNAPSHOT'))">
						<!-- already released, but empty rule is needed because jar might not be in .maven-workspace yet -->
						<xsl:value-of select="concat($dirname,'.release :')"/>
						<xsl:text>&#x0A;</xsl:text>
					</xsl:when>
					<xsl:otherwise>
						<xsl:choose>
							<xsl:when test="$is-release-dir or not($is-aggregator or $release-dir)">
								<xsl:value-of select="concat($dirname,'.release : | .maven-init .group-eval')"/>
								<xsl:text>&#x0A;</xsl:text>
								<xsl:text>&#x09;</xsl:text>
								<xsl:text>+$(EVAL) "</xsl:text>
								<xsl:value-of select="$MY_DIR"/>
								<xsl:text>/mvn-release.sh $$(dirname $@)"</xsl:text>
								<xsl:text>&#x0A;</xsl:text>
							</xsl:when>
							<xsl:otherwise>
								<!-- not($is-release-dir) and not($is-aggregator) and $release-dir -->
								<xsl:value-of select="concat($dirname,'.release : ',$release-dir,'/.release')"/>
								<xsl:text>&#x0A;</xsl:text>
								<xsl:text>&#x09;</xsl:text>
								<xsl:value-of select="concat('+$(EVAL) &quot;',
								                             $MY_DIR,
								                             '/mvn-release.sh ',
								                             $release-dir,
								                             '&quot; ',
								                             substring-after($module,concat($release-dir,'/')))"/>
								<xsl:text>&#x0A;</xsl:text>
							</xsl:otherwise>
						</xsl:choose>
						<xsl:text>&#x0A;</xsl:text>
						<xsl:value-of select="concat($dirname,'.release :')"/>
						<xsl:variable name="dependencies" as="xs:string*">
							<!--
							    If the release happens from the current directory, include also snapshot dependencies
							    existing only in a dependencyManagement section (i.e. not true dependencies). If the
							    release does not happen from the current directory it means that the current module
							    could have dependencies to other modules in the same multi-module project, and making
							    all internal dependencies, including those existing only in a dependencyManagement
							    section, explicit could result in circular dependencies. "import" dependencies
							    (i.e. BOMs) are always included because they don't result in circular
							    dependencies.
							    
							    Refuse to release if there are snapshot dependencies that do not match a module in the
							    super project (by appending the string "!!!" to the version). Also refuse to release if
							    there are snapshot dependencies that match a module in the super project but the
							    versions don't match (module version does not equal version of dependency and module
							    version does not equal version of dependency minus -SNAPSHOT).
							-->
							<xsl:for-each select="if ($is-release-dir or not($is-aggregator or $release-dir))
							                      then $artifacts-and-dependencies/self::pom:dependency
							                      else $artifacts-and-dependencies/self::pom:dependency
							                        [not(@fromDependencyManagement and not(@scope='import'))]">
								<xsl:choose>
									<xsl:when test="$internal-runtime-dependencies/pom:project[
									                  string(pom:groupId)=string(current()/pom:groupId) and
									                  string(pom:artifactId)=string(current()/pom:artifactId) and
									                  string(pom:version)=(string(current()/pom:version),
									                                       replace(current()/pom:version,'-SNAPSHOT$',''))]">
										<xsl:call-template name="location-in-repo">
											<xsl:with-param name="groupId" select="pom:groupId"/>
											<xsl:with-param name="artifactId" select="pom:artifactId"/>
											<xsl:with-param name="version" select="replace(pom:version,'-SNAPSHOT$','')"/>
											<xsl:with-param name="type" select="pom:type"/>
											<xsl:with-param name="classifier" select="pom:classifier"/>
										</xsl:call-template>
									</xsl:when>
									<xsl:when test="ends-with(pom:version, '-SNAPSHOT')">
										<xsl:call-template name="location-in-repo">
											<xsl:with-param name="groupId" select="pom:groupId"/>
											<xsl:with-param name="artifactId" select="pom:artifactId"/>
											<xsl:with-param name="version" select="concat(pom:version,'!!!')"/>
											<xsl:with-param name="type" select="pom:type"/>
											<xsl:with-param name="classifier" select="pom:classifier"/>
										</xsl:call-template>
									</xsl:when>
								</xsl:choose>
							</xsl:for-each>
						</xsl:variable>
						<xsl:variable name="dependencies" as="xs:string*" select="distinct-values($dependencies)"/>
						<xsl:if test="count($dependencies) &gt; 0">
							<xsl:text> </xsl:text>
							<xsl:if test="count($dependencies) &gt; 1">
								<xsl:text>\&#x0A;&#x09;</xsl:text>
							</xsl:if>
							<xsl:sequence select="string-join($dependencies, ' \&#x0A;&#x09;')"/>
						</xsl:if>
						<xsl:text>&#x0A;</xsl:text>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:if>
			<xsl:if test="not($is-aggregator)">
				<xsl:text>&#x0A;</xsl:text>
				<xsl:value-of select="concat($dirname,'.project',' : ',$dirname,'pom.xml')"/>
				<!--
				    all dependencies need to be installed in order to be able to run the
				    eclipse:eclipse goal and all non-Maven and non-snapshot dependencies need to be
				    installed in order to make the projects build in Eclipse
				    
				    FIXME: the correct working of the Makefile relies on Make putting this whole
				    "install" part before the "eclipse" part in the execution order, if this is not
				    satisfied the Eclipse projects are not linked up correctly
				-->
				<xsl:value-of select="concat(' ',$dirname,'.dependencies')"/>
				<xsl:text> \&#x0A;&#x09;</xsl:text>
				<xsl:text>| </xsl:text>
				<xsl:variable name="dependencies" as="xs:string*">
					<xsl:for-each select="$artifacts-and-dependencies/self::pom:dependency[not(@fromDependencyManagement)
					                                                                       or @scope='import']">
						<!-- if it is a Maven dependency -->
						<xsl:if test="$effective-pom/pom:project[
						                string(pom:groupId)=string(current()/pom:groupId) and
						                string(pom:artifactId)=string(current()/pom:artifactId)]">
							<xsl:sequence select="concat('.eclipse-projects/',pom:groupId,'.',pom:artifactId)"/>
						</xsl:if>
					</xsl:for-each>
				</xsl:variable>
				<xsl:variable name="dependencies" as="xs:string*" select="distinct-values($dependencies)"/>
				<xsl:if test="count($dependencies) &gt; 0">
					<xsl:sequence select="string-join($dependencies, ' \&#x0A;&#x09;  ')"/>
					<xsl:text> \&#x0A;&#x09;  </xsl:text>
				</xsl:if>
				<xsl:text>.group-eval</xsl:text>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:text>&#x09;</xsl:text>
				<xsl:text>+$(call eval-for-host-platform,</xsl:text>
				<xsl:value-of select="$MY_DIR"/>
				<xsl:text>/mvn-eclipse.sh,</xsl:text>
				<xsl:value-of select="substring($dirname,1,string-length($dirname) - 1)"/>
				<xsl:text>)</xsl:text>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:text>.SECONDARY :</xsl:text>
				<xsl:value-of select="concat('.eclipse-projects/',$groupId,'.',$artifactId)"/>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:value-of select="concat('.eclipse-projects/',$groupId,'.',$artifactId)"/>
				<xsl:text> : </xsl:text>
				<xsl:value-of select="concat($dirname,'.project')"/>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:value-of select="concat('clean-eclipse : ',$dirname,'.clean-eclipse')"/>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:text>.PHONY : </xsl:text>
				<xsl:value-of select="concat($dirname,'.clean-eclipse')"/>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:value-of select="concat($dirname,'.clean-eclipse :')"/>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:text>&#x09;</xsl:text>
				<xsl:value-of select="concat('rm -rf $(addprefix ',$dirname,',.project .settings .classpath)')"/>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:value-of select="concat('clean : ',$dirname,'.clean')"/>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:text>.PHONY : </xsl:text>
				<xsl:value-of select="concat($dirname,'.clean')"/>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:value-of select="concat($dirname,'.clean :')"/>
				<xsl:text>&#x0A;</xsl:text>
				<xsl:text>&#x09;</xsl:text>
				<xsl:value-of select="concat('rm -rf ',$dirname,'target')"/>
				<xsl:text>&#x0A;</xsl:text>
			</xsl:if>
		</xsl:result-document>
		<xsl:sequence select="$artifacts-and-dependencies"/>
	</xsl:template>
	
	<xsl:template match="pom:project" as="element()*">
		<xsl:param name="managed-internal-runtime-dependencies"/>
		<xsl:variable name="project" select="."/>
		<!--
		    only automatically selected profiles supported
		-->
		<xsl:for-each select="pom:parent|
		                      pom:dependencies/pom:dependency|
		                      pom:build/pom:plugins/pom:plugin|
		                      pom:build/pom:plugins/pom:plugin/pom:dependencies/pom:dependency|
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
							                 (pom:type/string(),'jar')[1]=(current()/pom:type/string(),'jar')[1] and
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
								<xsl:value-of select="($project/pom:dependencyManagement/pom:dependencies/pom:dependency
								                       [string(pom:groupId)=string(current()/pom:groupId) and
								                        string(pom:artifactId)=string(current()/pom:artifactId)]
								                       /(.[string(pom:classifier)=string(current()/pom:classifier)],.)
								                       /pom:version)[1]"/>
							</xsl:when>
							<xsl:otherwise>
								<!-- might be transitive dependency, but not supported here -->
								<xsl:message terminate="yes"
								             select="concat('error: artifact not found: ',
								                            string(pom:groupId),':',string(pom:artifactId))"/>
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
				<xsl:when test="ends-with($version, '-SNAPSHOT')">
					<xsl:variable name="type">
						<xsl:choose>
							<xsl:when test="self::pom:parent">
								<xsl:value-of select="'pom'"/>
							</xsl:when>
							<xsl:when test="pom:type">
								<xsl:value-of select="pom:type"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="'jar'"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:variable>
					<dependency>
						<groupId>
							<xsl:value-of select="pom:groupId"/>
						</groupId>
						<artifactId>
							<xsl:value-of select="pom:artifactId"/>
						</artifactId>
						<version>
							<xsl:value-of select="$version"/>
						</version>
						<type>
							<xsl:value-of select="$type"/>
						</type>
						<xsl:if test="pom:classifier">
							<classifier>
								<xsl:value-of select="pom:classifier"/>
							</classifier>
						</xsl:if>
					</dependency>
				</xsl:when>
				<!--
				    A non-snapshot dependency B of A may have a transitive dependency C who's
				    version is overwritten by Maven to a snapshot based on the
				    dependencyManagement of A.
				    
				    We try to support this use case here. We are making the assumption that that the
				    transitive dependencies of B as defined in the module (the snapshot) and the
				    transitive dependencies of the non-snapshot version of B are the same.
				-->
				<xsl:when test="self::pom:dependency and not(pom:scope='test') and not(pom:scope='provided')">
					<xsl:apply-templates select="$managed-internal-runtime-dependencies/pom:project[pom:groupId=$groupId and
					                                                                                pom:artifactId=$artifactId]">
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
		<xsl:value-of select="string-join((
		                        '$(MVN_LOCAL_REPOSITORY)/',
		                        translate($groupId,'.','/'),
		                        '/',$artifactId,
		                        '/',$version,
		                        '/',$artifactId,
		                        '-',$version,
		                        if ($classifier) then ('-',$classifier) else (),
		                        '.',$type),'')"/>
	</xsl:template>
	
</xsl:stylesheet>
