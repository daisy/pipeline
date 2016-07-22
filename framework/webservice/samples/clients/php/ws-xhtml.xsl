<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml" version="1.0"
    xmlns:data="http://www.daisy.org/ns/pipeline/data">

    <xsl:output indent="yes"/>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="/">
        <html>
            <head>
                <title>
                    <xsl:value-of select="local-name()"/>
                </title>
                <style type="text/css">
                    table {
                        margin : 1em 1em 1em 2em;
                        background : whitesmoke;
                        border-collapse : collapse;
                    }
                    table th,
                    table td {
                        border : 1px silver solid;
                        padding : 0.2em;
                        padding-left : 10px;
                        padding-right : 10px;
                        vertical-align : middle;
                        height : 20px;
                    }
                    table th {
                        background : gainsboro;
                        text-align : left;
                        white-space : nowrap;
                    }</style>
                <style>
                    body {
                        font-family : Ubuntu, sans-serif;
                    }</style>
                <link rel="stylesheet" type="text/css" href="http://fonts.googleapis.com/css?family=Ubuntu:regular,bold&amp;subset=Latin"/>
            </head>
            <body>
                <xsl:apply-templates/>
            </body>
        </html>
    </xsl:template>

    <xsl:template match="data:scripts">
        <h1>Scripts</h1>
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="data:script">
        <h2>
            <xsl:value-of select="data:nicename"/>
        </h2>
        <p>Script URI: <code class="uri"><xsl:value-of select="@href"/></code></p>
        <p>
            <xsl:value-of select="data:description"/>
        </p>
        <xsl:if test="data:input">
            <h3>Inputs</h3>
            <table>
                <tr>
                    <th>Name</th>
                    <th>Media Type</th>
                    <th>Sequence</th>
                    <th>Description</th>
                </tr>
                <xsl:for-each select="data:input">
                    <tr>
                        <td>
                            <xsl:value-of select="@name"/>
                        </td>
                        <td>
                            <xsl:value-of select="@mediaType"/>
                        </td>
                        <td>
                            <xsl:value-of select="@sequenceAllowed"/>
                        </td>
                        <td>
                            <xsl:value-of select="@desc"/>
                        </td>
                    </tr>
                </xsl:for-each>
            </table>
        </xsl:if>
        <xsl:if test="data:output">
            <h3>Outputs</h3>
            <table>
                <tr>
                    <th>Name</th>
                    <th>Media Type</th>
                    <th>Sequence</th>
                    <th>Description</th>
                </tr>
                <xsl:for-each select="data:output">
                    <tr>
                        <td>
                            <xsl:value-of select="@name"/>
                        </td>
                        <td>
                            <xsl:value-of select="@mediaType"/>
                        </td>
                        <td>
                            <xsl:value-of select="@sequenceAllowed"/>
                        </td>
                        <td>
                            <xsl:value-of select="@desc"/>
                        </td>
                    </tr>
                </xsl:for-each>
            </table>
        </xsl:if>
        <xsl:if test="data:option">
            <h3>Options</h3>
            <table>
                <tr>
                    <th>Name</th>
                    <th>Type</th>
                    <th>Media Type</th>
                    <th>Required</th>
                    <th>Description</th>
                </tr>
                <xsl:for-each select="data:option">
                    <tr>
                        <td>
                            <xsl:value-of select="@name"/>
                        </td>
                        <td>
                            <xsl:value-of select="@type"/>
                        </td>
                        <td>
                            <xsl:value-of select="@mediaType"/>
                        </td>
                        <td>
                            <xsl:value-of select="@required"/>
                        </td>
                        <td>
                            <xsl:value-of select="@desc"/>
                        </td>
                    </tr>
                </xsl:for-each>
            </table>
        </xsl:if>
        <hr/>
    </xsl:template>

    <xsl:template match="data:nicename | data:description"/>

    <xsl:template match="data:homepage">
        <p>Homepage: <a class="homepage" href="."><xsl:value-of select="."/></a></p>
    </xsl:template>
    
    <xsl:template match="data:jobs">
        <h1>Jobs</h1>
        <xsl:apply-templates/>
    </xsl:template>
    
    <xsl:template match="data:job">
        <h2>Job: <xsl:value-of select="@id"/></h2>
        <p>Status: <span class="status"><xsl:value-of select="@status"/></span></p>
        <xsl:apply-templates select="*[not(self::data:messages)]"/>
        <xsl:apply-templates select="data:messages"/>
    </xsl:template>
    
    <xsl:template match="data:result">
        <p>Result: <a class="result" href="{@href}"><xsl:value-of select="@href"/></a></p>
    </xsl:template>
    
    <xsl:template match="data:messages">
        <h3>Messages</h3>
        <xsl:apply-templates/>
    </xsl:template>
    
    <xsl:template match="data:log">
        <p>Log: <a class="log" href="{@href}"><xsl:value-of select="@href"/></a></p>
    </xsl:template>

</xsl:stylesheet>
