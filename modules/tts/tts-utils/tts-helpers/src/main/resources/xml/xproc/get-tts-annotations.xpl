<p:declare-step type="px:get-tts-annotations" version="1.0"
		xmlns:p="http://www.w3.org/ns/xproc"
		xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
		exclude-inline-prefixes="#all">

  <p:input port="config" primary="true"/>
  <p:output port="result" primary="true" sequence="true"/>

  <p:option name="content-type"/>

</p:declare-step>
