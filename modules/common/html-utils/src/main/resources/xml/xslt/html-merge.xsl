<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:svg="http://www.w3.org/2000/svg"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:m="http://www.w3.org/1998/Math/MathML"
                xmlns="http://www.w3.org/1999/xhtml"
                xpath-default-namespace="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="#all">

	<xsl:template match="/">
		<html>
			<xsl:sequence select="(collection()/html)[1]/(@lang|@xml:lang)"/>
			<xsl:sequence select="(collection()/html)[1]/head"/>
			<xsl:call-template name="merge-sections">
				<xsl:with-param name="content" select="collection()/html/body"/>
			</xsl:call-template>
		</html>
	</xsl:template>

	<xsl:template name="merge-sections">
		<xsl:param name="content" as="node()*" required="yes"/>
		<!--
		    mark sections that are mergeable with the previous section(s)
		-->
		<xsl:variable name="mark-mergeable-sections" as="node()*">
			<xsl:for-each-group select="$content" group-adjacent="boolean(self::body|
			                                                              self::section|
			                                                              self::nav|
			                                                              self::article|
			                                                              self::aside|
			                                                              self::text()[normalize-space(.)=''])">
				<xsl:choose>
					<xsl:when test="current-grouping-key()">
						<xsl:variable name="sections" select="current-group()/self::*"/>
						<xsl:for-each-group select="current-group()" group-ending-with="*">
							<xsl:variable name="i" select="position()"/>
							<xsl:choose>
								<xsl:when test="$i=1 or $i>count($sections)">
									<xsl:sequence select="current-group()"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:variable name="attrs" as="attribute()*" select="$sections[$i]/(@* except @id)"/>
									<xsl:variable name="prev-attrs" as="attribute()*" select="$sections[$i - 1]/(@* except @id)"/>
									<xsl:choose>
										<!--
										    * no associated heading
										    * no preceding section in original document
										    * same element name
										    * not referenced
										    * same attributes (except id)
										-->
										<xsl:when test="collection()//d:section[@owner=$sections[$i]/@id][not(@heading|preceding::d:section)]
										                and name($sections[$i])=name($sections[$i - 1])
										                and not($sections[$i]/@id[.=$idrefs])
										                and count($attrs)=count($prev-attrs)
										                and (every $a in $attrs satisfies
										                  exists($prev-attrs[name()=name($a)][string()=string($a)]))">
											<d:mergeable>
												<xsl:sequence select="current-group() except $sections[$i]"/>
												<xsl:sequence select="$sections[$i]/node()"/>
											</d:mergeable>
										</xsl:when>
										<xsl:otherwise>
											<xsl:sequence select="current-group()"/>
										</xsl:otherwise>
									</xsl:choose>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:for-each-group>
					</xsl:when>
					<xsl:otherwise>
						<xsl:sequence select="current-group()"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each-group>
		</xsl:variable>
		<!--
		    perform the merge and apply the same actions on the child sections
		-->
		<xsl:for-each-group select="$mark-mergeable-sections" group-starting-with="node()[not(self::d:mergeable)]">
			<xsl:choose>
				<xsl:when test="count(current-group())=1">
					<xsl:apply-templates select="current-group()"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:copy copy-namespaces="no">
						<xsl:for-each select="namespace::*[not(.='http://www.daisy.org/ns/pipeline/data')]">
							<xsl:sequence select="."/>
						</xsl:for-each>
						<xsl:sequence select="@*"/>
						<xsl:call-template name="merge-sections">
							<xsl:with-param name="content" select="current-group()/node()"/>
						</xsl:call-template>
					</xsl:copy>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each-group>
	</xsl:template>

	<xsl:template match="@*|node()">
		<xsl:copy copy-namespaces="no">
			<xsl:for-each select="namespace::*[not(.='http://www.daisy.org/ns/pipeline/data')]">
				<xsl:sequence select="."/>
			</xsl:for-each>
			<xsl:apply-templates mode="#current" select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

	<!--
	    get all ID references within the HTML documents
	-->
	<xsl:variable name="idrefs" as="xs:string*">
		<xsl:variable name="idrefs" as="xs:string*">
			<xsl:apply-templates mode="idrefs" select="collection()/html"/>
		</xsl:variable>
		<xsl:sequence select="distinct-values($idrefs)"/>
	</xsl:variable>

	<!--
	    not all of these can point to an HTML element, but include them all anyway
	-->
	<xsl:template mode="idrefs"
	              as="xs:string?"
	              match="@aria-describedat  |
	                     @longdesc          |
	                     link/@href         |
	                     a/@href            |
	                     area/@href         |
	                     script/@scr        |
	                     img/@src           |
	                     iframe/@src        |
	                     embed/@src         |
	                     object/@data       |
	                     audio/@src         |
	                     video/@src         |
	                     source/@src        |
	                     track/@src         |
	                     input/@src         |
	                     input/@formaction  |
	                     button/@formaction |
	                     form/@action       |
	                     blockquote/@cite   |
	                     q/@cite            |
	                     ins/@cite          |
	                     del/@cite          |
	                     head/@profile      |
	                     svg:*/@xlink:href  |
	                     svg:*/@href        |
	                     m:math/@altimg     |
	                     m:mglyph/@src      ">
		<xsl:if test="starts-with(.,'#')">
			<xsl:sequence select="substring(.,2)"/>
		</xsl:if>
	</xsl:template>

	<xsl:template mode="idrefs" match="@*|node()" as="xs:string*">
		<xsl:apply-templates mode="#current" select="@*|node()"/>
	</xsl:template>

</xsl:stylesheet>
