<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
  xmlns:c="http://www.w3.org/ns/xproc-step" 
  xmlns:tr="http://transpect.io"
  version="1.0"
  name="resolve-params"
  type="tr:resolve-params">
  
  <p:documentation xmlns="http://www.w3.org/1999/xhtml">
    <p>This step takes a c:param-set document as input. Parameters which follow the syntax 
      <code><b>${name}</b></code> are resolved with matching parameters from this document. For 
      example the parameter <code>${isbn}</code> will be replaced with the <code>@value</code> 
      of a <code>c:param</code> element which contains a matching <code>@name</code> attribute.</p>
    <p>Given this input document:</p>
    <pre><code>&lt;c:param-set xmlns:c="http://www.w3.org/ns/xproc-step"&gt;
  &lt;param name="isbn" value="(97[89]){1}\d{9}"/&gt;
  &lt;param name="epub-filename" value="<b>{$isbn}</b>\.epub"/&gt;
&lt;/c:param-set&gt;</code></pre>
    <p>This step would resolve the isbn parameter in <code>c:param[@name eq 'epub-filename']</code> 
      and generates this output:</p>
    <pre><code>&lt;c:param-set xmlns:c="http://www.w3.org/ns/xproc-step"&gt;
  &lt;param name="isbn" value="(97[89]){1}\d{9}"/&gt;
  &lt;param name="epub-filename" value="<b>(97[89]){1}\d{9}</b>\.epub"/&gt;
&lt;/c:param-set&gt;</code></pre>
  </p:documentation>
  
  <p:input port="source">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <h3>Input port: <code>source</code></h3>
      <p>The source port expects a c:param-set document.</p>
    </p:documentation>
  </p:input>
  
  <p:output port="result">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <h3>Output port: <code>result</code></h3>
      <p>The c:param-set document with resolved parameters.</p>
    </p:documentation>
  </p:output>
  
  <!--  *
        * check if input is a valid c:param-set document
        * -->
  <p:validate-with-relax-ng assert-valid="true">
    <p:input port="schema">
      <p:inline>
        <grammar xmlns:c="http://www.w3.org/ns/xproc-step" 
          xmlns:tr="http://transpect.io"
          xmlns="http://relaxng.org/ns/structure/1.0" 
          datatypeLibrary="http://www.w3.org/2001/XMLSchema-datatypes">
          <start>
            <element name="c:param-set">
              <optional>
                <attribute name="xml:base"/>
              </optional>
              <oneOrMore>
                <element name="c:param">
                  <ref name="param.content"/>
                </element>
              </oneOrMore>
            </element>
          </start>
          <define name="param.content">
            <interleave>
              <attribute name="name">
                <text/>
              </attribute>
              <attribute name="value">
                <text/>
              </attribute>
              <optional>
                <attribute name="namespace">
                  <data type="anyURI"/>
                </attribute>
              </optional>
            </interleave>
          </define>
        </grammar>
      </p:inline>
    </p:input>
  </p:validate-with-relax-ng>

  <!--  *
        * resolve parameters in c:param-set document
        * -->
  <p:xslt>
    <p:input port="stylesheet">
      <p:document href="../xsl/resolve-params.xsl"/>
    </p:input>
    <p:input port="parameters">
      <p:empty/>
    </p:input>
  </p:xslt>
  
</p:declare-step>