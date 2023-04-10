<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                type="px:html-outline" name="main">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Apply the <a
		href="https://html.spec.whatwg.org/multipage/sections.html#headings-and-sections">HTML5
		outline algorithm</a>.</p>
		<p>Returns the outline of a HTML document and optionally transforms the document in a
		certain way in relation to the outline.</p>
		<p>Note that the outline algorithm was never implemented in web browsers nor assistive
		technology. Therefore it should not be used by authors to convey document structure. The
		algorithm is however useful in the context of transformations, for example to generate a
		table of contents, or to rename heading elements according to their outline depth.</p>
	</p:documentation>
	
	<p:input port="source" primary="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2>HTML document</h2>
			<p>The HTML document from which the outline must be extracted.</p>
		</p:documentation>
	</p:input>
	<p:input port="input-toc" sequence="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2>Table of contents</h2>
			<p>A table of contents referencing some sections or headings in the source
			document. Only required if the "fix-heading-ranks" option is set to "toc-depth" (and not
			more than one document is allowed). The table of contents should be formatted as the
			<code>ol</code> element from a <a
			href="https://www.w3.org/publishing/epub3/epub-packages.html#sec-package-nav-def-model">EPUB
			<code>&lt;nav epub:type="toc"&gt;</code> element</a>.</p>
		</p:documentation>
		<p:empty/>
	</p:input>

	<p:output port="result" primary="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2>The modified HTML document.</h2>
			<p>Depending on the values of the "fix-heading-ranks", "fix-sectioning" and
			"fix-untitled-sections" options, heading elements may be inserted or renamed and section
			elements may be inserted, but the outline is guaranteed to be unchanged.</p>
			<p>All <code>body</code>, <code>article</code>, <code>aside</code>,
			<code>nav</code>, <code>section</code>, <code>h1</code>, <code>h2</code>,
			<code>h3</code>, <code>h4</code>, <code>h5</code>, <code>h6</code> and
			<code>hgroup</code> elements get an <code>id</code> attribute.</p>
		</p:documentation>
		<p:pipe step="normalize" port="result"/>
	</p:output>
	<p:output port="toc">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2>The outline in HTML format</h2>
			<p>The outline of the HTML document as a <code>ol</code> element. Can be
			used directly to include in a <a
			href="https://www.w3.org/publishing/epub3/epub-packages.html#sec-package-nav-def-model">EPUB
			<code>&lt;nav epub:type="toc"&gt;</code> element</a>.</p>
		</p:documentation>
		<p:pipe step="outline" port="result"/>
	</p:output>
	<p:output port="outline">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2>The raw outline</h2>
			<p>The unformatted outline of the HTML document as a <code>d:outline</code>
			document.</p>
		</p:documentation>
		<p:pipe step="raw-outline" port="result"/>
	</p:output>

	<p:option name="toc-output-base-uri" required="false" select="''">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The base URI of the resulting TOC document.</p>
			<p>May be omitted if the "toc" output is not used.</p>
		</p:documentation>
	</p:option>
	<p:option name="heading-links-only" select="'false'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Whether the <code>a</code> elements in the "result" output should only reference
			heading elements (<code>h1</code>, <code>h2</code>, <code>h3</code>, <code>h4</code>,
			<code>h5</code>, <code>h6</code> or <code>hgroup</code>) or whether they may also
			reference sectioning elements (<code>body</code>, <code>article</code>,
			<code>aside</code>, <code>nav</code> or <code>section</code>). If this option is set to
			"true", "fix-untitled-sections" is "keep" and "fix-untitled-sections-in-outline" is
			"imply-heading", the outline will contain generated section titles but they will be
			<code>span</code>s, not <code>a</code>s.</p>
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
				<dt>toc-depth</dt>
				<dd>The rank of a heading is set to the number of ancestor <code>ol</code> elements
				of the <code>a</code> element in the "input-toc" document that references the
				heading or the sectioning content element associated with the heading. If the
				heading is not referenced in the table of contents, the rank is 1.</dd>
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
				implied sections completely. Note that this may result in multiple <code>body</code>
				elements or may break the structure or semantics of the document some other
				way. This feature is intended for creating an intermediary HTML document that will
				be converted further into a format that does not allow implied sections (like
				DTBook).</dd>
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
				<dd>Insert heading elements. The rank is determined by the "fix-heading-ranks"
				option, whereby "keep" is treated as "toc-depth".</dd>
				<dt>imply-heading-from-aria-label</dt>
				<dd>Same as 'imply-heading' but only use <a
				href="https://www.w3.org/TR/wai-aria/#aria-label"><code>aria-label</code></a>
				attributes on sectioning elements to derive the headings from, don't generate
				"dummy" headings. The <code>aria-label</code> is replaced with a <a
				href="https://www.w3.org/TR/wai-aria/#aria-labelledby"><code>aria-labelledby</code></a>.</dd>
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

	<p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
		<p:documentation>
			px:assert
		</p:documentation>
	</p:import>
	<p:import href="html-add-ids.xpl">
		<p:documentation>
			px:html-add-ids
		</p:documentation>
	</p:import>

	<p:choose>
		<p:when test="$fix-heading-ranks='toc-depth'">
			<px:assert test-count-min="1" test-count-max="1" message="Exactly one document expected on 'toc' port"/>
		</p:when>
		<p:otherwise>
			<p:identity/>
		</p:otherwise>
	</p:choose>

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
		<p:with-param name="output-base-uri"
		              select="if ($toc-output-base-uri!='') then $toc-output-base-uri else base-uri(/*)"/>
		<p:with-option name="output-base-uri"
		               select="if ($toc-output-base-uri!='') then $toc-output-base-uri else base-uri(/*)"/>
	</p:xslt>
	<p:sink/>

	<p:choose>
		<p:when test="$fix-sectioning=('outline-depth','no-implied')
		              or $fix-heading-ranks=('outline-depth','toc-depth')
		              or $fix-untitled-sections=('imply-heading','imply-heading-from-aria-label')">
			<p:xslt>
				<p:input port="source">
					<p:pipe step="html-with-ids" port="result"/>
					<p:pipe step="outline" port="secondary"/>
					<p:pipe step="main" port="input-toc"/>
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

	<!--
	    Remove the non-root d:outline to make sure the result is the same regardless of the
	    "fix-sectioning" setting
	-->
	<p:unwrap match="/*//d:outline">
		<p:input port="source">
			<p:pipe step="outline" port="secondary"/>
		</p:input>
	</p:unwrap>
	<p:delete match="/d:outline/@owner" name="raw-outline"/>
	<p:sink/>

</p:declare-step>
