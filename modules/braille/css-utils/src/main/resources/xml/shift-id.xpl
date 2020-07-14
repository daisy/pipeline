<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                type="css:shift-id"
                exclude-inline-prefixes="#all"
                version="1.0">
    
    <p:documentation>
        Move css:id attributes to inline boxes.
    </p:documentation>
    
    <p:input port="source" sequence="true">
        <p:documentation>
            Boxes must be represented by css:box elements. All block boxes must have at least one
            descendant inline box and inline boxes must have no descendant block
            boxes. target-counter() values must be represented by css:counter elements. If a
            document in the input sequence represents a named flow this must be indicated with a
            css:flow attribute on the document element.
        </p:documentation>
    </p:input>
    
    <p:output port="result" sequence="true">
        <p:documentation>
            For each element in the input that has a css:id attribute and is not a descendant of an
            inline box and not an inline box itself, the attribute is moved to the first descendant
            or following inline box within the same block (which may be the element itself). If
            there is no such element, the attribute is placed on an empty css:_ element inserted as
            the last child of the last preceding inline box (in the same block). In the former case,
            if the attribute is moved to a css:box element that already has a css:id attribute in
            the input, the attribute is not overwritten. Instead, any target-counter() values and
            any elements in named flows that reference the non css:box element are altered to
            reference the following css:box element.
        </p:documentation>
    </p:output>
    
    <!--
        implemented in Java
    -->
    
</p:declare-step>
