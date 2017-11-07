<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                type="css:eval-target-content"
                exclude-inline-prefixes="#all"
                version="1.0">
    
    <p:documentation>
        Evaluate target-content() values.
    </p:documentation>
    
    <p:input port="source" sequence="true">
        <p:documentation>
            target-content() values in the input must be represented by css:content
            elements. Elements that are referenced by a target-content() value must be indicated
            with a css:id attribute that matches the css:content element's target
            attribute. Pseudo-elements must be represented by css:after, css:before, css:duplicate,
            css:alternate and css:footnote-call elements.
        </p:documentation>
    </p:input>
    
    <p:output port="result" sequence="true">
        <p:documentation>
            css:content elements are replaced by the child nodes of their target element (the
            element whose css:id attribute corresponds with the css:content element's target
            attribute). Elements get a css:anchor attribute that matches the xml:id attribute of the
            target element. Direct pseudo-elements of the target element are not copied.
        </p:documentation>
    </p:output>
    
    <p:wrap-sequence wrapper="_"/>
    
    <p:xslt>
        <p:input port="stylesheet">
            <p:document href="eval-target-content.xsl"/>
        </p:input>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
    </p:xslt>
    
    <p:filter select="/_/*"/>
    
</p:declare-step>
