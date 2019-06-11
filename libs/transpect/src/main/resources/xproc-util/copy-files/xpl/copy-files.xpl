<p:declare-step version="1.0"
  xmlns:p="http://www.w3.org/ns/xproc"
  xmlns:c="http://www.w3.org/ns/xproc-step"
  xmlns:cx="http://xmlcalabash.com/ns/extensions"
  xmlns:cxf="http://xmlcalabash.com/ns/extensions/fileutils"
  xmlns:tr="http://transpect.io"
  type="tr:copy-files" name="copy-files">

  <!-- Input: one hub document
       Output: same hub document, optionally with changed paths -->
  <p:input port="source" primary="true" sequence="false"/>
  <p:output port="result" primary="true" sequence="true"/>
  
  <!-- remove subpaths for every fileref attribute. no: files will be copied to target-dir-uri directly. -->
  <p:option name="retain-subpaths" required="false" select="'false'"/>
  
  <!-- target-dir-uri: copy files into this directory -->
  <p:option name="target-dir-uri" required="true"/>
  
  <!-- change-uri: modify the fileref attribute of the hub input? default: yes -->
  <p:option name="change-uri" required="false" select="'yes'" />
  
  <!-- change-uri-new-subpath: prefix this string to all filerefs? default: 'media' -->
  <p:option name="change-uri-new-subpath" required="false" select="'media'" />
  
  <!-- fileref-attribute-name-regex: attribute name of the file reference containing attribute? default: '^fileref$' -->
  <p:option name="fileref-attribute-name-regex" required="false" select="'^fileref$'" />

  <!-- fileref-hosting-element-name-regex: element name hosting the fileref attribute. default: see Hub/Docbook -->
  <p:option name="fileref-hosting-element-name-regex" required="false" 
    select="'^(audiodata|imagedata|textdata|videodata)$'" />

  <!-- fail-on-error: default value for basic steps cxf:mkdir and cxf:copy is true. here: false -->
  <p:option name="fail-on-error" required="false" select="'false'"/>

  <!-- debugging options -->
  <p:option name="debug" select="'yes'"/> 
  <p:option name="debug-dir-uri" select="'debug'"/>

  <p:import href="http://xmlcalabash.com/extension/steps/library-1.0.xpl" />
  <p:import href="http://transpect.io/xproc-util/store-debug/xpl/store-debug.xpl"/>

  <p:load name="load-stylesheet" 
    href="http://http://transpect.io/xproc-util/copy-files/xsl/copy-files.xsl"/>

  <p:xslt name="generate-copy-instructions" template-name="create-entries-from-hub">
    <p:with-param name="retain-subpaths" select="$retain-subpaths"/>
    <p:with-param name="target-dir-uri" select="$target-dir-uri"/>
    <p:with-param name="fileref-attribute-name-regex" select="$fileref-attribute-name-regex"/>
    <p:with-param name="fileref-hosting-element-name-regex" select="$fileref-hosting-element-name-regex"/>
    <p:input port="stylesheet">
      <p:pipe port="result" step="load-stylesheet"/>
    </p:input>
    <p:input port="source">
      <p:pipe port="source" step="copy-files"/>
    </p:input>
    <p:input port="parameters">
      <p:empty/>
    </p:input>
  </p:xslt>

  <tr:store-debug name="debug-copy-entries">
    <p:with-option name="pipeline-step" 
      select="concat('copy-files','/01.copy-instructions')"/><!--/', tokenize(base-uri(/*), '/')[last()], '-->
    <p:with-option name="active" select="$debug"/>
    <p:with-option name="base-uri" select="$debug-dir-uri"/>
  </tr:store-debug>

  <p:for-each name="copy-each-file-to-new-location">
    <p:iteration-source select="/c:copy-files/c:entry"/>

    <cxf:mkdir name="create-target-dir">
      <p:with-option name="href" select="string-join(tokenize(/*/@target, '/')[position() != last()], '/')"/>
      <p:with-option name="fail-on-error" select="$fail-on-error"/>
    </cxf:mkdir>

    <p:identity name="identity-current-for-copying">
      <p:input port="source">
        <p:pipe port="current" step="copy-each-file-to-new-location"/>
      </p:input>
    </p:identity>

    <cxf:copy name="just-copy">
      <p:with-option name="href" select="/*/@href"/>
      <p:with-option name="target" select="/*/@target"/>
      <p:with-option name="fail-on-error" select="$fail-on-error"/>
    </cxf:copy>

  </p:for-each>

  <p:choose name="modify-hub-source">
    <p:when test="$change-uri eq 'yes'">
      <p:xslt name="change-uri-in-hub" initial-mode="change-uri">
        <p:with-param name="retain-subpaths" select="$retain-subpaths"/>
        <p:with-param name="change-uri-new-subpath" select="$change-uri-new-subpath"/>
        <p:with-param name="fileref-attribute-name-regex" select="$fileref-attribute-name-regex"/>
        <p:with-param name="fileref-hosting-element-name-regex" select="$fileref-hosting-element-name-regex"/>
        <p:input port="source">
          <p:pipe port="source" step="copy-files"/>
        </p:input>
        <p:input port="stylesheet">
          <p:pipe port="result" step="load-stylesheet"/>
        </p:input>
      </p:xslt>
    </p:when>
    <p:otherwise>
      <p:identity>
        <p:input port="source">
          <p:pipe port="source" step="copy-files"/>
        </p:input>
      </p:identity>
    </p:otherwise>
  </p:choose>

</p:declare-step>