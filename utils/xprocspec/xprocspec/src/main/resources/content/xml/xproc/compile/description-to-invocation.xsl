<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" xmlns:p="http://www.w3.org/ns/xproc" xmlns:x="http://www.daisy.org/ns/xprocspec" xmlns:c="http://www.w3.org/ns/xproc-step">

    <xsl:output indent="yes" method="xml"/>
    <xsl:param name="test-base-uri" required="yes"/>
    <xsl:param name="name" required="yes"/>
    <xsl:param name="temp-dir" required="yes"/>

    <xsl:template match="/x:description">
        <p:declare-step xmlns:p="http://www.w3.org/ns/xproc" type="x:{$name}" name="main" xmlns:cx="http://xmlcalabash.com/ns/extensions" xmlns:c="http://www.w3.org/ns/xproc-step" version="1.0" xpath-version="2.0">
            <xsl:namespace name="x" select="'http://www.daisy.org/ns/xprocspec'"/>
            <xsl:text>
    </xsl:text>
            <p:output port="result"/>
            <xsl:text>
    </xsl:text>
            <p:import href="{resolve-uri(@script,base-uri(.))}"/>
            <xsl:text>
    </xsl:text>
            <p:variable name="start-time" select="adjust-dateTime-to-timezone(current-dateTime(),xs:dayTimeDuration('PT0H'))"/>
            <xsl:text>
    </xsl:text>
            <p:variable name="test-base-uri" select="'{$test-base-uri}'"/>
            <xsl:text>
    </xsl:text>
            <p:variable name="temp-dir" select="'{$temp-dir}'"/>
            <xsl:text>
    </xsl:text>
            
            <xsl:choose>
                <xsl:when test="x:scenario[@pending]">
                    <xsl:text>
    </xsl:text>
                    <xsl:comment select="'This test is marked as pending and will be skipped.'"/>
                    <p:identity name="result">
                        <xsl:text>
        </xsl:text>
                        <p:input port="source">
                            <xsl:text>
        </xsl:text>
                            <p:empty/>
                            <xsl:text>
        </xsl:text>
                        </p:input>
                        <xsl:text>
    </xsl:text>
                    </p:identity>
                    <xsl:text>

    </xsl:text>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:text>
    </xsl:text>
                    <p:try>
                        <xsl:text>
        </xsl:text>
                        <p:group>
                            <xsl:text>
        </xsl:text>
                            <xsl:element name="{replace(/*/x:step-declaration/*/@type,'.*\}','')}" namespace="{replace(/*/x:step-declaration/*/@x:type,'^\{(.*)\}.*$','$1')}">
                                <xsl:attribute name="name" select="'test'"/>
                                <xsl:for-each select="/*/x:scenario/x:call/x:option">
                                    <xsl:text>
        </xsl:text>
                                    <p:with-option name="{@name}" select="{@select}">
                                        <xsl:copy-of select="namespace::*"/>
                                        <p:inline>
                                            <context xml:base="{$test-base-uri}"/>
                                        </p:inline>
                                    </p:with-option>
                                    <!-- TODO: ability to set custom context for p:with-option ? -->
                                </xsl:for-each>

                                <xsl:variable name="parameter-ports" select="/*/x:step-declaration/*/p:input[@kind='parameter']"/>
                                <xsl:variable name="primary-parameter-port"
                                    select="if (count($parameter-ports)=1 and not($parameter-ports/@primary='false')) then $parameter-ports else if ($parameter-ports[@primary='true']) then $parameter-ports[@primary='true'] else if (count($parameter-ports[not(@primary='false')])=1) then $parameter-ports[not(@primary='false')] else ()"/>
                                <xsl:choose>
                                    <xsl:when test="$primary-parameter-port and not(/*/x:scenario/x:call/x:param)">
                                        <p:input port="{$primary-parameter-port/@port}">
                                            <xsl:choose>
                                                <xsl:when test="$primary-parameter-port/*">
                                                    <xsl:copy-of select="$primary-parameter-port/*"/>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <p:inline>
                                                        <c:param-set/>
                                                    </p:inline>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </p:input>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:for-each select="/*/x:scenario/x:call/x:param">
                                            <xsl:variable name="parameter-port" select="(@port,$primary-parameter-port/@port)[1]"/>
                                            <xsl:text>
        </xsl:text>
                                            <p:with-param name="{@name}" select="{@select}" port="{$parameter-port}">
                                                <p:inline>
                                                    <context xml:base="{$test-base-uri}"/>
                                                </p:inline>
                                            </p:with-param>
                                            <!-- TODO: ability to set custom context for p:with-param ? -->
                                        </xsl:for-each>
                                    </xsl:otherwise>
                                </xsl:choose>

                                <xsl:variable name="non-parameter-ports" select="/*/x:step-declaration/*/p:input[not(@kind='parameter')]"/>
                                <xsl:variable name="document" select="/*"/>
                                <xsl:for-each select="distinct-values($non-parameter-ports/((@kind,'document')[1]))">
                                    <xsl:variable name="kind" select="."/>
                                    <xsl:variable name="kind-ports" select="$non-parameter-ports[@kind=$kind or not(@kind) and $kind='document']"/>
                                    <xsl:variable name="primary-port"
                                        select="if (count($kind-ports)=1 and not($kind-ports/@primary='false')) then $kind-ports else if ($kind-ports[@primary='true']) then $kind-ports[@primary='true'] else if (count($kind-ports[not(@primary='false')])=1) then $kind-ports[not(@primary='false')] else ()"/>
                                    <xsl:if test="$primary-port and not($document/x:scenario/x:call/x:input[@port=$primary-port/@port])">
                                        <xsl:for-each select="$primary-port">
                                            <xsl:copy>
                                                <xsl:copy-of select="@port|@select"/>
                                                <!--<xsl:attribute name="kind" select="(@kind,'document')[1]"/>-->
                                                <!--<xsl:attribute name="primary" select="'true'"/>-->
                                                <xsl:copy-of select="node()"/>
                                            </xsl:copy>
                                        </xsl:for-each>
                                    </xsl:if>
                                </xsl:for-each>

                                <xsl:for-each select="/*/x:scenario/x:call/x:input">
                                    <xsl:variable name="port" select="@port"/>
                                    <xsl:variable name="declaration" select="/*/x:step-declaration/*/p:input[@port=$port]"/>
                                    <xsl:variable name="kind" select="$declaration/(@kind,'document')[1]"/>
                                    <xsl:variable name="kind-ports" select="/*/x:step-declaration/*/p:input[@kind=$kind]"/>
                                    <xsl:variable name="primary" select="$declaration/@primary='true' or not($declaration/@primary='false') and count(/*/x:step-declaration/*/p:input[(@kind,'document')[1]=$kind])=1"/>

                                    <xsl:text>
        </xsl:text>
                                    <p:input port="{@port}">
                                        <xsl:copy-of select="@select">
                                            <!-- TODO: @select won't resolve namespaces correctly in this dynamically evaluated xpath context -->
                                        </xsl:copy-of>

                                        <xsl:if test="not(x:document)">
                                            <p:empty/>
                                        </xsl:if>
                                        <xsl:for-each select="x:document">
                                            <xsl:choose>
                                                <xsl:when test="*">
                                                    <p:inline>
                                                        <xsl:copy-of select="@xml:base"/>
                                                        <xsl:copy-of select="*"/>
                                                    </p:inline>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <p:document href="{resolve-uri(@href,base-uri(.))}"/>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:for-each>
                                    </p:input>
                                </xsl:for-each>
                            </xsl:element>
                            <xsl:text>
        </xsl:text>
                            <xsl:for-each select="/*/x:step-declaration/*/p:output">
                                <xsl:text>
        </xsl:text>
                                <p:for-each>
                                    <xsl:text>
        </xsl:text>
                                    <p:iteration-source>
                                        <xsl:text>
        </xsl:text>
                                        <p:pipe port="{@port}" step="test"/>
                                        <xsl:text>
        </xsl:text>
                                    </p:iteration-source>
                                    <xsl:text>
        </xsl:text>
                                    <p:variable name="base-uri" select="base-uri(/*)"/>
                                    <xsl:text>
        </xsl:text>
                                    <p:wrap-sequence wrapper="x:document"/>
                                    <xsl:text>
        </xsl:text>
                                    <p:add-attribute match="/*" attribute-name="xml:base">
                                        <xsl:text>
        </xsl:text>
                                        <p:with-option name="attribute-value" select="$base-uri"/>
                                        <xsl:text>
        </xsl:text>
                                    </p:add-attribute>
                                    <xsl:text>
        </xsl:text>
                                    <p:add-attribute match="/*" attribute-name="type" attribute-value="inline"/>
                                    <xsl:text>
        </xsl:text>
                                </p:for-each>
                                <xsl:text>
        </xsl:text>
                                <p:wrap-sequence wrapper="x:output"/>
                                <xsl:text>
        </xsl:text>
                                <p:add-attribute match="/*" attribute-name="port" attribute-value="{@port}"/>
                                <xsl:text>
        </xsl:text>
                                <p:identity name="output.{@port}"/>
                                <xsl:text>
        </xsl:text>
                                <p:sink/>
                            </xsl:for-each>
                            <xsl:text>

        </xsl:text>
                            <p:wrap-sequence wrapper="try-catch-wrapper">
                                <xsl:text>
        </xsl:text>
                                <p:input port="source">
                                    <xsl:choose>
                                        <xsl:when test="/*/x:step-declaration/*/p:output">
                                            <xsl:for-each select="/*/x:step-declaration/*/p:output">
                                                <xsl:text>
        </xsl:text>
                                                <p:pipe port="result" step="output.{@port}"/>
                                            </xsl:for-each>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:text>
        </xsl:text>
                                            <p:empty/>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                    <xsl:text>
        </xsl:text>
                                </p:input>
                                <xsl:text>
        </xsl:text>
                            </p:wrap-sequence>
                            <xsl:text>
        </xsl:text>
                        </p:group>
                        <xsl:text>
        </xsl:text>
                        <p:catch name="catch">
                            <xsl:text>
        </xsl:text>
                            <p:identity>
                                <xsl:text>
            </xsl:text>
                                <p:input port="source">
                                    <xsl:text>
                </xsl:text>
                                    <p:pipe step="catch" port="error"/>
                                    <xsl:text>
            </xsl:text>
                                </p:input>
                                <xsl:text>
        </xsl:text>
                            </p:identity>
                            <xsl:text>
        </xsl:text>
                            <p:add-attribute match="/*" attribute-name="test-base-uri">
                                <xsl:text>
            </xsl:text>
                                <p:with-option name="attribute-value" select="$test-base-uri"/>
                                <xsl:text>
        </xsl:text>
                            </p:add-attribute>
                            <xsl:text>
        </xsl:text>
                            <p:add-attribute match="/*" attribute-name="error-location" attribute-value="running the script"/>
                            <xsl:text>
        </xsl:text>
                            <p:wrap-sequence wrapper="try-catch-wrapper"/>
                            <xsl:text>
        </xsl:text>
                        </p:catch>
                        <xsl:text>
    </xsl:text>
                    </p:try>
                    <xsl:text>
    </xsl:text>
                    <p:for-each>
                        <xsl:text>
        </xsl:text>
                        <p:iteration-source select="/try-catch-wrapper/*"/>
                        <xsl:text>
        </xsl:text>
                        <p:identity/>
                        <xsl:text>
    </xsl:text>
                    </p:for-each>
                    <xsl:text>
    </xsl:text>
                    <p:identity name="result"/>
                    <xsl:text>
    </xsl:text>
                </xsl:otherwise>
            </xsl:choose>


            <xsl:text>
    </xsl:text>
            <p:insert match="/*" position="last-child">
                <xsl:text>
        </xsl:text>
                <p:input port="source">
                    <xsl:text>
        </xsl:text>
                    <p:inline>
                        <xsl:for-each select="/*">
                            <xsl:copy>
                                <xsl:copy-of select="@*"/>
                                <xsl:copy-of select="node()"/>
                            </xsl:copy>
                        </xsl:for-each>
                        <xsl:text>
        </xsl:text>
                    </p:inline>
                    <xsl:text>
        </xsl:text>
                </p:input>
                <xsl:text>
        </xsl:text>
                <p:input port="insertion">
                    <xsl:text>
        </xsl:text>
                    <p:pipe step="result" port="result"/>
                    <xsl:text>
        </xsl:text>
                </p:input>
                <xsl:text>
    </xsl:text>
            </p:insert>
            <xsl:text>
    </xsl:text>
            <p:add-attribute match="/x:description/x:scenario" attribute-name="start-time">
                <xsl:text>
        </xsl:text>
                <p:with-option name="attribute-value" select="$start-time">
                    <xsl:text>
        </xsl:text>
                    <p:empty/>
                    <xsl:text>
        </xsl:text>
                </p:with-option>
                <xsl:text>
    </xsl:text>
            </p:add-attribute>
            <xsl:text>
    </xsl:text>
            <p:add-attribute match="/x:description/x:scenario" attribute-name="end-time">
                <xsl:text>
        </xsl:text>
                <p:with-option name="attribute-value" select="adjust-dateTime-to-timezone(current-dateTime(),xs:dayTimeDuration('PT0H'))">
                    <xsl:text>
        </xsl:text>
                    <p:empty/>
                    <xsl:text>
        </xsl:text>
                </p:with-option>
                <xsl:text>
    </xsl:text>
            </p:add-attribute>
            <xsl:text>
</xsl:text>
        </p:declare-step>
    </xsl:template>

</xsl:stylesheet>
