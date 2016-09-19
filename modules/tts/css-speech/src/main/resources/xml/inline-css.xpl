<p:declare-step type="px:inline-css"
		version="1.0" xmlns:p="http://www.w3.org/ns/xproc"
		xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
		exclude-inline-prefixes="#all">

  <p:input port="source" primary="true" sequence="true" />
  <p:input port="config"/>
  <p:output port="result" primary="true" sequence="true" />

  <p:option name="style-ns" required="false" select="'http://www.daisy.org/ns/pipeline/tmp'"/>

</p:declare-step>