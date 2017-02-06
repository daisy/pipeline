<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                type="css:shift-id"
                exclude-inline-prefixes="#all"
                version="1.0">
    
    <p:documentation>
        Move css:id attributes to boxes.
    </p:documentation>
    
    <p:input port="source" sequence="true">
        <p:documentation>
            Boxes must be represented by css:box elements. target-counter() values must be
            represented by css:counter elements. If a document in the input sequence represents a
            named flow this must be indicated with a css:flow attribute on the document element.
        </p:documentation>
    </p:input>
    
    <p:output port="result" sequence="true">
        <p:documentation>
            For each non css:box element in the input that has a css:id attribute and is not a
            descendant of an inline box and not an inline box itself, the attribute is
            moved to the first following css:box element. If this css:box element already has a
            css:id attribute in the input, the attribute is not overwritten. Instead, any
            target-counter() values and any elements in named flows that reference the non css:box
            element are altered to reference the following css:box element.
        </p:documentation>
    </p:output>
    
    <p:wrap-sequence wrapper="_"/>
    
    <p:xslt>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="shift-id.xsl"/>
        </p:input>
    </p:xslt>
    
    <p:filter select="/_/*"/>
    
</p:declare-step>
