<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:ssml-to-audio" name="main">

  <p:input port="source" primary="true" sequence="true" px:media-type="application/ssml+xml">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>The SSML documents.</p>
      <p>All <code>s</code> elements are expected to have <code>id</code> and <code>xml:lang</code>
      attributes.</p>
      <p>Mark names are expected to be of the form "X___Y", where "X" uniquely identifies the part
      of the sentence before the mark (and after the preceding mark), and where "Y" uniquely
      identifies the part of the sentence after the mark (and before the following mark).</p>
    </p:documentation>
  </p:input>
  <p:input port="config"/>
  <p:output port="result" primary="true">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p><code>d:audio-clips</code> document that lists the generated audio files and maps SSML
      elements (sentences, or parts of sentences before/after/between marks) to audio clips (audio
      file, start time, end time).</p>
    </p:documentation>
  </p:output>
  <p:output port="status">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>
        Status document expressing the success rate of the text-to-speech process. The
        format is an extension of the "<a
        href="http://daisy.github.io/pipeline/StatusXML">application/vnd.pipeline.status+xml</a>"
        format: a <code>d:status</code> element with a <code>result</code> attribute that
        has the value "ok" if there were no errors, or "error" when there was at least one
        error. A <code>tts-success-rate</code> attribute contains the percentage of the SSML
        input that got successfully converted to speech.
      </p>
    </p:documentation>
  </p:output>
  <p:option name="audio-file-type" select="'audio/mpeg'">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>The desired file type of the generated audio files, specified as a MIME type.</p>
      <p>Examples:</p>
      <ul>
        <li>"audio/mpeg" (MP3)</li>
        <li>"audio/x-wav" (WAVE)</li>
      </ul>
    </p:documentation>
  </p:option>
  <p:option name="include-log" select="'false'">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>Whether or not to make the TTS log available on the "log" port.</p>
    </p:documentation>
  </p:option>
  <p:output port="log" sequence="true"/>
  <p:option name="temp-dir" select="''">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>If not empty, this directory will be used to store audio files. The directory must not
      exist yet. Overrides the global <code>org.daisy.pipeline.tts.audio.tmpdir</code> setting.</p>
    </p:documentation>
  </p:option>

  <!--
      Implemented in ../../../java/org/daisy/pipeline/tts/calabash/impl/SynthesizeStep.java
  -->

</p:declare-step>
