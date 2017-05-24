<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                type="pxi:shift-obfl-marker"
                exclude-inline-prefixes="#all"
                version="1.0">
    
    <p:documentation>
        Move '-obfl-marker' declarations to inline boxes.
    </p:documentation>
    
    <p:input port="source" sequence="true">
        <p:documentation>
            Boxes must be represented by css:box elements. '-obfl-marker' properties must be
            declared in css:_obfl-marker attributes.
        </p:documentation>
    </p:input>
    
    <p:output port="result" sequence="true">
        <p:documentation>
            For each element in the input that is not an inline css:box and not a descendant of an
            inline css:box, if it has a css:_obfl-marker attribute it is moved to the first
            following inline css:box. If this css:box element already has a css:_obfl-marker
            attribute in the input, the '-obfl-marker' declarations are prepended to it.
        </p:documentation>
    </p:output>
    
    <!--
        implented in Java
    -->
    
</p:declare-step>
