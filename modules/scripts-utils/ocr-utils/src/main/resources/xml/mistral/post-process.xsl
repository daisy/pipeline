<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="3.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:map="http://www.w3.org/2005/xpath-functions/map"
                xmlns="http://www.w3.org/1999/xhtml"
                xpath-default-namespace="http://www.w3.org/1999/xhtml">

	<xsl:param name="metadata" as="map(xs:string,xs:string)"/>
	<xsl:param name="image-descriptions" as="map(xs:string,xs:string)"/>
	<xsl:param name="image-text-content" as="map(xs:string,xs:string)"/>
	<xsl:param name="image-sizes" as="map(xs:string,xs:integer)"/>
	<xsl:param name="replace-images" as="xs:string*"/>
	<xsl:param name="font-size" as="xs:integer?"/>

	<xsl:include href="http://www.daisy.org/pipeline/modules/common-utils/generate-id.xsl"/>

	<xsl:template match="html" priority="1">
		<xsl:call-template name="pf:next-match-with-generated-ids">
			<xsl:with-param name="prefix" select="'img_desc_'"/>
			<xsl:with-param name="for-elements" select="//img[map:contains($image-text-content,@src)]"/>
		</xsl:call-template>
	</xsl:template>

	<!--
	    set language
	-->
	<xsl:template match="html">
		<xsl:copy>
			<xsl:choose>
				<xsl:when test="map:contains($metadata,'language')">
					<xsl:attribute name="lang" select="$metadata('language')"/>
					<xsl:attribute name="xml:lang" select="$metadata('language')"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:apply-templates select="@lang|@xml:lang"/>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:apply-templates select="@* except (@lang|@xml:lang)|node()"/>
		</xsl:copy>
	</xsl:template>

	<!--
	    insert title and author
	-->

	<xsl:template match="head">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
			<xsl:if test="map:contains($metadata,'author')">
				<xsl:element name="meta" namespace="http://www.w3.org/1999/xhtml">
					<xsl:attribute name="name" select="'author'"/>
					<xsl:attribute name="content" select="$metadata('author')"/>
				</xsl:element>
			</xsl:if>
			<xsl:element name="style" namespace="http://www.w3.org/1999/xhtml">
				<xsl:attribute name="style" select="'text/css'"/>
				<xsl:text>div[role=doc-pagebreak] {
   display: block;
   margin-top: 0.5em;
   margin-bottom: 0.5em;
   margin-left: auto;
   margin-right: auto;
   border-style: inset;
   border-width: 1px;
}
.hidden-image-desc {
   position: absolute;
   left: -9999px;
   width: 1px;
   height: 1px;
   overflow:hidden;
}
table, th, td {
   border: 1px solid black;
}
table {
   border-collapse: collapse;
}
</xsl:text>
			</xsl:element>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="head/title">
		<xsl:choose>
			<xsl:when test="map:contains($metadata,'title')">
				<xsl:copy>
					<xsl:value-of select="$metadata('title')"/>
				</xsl:copy>
			</xsl:when>
			<xsl:otherwise>
				<xsl:variable name="first-heading" as="element()?" select="(//h1|//h2|//h3|//h4|//h5|//h6)[1]"/>
				<xsl:choose>
					<xsl:when test="exists($first-heading)">
						<xsl:copy>
							<xsl:value-of select="string($first-heading)"/>
						</xsl:copy>
					</xsl:when>
					<xsl:otherwise>
						<xsl:next-match/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!--
	    drop certain image captions
	-->
	<xsl:template match="figure">
		<xsl:variable name="has-caption" as="xs:boolean"
		              select="child::figcaption
		                      and not(count(*)=2
		                              and figcaption[@aria-hidden='true']
		                                            [some $src in ..//img/@src satisfies
		                                             normalize-space(string(.))=$src])"/>
		<xsl:next-match>
			<xsl:with-param name="has-caption" tunnel="true" select="$has-caption"/>
		</xsl:next-match>
	</xsl:template>

	<xsl:template match="figure/figcaption">
		<xsl:param name="has-caption" as="xs:boolean" tunnel="true"/>
		<xsl:if test="$has-caption">
			<xsl:next-match/>
		</xsl:if>
	</xsl:template>

	<!--
	    make sure all images are wrapped in a figure element (no matter whether they have a caption, text content, or alt text)
	-->
	<xsl:template match="img[not(ancestor::figure)]">
		<xsl:element name="figure" namespace="http://www.w3.org/1999/xhtml">
			<xsl:next-match/>
		</xsl:element>
	</xsl:template>

	<!--
	    unwrap p around img
	-->
	<xsl:template match="p[img and count(*)=1 and not(text()[normalize-space(.)])]">
		<xsl:apply-templates/>
	</xsl:template>

	<!--
	    insert image descriptions and possibly mark images as decorative or drop them
	-->
	<xsl:template match="img">
		<xsl:param name="has-caption" as="xs:boolean" tunnel="true" select="false()"/>
		<xsl:variable name="replace" as="xs:boolean" select="@src=$replace-images and not($has-caption)"/>
		<xsl:if test="not($replace and map:contains($image-text-content,@src))">
			<xsl:copy>
				<xsl:choose>
					<xsl:when test="$replace">
						<xsl:attribute name="alt" select="''"/>
						<xsl:attribute name="role" select="'presentation'"/>
					</xsl:when>
					<xsl:when test="map:contains($image-descriptions,@src)">
						<xsl:attribute name="alt" select="$image-descriptions(@src)"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:apply-templates select="@alt"/>
					</xsl:otherwise>
				</xsl:choose>
				<xsl:if test="map:contains($image-sizes,@src)">
					<xsl:attribute name="width" select="$image-sizes(@src) div 2"/>
				</xsl:if>
				<xsl:apply-templates select="@* except @alt"/>
				<xsl:if test="map:contains($image-text-content,@src)">
					<xsl:variable name="id" as="attribute(id)">
						<xsl:call-template name="pf:generate-id"/>
					</xsl:variable>
					<xsl:attribute name="aria-describedby" select="string($id)"/>
				</xsl:if>
			</xsl:copy>
		</xsl:if>
		<xsl:if test="map:contains($image-text-content,@src)">
			<xsl:variable name="content" as="node()*">
				<xsl:try>
					<xsl:sequence select="parse-xml(concat(
					                                  '&lt;html xmlns=&quot;http://www.w3.org/1999/xhtml&quot;&gt;',
					                                  $image-text-content(@src),
					                                  '&lt;/html&gt;')
					                                )/*/node()"/>
					<xsl:catch>
						<xsl:element name="p" namespace="http://www.w3.org/1999/xhtml">
							<xsl:element name="em" namespace="http://www.w3.org/1999/xhtml">
								<xsl:element name="b" namespace="http://www.w3.org/1999/xhtml">
									<xsl:text>Conversion error</xsl:text>
								</xsl:element>
								<xsl:text>: HTML could not be parsed:</xsl:text>
							</xsl:element>
						</xsl:element>
						<xsl:element name="pre" namespace="http://www.w3.org/1999/xhtml">
							<xsl:element name="code" namespace="http://www.w3.org/1999/xhtml">
								<xsl:value-of select="$image-text-content(@src)"></xsl:value-of>
							</xsl:element>
						</xsl:element>
					</xsl:catch>
				</xsl:try>
			</xsl:variable>
			<xsl:choose>
				<xsl:when test="not($replace)">
					<!-- not using aside because that gives an inferior layout in Word -->
					<xsl:element name="div" namespace="http://www.w3.org/1999/xhtml">
						<xsl:call-template name="pf:generate-id"/>
						<xsl:attribute name="class" select="'hidden-image-desc'"/>
						<xsl:element name="p" namespace="http://www.w3.org/1999/xhtml">
							<xsl:element name="em" namespace="http://www.w3.org/1999/xhtml">
								<xsl:text>This image contains the following text content:</xsl:text>
							</xsl:element>
						</xsl:element>
						<xsl:sequence select="$content"/>
					</xsl:element>
				</xsl:when>
				<xsl:otherwise>
					<xsl:sequence select="$content"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
	</xsl:template>

	<!--
	    process page breaks
	-->
	<xsl:template match="hr[@role='doc-pagebreak']">
		<xsl:element name="div" namespace="http://www.w3.org/1999/xhtml">
			<xsl:apply-templates select="@*|node()"/>
		</xsl:element>
	</xsl:template>

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>
