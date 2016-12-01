<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:x="http://www.daisy.org/ns/pipeline/xproc/test" xmlns:f="http://www.daisy.org/ns/pipeline/xproc/test/internal-functions" xmlns:rng="http://relaxng.org/ns/structure/1.0"
    xmlns="http://www.w3.org/1999/xhtml" xmlns:a="http://relaxng.org/ns/compatibility/annotations/1.0" version="2.0">

    <!-- TODO: join documentation that is written across through multiple <attribute/>s with the same name -->

    <xsl:template match="/rng:grammar">
        <xsl:text>
</xsl:text>
        <section>
            <xsl:text>
</xsl:text>
            <h2>Definitions</h2>
            <xsl:apply-templates select="//rng:element[not(ancestor::rng:element)]"/>
            <xsl:text>
</xsl:text>
        </section>
    </xsl:template>

    <xsl:template match="text()[not(ancestor::pre or ancestor::code)]">
        <xsl:value-of select="normalize-space(.)"/>
    </xsl:template>

    <xsl:template name="element" match="rng:element[@name]">
        <xsl:variable name="element-name" select="@name"/>
        <xsl:variable name="attributes" select=".//rng:attribute[count(ancestor::rng:element)=1]" as="node()*"/>
        <xsl:variable name="attributes" as="node()*">
            <!-- only document each attribute once -->
            <xsl:for-each select="$attributes">
                <xsl:variable name="position" select="position()"/>
                <xsl:if test="not($attributes[position()&lt;$position]/@name = @name)">
                    <xsl:sequence select="."/>
                </xsl:if>
            </xsl:for-each>
        </xsl:variable>
        <xsl:variable name="attributes" as="node()*">
            <!-- sort attribute documentation alphabetically -->
            <xsl:for-each select="$attributes">
                <xsl:sort select="(@name,@ns,'~~~~~~~~~~~')[1]"/>
                <xsl:sequence select="."/>
            </xsl:for-each>
        </xsl:variable>
        <xsl:variable name="elements" select="f:list-possible-elements(.)" as="node()*"/>
        <xsl:text>
</xsl:text>
        <section id="the-{@name}-element">
            <xsl:text>
</xsl:text>
            <h2>The <code><xsl:value-of select="@name"/></code> element</h2>
            <dl class="element">
                <dt>Content model:</dt>
                <xsl:choose>
                    <xsl:when test="$elements">
                        <xsl:for-each select="$elements">
                            <dd>
                                <xsl:choose>
                                    <xsl:when test="@name">
                                        <a href="#the-{@name}-element">
                                            <code>
                                                <xsl:value-of select="@name"/>
                                            </code>
                                        </a>
                                        <xsl:choose>
                                            <xsl:when test="ancestor::rng:zeroOrMore or ancestor::rng:oneOrMore[ancestor::rng:choice]">
                                                <small title="zero or more">*</small>
                                            </xsl:when>
                                            <xsl:when test="ancestor::rng:oneOrMore">
                                                <small title="oen or more">+</small>
                                            </xsl:when>
                                            <xsl:when test="ancestor::rng:optional or ancestor::rng:choice">
                                                <small title="optional">?</small>
                                            </xsl:when>
                                        </xsl:choose>
                                    </xsl:when>
                                    <xsl:when test="@ns">
                                        <xsl:choose>
                                            <xsl:when test="ancestor::rng:zeroOrMore or ancestor::rng:oneOrMore[ancestor::rng:choice]">Zero or more elements from the <code><xsl:value-of select="@ns"/></code> namespace.</xsl:when>
                                            <xsl:when test="ancestor::rng:oneOrMore">One or more elements from the <code><xsl:value-of select="@ns"/></code> namespace.</xsl:when>
                                            <xsl:when test="ancestor::rng:optional or ancestor::rng:choice">One element from the <code><xsl:value-of select="@ns"/></code> namespace (optional).</xsl:when>
                                            <xsl:otherwise>One element from the <code><xsl:value-of select="@ns"/></code> namespace.</xsl:otherwise>
                                        </xsl:choose> Any element in the <code><xsl:value-of select="@ns"/></code> namespace. </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:choose>
                                            <xsl:when test="ancestor::rng:zeroOrMore or ancestor::rng:oneOrMore[ancestor::rng:choice]">Zero or more elements from any namespace.</xsl:when>
                                            <xsl:when test="ancestor::rng:oneOrMore">One or more elements from any namespace.</xsl:when>
                                            <xsl:when test="ancestor::rng:optional or ancestor::rng:choice">One element from any namespace. (optional)</xsl:when>
                                            <xsl:otherwise>One element from any namespace.</xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </dd>
                        </xsl:for-each>
                    </xsl:when>
                    <xsl:otherwise>
                        <dd>Empty.</dd>
                    </xsl:otherwise>
                </xsl:choose>
                <dt>Content attributes:</dt>
                <xsl:choose>
                    <xsl:when test="$attributes">
                        <xsl:for-each select="$attributes">
                            <dd>
                                <xsl:choose>
                                    <xsl:when test="@name">
                                        <a href="#attr-{$element-name}-{@name}">
                                            <code>
                                                <xsl:value-of select="@name"/>
                                            </code>
                                        </a>
                                        <xsl:if test="ancestor::rng:optional or ancestor::rng:zeroOrMore or ancestor::rng:choice">
                                            <small title="optional">?</small>
                                        </xsl:if>
                                        <xsl:choose>
                                            <xsl:when test="rng:data[@type]"> -- type: <xsl:value-of select="rng:data/@type"/>
                                            </xsl:when>
                                            <xsl:when test="rng:choice"> -- values: <xsl:for-each select="for $v in (rng:choice/(rng:value|rng:data)) return $v">
                                                    <xsl:if test="position()&gt;1">
                                                        <xsl:text>, </xsl:text>
                                                    </xsl:if>
                                                    <xsl:choose>
                                                        <xsl:when test="self::rng:value">
                                                            <code><xsl:value-of select="normalize-space(.)"/></code>
                                                        </xsl:when>
                                                        <xsl:when test="self::rng:data"> or any value of type <code><xsl:value-of select="@type"/></code>
                                                        </xsl:when>
                                                    </xsl:choose>
                                                </xsl:for-each>
                                            </xsl:when>
                                        </xsl:choose>
                                    </xsl:when>
                                    <xsl:when test="@ns">
                                        <xsl:choose>
                                            <xsl:when test="ancestor::rng:zeroOrMore or ancestor::rng:oneOrMore[ancestor::rng:choice]">Zero or more attributes from the <code><xsl:value-of select="@ns"/></code> namespace.</xsl:when>
                                            <xsl:when test="ancestor::rng:oneOrMore">One or more attributes from the <code><xsl:value-of select="@ns"/></code> namespace.</xsl:when>
                                            <xsl:when test="ancestor::rng:optional or ancestor::rng:choice">One attribute from the <code><xsl:value-of select="@ns"/></code> namespace (optional).</xsl:when>
                                            <xsl:otherwise>One attribute from the <code><xsl:value-of select="@ns"/></code> namespace.</xsl:otherwise>
                                        </xsl:choose> Any attribute in the <code><xsl:value-of select="@ns"/></code> namespace. </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:choose>
                                            <xsl:when test="ancestor::rng:zeroOrMore or ancestor::rng:oneOrMore[ancestor::rng:choice]">Zero or more attributes from any namespace.</xsl:when>
                                            <xsl:when test="ancestor::rng:oneOrMore">One or more attributes from any namespace.</xsl:when>
                                            <xsl:when test="ancestor::rng:optional or ancestor::rng:choice">One attribute from any namespace. (optional)</xsl:when>
                                            <xsl:otherwise>One attribute from any namespace.</xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </dd>
                        </xsl:for-each>
                    </xsl:when>
                    <xsl:otherwise>
                        <dd>None.</dd>
                    </xsl:otherwise>
                </xsl:choose>
            </dl>

            <xsl:choose>
                <xsl:when test="a:documentation/*">
                    <xsl:text>
</xsl:text>
                    <xsl:for-each select="a:documentation/node()">
                        <xsl:call-template name="html"/>
                    </xsl:for-each>
                </xsl:when>
                <xsl:when test="a:documentation">
                    <xsl:text>
</xsl:text>
                    <p>
                        <xsl:for-each select="a:documentation/node()">
                            <xsl:call-template name="html"/>
                        </xsl:for-each>
                    </p>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:text>
</xsl:text>
                    <p><em>TODO</em>: the element <code><xsl:value-of select="$element-name"/></code> has not been documented yet.</p>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:text>
</xsl:text>
            <xsl:for-each select="$attributes[@name]">
                <xsl:variable name="attribute-name" select="@name"/>
                <xsl:choose>
                    <xsl:when test="a:documentation/*">
                        <xsl:text>
</xsl:text>
                        <xsl:variable name="with-id">
                            <xsl:for-each select="a:documentation/node()">
                                <xsl:copy>
                                    <xsl:copy-of select="@*"/>
                                    <xsl:if test="self::* and not(preceding-sibling::*)">
                                        <xsl:attribute name="id" select="concat('attr-',$element-name,'-',$attribute-name)"/>
                                    </xsl:if>
                                    <xsl:copy-of select="node()"/>
                                </xsl:copy>
                            </xsl:for-each>
                        </xsl:variable>
                        <xsl:for-each select="$with-id">
                            <xsl:call-template name="html"/>
                        </xsl:for-each>
                    </xsl:when>
                    <xsl:when test="a:documentation">
                        <xsl:text>
</xsl:text>
                        <p id="attr-{$element-name}-{$attribute-name}">
                            <xsl:for-each select="a:documentation/node()">
                                <xsl:call-template name="html"/>
                            </xsl:for-each>
                        </p>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:text>
</xsl:text>
                        <p id="attr-{$element-name}-{$attribute-name}"><em>TODO</em>: the attribute <code><xsl:value-of select="$attribute-name"/></code> has not been documented yet.</p>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
            <xsl:text>
</xsl:text>
        </section>
        <xsl:text>
</xsl:text>
    </xsl:template>

    <xsl:function name="f:list-possible-elements" as="node()*">
        <xsl:param name="element"/>

        <xsl:for-each select="$element/rng:*">
            <xsl:choose>
                <xsl:when test="self::rng:element">
                    <xsl:variable name="name" select="@name"/>
                    <xsl:variable name="ns" select="@ns"/>
                    <xsl:if test="not(preceding-sibling::rng:element[(@name=$name  or  @name=() and $name=() and @ns=$ns  or  @name=() and $name=() and @ns=() and $ns=())])">
                        <xsl:sequence select="."/>
                    </xsl:if>
                </xsl:when>
                <xsl:when test="self::rng:zeroOrMore or self::rng:oneOrMore or self::rng:optional or self::rng:choice or self::rng:group or self::rng:interleave or self::rng:mixed">
                    <xsl:variable name="elements" select="f:list-possible-elements(.)"/>
                    <xsl:for-each select="$elements">
                        <xsl:variable name="position" select="position()"/>
                        <xsl:variable name="name" select="@name"/>
                        <xsl:variable name="ns" select="@ns"/>
                        <xsl:if test="not($elements[position()&lt;$position and (@name=$name  or  @name=() and $name=() and @ns=$ns  or  @name=() and $name=() and @ns=() and $ns=())])">
                            <xsl:sequence select="."/>
                        </xsl:if>
                    </xsl:for-each>
                </xsl:when>
                <xsl:otherwise/>
            </xsl:choose>
        </xsl:for-each>
    </xsl:function>

    <xsl:template name="html">
        <xsl:choose>
            <xsl:when test="self::text()">
                <xsl:for-each select="tokenize(.,'`')">
                    <xsl:choose>
                        <xsl:when test="not(position() mod 2)">
                            <code>
                                <xsl:value-of select="."/>
                            </code>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="."/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy>
                    <xsl:for-each select="@*|node()">
                        <xsl:call-template name="html"/>
                    </xsl:for-each>
                </xsl:copy>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
