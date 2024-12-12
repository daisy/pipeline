<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                exclude-inline-prefixes="#all">

  <p:documentation xmlns="http://www.w3.org/1999/xhtml">
    <p>Transform a Markdown file created by Mistral OCR to a HTML fileset</p>
  </p:documentation>

  <p:option name="source" px:type="anyFileURI">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>The path to the Markdown file.</p>
    </p:documentation>
  </p:option>

  <p:option name="metadata" cx:as="map(xs:string,xs:string)">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>Document metadata</p>
      <p>Supported fields are title, author and language.</p>
    </p:documentation>
  </p:option>

  <p:option name="image-descriptions" cx:as="map(xs:string,xs:string)">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>Mapping from relative image paths to image descriptions.</p>
    </p:documentation>
  </p:option>

  <p:option name="image-text-content" cx:as="map(xs:string,xs:string)">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>Literal text content of images, contained in a map with the relative file paths of images as keys.</p>
    </p:documentation>
  </p:option>

  <p:option name="replace-images" cx:as="xs:string*">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>Replace images</p>
      <p>Relative file paths of images that ought to be replaced by their text content, or marked as decorative
      if they do not have text content. Images with a caption are always retained.</p>
    </p:documentation>
  </p:option>

  <p:option name="image-sizes" cx:as="map(xs:string,xs:integer)">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>Image widths in pixels, in a map with the relative file paths of images as keys.</p>
    </p:documentation>
  </p:option>

  <p:option name="result-dir" px:type="anyDirURI">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>The path to the output directory where the HTML fileset should be stored.</p>
    </p:documentation>
  </p:option>

  <p:output port="result">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>The fileset containing the HTML file(s) and all references resources. All
      files must be stored on disk.</p>
    </p:documentation>
    <p:pipe step="store" port="fileset.out"/>
  </p:output>

  <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
    <p:documentation>
      px:fileset-add-entry
      px:fileset-filter
      px:fileset-load
      px:fileset-update
      px:fileset-store
    </p:documentation>
  </p:import>
  <p:import href="http://www.daisy.org/pipeline/modules/pandoc-adapter/library.xpl">
    <p:documentation>
      px:pandoc-markdown-to-html
    </p:documentation>
  </p:import>

  <px:fileset-add-entry media-type="text/markdown">
    <p:with-option name="href" select="$source"/>
  </px:fileset-add-entry>

  <px:pandoc-markdown-to-html name="pandoc" detect-image-captions="true" px:progress=".7">
    <p:with-option name="result-dir" select="$result-dir"/>
  </px:pandoc-markdown-to-html>

  <px:fileset-filter media-types="application/xhtml+xml" name="filter-html">
    <p:input port="source.in-memory">
      <p:pipe step="pandoc" port="result.in-memory"/>
    </p:input>
  </px:fileset-filter>
  <px:fileset-load>
    <p:input port="in-memory">
      <p:pipe step="pandoc" port="result.in-memory"/>
    </p:input>
  </px:fileset-load>

  <p:for-each px:progress=".3">
    <p:xslt>
      <p:input port="stylesheet">
        <p:document href="post-process.xsl"/>
      </p:input>
      <p:with-param name="metadata" select="$metadata"/>
      <p:with-param name="image-descriptions" select="$image-descriptions"/>
      <p:with-param name="image-text-content" select="$image-text-content"/>
      <p:with-param name="image-sizes" select="$image-sizes"/>
      <p:with-param name="replace-images" select="$replace-images"/>
    </p:xslt>
  </p:for-each>
  <p:identity name="html-with-image-descriptions"/>
  <p:sink/>

  <px:fileset-update name="update-html">
    <p:input port="source.fileset">
      <p:pipe step="pandoc" port="result.fileset"/>
    </p:input>
    <p:input port="source.in-memory">
      <p:pipe step="filter-html" port="not-matched.in-memory"/>
    </p:input>
    <p:input port="update.fileset">
      <p:pipe step="filter-html" port="result"/>
    </p:input>
    <p:input port="update.in-memory">
      <p:pipe step="html-with-image-descriptions" port="result"/>
    </p:input>
  </px:fileset-update>

  <px:fileset-store name="store">
    <p:input port="in-memory.in">
      <p:pipe step="update-html" port="result.in-memory"/>
    </p:input>
  </px:fileset-store>

</p:declare-step>
