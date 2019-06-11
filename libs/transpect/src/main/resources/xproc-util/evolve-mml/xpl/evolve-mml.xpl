<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
  xmlns:c="http://www.w3.org/ns/xproc-step" version="1.0"
  xmlns:cx="http://xmlcalabash.com/ns/extensions" 
  xmlns:tr="http://transpect.io"
  xmlns:mml="http://www.w3.org/1998/Math/MathML"
  xmlns:hub="http://docbook.org/ns/docbook"
  xmlns:mml2tex="http://transpect.io/mml2tex"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  name="evolve-mml"
  type="tr:evolve-mml">
  
  <p:documentation>
    This step converts mml to tex. (via https://github.com/transpect/mml2tex)
    The mml2tex module must be available on URI http://transpect.io/mml2tex regardless of the value of the option 'type'.
  </p:documentation>
  
  <p:input port="source" primary="true">
    <p:documentation>
      Input hub containing equation/inlineequation elements.
    </p:documentation>
  </p:input>
  
  <p:input port="conf" primary="false">
    <p:documentation>
      Port for mml2tex conf.
    </p:documentation>
  </p:input>
  
  <p:option name="output-dir" required="true">
    <p:documentation>The folder where to save .tex and .mml files when option 'img' is used.</p:documentation>
  </p:option>
  <p:option name="type" required="false" select="'mml'">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>Content of the (inline)equation which contained MathML before.</p>
      <p>Multiple options can be joined with '+' (like 'mml+tex+img'):</p>
      <dl>
        <dd>mml</dd>
        <dt>Output the input MathML without modifications.</dt>
        <dd>img</dd>
        <dt>
          Output a (inline)mediaobject with imagedata/@fileref pointing to 'ltx-created-eq-1.gif' (or other declared file extension), where 1 is the position of the math-element.
          The .tex and .mml files will be written to the folder specified by 'output-dir' so the .gif can be created from them afterwards.
        </dt>
        <dd>tex</dd>
        <dt>Output a processing-instruction named mml2tex, containing the TeX-formula converted by mml2tex.</dt>
      </dl>
    </p:documentation>
  </p:option>
  <p:option name="outfile-prefix" required="false" select="'ltx-created-eq-'">
    <p:documentation>filename prefix for output equation files</p:documentation>
  </p:option>
  <p:option name="extension" required="false" select="'gif'">
    <p:documentation>Image extension for generated mediaobjects</p:documentation>
  </p:option>
  <p:option name="fail-on-error" required="false" select="'no'"/>
  <p:option name="preprocessing" required="false" select="'no'"/>
  <p:option name="debug" required="false" select="'no'"/>
  <p:option name="debug-dir-uri" select="'debug'"/>
  <p:option name="texmap" select="'http://transpect.io/mml2tex/texmap/texmap.xml'"/>
  <p:option name="texmap-upgreek" select="'http://transpect.io/mml2tex/texmap/texmap-upgreek.xml'"/>

  <p:output port="result" primary="true"/>
  
  <p:import href="http://xmlcalabash.com/extension/steps/library-1.0.xpl"/>
  <p:import href="http://transpect.io/xproc-util/store-debug/xpl/store-debug.xpl"/>
  <p:import href="http://transpect.io/mml2tex/xpl/mml2tex.xpl"/>
  
  <p:xslt name="cnt-math">
    <p:input port="stylesheet">
      <p:inline>
        <xsl:stylesheet version="2.0" xmlns="http://docbook.org/ns/docbook"
          xmlns:mml="http://www.w3.org/1998/Math/MathML">
          <xsl:template match="mml:math">
            <xsl:copy>
              <xsl:apply-templates mode="#current" select="@*"/>
              <xsl:attribute name="position" select="count(preceding::mml:math) + 1"/>
              <xsl:if test="parent::hub:inlineequation">
                <xsl:attribute name="inline" select="true()"/>
              </xsl:if>
              <xsl:apply-templates mode="#current"/>
            </xsl:copy>
          </xsl:template>
          <xsl:template match="@* | * | processing-instruction() | comment()" mode="#all" priority="-2">
            <xsl:copy>
              <xsl:apply-templates mode="#current" select="@*, node()"/>
            </xsl:copy>
          </xsl:template>
          <xsl:template match="text()" mode="#all" priority="-1">
            <xsl:value-of select="."/>
          </xsl:template>
        </xsl:stylesheet>
      </p:inline>
    </p:input>
    <p:input port="parameters">
      <p:empty/>
    </p:input>
  </p:xslt>
  
  <tr:store-debug pipeline-step="evolve-mml/mml-position"> 
    <p:input port="source">
      <p:pipe port="result" step="cnt-math"/>
    </p:input>
    <p:with-option name="active" select="$debug" />
    <p:with-option name="base-uri" select="$debug-dir-uri" />
  </tr:store-debug>
  
  <p:viewport match="mml:math" name="vp">
    <p:output port="result" primary="true"/>
    <p:variable name="outfile" select="concat($outfile-prefix, */@position)"/>
    <p:variable name="debug-uri" select="concat($debug-dir-uri, if (matches($debug-dir-uri, '/$')) then '' else '/', 'evolve-mml/formula', */@position)"></p:variable>
    
    <tr:store-debug name="mml" pipeline-step="math">
      <p:with-option name="active" select="$debug"/>
      <p:with-option name="base-uri" select="$debug-uri"/>
    </tr:store-debug>

    <p:wrap wrapper="tex" match="/"/>
    <mml2tex:convert>
      <p:input port="conf">
        <p:pipe port="conf" step="evolve-mml"/>
      </p:input>
      <p:with-option name="texmap-uri" select="$texmap"/>
      <p:with-option name="texmap-upgreek-uri" select="$texmap-upgreek"/>
      <p:with-option name="debug" select="$debug"/>
      <p:with-option name="debug-dir-uri" select="$debug-uri"/>
      <p:with-option name="preprocessing" select="$preprocessing"/>
      <p:with-option name="fail-on-error" select="$fail-on-error"/>
    </mml2tex:convert>
    <p:unwrap name="tex" match="tex"/>

    <p:sink/>

    <p:choose>
      <p:when test="matches($type, 'img')">
        <p:wrap match="processing-instruction()" name="tex-alt" wrapper="alt" wrapper-namespace="http://docbook.org/ns/docbook"
          wrapper-prefix="alt">
          <p:input port="source">
            <p:pipe port="result" step="tex"/>
          </p:input>
        </p:wrap>
        <p:store>
          <p:with-option name="href" select="concat($output-dir, '/', $outfile, '.tex')"/>
        </p:store>
        <p:store>
          <p:input port="source">
            <p:pipe port="result" step="mml"/>
          </p:input>
          <p:with-option name="href" select="concat($output-dir, '/', $outfile, '.mml')"/>
        </p:store>
      </p:when>
      <p:otherwise>
        <p:sink>
          <p:input port="source">
            <p:empty/>
          </p:input>
        </p:sink>
      </p:otherwise>
    </p:choose>
    
    <p:xslt template-name="init">
      <p:input port="source">
        <p:pipe port="result" step="mml"/>
        <p:pipe port="result" step="tex"/>
      </p:input>
      <p:input port="stylesheet">
        <p:inline>
          <xsl:stylesheet version="2.0" xmlns="http://docbook.org/ns/docbook">
            <xsl:param name="type"/>
            <xsl:param name="outfile"/>
            <xsl:template name="init">
              <math-wrapper>
                <xsl:if test="matches($type, 'mml')">
                  <alt>
                    <xsl:copy-of select="collection()[1]"/>
                  </alt>
                </xsl:if>
                <xsl:if test="matches($type, 'img')">
                  <xsl:choose>
                    <xsl:when test="//@inline">
                      <inlinemediaobject>
                        <imageobject>
                          <imagedata>
                            <xsl:attribute name="fileref" select="$outfile"/>
                          </imagedata>
                        </imageobject>
                      </inlinemediaobject>
                    </xsl:when>
                    <xsl:otherwise>
                      <mediaobject>
                        <imageobject>
                          <imagedata>
                            <xsl:attribute name="fileref" select="$outfile"/>
                          </imagedata>
                        </imageobject>
                      </mediaobject>
                    </xsl:otherwise>
                  </xsl:choose>
                </xsl:if>
                <xsl:if test="matches($type, 'tex')">
                  <xsl:copy-of select="collection()[2]"/>
                </xsl:if>
              </math-wrapper>
            </xsl:template>
          </xsl:stylesheet>
        </p:inline>
      </p:input>
      <p:input port="parameters">
        <p:empty/>
      </p:input>
      <p:with-param name="type" select="$type"/>
      <p:with-param name="outfile" select="$outfile, '.', $extension"/>
    </p:xslt>
    
    <tr:store-debug name="output" pipeline-step="converted">
      <p:with-option name="active" select="$debug"/>
      <p:with-option name="base-uri" select="$debug-uri"/>
    </tr:store-debug>
  </p:viewport>
  
  <p:delete match="mml:math/@position | mml:math/@inline"/>
  <p:unwrap match="hub:math-wrapper"/>
  
  <tr:store-debug pipeline-step="evolve-mml/converted">
    <p:with-option name="active" select="$debug"/>
    <p:with-option name="base-uri" select="$debug-dir-uri"/>
  </tr:store-debug>
</p:declare-step>
