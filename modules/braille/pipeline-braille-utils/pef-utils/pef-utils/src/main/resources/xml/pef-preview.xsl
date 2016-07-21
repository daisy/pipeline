<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:pef="http://www.daisy.org/ns/2008/pef"
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns="http://www.w3.org/1999/xhtml"
    exclude-result-prefixes="#all"
    version="2.0">
    
    <xsl:param name="table"/>
    
    <xsl:template match="/">
        <html>
            <head>
                <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
                <style type="text/css">
                    <xsl:value-of select="unparsed-text('pef-preview.css', 'utf-8')"/>
                </style>
                <script type="text/javascript">
                    <xsl:value-of select="unparsed-text('pef-preview.js', 'utf-8')"/>
                </script>
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
                <ul id="nav">
                    <xsl:for-each select="/pef:pef//pef:page">
                        <xsl:variable name="i" select="format-number(position(), '0')"/>
                        <li class="nav-page">
                            <a href="{concat('#page', $i)}">
                                <div class="page-number">
                                    <xsl:value-of select="$i"/>
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
                <div id="main">
                    <xsl:for-each select="/pef:pef//pef:page">
                        <xsl:variable name="i" select="format-number(position(), '0')"/>
                        <span class="bookmark" id="{concat('page', $i)}"> </span>
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
                                        <xsl:sequence select="pef:encode($table, string(.))"/>
                                    </div>
                                </xsl:for-each>
                            </div>
                        </div>
                    </xsl:for-each>
                </div>
            </body>
        </html>
    </xsl:template>
    
</xsl:stylesheet>
