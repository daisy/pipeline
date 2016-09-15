<p:library version="1.0" 
    xmlns:c="http://www.w3.org/ns/xproc-step"
    xmlns:cx="http://xmlcalabash.com/ns/extensions"
    xmlns:l="http://xproc.org/library"
    xmlns:p="http://www.w3.org/ns/xproc"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
   >

    <p:documentation>
        <h1 px:role="name">Validation Utilities Library</h1>
        <div px:role="author maintainer">
            <p px:role="name">Romain Deltour</p>
            <a px:role="contact" href="mailto:rdeltour@gmail.com">rdeltour@gmail.com</a>
            <p px:role="organization">DAISY Consortium</p>
        </div>
    </p:documentation>
    
    <p:import href="http://xmlcalabash.com/extension/steps/library-1.0.xpl"/>
    
    <p:import href="combine-validation-reports.xpl">
        <p:documentation>Utility step that combines many validation reports into one XML document.</p:documentation>
    </p:import>
    
    <p:import href="validation-report-to-html.xpl">
        <p:documentation>Step that converts one or more validation reports (created by the above step) into one HTML report.</p:documentation>
    </p:import>
    
    <p:import href="check-files-exist.xpl">
        <p:documentation>Step that, given a list of files, reports whether each exists on disk or not.</p:documentation>
    </p:import>

    <p:import href="validation-status.xpl">
        <p:documentation>Step that, given one or more validation reports, outputs validation status XML (http://code.google.com/p/daisy-pipeline/wiki/ValidationStatusXML).</p:documentation>
    </p:import>
    
    <p:import href="check-files-wellformed.xpl">
        <p:documentation>Step that, given a list of files, reports whether each is well-formed XML.</p:documentation>
    </p:import>
    
    <p:import href="create-validation-report-error.xpl">
        <p:documentation>Create an error for use in Validation Report XML.</p:documentation>
    </p:import>
    
    
    <p:documentation>Performs RELAX NG validation, returning two results: the source document (validated, if validation succeeds) and a report of the validation errors (if any). This step comes from the XProc.org library.</p:documentation>
    <p:declare-step name="main" type="l:relax-ng-report">
        <p:input port="source" primary="true"/>
        <p:input port="schema"/>
        <p:output port="result" primary="true"/>
        <p:output port="report" sequence="true">
            <p:pipe step="try" port="report"/>
        </p:output>
        <p:option name="dtd-attribute-values" select="'false'"/>
        <p:option name="dtd-id-idref-warnings" select="'false'"/>
        <p:option name="assert-valid" select="'false'"/> <!-- yes, false by default! -->
        
        <p:try name="try">
            <p:group>
                <p:output port="result" primary="true">
                    <p:pipe step="v-rng" port="result"/>
                </p:output>
                <p:output port="report">
                    <p:empty/>
                </p:output>
                
                <p:validate-with-relax-ng name="v-rng" assert-valid="true">
                    <p:input port="source">
                        <p:pipe step="main" port="source"/>
                    </p:input>
                    <p:input port="schema">
                        <p:pipe step="main" port="schema"/>
                    </p:input>
                    <p:with-option name="dtd-attribute-values" select="$dtd-attribute-values"/>
                    <p:with-option name="dtd-id-idref-warnings" select="$dtd-id-idref-warnings"/>
                </p:validate-with-relax-ng>
            </p:group>
            <p:catch name="catch">
                <p:output port="result" primary="true">
                    <p:pipe step="copy-source" port="result"/>
                </p:output>
                <p:output port="report">
                    <p:pipe step="copy-errors" port="result"/>
                </p:output>
                <p:identity name="copy-source">
                    <p:input port="source">
                        <p:pipe step="main" port="source"/>
                    </p:input>
                </p:identity>
                <p:identity name="copy-errors">
                    <p:input port="source">
                        <p:pipe step="catch" port="error"/>
                    </p:input>
                </p:identity>
            </p:catch>
        </p:try>
        
        <p:count name="count" limit="1">
            <p:input port="source">
                <p:pipe step="try" port="report"/>
            </p:input>
        </p:count>
        
        <p:choose>
            <p:when test="$assert-valid = 'true' and /c:result != '0'">
                <!-- This isn't very efficient, but it's an error case so that's
           probably ok. In any event, it assures that l:relax-ng-report
           raises the same errors that the validation raises. -->
                <p:validate-with-relax-ng name="v-rng" assert-valid="true">
                    <p:input port="source">
                        <p:pipe step="main" port="source"/>
                    </p:input>
                    <p:input port="schema">
                        <p:pipe step="main" port="schema"/>
                    </p:input>
                    <p:with-option name="dtd-attribute-values" select="$dtd-attribute-values"/>
                    <p:with-option name="dtd-id-idref-warnings" select="$dtd-id-idref-warnings"/>
                </p:validate-with-relax-ng>
            </p:when>
            <p:otherwise>
                <p:identity>
                    <p:input port="source">
                        <p:pipe step="try" port="result"/>
                    </p:input>
                </p:identity>
            </p:otherwise>
        </p:choose>
        
    </p:declare-step>
    
    
    <p:documentation>TBD</p:documentation>
    <p:declare-step name="report-errors" type="px:report-errors">
        <p:input port="source" primary="true"/>
        <p:input port="report" sequence="true"/>
        <p:output port="result" sequence="true"/>
        <p:option name="code" select="''"/>
        <p:option name="code-prefix"/>
        <p:option name="code-namespace"/>
        <!--We count the report docs to simply pipe the identity if there are no errors (used in the choose/when) -->
        <p:count name="count" limit="1">
            <p:input port="source">
                <p:pipe step="report-errors" port="report"/>
            </p:input>
        </p:count>
        <p:sink/>
        <!--We the repipe the primary source port-->
        <p:identity>
            <p:input port="source">
                <p:pipe port="source" step="report-errors"/>
            </p:input>
        </p:identity>
        <p:choose>
            <p:xpath-context>
                <p:pipe port="result" step="count"/>
            </p:xpath-context>
            <p:when test="/c:result = '0'">
                <p:identity/>
            </p:when>
            <p:when test="$code != '' and p:value-available('code-prefix') and p:value-available('code-namespace')">
                <cx:report-errors>
                    <p:input port="report">
                        <p:pipe step="report-errors" port="report"/>
                    </p:input>
                    <p:with-option name="code" select="$code"/>
                    <p:with-option name="code-prefix" select="$code-prefix"/>
                    <p:with-option name="code-namespace" select="$code-namespace"/>
                </cx:report-errors>
            </p:when>
            <p:when test="$code != '' and p:value-available('code-namespace')">
                <cx:report-errors>
                    <p:input port="report">
                        <p:pipe step="report-errors" port="report"/>
                    </p:input>
                    <p:with-option name="code" select="$code"/>
                    <p:with-option name="code-namespace" select="$code-namespace"/>
                </cx:report-errors>
            </p:when>
            <p:when test="$code != '' and p:value-available('code-prefix')">
                <cx:report-errors>
                    <p:input port="report">
                        <p:pipe step="report-errors" port="report"/>
                    </p:input>
                    <p:with-option name="code" select="$code"/>
                    <p:with-option name="code-prefix" select="$code-prefix"/>
                </cx:report-errors>
            </p:when>
            <p:when test="$code != ''">
                <cx:report-errors>
                    <p:input port="report">
                        <p:pipe step="report-errors" port="report"/>
                    </p:input>
                    <p:with-option name="code" select="$code"/>
                </cx:report-errors>
            </p:when>
            <p:otherwise>
                <cx:report-errors>
                    <p:input port="report">
                        <p:pipe step="report-errors" port="report"/>
                    </p:input>
                </cx:report-errors>
            </p:otherwise>
        </p:choose>
    </p:declare-step>
    
    <p:documentation>TBD</p:documentation>
    <p:declare-step name="validate-with-relax-ng-and-report" type="px:validate-with-relax-ng-and-report">
        <p:input port="source" primary="true"/>
        <p:input port="schema"/>
        <p:output port="result" primary="true"/>
        <p:option name="assert-valid" select="'true'"/>
        <p:option name="dtd-attribute-values" select="'false'"/>
        <p:option name="dtd-id-idref-warnings" select="'false'"/>
        
        <l:relax-ng-report name="validate">
            <p:input port="source">
                <p:pipe step="validate-with-relax-ng-and-report" port="source"/>
            </p:input>
            <p:input port="schema">
                <p:pipe step="validate-with-relax-ng-and-report" port="schema"/>
            </p:input>
            <p:with-option name="dtd-attribute-values" select="$dtd-attribute-values"/>
            <p:with-option name="dtd-id-idref-warnings" select="$dtd-id-idref-warnings"/>
        </l:relax-ng-report>
        <px:report-errors code-namespace="http://www.w3.org/ns/xproc-error">
            <p:input port="report">
                <p:pipe step="validate" port="report"/>
            </p:input>
            <p:with-option name="code" select="if ($assert-valid='true') then 'XC0053' else ''" />
        </px:report-errors>
    </p:declare-step>
</p:library>
