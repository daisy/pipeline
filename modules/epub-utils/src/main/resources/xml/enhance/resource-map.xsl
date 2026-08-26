<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="3.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:opf="http://www.idpf.org/2007/opf"
                xmlns:epub="http://www.idpf.org/2007/ops"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="#all">
	
	<xsl:include href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>
	
	<xsl:param name="default-rendition.package-document" required="yes" as="document-node(element(opf:package))"/>
	<xsl:param name="braille-rendition.package-document" required="yes" as="document-node(element(opf:package))"/>
	
	<xsl:param name="default-rendition.html" required="yes" as="document-node(element(html:html))*"/>
	<xsl:param name="braille-rendition.html" required="yes" as="document-node(element(html:html))*"/>

	<xsl:param name="output-base-uri" required="yes" as="xs:string"/>

	<xsl:key name="sync-point" match="*[@class='__tmp__sync__'][@id]" use="string(@id)"/>
	
	<xsl:template name="main">
		<html>
			<head>
				<meta charset="utf-8"/>
			</head>
			<body>
				<nav epub:type="resource-map">
					<xsl:for-each select="$default-rendition.html">
						<xsl:variable name="base" select="base-uri(/*)"/>
						<xsl:variable name="manifest-item" as="element()"
						              select="$default-rendition.package-document//opf:manifest/opf:item[resolve-uri(@href,base-uri(.))=$base]"/>
						<xsl:variable name="spine-itemref" as="element()?"
						              select="$default-rendition.package-document//opf:spine/opf:itemref[@idref=$manifest-item/@id]"/>
						<xsl:if test="$spine-itemref"> <!-- some itemrefs can be missing, for instance the navigation document -->
							<xsl:variable name="cfipath" as="xs:string"
							              select="string-join((
							                        pf:relativize-uri(base-uri($default-rendition.package-document/*),$output-base-uri),
							                        '#epubcfi(',
							                        epub:cfipath($spine-itemref),
							                        for $id in $spine-itemref/@id return ('[',$id,']'),
							                        '!'),'')"/>
							<!-- assume that there is one and only one corresponding HTML file in the braille rendition  -->
							<xsl:variable name="braille-base" as="xs:string?">
								<xsl:iterate select="//*[@class='__tmp__sync__'][@id]">
									<xsl:variable name="id" as="xs:string" select="@id"/>
									<xsl:variable name="id" as="attribute()?" select="$braille-rendition.html/key('sync-point',$id)/@id"/>
									<xsl:choose>
										<xsl:when test="exists($id)">
											<xsl:break select="base-uri(root($id))"/>
										</xsl:when>
										<xsl:otherwise>
											<xsl:next-iteration/>
										</xsl:otherwise>
									</xsl:choose>
								</xsl:iterate>
							</xsl:variable>
							<xsl:if test="$braille-base">
								<xsl:variable name="manifest-item" as="element()"
								              select="$braille-rendition.package-document//opf:manifest/opf:item[resolve-uri(@href,base-uri(.))=$braille-base]"/>
								<xsl:variable name="spine-itemref" as="element()"
								              select="$braille-rendition.package-document//opf:spine/opf:itemref[@idref=$manifest-item/@id]"/>
								<xsl:variable name="braille-cfipath" as="xs:string"
								              select="string-join((
								                        pf:relativize-uri(base-uri($braille-rendition.package-document/*),$output-base-uri),
								                        '#epubcfi(',
								                        epub:cfipath($spine-itemref),
								                        for $id in $spine-itemref/@id return ('[',$id,']'),
								                        '!'),'')"/>
								<xsl:for-each select="//*[@class='__tmp__sync__'][@id]">
									<xsl:variable name="id" as="xs:string" select="@id"/>
									<xsl:variable name="id" as="attribute()?" select="$braille-rendition.html/key('sync-point',$id)[1]/@id"/>
									<xsl:if test="$id">
										<ul>
											<li>
												<a href="{concat($cfipath,epub:cfipath(.),')')}"/>
											</li>
											<li>
												<a href="{concat($braille-cfipath,epub:cfipath($id/parent::*),')')}"/>
											</li>
										</ul>
									</xsl:if>
								</xsl:for-each>
							</xsl:if>
						</xsl:if>
					</xsl:for-each>
				</nav>
			</body>
		</html>
	</xsl:template>
	
	<xsl:function name="epub:cfipath" as="xs:string">
		<xsl:param name="node" as="node()"/>
		<!-- the following assumes that an element with @class='__tmp__sync__' has not preceding sibling elements-->
		<xsl:sequence select="string-join(('',
		                        (for $n in $node/(ancestor::*|self::node()) return
		                           if ($n/@class='__tmp__sync__')
		                           then string-join(('1',
		                                             $n/preceding-sibling::text()[last()][not(.='')]/string-length(string(.))),
		                                            ':')
		                           else string(2*count($n/(preceding-sibling::*[not(@class='__tmp__sync__')]|self::*))
		                                       +count($n/self::text())))
		                         [position()&gt;1]),
		                        '/')"/>
	</xsl:function>
	
</xsl:stylesheet>
