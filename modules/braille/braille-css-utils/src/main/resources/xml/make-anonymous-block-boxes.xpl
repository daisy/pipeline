<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                type="css:make-anonymous-block-boxes"
                exclude-inline-prefixes="#all">
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Wrap inline boxes that have sibling block boxes in anonymous block boxes.
        (http://braillespecs.github.io/braille-css/#anonymous-boxes).</p>
    </p:documentation>
    
    <p:input port="source">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The input is assumed to be a tree-of-boxes representation of a document where boxes
            are represented by css:box elements. The document root and top-level elements in named
            flows must be boxes or css:_ elements. The parent of a box must be another box (or a
            css:_ element if it's the document root or a top-level element in a named flow). Inline
            boxes must not have non-inline descendant boxes. Table-cell boxes must have a parent
            table box and table boxes must have only table-cell child boxes. All other nodes must
            have at least one inline box ancestor. If the input represents a named flow this must be
            indicated with a css:flow attribute on the document element.</p>
        </p:documentation>
    </p:input>
    
    <p:output port="result">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Adjacent inline boxes with one or more sibling block or table boxes are grouped and
            wrapped in an anonymous block box. If all top-level boxes in the normal flow are inline
            they are wrapped in an anonymous block box too. Top-level elements in a named flow that
            are inline boxes are wrapped in an anonymous block box each and their css:anchor
            attributes are moved to the anonymous block. Top-level elements in a named flow that are
            css:_ elements are changed into anonymous block boxes.</p>
        </p:documentation>
    </p:output>
    
    <p:rename match="/css:_[@css:flow[not(.='normal')]]/css:_" new-name="css:_block-box_"/>
    <p:wrap match="/css:_[@css:flow[not(.='normal')]]/css:box[@type='inline']" wrapper="css:_block-box_"/>
    <p:add-attribute match="css:_block-box_" attribute-name="type" attribute-value="block"/>
    <p:label-elements match="css:_block-box_[not(@css:anchor) and child::*/@css:anchor]"
                      attribute="css:_anchor_" label="child::*/@css:anchor"/>
    <p:delete match="css:_block-box_[not(@css:anchor)]/*/@css:anchor"/>
    <p:rename match="css:_block-box_/@css:_anchor_" new-name="css:anchor"/>
    <p:rename match="css:_block-box_" new-name="css:box"/>
    
    <p:wrap match="css:box[@type='inline'][preceding-sibling::css:box[@type=('block','table')] or
                                           following-sibling::css:box[@type=('block','table')] or
                                           parent::css:_ or
                                           not(parent::*)]"
            group-adjacent="true()"
            wrapper="css:_block-box_"/>
    <p:add-attribute match="css:_block-box_" attribute-name="type" attribute-value="block"/>
    <p:rename match="css:_block-box_" new-name="css:box"/>
    
</p:declare-step>
