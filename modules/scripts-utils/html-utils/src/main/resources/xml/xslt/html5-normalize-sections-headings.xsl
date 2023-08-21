<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="3.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:f="functions"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:epub="http://www.idpf.org/2007/ops"
                xmlns="http://www.w3.org/1999/xhtml"
                xpath-default-namespace="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="#all">

	<xsl:param name="fix-heading-ranks" required="yes"/> <!-- keep | outline-depth -->
	<xsl:param name="fix-sectioning" required="yes"/>    <!-- keep | outline-depth | no-implied -->
	<xsl:param name="fix-untitled-sections" required="yes"/> <!-- keep | imply-heading | imply-heading-from-aria-label -->

	<xsl:include href="untitled-section-titles.xsl"/>
	<xsl:include href="library.xsl"/>
	<xsl:include href="http://www.daisy.org/pipeline/modules/common-utils/generate-id.xsl"/>

	<!--
	    * d:outline correspond with body|article|aside|nav|section
	    * d:section correspond with fragments of body|article|aside|nav|section
	    * d:section with d:section parent/preceding-sibling correspond with implied sections
	    
	    * a d:outline has a @owner (body|article|aside|nav|section element)
	    * a d:outline has only d:section children
	    * a d:outline has at least one child
	    * a d:section that is the first child of a d:outline has a @owner (the same as the d:outline)
	    * a d:section may have d:outline or d:section children
	    * a d:section may have zero children
	    * a d:section that is the child of a d:section has only d:section children
	    * a d:section that is not a first child has a @heading
	    * a d:section that is the child of a d:section has a @heading
	-->
	<xsl:variable name="root-outline" select="collection()/d:outline"/>
	<xsl:variable name="root" select="collection()[1]"/>
	<xsl:variable name="base-uri" select="pf:normalize-uri(pf:html-base-uri(/*))"/>
	<xsl:variable name="input-toc" select="collection()[3]"/>

	<xsl:key name="id" match="*" use="@id"/>
	<xsl:key name="heading" match="d:section[@heading]" use="@heading"/>
	<xsl:key name="owner" match="d:section[@owner]" use="@owner"/>
	<xsl:key name="absolute-href" match="*[@href]" use="pf:normalize-uri(resolve-uri(@href,base-uri(.)))"/>

	<xsl:template match="*[@id=$root-outline/@owner]"> <!-- body -->
		<xsl:variable name="body" as="element(body)">
			<xsl:choose>
				<xsl:when test="$fix-heading-ranks=('outline-depth','toc-depth')">
					<xsl:apply-templates mode="rename-headings" select="."/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:sequence select="."/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="body" as="element()">
			<xsl:for-each select="$body">
				<xsl:choose>
					<xsl:when test="$fix-untitled-sections=('imply-heading','imply-heading-from-aria-label')">
						<xsl:apply-templates mode="add-implied-headings" select="."/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:sequence select="."/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
		</xsl:variable>
		<xsl:for-each select="$body">
			<xsl:choose>
				<xsl:when test="$fix-sectioning=('outline-depth','no-implied')">
					<xsl:apply-templates mode="wrap-implied-sections" select="$root-outline">
						<xsl:with-param name="sectioning-element" select="."/>
					</xsl:apply-templates>
				</xsl:when>
				<xsl:otherwise>
					<xsl:sequence select="."/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>
	</xsl:template>

	<xsl:template mode="wrap-implied-sections" match="d:outline">
		<xsl:param name="sectioning-element" as="element()" required="yes"/> <!-- body|article|aside|nav|section -->
		<xsl:variable name="sections" as="element(d:section)+" select="*"/>
		<xsl:choose>
			<xsl:when test="count($sections)=1 and not($sections/*)">
				<xsl:sequence select="$sectioning-element"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:variable name="fragment" as="node()*" select="$sectioning-element/node()"/>
				<xsl:variable name="outline" as="item()*">
					<xsl:iterate select="$sections">
						<!-- the unprocessed nodes in this outline -->
						<xsl:param name="remaining-content" as="node()*" select="$fragment"/>
						<xsl:on-completion>
							<xsl:call-template name="reconstruct-tree">
								<xsl:with-param name="nodes" select="$remaining-content"/>
								<xsl:with-param name="top" select="$sectioning-element"/>
								<xsl:with-param name="top-included" select="$fix-sectioning='no-implied'"/>
							</xsl:call-template>
						</xsl:on-completion>
						<xsl:variable name="section" as="element(d:section)" select="."/>
						<xsl:variable name="section-boundaries" as="map(xs:string,node()?)"
						              select="$section/f:section-boundaries(.,$remaining-content[1])"/>
						<xsl:apply-templates mode="#current" select="$section">
							<!-- not using 'start' because we don't want nodes in between sections
							     in 'no-implied' mode -->
							<xsl:with-param name="fragment" select="f:nodes-before($remaining-content,
							                                                       $section-boundaries('end'))"/>
							<xsl:with-param name="fragment-top" select="$sectioning-element"/>
							<xsl:with-param name="fragment-top-included" select="$fix-sectioning='no-implied'"/>
							<xsl:with-param name="wrapper-element" select="()"/>
						</xsl:apply-templates>
						<xsl:next-iteration>
							<xsl:with-param name="remaining-content"
							                select="f:nodes-not-before($remaining-content,
							                                           $section-boundaries('end'))"/>
						</xsl:next-iteration>
					</xsl:iterate>
				</xsl:variable>
				<xsl:call-template name="build">
					<xsl:with-param name="content" select="$outline"/>
					<xsl:with-param name="wrapper-elements" select="if ($fix-sectioning='no-implied')
					                                                then ()
					                                                else $sectioning-element"/>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- The result of this template may be a map item containing info to construct the output,
	     rather than a node sequence. -->
	<xsl:template mode="wrap-implied-sections" match="d:section" as="item()*">
		<!-- The nodes belonging to this section. None of the nodes may be ancestor/descendant of
		     each other. Which ancestors are to be included is determined by the fragment-top and
		     fragment-top-included parameters. -->
		<xsl:param name="fragment" as="node()*" required="yes"/>
		<xsl:param name="fragment-top" as="element()" required="yes"/>
		<xsl:param name="fragment-top-included" as="xs:boolean" required="yes"/>
		<!-- If specified, use this sectioning element to wrap the fragment. The wrapping is done at
		     the deepest possible level. -->
		<xsl:param name="wrapper-element" as="element()?" required="yes"/>
		<xsl:variable name="fragment-text" as="text()*"
		              select="$fragment/descendant-or-self::text()[normalize-space(.)]"/>
		<xsl:variable name="child-sectioning-element" as="element()?"
		              select="d:outline[1]/@owner/key('id',.,$root)"/>
		<xsl:variable name="deepest-common-ancestor" as="element()"
		              select="($fragment-top,
		                       for $e in $fragment-top/descendant::*
		                                 intersect ($child-sectioning-element,$fragment-text[1])[1]/ancestor::*
		                       return (if (not(exists($fragment-text except $e/descendant::node())))
		                               then $e
		                               else ())
		                       )[last()]"/>
		<xsl:variable name="leading-space" as="text()*"
		              select="$deepest-common-ancestor/preceding::text()
		                      intersect $fragment/descendant-or-self::node()"/>
		<xsl:variable name="trailing-space" as="text()*"
		              select="$deepest-common-ancestor/following::text()
		                      intersect $fragment/descendant-or-self::node()"/>
		<xsl:variable name="fragment" as="node()*" select="f:except-descendants(
		                                                     $deepest-common-ancestor/descendant::node()
		                                                     intersect $fragment/descendant-or-self::node())"/>
		<xsl:variable name="section" as="node()*">
			<xsl:variable name="subsections" as="element()*" select="*"/> <!-- element(d:section|d:outline)* -->
			<xsl:iterate select="$subsections">
				<!-- the unprocessed nodes in the parent section -->
				<xsl:param name="remaining-content" as="node()*" select="$fragment"/>
				<!-- the processed subsections (one sectioning element per section) and nodes in
				     between subsections, as a map containing info to construct the output -->
				<xsl:param name="done" as="map(xs:string,item()*)*" select="()"/>
				<xsl:on-completion>
					<xsl:call-template name="build">
						<xsl:with-param name="content" as="item()*">
							<xsl:call-template name="merge">
								<xsl:with-param name="input" as="item()*">
									<xsl:sequence select="$done"/>
									<xsl:call-template name="reconstruct-tree">
										<xsl:with-param name="nodes" select="$remaining-content"/>
										<xsl:with-param name="top" select="$deepest-common-ancestor"/>
										<xsl:with-param name="top-included" select="false()"/>
										<xsl:with-param name="builder" select="true()"/>
									</xsl:call-template>
								</xsl:with-param>
							</xsl:call-template>
						</xsl:with-param>
					</xsl:call-template>
				</xsl:on-completion>
				<xsl:variable name="subsection" as="element()" select="."/> <!-- element(d:section|d:outline) -->
				<xsl:choose>
					<xsl:when test="@owner">
						<xsl:variable name="sectioning-element" as="element()"
						              select="key('id',$subsection/@owner,$root)"/>
						<xsl:variable name="ancestors" as="element()*"
						              select="$deepest-common-ancestor/descendant::*
						                      intersect $sectioning-element/ancestor::*"/>
						<xsl:next-iteration>
							<xsl:with-param name="done" as="item()*">
								<xsl:call-template name="merge">
									<xsl:with-param name="input" as="item()*">
										<xsl:sequence select="$done"/>
										<xsl:call-template name="reconstruct-tree">
											<xsl:with-param name="nodes"
											                select="f:nodes-before($remaining-content,
											                                       $sectioning-element)"/>
											<xsl:with-param name="top" select="$deepest-common-ancestor"/>
											<xsl:with-param name="top-included" select="false()"/>
											<xsl:with-param name="builder" select="true()"/>
										</xsl:call-template>
										<xsl:call-template name="build">
											<xsl:with-param name="content" as="element()*">
												<xsl:apply-templates mode="#current" select="$subsection">
													<xsl:with-param name="sectioning-element"
													                select="$sectioning-element"/>
												</xsl:apply-templates>
											</xsl:with-param>
											<xsl:with-param name="wrapper-elements" select="$ancestors"/>
											<xsl:with-param name="builder" select="true()"/>
										</xsl:call-template>
									</xsl:with-param>
								</xsl:call-template>
							</xsl:with-param>
							<xsl:with-param name="remaining-content"
							                select="f:nodes-after($remaining-content,$sectioning-element)"/>
						</xsl:next-iteration>
					</xsl:when>
					<xsl:otherwise>
						<xsl:variable name="section-boundaries" as="map(xs:string,node()?)"
						              select="$subsection/f:section-boundaries(.,$remaining-content[1])"/>
						<xsl:next-iteration>
							<xsl:with-param name="done" as="item()*">
								<xsl:call-template name="merge">
									<xsl:with-param name="input" as="item()*">
										<xsl:sequence select="$done"/>
										<xsl:call-template name="reconstruct-tree">
											<xsl:with-param name="nodes"
											                select="f:nodes-before($remaining-content,
											                                       $section-boundaries('start'))"/>
											<xsl:with-param name="top" select="$deepest-common-ancestor"/>
											<xsl:with-param name="top-included" select="false()"/>
											<xsl:with-param name="builder" select="true()"/>
										</xsl:call-template>
										<xsl:apply-templates mode="#current" select="$subsection">
											<xsl:with-param name="fragment"
											                select="f:nodes-before(
											                          f:nodes-not-before($remaining-content,
											                                             $section-boundaries('start')),
											                          $section-boundaries('end'))"/>
											<xsl:with-param name="fragment-top" select="$deepest-common-ancestor"/>
											<xsl:with-param name="fragment-top-included" select="false()"/>
											<xsl:with-param name="wrapper-element" select="$generated-sectioning-element"/>
										</xsl:apply-templates>
									</xsl:with-param>
								</xsl:call-template>
							</xsl:with-param>
							<xsl:with-param name="remaining-content"
							                select="f:nodes-not-before($remaining-content,$section-boundaries('end'))"/>
						</xsl:next-iteration>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:iterate>
		</xsl:variable>
		<xsl:variable name="section" as="node()*">
			<xsl:call-template name="build">
				<xsl:with-param name="content" select="$section"/>
				<xsl:with-param name="wrapper-elements" select="$wrapper-element"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="common-ancestors" as="element()*"
		              select="(if ($fragment-top-included) then $fragment-top else (),
		                       $fragment-top/descendant::* intersect $deepest-common-ancestor/ancestor-or-self::*)"/>
		<xsl:call-template name="merge">
			<xsl:with-param name="input" as="item()*">
				<xsl:call-template name="reconstruct-tree">
					<xsl:with-param name="nodes" select="$leading-space"/>
					<xsl:with-param name="top" select="$fragment-top"/>
					<xsl:with-param name="top-included" select="$fragment-top-included"/>
					<xsl:with-param name="builder" select="true()"/>
				</xsl:call-template>
				<xsl:call-template name="build">
					<xsl:with-param name="content" select="$section"/>
					<xsl:with-param name="wrapper-elements" select="$common-ancestors"/>
					<xsl:with-param name="builder" select="true()"/>
				</xsl:call-template>
				<xsl:call-template name="reconstruct-tree">
					<xsl:with-param name="nodes" select="$trailing-space"/>
					<xsl:with-param name="top" select="$fragment-top"/>
					<xsl:with-param name="top-included" select="$fragment-top-included"/>
					<xsl:with-param name="builder" select="true()"/>
				</xsl:call-template>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<!-- merge last two items if they are maps and do this recursively -->
	<xsl:template name="merge" as="item()*">
		<xsl:param name="input" as="item()*"/>
		<xsl:iterate select="$input">
			<xsl:param name="merged" as="item()?" select="()"/>
			<xsl:on-completion>
				<xsl:sequence select="$merged"/>
			</xsl:on-completion>
			<xsl:choose>
				<xsl:when test="not(exists($merged))
				                or $merged instance of node()
				                or . instance of node()
				                or not($merged('wrapper-element') is .('wrapper-element'))">
					<xsl:sequence select="$merged"/>
					<xsl:next-iteration>
						<xsl:with-param name="merged" select="."/>
					</xsl:next-iteration>
				</xsl:when>
				<xsl:otherwise>
					<xsl:next-iteration>
						<xsl:with-param name="merged" as="item()">
							<xsl:call-template name="build">
								<xsl:with-param name="content" as="item()*">
									<xsl:call-template name="merge">
										<xsl:with-param name="input" select="($merged('content'),.('content'))"/>
									</xsl:call-template>
								</xsl:with-param>
								<xsl:with-param name="wrapper-elements" select="$merged('wrapper-element')"/>
								<xsl:with-param name="builder" select="true()"/>
							</xsl:call-template>
						</xsl:with-param>
					</xsl:next-iteration>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:iterate>
	</xsl:template>

	<xsl:template name="build" as="item()*">
		<!-- a sequence of nodes or 'builder' items -->
		<xsl:param name="content" as="item()*" required="yes"/>
		<!-- wrapper elements, from outer to inner -->
		<xsl:param name="wrapper-elements" as="element()*" select="()"/>
		<!-- when true and when $wrapper-elements is not empty, returns a map item containing info
		     to construct the output (using this template), otherwise returns a node sequence -->
		<xsl:param name="builder" as="xs:boolean" select="false()"/>
		<xsl:choose>
			<xsl:when test="$builder and $wrapper-elements">
				<xsl:map>
					<xsl:map-entry key="'content'">
						<xsl:call-template name="build">
							<xsl:with-param name="content" select="$content"/>
							<xsl:with-param name="wrapper-elements" select="$wrapper-elements[position()&gt;1]"/>
							<xsl:with-param name="builder" select="true()"/>
						</xsl:call-template>
					</xsl:map-entry>
					<xsl:map-entry key="'wrapper-element'" select="$wrapper-elements[1]"/>
				</xsl:map>
			</xsl:when>
			<xsl:when test="$wrapper-elements">
				<xsl:copy select="$wrapper-elements[1]">
					<!-- duplicate id attributes are removed in subsequent step -->
					<xsl:sequence select="@*"/>
					<xsl:call-template name="build">
						<xsl:with-param name="content" select="$content"/>
						<xsl:with-param name="wrapper-elements" select="$wrapper-elements[position()&gt;1]"/>
					</xsl:call-template>
				</xsl:copy>
			</xsl:when>
			<xsl:when test="$builder">
				<xsl:sequence select="$content"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:for-each select="$content">
					<xsl:choose>
						<xsl:when test=". instance of node()">
							<xsl:sequence select="."/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:call-template name="build">
								<xsl:with-param name="content" select=".('content')"/>
								<xsl:with-param name="wrapper-elements" select=".('wrapper-element')"/>
							</xsl:call-template>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:for-each>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- reconstruct original tree structure from flat sequence of nodes -->
	<xsl:template name="reconstruct-tree" as="item()*">
		<xsl:param name="nodes" as="node()*" required="yes"/>
		<xsl:param name="top" as="element()" required="yes"/>
		<xsl:param name="top-included" as="xs:boolean" required="yes"/>
		<xsl:param name="builder" as="xs:boolean" select="false()"/>
		<xsl:if test="$nodes">
			<xsl:apply-templates mode="reconstruct" select="$top">
				<xsl:with-param name="nodes" tunnel="yes" select="$nodes"/>
				<xsl:with-param name="top" tunnel="yes" select="$top"/>
				<xsl:with-param name="top-included" tunnel="yes" select="$top-included"/>
				<xsl:with-param name="builder" tunnel="yes" select="$builder"/>
			</xsl:apply-templates>
		</xsl:if>
	</xsl:template>

	<xsl:template mode="reconstruct" match="node()">
		<xsl:param name="nodes" tunnel="yes" as="node()*" required="yes"/>
		<xsl:param name="top" tunnel="yes" as="element()" required="yes"/>
		<xsl:param name="top-included" tunnel="yes" as="xs:boolean" required="yes"/>
		<xsl:param name="builder" tunnel="yes" as="xs:boolean" required="yes"/>
		<xsl:choose>
			<xsl:when test=". is $top and not($top-included)">
				<xsl:apply-templates mode="#current" select="node()"/>
			</xsl:when>
			<xsl:when test=". intersect $nodes">
				<xsl:sequence select="."/>
			</xsl:when>
			<xsl:when test="self::*[descendant::node() intersect $nodes]">
				<xsl:choose>
					<xsl:when test="$builder">
						<xsl:map>
							<xsl:map-entry key="'wrapper-element'" select="."/>
							<xsl:map-entry key="'content'">
								<xsl:apply-templates mode="#current" select="node()"/>
							</xsl:map-entry>
						</xsl:map>
					</xsl:when>
					<xsl:otherwise>
						<xsl:copy>
							<xsl:sequence select="@* except @id"/>
							<xsl:if test="not(descendant::node()
							                  intersect (descendant::node() intersect $nodes)[1]/preceding::node())">
								<xsl:sequence select="@id"/>
							</xsl:if>
							<xsl:apply-templates mode="#current" select="node()"/>
						</xsl:copy>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
		</xsl:choose>
	</xsl:template>

	<xsl:variable name="generated-sectioning-element" as="element()">
		<section/>
	</xsl:variable>

	<xsl:function name="f:section-boundaries" as="map(xs:string,node()?)">
		<xsl:param name="section" as="element(d:section)"/>
		<xsl:param name="start-not-before" as="node()"/>
		<xsl:variable name="parent-section" as="element()"
		              select="$section/parent::*"/> <!-- element(d:section|d:outline) -->
		<xsl:variable name="next-section" as="element()?"
		              select="$section/following-sibling::*[1]"/> <!-- element(d:section|d:outline)? -->
		<xsl:variable name="start-before" as="element()"
		              select="key('id',$section/(@owner,@heading)[1],$root)"/>
		<xsl:variable name="start-not-before" as="node()" select="f:propagate($start-not-before,false())"/>
		<xsl:variable name="start-opportunities" as="element()+" select="$start-before/ancestor-or-self::*"/>
		<xsl:variable name="start-opportunities" as="element()+"
		              select="if (not($section/preceding-sibling::*) and $parent-section/@heading)
		                      then ($start-opportunities
		                            intersect key('id',$parent-section/@heading,$root)/following::*)
		                      else $start-opportunities"/>
		<xsl:variable name="start-opportunities" as="element()+"
		              select="$start-opportunities
		                      intersect ($start-not-before/descendant-or-self::*,
		                                 $start-not-before/following::*)"/>
		<xsl:choose>
			<xsl:when test="not($next-section)">
				<xsl:sequence select="map {
				                       'start': $start-opportunities[1],
				                       'end': () }"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:variable name="end-before" as="element()"
				              select="key('id',$next-section/(@owner,@heading)[1],$root)"/>
				<xsl:variable name="end-after" as="element()"
				              select="(($section/descendant::*[@owner][not(following::*
				                                                           intersect $section/descendant::*)])[1]/@owner,
				                       ($section/descendant-or-self::*)[last()]/@heading)[1]
				                      /key('id',.,$root)"/>
				<xsl:variable name="end-opportunities" as="element()+"
				              select="$end-before/ancestor-or-self::*
				                      intersect $end-after/following::*"/>
				<xsl:sequence select="map {
				                       'start': $start-opportunities[1],
				                       'end': $end-opportunities[1] }"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:function>

	<xsl:function name="f:propagate" as="node()">
		<xsl:param name="split-point" as="node()"/>
		<xsl:param name="skip-whitespace" as="xs:boolean"/>
		<xsl:sequence select="if ($split-point/parent::*
		                          and not($split-point/preceding-sibling::*
		                                  |$split-point/preceding-sibling::text()[not($skip-whitespace)
		                                                                          or normalize-space(.)]))
		                      then f:propagate($split-point/parent::*,$skip-whitespace)
		                      else $split-point"/>
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

	<xsl:function name="f:except-descendants">
		<xsl:param name="nodes" as="node()*"/>
		<xsl:sequence select="$nodes except $nodes/descendant::node()"/>
	</xsl:function>

	<xsl:template mode="rename-headings" match="h1|h2|h3|h4|h5|h6|hgroup">
		<xsl:if test="not(exists(@id))">
			<xsl:message terminate="yes">coding error</xsl:message>
		</xsl:if>
		<xsl:variable name="section" as="element(d:section)?" select="key('heading',@id,$root-outline)[1]"/>
		<xsl:if test="not($section)">
			<xsl:message terminate="yes">coding error</xsl:message>
		</xsl:if>
		<xsl:variable name="outline-depth" as="xs:integer">
			<xsl:choose>
				<xsl:when test="$fix-heading-ranks='toc-depth'">
					<xsl:sequence select="((@id,$section/@owner)
					                       /key('absolute-href',concat($base-uri,'#',.),$input-toc)
					                       /count(ancestor::ol),
					                       1)[1]"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:sequence select="min((6,count($section/ancestor-or-self::d:section)))"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:choose>
			<xsl:when test="self::hgroup">
				<xsl:sequence select="."/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:element name="h{$outline-depth}">
					<xsl:sequence select="@*|node()"/>
				</xsl:element>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template mode="add-implied-headings" match="body" priority="1">
		<xsl:call-template name="pf:next-match-with-generated-ids">
			<xsl:with-param name="prefix" select="'aria_label_'"/>
			<xsl:with-param name="for-elements" select="(self::*|.//article|.//aside|.//nav|.//section)
			                                            [@aria-label]
			                                            [not(key('owner',@id,$root-outline)/@heading)]"/>
			<xsl:with-param name="in-use" select=".//@id"/>
		</xsl:call-template>
	</xsl:template>

	<xsl:template mode="add-implied-headings" match="body|article|aside|nav|section">
		<xsl:if test="not(exists(@id))">
			<xsl:message terminate="yes">coding error</xsl:message>
		</xsl:if>
		<xsl:variable name="section" as="element(d:section)?" select="key('owner',@id,$root-outline)"/>
		<xsl:if test="not($section)">
			<xsl:message terminate="yes">coding error</xsl:message>
		</xsl:if>
		<xsl:choose>
			<xsl:when test="$section/@heading">
				<xsl:next-match/>
			</xsl:when>
			<xsl:when test="$fix-untitled-sections='imply-heading-from-aria-label' and not(@aria-label)">
				<xsl:next-match/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy>
					<xsl:apply-templates mode="#current" select="@* except @aria-label"/>
					<xsl:variable name="first-content-node" as="node()?"
					              select="child::node()[not(self::text()[not(normalize-space(.))] or
					                                        @role='doc-pagebreak' or
					                                        @epub:type/tokenize(.,'\s+')='pagebreak')][1]"/>
					<xsl:variable name="rank" as="xs:integer">
						<xsl:choose>
							<xsl:when test="$fix-heading-ranks='outline-depth'">
								<xsl:sequence select="min((6,count($section/ancestor-or-self::d:section)))"/>
							</xsl:when>
							<xsl:when test="exists($input-toc)">
								<xsl:sequence select="((@id,$section/@owner)
								                       /key('absolute-href',concat($base-uri,'#',.),$input-toc)
								                       /count(ancestor::ol),
								                       1)[1]"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:sequence select="1"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:variable>
					<xsl:choose>
						<xsl:when test="@aria-label">
							<xsl:variable name="label-id" as="attribute(id)">
								<xsl:call-template name="pf:generate-id"/>
							</xsl:variable>
							<xsl:attribute name="aria-labelledby" select="$label-id"/>
							<xsl:apply-templates mode="#current" select="$first-content-node/preceding-sibling::node()"/>
							<xsl:element name="h{$rank}">
								<xsl:sequence select="$label-id"/>
								<xsl:value-of select="@aria-label"/>
							</xsl:element>
						</xsl:when>
						<xsl:otherwise>
							<xsl:apply-templates mode="#current" select="$first-content-node/preceding-sibling::node()"/>
							<xsl:element name="h{$rank}">
								<xsl:call-template name="get-untitled-section-title">
									<xsl:with-param name="sectioning-element" select="."/>
								</xsl:call-template>
							</xsl:element>
						</xsl:otherwise>
					</xsl:choose>
					<xsl:apply-templates mode="#current" select="$first-content-node|
					                                             $first-content-node/following-sibling::node()"/>
				</xsl:copy>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- ignore sectioning root elements other than body -->
	<xsl:template mode="rename-headings add-implied-headings" match="blockquote|details|fieldset|figure|td">
		<xsl:sequence select="."/>
	</xsl:template>

	<xsl:template mode="#default rename-headings add-implied-headings" match="@*|node()">
		<xsl:copy>
			<xsl:sequence select="@*"/>
			<xsl:apply-templates mode="#current"/>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>
