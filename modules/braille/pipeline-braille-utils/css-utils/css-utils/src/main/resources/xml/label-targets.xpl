<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                type="css:label-targets"
                exclude-inline-prefixes="#all"
                version="1.0">
    
    <p:documentation>
        Label elements that are referenced somewhere else in the document through a target-text(),
        target-string() or target-counter() value.
    </p:documentation>
    
    <p:input port="source" sequence="true">
        <p:documentation>
            target-text(), target-string(), target-counter() and target-content() values must be
            represented with css:text, css:string, css:counter and css:content elements with a
            target attribute.
        </p:documentation>
    </p:input>
    
    <p:output port="result" sequence="true">
        <p:documentation>
            For each element that is referenced somewhere, a css:id attribute that matches the
            xml:id or id attribute of the element is added in the output. No two elements will get
            the same css:id attribute.
        </p:documentation>
    </p:output>
    
    <p:wrap-sequence wrapper="_"/>
    
    <p:xslt>
        <p:input port="stylesheet">
            <p:document href="label-targets.xsl"/>
        </p:input>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
    </p:xslt>
    
    <p:delete match="@css:_id_[not(string(.)=(//css:text|//css:string|//css:counter|//css:content)/@target/string())]"/>
    <p:rename match="@css:_id_" new-name="css:id"/>
    
    <p:filter select="/_/*"/>
    
</p:declare-step>
