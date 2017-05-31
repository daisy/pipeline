<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step version="1.0" type="pxi:directory-list" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:p="http://www.w3.org/ns/xproc" xmlns:pxi="http://www.daisy.org/ns/xprocspec/xproc-internal/">

    <p:documentation>The p:directory-list step will return the contents of a single directory. The px:directory-list step will process a directory and it's subdirectories recursively. See also:
        http://xproc.org/library/#recursive-directory-list.</p:documentation>

    <p:output port="result"/>
    <p:option name="path" required="true"/>
    <p:option name="depth" select="-1"/>

    <p:declare-step type="pxi:directory-list-recursive">
        <p:output port="result"/>
        <p:option name="path" required="true"/>
        <p:option name="depth" select="-1"/>

        <p:directory-list>
            <p:with-option name="path" select="$path"/>
        </p:directory-list>

        <p:viewport match="/c:directory/c:directory">
            <p:variable name="name" select="/*/@name"/>

            <p:choose>
                <p:when test="$depth != 0">
                    <pxi:directory-list-recursive>
                        <p:with-option name="path" select="concat($path,'/',$name)"/>
                        <p:with-option name="depth" select="$depth - 1"/>
                    </pxi:directory-list-recursive>
                </p:when>
                <p:otherwise>
                    <p:identity/>
                </p:otherwise>
            </p:choose>
        </p:viewport>
    </p:declare-step>

    <pxi:directory-list-recursive>
        <p:with-option name="path" select="$path"/>
        <p:with-option name="depth" select="$depth"/>
    </pxi:directory-list-recursive>

    <!-- 
         * sort directories alphabetically for easier comparison with other directories
         * also pretty-print
    -->
    <p:xslt>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:inline>
                <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
                    <xsl:template match="/*">
                        <xsl:copy>
                            <xsl:copy-of select="@*"/>
                            <xsl:for-each select="*">
                                <xsl:sort select="@name"/>
                                <xsl:apply-templates select="."/>
                            </xsl:for-each>
                            <xsl:if test="*">
                                <xsl:text>
</xsl:text>
                            </xsl:if>
                        </xsl:copy>
                    </xsl:template>
                    <xsl:template match="@*">
                        <xsl:copy/>
                    </xsl:template>
                    <xsl:template match="*">
                        <xsl:text>
</xsl:text>
                        <xsl:value-of select="string-join(for $i in (1 to count(ancestor::*)) return '    ','')"/>
                        <xsl:copy>
                            <xsl:copy-of select="@*"/>
                            <xsl:for-each select="*">
                                <xsl:sort select="@name"/>
                                <xsl:apply-templates select="."/>
                            </xsl:for-each>
                            <xsl:if test="*">
                                <xsl:text>
</xsl:text>
                                <xsl:value-of select="string-join(for $i in (1 to count(ancestor::*)) return '    ','')"/>
                            </xsl:if>
                        </xsl:copy>
                    </xsl:template>
                </xsl:stylesheet>
            </p:inline>
        </p:input>
    </p:xslt>

</p:declare-step>
