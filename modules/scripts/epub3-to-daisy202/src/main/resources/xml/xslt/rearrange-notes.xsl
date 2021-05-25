<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="3.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:map="http://www.w3.org/2005/xpath-functions/map"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns="" xpath-default-namespace=""
                exclude-result-prefixes="#all">

	<xsl:import href="http://www.daisy.org/pipeline/modules/common-utils/generate-id.xsl"/>
	<xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>
	<xsl:import href="http://www.daisy.org/pipeline/modules/smil-utils/clock-functions.xsl"/>

	<xsl:key name="id" match="*[@id]" use="@id"/>

	<!--
	    mapping from noteref pars to sequence of associated note pars
	-->
	<xsl:variable name="noterefs" as="map(xs:string,xs:string*)"
	              select="parse-json(xml-to-json(collection()[last()]))"/>

	<xsl:template match="/" priority="1">
		<xsl:variable name="base-uri" select="pf:normalize-uri(pf:base-uri(/*))"/>
		<xsl:variable name="all-smils" select="collection()[position()&gt;1]"/>
		<xsl:variable name="notes-in-this-smil" as="xs:string*"
		              select="for $noteref in map:keys($noterefs) return
		                      for $note in $noterefs($noteref)[starts-with(.,concat($base-uri,'#'))] return
		                      substring-after($note,'#')"/>
		<xsl:variable name="noterefs-in-this-smil" as="map(xs:string,element()*)">
			<xsl:map>
				<xsl:for-each select="map:keys($noterefs)[starts-with(.,concat($base-uri,'#'))]">
					<xsl:variable name="noteref" select="."/>
					<xsl:for-each select="$noterefs($noteref)">
						<xsl:variable name="file-uri" select="substring-before(.,'#')"/>
						<xsl:variable name="id" select="substring-after(.,'#')"/>
						<xsl:map-entry key="substring-after($noteref,'#')"
						               select="$all-smils[pf:normalize-uri(pf:base-uri(/*))=$file-uri]/key('id',$id)"/>
					</xsl:for-each>
				</xsl:for-each>
			</xsl:map>
		</xsl:variable>
		<xsl:variable name="notes-referenced-in-this-smil" as="element()*"
		              select="for $ref in map:keys($noterefs-in-this-smil) return $noterefs-in-this-smil($ref)"/>
		<xsl:call-template name="pf:next-match-with-generated-ids">
			<xsl:with-param name="prefix" select="'id_'"/>
			<xsl:with-param name="for-elements"
			                select="($notes-referenced-in-this-smil except //*)/descendant-or-self::*[@id]"/>
			<xsl:with-param name="notes-in-this-smil" tunnel="yes" select="$notes-in-this-smil"/>
			<xsl:with-param name="noterefs-in-this-smil" tunnel="yes" select="$noterefs-in-this-smil"/>
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="/">
		<xsl:param name="notes-in-this-smil" tunnel="yes"/>
		<xsl:param name="noterefs-in-this-smil" tunnel="yes"/>
		<xsl:variable name="rearranged" as="document-node()">
			<xsl:document>
				<xsl:next-match/>
			</xsl:document>
		</xsl:variable>
		<xsl:apply-templates mode="recompute-dur" select="$rearranged"/>
		<xsl:variable name="base-uri" select="pf:normalize-uri(pf:base-uri(/*))"/>
		<xsl:variable name="notes-in-this-smil" as="element()*"
		              select="for $note in $notes-in-this-smil return key('id',$note)"/>
		<xsl:variable name="notes-referenced-in-this-smil" as="element()*"
		              select="for $ref in map:keys($noterefs-in-this-smil) return $noterefs-in-this-smil($ref)"/>
		<xsl:result-document href="mapping">
			<d:fileset>
				<xsl:for-each-group select="$notes-referenced-in-this-smil except //*" group-by="pf:base-uri(.)">
					<d:file href="{$base-uri}" original-href="{current-grouping-key()}">
						<xsl:for-each select="current-group()/descendant-or-self::*[@id]">
							<d:anchor original-id="{@id}">
								<xsl:call-template name="pf:generate-id"/>
							</d:anchor>
						</xsl:for-each>
					</d:file>
				</xsl:for-each-group>
				<d:file href="{$base-uri}">
					<xsl:for-each select="(//*[@id] except $notes-in-this-smil/descendant-or-self::*,
					                       ($notes-referenced-in-this-smil intersect //*)/descendant-or-self::*[@id])">
						<d:anchor id="{@id}"/>
					</xsl:for-each>
				</d:file>
			</d:fileset>
		</xsl:result-document>
	</xsl:template>

	<xsl:template match="par[@id]">
		<xsl:param name="notes-in-this-smil" tunnel="yes"/>
		<xsl:param name="noterefs-in-this-smil" tunnel="yes"/>
		<xsl:choose>
			<xsl:when test="@id=$notes-in-this-smil">
				<!--
				    omit note here
				-->
			</xsl:when>
			<xsl:when test="@id=map:keys($noterefs-in-this-smil)">
				<xsl:next-match/>
				<!--
				    insert note after noteref
				-->
				<xsl:for-each select="$noterefs-in-this-smil(@id)">
					<xsl:choose>
						<xsl:when test=". intersect collection()[1]//*">
							<xsl:sequence select="."/>
						</xsl:when>
						<xsl:otherwise>
							<!-- par is coming from other smil, so generate a new ids to ensure
							     there are no conflicts -->
							<xsl:apply-templates mode="generate-new-ids" select="."/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:for-each>
			</xsl:when>
			<xsl:otherwise>
				<xsl:next-match/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template mode="generate-new-ids" match="@id">
		<xsl:for-each select="..">
			<xsl:call-template name="pf:generate-id"/>
		</xsl:for-each>
	</xsl:template>

	<!-- there should only be a dur attribute on the root seq -->
	<xsl:template mode="recompute-dur" match="/smil/body/seq/@dur">
		<xsl:variable name="total-time" as="xs:double"
		              select="sum(for $audio in //audio
		                          return pf:mediaoverlay-clock-value-to-seconds($audio/@clip-end)
		                               - pf:mediaoverlay-clock-value-to-seconds($audio/@clip-begin))"/>
		<xsl:attribute name="dur" select="pf:mediaoverlay-seconds-to-timecount($total-time,'s')"/>
	</xsl:template>

	<xsl:template mode="recompute-dur" match="/smil/body/seq">
		<xsl:copy>
			<xsl:apply-templates mode="#current" select="@*"/>
			<xsl:sequence select="node()"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template mode="#default generate-new-ids recompute-dur" match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates mode="#current" select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>
