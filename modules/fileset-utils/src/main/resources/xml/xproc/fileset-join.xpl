<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:fileset-join">

  <p:documentation xmlns="http://www.w3.org/1999/xhtml">
    <p>Join zero or more <code>d:fileset</code> documents into a single one. The joint
    <code>xml:base</code> is the longest common URI of all the <code>xml:base</code> attributes. If
    an input fileset does not have an <code>xml:base</code> or if it is not a "file:" URI, it does
    not contribute to the new base. If none of the input fileset have an <code>xml:base</code> the
    joint fileset has no <code>xml:base</code> either. The <code>href</code> and
    <code>original-href</code> attributes are normalized. <code>href</code> are relativized against
    the new base, and <code>original-href</code> are made absolute. <code>@xml:base</code>
    attributes are removed from <code>d:file</code>. Files that have the same resolved
    <code>href</code> are merged, i.e. their attributes are combined. In case of conflicting
    attributes the last occurence wins.</p> Files are listed in the order in which their
    <code>href</code> occurs first in the source documents.
  </p:documentation>

  <p:input port="source" sequence="true"/>
  <p:output port="result" primary="true"/>

  <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
    <p:documentation>
      px:add-xml-base
    </p:documentation>
  </p:import>

  <!--
      this px:add-xml-base is to work around a bug that causes the base URI of documents with a
      relative root xml:base to be messed up
  -->
  <p:for-each>
    <px:add-xml-base root="false"/>
  </p:for-each>

  <p:xslt template-name="main">
    <p:input port="stylesheet">
      <p:document href="../xslt/fileset-join.xsl"/>
    </p:input>
    <p:input port="parameters">
      <p:empty/>
    </p:input>
  </p:xslt>

</p:declare-step>
