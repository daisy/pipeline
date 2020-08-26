<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns:epub="http://www.idpf.org/2007/ops"
                type="px:html-add-ids"
                version="1.0">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Add missing IDs to HTML documents and fix duplicate IDs.</p>
	</p:documentation>

	<p:input port="source" sequence="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The input HTML documents</p>
		</p:documentation>
	</p:input>
	<p:option name="match" required="false" select="'html:body|
	                                                 html:article|
	                                                 html:aside|
	                                                 html:nav|
	                                                 html:section|
	                                                 html:h1|html:h2|html:h3|html:h4|html:h5|html:h6|
	                                                 html:hgroup|
	                                                 *[tokenize(@epub:type,''\s+'')=''pagebreak'']'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Elements that need an <code>id</code> attribute.</p>
			<p>Should be a XSLTMatchPattern that matches only elements.</p>
			<p>By default matches all <code>body</code>, <code>article</code>, <code>aside</code>,
			<code>nav</code>, <code>section</code>, <code>h1</code>, <code>h2</code>,
			<code>h3</code>, <code>h4</code>, <code>h5</code>, <code>h6</code> and
			<code>hgroup</code>, and <code>epub:type='pagebreak'</code> elements.</p>
		</p:documentation>
	</p:option>
	<p:output port="result" sequence="true" primary="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The processed HTML documents</p>
			<p>All elements matched by the <code>match</code> expression have a <code>id</code>
			attribute.</p>
			<p>All <code>id</code> attributes are unique within the whole sequence of HTML
			documents.</p>
		</p:documentation>
		<p:pipe step="result" port="result"/>
	</p:output>
	<p:output port="mapping">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p><code>d:fileset</code> document that represents the renaming of <code>id</code>
			attributes.</p>
		</p:documentation>
		<p:pipe step="result" port="mapping"/>
	</p:output>

	<p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
		<p:documentation>
			px:add-ids
		</p:documentation>
	</p:import>

	<px:add-ids name="result">
		<p:with-option name="match" select="$match">
			<p:empty/>
		</p:with-option>
	</px:add-ids>

</p:declare-step>
