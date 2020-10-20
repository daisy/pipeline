<p:declare-step type="px:reshape"
		name="main"
		version="1.0" xmlns:p="http://www.w3.org/ns/xproc"
		xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
		exclude-inline-prefixes="#all">

  <p:option name="can-contain-sentences" required="true"/>
  <p:option name="cannot-be-sentence-child" required="false" select="''"/>
  <p:option name="special-sentences" required="false" select="''"/>
  <p:option name="output-word-tag" required="true"/>
  <p:option name="output-sentence-tag" required="true"/>
  <p:option name="word-attr" required="false" select="''"/>
  <p:option name="word-attr-val" required="false" select="''"/>
  <p:option name="output-ns" required="true"/>
  <p:option name="output-subsentence-tag" required="true"/>
  <p:option name="tmp-ns" select="'http://www.daisy.org/ns/pipeline/tmp'"/>
  <p:option name="tmp-word-tag" select="'ww'"/>
  <p:option name="tmp-sentence-tag" select="'ss'"/>
  <p:option name="exclusive-word-tag" select="'true'"/>
  <p:option name="exclusive-sentence-tag" select="'true'"/>
  <p:option name="id-prefix" required="false" select="''"/>

  <p:input port="source" primary="true"/>
  <p:output port="result" primary="true"/>
  <p:output port="sentence-ids">
    <p:pipe port="secondary" step="create-valid"/>
  </p:output>

  <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
    <p:documentation>
      px:set-base-uri
    </p:documentation>
  </p:import>

  <!-- Distribute some sentences to prevent them from having parents
       not compliant with the format. -->
  <p:xslt name="distribute">
    <p:with-option name="output-base-uri" select="base-uri(/*)"/>
    <!-- The output-sentence-tag is added so as to accept words which
         are children of a temporary sentence which is in turn the
         child of an existing sentence. -->
    <p:with-param name="can-contain-sentences"
		  select="concat($can-contain-sentences, ',', $output-sentence-tag)"/>
    <p:with-param name="cannot-be-sentence-child" select="$cannot-be-sentence-child"/>
    <p:with-param name="tmp-word-tag" select="$tmp-word-tag"/>
    <p:with-param name="tmp-sentence-tag" select="$tmp-sentence-tag"/>
    <p:with-param name="tmp-ns" select="$tmp-ns"/>
    <p:input port="stylesheet">
      <p:document href="distribute-sentences.xsl"/>
    </p:input>
  </p:xslt>

  <!-- Create the actual sentence/word elements. -->
  <p:xslt name="create-valid">
    <p:with-param name="can-contain-words" select="$can-contain-sentences"/>
    <p:with-param name="special-sentences" select="$special-sentences"/>
    <p:with-param name="tmp-word-tag" select="$tmp-word-tag"/>
    <p:with-param name="tmp-sentence-tag" select="$tmp-sentence-tag"/>
    <p:with-param name="output-word-tag" select="$output-word-tag"/>
    <p:with-param name="output-sentence-tag" select="$output-sentence-tag"/>
    <p:with-param name="word-attr" select="$word-attr"/>
    <p:with-param name="word-attr-val" select="$word-attr-val"/>
    <p:with-param name="output-ns" select="$output-ns"/>
    <p:with-param name="output-subsentence-tag" select="$output-subsentence-tag"/>
    <p:with-param name="exclusive-word-tag" select="$exclusive-word-tag"/>
    <p:with-param name="exclusive-sentence-tag" select="$exclusive-sentence-tag"/>
    <p:with-param name="id-prefix" select="$id-prefix"/>
    <p:input port="stylesheet">
      <p:document href="create-valid-breaks.xsl"/>
    </p:input>
  </p:xslt>
  <px:set-base-uri>
    <!-- not sure why this is needed -->
    <p:with-option name="base-uri" select="base-uri(/*)">
      <p:pipe step="main" port="source"/>
    </p:with-option>
  </px:set-base-uri>

</p:declare-step>
