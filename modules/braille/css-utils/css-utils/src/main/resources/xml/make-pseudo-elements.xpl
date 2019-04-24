<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                type="css:make-pseudo-elements"
                exclude-inline-prefixes="#all"
                version="1.0">
    
    <p:documentation>
        Generate pseudo-elements.
    </p:documentation>
    
    <p:input port="source">
        <p:documentation>
            Pseudo-element rules in the input must be declared in css:before, css:after,
            css:duplicate, css:alternate and css:footnote-call attributes. Elements in the input
            that participate in a named flow must be identified with css:flow attributes.
        </p:documentation>
    </p:input>
    
    <p:output port="result">
        <p:documentation>
            For each element with a css:before attribute in the input, an empty css:before element
            will be inserted in the output as the element's first child. Similarly, for each element
            with a css:after attribute, a css:after element will be inserted as the element's last
            child. For each element with a css:alternate or css:alternate-N or css:footnote-call
            attribute in the input, an empty css:alternate or css:footnote-call element is inserted
            directly after the element. A css:footnote-call element is only generated when the main
            element participates in the 'footnotes' flow. For each element with a css:duplicate
            attribute in the input, a copy of the element is inserted directly after the element,
            but the style attribute and any css:* attributes are omitted. The css:before, css:after,
            css:footnote-call, css:duplicate and css:alternate attributes are moved to the inserted
            elements and renamed to 'style'. In the case of ::footnote-call, ::duplicate and
            ::alternate, the original element gets a css:id attribute, and the pseudo-element gets a
            matching css:anchor attribute.
        </p:documentation>
    </p:output>
    
    <p:xslt px:progress="1">
        <p:input port="stylesheet">
            <p:document href="make-pseudo-elements.xsl"/>
        </p:input>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
    </p:xslt>
    
</p:declare-step>
