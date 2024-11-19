<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                type="pxi:preserve-white-space"
                exclude-inline-prefixes="#all"
                version="1.0">
    
    <p:documentation>
        Identify pieces of text that contain preserved white space.
    </p:documentation>
    
    <p:input port="source">
        <p:documentation>
            The 'white-space' properties of elements in the input must be declared in
            css:white-space attributes, and must conform to
            http://braillespecs.github.io/braille-css/#the-white-space-property. target-text(),
            target-string(), target-counter() and target-content() values must be represented by
            css:text, css:string, css:counter and css:content elements.
        </p:documentation>
    </p:input>
    
    <p:output port="result">
        <p:documentation>
            Each text node whose parent element's white-space property has a computed value of
            'pre-wrap' is wrapped in a css:white-space element. For text nodes with a value of
            'pre-line' only sequences of segment breaks are wrapped in a css:white-space element.
            css:string, css:text, css:counter and css:content elements with a computed value of
            'white-space' not equal to 'normal' get a css:white-space attribute.
        </p:documentation>
    </p:output>
    
    <p:xslt px:progress="1">
        <p:input port="stylesheet">
            <p:document href="preserve-white-space.xsl"/>
        </p:input>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
    </p:xslt>
    
</p:declare-step>
