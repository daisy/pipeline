<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:css-parse-content"
                exclude-inline-prefixes="#all">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Insert generated content from 'content' properties.</p>
    </p:documentation>

    <p:input port="source">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The 'content' properties of elements in the input must be declared in
            <code>css:content</code> attributes, and must conform to <a
            href="http://braillespecs.github.io/braille-css/#the-content-property"
            >http://braillespecs.github.io/braille-css/#the-content-property</a>. <code>attr()</code>
            values must have already been evaluated.</p>
        </p:documentation>
    </p:input>

    <p:output port="result">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>For each element in the input with a css:content attribute, the content list in that
            attribute is parsed and inserted in the output in place of the element's original
            content. String values are evaluated to text. counter(), string(), target-counter(),
            target-text(), target-string(), target-content(), flow() and leader() values and custom
            function are inserted as css:counter, css:string, css:text, css:content, css:flow,
            css:leader and css:custom-func elements.</p>
        </p:documentation>
    </p:output>

    <p:xslt px:progress="1">
        <p:input port="stylesheet">
            <p:document href="parse-content.xsl"/>
        </p:input>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
    </p:xslt>

</p:declare-step>
