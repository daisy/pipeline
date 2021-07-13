<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:tmp="http://www.daisy.org/ns/pipeline/tmp"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:l="http://xproc.org/library"
                xmlns:m="http://www.w3.org/1998/Math/MathML"
                type="px:dtbook-validate" name="main"
                exclude-inline-prefixes="#all">
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">DTBook Validator</h1>
        <p px:role="desc">Validates DTBook documents. Supports inclusion of MathML.</p>
        <div px:role="author maintainer">
            <p px:role="name">Marisa DeMeglio</p>
            <a px:role="contact" href="mailto:marisa.demeglio@gmail.com">marisa.demeglio@gmail.com</a>
            <p px:role="organization">DAISY Consortium</p>
        </div>
    </p:documentation>
    
    <!-- ***************************************************** -->
    <!-- INPUTS / OUTPUTS / OPTIONS -->
    <!-- ***************************************************** -->
    
    <p:input port="source.fileset" primary="true"/>
    <p:input port="source.in-memory" sequence="true">
        <p:empty/>
    </p:input>
    
    <p:output port="result" primary="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">Copy of input document</h1>
            <p px:role="desc">A copy of the input document; may include PSVI annotations.</p>
        </p:documentation>
        <p:pipe step="if-dtbook-wellformed" port="result"/>
    </p:output>
    
    <p:output port="xml-report" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">XML Reports</h1>
            <p px:role="desc">Raw output from all types of validation used (RelaxNG, Schematron, custom).</p>
        </p:documentation>
        <p:pipe step="if-dtbook-wellformed" port="xml-report"/>
    </p:output>
    
    <p:output port="html-report" px:media-type="application/vnd.pipeline.report+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">HTML Report</h1>
            <p px:role="desc">An HTML-formatted version of the validation report.</p>
        </p:documentation>
        <p:pipe step="html-report" port="result"/>
    </p:output>
    
    <p:output port="validation-status" px:media-type="application/vnd.pipeline.status+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">Validation status</h1>
            <p px:role="desc">Validation status (http://daisy.github.io/pipeline/StatusXML).</p>
        </p:documentation>
        <p:pipe step="validation-status" port="result"/>
    </p:output>
    
    <p:option name="mathml-version" select="'3.0'"/>
    <p:option name="check-images" select="'false'" cx:as="xs:string"/>
    <p:option name="nimas" select="'false'" cx:as="xs:string"/>
    
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
        <p:documentation>
            px:message
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-rebase
            px:fileset-load
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/validation-utils/library.xpl">
        <p:documentation>
            px:check-files-wellformed
            px:combine-validation-reports
            px:validation-status
            px:validation-report-to-html
        </p:documentation>
    </p:import>
    <p:import href="dtbook-validator.check-images.xpl">
        <p:documentation>
            pxi:dtbook-validator.check-images
        </p:documentation>
    </p:import>
    <p:import href="dtbook-validator.select-schema.xpl">
        <p:documentation>
            px:dtbook-validator.select-schema
        </p:documentation>
    </p:import>
    
    <p:variable name="base-uri" select="/*/d:file[@media-type='application/x-dtbook+xml']/resolve-uri(@href,base-uri(.))"/>
    
    <!--
        Make sure that the base uri of the fileset is the directory containing the DTBook. This
        should normally eliminate any relative hrefs starting with "..", which is required for this
        step to work.
    -->
    <px:fileset-rebase>
        <p:with-option name="new-base" select="$base-uri"/>
    </px:fileset-rebase>
    <p:identity name="source.fileset"/>
    
    <!-- ***************************************************** -->
    <!-- VALIDATION STEPS -->
    <!-- ***************************************************** -->
    
    <!--check that the package document is well-formed XML -->
    <px:message message="DTBook validator: Checking that DTBook document exists and is well-formed"/>
    <px:check-files-wellformed name="check-dtbook-wellformed"/>
    
    <p:identity>
        <p:input port="source">
            <p:pipe step="check-dtbook-wellformed" port="validation-status"/>
        </p:input>
    </p:identity>
    <px:message message="DTBook validator: Done checking that DTBook document exists and is well-formed"/>

    <p:choose name="if-dtbook-wellformed">
        
        <!-- if the dtbook file was well-formed -->
        <p:when test="d:validation-status/@result = 'ok'">

            <p:output port="result">
                <p:pipe step="wellformed-dtbook" port="result"/>
            </p:output>
            <p:output port="xml-report" primary="true"/>
            
            <px:fileset-load media-types="application/x-dtbook+xml" name="load-dtbook-doc">
                <p:input port="fileset">
                    <p:pipe step="source.fileset" port="result"/>
                </p:input>
                <p:input port="in-memory">
                    <p:pipe step="main" port="source.in-memory"/>
                </p:input>
            </px:fileset-load>
            
            <p:group name="wellformed-dtbook">
                <p:output port="result">
                    <p:pipe step="validate-against-relaxng" port="copy-of-document"/>
                </p:output>
                <p:output port="xml-report" primary="true"/>
                
                <p:variable name="dtbook-version" select="*/@version"/>
                <p:variable name="filename" select="tokenize($base-uri, '/')[last()]"/>
                <p:variable name="mathml" select="if (count(//m:math) > 0) then 'mathml' else ''"/>
                <p:variable name="document-type" select="if (count(//m:math) > 0)
                                                         then concat('DTBook ', $dtbook-version, ' with MathML ', $mathml-version)
                                                         else concat('DTBook ', $dtbook-version)"/>
                
                <px:message>
                    <p:with-option name="message" select="concat('DTBook Validator: Validating document: ', $base-uri)"/>
                </px:message>
                
                <!-- fetch the appropriate RNG schema -->
                <px:dtbook-validator.select-schema name="select-rng-schema">
                    <p:with-option name="dtbook-version" select="$dtbook-version"/>
                    <p:with-option name="mathml-version" select="$mathml-version"/>
                </px:dtbook-validator.select-schema>
                <p:sink/>
                
                <p:group name="validate-against-relaxng">
                    <p:output port="result" primary="true"/>
                    <p:output port="copy-of-document">
                        <p:pipe port="result" step="run-relaxng-validation"/>
                    </p:output>
                    <!-- validate with RNG -->
                    <l:relax-ng-report name="run-relaxng-validation" assert-valid="false">
                        <p:input port="schema">
                            <p:pipe port="result" step="select-rng-schema"/>
                        </p:input>
                        <p:input port="source">
                            <p:pipe step="load-dtbook-doc" port="result"/>
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
                
                <p:choose name="choose-schematron">
                    <p:when test="$nimas = 'true'">
                        <p:output port="result"/>
                        <p:identity name="use-nimas-schematron">
                            <p:input port="source">
                                <p:document href="http://www.daisy.org/pipeline/modules/dtbook-utils/schema/dtbook.mathml.nimas.sch"/>
                            </p:input>
                        </p:identity>
                    </p:when>
                    <p:otherwise>
                        <p:output port="result"/>
                        <p:identity name="use-default-schematron">
                            <p:input port="source">
                                <p:document href="http://www.daisy.org/pipeline/modules/dtbook-utils/schema/dtbook.mathml.sch"/>
                            </p:input>
                        </p:identity>
                    </p:otherwise>
                </p:choose>
                <!-- validate with schematron -->
                <p:validate-with-schematron assert-valid="false" name="validate-against-schematron">
                    <p:input port="schema">
                        <p:pipe port="result" step="choose-schematron"/>
                    </p:input>
                    <p:input port="source">
                        <p:pipe step="load-dtbook-doc" port="result"/>
                    </p:input>
                    <p:input port="parameters">
                        <p:empty/>
                    </p:input>
                </p:validate-with-schematron>
                <p:sink/>
                
                <!-- check images -->
                <p:choose name="check-images-exist">
                    <p:when test="$check-images = 'true'">
                        <p:output port="result"/>
                        <pxi:dtbook-validator.check-images>
                            <p:input port="source">
                                <p:pipe step="load-dtbook-doc" port="result"/>
                            </p:input>
                        </pxi:dtbook-validator.check-images>
                    </p:when>
                    <p:otherwise>
                        <p:output port="result"/>
                        <p:identity>
                            <p:input port="source">
                                <!-- generate an empty document -->
                                <p:inline>
                                    <d:errors/>
                                </p:inline>
                            </p:input>
                        </p:identity>
                    </p:otherwise>
                </p:choose>
                <p:sink/>
                
                <!-- combine reports-->
                <px:combine-validation-reports>
                    <p:with-option name="document-type" select="$document-type"/>
                    <p:with-option name="document-name" select="$filename"/>
                    <p:with-option name="document-path" select="$base-uri"/>
                    <p:with-option name="internal-info" select="$mathml"/>
                    <p:input port="source">
                        <!-- a sequence of reports -->
                        <p:pipe port="result" step="validate-against-relaxng"/>
                        <p:pipe port="report" step="validate-against-schematron"/>
                        <p:pipe port="result" step="check-images-exist"/>
                    </p:input>
                </px:combine-validation-reports>
            </p:group>
        </p:when>

        <!-- otherwise, just store a report from the wellformedness check -->
        <p:otherwise>
            <p:output port="result">
                <!-- we have to put something on this port... -->
                <p:inline>
                    <tmp:error/>
                </p:inline>
            </p:output>
            <p:output port="xml-report" primary="true"/>
            <px:message message="DTBook Validator: DTBook document is missing or not well-formed">
                <p:input port="source">
                    <p:inline>
                        <p:empty/>
                    </p:inline>
                </p:input>
            </px:message>
            <p:sink/>
            <px:combine-validation-reports>
                <p:with-option name="document-name" select="tokenize($base-uri, '/')[last()]"/>
                <p:with-option name="document-type" select="'N/A'"/>
                <p:with-option name="document-path" select="$base-uri"/>
                <p:input port="source">
                    <p:pipe port="report" step="check-dtbook-wellformed"/>
                </p:input>
            </px:combine-validation-reports>
        </p:otherwise>
    </p:choose>
    
    <px:validation-status name="validation-status"/>
    <p:sink/>
    
    <px:validation-report-to-html name="html-report">
        <p:input port="source">
            <p:pipe step="if-dtbook-wellformed" port="xml-report"/>
        </p:input>
    </px:validation-report-to-html>
    <p:sink/>
    
</p:declare-step>
