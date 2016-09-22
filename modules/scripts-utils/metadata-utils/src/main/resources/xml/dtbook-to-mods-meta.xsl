<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/" xmlns="http://www.loc.gov/mods/v3"
    xmlns:dcterms="http://purl.org/dc/terms/"
    version="2.0" exclude-result-prefixes="dtb">

    <xsl:output indent="yes" method="xml"/>

    <!-- create MODS output from metadata in a DTBook document-->

    <xsl:template match="/">
        <mods xmlns="http://www.loc.gov/mods/v3" version="3.3">
            <xsl:apply-templates/>
        </mods>
    </xsl:template>

    <xsl:template match="dtb:head">

        <xsl:apply-templates/>

        <!-- if there are any dtb:source* metadata properties, then create the relatedItem element -->
        <xsl:if test="./dtb:meta[contains(@name, 'dtb:source')]">
            <relatedItem type="original">
                <xsl:if test="./dtb:meta[@name = 'dtb:sourceRights']">
                    <accessCondition>
                        <xsl:value-of select="./dtb:meta[@name = 'dtb:sourceRights']/@content"/>
                    </accessCondition>
                </xsl:if>

                <xsl:if test="./dtb:meta[@name = 'dtb:sourceTitle']">
                    <titleInfo>
                        <title>
                            <xsl:value-of select="./dtb:meta[@name = 'dtb:sourceTitle']/@content"/>
                        </title>
                    </titleInfo>
                </xsl:if>

                <!-- if any of these properties are available, then create and populate the originInfo element -->
                <xsl:if
                    test="./dtb:meta[@name = 'dtb:sourceDate'] or ./dtb:meta[@name = 'dtb:sourceEdition'] or 
                           ./dtb:meta[@name = 'dtb:sourcePublisher']">
                    <originInfo>
                        <xsl:if test="./dtb:meta[@name = 'dtb:sourceDate']">
                            <dateIssued>
                                <xsl:value-of select="./dtb:meta[@name = 'dtb:sourceDate']/@content"
                                />
                            </dateIssued>
                        </xsl:if>
                        <xsl:if test="./dtb:meta[@name = 'dtb:sourceEdition']">
                            <edition>
                                <xsl:value-of
                                    select="./dtb:meta[@name = 'dtb:sourceEdition']/@content"/>
                            </edition>
                        </xsl:if>
                        <xsl:if test="./dtb:meta[@name = 'dtb:sourcePublisher']">
                            <publisher>
                                <xsl:value-of
                                    select="./dtb:meta[@name = 'dtb:sourcePublisher']/@content"/>
                            </publisher>
                        </xsl:if>
                    </originInfo>
                </xsl:if>
            </relatedItem>
        </xsl:if>

        <!-- if there are any dtb:produce* metadata properties, then create the originInfo element -->
        <xsl:if
            test="./dtb:meta[contains(@name, 'dtb:producer')] or ./dtb:meta[contains(@name, 'dtb:producedDate')]">
            <originInfo>
                <xsl:if test="./dtb:meta[@name = 'dtb:producer']">
                    <publisher>
                        <xsl:value-of select="./dtb:meta[@name = 'dtb:producer']/@content"/>
                    </publisher>
                </xsl:if>
                <xsl:if test="./dtb:meta[@name = 'dtb:producedDate']">
                    <dateCreated>
                        <xsl:value-of select="./dtb:meta[@name = 'dtb:producedDate']/@content"/>
                    </dateCreated>
                </xsl:if>
            </originInfo>
        </xsl:if>
    </xsl:template>

    <!-- process dublin core metadata -->
    <xsl:template match="dtb:meta[@name = 'dc:Title']">
        <titleInfo>
            <title>
                <xsl:value-of select="@content"/>
            </title>
        </titleInfo>
    </xsl:template>

    <xsl:template match="dtb:meta[@name= 'dc:Creator']">
        <name>
            <namePart>
                <xsl:value-of select="@content"/>
            </namePart>
            <role>
                <roleTerm type="text">author</roleTerm>
            </role>
        </name>
    </xsl:template>

    <xsl:template match="dtb:meta[@name= 'dc:Identifier']">
        <identifier type="uid">
            <xsl:value-of select="@content"/>
        </identifier>
    </xsl:template>

    <xsl:template match="dtb:meta[@name = 'dc:Language']">
        <language>
            <languageTerm type="code" authority="rfc3066">
                <xsl:value-of select="@content"/>
            </languageTerm>
        </language>
    </xsl:template>

    <xsl:template match="dtb:meta[@name = 'dc:Subject']">
        <subject>
            <topic>
                <xsl:value-of select="@content"/>
            </topic>
        </subject>
    </xsl:template>

    <xsl:template match="dtb:meta[@name = 'dc:Description']">
        <note>
            <xsl:value-of select="@content"/>
        </note>
    </xsl:template>

    <xsl:template match="dtb:meta[@name = 'dc:Type']">
        <xsl:choose>
            <xsl:when test="lower-case(@content) = 'text'">
                <typeOfResource>text</typeOfResource>
            </xsl:when>
            <xsl:when test="lower-case(@content) = 'sound'">
                <typeOfResource>sound recording</typeOfResource>
            </xsl:when>
            <xsl:when test="lower-case(@content) = 'image' or lower-case(@content) = 'stillimage'">
                <typeOfResource>still image</typeOfResource>
            </xsl:when>
            <xsl:when test="lower-case(@content) = 'movingimage'">
                <typeOfResource>moving image</typeOfResource>
            </xsl:when>
            <xsl:when test="lower-case(@content) = 'software'">
                <typeOfResource>software, multimedia</typeOfResource>
            </xsl:when>
            <xsl:when test="empty(@content)">
                <!-- empty values are permitted -->
                <typeOfResource/>
            </xsl:when>
            <xsl:otherwise>
                <!-- use the extension element to hold the dc:Type data -->
                <xsl:element name="extension">
                    <xsl:element name="dcterms:Type">
                        <xsl:value-of select="@content"/>
                    </xsl:element>
                </xsl:element>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="dtb:meta[@name = 'dc:Source' and @scheme = 'isbn']">
        <identifier type="isbn">
            <xsl:value-of select="@content"/>
        </identifier>
    </xsl:template>

    <xsl:template match="dtb:meta[@name= 'dc:Contributor']">
        <name>
            <namePart>
                <xsl:value-of select="@content"/>
            </namePart>
            <role>
                <roleTerm type="text">contributor</roleTerm>
            </role>
        </name>
    </xsl:template>

    <xsl:template match="dtb:meta[@name= 'dc:Rights']">
        <accessCondition>
            <xsl:value-of select="@content"/>
        </accessCondition>
    </xsl:template>

    <!-- identity template which discards everything -->
    <xsl:template match="@*|node()">
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
</xsl:stylesheet>
