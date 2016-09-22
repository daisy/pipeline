<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step version="1.0" type="px:directory-list" xmlns:c="http://www.w3.org/ns/xproc-step"
    xmlns:p="http://www.w3.org/ns/xproc" xmlns:px="http://www.daisy.org/ns/pipeline/xproc">

    <p:documentation>The p:directory-list step will return the contents of a single directory. The px:directory-list step will process a directory and it's subdirectories recursively. See also: http://xproc.org/library/#recursive-directory-list.</p:documentation>

    <p:output port="result"/>
    <p:option name="path" required="true"/>
    <p:option name="include-filter"/>
    <p:option name="exclude-filter"/>
    <p:option name="depth" select="-1"/>

    <p:choose>
        <p:when
            test="p:value-available('include-filter')
              and p:value-available('exclude-filter')">
            <p:directory-list>
                <p:with-option name="path" select="$path"/>
                <p:with-option name="include-filter" select="$include-filter"/>
                <p:with-option name="exclude-filter" select="$exclude-filter"/>
            </p:directory-list>
        </p:when>

        <p:when test="p:value-available('include-filter')">
            <p:directory-list>
                <p:with-option name="path" select="$path"/>
                <p:with-option name="include-filter" select="$include-filter"/>
            </p:directory-list>
        </p:when>

        <p:when test="p:value-available('exclude-filter')">
            <p:directory-list>
                <p:with-option name="path" select="$path"/>
                <p:with-option name="exclude-filter" select="$exclude-filter"/>
            </p:directory-list>
        </p:when>

        <p:otherwise>
            <p:directory-list>
                <p:with-option name="path" select="$path"/>
            </p:directory-list>
        </p:otherwise>
    </p:choose>

    <p:viewport match="/c:directory/c:directory">
        <p:variable name="name" select="/*/@name"/>

        <p:choose>
            <p:when test="$depth != 0">
                <p:choose>
                    <p:when
                        test="p:value-available('include-filter')
                          and p:value-available('exclude-filter')">
                        <px:directory-list>
                            <p:with-option name="path" select="concat($path,'/',$name)"/>
                            <p:with-option name="include-filter" select="$include-filter"/>
                            <p:with-option name="exclude-filter" select="$exclude-filter"/>
                            <p:with-option name="depth" select="$depth - 1"/>
                        </px:directory-list>
                    </p:when>

                    <p:when test="p:value-available('include-filter')">
                        <px:directory-list>
                            <p:with-option name="path" select="concat($path,'/',$name)"/>
                            <p:with-option name="include-filter" select="$include-filter"/>
                            <p:with-option name="depth" select="$depth - 1"/>
                        </px:directory-list>
                    </p:when>

                    <p:when test="p:value-available('exclude-filter')">
                        <px:directory-list>
                            <p:with-option name="path" select="concat($path,'/',$name)"/>
                            <p:with-option name="exclude-filter" select="$exclude-filter"/>
                            <p:with-option name="depth" select="$depth - 1"/>
                        </px:directory-list>
                    </p:when>

                    <p:otherwise>
                        <px:directory-list>
                            <p:with-option name="path" select="concat($path,'/',$name)"/>
                            <p:with-option name="depth" select="$depth - 1"/>
                        </px:directory-list>
                    </p:otherwise>
                </p:choose>
            </p:when>
            <p:otherwise>
                <p:identity/>
            </p:otherwise>
        </p:choose>
    </p:viewport>
    
    <p:xslt>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="../xslt/sort-directory-list.xsl"/>
        </p:input>
    </p:xslt>

</p:declare-step>
