<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                type="px:css-speech-cascade" name="main"
                exclude-inline-prefixes="#all">

  <p:documentation xmlns="http://www.w3.org/1999/xhtml">
    <p>CSS cascading and inlining of CSS Aural style sheets</p>
    <p>The inlining is done through special <code>@tts:*</code> attributes for each of the
    properties.</p>
    <p>Style sheets can be attached to the source or provided through the "config" port.</p>
  </p:documentation>

  <p:input port="source.fileset" primary="true"/>
  <p:input port="source.in-memory" sequence="true">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>Style sheets can be associated with the source in several ways: linked (using an
      <code>xml-stylesheet</code> processing instruction or a <code>link</code> element) and/or
      inlined (using <code>style</code> attributes).</p>
    </p:documentation>
  </p:input>
  <p:input port="config">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>Style sheets are specified using a <code>css</code> element with text content, see <a
      href="http://daisy.github.io/pipeline/Get-Help/User-Guide/Text-To-Speech/#css">TTS
      configuration</a>.</p>
    </p:documentation>
  </p:input>
  <p:output port="result.fileset" primary="true"/>
  <p:output port="result.in-memory" sequence="true">
    <p:pipe step="update" port="result.in-memory"/>
  </p:output>

  <p:option name="content-type" required="false" select="''">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>The type of document to be processed. Other input documents will be left unchanged. If no
      content-type is provided, all the documents will be processed.</p>
    </p:documentation>
  </p:option>

  <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
    <p:documentation>
      px:fileset-load
      px:fileset-update
    </p:documentation>
  </p:import>
  <p:import href="inline-css.xpl">
    <p:documentation>
      pxi:inline-css
    </p:documentation>
  </p:import>
  <p:import href="clean-up-namespaces.xpl">
    <p:documentation>
      pxi:clean-up-namespaces
    </p:documentation>
  </p:import>

  <p:variable name="style-ns" select="'http://www.daisy.org/ns/pipeline/tts'"/>

  <px:fileset-load name="load">
    <p:input port="in-memory">
      <p:pipe step="main" port="source.in-memory"/>
    </p:input>
    <p:with-option name="media-types" select="$content-type"/>
  </px:fileset-load>

  <p:for-each name="inline-css">
    <p:output port="result" sequence="true"/>
    <pxi:inline-css>
      <p:input port="config">
        <p:pipe port="config" step="main"/>
      </p:input>
      <p:with-option name="style-ns" select="$style-ns">
        <p:empty/>
      </p:with-option>
    </pxi:inline-css>
    <pxi:clean-up-namespaces name="clean-up"/>
  </p:for-each>
  <p:sink/>

  <px:fileset-update name="update">
    <p:input port="source.fileset">
      <p:pipe step="main" port="source.fileset"/>
    </p:input>
    <p:input port="source.in-memory">
      <p:pipe step="main" port="source.in-memory"/>
    </p:input>
    <p:input port="update.fileset">
      <p:pipe step="load" port="result.fileset"/>
    </p:input>
    <p:input port="update.in-memory">
      <p:pipe step="inline-css" port="result"/>
    </p:input>
  </px:fileset-update>

</p:declare-step>
