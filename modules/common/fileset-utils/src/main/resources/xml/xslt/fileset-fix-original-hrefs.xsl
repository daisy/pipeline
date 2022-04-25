<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                exclude-result-prefixes="#all">

	<xsl:include href="http://www.daisy.org/pipeline/modules/common-utils/library.xsl"/>
	<xsl:include href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>

	<xsl:param name="detect-existing" as="xs:boolean" required="yes"/>
	<xsl:param name="fail-on-missing" as="xs:boolean" required="yes"/>
	<xsl:param name="purge" as="xs:boolean" required="yes"/>
	<xsl:param name="in-memory-fileset" as="element(d:fileset)" required="yes"/>

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

	<!--
		Delete original-href that equal href
	-->
	<xsl:template match="d:file/@original-href[.=parent::*/@href/resolve-uri(.,base-uri(.))]"/>

	<xsl:template match="d:file">
		<xsl:variable name="href" select="@href/resolve-uri(.,base-uri(.))"/>
		<xsl:variable name="original-href" select="@original-href"/>
		<xsl:choose>
			<!--
			    Remove original-href if file exists in memory
			-->
			<xsl:when test="$in-memory-fileset//d:file/@href[resolve-uri(.,base-uri(.))=$href]">
				<xsl:copy>
					<xsl:apply-templates select="@* except @original-href"/>
					<xsl:apply-templates/>
				</xsl:copy>
			</xsl:when>
			<xsl:otherwise>
				<!--
				    else if file exists on disk, set original-href
				-->
				<xsl:variable name="href-on-disk" as="xs:boolean"
				              select="$detect-existing and pf:file-exists(replace($href,'^(jar|bundle):',''))"/>
				<xsl:if test="$detect-existing">
					<xsl:call-template name="pf:debug">
						<xsl:with-param name="msg">File at {} {}</xsl:with-param>
						<xsl:with-param name="args" select="($href,if ($href-on-disk) then 'exists' else 'does not exist')"/>
					</xsl:call-template>
				</xsl:if>
				<xsl:choose>
					<xsl:when test="$href-on-disk">
						<xsl:copy>
							<xsl:apply-templates select="@* except @original-href"/>
							<xsl:attribute name="original-href" select="$href"/>
							<xsl:apply-templates/>
						</xsl:copy>
					</xsl:when>
					<!--
					    else remove original-href or remove file if purge is set
					-->
					<xsl:when test="$original-href!=''">
						<xsl:variable name="original-href-on-disk" as="xs:boolean"
						              select="pf:file-exists(replace($original-href,'^(jar|bundle):',''))"/>
						<xsl:call-template name="pf:debug">
							<xsl:with-param name="msg">File at {} {}</xsl:with-param>
							<xsl:with-param name="args" select="($original-href,if ($original-href-on-disk) then 'exists' else 'does not exist')"/>
						</xsl:call-template>
						<xsl:choose>
							<xsl:when test="not($original-href-on-disk)">
								<xsl:variable name="message"
								              select="concat('Found document in fileset that was declared as being on disk ',
								                             'but was neither stored on disk nor in memory: ', $original-href)"/>
								<xsl:choose>
									<xsl:when test="$fail-on-missing">
										<xsl:call-template name="pf:error">
											<xsl:with-param name="msg" select="$message"/>
											<xsl:with-param name="code" select="QName('','PEZE01')"/>
										</xsl:call-template>
									</xsl:when>
									<xsl:when test="$purge">
										<xsl:call-template name="pf:warn">
											<xsl:with-param name="msg" select="$message"/>
										</xsl:call-template>
									</xsl:when>
									<xsl:otherwise>
										<xsl:call-template name="pf:warn">
											<xsl:with-param name="msg" select="$message"/>
										</xsl:call-template>
										<xsl:copy>
											<xsl:apply-templates select="@* except @original-href"/>
											<xsl:apply-templates/>
										</xsl:copy>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:when>
							<xsl:otherwise>
								<xsl:next-match/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:when>
					<xsl:otherwise>
						<xsl:variable name="message"
						              select="concat('Found document in fileset that is neither stored on disk nor in memory: ', $href)"/>
						<xsl:choose>
							<xsl:when test="$fail-on-missing">
								<xsl:call-template name="pf:error">
									<xsl:with-param name="msg" select="$message"/>
									<xsl:with-param name="code" select="QName('','PEZE00')"/>
								</xsl:call-template>
							</xsl:when>
							<xsl:when test="$purge">
								<xsl:call-template name="pf:warn">
									<xsl:with-param name="msg" select="$message"/>
								</xsl:call-template>
							</xsl:when>
							<xsl:otherwise>
								<xsl:call-template name="pf:warn">
									<xsl:with-param name="msg" select="$message"/>
								</xsl:call-template>
								<xsl:next-match/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

</xsl:stylesheet>
