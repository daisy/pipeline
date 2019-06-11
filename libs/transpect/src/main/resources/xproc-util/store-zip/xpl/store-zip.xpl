<p:declare-step version="1.0"
  xmlns:p="http://www.w3.org/ns/xproc"
  xmlns:c="http://www.w3.org/ns/xproc-step"
  xmlns:cx="http://xmlcalabash.com/ns/extensions"
  xmlns:tr="http://transpect.io"
  type="tr:store-zip" 
  name="store-zip">

  <!-- Input: sequences of Hub and/or c:zip-manifest documents
       Output: c:zipfile xml document (and zipped file on storage) -->
  <p:input port="source" primary="true" sequence="true">
    <p:inline>
      <c:zip-manifest/>
    </p:inline>
  </p:input>
  <p:output port="result" primary="true" sequence="false"/>

  <!-- uri where the resulting zip will be stored -->
  <p:option name="target-zip-uri" required="true"/>
  <!-- default values, see http://xmlcalabash.com/docs/reference/pxp-zip.html-->
  <p:option name="default-compression-method" required="false" select="'deflated'"/>
  <p:option name="default-compression-level" required="false" select="'default'"/>
  <p:option name="default-command" required="false" select="'update'"/>
  <!-- optional 'additional-uris-to-zip-root':
       ' file:'-separated list of uri´s which will be stored in root zip directory -->
  <p:option name="additional-file-uris-to-zip-root" required="false" select="''"/>

  <!-- debugging options -->
  <p:option name="debug" select="'yes'"/> 
  <p:option name="debug-dir-uri" select="'debug'"/>

  <p:import href="http://xmlcalabash.com/extension/steps/library-1.0.xpl" />
  <p:import href="http://transpect.io/xproc-util/store-debug/xpl/store-debug.xpl"/>
  
  <p:for-each>
    <p:choose name="manifest-entries">
      <p:when test="name(/*) eq 'c:zip-manifest' and //c:entry">
        <p:identity/>
      </p:when>
      <p:otherwise>
        <p:xslt name="generate-entries-from-hub" template-name="create-manifest-from-hub">
          <p:with-param name="additional-filerefs-to-zip-root" select="$additional-file-uris-to-zip-root"/>
          <p:with-param name="default-compression-level" select="$default-compression-level"/>
          <p:with-param name="default-compression-method" select="$default-compression-method"/>
          <p:with-param name="default-command" select="$default-command"/>
          <p:input port="stylesheet">
            <p:document href="http://transpect.io/xproc-util/store-zip/xsl/store-zip.xsl"/>
          </p:input>
          <p:input port="parameters">
            <p:empty/>
          </p:input>
        </p:xslt>
      </p:otherwise>
    </p:choose>
  <!-- output: a sequence of entries -->
  </p:for-each>

  <p:wrap-sequence name="wrap-root-zip-manifest" wrapper="c:zip-manifest"/>
  
  <p:add-attribute name="add-zip-uri-attr" match="/*" attribute-name="target-zip-uri">
    <p:with-option name="attribute-value" select="$target-zip-uri"/>
  </p:add-attribute>

  <p:unwrap name="dissolve-inner-zip-manifests" match="c:zip-manifest[ancestor::c:zip-manifest]"/>

  <tr:store-debug pipeline-step="store-zip/01.zip-manifest">
    <p:with-option name="active" select="$debug"/>
    <p:with-option name="base-uri" select="$debug-dir-uri"/>
  </tr:store-debug>

  <p:choose name="integrity-test">
    <p:when test="not(//c:entry)">
      <p:identity>
        <p:input port="source">
          <p:inline>
            <c:zipfile>store-zip: nothing to zip / no entries</c:zipfile>
          </p:inline>
        </p:input>
      </p:identity>
    </p:when>
    <p:otherwise>
      <cx:zip command="create" name="zip">
        <p:with-option name="href" select="$target-zip-uri"/>
        <p:input port="source">
          <p:empty/>
        </p:input>
        <p:input port="manifest">
          <p:pipe step="dissolve-inner-zip-manifests" port="result"/>
        </p:input>
      </cx:zip>
    </p:otherwise>
  </p:choose>

  <tr:store-debug pipeline-step="store-zip/02.result">
    <p:with-option name="active" select="$debug"/>
    <p:with-option name="base-uri" select="$debug-dir-uri"/>
  </tr:store-debug>

</p:declare-step>