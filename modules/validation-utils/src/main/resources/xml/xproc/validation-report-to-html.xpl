<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:xhtml="http://www.w3.org/1999/xhtml"
                exclude-inline-prefixes="#all"
                name="validation-report-to-html" type="px:validation-report-to-html">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1>Validation Report to HTML</h1>
        <p>Combines a series of validation reports into one single HTML report.</p>
    </p:documentation>

    <!-- 
    input format is a sequence of these documents:
    http://code.google.com/p/daisy-pipeline/wiki/ValidationReportXML
    -->
    <p:input port="source" primary="true" sequence="true"/>
    <p:output port="result" primary="true" px:media-type="application/vnd.pipeline.report+xml"/>
    <p:option name="toc" required="false" select="'false'"/>
    
    <p:for-each name="convert-to-html">
        <p:output port="result" sequence="true"/>
        
        <p:iteration-source>
            <p:pipe port="source" step="validation-report-to-html"/>
        </p:iteration-source>
        
        <p:viewport match="d:expected | d:was">
            <p:escape-markup method="xml"/>
            <p:string-replace match="/*/text()" replace="replace(/*,'(\n)[\s\n]+\n','$1')"/>
        </p:viewport>
        <p:xslt name="htmlify-validation-report">
            <p:input port="parameters">
                <p:empty/>
            </p:input>
            <p:input port="stylesheet">
                <p:document href="../xslt/validation-report-to-html.xsl"/>
            </p:input>
        </p:xslt>
        
    </p:for-each>
    
    <p:insert position="last-child" match="//xhtml:body" name="assemble-html-report">
        <p:input port="source">
            <p:inline>
                <html xmlns="http://www.w3.org/1999/xhtml">
                    <head>
                        <title>Validation Results</title>
                        <style type="text/css"> 
                            body { 
                            font-family: helvetica; 
                            } 
                            pre.box { 
                            white-space: pre-wrap; /* css-3 */ 
                            white-space: -moz-pre-wrap; /*Mozilla, since 1999 */ 
                            white-space: -pre-wrap; /* Opera 4-6 */
                            white-space: -o-pre-wrap; /* Opera 7 */ 
                            word-wrap: break-word; /*Internet Explorer 5.5+ */ 
                            } 
                            li.error div { 
                            display: table; 
                            border: gray thin solid; 
                            padding: 5px; 
                            } 
                            li.error div h3 { 
                            display: table-cell; 
                            padding-right: 10px; 
                            font-size: smaller; 
                            } 
                            li.error div pre.box { 
                            display: table-cell; 
                            } 
                            li { 
                            padding-bottom: 15px; 
                            } 
                            #toc {
                            border-spacing: 0px;
                            }
                            #toc th, #toc td {
                            padding: 5px 30px 5px 10px;
                            border-spacing: 0px;
                            font-size: 90%;
                            margin: 0px;
                            }
                            #toc th, #toc td {
                            text-align: left;
                            border-top: 1px solid #f1f8fe;
                            border-bottom: 1px solid #cbd2d8;
                            border-right: 1px solid #cbd2d8;
                            }
                            
                            #toc tr:nth-child(odd) {
                            background-color: #e0e9f0;
                            }
                            #toc tr:nth-child(even), #toc thead th {
                            background-color: #e8eff5; !important;
                            }
                            
                        </style>
                    </head>
                    <body>
                        <div id="header">
                            <h1>Validation Results</h1>
                            <p id="datetime">@@</p>
                            <table id="toc">
                                <thead>
                                    <tr>
                                        <th>File name</th>
                                        <th>Validation results</th>
                                        <th>Type</th>
                                        <th>Link to XML report</th>
                                        <th>Link to Document</th>
                                    </tr>
                                </thead>
                                <tbody/>
                            </table>
                        </div>
                    </body>
                </html>
            </p:inline>
        </p:input>
        <p:input port="insertion">
            <p:pipe port="result" step="convert-to-html"/>
            <p:inline xmlns="http://www.w3.org/1999/xhtml">
                <script src="https://google-code-prettify.googlecode.com/svn/loader/run_prettify.js"> </script>
            </p:inline>
        </p:input>
    </p:insert>
    
    <p:choose>
        <p:when test="$toc eq 'true'">
            <p:for-each name="generate-document-index">
                <p:output port="result"/>
                
                <p:iteration-source select="//xhtml:div[@class='document-validation-report']"/>
                
                <p:variable name="section-id" select="*/@id"/>
                <p:variable name="document-name" select="*/d:data/d:document-info/d:document-name/text()"/>
                <p:variable name="document-type" select="*/d:data/d:document-info/d:document-type/text()"/>
                <p:variable name="document-path" select="*/d:data/d:document-info/d:document-path/text()"/>
                <p:variable name="report-path" select="*/d:data/d:document-info/d:report-path/text()"/>
                <p:variable name="error-count" select="*/d:data/d:document-info/d:error-count/text()"/>
                <p:identity>
                    <p:input port="source">
                        <p:inline>
                            <tr xmlns="http://www.w3.org/1999/xhtml">
                                <td class="filename">@@</td>
                                <td class="issues"><a href="@@"><span>@@</span> found</a></td>
                                <td class="filetype">@@</td>
                                <td class="reportpath"><a href="@@">XML report</a></td>
                                <td class="filepath"><a href="@@">Document</a></td>
                            </tr>
                        </p:inline>
                    </p:input>
                </p:identity>
                
                <p:string-replace match="xhtml:td[@class='filename']/text()">
                    <p:with-option name="replace" select="concat('&quot;', $document-name, '&quot;')"/>
                </p:string-replace>
                
                <p:string-replace match="xhtml:td[@class='issues']/xhtml:a/@href">
                    <p:with-option name="replace" select="concat('&quot;', '#', $section-id, '&quot;')"/>
                </p:string-replace>
                
                <p:choose>
                    <p:when test="$error-count = 1">
                        <p:string-replace match="xhtml:td[@class='issues']/xhtml:a/xhtml:span/text()">
                            <p:with-option name="replace" select="'&quot;1 issue&quot;'"/>
                        </p:string-replace>        
                    </p:when>
                    <p:otherwise>
                        <p:string-replace match="xhtml:td[@class='issues']/xhtml:a/xhtml:span/text()">
                            <p:with-option name="replace" select="concat('&quot;', $error-count, ' issues &quot;')"/>
                        </p:string-replace>
                    </p:otherwise>
                </p:choose>
                
                
                <p:string-replace match="xhtml:td[@class='filetype']/text()">
                    <p:with-option name="replace" select="concat('&quot;', $document-type, '&quot;')"/>
                </p:string-replace>
                
                <p:string-replace match="xhtml:td[@class='reportpath']/xhtml:a/@href">
                    <p:with-option name="replace" select="concat('&quot;', $report-path, '&quot;')"/>
                </p:string-replace>
                
                <p:string-replace match="xhtml:td[@class='filepath']/xhtml:a/@href">
                    <p:with-option name="replace" select="concat('&quot;', $document-path, '&quot;')"/>
                </p:string-replace>
                
            </p:for-each>   
            
            <p:insert match="xhtml:table[@id='toc']/xhtml:tbody" position="last-child">
                <p:input port="source">
                    <p:pipe port="result" step="assemble-html-report"/>
                </p:input>
                <p:input port="insertion">
                    <p:pipe port="result" step="generate-document-index"/>
                </p:input>
            </p:insert>
            
        </p:when>
        <p:otherwise>
            <p:delete match="xhtml:table[@id='toc']"></p:delete>
        </p:otherwise>
    </p:choose>
    
    <!-- remove the temporary data element inserted by validation-report-to-html.xsl -->
    <p:delete match="//xhtml:div[@class='document-validation-report']/d:data"/>
        
    
    <p:string-replace match="//*[@id='datetime']/text()">
        <p:with-option name="replace"
            select="concat('&quot;Generated on ', current-date(), ' at ', current-time(), '&quot;')"
        />
    </p:string-replace>
    
    
        
</p:declare-step>
