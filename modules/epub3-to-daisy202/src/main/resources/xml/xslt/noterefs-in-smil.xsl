<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="3.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:map="http://www.w3.org/2005/xpath-functions/map"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns="" xpath-default-namespace=""
                exclude-result-prefixes="#all">

	<!--
	    compute mapping from noteref pars to sequence of associated note pars
	-->

	<xsl:import href="http://www.daisy.org/pipeline/modules/html-utils/library.xsl"/>

	<xsl:variable name="smil" select="collection()[/smil]"/>
	<xsl:variable name="html" select="collection()[/html:*]"/>

	<xsl:key name="id" match="*[@id]" use="@id"/>
	<xsl:key name="absolute-href" match="html:a[@href]" use="pf:normalize-uri(resolve-uri(@href,pf:html-base-uri(.)))"/>
	<xsl:key name="absolute-src" match="par[text]" use="pf:normalize-uri(text/resolve-uri(@src,base-uri(.)))"/>

	<xsl:template match="/d:fileset">
		<xsl:variable name="noterefs" select="."/>
		<xsl:variable name="noterefs" as="map(xs:string,xs:string*)">
			<xsl:map>
				<xsl:for-each select="$noterefs//d:file">
					<xsl:variable name="html-uri" select="pf:normalize-uri(resolve-uri(@href,base-uri(.)))"/>
					<xsl:for-each select="d:anchor">
						<xsl:variable name="noteref-id" as="xs:string" select="@id"/>
						<xsl:variable name="noteref-element" as="element()?"
						              select="$html[pf:normalize-uri(pf:html-base-uri(/*))=$html-uri]/key('id',$noteref-id)"/>
						<xsl:if test="exists($noteref-element)">
							<xsl:variable name="noteref-par-elements" as="element()*"
							              select="for $id in $noteref-element/descendant-or-self::*/@id
							                      return $smil/key('absolute-src',concat($html-uri,'#',$id))"/>
							<xsl:if test="exists($noteref-par-elements)">
								<xsl:variable name="note-uri" select="$noteref-element/resolve-uri(@href,pf:html-base-uri(.))"/>
								<xsl:variable name="note-file-uri" select="substring-before($note-uri,'#')"/>
								<xsl:variable name="note-id" select="substring-after($note-uri,'#')"/>
								<xsl:variable name="note-element" as="element()?"
								              select="$html[pf:normalize-uri(pf:html-base-uri(/*))=$note-file-uri]/key('id',$note-id)"/>
								<xsl:if test="exists($note-element)">
									<!--
									    a note could in theory be referenced by more than one noteref
									-->
									<xsl:if test="$html/key('absolute-href',$note-uri) is $noteref-element">
										<xsl:variable name="note-par-elements" as="element()*"
										              select="for $id in $note-element/descendant-or-self::*/@id
										                      return $smil/key('absolute-src',concat($note-file-uri,'#',$id))"/>
										<xsl:if test="exists($note-par-elements)">
											<!-- all pars have an @id after pxi:create-ncc -->
											<xsl:map-entry key="$noteref-par-elements[last()]/pf:normalize-uri(concat(base-uri(.),'#',@id))"
											               select="$note-par-elements/pf:normalize-uri(concat(base-uri(.),'#',@id))"/>
										</xsl:if>
									</xsl:if>
								</xsl:if>
							</xsl:if>
						</xsl:if>
					</xsl:for-each>
				</xsl:for-each>
			</xsl:map>
		</xsl:variable>
		<xsl:sequence select="json-to-xml(serialize($noterefs, map{'method':'json'}))"/>
	</xsl:template>

</xsl:stylesheet>
