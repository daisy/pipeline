<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step version="1.0" name="dtbook2005-3-to-zedai" type="pxi:dtbook2005-3-to-zedai"
    xmlns:p="http://www.w3.org/ns/xproc" 
    xmlns:c="http://www.w3.org/ns/xproc-step"
    xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
    exclude-inline-prefixes="pxi p c">

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
    <p:xslt name="rename-to-span">
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="rename-to-span.xsl"/>
        </p:input>
    </p:xslt>

    <p:documentation>Identify block-level code/kbd elements vs phrase-level</p:documentation>
    <p:xslt name="rename-code-kbd">
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="rename-code-kbd.xsl"/>
        </p:input>
    </p:xslt>

    <p:documentation>Identify block-level annotation elements vs phrase-level</p:documentation>
    <p:xslt name="rename-annotation">
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="rename-annotation.xsl"/>
        </p:input>
    </p:xslt>

    <p:documentation>Convert br to ln</p:documentation>
    <p:xslt name="convert-linebreaks">
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="convert-linebreaks.xsl"/>
        </p:input>
    </p:xslt>

    <p:documentation>Group items in definition lists</p:documentation>
    <p:xslt name="convert-deflist-contents">
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="group-deflist-contents.xsl"/>
        </p:input>
    </p:xslt>

    <p:documentation>Normalize imggroup element placement to suit ZedAI's content
        model.</p:documentation>
    <p:xslt name="moveout-imggroup">
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="moveout-imggroup.xsl"/>
        </p:input>
    </p:xslt>

    <p:documentation>Normalize list element placement to suit ZedAI's content
        model.</p:documentation>
    <p:xslt name="moveout-list">
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="moveout-list.xsl"/>
        </p:input>
    </p:xslt>

    <p:documentation>Normalize definition list element placement to suit ZedAI's content
        model.</p:documentation>
    <p:xslt name="moveout-deflist">
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="moveout-deflist.xsl"/>
        </p:input>
    </p:xslt>

    <p:documentation>Normalize prodnote element placement to suit ZedAI's content
        model.</p:documentation>
    <p:xslt name="moveout-prodnote">
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="moveout-prodnote.xsl"/>
        </p:input>
    </p:xslt>

    <p:documentation>Normalize div element placement to suit ZedAI's content
        model.</p:documentation>
    <p:xslt name="moveout-div">
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="moveout-div.xsl"/>
        </p:input>
    </p:xslt>

    <p:documentation>Normalize poem element placement to suit ZedAI's content
        model.</p:documentation>
    <p:xslt name="moveout-poem">
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="moveout-poem.xsl"/>
        </p:input>
    </p:xslt>

    <p:documentation>Normalize linegroup element placement to suit ZedAI's content
        model.</p:documentation>
    <p:xslt name="moveout-linegroup">
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="moveout-linegroup.xsl"/>
        </p:input>
    </p:xslt>

    <p:documentation>Normalize table element placement to suit ZedAI's content
        model.</p:documentation>
    <p:xslt name="moveout-table">
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="moveout-table.xsl"/>
        </p:input>
    </p:xslt>

    <p:documentation>Normalize sidebar element placement to suit ZedAI's content
        model.</p:documentation>
    <p:xslt name="moveout-sidebar">
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="moveout-sidebar.xsl"/>
        </p:input>
    </p:xslt>

    <p:documentation>Normalize note element placement to suit ZedAI's content
        model.</p:documentation>
    <p:xslt name="moveout-note">
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="moveout-note.xsl"/>
        </p:input>
    </p:xslt>

    <p:documentation>Normalize epigraph element placement to suit ZedAI's content
        model.</p:documentation>
    <p:xslt name="moveout-epigraph">
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="moveout-epigraph.xsl"/>
        </p:input>
    </p:xslt>

    <p:documentation>Normalize block-level annotation element placement to suit ZedAI's content
        model.</p:documentation>
    <p:xslt name="moveout-annotation-block">
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="moveout-annotation.xsl"/>
        </p:input>
    </p:xslt>

    <p:documentation>Normalize block-level code element placement to suit ZedAI's content
        model.</p:documentation>
    <p:xslt name="moveout-code">
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="moveout-code.xsl"/>
        </p:input>
    </p:xslt>

    <p:documentation>Normalize mixed block/inline content models by ensuring the content consists of
        all block or all inline elements.</p:documentation>
    <p:xslt name="normalize-block-inline">
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="normalize-block-inline.xsl"/>
        </p:input>
    </p:xslt>

    <p:documentation>Normalize mixed section/block content models by ensuring the content consists
        of all section or all block elements.</p:documentation>
    <p:xslt name="normalize-section-block">
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="normalize-section-block.xsl"/>
        </p:input>
    </p:xslt>

    
    <p:documentation>Translate element and attribute names from DTBook to ZedAI</p:documentation>
    <p:xslt name="translate-to-zedai">
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="./translate-elems-attrs-to-zedai.xsl"/>
        </p:input>
    </p:xslt>

    <p:documentation>Anchor any floating anotations</p:documentation>
    <p:xslt name="anchor-floating-annotations">
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="add-ref-to-annotations.xsl"/>
        </p:input>
    </p:xslt>

</p:declare-step>
