<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step version="1.0" type="px:fileset-add-ref" name="main" xmlns:p="http://www.w3.org/ns/xproc" xmlns:d="http://www.daisy.org/ns/pipeline/data" xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    exclude-inline-prefixes="px">

    <p:input port="source"/>
    <p:output port="result"/>

    <p:option name="href" required="true"/>
    <p:option name="ref" select="''"><!-- if relative; will be resolved relative to the file --></p:option>
    <p:option name="first" select="'false'"/>

    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>

    <!--TODO awkward, add the entry with XProc, then perform URI cleanup-->
    <p:xslt name="href-uri">
        <p:with-param name="href" select="$href"/>
        <p:with-param name="ref" select="$ref"/>
        <p:input port="stylesheet">
            <p:inline>
                <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:pf="http://www.daisy.org/ns/pipeline/functions" version="2.0" exclude-result-prefixes="#all">
                    <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/uri-functions.xsl"/>
                    <xsl:param name="href" required="yes"/>
                    <xsl:param name="ref" required="yes"/>
                    <xsl:template match="/*">
                        <d:ref>
                            <xsl:for-each select="d:file[pf:normalize-uri(resolve-uri(@href,base-uri(.))) = pf:normalize-uri(resolve-uri($href,base-uri(.)))][1]">
                                <xsl:attribute name="href" select="pf:relativize-uri($ref,base-uri(.))"/>
                                <xsl:attribute name="parent-href" select="@href"/>
                            </xsl:for-each>
                        </d:ref>
                    </xsl:template>
                </xsl:stylesheet>
            </p:inline>
        </p:input>
    </p:xslt>

    <p:choose>
        <p:when test="not(/*/@href)">
            <p:variable name="niceHref" select="if (starts-with($href,/*/@xml:base)) then replace($href,/*/@xml:base,'') else if (matches($href,'^[^/]+:')) then replace($href,'^.+/','') else $href">
                <p:pipe port="source" step="main"/>
            </p:variable>
            <p:variable name="niceRef" select="if (starts-with($ref,/*/@xml:base)) then replace($ref,/*/@xml:base,'') else if (matches($ref,'^[^/]+:')) then replace($ref,'^.+/','') else $ref">
                <p:pipe port="source" step="main"/>
            </p:variable>
            <p:identity>
                <p:input port="source">
                    <p:pipe port="source" step="main"/>
                </p:input>
            </p:identity>
            <px:message>
                <p:with-option name="message" select="concat('The file ',$niceHref,' referenced from ',$niceRef,' is not in the fileset.')"/>
            </px:message>
        </p:when>
        <p:otherwise>
            <p:variable name="href-uri-ified" select="/*/@href">
                <p:pipe port="result" step="href-uri"/>
            </p:variable>
            <p:variable name="file-href" select="/*/@parent-href">
                <p:pipe port="result" step="href-uri"/>
            </p:variable>

            <p:delete match="//@parent-href"/>
            <p:identity name="insertion"/>

            <!--Insert the entry as the last or first child of the file-->
            <p:insert>
                <p:with-option name="match" select="concat(&quot;//d:file[@href='&quot;,$file-href,&quot;']&quot;)"/>
                <p:with-option name="position" select="if ($first='true') then 'first-child' else 'last-child'"/>
                <p:input port="source">
                    <p:pipe port="source" step="main"/>
                </p:input>
                <p:input port="insertion">
                    <p:pipe port="result" step="insertion"/>
                </p:input>
            </p:insert>
        </p:otherwise>
    </p:choose>

</p:declare-step>
