<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                type="css:shift-string-set"
                exclude-inline-prefixes="#all"
                version="1.0">
    
    <p:documentation>
        Move 'string-set' declarations to inline boxes.
    </p:documentation>
    
    <p:input port="source">
        <p:documentation>
            Boxes must be represented by css:box elements. All block boxes must have at least one
            descendant inline box and inline boxes must have no descendant block boxes. 'string-set'
            properties must be declared in css:string-set attributes, and must conform to
            http://snaekobbi.github.io/braille-css-spec/#the-string-set-property.
        </p:documentation>
    </p:input>
    
    <p:output port="result">
        <p:documentation>
            For each element in the input that has a css:string-set attribute and is not a
            descendant of an inline box and not an inline box itself, the attribute is moved to the
            first descendant or following inline box within the same block (which may be the element
            itself). If there is no such element, the attribute is placed on an empty css:_ element
            inserted as the last child of the last preceding inline box (in the same block). In the
            former case, if the attribute is moved to a css:box element that already has a
            css:string-set attribute in the input, the 'string-set' declarations are prepended to
            it.
        </p:documentation>
    </p:output>
    
    <!--
        Implemented in Java
    -->
    
</p:declare-step>
