<?xml version="1.0" encoding="UTF-8"?>
<p:library version="1.0"
    xmlns:p="http://www.w3.org/ns/xproc"
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
    <px:break-and-reshape name="generic">
      <p:with-option name="inline-tags" select="'emph,span,ref,char,term,sub,sup,pagebreak,name,time,noteref,annoref,lnum,num,w,wpart,abbr'"/>
      <p:with-option name="ensure-word-before" select="'span'"/>
      <p:with-option name="ensure-word-after" select="'span'"/>
      <!-- note1: For now, if an element can-contain-sentences only in
           certain contexts, i.e. it depends on its ancestors, then
           the best we can do is ignore them no matter which context
           they appear in. -->
      <!-- note2: some of the following elements are not in the ZedAI
           namespace. -->
      <p:with-option name="can-contain-sentences" select="'address,annoref,annotation,aside,block,caption,citation,d,definition,description,emph,entry,expansion,h,hd,hpart,item,ln,longdesc,group,label,note,noteref,object,otherwise,p,phoneme,prosody,quote,rb,ref,repeat,rt,say-as,span,spine,sub,summary,td,term,th,tour,when'"/>
      <p:with-option name="output-ns" select="'http://www.daisy.org/ns/z3998/authoring/'"/>
      <p:with-option name="output-word-tag" select="'w'"/>
      <p:with-option name="output-sentence-tag" select="'s'"/>
      <p:with-option name="output-subsentence-tag" select="'span'"/>
      <p:with-option name="special-sentences" select="'name,time'"/>
    </px:break-and-reshape>

  </p:declare-step>

  <p:declare-step type="px:zedai-unwrap-words">
    <p:documentation>Remove the word markups from the input document.</p:documentation>
    <p:input port="source" primary="true"/>
    <p:output port="result" primary="true"/>
    <p:unwrap match="z:w" />
  </p:declare-step>

</p:library>
