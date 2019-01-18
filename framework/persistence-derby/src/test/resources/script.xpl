<p:declare-step version="1.0"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:my-script">
    
    <p:output port="result"/>
    
    <p:identity>
        <p:input port="source">
            <p:inline><message>foobar</message></p:inline>
        </p:input>
    </p:identity>
    
    <!-- <p:error code="FOO"/> -->
    
</p:declare-step>
