<p:declare-step type="pxi:compare" name="main" version="1.0" xmlns:p="http://www.w3.org/ns/xproc" xmlns:pxi="http://www.daisy.org/ns/xprocspec/xproc-internal/" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:x="http://www.daisy.org/ns/xprocspec">

    <!-- TODO: implement "ignore-ordering" option -->

    <p:input port="source" primary="true" sequence="true"/>
    <p:input port="alternate" sequence="true"/>
    <p:output port="result" primary="true"/>
    <p:option name="normalize-space" select="'true'"/>
    
    <p:option name="logfile" select="''"/>

    <p:serialization port="result" indent="true"/>

    <p:option name="fail-if-not-equal" select="'false'"/>

    <p:wrap-sequence name="wrapped-source" wrapper="wrapper"/>
    <p:delete match="/*/*/@xml:space"/>
    <p:choose>
        <p:when test="$normalize-space='true'">
            <p:string-replace match="text()" replace="normalize-space(replace(.,'&#x00a0;',' '))"/>
        </p:when>
        <p:otherwise>
            <p:identity/>
        </p:otherwise>
    </p:choose>
    <p:identity name="source"/>

    <p:wrap-sequence name="wrapped-alternate" wrapper="wrapper">
        <p:input port="source">
            <p:pipe port="alternate" step="main"/>
        </p:input>
    </p:wrap-sequence>
    <p:delete match="/*/*/@xml:space"/>
    <p:choose>
        <p:when test="$normalize-space='true'">
            <p:string-replace match="text()" replace="normalize-space(replace(.,'&#x00a0;',' '))"/>
        </p:when>
        <p:otherwise>
            <p:identity/>
        </p:otherwise>
    </p:choose>
    <p:identity name="alternate"/>

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
            <p:rename match="/*" new-name="x:expected" name="expected">
                <p:input port="source">
                    <p:pipe port="result" step="wrapped-alternate"/>
                </p:input>
            </p:rename>
            <p:rename match="/*" new-name="x:was" name="was">
                <p:input port="source">
                    <p:pipe port="result" step="wrapped-source"/>
                </p:input>
            </p:rename>
            <p:insert match="/*" position="last-child">
                <p:input port="source">
                    <p:pipe port="result" step="result"/>
                </p:input>
                <p:input port="insertion">
                    <p:pipe port="result" step="expected"/>
                    <p:pipe port="result" step="was"/>
                </p:input>
            </p:insert>
            <p:add-attribute match="/*/*" attribute-name="xml:space" attribute-value="preserve"/>
        </p:otherwise>
    </p:choose>

</p:declare-step>
