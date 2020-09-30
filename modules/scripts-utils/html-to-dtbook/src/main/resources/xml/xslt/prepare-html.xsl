<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="3.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:f="functions"
                xmlns="http://www.w3.org/1999/xhtml"
                xpath-default-namespace="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="#all">
	
	<xsl:template match="/html/body">
		<xsl:apply-templates mode="restructure" select=".">
			<xsl:with-param name="fragment" select="node()"/>
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template mode="restructure" match="body|section|aside|nav|article">
		<xsl:param name="fragment" as="node()*"/>
		<xsl:copy>
			<xsl:sequence select="@*"/>
			<xsl:variable name="subsections" as="element()*"
			              select="f:except-descendants(descendant::*[f:is-sectioning-element(.)])"/>
			<xsl:variable name="heading" as="element()?" select="descendant::*[f:is-heading-element(.)][1]
			                                                     except $subsections/descendant::*"/>
			<xsl:if test="$heading">
				<xsl:variable name="start" as="element()" select="$heading/f:propagate(.,'before')"/>
				<xsl:variable name="end" as="element()" select="$heading/f:propagate(.,'after')"/>
				<xsl:call-template name="reconstruct-tree">
					<xsl:with-param name="nodes" select="f:nodes-before($fragment,$start)"/>
				</xsl:call-template>
				<xsl:sequence select="f:except-descendants(
				                        f:nodes-not-after(f:nodes-not-before($fragment,$start),$end)
				                        /descendant-or-self::node()
				                        except $heading/ancestor::*)"/>
			</xsl:if>
			<xsl:variable name="fragment" as="node()*"
			              select="f:nodes-after($fragment,$heading/f:propagate(.,'after'))"/>
			<xsl:iterate select="$subsections">
				<xsl:param name="remaining-content" as="node()*" select="$fragment"/>
				<xsl:on-completion>
					<xsl:call-template name="reconstruct-tree">
						<xsl:with-param name="nodes" select="$remaining-content"/>
					</xsl:call-template>
				</xsl:on-completion>
				<xsl:variable name="start" as="element()" select="f:propagate(.,'before')"/>
				<xsl:variable name="end" as="element()" select="f:propagate(.,'after')"/>
				<xsl:call-template name="reconstruct-tree">
					<xsl:with-param name="nodes" select="f:nodes-before($remaining-content,$start)"/>
				</xsl:call-template>
				<xsl:apply-templates mode="#current" select=".">
					<xsl:with-param name="fragment"
					                select="f:nodes-not-after(
					                          f:nodes-not-before($remaining-content,$start),
					                          $end)"/>
				</xsl:apply-templates>
				<xsl:next-iteration>
					<xsl:with-param name="remaining-content" select="f:nodes-after($remaining-content,$end)"/>
				</xsl:next-iteration>
			</xsl:iterate>
		</xsl:copy>
	</xsl:template>

	<!-- reconstruct original tree structure from flat sequence of nodes -->
	<xsl:template name="reconstruct-tree" as="node()*">
		<xsl:param name="nodes" as="node()*" required="yes"/>
		<xsl:choose>
			<xsl:when test="not($nodes/self::*
			                   |$nodes/self::text()[normalize-space(.)])">
				<xsl:sequence select="$nodes"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates mode="reconstruct" select="collection()/html/body">
					<xsl:with-param name="nodes" tunnel="yes" select="$nodes"/>
				</xsl:apply-templates>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template mode="reconstruct" match="/html/body|
	                                        section|aside|nav|article">
		<xsl:apply-templates mode="#current" select="node()"/>
	</xsl:template>

	<xsl:template mode="reconstruct" match="node()">
		<xsl:param name="nodes" tunnel="yes" as="node()*" required="yes"/>
		<xsl:choose>
			<xsl:when test="ancestor-or-self::node() intersect $nodes">
				<xsl:sequence select="."/>
			</xsl:when>
			<xsl:when test="self::*[descendant::node() intersect $nodes]">
				<xsl:copy>
					<xsl:sequence select="@* except @id"/>
					<xsl:if test="not((descendant::node()[self::*
					                                     |self::text()[normalize-space(.)]]
					                   except descendant::*[f:is-heading-element(.)]/descendant-or-self::node())
					                  intersect (descendant::node() intersect $nodes)[1]/preceding::node())">
						<xsl:sequence select="@id"/>
					</xsl:if>
					<xsl:apply-templates mode="#current" select="node()"/>
				</xsl:copy>
			</xsl:when>
		</xsl:choose>
	</xsl:template>

	<xsl:function name="f:is-sectioning-element" as="xs:boolean">
		<xsl:param name="element" as="element()"/>
		<xsl:sequence select="exists($element/self::body
		                            |$element/self::section
		                            |$element/self::aside
		                            |$element/self::nav
		                            |$element/self::article)"/>
	</xsl:function>

	<!-- heading element or 'header' element that contains heading element -->
	<xsl:function name="f:is-heading-element" as="xs:boolean">
		<xsl:param name="element" as="element()"/>
		<xsl:sequence select="exists($element/self::h1
		                            |$element/self::h2
		                            |$element/self::h3
		                            |$element/self::h4
		                            |$element/self::h5
		                            |$element/self::h6
		                            |$element/self::hgroup
		                            |$element/self::header[*[f:is-heading-element(.)]])"/>
	</xsl:function>

	<xsl:function name="f:propagate" as="node()">
		<xsl:param name="split-point" as="node()"/>
		<xsl:param name="side" as="xs:string"/> <!-- before|after -->
		<xsl:choose>
			<xsl:when test="$side='before'">
				<xsl:sequence select="if ($split-point/parent::*
				                          and not($split-point
				                                  /preceding-sibling::node()[self::*
				                                                            |self::text()[normalize-space(.)]]))
				                      then f:propagate($split-point/parent::*,$side)
				                      else $split-point"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:sequence select="if ($split-point/parent::*
				                          and not($split-point
				                                  /following-sibling::node()[self::*
				                                                            |self::text()[normalize-space(.)]]))
				                      then f:propagate($split-point/parent::*,$side)
				                      else $split-point"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:function>

	<xsl:function name="f:nodes-before" as="node()*">
		<xsl:param name="nodes" as="node()*"/>
		<xsl:param name="split-before" as="node()?"/>
		<xsl:sequence select="if (not(exists($split-before)))
		                      then $nodes
		                      else f:except-descendants(
		                             $nodes/descendant-or-self::node()
		                             intersect $split-before/preceding::node())"/>
	</xsl:function>

	<xsl:function name="f:nodes-not-before" as="node()*">
		<xsl:param name="nodes" as="node()*"/>
		<xsl:param name="split-before" as="node()?"/>
		<xsl:sequence select="if (not(exists($split-before)))
		                      then ()
		                      else f:except-descendants(
		                             $nodes/descendant-or-self::node()
		                             except $split-before/(ancestor::node()|preceding::node()))"/>
	</xsl:function>

	<xsl:function name="f:nodes-after" as="node()*">
		<xsl:param name="nodes" as="node()*"/>
		<xsl:param name="split-after" as="node()?"/>
		<xsl:sequence select="if (not(exists($split-after)))
		                      then $nodes
		                      else f:except-descendants(
		                             $nodes/descendant-or-self::node()
		                             intersect $split-after/following::node())"/>
	</xsl:function>

	<xsl:function name="f:nodes-not-after" as="node()*">
		<xsl:param name="nodes" as="node()*"/>
		<xsl:param name="split-after" as="node()?"/>
		<xsl:sequence select="if (not(exists($split-after)))
		                      then ()
		                      else f:except-descendants(
		                             $nodes/descendant-or-self::node()
		                             except $split-after/(ancestor::node()|following::node()))"/>
	</xsl:function>

	<xsl:function name="f:except-descendants">
		<xsl:param name="nodes" as="node()*"/>
		<xsl:sequence select="$nodes except $nodes/descendant::node()"/>
	</xsl:function>

	<xsl:template mode="#default" match="@*|node()">
		<xsl:copy>
			<xsl:sequence select="@*"/>
			<xsl:apply-templates mode="#current"/>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>
