<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                type="px:report-errors"
                name="report-errors">

    <p:input port="source" primary="true"/>
    <p:input port="report" sequence="true"/>
    <p:output port="result" sequence="true"/>
    <p:option name="code" select="''"/>
    <p:option name="code-prefix"/>
    <p:option name="code-namespace"/>

    <!--We count the report docs to simply pipe the identity if there are no errors (used in the choose/when) -->
    <p:count name="count" limit="1">
        <p:input port="source">
            <p:pipe step="report-errors" port="report"/>
        </p:input>
    </p:count>
    <p:sink/>
    <!--We the repipe the primary source port-->
    <p:identity>
        <p:input port="source">
            <p:pipe port="source" step="report-errors"/>
        </p:input>
    </p:identity>
    <p:choose>
        <p:xpath-context>
            <p:pipe port="result" step="count"/>
        </p:xpath-context>
        <p:when test="/c:result = '0'">
            <p:identity/>
        </p:when>
        <p:when test="$code != '' and p:value-available('code-prefix') and p:value-available('code-namespace')">
            <cx:report-errors>
                <p:input port="report">
                    <p:pipe step="report-errors" port="report"/>
                </p:input>
                <p:with-option name="code" select="$code"/>
                <p:with-option name="code-prefix" select="$code-prefix"/>
                <p:with-option name="code-namespace" select="$code-namespace"/>
            </cx:report-errors>
        </p:when>
        <p:when test="$code != '' and p:value-available('code-namespace')">
            <cx:report-errors>
                <p:input port="report">
                    <p:pipe step="report-errors" port="report"/>
                </p:input>
                <p:with-option name="code" select="$code"/>
                <p:with-option name="code-namespace" select="$code-namespace"/>
            </cx:report-errors>
        </p:when>
        <p:when test="$code != '' and p:value-available('code-prefix')">
            <cx:report-errors>
                <p:input port="report">
                    <p:pipe step="report-errors" port="report"/>
                </p:input>
                <p:with-option name="code" select="$code"/>
                <p:with-option name="code-prefix" select="$code-prefix"/>
            </cx:report-errors>
        </p:when>
        <p:when test="$code != ''">
            <cx:report-errors>
                <p:input port="report">
                    <p:pipe step="report-errors" port="report"/>
                </p:input>
                <p:with-option name="code" select="$code"/>
            </cx:report-errors>
        </p:when>
        <p:otherwise>
            <cx:report-errors>
                <p:input port="report">
                    <p:pipe step="report-errors" port="report"/>
                </p:input>
            </cx:report-errors>
        </p:otherwise>
    </p:choose>

</p:declare-step>
