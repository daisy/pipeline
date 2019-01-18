<p:declare-step version="1.0"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                type="px:cx-eval-error">
    
    <p:output port="result"/>
    
    <p:declare-step type="cx:eval">
        <p:input port="source" sequence="true" primary="true"/>
        <p:input port="pipeline"/>
        <p:output port="result"/>
    </p:declare-step>
    
    <cx:eval>
        <p:input port="source">
            <p:empty/>
        </p:input>
        <p:input port="pipeline">
            <p:document href="xproc-error.xpl"/>
        </p:input>
    </cx:eval>
    
</p:declare-step>
