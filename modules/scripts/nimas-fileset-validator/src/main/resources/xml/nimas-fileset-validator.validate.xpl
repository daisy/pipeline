<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:pkg="http://openebook.org/namespaces/oeb-package/1.0/"
                type="px:nimas-fileset-validator" name="main"
                exclude-inline-prefixes="#all">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1>NIMAS Fileset Validator: Validate</h1>
        <p>Internal step.</p>
    </p:documentation>

    <!-- ***************************************************** -->
    <!-- INPUTS / OUTPUTS / OPTIONS -->
    <!-- ***************************************************** -->
    <p:input port="source.fileset" primary="true"/>
    <p:input port="source.in-memory" sequence="true">
        <p:empty/>
    </p:input>

    <p:output port="html-report" primary="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1>HTML Report</h1>
            <p>An HTML-formatted validation report comprising all documents'
                reports.</p>
        </p:documentation>
        <p:pipe step="html-report" port="result"/>
    </p:output>

    <p:output port="package-doc-validation-report">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1>Package Document XML Report</h1>
            <p>Raw validation output for the package document.</p>
        </p:documentation>
        <p:pipe step="if-package-wellformed" port="package-doc-validation-report"/>
    </p:output>

    <p:output port="dtbook-validation-report" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1>DTBook XML Report</h1>
            <p>Raw validation output for the DTBook file(s).</p>
        </p:documentation>
        <p:pipe step="if-package-wellformed" port="dtbook-validation-report"/>
    </p:output>

    <p:output port="validation-status">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1>Validation Status</h1>
            <p>Validation status (http://daisy.github.io/pipeline/StatusXML)</p>
        </p:documentation>
        <p:pipe step="validation-status" port="result"/>
    </p:output>

    <p:option name="mathml-version" cx:type="3.0|2.0"/>
    <p:option name="check-images" cx:as="xs:boolean"/>
    <p:option name="base-uri"/>

    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
        <p:documentation>
            px:message
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-rebase
            px:fileset-load
            px:fileset-add-entry
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/validation-utils/library.xpl">
        <p:documentation>
            px:check-files-wellformed
            px:combine-validation-reports
            px:validation-report-to-html
            px:validation-status
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/dtbook-utils/library.xpl">
        <p:documentation>
            px:dtbook-validate
        </p:documentation>
    </p:import>
    <p:import href="nimas-fileset-validator.validate-package-doc.xpl">
        <p:documentation>
            pxi:nimas-fileset-validator.validate-package-doc
        </p:documentation>
    </p:import>

    <p:variable name="package-doc-filename" select="tokenize($base-uri, '/')[last()]"/>

    <!--
        Make sure that the base uri of the fileset is the directory containing the OPF. This should
        normally eliminate any relative hrefs starting with "..", which is required for this step to
        work.
    -->
    <px:fileset-rebase>
        <p:with-option name="new-base"
                       select="/*/d:file[@media-type='application/oebps-package+xml']/resolve-uri(@href,base-uri(.))"/>
    </px:fileset-rebase>
    <p:identity name="source.fileset"/>
    
    <!-- ***************************************************** -->
    <!-- VALIDATION STEPS -->
    <!-- ***************************************************** -->

    <!--check that the package document is well-formed XML -->
    <px:message message="Nimas fileset validator: Checking that package document is well-formed"/>
    <px:check-files-wellformed name="check-package-wellformed"/>

    <p:choose name="if-package-wellformed">
        <p:xpath-context>
            <p:pipe step="check-package-wellformed" port="validation-status"/>
        </p:xpath-context>

        <!-- if the package file was well-formed -->
        <p:when test="d:validation-status/@result = 'ok'">

            <p:output port="dtbook-validation-report" sequence="true">
                <p:pipe step="validate-dtbooks" port="result"/>
            </p:output>
            <p:output port="package-doc-validation-report">
                <p:pipe step="validate-package-doc" port="result"/>
            </p:output>

            <px:fileset-load media-types="application/oebps-package+xml" name="load-package-doc">
                <p:input port="fileset">
                    <p:pipe step="source.fileset" port="result"/>
                </p:input>
                <p:input port="in-memory">
                    <p:pipe step="main" port="source.in-memory"/>
                </p:input>
            </px:fileset-load>

            <p:for-each name="validate-dtbooks">
                <p:output port="result" sequence="true">
                    <p:pipe port="result" step="validate-dtbook-group"/>
                </p:output>

                <p:iteration-source select="//pkg:item[@media-type='application/x-dtbook+xml']">
                    <p:pipe step="load-package-doc" port="result"/>
                </p:iteration-source>

                <p:variable name="dtbook-href" select="resolve-uri(*/@href, $base-uri)"/>
                <p:variable name="dtbook-filename" select="tokenize($dtbook-href, '/')[last()]"/>

                <!-- TODO: will this be unique?
                    We should actually relativize $dtbook-href wrt the package file rather than just take the filename part of the path. -->
                <p:variable name="report-filename"
                            select="concat(replace($dtbook-filename, '/', '_'), '-report.xml')"/>

                <p:group name="validate-dtbook-group">
                    <p:output port="result"/>

                    <px:fileset-add-entry media-type="application/x-dtbook+xml">
                        <p:with-option name="href" select="$dtbook-href"/>
                        <p:input port="source.fileset">
                            <p:inline>
                                <d:fileset/>
                            </p:inline>
                        </p:input>
                    </px:fileset-add-entry>

                    <px:dtbook-validate name="validate-dtbook">
                        <p:with-option name="check-images" select="$check-images"/>
                        <p:with-option name="mathml-version" select="$mathml-version"/>
                        <p:with-option name="nimas" select="true()"/>
                    </px:dtbook-validate>

                    <!-- add the report path -->
                    <p:insert position="last-child" match="//d:document-info">
                        <p:input port="insertion">
                            <p:inline>
                                <d:report-path>@@</d:report-path>
                            </p:inline>
                        </p:input>
                        <p:input port="source">
                            <p:pipe step="validate-dtbook" port="xml-report"/>
                        </p:input>
                    </p:insert>
                    <p:string-replace match="d:document-info/d:report-path/text()">
                        <p:with-option name="replace" select="concat('&quot;', $report-filename, '&quot;')"
                        />
                    </p:string-replace>
                </p:group>
            </p:for-each>
            
            <!-- wrap all the document-info elements -->
            <p:wrap-sequence name="wrap-doc-infos" wrapper="results" wrapper-prefix="tmp"
                wrapper-namespace="http://www.daisy.org/ns/pipeline/tmp">
                <p:input port="source" select="//d:document-info">
                    <p:pipe port="result" step="validate-dtbooks"/>
                </p:input>
            </p:wrap-sequence>

            <p:group name="validate-package-doc">
                <p:output port="result"/>
                <p:choose name="validate-package-doc-choose">
                    <p:xpath-context>
                        <p:pipe port="result" step="wrap-doc-infos"/>
                    </p:xpath-context>
                    <!-- did any dtbook docs have mathml? if so, then we will say that this book has mathml.-->
                    <p:when test="//d:document-info[@internal='mathml']">
                        <p:output port="result">
                            <p:pipe port="report" step="run-package-doc-validation"/>
                        </p:output>
                        
                        <pxi:nimas-fileset-validator.validate-package-doc name="run-package-doc-validation">
                            <p:input port="source">
                                <p:pipe step="load-package-doc" port="result"/>
                            </p:input>
                            <p:with-option name="math" select="'true'"/>
                        </pxi:nimas-fileset-validator.validate-package-doc>
                        <p:sink/>
                    </p:when>
                    <p:otherwise>
                        <p:output port="result">
                            <p:pipe port="report" step="run-package-doc-validation"/>
                        </p:output>
                        
                        <pxi:nimas-fileset-validator.validate-package-doc name="run-package-doc-validation">
                            <p:input port="source">
                                <p:pipe step="load-package-doc" port="result"/>
                            </p:input>
                            <p:with-option name="math" select="'false'"/>
                        </pxi:nimas-fileset-validator.validate-package-doc>
                        <p:sink/>
                    </p:otherwise>
                </p:choose>

                <!-- add the report path -->
                <p:insert position="last-child" match="d:document-info">
                    <p:input port="insertion">
                        <p:inline>
                            <d:report-path>@@</d:report-path>
                        </p:inline>
                    </p:input>
                </p:insert>
                <p:string-replace match="d:document-info/d:report-path/text()">
                    <p:with-option name="replace"
                        select="concat('&quot;', $package-doc-filename, '-report.xml&quot;')"/>
                </p:string-replace>
            </p:group>

        </p:when>

        <!-- otherwise, just store a report from the wellformedness check -->
        <p:otherwise>
            <p:output port="dtbook-validation-report" sequence="true">
                <p:empty/>
            </p:output>
            <p:output port="package-doc-validation-report">
                <p:pipe port="result" step="wrap-report"/>
            </p:output>

            <px:message message="Package document is not well-formed">
                <p:input port="source">
                    <p:inline>
                        <p:empty/>
                    </p:inline>
                </p:input>
            </px:message>
            <p:sink/>

            <p:group name="wrap-report">
                <p:output port="result"/>
                <px:combine-validation-reports>
                    <p:with-option name="document-name" select="$package-doc-filename"/>
                    <p:with-option name="document-type" select="'OPF 1.2'"/>
                    <p:with-option name="document-path" select="$base-uri"/>
                    <p:input port="source">
                        <p:pipe port="report" step="check-package-wellformed"/>
                    </p:input>
                </px:combine-validation-reports>
                <!-- add the report path -->
                <p:insert position="last-child" match="d:document-info">
                    <p:input port="insertion">
                        <p:inline>
                            <d:report-path>@@</d:report-path>
                        </p:inline>
                    </p:input>
                </p:insert>
                <p:string-replace match="d:document-info/d:report-path/text()">
                    <p:with-option name="replace"
                        select="concat('&quot;', $package-doc-filename, '-report.xml&quot;')"/>
                </p:string-replace>
            </p:group>

        </p:otherwise>
    </p:choose>
    
    <!-- ***************************************************** -->
    <!-- REPORT(S) TO HTML -->
    <!-- ***************************************************** -->
    
    <p:identity name="xml-reports">
        <p:input port="source">
            <p:pipe step="if-package-wellformed" port="package-doc-validation-report"/>
            <p:pipe step="if-package-wellformed" port="dtbook-validation-report"/>
        </p:input>
    </p:identity>
    
    <px:message message="Nimas fileset validator: Formatting report as HTML."/>
    <px:validation-report-to-html name="html-report" toc="true"/>
    <p:sink/>
    
    <px:validation-status name="validation-status">
        <p:input port="source">
            <p:pipe step="xml-reports" port="result"/>
        </p:input>
    </px:validation-status>
            
</p:declare-step>
