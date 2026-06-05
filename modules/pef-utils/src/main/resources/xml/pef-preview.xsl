<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:pef="http://www.daisy.org/ns/2008/pef"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:dp2="http://www.daisy.org/ns/pipeline/"
                xmlns="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="#all">
    
    <xsl:param name="table"/>
    
    <xsl:variable name="table-matches-braille-charset" as="xs:boolean"
                  select="exists(/pef:pef/pef:head/pef:meta/dp2:ascii-braille-charset[.=pef:get-table-id($table)])"/>
    
    <xsl:template match="/">
        <html>
            <head>
                <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
                <link rel="stylesheet" type="text/css" href="pef-preview.css"/>
                <script type="text/javascript" src="pef-preview.js"/>
            </head>
            <body>
                <div id="header">
                    <h1>
                        <span class="label">title</span>
                        <xsl:sequence select="string(/pef:pef/pef:head/pef:meta/dc:title)"/>
                    </h1>
                    <h2>
                        <span class="label">creator</span>
                        <xsl:sequence select="string(/pef:pef/pef:head/pef:meta/dc:creator)"/>
                    </h2>
                    <div id="view-buttons">
                        <button id="view-braille" class="active" onclick="toggleView()">Braille</button>
                        <button id="view-text" onclick="toggleView()">Text</button>
                        <button id="view-metadata" onclick="toggleMetadata()">Metadata</button>
                    </div>
                    <ul id="metadata">
                        <xsl:for-each select="/pef:pef/pef:head/pef:meta/dc:*[not(local-name()=('title','creator'))]">
                            <li>
                                <span class="label"><xsl:sequence select="local-name(.)"/></span>
                                <xsl:sequence select="string(.)"/>
                            </li>
                        </xsl:for-each>
                    </ul>
                </div>
                <ul id="nav" class="nav-volumes">
                    <xsl:variable name="volumes" select="count(//pef:volume)"/>
                    <xsl:for-each select="//pef:volume">
                        <xsl:variable name="volume" select="position()"/>
                        <li class="nav-volume" id="volume{$volume}">
                            <div class="volume-label">
                                <xsl:if test="$volume &gt; 1">
                                    <a class="volume-previous" href="#volume{$volume - 1}">
                                        <xsl:value-of select="$volume - 1"/>
                                    </a>
                                </xsl:if>
                                <a class="volume-current" href="#volume{$volume}">
                                    <xsl:value-of select="$volume"/>
                                </a>
                                <xsl:if test="$volume &lt; $volumes">
                                    <a class="volume-next" href="#volume{$volume + 1}">
                                        <xsl:value-of select="$volume + 1"/>
                                    </a>
                                </xsl:if>
                            </div>
                            <ul class="nav-pages">
                                <xsl:for-each select=".//pef:page">
                                    <xsl:variable name="page" select="format-number(position(), '0')"/>
                                    <li class="nav-page">
                                        <a href="#page{$volume}.{$page}">
                                            <div class="page-number">
                                                <xsl:value-of select="$page"/>
                                            </div>
                                            <div class="page">
                                                <xsl:for-each select="pef:row">
                                                    <div class="row">
                                                        <xsl:sequence select="@rowgap"/>
                                                        <xsl:sequence select="string(.)"/>
                                                    </div>
                                                </xsl:for-each>
                                            </div>
                                        </a>
                                    </li>
                                </xsl:for-each>
                            </ul>
                        </li>
                    </xsl:for-each>
                </ul>
                <div id="main">
                    <xsl:for-each select="//pef:volume">
                        <xsl:variable name="volume" select="position()"/>
                        <div class="volume">
                            <xsl:for-each select=".//pef:page">
                                <xsl:variable name="page" select="position()"/>
                                <span class="bookmark" id="page{$volume}.{$page}"> </span>
                                <div class="page">
                                    <div class="braille-page">
                                        <xsl:for-each select="pef:row">
                                            <div class="row">
                                                <xsl:sequence select="@rowgap"/>
                                                <xsl:sequence select="string(.)"/>
                                            </div>
                                        </xsl:for-each>
                                    </div>
                                    <div class="text-page">
                                        <xsl:for-each select="pef:row">
                                            <div class="row">
                                                <xsl:sequence select="@rowgap"/>
                                                <xsl:sequence select="if ($table-matches-braille-charset and exists(@dp2:ascii))
                                                                      then string(@dp2:ascii)
                                                                      else pf:pef-encode($table,string(.))"/>
                                            </div>
                                        </xsl:for-each>
                                    </div>
                                </div>
                            </xsl:for-each>
                        </div>
                    </xsl:for-each>
                </div>
            </body>
        </html>
    </xsl:template>
    
</xsl:stylesheet>
