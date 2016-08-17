<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                exclude-result-prefixes="#all">
	
	<xsl:import href="../library.xsl"/>
	
	<!--
	    API: implement xsl:template match="css:block"
	-->
	<xsl:template mode="#default after before" match="css:block">
		<xsl:message terminate="yes">Coding error</xsl:message>
	</xsl:template>
	
	<xsl:template match="/*">
		<xsl:apply-templates select="." mode="identify-blocks"/>
	</xsl:template>
	
	<xsl:template mode="identify-blocks" match="/*">
		<_ style="text-transform: none">
			<xsl:variable name="source-style" as="element()*">
				<xsl:call-template name="css:computed-properties">
					<xsl:with-param name="properties" select="$text-properties"/>
					<xsl:with-param name="context" select="$dummy-element"/>
				</xsl:call-template>
			</xsl:variable>
			<xsl:variable name="result-style" as="element()*">
				<xsl:call-template name="css:computed-properties">
					<xsl:with-param name="properties" select="$text-properties"/>
					<xsl:with-param name="context" select="$dummy-element"/>
					<xsl:with-param name="cascaded-properties" tunnel="yes" select="css:property('text-transform','none')"/>
				</xsl:call-template>
			</xsl:variable>
			<xsl:next-match>
				<xsl:with-param name="source-style" tunnel="yes" select="$source-style"/>
				<xsl:with-param name="result-style" tunnel="yes" select="$result-style"/>
			</xsl:next-match>
		</_>
	</xsl:template>
	
	<xsl:variable name="text-properties" as="xs:string*"
	              select="$css:properties[css:applies-to(., 'inline') and css:is-inherited(.)]"/>
	
	<xsl:template mode="identify-blocks" match="*">
		<!-- parent is block -->
		<xsl:param name="is-block" as="xs:boolean" select="true()" tunnel="yes"/>
		<!-- computed text properties of the parent element in the source -->
		<xsl:param name="source-style" as="element()*" tunnel="yes"/> <!-- css:property* -->
		<!-- computed text properties of the parent element in the result -->
		<xsl:param name="result-style" as="element()*" tunnel="yes"/> <!-- css:property* -->
		<xsl:variable name="style" as="element()*" select="css:deep-parse-stylesheet(@style)"/> <!-- css:rule* -->
		<xsl:variable name="context" as="element()" select="."/>
		<xsl:variable name="translated-style" as="element()*">
			<xsl:call-template name="translate-style">
				<xsl:with-param name="style" select="$style"/>
				<xsl:with-param name="context" tunnel="yes" select="$context"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:copy>
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
			<xsl:sequence select="@* except @style"/>
			<xsl:call-template name="insert-style">
				<xsl:with-param name="style" select="$translated-style"/>
			</xsl:call-template>
			<xsl:variable name="lang" as="xs:string?" select="(ancestor-or-self::*[@xml:lang][1]/@xml:lang,'und')[1]"/>
			<xsl:for-each-group select="*|text()"
			                    group-adjacent="$is-block and boolean(descendant-or-self::*[@css:display[not(.='inline')]])">
				<xsl:choose>
					<xsl:when test="current-grouping-key()">
						<xsl:for-each select="current-group()">
							<xsl:apply-templates mode="#current" select=".">
								<xsl:with-param name="is-block" select="$is-block" tunnel="yes"/>
								<xsl:with-param name="source-style" tunnel="yes" select="$source-style"/>
								<xsl:with-param name="result-style" tunnel="yes" select="$result-style"/>
							</xsl:apply-templates>
						</xsl:for-each>
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
						<xsl:apply-templates select="$block/css:block">
							<xsl:with-param name="context" select="$context"/>
							<xsl:with-param name="source-style" tunnel="yes" select="$source-style"/>
							<xsl:with-param name="result-style" tunnel="yes" select="$result-style"/>
						</xsl:apply-templates>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each-group>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template mode="identify-blocks" match="@*|text()">
		<xsl:sequence select="."/>
	</xsl:template>
	
	<xsl:template name="translate-style" as="element()*"> <!-- css:rule* -->
		<xsl:param name="style" as="element()*" required="yes"/> <!-- css:rule* -->
		<xsl:param name="source-style" as="element()*" tunnel="yes"/> <!-- css:property* -->
		<xsl:param name="result-style" as="element()*" tunnel="yes"/> <!-- css:property* -->
		<xsl:variable name="main-style" as="element()?" select="$style[not(@selector)]"/> <!-- css:rule* -->
		<xsl:variable name="translated-main-style" as="element()?"> <!-- css:rule* -->
			<xsl:apply-templates mode="translate-style" select="$main-style"/>
		</xsl:variable>
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
			<xsl:apply-templates mode="translate-style" select="$style[@selector=('::before','::after')]">
				<xsl:with-param name="restore-text-style" tunnel="yes" select="true()"/>
				<xsl:with-param name="source-style" tunnel="yes" select="$source-style"/>
				<xsl:with-param name="result-style" tunnel="yes" select="$result-style"/>
			</xsl:apply-templates>
			<xsl:sequence select="$style[@selector and matches(@selector,'^@text-transform')]"/>
			<xsl:apply-templates mode="translate-style" select="$style[@selector and not(matches(@selector,'^(::(before|after)$|@text-transform)'))]">
				<xsl:with-param name="restore-text-style" tunnel="yes" select="true()"/>
			</xsl:apply-templates>
		</xsl:variable>
		<xsl:apply-templates mode="insert-style" select="$translated-style"/>
	</xsl:template>
	
	<xsl:template mode="translate-style" match="css:rule">
		<xsl:param name="mode" as="xs:string" tunnel="yes" select="'#default'"/>
		<xsl:param name="source-style" as="element()*" tunnel="yes"/> <!-- css:property* -->
		<xsl:param name="restore-text-style" as="xs:boolean" tunnel="yes" select="false()"/>
		<xsl:copy>
			<xsl:sequence select="@selector"/>
			<xsl:variable name="mode" as="xs:string" select="if (@selector='::before') then 'before'
			                                                 else if (@selector='::after') then 'after'
			                                                 else $mode"/>
			<xsl:variable name="translated-style" as="element()*">
				<xsl:choose>
					<xsl:when test="css:rule">
						<xsl:call-template name="translate-style">
							<xsl:with-param name="style" select="css:rule"/>
							<xsl:with-param name="mode" tunnel="yes" select="$mode"/>
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
						<xsl:choose>
							<xsl:when test="$restore-text-style">
								<xsl:variable name="properties" as="element()*" select="css:property"/>
								<xsl:apply-templates mode="restore-text-style-and-translate-other-style"
								                     select="($properties,$source-style[not(@name=$properties/@name)])">
									<xsl:with-param name="mode" tunnel="yes" select="$mode"/>
									<xsl:with-param name="source-style" tunnel="yes" select="$source-style"/>
								</xsl:apply-templates>
							</xsl:when>
							<xsl:otherwise>
								<xsl:apply-templates mode="#current" select="css:property">
									<xsl:with-param name="mode" tunnel="yes" select="$mode"/>
									<xsl:with-param name="source-style" tunnel="yes" select="$source-style"/>
								</xsl:apply-templates>
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
	
	<xsl:template mode="restore-text-style-and-translate-other-style" match="css:property[@name=$text-properties]">
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
	
	<xsl:template mode="translate-style" match="css:property[@name='content' and not(@value='none')]">
		<xsl:param name="context" as="element()" tunnel="yes"/>
		<xsl:variable name="translated-content-list" as="element()*">
			<xsl:apply-templates mode="#current" select="css:parse-content-list(@value, $context)"/>
		</xsl:variable>
		<xsl:sequence select="css:property('content', if (exists($translated-content-list))
					                                  then css:serialize-content-list($translated-content-list)
					                                  else '&quot;&quot;')"/>
	</xsl:template>
	
	<xsl:template mode="translate-style" match="css:string[@value]|css:attr" as="element()?">
		<xsl:param name="context" as="element()" tunnel="yes"/>
		<xsl:param name="source-style" as="element()*" tunnel="yes"/> <!-- css:property* -->
		<xsl:param name="result-style" as="element()*" tunnel="yes"/> <!-- css:property* -->
		<xsl:param name="mode" as="xs:string" tunnel="yes"/> <!-- before|after -->
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
			<xsl:choose>
				<xsl:when test="$mode='before'">
					<xsl:apply-templates mode="before" select="$block/css:block">
						<xsl:with-param name="context" select="$context"/>
						<xsl:with-param name="source-style" tunnel="yes" select="$source-style"/>
						<xsl:with-param name="result-style" tunnel="yes" select="$result-style"/>
					</xsl:apply-templates>
				</xsl:when>
				<xsl:when test="$mode='after'">
					<xsl:apply-templates mode="after" select="$block/css:block">
						<xsl:with-param name="context" select="$context"/>
						<xsl:with-param name="source-style" tunnel="yes" select="$source-style"/>
						<xsl:with-param name="result-style" tunnel="yes" select="$result-style"/>
					</xsl:apply-templates>
				</xsl:when>
				<xsl:otherwise>
					<xsl:sequence select="$block/css:block"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<css:string value="{string-join($translated-block/string(.),'')}"/>
	</xsl:template>
	
	<xsl:template mode="treewalk" match="*">
		<xsl:param name="new-text-nodes" as="xs:string*" required="yes"/>
		<xsl:param name="source-style" as="element()*" tunnel="yes"/> <!-- css:property* -->
		<xsl:param name="result-style" as="element()*" tunnel="yes"/> <!-- css:property* -->
		<xsl:variable name="text-node-count" select="count(.//text())"/>
		<xsl:variable name="style" as="element()*" select="css:deep-parse-stylesheet(@style)"/> <!-- css:rule* -->
		<xsl:variable name="context" as="element()" select="."/>
		<xsl:variable name="translated-style" as="element()*">
			<xsl:call-template name="translate-style">
				<xsl:with-param name="style" select="$style"/>
				<xsl:with-param name="context" tunnel="yes" select="$context"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:copy>
			<xsl:variable name="source-style" as="element()*">
				<xsl:call-template name="css:computed-properties">
					<xsl:with-param name="properties" select="$text-properties"/>
					<xsl:with-param name="context" select="$dummy-element"/>
					<xsl:with-param name="cascaded-properties" tunnel="yes" select="$style/css:property"/>
					<xsl:with-param name="parent-properties" tunnel="yes" select="$source-style"/>
				</xsl:call-template>
			</xsl:variable>
			<xsl:variable name="result-style" as="element()*">
				<xsl:call-template name="css:computed-properties">
					<xsl:with-param name="properties" select="$text-properties"/>
					<xsl:with-param name="context" select="$dummy-element"/>
					<xsl:with-param name="cascaded-properties" tunnel="yes" select="$translated-style/css:property"/>
					<xsl:with-param name="parent-properties" tunnel="yes" select="$result-style"/>
				</xsl:call-template>
			</xsl:variable>
			<xsl:sequence select="@* except @style"/>
			<xsl:call-template name="insert-style">
				<xsl:with-param name="style" select="$translated-style"/>
			</xsl:call-template>
			<xsl:apply-templates mode="#current" select="child::node()[1]">
				<xsl:with-param name="new-text-nodes" select="$new-text-nodes[position()&lt;=$text-node-count]"/>
				<xsl:with-param name="source-style" tunnel="yes" select="$source-style"/>
				<xsl:with-param name="result-style" tunnel="yes" select="$result-style"/>
			</xsl:apply-templates>
		</xsl:copy>
		<xsl:apply-templates mode="#current" select="following-sibling::node()[1]">
			<xsl:with-param name="new-text-nodes" select="$new-text-nodes[position()&gt;$text-node-count]"/>
		</xsl:apply-templates>
	</xsl:template>
	
	<xsl:template mode="treewalk" match="text()">
		<xsl:param name="new-text-nodes" as="xs:string*" required="yes"/>
		<xsl:value-of select="$new-text-nodes[1]"/>
		<xsl:apply-templates mode="#current" select="following-sibling::node()[1]">
			<xsl:with-param name="new-text-nodes" select="$new-text-nodes[position()&gt;1]"/>
		</xsl:apply-templates>
	</xsl:template>
	
	<xsl:variable name="empty-style" as="element()"><css:rule/></xsl:variable>
	
	<xsl:template mode="treewalk" match="text()" priority="0.6">
		<xsl:param name="new-text-nodes" as="xs:string*" required="yes"/>
		<xsl:param name="restore-text-style" as="xs:boolean" tunnel="yes" select="false()"/>
		<xsl:param name="result-style" as="element()*" tunnel="yes"/> <!-- css:property* -->
		<xsl:choose>
			<xsl:when test="$restore-text-style">
				<xsl:variable name="restored-style" as="element()*"> <!-- css:rule? -->
					<xsl:call-template name="translate-style">
						<xsl:with-param name="style" select="$empty-style"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:variable name="restored-style" as="element()*">
					<xsl:apply-templates mode="insert-style" select="$restored-style/css:property"/>
				</xsl:variable>
				<xsl:choose>
					<xsl:when test="exists($restored-style)">
						<_>
							<xsl:sequence select="css:style-attribute(css:serialize-stylesheet($restored-style))"/>
							<xsl:variable name="result-style" as="element()*">
								<xsl:call-template name="css:computed-properties">
									<xsl:with-param name="properties" select="$text-properties"/>
									<xsl:with-param name="context" select="$dummy-element"/>
									<xsl:with-param name="cascaded-properties" tunnel="yes" select="$restored-style"/>
									<xsl:with-param name="parent-properties" tunnel="yes" select="$result-style"/>
								</xsl:call-template>
							</xsl:variable>
							<xsl:next-match>
								<xsl:with-param name="new-text-nodes" select="$new-text-nodes"/>
								<xsl:with-param name="result-style" tunnel="yes" select="$result-style"/>
							</xsl:next-match>
						</_>
					</xsl:when>
					<xsl:otherwise>
						<xsl:next-match>
							<xsl:with-param name="new-text-nodes" select="$new-text-nodes"/>
						</xsl:next-match>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:next-match>
					<xsl:with-param name="new-text-nodes" select="$new-text-nodes"/>
				</xsl:next-match>
			</xsl:otherwise>
		</xsl:choose>
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
			<xsl:when test="$result-style[@name=$name][@value=$value]"/>
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
		<xsl:param name="cascaded-properties" as="element()*" select="()" tunnel="yes"/> <!-- css:property* -->
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
		<xsl:param name="parent-properties" as="element()*" select="()" tunnel="yes"/> <!-- css:property* -->
		<xsl:choose>
			<xsl:when test="exists($parent-properties[@name=$property])">
				<xsl:sequence select="$parent-properties[@name=$property]"/>
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
