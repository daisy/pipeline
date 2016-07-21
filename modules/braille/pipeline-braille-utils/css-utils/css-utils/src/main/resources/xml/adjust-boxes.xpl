<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                type="css:adjust-boxes"
                exclude-inline-prefixes="#all"
                version="1.0">
    
    <p:documentation>
        Adjust the shape and position of boxes so that their content fits within their edges.
    </p:documentation>
    
    <p:input port="source">
        <p:documentation>
            The input is assumed to be a tree-of-boxes representation of a document where boxes are
            represented by css:box elements. The document root must be a box or a css:_ element. The
            parent of a box must be another box (or a css:_ element if it's the document
            root). Inline boxes must not have non-inline descendant or sibling boxes. Table-cell
            boxes must have a parent table box and table boxes must have only table-cell child
            boxes. All other nodes must have at least one inline box ancestor. Computed values of
            'margin-left', 'margin-right', 'padding-left', 'padding-right', 'border-left',
            'border-top', 'border-right', 'border-bottom' and 'text-indent' properties must be
            declared in css:margin-left, css:margin-right, css:padding-left, css:margin-right,
            css:border-left, css:border-top, css:border-right, css:border-bottom and css:text-indent
            attributes.
        </p:documentation>
    </p:input>
    
    <p:output port="result">
        <p:documentation>
            Block and table boxes are repositioned and reshaped in such a way that their content
            (including the first line box) does not overflow the left and right margin edges
            (i.e. the left and right content edges of the container box), and does not overflow the
            left and right border edges if a left or right border is present. While the edges of
            boxes may be adjusted, the text content and borders remain at their original position
            unless it would break the constraints above. Table-cell boxes are not reshaped.
        </p:documentation>
    </p:output>
    
    <p:xslt>
        <p:input port="stylesheet">
            <p:document href="adjust-boxes.xsl"/>
        </p:input>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
    </p:xslt>
    
</p:declare-step>
