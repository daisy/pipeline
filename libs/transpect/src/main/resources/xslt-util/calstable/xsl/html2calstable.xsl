<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  xmlns:css="http://www.w3.org/1996/css"
  xmlns:tr="http://transpect.io"
  xmlns="http://docbook.org/ns/docbook"
  exclude-result-prefixes="xs xhtml tr"
  version="2.0">

  <!-- Based on a stylesheet by Roman Huditsch, roman.huditsch@bkf.at -->
  <!-- 1. preprocess -->
  <!-- 2. expand-cells -->
  <!-- 3. html2cals -->

	<xsl:template match="node() | @*" mode="html2cals preprocess expand-cells" priority="-0.5">
		<xsl:copy copy-namespaces="no">
			<xsl:apply-templates select="@* | node()" mode="#current"/>
		</xsl:copy>
	</xsl:template>  

  <xsl:template match="*[*:table]" mode="html2cals">
    <xsl:variable name="preprocessed" as="element(*)">
      <xsl:copy copy-namespaces="no">
        <xsl:apply-templates select="@*, node()"  mode="preprocess"/>
      </xsl:copy>
    </xsl:variable>
    <xsl:variable name="expanded-cells" as="element()">
      <xsl:copy copy-namespaces="no">
        <xsl:apply-templates select="$preprocessed/@*, $preprocessed/node()"  mode="expand-cells"/>
      </xsl:copy>
    </xsl:variable>
      <xsl:copy copy-namespaces="no">
         <xsl:apply-templates select="$expanded-cells/@*, $expanded-cells/node()"  mode="html2cals"/>
      </xsl:copy>
  </xsl:template>

  <xsl:template match="*:table" mode="html2cals">
    <xsl:element name="table" namespace="">
      <xsl:apply-templates select="(@xml:id, @id, @srcpath, @border, @width, @css:*, @rend, @role, @content-type), *[not(self::*:thead | self::*:tr | self::*:tbody | self::*:tfoot )]" mode="html2cals"/>
      <xsl:element name="tgroup">
<!--        <xsl:message select="concat('Max Columns: ', tr:max-columns(.))"/>-->
        <xsl:call-template name="generate-colspecs">
          <xsl:with-param name="max" select="tr:max-columns(.)" as="xs:double"/>
        </xsl:call-template>
        <xsl:apply-templates select="*:thead" mode="html2cals"/>
        <xsl:element name="tbody">
          <xsl:apply-templates select="*:tr | *:tbody/*:tr" mode="html2cals"/>
        </xsl:element>
        <xsl:apply-templates select="*:tfoot" mode="html2cals"/>
      </xsl:element>
    </xsl:element>
  </xsl:template>

	<xsl:template match="@border" mode="html2cals">
		<xsl:attribute name="frame">
			<xsl:value-of select="if(starts-with(., '0')) then('none') else('all')"/>
		</xsl:attribute>
	</xsl:template>

	<xsl:template match="*:table/@width | *:table/@css:width" mode="html2cals">
		<xsl:attribute name="pgwide">
			<xsl:value-of select="if(.='100%') then('0') else('1')"/>
		</xsl:attribute>
	</xsl:template>

  <xsl:template name="generate-colspecs">
    <xsl:param name="border" tunnel="yes"/>
    <xsl:param name="max" as="xs:double"/>
    <xsl:param name="count" select="1" as="xs:double"/>
    <xsl:choose>
      <xsl:when test="$count &gt; $max"/>
      <xsl:otherwise>
        <colspec colnum="{$count}" colname="{concat('col', $count)}" colsep="{if($border) then('1') else('0')}">
          <xsl:choose>
            <xsl:when test="*:colgroup/*:col[$count]/@*[local-name() = 'width'][1]">
              <xsl:apply-templates select="*:colgroup/*:col[$count]/@*[local-name() = 'width'][1]" mode="html2cals"/>
            </xsl:when>
            <xsl:when test="( ./(*/* | *)/*:td[$count] | ./(*/* | *)/*:th[$count])/@*[local-name() = 'width'][1]">
              <xsl:attribute name="colwidth">
                <xsl:value-of select="concat(tr:max-width(., $count), '*')"/>
              </xsl:attribute>
            </xsl:when>
          </xsl:choose>
        </colspec>
        <xsl:call-template name="generate-colspecs">
          <xsl:with-param name="max" select="$max"/>
          <xsl:with-param name="count" select="$count + 1"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

	<xsl:template match="*:col/@width | *:col/@css:width" mode="html2cals">
		<xsl:attribute name="colwidth">
			<xsl:value-of select="."/>
		</xsl:attribute>
	</xsl:template>

  <xsl:template match="*:thead | *:tfoot" mode="html2cals">
		<xsl:element name="{local-name()}">
			<xsl:copy-of select="@valign"/>
			<xsl:apply-templates mode="#current"/>
		</xsl:element>
	</xsl:template>

	<xsl:template match="*:tr" mode="html2cals">
		<xsl:param name="border" tunnel="yes"/>
<!--		<row rowsep="{if($border) then('1') else('0')}">-->
		<row rowsep="{if (
                      (every $b in *:td satisfies ($b/@border = ('0', 'none')))
                      or
                      (every $b in *:td satisfies ($b/@css:border = ('0', 'none')))
                       or
                      (every $b in *:td satisfies ($b/@css:border-bottom-style = ('0', 'none')))
                    ) 
                  then '0'
                  else '1'}">
			<xsl:copy-of select="@valign"/>
			<xsl:apply-templates mode="#current"/>
		</row>
	</xsl:template>

	<xsl:template match="*:td" mode="html2cals">
		<xsl:variable name="position" select="count(preceding-sibling::*) + 1"/>
		<entry colname="col{$position}">
			<xsl:if test="@colspan &gt; 1">
				<xsl:attribute name="namest">
					<xsl:value-of select="concat('col',$position)"/>
				</xsl:attribute>
				<xsl:attribute name="nameend">
					<xsl:value-of select="concat('col',$position + number(@colspan) - 1)"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="@rowspan &gt; 1">
				<xsl:attribute name="morerows">
					<xsl:value-of select="number(@rowspan) - 1"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:copy-of select="@align"/>
		  <xsl:attribute name="colsep" select="if (@border = 'none' or @css:border = ('none', 'transparent') or @css:border-right-style='none') then '0' else '1'"/>
			<xsl:apply-templates mode="#current"/>
		</entry>
	</xsl:template>

	<xsl:function name="tr:max-columns" as="xs:integer">
		<xsl:param name="context" as="element()"/>
		<xsl:choose>
			<xsl:when test="$context/*:colgroup[not(@span)]">
				<xsl:sequence select="count($context/*:colgroup/*:col)"/>
			</xsl:when>
			<xsl:when test="$context/*:colgroup[@span]">
				<xsl:sequence select="$context/*:colgroup/@span"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:sequence select="max(for $x in ($context | $context/* )/*:tr  return count($x/*:td))"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:function>

	<xsl:function name="tr:max-width" as="xs:double">
		<xsl:param name="context" as="element()"/>
		<xsl:param name="count" as="xs:double"/>
		<xsl:sequence select="max(for $x in ($context/* | $context/*/* )/(*:td[$count] | *:th[$count])/@*[local-name() = 'width'] return (if($x castable as xs:double) then($x) else(replace($x, '[a-z%]', ''))))"/>
	</xsl:function>

	<xsl:template match="*:colgroup | *:td[@id=('rowspan', 'colspan')]" mode="html2cals"/>
	

	<xsl:template match="*[@colspan]" mode="preprocess">
		<td>
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates mode="#current"/>
		</td>
		<xsl:for-each select="1 to (xs:integer(@colspan)-1)">
			<td id="colspan"/>
		</xsl:for-each>
	</xsl:template>

	<xsl:template match="*:td[preceding::*:td[count(preceding-sibling::*:td)=count(current()/preceding-sibling::*:td)+1 and current()/@rowspan]]" mode="expand-cells">
		<xsl:variable name="rowDiff" select="count(parent::*:tr/preceding-sibling::*)+1 - (count(preceding::*:td[count(preceding-sibling::*:td)=count(current()/preceding-sibling::*:td)+1 and current()/@rowspan][1]/parent::*:tr/preceding-sibling::*)+1)" as="xs:integer"/>
		<xsl:variable name="rowspan" select="(preceding::*:td[count(preceding-sibling::*:td)=count(current()/preceding-sibling::*:td)+1 and current()/@rowspan][1]/@rowspan,1)[1]" as="xs:integer*"/>
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates mode="#current"/>
		</xsl:copy>
		<xsl:if test="$rowDiff &lt; $rowspan">
			<xsl:for-each select="1 to ($rowspan - 1)">
				<td id="rowspan"/>
			</xsl:for-each>
		</xsl:if>
	</xsl:template>

</xsl:stylesheet>