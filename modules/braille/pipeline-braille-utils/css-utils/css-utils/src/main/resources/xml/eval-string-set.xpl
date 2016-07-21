<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                type="css:eval-string-set"
                exclude-inline-prefixes="#all"
                version="1.0">
    
    <p:documentation>
        Evaluate 'string-set' properties.
    </p:documentation>
    
    <p:input port="source">
        <p:documentation>
            The 'string-set' properties of elements in the input must be declared in css:string-set
            attributes, and must conform to
            http://snaekobbi.github.io/braille-css-spec/#the-string-set-property. If a 'string-set'
            property contains the value 'content()', content must not have been transformed yet and
            pseudo-elements must not have been generated yet.
        </p:documentation>
    </p:input>
    
    <p:output port="result">
        <p:documentation>
            Each css:string-set attribute is evaluated, so that in the output each pair in the
            'string-set' property consists of an identifier and a single string value.
        </p:documentation>
    </p:output>
    
    <p:xslt>
        <p:input port="stylesheet">
            <p:document href="eval-string-set.xsl"/>
        </p:input>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
    </p:xslt>
    
</p:declare-step>
