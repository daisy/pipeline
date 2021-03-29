<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="3.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:map="http://www.w3.org/2005/xpath-functions/map"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns="" xpath-default-namespace=""
                exclude-result-prefixes="#all">

	<xsl:import href="http://www.daisy.org/pipeline/modules/html-utils/library.xsl"/>

	<xsl:key name="id" match="*[@id]" use="@id"/>
	<xsl:key name="href" match="html:a[@href]" use="@href"/>
	<xsl:key name="absolute-id" match="*[@id]" use="concat(pf:normalize-uri(pf:html-base-uri(.)),'#',@id)"/>

	<xsl:variable name="smil-base-uri" select="pf:base-uri(/*)"/>

	<!--
	    mapping from noteref pars to sequence of associated note pars
	-->
	<xsl:variable name="noterefs" as="map(xs:string,xs:string*)">
		<xsl:map>
			<xsl:for-each select="collection()/html:*">
				<xsl:variable name="content-doc" select="."/>
				<xsl:variable name="content-doc-uri" select="pf:normalize-uri(base-uri(.))"/>
				<xsl:for-each select="collection()[2]
				                      //d:file[pf:normalize-uri(resolve-uri(@href,base-uri(.)))=$content-doc-uri]
				                      /d:anchor">
					<xsl:variable name="noteref-element" as="element()?" select="key('id',@id,$content-doc)"/>
					<!--
					    for now only note references within the same (HTML and SMIL) document are supported
					-->
					<xsl:if test="$noteref-element/self::html:a[starts-with(@href,'#')]">
						<xsl:variable name="noteref-linkbacks" as="xs:string*"
						              select="$noteref-element/descendant::html:a
						                        [matches(@href,'.+\.[Ss][Mm][Ii][Ll]#.+$')]
						                        [starts-with(resolve-uri(@href,$content-doc-uri),concat($smil-base-uri,'#'))]
						                      /substring-after(@href,'#')"/>
						<xsl:if test="exists($noteref-linkbacks)">
							<xsl:variable name="note-element" as="element()?"
							              select="key('id',substring($noteref-element/@href,2),$content-doc)"/>
							<xsl:if test="exists($note-element)">
								<!--
								    a note could in theory be referenced by more than one noteref
								-->
								<xsl:if test="key('href',concat('#',$note-element/@id),$content-doc) is $noteref-element">
									<xsl:variable name="note-linkbacks" as="xs:string*"
									              select="$note-element/descendant::html:a
									                        [matches(@href,'.+\.[Ss][Mm][Ii][Ll]#.+$')]
									                        [starts-with(resolve-uri(@href,$content-doc-uri),concat($smil-base-uri,'#'))]
									                      /substring-after(@href,'#')"/>
									<xsl:if test="exists($note-linkbacks)">
										<xsl:map-entry key="$noteref-linkbacks[last()]" select="$note-linkbacks"/>
									</xsl:if>
								</xsl:if>
							</xsl:if>
						</xsl:if>
					</xsl:if>
				</xsl:for-each>
			</xsl:for-each>
		</xsl:map>
	</xsl:variable>

	<xsl:variable name="notes" as="xs:string*"
	              select="for $ref in map:keys($noterefs) return $noterefs($ref)"/>

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="par[@id]">
		<xsl:choose>
			<xsl:when test="@id=$notes">
				<!--
				    omit note here
				-->
			</xsl:when>
			<xsl:when test="map:contains($noterefs,@id)">
				<xsl:next-match/>
				<!--
				    insert note after noteref
				-->
				<xsl:sequence select="for $note in $noterefs(@id) return key('id',$note)"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:next-match/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

</xsl:stylesheet>
