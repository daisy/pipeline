<p:declare-step version="1.0"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:java-step-runtime-error">
    
    <p:output port="result"/>
    
    <p:declare-step type="px:java-step">
        <p:input port="source"/>
        <p:output port="result"/>
        <p:option name="throw-error" select="'false'"/>
    </p:declare-step>
    
    <px:java-step throw-error="true">
        <p:input port="source">
            <p:inline>
                <hello/>
            </p:inline>
        </p:input>
    </px:java-step>
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">java-step-runtime-error</h1>
        <p px:role="desc">java-step-runtime-error</p>
    </p:documentation>
    
</p:declare-step>
