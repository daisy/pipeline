<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step" version="1.0" type="custom:count" xmlns:custom="http://www.example.org/ns/custom" name="main" xmlns:x="http://www.daisy.org/ns/xprocspec">

    <p:input port="context" sequence="true"/>
    <p:input port="expect" sequence="true"/>
    <p:input port="parameters" kind="parameter" primary="true"/>
    <p:output port="result" primary="true"/>

    <p:parameters name="parameters">
        <p:input port="parameters">
            <p:pipe port="parameters" step="main"/>
        </p:input>
    </p:parameters>

    <p:group>

        <!-- extract "to" and "from" integers from the "count" parameter in the custom namespace -->
        <p:variable name="value" select="/*/*[@name='count' and @namespace='http://www.example.org/ns/custom']/@value">
            <p:pipe port="result" step="parameters"/>
        </p:variable>
        <p:variable name="from" select="replace($value, '\[(.*),.*', '$1')">
            <p:empty/>
        </p:variable>
        <p:variable name="to" select="replace($value, '.*,(.*)\]$', '$1')">
            <p:empty/>
        </p:variable>

        <!-- count context documents -->
        <p:identity>
            <p:input port="source">
                <p:pipe port="context" step="main"/>
            </p:input>
        </p:identity>
        <p:count name="count"/>

        <!-- construct x:test-result with x:expected and x:was -->
        <p:identity>
            <p:input port="source">
                <p:inline>
                    <x:test-result>
                        <x:expected> </x:expected>
                        <x:was> </x:was>
                    </x:test-result>
                </p:inline>
            </p:input>
        </p:identity>
        <p:string-replace match="/*/x:expected/node()">
            <p:with-option name="replace" select="concat(&quot;'&quot;, replace($value,&quot;'&quot;,&quot;&amp;apos;&quot;), &quot;'&quot;)"/>
        </p:string-replace>
        <p:string-replace match="/*/x:was/node()">
            <p:with-option name="replace" select="/*">
                <p:pipe port="result" step="count"/>
            </p:with-option>
        </p:string-replace>

        <!-- check whether or not the test passed -->
        <p:choose>
            <p:xpath-context>
                <p:pipe port="result" step="count"/>
            </p:xpath-context>
            <p:when test="number(/*) &gt;= number($from) and number(/*) &lt;= number($to)">
                <p:add-attribute match="/*" attribute-name="result" attribute-value="passed"/>
            </p:when>
            <p:otherwise>
                <p:add-attribute match="/*" attribute-name="result" attribute-value="failed"/>
            </p:otherwise>
        </p:choose>

    </p:group>

</p:declare-step>
