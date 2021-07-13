<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:functx="http://www.functx.com"
                xmlns:obfl="http://www.daisy.org/ns/2011/obfl"
                xmlns="http://www.daisy.org/ns/2011/obfl"
                xpath-default-namespace="http://www.daisy.org/ns/2011/obfl"
                exclude-result-prefixes="#all">

    <xsl:variable name="tocs" as="element(obfl:table-of-contents)*"
                  select="functx:distinct-deep(//toc-sequence/table-of-contents)"/>

    <xsl:template match="/obfl/volume-template[not(preceding-sibling::volume-template)]">
        <xsl:for-each select="$tocs">
            <xsl:copy>
                <xsl:attribute name="name" select="concat('toc_',position())"/>
                <xsl:sequence select="@*|node()"/>
            </xsl:copy>
        </xsl:for-each>
        <xsl:next-match/>
    </xsl:template>

    <xsl:template match="toc-sequence">
        <xsl:variable name="toc" as="element(obfl:table-of-contents)" select="table-of-contents"/>
        <xsl:variable name="toc" as="element(obfl:table-of-contents)" select="$tocs[.=$toc or deep-equal(., $toc)]"/>
        <xsl:copy>
            <xsl:attribute name="toc" select="concat('toc_',functx:index-of-node($tocs,$toc))"/>
            <xsl:apply-templates select="@*"/>
            <xsl:apply-templates select="node() except table-of-contents"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <!--
        http://www.xsltfunctions.com/xsl/functx_distinct-deep.html
    -->
    <xsl:function name="functx:distinct-deep" as="node()*">
        <xsl:param name="nodes" as="node()*"/>
        <xsl:sequence select="for $seq in (1 to count($nodes))
                              return $nodes[$seq][not(functx:is-node-in-sequence-deep-equal(
                                                    .,$nodes[position() &lt; $seq]))]"/>
    </xsl:function>

    <!--
        http://www.xsltfunctions.com/xsl/functx_is-node-in-sequence-deep-equal.html
    -->
    <xsl:function name="functx:is-node-in-sequence-deep-equal" as="xs:boolean">
        <xsl:param name="node" as="node()?"/>
        <xsl:param name="seq" as="node()*"/>
        <xsl:sequence select="some $nodeInSeq in $seq satisfies deep-equal($nodeInSeq,$node)"/>
    </xsl:function>

    <!--
        http://www.xsltfunctions.com/xsl/functx_index-of-node.html
    -->
    <xsl:function name="functx:index-of-node" as="xs:integer*">
        <xsl:param name="nodes" as="node()*"/>
        <xsl:param name="nodeToFind" as="node()"/>
        <xsl:sequence select="for $seq in (1 to count($nodes))
                              return $seq[$nodes[$seq] is $nodeToFind]"/>
    </xsl:function>

</xsl:stylesheet>
