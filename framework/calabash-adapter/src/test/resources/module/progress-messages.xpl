<p:declare-step version="1.0"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                type="px:progress-messages">
    
    <p:output port="result"/>
    
    <p:declare-step type="px:java-step">
        <p:input port="source"/>
        <p:output port="result"/>
        <p:option name="show-progress" select="'false'"/>
    </p:declare-step>
    
    <p:declare-step type="cx:eval">
        <p:input port="source" primary="true"/>
        <p:input port="pipeline"/>
        <p:output port="result"/>
    </p:declare-step>
    
    <p:identity>
        <p:input port="source">
            <p:inline>
                <foo/>
            </p:inline>
            <p:inline>
                <bar/>
            </p:inline>
        </p:input>
    </p:identity>
    
    <p:for-each px:message="px:progress-messages (1)" px:progress="1/2">
        <px:java-step show-progress="true" px:progress="1"/>
    </p:for-each>
    
    <p:wrap-sequence wrapper="_" px:progress="5%"/>
    
    <cx:eval px:message="px:progress-messages (2)" px:progress=".25">
        <p:input port="pipeline">
            <p:inline>
                <p:pipeline version="1.0" type="px:foo">
                    <p:identity px:message="px:foo (1)" px:progress=".5"/>
                    <p:identity px:message="px:foo (2)" px:progress=".5"/>
                </p:pipeline>
            </p:inline>
        </p:input>
    </cx:eval>
    
    <p:identity px:message="px:progress-messages (3)" px:progress=".1"/>
    
</p:declare-step>
