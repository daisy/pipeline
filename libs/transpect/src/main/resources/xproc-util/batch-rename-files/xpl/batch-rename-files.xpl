<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
  xmlns:c="http://www.w3.org/ns/xproc-step"
  xmlns:cx="http://xmlcalabash.com/ns/extensions"
  xmlns:cxf="http://xmlcalabash.com/ns/extensions/fileutils"
  xmlns:tr="http://transpect.io"
  version="1.0" 
  name="batch-rename-files"
  type="tr:batch-rename-files">
  
  <p:documentation xmlns="http://www.w3.org/1999/xhtml">
    <p>Renames file references in a XML document and its physical manifestations. 
      The fileref attributes are matched by a regex pattern. The filerefs are 
      replaced with the value of the replace option.</p>
    <h6>Example: rename file extensions</h6>
    <pre><code class="xml">
&lt;tr:batch-rename-files&gt;
  &lt;p:with-option name="attribute-name" select="'fileref'"/&gt;
  &lt;p:with-option name="regex-match" select="'^(.+)\.tif$'"/&gt;
  &lt;p:with-option name="regex-replace" select="'$1.jpg'"/&gt;
&lt;/tr:batch-rename-files&gt;
    </code></pre>
    <h6>Example: replace whitespace</h6>
    <pre><code class="xml">
&lt;tr:batch-rename-files&gt;
  &lt;p:with-option name="attribute-name" select="'fileref'"/&gt;
  &lt;p:with-option name="regex-match" select="'\s'"/&gt;
  &lt;p:with-option name="regex-replace" select="''"/&gt;
&lt;/tr:batch-rename-files&gt;
    </code></pre>
  </p:documentation>
  
  <p:input port="source">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>The source port expects an XML document.</p>
    </p:documentation>
  </p:input>
  
  <p:output port="result" primary="true" sequence="true">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>The result port provides the XML document with the renamed file references.</p>
    </p:documentation>
    <p:pipe port="result" step="rename-file-references"/>
  </p:output>
  
  <p:output port="report" primary="false">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>The files port provides the list of renamed files.</p>
    </p:documentation>
    <p:pipe port="result" step="move-files-result"/>
  </p:output>
  
  <p:option name="attribute-name" select="'fileref'">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>A string specifying exactly the filerefs that should be renamed.</p>
    </p:documentation>
  </p:option>
  
  <p:option name="regex-match" required="true">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>A string containing a match pattern.</p>
    </p:documentation>
  </p:option>
  
  <p:option name="regex-replace" required="true">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>A string containing the replace pattern.</p>
    </p:documentation>
  </p:option>
    
  <p:import href="http://xmlcalabash.com/extension/steps/library-1.0.xpl"/>
  
  <p:xslt name="rename-file-references">
    <p:with-param name="attribute-name" select="$attribute-name"/>
    <p:with-param name="regex-match" select="$regex-match"/>
    <p:with-param name="regex-replace" select="$regex-replace"/>
    <p:input port="stylesheet">
      <p:document href="../xsl/batch-rename-files.xsl"/>
    </p:input>
  </p:xslt>
  
  <p:for-each name="move-files">
    <p:iteration-source select="//*[@tr:original-href]"/>
    <p:variable name="original-href" select="resolve-uri(*/@tr:original-href, */base-uri())"/>
    <p:variable name="new-href" select="resolve-uri(*/@*[local-name() eq $attribute-name], */base-uri())"/>
    
    <p:try name="try">
      <p:group>
        <p:output port="result" primary="true">
          <p:pipe port="result" step="rename"/>
        </p:output>
        
        <cx:message>
          <p:with-option name="message" select="'move file: ', $original-href, '&#xa; => ', $new-href"/>
        </cx:message>
        
        <cxf:move name="move">
          <p:with-option name="href" select="$original-href"/>
          <p:with-option name="target" select="$new-href"/>
        </cxf:move>
        
        <p:rename match="c:result" new-name="file" new-prefix="c" new-namespace="http://www.w3.org/ns/xproc-step" name="rename">
          <p:input port="source">
            <p:pipe port="result" step="move"/>
          </p:input>
        </p:rename>
        
      </p:group>
      <p:catch name="catch">
        <p:output port="result" primary="true">
          <p:pipe port="result" step="unwrap"/>
        </p:output>
        
        <p:identity>
          <p:input port="source">
            <p:pipe port="error" step="catch"/>
          </p:input>
        </p:identity>
        
        <p:unwrap match="/c:errors" name="unwrap"/>
        
      </p:catch>
    </p:try>
    
    <p:add-attribute attribute-name="original-href" match="c:file|c:error">
      <p:with-option name="attribute-value" select="$original-href"/>
    </p:add-attribute>
    
    <p:add-attribute attribute-name="href" match="c:file">
      <p:with-option name="attribute-value" select="$new-href"/>
    </p:add-attribute>
    
  </p:for-each>
  
  <p:wrap-sequence wrapper="result" wrapper-prefix="c" wrapper-namespace="http://www.w3.org/ns/xproc-step" name="move-files-result"/>
  
</p:declare-step>
