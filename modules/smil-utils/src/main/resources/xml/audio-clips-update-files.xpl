<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:audio-clips-update-files" name="main">

  <p:documentation xmlns="http://www.w3.org/1999/xhtml">
    <p>Update references in a d:audio-clips document according to a mapping provided through a
    d:fileset document.</p>
  </p:documentation>

  <p:input port="source" primary="true">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>The input d:audio-clips document</p>
      <p>It is expected to have no two clips with the same textref.</p>
    </p:documentation>
  </p:input>

  <p:input port="mapping">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>The d:fileset document</p>
    </p:documentation>
  </p:input>

  <p:option name="output-base-uri" required="false">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>The base URI of the output document</p>
      <p>If not specified, the base URI gets inherited from the mapping document.</p>
    </p:documentation>
  </p:option>

  <p:output port="result">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>The output d:audio-clips document</p>
      <p>References to audio file (<code>src</code> attributes) and text fragments
      (<code>textref</code> attributes) files are updated according to the mapping from
      <code>@original-href</code> to <code>@href</code> and <code>@original-id</code> to
      <code>@id</code> in the "mapping" document. All references are relative to the base URI of the
      output document.</p>
    </p:documentation>
  </p:output>

  <p:choose>
      <p:when test="p:value-available('output-base-uri')">
          <p:add-attribute match="/*" attribute-name="xml:base">
              <p:input port="source">
                  <p:inline><_/></p:inline>
              </p:input>
              <p:with-option name="attribute-value" select="$output-base-uri"/>
          </p:add-attribute>
      </p:when>
      <p:otherwise>
          <p:identity>
              <p:input port="source">
                  <p:pipe step="main" port="mapping"/>
              </p:input>
          </p:identity>
      </p:otherwise>
  </p:choose>
  <p:identity name="output-base-uri"/>
  <p:sink/>

  <p:xslt>
      <p:input port="source">
          <p:pipe step="main" port="source"/>
          <p:pipe step="main" port="mapping"/>
      </p:input>
      <p:input port="stylesheet">
          <p:document href="audio-clips-update-files.xsl"/>
      </p:input>
      <p:with-param name="output-base-uri" select="base-uri(/*)">
          <p:pipe step="output-base-uri" port="result"/>
      </p:with-param>
      <p:with-option name="output-base-uri" select="base-uri(/*)">
          <p:pipe step="output-base-uri" port="result"/>
      </p:with-option>
  </p:xslt>

</p:declare-step>
