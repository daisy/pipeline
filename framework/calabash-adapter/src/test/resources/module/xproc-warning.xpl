<p:declare-step version="1.0"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:xproc-warning">
    
    <p:output port="result"/>
    
    <p:variable name="var" select="'world'"/>
    
    <p:identity px:message="Hello {upper-case($var)}!" px:message-severity="WARN">
        <p:input port="source">
            <p:inline>
                <foo/>
            </p:inline>
        </p:input>
    </p:identity>
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">xproc-warning</h1>
        <p px:role="desc">xproc-warning</p>
    </p:documentation>
    
</p:declare-step>
