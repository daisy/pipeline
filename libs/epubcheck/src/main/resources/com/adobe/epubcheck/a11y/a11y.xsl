<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:epub="http://www.idpf.org/2007/ops" xmlns:opf="http://www.idpf.org/2007/opf"
  xmlns="http://www.w3.org/1999/xhtml" xmlns:saxon="http://saxon.sf.net/"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" xpath-default-namespace="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="#all" version="2.0">

  <xsl:output indent="yes" method="html" doctype-system="about:legacy-compat" encoding="UTF-8"/>

  <xsl:template match="text() | comment()" mode="#default filtering tocnav"/>

  <xsl:param name="resources-dir" select="''"/>

  <xsl:variable name="css" xml:space="preserve">h1,th{text-align:center}*{box-sizing:border-box}html{font-family:'helvetica neue',helvetica,arial,sans-serif;font-size:16px;line-height:1.2em;color:#333;margin-left:2em;margin-right:2em}.outlines{display:-webkit-flex;display:flex;-webkit-flex-flow:row wrap;flex-flow:row wrap}.outlines>*{flex-grow:1;margin-right:1em}h2{margin-top:2em;padding-bottom:.4em;border-bottom:5px solid #ccc}li{line-height:1.4em}.h1:before,.h2:before,.h3:before,.h4:before,.h5:before,.h6:before,li .missing{display:inline-block;height:1.5em;padding:0 .4em;background-color:#449d44;border-radius:4px;color:#fff;font-size:.8em;font-family:monospace;margin:0 .5em 0 0}.h1:before{content:'h1'}.h2:before{content:'h2'}.h3:before{content:'h3'}.h4:before{content:'h4'}.h5:before{content:'h5'}.h6:before{content:'h6'}li .missing{font-family:inherit;background-color:#c9302c}table{table-layout:fixed;width:100%;min-width:800px;border-collapse:collapse;border:2px solid #333}thead th:nth-child(1){width:20%}thead th:nth-child(2),thead th:nth-child(4),thead th:nth-child(5),thead th:nth-child(6){width:16%}thead th:nth-child(3){width:16%;font-size:.7em}td,th{padding:.5em;border:1px solid #ddd;border-bottom:none;border-top:none}th{background-color:#333;border-color:#ddd;color:#efefef}tbody tr:nth-child(even){background-color:#f8f8f8}caption{caption-side:top;text-align:left;margin-bottom:1em}details summary{font-style:italic;margin-bottom:1em}details+details{margin-top:1em}img{max-width:100%;max-height:15em;height:auto}td.image{text-align:center;padding:.2em}td.presentation img{width:50%}td.missing{text-align:center;color:#888;font-style:italic}td.alt{border:1px solid #ddd}td.alt.missing{background-color:#f2dede}td.alt,td.image.presentation+td.alt.missing{background-color:#dff0d8}td.location{font-size:.8em;font-family:monospace;overflow-wrap:break-word}</xsl:variable>

  <xsl:template match="opf:package">
    <html>
      <xsl:variable name="filtered" as="node()*">
        <xsl:apply-templates mode="filtering"/>
      </xsl:variable>
      <head>
        <title>Accessibility Report</title>
        <style type="text/css">
                    <xsl:value-of select="$css" disable-output-escaping="yes"/>
                </style>
      </head>
      <body>
        <h1>Accessibility View</h1>
        <section id="outlines">
          <h2>Outlines</h2>
          <div class="outlines">
            <section id="tocnav">

              <h3>EPUB Navigation Document</h3>
              <xsl:apply-templates mode="tocnav"/>
            </section>
            <section id="headings">

              <h3>Headings hierarchy</h3>
              <xsl:call-template name="process-outline">
                <xsl:with-param name="outline" select="$filtered[not(self::img)]"/>
              </xsl:call-template>
            </section>
          </div>
        </section>
        <section id="images">
          <h2>Images</h2>
          <xsl:call-template name="process-images">
            <xsl:with-param name="images" select="$filtered[self::img]"/>
          </xsl:call-template>
        </section>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="opf:item[tokenize(@properties, '\s+') = 'nav']" mode="tocnav">
    <xsl:apply-templates select="document(@href)" mode="tocnav"/>
  </xsl:template>

  <xsl:template match="nav[tokenize(@epub:type, '\s+') = 'toc']" mode="tocnav">
    <xsl:apply-templates select="ol" mode="tocnav-copy"/>
  </xsl:template>
  <xsl:template match="span | a" mode="tocnav-copy">
    <xsl:value-of select="."/>
  </xsl:template>
  <xsl:template match="ol | li" mode="tocnav-copy">
    <xsl:copy>
      <xsl:apply-templates select="*" mode="#current"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="opf:itemref" mode="filtering">
    <xsl:apply-templates select="document(//opf:item[@id = current()/@idref]/@href)"
      mode="filtering">
      <xsl:with-param name="linear" as="xs:boolean"
        select="boolean(not(normalize-space(@linear) = 'no'))" tunnel="yes"/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="h1 | h2 | h3 | h4 | h5 | h6" mode="filtering">
    <xsl:param name="linear" as="xs:boolean" tunnel="yes"/>
    <xsl:if
      test="$linear and empty(ancestor::blockquote | ancestor::fieldset | ancestor::figure | ancestor::td)">
      <xsl:sequence select="."/>
    </xsl:if>
  </xsl:template>
  <xsl:template match="img" mode="filtering">
    <img src="{concat($resources-dir,substring(resolve-uri(@src,base-uri(.)),7))}"
      data-base="{substring(base-uri(),7)}" data-line="{saxon:line-number()}">
      <xsl:copy-of select="@alt | @role"/>
      <xsl:if test="exists(preceding-sibling::p[1])">
        <preceding>
          <xsl:value-of select="preceding-sibling::p[1]"/>
        </preceding>
      </xsl:if>
      <xsl:if test="exists(following-sibling::p[1])">
        <following>
          <xsl:value-of select="following-sibling::p[1]"/>
        </following>
      </xsl:if>
      <xsl:if test="ancestor::figure">
        <xsl:copy-of select="ancestor::figure/figcaption[1]"/>
      </xsl:if>
      <xsl:if test="@aria-describedby">
        <aria-describedby>
          <xsl:copy-of select="//*[@id eq current()/@aria-describedby]"/>
        </aria-describedby>
      </xsl:if>
    </img>
  </xsl:template>

  <xsl:template name="process-outline">
    <xsl:param name="outline" as="node()*"/>
    <xsl:call-template name="process-heading">
      <xsl:with-param name="context" select="$outline"/>
      <xsl:with-param name="level" select="1"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="process-heading">
    <xsl:param name="context" as="node()*" required="yes"/>
    <xsl:param name="level" as="xs:integer" required="yes"/>
    <xsl:variable name="hx" select="concat('h', $level)"/>
    <ul>
      <xsl:for-each-group select="$context" group-starting-with="*[local-name() = $hx]">
        <li>
          <span class="{if (current()[local-name()=$hx]) then $hx else 'missing'}">
            <xsl:value-of
              select="
                if (current()[local-name() = $hx]) then
                  current()
                else
                  concat('missing ', $hx, ' heading')"
            />
          </span>
          <xsl:variable name="remainder"
            select="
              if (current()[local-name() = $hx]) then
                current-group()[position() > 1]
              else
                current-group()"/>
          <xsl:if test="exists($remainder)">
            <xsl:call-template name="process-heading">
              <xsl:with-param name="context" select="$remainder"/>
              <xsl:with-param name="level" select="$level + 1"/>
            </xsl:call-template>
          </xsl:if>
        </li>
      </xsl:for-each-group>
    </ul>
  </xsl:template>

  <xsl:template name="process-images">
    <xsl:param name="images" as="node()*"/>
    <table>
      <caption>Images in the EPUB, with their description</caption>
      <thead>
        <tr>
          <th>Image</th>
          <th><code>alt</code> attribute</th>
          <th><code>aria-describedby</code> content</th>
          <th>Associated <code>figcaption</code></th>
          <th>Context</th>
          <th>Location</th>
        </tr>
      </thead>
      <tbody>
        <xsl:apply-templates select="$images"/>
      </tbody>
    </table>
  </xsl:template>

  <xsl:template match="img">
    <tr>
      <td class="image{if (@role eq 'presentation') then ' presentation' else ''}">
        <xsl:if test="@role eq 'presentation'">
          <span><code>presentation</code> role</span>
        </xsl:if>
        <a href="{@src}"><img src="{@src}" alt="{@alt}"/></a>
      </td>
      <td
        class="alt{if (empty(@alt) or normalize-space(@alt) eq '' and @role eq 'presentation') then ' missing' else ''}">
        <xsl:if test="empty(@alt)"> N/A</xsl:if>
        <xsl:value-of select="@alt"/>
      </td>
      <td>

        <xsl:if test="empty(aria-describedby)">
          <xsl:attribute name="class" select="'missing'"/> N/A </xsl:if>
        <xsl:copy-of select="string(aria-describedby)"/>
      </td>
      <td>

        <xsl:if test="empty(figcaption)">
          <xsl:attribute name="class" select="'missing'"/> N/A </xsl:if>
        <xsl:copy-of select="string(figcaption)"/>
      </td>
      <td>

        <xsl:if test="empty(preceding | following)">
          <xsl:attribute name="class" select="'missing'"/> N/A </xsl:if>
        <xsl:if test="preceding">
          <details>
            <summary>Preceding <code>p</code></summary>
            <xsl:copy-of select="string(preceding)"/>
          </details>
        </xsl:if>
        <xsl:if test="following">
          <details>
            <summary>Following <code>p</code></summary>
            <xsl:copy-of select="string(following)"/>
          </details>
        </xsl:if>
      </td>
      <td class="location">
        <xsl:value-of select="concat(@data-base, ':', @data-line)"/>
      </td>
    </tr>
  </xsl:template>

</xsl:stylesheet>
