<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:_="bar"
                type="px:bar-2.params">

  <p:option name="_:option-2" select="'X'">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <h1 px:role="name">Bar option 2</h1>
    </p:documentation>
    <p:pipeinfo>
      <px:type>
        <choice>
          <value>A</value>
          <value>B</value>
          <value>C</value>
        </choice>
      </px:type>
    </p:pipeinfo>
  </p:option>

</p:declare-step>
