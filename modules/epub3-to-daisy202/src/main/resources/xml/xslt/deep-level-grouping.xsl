<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions/deep-level-grouping" exclude-result-prefixes="#all">

    <!-- Space separated string of element names to un-nest -->
    <xsl:param name="name" required="yes"/>

    <!-- Namespace of element(s) to un-nest -->
    <xsl:param name="namespace" required="yes"/>

    <!-- The max allowed nesting depth, after which unnesting will begin -->
    <xsl:param name="max-depth" required="yes"/>
    <xsl:variable name="max-depth-int" select="xs:integer($max-depth)"/>

    <!-- Whether wrapper elements should be copied into the resulting unwrapped elements.
        The default is false to ensure a valid content model. In many cases the content model
        are fine with this set to true as well, and it would be less lossy.
        example:
            say that name="level" and max-depth="2"
            and that this is the input:
            <level>
                <level>
                    <content/>
                    <div>
                        <level>
                            <content/>
                        </level>
                    </div>
                </level>
            </level>
            
            by default, this is the result:
            <level>
                <level>
                    <content/>
                </level>
                <level>
                    <content/>
                </level>
            </level>
            
            if this parameter is true, then the result will be:
            <level>
                <level>
                    <content/>
                </level>
                <level>
                    <div>
                        <content/>
                    </div>
                </level>
            </level>
    -->
    <xsl:param name="copy-wrapping-elements-into-result" select="'false'"/>

    <xsl:output indent="yes"/>

    <xsl:variable name="tokenized-names" select="tokenize($name,'\s+')"/>

    <xsl:template match="*[f:is-level(.)]">
        <xsl:variable name="level" select="count(ancestor::*[f:is-level(.)])+1"/>

        <!-- deep-levels can be resource intensive for big documents; so only call the deep-levels template if necessary -->
        <xsl:choose>
            <xsl:when test="$level &lt; $max-depth-int">
                <xsl:message select="concat('level is less than ',$max-depth-int,', will not unwrap; at: ',f:xpath-string(.))"/>
                <xsl:copy>
                    <xsl:apply-templates select="@*|node()"/>
                </xsl:copy>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message select="concat('level is more than or equal to ',$max-depth-int,', will unwrap; at: ',f:xpath-string(.))"/>
                <xsl:call-template name="deep-levels"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="deep-levels">
        <xsl:variable name="this" select="."/>
        <xsl:variable name="deep-levels" select=".//*[f:is-level(.)] | .//*[f:is-level(.)]/following-sibling::*[not(.//*[f:is-level(.)])][1][not(self::*[f:is-level(.)])]"/>
        <xsl:message select="concat('found ',count($deep-levels),' deep levels at: ',f:xpath-string(.))"/>
        <xsl:choose>

            <xsl:when test="not($deep-levels)">
                <!-- no deep levels -->
                <xsl:message select="concat('no deep levels: ',f:xpath-string(.))"/>
                <xsl:call-template name="make-level">
                    <xsl:with-param name="content" select="node()"/>
                    <xsl:with-param name="root-level" select="$this"/>
                </xsl:call-template>
            </xsl:when>

            <xsl:otherwise>

                <!-- content in start of level -->
                <xsl:variable name="this-level" select="."/>
                <xsl:variable name="next-level" select="(.//*[f:is-level(.)])[1]"/>
                <xsl:variable name="content" select="$this-level//node()[not(descendant-or-self::*[f:is-level(.)])][$next-level >> .]"/>
                <xsl:variable name="content" select="$content[not(./ancestor::node()=$content)]"/>
                <xsl:call-template name="make-level">
                    <xsl:with-param name="content" select="$content"/>
                    <xsl:with-param name="root-level" select="$this"/>
                </xsl:call-template>

                <xsl:for-each select="$deep-levels">
                    <xsl:choose>
                        <xsl:when test="self::*[f:is-level(.)] and not(.//*[f:is-level(.)])">
                            <!-- no deeper levels -->
                            <xsl:message select="concat('no deeper levels: ',f:xpath-string(.))"/>
                            <xsl:call-template name="make-level">
                                <xsl:with-param name="content" select="node()"/>
                                <xsl:with-param name="root-level" select="$this"/>
                            </xsl:call-template>
                        </xsl:when>

                        <xsl:when test="self::*[f:is-level(.)]">
                            <!-- content in start of sublevel -->
                            <xsl:message select="concat('content in start of sublevel: ',f:xpath-string(.))"/>
                            <xsl:variable name="this-level" select="."/>
                            <xsl:variable name="next-level" select="(.//*[f:is-level(.)])[1]"/>
                            <xsl:variable name="content" select="$this-level//node()[not(descendant-or-self::*[f:is-level(.)])][$next-level >> .]"/>
                            <xsl:variable name="content" select="$content[not(ancestor::node()=$content)]"/>
                            <xsl:call-template name="make-level">
                                <xsl:with-param name="content" select="$content"/>
                                <xsl:with-param name="root-level" select="$this"/>
                            </xsl:call-template>
                        </xsl:when>

                        <xsl:when test="following::*[f:is-level(.)] intersect ancestor::*[f:is-level(.)][1]//*[f:is-level(.)]">
                            <!-- content in the middle of a sublevel -->
                            <xsl:message select="concat('content in the middle of a sublevel: ',f:xpath-string(.))"/>
                            <xsl:variable name="content" select="(. | following::node()) intersect (. | following-sibling::*)//descendant-or-self::*[f:is-level(.)]/preceding::node()"/>
                            <xsl:variable name="content" select="$content[not(./ancestor::node()=$content)]"/>
                            <xsl:call-template name="make-level">
                                <xsl:with-param name="content" select="$content"/>
                                <xsl:with-param name="root-level" select="$this"/>
                            </xsl:call-template>
                        </xsl:when>

                        <xsl:otherwise>
                            <!-- content at the end of a sublevel -->
                            <xsl:message select="concat('content at the end of a sublevel: ',f:xpath-string(.))"/>
                            <xsl:variable name="content" select="(. | following::node()) intersect ancestor::*[f:is-level(.)][1]//node()"/>
                            <xsl:variable name="content" select="$content[not(./ancestor::node()=$content)]"/>
                            <xsl:call-template name="make-level">
                                <xsl:with-param name="content" select="$content"/>
                                <xsl:with-param name="root-level" select="$this"/>
                            </xsl:call-template>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="make-level">
        <xsl:param name="content" as="node()*"/>
        <xsl:param name="root-level" as="node()"/>
        <xsl:variable name="common-ancestor" select="if (xs:string($copy-wrapping-elements-into-result)='true') then $root-level else ($content[1]/ancestor::*[not($content except .//node())])[last()]"/>
        <xsl:element name="{local-name($root-level)}" namespace="{namespace-uri($root-level)}">
            <xsl:copy-of select="$content[1]/ancestor-or-self::*[f:is-level(.)]/@*"/>
            <xsl:for-each select="$common-ancestor/node()">
                <xsl:choose>
                    <xsl:when test=". = $content">
                        <xsl:copy-of select="."/>
                    </xsl:when>
                    <xsl:when test=".//node() intersect $content">
                        <xsl:choose>
                            <xsl:when test="self::*[f:is-level(.)]">
                                <xsl:call-template name="make-level-content">
                                    <xsl:with-param name="this" select="."/>
                                    <xsl:with-param name="content" select="$content"/>
                                </xsl:call-template>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:copy>
                                    <xsl:copy-of select="@*"/>
                                    <xsl:call-template name="make-level-content">
                                        <xsl:with-param name="this" select="."/>
                                        <xsl:with-param name="content" select="$content"/>
                                    </xsl:call-template>
                                </xsl:copy>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                </xsl:choose>
            </xsl:for-each>
        </xsl:element>
    </xsl:template>

    <xsl:template name="make-level-content">
        <xsl:param name="this" as="node()"/>
        <xsl:param name="content" as="node()*"/>
        <xsl:for-each select="$this/node()">
            <xsl:choose>
                <xsl:when test=". = $content">
                    <xsl:copy-of select="."/>
                </xsl:when>
                <xsl:when test=".//node() intersect $content">
                    <xsl:choose>
                        <xsl:when test="self::*[f:is-level(.)]">
                            <xsl:call-template name="make-level-content">
                                <xsl:with-param name="this" select="."/>
                                <xsl:with-param name="content" select="$content"/>
                            </xsl:call-template>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:copy>
                                <xsl:copy-of select="@*"/>
                                <xsl:call-template name="make-level-content">
                                    <xsl:with-param name="this" select="."/>
                                    <xsl:with-param name="content" select="$content"/>
                                </xsl:call-template>
                            </xsl:copy>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:when>
            </xsl:choose>
        </xsl:for-each>
    </xsl:template>

    <xsl:function name="f:is-level">
        <xsl:param name="context" as="node()"/>
        <xsl:sequence select="local-name($context) = $tokenized-names and namespace-uri($context) = $namespace"/>
    </xsl:function>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:function name="f:xpath-string" as="xs:string">
        <xsl:param name="node"/>
        <xsl:sequence
            select="concat('/',string-join(for $n in ($node/ancestor-or-self::node()[not(. intersect /)]) return concat(if ($n/self::*) then $n/name() else if ($n/self::text()) then 'text()' else if ($n/self::comment()) then 'comment()' else 'node()','[',1+count(if ($n/self::*) then $n/preceding-sibling::*[name()=$n/name()] else if ($n/self::text()) then $n/preceding-sibling::text() else if ($n/self::comment()) then $n/preceding-sibling::comment() else $n/preceding-sibling::node()),']'),'/'))"
        />
    </xsl:function>

</xsl:stylesheet>
