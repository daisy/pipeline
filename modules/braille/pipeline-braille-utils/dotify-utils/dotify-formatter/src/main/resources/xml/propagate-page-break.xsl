<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                exclude-result-prefixes="#all"
                version="2.0">
    
    <xsl:include href="http://www.daisy.org/pipeline/modules/braille/css-utils/library.xsl" />
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="@css:page-break-before|
                         @css:page-break-after|
                         @css:page-break-inside"/>
    
    <xsl:template match="css:box[@type='block']">
        <xsl:param name="avoid-break-after" as="xs:boolean" select="false()"/>
        <xsl:param name="avoid-break-inside" as="xs:boolean" select="false()"/>
        <xsl:variable name="avoid-break-inside" as="xs:boolean" select="$avoid-break-inside or @css:page-break-inside='avoid'"/>
        <xsl:variable name="page-break-after" as="xs:string*" select="if ($avoid-break-after) then ('avoid') else ()"/>
        <xsl:variable name="self" as="element()" select="."/>
        <!--
            A 'page-break-before' property with value 'left', 'right' or 'always' is propagated to
            the closest ancestor-or-self block box with a preceding sibling, or if there is no such
            element, to the outermost ancestor-or-self block box.
        -->
        <xsl:variable name="page-break-before" as="xs:string*"
                      select=".[preceding-sibling::* or not(parent::css:box)]
                              /(.|descendant::css:box[@type='block'])
                               [@css:page-break-before=('always','right','left')]
                               [not(preceding::* intersect $self/descendant::css:box[@type='block'])]
                               /@css:page-break-before/string()"/>
        <!--
            A 'page-break-after' property with value 'avoid' is propagated to the closest
            ancestor-or-self block box with a following sibling.
        -->
        <xsl:variable name="page-break-after" as="xs:string*"
                      select="($page-break-after,
                               .[following-sibling::*]
                               /(.|descendant::css:box[@type='block'])
                                [@css:page-break-after='avoid']
                                [not(following::* intersect $self/descendant::css:box[@type='block'])]
                                /@css:page-break-after/string())"/>
        <!--
            A 'page-break-before' property with value 'avoid' is converted into a 'page-break-after'
            property on the preceding sibling of the closest ancestor-or-self block box with a
            preceding sibling.
        -->
        <xsl:variable name="page-break-after" as="xs:string*"
                      select="($page-break-after,
                               following-sibling::*[1]
                               /(.|descendant::css:box[@type='block'])
                                [@css:page-break-before='avoid']
                                [not(preceding::* intersect $self/following-sibling::*[1]/descendant::css:box[@type='block'])]
                                /@css:page-break-before/string())"/>
        <!--
            A 'page-break-after' property with value 'left', 'right' or 'always' is converted into a
            'page-break-before' property on the immediately following block box,
        -->
        <xsl:variable name="page-break-before" as="xs:string*"
                      select="(preceding-sibling::*[1]
                               /(.|descendant::css:box[@type='block'])
                                [@css:page-break-after=('always','right','left')]
                                [not(following::* intersect $self/preceding-sibling::*[1]/descendant::css:box[@type='block'])]
                                /@css:page-break-after/string(),
                               $page-break-before)"/>
        <!--
            or if there is no such element, moved to the outermost ancestor-or-self block box.
        -->
        <xsl:variable name="page-break-after" as="xs:string*"
                      select="($page-break-after,
                               .[not(parent::css:box) and not(following-sibling::*)]
                               /(.|descendant::css:box[@type='block'])
                                 [@css:page-break-after=('always','right','left')]
                                 [not(following::* intersect $self/descendant::css:box[@type='block'])]
                                 /@css:page-break-after/string())"/>
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:if test="exists($page-break-before)">
                <xsl:attribute name="css:page-break-before" select="pxi:combine($page-break-before)"/>
            </xsl:if>
            <xsl:if test="exists($page-break-after)">
                <xsl:attribute name="css:page-break-after" select="pxi:combine($page-break-after)"/>
            </xsl:if>
            <xsl:if test="$avoid-break-inside and not(child::css:box[@type='block'])">
                <xsl:attribute name="css:page-break-inside" select="'avoid'"/>
            </xsl:if>
            <!--
                A 'page-break-inside' property with value 'avoid' on a box with child block boxes is
                propagated to all its children, and all children except the last get a
                'page-break-after' property with value 'avoid'.
            -->
            <xsl:choose>
                <xsl:when test="child::css:box[@type='block']">
                    <xsl:apply-templates select="child::css:box[position()&lt;last()]">
                        <xsl:with-param name="avoid-break-after" select="$avoid-break-inside"/>
                        <xsl:with-param name="avoid-break-inside" select="$avoid-break-inside"/>
                    </xsl:apply-templates>
                    <xsl:apply-templates select="child::css:box[last()]">
                        <xsl:with-param name="avoid-break-inside" select="$avoid-break-inside"/>
                    </xsl:apply-templates>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:copy>
    </xsl:template>
    
    <!--
        In case of conflicting values for a certain property, 'left' and 'right' win from 'always',
        'always' wins from 'avoid', and 'avoid' wins from 'auto'. When 'left' and 'right' are
        combined, the value specified on the latest element in the document wins.
    -->
    <xsl:function name="pxi:combine" as="xs:string">
        <xsl:param name="values" as="xs:string*"/>
        <xsl:choose>
          <xsl:when test="$values[.=('left','right')]">
              <xsl:sequence select="$values[.=('left','right')][last()]"/>
          </xsl:when>
          <xsl:when test="'always'=$values">
              <xsl:sequence select="'always'"/>
          </xsl:when>
          <xsl:when test="'avoid'=$values">
              <xsl:sequence select="'avoid'"/>
          </xsl:when>
          <xsl:otherwise>
              <xsl:sequence select="'auto'"/>
          </xsl:otherwise>
        </xsl:choose>
    </xsl:function>
    
</xsl:stylesheet>
