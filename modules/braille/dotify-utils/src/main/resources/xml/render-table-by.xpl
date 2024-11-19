<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                type="pxi:render-table-by"
                exclude-inline-prefixes="#all"
                version="1.0">
    
    <p:documentation>
        Layout tables as lists.
    </p:documentation>
    
    <p:input port="source">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The input document must be valid HTML (namespace "http://www.w3.org/1999/xhtml") or
            DTBook ("http://www.daisy.org/z3986/2005/dtbook/"). The 'display', 'render-table-by',
            'table-header-policy' and 'flow' properties of elements in the input must be declared in
            css:display, css:render-table-by, css:table-header-policy and css:flow attributes. Style
            attributes must be simple declaration lists, except for the table element which may also
            have '::table-by()', '::list-item' and '::list-header' pseudo-elements. Properties with
            value 'inherit' are not allowed.</p>
        </p:documentation>
    </p:input>
    
    <p:output port="result">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Each table element with a 'display' property not equal to 'table' and with a
            'render-table-by' property is rendered according to the axes specified in the
            'render-table-by' property. The td and th elements are rearranged and put in a new tree
            structure consisting of css:table-by, css:list-item and css:list-header elements. tr,
            tbody, thead and tfoot elements are dropped. When a table header element is duplicated,
            descendent css:id attributes are excluded, as are elements that are not part of the
            normal flow. The style attributes of the css:table-by, css:list-item and css:list-header
            elements are determined by the '::table-by()', '::list-item' and '::list-header'
            pseudo-elements declared on the table element. Style attributes of th and td elements in
            the output are transformed (or added) in such a way that the computed style in the
            output equals the computed style of the element in the input. Similarly, xml:lang
            attributes are added to th and td elements.</p>
        </p:documentation>
    </p:output>
    
    <p:xslt px:progress="1">
        <p:input port="stylesheet">
            <p:document href="render-table-by.xsl"/>
        </p:input>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
    </p:xslt>
    
</p:declare-step>
