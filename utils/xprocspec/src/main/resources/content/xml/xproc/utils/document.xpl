<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step name="main" type="pxi:document" xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step" version="1.0"
    xmlns:pxi="http://www.daisy.org/ns/xprocspec/xproc-internal/" exclude-inline-prefixes="#all" xpath-version="2.0" xmlns:x="http://www.daisy.org/ns/xprocspec" xmlns:cx="http://xmlcalabash.com/ns/extensions">

    <p:input port="document" primary="true"/>
    <p:input port="description"/>
    <p:output port="result" sequence="true"/>
    <p:option name="logfile" select="''"/>

    <p:import href="../utils/load.xpl"/>
    <p:import href="../utils/recursive-directory-list.xpl"/>
    <p:import href="../utils/logging-library.xpl"/>

    <p:variable name="type" select="/*/@type"/>
    <p:variable name="select" select="/*/@select"/>

    <p:variable name="temp-dir" select="/*/@temp-dir">
        <p:pipe port="description" step="main"/>
    </p:variable>
    <p:variable name="base-uri" select="if (/*/@base-uri=('temp-dir')) then /*/@base-uri else ''"/>
    <p:variable name="base-dir" select="if ($base-uri='temp-dir') then $temp-dir else replace(base-uri(/*),'^(.*/)[^/]*$','$1')"/>

    <p:variable name="port" select="/*/@port"/>
    <p:variable name="href" select="resolve-uri(/*/@href, $base-dir)"/>
    <p:variable name="method" select="if (/*/@method=('xml','html','text','binary')) then /*/@method else 'xml'"/>
    <p:variable name="recursive" select="if (/*/@recursive=('true','false')) then /*/@recursive else 'false'"/>
    <p:variable name="ordered" select="/*/@ordered"/>

    <p:identity>
        <p:input port="source">
            <p:pipe step="main" port="description"/>
        </p:input>
    </p:identity>
    <p:choose>
        <p:when test="$type='port'">
            <p:variable name="position" select="if (/*/@position=('all','last') or matches(/*/@position,'\d+')) then /*/@position else 'all'">
                <p:pipe port="document" step="main"/>
            </p:variable>

            <p:choose>
                <p:when test="/x:description/(x:output, x:scenario/x:call/x:input)[@port=$port]">
                    <p:filter>
                        <p:with-option name="select" select="concat('/x:description/(x:output, x:scenario/x:call/x:input)[@port=&quot;',$port,'&quot;]/x:document')"/>
                    </p:filter>
                </p:when>
                <p:when test="/x:description/x:step-declaration/p:*/p:input[@port=$port]">
                    <p:filter>
                        <p:with-option name="select" select="concat('/x:description/x:step-declaration/p:*/p:input[@port=&quot;',$port,'&quot;]/p:inline')"/>
                    </p:filter>
                    <p:for-each>
                        <p:rename match="/*" new-name="x:document"/>
                        <p:add-attribute match="/*" attribute-name="type" attribute-value="document"/>
                        <p:add-attribute match="/*" attribute-name="port">
                            <p:with-option name="attribute-value" select="$port"/>
                        </p:add-attribute>
                    </p:for-each>
                </p:when>
                <p:otherwise>
                    <pxi:error code="XPS03" message="       * port not found: $1">
                        <p:with-option name="param1" select="$port"/>
                        <p:with-option name="logfile" select="$logfile"/>
                    </pxi:error>
                </p:otherwise>
            </p:choose>

            <pxi:assert test-count-min="1">
                <p:with-option name="message" select="concat('   * port not found: &quot;',$port,'&quot;')">
                    <p:empty/>
                </p:with-option>
                <p:with-option name="logfile" select="$logfile">
                    <p:empty/>
                </p:with-option>
            </pxi:assert>

            <p:choose>
                <p:xpath-context>
                    <p:inline>
                        <doc/>
                    </p:inline>
                </p:xpath-context>
                <p:when test="$position='all'">
                    <p:identity/>
                </p:when>
                <p:otherwise>
                    <p:split-sequence>
                        <p:with-option name="test" select="concat('position()=',(if ($position='last') then 'last()' else $position))">
                            <p:empty/>
                        </p:with-option>
                    </p:split-sequence>
                </p:otherwise>
            </p:choose>
            <p:for-each>
                <p:variable name="base" select="if (/*/@xml:base) then resolve-uri(/*/@xml:base,$base-dir) else base-uri(/*)"/>
                <p:delete match="/*/@*"/>
                <p:add-attribute match="/*" attribute-name="xml:base">
                    <p:with-option name="attribute-value" select="if (/*/*/@xml:base) then resolve-uri(/*/*/@xml:base,$base) else $base"/>
                </p:add-attribute>
            </p:for-each>
        </p:when>

        <p:when test="$type='file'">
            <p:identity>
                <p:input port="source">
                    <p:pipe port="document" step="main"/>
                </p:input>
            </p:identity>
            <p:group>
                <p:variable name="base" select="if (/*/@xml:base) then resolve-uri(/*/@xml:base,$base-dir) else base-uri(/*)"/>
                <pxi:load>
                    <p:with-option name="href" select="$href"/>
                    <p:with-option name="method" select="$method"/>
                    <p:with-option name="logfile" select="$logfile"/>
                </pxi:load>
                <p:choose>
                    <p:when test="/c:body and $method=('text','binary')">
                        <p:delete match="/*/@*"/>
                    </p:when>
                    <p:otherwise>
                        <p:identity/>
                    </p:otherwise>
                </p:choose>
                <p:wrap-sequence wrapper="x:document"/>
                <p:add-attribute match="/*" attribute-name="xml:base">
                    <p:with-option name="attribute-value" select="if (/*/*/@xml:base) then resolve-uri(/*/*/@xml:base,$base) else $base"/>
                </p:add-attribute>
            </p:group>
        </p:when>

        <p:when test="$type='directory'">
            <pxi:message message="    * listing directory$2: $1">
                <p:with-option name="param1" select="$href">
                    <p:empty/>
                </p:with-option>
                <p:with-option name="param2" select="if ($recursive='true') then ' recursively' else ''">
                    <p:empty/>
                </p:with-option>
                <p:with-option name="logfile" select="$logfile">
                    <p:empty/>
                </p:with-option>
            </pxi:message>
            <p:try>
                <p:group>
                    <pxi:directory-list>
                        <p:with-option name="path" select="$href"/>
                        <p:with-option name="depth" select="if ($recursive='true') then '-1' else '0'"/>
                    </pxi:directory-list>
                    <p:delete match="//*/@xml:base"/>
                    <p:wrap-sequence wrapper="x:document"/>
                    <p:add-attribute match="/*" attribute-name="xml:base">
                        <p:with-option name="attribute-value" select="$href"/>
                    </p:add-attribute>
                </p:group>
                <p:catch>
                    <pxi:message message="      * unable to read directory">
                        <p:with-option name="logfile" select="$logfile">
                            <p:empty/>
                        </p:with-option>
                    </pxi:message>
                    <p:identity>
                        <p:input port="source">
                            <p:empty/>
                        </p:input>
                    </p:identity>
                </p:catch>
            </p:try>
        </p:when>

        <p:when test="$type='errors'">
            <p:for-each>
                <p:iteration-source select="(/x:description/c:errors)[1]"/>
                <p:identity/>
            </p:for-each>
            <p:identity name="errors"/>
            <p:count name="errors.count"/>
            <p:choose>
                <p:when test=".='0'">
                    <p:identity>
                        <p:input port="source">
                            <p:pipe port="result" step="errors"/>
                        </p:input>
                    </p:identity>
                    <pxi:message message="    * no errors occured">
                        <p:with-option name="logfile" select="$logfile">
                            <p:empty/>
                        </p:with-option>
                    </pxi:message>
                </p:when>
                <p:otherwise>
                    <p:identity>
                        <p:input port="source">
                            <p:pipe port="result" step="errors"/>
                        </p:input>
                    </p:identity>

                    <p:group>
                        <p:variable name="base" select="base-uri(/*)"/>
                        <p:wrap-sequence wrapper="x:document"/>
                        <pxi:assert message="   * error document should contain errors">
                            <p:with-option name="test" select="count(/*/*) &gt; 0"/>
                            <p:with-option name="logfile" select="$logfile">
                                <p:empty/>
                            </p:with-option>
                        </pxi:assert>
                        <p:add-attribute match="/*" attribute-name="xml:base">
                            <p:with-option name="attribute-value" select="if (/*/*/@xml:base) then resolve-uri(/*/*/@xml:base,$base) else $base"/>
                        </p:add-attribute>
                    </p:group>
                </p:otherwise>
            </p:choose>
        </p:when>

        <p:when test="$type='zip'">
            <pxi:message message="    * listing zip file contents: $1">
                <p:with-option name="param1" select="$href">
                    <p:empty/>
                </p:with-option>
                <p:with-option name="logfile" select="$logfile">
                    <p:empty/>
                </p:with-option>
            </pxi:message>
            <p:try>
                <p:group>
                    <cx:unzip>
                        <p:with-option name="href" select="$href"/>
                    </cx:unzip>
                    <p:label-elements match="/c:zipfile" attribute="name" label="replace(@href,'^.*/([^/]*)$','$1')"/>
                    <p:delete match="c:directory"/>
                    <p:delete match="@*[not(namespace-uri()='' and local-name()='name')]"/>
                    <p:choose>
                        <p:when test="$ordered='true'">
                            <p:xslt>
                                <p:input port="stylesheet">
                                    <p:document href="sort-zip-entries.xsl"/>
                                </p:input>
                                <p:input port="parameters">
                                    <p:empty/>
                                </p:input>
                            </p:xslt>
                        </p:when>
                        <p:otherwise>
                            <p:identity/>
                        </p:otherwise>
                    </p:choose>
                    <p:wrap-sequence wrapper="x:document"/>
                    <p:add-attribute match="/*" attribute-name="xml:base">
                        <p:with-option name="attribute-value" select="$href"/>
                    </p:add-attribute>
                </p:group>
                <p:catch>
                    <pxi:message message="      * unable to read zip file">
                        <p:with-option name="logfile" select="$logfile">
                            <p:empty/>
                        </p:with-option>
                    </pxi:message>
                    <p:identity>
                        <p:input port="source">
                            <p:empty/>
                        </p:input>
                    </p:identity>
                </p:catch>
            </p:try>
        </p:when>

        <p:otherwise>
            <!-- default is 'inline' -->
            <p:identity>
                <p:input port="source">
                    <p:pipe port="document" step="main"/>
                </p:input>
            </p:identity>
        </p:otherwise>
    </p:choose>

    <p:for-each>
        <p:add-attribute match="/*" attribute-name="type" attribute-value="inline"/>
        <p:add-attribute match="/*" attribute-name="xml:space" attribute-value="preserve"/>

        <!-- Base URI cleanup -->
        <p:group>
            <p:choose>
                <p:when test="not(/*/@xml:base)">
                    <p:variable name="base-was" select="/*/*/@xml:base"/>

                    <!-- set x:document/@xml:base to either the xprocspec test document or to the temporary directory -->
                    <p:add-attribute match="/*" attribute-name="xml:base">
                        <p:with-option name="attribute-value" select="if ($base-uri='temp-dir') then $temp-dir else base-uri(/*)"/>
                    </p:add-attribute>

                    <!-- resolve inline document against x:document/@xml:base -->
                    <p:add-attribute match="/*/*" attribute-name="xml:base">
                        <p:with-option name="attribute-value" select="resolve-uri($base-was,/*/@xml:base)"/>
                    </p:add-attribute>

                    <!-- reset x:document/*/@xml:base to its original value, in case it was a relative URI -->
                    <p:add-attribute match="/*/*" attribute-name="xml:base">
                        <p:with-option name="attribute-value" select="$base-was"/>
                    </p:add-attribute>

                    <!-- delete explicit xml:base attribute if it was not present originally -->
                    <p:delete>
                        <p:with-option name="match" select="concat('/*[',(if ($base-was) then 'false()' else 'true()'),']/*/@xml:base')"/>
                    </p:delete>
                </p:when>
                <p:otherwise>
                    <p:identity/>
                </p:otherwise>
            </p:choose>
        </p:group>
    </p:for-each>

    <p:choose>
        <p:xpath-context>
            <p:empty/>
        </p:xpath-context>
        <p:when test="$select">
            <p:for-each name="select">
                <p:variable name="unfiltered-base" select="/*/@xml:base"/>
                <p:identity>
                    <p:input port="source">
                        <p:pipe port="document" step="main"/>
                    </p:input>
                </p:identity>
                <pxi:message message="select: $1">
                    <p:with-option name="param1" select="$select"/>
                </pxi:message>
                <p:xslt name="select.xslt">
                    <p:input port="parameters">
                        <p:empty/>
                    </p:input>
                    <p:input port="stylesheet">
                        <p:document href="document-to-select-xslt.xsl"/>
                    </p:input>
                </p:xslt>
                <p:for-each>
                    <p:iteration-source select="/*/*">
                        <p:pipe port="current" step="select"/>
                    </p:iteration-source>
                    <p:xslt>
                        <p:input port="parameters">
                            <p:empty/>
                        </p:input>
                        <p:input port="stylesheet">
                            <p:pipe port="result" step="select.xslt"/>
                        </p:input>
                    </p:xslt>
                    <p:add-attribute match="/*" attribute-name="type" attribute-value="inline"/>
                    <p:add-attribute match="/*" attribute-name="xml:space" attribute-value="preserve"/>
                    <p:add-attribute match="/*" attribute-name="xml:base">
                        <p:with-option name="attribute-value" select="resolve-uri(base-uri(/*),$unfiltered-base)"/>
                    </p:add-attribute>
                </p:for-each>
            </p:for-each>
        </p:when>
        <p:otherwise>
            <p:identity/>
        </p:otherwise>
    </p:choose>

</p:declare-step>
