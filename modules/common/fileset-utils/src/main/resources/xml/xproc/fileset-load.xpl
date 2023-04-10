<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                type="px:fileset-load" name="main"
                exclude-inline-prefixes="px">

  <p:input port="fileset" primary="true"/>
  <p:input port="in-memory" sequence="true">
    <p:empty/>
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>The input fileset.</p>
    </p:documentation>
  </p:input>

  <p:output port="result.fileset">
    <p:pipe step="result.fileset" port="result"/>
  </p:output>
  <p:output port="result" sequence="true" primary="true">
    <p:pipe step="load" port="result"/>
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>The filtered and loaded fileset.</p>
      <p>Files are loaded into memory, unless a file can not be loaded and the
      "fail-on-not-found" option is not set.</p>
      <p>The fileset ("xml:base" and "href" attributes and base URIs of documents) is normalized.</p>
      <p>"original-href" attributes are removed from the manifest.</p>
    </p:documentation>
  </p:output>

  <p:output port="unfiltered.fileset">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>The unfiltered result.</p>
      <p>A copy of the source fileset but with all matched files (matched by the <code>href</code>,
      <code>media-types</code> and <code>not-media-types</code> options) loaded into memory.</p>
    </p:documentation>
    <p:pipe step="unfiltered" port="result.fileset"/>
  </p:output>
  <p:output port="unfiltered.in-memory" sequence="true">
    <p:pipe step="unfiltered" port="result.in-memory"/>
  </p:output>

  <p:option name="href" select="''"/>
  <p:option name="media-types" select="''"/>
  <p:option name="not-media-types" select="''"/>
  <p:option name="fail-on-not-found" select="'false'"/>
  <p:option name="detect-serialization-properties" cx:as="xs:boolean" select="false()">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>Whether to detect serialization properties of XML documents when loaded from disk. The
      properties are added as attributes to the <code>d:file</code>. The following properties are
      detected:</p>
      <ul>
          <li>doctype</li>
          <li>doctype-public</li>
          <li>doctype-system</li>
      </ul>
      <p>These attributes are expected not to be present in the input unless the file is already
      loaded into memory (if they are present they will not be overwritten).</p>
    </p:documentation>
  </p:option>

  <p:import href="fileset-library.xpl">
    <p:documentation>
      px:fileset-filter
      px:fileset-create
      px:fileset-add-entry
      px:fileset-join
      px:fileset-update
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
      px:read-doctype
    </p:documentation>
  </p:import>
  <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
    <p:documentation>
      px:message
    </p:documentation>
  </p:import>
  <cx:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl" type="application/xslt+xml">
    <p:documentation>
      pf:unescape-uri
      pf:file-exists
    </p:documentation>
  </cx:import>


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
  <p:delete match="@original-href"/>
  <p:choose>
    <p:when test="$detect-serialization-properties">
      <p:identity name="fileset-without-serialization-properties"/>
      <p:sink/>
      <px:fileset-join>
        <p:input port="source">
          <p:pipe step="fileset-without-serialization-properties" port="result"/>
          <p:pipe step="load" port="newly-loaded-files-with-doctype"/>
        </p:input>
      </px:fileset-join>
    </p:when>
    <p:otherwise>
      <p:identity/>
    </p:otherwise>
  </p:choose>
  <p:identity name="result.fileset"/>
  <p:sink/>

  <!--
      input fileset updated with result fileset
  -->
  <p:choose name="unfiltered" cx:pure="true">
    <p:when test="$href='' and $media-types='' and $not-media-types=''">
      <p:output port="result.fileset" primary="true"/>
      <p:output port="result.in-memory" sequence="true">
        <p:pipe step="load" port="result"/>
      </p:output>
      <p:identity>
        <p:input port="source">
          <p:pipe step="result.fileset" port="result"/>
        </p:input>
      </p:identity>
    </p:when>
    <p:otherwise>
      <p:output port="result.fileset" primary="true"/>
      <p:output port="result.in-memory" sequence="true">
        <p:pipe step="update" port="result.in-memory"/>
      </p:output>
      <px:fileset-update name="update">
        <p:input port="source.fileset">
          <p:pipe step="main" port="fileset"/>
        </p:input>
        <p:input port="source.in-memory">
          <p:pipe step="main" port="in-memory"/>
        </p:input>
        <p:input port="update.fileset">
          <p:pipe step="result.fileset" port="result"/>
        </p:input>
        <p:input port="update.in-memory">
          <p:pipe step="load" port="result"/>
        </p:input>
      </px:fileset-update>
    </p:otherwise>
  </p:choose>
  <p:sink/>

  <p:count limit="1">
    <p:input port="source" select="/*/*">
      <p:pipe step="filtered-normalized" port="result"/>
    </p:input>
  </p:count>
  <p:choose name="load">
    <p:when test="number(/*)&gt;0">
      <p:output port="result" primary="true" sequence="true"/>
      <p:output port="newly-loaded-files-with-doctype" sequence="true">
        <p:pipe step="for-each" port="newly-loaded-files-with-doctype"/>
      </p:output>
      <p:for-each name="for-each">
        <p:output port="result" primary="true" sequence="true"/>
        <p:output port="newly-loaded-files-with-doctype" sequence="true">
          <p:pipe step="choose" port="newly-loaded-files-with-doctype"/>
        </p:output>
        <p:iteration-source select="//d:file">
          <p:pipe step="filtered-normalized" port="result"/>
        </p:iteration-source>
        <p:variable name="target" select="/*/resolve-uri(@href, base-uri(.))"/>
        <p:variable name="media-type" select="/*/@media-type"/>
        <p:variable name="method" select="/*/@method"/>
        <p:variable name="exists-in-memory" cx:as="xs:boolean" select="$target=//d:file/resolve-uri(@href,base-uri(.))">
          <p:pipe step="fileset.in-memory" port="result"/>
        </p:variable>
        <p:choose name="choose">

          <!-- from memory -->
          <p:when test="$exists-in-memory">
            <p:output port="result" primary="true" sequence="true"/>
            <p:output port="newly-loaded-files-with-doctype" sequence="true">
              <p:empty/>
            </p:output>
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

          <!-- load file into memory (from disk, HTTP, etc) -->
          <p:otherwise>
            <p:output port="result" primary="true" sequence="true">
                <p:pipe step="newly-loaded" port="result"/>
            </p:output>
            <p:output port="newly-loaded-files-with-doctype" sequence="true">
              <p:pipe step="newly-loaded-files-with-doctype" port="result"/>
            </p:output>
            <p:variable name="href" select="replace(/*/resolve-uri((@original-href,@href)[1], base-uri(.)),'^(jar|bundle):','')"/>
            <p:try>
              <p:group>
                <p:identity px:message-severity="DEBUG" px:message="loading {$target} from disk {$href}"/>
                <p:choose>
                  <p:when test="starts-with($href,'file:') and not(pf:file-exists($href))">
                    <p:error code="XC0011">
                      <p:input port="source">
                        <p:inline>
                          <c:message>File not found.</c:message>
                        </p:inline>
                      </p:input>
                    </p:error>
                  </p:when>
                  <p:when test="matches($href,'^file:') and contains($href,'!/')">
                    <p:variable name="file" select="replace($href, '^([^!]+)!/(.+)$', '$2')"/>
                    <p:variable name="path-in-zip" select="replace($href, '^([^!]+)!/(.+)$', '$2')"/>
                    <p:variable name="escaped-path-in-zip" select="pf:unescape-uri($path-in-zip)"/>
                    <p:identity px:message="Loading {$escaped-path-in-zip} from ZIP {$file}" px:message-severity="DEBUG"/>
                  </p:when>
                  <p:otherwise>
                    <p:identity/>
                  </p:otherwise>
                </p:choose>
                <p:sink/>
                <p:choose>
                  <p:variable name="href-maybe-in-zip" select="if (matches($href,'^file:') and contains($href,'!/'))
                                                               then replace($href,'^file:','jar:file:')
                                                               else $href"/>

                  <!-- Force HTML -->
                  <p:when test="$method='html'">
                    <pxi:load-html>
                      <p:with-option name="href" select="$href-maybe-in-zip"/>
                    </pxi:load-html>
                  </p:when>

                  <!-- Force XML -->
                  <p:when test="$method='xml'">
                      <p:load>
                          <p:with-option name="href" select="$href-maybe-in-zip"/>
                      </p:load>
                  </p:when>

                  <!-- Force text -->
                  <p:when test="$method='text'">
                    <px:data content-type="text/plain; charset=utf-8">
                      <p:with-option name="href" select="$href-maybe-in-zip"/>
                    </px:data>
                  </p:when>

                  <!-- Force binary -->
                  <p:when test="$method='binary'">
                    <px:data content-type="binary/octet-stream">
                      <p:with-option name="href" select="$href-maybe-in-zip"/>
                    </px:data>
                  </p:when>

                  <!-- HTML -->
                  <p:when test="$media-type='text/html' or $media-type='application/xhtml+xml'">
                    <pxi:load-html>
                      <p:with-option name="href" select="$href-maybe-in-zip"/>
                    </pxi:load-html>
                  </p:when>

                  <!-- XML -->
                  <p:when test="matches($media-type,'.*(/|\+)xml$')">
                    <p:try>
                      <p:group>
                        <p:load>
                          <p:with-option name="href" select="$href-maybe-in-zip"/>
                        </p:load>
                      </p:group>
                      <p:catch>
                        <px:message severity="WARN">
                          <p:input port="source">
                            <p:empty/>
                          </p:input>
                          <p:with-option name="message" select="concat('unable to load ',$href,' as XML; trying as text...')"/>
                        </px:message>
                        <px:data content-type="text/plain; charset=utf-8">
                          <p:with-option name="href" select="$href-maybe-in-zip"/>
                        </px:data>
                      </p:catch>
                    </p:try>
                  </p:when>

                  <!-- text -->
                  <p:when test="matches($media-type,'^text/')">
                    <px:data content-type="text/plain; charset=utf-8">
                      <p:with-option name="href" select="$href-maybe-in-zip"/>
                    </px:data>
                  </p:when>

                  <!-- binary -->
                  <p:otherwise>
                    <px:data content-type="binary/octet-stream">
                      <p:with-option name="href" select="$href-maybe-in-zip"/>
                    </px:data>
                  </p:otherwise>
                </p:choose>

                <p:choose>
                  <p:when test="not($href=$target) or (matches($href,'^file:') and contains($href,'!/'))">
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
            <p:identity name="newly-loaded"/>
            <p:for-each>
              <p:choose>
                <p:when test="$detect-serialization-properties and not(exists(/c:data))">
                  <p:sink/>
                  <px:read-doctype>
                    <p:with-option name="href" select="$href"/>
                  </px:read-doctype>
                  <p:for-each>
                    <p:choose>
                      <p:when test="/*/@doctype-public and /*/@doctype-system">
                        <px:fileset-add-entry>
                          <p:input port="source.fileset">
                              <p:inline exclude-inline-prefixes="#all"><d:fileset/></p:inline>
                          </p:input>
                          <p:with-option name="href" select="$target"/>
                          <p:with-param port="file-attributes" name="doctype-public" select="/*/@doctype-public"/>
                          <p:with-param port="file-attributes" name="doctype-system" select="/*/@doctype-system"/>
                        </px:fileset-add-entry>
                      </p:when>
                      <p:when test="/*/@doctype-declaration">
                        <px:fileset-add-entry>
                          <p:input port="source.fileset">
                              <p:inline exclude-inline-prefixes="#all"><d:fileset/></p:inline>
                          </p:input>
                          <p:with-option name="href" select="$target"/>
                          <p:with-param port="file-attributes" name="doctype" select="/*/@doctype-declaration"/>
                        </px:fileset-add-entry>
                      </p:when>
                      <p:otherwise>
                        <p:identity>
                          <p:input port="source">
                            <p:empty/>
                          </p:input>
                        </p:identity>
                      </p:otherwise>
                    </p:choose>
                  </p:for-each>
                </p:when>
                <p:otherwise>
                  <p:sink/>
                  <p:identity>
                    <p:input port="source">
                      <p:empty/>
                    </p:input>
                  </p:identity>
                </p:otherwise>
              </p:choose>
            </p:for-each>
            <p:identity name="newly-loaded-files-with-doctype"/>
            <p:sink/>
          </p:otherwise>
        </p:choose>
      </p:for-each>
    </p:when>
    <p:otherwise>
      <p:output port="result" primary="true" sequence="true"/>
      <p:output port="newly-loaded-files-with-doctype" sequence="true">
        <p:empty/>
      </p:output>
      <!-- no files matched filter criteria (or fileset empty) -->
      <p:identity>
        <p:input port="source">
          <p:empty/>
        </p:input>
      </p:identity>
      <p:choose>
        <p:when test="not($href='') and $fail-on-not-found='true'">
          <p:variable name="file-not-found-message"
                      select="if (not($href=''))
                              then concat('File is not part of fileset: ',$href)
                              else 'Fileset empty or no files matched filter criteria. No files loaded.'"/>
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
        <p:pipe step="normalized.fileset" port="result.fileset"/>
      </p:output>
      <p:variable name="base-uri" select="string(/*)">
        <p:pipe step="normalize-uri" port="normalized"/>
      </p:variable>
      <p:variable name="base-uri-changed" cx:as="xs:string" select="not($base-uri=base-uri(/))"/>
  
      <px:fileset-add-entry name="normalized.fileset">
        <p:with-option name="href" select="$base-uri"/>
        <p:input port="source.fileset">
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
