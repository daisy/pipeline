<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:px2="http://www.daisy.org/ns/pipeline"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:catalog="urn:oasis:names:tc:entity:xmlns:xml:catalog"
                xmlns:xd="http://github.com/vojtechtoman/xprocdoc"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                type="px:catalog-to-xprocdoc"
                name="main">
	
	<p:input port="source" sequence="false"/>
	<p:option name="catalog-base-uri" required="true"/>
	<p:option name="output-base-uri" required="true"/>
	
	<p:import href="process-sources.xpl"/>
	<p:import href="summary-to-xhtml.xpl"/>
	
	<p:for-each name="sources">
		<p:iteration-source select="/*/catalog:uri">
			<p:pipe step="main" port="source"/>
		</p:iteration-source>
		<p:choose>
			<p:when test="/*[ends-with(@uri,'.xpl') and not(@px2:content-type='params')]">
				<p:load>
					<p:with-option name="href" select="/*/resolve-uri(@uri,base-uri(.))"/>
				</p:load>
				<p:add-attribute match="/*" attribute-name="px:public-name">
					<p:with-option name="attribute-value" select="/*/@name">
						<p:pipe step="sources" port="current"/>
					</p:with-option>
				</p:add-attribute>
			</p:when>
			<p:otherwise>
				<p:identity>
					<p:input port="source">
						<p:empty/>
					</p:input>
				</p:identity>
			</p:otherwise>
		</p:choose>
	</p:for-each>

	<xd:process-sources/>
	
	<p:choose>
		<p:when test="$catalog-base-uri=''">
			<p:identity/>
		</p:when>
		<p:otherwise>
			<p:documentation>
				Rebase href attributes from the real base URI of the catalog document onto
				$catalog-base-uri.
			</p:documentation>
			<p:xslt>
				<p:input port="stylesheet">
					<p:inline>
						<xsl:stylesheet version="2.0">
							<xsl:param name="catalog-base-uri"/>
							<xsl:include href="../lib/uri-functions.xsl"/>
							<xsl:template match="xd:source/@href|
							                     xd:import/@href">
								<xsl:attribute name="href" select="pf:relativize-uri(.,$catalog-base-uri)"/>
							</xsl:template>
							<xsl:template match="@*|node()">
								<xsl:copy>
									<xsl:apply-templates select="@*|node()"/>
								</xsl:copy>
							</xsl:template>
						</xsl:stylesheet>
					</p:inline>
				</p:input>
				<p:with-param name="catalog-base-uri" select="base-uri(/*)">
					<p:pipe step="main" port="source"/>
				</p:with-param>
			</p:xslt>
			<p:xslt>
				<p:input port="stylesheet">
					<p:inline>
						<xsl:stylesheet version="2.0">
							<xsl:param name="catalog-base-uri"/>
							<xsl:template match="xd:source/@href|
							                     xd:import/@href">
								<xsl:attribute name="href" select="resolve-uri(.,$catalog-base-uri)"/>
							</xsl:template>
							<xsl:template match="@*|node()">
								<xsl:copy>
									<xsl:apply-templates select="@*|node()"/>
								</xsl:copy>
							</xsl:template>
						</xsl:stylesheet>
					</p:inline>
				</p:input>
				<p:with-param name="catalog-base-uri" select="$catalog-base-uri"/>
			</p:xslt>
		</p:otherwise>
	</p:choose>
	
	<xd:summary-to-xhtml>
		<!--
		    just passing a random file URI because it does not matter: because of the px:public-name
		    attributes no real file paths need to be included (and therefore no directory is needed
		    to relativize them against)
		-->
		<p:with-param name="input-base-uri" select="resolve-uri('../',base-uri(/*))">
			<p:pipe step="main" port="source"/>
		</p:with-param>
		<p:with-param name="output-base-uri" select="$output-base-uri">
			<p:empty/>
		</p:with-param>
		<p:with-param name="product" select="''"/>
		<p:with-param name="overview-file" select="''"/>
	</xd:summary-to-xhtml>
	<p:sink/>
	
</p:declare-step>
