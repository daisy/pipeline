<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:l="http://xproc.org/library"
                type="px:validate-with-relax-ng-and-report"
                name="validate-with-relax-ng-and-report">

    <p:input port="source" primary="true"/>
    <p:input port="schema"/>
    <p:output port="result" primary="true"/>
    <p:option name="assert-valid" select="'true'"/>
    <p:option name="dtd-attribute-values" select="'false'"/>
    <p:option name="dtd-id-idref-warnings" select="'false'"/>

    <p:import href="relax-ng-report.xpl">
        <p:documentation>
            l:relax-ng-report
        </p:documentation>
    </p:import>
    <p:import href="report-errors.xpl">
        <p:documentation>
            px:report-errors
        </p:documentation>
    </p:import>

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
