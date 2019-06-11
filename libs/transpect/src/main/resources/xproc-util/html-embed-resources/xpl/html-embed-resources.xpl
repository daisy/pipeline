<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
  xmlns:c="http://www.w3.org/ns/xproc-step" 
  xmlns:cx="http://xmlcalabash.com/ns/extensions"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:tr="http://transpect.io"
  xmlns:html="http://www.w3.org/1999/xhtml"
  xmlns:svg="http://www.w3.org/2000/svg"
  xmlns="http://transpect.io"
  version="1.0"
  name="html-embed-resources"
  type="tr:html-embed-resources">
  
  <p:serialization port="result" method="xhtml" omit-xml-declaration="false"/>
  
  <p:documentation xmlns:html="http://www.w3.org/1999/xhtml">
    <p>This step tries to embed external resources such as images, 
      CSS and JavaScript as data URI, as XML or as plain text into the HTML document.</p>
    <p>Consider the example below.</p>
    <pre>&lt;html xmlns="http://www.w3.org/1999/xhtml">
  &lt;head>
    &lt;title/>
  &lt;/head>
  &lt;body>
    &lt;div>
      &lt;img alt="a blue square" src="image.png" />
    &lt;/div>
  &lt;/body>
&lt;/html></pre>
    <p>After processing the HTML, the image is embedded as data URI.</p>
    <pre>&lt;html xmlns="http://www.w3.org/1999/xhtml">
  &lt;head>
    &lt;title/>
  &lt;/head>
  &lt;body>
    &lt;div>
      &lt;img alt="a blue square" src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAMAAAC6sdbXAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJ&#xA;bWFnZVJlYWR5ccllPAAAAyJpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdp&#xA;bj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6&#xA;eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMy1jMDExIDY2LjE0&#xA;NTY2MSwgMjAxMi8wMi8wNi0xNDo1NjoyNyAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJo&#xA;dHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlw&#xA;dGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAv&#xA;IiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RS&#xA;ZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpD&#xA;cmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENTNiAoV2luZG93cykiIHhtcE1NOkluc3RhbmNl&#xA;SUQ9InhtcC5paWQ6NjExNUU3Q0RFNkQ1MTFFNUE4MThFMjY3QjgwODYwQ0UiIHhtcE1NOkRvY3Vt&#xA;ZW50SUQ9InhtcC5kaWQ6NjExNUU3Q0VFNkQ1MTFFNUE4MThFMjY3QjgwODYwQ0UiPiA8eG1wTU06&#xA;RGVyaXZlZEZyb20gc3RSZWY6aW5zdGFuY2VJRD0ieG1wLmlpZDo2MTE1RTdDQkU2RDUxMUU1QTgx&#xA;OEUyNjdCODA4NjBDRSIgc3RSZWY6ZG9jdW1lbnRJRD0ieG1wLmRpZDo2MTE1RTdDQ0U2RDUxMUU1&#xA;QTgxOEUyNjdCODA4NjBDRSIvPiA8L3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1w&#xA;bWV0YT4gPD94cGFja2V0IGVuZD0iciI/PjJf70IAAAAGUExURQCe4AAAAB0uYYYAAAAOSURBVHja&#xA;YmDABwACDAAAHgABzCCyiwAAAABJRU5ErkJggg==&#xA;" />
    &lt;/div>
  &lt;/body>
&lt;/html></pre> 
  </p:documentation>
  
  <p:input port="source" primary="true">
    <p:documentation xmlns:html="http://www.w3.org/1999/xhtml">
      <p>expects an XHTML document</p>
    </p:documentation>
    <p:empty/>
  </p:input>
  
  <p:input port="catalog">
    <p:documentation>If it is a <code>&lt;catalog></code> document in the namespace
        <code>urn:oasis:names:tc:entity:xmlns:xml:catalog</code>, it will be used for catalog resolution of URIs that start with
      'http'.</p:documentation>
    <p:inline>
      <nodoc/>
    </p:inline>
  </p:input>
  
  <p:output port="result" primary="true">
    <p:documentation xmlns:html="http://www.w3.org/1999/xhtml">
      <p>provides the XHTML document with embedded resources</p>
    </p:documentation>
  </p:output>
  
  <p:option name="exclude" select="''">
    <p:documentation>Space-separated list of tokens. Available tokens are: image video script style audio object #all.
    (Question: support font as a category on its own?)</p:documentation>
  </p:option>

  <p:option name="max-base64-encoded-size-kb" select="1000">
      <p:documentation>If this limit in KiloByte is exceeded, the resource will not be embedded.</p:documentation>
  </p:option>
  
  <p:option name="unavailable-resource-message" select="'no'">
    <p:documentation>When this option is set to 'yes', a message is inserted for each unavailable resource.</p:documentation>
  </p:option>
  
  <p:option name="debug" select="'no'"/>
  <p:option name="fail-on-error" select="'true'"/>
  
  <p:import href="http://xmlcalabash.com/extension/steps/library-1.0.xpl"/>
  <p:import href="http://transpect.io/xproc-util/file-uri/xpl/file-uri.xpl"/>
  
  <p:variable name="top-level-base-uri" select="( /*/@xml:base, base-uri(/*) )[1]"/>
  
  <p:variable name="suppress-video" select="tokenize($exclude, '\s+')[. = ('#all', 'video')]"/>
  <p:variable name="suppress-audio" select="tokenize($exclude, '\s+')[. = ('#all', 'audio')]"/>
  <p:variable name="suppress-image" select="tokenize($exclude, '\s+')[. = ('#all', 'image')]" />
  <p:variable name="suppress-script" select="tokenize($exclude, '\s+')[. = ('#all', 'script')]"/>
  <p:variable name="suppress-style" select="tokenize($exclude, '\s+')[. = ('#all', 'style')]"/>
  <p:variable name="suppress-object" select="tokenize($exclude, '\s+')[. = ('#all', 'object')]"/>
  
  <p:viewport match="*[local-name() = ('img', 'audio', 'video', 'script')][@src]
                     |html:object[@data]
                     |html:link[@rel eq 'stylesheet'][@href]
                     |svg:image[@xlink:href]" 
              name="viewport">
    
    <p:variable name="local-base-uri" select="(base-uri(.), $top-level-base-uri)[1]"/>
    <p:variable name="href-attribute" select="replace(
                                                      (*[local-name() = ('img', 'audio', 'video', 'script')]/@src, 
                                                       html:object/@data, 
                                                       html:link/@href, 
                                                       svg:image/@xlink:href)[1],
                                                       '\\', '/')"/>
    <p:variable name="href" 
      select="if(starts-with($href-attribute, 'data:'))  (: leave data URIs as-is :)
              then $href-attribute
              else resolve-uri(if(matches($href-attribute, '^(http[s]?|file)://?')) (: resolve regular URIs :) 
                   then $href-attribute
                   else concat(replace($local-base-uri, '^(.+/).+$', '$1'), $href-attribute),
                   $local-base-uri)"/>
    
    <p:choose>
      <p:when test="exists(
                        /*[local-name() = ('img'[$suppress-image],
                                           'audio'[$suppress-audio], 
                                           'video'[$suppress-video], 
                                           'script'[$suppress-script])][@src]
                      | /html:object[@data][$suppress-object]
                      | /html:link[@rel eq 'stylesheet'][@href][$suppress-style]
                      | /svg:image[@xlink:href][$suppress-image]
                    )">
        <p:documentation>Suppress embedding for elements meeting these conditions. Unfortunately, the conditions could not
        be specified in the p:viewport match attribute because options and variables seem to be inaccessible there.</p:documentation>
        <p:identity/>
      </p:when>
      <p:when test="normalize-space($href-attribute) and not(starts-with($href-attribute, 'data:'))">
        
        <p:try>
          <p:group>
            
            <p:choose>
              <p:when test="$debug eq 'yes'">
                <cx:message>
                  <p:with-option name="message" select="'embed: ', $href"/>
                </cx:message>
              </p:when>
              <p:otherwise>
                <p:identity/>
              </p:otherwise>
            </p:choose>
            
            <!-- * 
                 * resolve URIs with tr:file-uri, construct and perform http-request
                 * -->
            
            <tr:file-uri fetch-http="true" name="file-uri">
              <p:with-option name="filename" select="$href"/>
              <p:input port="catalog">
                <p:pipe port="catalog" step="html-embed-resources"/>
              </p:input>
              <p:input port="resolver">
                <p:document href="http://transpect.io/xslt-util/xslt-based-catalog-resolver/xsl/resolve-uri-by-catalog.xsl"/>
              </p:input>
            </tr:file-uri>
            
            <p:sink/>
            
            <p:add-attribute attribute-name="href" match="/c:request" name="construct-http-request">
              <p:with-option name="attribute-value" select="/c:result/@local-href">
                <p:pipe port="result" step="file-uri"/>
              </p:with-option>
              <p:input port="source">
                <p:inline>
                  <c:request method="GET" detailed="true"/>
                </p:inline>
              </p:input>
            </p:add-attribute>
            
            <p:http-request name="http-request"/>
            
            <p:choose name="test-for-max-file-size">
              <p:variable name="base64-str-size" select="string-length(//c:body[1]) * 4 div 3 div 1000"/>
              <p:when test="xs:float($base64-str-size) &gt; xs:float($max-base64-encoded-size-kb)">
                
                <cx:message>
                  <p:with-option name="message" select="'[WARNING] File not embedded. Base64 encoded string size (', 
                                                        round-half-to-even($base64-str-size, 2) , 
                                                        ') exceeds limit of ', $max-base64-encoded-size-kb, ' KB: ', $href"/>
                </cx:message>
                
                <p:sink/>
                
                <p:identity>
                  <p:input port="source">
                    <p:pipe port="current" step="viewport"/>
                  </p:input>
                </p:identity>
                
              </p:when>
              <p:otherwise>
                
                <p:identity/>
                
              </p:otherwise>
            </p:choose>
            
            <p:add-attribute attribute-name="xml:base" name="add-xmlbase" match="//c:body" cx:depends-on="http-request">
              <p:with-option name="attribute-value" select="$href"/>
            </p:add-attribute>
            
            <!-- * 
                 * include the base64 string as data-URI or as text node
                 * -->
            
            <p:choose>
              <p:when test="html:img|html:audio|html:video|html:script|html:object|svg:image|html:picture">
                <p:xpath-context>
                  <p:pipe port="current" step="viewport"/>
                </p:xpath-context>
                <p:variable name="content-type" 
                  select="if(matches(//c:body[1]/@xml:base, '\.svg$', 'i'))
                          then 'image/svg+xml'
                          else replace(//c:body[1]/@content-type, '^(.+/.+);.+$', '$1')"/>
                <p:variable name="encoding" select="//c:body/@encoding"/>
                
                <p:string-replace match="*[local-name() = ('img', 'audio', 'video', 'script')]/@src
                                         |html:object/@data
                                         |svg:image/@xlink:href
                                         |html:video/html:source/@src
                                         |html:audio/@src
                                         |html:picture/html:source/@srcset" cx:depends-on="add-xmlbase">
                  <p:input port="source">
                    <p:pipe port="current" step="viewport"/>
                  </p:input>
                  <p:with-option name="replace" select="concat('''', 'data:', $content-type, ';', $encoding, ',', //c:body, '''')">
                    <p:pipe port="result" step="add-xmlbase"/>
                  </p:with-option>
                </p:string-replace>
                
              </p:when>
              
              <p:otherwise>
                
                <p:insert match="html:style" position="first-child" name="insert-style" cx:depends-on="add-xmlbase">
                  <p:input port="source">
                    <p:inline>
                      <style xmlns="http://www.w3.org/1999/xhtml"></style>
                    </p:inline>
                  </p:input>
                  <p:input port="insertion">
                    <p:pipe port="result" step="add-xmlbase"/>
                  </p:input>
                </p:insert>
                
                <!--  *
                      * process css resources
                      * -->
                
                <p:try name="try-extract-references-from-css">
                  <p:group>
                    <p:xslt name="extract-references-from-css">
                      <p:with-param name="base-uri" select="$href"/>
                      <p:input port="stylesheet">
                        <p:document href="../xsl/css-embed-resources.xsl"/>
                      </p:input>
                      <p:with-param name="suppress-image" select="$suppress-image"/>
                    </p:xslt>
                    
                    <p:viewport match="tr:data-uri" cx:depends-on="extract-references-from-css" name="viewport-data-uri">
                      <p:variable name="data-uri" select="tr:data-uri/@href"/>
                      <p:variable name="mime-type" select="tr:data-uri/@mime-type"/>
                      
                      <p:choose>
                        <p:when test="$debug eq 'yes'">
                          <cx:message>
                            <p:with-option name="message" select="'embed: ', $data-uri"/>
                          </cx:message>
                        </p:when>
                        <p:otherwise>
                          <p:identity/>
                        </p:otherwise>
                      </p:choose>
                      
                      <p:add-attribute attribute-name="href" match="/c:request" name="construct-http-request-css">
                        <p:with-option name="attribute-value" select="$data-uri"/>
                        <p:input port="source">
                          <p:documentation>We request it as application/octet-stream so that we are certain that it will be
                            base64 encoded, even if it were SVG or the like. (When reading resources from a Jar, we received
                          SVG as XML here, as opposed to when reading from file system – strange.))</p:documentation>
                          <p:inline>
                            <c:request method="GET" detailed="true" override-content-type="application/octet-stream"/>
                          </p:inline>
                        </p:input>
                      </p:add-attribute>
                      
                      <p:http-request name="http-request-css-resource" cx:depends-on="construct-http-request-css"/>
                      
                      <p:string-replace match="tr:data-uri/text()">
                        <p:input port="source">
                          <p:pipe port="current" step="viewport-data-uri"/>
                        </p:input>
                        <p:with-option name="replace" select="concat('''', 'data:', $mime-type, ';', c:body/@encoding, ',', replace(c:body, '&#xa;', ''), '''')">
                          <p:pipe port="result" step="http-request-css-resource"/>
                        </p:with-option>
                      </p:string-replace>
                      
                    </p:viewport>
                    
                    <p:unwrap match="html:style//tr:data-uri"/>
                    
                  </p:group>
                  <p:catch>
                    <p:identity>
                      <p:input port="source">
                        <p:pipe port="result" step="insert-style"/>
                      </p:input>
                    </p:identity>
                  </p:catch>
                </p:try>
                
                <p:unwrap match="html:style//c:body"/>
                
              </p:otherwise>
              
            </p:choose>
            
          </p:group>
          
          <!--  *
                * the try branch failed for any™ reason. Leave the reference as is
                * -->
          
          <p:catch>
            
            <p:choose>
              <p:when test="$fail-on-error eq 'true'">
                
                <p:error code="html-resource-embed-failed">
                  <p:input port="source">
                    <p:inline>
                      <c:error>Failed to embed HTML resource.</c:error>
                    </p:inline>
                  </p:input>
                </p:error>
                
              </p:when>
              <p:otherwise>
                
                <p:identity>
                  <p:input port="source">
                    <p:pipe port="current" step="viewport"/>
                  </p:input>
                </p:identity>
                
                <p:choose>
                  <p:when test="$unavailable-resource-message eq 'yes'">
                    
                    <p:xslt name="insert-unavailable-resource-message">
                      <p:input port="stylesheet">
                        <p:document href="../xsl/unavailable-resource-message.xsl"/>
                      </p:input>
                      <p:input port="parameters">
                        <p:empty/>
                      </p:input>
                    </p:xslt>
                    
                  </p:when>
                  <p:otherwise>
                    
                    <p:identity/>
                    
                  </p:otherwise>
                </p:choose>
                
                <cx:message>
                  <p:with-option name="message" select="'[WARNING] failed to embed file: ', $href"/>
                </cx:message>
                
              </p:otherwise>
            </p:choose>
            
          </p:catch>
        </p:try>
        
      </p:when>
      <p:otherwise>
        
        <p:identity>
          <p:input port="source">
            <p:pipe port="current" step="viewport"/>
          </p:input>
        </p:identity>
        
      </p:otherwise>
    </p:choose>
    
  </p:viewport>
  
</p:declare-step>
