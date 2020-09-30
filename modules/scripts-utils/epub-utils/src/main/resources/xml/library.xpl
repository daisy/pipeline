<?xml version="1.0" encoding="UTF-8"?>
<p:library xmlns:p="http://www.w3.org/ns/xproc" version="1.0">
	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Steps related to the <a
		href="http://www.idpf.org/epub/301/spec/epub-publications.html">EPUB Package
		Document</a> (the .opf file)</p>
	</p:documentation>
	<p:import href="pub/create-package-doc.xpl"/>
	<p:import href="pub/upgrade-package-doc.xpl"/>
	<p:import href="pub/add-mediaoverlays.xpl"/>
	<p:import href="pub/opf-spine-to-fileset.xpl"/>
	<p:import href="pub/compare-package-doc.xpl"/>
	<p:import href="pub/merge-prefix.xpl"/>
	<p:import href="pub/add-prefix.xpl"/>
	<p:import href="pub/ensure-core-media.xpl"/>
	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Steps related to <a
		href="http://www.idpf.org/epub/30/spec/epub30-contentdocs.html#sec-xhtml-nav">EPUB
		Navigation Documents</a></p>
	</p:documentation>
	<p:import href="nav/epub3-nav-create-navigation-doc.xpl"/>
	<p:import href="nav/epub3-nav-create-toc.xpl"/>
	<p:import href="nav/epub3-nav-create-page-list.xpl"/>
	<p:import href="nav/landmarks-to-guide.xpl"/>
	<p:import href="nav/guide-to-landmarks.xpl"/>
	<p:import href="nav/epub3-nav-to-ncx.xpl"/>
	<p:import href="nav/epub3-nav-from-ncx.xpl"/>
	<p:import href="nav/label-pagebreaks-from-nav.xpl"/>
	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Steps related to the <a href="https://www.w3.org/publishing/epub3/epub-ocf.html">EPUB
		Open Container Format (OCF)</a> (the ZIP, the mimetype file, the META-INF/container.xml
		file, etc.)</p>
	</p:documentation>
	<p:import href="ocf/ocf-finalize.xpl"/>
	<p:import href="ocf/store.xpl"/>
	<p:import href="ocf/load.xpl"/>
	<p:import href="ocf/epub3-safe-uris.xpl"/>
	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Steps related to EPUB 3 Media Overlays.</p>
	</p:documentation>
	<p:import href="mo/create-mediaoverlays.xpl"/>
	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Validation with EpubCheck and Ace.</p>
	</p:documentation>
	<p:import href="validate/epub-validate.xpl"/>
	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Other utility steps</p>
	</p:documentation>
	<p:import href="epub-rename-files.xpl"/>
	<p:import href="epub-update-links.xpl"/>
</p:library>
