<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                type="px:fileset-load" name="main"
                exclude-inline-prefixes="px">

  <p:input port="fileset" primary="true"/>
  <p:input port="in-memory" sequence="true">
    <p:empty/>
  </p:input>

  <p:output port="result.fileset">
    <p:pipe step="result.fileset" port="result"/>
  </p:output>
  <p:output port="result" sequence="true" primary="true">
    <p:pipe step="load" port="result"/>
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>The filtered and loaded fileset.</p>
      <p>All files are loaded into memory, unless if the "load-if-not-in-memory" option is set, then
      the "result" port will only contain documents that were already present in the "in-memory"
      input.</p>
      <p>"original-href" attributes are removed from the manifest.</p>
    </p:documentation>
  </p:output>

  <p:option name="href" select="''"/>
  <p:option name="media-types" select="''"/>
  <p:option name="not-media-types" select="''"/>
  <p:option name="fail-on-not-found" select="'false'"/>
  <p:option name="load-if-not-in-memory" select="'true'"/>

  <p:import href="fileset-library.xpl">
    <p:documentation>
      px:fileset-filter
      px:fileset-create
      px:fileset-add-entry
      px:fileset-join
    </p:documentation>
  </p:import>
  <p:import href="load-html.xpl">
    <p:documentation>
      pxi:load-html
    </p:documentation>
  </p:import>
  <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
    <p:documentation>
      px:info
      px:set-base-uri
      px:normalize-uri
      px:data
    </p:documentation>
  </p:import>
  <p:import href="http://www.daisy.org/pipeline/modules/zip-utils/library.xpl">
    <p:documentation>
      px:unzip
    </p:documentation>
  </p:import>
  <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
    <p:documentation>
      px:message
    </p:documentation>
  </p:import>

  <p:add-attribute match="/*" attribute-name="href">
    <p:with-option name="attribute-value" select="$href"/>
  </p:add-attribute>
  <p:add-attribute match="/*" attribute-name="media-types">
    <p:with-option name="attribute-value" select="$media-types"/>
  </p:add-attribute>
  <p:add-attribute match="/*" attribute-name="not-media-types">
    <p:with-option name="attribute-value" select="$not-media-types"/>
  </p:add-attribute>

  <p:choose>
    <p:when test="$href='' and $media-types='' and $not-media-types=''">
      <p:identity/>
    </p:when>
    <p:otherwise>
      <px:fileset-filter>
        <p:with-option name="href" select="$href"/>
        <p:with-option name="media-types" select="$media-types"/>
        <p:with-option name="not-media-types" select="$not-media-types"/>
      </px:fileset-filter>
    </p:otherwise>
  </p:choose>
  <px:fileset-join>
    <p:documentation>Normalize @href</p:documentation>
  </px:fileset-join>
  <p:identity name="filtered-normalized"/>
  <p:delete match="@original-href" name="result.fileset"/>
  <p:for-each>
    <p:iteration-source select="/*/*"/>
    <p:identity/>
  </p:for-each>
  <p:count limit="1" name="filtered.count"/>
  <p:identity>
    <p:input port="source">
      <p:pipe port="result" step="filtered-normalized"/>
    </p:input>
  </p:identity>

  <p:choose name="load">
    <p:when test="number(/*)&gt;0">
      <p:xpath-context>
        <p:pipe port="result" step="filtered.count"/>
      </p:xpath-context>
      <p:output port="result" sequence="true"/>
      <p:for-each>
        <p:output port="result" sequence="true"/>
        <p:iteration-source select="//d:file"/>
        <p:variable name="target" select="/*/resolve-uri(@href, base-uri(.))"/>
        <p:variable name="on-disk" select="/*/resolve-uri((@original-href,@href)[1], base-uri(.))"/>
        <p:variable name="media-type" select="/*/@media-type"/>
        <p:variable name="method" select="/*/@method"/>

        <p:choose>
          <p:xpath-context>
            <p:pipe port="result" step="fileset.in-memory"/>
          </p:xpath-context>

          <!-- from memory -->
          <p:when test="$target = //d:file/resolve-uri(@href,base-uri(.))">
            <p:split-sequence px:message="processing file from memory: {$target}" px:message-severity="DEBUG">
              <p:input port="source">
                <p:pipe port="in-memory" step="normalized"/>
              </p:input>
              <p:with-option name="test" select="concat('base-uri(/*)=&quot;',$target,'&quot;')">
                <p:empty/>
              </p:with-option>
            </p:split-sequence>
            <!-- take only the first -->
            <p:split-sequence test="position()=1"/>
          </p:when>

          <!-- not in memory, but don't load it from disk -->
          <p:when test="not($load-if-not-in-memory = 'true')">
            <p:sink/>
            <p:identity>
              <p:input port="source">
                <p:empty/>
              </p:input>
            </p:identity>
          </p:when>

          <!-- load file into memory (from disk, HTTP, etc) -->
          <p:otherwise>
            <p:try>
              <p:group>
                <px:message severity="DEBUG">
                  <p:with-option name="message" select="concat('loading ',$target,' from disk: ',$on-disk)"/>
                </px:message>
                <p:sink/>

                <p:choose>
                  <p:when test="starts-with($on-disk,'file:')">
                    <px:info>
                      <p:with-option name="href" select="replace(resolve-uri($on-disk, base-uri()), '^([^!]+)(!/.+)?$', '$1')">
                        <p:inline>
                          <doc/>
                        </p:inline>
                      </p:with-option>
                    </px:info>
                  </p:when>
                  <p:otherwise>
                    <p:identity>
                      <p:input port="source">
                        <p:empty/>
                      </p:input>
                    </p:identity>
                  </p:otherwise>
                </p:choose>
                <p:count name="file-exists"/>

                <p:choose>
                  <p:when test="number(.)=0 and starts-with($on-disk,'file:')">
                    <p:error code="XC0011">
                      <p:input port="source">
                        <p:inline>
                          <c:message>File not found.</c:message>
                        </p:inline>
                      </p:input>
                    </p:error>
                  </p:when>

                  <!-- Load from ZIP -->
                  <p:when test="contains($on-disk, '!/')">
                    <p:variable name="file" select="replace($on-disk, '^(jar:)?([^!]+)!/(.+)$', '$2')"/>
                    <p:variable name="path-in-zip" select="replace($on-disk, '^([^!]+)!/(.+)$', '$2')"/>
                    <p:sink/>
                    <p:xslt template-name="main">
                      <p:input port="source">
                        <p:empty/>
                      </p:input>
                      <p:input port="stylesheet">
                        <p:inline>
                          <xsl:stylesheet version="2.0" xmlns:pf="http://www.daisy.org/ns/pipeline/functions">
                            <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/uri-functions.xsl"/>
                            <xsl:param name="uri" required="yes"/>
                            <xsl:template name="main">
                              <c:result>
                                <xsl:value-of select="pf:unescape-uri($uri)"/>
                              </c:result>
                            </xsl:template>
                          </xsl:stylesheet>
                        </p:inline>
                      </p:input>
                      <p:with-param name="uri" select="$path-in-zip"/>
                    </p:xslt>
                    <p:group>
                      <p:variable name="escaped-path-in-zip" select="."/>
                      <p:sink/>
                      <p:choose px:message="Loading {$escaped-path-in-zip} from ZIP {$file}" px:message-severity="DEBUG">
                        <p:when test="$method='html' or ($method='' and $media-type='text/html')">
                          <!-- can not use px:unzip; use workaround instead -->
                          <pxi:load-html>
                            <p:with-option name="href" select="concat('jar:',$on-disk)"/>
                          </pxi:load-html>
                        </p:when>
                        <p:when test="$method='text' or ($method=''
                                                         and matches($media-type,'^text/')
                                                         and not(matches($media-type,'.*/xml$') or matches($media-type,'.*\+xml$')))">
                          <!-- can not use px:unzip; use workaround instead -->
                          <px:data content-type="text/plain; charset=utf-8">
                            <p:with-option name="href" select="concat('jar:',$on-disk)"/>
                          </px:data>
                        </p:when>
                        <p:otherwise>
                          <px:unzip>
                            <p:with-option name="href" select="$file"/>
                            <p:with-option name="file" select="$escaped-path-in-zip"/>
                            <p:with-option name="content-type" select="if ($method='xml') then 'application/xml'
                                                                       else if ($method='binary') then 'binary/octet-stream'
                                                                       else $media-type"/>
                          </px:unzip>
                          <px:set-base-uri>
                            <p:with-option name="base-uri" select="$target"/>
                          </px:set-base-uri>
                        </p:otherwise>
                      </p:choose>
                    </p:group>
                  </p:when>

                  <!-- Force HTML -->
                  <p:when test="$method='html'">
                    <pxi:load-html>
                      <p:with-option name="href" select="$on-disk"/>
                    </pxi:load-html>
                  </p:when>

                  <!-- Force XML -->
                  <p:when test="$method='xml'">
                    <p:try>
                      <p:group>
                        <p:load>
                          <p:with-option name="href" select="$on-disk"/>
                        </p:load>
                      </p:group>
                      <p:catch>
                        <px:message severity="WARN">
                          <p:input port="source">
                            <p:empty/>
                          </p:input>
                          <p:with-option name="message" select="concat('unable to load ',$on-disk,' as XML')"/>
                        </px:message>
                      </p:catch>
                    </p:try>
                  </p:when>

                  <!-- Force text -->
                  <p:when test="$method='text'">
                    <px:data content-type="text/plain; charset=utf-8">
                      <p:with-option name="href" select="$on-disk"/>
                    </px:data>
                  </p:when>

                  <!-- Force binary -->
                  <p:when test="$method='binary'">
                    <px:data content-type="binary/octet-stream">
                      <p:with-option name="href" select="$on-disk"/>
                    </px:data>
                  </p:when>

                  <!-- HTML -->
                  <p:when test="$media-type='text/html' or $media-type='application/xhtml+xml'">
                    <pxi:load-html>
                      <p:with-option name="href" select="$on-disk"/>
                    </pxi:load-html>
                  </p:when>

                  <!-- XML -->
                  <p:when test="matches($media-type,'.*(/|\+)xml$')">
                    <p:try>
                      <p:group>
                        <p:load>
                          <p:with-option name="href" select="$on-disk"/>
                        </p:load>
                      </p:group>
                      <p:catch>
                        <px:message severity="WARN">
                          <p:input port="source">
                            <p:empty/>
                          </p:input>
                          <p:with-option name="message" select="concat('unable to load ',$on-disk,' as XML; trying as text...')"/>
                        </px:message>
                        <px:data content-type="text/plain; charset=utf-8">
                          <p:with-option name="href" select="$on-disk"/>
                        </px:data>
                      </p:catch>
                    </p:try>
                  </p:when>

                  <!-- text -->
                  <p:when test="matches($media-type,'^text/')">
                    <px:data content-type="text/plain; charset=utf-8">
                      <p:with-option name="href" select="$on-disk"/>
                    </px:data>
                  </p:when>

                  <!-- binary -->
                  <p:otherwise>
                    <px:data content-type="binary/octet-stream">
                      <p:with-option name="href" select="$on-disk"/>
                    </px:data>
                  </p:otherwise>

                </p:choose>
                
                <p:choose>
                  <p:when test="not($on-disk=$target)">
                    <px:set-base-uri>
                      <p:with-option name="base-uri" select="$target"/>
                    </px:set-base-uri>
                  </p:when>
                  <p:otherwise>
                    <p:identity/>
                  </p:otherwise>
                </p:choose>
                
              </p:group>
              <p:catch name="catch">
                <!-- could not retrieve file from neither memory nor disk -->
                <p:variable name="file-not-found-message" select="concat('Could neither retrieve file from memory nor disk: ',$target)"/>
                <p:choose>
                  <p:when test="$fail-on-not-found='true'">
                    <p:in-scope-names name="vars"/>
                    <p:template>
                      <p:input port="template">
                        <p:inline>
                          <c:message><![CDATA[]]>{$file-not-found-message}<![CDATA[]]>&#xa;Cause: <c:cause/></c:message>
                        </p:inline>
                      </p:input>
                      <p:input port="source">
                        <p:empty/>
                      </p:input>
                      <p:input port="parameters">
                        <p:pipe step="vars" port="result"/>
                      </p:input>
                    </p:template>
                    <p:insert match="/*/c:cause" position="first-child" name="error">
                      <p:input port="insertion">
                        <p:pipe step="catch" port="error"/>
                      </p:input>
                    </p:insert>
                    <p:error code="PEZE00">
                      <p:input port="source">
                        <p:pipe port="result" step="error"/>
                      </p:input>
                    </p:error>
                  </p:when>
                  <p:otherwise>
                    <px:message severity="WARN">
                      <p:with-option name="message" select="$file-not-found-message"/>
                    </px:message>
                  </p:otherwise>
                </p:choose>
                <p:identity>
                  <p:input port="source">
                    <p:empty/>
                  </p:input>
                </p:identity>
              </p:catch>
            </p:try>
          </p:otherwise>
        </p:choose>
      </p:for-each>

    </p:when>
    <p:otherwise>
      <p:output port="result" sequence="true"/>
      <!-- no files matched filter criteria (or fileset empty) -->
      <p:variable name="file-not-found-message"
        select="if (not($href='')) then concat('File is not part of fileset: ',$href) else 'Fileset empty or no files matched filter criteria. No files loaded.'"/>
      <p:choose>
        <p:when test="not($href='') and $fail-on-not-found='true'">
          <p:in-scope-names name="vars"/>
          <p:template name="error">
            <p:input port="template">
              <p:inline>
                <c:message><![CDATA[]]>{$file-not-found-message}<![CDATA[]]></c:message>
              </p:inline>
            </p:input>
            <p:input port="source">
              <p:empty/>
            </p:input>
            <p:input port="parameters">
              <p:pipe step="vars" port="result"/>
            </p:input>
          </p:template>
          <p:error code="PEZE00">
            <p:input port="source">
              <p:pipe port="result" step="error"/>
            </p:input>
          </p:error>
        </p:when>
        <p:otherwise>
          <p:identity/>
        </p:otherwise>
      </p:choose>
      <p:identity>
        <p:input port="source">
          <p:empty/>
        </p:input>
      </p:identity>
    </p:otherwise>
  </p:choose>
  <p:sink/>

  <!-- URI normalization -->
  <px:fileset-create>
    <p:with-option name="base" select="base-uri(/*)">
      <p:pipe port="fileset" step="main"/>
    </p:with-option>
  </px:fileset-create>
  <px:message severity="DEBUG" message="Initialized in-memory fileset with xml:base=&quot;$1&quot;">
    <p:with-option name="param1" select="base-uri(/*)"/>
  </px:message>
  <p:identity name="fileset.in-memory-base"/>
  <p:sink/>
  <p:for-each name="normalized">
    <p:output port="in-memory" sequence="true">
        <p:pipe step="normalized.group" port="in-memory"/>
    </p:output>
    <p:output port="filesets" sequence="true" primary="true"/>

    <p:iteration-source>
      <p:pipe port="in-memory" step="main"/>
    </p:iteration-source>

    <!--
        - The base URI is computed based on the xml:base attribute if present. If it is a relative
          URI, it is resolved against the original base URI.
        - Normalize URI (e.g. "file:///" to "file:/")
    -->
    <px:normalize-uri name="normalize-uri">
      <p:with-option name="href" select="resolve-uri(base-uri(/*))"/>
    </px:normalize-uri>
    <p:group name="normalized.group">
      <p:output port="in-memory" sequence="true">
        <p:pipe step="normalized.in-memory" port="result"/>
      </p:output>
      <p:output port="filesets" sequence="true" primary="true">
        <p:pipe step="normalized.fileset" port="result"/>
      </p:output>
      <p:variable name="base-uri" select="string(/*)">
        <p:pipe step="normalize-uri" port="normalized"/>
      </p:variable>
      <p:variable name="base-uri-changed" select="not($base-uri=base-uri(/))"/>
  
      <px:fileset-add-entry name="normalized.fileset">
        <p:with-option name="href" select="$base-uri"/>
        <p:input port="source">
          <p:pipe port="result" step="fileset.in-memory-base"/>
        </p:input>
      </px:fileset-add-entry>

      <p:choose>
        <!--
            If URI was normalized in px:fileset-add-entry, adapt the actual base URI of the
            document. Also adapt the actual base URI if the value passed to px:fileset-add-entry was
            computed based on the xml:base attribute.
        -->
        <p:when test="/d:fileset/d:file/resolve-uri(@href, base-uri()) != $base-uri
                      or $base-uri-changed='true'">
          <px:set-base-uri>
            <p:input port="source">
              <p:pipe port="current" step="normalized"/>
            </p:input>
            <p:with-option name="base-uri" select="$base-uri"/>
          </px:set-base-uri>
        </p:when>
        <p:otherwise>
          <p:identity>
            <p:input port="source">
              <p:pipe port="current" step="normalized"/>
            </p:input>
          </p:identity>
        </p:otherwise>
      </p:choose>
      <p:identity name="normalized.in-memory"/>
      <p:sink/>
    </p:group>
  </p:for-each>

  <p:wrap-sequence wrapper="d:fileset"/>
  <p:choose>
    <p:when test="count(distinct-values(/*/*/base-uri())) = 1">
      <p:add-attribute match="/*" attribute-name="xml:base">
        <p:with-option name="attribute-value" select="/*/*[1]/base-uri()"/>
      </p:add-attribute>
      <p:unwrap match="/*/*"/>
    </p:when>
    <p:otherwise>
      <px:fileset-join>
        <p:input port="source">
          <p:pipe port="filesets" step="normalized"/>
        </p:input>
      </px:fileset-join>
    </p:otherwise>
  </p:choose>
  <p:identity name="fileset.in-memory"/>
  <p:sink/>

</p:declare-step>
