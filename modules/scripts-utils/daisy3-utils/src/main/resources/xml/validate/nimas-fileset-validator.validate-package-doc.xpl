<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:l="http://xproc.org/library"
                type="pxi:nimas-fileset-validator.validate-package-doc"
                name="nimas-fileset-validator.validate-package-doc"
                exclude-inline-prefixes="#all">
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1>NIMAS Fileset Validator Helper: Validate package documents</h1>
        <p>Validates package documents (*.opf).</p>
    </p:documentation>
    
    <!-- ***************************************************** -->
    <!-- INPUTS / OUTPUTS / OPTIONS -->
    <!-- ***************************************************** -->
    <p:input port="source" primary="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>A package document (.opf).</p>
        </p:documentation>
    </p:input>
    
    <p:output port="result" primary="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>A copy of the input document; may include PSVI annotations.</p>
        </p:documentation>
        <p:pipe port="copy-of-document" step="validate-against-relaxng"/>
    </p:output>
    
    <p:output port="report">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Raw output from the RelaxNG validation.</p>
        </p:documentation>
        <p:pipe port="result" step="wrap-reports"/>
    </p:output>
    
    <p:option name="math" required="false" select="'false'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Indicates the presence of MathML in the book. 
                When set to true, the validator checks that the correct metadata is present in the package document.</p>
        </p:documentation>
    </p:option>
    
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
        <p:documentation>
            px:message
        </p:documentation>
    </p:import>
    
    <p:import href="http://www.daisy.org/pipeline/modules/validation-utils/library.xpl">
        <p:documentation>
            px:check-files-exist
            px:combine-validation-reports
        </p:documentation>
    </p:import>
    
    <p:import href="nimas-fileset-validator.fileset-filter.xpl">
        <p:documentation>
            pxi:nimas-fileset-validator.fileset-filter
        </p:documentation>
    </p:import>
    
    <p:variable name="base-uri" select="base-uri()">
        <p:pipe port="source" step="nimas-fileset-validator.validate-package-doc"/>
    </p:variable>
    <p:variable name="filename" 
        select="tokenize($base-uri, '/')[last()]"/>
    
    <p:variable name="document-type" select="if ($math eq 'true') 
        then 'OPF 1.2 (MathML detected)' 
        else 'OPF 1.2'"/>
    
    <px:message message="Nimas fileset validator: Validating package document."/>
    <p:sink/>
    
    <!-- ***************************************************** -->
    <!-- VALIDATION STEPS -->
    <!-- ***************************************************** -->
    <p:group name="validate-against-relaxng">
        <p:output port="result" primary="true"/>
        <p:output port="copy-of-document">
            <p:pipe port="result" step="run-relaxng-validation"/>
        </p:output>
        
        <!-- validate with RNG -->
        <l:relax-ng-report name="run-relaxng-validation" assert-valid="false">
            <p:input port="schema">
                <p:document href="./schema/rng/package-doc-1.2.rng"/>
            </p:input>
            <p:input port="source">
                <p:pipe port="source" step="nimas-fileset-validator.validate-package-doc"/>
            </p:input>
        </l:relax-ng-report>
        
        <!-- see if there was a report generated -->
        <p:count name="count-relaxng-report" limit="1">
            <p:documentation>RelaxNG validation doesn't always produce a report, so this serves as a
                test to see if there was a document produced.</p:documentation>
            <p:input port="source">
                <p:pipe port="report" step="run-relaxng-validation"/>
            </p:input>
        </p:count>
        
        <!-- if there were no errors, relaxng validation comes up empty. we need to have something to pass around, hence this step -->
        <p:choose name="get-relaxng-report">
            <p:xpath-context>
                <p:pipe port="result" step="count-relaxng-report"/>
            </p:xpath-context>
            <!-- if there was no relaxng report, then put an empty errors list document as output -->
            <p:when test="/c:result = '0'">
                <p:identity>
                    <p:input port="source">
                        <p:inline>
                            <c:errors/>
                        </p:inline>
                    </p:input>
                </p:identity>
            </p:when>
            <p:otherwise>
                <p:identity>
                    <p:input port="source">
                        <p:pipe port="report" step="run-relaxng-validation"/>
                    </p:input>
                </p:identity>
            </p:otherwise>
        </p:choose>
    </p:group>
    
    <!-- based on the $math option, choose a schematron file -->
    <p:choose name="choose-schematron-file">
        <p:when test="$math eq 'true'">
            <p:output port="result"/>
            <p:identity>
                <p:input port="source">
                    <p:document href="./schema/sch/package-doc-with-mathml.sch"/>
                </p:input>
            </p:identity>
        </p:when>
        <p:otherwise>
            <p:output port="result"/>
            <p:identity>
                <p:input port="source">
                    <p:document href="./schema/sch/package-doc-without-mathml.sch"/>
                </p:input>
            </p:identity>
        </p:otherwise>
    </p:choose>
    
    <!-- validate with schematron -->
    <p:validate-with-schematron assert-valid="false" name="validate-against-schematron">
        <p:input port="schema">
            <p:pipe port="result" step="choose-schematron-file"/>
        </p:input>
        <p:input port="source">
            <p:pipe port="source" step="nimas-fileset-validator.validate-package-doc"/>
        </p:input>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        
        
    </p:validate-with-schematron>
    <p:sink/>
    
    <p:group name="check-files-exist">
        <p:output port="result">
            <p:pipe port="report" step="check-pdfs-exist"/>
        </p:output>
        
        <!-- check that any referenced PDFs exist on disk -->
        <pxi:nimas-fileset-validator.fileset-filter media-type="application/pdf">
            <p:input port="source">
                <p:pipe port="source" step="nimas-fileset-validator.validate-package-doc"/>
            </p:input>
        </pxi:nimas-fileset-validator.fileset-filter>
        <px:check-files-exist name="check-pdfs-exist"/>
        <p:sink/>
    </p:group>
    <p:sink/>
    
    <px:combine-validation-reports name="wrap-reports">
        <p:with-option name="document-name" select="$filename"/>
        <p:with-option name="document-type" select="$document-type"/>
        <p:with-option name="document-path" select="$base-uri"/>
        <p:input port="source">
            <!-- a sequence of reports -->
            <p:pipe port="result" step="validate-against-relaxng"/>
            <p:pipe port="report" step="validate-against-schematron"/>
            <p:pipe port="result" step="check-files-exist"/>
        </p:input>
    </px:combine-validation-reports>
    
</p:declare-step>
