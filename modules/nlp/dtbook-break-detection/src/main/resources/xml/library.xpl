<?xml version="1.0" encoding="UTF-8"?>
<p:library version="1.0"
    xmlns:p="http://www.w3.org/ns/xproc"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    xmlns:cx="http://xmlcalabash.com/ns/extensions"
    xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
    xmlns:math="http://www.w3.org/1998/Math/MathML">

  <p:declare-step type="px:dtbook-break-detect">

    <p:documentation>Break an input DTBook document into words and sentences by inserting word and sentence elements.</p:documentation>

    <p:input port="source" primary="true"/>
    <p:output port="result" primary="true"/>
    <p:output port="sentence-ids">
      <p:pipe port="sentence-ids" step="generic"/>
    </p:output>

    <p:import href="http://www.daisy.org/pipeline/modules/nlp-common/library.xpl">
      <p:documentation>
        px:break-and-reshape
      </p:documentation>
    </p:import>


    <!-- The 'can-contain-sentences' covers almost all possible
         cases. We don't usually need to make a special case for nodes
         that cannot be containED by sentences because those nodes are
         children of nodes that cannot contain sentences,
         e.g. headings are children of levels, which cannot contain
         sentences, thus there is no way to insert 'sent' between
         levels and headings. So most of the $cannot-be-sentence-child
         are redundant, but some are necessary nonetheless
         (e.g. linenum and epigraph). -->

    <px:break-and-reshape name="generic"
        inline-tags="dtb:acronym|dtb:em|dtb:strong|dtb:a|dtb:abbr|dtb:dfn|dtb:linenum|dtb:pagenum|
                     dtb:samp|dtb:span|dtb:sup|dtb:sub|dtb:w|dtb:noteref|dtb:br|dtb:math"
        ensure-word-before="dtb:acronym|dtb:span|dtb:linenum|dtb:pagenum|dtb:samp|dtb:noteref|dtb:abbr|
                            dtb:acronym|dtb:br|dtb:math"
        ensure-word-after="dtb:acronym|dtb:span|dtb:linenum|dtb:pagenum|dtb:samp|dtb:noteref|dtb:abbr|
                           dtb:acronym|dtb:br|dtb:math"
        can-contain-sentences="dtb:address|dtb:author|dtb:notice|dtb:prodnote|dtb:sidebar|dtb:line|
                               dtb:em|dtb:strong|dtb:dfn|dtb:kdb|dtb:code|dtb:samp|dtb:cite|dtb:abbr|
                               dtb:acronym|dtb:sub|dtb:sup|dtb:span|dtb:bdo|dtb:q|dtb:p|dtb:doctitle|
                               dtb:docauthor|dtb:levelhd|dtb:hd|dtb:h1|dtb:h2|dtb:h3|dtb:h4|dtb:h5|
                               dtb:h6|dtb:dt|dtb:dd|dtb:li|dtb:lic|dtb:caption|dtb:th|dtb:td|
                               dtb:bridgehead|dtb:byline|dtb:covertitle|dtb:epigraph|dtb:dateline|dtb:a"
        cannot-be-sentence-child="dtb:linenum|dtb:epigraph|dtb:td|dtb:th|dtb:tr|dtb:tfoot|dtb:thead|
                                  dtb:tbody|dtb:colgroup|dtb:col|dtb:list|dtb:li|dtb:lic|dtb:table|
                                  dtb:bridgehead|dtb:blockquote|dtb:dl|dtb:dd|dtb:div|dtb:title|
                                  dtb:author|dtb:sidebar|dtb:note|dtb:annotation|dtb:byline|dtb:dateline|
                                  dtb:linegroup|dtb:poem|dtb:p|dtb:doctitle|dtb:docauthor|dtb:covertitle|
                                  dtb:h1|dtb:h2|dtb:h3|dtb:h4|dtb:h5|dtb:h6|dtb:hd"
        special-sentences="dtb:pagenum|dtb:annoref|dtb:noteref|dtb:linenum|math:math"
        output-ns="http://www.daisy.org/z3986/2005/dtbook/"
        output-word-tag="w"
        output-sentence-tag="sent"
        output-subsentence-tag="span"/>

    <!-- Add IDs to nodes holding an attribute whose value should be
         synthesized (e.g. the @alt attribute of the images). Those
         nodes are not added to the context-free sentence-ids list
         because we would not be able to map IDs to content without
         knowing the context (types of node and types of
         document). -->
    <p:xslt>
      <p:input port="stylesheet">
    	<p:document href="add-ids.xsl"/>
      </p:input>
      <p:input port="parameters">
    	<p:empty/>
      </p:input>
    </p:xslt>

  </p:declare-step>

  <p:declare-step type="px:dtbook-unwrap-words">

    <p:documentation>Remove the word markups from the input document.</p:documentation>

    <p:input port="source" primary="true"/>
    <p:output port="result" primary="true"/>
    <p:unwrap match="dtb:w" />
  </p:declare-step>

</p:library>
