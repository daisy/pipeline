<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="pxi:select-by-base"
    xmlns:p="http://www.w3.org/ns/xproc"
    xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
    exclude-inline-prefixes="#all"
    version="1.0">
    
    <p:input port="source" sequence="true" primary="true"/>
    <p:option name="base" required="true"/>
    <p:output port="matched" sequence="true" primary="true"/>
    <p:output port="not-matched" sequence="true">
        <p:pipe step="split-sequence" port="not-matched"/>
    </p:output>
    
    <p:split-sequence name="split-sequence">
        <p:with-option name="test" select="concat('base-uri(/*)=(&quot;', replace($base, '\s+', '&quot;,&quot;'), '&quot;)')">
            <p:empty/>
        </p:with-option>
    </p:split-sequence>
    
</p:declare-step>
