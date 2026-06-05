<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                type="px:isolate-skippable" name="main">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>Break up sentences into sub-sentences that do not contain skippable elements.</p>
    </p:documentation>

    <p:input port="source" primary="true">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>The input document</p>
      </p:documentation>
    </p:input>
    <p:input port="sentence-ids">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>List of sentences as a <code>d:sentences</code> document with a <code>d:sentence</code>
        child for each sentence. The <code>d:sentence</code> elements point to the respective DTBook
        elements with an <code>id</code> attribute.</p>
      </p:documentation>
    </p:input>

    <p:option name="match" required="true">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        XSLT match pattern to select the skippable elements.
      </p:documentation>
    </p:option>

    <p:output port="result" primary="true">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>The result document.</p>
        <p>For every sentence element that contains one or more skippable elements, that is
        <code>pagenum</code>, <code>noteref</code>, <code>annoref</code>, <code>linenum</code> or
        <code>math</code>,</p>
        <ul>
          <li>the skippable elements do not share any ancestors with other nodes in the sentence
          (nodes that are not themselves ancestors of the skippables), and</li>
          <li>the sentence element has no (non-whitespace-only) child text nodes.</li>
        </ul>
        <p>The reading order is preserved, and apart from elements that are broken up and wrapper
        <code>span</code> elements that are inserted, the structure of the DTBook is preserved.</p>
        <p>Wrapper <code>span</code> elements</p> are given a unique <code>id</code> attribute.
      </p:documentation>
    </p:output>
    <p:output port="skippable-ids">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>List of skippable elements as a <code>d:skippables</code> document with a
        <code>d:skippable</code> child for each skippable element. The <code>d:skippable</code>
        elements point to the respective DTBook elements with an <code>id</code> attribute.</p>
      </p:documentation>
      <p:pipe step="skippables" port="result"/>
    </p:output>

    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
      <p:documentation>
        px:add-ids
      </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
      <p:documentation>
        px:set-base-uri
      </p:documentation>
    </p:import>

    <p:add-attribute attribute-name="skippable" attribute-value="skippable">
      <!-- assumes that input does not already have attributes named "skippable" -->
      <p:with-option name="match" select="$match"/>
    </p:add-attribute>
    <px:add-ids name="add-ids-for-skippables" match="*[@skippable]">
      <p:documentation>Make sure that skippable elements have an id attribute.</p:documentation>
    </px:add-ids>
    <p:for-each>
      <p:iteration-source select="//*[@skippable]"/>
      <p:add-attribute match="/*" attribute-name="id">
        <p:input port="source">
          <p:inline><d:skippable/></p:inline>
        </p:input>
        <p:with-option name="attribute-value" select="/*/@id"/>
      </p:add-attribute>
    </p:for-each>
    <p:wrap-sequence wrapper="d:skippables"/>
    <px:set-base-uri name="skippables">
      <p:documentation>Give the skippable IDs document the same base URI as the source and result documents.</p:documentation>
      <p:with-option name="base-uri" select="base-uri(/*)">
        <p:pipe step="main" port="source"/>
      </p:with-option>
    </px:set-base-uri>
    <p:sink/>

    <p:xslt>
      <p:input port="source">
        <p:pipe step="add-ids-for-skippables" port="result"/>
        <p:pipe port="sentence-ids" step="main"/>
      </p:input>
      <p:input port="stylesheet">
        <p:document href="isolate-skippable.xsl"/>
      </p:input>
      <p:input port="parameters">
        <p:empty/>
      </p:input>
    </p:xslt>

</p:declare-step>
