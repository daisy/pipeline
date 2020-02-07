<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                exclude-result-prefixes="#all"
                version="2.0">
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*[@css:_obfl-on-toc-start|
                           @css:_obfl-on-volume-start|
                           @css:_obfl-on-volume-end|
                           @css:_obfl-on-toc-end]">
        <xsl:copy>
            <xsl:sequence select="@* except (@css:_obfl-on-toc-start|
                                             @css:_obfl-on-volume-start|
                                             @css:_obfl-on-volume-end|
                                             @css:_obfl-on-toc-end)"/>
            <xsl:if test="@css:_obfl-on-toc-start">
                <css:_obfl-on-toc-start style="{@css:_obfl-on-toc-start}"/>
            </xsl:if>
            <xsl:if test="@css:_obfl-on-volume-start">
                <css:_obfl-on-volume-start style="{@css:_obfl-on-volume-start}"/>
            </xsl:if>
            <xsl:if test="@css:_obfl-on-volume-end">
                <css:_obfl-on-volume-end style="{@css:_obfl-on-volume-end}"/>
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
    
    <xsl:template match="*[@css:_obfl-alternate-scenario]">
        <xsl:if test="@css:flow[not(.='normal')]">
            <xsl:message terminate="yes">Elements with a ::-obfl-alternate-scenario pseudo-element must participate in the normal flow.</xsl:message>
        </xsl:if>
        <!--
            The reason we use attributes to tag the scenarios, and not elements, is because elements
            are renamed later in the process. We ensure the tree structure is not changed by forcing
            the "obfl-scenarios" and "obfl-scenario" elements to be blocks (the latter happens later
            in pxi:css-to-obfl).
        -->
        <css:_ css:_obfl-scenarios="_" css:display="block">
            <xsl:copy>
                <xsl:attribute name="css:_obfl-scenario" select="'_'"/>
                <xsl:sequence select="@* except @css:_obfl-alternate-scenario"/>
                <xsl:if test="not(@name)">
                    <xsl:attribute name="name" select="name(.)"/>
                </xsl:if>
                <xsl:apply-templates/>
            </xsl:copy>
            <!--
                Copy element because it may be a html:table, which is needed if it has a
                render-table-by property.
            -->
            <xsl:copy>
                <xsl:attribute name="css:_obfl-scenario" select="'_'"/>
                <xsl:attribute name="style" select="@css:_obfl-alternate-scenario"/>
                <xsl:sequence select="@* except (@style|@css:*)"/>
                <xsl:apply-templates/>
            </xsl:copy>
        </css:_>
    </xsl:template>
    
</xsl:stylesheet>
