<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns:epub="http://www.idpf.org/2007/ops"
                xmlns="http://www.idpf.org/2007/opf"
                xpath-default-namespace="http://www.idpf.org/2007/opf"
                exclude-result-prefixes="#all">

	<xsl:template match="item">
		<xsl:variable name="href" select="resolve-uri(@href,base-uri(.))"/>
		<xsl:variable name="doc" as="document-node()?" select="collection()[base-uri(/*)=$href]"/>
		<xsl:choose>
			<xsl:when test="exists($doc)">
				<xsl:copy>
					<xsl:sequence select="@* except @properties"/>
					<xsl:variable name="properties" as="xs:string*" select="tokenize(@properties,'\s+')[not(.='')]"/>
					<xsl:variable name="properties" as="xs:string*">
						<xsl:sequence select="$properties"/>
						<xsl:if test="distinct-values($doc//namespace::*)='http://www.w3.org/1998/Math/MathML'">
							<xsl:sequence select="'mathml'"/>
						</xsl:if>
						<xsl:if test="($doc//html:embed|//html:iframe)/@src/ends-with(.,'.svg') or
						              ($doc//html:embed|//html:object)/@type='image/svg+xml' or
						              distinct-values($doc//namespace::*)='http://www.w3.org/2000/svg'">
							<xsl:sequence select="'svg'"/>
						</xsl:if>
						<xsl:if test="$doc//*/@href[starts-with(.,'javascript:')] or
						              $doc//html:script/@type=('',
						                                       'text/javascript',
						                                       'text/ecmascript',
						                                       'text/javascript1.0',
						                                       'text/javascript1.1',
						                                       'text/javascript1.2',
						                                       'text/javascript1.3',
						                                       'text/javascript1.4',
						                                       'text/javascript1.5',
						                                       'text/jscript',
						                                       'text/livescript',
						                                       'text/x-javascript',
						                                       'text/x-ecmascript',
						                                       'application/x-javascript',
						                                       'application/x-ecmascript',
						                                       'application/javascript',
						                                       'application/ecmascript') or
						              $doc//*/@*/name()=('onabort',           'onerror',           'onpageshow',
						                                 'onafterprint',      'onfocus',           'onpause',
						                                 'onbeforeprint',     'onhashchange',      'onplay',
						                                 'onbeforeunload',    'oninput',           'onplaying',
						                                 'onblur',            'oninvalid',         'onpopstate',
						                                 'oncanplay',         'onkeydown',         'onprogress',
						                                 'oncanplaythrough',  'onkeypress',        'onratechange',
						                                 'onchange',          'onkeyup',           'onreset',
						                                 'onclick',           'onload',            'onresize',
						                                 'oncontextmenu',     'onloadeddata',      'onscroll',
						                                 'oncuechange',       'onloadedmetadata',  'onseeked',
						                                 'ondblclick',        'onloadstart',       'onseeking',
						                                 'ondrag',            'onmessage',         'onselect',
						                                 'ondragend',         'onmousedown',       'onshow',
						                                 'ondragenter',       'onmousemove',       'onstalled',
						                                 'ondragleave',       'onmouseout',        'onstorage',
						                                 'ondragover',        'onmouseover',       'onsubmit',
						                                 'ondragstart',       'onmouseup',         'onsuspend',
						                                 'ondrop',            'onmousewheel',      'ontimeupdate',
						                                 'ondurationchange',  'onoffline',         'onunload',
						                                 'onemptied',         'ononline',          'onvolumechange',
						                                 'onended',           'onpagehide',        'onwaiting'
						                                 )">
							<xsl:sequence select="'scripted'"/>
						</xsl:if>
						<xsl:if test="$doc//epub:switch">
							<xsl:sequence select="'switch'"/>
						</xsl:if>
						<xsl:if test="$doc//*/@src[contains(tokenize(.,'/')[1],':')]">
							<xsl:sequence select="'remote-resources'"/>
						</xsl:if>
					</xsl:variable>
					<xsl:if test="exists($properties)">
						<xsl:attribute name="properties" select="string-join(distinct-values($properties),' ')"/>
					</xsl:if>
					<xsl:apply-templates/>
				</xsl:copy>
			</xsl:when>
			<xsl:otherwise>
				<xsl:next-match/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>
