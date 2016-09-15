<p:declare-step type="px:isolate-daisy3-skippable" version="1.0" name="main"
		xmlns:p="http://www.w3.org/ns/xproc"
		xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
		xmlns:dt="http://www.daisy.org/z3986/2005/dtbook/">

    <p:input port="source" primary="true"/>
    <p:input port="sentence-ids"/>

    <p:output port="result" primary="true">
      <p:pipe port="result" step="isolate-result"/>
    </p:output>
    <p:output port="skippable-ids">
      <p:pipe port="result" step="skippable"/>
    </p:output>

    <p:option name="id-prefix" select="''"/>

    <p:xslt name="isolate-xslt">
      <p:input port="source">
	<p:pipe port="source" step="main"/>
	<p:pipe port="sentence-ids" step="main"/>
      </p:input>
      <p:input port="stylesheet">
	<p:document href="isolate-daisy3-skippable.xsl"/>
      </p:input>
      <p:with-param name="id-prefix" select="$id-prefix"/>
    </p:xslt>
    <p:delete match="dt:skippable" name="isolate-result"/>

    <p:identity>
      <p:input port="source" select="//dt:skippable">
	<p:pipe port="result" step="isolate-xslt"/>
      </p:input>
    </p:identity>
    <p:wrap-sequence name="skippable" wrapper="dt:skippables"/>

</p:declare-step>
