<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:add-parameters"
                name="main"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                exclude-inline-prefixes="#all"
                version="1.0">
    
    <p:documentation>
        Add parameters to a c:param-set document.
    </p:documentation>
    
    <p:input port="source" sequence="true">
        <p:documentation>
            A sequence of c:param-set documents.
        </p:documentation>
    </p:input>
    
    <p:input kind="parameter" port="parameters" sequence="true">
        <p:inline>
            <c:param-set/>
        </p:inline>
    </p:input>
    
    <p:output port="result">
        <p:documentation>
            A single c:param-set document with the specified parameters added.
        </p:documentation>
    </p:output>
    
    <p:import href="merge-parameters.xpl"/>
    
    <px:merge-parameters>
        <p:input port="source">
            <p:pipe step="main" port="source"/>
            <p:pipe step="main" port="parameters"/>
        </p:input>
    </px:merge-parameters>
    
</p:declare-step>
