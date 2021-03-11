<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:ssml-to-audio" name="main">

  <p:input port="source" primary="true" sequence="true" px:media-type="application/ssml+xml"/>
  <p:input port="config"/>
  <p:output port="result" primary="true" />
  <p:output port="status">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>
        Status document expressing the success rate of the text-to-speech process. The
        format is an extension of the "<a
        href="http://daisy.github.io/pipeline/StatusXML">application/vnd.pipeline.status+xml</a>"
        format: a <code>d:status</code> element with a <code>result</code> attribute that
        has the value "ok" if there were no errors, or "error" when there was at least one
        error. A <code>success-rate</code> attribute contains the percentage of the SSML
        input that got successfully converted to speech.
      </p>
    </p:documentation>
  </p:output>
  <p:output port="log" sequence="true"/>
  <p:option name="temp-dir" select="''">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>If not empty, this directory will be used to store audio files. The directory must not
      exist yet. Overrides the global <code>org.daisy.pipeline.tts.audio.tmpdir</code> setting.</p>
    </p:documentation>
  </p:option>

  <!--
      Implemented in ../../../java/org/daisy/pipeline/tts/synthesize/calabash/impl/SynthesizeStep.java
  -->

</p:declare-step>
