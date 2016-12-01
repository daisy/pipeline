<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" type="pxi:test-report" name="main" xmlns:cx="http://xmlcalabash.com/ns/extensions" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:pxi="http://www.daisy.org/ns/xprocspec/xproc-internal/"
    exclude-inline-prefixes="#all" version="1.0" xpath-version="2.0" xmlns:x="http://www.daisy.org/ns/xprocspec" xmlns:html="http://www.w3.org/1999/xhtml">

    <p:documentation>Makes the machine-readable reports human-readable.</p:documentation>

    <p:input port="source" sequence="true"/>
    <p:output port="result" primary="true" sequence="true">
        <p:pipe port="result" step="result"/>
    </p:output>
    <p:output port="junit">
        <p:pipe port="result" step="junit"/>
    </p:output>
    <p:output port="html">
        <p:pipe port="result" step="html"/>
    </p:output>

    <p:option name="start-time" required="true"/>
    <p:option name="end-time" required="true"/>
    <p:option name="logfile" select="''"/>
    
    <p:option name="step-available-rng" select="'false'"/>

    <p:import href="../utils/logging-library.xpl"/>
    <p:import href="../utils/validate-with-relax-ng.xpl"/>
    
    <p:try>
        <p:group>
            <p:load>
                <p:with-option name="href" select="$logfile">
                    <p:empty/>
                </p:with-option>
            </p:load>
        </p:group>
        <p:catch>
            <p:identity>
                <p:input port="source">
                    <p:empty/>
                </p:input>
            </p:identity>
        </p:catch>
    </p:try>
    <p:identity name="logfile"/>
    
    <p:insert match="/html:html/html:body" position="last-child">
        <p:input port="source">
            <p:document href="report-template.xhtml"/>
        </p:input>
        <p:input port="insertion">
            <p:pipe port="source" step="main"/>
            <p:pipe port="result" step="logfile"/>
        </p:input>
    </p:insert>
    <p:viewport match="/*//*[self::c:error | self::x:expected | self::x:was | self::x:document]">
        <p:xslt>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
            <p:input port="stylesheet">
                <p:document href="pretty-print.xsl"/>
            </p:input>
        </p:xslt>
    </p:viewport>
    <p:xslt>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="report-to-html.xsl"/>
        </p:input>
    </p:xslt>
    <p:identity name="html"/>

    <p:wrap-sequence wrapper="x:test-report">
        <p:input port="source">
            <p:pipe port="source" step="main"/>
        </p:input>
    </p:wrap-sequence>

    <!-- validate output grammar -->
    <p:for-each>
        <p:identity name="try.input"/>
        <p:try>
            <p:group>
                <pxi:validate-with-relax-ng>
                    <p:input port="schema">
                        <p:document href="../../schema/xprocspec.results.rng"/>
                    </p:input>
                    <p:with-option name="step-available" select="$step-available-rng">
                        <p:empty/>
                    </p:with-option>
                </pxi:validate-with-relax-ng>
                <p:wrap-sequence wrapper="calabash-issue-102"/>
            </p:group>
            <p:catch name="catch">
                <p:identity>
                    <p:input port="source">
                        <p:pipe port="error" step="catch"/>
                    </p:input>
                </p:identity>
                <p:add-attribute match="/*" attribute-name="error-location" attribute-value="report.xpl - validation of output grammar"/>

                <p:identity name="errors-without-was"/>
                <p:wrap-sequence wrapper="x:was">
                    <p:input port="source">
                        <p:pipe port="result" step="try.input"/>
                    </p:input>
                </p:wrap-sequence>
                <p:add-attribute match="/*" attribute-name="xml:base">
                    <p:with-option name="attribute-value" select="base-uri(/*/*)"/>
                </p:add-attribute>
                <p:wrap-sequence wrapper="c:error"/>
                <p:add-attribute match="/*" attribute-name="type" attribute-value="was"/>
                <p:identity name="was"/>
                <p:insert match="/*" position="last-child">
                    <p:input port="source">
                        <p:pipe port="result" step="errors-without-was"/>
                    </p:input>
                    <p:input port="insertion">
                        <p:pipe port="result" step="was"/>
                    </p:input>
                </p:insert>

                <p:wrap-sequence wrapper="x:test-report"/>
                <p:wrap-sequence wrapper="calabash-issue-102"/>
            </p:catch>
        </p:try>
        <p:for-each>
            <!-- temporary fix for https://github.com/ndw/xmlcalabash1/issues/102 -->
            <p:iteration-source select="/calabash-issue-102/*"/>
            <p:identity/>
        </p:for-each>
    </p:for-each>
    <p:identity name="result"/>

    <p:for-each>
        <p:iteration-source>
            <p:pipe port="source" step="main"/>
        </p:iteration-source>
        <p:viewport match="x:expected|c:error">
            <p:escape-markup/>
        </p:viewport>
        <p:xslt>
            <p:with-param name="start-time" select="$start-time"/>
            <p:with-param name="end-time" select="$end-time"/>
            <p:input port="stylesheet">
                <p:document href="report-to-junit.xsl"/>
            </p:input>
        </p:xslt>
    </p:for-each>
    <p:wrap-sequence wrapper="testsuites"/>
    <p:add-attribute match="/*" attribute-name="time">
        <p:with-option name="attribute-value" select="(/*/*/@temp-global-duration)[1]"/>
    </p:add-attribute>
    <p:delete match="/*/*/@temp-global-duration"/>
    <!-- attribue @disabled is not used; don't know what it does compared to @skipped... -->
    <p:add-attribute match="/*" attribute-name="errors">
        <p:with-option name="attribute-value" select="sum(/*/*/number(@errors))"/>
    </p:add-attribute>
    <p:add-attribute match="/*" attribute-name="failures">
        <p:with-option name="attribute-value" select="sum(/*/*/number(@failures))"/>
    </p:add-attribute>
    <p:add-attribute match="/*" attribute-name="name">
        <p:with-option name="attribute-value" select="(/*/*/@temp-global-name)[1]"/>
    </p:add-attribute>
    <p:delete match="/*/*/@temp-global-name"/>
    <p:add-attribute match="/*" attribute-name="tests">
        <p:with-option name="attribute-value" select="sum(/*/*/number(@tests))"/>
    </p:add-attribute>
    <p:identity name="junit"/>

</p:declare-step>
