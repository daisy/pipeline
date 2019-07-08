<?xml version="1.0"?>
<p:declare-step  
  xmlns:p="http://www.w3.org/ns/xproc" 
  xmlns:c="http://www.w3.org/ns/xproc-step"
  xmlns:tr="http://transpect.io" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:mml="http://www.w3.org/1998/Math/MathML"
  version="1.0"
  name="mathtype2mml-internal"
  type="tr:mathtype2mml-internal">

  <p:documentation>Convert an OLE-Object containing a Mathtype equation to MathML.
  Uses Jruby to create an XML-representation of the MTEF formula.</p:documentation>

  <p:input port="additional-font-maps" primary="false" sequence="true">
    <p:documentation>
      A sequence of &lt;symbols&gt;, containing mapped characters.
      Each &lt;symbols&gt; is required to contain the name of its @font-family as an attribute @name.
      If no @name is present, there will be an attempt to extract it from the base_uri (filename).
      Example, the value of @char is the unicode character that will be in the mml output:
      <symbols name="Times New Roman">
        <symbol number="002F" entity="&#x002f;" char="&#x002f;"/>
      </symbols>
    </p:documentation>
    <p:empty/>
  </p:input>

  <p:output port="result" primary="true" sequence="true">
    <p:documentation>The MathML equation from file @href.</p:documentation>
  </p:output>
  <p:output port="mtef-xml" primary="false" sequence="true">
    <p:documentation>The xml produced by mtef2xml step.</p:documentation>
    <p:pipe port="result" step="mtef2xml"/>
  </p:output>
  <p:output port="xml2mml" primary="false" sequence="true">
    <p:documentation>First mml produced, possibly invalid.</p:documentation>
    <p:pipe port="result" step="xml2mml"/>
  </p:output>
  <p:output port="map-fonts" primary="false" sequence="true">
    <p:documentation>Replaced characters for which a font-map was available.</p:documentation>
    <p:pipe port="result" step="map-fonts"/>
  </p:output>
  <p:output port="handle-whitespace" primary="false" sequence="true">
    <p:documentation>Whitespace translated according to option mml-space-handling (default: mspace).</p:documentation>
    <p:pipe port="result" step="handle-whitespace"/>
  </p:output>
  <p:output port="operator-elements" primary="false" sequence="true">
    <p:documentation>Put Operator-like elements like '(' in mo.</p:documentation>
    <p:pipe port="result" step="operator-elements"/>
  </p:output>
  <p:output port="repair-subsup" primary="false" sequence="true">
    <p:documentation>The mml with a (possibly empty) base element for each exponent (msub|msup|msubsup|mmultiscripts).</p:documentation>
    <p:pipe port="result" step="repair-subsup"/>
  </p:output>
  <p:output port="combine-elements" primary="false" sequence="true">
    <p:documentation>The mml with combined mtext|mn elements where applicable.</p:documentation>
    <p:pipe port="result" step="combine-elements"/>
  </p:output>
  <p:output port="split-elements" primary="false" sequence="true">
    <p:documentation>The mml where characters in mn are extracted to mi.</p:documentation>
    <p:pipe port="result" step="split-elements"/>
  </p:output>
  <p:output port="clean-up" primary="false" sequence="true">
    <p:documentation>Dissolved mrows with exactly one child element.</p:documentation>
    <p:pipe port="result" step="clean-up"/>
  </p:output>
  <p:option name="href">
    <p:documentation>The equation file URI. (OLE-Object)</p:documentation>
  </p:option>
  <p:option name="mml-space-handling" select="'mspace'">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>Whitespace conversion from MTEF to MathML<br/>default is 'mspace'</p>
      <dl>
        <dt>char</dt>
        <dd>All whitespace will be in mtext, without xml:space attributes</dd>
        <dt>mspace</dt>
        <dd>All Mathtype whitespace will be converted to mspace, with @width set by options:
          <ul>
            <li>em-width</li>
            <li>en-width</li>
            <li>standard-width</li>
            <li>thin-width</li>
            <li>hair-width</li>
            <li>zero-width</li>
          </ul>
        </dd>
      </dl>
    </p:documentation>
  </p:option>
  <p:option name="debug" select="'no'">
    <p:documentation>
      <p>Output xsl:message for debugging (like 'unmatched element' for unsupported features).<br/>Default: 'no'.</p>
    </p:documentation>
  </p:option>
  <p:option name="em-width" select="'1em'">
    <p:documentation>Only active with option mml-space-handling set to 'mspace'. Value for mspace/width with Mathtype em-width. Default is '1em'.</p:documentation>
  </p:option>
  <p:option name="en-width" select="'0.33em'">
    <p:documentation>Only active with option mml-space-handling set to 'mspace'. Value for mspace/width with Mathtype en-width. Default is '0.33em'.</p:documentation>
  </p:option>
  <p:option name="standard-width" select="'0.16em'">
    <p:documentation>Only active with option mml-space-handling set to 'mspace'. Value for mspace/width with Mathtype standard-width. Default is '0.16em'.</p:documentation>
  </p:option>
  <p:option name="thin-width" select="'0.08em'">
    <p:documentation>Only active with option mml-space-handling set to 'mspace'. Value for mspace/width with Mathtype thin-width. Default is '0.08em'.</p:documentation>
  </p:option>
  <p:option name="hair-width" select="'0.08em'">
    <p:documentation>Only active with option mml-space-handling set to 'mspace'. Value for mspace/width with Mathtype hair-width. Default is '0.08em'.</p:documentation>
  </p:option>
  <p:option name="zero-width" select="'0em'">
    <p:documentation>Only active with option mml-space-handling set to 'mspace'. Value for mspace/width with Mathtype zero-width. Default is '0em'.</p:documentation>
  </p:option>
  
  <p:import href="mtef2xml-declaration.xpl"/>
  
  <tr:mtef2xml name="mtef2xml">
	 <p:with-option name="href" select="$href"/>
  </tr:mtef2xml>

  <p:xslt name="xml2mml">
    <p:input port="source">
      <p:pipe port="result" step="mtef2xml"/>
      <p:document href="../fontmaps/MathType_MTCode.xml"/>
    </p:input>
    <p:input port="parameters">
      <p:empty/>
    </p:input>
    <p:input port="stylesheet">
      <p:document href="../xsl/transform.xsl"/>
    </p:input>
    <p:with-param name="debug" select="$debug"><p:empty/></p:with-param>
  </p:xslt>

  <p:xslt initial-mode="map-fonts" name="map-fonts">
    <p:input port="source">
      <p:pipe port="result" step="xml2mml"/>
      <p:document href="../fontmaps/MathType_MTCode.xml"/>
      <p:pipe port="additional-font-maps" step="mathtype2mml-internal"/>
    </p:input>
    <p:input port="parameters">
      <p:empty/>
    </p:input>
    <p:input port="stylesheet">
      <p:document href="../xsl/map-fonts.xsl"/>
    </p:input>
  </p:xslt>

  <p:xslt initial-mode="handle-whitespace" name="handle-whitespace">
    <p:input port="parameters">
      <p:empty/>
    </p:input>
    <p:input port="stylesheet">
      <p:document href="../xsl/whitespace-handle.xsl"/>
    </p:input>
    <p:with-param name="mml-space-handling" select="$mml-space-handling"/>
    <p:with-param name="em-width" select="$em-width"/>
    <p:with-param name="en-width" select="$en-width"/>
    <p:with-param name="standard-width" select="$standard-width"/>
    <p:with-param name="thin-width" select="$thin-width"/>
    <p:with-param name="hair-width" select="$hair-width"/>
    <p:with-param name="zero-width" select="$zero-width"/>
  </p:xslt>

  <p:xslt initial-mode="operator-elements" name="operator-elements">
    <p:input port="parameters">
      <p:empty/>
    </p:input>
    <p:input port="stylesheet">
      <p:document href="../xsl/operator-elements.xsl"/>
    </p:input>
  </p:xslt>

  <p:xslt initial-mode="combine-elements" name="combine-elements">
    <p:input port="parameters">
      <p:empty/>
    </p:input>
    <p:input port="stylesheet">
      <p:document href="../xsl/combine-elements.xsl"/>
    </p:input>
  </p:xslt>

  <p:xslt initial-mode="split-elements" name="split-elements">
    <p:input port="parameters">
      <p:empty/>
    </p:input>
    <p:input port="stylesheet">
      <p:document href="../xsl/split-elements.xsl"/>
    </p:input>
  </p:xslt>

  <p:xslt initial-mode="repair-subsup" name="repair-subsup">
    <p:input port="parameters">
      <p:empty/>
    </p:input>
    <p:input port="stylesheet">
      <p:document href="../xsl/repair-subsup.xsl"/>
    </p:input>
  </p:xslt>
  
  <p:xslt initial-mode="clean-up" name="clean-up">
    <p:input port="parameters">
      <p:empty/>
    </p:input>
    <p:input port="stylesheet">
      <p:document href="../xsl/clean-up.xsl"/>
    </p:input>
  </p:xslt>
  
</p:declare-step>
