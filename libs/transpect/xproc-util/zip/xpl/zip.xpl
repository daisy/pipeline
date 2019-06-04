<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
    xmlns:c="http://www.w3.org/ns/xproc-step"
	  xmlns:pxf="http://exproc.org/proposed/steps/file"
	  xmlns:pxp="http://exproc.org/proposed/steps"
	  xmlns:tr="http://transpect.io"
	  xmlns:cx="http://xmlcalabash.com/ns/extensions"
	  version="1.0"
    type="tr:zip"
    name="zip">
  
  <p:documentation>
    Extends the pxp:zip step to check whether all in a manifest referenced items are available.
  </p:documentation>
  
	<p:input port="source" primary="true">
	  <p:documentation>A zip manifest</p:documentation>
	</p:input>
  
  <p:output port="result">
    <p:pipe port="result" step="zipping"/>
  </p:output>
  
 	<p:output port="report" sequence="true">
 	  <p:pipe port="result" step="wrap-missing-zip-file-errors"/>
 	</p:output>
  
  <p:option name="debug" select="'no'"/>
  <p:option name="debug-dir-uri" select="'debug'"/>
  <p:option name="command" select="'create'" required="false"/>
  <p:option name="compression-method" select="'deflated'" required="false"/>
  <p:option name="compression-level" select="'default'" required="false"/>
  <p:option name="href" required="true">
    <p:documentation>URI where the zip is created</p:documentation>
  </p:option>
  
  <p:viewport match="c:entry" name="zip-entries">
    <p:viewport-source>
      <p:pipe port="source" step="zip"/>
    </p:viewport-source>
    <p:try name="try-entry">
      <p:group>
        <pxf:info name="zip-entry-info">
          <p:with-option name="href" select="/c:entry/@href"/>
        </pxf:info>
        <p:identity>
          <p:input port="source">
            <p:pipe port="current" step="zip-entries"/>
          </p:input>
        </p:identity>
      </p:group>
      <p:catch name="catch-entry">
        <p:identity>
          <p:input port="source">
            <p:pipe port="error" step="catch-entry"/>
          </p:input>
        </p:identity>
        <p:group>
          <p:variable name="previous-message" select="string(/c:errors/c:error)"/>
          <p:string-replace match="c:error/text()">
            <p:with-option name="replace" 
              select="concat('''', $previous-message, ': Cannot open ',  /c:entry/@href, ' for zip entry ', /c:entry/@name, '''')">
              <p:pipe port="current" step="zip-entries"/>
            </p:with-option>
          </p:string-replace>
        </p:group>
      </p:catch>
    </p:try>
  </p:viewport>
  
  <tr:store-debug pipeline-step="zip/zip-manifest-maybe-with-errors" name="store-zip-manifest0">
    <p:with-option name="active" select="$debug"/>
    <p:with-option name="base-uri" select="$debug-dir-uri"/>
  </tr:store-debug>
  
  <p:delete match="c:errors" name="zip-manifest-existing-only"/>

  <tr:store-debug pipeline-step="zip/zip-manifest" name="store-zip-manifest">
    <p:with-option name="active" select="$debug"/>
    <p:with-option name="base-uri" select="$debug-dir-uri"/>
  </tr:store-debug>

  <p:sink name="sink2"/>

  <p:wrap-sequence name="wrap-missing-zip-file-errors0" wrapper="c:errors">
    <p:input port="source" select="//c:error[@code]">
      <p:pipe port="result" step="store-zip-manifest0"/>
    </p:input>
  </p:wrap-sequence>
  
  <p:add-attribute attribute-name="tr:rule-family" name="wrap-missing-zip-file-errors" match="/*" 
    attribute-value="zip-input-missing"/>
  
  <p:sink name="sink3"/>

  <pxp:zip name="zipping" cx:depends-on="wrap-missing-zip-file-errors">
    <p:input port="source">
      <p:empty/>
    </p:input>
    <p:input port="manifest">
      <p:pipe port="result" step="zip-manifest-existing-only"/>
    </p:input>
    <p:with-option name="href" select="$href"/>
    <p:with-option name="command" select="$command"/>
    <p:with-option name="compression-method" select="$compression-method"/>
    <p:with-option name="compression-level" select="$compression-level"/>
  </pxp:zip>
  
</p:declare-step>