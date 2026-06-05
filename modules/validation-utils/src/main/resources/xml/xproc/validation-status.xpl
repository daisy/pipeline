<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                xmlns:tmp="http://www.daisy.org/ns/pipeline/tmp"
                type="px:validation-status" name="validation-status"
                exclude-inline-prefixes="#all">
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1>Validation status XML</h1>
        <p>Given one or more validation reports, produce XML that can be used on the reserved validation-status port. 
            If any of the reports contain errors, the validation status will be 'error'. Otherwise, it will be 'ok'.</p>
    </p:documentation>
    
    <p:input port="source" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>One or more validation reports (http://code.google.com/p/daisy-pipeline/wiki/ValidationReportXML).</p>
        </p:documentation>
    </p:input>
    
    <p:output port="result" px:media-type="application/vnd.pipeline.status+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Validation status (http://daisy.github.io/pipeline/StatusXML).</p>
        </p:documentation>
    </p:output>
    
    <p:for-each name="has-errors">
        <p:output port="result" sequence="true"/>
        <p:choose>
            <p:when test="count(//d:error|//svrl:failed-assert|//svrl:successful-report|//d:message[@severity='error']) > 0">
                <p:identity>
                    <p:input port="source">
                        <p:inline>
                            <tmp:true/>
                        </p:inline>
                    </p:input>
                </p:identity>
            </p:when>
            <p:otherwise>
                <p:identity>
                    <p:input port="source">
                        <p:empty/>
                    </p:input>
                </p:identity>
            </p:otherwise>
        </p:choose>
    </p:for-each>
    <p:wrap-sequence name="wrap-has-errors" wrapper="results"
        wrapper-namespace="http://www.daisy.org/ns/pipeline/tmp" wrapper-prefix="tmp">
        <p:input port="source">
            <p:pipe port="result" step="has-errors"/>
        </p:input>
    </p:wrap-sequence>
    
    <p:identity name="output-doc">
        <p:input port="source">
            <p:inline>
                <d:validation-status result="@@"/>
            </p:inline>
        </p:input>
    </p:identity>
    
    <p:choose>
        <p:xpath-context>
            <p:pipe port="result" step="wrap-has-errors"/>
        </p:xpath-context>
        
        <p:when test="//tmp:true">
            <p:string-replace match="d:validation-status/@result" replace="'error'"/>
        </p:when>
        <p:otherwise>
            <p:string-replace match="d:validation-status/@result" replace="'ok'"/>
        </p:otherwise>
    </p:choose>
    
</p:declare-step>