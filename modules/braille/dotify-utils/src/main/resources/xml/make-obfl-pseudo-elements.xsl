<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                exclude-result-prefixes="#all"
                version="2.0">
    
    <xsl:include href="http://www.daisy.org/pipeline/modules/braille/css-utils/library.xsl"/>
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*[@css:_obfl-on-toc-start|
                           @css:_obfl-on-collection-start|
                           @css:_obfl-on-volume-start|
                           @css:_obfl-on-volume-end|
                           @css:_obfl-on-collection-end|
                           @css:_obfl-on-toc-end]">
        <xsl:copy>
            <xsl:sequence select="@* except (@css:_obfl-on-toc-start|
                                             @css:_obfl-on-collection-start|
                                             @css:_obfl-on-volume-start|
                                             @css:_obfl-on-volume-end|
                                             @css:_obfl-on-collection-end|
                                             @css:_obfl-on-toc-end)"/>
            <xsl:if test="@css:_obfl-on-toc-start">
                <css:_obfl-on-toc-start style="{@css:_obfl-on-toc-start}"/>
            </xsl:if>
            <xsl:if test="@css:_obfl-on-collection-start">
                <css:_obfl-on-collection-start style="{@css:_obfl-on-collection-start}"/>
            </xsl:if>
            <xsl:if test="@css:_obfl-on-volume-start">
                <css:_obfl-on-volume-start style="{@css:_obfl-on-volume-start}"/>
            </xsl:if>
            <xsl:if test="@css:_obfl-on-volume-end">
                <css:_obfl-on-volume-end style="{@css:_obfl-on-volume-end}"/>
            </xsl:if>
            <xsl:if test="@css:_obfl-on-collection-end">
                <css:_obfl-on-collection-end style="{@css:_obfl-on-collection-end}"/>
            </xsl:if>
            <xsl:if test="@css:_obfl-on-toc-end">
                <css:_obfl-on-toc-end style="{@css:_obfl-on-toc-end}"/>
            </xsl:if>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*[@css:_obfl-on-resumed[not(.='_')]]">
        <xsl:copy>
            <xsl:sequence select="@* except @css:_obfl-on-resumed"/>
            <xsl:apply-templates/>
        </xsl:copy>
        <!--
            Set an attribute in addition to the element because the element are renamed later in the process.
        -->
        <css:_obfl-on-resumed css:_obfl-on-resumed="_" style="{@css:_obfl-on-resumed}">
            <!--
                Copy attributes so that attr(...) can be evaluated correctly
            -->
            <xsl:sequence select="@* except (@style|@css:*)"/>
        </css:_obfl-on-resumed>
    </xsl:template>
    
    <xsl:template match="*[@css:*[matches(local-name(),'^_obfl-alternate-scenario(-[1-9][0-9]*)?$')]]">
        <xsl:if test="@css:flow[not(.='normal')]">
            <xsl:message terminate="yes">Elements with a :-obfl-alternate-scenario pseudo-class must participate in the normal flow.</xsl:message>
        </xsl:if>
        <!--
            The reason we use attributes to tag the scenarios, and not elements, is because elements
            are renamed later in the process. We ensure the tree structure is not changed by forcing
            the "obfl-scenarios" and "obfl-scenario" elements to be blocks (the latter happens later
            in pxi:css-to-obfl).
        -->
        <xsl:variable name="this" as="element()" select="."/>
        <css:_ css:_obfl-scenarios="_" css:display="block">
            <xsl:variable name="scenario-attributes" as="attribute()*"
                          select="@css:obfl-alternate-scenario|
                                  @css:*[matches(local-name(),'^_obfl-alternate-scenario-[1-9][0-9]*$')]"/>
            <xsl:copy>
                <xsl:attribute name="css:_obfl-scenario" select="'_'"/>
                <xsl:sequence select="@* except $scenario-attributes"/>
                <xsl:if test="not(@name)">
                    <xsl:attribute name="name" select="name(.)"/>
                </xsl:if>
                <xsl:apply-templates/>
            </xsl:copy>
            <xsl:variable name="scenarios" as="xs:string*">
                <xsl:if test="@css:_obfl-alternate-scenario">
                    <!-- :-obfl-alternate-scenario(1) is equivalent to :-obfl-alternate-scenario -->
                    <xsl:sequence select="'1'"/>
                </xsl:if>
                <xsl:for-each select="$scenario-attributes except @css:_obfl-alternate-scenario">
                    <xsl:sequence select="replace(local-name(.),'^_obfl-alternate-scenario-','')"/>
                </xsl:for-each>
            </xsl:variable>
            <!--
                Create copies of the element for every :-obfl-alternate-scenario pseudo-class
            -->
            <xsl:for-each select="distinct-values($scenarios)">
                <xsl:variable name="scenario" as="xs:string" select="."/>
                <xsl:for-each select="$this">
                    <!--
                        Copy element because it may be a html:table, which is needed if it has a
                        render-table-by property.
                    -->
                    <xsl:copy>
                        <xsl:attribute name="css:_obfl-scenario" select="'_'"/>
                        <xsl:variable name="style" as="xs:string*">
                            <!-- pseudo-classes always derive from the main style -->
                            <xsl:sequence select="@style"/>
                            <xsl:sequence select="@css:*[local-name()=concat('_obfl-alternate-scenario-',$scenario)]"/>
                            <xsl:if test="$scenario='1'">
                                <xsl:sequence select="@css:_obfl-alternate-scenario"/>
                            </xsl:if>
                        </xsl:variable>
                        <xsl:choose>
                            <xsl:when test="count($style)=1">
                                <xsl:attribute name="style" select="$style"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <!-- merge @style and @css:_obfl-alternate-scenario* -->
                                <xsl:attribute name="style" select="css:serialize-stylesheet(
                                                                      for $s in $style return
                                                                        css:parse-stylesheet($s))"/>
                            </xsl:otherwise>
                        </xsl:choose>
                        <!-- skip all css:* attributes except property attributes but including css:flow
                             ('display', 'render-table-by', 'table-header-policy' and 'flow' are the only
                             properties that may be defined as attributes at this point) -->
                        <xsl:sequence select="(@* except (@css:*|@style))|
                                              @css:display|
                                              @css:render-table-by|
                                              @css:table-header-policy"/>
                        <xsl:apply-templates/>
                    </xsl:copy>
                </xsl:for-each>
            </xsl:for-each>
        </css:_>
    </xsl:template>
    
</xsl:stylesheet>
