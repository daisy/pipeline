<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                name="main"
                type="css:split"
                exclude-inline-prefixes="#all"
                version="1.0">
    
    <p:documentation>
        Split a document into parts.
    </p:documentation>
    
    <p:input port="source">
        <p:documentation>
            The input is a single document.
        </p:documentation>
    </p:input>
    
    <p:option name="split-before" required="false" select="'/*'">
        <p:documentation>
            The 'split-before' option must be an XSLTMatchPattern that matches only elements.
        </p:documentation>
    </p:option>
    
    <p:option name="split-after" required="false" select="'/*'">
        <p:documentation>
            The 'split-after' option must be an XSLTMatchPattern that matches only elements.
        </p:documentation>
    </p:option>
    
    <p:output port="result" sequence="true">
        <p:documentation>
            The output is a sequence of one or more documents and is the result of splitting the
            input document before or after the split points specified with the 'split-before' and
            'split-after' options. Splitting before an element means duplicating the document,
            deleting the element and its following nodes from the first copy, and deleting the
            elements's preceding nodes from the second copy. Similarly, splitting after an element
            means deleting the element and its preceding nodes from the second copy and deleting the
            element's following nodes from the first copy. css:box elements that are split get a
            part attribute with value 'first', 'middle' or 'last'. The attributes css:id,
            css:string-set, css:counter-set, css:counter-reset, css:counter-increment,
            css:counter-set-*, css:counter-reset-* and css:counter-increment-* are omitted on
            css:box elements with a part attribute equal to 'middle' or 'last'.
        </p:documentation>
    </p:output>
    
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
        <p:documentation>
            px:assert
            px:xml-chunker
        </p:documentation>
    </p:import>
    
    <px:assert error-code="XXX" message="Input may not have part attributes">
        <p:with-option name="test" select="not(//css:box/@part)"/>
    </px:assert>
    
    <px:xml-chunker part-attribute="part" propagate="false">
        <p:with-option name="always-break-before" select="$split-before"/>
        <p:with-option name="always-break-after" select="$split-after"/>
    </px:xml-chunker>
    
    <p:for-each>
        <p:delete match="*[not(self::css:box)]/@part"/>
        <p:label-elements match="css:box[@part=('head','tail')]" attribute="part" replace="true"
                          label="if (@part='head') then 'first' else 'last'"/>
        <p:delete match="*[@part=('middle','last')]/@css:*[local-name(.)=('id',
                                                                          'string-set')
                                                           or matches(local-name(.),'^counter-(set|reset|increment).*')]"/>
    </p:for-each>
    
</p:declare-step>
