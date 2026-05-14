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
		xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
		xmlns:f="functions"
		exclude-result-prefixes="#all">
	
	<xsl:function name="style:name">
		<xsl:param name="style-name" as="xs:string"/>
		<xsl:sequence select="replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(
		                      $style-name, '_', '_5f_'),
		                                   ' ', '_20_'),
		                                   '#', '_23_'),
		                                   '/', '_2f_'),
		                                   ':', '_3a_'),
		                                   '=', '_3d_'),
		                                   '>', '_3e_'),
		                                   '\[', '_5b_'),
		                                   '\]', '_5d_'),
		                                   '\|', '_7c_')"/>
	</xsl:function>
		
	<xsl:function name="style:display-name">
		<xsl:param name="style-name" as="xs:string"/>
		<xsl:sequence select="replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(
		                      $style-name, '_20_', ' '),
		                                   '_23_', '#'),
		                                   '_2f_', '/'),
		                                   '_3a_', ':'),
		                                   '_3d_', '='),
		                                   '_3e_', '>'),
		                                   '_5b_', '['),
		                                   '_5d_', ']'),
		                                   '_5f_', '_'),
		                                   '_7c_', '|')"/>
	</xsl:function>
	
	<xsl:function name="style:family">
		<xsl:param name="element" as="element()"/>
		<xsl:sequence select="if ($element[self::text:p or self::text:h]) then 'paragraph' else
		                      if ($element[self::text:span or self::text:a]) then 'text' else ''"/>
	</xsl:function>
	
	<!--
	    Assume collection()[1] is the template content.xml
	    TODO: what about automatic-styles in styles.xml?
	-->
	<xsl:function name="style:is-automatic-style" as="xs:boolean">
		<xsl:param name="style-name" as="xs:string"/>
		<xsl:param name="family" as="xs:string"/>
		<xsl:sequence select="boolean(collection()[1]//office:automatic-styles/style:style
		                      [@style:name=$style-name and @style:family=$family])"/>
	</xsl:function>
	
	<xsl:function name="fo:language">
		<xsl:param name="lang" as="xs:string"/>
		<xsl:sequence select="lower-case(replace($lang, '^([^-_]+).*', '$1'))"/>
	</xsl:function>
	
	<xsl:function name="fo:country">
		<xsl:param name="lang" as="xs:string"/>
		<xsl:variable name="country" select="upper-case(substring-after(translate($lang, '-', '_'), '_'))"/>
		<xsl:sequence select="if ($country!='') then $country else 'none'"/>
	</xsl:function>
	
	<xsl:function name="f:lang" as="xs:string">
		<xsl:param name="node" as="node()"/>
		<xsl:sequence select="string($node/ancestor-or-self::*[@xml:lang][1]/@xml:lang)"/>
	</xsl:function>
	
	<xsl:function name="f:space" as="xs:string">
		<xsl:param name="node" as="node()"/>
		<xsl:sequence select="string($node/ancestor-or-self::*[@xml:space][1]/@xml:space)"/>
	</xsl:function>
	
	<xsl:function name="dtb:style-name">
		<xsl:param name="element" as="element()"/>
		<xsl:sequence select="style:name(concat('dtb:', local-name($element)))"/>
	</xsl:function>
	
	<xsl:function name="f:node-trace" as="xs:string">
		<xsl:param name="node" as="node()?"/>
		<xsl:sequence select="if ($node)
		                      then concat(
		                             f:node-trace($node/parent::*), '/',
		                             if ($node/self::element())
		                               then concat(name($node), '[', count($node/preceding-sibling::*[name()=name($node)]) + 1, ']')
		                               else if ($node/self::attribute()) then concat('@', name($node))
		                               else if ($node/self::text()) then 'text()'
		                               else '?')
		                      else ''"/>
	</xsl:function>
	
	<xsl:template name="generate-automatic-style-name" as="xs:string">
		<xsl:param name="existing-style-names" as="xs:string*"/>
		<xsl:param name="prefix" as="xs:string"/>
		<xsl:param name="i" select="1"/>
		<xsl:variable name="style-name" select="concat($prefix, $i)"/>
		<xsl:choose>
			<xsl:when test="not($style-name=$existing-style-names)">
				<xsl:sequence select="$style-name"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="generate-automatic-style-name">
					<xsl:with-param name="existing-style-names" select="$existing-style-names"/>
					<xsl:with-param name="prefix" select="$prefix"/>
					<xsl:with-param name="i" select="$i + 1"/>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template name="language-properties">
		<xsl:param name="lang" as="xs:string"/>
		<xsl:variable name="fo:language" select="fo:language($lang)"/>
		<xsl:variable name="fo:country" select="fo:country($lang)"/>
		<xsl:attribute name="fo:language" select="$fo:language"/>
		<xsl:attribute name="fo:country" select="$fo:country"/><!--
		<xsl:attribute name="style:language-asian" select="$fo:language"/>
		<xsl:attribute name="style:country-asian" select="$fo:country"/>
		<xsl:attribute name="style:language-complex" select="$fo:language"/>
		<xsl:attribute name="style:country-complex" select="$fo:country"/>-->
	</xsl:template>
	
	<!-- ================ -->
	<!-- DTBOOK UTILITIES -->
	<!-- ================ -->
	
	<xsl:template match="dtb:table|dtb:thead|dtb:tbody|dtb:tfoot" mode="insert-covered-table-cells">
		<xsl:copy>
			<xsl:sequence select="@*"/>
			<xsl:for-each-group select="*" group-adjacent="boolean(self::dtb:tr)">
				<xsl:choose>
					<xsl:when test="current-grouping-key()">
						<xsl:call-template name="dtb:insert-covered-table-cells">
							<xsl:with-param name="rows_in" select="current-group()"/>
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						<xsl:for-each select="current-group()">
							<xsl:apply-templates select="." mode="#current"/>
						</xsl:for-each>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each-group>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="dtb:pagenum|dtb:caption" mode="insert-covered-table-cells">
		<xsl:sequence select="."/>
	</xsl:template>
	
	<!--
	    * add <dtb:td class="covered"/>
	      => for every @colspan or @rowspan
	      => can be used in subsequent step to create <table:covered-table-cell/>
	    * add <dtb:td class="phantom"/>
	      => for every @rowspan that makes a cell span across the bottom of the table
	      => are not added to fill up rows equally (both MS Word and LibreOffice will
	         handle the case where rows don't contain the same number of cells correctly)
	-->
	<xsl:template name="dtb:insert-covered-table-cells" as="element()*">
		<xsl:param name="rows_in" as="element()*"/>
		<xsl:param name="cells_in" as="element()*"/>
		<xsl:param name="cells_out" as="element()*"/>
		<xsl:param name="cells_covered" as="element()*"/>
		<xsl:param name="row" as="xs:integer" select="0"/>
		<xsl:param name="col" as="xs:integer" select="1"/>
		<xsl:choose>
			<xsl:when test="$cells_covered[@row=$row and @col=$col]">
				<xsl:call-template name="dtb:insert-covered-table-cells">
					<xsl:with-param name="rows_in" select="$rows_in"/>
					<xsl:with-param name="cells_in" select="$cells_in"/>
					<xsl:with-param name="cells_out" select="($cells_out, $cells_covered[@row=$row and @col=$col])"/>
					<xsl:with-param name="cells_covered" select="$cells_covered[not(@row=$row and @col=$col)]"/>
					<xsl:with-param name="row" select="$row"/>
					<xsl:with-param name="col" select="$col + 1"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="$cells_in[1]">
				<xsl:variable name="new_cells_covered" as="element()*">
					<xsl:variable name="colspan" as="xs:integer" select="$cells_in[1]/((@colspan,1)[1])"/>
					<xsl:variable name="rowspan" as="xs:integer" select="$cells_in[1]/((@rowspan,1)[1])"/>
					<xsl:if test="$colspan + $rowspan &gt; 2">
						<xsl:sequence select="for $i in 1 to $rowspan return
						                      for $j in 1 to $colspan return
						                        if (not($i=1 and $j=1)) then dtb:covered-table-cell($row + $i - 1, $col + $j - 1) else ()"/>
					</xsl:if>
				</xsl:variable>
				<xsl:call-template name="dtb:insert-covered-table-cells">
					<xsl:with-param name="rows_in" select="$rows_in"/>
					<xsl:with-param name="cells_in" select="$cells_in[position() &gt; 1]"/>
					<xsl:with-param name="cells_out" select="($cells_out, $cells_in[1])"/>
					<xsl:with-param name="cells_covered" select="($cells_covered, $new_cells_covered)"/>
					<xsl:with-param name="row" select="$row"/>
					<xsl:with-param name="col" select="$col + 1"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="$cells_covered[@row=$row and @col &gt; $col]">
				<xsl:variable name="phantom_cell" as="element()">
					<dtb:td class="phantom"/>
				</xsl:variable>
				<xsl:call-template name="dtb:insert-covered-table-cells">
					<xsl:with-param name="rows_in" select="$rows_in"/>
					<xsl:with-param name="cells_out" select="($cells_out, $phantom_cell)"/>
					<xsl:with-param name="cells_covered" select="$cells_covered"/>
					<xsl:with-param name="row" select="$row"/>
					<xsl:with-param name="col" select="$col + 1"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:if test="$row &gt; 0">
					<xsl:element name="dtb:tr">
						<xsl:sequence select="$cells_out"/>
					</xsl:element>
				</xsl:if>
				<xsl:if test="exists($rows_in) or exists($cells_covered[@row &gt; $row])">
					<xsl:call-template name="dtb:insert-covered-table-cells">
						<xsl:with-param name="rows_in" select="$rows_in[position() &gt; 1]"/>
						<xsl:with-param name="cells_in" select="$rows_in[1]/(dtb:td|dtb:th)"/>
						<xsl:with-param name="cells_covered" select="$cells_covered"/>
						<xsl:with-param name="row" select="$row + 1"/>
					</xsl:call-template>
				</xsl:if>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:function name="dtb:covered-table-cell">
		<xsl:param name="row"/>
		<xsl:param name="col"/>
		<dtb:td class="covered" row="{$row}" col="{$col}"/>
	</xsl:function>
</xsl:stylesheet>
