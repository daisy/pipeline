<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:functx="http://www.functx.com"
  xmlns:calstable="http://docs.oasis-open.org/ns/oasis-exchange/table"
  xmlns="http://docbook.org/ns/docbook"
  exclude-result-prefixes="xs functx calstable"
  version="2.0">
  
  <!-- This stylesheet resolves tables that are nested within tables cells. It depends on 
       a table normalization which creates virtual cells for each cell with rowspan and colspan.

      Nesting means: *:/informaltables inside *:entry. 
      *:entrytbl are not supported yet, despite their occasional appearance in expressions/matching patterns.

      Nesting can only be resolved under this condition:
      – Nested informaltables fill up an entry entirely. No other paras or other informaltables next to them. 
  -->
  
  <xsl:function name="calstable:resolve-nested-tables">
    <xsl:param name="table" as="element()"/>
    <xsl:apply-templates select="$table" mode="calstable:resolve-nested-tables"/>
  </xsl:function>
  
  <!-- add virtual rows for the maximum row number of the embedded tables -->
  
  <xsl:template match="*:row[calstable:nested-table-exists(.)]" mode="calstable:resolve-nested-tables">
    <xsl:variable name="row" select="." as="element()"/>
    <xsl:variable name="nested-table" select="*//(*:tgroup[calstable:same-scope(., current())] | self::*:entrytbl)" as="element()+"/>
    <xsl:variable name="nested-table-rows" select="max(for $i in $nested-table return count($i/*/*:row))" as="xs:integer"/>
    <xsl:copy>
      <xsl:apply-templates select="@*, node()" mode="calstable:create-virtual-entries">
        <xsl:with-param name="row-index" select="1" tunnel="yes"/>
      </xsl:apply-templates>
    </xsl:copy>
    <!-- create virtual rows -->
    <xsl:for-each select="2 to ($nested-table-rows)">
      <xsl:variable name="row-index" select="." as="xs:integer"/>
      <row calstable:type="nested-table">
        <xsl:apply-templates select="$row/*:entry" mode="calstable:create-virtual-entries">
          <xsl:with-param name="row-index" select="$row-index" tunnel="yes"/>
        </xsl:apply-templates>
      </row>
    </xsl:for-each>
  </xsl:template>
  
  <!-- resolve the embedded table -->
  
  <xsl:template match="*:entry[calstable:nested-table-exists(.)]" mode="calstable:create-virtual-entries">
    <xsl:param name="row-index" tunnel="yes" as="xs:integer"/>
    <xsl:variable name="nested-table" select="(.//*:informaltable|.//*:table)" as="element()+"/>
    <xsl:variable name="content-before" select="$nested-table/preceding-sibling::*" as="element()*"/>
    <xsl:variable name="content-after" select="$nested-table/following-sibling::*" as="element()*"/>
    <xsl:variable name="last-row-index" select="count($nested-table//*:row)" as="xs:integer"/>
    <!-- iterate over entries -->
    <xsl:for-each select="$nested-table//*:row[$row-index]/*:entry">
      <xsl:copy>
        <xsl:attribute name="calstable:type" select="'nested-table'"/>
        <xsl:apply-templates select="@*" mode="calstable:resolve-nested-tables"/>
        <!-- wrap content into first cell -->
        <xsl:if test="exists($content-before) and $row-index eq 1 and position() eq 1">
          <xsl:apply-templates select="$content-before" mode="calstable:resolve-nested-tables"/>
        </xsl:if>
        <!-- apply templates, switch back in default mode -->
        <xsl:apply-templates mode="calstable:create-virtual-entries"/>
        <!-- append content after last table -->
        <xsl:if test="exists($content-after) and $row-index eq $last-row-index and position() eq 1">
          <xsl:apply-templates select="$content-after" mode="calstable:resolve-nested-tables"/>
        </xsl:if>
      </xsl:copy>
    </xsl:for-each>
  </xsl:template>
  
  <!-- identity for nodes in the first row, fill subsequent rows with empty cells -->
  
  <xsl:template match="*:entry" mode="calstable:create-virtual-entries">
    <xsl:param name="row-index" tunnel="yes" as="xs:integer"/>
    <xsl:variable name="id" select="concat('calstable_', generate-id())" as="xs:string"/>
    <xsl:choose>
      <xsl:when test="$row-index eq 1">
        <xsl:copy>
          <xsl:attribute name="xml:id" select="(@xml:id, $id)[1]"/>
          <xsl:apply-templates select="@* except @xml:id, node()" mode="calstable:resolve-nested-tables"/>
        </xsl:copy>
      </xsl:when>
      <xsl:otherwise>
        <entry calstable:type="nested-table-virtual-cell">
          <xsl:apply-templates select="@* except @calstable:*" mode="calstable:resolve-nested-tables"/>
        </entry>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- fill with empty cells when a nested table exists in this column -->
  
  <xsl:template match="*:entry[calstable:nested-table-exists-in-column(., position())]" mode="calstable:resolve-nested-tables">
    <xsl:variable name="index" select="calstable:index-of(parent::*:row/*:entry, .)" as="xs:integer"/>
    <xsl:variable name="max-cell-count"
                  select="max(
                              for $i in (parent::*:row/preceding-sibling::*:row/*:entry[$index]//*:row,
                                         parent::*:row/following-sibling::*:row/*:entry[$index]//*:row)
                              return count($i/*:entry)
                              )" as="xs:integer"/>
    <xsl:variable name="idref" 
                  select="(@linkend,
                           preceding::*:entry[1][@linkend]/@linkend)[1]" as="xs:string*"/>
    <xsl:copy>
      <xsl:choose>
        <xsl:when test="preceding::*:entry[@linkend eq $idref]">
          <xsl:attribute name="linkend" select="$idref"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="xml:id" select="(@xml:id, @linkend)[1]"/>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="@* except @linkend, node()" mode="#current"/>
    </xsl:copy>
    <xsl:for-each select="2 to $max-cell-count">
      <entry calstable:type="nested-table-in-column">
        <xsl:attribute name="linkend" select="$idref"/>
      </entry>
    </xsl:for-each>
  </xsl:template>
  
  <!-- additional colspec -->
  
  <xsl:template match="*:tgroup" mode="calstable:resolve-nested-tables">
    <xsl:variable name="tgroup" select="." as="element()"/>
    <xsl:copy>
      <xsl:apply-templates select="@*" mode="#current"/>
      <xsl:for-each select="*:colspec">
        <xsl:variable name="index" select="position()" as="xs:integer"/>
        <xsl:variable name="additional-cols"
                      select="distinct-values(
                                for $i in $tgroup/*/*:row/*:entry[$index] 
                                return max(
                                  for $j in $i//*:row 
                                  return count($j/*:entry)
                                )
                              )" as="xs:integer*"/>
        <xsl:if test="count($additional-cols) gt 1">
          <xsl:message select="'Unequal number of nested table columns below ', ."/>
        </xsl:if>
        <xsl:variable name="colwidth" select="@colwidth" as="attribute(colwidth)?"/>
        <xsl:copy>
          <xsl:apply-templates select="@*, node()" mode="#current"/>
        </xsl:copy>
        <xsl:for-each select="2 to max($additional-cols)">
          <colspec calstable:type="nested-table">
            <xsl:attribute name="colname" select="concat('col-', ($index + position()))"/>
            <xsl:if test="exists($colwidth)">
              <xsl:attribute name="colwidth" 
                             select="concat(
                                            xs:decimal(replace($colwidth, '[a-z]+$', '$1')) div ($additional-cols - 1),
                                            replace($colwidth, '^[0-9]+\.?[0-9]*', '$1')
                                            )"/>
            </xsl:if>
          </colspec>
        </xsl:for-each>
      </xsl:for-each>
      <xsl:apply-templates select="* except *:colspec" mode="#current"/>
    </xsl:copy>
  </xsl:template>
  
  <!-- identity template -->
  
  <xsl:template match="@*|*|processing-instruction()" mode="#all">
    <xsl:copy>
      <xsl:apply-templates select="@*, node()" mode="#current"/>
    </xsl:copy>
  </xsl:template>
  
  <!-- function to check whether a nested table exists in the current column -->
  
  <xsl:function name="calstable:nested-table-exists-in-column" as="xs:boolean">
    <xsl:param name="element" as="element(*)"/><!-- entry or entrytbl in any namespace -->
    <xsl:param name="index" as="xs:integer"/>
    <xsl:variable name="adjacent-cells-in-column" 
                  select="($element/parent::*:row/preceding-sibling::*:row/*[$index],
                          $element/parent::*:row/following-sibling::*:row/*[$index])" as="element()*"/><!-- entry or entrytbl in any namespace -->
    <xsl:variable name="nested-table-exists-in-column" 
                  select="some $i in $adjacent-cells-in-column satisfies calstable:nested-table-exists($i)" as="xs:boolean"/>
    <xsl:value-of select="$nested-table-exists-in-column"/>
  </xsl:function>
  
  <!-- check whether the element contains a nested table on the descendant axis -->
  
  <xsl:function name="calstable:nested-table-exists" as="xs:boolean">
    <xsl:param name="element" as="element()+"/><!-- entry or entrytbl in any namespace -->
    <xsl:sequence select="exists($element//*:tgroup[calstable:same-scope(., $element)] | $element/self::*:entrytbl)"/>
  </xsl:function>
  
  <xsl:function name="calstable:index-of" as="xs:integer*">
    <xsl:param name="nodes" as="node()*"/>
    <xsl:param name="nodeToFind" as="node()*"/>
    <xsl:sequence select="index-of($nodes/generate-id(), $nodeToFind/generate-id())"/>
  </xsl:function>

  <xsl:function name="calstable:same-scope" as="xs:boolean">
    <xsl:param name="node" as="node()" />
    <xsl:param name="ancestor-elt" as="element(*)*" />
    <xsl:sequence 
      select="not(
                $node/ancestor::*[
                  local-name() = $calstable:scope-establishing-elements]
                  [
                    some $a in ancestor::* 
                    satisfies (
                      some $b in $ancestor-elt 
                      satisfies ($a is $b))
                  ]
                )" />
  </xsl:function>
  
  <xsl:variable name="calstable:scope-establishing-elements" as="xs:string*"
    select="('tgroup', 'entrytbl')"/>
</xsl:stylesheet>
