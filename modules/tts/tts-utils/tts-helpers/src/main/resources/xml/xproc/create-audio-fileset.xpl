<p:declare-step version="1.0" type="px:create-audio-fileset" name="main"
		xmlns:p="http://www.w3.org/ns/xproc"
		xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
		xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal">

  <p:input port="source" primary="true" /> <!-- audio clips -->
  <p:output port="fileset.out" sequence="false" primary="true">  <!-- fileset of audio files -->
    <p:pipe port="result" step="fileset.result"/>
  </p:output>
  <p:output port="result" sequence="false">  <!-- modified audio map if $anti-conflict-prefix is provided -->
    <p:pipe port="result" step="new-audio-map"/>
  </p:output>

  <p:option name="output-dir" required="true"/>
  <p:option name="audio-relative-dir" required="true"/>
  <p:option name="anti-conflict-prefix" required="false" select="''"/>

  <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
  <p:import href="internal/list-audio-files.xpl"/>

  <p:variable name="audio-dir" select="concat($output-dir, $audio-relative-dir)">
    <p:empty/>
  </p:variable>

  <!-- Iterate over the sound clips of the audio-map. -->
  <pxi:list-audio-files/>
  <p:for-each name="for-each-audio">
    <p:iteration-source select="//*[@src]"/>
    <p:variable name="former-src" select="/*/@src">
      <p:pipe port="current" step="for-each-audio"/>
    </p:variable>
    <p:variable name="new-src" select="concat($audio-dir, $anti-conflict-prefix, tokenize($former-src, '[\\/]+')[last()])"/>
    <px:fileset-create/>
    <!-- TODO: deal with other format than audio/mpeg -->
    <px:fileset-add-entry first="true" media-type="audio/mpeg">
      <p:with-option name="href" select="$new-src" />
      <p:with-option name="original-href" select="$former-src" />
    </px:fileset-add-entry>
  </p:for-each>
  <px:fileset-join name="fileset.result"/>

  <p:string-replace match="@src" name="new-audio-map">
    <p:input port="source">
      <p:pipe port="source" step="main"/>
    </p:input>
    <p:with-option name="replace"
		   select="concat('replace(., ''/([^/]+)$'', ''/', $anti-conflict-prefix, '$1'')')"/>
  </p:string-replace>

</p:declare-step>
