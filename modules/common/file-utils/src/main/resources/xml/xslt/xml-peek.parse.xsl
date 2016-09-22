<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="#all" version="2.0" xmlns="http://www.w3.org/ns/xproc-step"
    xmlns:c="http://www.w3.org/ns/xproc-step" xpath-default-namespace="http://www.w3.org/ns/xproc-step">

    <!-- character groups -->
    <xsl:variable name="NameStartChar"
        select="':A-Z_a-z&#xC0;-&#xD6;&#xD8;-&#xF6;&#xF8;-&#x2FF;&#x370;-&#x37D;&#x37F;-&#x1FFF;&#x200C;-&#x200D;&#x2070;-&#x218F;&#x2C00;-&#x2FEF;&#x3001;-&#xD7FF;&#xF900;-&#xFDCF;&#xFDF0;-&#xFFFD;&#x10000;-&#xEFFFF;'"/>
    <xsl:variable name="NameChar" select="concat($NameStartChar,'\-\.0-9&#xB7;&#x0300;-&#x036F;&#x203F;-&#x2040;')"/>
    <xsl:variable name="PubidCharNoApos" select="'&#x20;&#xD;&#xA;a-zA-Z0-9\-\(\)\+,\./:=\?;!\*#@\$_%'"/>
    <xsl:variable name="PubidChar" select="concat($PubidCharNoApos,'''')"/>

    <!-- regex character sequences -->
    <xsl:variable name="Name" select="concat('[',$NameStartChar,'][',$NameChar,']*')"/>
    <xsl:variable name="S" select="'[&#x20;&#x9;&#xD;&#xA;]+'"/>
    <xsl:variable name="SystemLiteral" select="'(&quot;[^&quot;]*&quot;|''[^'']*'')'"/>
    <xsl:variable name="PubidLiteral" select="concat('(&quot;[',$PubidChar,']*&quot;|''[',$PubidCharNoApos,']*'')')"/>

    <xsl:template match="/*">

        <c:result>
            <xsl:attribute name="xml:space" select="'preserve'"/>
            <xsl:text>
</xsl:text>

            <xsl:variable name="bom">
                <xsl:if test="@bom-type">
                    <xsl:text>    </xsl:text>
                    <c:bom type="{@bom-type}">
                        <xsl:if test="@bom-base64">
                            <xsl:attribute name="base64" select="@bom-base64"/>
                        </xsl:if>
                        <xsl:if test="@bom-hex">
                            <xsl:attribute name="hex" select="@bom-hex"/>
                        </xsl:if>
                    </c:bom>
                    <xsl:text>
</xsl:text>
                </xsl:if>
            </xsl:variable>

            <xsl:variable name="result">
                <xsl:analyze-string select="text()" regex="&lt;[?!][^&gt;]*&gt;[^&lt;]*">
                    <xsl:matching-substring>
                        <xsl:choose>
                            <xsl:when test="starts-with(.,'&lt;?')">
                                <c:processing-instruction>
                                    <xsl:value-of select="replace(.,'\s+$','')"/>
                                </c:processing-instruction>
                            </xsl:when>
                            <xsl:when test="starts-with(.,'&lt;!--')">
                                <c:comment>
                                    <xsl:value-of select="replace(.,'\s+$','')"/>
                                </c:comment>
                            </xsl:when>
                            <xsl:otherwise>
                                <c:doctype>
                                    <xsl:if test="upper-case(substring(.,1,9))='&lt;!DOCTYPE'">
                                        <xsl:attribute name="doctype-start" select="'true'"/>
                                    </xsl:if>
                                    <xsl:value-of select="."/>
                                </c:doctype>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:matching-substring>
                    <xsl:non-matching-substring>
                        <xsl:choose>
                            <xsl:when test="contains(.,'&lt;')">
                                <xsl:variable name="before-root-element" select="substring-before(.,'&lt;')"/>
                                <xsl:if test="$before-root-element">
                                    <c:other>
                                        <xsl:value-of select="$before-root-element"/>
                                    </c:other>
                                </xsl:if>
                                <c:root-element>
                                    <xsl:value-of select="concat('&lt;',replace(substring-after(.,'&lt;'),'&gt;[^&gt;]*$','&gt;'))"/>
                                </c:root-element>
                            </xsl:when>
                            <xsl:when test="normalize-space(.)">
                                <c:other>
                                    <xsl:value-of select="."/>
                                </c:other>
                            </xsl:when>
                        </xsl:choose>
                    </xsl:non-matching-substring>
                </xsl:analyze-string>
            </xsl:variable>
            <xsl:variable name="result">
                <xsl:for-each-group select="$result/*" group-adjacent="self::c:doctype or self::c:other and preceding-sibling::*[not(self::c:other)][1]/self::c:doctype">
                    <xsl:choose>
                        <xsl:when test="current-grouping-key()">
                            <xsl:for-each-group select="current-group()" group-starting-with="c:doctype[@doctype-start='true']">
                                <c:doctype>
                                    <xsl:copy-of select="current-group()"/>
                                </c:doctype>
                            </xsl:for-each-group>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:for-each-group select="current-group()" group-adjacent="self::c:comment or self::c:other and preceding-sibling::*[not(self::c:other)][1]/self::c:comment">
                                <xsl:choose>
                                    <xsl:when test="current-grouping-key()">
                                        <xsl:for-each-group select="current-group()" group-starting-with="c:comment[starts-with(.,'&lt;!--')]">
                                            <c:comment>
                                                <xsl:copy-of select="current-group()"/>
                                            </c:comment>
                                        </xsl:for-each-group>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:copy-of select="current-group()"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:for-each-group>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each-group>
            </xsl:variable>
            <xsl:variable name="result">
                <xsl:for-each select="$result/*">
                    <xsl:choose>
                        <xsl:when test="self::c:other and position() = 1">
                            <!-- probably a BOM, but not easy to parse in XSLT so don't try to interpret which type of BOM -->
                            <c:bom>
                                <xsl:copy-of select="$bom/*/@*"/>
                                <xsl:value-of select="."/>
                            </c:bom>
                        </xsl:when>
                        <xsl:when test="self::c:processing-instruction">
                            <xsl:element name="c:{replace(text(),concat('^&lt;\?(',$Name,')(',$S,'|\?).*$'),'$1','s')}">
                                <xsl:analyze-string select="text()" regex="{concat($Name,'\s*=\s*(&quot;[^&quot;]*&quot;|''[^'']*'')')}">
                                    <xsl:matching-substring>
                                        <xsl:attribute name="{tokenize(.,'=')[1]}" select="replace(replace(.,concat($Name,'\s*=\s*(&quot;[^&quot;]*&quot;|''[^'']*'')'),'$1'),'^.(.*).$','$1','s')"/>
                                    </xsl:matching-substring>
                                </xsl:analyze-string>
                                <xsl:copy-of select="text()"/>
                            </xsl:element>
                        </xsl:when>
                        <xsl:when test="self::c:doctype">
                            <xsl:copy>
                                <xsl:variable name="doctype" select="replace(string-join(*/text(),''),'\s+$','','s')"/>
                                <xsl:attribute name="name" select="replace($doctype,concat('&lt;!DOCTYPE',$S,'(',$Name,')','[\s&gt;].*'),'$1','s')"/>
                                <xsl:variable name="public"
                                    select="if (matches($doctype,concat('^&lt;!DOCTYPE',$S,$Name,$S,'PUBLIC',$S,$PubidLiteral,$S,$SystemLiteral,'.*$'))) then replace(replace($doctype,concat('^&lt;!DOCTYPE',$S,$Name,$S,'PUBLIC',$S,'(',$PubidLiteral,')',$S,'.*$'),'$1'),'^.(.*).$','$1') else ()"/>
                                <xsl:variable name="system"
                                    select="if (matches($doctype,concat('^&lt;!DOCTYPE',$S,$Name,$S,'PUBLIC',$S,$PubidLiteral,$S,$SystemLiteral,'.*$'))) then replace(replace($doctype,concat('^&lt;!DOCTYPE',$S,$Name,$S,'PUBLIC',$S,$PubidLiteral,$S,'(',$SystemLiteral,')(',$S,'|\[|&gt;).*$'),'$2'),'^.(.*).$','$1') else ()"/>
                                <xsl:variable name="system"
                                    select="if (not($system) and matches($doctype,concat('^&lt;!DOCTYPE',$S,$Name,$S,'SYSTEM',$S,$SystemLiteral,'.*$'))) then replace(replace($doctype,concat('^&lt;!DOCTYPE',$S,$Name,$S,'SYSTEM',$S,'(',$SystemLiteral,')(',$S,'|\[|&gt;).*$'),'$1'),'^.(.*).$','$1') else $system"/>
                                <xsl:variable name="internal" select="if (contains($doctype,'[')) then replace($doctype,'^[^\[]*\[\s*(.*?)\s*\][^\]]*$','$1','s') else ()"/>
                                <xsl:if test="$public">
                                    <xsl:attribute name="public" select="$public"/>
                                </xsl:if>
                                <xsl:if test="$system">
                                    <xsl:attribute name="system" select="$system"/>
                                </xsl:if>
                                <xsl:if test="$internal or $internal=''">
                                    <xsl:attribute name="internal" select="$internal"/>
                                </xsl:if>
                                <xsl:value-of select="$doctype"/>
                            </xsl:copy>
                        </xsl:when>
                        <xsl:when test="self::c:comment">
                            <xsl:copy>
                                <xsl:variable name="comment" select="string-join(*/text(),'')"/>
                                <xsl:attribute name="text" select="substring-after(substring-before($comment,'--&gt;'),'&lt;!--')"/>
                                <xsl:value-of select="$comment"/>
                            </xsl:copy>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:copy-of select="."/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>
            </xsl:variable>

            <xsl:if test="not($result/c:bom and $bom)">
                <xsl:copy-of select="$bom"/>
            </xsl:if>

            <xsl:for-each select="$result/*">
                <xsl:text>    </xsl:text>
                <xsl:copy-of select="."/>
                <xsl:text>
</xsl:text>
            </xsl:for-each>
        </c:result>
    </xsl:template>

</xsl:stylesheet>
