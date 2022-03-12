<?xml version="1.0" encoding="UTF-8"?>
<p:library xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
           xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
           xmlns:html="http://www.w3.org/1999/xhtml"
           xmlns:ssml="http://www.w3.org/2001/10/synthesis">

  <p:declare-step type="px:html-break-detect">

    <p:documentation>Break an input XHTML document into words and sentences by inserting word and sentence elements.</p:documentation>

    <p:input port="source" primary="true"/>
    <p:output port="result" primary="true"/>
    <p:output port="sentence-ids">
      <p:pipe port="sentence-ids" step="generic"/>
    </p:output>

    <p:option name="id-prefix" required="false" select="''"/>

    <p:option name="sentence-attr" required="false" select="''">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Attribute to be added to sentence spans.</p>
      </p:documentation>
    </p:option>

    <p:option name="sentence-attr-val" required="false" select="''">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Corresponding attribute value.</p>
      </p:documentation>
    </p:option>

    <p:import href="http://www.daisy.org/pipeline/modules/nlp-common/library.xpl">
      <p:documentation>
        px:break-and-reshape
      </p:documentation>
    </p:import>

    <!-- Based on the containers of phrasing, flow and transparent content of HTML5, the
         "can-contain-sentences" elements are the following: -->
    <!-- ssml:phoneme is not valid HTML5 or EPUB but is included as experimental feature -->
    <px:break-and-reshape name="generic"
        inline-tags="html:span|html:i|html:b|html:a|html:br|html:del|html:font|html:ruby|html:s|
                     html:small|html:strike|html:strong|html:sup|html:sub|html:u|html:q|html:address|
                     html:abbr|html:em|html:style|ssml:phoneme"
        ensure-word-before="html:span|html:br|html:ruby|html:s|html:address|html:abbr|html:style"
        ensure-word-after="span|html:br|html:ruby|html:s|html:address|html:abbr|html:style"
        can-contain-sentences="html:body|html:section|html:nav|html:article|html:aside|html:h1|html:h2|
                               html:h3|html:h4|html:h5|html:h6|html:header|html:footer|html:address|
                               html:p|html:pre|html:blockquote|html:li|html:dt|html:dd|html:a|html:q|
                               html:cite|html:em|html:strong|html:small|html:mark|html:dfn|html:abbr|
                               html:time|html:progress|html:meter|html:code|html:var|html:samp|html:kdb|
                               html:sub|html:sup|html:span|html:i|html:b|html:bdo|html:rt|html:ins|
                               html:del|html:caption|html:figcaption|html:td|html:th|html:form|html:label|
                               html:input|html:button|html:datalist|html:output|html:bb|html:menu|
                               html:legend|html:div"
        output-ns="http://www.w3.org/1999/xhtml"
        output-word-tag="span"
        word-attr="role"
        word-attr-val="word"
        output-sentence-tag="span"
        output-subsentence-tag="span"
        exclusive-sentence-tag="false"
        exclusive-word-tag="false">
      <p:with-option name="id-prefix" select="$id-prefix"/>
      <p:with-option name="sentence-attr" select="$sentence-attr"/>
      <p:with-option name="sentence-attr-val" select="$sentence-attr-val"/>
    </px:break-and-reshape>

  </p:declare-step>

  <p:declare-step type="px:html-unwrap-words">
    <p:documentation>Remove the word markups from the input document.</p:documentation>
    <p:input port="source" primary="true"/>
    <p:output port="result" primary="true"/>
    <p:unwrap match="html:span[@role='word' and not(@* except @role)]"/>
    <p:delete match="html:span/@role[.='word']"/>
  </p:declare-step>

</p:library>
