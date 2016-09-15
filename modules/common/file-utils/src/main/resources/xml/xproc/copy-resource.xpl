<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal" type="px:copy-resource" version="1.0">
    
    <p:output port="result"/>
    <p:option name="href" required="true"/>
    <p:option name="target" required="true"/>
    <p:option name="fail-on-error" select="'true'"/>
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Copies the resource pointed by the option "href" to the target file. The 
            "target" option MUST be a uri with a "file:" scheme. If the "target" uri is a directory
            ( the path ends with / ) the step will try to name the file based on the href path. 
            The return value is consistent with the cx:copy step, a <result> </result>element with 
            the target file as text.
        </p>
    </p:documentation>
    <p:declare-step type="pxi:copy-resource">
        <p:output port="result"/>
        <p:option name="href" required="true"/>
        <p:option name="target" required="true"/>
        <p:option name="fail-on-error" select="'true'"/>
    </p:declare-step>
    
    <p:import href="file-library.xpl"/>
    
    <p:choose>
        <p:when test="p:step-available('pxi:copy-resource')">
            <pxi:copy-resource>
                <p:with-option name="href" select="$href"/>
                <p:with-option name="target" select="$target"/>
                <p:with-option name="fail-on-error" select="$fail-on-error"/>
            </pxi:copy-resource>
        </p:when>
        <p:otherwise>
            <!--
                If pxi:copy-resource is not available, then you're probably using vanilla calabash
                outside of the Pipeline 2 framework and px:copy should do the job just fine
            -->
            <px:copy name="calabash-copy">
                <p:with-option name="href" select="$href"/>
                <p:with-option name="target" select="$target"/>
                <p:with-option name="fail-on-error" select="$fail-on-error"/>
            </px:copy>
            <p:identity>
                <p:input port="source">
                    <p:pipe port="result" step="calabash-copy"/>
                </p:input>
            </p:identity>
        </p:otherwise>
    </p:choose>
    
    

    
    
</p:declare-step>
