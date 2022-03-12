<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
                exclude-result-prefixes="#all"
                version="2.0">
    
    <xsl:include href="library.xsl"/>
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="html:table[@css:display='table']|
                         dtb:table[@css:display='table']"
                  priority="0.6">
        <xsl:copy>
            <xsl:attribute name="css:table" select="'_'"/>
            <xsl:sequence select="@* except @css:display"/>
            <xsl:call-template name="walk-table-rows"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="html:thead| dtb:thead|
                         html:tbody| dtb:tbody|
                         html:tfoot| dtb:tfoot|
                         html:tr|    dtb:tr"
                  mode="table">
        <xsl:copy>
            <xsl:apply-templates select="@*" mode="#current"/>
            <xsl:call-template name="walk-table-rows"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template name="walk-table-rows">
        <xsl:param name="rows" as="node()*" select="node()"/>
        <xsl:param name="row" as="xs:integer" select="1"/>
        <xsl:param name="covered-cells" as="element()*"/>
        <xsl:choose>
            <xsl:when test="not(exists($rows))"/>
            <xsl:otherwise>
                <xsl:variable name="first-row" as="node()" select="$rows[1]"/>
                <xsl:variable name="next-rows" as="node()*" select="$rows[position() &gt; 1]"/>
                <xsl:choose>
                    <xsl:when test="$first-row/(self::html:tr or self::dtb:tr)">
                        <xsl:variable name="cells" as="node()*">
                            <xsl:call-template name="walk-table-cells">
                                <xsl:with-param name="cells" select="$first-row/node()"/>
                                <xsl:with-param name="row" select="$row"/>
                                <xsl:with-param name="covered-cells" select="$covered-cells[@row=$row]"/>
                            </xsl:call-template>
                        </xsl:variable>
                        <xsl:for-each select="$first-row">
                            <xsl:copy>
                                <xsl:apply-templates select="$first-row/@*" mode="table"/>
                                <xsl:sequence select="$cells[not(self::css:covered-table-cell)]"/>
                            </xsl:copy>
                        </xsl:for-each>
                        <xsl:call-template name="walk-table-rows">
                            <xsl:with-param name="rows" select="$next-rows"/>
                            <xsl:with-param name="row" select="$row + 1"/>
                            <!--
                                TODO: handle colspan/rowspan == 0
                            -->
                            <xsl:with-param name="covered-cells"
                                            select="($covered-cells[not(@row=$row)],
                                                     $cells[self::css:covered-table-cell and not(@row=$row)])"/>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:apply-templates select="$first-row" mode="table"/>
                        <xsl:call-template name="walk-table-rows">
                            <xsl:with-param name="rows" select="$next-rows"/>
                            <xsl:with-param name="row" select="$row"/>
                            <xsl:with-param name="covered-cells" select="$covered-cells"/>
                        </xsl:call-template>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template name="walk-table-cells">
        <xsl:param name="cells" as="node()*"/>
        <xsl:param name="covered-cells" as="element()*"/>
        <xsl:param name="row" as="xs:integer" required="yes"/>
        <xsl:param name="col" as="xs:integer" select="1"/>
        <xsl:choose>
            <xsl:when test="$covered-cells[@row=$row and @col=$col]">
                <xsl:call-template name="walk-table-cells">
                    <xsl:with-param name="cells" select="$cells"/>
                    <xsl:with-param name="covered-cells" select="$covered-cells[not(@row=$row and @col=$col)]"/>
                    <xsl:with-param name="row" select="$row"/>
                    <xsl:with-param name="col" select="$col + 1"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="not(exists($cells))"/>
            <xsl:otherwise>
                <xsl:variable name="first-cell" as="node()" select="$cells[1]"/>
                <xsl:variable name="next-cells" as="node()*" select="$cells[position() &gt; 1]"/>
                <xsl:choose>
                    <xsl:when test="$first-cell/(self::html:td or self::dtb:td or
                                                 self::html:th or self::dtb:th)">
                        <xsl:variable name="colspan" as="xs:integer" select="xs:integer(($first-cell/@colspan,'1')[1])"/>
                        <xsl:variable name="rowspan" as="xs:integer" select="xs:integer(($first-cell/@rowspan,'1')[1])"/>
                        <xsl:apply-templates select="$first-cell" mode="table">
                            <xsl:with-param name="row" select="$row"/>
                            <xsl:with-param name="col" select="$col"/>
                            <xsl:with-param name="rowspan" select="$rowspan"/>
                            <xsl:with-param name="colspan" select="$colspan"/>
                        </xsl:apply-templates>
                        <xsl:variable name="newly-covered-cells" as="element()*">
                            <xsl:if test="$colspan + $rowspan &gt; 2">
                                <xsl:sequence select="for $i in 1 to $rowspan return
                                                      for $j in 1 to $colspan return
                                                        if (not($i=1 and $j=1)) then
                                                          css:covered-table-cell($row + $i - 1, $col + $j - 1) else ()"/>
                            </xsl:if>
                        </xsl:variable>
                        <xsl:sequence select="$newly-covered-cells"/>
                        <xsl:call-template name="walk-table-cells">
                            <xsl:with-param name="cells" select="$next-cells"/>
                            <xsl:with-param name="covered-cells" select="($covered-cells,$newly-covered-cells)"/>
                            <xsl:with-param name="row" select="$row"/>
                            <xsl:with-param name="col" select="$col + 1"/>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:apply-templates select="$first-cell" mode="table"/>
                        <xsl:call-template name="walk-table-cells">
                            <xsl:with-param name="cells" select="$next-cells"/>
                            <xsl:with-param name="covered-cells" select="$covered-cells"/>
                            <xsl:with-param name="row" select="$row"/>
                            <xsl:with-param name="col" select="$col"/>
                        </xsl:call-template>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:function name="css:covered-table-cell">
        <xsl:param name="row"/>
        <xsl:param name="col"/>
        <css:covered-table-cell row="{$row}" col="{$col}"/>
    </xsl:function>
    
    <xsl:template match="@css:display[.='table']">
        <xsl:message>'display: table' only supported on HTML or DTBook "table" elements.</xsl:message>
    </xsl:template>
    
    <xsl:template match="html:td| dtb:td|
                         html:th| dtb:th"
                  mode="table">
        <xsl:param name="row" as="xs:integer" required="yes"/>
        <xsl:param name="col" as="xs:integer" required="yes"/>
        <xsl:param name="rowspan" as="xs:integer" required="yes"/>
        <xsl:param name="colspan" as="xs:integer" required="yes"/>
        <xsl:copy>
            <xsl:attribute name="css:table-cell" select="'_'"/>
            <xsl:choose>
                <xsl:when test="parent::html:tr/parent::html:thead or parent::dtb:tr/parent::dtb:thead">
                    <xsl:attribute name="css:table-header-group" select="1"/>
                </xsl:when>
                <xsl:when test="parent::html:tr/parent::html:tbody or parent::dtb:tr/parent::dtb:tbody">
                    <xsl:attribute name="css:table-row-group"
                                   select="1 + count(parent::*/parent::*
                                                     /(preceding-sibling::html:tbody|preceding-sibling::dtb:tbody))"/>
                </xsl:when>
                <xsl:when test="parent::html:tr/parent::html:tfoot or parent::dtb:tr/parent::dtb:tfoot">
                    <xsl:attribute name="css:table-footer-group" select="1"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="css:table-row-group" select="1"/>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:attribute name="css:table-row" select="$row"/>
            <xsl:attribute name="css:table-column" select="$col"/>
            <xsl:if test="$rowspan &gt; 1">
                <xsl:attribute name="css:table-row-span" select="$rowspan"/>
            </xsl:if>
            <xsl:if test="$colspan &gt; 1">
                <xsl:attribute name="css:table-column-span" select="$colspan"/>
            </xsl:if>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="html:caption|dtb:caption" mode="table">
        <xsl:copy>
            <xsl:attribute name="css:table-caption" select="'_'"/>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="dtb:pagenum" mode="table">
        <xsl:apply-templates select="."/>
    </xsl:template>
    
    <xsl:template match="css:before|css:after" mode="table">
        <xsl:apply-templates select="."/>
    </xsl:template>
    
    <xsl:template match="html:col|      dtb:col|
                         html:colgroup| dtb:colgroup"
                  mode="table">
        <xsl:message select="concat('&quot;',name(),'&quot; element not supported.')"/>
    </xsl:template>
    
    <xsl:template match="*" mode="table">
        <xsl:message select="concat('&quot;',name(),'&quot; element not supported within table.')"/>
    </xsl:template>
    
    <xsl:template match="text()" mode="table">
        <xsl:sequence select="."/>
    </xsl:template>
    
    <xsl:template match="@*" mode="table">
        <xsl:sequence select="."/>
    </xsl:template>
    
</xsl:stylesheet>
