<p:declare-step type="px:remove-inline-css-speech" version="1.0" name="main"
		xmlns:p="http://www.w3.org/ns/xproc"
		xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
		xmlns:tts="http://www.daisy.org/ns/pipeline/tts"
		exclude-inline-prefixes="#all">

  <p:documentation>
    Delete the attributes added by the CSS speech inlining step.
  </p:documentation>

  <p:input port="source" primary="true"/>
  <p:output port="result" primary="true"/>

  <p:import href="clean-up-namespaces.xpl"/>

  <p:delete match="@tts:*"/>
  <px:clean-up-namespaces/>

</p:declare-step>
