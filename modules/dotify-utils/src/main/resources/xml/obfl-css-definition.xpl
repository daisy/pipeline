<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                type="pxi:obfl-css-definition"
                name="main"
                exclude-inline-prefixes="#all">
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Convert CSS styles to OBFL properties.</p>
    </p:documentation>
    
    <p:input port="source" primary="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The input is assumed to be a tree-of-boxes representation of a document, where boxes
            are represented by css:box elements. Box properties may be declared in style attributes
            or individial css:* attributes or both.</p>
        </p:documentation>
    </p:input>
    
    <p:input port="parameters" kind="parameter" primary="false"/>
    
    <p:output port="result">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>css:* attributes are added on boxes in such a way that for each box and for each OBFL
            property, the computed value of the OBFL property is equal to the computed value of the
            corresponding CSS property in the input. Declarations are omitted from the output when
            possible, and 'initial' and 'inherit' values are concretized. style attributes on boxes
            are dropped, and if they contain unhandled properties, it will result in
            warnings. Unhandled css:* attributes in the input are retained.</p>
        </p:documentation>
    </p:output>
    
    <p:xslt px:progress="1">
        <p:input port="stylesheet">
            <p:document href="obfl-css-definition.xsl"/>
        </p:input>
        <p:input port="parameters">
            <p:pipe step="main" port="parameters"/>
        </p:input>
    </p:xslt>
    
</p:declare-step>
