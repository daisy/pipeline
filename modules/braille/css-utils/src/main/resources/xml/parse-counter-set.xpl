<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                type="css:parse-counter-set"
                exclude-inline-prefixes="#all"
                version="1.0">
    
    <p:documentation>
        Extract individual counter manipulations from 'counter-set', 'counter-reset' and
        'counter-increment' properties.
    </p:documentation>
    
    <p:input port="source">
        <p:documentation>
            The 'counter-set', 'counter-reset' and 'counter-increment' properties of elements in the
            input must be declared in css:counter-set, css:counter-reset and css:counter-increment
            attributes and must conform to
            http://braillespecs.github.io/braille-css/#h4_manipulating-counters-the-counter-increment-counter-set-and-counter-reset-properties.
        </p:documentation>
    </p:input>
    
    <p:option name="counters" select="'#all'">
        <p:documentation>
            The 'counters' option must be a space separated list of counter names, or the word
            '#all'.
        </p:documentation>
    </p:option>
    
    <p:option name="exclude-counters" select="''">
        <p:documentation>
            The 'exclude-counters' option must be a space separated list of counter names, or the
            empty string. The option is ignored if the 'counters' option is also specified (and has
            a value other than '#all').
        </p:documentation>
    </p:option>
    
    <p:output port="result">
        <p:documentation>
            Elements in the output will get a css:counter-set-*, css:counter-reset-* or
            css:counter-increment-* attribute for each counter manipulation in the element's
            css:counter-set, css:counter-reset and css:counter-increment attributes whose counter
            name is specified in the 'counters' option, or not specified in the 'exclude-counters'
            option if the 'counters' option is '#all'. Other counter manipulations are retained in
            the css:counter-set, css:counter-reset and css:counter-increment attributes. For
            example, if the 'counters' option is 'chapter', the attribute
            css:counter-increment="part chapter" is split into the attributes
            css:counter-increment="part" and css:counter-increment-chapter="1". Any attributes in
            the input with the same name will be overwritten.
        </p:documentation>
    </p:output>
    
    <p:xslt px:progress="1">
        <p:input port="stylesheet">
            <p:document href="parse-counter-set.xsl"/>
        </p:input>
        <p:with-param name="counter-names" select="$counters"/>
        <p:with-param name="exclude-counter-names" select="$exclude-counters"/>
    </p:xslt>
        
</p:declare-step>
