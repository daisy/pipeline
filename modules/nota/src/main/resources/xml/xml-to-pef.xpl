<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="nota:xml-to-pef" version="1.0"
                xmlns:nota="http://www.nota.dk"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc">
	
	<p:option name="stylesheet" select="'http://www.nota.dk/pipeline/modules/braille/default.scss'"/>
	
	<p:option name="contraction-grade" required="false" select="'0'">
		<p:pipeinfo>
			<px:data-type>
				<choice>
					<documentation xmlns="http://relaxng.org/ns/compatibility/annotations/1.0" xml:lang="da">
						<value>uforkortet</value>
						<value>lille forkortelse</value>
						<value>stor forkortelse</value>
					</documentation>
					<value>0</value>
					<value>1</value>
					<value>2</value>
				</choice>
			</px:data-type>
		</p:pipeinfo>
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Translation/formatting of text: Contraction grade</h2>
			<p px:role="desc">`uforkortet` (uncontracted), `lille forkortels` (partly contracted) or `stor forkortelse` (fully contracted)</p>
		</p:documentation>
	</p:option>
	
	<p:option name="ascii-file-format" select="'(table:&quot;com_braillo.BrailloTableProvider.TableType.BRAILLO_6DOT_001_00&quot;)'"/>
	<p:option name="ascii-table" select="'(id:&quot;com_braillo.BrailloTableProvider.TableType.BRAILLO_6DOT_001_00&quot;)'"/>
	<p:option name="include-preview" select="'false'"/>
	<p:option name="include-brf" select="'false'"/>
	<p:option name="include-obfl" required="false" px:type="boolean" select="'false'"/>
	
	<p:option name="page-width" select="32"/>
	<p:option name="page-height" select="27"/>
	<p:option name="left-margin"/>
	<p:option name="duplex"/>
	<p:option name="levels-in-footer"/>
	<p:option name="main-document-language"/>
	<p:option name="hyphenation"/>
	<p:option name="line-spacing"/>
	<p:option name="tab-width"/>
	<p:option name="capital-letters" select="'false'"/>
	<p:option name="accented-letters"/>
	<p:option name="polite-forms"/>
	<p:option name="downshift-ordinal-numbers"/>
	<p:option name="include-captions"/>
	<p:option name="include-images"/>
	<p:option name="include-image-groups"/>
	<p:option name="include-line-groups"/>
	<p:option name="text-level-formatting"/>
	<p:option name="include-note-references"/>
	<p:option name="include-production-notes"/>
	<p:option name="show-braille-page-numbers"/>
	<p:option name="show-print-page-numbers"/>
	<p:option name="force-braille-page-break"/>
	<p:option name="toc-depth"/>
	<p:option name="footnotes-placement"/>
	<p:option name="colophon-metadata-placement"/>
	<p:option name="rear-cover-placement"/>
	<p:option name="number-of-sheets"/>
	<p:option name="maximum-number-of-sheets"/>
	<p:option name="minimum-number-of-sheets"/>
	
	<p:option name="pef-output-dir"/>
	<p:option name="brf-output-dir"/>
	<p:option name="preview-output-dir"/>
	<p:option name="temp-dir"/>
	
</p:declare-step>
