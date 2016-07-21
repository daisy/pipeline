<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                type="css:make-table-grid"
                exclude-inline-prefixes="#all"
                version="1.0">
    
    <p:documentation>
        Indicate the groups and positions of table cells within tables.
    </p:documentation>
    
    <p:input port="source">
        <p:documentation>
            The input document must be valid HTML (namespace "http://www.w3.org/1999/xhtml") or
            DTBook ("http://www.daisy.org/z3986/2005/dtbook/"). The 'display' properties of elements
            in the input must be declared in css:display attributes, and must conform to
            http://snaekobbi.github.io/braille-css-spec/#the-display-property.
        </p:documentation>
    </p:input>
    
    <p:output port="result">
        <p:documentation>
            Each table element with a 'display' property of 'table' is marked with a css:table
            attribute. Its table cells are marked with a css:table-cell attribute. The table cell's
            group, position and dimensions are indicated with css:table-header-group,
            css:table-row-group, css:table-footer-group, css:table-row, css:table-column,
            css:table-row-span and css:table:column-span attributes. Table captions are marked with
            a css:table-caption attribute.
        </p:documentation>
    </p:output>
    
    <p:xslt>
        <p:input port="stylesheet">
            <p:document href="make-table-grid.xsl"/>
        </p:input>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
    </p:xslt>
    
</p:declare-step>
