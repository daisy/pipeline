<?xml version="1.0" encoding="UTF-8"?>
<p:library xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
           xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
           xmlns:cx="http://xmlcalabash.com/ns/extensions"
           xmlns:z="http://www.daisy.org/ns/z3998/authoring">

  <p:declare-step type="px:zedai-break-detect">

    <p:documentation>Break an input Zedai document into words and sentences by inserting word and sentence elements.</p:documentation>

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

    <!-- Note 1: For now, if an element "can-contain-sentences" only in
         certain contexts, i.e. it depends on its ancestors, then
         the best we can do is ignore them no matter which context
         they appear in. -->
    <!-- Note 2: some of the "can-contain-sentences" elements are not in
         the ZedAI namespace. -->
    <px:break-and-reshape name="generic"
        inline-tags="z:emph|z:span|z:ref|z:char|z:term|z:sub|z:sup|z:pagebreak|z:name|z:time|z:noteref|
                     z:annoref|z:lnum|z:num|z:w|z:wpart|z:abbr"
        ensure-word-before="span"
        ensure-word-after="span"
        can-contain-sentences="z:address|z:annoref|z:annotation|z:aside|z:block|z:caption|z:citation|z:d|
                               z:definition|z:description|z:emph|z:entry|z:expansion|z:h|z:hd|z:hpart|
                               z:item|z:ln|z:longdesc|z:group|z:label|z:note|z:noteref|z:object|
                               z:otherwise|z:p|z:phoneme|z:prosody|z:quote|z:rb|z:ref|z:repeat|z:rt|
                               z:say-as|z:span|z:spine|z:sub|z:summary|z:td|z:term|z:th|z:tour|z:when"
        output-ns="http://www.daisy.org/ns/z3998/authoring/"
        output-word-tag="w"
        output-sentence-tag="s"
        output-subsentence-tag="span"
        special-sentences="z:name|z:time"/>

  </p:declare-step>

  <p:declare-step type="px:zedai-unwrap-words">
    <p:documentation>Remove the word markups from the input document.</p:documentation>
    <p:input port="source" primary="true"/>
    <p:output port="result" primary="true"/>
    <p:unwrap match="z:w" />
  </p:declare-step>

</p:library>
