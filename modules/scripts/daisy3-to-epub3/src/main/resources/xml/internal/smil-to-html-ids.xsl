<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:d="http://www.daisy.org/ns/pipeline/data"
    xmlns:smil="http://www.w3.org/2001/SMIL20/"
    xmlns:dtbook="http://www.daisy.org/z3986/2005/dtbook/" xmlns:html="http://www.w3.org/1999/xhtml"
    exclude-result-prefixes="#all" version="2.0">

    <xsl:variable name="smils" select="collection()[/smil:smil]"/>
    <xsl:variable name="dtbook-uris" select="collection()[/dtbook:dtbook]/base-uri(*)"/>
    <xsl:variable name="htmls" select="collection()[/html:html]"/>
    
    <xsl:key name="ids" match="*[@id]" use="@id"/>

    <xsl:template name="create-id-map">
        <d:idmap>
            <xsl:for-each select="$smils">
                <d:doc href="{base-uri(.)}">
                    <xsl:apply-templates/>
                </d:doc>
            </xsl:for-each>
        </d:idmap>
    </xsl:template>

    <xsl:template match="smil:text[@id]|smil:*[@id][count(smil:text)=1]">
        <xsl:variable name="dtbook-ref"
            select="resolve-uri(substring-before(descendant-or-self::smil:text/@src,'#'),base-uri(.))"/>
        <xsl:variable name="idref" select="substring-after(descendant-or-self::smil:text/@src,'#')"/>
        <xsl:variable name="new-idref" select="if(count($dtbook-uris)=1) then $idref else concat(index-of($dtbook-ref,$dtbook-uris),'_',$idref)"/>
        <d:id old-id="{@id}"
            new-id="{$new-idref}"
            new-src="{$htmls[key('ids',$new-idref,.)][1]/base-uri(*)}"/>
    </xsl:template>

</xsl:stylesheet>
