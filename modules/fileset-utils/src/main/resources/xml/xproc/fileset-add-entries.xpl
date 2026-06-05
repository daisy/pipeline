<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                exclude-inline-prefixes="px"
                type="px:fileset-add-entries" name="main">

  <p:documentation xmlns="http://www.w3.org/1999/xhtml">
    <p>Add new entries to a fileset.</p>
  </p:documentation>

  <p:input port="source.fileset" primary="true">
    <p:inline exclude-inline-prefixes="#all"><d:fileset/></p:inline>
  </p:input>
  <p:input port="source.in-memory" sequence="true">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>The input fileset</p>
    </p:documentation>
    <p:empty/>
  </p:input>

  <p:output port="result.fileset" primary="true"/>
  <p:output port="result.in-memory" sequence="true">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>The fileset with the new entries added</p>
      <p>The result.in-memory port contains all the documents from the source.in-memory port, and if
      the new entries were provided via de "entries" port, those documents are appended (or prepended,
      depending on the "first" option).</p>
      <p>If the input fileset already contained a file with the same URI as one of the new entries,
      the new entry is not added, unless when the 'replace' option is set, in which case the old
      entry is removed and the new one is added to the beginning or the end. (Note however that the
      document sequence is not changed, only the manifest.) If the 'replace-attributes' option is
      set, attributes of the existing entry may be added or replaced.</p>
    </p:documentation>
    <p:pipe step="add-entries" port="result.in-memory"/>
  </p:output>

  <p:input port="entries" sequence="true">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>The documents to add to the fileset</p>
      <p>Must be empty if the href option is specified.</p>
    </p:documentation>
    <p:empty/>
  </p:input>

  <p:option name="href" cx:as="xs:string*" select="()">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>The URIs of the new entries</p>
      <p>If the entries are provided via the entry port, the href option must not be specified. In
      this case the URIs of the entries are the base URIs of the documents.</p>
    </p:documentation>
  </p:option>

  <p:option name="media-type" select="''"/>
  <p:option name="original-href" select="''"><!-- if relative; will be resolved relative to the file --></p:option>
  <p:option name="first" cx:as="xs:boolean" select="false()"/>
  <p:option name="replace" cx:as="xs:boolean" select="false()"/>
  <p:option name="replace-attributes" cx:as="xs:boolean" select="false()"/>

  <p:input port="file-attributes" kind="parameter" primary="false">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>Additional attributes to add to the new d:file entries, or to existing entries if the
      fileset already contained files with the same URI as one of the provided entries and if
      <code>replace-attributes</code> is set to <code>true</code>.</p>
      <p>Attributes named <code>href</code>, <code>original-href</code> or <code>media-type</code>
      are not allowed.</p>
    </p:documentation>
    <p:inline>
      <c:param-set/>
    </p:inline>
  </p:input>

  <p:option name="assert-single-entry" cx:as="xs:boolean" select="false()">
      <!-- used by px:fileset-add-entry -->
  </p:option>

  <p:declare-step type="pxi:fileset-add-entries">
    <p:input port="source.fileset" primary="true"/>
    <p:input port="source.in-memory" sequence="true"/>
    <p:output port="result.fileset" primary="true"/>
    <p:output port="result.in-memory" sequence="true"/>
    <p:input port="entries" sequence="true"/>
    <p:option name="href"/>
    <p:option name="media-type"/>
    <p:option name="original-href"/>
    <p:option name="first"/>
    <p:option name="replace"/>
    <p:option name="replace-attributes"/>
    <p:option name="assert-single-entry"/>
    <p:input port="file-attributes" kind="parameter" primary="false"/>
    <!-- Implemented in ../../../java/org/daisy/pipeline/fileset/calabash/impl/AddEntriesStep.java -->
  </p:declare-step>

  <pxi:fileset-add-entries name="add-entries">
    <p:input port="source.in-memory">
      <p:pipe step="main" port="source.in-memory"/>
    </p:input>
    <p:input port="entries">
      <p:pipe step="main" port="entries"/>
    </p:input>
    <p:input port="file-attributes">
      <p:pipe step="main" port="file-attributes"/>
    </p:input>
    <p:with-option name="href" select="$href"/>
    <p:with-option name="media-type" select="$media-type"/>
    <p:with-option name="original-href" select="$original-href"/>
    <p:with-option name="first" select="$first"/>
    <p:with-option name="replace" select="$replace"/>
    <p:with-option name="replace-attributes" select="$replace-attributes"/>
    <p:with-option name="assert-single-entry" select="$assert-single-entry"/>
  </pxi:fileset-add-entries>

</p:declare-step>
