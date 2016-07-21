<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                type="css:make-anonymous-inline-boxes"
                exclude-inline-prefixes="#all"
                version="1.0">
    
    <p:documentation>
        Break inline boxes around contained block boxes and create anonymous inline boxes
        (http://snaekobbi.github.io/braille-css-spec/#anonymous-boxes).
    </p:documentation>
    
    <p:input port="source">
        <p:documentation>
            The input is assumed to be a tree-of-boxes representation of a document where boxes are
            represented by css:box elements. The parent of a box must be another box or a
            css:_ element.
        </p:documentation>
    </p:input>
    
    <p:output port="result">
        <p:documentation>
            Inline boxes that have descendant block, table or table-cell boxes are either unwrapped,
            or if the element has one or more css:* attributes or if it's the document element,
            renamed to css:_. For such elements, the inherited properties (specified in the
            element's style attribute) are moved to the next preserved descendant box, and 'inherit'
            values on the next preserved descendant box are concretized. css:_ elements are
            retained. All adjacent nodes that are not boxes or css:_ elements containing a box and
            that are not already contained in an inline box are wrapped into an anonymous one.
        </p:documentation>
    </p:output>
    
    <p:xslt>
        <p:input port="stylesheet">
            <p:document href="make-anonymous-inline-boxes.xsl"/>
        </p:input>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
    </p:xslt>
    
</p:declare-step>
