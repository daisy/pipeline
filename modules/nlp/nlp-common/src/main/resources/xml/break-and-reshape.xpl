<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
		xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
		xmlns:cx="http://xmlcalabash.com/ns/extensions"
		xmlns:tmp="http://www.daisy.org/ns/pipeline/tmp"
		type="px:break-and-reshape"
		exclude-inline-prefixes="#all">

  <p:option name="can-contain-sentences" required="true" cx:type="XSLTMatchPattern">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      XSLT match pattern to select the elements that can be direct
      parent of sentences, words and sub-sentences.
    </p:documentation>
  </p:option>

  <p:option name="cannot-be-sentence-child" required="false" select="''" cx:type="XSLTMatchPattern">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      XSLT match pattern to select the elements that cannot be
      contained by sentence elements.
    </p:documentation>
  </p:option>

  <p:option name="special-sentences" required="false" select="''" cx:type="XSLTMatchPattern">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      XSLT match pattern to select the elements that cannot contain
      sentence elements but must be considered as sentences (such as
      DTBook pagenums). They must be able to hold an ID attribute.
    </p:documentation>
  </p:option>

  <p:option name="inline-tags" required="true" cx:type="XSLTMatchPattern">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      XSLT match pattern to select the elements that do not necessary
      separate sentences.
    </p:documentation>
  </p:option>

  <p:option name="ensure-word-before" required="false" select="''" cx:type="XSLTMatchPattern">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      XSLT match pattern to select elements that end the current word.
    </p:documentation>
  </p:option>
  <p:option name="ensure-word-after" required="false" select="''" cx:type="XSLTMatchPattern"/>

  <p:option name="ensure-sentence-before" required="false" select="''" cx:type="XSLTMatchPattern">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      XSLT match pattern to select elements that end the current
      sentence.
    </p:documentation>
  </p:option>
  <p:option name="ensure-sentence-after" required="false" select="''" cx:type="XSLTMatchPattern"/>

  <p:option name="output-word-tag" required="true">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      Name of the element used for representing a word.
    </p:documentation>
  </p:option>

  <p:option name="output-sentence-tag" required="true">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      Name of the element used for representing a sentence.
    </p:documentation>
  </p:option>

  <p:option name="word-attr" required="false" select="''">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      Attribute name of the element used for representing a word.
    </p:documentation>
  </p:option>

  <p:option name="word-attr-val" required="false" select="''">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      Corresponding attribute value of the option 'word-attr'.
    </p:documentation>
  </p:option>

  <p:option name="sentence-attr" required="false" select="''">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      Attribute name of the element used for representing a sentence.
    </p:documentation>
  </p:option>

  <p:option name="sentence-attr-val" required="false" select="''">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      Corresponding attribute value of the option 'sentence-attr'.
    </p:documentation>
  </p:option>

  <p:option name="output-ns" required="true">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      Output namespace in which the words and the sentences will be
      created.
    </p:documentation>
  </p:option>

  <p:option name="id-prefix" required="false" select="''">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      Generated IDs will be prefixed with this option so as to prevent
      conflicts when the lexing is run multiple times whereas the
      produced IDs are intented to be stored in the same place, such
      as a central list of audio clips.
    </p:documentation>
  </p:option>

  <p:option name="output-subsentence-tag" required="true"/>
  <p:option name="exclusive-word-tag" select="'true'"/>
  <p:option name="exclusive-sentence-tag" select="'true'"/>

  <p:input port="source" primary="true">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      Input document (Zedai, DTBook etc.).
    </p:documentation>
  </p:input>

  <p:output port="result" primary="true">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      Input document with the words and the sentences.
    </p:documentation>
  </p:output>

  <p:output port="sentence-ids">
    <p:pipe port="sentence-ids" step="reshape"/>
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      List of the sentences' id.
    </p:documentation>
  </p:output>

  <p:import href="break-detect.xpl">
    <p:documentation>
      px:break-detect
    </p:documentation>
  </p:import>
  <p:import href="reshape.xpl">
    <p:documentation>
      px:reshape
    </p:documentation>
  </p:import>

  <!-- The tags are chosen to not conflict with other elements since
       the namespace is not always used in the XSLT scripts, which
       otherwise helps distinguish temporary tags from the others. -->
  <p:variable name="tmp-word-tag" select="'tmp:ww'"/>
  <p:variable name="tmp-sentence-tag" select="'tmp:ss'"/>

  <!-- run the java-based lexing step -->
  <px:break-detect>
    <p:with-option name="inline-tags" select="$inline-tags"/>
    <p:with-option name="ensure-word-before" select="$ensure-word-before"/>
    <p:with-option name="ensure-word-after" select="$ensure-word-after"/>
    <p:with-option name="ensure-sentence-before" select="$ensure-sentence-before"/>
    <p:with-option name="ensure-sentence-after" select="$ensure-sentence-after"/>
    <p:with-option name="output-word-tag" select="$tmp-word-tag"/>
    <p:with-option name="output-sentence-tag" select="$tmp-sentence-tag"/>
  </px:break-detect>

  <px:reshape name="reshape">
    <p:with-option name="can-contain-sentences" select="$can-contain-sentences"/>
    <p:with-option name="cannot-be-sentence-child" select="$cannot-be-sentence-child"/>
    <p:with-option name="special-sentences" select="$special-sentences"/>
    <p:with-option name="output-word-tag" select="$output-word-tag"/>
    <p:with-option name="output-sentence-tag" select="$output-sentence-tag"/>
    <p:with-option name="word-attr" select="$word-attr"/>
    <p:with-option name="word-attr-val" select="$word-attr-val"/>
    <p:with-option name="sentence-attr" select="$sentence-attr"/>
    <p:with-option name="sentence-attr-val" select="$sentence-attr-val"/>
    <p:with-option name="output-ns" select="$output-ns"/>
    <p:with-option name="output-subsentence-tag" select="$output-subsentence-tag"/>
    <p:with-option name="tmp-word-tag" select="$tmp-word-tag"/>
    <p:with-option name="tmp-sentence-tag" select="$tmp-sentence-tag"/>
    <p:with-option name="exclusive-word-tag" select="$exclusive-word-tag"/>
    <p:with-option name="exclusive-sentence-tag" select="$exclusive-sentence-tag"/>
    <p:with-option name="id-prefix" select="$id-prefix"/>
  </px:reshape>

</p:declare-step>
