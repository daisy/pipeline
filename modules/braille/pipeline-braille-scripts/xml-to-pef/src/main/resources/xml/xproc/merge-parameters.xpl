<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:merge-parameters"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                exclude-inline-prefixes="#all"
                version="1.0">
    
    <p:documentation>
        Merge c:param-set documents.
    </p:documentation>
    
    <p:input port="source" sequence="true">
        <p:documentation>
            Sequence of c:param-set documents.
        </p:documentation>
    </p:input>
    
    <p:output port="result">
        <p:documentation>
            Exactly one c:param-set document.
        </p:documentation>
    </p:output>
    
    <p:wrap-sequence wrapper="c:param-set"/>
    <p:unwrap match="/c:param-set/c:param-set"/>
    <p:delete match="/c:param-set/c:param[@name = following-sibling::c:param/@name]"/>
    
</p:declare-step>
