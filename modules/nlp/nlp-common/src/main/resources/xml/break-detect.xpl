<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:break-detect"
                exclude-inline-prefixes="#all">

  <p:input port="source" primary="true" />
  <p:output port="result" primary="true" />

  <p:option name="inline-tags" required="true"/>
  <p:option name="output-word-tag" required="true"/>
  <p:option name="output-sentence-tag" required="true"/>
  <p:option name="ensure-word-before" required="false" select="''"/>
  <p:option name="ensure-word-after" required="false" select="''"/>
  <p:option name="ensure-sentence-before" required="false" select="''"/>
  <p:option name="ensure-sentence-after" required="false" select="''"/>

  <!--
      Implemented in ../../java/org/daisy/pipeline/nlp/breakdetect/calabash/impl/BreakDetectStep.java
  -->

</p:declare-step>
