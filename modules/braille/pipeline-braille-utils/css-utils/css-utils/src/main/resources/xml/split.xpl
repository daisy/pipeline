<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
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
    
    <p:option name="split-before" required="true">
        <p:documentation>
            The 'split-before' option must be an XSLTMatchPattern that matches only elements.
        </p:documentation>
    </p:option>
    
    <p:option name="split-after" required="true">
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
            css:string-set, css:string-entry, css:counter-set, css:counter-reset,
            css:counter-increment, css:counter-set-*, css:counter-reset-* and
            css:counter-increment-* are omitted on css:box elements with a part attribute equal to
            'middle' or 'last'.
        </p:documentation>
    </p:output>
    
    <p:declare-step type="pxi:split-into-sections-inner">
        <p:input port="source"/>
        <p:output port="result" sequence="true"/>
        <p:variable name="split-point"
                    select="(//*[@pxi:split-before or
                                (@pxi:split-after and not(descendant::*[@pxi:split-before or @pxi:split-after]))]
                            )[1]/@xml:id"/>
        <p:choose>
            <p:when test="$split-point!=''">
                <p:variable name="position" select="if (//*[@xml:id=$split-point]/@pxi:split-before) then 'before' else 'after'"/>
                <p:variable name="matcher" select="concat('*[@xml:id=&quot;', $split-point, '&quot;]')"/>
                <p:delete>
                    <p:with-option name="match" select="concat($matcher,'/@pxi:split-',$position)"/>
                </p:delete>
                <p:identity name="unsplit"/>
                <p:label-elements attribute="part" label="if (@part=('middle','last')) then 'middle' else 'first'">
                    <p:with-option name="match" select="concat('css:box[descendant::',$matcher,']')"/>
                </p:label-elements>
                <p:delete>
                    <p:with-option name="match" select="if ($position='before')
                                                        then concat('node()[preceding::',$matcher,']|',$matcher)
                                                        else concat('node()[preceding::',$matcher,']')"/>
                </p:delete>
                <p:identity name="first-part"/>
                <p:identity>
                    <p:input port="source">
                        <p:pipe step="unsplit" port="result"/>
                    </p:input>
                </p:identity>
                <p:label-elements attribute="part" label="if (@part=('first','middle')) then 'middle' else 'last'">
                    <p:with-option name="match" select="concat('css:box[descendant::',$matcher,']')"/>
                </p:label-elements>
                <p:delete>
                    <p:with-option name="match" select="if ($position='before')
                                                        then concat('node()[following::',$matcher,']')
                                                        else concat('node()[following::',$matcher,']|',$matcher)"/>
                </p:delete>
                <p:for-each>
                    <pxi:split-into-sections-inner/>
                </p:for-each>
                <p:identity name="next-parts"/>
                <p:identity>
                    <p:input port="source">
                        <p:pipe step="first-part" port="result"/>
                        <p:pipe step="next-parts" port="result"/>
                    </p:input>
                </p:identity>
            </p:when>
            <p:otherwise>
                <p:identity/>
            </p:otherwise>
        </p:choose>
    </p:declare-step>
    
    <p:add-attribute attribute-name="pxi:split-before" attribute-value="true">
        <p:with-option name="match" select="$split-before"/>
    </p:add-attribute>
    
    <p:add-attribute attribute-name="pxi:split-after" attribute-value="true">
        <p:with-option name="match" select="$split-after"/>
    </p:add-attribute>
    
    <p:label-elements attribute="xml:id" replace="false" label="concat('__temp__',$p:index)"
                      match="*[@pxi:split-before or @pxi:split-after]"/>
    
    <pxi:split-into-sections-inner/>
    
    <p:for-each>
        <p:delete match="@xml:id[starts-with(., '__temp__')]|@pxi:split-before|@pxi:split-after"/>
    </p:for-each>
    
    <p:for-each>
        <p:delete match="css:box[@part=('middle','last')]/@css:*[matches(local-name(),'^counter-(reset|set|increment)')]|
                         css:box[@part=('middle','last')]/@css:string-entry|
                         css:box[@part=('middle','last')]/@css:string-set|
                         css:box[@part=('middle','last')]/@css:id"/>
    </p:for-each>
    
</p:declare-step>
