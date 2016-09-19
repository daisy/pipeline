<p:declare-step version="1.0" type="px:rm-audio-files"
		xmlns:p="http://www.w3.org/ns/xproc"
		xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
		xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal">

  <p:input port="source" primary="true" /> <!-- audio clips -->
  <p:output port="result" primary="false" sequence="true">
    <p:empty/>
  </p:output>

  <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl"/>
  <p:import href="internal/list-audio-files.xpl"/>

  <pxi:list-audio-files/>
  <p:for-each>
    <p:iteration-source select="//*[@src]"/>
    <px:delete>
      <p:with-option name="href" select="/*/@src"/>
    </px:delete>
  </p:for-each>

</p:declare-step>
