<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                type="pxi:shift-obfl-marker"
                exclude-inline-prefixes="#all"
                version="1.0">
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Move '-obfl-marker' declarations to inline boxes.</p>
    </p:documentation>
    
    <p:input port="source">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Boxes must be represented by <code>css:box</code> elements. All block boxes must have
            at least one descendant inline box and inline boxes must have no descendant block
            boxes. '-obfl-marker' properties must be declared in <code>css:_obfl-marker</code>
            attributes.</p>
        </p:documentation>
    </p:input>
    
    <p:output port="result">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>For each element in the input that has a <code>css:_obfl-marker</code> attribute and
            is not a descendant of an inline box and not an inline box itself, the attribute is
            moved to the first descendant or following inline box within the same block (which may
            be the element itself). If there is no such element, the attribute is placed on an empty
            <code>css:_</code> element inserted as the last child of the last preceding inline box
            (in the same block). In the former case, if the attribute is moved to a
            <code>css:box</code> element that already has a <code>css:_obfl-marker</code> attribute
            in the input, the '-obfl-marker' declarations are prepended to it.</p>
        </p:documentation>
    </p:output>
    
    <!--
        Implemented in ../../java/org/daisy/pipeline/braille/dotify/calabash/impl/ShiftObflMarkerStep.java
    -->
    
</p:declare-step>
