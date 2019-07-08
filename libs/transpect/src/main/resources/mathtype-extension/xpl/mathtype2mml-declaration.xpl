<?xml version="1.0"?>
<p:declare-step  
  xmlns:p="http://www.w3.org/ns/xproc" 
  xmlns:c="http://www.w3.org/ns/xproc-step"
  xmlns:tr="http://transpect.io" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:mml="http://www.w3.org/1998/Math/MathML"
  version="1.0"
  name="mathtype2mml"
  type="tr:mathtype2mml">

  <p:documentation>Convert an OLE-Object containing a Mathtype equation to MathML.
  Uses Jruby to create an XML-representation of the MTEF formula.
  This step requires xproc-util.
  Conversion without xproc-util is provided by tr:mathtype2mml-internal.</p:documentation>

  <p:input port="additional-font-maps" primary="false" sequence="true">
    <p:documentation>
      A sequence of &lt;symbols&gt;, containing mapped characters.
      Each &lt;symbols&gt; is required to contain the name of its font-family as an attribute @name.
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
  <p:serialization port="result" indent="true" omit-xml-declaration="false"/>
  <p:option name="href">
    <p:documentation>The equation file URI. (OLE-Object)</p:documentation>
  </p:option>
  <p:option name="debug" select="'no'"/>
  <p:option name="debug-dir-uri" select="'debug'"/>
  <p:option name="mml-space-handling" select="'mspace'">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>Whitespace conversion from MTEF to MathML<br/>default is 'mspace'</p>
      <dl>
        <dt>char</dt>
        <dd>All whitespace will be in mtext, without xml:space attributes, as characters</dd>
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
  <p:option name="debug-xsl-message" select="'no'">
    <p:documentation>
      <p>Output internal xsl:message for debugging (like 'unmatched element' for unsupported features).<br/>Default: 'no'.</p>
    </p:documentation>
  </p:option>

  <p:import href="mathtype2mml-declaration-internal.xpl"/>
  <p:import href="http://transpect.io/xproc-util/store-debug/xpl/store-debug.xpl"/>

  <p:variable name="basename" select="replace($href,  '^.+/(.+)\.[a-z]+$', '$1')"/>

  <tr:mathtype2mml-internal name="mathtype2mml-internal">
    <p:input port="additional-font-maps">
      <p:pipe port="additional-font-maps" step="mathtype2mml"/>
    </p:input>
    <p:with-option name="href" select="$href"/>
    <p:with-option name="mml-space-handling" select="$mml-space-handling"/>
    <p:with-option name="debug" select="$debug"/>
    <p:with-option name="em-width" select="$em-width"/>
    <p:with-option name="en-width" select="$en-width"/>
    <p:with-option name="standard-width" select="$standard-width"/>
    <p:with-option name="thin-width" select="$thin-width"/>
    <p:with-option name="hair-width" select="$hair-width"/>
    <p:with-option name="zero-width" select="$zero-width"/>
  </tr:mathtype2mml-internal>

  <p:choose>
    <p:when test="//c:errors">
      <p:identity/>
    </p:when>
    <p:otherwise>
      <tr:store-debug>
        <p:input port="source">
          <p:pipe port="mtef-xml" step="mathtype2mml-internal"/>
        </p:input>
        <p:with-option name="pipeline-step" select="concat('mathtype2mml/', $basename, '/02-mtef2xml')"/>
        <p:with-option name="active" select="$debug"/>
        <p:with-option name="base-uri" select="$debug-dir-uri"/>
      </tr:store-debug>

      <tr:store-debug>
        <p:input port="source">
          <p:pipe port="xml2mml" step="mathtype2mml-internal"/>
        </p:input>
        <p:with-option name="pipeline-step" select="concat('mathtype2mml/', $basename, '/04-xml2mml')"/>
        <p:with-option name="active" select="$debug"/>
        <p:with-option name="base-uri" select="$debug-dir-uri"/>
      </tr:store-debug>

      <tr:store-debug>
        <p:input port="source">
          <p:pipe port="map-fonts" step="mathtype2mml-internal"/>
        </p:input>
        <p:with-option name="pipeline-step" select="concat('mathtype2mml/', $basename, '/06-map-fonts')"/>
        <p:with-option name="active" select="$debug"/>
        <p:with-option name="base-uri" select="$debug-dir-uri"/>
      </tr:store-debug>

      <tr:store-debug>
        <p:input port="source">
          <p:pipe port="handle-whitespace" step="mathtype2mml-internal"/>
        </p:input>
        <p:with-option name="pipeline-step" select="concat('mathtype2mml/', $basename, '/08-handle-whitespace')"/>
        <p:with-option name="active" select="$debug"/>
        <p:with-option name="base-uri" select="$debug-dir-uri"/>
      </tr:store-debug>

      <tr:store-debug>
        <p:input port="source">
          <p:pipe port="operator-elements" step="mathtype2mml-internal"/>
        </p:input>
        <p:with-option name="pipeline-step" select="concat('mathtype2mml/', $basename, '/10-operator-elements')"/>
        <p:with-option name="active" select="$debug"/>
        <p:with-option name="base-uri" select="$debug-dir-uri"/>
      </tr:store-debug>

      <tr:store-debug>
        <p:input port="source">
          <p:pipe port="combine-elements" step="mathtype2mml-internal"/>
        </p:input>
        <p:with-option name="pipeline-step" select="concat('mathtype2mml/', $basename, '/12-combine-elements')"/>
        <p:with-option name="active" select="$debug"/>
        <p:with-option name="base-uri" select="$debug-dir-uri"/>
      </tr:store-debug>
      
      <tr:store-debug>
        <p:input port="source">
          <p:pipe port="split-elements" step="mathtype2mml-internal"/>
        </p:input>
        <p:with-option name="pipeline-step" select="concat('mathtype2mml/', $basename, '/13-split-elements')"/>
        <p:with-option name="active" select="$debug"/>
        <p:with-option name="base-uri" select="$debug-dir-uri"/>
      </tr:store-debug>

      <tr:store-debug>
        <p:input port="source">
          <p:pipe port="repair-subsup" step="mathtype2mml-internal"/>
        </p:input>
        <p:with-option name="pipeline-step" select="concat('mathtype2mml/', $basename, '/14-repair-subsup')"/>
        <p:with-option name="active" select="$debug"/>
        <p:with-option name="base-uri" select="$debug-dir-uri"/>
      </tr:store-debug>

      <tr:store-debug>
        <p:input port="source">
          <p:pipe port="clean-up" step="mathtype2mml-internal"/>
        </p:input>
        <p:with-option name="pipeline-step" select="concat('mathtype2mml/', $basename, '/20-clean-up')"/>
        <p:with-option name="active" select="$debug"/>
        <p:with-option name="base-uri" select="$debug-dir-uri"/>
      </tr:store-debug>

      <p:xslt>
        <p:input port="stylesheet">
          <p:inline>
            <xsl:stylesheet version="2.0" xpath-default-namespace="http://www.w3.org/1998/Math/MathML">
              <xsl:template match="@*">
                <xsl:copy/>
              </xsl:template>

              <xsl:template match="*">
                <xsl:element name="mml:{local-name()}" namespace="http://www.w3.org/1998/Math/MathML">
                  <xsl:apply-templates select="@*, node()"/>
                </xsl:element>
              </xsl:template>
            </xsl:stylesheet>
          </p:inline>
        </p:input>
        <p:input port="parameters">
          <p:empty/>
        </p:input>
      </p:xslt>
    </p:otherwise>
  </p:choose>

</p:declare-step>
