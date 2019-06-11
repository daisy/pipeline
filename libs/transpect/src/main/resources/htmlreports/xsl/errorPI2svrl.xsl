<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:s="http://purl.oclc.org/dsdl/schematron"
  xmlns:tr="http://transpect.io" 
  xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
  xmlns="http://purl.oclc.org/dsdl/svrl"
  exclude-result-prefixes="xs"
  version="2.0">
  
  <!--  * This stylesheet generates a SVRL from an XML document which contains 
        * error messages as processing instructions (PI). Naturally, these PIs come 
        * from our Calabash extension step rng-validate-to-PI.xpl.
        * => http://transpect.io/calabash-extensions/rng-extension/xpl/rng-validate-to-PI.xpl
        * The SVRL acts as input for the patch-svrl.xpl step which generates an 
        * HTML report from the SVRL and a HTML document with @srcpath attributes.
        * -->

  <xsl:output indent="yes"/>

  <!-- Space-separated list of PI names -->
  <xsl:param name="pi-names" as="xs:string"/>
  
  <!-- message | warning | error | fatal-error -->
  <xsl:param name="severity" as="xs:string"/>

  <xsl:param name="group-by-srcpath" as="xs:string"/>

  <xsl:param name="step-name" as="xs:string"/>

  <!-- e.g., "RNG_tei-cssa: message text" -->
  <xsl:variable name="msg-regex" select="'^((\w+)[-_]([-_\w]+)):?\s+(.+)$'" as="xs:string"/>

	<xsl:variable name="source-dir-uri" as="xs:string"
		select="(/*/@source-dir-uri, /*/*:info/*:keywordset[@role = 'hub']/*:keyword[@role = 'source-dir-uri'], '')[1]"/>

  <xsl:template match="/">
    <xsl:for-each-group select="//processing-instruction()[name() = tokenize($pi-names, '\s+')]" group-by="replace(., $msg-regex, '$2')">
      <xsl:result-document href="{resolve-uri(concat(current-grouping-key(), '.svrl.xml'))}">
        <svrl:schematron-output tr:rule-family="{current-grouping-key()}">
          <xsl:if test="normalize-space($step-name)">
            <xsl:attribute name="tr:step-name" select="$step-name"/>
          </xsl:if>
          <xsl:for-each-group select="current-group()" group-by="replace(., $msg-regex, if($group-by-srcpath='yes') then '$1' else '$3')">
            <svrl:active-pattern document="{base-uri()}" id="{current-grouping-key()}" name="{current-grouping-key()}"/>
            <xsl:apply-templates select="current-group()">
              <xsl:with-param name="id" select="current-grouping-key()"/>
            </xsl:apply-templates>
          </xsl:for-each-group>
        </svrl:schematron-output>
      </xsl:result-document>
    </xsl:for-each-group>
  </xsl:template>
  
  <!-- if it ends in 'ok', no message should be generated -->
  <xsl:template match="processing-instruction()[not(matches(., '\S+\sok$'))]">
    <xsl:param name="id" as="xs:string"/>
    <!-- determine severity by abbreviation of error types -->
    <xsl:variable name="actual-severity" select="if(matches(., 'NFO')) then 'info'
                                            else if(matches(., 'ERR')) then 'error'
                                            else if(matches(., 'WRN')) then 'warning'
                                            else if(matches(., 'NRE')) then 'fatal-error'
                                            else $severity" as="xs:string">
    </xsl:variable>
    <!-- try to get any element including an srcpath attribute near the error location -->
    <xsl:variable name="srcpath" select="(ancestor::*[@srcpath][1]/@srcpath,
                                          following-sibling::*[1][@srcpath]/@srcpath,
                                          (..//@srcpath)[1],
                                          preceding::*[@srcpath][1]/@srcpath,
                                          following::*[@srcpath][1]/@srcpath
                                          )[1]" as="xs:string?"/>
    
    <!--  * currently, we are unable to group schema validation errors by their type, hence this would
          * mean to parse the error message with regular expressions. Therefore we follow the approach to 
          * group the error message by the element name. If this is not applicable, we use simply the name 
          * of the former processing instruction.
          * -->
    <xsl:variable name="error-name" select="if($srcpath and $group-by-srcpath='yes') 
                                            then replace(tokenize($srcpath, '/')[last()], '\[\d+\](;[a-z]=\d+)?$', '') 
                                            else $id" as="xs:string"/>
    <xsl:call-template name="svrl:successful-report">
      <xsl:with-param name="actual-severity" select="$actual-severity"/>
      <xsl:with-param name="srcpath" select="$srcpath"/>
      <xsl:with-param name="error-name" select="$error-name"/>
    </xsl:call-template>
    
  </xsl:template>
  
  <xsl:template name="svrl:successful-report" as="element(svrl:successful-report)">
    <!-- context: processing instruction with validation error message -->
    <xsl:param name="error-name" as="xs:string"/>
    <xsl:param name="actual-severity" as="xs:string"/>
    <xsl:param name="srcpath" as="xs:string?"/>
    <svrl:successful-report test="(: unknown :)" id="{$error-name}" role="{$actual-severity}" location="/">
      <svrl:text>
        <span xmlns="http://purl.oclc.org/dsdl/schematron" class="srcpath">
          <xsl:if test="not($srcpath)">
            <xsl:message>errorPI2svrl: could not find srcpath for PI <xsl:value-of select="string-join((name(), .), ': ')"/></xsl:message>
          </xsl:if>
          <xsl:value-of select="string-join(($source-dir-uri, if (not($srcpath)) then 'BC_orphans' else $srcpath), '')"/>
        </span>
        <xsl:call-template name="svrl:error-text"/>
      </svrl:text>
    </svrl:successful-report>
  </xsl:template>
  
  <xsl:template name="svrl:error-text">
    <!-- context: processing instruction with validation error message -->
    <xsl:value-of select="replace(., '^.+?( (NFO|ERR|WRN|NRE))?\s+', '')"/>
  </xsl:template>
  
</xsl:stylesheet>
