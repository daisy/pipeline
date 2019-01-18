<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step version="1.0" type="px:apply-stylesheets"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                exclude-inline-prefixes="px pxi css"
                name="main">
	
	<p:documentation>
		Apply CSS/SASS and/or XSLT stylesheets.
	</p:documentation>
	
	<p:input port="source">
		<p:documentation>
			Style sheets can be associated with the source in several ways: linked (using an
			'xml-stylesheet' processing instruction or a 'link' element), embedded (using a 'style'
			element) and/or inlined (using 'style' attributes).
		</p:documentation>
	</p:input>
	
	<p:output port="result">
		<p:documentation>
			Style sheets are applied to the document in the following way: XSLT style sheets are
			applied before CSS/SASS style sheets. XSLT style sheets are applied one by one, first
			the ones from the 'stylesheets' option, then the ones associated with the source
			document, in the order in which they are specified. CSS/SASS style sheets are applied by
			"inlining" them, i.e. performing the cascade and capturing the styles of individual
			elements in 'style' attributes. All CSS/SASS style sheets are applied at once, but the
			order in which they are specified (first the ones from the 'stylesheets' option, then
			the ones associated with the source document) has an influence on the cascading order.
		</p:documentation>
	</p:output>
	
	<p:option name="stylesheets" required="false" select="''">
		<p:documentation>
			A space separated list of URIs, absolute or relative to source. XSLT style sheets must
			be specified before CSS/SASS style sheets.
		</p:documentation>
	</p:option>
	
	<p:input port="parameters" kind="parameter" primary="false">
		<p:documentation>
			These parameters are passed on to the XSLT transformations and injected as variables
			into SASS style sheets.
		</p:documentation>
	</p:input>
	
	<p:declare-step type="pxi:recursive-xslt" name="recursive-xslt">
		<p:input port="source"/>
		<p:output port="result"/>
		<p:option name="stylesheets" required="true"/>
		<p:input kind="parameter" port="parameters" primary="true"/>
		<p:choose px:progress="1">
			<p:when test="exists(tokenize($stylesheets,'\s+')[not(.='')])">
				<p:variable name="stylesheet" select="tokenize($stylesheets,'\s+')[not(.='')][1]"/>
				<p:load name="stylesheet">
					<p:with-option name="href" select="resolve-uri($stylesheet,base-uri(/*))"/>
				</p:load>
				<p:xslt px:message="Applying {$stylesheet}" px:progress="1/{count(tokenize($stylesheets,'\s+'))}">
					<p:input port="source">
						<p:pipe step="recursive-xslt" port="source"/>
					</p:input>
					<p:input port="stylesheet">
						<p:pipe step="stylesheet" port="result"/>
					</p:input>
				</p:xslt>
				<pxi:recursive-xslt px:progress="{count(tokenize($stylesheets,'\s+')) - 1}/{count(tokenize($stylesheets,'\s+'))}">
					<p:with-option name="stylesheets" select="string-join(tokenize($stylesheets,'\s+')[not(.='')][position()&gt;1],' ')"/>
				</pxi:recursive-xslt>
			</p:when>
			<p:otherwise>
				<p:identity/>
			</p:otherwise>
		</p:choose>
	</p:declare-step>
	
	<p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
	<p:import href="http://www.daisy.org/pipeline/modules/braille/common-utils/library.xpl"/>
	<p:import href="http://www.daisy.org/pipeline/modules/braille/css-utils/library.xpl"/>
	
	<p:variable name="xslt-stylesheets" select="string-join(tokenize($stylesheets,'\s+')[matches(.,'\.xslt?$')],' ')"/>
	<p:variable name="css-stylesheets" select="string-join(tokenize($stylesheets,'\s+')[matches(.,'\.s?css$')],' ')"/>
	
	<p:choose>
		<p:when test="not(string-join(($xslt-stylesheets,$css-stylesheets)[not(.='')],' ')=normalize-space($stylesheets))">
			<px:error code="BRL01" message="Invalid `stylesheets` option specified: $1">
				<p:with-option name="param1" select="$stylesheets"/>
			</px:error>
		</p:when>
		<p:otherwise>
			<p:identity/>
		</p:otherwise>
	</p:choose>
	
	<px:parse-xml-stylesheet-instructions name="xml-stylesheet-instructions" px:progress=".05"/>
	
	<p:group px:progress=".95">
		<p:variable name="all-xslt-stylesheets"
		            select="string-join((
		                      $xslt-stylesheets,
		                      /d:xml-stylesheets/d:xml-stylesheet[not(@type) and matches(@href,'\.xslt?$')]
		                      /@href),' ')"/>
		<p:variable name="all-css-stylesheets"
		            select="string-join((
		                      $css-stylesheets,
		                      /d:xml-stylesheets/d:xml-stylesheet[@type='text/css' or (not(@type[not(.='text/css')]) and matches(@href,'\.s?css$'))]
		                      /@href),' ')"/>
		<p:identity>
			<p:input port="source">
				<p:pipe step="main" port="source"/>
			</p:input>
		</p:identity>
		<pxi:recursive-xslt px:progress=".50">
			<p:with-option name="stylesheets" select="$all-xslt-stylesheets"/>
			<p:input port="parameters">
				<p:pipe step="main" port="parameters"/>
			</p:input>
		</pxi:recursive-xslt>
		<css:inline px:message="Applying CSS{if (exists(tokenize($all-css-stylesheets,'\s+')[not(.='')]))
		                                     then concat(':',string-join(('',tokenize($all-css-stylesheets,'\s+')[not(.='')]),'&#x0A;- '))
		                                     else ''}"
		            px:progress=".50">
			<p:with-option name="default-stylesheet" select="$all-css-stylesheets"/>
			<p:input port="sass-variables">
				<p:pipe step="main" port="parameters"/>
			</p:input>
		</css:inline>
	</p:group>
	
</p:declare-step>
