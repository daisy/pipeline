<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:mock-script"
                version="1.0">
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">Example script</h1>
        <p px:role="desc">Does stuff.</p>
    </p:documentation>
    
    <p:input port="source">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">Source document</h1>
      </p:documentation>
    </p:input>
    
    <p:output port="result"/>
    
    <p:declare-step type="px:sleep">
        <p:input port="source"/>
        <p:output port="result"/>
        <p:option name="milliseconds" required="true"/>
    </p:declare-step>
    
    <px:sleep milliseconds="3000"/>
    
    <p:xslt>
        <p:input port="stylesheet">
            <p:document href="foo.xsl"/>
        </p:input>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
    </p:xslt>
    
</p:declare-step>
