# htmlreports
XProc steps for RelaxNG and Schematron validation and HTML reports

## tr:patch-svrl

### Synopsis

```xml
<tr:patch-svrl name="htmlreport" xmlns:tr="http://transpect.io">
    <p:input port="source" primary="true"/>                         <!-- HTML document with @srcpath attributes -->
    <p:input port="reports" sequence="true" primary="false"/>       <!-- reports as c:errors and svrl:schematron-output -->
    <p:input port="params" primary="false"/>                        <!-- parameters, expects a c:param-set -->
    <p:output port="result" primary="true"/>                        <!-- HTML report -->
    <p:output port="secondary" sequence="true" primary="false"/>    <!-- collected messages as tr:message -->
    <p:with-option name="report-title" select="'my report title'"/> <!-- 'xs:string' -->
    <p:with-option name="show-adjusted-srcpath" select="'yes'"/>    <!-- 'yes'|'no' -->
    <p:with-option name="show-step-name" select="'no'"/>            <!-- 'yes'|'no' -->
    <p:with-option name="severity-default-name" select="'warning'"/><!-- xs:string -->
    <p:with-option name="debug" select="'yes'"/>                    <!-- 'yes'|'no' -->
    <p:with-option name="debug-dir-uri"/>                           <!-- URI -->
    <p:with-option name="status-dir-uri"/>                          <!-- URI -->
  </tr:patch-svrl>
```

### Import URI

`http://transpect.io/htmlreports/xpl/patch-svrl.xpl`

### Dependencies

* https://github.com/transpect/xproc-util
* https://github.com/transpect/xslt-util
* https://github.com/transpect/schematron

### Description

This step patches an error report(s) into an HTML document. Permitted formats for the error report are SVRL, XProc c:errors or both combined within a c:reports document. To provide a name, the top-level element should carry an `@tr:rule-family` attribute

