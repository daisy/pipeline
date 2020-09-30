<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                exclude-result-prefixes="#all">

	<xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>

	<xsl:template match="/d:fileset">
		<!--
		    normalize to sequence of d:anchor with absolute href and id
		-->
		<xsl:variable name="page-list" as="element(d:fileset)">
			<xsl:apply-templates mode="normalize" select=".">
				<xsl:with-param name="base" tunnel="yes" select="base-uri(.)"/>
			</xsl:apply-templates>
		</xsl:variable>
		<xsl:variable name="page-list" as="element(d:anchor)*" select="$page-list//d:anchor"/>
		<!--
		    update href and id
		-->
		<xsl:variable name="mapping" as="element(d:fileset)">
			<xsl:for-each select="collection()[2]/*">
				<xsl:apply-templates mode="normalize" select=".">
					<xsl:with-param name="base" tunnel="yes" select="base-uri(.)"/>
				</xsl:apply-templates>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="page-list" as="element(d:anchor)*">
			<xsl:for-each select="$page-list">
				<xsl:copy>
					<xsl:variable name="file" as="xs:string" select="@href"/>
					<xsl:variable name="fragment" as="xs:string" select="@id"/>
					<xsl:variable name="new-file" as="element(d:file)*" select="$mapping/d:file[@original-href=$file]"/>
					<xsl:variable name="new-file" as="element(d:file)?" select="($new-file[d:anchor[(@original-id,@id)[1]=$fragment]],
					                                                             $new-file)[1]"/>
					<xsl:variable name="new-fragment" as="xs:string?" select="if (exists($new-file))
					                                                          then $new-file/d:anchor[(@original-id,@id)[1]=$fragment]/@id
					                                                          else $mapping/d:file[not(@original-href)][@href=$file][1]
					                                                                       /d:anchor[(@original-id,@id)[1]=$fragment]/@id"/>
					<xsl:variable name="new-file" as="xs:string?" select="$new-file/@href"/>
					<xsl:attribute name="href" select="($new-file,$file)[1]"/>
					<xsl:attribute name="id" select="($new-fragment,$fragment)[1]"/>
				</xsl:copy>
			</xsl:for-each>
		</xsl:variable>
		<!--
		    group d:anchor with same href
		-->
		<xsl:copy>
			<xsl:sequence select="@xml:base"/>
			<xsl:for-each-group select="$page-list" group-by="@href">
				<d:file>
					<xsl:sequence select="@href"/>
					<xsl:for-each select="current-group()">
						<xsl:copy>
							<xsl:sequence select="@id"/>
						</xsl:copy>
					</xsl:for-each>
				</d:file>
			</xsl:for-each-group>
		</xsl:copy>
	</xsl:template>

	<xsl:template mode="normalize"
	              match="/d:fileset|
	                     /d:fileset/d:file/d:anchor/@id|
	                     /d:fileset/d:file/d:anchor/@original-id">
		<xsl:copy>
			<xsl:apply-templates mode="#current" select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template mode="normalize"
	              match="/d:fileset/d:file[@href]">
		<xsl:variable name="normalized-file" as="element(d:file)">
			<xsl:copy>
				<xsl:variable name="normalized-attrs" as="attribute()*">
					<xsl:apply-templates mode="#current" select="@*"/>
				</xsl:variable>
				<xsl:sequence select="$normalized-attrs"/>
				<xsl:if test="not(@original-href)">
					<xsl:attribute name="original-href" select="$normalized-attrs[name()='href']"/>
				</xsl:if>
			</xsl:copy>
		</xsl:variable>
		<xsl:sequence select="$normalized-file"/>
		<xsl:apply-templates mode="#current">
			<xsl:with-param name="normalized-parent" select="$normalized-file"/>
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template mode="normalize"
	              match="/d:fileset/d:file/d:anchor[@id]">
		<xsl:param name="normalized-parent" as="element(d:file)" required="yes"/>
		<xsl:copy>
			<xsl:apply-templates mode="#current" select="@*"/>
			<xsl:if test="not(@original-id)">
				<xsl:attribute name="original-id" select="@id"/>
			</xsl:if>
			<xsl:sequence select="$normalized-parent/(@href|@original-href)"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template mode="normalize"
	              match="d:fileset/@xml:base|
	                     d:file/@href|
	                     d:file/@original-href">
		<xsl:param name="base" tunnel="yes" required="yes"/>
		<xsl:attribute name="{name()}" select="pf:normalize-uri(resolve-uri(.,$base))"/>
	</xsl:template>

	<xsl:template mode="normalize" match="@*|node()" priority="0.4"/>

</xsl:stylesheet>
