<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                type="pxi:dtbook-repair">

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

    <!--Removes levelx if it has descendant headings of x-1 (this simplifies later steps).
        Note: Level normalizer cannot fix level1/level2/level1-->
    <p:xslt>
        <p:input port="stylesheet"><p:document href="xsl/repair-levelnormalizer.xsl"/></p:input>
        <p:input port="parameters"><p:empty/></p:input>
    </p:xslt>
    <!--Splits a level into several levels on every additional heading on the same level-->
    <p:xslt>
        <p:input port="stylesheet"><p:document href="xsl/repair-levelsplitter.xsl"/></p:input>
        <p:input port="parameters"><p:empty/></p:input>
    </p:xslt>
    <!--Add levels where needed.-->
    <p:xslt>
        <p:input port="stylesheet"><p:document href="xsl/repair-add-levels.xsl"/></p:input>
        <p:input port="parameters"><p:empty/></p:input>
    </p:xslt>
    <!--Changes a hx into a p with @class="hx" if parent isn't levelx
        Note:
            "Remove illegal headings" cannot handle hx in inline context.
            Support for this could be added.-->
    <p:xslt>
        <p:input port="stylesheet"><p:document href="xsl/repair-remove-illegal-headings.xsl"/></p:input>
        <p:input port="parameters"><p:empty/></p:input>
    </p:xslt>
    <!--Removes nested p-->
    <p:xslt>
        <p:input port="stylesheet"><p:document href="xsl/repair-flatten-redundant-nesting.xsl"/></p:input>
        <p:input port="parameters"><p:empty/></p:input>
    </p:xslt>
    <!--Adds an empty p-tag if hx is the last element-->
    <p:xslt>
        <p:input port="stylesheet"><p:document href="xsl/repair-complete-structure.xsl"/></p:input>
        <p:input port="parameters"><p:empty/></p:input>
    </p:xslt>
    <!--List fix:
        - wraps a list in li when the parent of the list is another list
        - adds @type if missing (default value is "pl")
        - corrects @depth atribute
        - removes enum attribute if the list is not ordered
        - removes start attribute if the list is not ordered-->
    <p:xslt>
        <p:input port="stylesheet"><p:document href="xsl/repair-lists.xsl"/></p:input>
        <p:input port="parameters"><p:empty/></p:input>
    </p:xslt>
    <!--idref must be present on noteref and annoref. Add idref if missing or
        change if empty.

        The value of the idref must include a fragment identifier.
        Add a hash mark in the beginning of all idref attributes that don't
        contain a hash mark.-->
    <p:xslt>
        <p:input port="stylesheet"><p:document href="xsl/repair-idref.xsl"/></p:input>
        <p:input port="parameters"><p:empty/></p:input>
    </p:xslt>
    <!--Similar to tidy-remove-empty-elements, but removes empty/whitespace elements
        that must have children.-->
    <p:xslt>
        <p:input port="stylesheet"><p:document href="xsl/repair-remove-empty-elements.xsl"/></p:input>
        <p:input port="parameters"><p:empty/></p:input>
    </p:xslt>
    <!--Update the @page attribute to make it match the contents of the pagenum element.
        If @page="normal" but the contents of the element doesn't match "normal"
        content, the @page attribute is changed to:
          - @page="front" if the contents is roman numerals and the pagenum element
            is located in the frontmatter of the book
          - @page="special" otherwise
        If @page="front" but the contents of the element doesn't match "front"
        content (neither roman nor arabic numerals), the @page attribute is changed to "special"-->
    <p:xslt>
        <p:input port="stylesheet"><p:document href="xsl/repair-pagenum-type.xsl"/></p:input>
        <p:input port="parameters"><p:empty/></p:input>
    </p:xslt>
    <!-- - fix metadata case errors
         - remove unknown dc-metadata
         - add dtb:uid (if missing) from dc:Identifier
         - add dc:Title (if missing) from doctitle
         - add auto-generated dtb:uid if missing (or if it has empty contents)-->
    <p:xslt>
        <p:input port="stylesheet"><p:document href="xsl/repair-metadata.xsl"/></p:input>
        <p:input port="parameters"><p:empty/></p:input>
    </p:xslt>

    <!--TODO : port the pipeline1 se_tpb_dtbookFix "InvalidURIExecutor" as a step and call it here -->

</p:declare-step>
