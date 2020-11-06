<p:library xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
           xmlns:xd="http://github.com/vojtechtoman/xprocdoc"
           exclude-inline-prefixes="#all">

  <p:declare-step type="xd:summary-to-xhtml" name="summary-to-xhtml">
    <p:input port="source" primary="true"/>
    <p:input port="stylesheet">
      <p:document href="xd2html.xsl"/>
    </p:input>
    <p:input port="parameters" kind="parameter"/>
    <p:output port="result" sequence="true"/>

    <p:xslt version="2.0" name="xslt">
      <p:input port="stylesheet">
        <p:pipe step="summary-to-xhtml" port="stylesheet"/>
      </p:input>
    </p:xslt>
    <p:sink/>

    <p:for-each>
      <p:iteration-source>
        <p:pipe step="xslt" port="secondary"/>
      </p:iteration-source>
      <p:store name="store">
        <p:with-option name="href" select="p:base-uri()"/>
      </p:store>
      <p:identity>
        <p:input port="source">
          <p:pipe step="store" port="result"/>
        </p:input>
      </p:identity>
    </p:for-each>
  </p:declare-step>

</p:library>
