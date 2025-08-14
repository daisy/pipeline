<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                type="pxi:dtbook-narrator">

    <p:input port="source" px:media-type="application/x-dtbook+xml" sequence="false">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>A single DTBook document</p>
        </p:documentation>
    </p:input>
    <p:output port="result" px:media-type="application/x-dtbook+xml" sequence="false">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The result DTBook document</p>
        </p:documentation>
    </p:output>
    <p:option name="publisher" select="''" >
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2>Publisher</h2>
            <p>Publisher to be added as dc:Publisher in the dtbook</p>
        </p:documentation>
    </p:option>

    <!--
    * Adds dc:Language, dc:Date and dc:Publisher to dtbook, if not present in input,
      or given but with null/whitespace only content values.
    * * dc:Language is taken from xml:lang if set, else inparam
    * * dc:Date is taken as inparam
    * * dc:Publisher is taken as inparam
    * Removes dc:description and dc:subject if not valued
    * Removes dc:Format (will be added by the fileset generator)
    -->
    <p:xslt>
        <p:input port="stylesheet"><p:document href="xsl/narrator-metadata.xsl"/></p:input>
        <p:with-param name="publisherValue" select="$publisher"/>
    </p:xslt>

    <!--
    Prepare a dtbook to the Narrator schematron rules:
    - Rule 14: Don't allow <h x+1> in <level x+1> unless <h x> in <level x> is present
    This fix assumes headings are not empty (e.g. empty headings were
    removed by a previous fix)
    -->
    <p:xslt>
        <p:input port="stylesheet"><p:document href="xsl/narrator-headings-r14.xsl"/></p:input>
        <p:input port="parameters"><p:empty/></p:input>
    </p:xslt>
    <!--
    Prepare a dtbook to the Narrator schematron rules:
    - Rule 100: Every document needs at least one heading on level 1
    This fix assumes headings are not empty (e.g. empty headings were
    removed by a previous fix)
    -->
    <p:xslt>
        <p:input port="stylesheet"><p:document href="xsl/narrator-headings-r100.xsl"/></p:input>
        <p:input port="parameters"><p:empty/></p:input>
    </p:xslt>
    <!--
    - Adds the dc:Title meta element, if not present in input,
    or given but with null/whitespace only content values.
    - Adds the doctitle element in the frontmatter, if not
    present in input, or given but with null/whitespace only
    content values.

    Title value is taken:
    - from the 'dc:Title' metadata is present
    - or else from the first 'doctitle' element in the bodymatter
    - or else from the first heading 1.
    -->
    <p:xslt>
        <p:input port="stylesheet"><p:document href="xsl/narrator-title.xsl"/></p:input>
        <p:input port="parameters"><p:empty/></p:input>
    </p:xslt>
    <!--
    Prepare a dtbook to the Narrator schematron rules:
    - Rule 07: No <list> or <dl> inside <p> :
      - Breaks the parent paragraph into a sequence of paragraphs, list and dl
      - Each newly created paragraph has the same attributes as the original one
      - New paragraph IDs are created if necessary
      - The original paragraph ID is conserved for the first paragraph created
    -->
    <p:xslt>
        <p:input port="stylesheet"><p:document href="xsl/narrator-lists.xsl"/></p:input>
        <p:input port="parameters"><p:empty/></p:input>
    </p:xslt>

</p:declare-step>
