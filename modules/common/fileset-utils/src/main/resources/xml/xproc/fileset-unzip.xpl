<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:fileset-unzip" xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:d="http://www.daisy.org/ns/pipeline/data"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc" version="1.0" name="main" xmlns:letex="http://www.le-tex.de/namespace" xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
    xmlns:cx="http://xmlcalabash.com/ns/extensions">
    
    <p:documentation>A step for extracting information out of ZIP archives. Compatible with EXProcs `pxp:unzip` and Calabash's `cx:unzip`, but with additional options that default to the same behaviour as `pxp:unzip`.</p:documentation>
    
    <!-- Standard pxp:unzip output -->
    <p:output port="result" sequence="true" primary="true">    <!-- either a c:zipfile manifest if no file is specified, the file contents if load-to-memory is true, or the empty sequence otherwise -->
        <p:pipe port="result" step="result"/>
    </p:output>
    
    <!-- Additional secondary output -->
    <p:output port="fileset">                                  <!-- a d:fileset which either contains only the file loaded (if file is specified), or the equivalent of the zip manifest otherwise -->
        <p:pipe port="result" step="fileset"/>
    </p:output>
    
    <!-- Standard pxp:unzip options -->
    <p:option name="href" required="true"/>                    <!-- anyURI (file) -->
    <p:option name="file"/>                                    <!-- string (zip entry) -->
    <p:option name="content-type"/>                            <!-- string (for instance 'application/octet-stream') -->
    
    <!-- Additional optional options -->
    <p:option name="unzipped-basedir"/>                        <!-- anyURI (directory; no default) -->
    <p:option name="load-to-memory"/>                          <!-- boolean (default: true if file specified, false otherwise) -->
    <p:option name="store-to-disk" select="'false'"/>          <!-- boolean (default: false) -->
    <p:option name="overwrite" select="'false'"/>              <!-- boolean (default: false) -->
    
    <p:import href="fileset-library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/zip-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    
    <!--
        The value of the href option must be an IRI. It is a dynamic error if the document so identified does not exist or cannot be read.
        
        The value of the file option, if specified, must be the fully qualified path-name of a document in the archive. It is dynamic error if the value specified does not identify a file in the archive.
        
        It is a dynamic error if the store-to-disk option is true and the value of the unzipped-basedir option does not identify a directory. 
        
        If the file option is specified and the load-to-memory option is either true or not specified, then the selected file in the archive is extracted and returned on the result port:
        
            If the content-type is not specified, or if an XML content type is specified, the file is parsed as XML and returned. It is a dynamic error if the file is not well-formed XML.
            If the content-type specified is not an XML content type, the file is base64 encoded and returned in a single c:data element.
            If the unzipped-basedir option is set, then the base URI of the result document is set to the unzipped-basedir + the filename of the zip entry (filename, not full zip entry path)
        
        If the file option is specified and the load-to-memory option is false, then a table of contents for the archive containing only the given file entry is returned on the result port.
        
        If the file option is not specified and the load-to-memory option is either false or not specified, then a table of contents for all the files in the archive is returned on the result port.
        
        If the file option is not specified and the load-to-memory option is true, then all the files in the ZIP archive are extracted and returned on the result port:
        
        If the content-type is not specified, or if an XML content type is specified, each file is parsed as XML and returned. It is a dynamic error if a file is not well-formed XML.
        If the content-type specified is not an XML content type, each file is base64 encoded and returned in c:data elements.
        If the unzipped-basedir option is set, then the base URI of the result document is set to
            the unzipped-basedir + the zip entry name (the full zip entry path, not just the filename, note that this is different from the case where file is specified)
        
        If the file option is not specified, then a d:fileset is returned on the fileset output port (equivalent of the zip manifest, regardless of the value of load-to-memory).
    -->
    
    <p:choose name="choose">
        <p:when test="p:value-available('load-to-memory')">
            <p:output port="result" sequence="true" primary="true">
                <p:pipe port="result" step="choose.inner"/>
            </p:output>
            <p:output port="otherwise">
                <p:pipe port="otherwise" step="choose.inner"/>
            </p:output>
            <p:choose name="choose.inner">
                <p:when test="p:value-available('file') and $load-to-memory = 'false'">
                    <p:output port="result" sequence="true" primary="true"/>
                    <p:output port="otherwise">
                        <p:inline exclude-inline-prefixes="#all">
                            <c:otherwise>false</c:otherwise>
                        </p:inline>
                    </p:output>
                    
                    <!-- single file; don't load to memory, return manifest instead containing only the requested file -->
                    
                    <px:unzip>
                        <p:with-option name="href" select="$href"/>
                    </px:unzip>
                    <p:delete>
                        <p:with-option name="match" select="concat('/*/*[not(@name=''',replace($file,'''',''''''),''')]')"/>
                    </p:delete>
                    
                </p:when>
                <p:when test="not(p:value-available('file')) and $load-to-memory = 'true'">
                    <p:output port="result" sequence="true" primary="true"/>
                    <p:output port="otherwise">
                        <p:inline exclude-inline-prefixes="#all">
                            <c:otherwise>false</c:otherwise>
                        </p:inline>
                    </p:output>
                    
                    <!-- all files; load to memory -->
                    
                    <px:unzip>
                        <p:with-option name="href" select="$href"/>
                    </px:unzip>
                    <p:delete match="/*/*[ends-with(@name,'/')]"/>
                    <p:for-each>
                        <p:iteration-source select="/*/*"/>
                        <p:variable name="entry-name" select="/*/@name"/>
                        <p:choose>
                            <p:when test="p:value-available('content-type')">
                                <px:unzip>
                                    <p:with-option name="href" select="$href"/>
                                    <p:with-option name="file" select="$entry-name"/>
                                    <p:with-option name="content-type" select="$content-type"/>
                                </px:unzip>
                            </p:when>
                            <p:otherwise>
                                <px:unzip content-type="application/octet-stream">
                                    <p:with-option name="href" select="$href"/>
                                    <p:with-option name="file" select="$entry-name"/>
                                </px:unzip>
                            </p:otherwise>
                        </p:choose>
                        <p:choose>
                            <p:when test="p:value-available('unzipped-basedir')">
                                <p:variable name="basedir" select="if (ends-with($unzipped-basedir,'/')) then $unzipped-basedir else concat($unzipped-basedir,'/')"/>
                                <p:add-attribute match="/*" attribute-name="xml:base">
                                    <p:with-option name="attribute-value" select="resolve-uri($entry-name, $basedir)"/>
                                </p:add-attribute>
                            </p:when>
                            <p:otherwise>
                                <p:identity/>
                            </p:otherwise>
                        </p:choose>
                    </p:for-each>
                </p:when>
                <p:otherwise>
                    <p:output port="result" sequence="true" primary="true"/>
                    <p:output port="otherwise">
                        <p:inline exclude-inline-prefixes="#all">
                            <c:otherwise>true</c:otherwise>
                        </p:inline>
                    </p:output>
                    
                    <p:identity>
                        <p:input port="source">
                            <p:empty/>
                        </p:input>
                    </p:identity>
                </p:otherwise>
            </p:choose>
        </p:when>
        <p:otherwise>
            <p:output port="result" sequence="true" primary="true"/>
            <p:output port="otherwise">
                <p:inline exclude-inline-prefixes="#all">
                    <c:otherwise>true</c:otherwise>
                </p:inline>
            </p:output>
            
            <p:identity>
                <p:input port="source">
                    <p:empty/>
                </p:input>
            </p:identity>
        </p:otherwise>
    </p:choose>
    <p:choose>
        <p:xpath-context>
            <p:pipe port="otherwise" step="choose"/>
        </p:xpath-context>
        <p:when test="not(/*/text()='true')">
            <p:identity/>
        </p:when>
        <p:otherwise>
            <!-- default pxp:unzip behavior -->
            
            <p:choose>
                <p:when test="p:value-available('file') and p:value-available('content-type')">
                    <px:unzip>
                        <p:with-option name="href" select="$href"/>
                        <p:with-option name="file" select="$file"/>
                        <p:with-option name="content-type" select="$content-type"/>
                    </px:unzip>
                </p:when>
                <p:when test="p:value-available('file')">
                    <px:unzip>
                        <p:with-option name="href" select="$href"/>
                        <p:with-option name="file" select="$file"/>
                    </px:unzip>
                </p:when>
                <p:otherwise>
                    <px:unzip>
                        <p:with-option name="href" select="$href"/>
                    </px:unzip>
                </p:otherwise>
            </p:choose>
            
            <p:choose>
                <p:when test="p:value-available('file') and p:value-available('unzipped-basedir')">
                    <!-- set basedir for unzipped file -->
                    <p:variable name="basedir" select="if (ends-with($unzipped-basedir,'/')) then $unzipped-basedir else concat($unzipped-basedir,'/')"/>
                    <p:add-attribute match="/*" attribute-name="xml:base">
                        <p:with-option name="attribute-value" select="resolve-uri(replace($file,'.*/',''), $basedir)"/>
                    </p:add-attribute>
                </p:when>
                <p:otherwise>
                    <p:identity/>
                </p:otherwise>
            </p:choose>
            
        </p:otherwise>
    </p:choose>
    <p:identity name="result"/>
    <p:sink/>
    
    <p:choose>
        <p:when test="$store-to-disk = 'true' and not(p:value-available('unzipped-basedir'))">
            <px:error code="PZU001" message="When store-to-disk='true' then unzipped-basedir must also be defined"/>
            
        </p:when>
        <p:when test="$store-to-disk = 'true'">
            <!-- store first, then create fileset -->
            
            <p:variable name="basedir" select="if (ends-with($unzipped-basedir,'/')) then $unzipped-basedir else concat($unzipped-basedir,'/')"/>
            
            <p:choose>
                <p:when test="p:step-available('letex:unzip')">
                    <!-- unzip a single or multiple files directly to disk -->
                    
                    <p:choose>
                        <p:when test="p:value-available('file')">
                            <letex:unzip>
                                <p:with-option name="zip" select="$href"/>
                                <p:with-option name="dest-dir" select="$basedir"/>
                                <p:with-option name="overwrite" select="if ($overwrite = 'true') then 'yes' else 'no'"/>
                                <p:with-option name="file" select="$file"/>
                            </letex:unzip>
                        </p:when>
                        <p:otherwise>
                            <letex:unzip>
                                <p:with-option name="zip" select="$href"/>
                                <p:with-option name="dest-dir" select="$basedir"/>
                                <p:with-option name="overwrite" select="if ($overwrite = 'true') then 'yes' else 'no'"/>
                            </letex:unzip>
                        </p:otherwise>
                    </p:choose>
                    <p:rename match="/*" new-name="d:fileset"/>
                    <p:rename match="/*/*" new-name="d:file"/>
                    <p:label-elements match="/*/d:file" attribute="href"
                                      label="string-join(for $t in tokenize(@name,'/') return encode-for-uri($t),'/')"/>
                    <p:delete match="/*/*/@name"/>
                    <p:viewport match="/*/*">
                        <p:add-attribute match="/*" attribute-name="original-href">
                            <p:with-option name="attribute-value" select="resolve-uri(/*/@href, base-uri(.))"/>
                        </p:add-attribute>
                    </p:viewport>
                    
                </p:when>
                <p:otherwise>
                    <!-- load files to memory, then store to disk -->
                    
                    
                    <p:choose>
                        <p:when test="p:value-available('file')">
                            <!-- a single file -->
                            
                            <p:add-attribute match="/*/*" attribute-name="name">
                                <p:input port="source">
                                    <p:inline exclude-inline-prefixes="#all">
                                        <c:zipfile xmlns:c="http://www.w3.org/ns/xproc-step">
                                            <c:file/>
                                        </c:zipfile>
                                    </p:inline>
                                </p:input>
                                <p:with-option name="attribute-value" select="$file"/>
                            </p:add-attribute>
                            
                        </p:when>
                        <p:otherwise>
                            <!-- all files -->
                            
                            <px:unzip>
                                <p:with-option name="href" select="$href"/>
                            </px:unzip>
                        </p:otherwise>
                    </p:choose>
                    <px:message severity="WARN" message="letex:unzip is not available (you are probably running this outside of the DAISY Pipeline 2 framework); unzipping will be slow for big ZIP files"/>
                    
                    <p:for-each>
                        <p:iteration-source select="/*/c:file"/>
                        
                        <p:variable name="entry-name" select="/*/@name"/>
                        <p:variable name="target-href" select="resolve-uri(if (p:value-available('file')) then replace($entry-name,'.*/','') else $entry-name, $basedir)"/>
                        
                        <p:try>
                            <p:group>
                                <px:info>
                                    <p:with-option name="href" select="$target-href"/>
                                </px:info>
                            </p:group>
                            <p:catch>
                                <p:identity>
                                    <p:input port="source">
                                        <p:empty/>
                                    </p:input>
                                </p:identity>
                            </p:catch>
                        </p:try>
                        <p:count/>
                        
                        <p:choose>
                            <p:when test="/*/text() = '0' or $overwrite = 'true'">
                                <px:unzip content-type="application/octet-stream">
                                    <p:with-option name="href" select="$href"/>
                                    <p:with-option name="file" select="$entry-name"/>
                                </px:unzip>
                                <p:store name="store-file" cx:decode="true" encoding="base64">
                                    <p:with-option name="href" select="$target-href"/>
                                </p:store>
                                <p:rename match="/*" new-name="d:file">
                                    <p:input port="source">
                                        <p:pipe port="result" step="store-file"/>
                                    </p:input>
                                </p:rename>
                                <p:add-attribute match="/*" attribute-name="original-href">
                                    <p:with-option name="attribute-value" select="/*/text()"/>
                                </p:add-attribute>
                                <p:delete match="/*/text()"/>
                                <p:add-attribute match="/*" attribute-name="href">
                                    <p:with-option name="attribute-value" select="replace(/*/@original-href,'.*/','')"/>
                                </p:add-attribute>
                                <p:wrap-sequence wrapper="d:fileset"/>
                                <p:add-attribute match="/*" attribute-name="xml:base">
                                    <p:with-option name="attribute-value" select="replace(/*/*/@original-href,'[^/]+$','')"/>
                                </p:add-attribute>
                                
                            </p:when>
                            <p:otherwise>
                                <!-- don't overwrite existing file -->
                                
                                <px:fileset-create>
                                    <p:with-option name="base" select="replace($target-href,'[^/]+$','')"/>
                                </px:fileset-create>
                                <px:fileset-add-entry>
                                    <p:with-option name="href" select="replace($target-href,'.*/','')"/>
                                </px:fileset-add-entry>
                            </p:otherwise>
                        </p:choose>
                    </p:for-each>
                    <px:fileset-join/>
                    
                </p:otherwise>
            </p:choose>
            
        </p:when>
        <p:when test="p:value-available('file')">
            <!-- single-file fileset -->
            
            <p:choose>
                <p:when test="p:value-available('unzipped-basedir')">
                    <p:variable name="basedir" select="if (ends-with($unzipped-basedir,'/')) then $unzipped-basedir else concat($unzipped-basedir,'/')"/>
                    <px:fileset-create>
                        <p:with-option name="base" select="$basedir"/>
                    </px:fileset-create>
                </p:when>
                <p:otherwise>
                    <px:fileset-create/>
                </p:otherwise>
            </p:choose>
            
            <px:fileset-add-entry>
                <p:with-option name="href" select="replace($file,'.*/','')"/>
            </px:fileset-add-entry>
            
        </p:when>
        <p:otherwise>
            <!-- create fileset by reading zip manifest -->
            
            <px:unzip>
                <p:with-option name="href" select="$href"/>
            </px:unzip>
            <p:rename match="/*" new-name="d:fileset"/>
            <p:choose>
                <p:when test="p:value-available('unzipped-basedir')">
                    <p:variable name="basedir" select="if (ends-with($unzipped-basedir,'/')) then $unzipped-basedir else concat($unzipped-basedir,'/')"/>
                    <p:add-attribute match="/*" attribute-name="xml:base">
                        <p:with-option name="attribute-value" select="$basedir"/>
                    </p:add-attribute>
                </p:when>
                <p:otherwise>
                    <p:identity/>
                </p:otherwise>
            </p:choose>
            <p:delete match="/*/@*[not(name()='xml:base')]"/>
            <p:delete match="/*/*[ends-with(@name,'/')]"/>
            <p:viewport match="/*/*">
                <p:rename match="/*" new-name="d:file"/>
                <p:add-attribute match="/*" attribute-name="href">
                    <p:with-option name="attribute-value"
                                   select="string-join(for $t in tokenize(/*/@name,'/') return encode-for-uri($t),'/')"/>
                </p:add-attribute>
            </p:viewport>
            <p:delete match="/*/*/@*[not(name()='href')]"/>
            
        </p:otherwise>
    </p:choose>
    <p:identity name="fileset"/>
    <p:sink/>
    
</p:declare-step>
