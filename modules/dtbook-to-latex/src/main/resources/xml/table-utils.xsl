<?xml version="1.0" encoding="utf-8"?>

<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  exclude-result-prefixes="dtb">
	
  <!-- Copy a sequence of rows and normalize row- and colspans, i.e.
       insert empty cells instead for row- and colspans -->
  <xsl:template name="dtb:insert-covered-table-cells" as="element()*">
    <xsl:param name="table_cells" as="element()*"/>
    <xsl:param name="covered_cells" as="element()*"/>
    <xsl:param name="current_row" as="element()*"/>
    <xsl:param name="row_count" as="xs:integer" select="0"/>
    <xsl:param name="clone_cells" as="xs:boolean" select="false()" tunnel="yes"/>
    <xsl:param name="insert_if_colspan" as="xs:boolean" select="true()" tunnel="yes"/>
    <xsl:param name="insert_if_rowspan" as="xs:boolean" select="true()" tunnel="yes"/>
    <xsl:variable name="cell_count" select="count($current_row)"/>
    <xsl:choose>
      <xsl:when test="$covered_cells[@row=($row_count+1) and @col=($cell_count+1)]">
	<xsl:call-template name="dtb:insert-covered-table-cells">
	  <xsl:with-param name="table_cells" select="$table_cells"/>
	  <xsl:with-param name="current_row" select="($current_row, $covered_cells[@row=($row_count+1) and @col=($cell_count+1)])"/>
	  <xsl:with-param name="row_count" select="$row_count"/>
	  <xsl:with-param name="covered_cells" select="$covered_cells[not(@row=($row_count+1) and @col=($cell_count+1))]"/>
	</xsl:call-template>
      </xsl:when>
      <xsl:when test="$table_cells[1][count(parent::*/preceding-sibling::dtb:tr)=$row_count]">
	<xsl:variable name="new_covered_cells" as="element()*">
	  <xsl:variable name="colspan" as="xs:integer" select="if ($table_cells[1]/@colspan) then $table_cells[1]/@colspan else 1"/>
	  <xsl:variable name="rowspan" as="xs:integer" select="if ($table_cells[1]/@rowspan) then $table_cells[1]/@rowspan else 1"/>
	  <xsl:if test="($insert_if_colspan and $colspan &gt; 1) or ($insert_if_rowspan and $rowspan &gt; 1)">
	    <xsl:sequence select="for $i in 1 to $rowspan return
				  for $j in 1 to $colspan return
				    if (not($i=1 and $j=1)) then
				      dtb:covered-table-cell($row_count + $i, $cell_count + $j, $table_cells[1], $clone_cells) 
				      else ()"/>
	  </xsl:if>
	</xsl:variable>
	<xsl:call-template name="dtb:insert-covered-table-cells">
	  <xsl:with-param name="table_cells" select="$table_cells[position() &gt; 1]"/>
	  <xsl:with-param name="current_row" select="($current_row, $table_cells[1])"/>
	  <xsl:with-param name="row_count" select="$row_count"/>
	  <xsl:with-param name="covered_cells" select="($covered_cells, $new_covered_cells)"/>
	</xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
	<xsl:if test="exists($current_row)">
	  <xsl:element name="dtb:tr">
	    <xsl:sequence select="$current_row"/>
	  </xsl:element>
	  <xsl:if test="exists($table_cells)">
	    <xsl:call-template name="dtb:insert-covered-table-cells">
	      <xsl:with-param name="table_cells" select="$table_cells"/>
	      <xsl:with-param name="current_row" select="()"/>
	      <xsl:with-param name="row_count" select="$row_count + 1"/>
	      <xsl:with-param name="covered_cells" select="$covered_cells"/>
	    </xsl:call-template>
	  </xsl:if>
	</xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
	
  <xsl:function name="dtb:covered-table-cell">
    <xsl:param name="row"/>
    <xsl:param name="col"/>
    <xsl:param name="original_cell"/>
    <xsl:param name="clone_cells"/>
    <xsl:element namespace="{namespace-uri($original_cell)}" name="{name($original_cell)}" >
      <xsl:attribute name="row" select="$row"/>
      <xsl:attribute name="col" select="$col"/>
      <xsl:attribute name="covered-table-cell" select="'yes'"/>
      <xsl:sequence select="if ($clone_cells) then $original_cell/node() else ()"/>
    </xsl:element>
  </xsl:function>

</xsl:stylesheet>
