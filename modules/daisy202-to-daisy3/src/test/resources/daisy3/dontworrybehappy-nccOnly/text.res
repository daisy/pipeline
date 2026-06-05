<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE resources PUBLIC "-//NISO//DTD resource 2005-1//EN" "http://www.daisy.org/z3986/2005/resource-2005-1.dtd" []>
<resources xmlns="http://www.daisy.org/z3986/2005/resource/" version="2005-1">
  
  <!-- SKIPPABLE NCX -->
  
  <scope nsuri="http://www.daisy.org/z3986/2005/ncx/">
    <nodeSet id="ns001" select="//smilCustomTest[@bookStruct='LINE_NUMBER']">
      <resource xml:lang="en" id="r001">
        <text>Row</text>
      </resource>
    </nodeSet>
    
    <nodeSet id="ns002" select="//smilCustomTest[@bookStruct='NOTE']">
      <resource xml:lang="en" id="r002">
        <text>Note</text>
      </resource>
    </nodeSet>
    
    <nodeSet id="ns003" select="//smilCustomTest[@bookStruct='NOTE_REFERENCE']">
      <resource xml:lang="en" id="r003">
        <text>Note reference</text>
      </resource>
    </nodeSet>
    
    <nodeSet id="ns004" select="//smilCustomTest[@bookStruct='ANNOTATION']">
      <resource xml:lang="en" id="r004">
        <text>Annotation</text>
      </resource>
    </nodeSet>
    
    <nodeSet id="ns005" select="//smilCustomTest[@id='annoref']">
      <resource xml:lang="en" id="r005">
        <text>Annotation reference</text>
      </resource>
    </nodeSet>
    
    <nodeSet id="ns006" select="//smilCustomTest[@bookStruct='PAGE_NUMBER']">
      <resource xml:lang="en" id="r006">
        <text>Page</text>
      </resource>
    </nodeSet>
    
    <nodeSet id="ns007" select="//smilCustomTest[@bookStruct='OPTIONAL_SIDEBAR']">
      <resource xml:lang="en" id="r007">
        <text>Optional sidebar</text>
      </resource>
    </nodeSet>
    
    <nodeSet id="ns008" select="//smilCustomTest[@bookStruct='OPTIONAL_PRODUCER_NOTE']">
      <resource xml:lang="en" id="r008">
        <text>Optional producer note</text>
      </resource>
    </nodeSet>
  </scope>



	<!-- ESCAPABLE SMIL -->
	
	<scope nsuri="http://www.w3.org/2001/SMIL20/">   
   
    <nodeSet id="esns001" select="//seq[@bookStruct='line']">
      <resource xml:lang="en" id="esr001">
        <text>Row</text>
      </resource>
    </nodeSet>
    
    <nodeSet id="esns002" select="//seq[@class='note']">
      <resource xml:lang="en" id="esr002">
        <text>Note</text>
      </resource>
    </nodeSet>
     
    <nodeSet id="esns003" select="//seq[@class='noteref']">
      <resource xml:lang="en" id="esr003">
        <text>Note reference</text>
      </resource>
    </nodeSet>
    
    <nodeSet id="esns004" select="//seq[@class='annotation']">
      <resource xml:lang="en" id="esr004">
        <text>Annotation</text>
      </resource>
    </nodeSet>
    
    <nodeSet id="esns005" select="//seq[@class='annoref']">
      <resource xml:lang="en" id="esr005">
        <text>Annotation reference</text>
      </resource>
    </nodeSet>
    
    <nodeSet id="esns006" select="//seq[@class='pagenum']">
      <resource xml:lang="en" id="esr006">
        <text>Page</text>
      </resource>
    </nodeSet>
    
    <nodeSet id="esns007" select="//seq[@class='sidebar']">
      <resource xml:lang="en" id="esr007">
        <text>Optional sidebar</text>
      </resource>
    </nodeSet>
    
    <nodeSet id="esns008" select="//seq[@class='prodnote']">
      <resource xml:lang="en" id="esr008">
        <text>Optional producer note</text>
      </resource>
    </nodeSet>
    
  </scope>


	<!-- ESCAPABLE DTBOOK -->

  <scope nsuri="http://www.daisy.org/z3986/2005/dtbook/"> 
  
    <nodeSet id="ns009" select="//annotation">
      <resource xml:lang="en" id="r009">
        <text>Annotation</text>
      </resource>
    </nodeSet>
    
    <nodeSet id="ns010" select="//blockquote">
      <resource xml:lang="en" id="r010">
        <text>Quote</text>
      </resource>
    </nodeSet>
  
	<nodeSet id="ns011" select="//code">
      <resource xml:lang="en" id="r011">
        <text>Code</text>
      </resource>
    </nodeSet>
    
    <nodeSet id="ns012" select="//list">
      <resource xml:lang="en" id="r012">
        <text>List</text>
      </resource>
    </nodeSet>
    
    <nodeSet id="ns018" select="//note">
      <resource xml:lang="en" id="r018">
        <text>Note</text>
      </resource>
    </nodeSet>
    
    <nodeSet id="ns013" select="//poem">
      <resource xml:lang="en" id="r013">
        <text>Poem</text>
      </resource>
    </nodeSet>
    
    <nodeSet id="ns0014" select="//prodnote[@render='optional']">
      <resource xml:lang="en" id="r014">
        <text>Optional producer note</text>
      </resource>
    </nodeSet>
    
    <nodeSet id="ns015" select="//sidebar[@render='optional']">
      <resource xml:lang="en" id="r015">
        <text>Optional sidebar</text>
      </resource>
    </nodeSet>
    
	<nodeSet id="ns016" select="//table">
      <resource xml:lang="en" id="r016">
        <text>Table</text>
      </resource>
    </nodeSet>
    
    <nodeSet id="ns017" select="//tr">
      <resource xml:lang="en" id="r017">
        <text>Table row</text>
      </resource>
    </nodeSet>
   </scope>

</resources>