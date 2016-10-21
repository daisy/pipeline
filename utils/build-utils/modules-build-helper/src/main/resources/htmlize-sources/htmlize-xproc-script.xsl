<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline"
                xmlns:pxd="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:cat="urn:oasis:names:tc:entity:xmlns:xml:catalog"
                exclude-result-prefixes="#all"
                version="2.0">
	
	<xsl:include href="htmlize-xproc.xsl"/>
	<xsl:include href="../lib/extend-script.xsl"/>
	
	<xsl:template mode="serialize" match="/*">
		<xsl:param name="parent-in-original-script" tunnel="yes" as="element()?" select="()"/>
		<xsl:if test="not($entry-in-catalog/@px:script='true')">
			<xsl:message terminate="yes">Error</xsl:message>
		</xsl:if>
		<xsl:choose>
			<xsl:when test="not($parent-in-original-script)
			                and $entry-in-catalog/@px:extends">
				<xsl:variable name="inherited-script">
					<xsl:call-template name="extend-script">
						<xsl:with-param name="script-uri" select="$entry-in-catalog/resolve-uri(@uri,base-uri(.))"/>
						<xsl:with-param name="extends-uri" select="$entry-in-catalog/resolve-uri(@px:extends,base-uri(.))"/>
						<xsl:with-param name="catalog-xml" select="$catalog-xml/*"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:apply-templates mode="#current" select="$inherited-script/*">
					<xsl:with-param name="parent-in-original-script" tunnel="yes" select="/*"/>
				</xsl:apply-templates>
			</xsl:when>
			<xsl:otherwise>
				<xsl:next-match/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template mode="serialize" match="/*/@type">
		<xsl:call-template name="set-property">
			<xsl:with-param name="property" select="'id'"/>
			<xsl:with-param name="content"
			                select="if (namespace-uri-for-prefix(substring-before(.,':'),/*)='http://www.daisy.org/ns/pipeline/xproc')
			                        then substring-after(.,':')
			                        else ."/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template mode="serialize"
	              match="/*/p:documentation[not(preceding-sibling::p:*)]/*[@pxd:role='name']">
		<xsl:call-template name="set-property">
			<xsl:with-param name="property" select="'name'"/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template mode="serialize"
	              match="/*/p:documentation[not(preceding-sibling::p:*)]/*[@pxd:role='desc']">
		<xsl:variable name="content" as="node()*">
			<xsl:apply-templates mode="serialize"/>
		</xsl:variable>
		<xsl:variable name="content" select="string-join($content/string(),'')"/>
		<xsl:call-template name="set-property">
			<xsl:with-param name="property" select="'desc'"/>
			<xsl:with-param name="content" select="if (@xml:space='preserve') then $content else normalize-space($content)"/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template match="/*/p:option[p:pipeinfo/pxd:data-type]" mode="finalize-script">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="#current"/>
			<xsl:attribute name="pxd:data-type"
			               select="(p:pipeinfo/pxd:data-type/@id,
			                        p:pipeinfo/pxd:data-type/child::*/@id,
			                        concat(/*/@type,'-',@name))[1]"/>
			<xsl:apply-templates select="p:pipeinfo/pxd:data-type" mode="data-type-attribute"/>
			<xsl:apply-templates select="node()" mode="#current"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="/*/p:option/p:pipeinfo" mode="finalize-script">
		<xsl:if test="p:pipeinfo/(* except pxd:data-type)">
			<xsl:next-match/>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="/*/p:option/p:pipeinfo/pxd:data-type" mode="finalize-script"/>
	
	<xsl:template match="/*/p:option/@pxd:data" mode="serialize" priority="1.1">
		<xsl:if test="not(parent::*/p:pipeinfo/pxd:data-type)">
			<xsl:next-match/>
		</xsl:if>
	</xsl:template>
	
	<xsl:template priority="1"
	              mode="serialize"
	              match="/*/p:option|
	                     /*/p:option/@*|
	                     /*/p:option/p:pipeinfo|
	                     /*/p:option/p:pipeinfo/pxd:data-type|
	                     /*/p:option/p:documentation|
	                     /*/p:input|
	                     /*/p:input/@*|
	                     /*/p:input/p:documentation">
		<xsl:param name="parent-in-original-script" tunnel="yes" as="element()?" select="()"/>
		<xsl:variable name="name" select="concat('{',namespace-uri(.),'}',name(.))"/>
		<xsl:choose>
			<xsl:when test="$parent-in-original-script">
				<xsl:variable name="self-in-original-script"
				              select="if (self::p:option)
				                      then $parent-in-original-script/p:option[@name=current()/@name]
				                      else if (self::p:input)
				                      then $parent-in-original-script/p:input[@port=current()/@port]
				                      else if (self::*)
				                      then $parent-in-original-script/*[concat('{',namespace-uri(.),'}',name(.))=$name]
				                      else $parent-in-original-script/@*[concat('{',namespace-uri(.),'}',name(.))=$name]"/>
				<xsl:choose>
					<xsl:when test="not(exists($self-in-original-script))">
						<span class="script-inherited">
							<xsl:next-match>
								<xsl:with-param name="parent-in-original-script" tunnel="yes" select="()"/>
							</xsl:next-match>
						</span>
					</xsl:when>
					<xsl:otherwise>
						<xsl:next-match>
							<xsl:with-param name="parent-in-original-script" tunnel="yes" select="$self-in-original-script"/>
						</xsl:next-match>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:next-match/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template mode="serialize" match="/*/p:option[not(@pxd:output=('result','temp') or @pxd:type='anyFileURI')]">
		<xsl:call-template name="set-rel">
			<xsl:with-param name="rel" select="'option'"/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template mode="serialize" match="/*/p:input|
	                                      /*/p:option[not(@pxd:output=('result','temp')) and @pxd:type='anyFileURI']">
		<xsl:call-template name="set-rel">
			<xsl:with-param name="rel" select="'input'"/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template mode="serialize" match="/*/p:output|
	                                      /*/p:option[@pxd:output='result']">
		<xsl:call-template name="set-rel">
			<xsl:with-param name="rel" select="'output'"/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template mode="serialize" match="/*/p:option[@pxd:output='temp']">
		<!--
		    ignore
		-->
	</xsl:template>
	
	<xsl:template mode="attribute-value" match="/*/p:input/@port|
	                                            /*/p:output/@port|
	                                            /*/p:option/@name">
		<xsl:call-template name="set-property">
			<xsl:with-param name="property" select="'id'"/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template mode="attribute-value" match="/*/p:input/@sequence|
	                                            /*/p:output/@sequence|
	                                            /*/p:option/@pxd:sequence">
		<xsl:call-template name="set-property">
			<xsl:with-param name="property" select="'sequence'"/>
			<xsl:with-param name="datatype" select="'xsd:boolean'"/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template mode="attribute-value" match="/*/p:option[not(@pxd:output='result' or @pxd:type='anyFileURI')]/@required">
		<xsl:call-template name="set-property">
			<xsl:with-param name="property" select="'required'"/>
			<xsl:with-param name="datatype" select="'xsd:boolean'"/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:variable name="STRING_RE">^\s*('([^']*)'|"([^"]*)")\s*$</xsl:variable>
	
	<xsl:template mode="attribute-value" match="/*/p:option[not(@pxd:output='result' or @pxd:type='anyFileURI')]/@select">
		<xsl:call-template name="set-property">
			<xsl:with-param name="property" select="'default'"/>
			<xsl:with-param name="content" select="replace(.,$STRING_RE,'$2$3')"/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template mode="attribute-value" match="/*/p:option[not(@pxd:output='result' or @pxd:type='anyFileURI')]/@pxd:data-type|
	                                            /*/p:option[not(@pxd:output='result' or @pxd:type='anyFileURI' or @pxd:data-type or p:pipeinfo/pxd:data-type)]/@pxd:type">
		<xsl:call-template name="set-property">
			<xsl:with-param name="property" select="'data-type'"/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template priority="0.6"
	              mode="serialize"
	              match="/*/p:option/p:pipeinfo/pxd:data-type">
		<span rel="data-type">
			<xsl:next-match/>
		</span>
	</xsl:template>
	
	<xsl:template mode="attribute-value" match="/*/p:input/@pxd:media-type|
	                                            /*/p:output/@pxd:media-type|
	                                            /*/p:option[@pxd:output='result' or @pxd:type='anyFileURI']/@pxd:media-type">
		<xsl:call-template name="set-property">
			<xsl:with-param name="property" select="'media-type'"/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template mode="serialize"
	              match="/*/p:option/p:documentation/*[@pxd:role='name']|
	                     /*/p:input/p:documentation/*[@pxd:role='name']|
	                     /*/p:output/p:documentation/*[@pxd:role='name']">
		<xsl:call-template name="set-property">
			<xsl:with-param name="property" select="'name'"/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template mode="serialize"
	              match="/*/p:option/p:documentation/*[@pxd:role='desc']|
	                     /*/p:input/p:documentation/*[@pxd:role='desc']|
	                     /*/p:output/p:documentation/*[@pxd:role='desc']">
		<xsl:variable name="content" as="node()*">
			<xsl:apply-templates mode="serialize"/>
		</xsl:variable>
		<xsl:variable name="content" select="string-join($content/string(),'')"/>
		<xsl:call-template name="set-property">
			<xsl:with-param name="property" select="'desc'"/>
			<xsl:with-param name="content" select="if (@xml:space='preserve') then $content else normalize-space($content)"/>
		</xsl:call-template>
	</xsl:template>
	
</xsl:stylesheet>
