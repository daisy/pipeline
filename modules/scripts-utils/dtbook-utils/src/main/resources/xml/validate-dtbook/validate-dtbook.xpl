<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:pe="http://www.daisy.org/ns/pipeline/errors"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:tmp="http://www.daisy.org/ns/pipeline/tmp"
                xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:l="http://xproc.org/library"
                xmlns:m="http://www.w3.org/1998/Math/MathML"
                type="px:dtbook-validate" name="main"
                exclude-inline-prefixes="#all">
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1>DTBook Validator</h1>
        <p>Validates DTBook documents. Supports inclusion of MathML.</p>
        <address>
            Authors:
            <dl px:role="author">
                <dt>Name:</dt>
                <dd px:role="name">Marisa DeMeglio</dd>
                <dt>E-mail:</dt>
                <dd><a href="mailto:marisa.demeglio@gmail.com">marisa.demeglio@gmail.com</a></dd>
                <dt>Organization:</dt>
                <dd px:role="organization">DAISY Consortium</dd>
            </dl>
        </address>
    </p:documentation>
    
    <!-- ***************************************************** -->
    <!-- INPUTS / OUTPUTS / OPTIONS -->
    <!-- ***************************************************** -->
    
    <p:input port="source.fileset" primary="true"/>
    <p:input port="source.in-memory" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Input fileset</p>
            <p>Should contain a single DTBook file.</p>
            <p>May contain referenced images, but if it doesn't and images are stored on disk the
            validator will find them too.</p>
        </p:documentation>
        <p:empty/>
    </p:input>
    
    <p:output port="result" primary="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1>The DTBook document, or a dummy <code>tmp:error</code> document if the input does
            not contain a DTBook or it is not well-formed.</h1>
        </p:documentation>
        <p:pipe step="if-dtbook-wellformed" port="result"/>
    </p:output>
    
    <p:option name="report-method" cx:type="port|log|error" select="'port'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Select the method used for reporting validation issues:</p>
            <dl>
                <dt>port</dt>
                <dd>Issues are reported only on the xml-report and html-report output ports.</dd>
                <dt>log</dt>
                <dd>In addition to the xml-report and html-report output ports, issues are also
                reported through warning messages.</dd>
                <dt>error</dt>
                <dd>Issues are reported through error messages and also trigger an XProc
                error. (Note that an error is always thrown when a DTBook has an unexpected
                version.)</dd>
            </dl>
        </p:documentation>
    </p:option>
    
    <p:output port="xml-report">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1>XML Reports</h1>
            <p>Raw output from all types of validation used (RelaxNG, Schematron, custom).</p>
        </p:documentation>
        <p:pipe step="if-dtbook-wellformed" port="xml-report"/>
    </p:output>
    
    <p:output port="html-report" px:media-type="application/vnd.pipeline.report+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1>HTML Report</h1>
            <p>A single HTML-formatted version of the validation report.</p>
        </p:documentation>
        <p:pipe step="html-report" port="result"/>
    </p:output>
    
    <p:output port="validation-status" px:media-type="application/vnd.pipeline.status+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1>Validation status</h1>
            <p>Validation status (http://daisy.github.io/pipeline/StatusXML).</p>
        </p:documentation>
        <p:pipe step="validation-status" port="result"/>
    </p:output>
    
    <p:option name="mathml-version" select="'3.0'" cx:type="3.0|2.0">
        <p:documentation>
            <p>Version of MathML in the DTBook file</p>
        </p:documentation>
    </p:option>
    <p:option name="check-images" select="false()" cx:as="xs:boolean">
        <p:documentation>
            <p>Check to see that referenced images exist on disk</p>
        </p:documentation>
    </p:option>
    <p:option name="nimas" select="false()" cx:as="xs:boolean">
        <p:documentation>
            <p>Validate against NIMAS 1.1</p>
        </p:documentation>
    </p:option>
    <p:option name="skip-schematron" select="false()" cx:as="xs:boolean">
        <p:documentation>
            <p>Skip Schematron validation</p>
            <p>Should not be set when <code>nimas</code> is also set as NIMAS validation happens
            with Schematron.</p>
        </p:documentation>
    </p:option>
    <p:option name="allow-aural-css-attributes" select="false()" cx:as="xs:boolean">
        <p:documentation>
            <p>Whether the input contains aural CSS attributes (attributes with namespace
            "http://www.daisy.org/ns/pipeline/tts").</p>
        </p:documentation>
    </p:option>
    
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
            l:relax-ng-report
            px:report-errors
            px:check-files-wellformed
            px:combine-validation-reports
            px:validation-status
            px:validation-report-to-html
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/css-utils/library.xpl">
        <p:documentation>
            px:css-speech-clean
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
    
    <!-- ***************************************************** -->
    <!-- VALIDATION STEPS -->
    <!-- ***************************************************** -->
    
    <px:fileset-filter media-types="application/x-dtbook+xml"/>
    <px:check-files-wellformed px:message="Checking that DTBook document exists and is well-formed"
                               name="check-dtbook-wellformed">
        <p:input port="source.in-memory">
            <p:pipe step="main" port="source.in-memory"/>
        </p:input>
    </px:check-files-wellformed>
    
    <p:identity>
        <p:input port="source">
            <p:pipe step="check-dtbook-wellformed" port="validation-status"/>
        </p:input>
    </p:identity>

    <p:choose name="if-dtbook-wellformed">
        
        <!-- if the dtbook file was well-formed -->
        <p:when test="d:validation-status/@result = 'ok'">

            <p:output port="result">
                <p:pipe step="load-dtbook-doc" port="result"/>
            </p:output>
            <p:output port="xml-report" primary="true"/>
            
            <px:fileset-load media-types="application/x-dtbook+xml" name="load-dtbook-doc">
                <p:input port="fileset">
                    <p:pipe step="main" port="source.fileset"/>
                </p:input>
                <p:input port="in-memory">
                    <p:pipe step="main" port="source.in-memory"/>
                    <p:pipe step="check-dtbook-wellformed" port="result.in-memory"/>
                </p:input>
            </px:fileset-load>
            
            <p:choose>
                <p:when test="$allow-aural-css-attributes">
                    <px:css-speech-clean>
                        <p:documentation>Remove aural CSS attributes before validation</p:documentation>
                    </px:css-speech-clean>
                </p:when>
                <p:otherwise>
                    <p:identity/>
                </p:otherwise>
            </p:choose>

            <p:identity name="wellformed-dtbook"/>
            <p:group>
                <p:output port="xml-report" primary="true"/>
                
                <p:variable name="dtbook-version" select="*/@version"/>
                <p:variable name="filename" select="tokenize($base-uri, '/')[last()]"/>
                <!-- note that it is not possible to detect the MathML version -->
                <p:variable name="mathml" select="if (count(//m:math) > 0) then 'mathml' else ''"/>
                <p:variable name="document-type" select="if (count(//m:math) > 0)
                                                         then concat('DTBook ', $dtbook-version, ' with MathML ', $mathml-version)
                                                         else concat('DTBook ', $dtbook-version)"/>
                
                <px:assert error-code="pe:DTB001" message="DTBook is missing required 'version' attribute">
                    <p:with-option name="test" select="exists(*/@version)"/>
                </px:assert>
                <px:assert error-code="pe:DTB001"
                           message="DTBook version must be one of '2005-1', '2005-2', '2005-3' or '1.1.0', but got '$1'">
                    <p:with-option name="test" select="$dtbook-version=('2005-1','2005-2','2005-3','1.1.0')"/>
                    <p:with-option name="param1" select="$dtbook-version"/>
                </px:assert>
                <p:identity px:message="Validating document: {$base-uri}"/>
                
                <!-- fetch the appropriate RNG schema -->
                <px:dtbook-validator.select-schema name="select-rng-schema">
                    <p:with-option name="dtbook-version" select="$dtbook-version"/>
                    <p:with-option name="mathml-version" select="$mathml-version"/>
                </px:dtbook-validator.select-schema>
                <p:sink/>
                
                <p:group name="validate-against-relaxng">
                    <p:output port="result" primary="true"/>
                    
                    <!-- validate with RNG -->
                    <l:relax-ng-report name="run-relaxng-validation" assert-valid="false">
                        <p:input port="schema">
                            <p:pipe port="result" step="select-rng-schema"/>
                        </p:input>
                        <p:input port="source">
                            <p:pipe step="wellformed-dtbook" port="result"/>
                        </p:input>
                    </l:relax-ng-report>
                    <p:sink/>
                    
                    <!-- see if there was a report generated -->
                    <p:count limit="1">
                        <p:documentation>RelaxNG validation doesn't always produce a report, so this serves as a
                            test to see if there was a document produced.</p:documentation>
                        <p:input port="source">
                            <p:pipe port="report" step="run-relaxng-validation"/>
                        </p:input>
                    </p:count>
                    
                    <!-- possibly report errors as warning messages -->
                    <p:choose>
                        <p:when test="/c:result=0">
                            <p:identity/>
                        </p:when>
                        <p:when test="$report-method=('log','error')">
                            <px:report-errors>
                                <p:input port="report">
                                    <p:pipe step="run-relaxng-validation" port="report"/>
                                </p:input>
                                <p:with-option name="method" select="$report-method"/>
                            </px:report-errors>
                        </p:when>
                        <p:otherwise>
                            <p:identity/>
                        </p:otherwise>
                    </p:choose>
                    
                    <!-- if there were no errors, relaxng validation comes up empty. we need to have something to pass around, hence this step -->
                    <p:choose name="get-relaxng-report">
                        <p:when test="/c:result=0">
                            <p:sink/>
                            <p:identity>
                                <p:input port="source">
                                    <p:inline><c:errors/></p:inline>
                                </p:input>
                            </p:identity>
                        </p:when>
                        <p:otherwise>
                            <p:sink/>
                            <p:identity>
                                <p:input port="source">
                                    <p:pipe port="report" step="run-relaxng-validation"/>
                                </p:input>
                            </p:identity>
                        </p:otherwise>
                    </p:choose>
                </p:group>
                <p:sink/>
                
                <!-- validate with schematron -->
                <p:group name="validate-against-schematron">
                    <p:output port="report" sequence="true"/>
                    <p:choose>
                        <p:when test="$skip-schematron">
                            <p:output port="report" sequence="true"/>
                            <p:identity>
                                <p:input port="source">
                                    <p:empty/>
                                </p:input>
                            </p:identity>
                        </p:when>
                        <p:otherwise>
                            <p:output port="report" sequence="true"/>
                            <p:choose name="choose-schematron">
                                <p:when test="$nimas">
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
                            <p:sink/>
                            <p:validate-with-schematron assert-valid="false" name="run-schematron-validation">
                                <p:input port="schema">
                                    <p:pipe port="result" step="choose-schematron"/>
                                </p:input>
                                <p:input port="source">
                                    <p:pipe step="wellformed-dtbook" port="result"/>
                                </p:input>
                                <p:input port="parameters">
                                    <p:empty/>
                                </p:input>
                            </p:validate-with-schematron>
                            <p:sink/>
                            <p:identity>
                                <p:input port="source">
                                    <p:pipe step="run-schematron-validation" port="report"/>
                                </p:input>
                            </p:identity>
                            
                            <!-- possibly report errors as warning messages -->
                            <p:choose>
                                <p:xpath-context>
                                    <p:empty/>
                                </p:xpath-context>
                                <p:when test="$report-method=('log','error')">
                                    <p:variable name="errors" select="collection()//(svrl:failed-assert|svrl:successful-report)"/>
                                    <p:choose>
                                        <p:xpath-context>
                                            <p:empty/>
                                        </p:xpath-context>
                                        <p:when test="count($errors)=0">
                                            <p:identity/>
                                        </p:when>
                                        <p:otherwise>
                                            <p:sink/>
                                            <p:xslt template-name="main" name="errors">
                                                <p:input port="stylesheet">
                                                    <p:inline>
                                                        <xsl:stylesheet version="2.0" xpath-default-namespace="http://purl.oclc.org/dsdl/svrl">
                                                            <xsl:param name="errors" as="element()*"/>
                                                            <xsl:template name="main">
                                                                <c:errors>
                                                                    <xsl:for-each select="$errors">
                                                                        <c:error>
                                                                            <xsl:sequence select="normalize-space(string(text))"/>
                                                                        </c:error>
                                                                    </xsl:for-each>
                                                                </c:errors>
                                                            </xsl:template>
                                                        </xsl:stylesheet>
                                                    </p:inline>
                                                </p:input>
                                                <p:with-param port="parameters" name="errors" select="$errors"/>
                                                <p:input port="source">
                                                    <p:empty/>
                                                </p:input>
                                            </p:xslt>
                                            <px:report-errors>
                                                <p:input port="report">
                                                    <p:pipe step="errors" port="result"/>
                                                </p:input>
                                                <p:with-option name="method" select="$report-method"/>
                                            </px:report-errors>
                                        </p:otherwise>
                                    </p:choose>
                                </p:when>
                                <p:otherwise>
                                    <p:identity/>
                                </p:otherwise>
                            </p:choose>
                        </p:otherwise>
                    </p:choose>
                </p:group>
                <p:sink/>
                
                <!-- check images -->
                <p:group name="check-images-exist">
                    <p:output port="result"/>
                    <p:choose>
                        <p:when test="$check-images">
                            <pxi:dtbook-validator.check-images name="run-images-check">
                                <p:input port="source.fileset">
                                    <p:pipe step="load-dtbook-doc" port="unfiltered.fileset"/>
                                </p:input>
                                <p:input port="source.in-memory">
                                    <p:pipe step="load-dtbook-doc" port="unfiltered.in-memory"/>
                                </p:input>
                            </pxi:dtbook-validator.check-images>
                            
                            <!-- possibly report errors as warning messages -->
                            <p:choose>
                                <p:when test="$report-method=('log','error')">
                                    <p:xslt name="errors">
                                        <p:input port="stylesheet">
                                            <p:inline>
                                                <xsl:stylesheet version="2.0" xpath-default-namespace="http://www.daisy.org/ns/pipeline/data">
                                                    <xsl:template match="/*">
                                                        <c:errors>
                                                            <xsl:for-each select="error">
                                                                <c:error>
                                                                    <xsl:value-of select="normalize-space(string(desc))"/>
                                                                    <xsl:text>: </xsl:text>
                                                                    <xsl:value-of select="normalize-space(string(file))"/>
                                                                </c:error>
                                                            </xsl:for-each>
                                                        </c:errors>
                                                    </xsl:template>
                                                </xsl:stylesheet>
                                            </p:inline>
                                        </p:input>
                                        <p:input port="parameters">
                                            <p:empty/>
                                        </p:input>
                                    </p:xslt>
                                    <p:sink/>
                                    <px:report-errors>
                                        <p:input port="source">
                                            <p:pipe step="run-images-check" port="result"/>
                                        </p:input>
                                        <p:input port="report">
                                            <p:pipe step="errors" port="result"/>
                                        </p:input>
                                        <p:with-option name="method" select="$report-method"/>
                                    </px:report-errors>
                                </p:when>
                                <p:otherwise>
                                    <p:identity/>
                                </p:otherwise>
                            </p:choose>
                        </p:when>
                        <p:otherwise>
                            <p:identity>
                                <p:input port="source">
                                    <!-- generate an empty document -->
                                    <p:inline><d:errors/></p:inline>
                                </p:input>
                            </p:identity>
                        </p:otherwise>
                    </p:choose>
                </p:group>
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
                <p:inline><tmp:error/></p:inline>
            </p:output>
            <p:output port="xml-report" primary="true"/>
            <p:identity>
                <p:input port="source">
                    <p:pipe step="check-dtbook-wellformed" port="report"/>
                </p:input>
            </p:identity>
            
            <!-- possibly report errors as warning messages -->
            <p:choose>
                <p:when test="$report-method=('log','error')">
                    <p:xslt name="errors">
                        <p:input port="stylesheet">
                            <p:inline>
                                <xsl:stylesheet version="2.0" xpath-default-namespace="http://www.daisy.org/ns/pipeline/data">
                                    <xsl:template match="/*">
                                        <c:errors>
                                            <xsl:for-each select="error">
                                                <c:error>
                                                    <xsl:value-of select="normalize-space(string(desc))"/>
                                                    <xsl:text>: </xsl:text>
                                                    <xsl:value-of select="normalize-space(string(file))"/>
                                                </c:error>
                                            </xsl:for-each>
                                        </c:errors>
                                    </xsl:template>
                                </xsl:stylesheet>
                            </p:inline>
                        </p:input>
                        <p:input port="parameters">
                            <p:empty/>
                        </p:input>
                    </p:xslt>
                    <p:sink/>
                    <px:report-errors>
                        <p:input port="source">
                            <p:pipe step="check-dtbook-wellformed" port="report"/>
                        </p:input>
                        <p:input port="report">
                            <p:pipe step="errors" port="result"/>
                        </p:input>
                        <p:with-option name="method" select="$report-method"/>
                    </px:report-errors>
                </p:when>
                <p:otherwise>
                    <p:identity/>
                </p:otherwise>
            </p:choose>
            <px:message message="DTBook document is missing or not well-formed"/>
            <p:identity name="check-dtbook-wellformed-report"/>
            <p:sink/>
            
            <px:combine-validation-reports>
                <p:with-option name="document-name" select="tokenize($base-uri, '/')[last()]"/>
                <p:with-option name="document-type" select="'N/A'"/>
                <p:with-option name="document-path" select="$base-uri"/>
                <p:input port="source">
                    <p:pipe step="check-dtbook-wellformed-report" port="result"/>
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
