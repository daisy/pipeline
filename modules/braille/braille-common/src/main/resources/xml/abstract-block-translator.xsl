<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="3.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:s="org.daisy.pipeline.braille.css.xpath.Style"
                xmlns:t="org.daisy.pipeline.braille.css.xpath.StyledText"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                exclude-result-prefixes="#all">
	
	<xsl:import href="http://www.daisy.org/pipeline/modules/braille/css-utils/library.xsl"/>
	<xsl:include href="http://www.daisy.org/pipeline/modules/common-utils/library.xsl"/>
	
	<xsl:param name="braille-charset"/>
	<xsl:param name="translate-attributes" select="''" static="yes">
		<!-- XSLT match pattern -->
		<!-- FIXME: does not support namespaces! -->
	</xsl:param>
	<xsl:param name="include-braille-code-in-language" as="xs:boolean" select="false()"/>
	
	<!--
	    API: implement xsl:template match="css:block"
	-->
	<xsl:template match="css:block">
		<xsl:message terminate="yes">Coding error</xsl:message>
	</xsl:template>
	
	<xsl:template match="/*">
		<xsl:apply-templates select="." mode="identify-blocks"/>
	</xsl:template>
	
	<xsl:variable name="TEXT_TRANSFORM_NONE" as="item()" select="css:parse-stylesheet('text-transform: none')"/>
	<xsl:variable name="BRAILLE_CHARSET_VALUE" as="xs:string" select="if ($braille-charset!='') then 'custom' else 'unicode'"/>
	<xsl:variable name="BRAILLE_CHARSET_DECLARATION" as="item()"
	              select="css:parse-stylesheet(concat('braille-charset: ',$BRAILLE_CHARSET_VALUE))"/>
	
	<xsl:template mode="identify-blocks" match="/*">
		<xsl:next-match>
			<xsl:with-param name="source-style" tunnel="yes" select="()"/>
			<xsl:with-param name="source-language" tunnel="yes" select="()"/>
			<xsl:with-param name="result-style" tunnel="yes" select="()"/>
			<xsl:with-param name="result-language" tunnel="yes" select="()"/>
			<xsl:with-param name="portion" select="1.0"/>
		</xsl:next-match>
	</xsl:template>
	
	<xsl:variable name="text-properties" as="xs:string*" select="('white-space',
	                                                              'hyphens',
	                                                              'hyphenate-character',
	                                                              'text-transform',
	                                                              'braille-charset',
	                                                              'letter-spacing',
	                                                              'word-spacing')"/>
	
	<xsl:template mode="identify-blocks" match="*">
		<!-- parent is block -->
		<xsl:param name="is-block" as="xs:boolean" select="true()" tunnel="yes"/>
		<!-- style of the parent element in the source -->
		<xsl:param name="source-style" as="item()?" tunnel="yes"/>
		<!-- language of the parent element in the source -->
		<xsl:param name="source-language" as="xs:string?" tunnel="yes"/>
		<!-- style of the parent element in the result -->
		<xsl:param name="result-style" as="item()?" tunnel="yes"/>
		<!-- language of the parent element in the result -->
		<xsl:param name="result-language" as="xs:string?" tunnel="yes"/>
		<xsl:param name="portion" required="yes"/>
		<xsl:variable name="source-style" as="item()?" select="css:parse-stylesheet(@style,$source-style)"/>
		<xsl:variable name="source-style" as="item()?" select="s:merge(($source-style,@css:*/css:parse-stylesheet(.)))"/>
		<xsl:variable name="source-language" as="xs:string?" select="(@xml:lang/string(.),$source-language)[1]"/>
		<xsl:variable name="translated-style" as="item()?">
			<!-- don't translate style if 'content' property present -->
			<xsl:choose>
				<xsl:when test="exists(s:get($source-style,'content'))">
					<xsl:sequence select="$source-style"/>"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="translate-style">
						<xsl:with-param name="style" select="$source-style"/>
					</xsl:call-template>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="translated-language" as="xs:string?">
			<xsl:call-template name="translated-language">
				<xsl:with-param name="source-style" tunnel="yes" select="$source-style"/>
				<xsl:with-param name="source-language" tunnel="yes" select="$source-language"/>
				<xsl:with-param name="translated-style" select="$translated-style"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="style" as="attribute()?" select="css:style-attribute(css:serialize-stylesheet($translated-style,$result-style))"/>
		<xsl:variable name="lang" as="attribute()?">
			<xsl:for-each select="$translated-language[not(.=$result-language)]">
				<xsl:attribute name="xml:lang" select="."/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:element name="{name(.)}" namespace="{namespace-uri(.)}">
			<xsl:sequence select="$style"/>
			<xsl:sequence select="$lang"/>
			<xsl:apply-templates mode="#current" select="@* except (@style|@css:*|@xml:lang)">
				<xsl:with-param name="source-style" tunnel="yes" select="$source-style"/>
				<xsl:with-param name="source-language" tunnel="yes" select="$source-language"/>
			</xsl:apply-templates>
			<xsl:variable name="is-block" as="xs:boolean"
			              select="$is-block and descendant-or-self::*[@css:display[not(.='inline')]]"/>
			<xsl:for-each-group select="*|text()"
			                    group-adjacent="$is-block and boolean(descendant-or-self::*[@css:display[not(.='inline')]])">
				<xsl:variable name="group-portion" select="$portion div last()"/>
				<xsl:choose>
					<xsl:when test="current-grouping-key()">
						<xsl:for-each select="current-group()">
							<xsl:variable name="group-portion" select="$group-portion div last()"/>
							<xsl:apply-templates mode="#current" select=".">
								<xsl:with-param name="is-block" select="$is-block" tunnel="yes"/>
								<xsl:with-param name="source-style" tunnel="yes" select="$source-style"/>
								<xsl:with-param name="source-language" tunnel="yes" select="$source-language"/>
								<xsl:with-param name="result-style" tunnel="yes" select="$translated-style"/>
								<xsl:with-param name="result-language" tunnel="yes" select="$translated-language"/>
								<xsl:with-param name="portion" select="$group-portion"/>
							</xsl:apply-templates>
						</xsl:for-each>
					</xsl:when>
					<!-- preserve indentation outside of blocks -->
					<xsl:when test="every $n in current-group() satisfies
					                $n/self::text() and matches(string($n),'^[ \t\n\r&#x2800;&#x00AD;&#x200B;]*$')">
						<xsl:call-template name="pf:progress">
							<xsl:with-param name="progress" select="string($group-portion)"/>
						</xsl:call-template>
						<xsl:value-of select="."/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:variable name="block">
							<xsl:element name="css:block">
								<xsl:for-each select="current-group()">
									<xsl:sequence select="."/>
								</xsl:for-each>
							</xsl:element>
						</xsl:variable>
						<xsl:call-template name="pf:progress">
							<xsl:with-param name="progress" select="string($group-portion)"/>
						</xsl:call-template>
						<xsl:apply-templates select="$block/css:block">
							<xsl:with-param name="source-style" tunnel="yes" select="$source-style"/>
							<xsl:with-param name="source-language" tunnel="yes" select="$source-language"/>
							<xsl:with-param name="result-style" tunnel="yes" select="$translated-style"/>
							<xsl:with-param name="result-language" tunnel="yes" select="$translated-language"/>
							<xsl:with-param name="portion" select="$group-portion"/>
						</xsl:apply-templates>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each-group>
		</xsl:element>
	</xsl:template>
	
	<xsl:template mode="identify-blocks" match="text()|@*">
		<xsl:sequence select="."/>
	</xsl:template>
	
	<xsl:template name="translate-style" as="item()?">
		<xsl:param name="style" as="item()?" required="yes"/>
		<xsl:variable name="style" as="item()*">
			<xsl:for-each select="s:iterate($style)">
				<xsl:choose>
					<xsl:when test="exists(s:property(.))">
						<xsl:variable name="property" as="xs:string" select="s:property(.)"/>
						<xsl:variable name="value" as="xs:string" select="string(s:get($style,$property))"/>
						<xsl:choose>
							<xsl:when test="$property='text-transform' and not($value='none')">
								<!-- replaced with 'none' below -->
							</xsl:when>
							<xsl:when test="$property='braille-charset' and not($value=$BRAILLE_CHARSET_VALUE)">
								<!-- replaced below -->
							</xsl:when>
							<xsl:when test="$property='hyphens' and $value='auto'">
								<!-- manual (initial) -->
							</xsl:when>
							<xsl:otherwise>
								<xsl:sequence select="."/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:when>
					<xsl:when test="exists(s:selector(.))">
						<!-- Not processing nested rules. Note that ::before and ::after rules and
						     their 'content' rules have previously been expanded, so they will be
						     pre-translated. ::before and ::after rules that are stacked onto other
						     pseudo-elements have not been expanded so will not be
						     pre-translated. -->
						<xsl:sequence select="."/>
					</xsl:when>
					<xsl:otherwise>
						<!-- can not happen -->
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
		</xsl:variable>
		<xsl:sequence select="s:merge(($TEXT_TRANSFORM_NONE,$BRAILLE_CHARSET_DECLARATION,$style))"/>
	</xsl:template>
	
	<xsl:template mode="text-items" match="*" as="item()*">
		<xsl:param name="source-style" as="item()?" tunnel="yes"/>
		<xsl:param name="source-language" as="xs:string?" tunnel="yes"/>
		<xsl:variable name="source-style" as="item()?" select="css:parse-stylesheet(@style,$source-style)"/>
		<xsl:variable name="source-language" as="xs:string?" select="(@xml:lang/string(.),$source-language)[1]"/>
		<xsl:apply-templates mode="#current">
			<xsl:with-param name="source-style" tunnel="yes" select="$source-style"/>
			<xsl:with-param name="source-language" tunnel="yes" select="$source-language"/>
		</xsl:apply-templates>
	</xsl:template>
	
	<xsl:template mode="text-items" match="text()" as="item()">
		<xsl:param name="source-style" as="item()?" tunnel="yes"/>
		<xsl:param name="source-language" as="xs:string?" tunnel="yes"/>
		<xsl:variable name="source-style" as="item()?" select="css:parse-stylesheet((),$source-style)"/>
		<xsl:sequence select="t:of(string(.),$source-style,$source-language)"/>
	</xsl:template>
	
	<xsl:template mode="treewalk" match="*">
		<xsl:param name="new-text-nodes" as="item()*" required="yes"/>
		<xsl:param name="source-style" as="item()?" tunnel="yes"/>
		<xsl:param name="source-language" as="xs:string?" tunnel="yes"/>
		<xsl:param name="result-style" as="item()?" tunnel="yes"/>
		<xsl:param name="result-language" as="xs:string?" tunnel="yes"/>
		<xsl:variable name="text-node-count" select="count(.//text())"/>
		<xsl:variable name="source-style" as="item()?" select="css:parse-stylesheet(@style,$source-style)"/>
		<xsl:variable name="source-style" as="item()?" select="s:merge(($source-style,@css:*/css:parse-stylesheet(.)))"/>
		<xsl:variable name="source-language" as="xs:string?" select="(@xml:lang/string(.),$source-language)[1]"/>
		<xsl:variable name="translated-style" as="item()?">
			<!-- don't translate style if 'content' property present -->
			<xsl:choose>
				<xsl:when test="exists(s:get($source-style,'content'))">
					<xsl:sequence select="$source-style"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="translate-style">
						<xsl:with-param name="style" select="$source-style"/>
					</xsl:call-template>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="translated-language" as="xs:string?">
			<xsl:call-template name="translated-language">
				<xsl:with-param name="source-style" tunnel="yes" select="$source-style"/>
				<xsl:with-param name="source-language" tunnel="yes" select="$source-language"/>
				<xsl:with-param name="translated-style" select="$translated-style"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="style" as="attribute()?" select="css:style-attribute(css:serialize-stylesheet($translated-style,$result-style))"/>
		<xsl:variable name="lang" as="attribute()?">
			<xsl:for-each select="$translated-language[not(.=$result-language)]">
				<xsl:attribute name="xml:lang" select="."/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:element name="{name(.)}" namespace="{namespace-uri(.)}">
			<xsl:sequence select="$style"/>
			<xsl:sequence select="$lang"/>
			<xsl:apply-templates mode="#current" select="@* except (@style|@css:*|@xml:lang)">
				<xsl:with-param name="source-style" tunnel="yes" select="$source-style"/>
				<xsl:with-param name="source-language" tunnel="yes" select="$source-language"/>
			</xsl:apply-templates>
			<!-- ignore child nodes if 'content' property present -->
			<xsl:if test="empty(s:get($translated-style,'content'))">
				<xsl:apply-templates mode="#current" select="child::node()[1]">
					<xsl:with-param name="new-text-nodes" select="$new-text-nodes[position()&lt;=$text-node-count]"/>
					<xsl:with-param name="source-style" tunnel="yes" select="$source-style"/>
					<xsl:with-param name="source-language" tunnel="yes" select="$source-language"/>
					<xsl:with-param name="result-style" tunnel="yes" select="$translated-style"/>
					<xsl:with-param name="result-language" tunnel="yes" select="$translated-language"/>
				</xsl:apply-templates>
			</xsl:if>
		</xsl:element>
		<xsl:apply-templates mode="#current" select="following-sibling::node()[1]">
			<xsl:with-param name="new-text-nodes" select="$new-text-nodes[position()&gt;$text-node-count]"/>
		</xsl:apply-templates>
	</xsl:template>
	
	<xsl:template mode="treewalk" match="text()">
		<xsl:param name="new-text-nodes" as="item()*" required="yes"/>
		<xsl:param name="result-style" as="item()*" tunnel="yes"/>
		<xsl:param name="result-language" as="xs:string?" tunnel="yes"/>
		<xsl:variable name="text" as="xs:string" select="t:getText($new-text-nodes[1])"/>
		<xsl:variable name="translated-style" as="item()?" select="t:getStyle($new-text-nodes[1])"/>
		<xsl:variable name="translated-language" as="xs:string?" select="t:getLanguage($new-text-nodes[1])"/>
		<xsl:variable name="style" as="attribute()?" select="css:style-attribute(css:serialize-stylesheet($translated-style,$result-style))"/>
		<xsl:variable name="lang" as="attribute()?">
			<xsl:for-each select="$translated-language[not(.=$result-language)]">
				<xsl:attribute name="xml:lang" select="."/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:choose>
			<xsl:when test="empty($style) and empty($lang)
			                or $text=''
			                or normalize-space($text)=''
			                   and not(s:get($translated-style,'white-space')[not(string(.)='normal')])">
				<xsl:value-of select="$text"/>
			</xsl:when>
			<xsl:otherwise>
				<_>
					<xsl:sequence select="$style"/>
					<xsl:sequence select="$lang"/>
					<xsl:value-of select="$text"/>
				</_>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:apply-templates mode="#current" select="following-sibling::node()[1]">
			<xsl:with-param name="new-text-nodes" select="$new-text-nodes[position()&gt;1]"/>
		</xsl:apply-templates>
	</xsl:template>
	
	<xsl:template mode="treewalk" match="@*">
		<xsl:sequence select="."/>
	</xsl:template>

	<xsl:template mode="identify-blocks treewalk" _match="{$translate-attributes}" use-when="$translate-attributes!=''">
		<xsl:param name="source-style" as="item()?" tunnel="yes"/>
		<xsl:param name="source-language" as="xs:string?" tunnel="yes"/>
		<xsl:choose>
			<xsl:when test="self::attribute()">
				<xsl:variable name="block">
					<xsl:element name="css:block">
						<xsl:value-of select="string(.)"/>
					</xsl:element>
				</xsl:variable>
				<xsl:variable name="translated-block">
					<xsl:apply-templates select="$block/css:block"/>
				</xsl:variable>
				<xsl:attribute name="{name()}" select="string($translated-block)"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:next-match/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="translated-language" as="xs:string?">
		<xsl:param name="source-language" as="xs:string?" tunnel="yes"/>
		<xsl:param name="result-language" as="xs:string?" tunnel="yes"/>
		<xsl:param name="translated-style" as="item()?"/>
		<xsl:choose>
			<xsl:when test="$include-braille-code-in-language">
				<xsl:variable name="block">
					<xsl:element name="css:block">
						<xsl:text>xxx</xsl:text>
					</xsl:element>
				</xsl:variable>
				<xsl:variable name="translated-block" as="node()*">
					<xsl:apply-templates select="$block/css:block"/>
				</xsl:variable>
				<xsl:sequence select="($translated-block/self::*[1]/@xml:lang/string(.),
				                       $result-language)[1]"/>
			</xsl:when>
			<xsl:when test="empty($source-language)"/>
			<xsl:when test="s:get($translated-style,'text-transform')[string(.)='none']">
				<xsl:sequence select="replace($source-language,
				                              '^([a-zA-Z]{2,8})(?:-[A-Z][a-z]{3})?(-.+)?$',
				                              '$1-Brai$2')"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:sequence select="$source-language"/>"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

</xsl:stylesheet>
