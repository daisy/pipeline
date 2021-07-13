<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                type="css:eval-target-content"
                exclude-inline-prefixes="#all"
                version="1.0">
    
    <p:documentation>
        Evaluate target-content() values.
    </p:documentation>
    
    <p:input port="source" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>target-content() values in the input must be represented by css:content
            elements. Elements that are referenced by a target-content() value must be indicated
            with a css:id attribute that matches the css:content element's target
            attribute. Pseudo-elements must be represented by css:after, css:before, css:duplicate,
            css:alternate and css:footnote-call elements.</p>
        </p:documentation>
    </p:input>
    
    <p:output port="result" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>css:content elements are replaced by the child nodes of their target element (the
            element whose css:id attribute corresponds with the css:content element's target
            attribute). Elements get a css:anchor attribute that matches the xml:id attribute of the
            target element. Direct pseudo-elements of the target element are not copied. Style
            attributes are added in the output in such a way that for each element, its computed
            style at the output is equal to its computed style in the input.</p>
        </p:documentation>
    </p:output>
    
    <p:wrap-sequence wrapper="_"/>
    
    <p:xslt px:progress="1">
        <p:input port="stylesheet">
            <p:document href="eval-target-content.xsl"/>
        </p:input>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
    </p:xslt>
    
    <p:filter select="/_/*"/>
    
</p:declare-step>
