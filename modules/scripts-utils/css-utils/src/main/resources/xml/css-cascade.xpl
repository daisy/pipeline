<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                exclude-inline-prefixes="#all"
                type="px:css-cascade"
                name="main">
	
	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p><a href="http://braillespecs.github.io/braille-css/#h2_cascade">Cascade</a> and inline
		CSS and <a href="https://sass-lang.com/documentation">SCSS</a> style sheets in XML.</p>
		<p>Inlining is done with <code>style</code> attributes with the <a
		href="http://braillespecs.github.io/braille-css/#h2_style-attribute">syntax</a> described in
		braille CSS.</p>
	</p:documentation>
	
	<p:input port="source" sequence="false" primary="true">
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
			'default-stylesheet' option, must either exist on disk, or must be provided in memory
			via this port. Style sheets on this port must be wrapped in &lt;c:result
			content-type="text/plain"&gt; elements. Style sheet URIs are resolved by matching
			against the context documents's base URIs.</p>
		</p:documentation>
		<p:empty/>
	</p:input>
	
	<p:input port="parameters" kind="parameter" primary="false">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Parameters that are passed to SCSS style sheets (as <a
			href="https://sass-lang.com/documentation/variables#scope">global variables</a>). They
			are also passed to XSLT transformations that are included from CSS through
			<code>@xslt</code> rules.
			</p>
		</p:documentation>
	</p:input>
	
	<p:output port="result" sequence="false" primary="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>All styles are parsed, validated, normalized, cascaded and finally serialized into
			<code>style</code> attributes.</p>
			<p>Shorthand declarations such as <code>margin: a b c d</code> are decomposed as
			<code>margin-top: a; margin-right: b; margin-bottom: c; margin-left: d</code>.</p>
			<p>'before' and 'after' rules are serialized as <code>&amp;::before { ... }</code> and
			<code>&amp;::after { ... }</code>.</p>
			<p>'page' properties are replaced with the corresponding named 'page' rules (without the
			page type selector). If the root element does not have a 'page' property, the default
			page rule is inserted. 'page' rules are serialized as <code>@page { ... } @page:left {
			... } @page:right { ... }</code>. The declarations that named page rules inherit from
			the default page rule are made explicit. The declarations that 'left' and 'right' page
			rules inherit from their principal page rule are also made explicit.</p>
			<p>'volume' rules are serialized as <code>@volume { ... } @volume:first { ... }
			@volume:last { ... } @volume:nth(...) { ... } @volume:nth-last(...) { ... }</code> and
			inserted for the root element. The declarations that '@volume:first' etc. inherit from
			the principal volume rule are made explicit. ':nth(1)' and ':nth-last(1)' are normalized
			to ':first' and ':last'. A special '@volume:only' rule, which is a combination of the
			'@volume:first' and '@volume:last' rules, is created for the case that there is only a
			single volume.</p>
		</p:documentation>
	</p:output>
	
	<p:option name="default-stylesheet" required="false" select="''">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Space separated list of URIs, absolute or relative to source. Applied prior to all
			other style sheets defined within the source.</p>
		</p:documentation>
	</p:option>
	
	<p:option name="type" required="false" select="'text/css text/x-scss'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The type of associated style sheets to apply. May be a space separated list. Allowed
			values are "text/css" and "text/x-scss". If omitted, all CSS and SCSS style sheets are
			applied.</p>
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
	
	<p:option name="attribute-name" required="false" select="'style'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Name of attribute to use for inlined styles. Default name is 'style'.</p>
		</p:documentation>
	</p:option>
	
	<p:declare-step type="pxi:css-cascade">
		<p:input port="source" primary="true"/>
		<p:input port="context" sequence="true"/>
		<p:input port="parameters" kind="parameter" primary="false"/>
		<p:output port="result"/>
		<p:option name="default-stylesheet"/>
		<p:option name="media"/>
		<p:option name="type"/>
		<p:option name="attribute-name"/>
		<!--
		    Implemented in ../../java/org/daisy/pipeline/css/calabash/impl/CssCascadeStep.java
		-->
	</p:declare-step>
	
	<p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
		<p:documentation>
			px:parse-xml-stylesheet-instructions
		</p:documentation>
	</p:import>
	
	<px:parse-xml-stylesheet-instructions name="xml-stylesheet-instructions" px:progress=".05"/>
	<p:group px:progress=".95">
		<p:variable name="stylesheets-from-xml-stylesheet-instructions" cx:as="xs:string*"
		            select="/d:fileset/d:file
		                       [not(@stylesheet-media) or pf:media-query-matches(@stylesheet-media,$media)]
		                       [(@media-type=('text/css','text/x-scss') and @media-type=tokenize($type,'\s+'))]
		                     /string(@href)">
			<p:pipe step="xml-stylesheet-instructions" port="fileset"/>
		</p:variable>
		<p:sink/>
		<p:identity>
			<p:input port="source">
				<p:pipe step="main" port="source"/>
			</p:input>
		</p:identity>
		<p:choose px:progress="1">
			<p:when test="$default-stylesheet!=''
			              or exists($stylesheets-from-xml-stylesheet-instructions)
			              or //*[local-name()='style']
			                    [not(@media) or pf:media-query-matches(@media,$media)]
			                    [(@type=('text/css','text/x-scss') and @type=tokenize($type,'\s+'))
			                     or ('text/css'=tokenize($type,'\s+') and not(@type))]
			              or //*[local-name()='link' and @rel='stylesheet']
			                    [not(@media) or pf:media-query-matches($media,@media)]
			                    [(@type=('text/css','text/x-scss') and @type=tokenize($type,'\s+'))
			                     or ('text/css'=tokenize($type,'\s+') and not(@type) and matches(@href,'\.css$'))
			                     or ('text/x-scss'=tokenize($type,'\s+') and not(@type) and matches(@href,'\.scss$'))]
			              or //@style">
				<pxi:css-cascade px:progress="1">
					<p:input port="context">
						<p:pipe step="main" port="context"/>
					</p:input>
					<p:input port="parameters">
						<p:pipe step="main" port="parameters"/>
					</p:input>
					<p:with-option name="default-stylesheet"
					               select="string-join(($default-stylesheet[not(.='')],$stylesheets-from-xml-stylesheet-instructions),' ')"/>
					<p:with-option name="media" select="$media"/>
					<p:with-option name="type" select="$type"/>
					<p:with-option name="attribute-name" select="$attribute-name"/>
				</pxi:css-cascade>
			</p:when>
			<p:otherwise>
				<p:identity/>
			</p:otherwise>
		</p:choose>
	</p:group>
	
</p:declare-step>
