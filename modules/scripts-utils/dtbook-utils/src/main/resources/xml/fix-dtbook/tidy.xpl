<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                type="pxi:dtbook-tidy" name="main">

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

    <p:option name="simplifyHeadingLayout" select="false()" cx:as="xs:boolean">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2>Simplify headings layout</h2>
            <p>TBD</p>
        </p:documentation>
    </p:option>
    <p:option name="externalizeWhitespace" select="false()" cx:as="xs:boolean">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2>Externalize whitespaces</h2>
            <p>TBD</p>
        </p:documentation>
    </p:option>
    <p:option name="documentLanguage" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2>Document language</h2>
            <p>TBD</p>
        </p:documentation>
    </p:option>
    <!--Removes
        * empty/whitespace p except when
                1. preceded by hx or no preceding element and parent is a level
                and
                2. followed only by other empty p
        * empty/whitespace em, strong, sub, sup-->
    <p:xslt name="tidy-remove-empty-elements" px:message="tidy-remove-empty-elements">
        <p:input port="stylesheet"><p:document href="xsl/tidy-remove-empty-elements.xsl"/></p:input>
        <p:input port="parameters"><p:empty/></p:input>
    </p:xslt>
    <p:choose>
        <p:when test="$simplifyHeadingLayout">
            <!--Redundant level structure is sometimes used to mimic the original layout,
                but can pose a problem in some circumstances. "Level cleaner" simplifies
                the level structure by removing redundant levels (subordinate levels will
                be moved upwards). Note that the headings of the affected levels will
                also change, which will alter the appearance of the layout.-->
            <p:xslt name="tidy-level-cleaner" px:message="tidy-level-cleaner">
                <p:input port="stylesheet"><p:document href="xsl/tidy-level-cleaner.xsl"/></p:input>
                <p:input port="parameters"><p:empty/></p:input>
            </p:xslt>
        </p:when>
        <p:otherwise>
            <p:identity/>
        </p:otherwise>
    </p:choose>
    <!--Moves
        * pagenum inside h[x] before h[x]
        * pagenum inside a word after the word-->
    <p:xslt name="tidy-move-pagenum" px:message="tidy-move-pagenum">
        <p:input port="stylesheet"><p:document href="xsl/tidy-move-pagenum.xsl"/></p:input>
        <p:input port="parameters"><p:empty/></p:input>
    </p:xslt>
    <!--Update the @page attribute to make it match the contents of the pagenum
        element.

        If @page="front" but the contents of the element is an arabic number,
        the @page attribute is changed to "normal"
        (note:  arabic numbers are theoretically allowed from @page="front", but
        are not considered standard practice by many)

        If @page="special" but the element has no content, adds a dummy content
        ("page break").-->
    <p:xslt name="tidy-pagenum-type" px:message="tidy-pagenum-type">
        <p:input port="stylesheet"><p:document href="xsl/tidy-pagenum-type.xsl"/></p:input>
        <p:input port="parameters"><p:empty/></p:input>
    </p:xslt>
    <!--Removes otherwise empty p or li around pagenum (except p in td)-->
    <p:xslt name="tidy-change-inline-pagenum-to-block" px:message="tidy-change-inline-pagenum-to-block">
        <p:input port="stylesheet"><p:document href="xsl/tidy-change-inline-pagenum-to-block.xsl"/></p:input>
        <p:input port="parameters"><p:empty/></p:input>
    </p:xslt>
    <!--Inserts docauthor and doctitle if a frontmatter exists without those elements-->
    <p:xslt name="tidy-add-author-title" px:message="tidy-add-author-title">
        <p:input port="stylesheet"><p:document href="xsl/tidy-add-author-title.xsl"/></p:input>
        <p:input port="parameters"><p:empty/></p:input>
    </p:xslt>
    <!--Adds @xml:lang to dtbook, if dc:Language metadata is present-->
    <p:xslt name="tidy-add-lang" px:message="tidy-add-lang">
        <p:input port="stylesheet"><p:document href="xsl/tidy-add-lang.xsl"/></p:input>
        <p:with-param name="documentLanguage" select="$documentLanguage"/>
    </p:xslt>
    <p:choose>
        <p:when test="$externalizeWhitespace">
            <!--Externalizes leading and trailing whitespace from em, strong, sub, sup, pagenum, noteref.
                Handles any level of nesting, e.g.:
                    <em> <strong> this </strong> <strong> is <pagenum id="p-1"> 1 </pagenum> </strong> an example </em>-->
            <p:xslt name="tidy-externalize-whitespace" px:message="tidy-externalize-whitespace">
                <p:input port="stylesheet"><p:document href="xsl/tidy-externalize-whitespace.xsl"/></p:input>
                <p:input port="parameters"><p:empty/></p:input>
            </p:xslt>
        </p:when>
        <p:otherwise>
            <p:identity/>
        </p:otherwise>
    </p:choose>
    <!--Removes existing whitespace nodes and indents output to aid debugging.
            - Does not remove whitespace or apply indentation in inline context
            - Does not apply indentation when number of children is 1.-->
    <p:xslt name="tidy-indent" px:message="tidy-indent">
        <p:input port="stylesheet"><p:document href="xsl/tidy-indent.xsl"/></p:input>
        <p:input port="parameters"><p:empty/></p:input>
    </p:xslt>

</p:declare-step>
