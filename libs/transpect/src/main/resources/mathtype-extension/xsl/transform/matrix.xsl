<?xml version="1.0" encoding="UTF-8"?>
<!--
// =====================================================
// Matrices
// =====================================================

// matrix translation (left, center, right)
matrix/l    = "<(ns)mtable columnalign='left'>$+$n#$-$n</(ns)mtable>";
matrix      = "<(ns)mtable>$+$n#$-$n</(ns)mtable>";
matrix/r    = "<(ns)mtable columnalign='right'>$+$n#$-$n</(ns)mtable>";

// matrix line translation (left, center, right)
matrow/l   = "<(ns)mtr columnalign='left'>$+$n#$-$n</(ns)mtr>$n";
matrow     = "<(ns)mtr>$+$n#$-$n</(ns)mtr>$n";
matrow/r   = "<(ns)mtr columnalign='right'>$+$n#$-$n</(ns)mtr>$n";

// matrix element translation (except for last element) (left, center, right)
matelem/l   = "<(ns)mtd columnalign='left'>$+$n#$-$n</(ns)mtd>$n";
matelem     = "<(ns)mtd>$+$n#$-$n</(ns)mtd>$n";
matelem/r   = "<(ns)mtd columnalign='right'>$+$n#$-$n</(ns)mtd>$n";

// matrix element translation (last element only) (left, center, right)
matelem/last/l   = "<(ns)mtd columnalign='left'>$+$n#$-$n</(ns)mtd>";
matelem/last     = "<(ns)mtd>$+$n#$-$n</(ns)mtd>";
matelem/last/r   = "<(ns)mtd columnalign='right'>$+$n#$-$n</(ns)mtd>";
 -->


<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:mml="http://www.w3.org/1998/Math/MathML"
                xmlns="http://www.w3.org/1998/Math/MathML"
                exclude-result-prefixes="xs mml"
                version="2.0">

  <xsl:template match="matrix/h_just[text() = ('left', 'right', 'center')]">
    <xsl:attribute name="columnalign">
      <xsl:value-of select="./text()"/>
    </xsl:attribute>
  </xsl:template>

  <xsl:template name="map-parts-to-linestyle">
    <xsl:param name="parts" as="node()*"/>
    <xsl:variable name="raw-strings" as="xs:string*">
      <xsl:for-each select="$parts">
        <xsl:choose>
          <xsl:when test=". = '0'">none</xsl:when>
          <xsl:when test=". = '1'">solid</xsl:when>
          <xsl:when test=". = '2'">dashed</xsl:when>
          <!-- TODO: dotted? not allowed in MathML-->
          <xsl:otherwise>dashed</xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
    </xsl:variable>
    <xsl:value-of select="if ($raw-strings = ('solid', 'dashed')) then $raw-strings else ()"/>
  </xsl:template>

  <xsl:template match="matrix">
    <!-- TODO: frames -->
    <xsl:variable name="rows" select="number(rows)" as="xs:double"/>
    <xsl:variable name="cols" select="number(cols)" as="xs:double"/>
    <xsl:if test="$debug and not($rows * $cols eq count(slot | pile))">
      <xsl:message terminate="no">
        <xsl:text>Number of slots/piles doesnt match table definition! </xsl:text>
        <xsl:value-of select="count(slot | pile)"/>
        <xsl:text> slot|pile. </xsl:text>
        <xsl:value-of select="$rows"/>
        <xsl:text> rows. </xsl:text>
        <xsl:value-of select="$cols"/>
        <xsl:text> cols.</xsl:text>
      </xsl:message>
    </xsl:if>
    <xsl:variable name="frame" as="xs:string*">
      <xsl:variable name="distinct-borders" as="xs:string*">
        <xsl:variable name="map-names" as="xs:string">
          <xsl:call-template name="map-parts-to-linestyle">
            <xsl:with-param name="parts" select="
              row_parts[position() = (1, last())],
              col_parts[position() = (1, last())]"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:sequence select="distinct-values(tokenize($map-names, ' '))"/>
      </xsl:variable>
      <!-- altering frame per-side is not allowed on mtable -->
      <xsl:value-of select="
        if (
          $distinct-borders[1] = ('solid', 'dashed') and
          count($distinct-borders) eq 1)
        then $distinct-borders[1]
        else ()"/>
    </xsl:variable>
    <xsl:variable name="rowlines" as="xs:string*">
      <xsl:call-template name="map-parts-to-linestyle">
        <xsl:with-param name="parts" select="row_parts[not(position() = (1, last()))]"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="columnlines" as="xs:string*">
      <xsl:call-template name="map-parts-to-linestyle">
        <xsl:with-param name="parts" select="col_parts[not(position() = (1, last()))]"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="matrix_elements">
      <xsl:apply-templates select="slot | pile"/>
    </xsl:variable>
    <xsl:variable name="with-start-rows-att">
      <xsl:apply-templates mode="matrix-preprocess" select="$matrix_elements">
        <xsl:with-param name="cols" select="$cols"/>
      </xsl:apply-templates>
    </xsl:variable>
    <mtable>
      <xsl:if test="$frame">
        <xsl:attribute name="frame" select="$frame"/>
      </xsl:if>
      <xsl:if test="$rowlines">
        <xsl:attribute name="rowlines" select="$rowlines"/>
      </xsl:if>
      <xsl:if test="$columnlines">
        <xsl:attribute name="columnlines" select="$columnlines"/>
      </xsl:if>
      <xsl:apply-templates select="h_just"/>
      <xsl:for-each-group group-starting-with="mml:mtd[start-row]" select="$with-start-rows-att/mml:*">
        <mtr>
          <xsl:apply-templates mode="matrix-postprocess" select="current-group()"/>
        </mtr>
      </xsl:for-each-group>
    </mtable>
  </xsl:template>
  
  <xsl:template match="mml:mtd" mode="matrix-preprocess">
    <xsl:param as="xs:double" name="cols" required="yes"/>
    <mtd>
      <xsl:if test="$cols = 1 or position() mod $cols eq 1">
        <start-row xmlns=""/>
      </xsl:if>
      <xsl:copy-of select="@*, node()"/>
    </mtd>
  </xsl:template>

  <xsl:template match="mml:mtd" mode="matrix-postprocess">
    <mtd>
      <xsl:copy-of select="@*, node() except start-row"/>
    </mtd>
  </xsl:template>

  <xsl:template match="start-row" mode="matrix-postprocess"/>

  <xsl:template match="matrix/pile" priority="2">
    <mtd>
      <xsl:next-match/>
    </mtd>
  </xsl:template>
  
  <xsl:template match="matrix/slot" priority="2">
    <mtd>
      <xsl:next-match/>
    </mtd>
  </xsl:template>
</xsl:stylesheet>
