<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step version="1.0" type="px:apply-stylesheets"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                exclude-inline-prefixes="px pxi"
                name="main">
	
	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Apply CSS, <a href="https://sass-lang.com/documentation">SCSS</a> and/or XSLT
		stylesheets</p>.
	</p:documentation>
	
	<p:input port="source" primary="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Style sheets can be associated with the source in several ways: linked (using an
			<code>xml-stylesheet</code> processing instruction or a <code>link</code> element),
			embedded (using a <code>style</code> element) and/or inlined (using <code>style</code>
			attributes).</p>
		</p:documentation>
	</p:input>
	
	<p:input port="context" sequence="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Style sheets that are linked to from the source document, or included via the
			'stylesheets' option, must either exist on disk, or must be provided in memory via this
			port. Style sheets on this port must be wrapped in <code>&lt;c:result
			content-type="text/plain"&gt;</code> elements. Style sheet URIs are resolved by matching
			against the context documents's base URIs.</p>
		</p:documentation>
		<p:empty/>
	</p:input>
	
	<p:output port="result">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Style sheets are applied to the document in the following way: XSLT style sheets are
			applied before CSS/SCSS style sheets. XSLT style sheets are applied one by one, first
			the ones from the 'stylesheets' option, then the ones associated with the source
			document, in the order in which they are specified. CSS/SCSS style sheets are applied by
			"inlining" them, i.e. performing the cascade and capturing the styles of individual
			elements in <code>style</code> attributes, using the <a
			href="http://braillespecs.github.io/braille-css/#h2_style-attribute">syntax</a>
			described in braille CSS. All CSS/SCSS style sheets are applied at once, but the order
			in which they are specified (first the ones from the 'stylesheets' option, then the ones
			associated with the source document) has an influence on the cascading order.</p>
		</p:documentation>
	</p:output>
	
	<p:option name="stylesheets" required="false" select="''">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>A space separated list of URIs, absolute or relative to source. XSLT style sheets
			must be specified before CSS/SCSS style sheets.</p>
			<!--
			    File extensions assumed to be one of:
			    - .css
			    - .scss
			    - .xsl
			    - .xslt
			-->
		</p:documentation>
	</p:option>
	
	<p:option name="type" required="false" select="'text/css text/x-scss text/xsl'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The type of associated style sheets to apply. May be a space separated list. Allowed
			values are "text/css", "text/x-scss", "text/xsl" and "application/xslt+xml". If omitted,
			all CSS, SCSS and XSLT style sheets are applied.</p>
		</p:documentation>
	</p:option>
	
	<p:option name="media" required="false" select="'embossed'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The target medium type as a <a href="https://www.w3.org/TR/mediaqueries-4/">media
			query</a>. All rules that are contained in a style sheet that matches the specified
			medium are included. Supported media types are "embossed" and "print". When the target
			medium is embossed, CSS is interpreted according to the rules of <a
			href="http://braillespecs.github.io/braille-css">braille CSS</a>. Supported media
			features are '<a href="https://www.w3.org/TR/mediaqueries-4/#width">width</a>' and '<a
			href="https://www.w3.org/TR/mediaqueries-4/#height">height</a>'.</p>
		</p:documentation>
	</p:option>
	
	<p:input port="parameters" kind="parameter" primary="false">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Parameters that are passed to XSLT transformations and SCSS style sheets (as <a
			href="https://sass-lang.com/documentation/variables#scope">global variables</a>).</p>
			<!-- Note that parameters are always strings. This is due to a limitation in
			     XMLCalabash. Because of this, parameters in XSLT style sheets need to be declared
			     as xs:string. In SCSS style sheets the parameters are interpreted as a number, a
			     color or an ident if possible. If a parameter is passed from a SCSS style sheet to
			     an XSLT using an @xslt rule, the idents true and false become booleans. -->
		</p:documentation>
	</p:input>
	
	<p:declare-step type="pxi:recursive-xslt" name="recursive-xslt">
		<p:input port="source"/>
		<p:output port="result"/>
		<p:option name="stylesheets" cx:as="xs:string*" required="true"/>
		<p:input kind="parameter" port="parameters" primary="true"/>
		<p:choose px:progress="1">
			<p:when test="exists($stylesheets)">
				<p:variable name="stylesheet" select="$stylesheets[1]"/>
				<p:load name="stylesheet">
					<p:with-option name="href" select="resolve-uri($stylesheet,base-uri(/*))"/>
				</p:load>
				<p:xslt px:message="Applying XSLT: {$stylesheet}" px:progress="1/{count($stylesheets)}">
					<p:input port="source">
						<p:pipe step="recursive-xslt" port="source"/>
					</p:input>
					<p:input port="stylesheet">
						<p:pipe step="stylesheet" port="result"/>
					</p:input>
				</p:xslt>
				<pxi:recursive-xslt px:progress="{count($stylesheets) - 1}/{count($stylesheets)}">
					<p:with-option name="stylesheets" select="$stylesheets[position()&gt;1]"/>
				</pxi:recursive-xslt>
			</p:when>
			<p:otherwise>
				<p:identity/>
			</p:otherwise>
		</p:choose>
	</p:declare-step>
	
	<p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
		<p:documentation>
			px:error
			px:parse-xml-stylesheet-instructions
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/css-utils/library.xpl">
		<p:documentation>
			px:css-cascade
		</p:documentation>
	</p:import>
	
	<p:variable name="xslt-user-stylesheets" cx:as="xs:string*" select="tokenize($stylesheets,'\s+')[matches(.,'\.xslt?$')]"/>
	<p:variable name="css-user-stylesheets" cx:as="xs:string*" select="tokenize($stylesheets,'\s+')[matches(.,'\.s?css$')]"/>
	
	<p:choose>
		<p:when test="not(string-join(($xslt-user-stylesheets,$css-user-stylesheets),' ')=normalize-space($stylesheets))">
			<px:error code="BRL01" message="Invalid `stylesheets` option specified: $1">
				<p:with-option name="param1" select="$stylesheets"/>
			</px:error>
		</p:when>
		<p:when test="exists($xslt-user-stylesheets)">
			<p:identity px:message-severity="WARN"
			            px:message="Applying XSLT user style sheets is deprecated and might not be supported anymore in a future version of Pipeline. Please apply any XSLT style sheets during pre-processing of the document."/>
		</p:when>
		<p:otherwise>
			<p:identity/>
		</p:otherwise>
	</p:choose>
	
	<px:parse-xml-stylesheet-instructions name="xml-stylesheet-instructions" px:progress=".05"/>
	<p:sink/>
	<p:identity>
		<p:input port="source">
			<p:pipe step="main" port="source"/>
		</p:input>
	</p:identity>
	<p:group px:progress=".95">
		<p:variable name="xslt-stylesheets-from-xml-stylesheet-instructions" cx:as="xs:string*"
		            select="/d:fileset/d:file
		                      [('text/xsl','application/xslt+xml')=tokenize($type,'\s+')
	                           and (
		                         @media-type=('text/xsl','application/xslt+xml'))]
		                    /string(@href)">
			<p:pipe step="xml-stylesheet-instructions" port="fileset"/>
		</p:variable>
		<p:choose>
			<p:when test="exists($xslt-stylesheets-from-xml-stylesheet-instructions)">
				<p:identity px:message-severity="WARN"
				            px:message="XSLT style sheets attached through 'xml-stylesheet' processing instructions will be ignored in a future version of Pipeline. Please apply any XSLT style sheets during pre-processing of the document."/>
			</p:when>
			<p:otherwise>
				<p:identity/>
			</p:otherwise>
		</p:choose>
		<pxi:recursive-xslt px:progress=".50">
			<p:with-option name="stylesheets" select="($xslt-user-stylesheets,$xslt-stylesheets-from-xml-stylesheet-instructions)"/>
			<p:input port="parameters">
				<p:pipe step="main" port="parameters"/>
			</p:input>
		</pxi:recursive-xslt>
		<p:choose px:progress=".50">
			<p:when test="tokenize($type,'\s')=('text/css','text/x-scss')">
				<p:variable name="message" cx:as="xs:string"
				            select="concat('Applying CSS',
				                           if (exists($css-user-stylesheets))
				                           then concat(':',if (count($css-user-stylesheets)=1)
				                                           then concat(' ',$css-user-stylesheets[1])
				                                           else string-join(('',$css-user-stylesheets),'&#x0A;- '))
				                           else '')"/>
				<px:css-cascade px:message="{$message}">
					<p:with-option name="user-stylesheet" select="string-join($css-user-stylesheets,' ')"/>
					<p:with-option name="type" select="string-join(tokenize($type,'\s')[.=('text/css','text/x-scss')],' ')"/>
					<p:with-option name="media" select="$media"/>
					<p:input port="source.in-memory">
						<p:pipe step="main" port="context"/>
					</p:input>
					<p:input port="parameters">
						<p:pipe step="main" port="parameters"/>
					</p:input>
				</px:css-cascade>
			</p:when>
			<p:otherwise>
				<p:identity/>
			</p:otherwise>
		</p:choose>
	</p:group>
	
</p:declare-step>
