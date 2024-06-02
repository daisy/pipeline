<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0"
		xmlns:dc="http://purl.org/dc/elements/1.1/"
		xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0"
		xmlns:xforms="http://www.w3.org/2002/xforms"
		xmlns:svg="urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0"
		xmlns:form="urn:oasis:names:tc:opendocument:xmlns:form:1.0"
		xmlns:dom="http://www.w3.org/2001/xml-events"
		xmlns:number="urn:oasis:names:tc:opendocument:xmlns:datastyle:1.0"
		xmlns:table="urn:oasis:names:tc:opendocument:xmlns:table:1.0"
		xmlns:script="urn:oasis:names:tc:opendocument:xmlns:script:1.0"
		xmlns:meta="urn:oasis:names:tc:opendocument:xmlns:meta:1.0"
		xmlns:draw="urn:oasis:names:tc:opendocument:xmlns:drawing:1.0"
		xmlns:math="http://www.w3.org/1998/Math/MathML"
		xmlns:dr3d="urn:oasis:names:tc:opendocument:xmlns:dr3d:1.0"
		xmlns:style="urn:oasis:names:tc:opendocument:xmlns:style:1.0"
		xmlns:xs="http://www.w3.org/2001/XMLSchema"
		xmlns:xlink="http://www.w3.org/1999/xlink"
		xmlns:chart="urn:oasis:names:tc:opendocument:xmlns:chart:1.0"
		xmlns:config="urn:oasis:names:tc:opendocument:xmlns:config:1.0"
		xmlns:fo="urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0"
		xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
		xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
		xmlns:d="http://www.daisy.org/ns/pipeline/data"
		xmlns:f="functions"
		exclude-result-prefixes="#all">
	
	<xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>
	<xsl:import href="http://www.daisy.org/pipeline/modules/image-utils/library.xsl"/>
	<xsl:include href="utilities.xsl"/>
	
	<!-- ========================== -->
	<!-- How to use this stylesheet -->
	<!-- ========================== -->
	<!--
	    This stylesheet provides only a basic conversion but is designed to be easily extended.
	    
	    At the core of the stylesheet are the `text:span`, `text:a`, `text:p`, `text:h` and
	    `text:list` templates, with which the respective basic ODT elements are created. These
	    templates have a number of tunnel parameters which can be reset anywhere up the call
	    stack. For example, the styling is controlled by the tunnel parameters `paragraph_style',
	    `text_style` and `list_style`. These are set automatically in 'implicit' templates (see
	    below).
	    
	    Most other templates are build upon these core templates. Heavy use is made of styles; by
	    default most DTBook elements get their own style in the ODT.
	    
	    XSLT modes are mainly used for indicating the current context in the output document.
	    Always knowing the context makes the output more predictable and makes it easier to avoid
	    creating invalid ODT. When no template matches a given 'input element/output context'
	    combination, the stylesheet tries to insert a 'FIXME' comment, and when failing to do that,
	    terminates the conversion.
	    
	    A second use of XSLT modes is for determining the rendering 'type' of a DTBook node.
	    Templates with the mode `is-block-element` should return a boolean value that indicates
	    whether the matched node is a block element or not. This information is used e.g. by the
	    `group-inline-nodes` template, which groups adjacent inline nodes in a `text:p`.
	    
	    Finally, the `text-style`, `paragraph-style` and `list-style` modes are responsible for the
	    styling of a DTBook node. Templates with these modes should return an optional 'style name'
	    string. The information is used to set the corresponding tunnel parameters `text_style`,
	    `paragraph_style` and `list_style`. Decoupling the content generation and the styling allows
	    for much more flexibility.
	    
	    In order to use this stylesheet, it is recommended that you include (not import) it in
	    another stylesheet and extend it with your own templates. With the exception of modes
	    `text-style`, `paragraph-style`, `list-style` and `is-block-element`, you should use
	    `xsl:next-match` (or `apply-imports`) as much as possible, and try to avoid completely
	    overriding templates. Most templates are configurable with tunnel parameters.
	    
	    A special word has to be said about template priorities. This stylesheet subdivides its
	    templates into a number of priority ranges, and any custom extension should do the same.
	    The normal priority range is [ -1 .. 1 ]. Everything above 1 is reserved for 'implicit'
	    templates that should be matched no matter what. These are typically responsible for setting
	    various tunnel parameters on each DTBook element encountered. For instance, `lang` is reset
	    whenever the language in the DTBook changes. The parameters `paragraph_style', `text_style`
	    and `list_style` are reset whenever a node matches a 'style' template. Everything below -1
	    is reserved for 'defaults', which are typically used to capture unmatched nodes and issue
	    warnings or errors.
	    
	    Using xsl:include is preferred over xsl:import because templates in an 'importing'
	    stylesheet always have higher priority than templates in the imported stylesheet. So if you
	    are using xml:import and overriding templates without `xsl:next-match`, keep in mind that
	    you are also overriding implicit templates. The downside of using xsl:include is that you
	    have to explicitely set a high enough priority on you custom templates.
	-->
	
	<!-- ======= -->
	<!-- OPTIONS -->
	<!-- ======= -->
	
	<xsl:param name="image_dpi" as="xs:string" select="'600'"/>
	<xsl:param name="page_numbers" as="xs:string" select="'false'"/>
	<xsl:param name="page_numbers_float" as="xs:string" select="'true'"/>
	
	<!-- ======== -->
	<!-- TEMPLATE -->
	<!-- ======== -->
	
	<xsl:template match="/">
		<xsl:apply-templates select="/*" mode="template"/>
	</xsl:template>
	
	<xsl:template match="@*|node()" mode="template">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" mode="template"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="@fo:language|@fo:country|
	                     @style:language-asian|@style:country-asian|
	                     @style:language-complex|@style:country-complex"
	              mode="template"/>
	
	<xsl:template match="/office:document-content/office:body/office:text" mode="template">
		<xsl:copy>
			<xsl:apply-templates mode="template"/>
			<xsl:apply-templates select="collection()[2]/*" mode="office:text"/>
		</xsl:copy>
	</xsl:template>
	
	<!-- =================== -->
	<!-- STRUCTURAL ELEMENTS -->
	<!-- =================== -->
	
	<xsl:template match="dtb:dtbook" mode="office:text">
		<xsl:apply-templates mode="#current"/>
	</xsl:template>
	
	<xsl:template match="dtb:book|dtb:frontmatter|dtb:bodymatter|dtb:rearmatter|
	                     dtb:level1|dtb:level2|dtb:level3|dtb:level4|dtb:level5|dtb:level6"
	              mode="office:text">
		<xsl:apply-templates mode="#current"/>
	</xsl:template>
	
	<!-- ======== -->
	<!-- HEADINGS -->
	<!-- ======== -->
	
	<xsl:template match="dtb:h1|dtb:h2|dtb:h3|dtb:h4|dtb:h5|dtb:h6" mode="paragraph-style">
		<xsl:sequence select="dtb:style-name(.)"/>
	</xsl:template>
	
	<xsl:template match="dtb:h1|dtb:h2|dtb:h3|dtb:h4|dtb:h5|dtb:h6" mode="office:text text:section" priority="1">
		<xsl:call-template name="insert-pagenum-after"/>
	</xsl:template>
	
	<xsl:template match="dtb:h1|dtb:h2|dtb:h3|dtb:h4|dtb:h5|dtb:h6" mode="office:text text:section text:list-item">
		<xsl:call-template name="text:h">
			<xsl:with-param name="text:outline-level" select="number(substring(local-name(.),2,1))"/>
		</xsl:call-template>
	</xsl:template>
	
	<!-- ========== -->
	<!-- PARAGRAPHS -->
	<!-- ========== -->
	
	<xsl:template match="dtb:p" mode="paragraph-style">
		<xsl:param name="paragraph_style" as="xs:string?" tunnel="yes"/>
		<xsl:sequence select="($paragraph_style, dtb:style-name(.))[1]"/>
	</xsl:template>
	
	<xsl:template match="dtb:p[.//dtb:pagenum]" priority="0.6"
	              mode="office:text office:annotation text:section text:list-item table:table-cell">
		<xsl:choose>
			<xsl:when test="$page_numbers='true' and $page_numbers_float='true'">
				<xsl:call-template name="insert-pagenum-after"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates select="$group-inline-nodes" mode="#current">
					<xsl:with-param name="select" select="*|text()"/>
				</xsl:apply-templates>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="dtb:p" mode="office:text office:annotation text:section text:list-item
	                                  table:table-cell text:note-body">
		<xsl:call-template name="text:p"/>
	</xsl:template>
	
	<!-- ===== -->
	<!-- LISTS -->
	<!-- ===== -->
	
	<xsl:template match="dtb:list" mode="list-style">
		<xsl:sequence select="style:name(concat('dtb:list_', (@type, 'ul')[1]))"/>
	</xsl:template>
	
	<xsl:template match="dtb:dl" mode="list-style">
		<xsl:sequence select="dtb:style-name(.)"/>
	</xsl:template>
	
	<xsl:template match="dtb:li|dtb:dd" mode="paragraph-style">
		<xsl:sequence select="dtb:style-name(.)"/>
	</xsl:template>
	
	<xsl:template match="dtb:dt" mode="text-style">
		<xsl:sequence select="dtb:style-name(.)"/>
	</xsl:template>
	
	<xsl:template match="dtb:list|dtb:dl" mode="office:text text:section" priority="1">
		<xsl:call-template name="insert-pagenum-after"/>
	</xsl:template>
	
	<xsl:template match="dtb:list" mode="office:text office:annotation text:section table:table-cell text:list-item">
		<xsl:call-template name="text:list"/>
	</xsl:template>
	
	<xsl:template match="dtb:li" mode="text:list">
		<xsl:element name="text:list-item">
			<xsl:apply-templates select="$group-inline-nodes" mode="text:list-item">
				<xsl:with-param name="select" select="*|text()"/>
			</xsl:apply-templates>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="dtb:lic" mode="text:p text:span">
		<xsl:apply-templates mode="#current"/>
	</xsl:template>
	
	<xsl:template match="dtb:dl" mode="office:text office:annotation text:section">
		<xsl:call-template name="text:list"/>
	</xsl:template>
	
	<xsl:template match="dtb:dt" mode="text:list">
		<xsl:if test="not(following-sibling::*[1]/self::dtb:dd)">
			<xsl:element name="text:list-item">
				<xsl:call-template name="text:p"/>
			</xsl:element>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="dtb:dd" mode="text:list">
		<xsl:variable name="dt" select="preceding-sibling::*[1]/self::dtb:dt"/>
		<xsl:variable name="colon">
			<xsl:text>: </xsl:text>
		</xsl:variable>
		<xsl:element name="text:list-item">
			<xsl:call-template name="text:p">
				<xsl:with-param name="apply-templates" select="if ($dt) then ($dt, $colon, *|text()) else (*|text())"/>
			</xsl:call-template>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="dtb:dt" mode="text:p">
		<xsl:call-template name="text:span"/>
	</xsl:template>
	
	<!-- ====== -->
	<!-- TABLES -->
	<!-- ====== -->
	
	<xsl:template match="dtb:td|dtb:th|dtb:table/dtb:caption" mode="paragraph-style">
		<xsl:sequence select="dtb:style-name(.)"/>
	</xsl:template>
	
	<xsl:template match="dtb:table[.//dtb:pagenum]" priority="0.6" mode="office:text text:section">
		<xsl:choose>
			<xsl:when test="$page_numbers='true' and $page_numbers_float='true'">
				<xsl:call-template name="insert-pagenum-after"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:next-match/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="dtb:table" mode="office:text text:section">
		<xsl:variable name="dtb:table" as="element()">
			<xsl:apply-templates select="." mode="insert-covered-table-cells"/>
		</xsl:variable>
		<xsl:apply-templates select="$dtb:table/dtb:caption" mode="#current"/>
		<xsl:variable name="max_width" select="max($dtb:table//dtb:tr/count(dtb:td|dtb:th))"/>
			<xsl:if test="$dtb:table//dtb:td[@class='phantom']
			              or (some $width in $dtb:table//dtb:tr/count(dtb:td|dtb:th)
			                  satisfies $width != $max_width)">
				<xsl:element name="text:p">
					<xsl:attribute name="text:style-name" select="'ERROR'"/>
					<xsl:call-template name="office:annotation">
						<xsl:with-param name="apply-templates">
							<dtb:span xml:lang="en">Not every row in this tables contains the same number of cells!</dtb:span>
						</xsl:with-param>
					</xsl:call-template>
					<xsl:text>FIX THE FOLLOWING TABLE!</xsl:text>
				</xsl:element>
			</xsl:if>
		<xsl:element name="table:table">
			<xsl:attribute name="table:name" select="concat('dtb:table#', count(preceding::dtb:table) + 1)"/>
			<xsl:element name="table:table-column">
				<xsl:attribute name="table:number-columns-repeated" select="$max_width"/>
			</xsl:element>
			<xsl:apply-templates mode="table:table" select="$dtb:table/dtb:thead"/>
			<xsl:apply-templates mode="table:table" select="$dtb:table/(dtb:tr|dtb:pagenum)"/>
			<xsl:apply-templates mode="table:table" select="$dtb:table/dtb:tbody"/>
			<xsl:apply-templates mode="table:table" select="$dtb:table/dtb:tfoot"/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="dtb:thead" mode="table:table">
		<xsl:element name="table:table-header-rows">
			<xsl:apply-templates mode="table:table-header-rows"/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="dtb:tbody|dtb:tfoot" mode="table:table">
		<xsl:apply-templates mode="#current"/>
	</xsl:template>
	
	<xsl:template match="dtb:pagenum" mode="table:table">
		<xsl:variable name="pagenum_row" as="element()">
			<xsl:element name="dtb:tr">
				<xsl:element name="dtb:td">
					<xsl:attribute name="class" select="'pagenum'"/>
					<xsl:sequence select="."/>
				</xsl:element>
			</xsl:element>
		</xsl:variable>
		<xsl:apply-templates select="$pagenum_row" mode="#current"/>
	</xsl:template>
	
	<xsl:template match="dtb:tr" mode="table:table table:table-header-rows">
		<xsl:element name="table:table-row">
			<xsl:apply-templates mode="table:table-row"/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="dtb:td|dtb:th" mode="table:table-row">
		<xsl:variable name="colspan" as="xs:integer" select="(@colspan,1)[1]"/>
		<xsl:variable name="rowspan" as="xs:integer" select="(@rowspan,1)[1]"/>
		<xsl:element name="table:table-cell">
			<xsl:attribute name="office:value-type" select="'string'"/>
			<xsl:if test="$colspan &gt; 1">
				<xsl:attribute name="table:number-columns-spanned" select="$colspan"/>
			</xsl:if>
			<xsl:if test="$rowspan &gt; 1">
				<xsl:attribute name="table:number-rows-spanned" select="$rowspan"/>
			</xsl:if>
			<xsl:apply-templates select="$group-inline-nodes" mode="table:table-cell">
				<xsl:with-param name="select" select="*|text()"/>
			</xsl:apply-templates>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="dtb:td[@class='covered']" mode="table:table-row">
		<xsl:element name="table:covered-table-cell"/>
	</xsl:template>
	
	<xsl:template match="dtb:table/dtb:caption" mode="office:text text:section">
		<xsl:apply-templates select="$group-inline-nodes" mode="#current">
			<xsl:with-param name="select" select="*|text()"/>
		</xsl:apply-templates>
	</xsl:template>
	
	<!-- ===== -->
	<!-- NOTES -->
	<!-- ===== -->
	
	<xsl:template match="dtb:note" mode="paragraph-style">
		<xsl:sequence select="style:name(concat('dtb:note_', (@class, 'footnote')[.=('footnote','endnote')][1]))"/>
	</xsl:template>
	
	<xsl:template match="dtb:annotation" mode="paragraph-style">
		<xsl:sequence select="dtb:style-name(.)"/>
	</xsl:template>
	
	<xsl:template match="dtb:noteref|dtb:annoref" mode="text:p text:h text:span">
		<xsl:variable name="id" select="translate(@idref,'#','')"/>
		<xsl:variable name="note" select="if (self::dtb:noteref) then dtb:find-note($id)
		                                  else dtb:find-annotation($id)"/>
		<xsl:if test="self::dtb:annoref">
			<xsl:apply-templates mode="#current"/>
		</xsl:if>
		<xsl:element name="text:note">
			<xsl:attribute name="text:note-class" select="($note/@class, 'footnote')[.=('footnote','endnote')][1]"/>
			<xsl:attribute name="text:id" select="$note/@id"/>
			<!--
			    LO takes care of updating this
			-->
			<xsl:element name="text:note-citation"></xsl:element>
			<xsl:element name="text:note-body">
				<xsl:apply-templates select="$note" mode="text:note-body">
					<xsl:with-param name="skip_notes" select="false()" tunnel="yes"/>
				</xsl:apply-templates>
			</xsl:element>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="dtb:note|dtb:annotation" mode="text:note-body">
		<xsl:apply-templates select="$group-inline-nodes" mode="#current">
			<xsl:with-param name="select" select="*|text()"/>
			<xsl:with-param name="skip_notes" select="true()" tunnel="yes"/>
		</xsl:apply-templates>
	</xsl:template>
	
	<!--
	    Render notes only if `skip_notes` is explicitely set too false. Detect notes
	    that are not referenced anywhere.
	-->
	<xsl:template match="dtb:note|dtb:annotation" priority="0.9"
	              mode="text:p text:h text:span office:text office:annotation text:section">
		<xsl:param name="skip_notes" as="xs:boolean" select="true()" tunnel="yes"/>
		<xsl:choose>
			<xsl:when test="not($skip_notes)">
				<xsl:next-match/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:variable name="id" select="string(@id)"/>
				<xsl:variable name="refs" select="if (self::dtb:note)
				                                  then collection()[2]//dtb:noteref[@idref=concat('#',$id)]
				                                  else collection()[2]//dtb:annoref[@idref=concat('#',$id)]"/>
				<xsl:if test="not(exists($refs))">
					<xsl:message terminate="yes">
						<xsl:text>ERROR! </xsl:text>
						<xsl:sequence select="name(.)"/>
						<xsl:text> with id #</xsl:text>
						<xsl:sequence select="$id"/>
						<xsl:text> is never referenced.</xsl:text>
					</xsl:message>
				</xsl:if>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:variable name="dtb:notes" as="element()*" select="collection()[2]//dtb:note"/>
	
	<xsl:function name="dtb:find-note" as="element()">
		<xsl:param name="id" as="xs:string"/>
		<xsl:variable name="note" select="$dtb:notes[@id=$id]"/>
		<xsl:if test="not(exists($note))">
			<xsl:message terminate="yes">
				<xsl:text>ERROR! dtb:note with id #</xsl:text>
				<xsl:sequence select="$id"/>
				<xsl:text> could not be found.</xsl:text>
			</xsl:message>
		</xsl:if>
		<xsl:sequence select="$note"/>
	</xsl:function>
	
	<xsl:variable name="dtb:annotations" as="element()*" select="collection()[2]//dtb:annotation"/>
	
	<xsl:function name="dtb:find-annotation" as="element()">
		<xsl:param name="id" as="xs:string"/>
		<xsl:variable name="annotation" select="$dtb:annotations[@id=$id]"/>
		<xsl:if test="not(exists($annotation))">
			<xsl:message terminate="yes">
				<xsl:text>ERROR! dtb:annotation with id #</xsl:text>
				<xsl:sequence select="$id"/>
				<xsl:text> could not be found.</xsl:text>
			</xsl:message>
		</xsl:if>
		<xsl:sequence select="$annotation"/>
	</xsl:function>
	
	<!-- ==================== -->
	<!-- OTHER BLOCK ELEMENTS -->
	<!-- ==================== -->
	
	<xsl:template match="dtb:blockquote|dtb:epigraph|dtb:poem|dtb:prodnote|
	                     dtb:doctitle|dtb:docauthor|dtb:byline|dtb:bridgehead|dtb:hd|dtb:covertitle"
	              mode="paragraph-style">
		<xsl:sequence select="dtb:style-name(.)"/>
	</xsl:template>
	
	<xsl:template match="dtb:sidebar" mode="office:text text:section">
		<xsl:param name="sidebar_announcement" as="node()*" tunnel="yes"/>
		<xsl:param name="sidebar_deannouncement" as="node()*" tunnel="yes"/>
		<xsl:call-template name="text:section">
			<xsl:with-param name="text:style-name" select="dtb:style-name(.)"/>
			<xsl:with-param name="number" select="count(preceding::dtb:sidebar) + count(ancestor::dtb:sidebar) + 1"/>
			<xsl:with-param name="apply-templates" select="($sidebar_announcement, *|text(), $sidebar_deannouncement)"/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template match="dtb:blockquote|dtb:epigraph|dtb:poem" mode="office:text office:annotation text:section text:list-item">
		<xsl:apply-templates mode="#current"/>
	</xsl:template>
	
	<xsl:template match="dtb:prodnote" mode="office:text office:annotation text:section">
		<xsl:param name="prodnote_announcement" as="node()*" tunnel="yes"/>
		<xsl:apply-templates select="$group-inline-nodes" mode="#current">
			<xsl:with-param name="select" select="($prodnote_announcement, *|text())"/>
		</xsl:apply-templates>
	</xsl:template>
	
	<xsl:template match="dtb:doctitle|dtb:docauthor|dtb:byline|dtb:bridgehead|dtb:hd|
	                     dtb:covertitle|dtb:author"
	              mode="office:text office:annotation text:section">
		<xsl:call-template name="text:p"/>
	</xsl:template>
	
	<xsl:template match="dtb:linegroup" mode="office:text office:annotation text:section">
		<xsl:apply-templates mode="#current"/>
	</xsl:template>
	
	<xsl:template match="dtb:line" mode="office:text office:annotation text:section">
		<xsl:call-template name="text:p"/>
	</xsl:template>
	
	<!-- ====== -->
	<!-- IMAGES -->
	<!-- ====== -->
	
	<xsl:template match="dtb:img|dtb:imggroup/dtb:caption" mode="paragraph-style">
		<xsl:sequence select="dtb:style-name(.)"/>
	</xsl:template>
	
	<xsl:template match="dtb:imggroup" mode="office:text office:annotation text:section table:table-cell text:list-item">
		<xsl:apply-templates select="dtb:caption" mode="#current"/>
		<xsl:apply-templates select="*[not(self::dtb:caption)]" mode="#current"/>
	</xsl:template>
	
	<xsl:template match="dtb:img" mode="office:text office:annotation text:section table:table-cell text:list-item">
		<xsl:variable name="src" select="resolve-uri(@src, base-uri(collection()[2]/*))"/>
		<xsl:choose>
			<xsl:when test="matches($src,'^https?://')
			                or (matches($src,'^file:') and pf:file-exists($src))">
				<xsl:variable name="image_dimensions" as="xs:integer*" select="pf:image-dimensions($src)"/>
				<xsl:call-template name="text:p">
					<xsl:with-param name="sequence">
						<xsl:element name="draw:frame">
							<xsl:attribute name="draw:name" select="concat('dtb:img#', count(preceding::dtb:img) + 1)"/>
							<xsl:attribute name="draw:style-name" select="dtb:style-name(.)"/>
							<xsl:attribute name="text:anchor-type" select="'as-char'"/>
							<xsl:attribute name="draw:z-index" select="'0'"/>
							<xsl:attribute name="svg:width" select="format-number($image_dimensions[1] div number($image_dpi), '0.0000in')"/>
							<xsl:attribute name="svg:height" select="format-number($image_dimensions[2] div number($image_dpi), '0.0000in')"/>
							<xsl:element name="draw:image">
								<xsl:attribute name="xlink:href" select="$src"/>
								<xsl:attribute name="xlink:type" select="'simple'"/>
								<xsl:attribute name="xlink:show" select="'embed'"/>
								<xsl:attribute name="xlink:actuate" select="'onLoad'"/>
							</xsl:element>
							<xsl:if test="@alt">
								<xsl:element name="svg:title">
									<xsl:sequence select="string(@alt)"/>
								</xsl:element>
							</xsl:if>
						</xsl:element>
					</xsl:with-param>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="text:p">
					<xsl:with-param name="sequence">
						<xsl:call-template name="office:annotation">
							<xsl:with-param name="apply-templates">
								<dtb:span xml:lang="en">
									<xsl:text>Missing image: </xsl:text>
									<dtb:strong><xsl:value-of select="@src"/></dtb:strong>
								</dtb:span>
							</xsl:with-param>
						</xsl:call-template>
					</xsl:with-param>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="dtb:imggroup/dtb:caption" mode="office:text office:annotation text:section table:table-cell text:list-item">
		<xsl:param name="caption_prefix" as="node()*" tunnel="yes"/>
		<xsl:param name="caption_suffix" as="node()*" tunnel="yes"/>
		<xsl:apply-templates select="$group-inline-nodes" mode="#current">
			<xsl:with-param name="select" select="($caption_prefix, *|text(), $caption_suffix)"/>
		</xsl:apply-templates>
	</xsl:template>
	
	<!-- ==== -->
	<!-- MATH -->
	<!-- ==== -->
	
	<xsl:template match="math:math" mode="text:p text:h text:span">
		<xsl:variable name="asciimath" select="string(math:semantics/math:annotation[@encoding='ASCIIMath'])"/>
		<xsl:variable name="count" as="xs:integer" select="count(preceding::math:math) + 1"/>
		<xsl:element name="draw:frame">
			<xsl:attribute name="draw:name" select="concat('math:math#', $count)"/>
			<xsl:attribute name="draw:style-name" select="dtb:style-name(.)"/>
			<xsl:attribute name="text:anchor-type" select="'as-char'"/>
			<xsl:attribute name="draw:z-index" select="'0'"/>
			<xsl:element name="draw:object">
				<xsl:sequence select="."/>
			</xsl:element>
			<xsl:if test="$asciimath!=''">
				<xsl:element name="svg:title">
					<xsl:sequence select="$asciimath"/>
				</xsl:element>
			</xsl:if>
		</xsl:element>
	</xsl:template>
	
	<!-- ============== -->
	<!-- PAGE NUMBERING -->
	<!-- ============== -->
	
	<xsl:template match="dtb:pagenum" mode="paragraph-style">
		<xsl:sequence select="dtb:style-name(.)"/>
	</xsl:template>
	
	<!--
	    FIXME: what if pagenum_done?
	-->
	<xsl:template match="dtb:pagenum" as="xs:boolean" mode="is-block-element">
		<xsl:sequence select="$page_numbers='true'"/>
	</xsl:template>
	
	<xsl:template match="dtb:pagenum" mode="office:text office:annotation text:section table:table-cell">
		<xsl:param name="pagenum_prefix" as="node()*" tunnel="yes"/>
		<xsl:call-template name="text:p">
			<xsl:with-param name="apply-templates" select="($pagenum_prefix, *|text())"/>
		</xsl:call-template>
	</xsl:template>
	
	<!--
	    Skip pagenum if page numbering is not enabled, or if it has been rendered already (moved)
	-->
	<xsl:template match="dtb:pagenum" priority="0.9"
	              mode="office:text text:section office:annotation
	                    table:table-cell table:table table:table-header-rows table:table-row
	                    text:list text:list-item text:note-body text:h text:p text:span">
		<xsl:param name="pagenum_done" as="xs:boolean" select="false()" tunnel="yes"/>
		<xsl:if test="$page_numbers='true' and not($pagenum_done)">
			<xsl:next-match/>
		</xsl:if>
	</xsl:template>
	
	<!--
	    Move all page number descendants to after this element.
	-->
	<xsl:template name="insert-pagenum-after">
		<xsl:next-match>
			<xsl:with-param name="pagenum_done" select="true()" tunnel="yes"/>
		</xsl:next-match>
		<xsl:apply-templates mode="#current" select=".//dtb:pagenum"/>
	</xsl:template>
	
	<!-- ====================== -->
	<!-- INLINE ELEMENTS & TEXT -->
	<!-- ====================== -->
	
	<xsl:template match="dtb:em|dtb:strong|dtb:sub|dtb:sup|dtb:cite|dtb:q|dtb:author|dtb:title|
	                     dtb:acronym|dtb:abbr|dtb:kbd|dtb:code|dtb:samp|dtb:linenum|dtb:a[@href]"
	              mode="text-style">
		<xsl:sequence select="dtb:style-name(.)"/>
	</xsl:template>
	
	<xsl:template match="dtb:span|dtb:sent|dtb:a" mode="text:p text:h text:span">
		<xsl:call-template name="text:span"/>
	</xsl:template>
	
	<xsl:template match="dtb:em|dtb:strong|dtb:sub|dtb:sup|dtb:cite|dtb:q|dtb:author|dtb:title|
	                     dtb:acronym|dtb:abbr|dtb:kbd|dtb:code|dtb:samp|dtb:linenum"
	              mode="text:p text:h text:span">
		<xsl:call-template name="text:span"/>
	</xsl:template>
	
	<xsl:template match="dtb:code|dtb:samp" mode="office:text office:annotation text:section text:list-item table:table-cell text:note-body">
		<xsl:call-template name="text:p"/>
	</xsl:template>
	
	<xsl:template match="dtb:a[@id]" mode="text:p text:h text:span" priority="1">
		<xsl:element name="text:bookmark">
			<xsl:attribute name="text:name" select="@id"/>
		</xsl:element>
		<xsl:next-match/>
	</xsl:template>
	
	<xsl:template match="dtb:a[@href]" mode="text:p text:h text:span" priority="0.9">
		<xsl:call-template name="text:a">
			<xsl:with-param name="xlink:href" select="@href"/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template match="dtb:br" mode="text:p text:h text:span text:a">
		<text:line-break/>
	</xsl:template>
	
	<xsl:template match="text()" mode="text:p text:h text:span text:a">
		<xsl:param name="text" as="xs:string" select="."/>
		<xsl:param name="space" as="xs:string" tunnel="yes"/>
		<xsl:choose>
			<xsl:when test="$space='preserve'">
				<xsl:analyze-string select="$text" regex="\S+">
					<xsl:matching-substring>
						<xsl:value-of select="."/>
					</xsl:matching-substring>
					<xsl:non-matching-substring>
						<xsl:analyze-string select="." regex="\n">
							<xsl:matching-substring>
								<xsl:element name="text:line-break"/>
							</xsl:matching-substring>
							<xsl:non-matching-substring>
								<xsl:element name="text:s">
									<xsl:attribute name="text:c" select="string-length(.)"/>
								</xsl:element>
							</xsl:non-matching-substring>
						</xsl:analyze-string>
					</xsl:non-matching-substring>
				</xsl:analyze-string>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$text"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="text()" mode="#all" priority="-1.4">
		<xsl:choose>
			<xsl:when test="normalize-space(.)=''">
				<xsl:value-of select="."/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="TERMINATE"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!-- ======== -->
	<!-- LANGUAGE -->
	<!-- ======== -->
	
	<xsl:variable name="document_lang" select="string(collection()[2]/dtb:dtbook/@xml:lang)"/>
	
	<xsl:template match="dtb:*"
	              mode="office:text
	                    text:h text:list text:list-item text:note-body text:p text:section text:span
	                    table:table table:table-cell table:table-header-rows table:table-row"
	              priority="1.1">
		<xsl:next-match>
			<xsl:with-param name="lang" select="f:lang(.)" tunnel="yes"/>
		</xsl:next-match>
	</xsl:template>
	
	<!-- ========== -->
	<!-- WHITESPACE -->
	<!-- ========== -->
	
	<xsl:template match="dtb:*"
	              mode="office:text
	                    text:h text:list text:list-item text:note-body text:p text:section text:span
	                    table:table table:table-cell table:table-header-rows table:table-row"
	              priority="1.2">
		<xsl:next-match>
			<xsl:with-param name="space" select="f:space(.)" tunnel="yes"/>
		</xsl:next-match>
	</xsl:template>
	
	<!-- ===== -->
	<!-- STYLE -->
	<!-- ===== -->
	
	<xsl:template name="inherit-text-style">
		<xsl:param name="text_style" as="xs:string?" tunnel="yes"/>
		<xsl:sequence select="$text_style"/>
	</xsl:template>
	
	<xsl:template name="inherit-paragraph-style">
		<xsl:param name="paragraph_style" as="xs:string?" tunnel="yes"/>
		<xsl:sequence select="$paragraph_style"/>
	</xsl:template>
	
	<xsl:template name="inherit-list-style">
		<xsl:param name="list_style" as="xs:string?" tunnel="yes"/>
		<xsl:sequence select="$list_style"/>
	</xsl:template>
	
	<xsl:template match="dtb:*"
	              mode="office:text
	                    text:h text:list text:list-item text:note-body text:p text:section text:span
	                    table:table table:table-cell table:table-header-rows table:table-row"
	              priority="1.3">
		<xsl:next-match>
			<xsl:with-param name="text_style" as="xs:string?" tunnel="yes">
				<xsl:apply-templates select="." mode="text-style"/>
			</xsl:with-param>
			<xsl:with-param name="paragraph_style" as="xs:string?" tunnel="yes">
				<xsl:apply-templates select="." mode="paragraph-style"/>
			</xsl:with-param>
			<xsl:with-param name="list_style" as="xs:string?" tunnel="yes">
				<xsl:apply-templates select="." mode="list-style"/>
			</xsl:with-param>
		</xsl:next-match>
	</xsl:template>
	
	<xsl:template match="dtb:*" as="xs:string?" mode="text-style" priority="-1.1">
		<xsl:call-template name="inherit-text-style"/>
	</xsl:template>
	
	<xsl:template match="dtb:*" as="xs:string?" mode="paragraph-style" priority="-1.1">
		<xsl:call-template name="inherit-paragraph-style"/>
	</xsl:template>
	
	<xsl:template match="dtb:*" as="xs:string?" mode="list-style" priority="-1.1">
		<xsl:call-template name="inherit-list-style"/>
	</xsl:template>
	
	<!-- =============== -->
	<!-- EVERYTHING ELSE -->
	<!-- =============== -->
	
	<xsl:template match="dtb:head" mode="#all"/>
	
	<xsl:template match="*" mode="office:text office:annotation text:section text:list-item table:table-cell text:note-body" priority="-1.3">
		<xsl:element name="text:p">
			<xsl:attribute name="text:style-name" select="'ERROR'"/>
			<xsl:call-template name="FIXME"/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="*" mode="text:p text:h text:span text:a" priority="-1.3">
		<xsl:element name="text:span">
			<xsl:attribute name="text:style-name" select="'ERROR'"/>
			<xsl:call-template name="FIXME"/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="*" mode="#all" priority="-1.4">
		<xsl:call-template name="TERMINATE"/>
	</xsl:template>
	
	<xsl:template match="@*" mode="#all" priority="-1.4"/>
	
	<!-- ========= -->
	<!-- UTILITIES -->
	<!-- ========= -->
	
	<xsl:variable name="group-inline-nodes">
		<group-inline-nodes/>
	</xsl:variable>
	
	<xsl:template match="group-inline-nodes" mode="#all">
		<xsl:param name="select" as="node()*"/>
		<xsl:for-each-group select="$select" group-adjacent="boolean(descendant-or-self::*[f:is-block-element(.)])">
			<xsl:choose>
				<xsl:when test="current-grouping-key()">
					<xsl:apply-templates select="current-group()" mode="#current"/>
				</xsl:when>
				<xsl:when test="normalize-space(string-join(current-group()/string(.), ''))=''"/>
				<xsl:otherwise>
					<xsl:call-template name="text:p">
						<xsl:with-param name="apply-templates" select="current-group()"/>
					</xsl:call-template>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each-group>
	</xsl:template>
	
	<xsl:function name="f:is-block-element" as="xs:boolean">
		<xsl:param name="node" as="node()"/>
		<xsl:apply-templates select="$node" mode="is-block-element"/>
	</xsl:function>
	
	<xsl:template match="dtb:*|math:*" as="xs:boolean" mode="is-block-element" priority="-1.1">
		<xsl:sequence select="false()"/>
	</xsl:template>
	
	<xsl:template match="dtb:p|dtb:list|dtb:dl|dtb:table|dtb:imggroup|dtb:blockquote"
	              as="xs:boolean"
	              mode="is-block-element">
		<xsl:sequence select="true()"/>
	</xsl:template>
	
	<!-- ====================================================== -->
	
	<xsl:template name="text:span">
		<xsl:param name="lang" as="xs:string" tunnel="yes"/>
		<xsl:param name="text_style" as="xs:string?" tunnel="yes"/>
		<xsl:param name="cur_text_lang" as="xs:string?" tunnel="yes"/>
		<xsl:param name="cur_text_style" as="xs:string?" tunnel="yes"/>
		<xsl:param name="apply-templates" as="node()*" select="*|text()"/>
		<xsl:param name="sequence" as="node()*"/>
		<xsl:choose>
			<xsl:when test="$lang!=($cur_text_lang,$document_lang)[1]">
				<xsl:element name="text:span">
					<xsl:attribute name="xml:lang" select="$lang"/>
					<xsl:choose>
						<xsl:when test="$text_style">
							<xsl:call-template name="text:span">
								<xsl:with-param name="cur_text_lang" select="$lang" tunnel="yes"/>
								<xsl:with-param name="apply-templates" select="$apply-templates"/>
								<xsl:with-param name="sequence" select="$sequence"/>
							</xsl:call-template>
						</xsl:when>
						<xsl:when test="exists($sequence)">
							<xsl:sequence select="$sequence"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:apply-templates select="$apply-templates" mode="text:span">
								<xsl:with-param name="cur_text_lang" select="$lang" tunnel="yes"/>
							</xsl:apply-templates>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:element>
			</xsl:when>
			<xsl:when test="$text_style">
				<xsl:element name="text:span">
					<xsl:attribute name="text:style-name" select="$text_style"/>
					<!--
					    In MS Word, when a named style appears inside of another named or automatic style,
					    the inner style will be applied and will 'overwrite' the outer style.
					-->
					<xsl:if test="$cur_text_style and $text_style
					              and $cur_text_style != $text_style
					              and not(style:is-automatic-style($text_style, 'text'))">
						<xsl:call-template name="office:annotation">
							<xsl:with-param name="apply-templates">
								<dtb:span xml:lang="en">
									Nested styles:
									<dtb:strong><xsl:value-of select="style:display-name($text_style)"/></dtb:strong>
									inside
									<dtb:strong><xsl:value-of select="style:display-name($cur_text_style)"/></dtb:strong>
								</dtb:span>
							</xsl:with-param>
						</xsl:call-template>
					</xsl:if>
					<xsl:choose>
						<xsl:when test="exists($sequence)">
							<xsl:sequence select="$sequence"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:apply-templates select="$apply-templates" mode="text:span">
								<xsl:with-param name="text_style" select="()" tunnel="yes"/>
								<xsl:with-param name="cur_text_style" select="$text_style" tunnel="yes"/>
							</xsl:apply-templates>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:element>
			</xsl:when>
			<xsl:when test="exists($sequence)">
				<xsl:sequence select="$sequence"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates select="$apply-templates" mode="#current"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template name="text:a">
		<xsl:param name="lang" as="xs:string" tunnel="yes"/>
		<xsl:param name="text_style" as="xs:string?" tunnel="yes"/>
		<xsl:param name="cur_text_lang" as="xs:string?" tunnel="yes"/>
		<xsl:param name="xlink:href" as="xs:string"/>
		<xsl:element name="text:a">
			<xsl:attribute name="xlink:href" select="$xlink:href"/>
			<xsl:attribute name="xlink:type" select="'simple'"/>
			<xsl:choose>
				<xsl:when test="$text_style or $lang!=($cur_text_lang,$document_lang)[1]">
					<xsl:call-template name="text:span"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:apply-templates mode="text:a"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:element>
	</xsl:template>
	
	<xsl:template name="text:p">
		<xsl:param name="lang" as="xs:string" tunnel="yes"/>
		<xsl:param name="text_style" as="xs:string?" tunnel="yes"/>
		<xsl:param name="paragraph_style" as="xs:string?" tunnel="yes"/>
		<xsl:param name="apply-templates" as="node()*" select="*|text()"/>
		<xsl:param name="sequence" as="node()*"/>
		<xsl:element name="text:p">
			<xsl:attribute name="text:style-name" select="($paragraph_style, 'Standard')[1]"/>
			<xsl:choose>
				<xsl:when test="$text_style or $lang!=$document_lang">
					<xsl:call-template name="text:span">
						<xsl:with-param name="apply-templates" select="$apply-templates"/>
						<xsl:with-param name="sequence" select="$sequence"/>
						<xsl:with-param name="cur_text_lang" select="()" tunnel="yes"/>
						<xsl:with-param name="cur_text_style" select="()" tunnel="yes"/>
					</xsl:call-template>
				</xsl:when>
				<xsl:when test="exists($sequence)">
					<xsl:sequence select="$sequence"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:apply-templates select="$apply-templates" mode="text:p"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:element>
	</xsl:template>
	
	<xsl:template name="text:h">
		<xsl:param name="lang" as="xs:string" tunnel="yes"/>
		<xsl:param name="text_style" as="xs:string?" tunnel="yes"/>
		<xsl:param name="paragraph_style" as="xs:string?" tunnel="yes"/>
		<xsl:param name="text:outline-level" as="xs:double"/>
		<xsl:param name="apply-templates" as="node()*" select="*|text()"/>
		<xsl:param name="sequence" as="node()*"/>
		<xsl:element name="text:h">
			<xsl:attribute name="text:outline-level" select="$text:outline-level"/>
			<xsl:attribute name="text:style-name" select="($paragraph_style, style:name(concat('Heading ', $text:outline-level)))[1]"/>
			<xsl:choose>
				<xsl:when test="$text_style or $lang!=$document_lang">
					<xsl:call-template name="text:span">
						<xsl:with-param name="apply-templates" select="$apply-templates"/>
						<xsl:with-param name="sequence" select="$sequence"/>
						<xsl:with-param name="cur_text_lang" select="()" tunnel="yes"/>
						<xsl:with-param name="cur_text_style" select="()" tunnel="yes"/>
					</xsl:call-template>
				</xsl:when>
				<xsl:when test="exists($sequence)">
					<xsl:sequence select="$sequence"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:apply-templates select="$apply-templates" mode="text:h"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:element>
	</xsl:template>
	
	<xsl:template name="text:section">
		<xsl:param name="text:style-name" as="xs:string"/>
		<xsl:param name="number" as="xs:double"/>
		<xsl:param name="apply-templates" as="node()*" select="*|text()"/>
		<xsl:param name="sequence" as="node()*"/>
		<xsl:element name="text:section">
			<xsl:attribute name="text:name" select="concat(style:display-name($text:style-name), '#', $number)"/>
			<xsl:attribute name="text:style-name" select="$text:style-name"/>
			<xsl:choose>
				<xsl:when test="exists($sequence)">
					<xsl:sequence select="$sequence"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:apply-templates select="$group-inline-nodes" mode="text:section">
						<xsl:with-param name="select" select="$apply-templates"/>
					</xsl:apply-templates>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:element>
	</xsl:template>
	
	<xsl:template name="text:list">
		<xsl:param name="list_style" as="xs:string?" tunnel="yes"/>
		<xsl:element name="text:list">
			<xsl:attribute name="text:style-name" select="($list_style, style:name('List1'))[1]"/>
			<xsl:apply-templates mode="text:list"/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template name="office:annotation">
		<xsl:param name="apply-templates" as="node()*" select="*|text()"/>
		<!--
		    avoid nested annotations
		-->
		<xsl:param name="inside_annotation" as="xs:boolean" tunnel="yes" select="false()"/>
		<xsl:if test="not($inside_annotation)">
			<xsl:element name="office:annotation">
				<xsl:element name="dc:creator">
					<xsl:text>dtbook-to-odt</xsl:text>
				</xsl:element>
				<xsl:element name="dc:date">
					<xsl:sequence select="current-dateTime()"/>
				</xsl:element>
				<xsl:apply-templates select="$group-inline-nodes" mode="office:annotation">
					<xsl:with-param name="select" select="$apply-templates"/>
					<xsl:with-param name="inside_annotation" tunnel="yes" select="true()"/>
					<!--
					    reset styling
					-->
					<xsl:with-param name="text_style" select="()" tunnel="yes"/>
					<xsl:with-param name="paragraph_style" select="()" tunnel="yes"/>
					<xsl:with-param name="list_style" select="()" tunnel="yes"/>
				</xsl:apply-templates>
			</xsl:element>
		</xsl:if>
	</xsl:template>
	
	<!-- ====================================================== -->
	
	<xsl:template name="FIXME">
		<xsl:param name="inside_annotation" as="xs:boolean" tunnel="yes" select="false()"/>
		<xsl:message>
			<xsl:text>[ODT] FIXME!! </xsl:text>
			<xsl:text>Some content cannot be rendered here. </xsl:text>
			<xsl:text>Trace: </xsl:text>
			<xsl:sequence select="f:node-trace(.)"/>
		</xsl:message>
		<xsl:choose>
			<xsl:when test="not($inside_annotation)">
				<xsl:call-template name="office:annotation">
					<xsl:with-param name="apply-templates">
						<dtb:p xml:lang="en">Some content cannot be rendered here.</dtb:p>
						<dtb:p xml:lang="en">
							<dtb:strong>Content:</dtb:strong>
						</dtb:p>
						<xsl:sequence select="."/>
						<dtb:p xml:lang="en">
							<dtb:strong>Trace:</dtb:strong>
							<xsl:text> </xsl:text>
							<xsl:value-of select="f:node-trace(.)"/>
						</dtb:p>
					</xsl:with-param>
				</xsl:call-template>
				<xsl:text>FIXME!!</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text> ? </xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template name="TERMINATE">
		<xsl:message terminate="yes">
			<xsl:text>Coding error: unexpected </xsl:text>
			<xsl:sequence select="f:node-trace(.)"/>
			<xsl:text> (mode: </xsl:text>
			<xsl:apply-templates select="$f:print-mode" mode="#current"/>
			<xsl:text>)</xsl:text>
		</xsl:message>
	</xsl:template>
	
	<xsl:variable name="f:print-mode"><f:print-mode/></xsl:variable>
	<xsl:template match="f:print-mode">#default</xsl:template>
	<xsl:template match="f:print-mode" mode="insert-covered-table-cells">insert-covered-table-cells</xsl:template>
	<xsl:template match="f:print-mode" mode="is-block-element">is-block-element</xsl:template>
	<xsl:template match="f:print-mode" mode="list-style">list-style</xsl:template>
	<xsl:template match="f:print-mode" mode="office:annotation">office:annotation</xsl:template>
	<xsl:template match="f:print-mode" mode="office:text">office:text</xsl:template>
	<xsl:template match="f:print-mode" mode="paragraph-style">paragraph-style</xsl:template>
	<xsl:template match="f:print-mode" mode="table:table">table:table</xsl:template>
	<xsl:template match="f:print-mode" mode="table:table-cell">table:table-cell</xsl:template>
	<xsl:template match="f:print-mode" mode="table:table-header-rows">table:table-header-rows</xsl:template>
	<xsl:template match="f:print-mode" mode="table:table-row">table:table-row</xsl:template>
	<xsl:template match="f:print-mode" mode="template">template</xsl:template>
	<xsl:template match="f:print-mode" mode="text-style">text-style</xsl:template>
	<xsl:template match="f:print-mode" mode="text:a">text:a</xsl:template>
	<xsl:template match="f:print-mode" mode="text:h">text:h</xsl:template>
	<xsl:template match="f:print-mode" mode="text:list">text:list</xsl:template>
	<xsl:template match="f:print-mode" mode="text:list-item">text:list-item</xsl:template>
	<xsl:template match="f:print-mode" mode="text:note-body">text:note-body</xsl:template>
	<xsl:template match="f:print-mode" mode="text:p">text:p</xsl:template>
	<xsl:template match="f:print-mode" mode="text:section">text:section</xsl:template>
	<xsl:template match="f:print-mode" mode="text:span">text:span</xsl:template>
	<xsl:template match="f:print-mode" mode="#all" priority="-1">?</xsl:template>
	
</xsl:stylesheet>
