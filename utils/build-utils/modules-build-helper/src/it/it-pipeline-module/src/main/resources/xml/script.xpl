<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:script" version="1.0">
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">Example script</h1>
        <p px:role="desc">Does stuff.</p>
    </p:documentation>
    
    <p:input port="source"/>
    
    <p:option name="option-1" required="true">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">Option 1</h1>
        <p px:role="desc">Enables something.

For more info see [link](http://example.org/more-info).</p>
      </p:documentation>
    </p:option>
    
    <p:option name="option-2" required="false" select="'xyz'">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">Option 2</h1>
      </p:documentation>
    </p:option>
    
    <p:output port="result"/>
    
    <p:xslt>
        <p:input port="stylesheet">
            <p:document href="foo.xsl"/>
        </p:input>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
    </p:xslt>
    
</p:declare-step>
