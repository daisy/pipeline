<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                type="css:parse-properties"
                exclude-inline-prefixes="#all"
                version="1.0">
    
    <p:documentation>
        Extract individual declarations from style sheets.
    </p:documentation>
    
    <p:input port="source">
        <p:documentation>
            Style sheets of elements must be declared in `style` attributes.
        </p:documentation>
    </p:input>
    
    <p:option name="properties" select="'#all'">
        <p:documentation>
            The 'properties' option must be a space separated list of property names, or the word
            '#all'.
        </p:documentation>
    </p:option>
    
    <p:output port="result">
        <p:documentation>
            Elements in the output will get a css:* attribute for each declaration in the element's
            style attribute whose property name is specified in the 'properties' option. For
            example, the declaration `text-indent: 1' becomes the attribute css:text-indent="1". Any
            attributes in the input with the same name will be overwritten. The property values
            'inherit' and 'initial' are concretized. Invalid declarations are dropped. Declarations
            whose property names are not specified in the 'properties' option are retained in the
            style attribute, which is dropped when empty. If the 'properties' option is '#all', all
            declarations are extracted.
        </p:documentation>
    </p:output>
    
    <p:xslt>
        <p:input port="stylesheet">
            <p:document href="parse-properties.xsl"/>
        </p:input>
        <p:with-param name="property-names" select="$properties"/>
    </p:xslt>
    
</p:declare-step>
