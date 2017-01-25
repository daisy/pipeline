<p:declare-step version="1.0"
            xmlns:p="http://www.w3.org/ns/xproc"
            xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
            type="px:htmlize-catalog"
            name="main"
            exclude-inline-prefixes="#all">
    
    <p:input port="sources" sequence="true"/>
    <p:input port="parameters" kind="parameter"/>
    <p:option name="input-base-uri" required="true"/>
    <p:option name="output-base-uri" required="true"/>
    
    <p:for-each>
        <p:output port="result" primary="true">
            <p:pipe step="xslt" port="secondary"/>
        </p:output>
        <p:xslt name="xslt">
            <p:input port="stylesheet">
                <p:document href="htmlize-catalog.xsl"/>
            </p:input>
            <p:with-param name="input-base-uri" select="$input-base-uri"/>
            <p:with-param name="output-base-uri" select="$output-base-uri"/>
            <p:input port="parameters">
                <p:pipe step="main" port="parameters"/>
            </p:input>
        </p:xslt>
        <p:sink/>
    </p:for-each>
    
    <p:for-each>
        <p:store>
            <p:with-option name="href" select="p:base-uri()"/>
        </p:store>
    </p:for-each>
    
</p:declare-step>
