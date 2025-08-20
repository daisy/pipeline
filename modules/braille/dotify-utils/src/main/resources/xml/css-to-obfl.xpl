<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="pxi:css-to-obfl"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:obfl="http://www.daisy.org/ns/2011/obfl"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:s="org.daisy.pipeline.braille.css.xpath.Style"
                exclude-inline-prefixes="pxi xsl"
                version="1.0">
    
    <p:documentation>
        Convert a document with inline braille CSS to OBFL (Open Braille Formatting Language).
    </p:documentation>
    
    <p:input port="source"/>
    <p:output port="result"/>
    
    <p:option name="document-locale" select="'und'">
      <!--
          This option exists in case a xml:lang attribute on the root element is not present, but
          language information is available e.g. from "dc:language" meta data.
      -->
    </p:option>
    <p:option name="text-transform" select="''"/>
    <p:option name="braille-charset" select="''"/>
    <p:option name="medium" select="()"/>
    <p:option name="skip-margin-top-of-page" select="false()" cx:as="xs:boolean"/>
    
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        px:error
        px:assert
      </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/css-utils/library.xpl">
      <p:documentation>
        px:css-cascade
      </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/braille/css-utils/library.xpl">
      <p:documentation>
        px:css-parse-stylesheet
        px:css-parse-properties
        px:css-parse-content
      </p:documentation>
    </p:import>
    <p:import href="adjust-boxes.xpl">
      <p:documentation>
        pxi:adjust-boxes
      </p:documentation>
    </p:import>
    <p:import href="eval-counter.xpl">
      <p:documentation>
        pxi:eval-counter
      </p:documentation>
    </p:import>
    <p:import href="eval-target-content.xpl">
      <p:documentation>
        pxi:eval-target-content
      </p:documentation>
    </p:import>
    <p:import href="eval-target-text.xpl">
      <p:documentation>
        pxi:eval-target-text
      </p:documentation>
    </p:import>
    <p:import href="flow-from.xpl">
      <p:documentation>
        pxi:flow-from
      </p:documentation>
    </p:import>
    <p:import href="flow-into.xpl">
      <p:documentation>
        pxi:flow-into
      </p:documentation>
    </p:import>
    <p:import href="label-targets.xpl">
      <p:documentation>
        pxi:label-targets
      </p:documentation>
    </p:import>
    <p:import href="make-anonymous-block-boxes.xpl">
      <p:documentation>
        pxi:make-anonymous-block-boxes
      </p:documentation>
    </p:import>
    <p:import href="make-anonymous-inline-boxes.xpl">
      <p:documentation>
        pxi:make-anonymous-inline-boxes
      </p:documentation>
    </p:import>
    <p:import href="make-boxes.xpl">
      <p:documentation>
        pxi:make-boxes
      </p:documentation>
    </p:import>
    <p:import href="make-pseudo-elements.xpl">
      <p:documentation>
        pxi:make-pseudo-elements
      </p:documentation>
    </p:import>
    <p:import href="make-table-grid.xpl">
      <p:documentation>
        pxi:make-table-grid
      </p:documentation>
    </p:import>
    <p:import href="preserve-white-space.xpl">
      <p:documentation>
        pxi:preserve-white-space
      </p:documentation>
    </p:import>
    <p:import href="render-table-by.xpl">
      <p:documentation>
        pxi:render-table-by
      </p:documentation>
    </p:import>
    <p:import href="shift-id.xpl">
      <p:documentation>
        pxi:shift-id
      </p:documentation>
    </p:import>
    <p:import href="shift-string-set.xpl">
      <p:documentation>
        pxi:shift-string-set
      </p:documentation>
    </p:import>
    <p:import href="split.xpl">
      <p:documentation>
        pxi:split
      </p:documentation>
    </p:import>
    <p:import href="propagate-page-break.xpl">
      <p:documentation>
        pxi:propagate-page-break
      </p:documentation>
    </p:import>
    <p:import href="shift-obfl-marker.xpl">
      <p:documentation>
        pxi:shift-obfl-marker
      </p:documentation>
    </p:import>
    <p:import href="make-obfl-pseudo-elements.xpl">
      <p:documentation>
        pxi:make-obfl-pseudo-elements
      </p:documentation>
    </p:import>
    <p:import href="extract-obfl-pseudo-elements.xpl">
      <p:documentation>
        pxi:extract-obfl-pseudo-elements
      </p:documentation>
    </p:import>
    <p:import href="obfl-css-definition.xpl">
      <p:documentation>
        pxi:obfl-css-definition
      </p:documentation>
    </p:import>
    <cx:import href="http://www.daisy.org/pipeline/modules/css-utils/library.xsl" type="application/xslt+xml">
      <p:documentation>
        pf:media-query-matches
      </p:documentation>
    </cx:import>
    <cx:import href="http://www.daisy.org/pipeline/modules/braille/css-utils/library.xsl" type="application/xslt+xml">
      <p:documentation>
        css:parse-counter-styles
      </p:documentation>
    </cx:import>
    <cx:import href="deep-parse-page-and-volume-stylesheets.xsl" type="application/xslt+xml">
      <p:documentation>
        f:deep-parse-page-and-volume-stylesheets
      </p:documentation>
    </cx:import>
    
    <p:declare-step type="pxi:recursive-parse-stylesheet-and-make-pseudo-elements">
        <p:input port="source"/>
        <p:output port="result"/>
        <px:css-parse-stylesheet>
            <p:documentation>
                Make css:page, css:volume, css:text-transform, css:hyphenation-resource,
                css:counter-style, css:after, css:before, css:footnote-call, css:duplicate,
                css:alternate, css:top-of-page, css:_obfl-alternate-scenario*,
                css:_obfl-on-toc-start, css:_obfl-on-collection-start, css:_obfl-on-volume-start,
                css:_obfl-on-volume-end, css:_obfl-on-collection-end, css:_obfl-on-toc-end,
                css:_obfl-volume-transition and css:_obfl-on-resumed attributes.
            </p:documentation>
        </px:css-parse-stylesheet>
        <px:css-parse-properties properties="flow">
            <p:documentation>
                Make css:flow attributes.
            </p:documentation>
        </px:css-parse-properties>
        <p:delete match="*[@css:_obfl-scenario]/@css:flow">
            <p:documentation>
                ::-obfl-alternate-scenario pseudo-elements must participate in the normal flow.
            </p:documentation>
        </p:delete>
        <p:choose>
            <p:when test="//*/@css:before|
                          //*/@css:after|
                          //*/@css:duplicate|
                          //*/@css:alternate|
                          //*/@css:footnote-call|
                          //*/@css:_obfl-on-toc-start|
                          //*/@css:_obfl-on-collection-start|
                          //*/@css:_obfl-on-volume-start|
                          //*/@css:_obfl-on-volume-end|
                          //*/@css:_obfl-on-collection-end|
                          //*/@css:_obfl-on-toc-end|
                          //*/@css:_obfl-on-resumed[not(.='_')]|
                          //*/@css:*[matches(local-name(),'^_obfl-alternate-scenario')]
                          ">
                <pxi:make-pseudo-elements>
                    <p:documentation>
                        Make css:before, css:after, css:duplicate, css:alternate and
                        css:footnote-call pseudo-elements from css:before, css:after, css:duplicate,
                        css:alternate and css:footnote-call attributes.
                    </p:documentation>
                </pxi:make-pseudo-elements>
                <pxi:make-obfl-pseudo-elements>
                    <p:documentation>
                        Make css:_obfl-on-toc-start, css:_obfl-on-collection-start,
                        css:_obfl-on-volume-start, css:_obfl-on-volume-end,
                        css:_obfl-on-collection-end, css:_obfl-on-toc-end, css:_obfl-on-resumed and
                        *[@css:_obfl-scenario] pseudo-elements.
                    </p:documentation>
                </pxi:make-obfl-pseudo-elements>
                <p:viewport match="//*[@css:_obfl-scenario and @style]">
                    <p:documentation>
                        Apply possible relative rules.
                    </p:documentation>
                    <px:css-cascade/>
                </p:viewport>
                <pxi:recursive-parse-stylesheet-and-make-pseudo-elements/>
            </p:when>
            <p:otherwise>
                <p:identity/>
            </p:otherwise>
        </p:choose>
    </p:declare-step>
    
    <p:add-xml-base/>
    <!--
        Force the obfl and css namespaces on the root element so that they do not end up on every
        single element that uses these namespaces.
        
        document('') refers to the document node of the containing stylesheet module (see
        https://www.w3.org/TR/xslt20/#document).
        
        FIXME: Had to be disabled because it fails when run from the dtbook-to-pef tests without
        OSGi. It does work with OSGi, and also when run from the dotify-formatter tests. Possibly it
        has to do with the base URI of this file being a jar:file: URI in this case.
    -->
    <!--
    <p:xslt>
        <p:input port="stylesheet">
            <p:inline>
                <xsl:stylesheet version="2.0">
                    <xsl:template match="/*">
                        <xsl:copy>
                            <xsl:copy-of select="document('')/*/namespace::*[name()='obfl']"/>
                            <xsl:copy-of select="document('')/*/namespace::*[name()='css']"/>
                            <xsl:sequence select="@*|node()"/>
                        </xsl:copy>
                    </xsl:template>
                </xsl:stylesheet>
            </p:inline>
        </p:input>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
    </p:xslt>
    -->
    
    <p:wrap match="/*" wrapper="_">
        <!--
            Wrap everything in new root element so that css:make-pseudo-elements below can not fail
            (it may otherwise try to output multiple documents).
        -->
    </p:wrap>

    <pxi:recursive-parse-stylesheet-and-make-pseudo-elements px:progress=".04">
        <p:documentation>
            Make css:page, css:volume, css:text-transform, css:hyphenation-resource,
            css:counter-style, css:top-of-page and css:_obfl-volume-transition attributes,
            css:after, css:before, css:duplicate, css:alternate, css:footnote-call,
            css:_obfl-on-toc-start, css:_obfl-on-volume-start, css:_obfl-on-volume-end,
            css:_obfl-on-toc-end, css:_obfl-on-resumed and css:_obfl-alternate-scenario
            pseudo-elements.
        </p:documentation>
    </pxi:recursive-parse-stylesheet-and-make-pseudo-elements>
    
    <px:assert name="assert-text-transform-only-on-root"
               error-code="XXX" message="@text-transform rules are only allowed on root element">
        <p:with-option name="test" select="not(/_/*//*/@css:text-transform)"/>
    </px:assert>
    <px:assert name="assert-hyphenation-resource-only-on-root"
               error-code="XXX" message="@hyphenation-resource rules are only allowed on root element">
        <p:with-option name="test" select="not(/_/*//*/@css:hyphenation-resource)"/>
    </px:assert>
    <px:assert name="assert-counter-style-only-on-root"
               error-code="XXX" message="@counter-style rules are only allowed on root element">
        <p:with-option name="test" select="not(/_/*//*/@css:counter-style)"/>
    </px:assert>
    <px:assert name="assert-volume-transition-only-on-root"
               error-code="XXX" message="@-obfl-volume-transition rules are only allowed on root element">
        <p:with-option name="test" select="not(/_/*//*/@css:_obfl-volume-transition)"/>
    </px:assert>
    <p:delete match="@css:text-transform|
                     @css:hyphenation-resource|
                     @css:counter-style|
                     @css:_obfl-volume-transition">
        <p:documentation>
            Delete @css:text-transform, @css:hyphenation-resource, @css:counter-style and
            @css:_obfl-volume-transition attributes.
        </p:documentation>
    </p:delete>
    
    <px:css-parse-properties px:progress=".02"
                             properties="display render-table-by table-header-policy">
        <p:documentation>
            Make css:display, css:render-table-by and css:table-header-policy attributes.
        </p:documentation>
    </px:css-parse-properties>
    
    <pxi:render-table-by px:progress=".02">
        <p:documentation>
            Layout tables as lists.
        </p:documentation>
    </pxi:render-table-by>
    
    <p:viewport px:progress=".01"
                match="*[@css:render-table-by and not(@css:display='table')]">
        <pxi:recursive-parse-stylesheet-and-make-pseudo-elements>
            <p:documentation>
                Need another pass because css:render-table-by inserts new styles.
            </p:documentation>
        </pxi:recursive-parse-stylesheet-and-make-pseudo-elements>
    </p:viewport>
    
    <p:group px:progress=".88">
    <p:variable name="page-and-volume-styles" select="f:deep-parse-page-and-volume-stylesheets(/)"/>
    
    <p:filter select="/_/*"/>

    <p:for-each px:progress=".03">
        <pxi:extract-obfl-pseudo-elements>
            <p:documentation>
                Extract css:_obfl-on-toc-start, css:_obfl-on-collection-start,
                css:_obfl-on-volume-start, css:_obfl-on-volume-end, css:_obfl-on-collection-end and
                css:_obfl-on-toc-end pseudo-elements into their own documents.
            </p:documentation>
        </pxi:extract-obfl-pseudo-elements>
    </p:for-each>
    
    <p:for-each px:progress=".05">
        <px:css-parse-properties px:progress=".50"
                                 properties="content string-set counter-reset counter-set counter-increment -obfl-marker">
            <p:documentation>
                Make css:content, css:string-set, css:counter-reset, css:counter-set,
                css:counter-increment and css:_obfl-marker attributes.
            </p:documentation>
        </px:css-parse-properties>
        <px:css-parse-content px:progress=".015">
            <p:documentation>
                Make css:string, css:text, css:content, css:counter and css:custom-func elements and
                text nodes from css:content attributes. <!-- depends on
                pxi:recursive-parse-stylesheet-and-make-pseudo-elements to evaluate attr() values in
                content properties -->
            </p:documentation>
        </px:css-parse-content>
    </p:for-each>
    
    <p:group px:progress=".05">
        <p:documentation>
            Split into a sequence of flows.
        </p:documentation>
        <p:for-each px:progress=".50">
            <px:css-parse-properties px:progress="1"
                                     properties="flow">
                <p:documentation>
                    Make css:flow attributes.
                </p:documentation>
            </px:css-parse-properties>
        </p:for-each>
        <p:split-sequence test="not(/css:_[@css:flow])" name="_1"/>
        <p:wrap-sequence wrapper="_"/>
        <pxi:flow-into name="_2" px:progress=".50">
            <p:documentation>
                Extract named flows based on css:flow attributes and place anchors (css:id
                attributes) in the normal flow.
            </p:documentation>
        </pxi:flow-into>
        <p:filter select="/_/*" name="_3"/>
        <p:identity>
            <p:input port="source">
                <p:pipe step="_3" port="result"/>
                <p:pipe step="_2" port="flows"/>
                <p:pipe step="_1" port="not-matched"/>
            </p:input>
        </p:identity>
    </p:group>
    
    <pxi:label-targets name="label-targets" px:progress=".01">
        <p:documentation>
            Make css:id attributes. <!-- depends on parse-content -->
        </p:documentation>
    </pxi:label-targets>
    
    <p:for-each>
      <p:delete match="@xml:base"/> <!-- this sets the base URI to the empty string -->
    </p:for-each>
    
    <pxi:eval-target-content px:progress=".01">
        <p:documentation>
            Evaluate css:content elements. <!-- depends on parse-content and label-targets -->
        </p:documentation>
    </pxi:eval-target-content>
    
    <p:for-each px:progress=".24">
        <px:css-parse-properties px:progress=".20"
                                 properties="white-space display">
            <p:documentation>
                Make css:white-space and css:display attributes.
            </p:documentation>
        </px:css-parse-properties>
        <pxi:preserve-white-space px:progress=".10">
            <p:documentation>
                Make css:white-space elements from css:white-space attributes.
            </p:documentation>
        </pxi:preserve-white-space>
        <p:add-attribute px:progress=".01"
                         match="*[@css:_obfl-scenario and not(@css:display[.=('block','table','none')])]"
                         attribute-name="css:display"
                         attribute-value="block">
            <p:documentation>
                Force "obfl-scenario" elements to be blocks.
            </p:documentation>
        </p:add-attribute>
        <p:add-attribute px:progress=".01"
                         match="*[@css:display=('-obfl-toc','-obfl-table-of-contents')]"
                         attribute-name="css:_obfl-toc" attribute-value="_">
            <p:documentation>
                Mark display:-obfl-toc elements.
            </p:documentation>
        </p:add-attribute>
        <p:add-attribute px:progress=".01"
                         match="*[@css:display='-obfl-list-of-references']"
                         attribute-name="css:_obfl-list-of-references" attribute-value="_">
            <p:documentation>
                Mark display:-obfl-list-of-references elements.
            </p:documentation>
        </p:add-attribute>
        <p:add-attribute px:progress=".01"
                         match="*[@css:display=('-obfl-toc','-obfl-table-of-contents','-obfl-list-of-references')]"
                         attribute-name="css:display" attribute-value="block">
            <p:documentation>
                Treat display:-obfl-toc and display:-obfl-list-of-references as block.
            </p:documentation>
        </p:add-attribute>
        <pxi:make-table-grid px:progress=".20">
            <p:documentation>
                Create table grid structures from HTML/DTBook tables.
            </p:documentation>
        </pxi:make-table-grid>
        <pxi:make-boxes px:progress=".20">
            <p:documentation>
                Make css:box elements based on css:display attributes and list-style-type
                properties. <!-- depends on flow-into, label-targets and make-table-grid -->
            </p:documentation>
        </pxi:make-boxes>
        <p:add-attribute px:progress=".01"
                         match="css:box[not(@type)]" attribute-name="type" attribute-value="inline"/>
        <p:group px:progress=".25">
            <p:documentation>
                Move css:render-table-by, css:_obfl-table-col-spacing, css:_obfl-table-row-spacing
                and css:_obfl-preferred-empty-space attributes to 'table' css:box elements.
            </p:documentation>
            <px:css-parse-properties px:progress=".50"
                                     properties="-obfl-table-col-spacing -obfl-table-row-spacing -obfl-preferred-empty-space"/>
            <p:label-elements match="*[@css:render-table-by]/css:box[@type='table']"
                              attribute="css:render-table-by"
                              label="parent::*/@css:render-table-by"/>
            <p:label-elements match="*[@css:_obfl-table-col-spacing]/css:box[@type='table']"
                              attribute="css:_obfl-table-col-spacing"
                              label="parent::*/@css:_obfl-table-col-spacing"/>
            <p:label-elements match="*[@css:_obfl-table-row-spacing]/css:box[@type='table']"
                              attribute="css:_obfl-table-row-spacing"
                              label="parent::*/@css:_obfl-table-row-spacing"/>
            <p:label-elements match="*[@css:_obfl-preferred-empty-space]/css:box[@type='table']"
                              attribute="css:_obfl-preferred-empty-space"
                              label="parent::*/@css:_obfl-preferred-empty-space"/>
            <p:delete match="*[not(self::css:box[@type='table'])]/@css:render-table-by">
                <!--
                    This also deletes the css:render-table-by attributes that where already
                    processed with the css:render-table-by step above.
                -->
            </p:delete>
            <p:delete match="*[not(self::css:box[@type='table'])]/@css:_obfl-table-col-spacing"/>
            <p:delete match="*[not(self::css:box[@type='table'])]/@css:_obfl-table-row-spacing"/>
            <p:delete match="*[not(self::css:box[@type='table'])]/@css:_obfl-preferred-empty-space"/>
        </p:group>
    </p:for-each>
    
    <p:group px:progress=".61">
    <p:variable name="page-counters"
                cx:as="xs:string*"
                select="let $page-styles := for $s in $page-and-volume-styles return s:get($s,'@page'),
                            $volume-styles := for $s in $page-and-volume-styles return s:get($s,'@volume'),
                            $volume-styles := for $s in $volume-styles return ($s,for $k in s:keys($s)[matches(.,'^&amp;:')] return s:get($s,$k)),
                            $volume-begin-styles := for $s in $volume-styles return s:get($s,'@begin'),
                            $volume-begin-page-styles := for $s in $volume-begin-styles return s:get($s,'@page'),
                            $volume-end-styles := for $s in $volume-styles return s:get($s,'@end'),
                            $volume-end-page-styles := for $s in $volume-end-styles return s:get($s,'@page')
                        return distinct-values((
                          if (empty($page-styles)) then
                            'page'
                          else
                            for $s in $page-styles return
                              (for $p in s:get($s,'counter-increment') return tokenize(string($p),'\s+'),'page')[1]
                          ,
                          if (empty($volume-begin-page-styles)) then
                            'pre-page'
                          else
                            for $s in $volume-begin-page-styles return
                              (for $p in s:get($s,'counter-increment') return tokenize(string($p),'\s+'),'pre-page')[1]
                          ,
                          if (empty($volume-end-page-styles)) then
                            'post-page'
                          else
                            for $s in $volume-end-page-styles return
                              (for $p in s:get($s,'counter-increment') return tokenize(string($p),'\s+'),'post-page')[1]
                        ))"/>
    
    <pxi:eval-counter px:progress=".17">
        <p:documentation>
            Evaluate css:counter elements. All css:counter-set, css:counter-increment and
            css:counter-reset attributes in the output will be manipulating page counters. <!--
            depends on label-targets, parse-content and make-boxes -->
        </p:documentation>
        <p:with-option name="exclude-counters" select="string-join(($page-counters,
                                                                    'volume',
                                                                    '-obfl-volumes',
                                                                    '-obfl-sheets-in-document',
                                                                    '-obfl-sheets-in-volume',
                                                                    '-obfl-started-volume-number',
                                                                    '-obfl-started-page-number',
                                                                    '-obfl-started-volume-first-content-page'),' ')">
            <p:empty/>
        </p:with-option>
        <p:with-option name="counter-styles" select="css:parse-counter-styles(/_/*/@css:counter-style)">
            <p:pipe step="assert-counter-style-only-on-root" port="result"/>
        </p:with-option>
    </pxi:eval-counter>
    
    <p:group px:progress=".03">
        <p:for-each>
            <p:group>
                <p:documentation>
                    Rename -obfl-collection() to flow() so that css:flow-from will process them,
                    except if ::-obfl-on-volume-start or ::-obfl-on-volume-end pseudo-elements are
                    present and the scope is 'document'.
                </p:documentation>
                <p:label-elements match="*[@css:_obfl-list-of-references
                                           and not(@css:_obfl-on-volume-start or @css:_obfl-on-volume-start)]
                                          //css:custom-func[@name='-obfl-collection']|
                                         *[@css:_obfl-list-of-references
                                           and (@css:_obfl-on-volume-start or @css:_obfl-on-volume-start)]
                                          //css:custom-func[@name='-obfl-collection']
                                                           [not(@arg2='document')]"
                                  attribute="from" label="@arg1"/>
                <p:rename match="css:custom-func[@name='-obfl-collection'][@from]" new-name="css:flow"/>
                <p:label-elements match="css:flow[@name='-obfl-collection'][@arg2]" attribute="scope" label="@arg2"/>
                <p:delete match="css:flow[@name='-obfl-collection']/@name|
                                 css:flow[@name='-obfl-collection']/@arg1|
                                 css:flow[@name='-obfl-collection']/@arg2"/>
            </p:group>
            <px:assert error-code="XXX"
                       message="An element with 'display: -obfl-list-of-references' must consist of exactly one
 -obfl-collection() or flow() and nothing more.">
                <p:documentation>
                    Check that elements with "display: -obfl-list-of-references" contain exactly one
                    -obfl-collection() or flow().
                </p:documentation>
                <p:with-option name="test" select="every $e in //*[@css:_obfl-list-of-references] satisfies
                                                   not($e//node()[not(self::css:box[@type='inline']|
                                                                      self::css:_|
                                                                      self::css:flow|
                                                                      self::css:custom-func[@name='-obfl-collection'])])"/>
            </px:assert>
        </p:for-each>
        <pxi:flow-from px:progress=".03">
            <p:documentation>
                Evaluate css:flow elements. <!-- depends on parse-content and eval-counter -->
            </p:documentation>
        </pxi:flow-from>
        <p:for-each>
            <p:group>
                <p:documentation>
                    Rename -obfl-collection() that were previously skipped to flow().
                </p:documentation>
                <p:label-elements match="*[@css:_obfl-list-of-references]
                                          //css:custom-func[@name='-obfl-collection']"
                                  attribute="from" label="@arg1"/>
                <p:rename match="css:custom-func[@name='-obfl-collection'][@from]" new-name="css:flow"/>
                <p:label-elements match="css:flow[@name='-obfl-collection'][@arg2]" attribute="scope" label="@arg2"/>
                <p:delete match="css:flow[@name='-obfl-collection']/@name|
                                 css:flow[@name='-obfl-collection']/@arg1|
                                 css:flow[@name='-obfl-collection']/@arg2"/>
            </p:group>
            <p:documentation>
                Wrap unevaluated css:flow in block box so that we can be sure that when evaluated
                later inline boxes have no descendant block boxes (see also
                css:make-anonymous-inline-boxes).
                
                Note that this is currently a pointless step because unless they are a descendant of
                a "list-of-references" element, unevaluated css:flow elements will be ignored (and
                result in a warning) later.
            </p:documentation>
            <p:wrap match="css:flow[not(ancestor::*[@css:_obfl-list-of-references])]"
                    wrapper="css:_block-box_"
                    group-adjacent="true()"/>
            <p:add-attribute match="css:_block-box_" attribute-name="type" attribute-value="block"/>
            <p:rename match="css:_block-box_" new-name="css:box"/>
        </p:for-each>
    </p:group>
    
    <pxi:eval-target-text px:progress=".01">
        <p:documentation>
            Evaluate css:text elements. <!-- depends on label-targets and parse-content -->
        </p:documentation>
    </pxi:eval-target-text>
    
    <p:for-each px:progress=".13">
        <pxi:make-anonymous-inline-boxes px:progress=".50">
            <p:documentation>
                Wrap/unwrap with inline css:box elements.
            </p:documentation>
        </pxi:make-anonymous-inline-boxes>
        <p:delete px:progress=".05"
                  match="/*[@css:flow]//*/@css:volume|
                         //css:box[@type='table']//*/@css:page|
                         //css:box[@type='table']//*/@css:volume|
                         //css:box[@type='table']//*/@css:counter-set">
            <p:documentation>
                Don't support 'volume' within named flows. Don't support 'volume', 'page' and
                'counter-set' within tables.
            </p:documentation>
        </p:delete>
        <p:group px:progress=".05">
            <p:documentation>
                Move css:counter-set attribute to css:box elements.
            </p:documentation>
            <p:insert match="css:_[@css:counter-set]" position="first-child">
                <p:input port="insertion">
                    <p:inline><css:box type="inline"/></p:inline>
                </p:input>
            </p:insert>
            <p:label-elements match="css:_[@css:counter-set]/css:box[1]"
                              attribute="css:counter-set"
                              label="parent::css:_/@css:counter-set"/>
            <p:delete match="css:_/@css:counter-set"/>
        </p:group>
        <p:group px:progress=".05">
            <p:documentation>
                Move css:page attributes to css:box elements.
            </p:documentation>
            <p:label-elements match="css:box[not(@css:page)][(ancestor::css:_[@css:page]|ancestor::*[not(self::css:_)])[last()]/self::css:_]"
                              attribute="css:page"
                              label="(ancestor::*[@css:page])[last()]/@css:page"/>
            <p:delete match="css:_/@css:page"/>
        </p:group>
        <p:group px:progress=".05">
            <p:documentation>
                Move css:volume attributes to css:box elements.
            </p:documentation>
            <p:label-elements match="css:box[not(@css:volume)][(ancestor::css:_[@css:volume]|ancestor::*[not(self::css:_)])[last()]/self::css:_]"
                              attribute="css:volume"
                              label="(ancestor::*[@css:volume])[last()]/@css:volume"/>
            <p:delete match="css:_/@css:volume"/>
        </p:group>
        <p:delete match="css:box[@type='inline']/@css:top-of-page">
            <p:documentation>
                :top-of-page is only supported on block elements.
            </p:documentation>
        </p:delete>
        <pxi:shift-string-set px:progress=".15">
            <p:documentation>
                Move css:string-set attributes to inline css:box elements. <!-- depends on
                make-anonymous-inline-boxes -->
            </p:documentation>
        </pxi:shift-string-set>
        <pxi:shift-obfl-marker px:progress=".15">
            <p:documentation>
                Move css:_obfl-marker attributes to inline css:box elements. <!-- depends on
                make-anonymous-inline-boxes -->
            </p:documentation>
        </pxi:shift-obfl-marker>
    </p:for-each>
    
    <pxi:shift-id px:progress=".01">
        <p:documentation>
            Move css:id attributes to inline css:box elements.
        </p:documentation>
    </pxi:shift-id>
    
    <p:for-each px:progress=".27">
        <p:unwrap match="css:_[not(@css:* except (@css:_obfl-on-resumed|
                                                  @css:_obfl-list-of-references|
                                                  @css:_obfl-on-collection-start|
                                                  @css:_obfl-on-collection-end|
                                                  @css:_obfl-on-volume-start|
                                                  @css:_obfl-on-volume-end))
                               and parent::*]">
            <p:documentation>
                All css:_ elements except for root elements, top-level elements in named flows (with
                css:anchor attribute), and empty elements with a css:id, css:string-set or
                css:_obfl-marker attribute within a css:box element should be gone now.
                FIXME: We ignore that css:_ elements with a css:_obfl-on-resumed attribute may exist
                if an inline `::-obfl-on-resumed` pseudo-element contains a block
                pseudo-element. <!-- depends on shift-id and shift-string-set -->
            </p:documentation>
        </p:unwrap>
        <pxi:make-anonymous-block-boxes px:progress=".05">
            <p:documentation>
                Wrap inline css:box elements in block css:box elements where necessary. <!-- depends
                on unwrap css:_ -->
            </p:documentation>
        </pxi:make-anonymous-block-boxes>
        <px:css-parse-properties px:progress=".25"
                                 properties="margin-left margin-right margin-top margin-bottom
                                             padding-left padding-right padding-top padding-bottom
                                             border-left-pattern border-right-pattern border-top-pattern
                                             border-bottom-pattern border-left-style border-right-style
                                             border-top-style border-bottom-style text-indent">
            <p:documentation>
                Make css:margin-left, css:margin-right, css:margin-top, css:margin-bottom,
                css:padding-left, css:padding-right, css:padding-top, css:padding-bottom,
                css:border-left-pattern, css:border-right-pattern, css:border-top-pattern,
                css:border-bottom-pattern, css:border-left-style, css:border-right-style,
                css:border-top-style, css:border-bottom-style and css:text-indent attributes.
            </p:documentation>
        </px:css-parse-properties>
        <pxi:adjust-boxes px:progress=".25">
            <p:documentation>
                <!-- depends on make-anonymous-block-boxes -->
            </p:documentation>
        </pxi:adjust-boxes>
        <pxi:obfl-css-definition px:progress=".40">
            <p:documentation>
                Convert CSS styles to corresponding OBFL properties.
            </p:documentation>
            <p:with-param name="initial-braille-charset" select="if ($braille-charset='') then 'unicode' else 'custom'"/>
        </pxi:obfl-css-definition>
        <p:xslt px:progress=".03">
            <p:input port="parameters">
                <p:empty/>
            </p:input>
            <p:input port="stylesheet">
                <p:document href="css-to-obfl.block-boxes-with-no-line-boxes.xsl"/>
            </p:input>
            <p:documentation>
                Remove text nodes from block boxes with no line boxes.
            </p:documentation>
        </p:xslt>
        <p:delete px:progrss=".02"
                  match="//css:box[@type='table']//*/@css:page-break-before|
                         //css:box[@type='table']//*/@css:page-break-after|
                         //*[@css:_obfl-toc]//*/@css:page-break-before|
                         //*[@css:_obfl-toc]//*/@css:page-break-after">
            <p:documentation>
                Don't support 'page-break-before' and 'page-break-after' within tables or
                '-obfl-toc' elements.
            </p:documentation>
        </p:delete>
    </p:for-each>
    
    <p:group px:progress=".27">
        <p:documentation>
            Split flows into sections.
        </p:documentation>
        <p:for-each px:progress=".50">
            <pxi:propagate-page-break px:progress=".80">
                <p:documentation>
                    Insert forced page breaks to satisfy the 'page' and 'volume' properties. <!--
                    depends on make-anonymous-block-boxes -->
                </p:documentation>
            </pxi:propagate-page-break>
            <p:group px:progress=".10">
                <p:documentation>
                    Convert css:page-break-after="auto-right" to a css:page-break-before on the
                    following sibling, and css:volume-break-after="always" to a
                    css:volume-break-before on the following sibling.
                </p:documentation>
                <p:add-attribute match="css:box[@type='block'][preceding-sibling::*[1]/@css:page-break-after='auto-right']"
                                 attribute-name="css:page-break-before"
                                 attribute-value="auto-right"/>
                <p:add-attribute match="css:box[@type='block'][preceding-sibling::*[1]/@css:volume-break-after='always']"
                                 attribute-name="css:volume-break-before"
                                 attribute-value="always"/>
                <p:delete match="css:box[@type='block'][following-sibling::*]/@css:page-break-after[.='auto-right']"/>
                <p:delete match="css:box[@type='block'][following-sibling::*]/@css:volume-break-after[.='always']"/>
            </p:group>
            <pxi:split px:progress=".10"
                       split-before="css:box[preceding::css:box]
                                            [@css:counter-set or
                                             @css:page-break-before='auto-right' or
                                             @css:volume-break-before='always']
                                            [not(ancestor::*[@css:_obfl-scenarios])]">
                <p:documentation>
                    Split before css:counter-set attributes, and before css:volume-break-before
                    attributes with value 'always'. <!-- depends on make-boxes and
                    propagate-page-break -->
                </p:documentation>
            </pxi:split>
        </p:for-each>
        <p:for-each px:progress=".50">
            <p:group px:progress=".10">
                <p:documentation>
                    Move css:page, css:counter-set and css:volume attributes to css:_ root element.
                </p:documentation>
                <p:wrap wrapper="css:_" match="/*[not(self::css:_)]"/>
                <p:label-elements match="/*[descendant::*/@css:page]" attribute="css:page"
                                  label="(descendant::*/@css:page)[last()]"/>
                <p:label-elements match="/*[descendant::*/@css:counter-set]" attribute="css:counter-set"
                                  label="(descendant::*/@css:counter-set)[last()]"/>
                <p:label-elements match="/*[descendant::*/@css:volume]" attribute="css:volume"
                                  label="(descendant::*/@css:volume)[last()]"/>
                <p:delete match="/*//*/@css:page"/>
                <p:delete match="/*//*/@css:counter-set"/>
                <p:delete match="/*//*/@css:volume"/>
            </p:group>
            <p:group px:progress=".05">
                <p:documentation>
                    Delete properties connected to the top of a box if it is not the first part of a
                    split up box. Delete properties connected to the bottom of a box if it is not
                    the last part of a split up box.
                </p:documentation>
                <p:delete match="css:box[@part[not(.='first')]]/@css:margin-top|
                                 css:box[@part[not(.='first')]]/@css:padding-top|
                                 css:box[@part[not(.='first')]]/@css:page-break-before|
                                 css:box[@part[not(.='last')]]/@css:margin-bottom|
                                 css:box[@part[not(.='last')]]/@css:padding-bottom|
                                 css:box[@part[not(.='last')]]/@css:page-break-after"/>
            </p:group>
            <p:group  px:progress=".85">
                <p:documentation>
                    Move around and change page breaking related properties so that they can be mapped
                    one-to-one on OBFL properties.
                </p:documentation>
                <pxi:propagate-page-break px:progress=".85">
                    <p:documentation>
                        Propagate css:page-break-before, css:page-break-after,
                        css:page-break-inside, css:volume-break-before and css:volume-break-after
                        attributes. (Needs to be done a second time because the box tree has been
                        broken up by css:split. css:page-break-before='right' will now be propagated
                        all the way to the root box.)
                    </p:documentation>
                </pxi:propagate-page-break>
                <p:group px:progress=".05">
                    <p:documentation>
                        Rename 'auto-right' (the special forced page break value to satisfy the
                        'page' property) to 'right'.
                    </p:documentation>
                    <p:add-attribute match="css:box[@css:page-break-after='auto-right']"
                                     attribute-name="css:page-break-after"
                                     attribute-value='right'/>
                    <p:add-attribute match="css:box[@css:page-break-before='auto-right']"
                                     attribute-name="css:page-break-before"
                                     attribute-value='right'/>
                </p:group>
                <p:group px:progress=".05">
                    <p:documentation>
                        Convert css:page-break-before="avoid" to a css:page-break-after on the
                        preceding sibling and css:page-break-after="always|right|left" to a
                        css:page-break-before on the following sibling.
                    </p:documentation>
                    <p:add-attribute match="css:box[@type='block'][following-sibling::*[1]/@css:page-break-before='avoid']"
                                     attribute-name="css:page-break-after"
                                     attribute-value="avoid"/>
                    <p:label-elements match="css:box[@type='block'][preceding-sibling::*[1]/@css:page-break-after=('always','left','right')]"
                                      attribute="css:page-break-before"
                                      label="preceding-sibling::*[1]/@css:page-break-after"/>
                    <p:delete match="@css:page-break-before[.='avoid']"/>
                    <p:delete match="css:box[@type='block'][following-sibling::*]/@css:page-break-after[.=('always','left','right')]"/>
                </p:group>
                <p:group px:progress=".05">
                    <p:documentation>
                        Move css:page-break-after="avoid" to last descendant block.
                    </p:documentation>
                    <p:add-attribute match="css:box[@type='block'
                                                    and not(child::css:box[@type='block'])
                                                    and (some $self in . satisfies
                                                      some $ancestor in $self/ancestor::*[@css:page-break-after='avoid'] satisfies
                                                        not($self/following::css:box intersect $ancestor//*))]"
                                     attribute-name="css:page-break-after"
                                     attribute-value="avoid"/>
                    <p:delete match="css:box[@type='block' and child::css:box[@type='block']]/@css:page-break-after[.='avoid']"/>
                </p:group>
            </p:group>
        </p:for-each>
    </p:group>
    
    <p:for-each px:progress=".01">
        <p:choose px:progress="1">
            <p:documentation>
                Move css:margin-top of blocks that have a preceding (non-empty) block to
                css:margin-bottom of their preceding block, and rename other css:margin-top to
                css:margin-top-skip-if-top-of-page.
            </p:documentation>
            <p:when test="$skip-margin-top-of-page">
                <p:label-elements px:progress=".20"
                                  match="css:box
                                           [@type='block']
                                           [descendant::css:box[@type='inline' and child::node()]]
                                           [following-sibling::*[1]
                                              [some $self in . satisfies
                                                 $self/descendant-or-self::*
                                                   [@css:margin-top][1]
                                                   [not(preceding::* intersect $self/descendant::*)]
                                                   [not((ancestor::* intersect $self/descendant-or-self::*)
                                                        [@css:border-top-pattern or @css:border-top-style])]]]"
                                  attribute="css:_margin-bottom_"
                                  label="max((0,
                                              @css:margin-bottom/number(),
                                              following::*[@css:margin-top][1]/@css:margin-top/number()))"/>
                <p:delete px:progress=".60"
                          match="@css:margin-top[(preceding::css:box[@type='block']
                                                    except ancestor::*/preceding-sibling::*/descendant::*)
                                                   [last()][@css:_margin-bottom_]]"/>
                <p:rename match="@css:_margin-bottom_" new-name="css:margin-bottom"/>
                <p:rename px:progress=".20"
                          match="css:box[@type='block'][@css:margin-top]/@css:margin-top"
                          new-name="css:margin-top-skip-if-top-of-page"/>
            </p:when>
            <p:otherwise>
                <p:identity/>
            </p:otherwise>
        </p:choose>
    </p:for-each>
    
    <p:xslt px:progress=".06">
        <p:input port="stylesheet">
            <p:document href="css-to-obfl.xsl"/>
        </p:input>
        <p:with-param name="document-locale" select="$document-locale">
            <p:empty/>
        </p:with-param>
        <p:with-param name="braille-charset-table" select="$braille-charset">
            <p:empty/>
        </p:with-param>
        <p:with-param name="page-and-volume-styles" select="$page-and-volume-styles">
            <p:empty/>
        </p:with-param>
        <p:with-param name="page-counters" select="$page-counters">
            <p:empty/>
        </p:with-param>
        <p:with-param name="volume-transition"
                      select="for $s in /_/*/@css:_obfl-volume-transition
                                            /css:parse-stylesheet(concat('@-obfl-volume-transition { ',string(.),' }'))
                              return s:get($s,'@-obfl-volume-transition')">
            <p:pipe step="assert-volume-transition-only-on-root" port="result"/>
        </p:with-param>
        <p:with-param name="default-text-transform" select="$text-transform">
            <p:empty/>
        </p:with-param>
        <p:with-param name="pages-per-sheet"
                      select="if (exists($medium) and pf:media-query-matches('(-daisy-saddle-stitch)',$medium))
                              then 4
                              else 2">
            <p:empty/>
        </p:with-param>
        <p:with-param name="text-transforms" select="/_/*/@css:text-transform/css:parse-stylesheet(.)">
            <p:pipe step="assert-text-transform-only-on-root" port="result"/>
        </p:with-param>
        <p:with-param name="hyphenation-resources" select="/_/*/@css:hyphenation-resource/css:parse-stylesheet(.)">
            <p:pipe step="assert-hyphenation-resource-only-on-root" port="result"/>
        </p:with-param>
        <p:with-param name="counter-styles" select="/_/*/@css:counter-style/css:parse-stylesheet(.)">
            <p:pipe step="assert-counter-style-only-on-root" port="result"/>
        </p:with-param>
    </p:xslt>
    
    <!--
        generate layout-masters
    -->
    <p:xslt px:progress=".04">
        <p:input port="stylesheet">
            <p:document href="generate-obfl-layout-master.xsl"/>
        </p:input>
        <p:with-param name="medium" select="$medium">
            <p:empty/>
        </p:with-param>
        <p:with-param name="braille-charset-table" select="$braille-charset">
            <p:empty/>
        </p:with-param>
        <p:with-param name="page-and-volume-styles" select="$page-and-volume-styles">
            <p:empty/>
        </p:with-param>
        <p:with-param name="counter-styles" select="/_/*/@css:counter-style/css:parse-stylesheet(.)">
            <p:pipe step="assert-counter-style-only-on-root" port="result"/>
        </p:with-param>
    </p:xslt>
    
    </p:group>
    </p:group>
    
    <!--
        add range attributes to toc-entry-on-resumed
        
        It is assumed that a ::-obfl-on-resumed pseudo-element is used on an element that represents
        a single regular toc entry (a block element that generates only a single block box with
        inline content), in which case the first preceding toc-entry element gives us the start of
        the range and the last toc-entry before the first following toc-entry-on-resumed gives us
        the end of the range. This is the only sensible way to use ::-obfl-on-resumed
        pseudo-element.
    -->
    <p:label-elements match="obfl:toc-entry-on-resumed"
                      attribute="range"
                      label="concat('[',
                                    ((preceding::obfl:toc-entry)[last()] intersect ancestor::obfl:table-of-contents/descendant::*)/@ref-id,
                                    ',',
                                    ((following::obfl:toc-entry-on-resumed[1]/preceding::obfl:toc-entry)[last()]
                                      intersect ancestor::obfl:table-of-contents/descendant::*
                                      intersect following::*)/@ref-id,
                                    ')')"/>
    <p:choose>
      <p:when test="//obfl:toc-entry-on-resumed[starts-with(@range,'[,')]">
        <px:error code="XXX" message="Unexpected use of ::-obfl-on-resumed"/>
      </p:when>
      <p:otherwise>
        <p:identity/>
      </p:otherwise>
    </p:choose>
    
    <!--
        fill in <marker class="foo/prev"/> values
    -->
    <p:label-elements px:progress=".01"
                      match="obfl:marker[not(@value)]
                                        [some $class in @class satisfies
                                         preceding::obfl:marker[concat(@class,'/prev')=$class]]"
                      attribute="value"
                      label="(for $class in @class return
                              (preceding::obfl:marker[concat(@class,'/prev')=$class])[last()]/@value)[1]"/>
    <p:delete match="obfl:marker[not(@value)]"/>
    
    <!--
        remove markers that are not used
    -->
    <p:delete match="obfl:marker[for $class in @class return
                                 not(//obfl:marker-reference[@marker=$class] or
                                     //obfl:marker-indicator[tokenize(normalize-space(@markers),' ')=$class])]"/>

    <!--
        FIXME: because otherwise empty marker values would be regarded as absent in Dotify
        (FieldResolver.resolveCompoundMarkerReferenceField)
    -->
    <p:add-attribute px:progress=".01"
                     match="obfl:marker[@value='']" attribute-name="value" attribute-value="&#x200B;"/>

    <!--
        move table-of-contents elements to the right place
    -->
    <p:xslt px:progress=".01">
        <p:input port="stylesheet">
            <p:document href="move-table-of-contents.xsl"/>
        </p:input>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
    </p:xslt>
    
</p:declare-step>
