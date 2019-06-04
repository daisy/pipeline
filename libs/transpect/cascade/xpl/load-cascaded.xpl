<?xml version="1.0" encoding="utf-8"?>
<p:library xmlns:p="http://www.w3.org/ns/xproc" 
  xmlns:c="http://www.w3.org/ns/xproc-step"
  xmlns:cx="http://xmlcalabash.com/ns/extensions" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:s="http://purl.oclc.org/dsdl/schematron"
  xmlns:tr="http://transpect.io"
  version="1.0">

  <p:import href="http://xmlcalabash.com/extension/steps/library-1.0.xpl" />
  <p:import href="http://transpect.io/xproc-util/store-debug/xpl/store-debug.xpl"/>

  <p:declare-step name="load-cascaded" type="tr:load-cascaded">
    <p:documentation>Loads the most specific XML file $filename from a sequence of paths, supplied as 
    parameters on the paths port ($s9y1-path, $s9y2-path, …, in descending specificity). 
    The XML file can be an XProc pipeline, an XSLT stylesheet, a Schematron schema, an XHTML file, whatever.  
    If no document named {$filename} is found at a given path, it checks whether {$filename}.xsl exists.
    If it exist, the main template of this XSL is invoked and this should return the document that was 
    originally expected at {$filename}.
    If neither a proper {$filename} nor {$filename}.xsl can be found under any of the paths, and if
    a non-empty option $fallback is specified, the fallback file will be used. 
    </p:documentation>

    <p:option name="filename" required="true"/>
    <p:option name="required" required="false" select="'yes'"/>
    <p:option name="fallback" required="false" select="''"/>
    <p:option name="set-xml-base-attribute" required="false" select="'yes'"/>
    <p:option name="debug" required="false" select="'no'"/>
    <p:option name="debug-dir-uri" />

    <p:input port="paths" kind="parameter" primary="true"/>
    <p:input port="source" primary="true" sequence="true">
      <p:documentation>If we don’t have this dummy primary port, the document(s) on the default readable port may 
        replace the catalog on the catalog port.</p:documentation>
      <p:empty/>
    </p:input>
    <p:input port="catalog" sequence="true">
      <p:document href="http://this.transpect.io/xmlcatalog/catalog.xml"/>
    </p:input>
    <p:output port="result" primary="true"/>

    <p:xslt template-name="main" name="load-cascaded-xsl">
      <p:input port="source">
        <p:pipe port="catalog" step="load-cascaded"/>
      </p:input>
      <p:input port="parameters">
        <p:pipe port="paths" step="load-cascaded"/>
      </p:input>
      <p:with-param name="filename" select="$filename"/>
      <p:with-param name="fallback" select="$fallback"/>
      <p:with-param name="required" select="$required"/>
      <p:with-param name="set-xml-base-attribute" select="$set-xml-base-attribute"/>
      <p:input port="stylesheet">
        <!-- Overwrite the URI resolution for this particular URL in order to be able to provide your own cascaded loader: -->
        <p:document href="http://transpect.io/cascade/xsl/load-cascaded.xsl"/>
      </p:input>
    </p:xslt>

    <p:sink/>

    <p:choose name="potentially-eval-returned-xsl">
      <p:xpath-context>
        <p:pipe port="result" step="load-cascaded-xsl"/>
      </p:xpath-context>
      <p:when test="replace(base-uri(/*), '^.+/', '') = concat(replace($filename, '^.+/', ''), '.xsl')">
        <p:xslt template-name="main">
          <p:with-option name="output-base-uri" select="resolve-uri($filename)"/>
          <p:input port="source"><p:empty/></p:input>
          <p:input port="parameters">
            <p:pipe port="paths" step="load-cascaded"/>
          </p:input>
          <p:input port="stylesheet">
            <p:pipe port="result" step="load-cascaded-xsl"/>
          </p:input>
        </p:xslt>
      </p:when>
      <p:otherwise>
        <p:identity>
          <p:input port="source">
            <p:pipe port="result" step="load-cascaded-xsl"/>
          </p:input>
        </p:identity>
      </p:otherwise>
    </p:choose>
    
    <tr:store-debug name="store-debug">
      <p:with-option name="active" select="$debug"/>
      <p:with-option name="base-uri" select="$debug-dir-uri"/>
      <p:with-option name="pipeline-step" select="replace($filename, '^(.+?)\.([^/]+)$', '$1')"/>
      <p:with-option name="extension" select="replace($filename, '^(.+?)\.([^/]+)$', '$2')"/>
    </tr:store-debug>

  </p:declare-step>


  <!-- step load-cascaded-binary: retrieve an uri for a binary file
       input example: idml/templates/two-column-layout.idml
       output will be /tr:result[@uri] -->
  <p:declare-step name="load-cascaded-binary" type="tr:load-cascaded-binary">

    <p:option name="filename" required="true"/>
    <p:option name="required" required="false" select="'yes'"/>
    <p:option name="fallback" required="false" select="''"/>
    <p:option name="result-with-file-prefix" required="false" select="'no'"/>
    <p:option name="debug" required="false" select="'no'"/>
    <p:option name="debug-dir-uri" />

    <p:input port="paths" kind="parameter" primary="true"/>
    <p:output port="result" primary="true"/>

    <p:xslt template-name="return-cascade-paths">
      <p:with-param name="filename" select="$filename"/>
      <p:with-param name="fallback" select="$fallback"/>
      <p:input port="source"><p:empty/></p:input>
      <p:input port="parameters">
        <p:pipe port="paths" step="load-cascaded-binary"/>
      </p:input>
      <p:input port="stylesheet">
        <p:document href="http://transpect.io/cascade/xsl/load-cascaded.xsl"/>
      </p:input>
    </p:xslt>

    <p:for-each name="cascade-directory-list">
      <p:iteration-source select="/tr:results/tr:result"/>
      <p:try>
        <p:group>
          <p:directory-list>
            <p:with-option name="path" select="resolve-uri(/*/@path)"/>
          </p:directory-list>
        </p:group>
        <p:catch>
          <p:identity>
            <p:input port="source">
              <p:inline>
                <c:directory-unavailable/>
              </p:inline>
            </p:input>
          </p:identity>
        </p:catch>
      </p:try>
    </p:for-each>

    <p:wrap-sequence wrapper="directories" />

    <p:xslt template-name="return-cascaded-binary-uri">
      <p:with-param name="filename" select="$filename"/>
      <p:with-param name="required" select="$required"/>
      <p:with-param name="binary-resultpath-with-file-prefix" select="$result-with-file-prefix"/>
      <p:input port="stylesheet">
        <p:document href="http://transpect.io/cascade/xsl/load-cascaded.xsl"/>
      </p:input>
    </p:xslt>

  </p:declare-step>

  <p:declare-step name="load-whole-cascade" type="tr:load-whole-cascade">
    <p:documentation>Loads all documents of a specific name from the path cascade. 
    With the default order of 'least-specific-first', the most generic document will be first, the most specific last.
    The option 'order' may be set to 'most-specific-first'.</p:documentation>
    <p:option name="filename" required="true"/>
    <p:option name="order" select="'least-specific-first'"/>
    <p:input port="paths" kind="parameter" primary="true"/>
    <p:output port="result" primary="true" sequence="true">
      <p:pipe port="secondary" step="xslt-load-whole-cascade"/>
    </p:output>
    <p:xslt name="xslt-load-whole-cascade" template-name="main">
      <p:input port="source">
        <p:empty/>
      </p:input>
      <p:input port="parameters">
        <p:pipe port="paths" step="load-whole-cascade"/>
      </p:input>
      <p:with-param name="filename" select="$filename"/>
      <p:with-param name="order" select="$order"/>
      <p:input port="stylesheet">
        <p:inline>
          <xsl:stylesheet version="2.0">
            <xsl:param name="filename" as="xs:string"/>
            <xsl:param name="order" as="xs:string"/>
            <xsl:param name="s9y1-path" as="xs:string?"/>
            <xsl:param name="s9y2-path" as="xs:string?"/>
            <xsl:param name="s9y3-path" as="xs:string?"/>
            <xsl:param name="s9y4-path" as="xs:string?"/>
            <xsl:param name="s9y5-path" as="xs:string?"/>
            <xsl:param name="s9y6-path" as="xs:string?"/>
            <xsl:param name="s9y7-path" as="xs:string?"/>
            <xsl:param name="s9y8-path" as="xs:string?"/>
            <xsl:param name="s9y9-path" as="xs:string?"/>
            
             <xsl:function name="tr:load-document-nodes" as="document-node(element(*))?">
               <xsl:param name="file-uri" as="xs:string"/>
               <xsl:apply-templates select="doc($file-uri)" mode="add-base">
                 <xsl:with-param name="base" select="$file-uri"/>
               </xsl:apply-templates>
             </xsl:function>
           
            <xsl:function name="tr:load-docs" as="document-node()*">
              <xsl:param name="filename" as="xs:string"/>
              <xsl:param name="uris" as="xs:string*"/>
              <xsl:if test="exists($uris)">
                <xsl:if test="doc-available(concat($uris[1], '/', $filename))">
                  <xsl:sequence select="tr:load-document-nodes(concat($uris[1], '/', $filename))"/>
                </xsl:if>
                <xsl:sequence select="tr:load-docs($filename, $uris[position() gt 1])"/>
              </xsl:if>
            </xsl:function>
            
            <xsl:template match="/*" mode="add-base">
              <xsl:param name="base" as="xs:string"/>
              <xsl:document>
                <xsl:copy>
                  <xsl:attribute name="xml:base" select="$base"/>
                  <xsl:apply-templates select="@*, node()" mode="identity"/>
                </xsl:copy>
              </xsl:document>
            </xsl:template>
            
            <xsl:template match="@*|node()" mode="identity">
              <xsl:copy>
                <xsl:apply-templates select="@*, node()" mode="identity"/>
              </xsl:copy>
            </xsl:template>
            
            <xsl:variable name="docs" as="document-node()*"
              select="tr:load-docs($filename, ($s9y9-path, $s9y8-path, $s9y7-path, $s9y6-path, $s9y5-path, $s9y4-path, $s9y3-path, $s9y2-path, $s9y1-path))"/>
            
            <xsl:template name="main">
              <xsl:for-each select="if ($order = 'most-specific-first') then reverse($docs) else $docs">
                <xsl:result-document href="{concat(base-uri(/*), position())}">
                  <xsl:sequence select="."/>
                </xsl:result-document>
              </xsl:for-each>
            </xsl:template>
          </xsl:stylesheet>
        </p:inline>
      </p:input>
    </p:xslt>
    <p:sink/>
  </p:declare-step>

</p:library>
