<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                type="css:parse-content"
                exclude-inline-prefixes="#all"
                version="1.0">
    
    <p:documentation>
        Insert generated content from 'content' properties.
    </p:documentation>
    
    <p:input port="source">
        <p:documentation>
            The 'content' properties of elements in the input must be declared in css:content
            attributes, and must conform to
            http://braillespecs.github.io/braille-css/#the-content-property. '::before' and
            '::after' and '::footnote-call' pseudo-elements must be represented by css:before,
            css:after and css:footnote-call elements. '::alternate' pseudo-elements must be
            reprented by css:alternate elements with a css:anchor attribute that points to the
            original element. Custom pseudo-elements must be represented by css:_* elements.
        </p:documentation>
    </p:input>
    
    <p:output port="result">
        <p:documentation>
            For each '::before', '::after', '::footnote-call' or '::alternate' pseudo-element in the
            input with a css:content attribute, the content list in that attribute is parsed, partly
            evaluated, and inserted in the output in place of the element's original content. String
            values and attr() values are evaluated to text. counter(), string(), target-counter(),
            target-text(), target-string(), target-content(), flow() and leader() values and custom
            function are inserted as css:counter, css:string, css:text, css:content, css:flow,
            css:leader and css:custom-func elements.
        </p:documentation>
    </p:output>
    
    <p:xslt>
        <p:input port="stylesheet">
            <p:document href="parse-content.xsl"/>
        </p:input>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
    </p:xslt>
    
</p:declare-step>
