<?xml version="1.0" encoding="UTF-8"?>
<!-- <xsl:package version="3.0" -->
<!--              name="http://www.daisy.org/pipeline/modules/css-utils/library.xsl" -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
             xmlns:xs="http://www.w3.org/2001/XMLSchema"
             xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
             xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions"
             xmlns:d="http://www.daisy.org/ns/pipeline/data"
             xmlns:c="http://www.w3.org/ns/xproc-step"
             xmlns:java="implemented-in-java"
             exclude-result-prefixes="#all"
             >

	<!-- <xsl:expose component="function" names="pf:css-to-fileset" visibility="public"/> -->

	<xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>

	<xsl:function name="pf:css-to-fileset" as="element(d:fileset)">
		<xsl:param name="css-uri" as="xs:string"/>
		<xsl:param name="context.fileset" as="document-node(element(d:fileset))?"/>
		<xsl:param name="context.in-memory" as="document-node()*"/>
		<xsl:variable name="css" as="xs:string?" select="f:load($css-uri,$context.fileset,$context.in-memory)"/>
		<d:fileset>
			<xsl:attribute name="xml:base" select="$css-uri"/>
			<xsl:for-each select="($css-uri,
			                       if ($css) then pf:css-to-fileset($css,$css-uri,$context.fileset,$context.in-memory)
			                       else ())">
				<xsl:variable name="href" select="."/>
				<xsl:variable name="file-in-context" as="element(d:file)?"
							  select="$context.fileset//d:file[resolve-uri(@href,base-uri(.))=$href][1]"/>
				<d:file href="{pf:relativize-uri($href,$css-uri)}">
					<xsl:choose>
						<xsl:when test="$file-in-context[@original-href]">
							<xsl:sequence select="$file-in-context/@original-href"/>
						</xsl:when>
						<xsl:when test="$file-in-context"/>
						<xsl:otherwise>
							<xsl:attribute name="original-href" select="$href"/>
						</xsl:otherwise>
					</xsl:choose>
					<xsl:if test="pf:get-extension($href)='css'">
						<xsl:attribute name="media-type" select="'text/css'"/>
					</xsl:if>
				</d:file>
			</xsl:for-each>
		</d:fileset>
	</xsl:function>

	<xsl:function name="pf:css-to-fileset" as="xs:string*">
		<xsl:param name="css" as="xs:string"/>
		<xsl:param name="css-base" as="xs:string"/>
		<xsl:param name="context.fileset" as="document-node(element(d:fileset))?"/>
		<xsl:param name="context.in-memory" as="document-node()*"/>
		<xsl:analyze-string select="$css" regex="@import\s+(.*?)(\s*|\s+[^)].*);">
			<xsl:matching-substring>
				<xsl:variable name="url" select="f:parse-css-url(replace(regex-group(1),'^url\(\s*(.*?)\s*\)$','$1'))"/>
				<xsl:if test="$url">
					<xsl:variable name="url" select="resolve-uri($url,$css-base)"/>
					<!-- TODO: remove query fragments -->
					<xsl:sequence select="$url"/>
					<xsl:if test="pf:get-extension($url)='css'">
						<xsl:variable name="css" as="xs:string?"
						              select="f:load($url,$context.fileset,$context.in-memory)"/>
						<xsl:if test="exists($css)">
							<xsl:sequence select="pf:css-to-fileset($css,$url,$context.fileset,$context.in-memory)"/>
						</xsl:if>
					</xsl:if>
				</xsl:if>
			</xsl:matching-substring>
			<xsl:non-matching-substring>
				<xsl:analyze-string select="." regex="url\(\s*(.*?)\s*\)">
					<xsl:matching-substring>
						<xsl:variable name="url" select="f:parse-css-url(regex-group(1))"/>
						<xsl:if test="$url">
							<xsl:variable name="url" select="resolve-uri($url,$css-base)"/>
							<xsl:sequence select="$url"/>
							<xsl:if test="pf:get-extension($url)='css'">
								<xsl:variable name="css" as="xs:string?"
								              select="f:load($url,$context.fileset,$context.in-memory)"/>
								<xsl:if test="exists($css)">
									<xsl:sequence select="pf:css-to-fileset($css,$url,$context.fileset,$context.in-memory)"/>
								</xsl:if>
							</xsl:if>
						</xsl:if>
					</xsl:matching-substring>
				</xsl:analyze-string>
			</xsl:non-matching-substring>
		</xsl:analyze-string>
	</xsl:function>

	<xsl:function name="f:parse-css-url" as="xs:string?">
		<xsl:param name="url" as="xs:string"/>
		<xsl:sequence select="if (matches($url,'''(.*?)''')) then replace($url,'''(.*?)''','$1')
		                      else if (matches($url,'&quot;(.*?)&quot;')) then replace($url,'&quot;(.*?)&quot;','$1')
		                      else if (matches($url,'^[^''&quot;]') and not(matches($url,'^attr\('))) then $url
		                      else ()"
		              />
	</xsl:function>

	<xsl:function name="f:load" as="xs:string?">
		<xsl:param name="url" as="xs:string"/>
		<xsl:param name="context.fileset" as="document-node(element(d:fileset))?"/>
		<xsl:param name="context.in-memory" as="document-node()*"/>
		<xsl:variable name="file" as="element(d:file)?"
		              select="$context.fileset//d:file[resolve-uri(@href,base-uri(.))=$url][1]"/>
		<xsl:choose>
			<xsl:when test="$file[@original-href]">
				<xsl:variable name="url" select="$file/resolve-uri(@original-href,base-uri(.))"/>
				<xsl:if test="unparsed-text-available($url)">
					<xsl:sequence select="unparsed-text($url)"/>
				</xsl:if>
			</xsl:when>
			<xsl:when test="$file">
				<xsl:sequence select="$context.in-memory[/c:data][base-uri(/*)=$url][1]/string(.)"/>
			</xsl:when>
			<xsl:when test="unparsed-text-available($url)">
				<xsl:sequence select="unparsed-text($url)"/>
			</xsl:when>
		</xsl:choose>
	</xsl:function>

	<doc xmlns="http://www.oxygenxml.com/ns/doc/xsl">
		<desc>
			<p>Test whether a media query matches a medium.</p>
		</desc>
	</doc>
	<java:function name="pf:media-query-matches" as="xs:boolean">
		<xsl:param name="media-query" as="xs:string"/>
		<xsl:param name="medium" as="xs:string"/>
		<!--
		    Implemented in ../../java/org.daisy/pipeline/css/saxon/impl/MediaQueryMatchesDefinition.java
		-->
	</java:function>

</xsl:stylesheet>
