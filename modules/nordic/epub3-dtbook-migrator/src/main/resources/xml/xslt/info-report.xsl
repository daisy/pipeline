<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="#all" version="2.0"
    xmlns:d="http://www.daisy.org/ns/pipeline/data" xmlns:epub="http://www.idpf.org/2007/ops">

    <xsl:template match="/*">
        <d:document-validation-report>
            <d:document-info>
                <d:document-name>List of elements, types and classes used</d:document-name>
                <d:document-type>Fileset statistics</d:document-type>
                <d:error-count>0</d:error-count>
                <d:properties>
                    <xsl:variable name="types">
                        <xsl:for-each select="distinct-values(collection()//tokenize(@epub:type,'\s+'))">
                            <xsl:variable name="name" select="."/>
                            <d:property name="epub:type: '{$name}'" content="{count(collection()//*[tokenize(@epub:type,'\s+')=$name])}"/>
                        </xsl:for-each>
                    </xsl:variable>
                    <xsl:variable name="classes">
                        <xsl:for-each select="distinct-values(collection()//tokenize(@class,'\s+'))">
                            <xsl:variable name="name" select="."/>
                            <d:property name="class: '{$name}'" content="{count(collection()//*[tokenize(@class,'\s+')=$name])}"/>
                        </xsl:for-each>
                    </xsl:variable>
                    <xsl:variable name="elements">
                        <xsl:for-each select="distinct-values(collection()//*/name())">
                            <xsl:variable name="name" select="."/>
                            <d:property name="element: '{$name}'" content="{count(collection()//*[name()=$name])}"/>
                        </xsl:for-each>
                    </xsl:variable>
                    <xsl:for-each select="$types/*">
                        <xsl:sort select="xs:integer(@content)"/>
                        <xsl:copy-of select="." exclude-result-prefixes="#all"/>
                    </xsl:for-each>
                    <xsl:for-each select="$classes/*">
                        <xsl:sort select="xs:integer(@content)"/>
                        <xsl:copy-of select="." exclude-result-prefixes="#all"/>
                    </xsl:for-each>
                    <xsl:for-each select="$elements/*">
                        <xsl:sort select="xs:integer(@content)"/>
                        <xsl:copy-of select="." exclude-result-prefixes="#all"/>
                    </xsl:for-each>
                </d:properties>
            </d:document-info>
        </d:document-validation-report>
    </xsl:template>

</xsl:stylesheet>
