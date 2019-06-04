<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
  xmlns:c="http://www.w3.org/ns/xproc-step"
  xmlns:cx="http://xmlcalabash.com/ns/extensions"
  xmlns:calstable="http://docs.oasis-open.org/ns/oasis-exchange/table"
  xmlns:tr="http://transpect.io"
  xmlns="http://docbook.org/ns/docbook"
  version="1.0" 
  name="resolve-nested-calstables"
  type="tr:resolve-nested-calstables">
  
  <p:input port="source"/>  
  <p:output port="result"/>
    
  <p:import href="normalize.xpl"/>
  <p:import href="http://xmlcalabash.com/extension/steps/library-1.0.xpl"/>
  
  <!-- resolve colspans and rowspans in the first place -->
  
  <tr:normalize-calstables name="normalize"/>

  <p:viewport match="*:tgroup[empty(ancestor::*:tgroup)]">
    
    <p:xslt initial-mode="calstable:resolve-nested-tables">
      <p:input port="stylesheet">
        <p:document href="../xsl/resolve-nested-tables.xsl"/>
      </p:input>
      <p:input port="parameters">
        <p:empty/>
      </p:input>
    </p:xslt>
    
  </p:viewport>

</p:declare-step>