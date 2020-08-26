<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:tts="http://www.daisy.org/ns/pipeline/tts"
                type="px:css-speech-clean"
                exclude-inline-prefixes="#all">

  <p:documentation>
    Delete the attributes added by the CSS speech inlining step.
  </p:documentation>

  <p:input port="source" primary="true"/>
  <p:output port="result" primary="true"/>

  <p:import href="clean-up-namespaces.xpl"/>

  <p:delete match="@tts:*"/>
  <pxi:clean-up-namespaces/>

</p:declare-step>
