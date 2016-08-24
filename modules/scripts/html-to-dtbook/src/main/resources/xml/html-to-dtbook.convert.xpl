<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step version="1.0" name="html-to-dtbook.convert" type="px:html-to-dtbook-convert" xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc" xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal" xmlns:tmp="http://www.daisy.org/ns/pipeline/tmp" xmlns:d="http://www.daisy.org/ns/pipeline/data"
    xmlns:html="http://www.w3.org/1999/xhtml" xmlns:dtbook="http://www.daisy.org/z3986/2005/dtbook/" xpath-version="2.0" exclude-inline-prefixes="#all">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <!-- Script documentation -->
        <h1 px:role="name">HTML to DTBook</h1>
        <p px:role="desc">Converts an HTML document into DTBook</p>
    </p:documentation>

    <p:input port="fileset.in" primary="true">
        <p:documentation>A fileset referencing all resources to be converted. Contains references to the HTML file and any resources it references (images etc.).</p:documentation>
    </p:input>

    <p:input port="in-memory.in" sequence="true">
        <p:documentation>In-memory documents (HTML).</p:documentation>
    </p:input>

    <p:output port="fileset.out" primary="true">
        <p:documentation> A fileset containing references to all the DTBook files and any resources they reference (images etc.). The xml:base is also set with an absolute URI for each file, and is intended to represent the "original file", while the
            href can change during conversions to reflect the path and filename of the resource in the output fileset. </p:documentation>
        <p:pipe port="result" step="fileset"/>
    </p:output>

    <p:output port="in-memory.out" sequence="true">
        <p:documentation> A sequence of all the DTBook documents loaded from disk so that the DTBook conversion step does not depend on documents being stored on disk. This means that the conversion step can receive DTBook documents either through
            this step, or as a result from other conversion steps, allowing for easy chaining of scripts. </p:documentation>
        <p:pipe port="in-memory" step="convert"/>
    </p:output>

    <p:option name="output-dir" required="true" px:output="result" px:type="anyDirURI">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Output directory</h2>
            <p px:role="desc">Target directory for the resulting DTBook(s).</p>
        </p:documentation>
    </p:option>

    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/dtbook-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/html-utils/library.xpl"/>


    <p:variable name="top-uid" select="string-join(tokenize(replace(concat('',current-dateTime()),'[-:]',''),'[T\+\.]')[position() &lt;= 3],'-')"/>

    <p:for-each name="convert">
        <p:output port="in-memory">
            <p:pipe port="result" step="convert.in-memory"/>
        </p:output>
        <p:output port="fileset">
            <p:pipe port="result" step="convert.fileset"/>
        </p:output>
        <p:iteration-source>
            <p:pipe port="in-memory.in" step="html-to-dtbook.convert"/>
        </p:iteration-source>

        <p:variable name="base-uri" select="base-uri()"/>

        <!-- basic fix for h1-h6 hierarchy -->
        <p:choose>
            <p:when test="not(//html:h1)">
                <p:rename match="//html:h2" new-name="h1" new-namespace="http://www.w3.org/1999/xhtml"/>
                <p:rename match="//html:h3" new-name="h2" new-namespace="http://www.w3.org/1999/xhtml"/>
                <p:rename match="//html:h4" new-name="h3" new-namespace="http://www.w3.org/1999/xhtml"/>
                <p:rename match="//html:h5" new-name="h4" new-namespace="http://www.w3.org/1999/xhtml"/>
                <p:rename match="//html:h6" new-name="h5" new-namespace="http://www.w3.org/1999/xhtml"/>
            </p:when>
            <p:otherwise>
                <p:identity/>
            </p:otherwise>
        </p:choose>
        <p:choose>
            <p:when test="not(//html:h2)">
                <p:rename match="//html:h3" new-name="h2" new-namespace="http://www.w3.org/1999/xhtml"/>
                <p:rename match="//html:h4" new-name="h3" new-namespace="http://www.w3.org/1999/xhtml"/>
                <p:rename match="//html:h5" new-name="h4" new-namespace="http://www.w3.org/1999/xhtml"/>
                <p:rename match="//html:h6" new-name="h5" new-namespace="http://www.w3.org/1999/xhtml"/>
            </p:when>
            <p:otherwise>
                <p:identity/>
            </p:otherwise>
        </p:choose>
        <p:choose>
            <p:when test="not(//html:h3)">
                <p:rename match="//html:h4" new-name="h3" new-namespace="http://www.w3.org/1999/xhtml"/>
                <p:rename match="//html:h5" new-name="h4" new-namespace="http://www.w3.org/1999/xhtml"/>
                <p:rename match="//html:h6" new-name="h5" new-namespace="http://www.w3.org/1999/xhtml"/>
            </p:when>
            <p:otherwise>
                <p:identity/>
            </p:otherwise>
        </p:choose>
        <p:choose>
            <p:when test="not(//html:h4)">
                <p:rename match="//html:h5" new-name="h4" new-namespace="http://www.w3.org/1999/xhtml"/>
                <p:rename match="//html:h6" new-name="h5" new-namespace="http://www.w3.org/1999/xhtml"/>
            </p:when>
            <p:otherwise>
                <p:identity/>
            </p:otherwise>
        </p:choose>
        <p:choose>
            <p:when test="not(//html:h5)">
                <p:rename match="//html:h6" new-name="h5" new-namespace="http://www.w3.org/1999/xhtml"/>
            </p:when>
            <p:otherwise>
                <p:identity/>
            </p:otherwise>
        </p:choose>

        <p:rename match="html:frameset" new-name="div"/>
        <p:viewport match="html:iframe|html:frame">
            <p:rename match="/*" new-name="div"/>
            <p:delete match="/*/node()"/>
            <p:identity name="frame.div"/>
            <px:message>
                <p:with-option name="message" select="concat('found frame: ',p:resolve-uri(/*/@src,$base-uri))"/>
            </px:message>
            <p:try>
                <p:group>
                    <px:html-load name="frame.contents">
                        <p:with-option name="href" select="p:resolve-uri(/*/@src,$base-uri)"/>
                    </px:html-load>
                    <px:message>
                        <p:with-option name="message" select="concat('copying frame contents from: ',p:resolve-uri(/*/@src,$base-uri))"/>
                    </px:message>
                    <p:insert match="/*" position="last-child">
                        <p:input port="source">
                            <p:pipe port="result" step="frame.div"/>
                        </p:input>
                        <p:input port="insertion">
                            <p:pipe port="result" step="frame.contents"/>
                        </p:input>
                    </p:insert>
                </p:group>
                <p:catch>
                    <p:variable name="href" select="p:resolve-uri(/*/@src,$base-uri)"/>
                    <p:in-scope-names name="vars"/>
                    <p:template name="frame.link">
                        <p:input port="template">
                            <p:inline>
                                <a target="_blank" href="{$href}">{$href}</a>
                            </p:inline>
                        </p:input>
                        <p:input port="source">
                            <p:empty/>
                        </p:input>
                        <p:input port="parameters">
                            <p:pipe step="vars" port="result"/>
                        </p:input>
                    </p:template>
                    <px:message>
                        <p:with-option name="message" select="concat('linking to frame contents (target is not parsable HTML): ',$href)"/>
                    </px:message>
                    <p:insert match="/*" position="last-child">
                        <p:input port="source">
                            <p:pipe port="result" step="frame.div"/>
                        </p:input>
                        <p:input port="insertion">
                            <p:pipe port="result" step="frame.link"/>
                        </p:input>
                    </p:insert>
                </p:catch>
            </p:try>
        </p:viewport>

        <p:xslt>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
            <p:input port="stylesheet">
                <p:document href="http://www.daisy.org/pipeline/modules/html-utils/html5-upgrade.xsl"/>
            </p:input>
        </p:xslt>

        <p:xslt>
            <p:with-param name="uid" select="concat($top-uid,'-',p:iteration-position())"/>
            <p:with-param name="title" select="(/*/*[1]/html:title,/*/*[2]//html:h1,/*/*[2]//html:h2,/*/*[2]//html:h3,/*/*[2]//html:h4,/*/*[2]//html:h5,/*/*[2]//html:h6)[1]"/>
            <p:with-param name="cssURI" select="'dtbook.2005.basic.css'"/>
            <p:input port="stylesheet">
                <p:document href="xhtml2dtbook.xsl"/>
            </p:input>
        </p:xslt>
        <p:identity name="convert.dtbook.original-base"/>

        <px:message>
            <p:with-option name="message" select="concat(replace($base-uri,'.*/',''),' was converted from HTML to DTBook')"/>
        </px:message>

        <p:add-attribute match="/*" attribute-name="xml:base">
            <p:with-option name="attribute-value" select="concat(p:resolve-uri(replace(replace($base-uri,'^.*/([^/]*)$','$1'),'(.*)\.[^\.]*$','$1'), $output-dir),'.xml')"/>
        </p:add-attribute>
        <p:delete match="/*/@xml:base"/>
        <p:identity name="convert.in-memory"/>

        <px:dtbook-load>
            <p:input port="source">
                <p:pipe port="result" step="convert.dtbook.original-base"/>
            </p:input>
        </px:dtbook-load>
        <p:add-attribute match="//d:file[@href='dtbook.2005.basic.css']" attribute-name="original-href">
            <p:with-option name="attribute-value" select="p:resolve-uri('../css/dtbook.2005.basic.css',base-uri(/*))">
                <p:inline>
                    <irrelevant/>
                </p:inline>
            </p:with-option>
        </p:add-attribute>
        <p:group>
            <p:variable name="fileset-base" select="/*/@xml:base"/>
            <p:viewport match="/*/d:file">
                <p:choose>
                    <p:when test="not(/*/@original-href)">
                        <p:add-attribute match="/*" attribute-name="original-href">
                            <p:with-option name="attribute-value" select="p:resolve-uri(/*/@href, $fileset-base)"/>
                        </p:add-attribute>
                    </p:when>
                    <p:otherwise>
                        <p:identity/>
                    </p:otherwise>
                </p:choose>
            </p:viewport>
        </p:group>
        <p:add-attribute match="//d:file[@media-type='application/x-dtbook+xml']" attribute-name="href">
            <p:with-option name="attribute-value" select="base-uri(/*)">
                <p:pipe port="result" step="convert.in-memory"/>
            </p:with-option>
        </p:add-attribute>
        <p:add-attribute match="/d:fileset" attribute-name="xml:base">
            <p:with-option name="attribute-value" select="$output-dir"/>
        </p:add-attribute>
        <p:identity name="convert.fileset"/>

    </p:for-each>

    <px:fileset-join>
        <p:input port="source">
            <p:pipe port="fileset" step="convert"/>
        </p:input>
    </px:fileset-join>
    <p:identity name="fileset"/>

</p:declare-step>
