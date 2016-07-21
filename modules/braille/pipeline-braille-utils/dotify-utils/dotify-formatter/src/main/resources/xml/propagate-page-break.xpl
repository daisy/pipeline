<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                type="pxi:propagate-page-break"
                exclude-inline-prefixes="#all"
                version="1.0">
    
    <p:documentation>
        Change, add and remove page-break properties so that they can be mapped one-to-one on OBFL
        properties.
    </p:documentation>
    
    <p:input port="source">
        <p:documentation>
            The input is assumed to be a tree-of-boxes representation of a document where boxes are
            represented by css:box elements. The document root must be a box or a css:_ element. The
            parent of a box must be another box (or a css:_ element if it's the document
            root). Inline boxes must have at least one non-inline box ancestor and must not have
            non-inline descendant or sibling boxes. All other nodes must have at least one inline
            box ancestor. The 'page-break' properties of block boxes must be declared in
            css:page-break-before, css:page-break-after and css:page-break-inside attributes.
        </p:documentation>
    </p:input>
    
    <p:output port="result">
        <p:documentation>
            A 'page-break-before' property with value 'left', 'right' or 'always' is propagated to
            the closest ancestor-or-self block box with a preceding sibling, or if there is no such
            element, to the outermost ancestor-or-self block box. A 'page-break-after' property with
            value 'avoid' is propagated to the closest ancestor-or-self block box with a following
            sibling. A 'page-break-before' property with value 'avoid' is converted into a
            'page-break-after' property on the preceding sibling of the closest ancestor-or-self
            block box with a preceding sibling. A 'page-break-after' property with value 'left',
            'right' or 'always' is converted into a 'page-break-before' property on the immediately
            following block box, or if there is no such element, moved to the outermost
            ancestor-or-self block box. A 'page-break-inside' property with value 'avoid' on a box
            with child block boxes is propagated to all its children, and all children except the
            last get a 'page-break-after' property with value 'avoid'. In case of conflicting values
            for a certain property, 'left' and 'right' win from 'always', 'always' wins from
            'avoid', and 'avoid' wins from 'auto'. When 'left' and 'right' are combined, the value
            specified on the latest element in the document wins. In case of conflicting values
            between adjacent siblings, the value 'always' takes precedence over 'avoid'.
        </p:documentation>
    </p:output>
    
    <p:xslt>
        <p:input port="stylesheet">
            <p:document href="propagate-page-break.xsl"/>
        </p:input>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
    </p:xslt>
    
    <!--
        In case of conflicting values between adjacent siblings, the value 'always' takes precedence
        over 'avoid'.
    -->
    <p:delete match="@css:page-break-after[.='avoid' and parent::*/following-sibling::*[1]/@css:page-break-before=('always','right','left')]"/>
    
</p:declare-step>
