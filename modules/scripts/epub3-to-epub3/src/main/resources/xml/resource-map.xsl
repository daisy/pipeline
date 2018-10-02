<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:opf="http://www.idpf.org/2007/opf"
                xmlns:epub="http://www.idpf.org/2007/ops"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="#all"
                version="2.0">
	
	<xsl:include href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>
	
	<xsl:param name="default-rendition.html.base"/>
	<xsl:param name="braille-rendition.html.base"/>
	<xsl:param name="rendition-mapping.base"/>
	
	<xsl:variable name="default-rendition.package-document" select="collection()[1]"/>
	<xsl:variable name="braille-rendition.package-document" select="collection()[2]"/>
	
	<xsl:variable name="default-rendition.manifest-item" as="element()"
	              select="$default-rendition.package-document//opf:manifest/opf:item[resolve-uri(@href,base-uri(.))=$default-rendition.html.base]"/>
	<xsl:variable name="default-rendition.spine-itemref" as="element()?"
	              select="$default-rendition.package-document//opf:spine/opf:itemref[@idref=$default-rendition.manifest-item/@id]"/>
	
	<xsl:variable name="braille-rendition.manifest-item" as="element()"
	              select="$braille-rendition.package-document//opf:manifest/opf:item[resolve-uri(@href,base-uri(.))=$braille-rendition.html.base]"/>
	<xsl:variable name="braille-rendition.spine-itemref" as="element()?"
	              select="$braille-rendition.package-document//opf:spine/opf:itemref[@idref=$braille-rendition.manifest-item/@id]"/>
	
	<xsl:template name="main">
		<!--
		    some itemrefs can be missing, for instance the navigation document
		-->
		<xsl:if test="$default-rendition.spine-itemref and $braille-rendition.spine-itemref">
			<nav epub:type="resource-map">
				<xsl:attribute name="xml:base" select="$rendition-mapping.base"/>
				<ul>
					<li>
						<a href="{string-join((
						            pf:relativize-uri(base-uri($default-rendition.package-document/*),$rendition-mapping.base),
						            '#epubcfi(',
						            epub:cfipath($default-rendition.spine-itemref),
						            for $id in $default-rendition.spine-itemref/@id return ('[',$id,']'),
						            ')'),'')}"/>
					</li>
					<li>
						<a href="{string-join((
						            pf:relativize-uri(base-uri($braille-rendition.package-document/*),$rendition-mapping.base),
						            '#epubcfi(',
						            epub:cfipath($braille-rendition.spine-itemref),
						            for $id in $braille-rendition.spine-itemref/@id return ('[',$id,']'),
						            ')'),'')}"/>
					</li>
				</ul>
			</nav>
		</xsl:if>
	</xsl:template>
	
	<xsl:function name="epub:cfipath" as="xs:string">
		<xsl:param name="node" as="node()"/>
		<xsl:sequence select="string-join(('',
		                        (for $n in $node/(ancestor::*|self::node()) return
		                           string(2*count($n/(preceding-sibling::*|self::*))+count($n/self::text())))
		                         [position()&gt;1]),
		                        '/')"/>
	</xsl:function>
	
</xsl:stylesheet>
