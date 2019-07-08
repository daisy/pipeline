<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" 
  xmlns:c="http://www.w3.org/ns/xproc-step"
  xmlns:cx="http://xmlcalabash.com/ns/extensions"
  xmlns:mt="http://transpect.io/calabash-extensions/mathtype-extension/"
  xmlns:tr="http://transpect.io"
  version="1.0" 
  name="mathtype2mml-directory"
  type="tr:mathtype2mml-directory">

  <p:option name="source-dir" required="true">
    <p:documentation>Input directory: (MathType) formulas</p:documentation>
  </p:option>
  <p:option name="store-mml" required="false" select="'false'">
    <p:documentation>Write generated MathML to disk?</p:documentation>
  </p:option>
  <p:option name="target-dir" required="false" select="''">
    <p:documentation>Output directory for MathML (option 'store-mml' is set to 'true')</p:documentation>
  </p:option>
  <p:option name="type" required="true">
    <p:documentation>possible values in order of conversion quality: bin, wmf, eps</p:documentation>
  </p:option>
  <p:option name="mml-ext" select="'.mml'">
    <p:documentation>File extension for MathML result</p:documentation>
  </p:option>
  <p:option name="debug" select="'no'"/>
  <p:option name="debug-dir-uri" select="concat($target-dir, 'debug/')"/>
  
  <p:input port="additional-font-maps">
    <p:empty/>
  </p:input>
  
  <p:output port="result" primary="true">
    <p:documentation>'c:results' xml document with all MathML (or error) results.</p:documentation>
  </p:output>
  
  <p:import href="http://xmlcalabash.com/extension/steps/library-1.0.xpl"/>
  <p:import href="http://transpect.io/calabash-extensions/mathtype-extension/xpl/mathtype2mml-declaration.xpl"/>
  
  <p:variable name="mtef-type" select="('.eps'[$type='eps'], '.wmf'[$type = 'wmf'], '.bin')[1]"/>
  <p:variable name="include" select="concat('.*\', $mtef-type)"/>
  <p:variable name="exclude" select="concat('.*\', $mml-ext)"/>
  <p:try>
    <p:group>
      <p:directory-list name="list">
        <p:with-option name="path" select="$source-dir"/>
        <p:with-option name="include-filter" select="$include"/>
        <p:with-option name="exclude-filter" select="$exclude"/>
      </p:directory-list>
      <cx:message>
        <p:with-option name="message" 
          select="concat('Number of files found to process: ', count(/*/c:file), 
                    ' (source-dir: ''', $source-dir, ''', include-filter: ''', 
                    $include, ''', exclude-filter: ''', $exclude, ''')')"/>
      </cx:message>
      <p:viewport match="c:directory/c:file" name="process-files">
        <p:output port="result" primary="true"/>
        <p:variable name="file" select="replace(c:file/@name, $mtef-type, '')"/>
        <p:variable name="position" select="replace($file, ('image'[$mtef-type = '.wmf'], 'oleObject')[1], '')"/>
        <p:variable name="store-path" select="concat($target-dir, concat($file, $mml-ext))"/>
        <cx:message>
          <p:with-option name="message" 
            select="concat('---------------------------------&#xa;
        --- render_singlefile: ', $file, $mtef-type, '&#xa;
        ---------------------------------')"/>
        </cx:message>
        <p:try>
          <p:group>
            <p:output port="result" primary="true">
              <p:pipe port="result" step="mathtype2mml"/>
            </p:output>
            <tr:mathtype2mml name="mathtype2mml">
              <p:input port="additional-font-maps">
                <p:pipe port="additional-font-maps" step="mathtype2mml-directory"/>
              </p:input>
              <p:with-option name="href" select="concat($source-dir, c:file/@name)"/>
              <!-- <p:with-option name="mml-space-handling" select="'char'"/> -->
              <p:with-option name="mml-space-handling" select="'mspace'"/>
              <p:with-option name="debug" select="$debug"/>
              <p:with-option name="debug-dir-uri" select="$debug-dir-uri"/>
            </tr:mathtype2mml>
            <p:choose>
              <p:when test="$store-mml = 'true' and $target-dir != ''">
                <p:try>
                  <p:group>
                    <p:store indent="true">
                      <p:with-option name="href" select="$store-path"/>
                    </p:store>
                  </p:group>
                  <p:catch name="catch-store">
                    <cx:message>
                      <p:input port="source">
                        <p:pipe port="error" step="catch-store"/>
                      </p:input>
                      <p:with-option name="message" select="'Result cannot be written to disk: ', $store-path"/>
                    </cx:message>
                    <p:sink/>
                  </p:catch>
                </p:try>
              </p:when>
              <p:otherwise>
                <p:sink/>
              </p:otherwise>
            </p:choose>
          </p:group>
          <p:catch>
            <p:output port="result" primary="true"/>
            <p:identity>
              <p:input port="source">
                <p:inline>
                  <math xmlns="http://www.w3.org/1998/Math/MathML"/>
                </p:inline>
              </p:input>
            </p:identity>
          </p:catch>
        </p:try>
        <p:wrap match="*:math | c:errors" wrapper="file" wrapper-namespace="http://www.w3.org/ns/xproc-step" wrapper-prefix="c"/>
        <p:add-attribute attribute-name="number" match="c:file">
          <p:with-option name="attribute-value" select="$position"/>
        </p:add-attribute>
        <p:add-attribute attribute-name="name" match="c:file">
          <p:with-option name="attribute-value" select="/*/@name">
            <p:pipe port="current" step="process-files"/>
          </p:with-option>
        </p:add-attribute>
      </p:viewport>
    </p:group>
    <p:catch>
      <cx:message>
        <p:input port="source">
          <p:empty/>
        </p:input>
        <p:with-option name="message" select="'Error?'"/>
      </cx:message>
    </p:catch>
  </p:try>
  <p:wrap-sequence wrapper="c:results"/>
  
</p:declare-step>
