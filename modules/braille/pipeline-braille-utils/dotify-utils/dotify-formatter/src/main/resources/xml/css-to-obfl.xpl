<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="pxi:css-to-obfl"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:obfl="http://www.daisy.org/ns/2011/obfl"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                exclude-inline-prefixes="pxi xsl"
                version="1.0">
    
    <p:documentation>
        Convert a document with inline braille CSS to OBFL (Open Braille Formatting Language).
    </p:documentation>
    
    <p:input port="source" sequence="true"/>
    <p:output port="result" sequence="false"/>
    
    <p:option name="text-transform" required="true"/>
    <p:option name="duplex" select="'true'"/>
    <p:option name="skip-margin-top-of-page" select="'false'"/>
    
    <p:import href="http://www.daisy.org/pipeline/modules/braille/css-utils/library.xpl"/>
    <p:import href="propagate-page-break.xpl"/>
    <p:import href="shift-obfl-marker.xpl"/>
    <p:import href="make-obfl-pseudo-elements.xpl"/>
    
    <p:declare-step type="pxi:recursive-parse-stylesheet-and-make-pseudo-elements">
        <p:input port="source"/>
        <p:output port="result" sequence="true"/>
        <css:parse-stylesheet>
            <p:documentation>
                Make css:page, css:volume, css:after, css:before, css:footnote-call, css:duplicate,
                css:alternate, css:_obfl-on-toc-start, css:_obfl-on-volume-start,
                css:_obfl-on-volume-end and css:_obfl-on-toc-end attributes.
            </p:documentation>
        </css:parse-stylesheet>
        <css:parse-properties properties="flow">
            <p:documentation>
                Make css:flow attributes.
            </p:documentation>
        </css:parse-properties>
        <p:choose>
            <p:when test="//*/@css:before|
                          //*/@css:after|
                          //*/@css:duplicate|
                          //*/@css:alternate|
                          //*/@css:footnote-call|
                          //*/@css:_obfl-on-toc-start|
                          //*/@css:_obfl-on-volume-start|
                          //*/@css:_obfl-on-volume-end|
                          //*/@css:_obfl-on-toc-end">
                <css:make-pseudo-elements>
                    <p:documentation>
                        Make css:before, css:after, css:duplicate, css:alternate and
                        css:footnote-call pseudo-elements from css:before, css:after, css:duplicate,
                        css:alternate and css:footnote-call attributes.
                    </p:documentation>
                </css:make-pseudo-elements>
                <pxi:make-obfl-pseudo-elements>
                    <p:documentation>
                        Make css:_obfl-on-toc-start, css:_obfl-on-volume-start,
                        css:_obfl-on-volume-end and css:_obfl-on-toc-end pseudo-element documents.
                    </p:documentation>
                </pxi:make-obfl-pseudo-elements>
                <p:for-each>
                    <pxi:recursive-parse-stylesheet-and-make-pseudo-elements/>
                </p:for-each>
            </p:when>
            <p:otherwise>
                <p:rename match="@css:_obfl-on-toc-start-ref" new-name="css:_obfl-on-toc-start"/>
                <p:rename match="@css:_obfl-on-volume-start-ref" new-name="css:_obfl-on-volume-start"/>
                <p:rename match="@css:_obfl-on-volume-end-ref" new-name="css:_obfl-on-volume-end"/>
                <p:rename match="@css:_obfl-on-toc-end-ref" new-name="css:_obfl-on-toc-end"/>
            </p:otherwise>
        </p:choose>
    </p:declare-step>
    
    <p:add-xml-base/>
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
    
    <css:parse-properties properties="display render-table-by table-header-policy">
        <p:documentation>
            Make css:display, css:render-table-by and css:table-header-policy attributes.
        </p:documentation>
    </css:parse-properties>
    
    <css:render-table-by>
        <p:documentation>
            Layout tables as lists.
        </p:documentation>
    </css:render-table-by>
    
    <pxi:recursive-parse-stylesheet-and-make-pseudo-elements>
        <p:documentation>
            Make css:page and css:volume attributes, css:after, css:before, css:duplicate,
            css:alternate and css:footnote-call pseudo-elements, and css:_obfl-on-toc-start,
            css:_obfl-on-volume-start, css:_obfl-on-volume-end and css:_obfl-on-toc-end
            pseudo-element documents.
        </p:documentation>
    </pxi:recursive-parse-stylesheet-and-make-pseudo-elements>
    
    <p:for-each>
        <css:parse-properties properties="content string-set counter-reset counter-set counter-increment -obfl-marker">
            <p:documentation>
                Make css:content, css:string-set, css:counter-reset, css:counter-set,
                css:counter-increment and css:_obfl-marker attributes.
            </p:documentation>
        </css:parse-properties>
        <css:eval-string-set>
            <p:documentation>
                Evaluate css:string-set attributes.
            </p:documentation>
        </css:eval-string-set>
    </p:for-each>
    
    <p:wrap-sequence wrapper="_"/>
    <css:parse-content>
        <p:documentation>
            Make css:string, css:text, css:content and css:counter elements from css:content
            attributes. <!-- depends on make-pseudo-element -->
        </p:documentation>
    </css:parse-content>
    <p:filter select="/_/*"/>
    
    <p:group>
        <p:documentation>
            Split into a sequence of flows.
        </p:documentation>
        <p:for-each>
            <css:parse-properties properties="flow">
                <p:documentation>
                    Make css:flow attributes.
                </p:documentation>
            </css:parse-properties>
        </p:for-each>
        <p:split-sequence test="/*[not(@css:flow)]" name="_1"/>
        <p:wrap wrapper="_" match="/*"/>
        <css:flow-into name="_2">
            <p:documentation>
                Extract named flows based on css:flow attributes and place anchors (css:id
                attributes) in the normal flow.
            </p:documentation>
        </css:flow-into>
        <p:filter select="/_/*" name="_3"/>
        <p:identity>
            <p:input port="source">
                <p:pipe step="_3" port="result"/>
                <p:pipe step="_2" port="flows"/>
                <p:pipe step="_1" port="not-matched"/>
            </p:input>
        </p:identity>
    </p:group>
    
    <css:label-targets name="label-targets">
        <p:documentation>
            Make css:id attributes. <!-- depends on parse-content -->
        </p:documentation>
    </css:label-targets>
    
    <css:eval-target-content>
        <p:documentation>
            Evaluate css:content elements. <!-- depends on parse-content and label-targets -->
        </p:documentation>
    </css:eval-target-content>
    
    <p:for-each>
        <css:parse-properties properties="white-space display list-style-type page-break-before page-break-after volume-break-before">
            <p:documentation>
                Make css:white-space, css:display, css:list-style-type, css:page-break-before,
                css:page-break-after and css:volume-break-before attributes.
            </p:documentation>
        </css:parse-properties>
        <css:preserve-white-space>
            <p:documentation>
                Make css:white-space elements from css:white-space attributes.
            </p:documentation>
        </css:preserve-white-space>
        <p:add-attribute match="*[@css:display='-obfl-toc']" attribute-name="css:_obfl-toc" attribute-value="_">
            <p:documentation>
                Mark display:-obfl-toc elements.
            </p:documentation>
        </p:add-attribute>
        <p:add-attribute match="*[@css:display='-obfl-toc']" attribute-name="css:display" attribute-value="block">
            <p:documentation>
                Treat display:-obfl-toc as block.
            </p:documentation>
        </p:add-attribute>
        <css:make-table-grid>
            <p:documentation>
                Create table grid structures from HTML/DTBook tables.
            </p:documentation>
        </css:make-table-grid>
        <css:make-boxes>
            <p:documentation>
                Make css:box elements based on css:display and css:list-style-type attributes. <!--
                depends on flow-into, label-targets and make-table-grid -->
            </p:documentation>
        </css:make-boxes>
        <!--
            FIXME: this may create unncessesary empty blocks in result OBFL
        -->
        <p:rename match="css:_[not(child::node()) and @css:id]" new-name="css:box">
            <p:documentation>
                In order to keep the positions of anchors precise, rename anchors in the form of
                empty css:_ elements to inline boxes, because otherwise they would be moved by
                css:shift-id. <!-- depends on flow-into and make-boxes -->
            </p:documentation>
        </p:rename>
        <p:add-attribute match="css:box[not(@type)]" attribute-name="type" attribute-value="inline"/>
        <p:group>
            <p:documentation>
                Move css:render-table-by, css:_obfl-table-col-spacing, css:_obfl-table-row-spacing
                and css:_obfl-preferred-empty-space attributes to 'table' css:box elements.
            </p:documentation>
            <css:parse-properties properties="-obfl-table-col-spacing -obfl-table-row-spacing -obfl-preferred-empty-space"/>
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
            <p:delete match="*[not(self::css:box[@type='table'])]/@css:render-table-by"/>
            <p:delete match="*[not(self::css:box[@type='table'])]/@css:_obfl-table-col-spacing"/>
            <p:delete match="*[not(self::css:box[@type='table'])]/@css:_obfl-table-row-spacing"/>
            <p:delete match="*[not(self::css:box[@type='table'])]/@css:_obfl-preferred-empty-space"/>
        </p:group>
    </p:for-each>
    
    <css:eval-counter exclude-counters="page">
        <p:documentation>
            Evaluate css:counter elements. <!-- depends on label-targets, parse-content and
            make-boxes -->
        </p:documentation>
    </css:eval-counter>
    
    <css:flow-from>
        <p:documentation>
            Evaluate css:flow elements. <!-- depends on parse-content and eval-counter -->
        </p:documentation>
    </css:flow-from>
    
    <css:eval-target-text>
        <p:documentation>
            Evaluate css:text elements. <!-- depends on label-targets and parse-content -->
        </p:documentation>
    </css:eval-target-text>
    
    <p:for-each>
        <css:make-anonymous-inline-boxes>
            <p:documentation>
                Wrap/unwrap with inline css:box elements.
            </p:documentation>
        </css:make-anonymous-inline-boxes>
    </p:for-each>
    
    <p:group>
        <p:documentation>
            Split flows into sections.
        </p:documentation>
        <p:for-each>
            <css:parse-counter-set counters="page">
                <p:documentation>
                    Make css:counter-set-page attributes.
                </p:documentation>
            </css:parse-counter-set>
            <p:delete match="/*[@css:flow]//*/@css:volume|
                             /*[@css:flow]//*/@css:counter-set-page|
                             //css:box[@type='table']//*/@css:page-break-before|
                             //css:box[@type='table']//*/@css:page-break-after|
                             //css:box[@type='table']//*/@css:page|
                             //css:box[@type='table']//*/@css:volume|
                             //css:box[@type='table']//*/@css:counter-set-page|
                             //*[@css:obfl-toc]//*/@css:page-break-before|
                             //*[@css:obfl-toc]//*/@css:page-break-after">
                <p:documentation>
                     Don't support 'volume' and 'counter-set: page' within named flows or
                     tables. Don't support 'page' within tables. Don't support 'page-break-before'
                     and 'page-break-after' within tables or '-obfl-toc' elements.
                </p:documentation>
            </p:delete>
            <css:split split-before="*[@css:page or @css:volume or @css:counter-set-page]|
                                     css:box[@type='block' and @css:page-break-before='right']|
                                     css:box[@type='block' and @css:volume-break-before='always']|
                                     css:box[@type='table']"
                       split-after="*[@css:page or @css:volume]|
                                    css:box[@type='block' and @css:page-break-after='right']|
                                    css:box[@type='table']">
                <p:documentation>
                    Split before and after css:page attributes, before css:counter-set-page
                    attributes, before and after css:volume attributes, before and after tables,
                    before css:page-break-before attributes with value 'right', after
                    css:page-break-after attributes with value 'right', and before
                    css:volume-break-before attributes with value 'always'. <!-- depends on
                    make-boxes -->
                </p:documentation>
            </css:split>
        </p:for-each>
        <p:for-each>
            <p:group>
                <p:documentation>
                    Move css:page, css:counter-set-page and css:volume attributes to css:_ root
                    element.
                </p:documentation>
                <p:wrap wrapper="css:_" match="/*[not(@css:flow)]"/>
                <p:label-elements match="/*[descendant::*/@css:page]" attribute="css:page"
                                  label="(descendant::*/@css:page)[last()]"/>
                <p:label-elements match="/*[descendant::*/@css:counter-set-page]" attribute="css:counter-set-page"
                                  label="(descendant::*/@css:counter-set-page)[last()]"/>
                <p:label-elements match="/*[descendant::*/@css:volume]" attribute="css:volume"
                                  label="(descendant::*/@css:volume)[last()]"/>
                <p:delete match="/*//*/@css:page"/>
                <p:delete match="/*//*/@css:counter-set-page"/>
                <p:delete match="/*//*/@css:volume"/>
            </p:group>
            <p:rename match="css:box[@type='inline' and not(@css:id)]
                             [matches(string(.), '^[\s&#x2800;]*$') and
                             not(descendant::css:white-space or
                             descendant::css:string or
                             descendant::css:counter or
                             descendant::css:text or
                             descendant::css:content or
                             descendant::css:leader or
                             descendant::css:custom-func)]"
                      new-name="css:_">
                <p:documentation>
                    Delete empty inline boxes (possible side effect of css:split), except if they
                    have a css:id attribute (as a result of css:flow-into).
                </p:documentation>
            </p:rename>
            <p:delete match="css:_/@type"/>
        </p:for-each>
        <p:group>
            <p:documentation>
                Repeat css:string-set attributes at the beginning of sections as css:string-entry.
            </p:documentation>
            <p:split-sequence test="/*[not(@css:flow)]" name="_1"/>
            <css:repeat-string-set name="_2"/>
            <p:identity>
                <p:input port="source">
                    <p:pipe step="_2" port="result"/>
                    <p:pipe step="_1" port="not-matched"/>
                </p:input>
            </p:identity>
        </p:group>
        <css:shift-string-set>
            <p:documentation>
                Move css:string-set attributes. <!-- depends on make-anonymous-inline-boxes -->
            </p:documentation>
        </css:shift-string-set>
        <pxi:shift-obfl-marker>
            <p:documentation>
                Move css:_obfl-marker attributes. <!-- depends on make-anonymous-inline-boxes -->
            </p:documentation>
        </pxi:shift-obfl-marker>
        <css:shift-id>
            <p:documentation>
                Move css:id attributes to css:box elements.
            </p:documentation>
        </css:shift-id>
    </p:group>
    
    <p:for-each>
        <p:unwrap match="css:_[not(@css:*) and parent::*]" name="unwrap-css-_">
            <p:documentation>
                All css:_ elements except for root elements, top-level elements in named flows (with
                css:anchor attribute), and empty elements with a css:string-set or css:_obfl-marker
                attribute within a css:box element should be gone now. <!-- depends on shift-id and
                shift-string-set -->
            </p:documentation>
        </p:unwrap>
        <css:make-anonymous-block-boxes>
            <p:documentation>
                Wrap inline css:box elements in block css:box elements where necessary. <!-- depends
                on unwrap css:_ -->
            </p:documentation>
        </css:make-anonymous-block-boxes>
    </p:for-each>
    
    <p:split-sequence test="//css:box"/>
    
    <p:for-each>
        <css:parse-properties properties="margin-left margin-right margin-top margin-bottom
                                          padding-left padding-right padding-top padding-bottom
                                          border-left border-right border-top border-bottom text-indent">
            <p:documentation>
                Make css:margin-left, css:margin-right, css:margin-top, css:margin-bottom,
                css:padding-left, css:padding-right, css:padding-top and css:padding-bottom,
                css:border-left, css:border-right, css:border-top, css:border-bottom and
                css:text-indent attributes.
            </p:documentation>
        </css:parse-properties>
        <css:adjust-boxes>
            <p:documentation>
                <!-- depends on make-anonymous-block-boxes -->
            </p:documentation>
        </css:adjust-boxes>
        <css:new-definition>
            <p:input port="definition">
                <p:document href="obfl-css-definition.xsl"/>
            </p:input>
        </css:new-definition>
        <p:delete match="css:box[@type='block']
                                [matches(string(.), '^[\s&#x2800;]*$') and
                                 not(descendant::css:white-space or
                                     descendant::css:string or
                                     descendant::css:counter or
                                     descendant::css:text or
                                     descendant::css:content or
                                     descendant::css:leader or
                                     descendant::css:custom-func)]
                                //text()">
            <p:documentation>
                Remove text nodes from block boxes with no line boxes.
            </p:documentation>
        </p:delete>
        <pxi:propagate-page-break>
            <p:documentation>
                Resolve css:page-break-before="avoid" and css:page-break-after="always".
                <!-- depends on make-anonymous-block-boxes -->
            </p:documentation>
        </pxi:propagate-page-break>
        <p:group>
            <p:documentation>
                Move css:page-break-after="avoid" to last descendant block. (TODO: move to
                pxi:propagate-page-break?)
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
        <p:group>
            <p:documentation>
                Move volume-break-before="always" to the outermost ancestor-or-self block box.
            </p:documentation>
            <p:add-attribute match="/css:_/css:box[@type='block'][descendant::css:box[@type='block'][@css:volume-break-before='always']]"
                             attribute-name="css:volume-break-before"
                             attribute-value="always"/>
            <p:delete match="/css:_/css:box//css:box/@css:volume-break-before"/>
        </p:group>
        <p:choose>
            <p:documentation>
                Delete css:margin-top from first block and move css:margin-top of other blocks to
                css:margin-bottom of their preceding block.
            </p:documentation>
            <p:when test="$skip-margin-top-of-page='true'">
                <p:delete match="css:box
                                   [@type='block']
                                   [@css:margin-top]
                                   [not(preceding::*)]
                                   [not(ancestor::*[@css:border-top])]
                                 /@css:margin-top"/>
                <p:label-elements match="css:box
                                           [@type='block']
                                           [following-sibling::*[1]
                                              [some $self in . satisfies
                                                 $self/descendant-or-self::*
                                                   [@css:margin-top][1]
                                                   [not(preceding::* intersect $self/descendant::*)]
                                                   [not((ancestor::* intersect $self/descendant-or-self::*)[@css:border-top])]]]"
                                  attribute="css:_margin-bottom_"
                                  label="max((0,
                                              @css:margin-bottom/number(),
                                              following::*[@css:margin-top][1]/@css:margin-top/number()))"/>
                <p:delete match="@css:margin-top[(preceding::css:box[@type='block']
                                                    except ancestor::*/preceding-sibling::*/descendant::*)
                                                   [last()][@css:_margin-bottom_]]"/>
                <p:rename match="@css:_margin-bottom_" new-name="css:margin-bottom"/>
            </p:when>
            <p:otherwise>
                <p:identity/>
            </p:otherwise>
        </p:choose>
    </p:for-each>
    
    <p:split-sequence test="//css:box[@css:border-top|
                                      @css:border-bottom|
                                      @css:margin-top|
                                      @css:margin-bottom|
                                      descendant::text()|
                                      descendant::css:white-space|
                                      descendant::css:string|
                                      descendant::css:counter|
                                      descendant::css:text|
                                      descendant::css:content|
                                      descendant::css:leader|
                                      descendant::css:custom-func]">
        <p:documentation>
            Remove empty sections.
        </p:documentation>
    </p:split-sequence>
    
    <!-- for debug info -->
    <p:for-each><p:identity/></p:for-each>
    
    <p:xslt template-name="start">
        <p:input port="stylesheet">
            <p:document href="css-to-obfl.xsl"/>
        </p:input>
        <p:with-param name="braille-translator-query" select="if ($text-transform='auto') then '' else $text-transform">
            <p:empty/>
        </p:with-param>
        <p:with-param name="duplex" select="$duplex">
            <p:empty/>
        </p:with-param>
    </p:xslt>
    
    <!--
        add <marker class="foo/prev"/>
    -->
    <p:insert match="obfl:marker[not(matches(@class,'^indicator/|/entry$'))]
                                [some $class in @class satisfies preceding::obfl:marker[@class=$class]]" position="before">
        <p:input port="insertion">
          <p:inline><marker xmlns="http://www.daisy.org/ns/2011/obfl"/></p:inline>
        </p:input>
    </p:insert>
    <p:label-elements match="obfl:marker[not(@class)]" attribute="class" label="concat(following-sibling::obfl:marker[1]/@class,'/prev')"/>
    <p:label-elements match="obfl:marker[not(@value)]" attribute="value"
                      label="string-join(for $class in @class return (preceding::obfl:marker[concat(@class,'/prev')=$class])[last()]/@value,'')"/>
    
    <!--
        because empty marker values would be regarded as absent in BrailleFilterImpl
    -->
    <p:add-attribute match="obfl:marker[@value='']" attribute-name="value" attribute-value="&#x200B;"/>
    
    <!--
        move table-of-contents elements to the right place
    -->
    <p:group>
        <p:identity name="_1"/>
        <p:insert match="/obfl:obfl/obfl:volume-template[not(preceding-sibling::obfl:volume-template)]" position="before">
            <p:input port="insertion" select="//obfl:toc-sequence/obfl:table-of-contents">
                <p:pipe step="_1" port="result"/>
            </p:input>
        </p:insert>
        <p:delete match="obfl:toc-sequence/obfl:table-of-contents"/>
    </p:group>
    
</p:declare-step>
