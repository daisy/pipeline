<p:declare-step type="px:compare" name="px-compare" version="1.0" xmlns:p="http://www.w3.org/ns/xproc" xmlns:px="http://www.daisy.org/ns/pipeline/xproc" xmlns:c="http://www.w3.org/ns/xproc-step">
    
    <!-- TODO: implement "ignore-ordering" option -->
    <!-- TODO: move to XProc testing utility module to avoid code duplication across modules -->
    
    <p:input port="source" primary="true" sequence="true"/>
    <p:input port="alternate" sequence="true"/>
    <p:output port="result" primary="true"/>
    
    <p:serialization port="result" indent="true"/>
    
    <p:option name="fail-if-not-equal" select="'false'"/>

    <p:wrap-sequence name="wrapped-source" wrapper="wrapper"/>
    <p:string-replace match="text()" replace="normalize-space()" name="source"/>

    <p:wrap-sequence name="wrapped-alternate" wrapper="wrapper">
        <p:input port="source">
            <p:pipe port="alternate" step="px-compare"/>
        </p:input>
    </p:wrap-sequence>
    <p:string-replace match="text()" replace="normalize-space()" name="alternate"/>

    <p:compare name="compare">
        <p:with-option name="fail-if-not-equal" select="$fail-if-not-equal"/>
        <p:input port="source">
            <p:pipe port="result" step="source"/>
        </p:input>
        <p:input port="alternate">
            <p:pipe port="result" step="alternate"/>
        </p:input>
    </p:compare>

    <p:add-attribute match="/*" attribute-name="result">
        <p:input port="source">
            <p:pipe port="result" step="compare"/>
        </p:input>
        <p:with-option name="attribute-value" select="/*">
            <p:pipe port="result" step="compare"/>
        </p:with-option>
    </p:add-attribute>
    <p:delete match="/*/node()" name="result"/>
    
    <p:choose>
        <p:when test="/*/@result='true'">
            <p:identity/>
        </p:when>
        <p:otherwise>
            <p:rename match="/*" new-name="c:was" name="was">
                <p:input port="source">
                    <p:pipe port="result" step="wrapped-source"/>
                </p:input>
            </p:rename>
            <p:rename match="/*" new-name="c:expected" name="expected">
                <p:input port="source">
                    <p:pipe port="result" step="wrapped-alternate"/>
                </p:input>
            </p:rename>
            <p:insert match="/*" position="last-child">
                <p:input port="source">
                    <p:pipe port="result" step="result"/>
                </p:input>
                <p:input port="insertion">
                    <p:pipe port="result" step="was"/>
                    <p:pipe port="result" step="expected"/>
                </p:input>
            </p:insert>
        </p:otherwise>
    </p:choose>

</p:declare-step>
