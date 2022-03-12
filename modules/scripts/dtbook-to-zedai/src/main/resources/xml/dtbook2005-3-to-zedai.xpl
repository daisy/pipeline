<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"  version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                name="dtbook2005-3-to-zedai" type="pxi:dtbook2005-3-to-zedai"
                exclude-inline-prefixes="pxi p">

    <p:documentation>
        Transforms DTBook 2005-3 XML into ZedAI XML. Part of the DTBook-to-ZedAI module.
    </p:documentation>

    <p:input port="source" primary="true"/>
    
    <!-- output is ZedAI -->
    <p:output port="result" primary="true">
        <p:pipe port="result" step="anchor-floating-annotations"/>
    </p:output>

    <p:documentation>Preprocess certain inline elements by making them into spans. This streamlines
        the number of transformation cases that need to be dealt with later.</p:documentation>
    <p:xslt name="rename-to-span" px:progress="1/22" px:message="Renaming certain elements to span with @role" px:message-severity="DEBUG">
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="rename-to-span.xsl"/>
        </p:input>
    </p:xslt>

    <p:documentation>Identify block-level code/kbd elements vs phrase-level</p:documentation>
    <p:xslt name="rename-code-kbd" px:progress="1/22" px:message-severity="DEBUG"
			px:message="Renaming code and kbd elements to reflect block or phrase variants">
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="rename-code-kbd.xsl"/>
        </p:input>
    </p:xslt>

    <p:documentation>Identify block-level annotation elements vs phrase-level</p:documentation>
    <p:xslt name="rename-annotation" px:progress="1/22" px:message-severity="DEBUG"
			px:message="Renaming annotation elements to identify block or phrase variants">
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="rename-annotation.xsl"/>
        </p:input>
    </p:xslt>

    <p:documentation>Convert br to ln</p:documentation>
    <p:xslt name="convert-linebreaks" px:progress="1/22" px:message="Convert br to lines" px:message-severity="DEBUG">
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="convert-linebreaks.xsl"/>
        </p:input>
    </p:xslt>

    <p:documentation>Group items in definition lists</p:documentation>
    <p:xslt name="convert-deflist-contents" px:progress="1/22" px:message="Grouping contents of a definition list into items" px:message-severity="DEBUG">
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="group-deflist-contents.xsl"/>
        </p:input>
    </p:xslt>

    <p:documentation>Normalize imggroup element placement to suit ZedAI's content
        model.</p:documentation>
    <p:xslt name="moveout-imggroup" px:progress="1/22" px:message="Move out inlined image groups" px:message-severity="DEBUG">
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="moveout-imggroup.xsl"/>
        </p:input>
    </p:xslt>

    <p:documentation>Normalize list element placement to suit ZedAI's content
        model.</p:documentation>
    <p:xslt name="moveout-list" px:progress="1/22" px:message="Move out inlined lists" px:message-severity="DEBUG">
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="moveout-list.xsl"/>
        </p:input>
    </p:xslt>

    <p:documentation>Normalize definition list element placement to suit ZedAI's content
        model.</p:documentation>
    <p:xslt name="moveout-deflist" px:progress="1/22" px:message="Move out inlined definition lists" px:message-severity="DEBUG">
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="moveout-deflist.xsl"/>
        </p:input>
    </p:xslt>

    <p:documentation>Normalize prodnote element placement to suit ZedAI's content
        model.</p:documentation>
    <p:xslt name="moveout-prodnote" px:progress="1/22" px:message="Move out inlined prodnotes" px:message-severity="DEBUG">
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="moveout-prodnote.xsl"/>
        </p:input>
    </p:xslt>

    <p:documentation>Normalize div element placement to suit ZedAI's content
        model.</p:documentation>
    <p:xslt name="moveout-div" px:progress="1/22" px:message="Move out inlined divs" px:message-severity="DEBUG">
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="moveout-div.xsl"/>
        </p:input>
    </p:xslt>

    <p:documentation>Normalize poem element placement to suit ZedAI's content
        model.</p:documentation>
    <p:xslt name="moveout-poem" px:progress="1/22" px:message="Move out inlined poems" px:message-severity="DEBUG">
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="moveout-poem.xsl"/>
        </p:input>
    </p:xslt>

    <p:documentation>Normalize linegroup element placement to suit ZedAI's content
        model.</p:documentation>
    <p:xslt name="moveout-linegroup" px:progress="1/22" px:message="Move out inlined linegroups" px:message-severity="DEBUG">
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="moveout-linegroup.xsl"/>
        </p:input>
    </p:xslt>

    <p:documentation>Normalize table element placement to suit ZedAI's content
        model.</p:documentation>
    <p:xslt name="moveout-table" px:progress="1/22" px:message="Move out inlined tables" px:message-severity="DEBUG">
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="moveout-table.xsl"/>
        </p:input>
    </p:xslt>

    <p:documentation>Normalize sidebar element placement to suit ZedAI's content
        model.</p:documentation>
    <p:xslt name="moveout-sidebar" px:progress="1/22" px:message="Move out inlined sidebars" px:message-severity="DEBUG">
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="moveout-sidebar.xsl"/>
        </p:input>
    </p:xslt>

    <p:documentation>Normalize note element placement to suit ZedAI's content
        model.</p:documentation>
    <p:xslt name="moveout-note" px:progress="1/22" px:message="Move out inlined notes" px:message-severity="DEBUG">
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="moveout-note.xsl"/>
        </p:input>
    </p:xslt>

    <p:documentation>Normalize epigraph element placement to suit ZedAI's content
        model.</p:documentation>
    <p:xslt name="moveout-epigraph" px:progress="1/22" px:message="Move out inlined epigraphs" px:message-severity="DEBUG">
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="moveout-epigraph.xsl"/>
        </p:input>
    </p:xslt>

    <p:documentation>Normalize block-level annotation element placement to suit ZedAI's content
        model.</p:documentation>
    <p:xslt name="moveout-annotation-block" px:progress="1/22" px:message="Move out inlined annotation blocks" px:message-severity="DEBUG">
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="moveout-annotation.xsl"/>
        </p:input>
    </p:xslt>

    <p:documentation>Normalize block-level code element placement to suit ZedAI's content
        model.</p:documentation>
    <p:xslt name="moveout-code" px:progress="1/22" px:message="Move out inlined code blocks" px:message-severity="DEBUG">
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="moveout-code.xsl"/>
        </p:input>
    </p:xslt>

    <p:documentation>Normalize mixed block/inline content models by ensuring the content consists of
        all block or all inline elements.</p:documentation>
    <p:xslt name="normalize-block-inline" px:progress="1/22" px:message="Normalize mixed block/inline content" px:message-severity="DEBUG">
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="normalize-block-inline.xsl"/>
        </p:input>
    </p:xslt>

    <p:documentation>Normalize mixed section/block content models by ensuring the content consists
        of all section or all block elements.</p:documentation>
    <p:xslt name="normalize-section-block" px:progress="1/22" px:message="Normalize mixed section/block content" px:message-severity="DEBUG">
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="normalize-section-block.xsl"/>
        </p:input>
    </p:xslt>

    
    <p:documentation>Translate element and attribute names from DTBook to ZedAI</p:documentation>
    <p:xslt name="translate-to-zedai" px:progress="1/22" px:message="Translate to ZedAI" px:message-severity="DEBUG">
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="./translate-elems-attrs-to-zedai.xsl"/>
        </p:input>
    </p:xslt>

    <p:documentation>Anchor any floating anotations</p:documentation>
    <p:xslt name="anchor-floating-annotations" px:progress="1/22">
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="add-ref-to-annotations.xsl"/>
        </p:input>
    </p:xslt>

</p:declare-step>
