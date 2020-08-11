<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:bar="bar">

    <p:option name="option-1" required="true">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">Option 1</h1>
        <p px:role="desc" xml:space="preserve">Enables something.

For more info see [link](http://example.org/more-info).</p>
      </p:documentation>
      <p:pipeinfo>
        <px:type>
          <choice>
            <value>value-1</value>
            <value>value-2</value>
          </choice>
        </px:type>
      </p:pipeinfo>
    </p:option>

    <p:input port="foo-params" kind="parameter" px:options="foo"/>

    <p:option name="bar:option-1" required="false" select="'xyz'">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">Bar option 1</h1>
      </p:documentation>
    </p:option>

</p:declare-step>
