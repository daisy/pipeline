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
		<xsl:if test="not($entry-in-catalog/@px:content-type='script')">
			<xsl:message terminate="yes">Error</xsl:message>
		</xsl:if>
		<xsl:choose>
			<xsl:when test="not($parent-in-original-script)
			                and $entry-in-catalog/@px:extends">
				<xsl:variable name="inherited-script">
					<xsl:call-template name="extend-script">
						<xsl:with-param name="script-uri" select="$entry-in-catalog/resolve-uri(@uri,base-uri(.))"/>
						<xsl:with-param name="extends-uri" select="for $u in tokenize($entry-in-catalog/@px:extends,'\s+')[not(.='')]
						                                           return resolve-uri($u,base-uri($entry-in-catalog))"/>
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
	
	<xsl:template match="/*/p:option[p:pipeinfo/pxd:type]" mode="finalize-script">
		<!--
		    the URI of the script that is being HTMLized, or one of the XProc files it extends
		-->
		<xsl:param name="script-uri" tunnel="yes" required="yes"/>
		<xsl:choose>
			<xsl:when test="exists($catalog-xml//cat:uri[@name or @px:content-type='script']
			                                            [resolve-uri(@uri,base-uri(.))=$script-uri])">
				<!--
				    The (possibly inherited) type is defined in a script or a public
				    file. process-catalog will move the definition into a separate file and generate
				    the ID "[root type]-[option name]" for it. Add a px:type attribute with that
				    same ID to the p:option and drop the px:type element.
				-->
				<xsl:copy>
					<xsl:apply-templates select="@*" mode="#current"/>
					<!-- FIXME: make @type optional -->
					<xsl:if test="not(/*/@type)">
						<xsl:message terminate="yes">missing @type: <xsl:sequence select="/*"/></xsl:message>
					</xsl:if>
					<xsl:attribute name="pxd:type"
					               select="concat(/*/@type,'-',
					                              if (contains(@name,':')) then substring-after(@name,':') else @name)"/>
					<xsl:apply-templates select="node()" mode="#current"/>
				</xsl:copy>
			</xsl:when>
			<xsl:otherwise>
				<!--
				    Otherwise keep the type definition.
				-->
				<xsl:sequence select="."/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="/*/p:option/p:pipeinfo" mode="finalize-script">
		<xsl:if test="p:pipeinfo/(* except pxd:type)">
			<xsl:next-match/>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="/*/p:option/p:pipeinfo/pxd:type" mode="finalize-script"/>
	
	<xsl:template match="/*/p:option/@pxd:type" mode="serialize" priority="1.1">
		<xsl:if test="not(parent::*/p:pipeinfo/pxd:type)">
			<xsl:next-match/>
		</xsl:if>
	</xsl:template>
	
	<xsl:template priority="1"
	              mode="serialize"
	              match="/*/p:option|
	                     /*/p:option/@*|
	                     /*/p:option/p:pipeinfo|
	                     /*/p:option/p:pipeinfo/pxd:type|
	                     /*/p:option/p:documentation|
	                     /*/p:input|
	                     /*/p:input/@*|
	                     /*/p:input/p:documentation|
	                     /*/p:output|
	                     /*/p:output/@*|
	                     /*/p:output/p:documentation">
		<xsl:param name="parent-in-original-script" tunnel="yes" as="element()?" select="()"/>
		<xsl:variable name="name" select="concat('{',namespace-uri(.),'}',name(.))"/>
		<xsl:choose>
			<xsl:when test="$parent-in-original-script">
				<xsl:variable name="self-in-original-script"
				              select="if (self::p:option)
				                      then $parent-in-original-script/p:option[@name=current()/@name]
				                      else if (self::p:input)
				                      then $parent-in-original-script/p:input[@port=current()/@port]
				                      else if (self::p:output)
				                      then $parent-in-original-script/p:output[@port=current()/@port]
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
	
	<xsl:template mode="attribute-value" match="/*/p:option[not(@pxd:output='result' or @pxd:type='anyFileURI')]/@pxd:type">
		<xsl:call-template name="set-property">
			<xsl:with-param name="property" select="'data-type'"/>
			<xsl:with-param name="content" select="replace(.,'^xsd?:','')"/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template priority="0.6"
	              mode="serialize"
	              match="/*/p:option/p:pipeinfo/pxd:type">
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
