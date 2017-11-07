<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                type="css:parse-stylesheet"
                exclude-inline-prefixes="#all"
                version="1.0">
    
    <p:documentation>
        Extract pseudo-element rules and at-rules from style sheets.
    </p:documentation>
    
    <p:input port="source">
        <p:documentation>
            Style sheets of elements in the input must be declared in style attributes, which must
            conform to http://braillespecs.github.io/braille-css/#style-attribute.
        </p:documentation>
    </p:input>
    
    <p:output port="result">
        <p:documentation>
            Elements in the output will get a css:* attribute for each pseudo-element rule or
            at-rule in the element's style attribute. Rules with the same keyword but different
            pseudo-classes are combined into a single attribute. For example, the rule `@page {
            size: 40 25 }' becomes the attribute css:page="size: 40 25", the rules `@volume {
            max-length: 100 } @volume:first { max-length: 50 }` become the attribute css:volume="{
            max-length: 100 } :first { max-length: 50 }", the rule `::before { content: '⠶' }'
            becomes the attribute css:before="content: '⠶'", and the rule `@text-transform foo
            {...}` becomes the attribute css:text-transform-foo="...". Any attributes in the input
            with the same name will be overwritten. Only properties defined on the element itself
            will be retained in the style attribute, as a simple declaration list. The style
            attribute is dropped when empty.
        </p:documentation>
    </p:output>
    
    <p:xslt>
        <p:input port="stylesheet">
            <p:document href="parse-stylesheet.xsl"/>
        </p:input>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
    </p:xslt>
    
</p:declare-step>
