<p:declare-step type="px:compare" name="px-compare" version="1.0"
    xmlns:p="http://www.w3.org/ns/xproc" xmlns:px="http://www.daisy.org/ns/pipeline/xproc">
    
    <p:input port="source" primary="true"/>
    <p:input port="alternate"/>
    <p:output port="result" primary="false">
        <p:pipe port="result" step="compare"/>
    </p:output>
    
    <p:option name="fail-if-not-equal" select="'false'"/>
    
    <p:string-replace match="text()" replace="normalize-space()" name="source"/>
    
    <p:string-replace match="text()" replace="normalize-space()" name="alternate">
        <p:input port="source">
            <p:pipe port="alternate" step="px-compare"/>
        </p:input>
    </p:string-replace>
    
    <p:compare name="compare">
        <p:with-option name="fail-if-not-equal" select="$fail-if-not-equal"/>
        <p:input port="source">
            <p:pipe port="result" step="source"/>
        </p:input>
        <p:input port="alternate">
            <p:pipe port="result" step="alternate"/>
        </p:input>
    </p:compare>
    
</p:declare-step>
