<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                type="px:html-outline">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Apply the <a
		href="https://html.spec.whatwg.org/multipage/sections.html#headings-and-sections">HTML5
		outline algorithm</a>.</p>
		<p>Returns the outline of a HTML document and optionally transforms the document in a
		certain way in relation to the outline.</p>
	</p:documentation>
	
	<p:input port="source">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">HTML document</h2>
			<p px:role="desc">The HTML document from which the outline must be extracted.</p>
		</p:documentation>
	</p:input>

	<p:output port="result" primary="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">The outline</h2>
			<p px:role="desc">The outline of the HTML document as a <code>ol</code> element. Can be
			used directly as a table of contents.</p>
		</p:documentation>
		<p:pipe step="outline" port="result"/>
	</p:output>
	<p:output port="outline">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">The raw outline</h2>
			<p px:role="desc">The unformatted outline of the HTML document as a
			<code>d:outline</code> document.</p>
		</p:documentation>
		<p:pipe step="raw-outline" port="result"/>
	</p:output>
	<p:output port="content-doc">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">The modified HTML document.</h2>
			<p px:role="desc">Depending on the values of the "fix-heading-ranks", "fix-sectioning"
			and "fix-untitled-sections" options, heading elements may be inserted or renamed and
			section elements may be inserted, but the outline is guaranteed to be unchanged.</p>
			<p px:role="desc">All <code>body</code>, <code>article</code>, <code>aside</code>,
			<code>nav</code>, <code>section</code>, <code>h1</code>, <code>h2</code>,
			<code>h3</code>, <code>h4</code>, <code>h5</code>, <code>h6</code> and
			<code>hgroup</code> elements get an <code>id</code> attribute.</p>
		</p:documentation>
		<p:pipe step="normalize" port="result"/>
	</p:output>

	<p:option name="output-base-uri" required="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The base URI of the resulting outline.</p>
		</p:documentation>
	</p:option>
	<p:option name="heading-links-only" select="'false'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Whether the <code>a</code> elements in the "result" output should only reference
			heading elements (<code>h1</code>, <code>h2</code>, <code>h3</code>, <code>h4</code>,
			<code>h5</code>, <code>h6</code> or <code>hgroup</code>) or whether they may also
			reference sectioning elements (<code>body</code>, <code>article</code>,
			<code>aside</code>, <code>nav</code> or <code>section</code>). If this option is set to
			"true" and the "fix-untitled-sections-in-outline" option is set to "imply-heading", the
			outline will contain generated section titles but they will be <code>span</code>s, not
			<code>a</code>s.</p>
		</p:documentation>
	</p:option>
	<p:option name="fix-heading-ranks" select="'keep'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Whether to change the <a
			href="https://html.spec.whatwg.org/multipage/sections.html#rank">rank</a> of <a
			href="https://html.spec.whatwg.org/multipage/dom.html#heading-content-2">heading content
			elements</a> in the HTML document.</p>
			<dl>
				<dt>outline-depth</dt>
				<dd>The rank must match the <a
				href="https://html.spec.whatwg.org/multipage/sections.html#outline-depth">outline
				depth</a> of the heading (or 6 if the depth is higher).</dd>
				<dt>keep</dt>
				<dd>Don't rename heading elements. Default value.</dd>
			</dl>
		</p:documentation>
	</p:option>
	<p:option name="fix-sectioning" select="'keep'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Whether to insert <a
			href="https://html.spec.whatwg.org/multipage/dom.html#sectioning-content-2">sectioning
			content elements</a>.</p>
			<dl>
				<dt>outline-depth</dt>
				<dd>For all nodes, the number of ancestor sectioning content and <a
				href="https://html.spec.whatwg.org/multipage/sections.html#sectioning-root">sectioning
				root</a> elements must match the <a
				href="https://html.spec.whatwg.org/multipage/sections.html#outline-depth">outline
				depth</a>. All nodes that belong to a certain section have a single common ancestor
				element that wraps that section, or the outline containing that section. This means
				that elements in the source that span multiple sections may have to split up in the
				result.</dd>
				<dt>no-implied</dt>
				<dd>Like outline-depth, but in addition create new sections as needed to get rid of
				implied sections. Note that this may result in multiple <code>body</code> elements,
				so a cleanup step may be required.</dd>
				<dt>keep</dt>
				<dd>Do nothing. Default value.</dd>
			</dl>
		</p:documentation>
	</p:option>
	<p:option name="fix-untitled-sections" select="'keep'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Whether to generate a <a
			href="https://html.spec.whatwg.org/multipage/dom.html#heading-content-2">heading content
			element</a> for sections that don't have one.</p>
			<dl>
				<dt>imply-heading</dt>
				<dd>Insert heading elements. The rank is determined by the outline depth.</dd>
				<dt>keep</dt>
				<dd>Don't insert heading elements. Default value.</dd>
			</dl>
		</p:documentation>
	</p:option>
	<p:option name="fix-untitled-sections-in-outline" select="'imply-heading'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>How to handle sections in the outline without an associated <a
			href="https://html.spec.whatwg.org/multipage/dom.html#heading-content-2">heading content
			element</a>. Setting this option has no effect if "fix-untitled-sections" is set to
			"imply-heading".</p>
			<dl>
				<dt>imply-heading</dt>
				<dd>Generate a heading text for a such sections. This is the default value.</dd>
				<dt>unwrap</dt>
				<dd>Replace the sections with their subsections.</dd>
			</dl>
		</p:documentation>
	</p:option>

	<p:import href="html-add-ids.xpl">
		<p:documentation>
			px:html-add-ids
		</p:documentation>
	</p:import>

	<p:documentation>Add ID attributes</p:documentation>
	<px:html-add-ids name="html-with-ids"/>

	<p:documentation>Create the outline</p:documentation>
	<p:xslt name="outline">
		<p:input port="stylesheet">
			<p:document href="../xslt/html5-outline.xsl"/>
		</p:input>
		<p:with-param name="heading-links-only" select="$heading-links-only"/>
		<p:with-param name="fix-untitled-sections-in-outline"
		              select="($fix-untitled-sections[.='imply-heading'],$fix-untitled-sections-in-outline)[1]"/>
		<p:with-param name="output-base-uri" select="$output-base-uri"/>
		<p:with-option name="output-base-uri" select="$output-base-uri"/>
	</p:xslt>
	<p:sink/>

	<p:choose>
		<p:when test="$fix-sectioning=('outline-depth','no-implied')
		              or $fix-heading-ranks='outline-depth'
		              or $fix-untitled-sections='imply-heading'">
			<p:xslt>
				<p:input port="source">
					<p:pipe step="html-with-ids" port="result"/>
					<p:pipe step="outline" port="secondary"/>
				</p:input>
				<p:input port="stylesheet">
					<p:document href="../xslt/html5-normalize-sections-headings.xsl"/>
				</p:input>
				<p:with-param name="fix-heading-ranks" select="$fix-heading-ranks"/>
				<p:with-param name="fix-sectioning" select="$fix-sectioning"/>
				<p:with-param name="fix-untitled-sections" select="$fix-untitled-sections"/>
			</p:xslt>
			<p:xslt>
				<!-- Remove duplicate ids created by html5-normalize-sections-headings.xsl -->
				<p:input port="stylesheet">
					<p:document href="../xslt/remove-duplicate-ids.xsl"/>
				</p:input>
				<p:input port="parameters">
					<p:empty/>
				</p:input>
			</p:xslt>
		</p:when>
		<p:otherwise>
			<p:identity>
				<p:input port="source">
					<p:pipe step="html-with-ids" port="result"/>
				</p:input>
			</p:identity>
		</p:otherwise>
	</p:choose>
	<p:identity name="normalize"/>
	<p:sink/>

	<p:unwrap match="/*//d:outline">
		<p:input port="source">
			<p:pipe step="outline" port="secondary"/>
		</p:input>
	</p:unwrap>
	<p:delete match="/d:outline/@owner" name="raw-outline"/>
	<p:sink/>

</p:declare-step>
