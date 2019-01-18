<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                type="pxi:propagate-page-break"
                exclude-inline-prefixes="#all"
                version="1.0">
    
    <p:documentation>
        Propagate page breaking related properties.
    </p:documentation>
    
    <p:input port="source">
        <p:documentation>
            The input is assumed to be a tree-of-boxes representation of a document where boxes are
            represented by css:box elements. The document root must be a box or a css:_ element. The
            parent of a box must be another box (or a css:_ element if it's the document
            root). Inline boxes must have at least one non-inline box ancestor and must not have
            non-inline descendant or sibling boxes. All other nodes must have at least one inline
            box ancestor. The 'page-break' and 'volume-break' properties of block boxes must be
            declared in css:page-break-before, css:page-break-after, css:page-break-inside and
            css:volume-break-before attributes. 'page' properties must be declared in css:page
            attributes. 'counter-set' properties for the 'page' counter must be declared in
            css:counter-set-page attributes. 'volume' properties must be declared in css:volume
            attributes. <!-- TODO: handle css:counter-set-page? -->
        </p:documentation>
    </p:input>
    
    <p:output port="result">
        <p:documentation>
            A 'page-break-before' or 'volume-break-before' property is propagated to the closest
            ancestor-or-self block box with a preceding sibling, or if there is no such element, to
            the outermost ancestor-or-self block box. A 'page-break-after' or 'volume-break-after'
            property is propagated to the closest ancestor-or-self block box with a following
            sibling, or if there is no such element, moved to the outermost ancestor-or-self block
            box. A 'page-break-inside' property with value 'avoid' on a box with child block boxes
            is propagated to all its children, and all children except the last get a
            'page-break-after' property with value 'avoid'. In case of conflicting values for a
            certain property, 'left' and 'right' win from 'always', 'always' wins from 'avoid', and
            'avoid' wins from 'auto'. When 'left' and 'right' are combined, the value specified on
            the latest element in the document wins. In case of conflicting values between adjacent
            siblings, the same precedence rules apply. Forced page breaks of type 'auto-right' are
            introduced where needed to satisfy the 'page' properties. Forced page breaks of type
            'auto-always' are introduced where needed to satisfy the 'volume' properties. These
            forced page breaks are propagated also as described above.
        </p:documentation>
    </p:output>
    
    <p:label-elements match="css:box[some $page in @css:page satisfies
                                     preceding::css:box[@type='inline'][1][not((ancestor-or-self::*/@css:page)[last()][.=$page])]]"
                      attribute="css:start-page"
                      label="@css:page"/>
    <p:label-elements match="css:box[some $page in @css:page satisfies
                                     following::css:box[@type='inline'][1][not((ancestor-or-self::*/@css:page)[last()][.=$page])]]"
                      attribute="css:end-page"
                      label="@css:page"/>
    <p:label-elements match="css:box[some $volume in @css:volume satisfies
                                     preceding::css:box[@type='inline'][1][not((ancestor-or-self::*/@css:volume)[last()][.=$volume])]]"
                      attribute="css:start-volume"
                      label="@css:volume"/>
    <p:label-elements match="css:box[some $volume in @css:volume satisfies
                                     following::css:box[@type='inline'][1][not((ancestor-or-self::*/@css:volume)[last()][.=$volume])]]"
                      attribute="css:end-volume"
                      label="@css:volume"/>
    
    <p:identity px:progress=".10"/>
    <p:xslt px:progress=".80">
        <p:input port="stylesheet">
            <p:document href="propagate-page-break.xsl"/>
        </p:input>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
    </p:xslt>
    
    <!--
        In case of conflicting values between adjacent siblings, the same precedence rules apply.
    -->
    <p:delete match="@css:page-break-after[.='auto']"/>
    <p:delete match="@css:page-break-before[.='auto']"/>
    <p:delete match="@css:page-break-after[.='avoid' and parent::*/following-sibling::*[1]/@css:page-break-before=('always','right','auto-right','left')]"/>
    <p:delete match="@css:page-break-before[.='avoid' and parent::*/preceding-sibling::*[1]/@css:page-break-after=('always','right','auto-right','left')]"/>
    <p:delete match="@css:page-break-after[.=('always','right','left') and parent::*/following-sibling::*[1]/@css:page-break-before=('right','auto-right','left')]"/>
    <p:delete match="@css:page-break-before[.='always' and parent::*/preceding-sibling::*[1]/@css:page-break-after=('right','auto-right','left')]"/>
    <p:delete match="@css:page-break-before[.=('right','left') and parent::*/preceding-sibling::*[1]/@css:page-break-after='auto-right']"/>
    <p:delete match="@css:volume-break-after[.='auto']"/>
    <p:delete match="@css:volume-break-before[.='auto']"/>
    
</p:declare-step>
