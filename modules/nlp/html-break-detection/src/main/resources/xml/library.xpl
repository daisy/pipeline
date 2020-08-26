<?xml version="1.0" encoding="UTF-8"?>
<p:library version="1.0"
    xmlns:p="http://www.w3.org/ns/xproc"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    xmlns:cx="http://xmlcalabash.com/ns/extensions"
    xmlns:xhtml="http://www.w3.org/1999/xhtml">

  <p:declare-step type="px:html-break-detect">

    <p:documentation>Break an input XHTML document into words and sentences by inserting word and sentence elements.</p:documentation>

    <p:input port="source" primary="true"/>
    <p:output port="result" primary="true"/>
    <p:output port="sentence-ids">
      <p:pipe port="sentence-ids" step="generic"/>
    </p:output>

    <p:option name="id-prefix" required="false" select="''"/>

    <p:import href="http://www.daisy.org/pipeline/modules/nlp-common/library.xpl">
      <p:documentation>
        px:break-and-reshape
      </p:documentation>
    </p:import>

    <px:break-and-reshape name="generic">
      <p:with-option name="inline-tags" select="'span,i,b,a,br,del,font,ruby,s,small,strike,strong,sup,u,q,address,abbr,em,style'"/>
      <p:with-option name="ensure-word-before" select="'span,br,ruby,s,address,abbr,style'"/>
      <p:with-option name="ensure-word-after" select="'span,br,ruby,s,address,abbr,style'"/>
      <!-- Based on the containers of phrasing, flow and transparent content of HTML5 DTB: -->
      <p:with-option name="can-contain-sentences" select="'body,section,nav,article,aside,h1,h2,h3,h4,h5,h6,header,footer,address,p,pre,blockquote,li,dt,dd,a,q,cite,em,strong,small,mark,dfn,abbr,time,progress,meter,code,var,samp,kdb,sub,sup,span,i,b,bdo,rt,ins,del,caption,td,th,form,label,input,button,datalist,output,bb,menu,legend,div'"/>
      <p:with-option name="output-ns" select="'http://www.w3.org/1999/xhtml'"/>
      <p:with-option name="output-word-tag" select="'span'"/>
      <p:with-option name="word-attr" select="'role'"/>
      <p:with-option name="word-attr-val" select="'word'"/>
      <p:with-option name="output-sentence-tag" select="'span'"/>
      <p:with-option name="output-subsentence-tag" select="'span'"/>
      <p:with-option name="exclusive-sentence-tag" select="'false'"/>
      <p:with-option name="exclusive-word-tag" select="'false'"/>
      <p:with-option name="id-prefix" select="$id-prefix"/>
    </px:break-and-reshape>

  </p:declare-step>

  <p:declare-step type="px:html-unwrap-words">
    <p:documentation>Remove the word markups from the input document.</p:documentation>
    <p:input port="source" primary="true"/>
    <p:output port="result" primary="true"/>
    <p:unwrap match="xhtml:span[@role='word']" />
  </p:declare-step>

</p:library>
