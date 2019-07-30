<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                type="css:new-definition"
                name="main"
                exclude-inline-prefixes="#all"
                version="1.0">
    
    <p:documentation>
        Migrate to a custom CSS definition.
    </p:documentation>
    
    <p:input port="source" primary="true">
        <p:documentation>
            The input is assumed to be a tree-of-boxes representation of a document, where boxes are
            represented by css:box elements. Box properties may be declared in style attributes or
            individial css:* attributes or both.
        </p:documentation>
    </p:input>
    
    <p:input port="definition">
        <p:documentation>
            The new CSS definition must be a xsl:stylesheet consisting of a one variable and four
            functions. All properties and functions must be in the namespace "css:new-definition".
            The 'properties' variable is a sequence of strings that defines the supported
            properties. The 'is-valid' function defines whether a property declaration is valid.
            The 'is-inherited' function defines whether a property is inherited. The 'initial-value'
            function defines the initial value of a property. The 'applies-to' function defines
            whether a property applies in a certain context. All functions are possibly context
            dependent.
        </p:documentation>
    </p:input>
    
    <p:output port="result">
        <p:documentation>
            css:* attributes will be added on boxes in the output in such a way that for each box
            and for each of the new definition's properties, the value of the property computed at
            the input according to the old CSS definition is equal to the value computed at the
            output according to the new definition. Property declarations are omitted from the
            output when possible, 'initial' values are concretized, and 'inherit' values are
            concretized only when needed. style attributes on boxes at the output are dropped. css:*
            attributes that don't correspond to a property in the new definition are retained.
        </p:documentation>
    </p:output>
    
    <p:add-attribute match="xsl:include" attribute-name="href" name="include">
        <p:input port="source">
            <p:inline>
                <xsl:include/>
            </p:inline>
        </p:input>
        <p:with-option name="attribute-value" select="resolve-uri('new-definition.xsl')">
            <p:inline>
                <irrelevant/>
            </p:inline>
        </p:with-option>
    </p:add-attribute>
    
    <p:insert name="stylesheet" position="first-child">
        <p:input port="source">
            <p:pipe step="main" port="definition"/>
        </p:input>
        <p:input port="insertion">
            <p:pipe step="include" port="result"/>
        </p:input>
    </p:insert>
    
    <p:xslt px:progress="1">
        <p:input port="source">
            <p:pipe step="main" port="source"/>
        </p:input>
        <p:input port="stylesheet">
            <p:pipe step="stylesheet" port="result"/>
        </p:input>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
    </p:xslt>
    
</p:declare-step>
