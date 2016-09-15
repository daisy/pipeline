<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step name="main" type="px:create-res-file" version="1.0"
		xmlns:p="http://www.w3.org/ns/xproc"
		xmlns:px="http://www.daisy.org/ns/pipeline/xproc">

  <p:output port="result" primary="true">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>DAISY 3 resource file.</p>
    </p:documentation>
  </p:output>

  <p:option name="output-dir">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>Output directory URI if the resource file were to be stored or refered by a fileset.</p>
    </p:documentation>
  </p:option>

  <p:option name="lang">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>Main language of the DTBook document which the resource file refers to.</p>
    </p:documentation>
  </p:option>

  <p:add-attribute match="/*" attribute-name="xml:base">
    <p:input port="source">
      <!-- TODO: use different words depending on the document
	   language to localize the file, and generate the
	   corresponding audio with the available TTS. To do so, it
	   could return a list of ssml:s with custom ids and call
	   ssml-to-audio. -->
      <p:inline xmlns="http://www.daisy.org/z3986/2005/resource/">
	<resources version="2005-1">
	  <scope nsuri="http://www.daisy.org/z3986/2005/ncx/" >
	    <nodeSet id="page-set"
		     select="//smilCustomTest[@bookStruct='PAGE_NUMBER']">
	      <resource xml:lang="en">
		<text>page</text>
	      </resource>
	    </nodeSet>
	    <nodeSet id="note-set"
		     select="//smilCustomTest[@bookStruct='NOTE']">
	      <resource xml:lang="en">
		<text>note</text>
	      </resource>
	    </nodeSet>
	    <nodeSet id="notref-set"
		     select="//smilCustomTest[@bookStruct='NOTE_REFERENCE']">
	      <resource xml:lang="en">
		<text>note</text>
	      </resource>
	    </nodeSet>
	    <nodeSet id="annot-set"
		     select="//smilCustomTest[@bookStruct='ANNOTATION']">
	      <resource xml:lang="en">
		<text>annotation</text>
	      </resource>
	    </nodeSet>
	    <nodeSet id="line-set"
		     select="//smilCustomTest[@bookStruct='LINE_NUMBER']">
	      <resource xml:lang="en">
		<text>line</text>
	      </resource>
	    </nodeSet>
	    <nodeSet id="sidebar-set"
		     select="//smilCustomTest[@bookStruct='OPTIONAL_SIDEBAR']">
	      <resource xml:lang="en">
		<text>sidebar</text>
	      </resource>
	    </nodeSet>
	    <nodeSet id="prodnote-set"
		     select="//smilCustomTest[@bookStruct='OPTIONAL_PRODUCER_NOTE']">
	      <resource xml:lang="en">
		<text>note</text>
	      </resource>
	    </nodeSet>
	  </scope>
	  <scope nsuri="http://www.w3.org/2001/SMIL20/">
	    <nodeSet id="math-seq-set" select="//seq[@class='math']">
	      <resource xml:lang="en">
		<text>mathematical formula</text>
	      </resource>
	    </nodeSet>
	    <nodeSet id="math-par-set" select="//par[@class='math']">
	      <resource xml:lang="en">
		<text>mathematical formula</text>
	      </resource>
	    </nodeSet>
	  </scope>
	</resources>
      </p:inline>
    </p:input>
    <p:with-option name="attribute-value" select="concat($output-dir, 'resources.res')"/>
  </p:add-attribute>

</p:declare-step>
