<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:smil="http://www.w3.org/2001/SMIL20/"
                xpath-default-namespace="http://openebook.org/namespaces/oeb-package/1.0/"
                exclude-result-prefixes="#all">

    <xsl:output indent="yes" method="xml"/>

    <xsl:template match="package">
        <d:fileset>
            <xsl:attribute name="xml:base" select="replace(base-uri(.),'[^/]+$','')"/>

            <!--get the SMILs in spine order-->
            <xsl:variable name="smils" as="element()*">
                <xsl:for-each select="spine/itemref">
                    <xsl:sequence select="//manifest/item[@id=current()/@idref]"/>
                </xsl:for-each>
            </xsl:variable>

            <!--get the DTBooks in reading order-->
            <xsl:variable name="dtbooks" as="element()*">
                <xsl:sequence select="manifest/item[@media-type='application/x-dtbook+xml']"/>
                <!--
                NOTE:
                  The following wasn't working when invoked in XProc.
                  Ordering has been implemented in the caller XProc script
                -->
                <!--<xsl:choose>
                    <xsl:when test="count(manifest/item[@media-type='application/x-dtbook+xml'])=1">
                        <!-\-if there is only one DTBook no need to compute the order-\->
                        <xsl:sequence select="manifest/item[@media-type='application/x-dtbook+xml']"
                        />
                    </xsl:when>
                    <xsl:otherwise>
                        <!-\-else we load and parse each SMIL to get DTBook references in document order-\->
                        <xsl:variable name="dtbook-uris"
                            select="distinct-values(document($smils/resolve-uri(@href,base-uri(.)))//smil:text/replace(@src,'#.*$',''))" as="xs:string*"/>
                        <xsl:message select="concat('uris=',string-join($dtbook-uris,','))"></xsl:message>
                        <xsl:sequence select="manifest/item[@href=$dtbook-uris]"/>
                    </xsl:otherwise>
                </xsl:choose>-->
            </xsl:variable>
            <!--Finally apply templates to create the fileset: first SMIL, then DTBooks, then resources-->
            <xsl:apply-templates select="$smils"/>
            <xsl:apply-templates select="$dtbooks"/>
            <xsl:apply-templates
                select="manifest/item[not(@media-type=('application/smil','application/x-dtbook+xml'))]"
            />
        </d:fileset>
    </xsl:template>

    <xsl:template match="item">
        <d:file href="{@href}" media-type="{
            if(@media-type='application/smil') then 'application/smil+xml'
            else if (@media-type='text/xml' and ends-with(@href,'.opf')) then 'application/oebps-package+xml'
            else if (@media-type='text/xml' and ends-with(@href,'.ncx')) then 'application/x-dtbncx+xml'
            else @media-type}"/>
    </xsl:template>

    <xsl:template match="text()"/>


</xsl:stylesheet>
