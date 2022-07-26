<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                exclude-result-prefixes="#all">
	
	<xsl:import href="http://www.daisy.org/pipeline/modules/braille/css-utils/library.xsl"/>
	<xsl:include href="http://www.daisy.org/pipeline/modules/common-utils/library.xsl"/>
	
	<xsl:param name="braille-charset"/>
	
	<!--
	    API: implement xsl:template match="css:block"
	-->
	<xsl:template match="css:block">
		<xsl:message terminate="yes">Coding error</xsl:message>
	</xsl:template>
	
	<xsl:template match="/*">
		<xsl:apply-templates select="." mode="identify-blocks"/>
	</xsl:template>
	
	<xsl:template mode="identify-blocks" match="/*">
		<xsl:variable name="initial-style" as="element()*">
			<xsl:call-template name="css:computed-properties">
				<xsl:with-param name="properties" select="$text-properties"/>
				<xsl:with-param name="context" select="$dummy-element"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:next-match>
			<xsl:with-param name="source-style" tunnel="yes" select="$initial-style"/>
			<xsl:with-param name="result-style" tunnel="yes" select="$initial-style"/>
			<xsl:with-param name="portion" select="1.0"/>
		</xsl:next-match>
	</xsl:template>
	
	<xsl:variable name="text-properties" as="xs:string*"
	              select="$css:properties[css:applies-to(., 'inline') and css:is-inherited(.)]"/>
	
	<xsl:template mode="identify-blocks" match="*">
		<!-- parent is block -->
		<xsl:param name="is-block" as="xs:boolean" select="true()" tunnel="yes"/>
		<!-- computed text properties of the parent element in the source -->
		<xsl:param name="source-style" as="element(css:property)*" tunnel="yes"/>
		<!-- computed text properties of the parent element in the result -->
		<xsl:param name="result-style" as="element(css:property)*" tunnel="yes"/>
		<xsl:param name="portion" required="yes"/>
		<xsl:variable name="style" as="element(css:rule)*">
			<xsl:call-template name="css:deep-parse-stylesheet">
				<xsl:with-param name="stylesheet" select="@style"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="style" as="element(css:rule)*">
			<css:rule>
				<xsl:if test="@css:*">
					<xsl:apply-templates mode="css:attribute-as-property" select="@css:*"/>
				</xsl:if>
				<xsl:sequence select="$style[not(@selector)]/*"/>
			</css:rule>
			<xsl:sequence select="$style[@selector]"/>
		</xsl:variable>
		<xsl:variable name="context" as="element()" select="."/>
		<xsl:variable name="translated-style" as="element(css:rule)*">
			<xsl:call-template name="translate-style">
				<xsl:with-param name="style" select="$style"/>
				<xsl:with-param name="context" tunnel="yes" select="$context"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:element name="{name(.)}" namespace="{namespace-uri(.)}">
			<xsl:sequence select="@* except (@style|@css:*|@xml:lang)"/>
			<xsl:call-template name="insert-style">
				<xsl:with-param name="style" select="$translated-style"/>
			</xsl:call-template>
			<xsl:variable name="lang" as="xs:string" select="(ancestor-or-self::*[@xml:lang][1]/@xml:lang,'und')[1]"/>
			<xsl:if test="@xml:lang
			              or (
			                ancestor::*[@xml:lang]
			                and $translated-style[not(@selector)]/css:property[@name='text-transform']
			                and not($translated-style[not(@selector)]/css:property[@name='text-transform'][1]/@value/string(.)
			                        =($result-style[@name='text-transform']/@value/string(.),'auto')[1]))">
				<xsl:attribute name="xml:lang"
				               select="if (($translated-style[not(@selector)]/css:property[@name='text-transform']/@value/string(.),
				                            $result-style[@name='text-transform']/@value/string(.),
				                            'auto'
				                            )[1]='none')
				                       then concat($lang,'-Brai')
				                       else $lang"/>
			</xsl:if>
			<xsl:variable name="is-block" as="xs:boolean"
			              select="$is-block and descendant-or-self::*[@css:display[not(.='inline')]]"/>
			<xsl:variable name="source-style" as="element()*">
				<xsl:call-template name="css:computed-properties">
					<xsl:with-param name="properties" select="$text-properties"/>
					<!--
					    passing dummy context because not used by css:cascaded-properties and
					    css:parent-property below
					-->
					<xsl:with-param name="context" select="$dummy-element"/>
					<xsl:with-param name="cascaded-properties" tunnel="yes" select="$style[not(@selector)]/css:property"/>
					<xsl:with-param name="parent-properties" tunnel="yes" select="$source-style"/>
				</xsl:call-template>
			</xsl:variable>
			<xsl:variable name="result-style" as="element()*">
				<xsl:call-template name="css:computed-properties">
					<xsl:with-param name="properties" select="$text-properties"/>
					<xsl:with-param name="context" select="$dummy-element"/>
					<xsl:with-param name="cascaded-properties" tunnel="yes" select="$translated-style[not(@selector)]/css:property"/>
					<xsl:with-param name="parent-properties" tunnel="yes" select="$result-style"/>
				</xsl:call-template>
			</xsl:variable>
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
								<xsl:with-param name="result-style" tunnel="yes" select="$result-style"/>
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
								<!--
								    TODO: better to pass as parameter instead of attribute?
								-->
								<xsl:if test="$lang">
									<xsl:attribute name="xml:lang" select="$lang"/>
								</xsl:if>
								<xsl:for-each select="current-group()">
									<xsl:sequence select="."/>
								</xsl:for-each>
							</xsl:element>
						</xsl:variable>
						<xsl:call-template name="pf:progress">
							<xsl:with-param name="progress" select="string($group-portion)"/>
						</xsl:call-template>
						<xsl:apply-templates select="$block/css:block">
							<xsl:with-param name="context" select="$context"/>
							<xsl:with-param name="source-style" tunnel="yes" select="$source-style"/>
							<xsl:with-param name="result-style" tunnel="yes" select="$result-style"/>
							<xsl:with-param name="portion" select="$group-portion"/>
						</xsl:apply-templates>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each-group>
		</xsl:element>
	</xsl:template>
	
	<xsl:template mode="identify-blocks" match="text()">
		<xsl:sequence select="."/>
	</xsl:template>
	
	<xsl:template name="translate-style" as="element(css:rule)*">
		<xsl:param name="style" as="element(css:rule)*" required="yes"/>
		<xsl:param name="source-style" as="element(css:property)*" tunnel="yes"/>
		<xsl:param name="result-style" as="element(css:property)*" tunnel="yes"/>
		<xsl:variable name="main-style" as="element(css:rule)*" select="$style[not(@selector)]"/>
		<xsl:variable name="translated-main-style" as="element(css:rule)*">
			<xsl:apply-templates mode="translate-style" select="$main-style"/>
		</xsl:variable>
		<!--
		    FIXME: move this to the template for ::before and ::after rules, so that it is also done
		    for e.g. ::list-item::after
		-->
		<xsl:variable name="source-style" as="element()*">
			<xsl:call-template name="css:computed-properties">
				<xsl:with-param name="properties" select="$text-properties"/>
				<xsl:with-param name="context" select="$dummy-element"/>
				<xsl:with-param name="cascaded-properties" tunnel="yes" select="$main-style/css:property"/>
				<xsl:with-param name="parent-properties" tunnel="yes" select="$source-style"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="result-style" as="element()*">
			<xsl:call-template name="css:computed-properties">
				<xsl:with-param name="properties" select="$text-properties"/>
				<xsl:with-param name="context" select="$dummy-element"/>
				<xsl:with-param name="cascaded-properties" tunnel="yes" select="$translated-main-style/css:property"/>
				<xsl:with-param name="parent-properties" tunnel="yes" select="$result-style"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="translated-style" as="element()*">
			<xsl:sequence select="$translated-main-style"/>
			<xsl:apply-templates mode="translate-style" select="$style[@selector=('&amp;::before','&amp;::after')]">
				<xsl:with-param name="source-style" tunnel="yes" select="$source-style"/>
				<xsl:with-param name="result-style" tunnel="yes" select="$result-style"/>
			</xsl:apply-templates>
			<!--
			    It does not make sense to translate @text-transform rules. Not dropping the rules
			    because dependending on the restore-text-style parameter, text-transform values
			    (other than none) may still be present in the output.
			-->
			<xsl:sequence select="$style[matches(@selector,'^@text-transform')]"/>
			<xsl:apply-templates mode="translate-style" select="$style[@selector
			                                                           and not(@selector=('&amp;::before','&amp;::after'))
			                                                           and not(matches(@selector,'^@text-transform'))]"/>
		</xsl:variable>
		<xsl:apply-templates mode="insert-style" select="$translated-style"/>
	</xsl:template>
	
	<xsl:template mode="translate-style"
	              match="css:rule[@selector=('&amp;::after','&amp;::before',
	                                         '@top-left','@top-center','@top-right','@right',
	                                         '@bottom-right','@bottom-center','@bottom-left','@left')]
	                             [css:property[@name='content']/*[not(self::css:string[@value]|self::css:attr)]]|
	                     css:rule[@selector=('&amp;::after','&amp;::before')]
	                             /css:rule[not(@selector)]
	                             [css:property[@name='content']/*[not(self::css:string[@value]|self::css:attr)]]">
		<xsl:next-match>
			<!--
			    Don't pre-translate when the content property has other values than strings or
			    attr() values.
			-->
			<xsl:with-param name="restore-text-style" tunnel="yes" select="true()"/>
		</xsl:next-match>
	</xsl:template>
	
	<xsl:template mode="translate-style" match="css:rule">
		<xsl:param name="source-style" as="element(css:property)*" tunnel="yes"/>
		<xsl:param name="restore-text-style" as="xs:boolean" tunnel="yes" select="false()"/>
		<xsl:copy>
			<xsl:sequence select="@selector"/>
			<xsl:variable name="translated-style" as="element()*">
				<xsl:choose>
					<xsl:when test="css:rule">
						<xsl:call-template name="translate-style">
							<xsl:with-param name="style" select="css:rule"/>
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						<xsl:variable name="source-style" as="element()*">
							<xsl:call-template name="css:computed-properties">
								<xsl:with-param name="properties" select="$text-properties"/>
								<xsl:with-param name="context" select="$dummy-element"/>
								<xsl:with-param name="cascaded-properties" tunnel="yes" select="css:property"/>
								<xsl:with-param name="parent-properties" tunnel="yes" select="$source-style"/>
							</xsl:call-template>
						</xsl:variable>
						<xsl:variable name="properties" as="element()*" select="css:property"/>
						<xsl:variable name="translated-properties" as="element()*">
							<xsl:choose>
								<xsl:when test="$restore-text-style">
									<xsl:apply-templates mode="restore-text-style-and-translate-other-style"
									                     select="($properties,$source-style[not(@name=$properties/@name)])">
										<xsl:with-param name="source-style" tunnel="yes"
										                select="($source-style,$properties[@name='content'])"/>
									</xsl:apply-templates>
								</xsl:when>
								<xsl:otherwise>
									<xsl:apply-templates mode="#current" select="($properties,$source-style[not(@name=$properties/@name)])">
										<xsl:with-param name="source-style" tunnel="yes" select="$source-style"/>
									</xsl:apply-templates>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:variable>
						<xsl:choose>
							<!-- only include braille-charset if text-transform is "none" -->
							<xsl:when test="$translated-properties[@name='text-transform' and @value='none']">
								<xsl:sequence select="$translated-properties"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:sequence select="$translated-properties[not(@name='braille-charset')]"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:variable>
			<xsl:apply-templates mode="insert-style" select="$translated-style"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template mode="restore-text-style-and-translate-other-style" match="css:property">
		<xsl:apply-templates mode="translate-style" select="."/>
	</xsl:template>
	
	<xsl:template mode="restore-text-style-and-translate-other-style" match="css:property[@name=($text-properties,'content')]">
		<xsl:param name="source-style" as="element()*" tunnel="yes"/>
		<xsl:variable name="name" as="xs:string" select="@name"/>
		<xsl:sequence select="$source-style[@name=$name]"/>
	</xsl:template>
	
	<xsl:template mode="translate-style" match="css:property">
		<xsl:sequence select="."/>
	</xsl:template>
	
	<xsl:template mode="translate-style" match="css:property[@name='text-transform']">
		<css:property name="text-transform" value="none"/>
	</xsl:template>
	
	<xsl:template mode="translate-style" match="css:property[@name='braille-charset']">
		<css:property name="braille-charset" value="{if ($braille-charset!='') then 'custom' else 'unicode'}"/>
	</xsl:template>
	
	<xsl:template mode="translate-style" match="css:property[@name='string-set']">
		<xsl:if test="@value!='none'">
			<xsl:variable name="evaluated-string-set" as="element()*">
				<xsl:apply-templates mode="eval-string-set" select="css:parse-string-set(@value)"/>
			</xsl:variable>
			<xsl:copy>
				<xsl:sequence select="@name"/>
				<xsl:attribute name="value" select="css:serialize-string-set($evaluated-string-set)"/>
			</xsl:copy>
		</xsl:if>
	</xsl:template>
	
	<xsl:template mode="translate-style"
	              match="css:content|css:string[@name]|css:counter|css:text|css:leader|css:custom-func|css:flow[@from]">
		<xsl:sequence select="."/>
	</xsl:template>
	
	<xsl:template mode="eval-string-set" match="css:string-set" as="element()">
		<xsl:param name="context" as="element()" tunnel="yes"/>
		<xsl:copy>
			<xsl:sequence select="@name"/>
			<xsl:variable name="evaluated-content-list" as="element()*">
				<xsl:apply-templates mode="#current" select="css:parse-content-list(@value, $context)">
					<xsl:with-param name="string-name" select="@name" tunnel="yes"/>
				</xsl:apply-templates>
			</xsl:variable>
			<xsl:attribute name="value" select="if (exists($evaluated-content-list))
			                                    then css:serialize-content-list($evaluated-content-list)
			                                    else '&quot;&quot;'"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template mode="eval-string-set" match="css:string[@value]|css:attr" as="element()?">
		<xsl:param name="context" as="element()" tunnel="yes"/>
		<xsl:variable name="evaluated-string" as="xs:string?">
			<xsl:apply-templates mode="css:eval" select=".">
				<xsl:with-param name="context" select="$context"/>
			</xsl:apply-templates>
		</xsl:variable>
		<xsl:if test="exists($evaluated-string)">
			<css:string value="{$evaluated-string}"/>
		</xsl:if>
	</xsl:template>
	
	<xsl:template mode="eval-string-set" match="css:content[not(@target)]" as="element()?">
		<xsl:param name="context" as="element()" tunnel="yes"/>
		<xsl:variable name="as-string" as="xs:string" select="string($context)"/>
		<xsl:if test="not($as-string='')">
			<css:string value="{$as-string}"/>
		</xsl:if>
	</xsl:template>
	
	<xsl:template mode="eval-string-set" match="css:string[@name][not(@target)]">
		<xsl:message>string() function not supported in string-set property</xsl:message>
	</xsl:template>
	
	<xsl:template mode="eval-string-set" match="css:counter[not(@target)]">
		<xsl:message>counter() function not supported in string-set property</xsl:message>
	</xsl:template>
	
	<xsl:template mode="eval-string-set" match="css:text[@target]">
		<xsl:message>target-text() function not supported in string-set property</xsl:message>
	</xsl:template>
	
	<xsl:template mode="eval-string-set" match="css:string[@name][@target]">
		<xsl:message>target-string() function not supported in string-set property</xsl:message>
	</xsl:template>
	
	<xsl:template mode="eval-string-set" match="css:counter[@target]">
		<xsl:message>target-counter() function not supported in string-set property</xsl:message>
	</xsl:template>
	
	<xsl:template mode="eval-string-set" match="css:content[@target]">
		<xsl:message>target-content() function not supported in string-set property</xsl:message>
	</xsl:template>
	
	<xsl:template mode="eval-string-set" match="css:leader">
		<xsl:message>leader() function not supported in string-set property</xsl:message>
	</xsl:template>
	
	<xsl:template mode="eval-string-set" match="css:custom-func">
		<xsl:message><xsl:value-of select="@name"/>() function not supported in string-set property</xsl:message>
	</xsl:template>
	
	<xsl:template mode="translate-style" match="css:property[@name='content' and @value[not(.='none')]]">
		<xsl:param name="context" as="element()" tunnel="yes"/>
		<xsl:call-template name="translate-content-list">
			<xsl:with-param name="content-list" select="css:parse-content-list(@value, $context)"/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template mode="translate-style" match="css:property[@name='content' and not(@value)]" name="translate-content-list">
		<xsl:param name="content-list" as="element()*" select="*"/>
		<xsl:variable name="translated-content-list" as="element()*">
			<xsl:apply-templates mode="#current" select="$content-list"/>
		</xsl:variable>
		<xsl:sequence select="css:property('content', if (exists($translated-content-list))
		                                              then css:serialize-content-list($translated-content-list)
		                                              else 'none')"/>
	</xsl:template>
	
	<!--
	    FIXME: Pass context when translating segments of a single content property. If possible also
	    pass context when translating inline pseudo-elements.
	-->
	<xsl:template mode="translate-style" match="css:string[@value]|css:attr" as="element()?">
		<xsl:param name="context" as="element()" tunnel="yes"/>
		<xsl:param name="source-style" as="element(css:property)*" tunnel="yes"/>
		<xsl:param name="result-style" as="element(css:property)*" tunnel="yes"/>
		<xsl:variable name="evaluated-string" as="xs:string">
			<xsl:apply-templates mode="css:eval" select=".">
				<xsl:with-param name="context" select="$context"/>
			</xsl:apply-templates>
		</xsl:variable>
		<xsl:variable name="lang" as="xs:string?" select="($context/ancestor-or-self::*[@xml:lang][1]/@xml:lang,'und')[1]"/>
		<xsl:variable name="block">
			<xsl:element name="css:block">
				<xsl:if test="$lang">
					<xsl:attribute name="xml:lang" select="$lang"/>
				</xsl:if>
				<xsl:value-of select="$evaluated-string"/>
			</xsl:element>
		</xsl:variable>
		<xsl:variable name="source-style" as="element()*" select="$source-style"/>
		<xsl:variable name="result-style" as="element()*" select="$result-style"/>
		<xsl:variable name="translated-block" as="node()*">
			<xsl:apply-templates select="$block/css:block">
				<xsl:with-param name="context" select="$context"/>
				<xsl:with-param name="source-style" tunnel="yes" select="$source-style"/>
				<xsl:with-param name="result-style" tunnel="yes" select="$result-style"/>
			</xsl:apply-templates>
		</xsl:variable>
		<css:string value="{string-join($translated-block/string(.),'')}"/>
	</xsl:template>
	
	<xsl:variable name="empty-style" as="element(css:rule)"><css:rule/></xsl:variable>
	
	<xsl:template mode="treewalk" match="*">
		<xsl:param name="new-text-nodes" as="xs:string*" required="yes"/>
		<xsl:param name="source-style" as="element(css:property)*" tunnel="yes"/>
		<xsl:param name="result-style" as="element(css:property)*" tunnel="yes"/>
		<xsl:variable name="text-node-count" select="count(.//text())"/>
		<xsl:variable name="style" as="element(css:rule)*">
			<xsl:if test="@css:*">
				<css:rule>
					<xsl:apply-templates mode="css:attribute-as-property" select="@css:*"/>
				</css:rule>
			</xsl:if>
			<xsl:call-template name="css:deep-parse-stylesheet">
				<xsl:with-param name="stylesheet" select="@style"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="style" as="element(css:rule)*"
		              select="if (exists($style)) then $style else $empty-style"/>
		<xsl:variable name="context" as="element()" select="."/>
		<xsl:variable name="translated-style" as="element(css:rule)*">
			<xsl:call-template name="translate-style">
				<xsl:with-param name="style" select="$style"/>
				<xsl:with-param name="context" tunnel="yes" select="$context"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:element name="{name(.)}" namespace="{namespace-uri(.)}">
			<xsl:sequence select="@* except (@style|@css:*|@xml:lang)"/>
			<xsl:call-template name="insert-style">
				<xsl:with-param name="style" select="$translated-style"/>
			</xsl:call-template>
			<xsl:if test="@xml:lang
			              or (
			                ancestor::*[@xml:lang]
			                and $translated-style[not(@selector)]/css:property[@name='text-transform']
			                and not($translated-style[not(@selector)]/css:property[@name='text-transform'][1]/@value/string(.)
			                        =($result-style[@name='text-transform']/@value/string(.),'auto')[1]))">
				<xsl:variable name="lang" as="xs:string" select="(ancestor-or-self::*[@xml:lang][1]/@xml:lang,'und')[1]"/>
				<xsl:attribute name="xml:lang"
				               select="if (($translated-style[not(@selector)]/css:property[@name='text-transform']/@value/string(.),
				                            $result-style[@name='text-transform']/@value/string(.),
				                            'auto'
				                            )[1]='none')
				                       then concat($lang,'-Brai')
				                       else $lang"/>
			</xsl:if>
			<xsl:variable name="source-style" as="element()*">
				<xsl:call-template name="css:computed-properties">
					<xsl:with-param name="properties" select="$text-properties"/>
					<xsl:with-param name="context" select="$dummy-element"/>
					<xsl:with-param name="cascaded-properties" tunnel="yes" select="$style[not(@selector)]/css:property"/>
					<xsl:with-param name="parent-properties" tunnel="yes" select="$source-style"/>
				</xsl:call-template>
			</xsl:variable>
			<xsl:variable name="result-style" as="element()*">
				<xsl:call-template name="css:computed-properties">
					<xsl:with-param name="properties" select="$text-properties"/>
					<xsl:with-param name="context" select="$dummy-element"/>
					<xsl:with-param name="cascaded-properties" tunnel="yes" select="$translated-style[not(@selector)]/css:property"/>
					<xsl:with-param name="parent-properties" tunnel="yes" select="$result-style"/>
				</xsl:call-template>
			</xsl:variable>
			<xsl:apply-templates mode="#current" select="child::node()[1]">
				<xsl:with-param name="new-text-nodes" select="$new-text-nodes[position()&lt;=$text-node-count]"/>
				<xsl:with-param name="source-style" tunnel="yes" select="$source-style"/>
				<xsl:with-param name="result-style" tunnel="yes" select="$result-style"/>
				<xsl:with-param name="restore-text-style" tunnel="yes" select="false()"/>
			</xsl:apply-templates>
		</xsl:element>
		<xsl:apply-templates mode="#current" select="following-sibling::node()[1]">
			<xsl:with-param name="new-text-nodes" select="$new-text-nodes[position()&gt;$text-node-count]"/>
		</xsl:apply-templates>
	</xsl:template>
	
	<xsl:template mode="treewalk" match="text()">
		<xsl:param name="new-text-nodes" as="xs:string*" required="yes"/>
		<xsl:param name="restore-text-style" as="xs:boolean" tunnel="yes" select="false()"/>
		<xsl:param name="result-style" as="element(css:property)*" tunnel="yes"/>
		<xsl:choose>
			<xsl:when test="$restore-text-style">
				<xsl:variable name="restored-style" as="element(css:rule)?">
					<xsl:call-template name="translate-style">
						<xsl:with-param name="style" select="$empty-style"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:variable name="restored-style" as="element(css:property)*">
					<xsl:apply-templates mode="insert-style" select="$restored-style/css:property"/>
				</xsl:variable>
				<xsl:choose>
					<xsl:when test="exists($restored-style) and
					                not(string(.)='') and
					                (not(normalize-space(.)='')
					                 or ($restored-style/css:property[@name='white-space'],
					                     $result-style/css:property[@name='white-space'])[1][not(@value='normal')])">
						<_>
							<xsl:sequence select="css:style-attribute(css:serialize-stylesheet($restored-style))"/>
							<xsl:if test="ancestor::*[@xml:lang]
							              and $restored-style[@name='text-transform']
							              and not($restored-style[@name='text-transform'][1]/@value/string(.)
							                      =($result-style[@name='text-transform']/@value/string(.),'auto')[1])">
								<xsl:variable name="lang" as="xs:string" select="ancestor::*[@xml:lang][1]/@xml:lang"/>
								<xsl:attribute name="xml:lang"
								               select="if ($restored-style[@name='text-transform'][1]/@value='none')
								                       then concat($lang,'-Brai')
								                       else $lang"/>
							</xsl:if>
							<xsl:value-of select="$new-text-nodes[1]"/>
						</_>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="$new-text-nodes[1]"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$new-text-nodes[1]"/>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:apply-templates mode="#current" select="following-sibling::node()[1]">
			<xsl:with-param name="new-text-nodes" select="$new-text-nodes[position()&gt;1]"/>
		</xsl:apply-templates>
	</xsl:template>
	
	<xsl:template mode="restore-text-style" match="css:property[@name=$text-properties]">
		<xsl:param name="source-style" as="element()*" tunnel="yes"/>
		<xsl:variable name="name" as="xs:string" select="@name"/>
		<xsl:sequence select="$source-style[@name=$name]"/>
	</xsl:template>
	
	<xsl:template name="insert-style" as="attribute()?"> <!-- @style? -->
		<xsl:param name="style" as="element()*" required="yes"/> <!-- (css:rule|css:property)* -->
		<xsl:variable name="style" as="element()*">
			<xsl:apply-templates mode="insert-style" select="$style"/>
		</xsl:variable>
		<xsl:sequence select="css:style-attribute(css:serialize-stylesheet($style))"/>
	</xsl:template>
	
	<xsl:template mode="insert-style" match="css:rule">
		<xsl:if test="exists(*)">
			<xsl:sequence select="."/>
		</xsl:if>
	</xsl:template>
	
	<xsl:template mode="insert-style" match="css:property">
		<xsl:sequence select="."/>
	</xsl:template>
	
	<xsl:template mode="insert-style" match="css:property[@name=$text-properties]">
		<xsl:param name="result-style" as="element()*" tunnel="yes"/>
		<xsl:variable name="name" as="xs:string" select="@name"/>
		<xsl:variable name="value" as="xs:string" select="@value"/>
		<xsl:choose>
			<xsl:when test="$result-style[@name=$name][@value=$value]"/> <!-- all text properties are inheriting -->
			<xsl:otherwise>
				<xsl:sequence select="."/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:variable name="dummy-element" as="element()"><_/></xsl:variable>
	
	<xsl:template name="css:cascaded-properties" as="element()*">
		<xsl:param name="properties" as="xs:string*" select="('#all')"/>
		<xsl:param name="validate" as="xs:boolean" select="false()"/>
		<xsl:param name="context" as="element()" select="."/>
		<xsl:param name="cascaded-properties" as="element(css:property)*" select="()" tunnel="yes"/>
		<xsl:sequence select="for $name in distinct-values(
		                                     if ('#all'=$properties)
		                                     then $cascaded-properties/@name
		                                     else $properties)
		                      return $cascaded-properties[@name=$name][last()]"/>
	</xsl:template>
	
	<xsl:template name="css:parent-property" as="element()?">
		<xsl:param name="property" as="xs:string" required="yes"/>
		<xsl:param name="compute" as="xs:boolean" select="false()"/>
		<xsl:param name="concretize-inherit" as="xs:boolean" select="true()"/>
		<xsl:param name="concretize-initial" as="xs:boolean" select="true()"/>
		<xsl:param name="validate" as="xs:boolean"/>
		<xsl:param name="context" as="element()" select="."/>
		<xsl:param name="parent-properties" as="element(css:property)*" select="()" tunnel="yes"/>
		<xsl:choose>
			<xsl:when test="exists($parent-properties[@name=$property])">
				<xsl:sequence select="$parent-properties[@name=$property][last()]"/>
			</xsl:when>
			<xsl:when test="$concretize-initial">
				<xsl:sequence select="css:property($property, css:initial-value($property))"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:sequence select="css:property($property, 'initial')"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
</xsl:stylesheet>
