<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step version="1.0" 
  xmlns:p="http://www.w3.org/ns/xproc"
  xmlns:c="http://www.w3.org/ns/xproc-step" 
  xmlns:tr="http://transpect.io"
  name="debug-single-mode">
  
  <p:documentation xmlns="http://www.w3.org/1999/xhtml">
    <p>The purpose of this XProc pipeline ist to debug a single xslt mode in transpect projects. For faster debugging without the need to run the entire frontend pipeline (i.e. docx2ub -> evolve-hub -> xml2idml)</p>
    <p>Sample invocation:</p>
    <pre><code>calabash/calabash.sh -D \
		-i source=debug-directory/evolve-hub/06.hub_preprocess-hierarchy.xml \
		-i paths=debug-directory/paths.xml \
		-o result=result.xml \
		xproc-util/debug/debug-single-xsltmode/debug-single-xsltmode.xpl \
		mode=hub:hierarchy \
		load=evolve-hub/driver</code></pre>
  </p:documentation>
  
  <p:option name="mode" required="true">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>Mode name to process/debug the source document in. For example: <kbd>hub:hierarchy</kbd></p>
    </p:documentation>
  </p:option>
  <p:option name="load" required="true">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>The relative sylesheet to load by load-cascaded mechanism. 
        Example: <kbd>evolve-hub/driver</kbd> (will be merged with '.xsl' to evolve-hub/driver.xsl).</p>
    </p:documentation>
  </p:option>
    
  <p:input port="source" primary="true">
    <p:documentation>The source/input document to debug.</p:documentation>
  </p:input>
  <p:input port="paths">
    <p:documentation>Transpect project paths file.</p:documentation>
  </p:input>
  <p:output port="result" primary="true">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>The conversion result of the input processed by xslt-mode <em>mode</em> (see <code>p:option</code> mode).</p>
    </p:documentation>
  </p:output>
  
  <p:import href="http://transpect.io/cascade/xpl/load-cascaded.xpl"/>
  <p:import href="http://transpect.io/xproc-util/xslt-mode/xpl/xslt-mode.xpl"/>
    
  <tr:load-cascaded name="load-stylesheet">
    <p:with-option name="filename" 
      select="if($load eq '' and contains($mode, ':')) 
              then concat(substring-before($mode, ':'), '/', substring-before($mode, ':'), '.xsl') 
              else concat($load, '.xsl')">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>There is a auto completion possible: If <code>p:option</code> load is empty, 
          then the value of <code>p:option</code> mode will be separated by character '<kbd>:</kbd>' 
          and the filename will be the result of &lt;string before '<kbd>:</kbd>'>/&lt;string after '<kbd>:</kbd>'>.xsl.</p>
      </p:documentation>
    </p:with-option>
    <p:input port="paths">
      <p:pipe port="paths" step="debug-single-mode"/>
    </p:input>
    <p:with-option name="debug" select="'no'"/>
    <p:with-option name="debug-dir-uri" select="'debug'"/>
  </tr:load-cascaded>

  <p:sink/>

  <tr:xslt-mode msg="yes">
    <p:input port="source">
      <p:pipe port="source" step="debug-single-mode"/>
    </p:input>
    <p:with-option name="mode" select="$mode"/>
    <p:input port="stylesheet"><p:pipe step="load-stylesheet" port="result"/></p:input>
    <p:input port="models"><p:empty/></p:input>
    <p:input port="parameters"><p:empty/></p:input>
    <p:with-option name="debug" select="'no'"/>
    <p:with-option name="debug-dir-uri" select="'debug'"/>
  </tr:xslt-mode>
    
</p:declare-step>