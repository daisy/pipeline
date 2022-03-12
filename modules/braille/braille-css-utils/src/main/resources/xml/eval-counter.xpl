<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                type="css:eval-counter"
                exclude-inline-prefixes="#all">
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Evaluate counter() and target-counter() values according to
        http://braillespecs.github.io/braille-css/#h4_printing-counters-the-counter-function and
        http://braillespecs.github.io/braille-css/#h4_the-target-counter-function.</p>
    </p:documentation>
    
    <p:input port="source" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>counter() and target-counter() values in the input must be represented by css:counter
            elements. The 'counter-set', 'counter-reset' and 'counter-increment' properties of
            elements must be declared in css:counter-set, css:counter-reset and
            css:counter-increment attributes and must conform to
            http://braillespecs.github.io/braille-css/#h4_manipulating-counters-the-counter-increment-counter-set-and-counter-reset-properties.
            Elements that are referenced by a target-counter() value must be indicated with a css:id
            attribute that matches the css:counter element's target attribute. If a document in the
            input sequence represents a named flow this must be indicated with a css:flow attribute
            on the document element.</p>
        </p:documentation>
    </p:input>
    
    <p:option name="counters" select="'#all'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The 'counters' option must be a space separated list of counter names, or the word
            '#all'.</p>
        </p:documentation>
    </p:option>
    
    <p:option name="exclude-counters" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The 'exclude-counters' option must be a space separated list of counter names, or the
            empty string. The option is ignored if the 'counters' option is also specified (and has
            a value other than '#all').</p>
        </p:documentation>
    </p:option>
    
    <p:option name="counter-styles" select="map{}">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The 'counter-styles' option must be a map from counter style names to counter style
            definitions represented by <code>css:counter-style</code> elements.</p>
        </p:documentation>
    </p:option>
    
    <p:output port="result" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>For each css:counter element whose counter name is specified in the 'counters'
            option, or not specified in the 'exclude-counters' option if the 'counters' option is
            '#all', the counter value is computed and the counter representation for that value and
            the specified counter style is wrapped in an inline css:box element and inserted in
            place of the css:counter element. The css:box element has a css:text-transform attribute
            that represents the 'text-transform' descriptor of the counter style (default
            'auto'). If the css:counter element represents a target-counter() value, the css:box
            element has a css:anchor attribute with the target ID.</p>
        </p:documentation>
    </p:output>
    
    <p:import href="parse-counter-set.xpl">
        <p:documentation>
            css:parse-counter-set
        </p:documentation>
    </p:import>
    <cx:import href="braille-css.xsl" type="application/xslt+xml">
        <p:documentation>
            css:parse-counter-styles
        </p:documentation>
    </cx:import>
    
    <p:for-each px:progress=".5">
        <css:parse-counter-set>
            <p:with-option name="counters" select="$counters"/>
            <p:with-option name="exclude-counters" select="$exclude-counters"/>
        </css:parse-counter-set>
        <p:label-elements attribute="xml:id" replace="false" label="concat('__temp__',$p:index)" match="css:counter"/>
    </p:for-each>
    <p:identity name="input"/>
    
    <p:split-sequence test="/*[not(@css:flow[not(.='normal')])]"/>
    <p:wrap-sequence wrapper="_" name="context"/>
    
    <p:for-each name="result" px:progress=".5">
        <p:iteration-source>
            <p:pipe step="input" port="result"/>
        </p:iteration-source>
        <p:xslt>
            <p:input port="source">
                <p:pipe step="result" port="current"/>
                <p:pipe step="context" port="result"/>
            </p:input>
            <p:input port="stylesheet">
                <p:document href="eval-counter.xsl"/>
            </p:input>
            <p:with-param name="counter-names" select="$counters"/>
            <p:with-param name="exclude-counter-names" select="$exclude-counters"/>
            <!--
                the css:parse-counter-styles is only there for testing purposes (because we can not specify the styles as a map from XProcSpec)
            -->
            <p:with-param name="counter-styles" select="if ($counter-styles instance of map(xs:string,element(css:counter-style)))
                                                        then $counter-styles
                                                        else css:parse-counter-styles($counter-styles)"/>
        </p:xslt>
    </p:for-each>
    
</p:declare-step>
