<p:declare-step type="px:break-and-reshape"
		version="1.0" xmlns:p="http://www.w3.org/ns/xproc"
		xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
		xmlns:tmp="http://www.daisy.org/ns/pipeline/tmp"
		exclude-inline-prefixes="#all">

  <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
  <p:import href="break-detect.xpl"/>
  <p:import href="reshape.xpl"/>

  <p:option name="can-contain-sentences" required="true">
    <p:documentation>
      Comma-separated list of elements that can be direct parent of
      sentences, words and sub-sentences.
    </p:documentation>
  </p:option>

  <p:option name="cannot-be-sentence-child" required="false" select="''">
    <p:documentation>
      Comma-separated list of elements that cannot be contained by
      sentence elements.
    </p:documentation>
  </p:option>

  <p:option name="special-sentences" required="false" select="''">
    <p:documentation>
      Comma-separated list of elements that cannot contain sentence
      elements but must be considered as sentences (such as DTBook
      pagenums). They must be able to hold an ID attribute.
    </p:documentation>
  </p:option>

  <p:option name="inline-tags" required="true">
    <p:documentation>
      Comma-separated list of elements that do not necessary separate
      sentences.
    </p:documentation>
  </p:option>

  <p:option name="output-word-tag" required="true">
    <p:documentation>
      Name of the element used for representing a word.
    </p:documentation>
  </p:option>

  <p:option name="output-sentence-tag" required="true">
    <p:documentation>
      Name of the element used for representing a sentence.
    </p:documentation>
  </p:option>

  <p:option name="word-attr" required="false" select="''">
    <p:documentation>
      Attribute name of the element used for representing a word.
    </p:documentation>
  </p:option>

  <p:option name="word-attr-val" required="false" select="''">
    <p:documentation>
      Corresponding attribute value of the option 'word-attr'.
    </p:documentation>
  </p:option>

  <p:option name="output-ns" required="true">
    <p:documentation>
      Output namespace in which the words and the sentences will be
      created.
    </p:documentation>
  </p:option>

  <p:option name="ensure-word-before" required="false" select="''">
    <p:documentation>
      Comma-separated list of elements. When such elements are
      detected, the Lexer is forced to end the current word.
    </p:documentation>
  </p:option>
  <p:option name="ensure-word-after" required="false" select="''"/>

  <p:option name="ensure-sentence-before" required="false" select="''">
    <p:documentation>
      Comma-separated list of elements. When such elements are
      detected, the Lexer is forced to end the current sentence.
    </p:documentation>
  </p:option>
  <p:option name="ensure-sentence-after" required="false" select="''"/>

  <p:option name="id-prefix" required="false" select="''">
    <p:documentation>
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
    <p:documentation>
      Input document (Zedai, DTBook etc.).
    </p:documentation>
  </p:input>

  <p:output port="result" primary="true">
    <p:documentation>
      Input document with the words and the sentences.
    </p:documentation>
  </p:output>

  <p:output port="sentence-ids">
    <p:pipe port="sentence-ids" step="reshape"/>
    <p:documentation>
      List of the sentences' id.
    </p:documentation>
  </p:output>

  <!-- The tags are chosen to not conflict with other elements since
       the namespace is not always used in the XSLT scripts, which
       otherwise helps distinguish temporary tags from the others. -->
  <p:variable name="tmp-ns" select="'http://www.daisy.org/ns/pipeline/tmp'"/>
  <p:variable name="tmp-word-tag" select="'ww'"/>
  <p:variable name="tmp-sentence-tag" select="'ss'"/>

  <!-- run the java-based lexing step -->
  <px:break-detect name="break">
    <p:with-option name="inline-tags" select="$inline-tags"/>
    <p:with-option name="output-word-tag" select="$tmp-word-tag"/>
    <p:with-option name="output-sentence-tag" select="$tmp-sentence-tag"/>
    <p:with-option name="tmp-ns" select="$tmp-ns"/>
    <p:with-option name="ensure-word-before" select="$ensure-word-before"/>
    <p:with-option name="ensure-word-after" select="$ensure-word-after"/>
    <p:with-option name="ensure-sentence-before" select="$ensure-sentence-before"/>
    <p:with-option name="ensure-sentence-after" select="$ensure-sentence-after"/>
  </px:break-detect>
  <px:message message="Java-based break detection done." severity="DEBUG"/>

  <px:reshape name="reshape">
    <p:with-option name="can-contain-sentences" select="$can-contain-sentences"/>
    <p:with-option name="cannot-be-sentence-child" select="$cannot-be-sentence-child"/>
    <p:with-option name="special-sentences" select="$special-sentences"/>
    <p:with-option name="output-word-tag" select="$output-word-tag"/>
    <p:with-option name="output-sentence-tag" select="$output-sentence-tag"/>
    <p:with-option name="word-attr" select="$word-attr"/>
    <p:with-option name="word-attr-val" select="$word-attr-val"/>
    <p:with-option name="output-ns" select="$output-ns"/>
    <p:with-option name="output-subsentence-tag" select="$output-subsentence-tag"/>
    <p:with-option name="tmp-ns" select="$tmp-ns"/>
    <p:with-option name="tmp-word-tag" select="$tmp-word-tag"/>
    <p:with-option name="tmp-sentence-tag" select="$tmp-sentence-tag"/>
    <p:with-option name="exclusive-word-tag" select="$exclusive-word-tag"/>
    <p:with-option name="exclusive-sentence-tag" select="$exclusive-sentence-tag"/>
    <p:with-option name="id-prefix" select="$id-prefix"/>
  </px:reshape>
  <px:message message="Full break detection done" severity="DEBUG"/>

</p:declare-step>
