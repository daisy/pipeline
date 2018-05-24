<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
  xmlns:rel="http://schemas.openxmlformats.org/package/2006/relationships"
  
  xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"
  
  xmlns="http://www.w3.org/1999/xhtml" xmlns:xsw="http://coko.foundation/xsweet"
  exclude-result-prefixes="#all">
  
   
  <xsl:template match="w:tc">
    <td>
      
      <xsl:variable name="style">
        <!-- fallback to table styles if tcPr is missing; when it is present it will do the traversal itself. -->
        <xsl:apply-templates mode="render-css" select="(w:tcPr, ancestor::w:tbl/w:tblPr)[1]">
          <xsl:with-param tunnel="yes" name="cell" select="."/>
        </xsl:apply-templates>
      </xsl:variable>
      
      <!-- Adding the attribute only when there is a value. -->
      <xsl:if test="matches($style, '\S')">
        <xsl:attribute name="style" select="$style"/>
      </xsl:if>
      <!-- spanning logic: column and row spanning accomplished by static query of the WordML -->
      <xsl:for-each select="w:tcPr/w:gridSpan">
        <xsl:attribute name="colspan" select="@w:val"/>
      </xsl:for-each>
      
      <xsl:if test="w:tcPr/w:vMerge[@w:val='restart']">
        <xsl:variable name="row-count" as="xs:integer">
          <xsl:apply-templates select="." mode="iterate-rowspan-count"/>
        </xsl:variable>
        <xsl:attribute name="rowspan" select="$row-count"/>
      </xsl:if>
      <xsl:apply-templates select="*"/>
    </td>
  </xsl:template>
  
  <xsl:template mode="iterate-rowspan-count" match="w:tc" as="xs:integer">  
    <xsl:param name="so-far" select="0"/>
    <xsl:variable name="pos" select="xsw:column-position(.)"/>
    <!-- Going to add one by visiting a cell in the next row in the same position, set to merge -->
    <xsl:variable name="merged-row-cell" select="parent::w:tr/following-sibling::w:tr[1]/key('cell-by-position',$pos,.)
      [exists(w:tcPr/w:vMerge[not(@w:val='restart')]) ]"/>
    <xsl:apply-templates mode="iterate-rowspan-count" select="$merged-row-cell">
      <xsl:with-param name="so-far" select="$so-far + 1"/>
    </xsl:apply-templates>
    <!-- If not, we just report where we have gotten (including ourselves) -->
    <xsl:if test="empty($merged-row-cell)">
      <xsl:sequence select="$so-far + 1"/>
    </xsl:if>
  </xsl:template>

  <!-- speed up this stylesheet in some processors by matching only w:tbl[descendant::w:vMerge]//w:tc ? -->
  <xsl:key name="cell-by-position" match="w:tc" use="xsw:column-position(.)"/>

  <xsl:function name="xsw:column-position" as="xs:integer">
    <xsl:param name="cell" as="element(w:tc)"/>
    <xsl:sequence select="sum( $cell/(.|preceding-sibling::w:tc) / (w:tcPr/w:gridSpan/xs:integer(@w:val),1)[1] )"/>
  </xsl:function>
  
  <!-- Cells dropped because merged vertically into preceding rows -->
  <xsl:template match="w:tc[w:tcPr/w:vMerge[not(@w:val='restart')] ]"/>
  
  <xsl:template mode="render-css" match="w:tcPr | w:tblPr" as="xs:string?">
    <xsl:param tunnel="yes" required="yes" name="cell" as="element(w:tc)"/>
    
    <!-- xsw:cellStyles[xsw:prop] will be provided for each traversal --> 
    <xsl:variable name="styles-tree">
      <!-- Also get styles in here? -->
      <xsl:apply-templates mode="build-properties" select="self::w:tcPr/ancestor::w:tbl/w:tblPr"/>
      <xsl:apply-templates mode="build-properties" select="."/>
    </xsl:variable>
    
    <!-- Building element proxy for CSS, thereby resolving overloaded properties (last one wins)
         Since mode 'build-properties' delivers the properties from furthest to closest, this
         has the effect of implementing the style cascade from the Word. -->
    <xsl:variable name="styleProxy" as="element(xsw:style)">
      <!-- We will have xsw:style font-size='14pt' font-weight='bold' etc. -->
      <xsw:style>
        <xsl:for-each select="$styles-tree//xsw:prop">
          <xsl:attribute name="{@name}" select="."/>
        </xsl:for-each>
      </xsw:style>
    </xsl:variable>
    
    <!-- Now we deliver a string of ; delimited constructs from the $styleProxy attributes -->
    <!-- replace(name(),'^xsweet','-xsweet')   -->
    <xsl:value-of separator="; " select="$styleProxy/@*/concat(name(),': ',.) "/>
    
    <!-- Used to apply templates in current mode: no more! <xsl:apply-templates mode="#current"/> -->
    
  </xsl:template>
  
  <xsl:template mode="build-properties" as="element()+" match="w:tblPr | w:tcPr">
    
    <xsl:apply-templates mode="#current" select="w:tblStyle/key('styles-by-id',@w:val, $styles)"/>
    
    <xsw:cellStyles>
      <!-- table cells have vertical align 'top' by default -->
      <xsw:prop name="vertical-align">top</xsw:prop>
      <!--<xsl:for-each select="w:rStyle">
        <xsl:attribute name="calls" select="@w:val"/>
      </xsl:for-each>
      <xsl:for-each select="../w:basedOn">
        <xsl:attribute name="based-on" select="@w:val"/>
      </xsl:for-each>-->
      <!-- Applying templates produces a sequence of xsw:prop elements -->
      <xsl:apply-templates mode="#current"/>
    </xsw:cellStyles>
  </xsl:template>

  <xsl:template mode="build-properties" match="w:tcBorders">
    <xsl:variable name="here" select="."/>
    <xsl:for-each select="('top','bottom','left','right')">
      <xsl:variable name="position" select="."/>
      <!-- wouldn't ordinarily do this but here it's expedient -->
      <xsl:apply-templates mode="#current" select="$here/child::*[local-name(.) eq $position]">
        <xsl:with-param name="position" tunnel="yes" as="xs:string" select="$position"/>
      </xsl:apply-templates>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template mode="build-properties" match="w:tblBorders">
    <xsl:param tunnel="yes" required="yes" name="cell"/>
    <!--<w:tblBorders>
      <w:top w:val="single" w:sz="4" w:space="0" w:color="auto"/>
      <w:left w:val="single" w:sz="4" w:space="0" w:color="auto"/>
      <w:bottom w:val="single" w:sz="4" w:space="0" w:color="auto"/>
      <w:right w:val="single" w:sz="4" w:space="0" w:color="auto"/>
      <w:insideH w:val="single" w:sz="4" w:space="0" w:color="auto"/>
      <w:insideV w:val="single" w:sz="4" w:space="0" w:color="auto"/>
    </w:tblBorders>
    For each cell we have to visit each of top, bottom, left and right, except these vary by cell position
    -->
    <xsl:variable name="topmost"    select="empty($cell/../preceding-sibling::w:tr)"/>
    <xsl:variable name="bottommost" select="empty($cell/../following-sibling::w:tr)"/>
    <xsl:variable name="leftmost"   select="empty($cell/preceding-sibling::w:tc)"/>
    <xsl:variable name="rightmost"  select="empty($cell/following-sibling::w:tc)"/>
        
    <xsl:apply-templates mode="#current" select="if ($topmost)    then w:top    else w:insideH">
      <xsl:with-param name="position" tunnel="yes" as="xs:string">top</xsl:with-param>
    </xsl:apply-templates>
    <xsl:apply-templates mode="#current" select="if ($bottommost) then w:bottom else w:insideH">
      <xsl:with-param name="position" tunnel="yes" as="xs:string">bottom</xsl:with-param>
    </xsl:apply-templates>
    <xsl:apply-templates mode="#current" select="if ($leftmost)   then w:left   else w:insideV">
      <xsl:with-param name="position" tunnel="yes" as="xs:string">left</xsl:with-param>
    </xsl:apply-templates>
    <xsl:apply-templates mode="#current" select="if ($rightmost)  then w:right  else w:insideV">
      <xsl:with-param name="position" tunnel="yes" as="xs:string">right</xsl:with-param>
    </xsl:apply-templates>
  </xsl:template>
  
  <xsl:template mode="build-properties"  as="element(xsw:prop)*" match="w:tblBorders/* | w:tcBorders/*">
    <!-- we have four properties we can acquire from w:val="single" w:sz="4" w:space="0" w:color="auto" -->
    <xsl:apply-templates select="@*" mode="#current"/>
  </xsl:template>

  <xsl:template mode="build-properties" as="element(xsw:prop)?" match="w:tblBorders/*/@* | w:tcBorders/*/@*"/>

  <xsl:template mode="build-properties" as="element(xsw:prop)" priority="1"
    match="w:tblBorders/*/@w:val | w:tcBorders/*/@w:val">
      <xsl:param name="position" tunnel="yes" as="xs:string" required="yes"/>
      <xsl:variable name="ms-border-style" select="."/>
      <xsw:prop name="border-{$position}-style">
        <xsl:value-of select="$border-map/xsw:border[@ms-style=$ms-border-style]/../@css-style"/>
      </xsw:prop>
    </xsl:template>
    
  <xsl:template mode="build-properties" as="element(xsw:prop)?" priority="1"
    match="w:tblBorders/*/@w:sz | w:tcBorders/*/@w:sz">
    <xsl:param name="position" tunnel="yes" as="xs:string" required="yes"/>
    <!-- Table borders being line borders they convert to 1/8 pt
         However 'double' borders come out better scaled to 1/2. -->
    <xsl:if test="not(. = '0')">
      <xsl:variable name="scale-factor"
        select="if (../@w:val = $border-map[@css-style='double']/xsw:border/@ms-style) then 2 else 8"/>
      <xsw:prop name="border-{$position}-width">
        <xsl:value-of select="number(.) div $scale-factor"/>
        <xsl:text>pt</xsl:text>
      </xsw:prop>
    </xsl:if>
  </xsl:template>
  
    <xsl:template mode="build-properties" as="element(xsw:prop)?" priority="1"
      match="w:tblBorders/*/@w:space | w:tcBorders/*/@w:space">
      <xsl:param name="position" tunnel="yes" as="xs:string" required="yes"/>
      <!-- Represented directly in point values according to http://officeopenxml.com/WPtableBorders.php -->
      <xsl:if test="not(.='0')">
        <xsw:prop name="padding-{$position}">
        <xsl:value-of select="."/>
        <xsl:text>pt</xsl:text>
      </xsw:prop>
      </xsl:if>
    </xsl:template>
  
  <xsl:template mode="build-properties"  as="element(xsw:prop)*" priority="1"
    match="w:tblBorders/*/@w:color | w:tcBorders/*/@w:color">
    <xsl:param name="position" tunnel="yes" as="xs:string" required="yes"/>
    <xsl:if test="not(.=('000000','auto'))">
      <xsw:prop name="border-{$position}-color">
        <xsl:value-of select="replace(., '^\d', '#$0')"/>
      </xsw:prop>
    </xsl:if>
  </xsl:template>
  
  <xsl:template mode="build-properties"  as="element(xsw:prop)*" priority="1"
    match="w:vAlign">
    <!--  <w:vAlign w:val="top"/>
        <w:vAlign w:val="center"/>
        <w:vAlign w:val="bottom"/>
    http://officeopenxml.com/WPtableCellProperties-verticalAlignment.php
    
    These map to the following CSS attributes:
      vertical-align: top
      vertical-align: middle
      vertical-align: bottom-->
    <xsw:prop name="vertical-align">
      <xsl:value-of select="if (string(@w:val) eq 'center') then 'middle' else string(@w:val)"/>
    </xsw:prop>
  </xsl:template>
  
  

  <!--See http://officeopenxml.com/WPtableBorders.php-->
  <xsl:variable name="border-map" as="element()*">
    <xsw:border css-style="none">
      <xsw:border ms-style="none"><!-- no border--></xsw:border>
      <xsw:border ms-style="nil"><!-- no border--></xsw:border>
    </xsw:border>

    <xsw:border css-style="hidden"/>

    <xsw:border css-style="dotted">
      <xsw:border ms-style="dotted"><!-- a dotted line--></xsw:border>
      <xsw:border ms-style="dotDotDash"><!-- a line with a repeating dot - dot - dash sequence--></xsw:border>
    </xsw:border>

    <xsw:border css-style="dashed">
      <xsw:border ms-style="dotDash"><!-- a line with alternating dots and dashes--></xsw:border>
      <xsw:border ms-style="dashSmallGap"><!-- a dashed line with small gaps--></xsw:border>
      <xsw:border ms-style="dashed"><!-- a dashed line--></xsw:border>
      <xsw:border ms-style="dashDotStroked"><!-- a line with a series of alternating thin and thick strokes--></xsw:border>
    </xsw:border>

    <xsw:border css-style="solid">
      <xsw:border ms-style="wave"><!-- a wavy line--></xsw:border>
      <xsw:border ms-style="single"><!-- a single line--></xsw:border>
      <xsw:border ms-style="thick"><!-- a single line--></xsw:border>
    </xsw:border>

    <xsw:border css-style="double">
      <xsw:border ms-style="doubleWave"><!-- a double wavy line--></xsw:border>
      <xsw:border ms-style="double"><!-- a double line--></xsw:border>
      <xsw:border ms-style="triple"><!-- a triple line--></xsw:border>
      <xsw:border ms-style="thickThinLargeGap"><!-- a thick line contained within a thin line with a large-sized intermediate gap--></xsw:border>
      <xsw:border ms-style="thickThinMediumGap"><!-- a thick line contained within a thin line with a medium-sized intermediate gap--></xsw:border>
      <xsw:border ms-style="thickThinSmallGap"><!-- a thick line contained within a thin line with a small intermediate gap--></xsw:border>
      <xsw:border ms-style="thinThickLargeGap"><!-- a thin line contained within a thick line with a large-sized intermediate gap--></xsw:border>
      <xsw:border ms-style="thinThickMediumGap"><!-- a thick line contained within a thin line with a medium-sized intermediate gap--></xsw:border>
      <xsw:border ms-style="thinThickSmallGap"><!-- a thick line contained within a thin line with a small intermediate gap--></xsw:border>
      <xsw:border ms-style="thinThickThinLargeGap"><!-- a thin-thick-thin line with a large gap--></xsw:border>
      <xsw:border ms-style="thinThickThinMediumGap"><!-- a thin-thick-thin line with a medium gap--></xsw:border>
      <xsw:border ms-style="thinThickThinSmallGap"><!-- a thin-thick-thin line with a small gap--></xsw:border>
    </xsw:border>

    <xsw:border css-style="groove">
      <xsw:border ms-style="threeDEngrave"><!-- a three-staged gradient like, getting darker away from the paragraph--></xsw:border>
    </xsw:border>

    <xsw:border css-style="ridge">
      <xsw:border ms-style="threeDEmboss"><!-- a three-staged gradient line, getting darker towards the paragraph--></xsw:border>
    </xsw:border>

    <xsw:border css-style="inset">
      <xsw:border ms-style="inset"><!-- an inset set of lines--></xsw:border>
    </xsw:border>

    <xsw:border css-style="outset">
      <xsw:border ms-style="outset"><!-- an outset set of lines--></xsw:border>
    </xsw:border>

  </xsl:variable>
  
  
  
</xsl:stylesheet>