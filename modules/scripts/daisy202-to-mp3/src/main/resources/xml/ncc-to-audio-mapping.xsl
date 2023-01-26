<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="3.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:html="http://www.w3.org/1999/xhtml">

	<xsl:param name="smils" as="document-node(element(smil))*" required="yes"/> <!-- in reading order -->
	<xsl:param name="depth" as="xs:integer" required="no" select="3"/> <!-- positive number (1 means no folders, 2 means 1 level of folders) -->
	<xsl:param name="max-folder-name-length" as="xs:integer" required="no" select="58"/>
	<xsl:param name="max-file-name-length" as="xs:integer" required="no" select="185"/>

	<xsl:include href="http://www.daisy.org/pipeline/modules/common-utils/library.xsl"/>
	<xsl:include href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>
	<xsl:include href="http://www.daisy.org/pipeline/modules/smil-utils/clock-functions.xsl"/>

	<xsl:variable name="media-type" select="'audio/mpeg'"/>
	<xsl:variable name="file-extension" select="'mp3'"/>
	<xsl:variable name="smil-doc-index-from-base-uri" as="map(xs:string,xs:integer)">
		<xsl:map>
			<xsl:for-each select="$smils">
				<xsl:map-entry key="pf:normalize-uri(base-uri(.))" select="position()"/>
			</xsl:for-each>
		</xsl:map>
	</xsl:variable>

	<xsl:key name="id" match="par[@id]|seq[@id]|text[@id]" use="@id"/>

	<xsl:template match="/html:html">
		<d:fileset>
			<xsl:iterate select="html:body//*">
				<xsl:param name="from-smil-doc" as="xs:integer" select="0"/> <!-- index in $smils -->
				<xsl:param name="from-smil-elem" as="element()?" select="()"/> <!-- par|seq -->
				<xsl:param name="dest-file" as="xs:integer*" select="for $x in 1 to $depth return 0"/> <!-- (0,0,0,...) -->
				<xsl:param name="label" as="xs:string" select="''"/>
				<xsl:param name="clips-for-dest-file" as="element(audio)*" select="()"/> <!-- with absolute and normalized @src -->
				<xsl:on-completion>
					<xsl:variable name="clips-for-dest-file" as="element(audio)*">
						<xsl:sequence select="$clips-for-dest-file"/>
						<xsl:if test="exists($from-smil-elem)">
							<xsl:call-template name="audio-clips-between">
								<xsl:with-param name="from-smil-doc" select="$from-smil-doc"/>
								<xsl:with-param name="from-smil-elem" select="$from-smil-elem"/>
								<xsl:with-param name="to-smil-doc" select="count($smils)"/>
								<xsl:with-param name="to-smil-elem" select="()"/>
							</xsl:call-template>
						</xsl:if>
					</xsl:variable>
					<xsl:call-template name="add-clips-to-dest-file">
						<xsl:with-param name="audio-clips" select="$clips-for-dest-file"/>
						<xsl:with-param name="dest-file">
							<xsl:call-template name="dest-file">
								<xsl:with-param name="index" select="$dest-file"/>
								<xsl:with-param name="label" select="$label"/>
							</xsl:call-template>
						</xsl:with-param>
					</xsl:call-template>
				</xsl:on-completion>
				<xsl:choose>
					<xsl:when test="not(self::html:h1|self::html:h2|self::html:h3|self::html:h4|self::html:h5|self::html:h6)">
						<xsl:next-iteration/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:variable name="src" select="pf:normalize-uri(resolve-uri(html:a/@href,base-uri(.)))"/>
						<xsl:variable name="to-smil-doc" as="xs:integer?"
						              select="$smil-doc-index-from-base-uri(substring-before($src,'#'))"/>
						<xsl:variable name="to-smil-elem" as="element()?"
						              select="if (exists($to-smil-doc))
						                      then key('id',substring-after($src,'#'),$smils[$to-smil-doc])
						                      else ()"/>
						<xsl:variable name="to-smil-elem" as="element()?"
						              select="if ($to-smil-elem/self::text)
						                      then $to-smil-elem/parent::*
						                      else $to-smil-elem"/>
						<xsl:if test="not(exists($to-smil-elem))">
							<xsl:call-template name="pf:error">
								<xsl:with-param name="msg">NCC invalid: "href" attribute contains a broken
								reference: {}</xsl:with-param>
								<xsl:with-param name="args" select="html:a/@href"/>
							</xsl:call-template>
						</xsl:if>
						<xsl:variable name="clips-for-dest-file" as="element(audio)*">
							<xsl:sequence select="$clips-for-dest-file"/>
							<xsl:if test="exists($from-smil-elem)">
								<xsl:call-template name="audio-clips-between">
									<xsl:with-param name="from-smil-doc" select="$from-smil-doc"/>
									<xsl:with-param name="from-smil-elem" select="$from-smil-elem"/>
									<xsl:with-param name="to-smil-doc" select="$to-smil-doc"/>
									<xsl:with-param name="to-smil-elem" select="$to-smil-elem"/>
								</xsl:call-template>
							</xsl:if>
						</xsl:variable>
						<xsl:variable name="level" as="xs:integer" select="xs:integer(substring-after(local-name(),'h'))"/>
						<xsl:choose>
							<xsl:when test="$level&lt;=$depth">
								<xsl:call-template name="add-clips-to-dest-file">
									<xsl:with-param name="audio-clips" select="$clips-for-dest-file"/>
									<xsl:with-param name="dest-file">
										<xsl:call-template name="dest-file">
											<xsl:with-param name="index" select="$dest-file"/>
											<xsl:with-param name="label" select="$label"/>
										</xsl:call-template>
									</xsl:with-param>
								</xsl:call-template>
								<xsl:next-iteration>
									<xsl:with-param name="from-smil-doc" select="$to-smil-doc"/>
									<xsl:with-param name="from-smil-elem" select="$to-smil-elem"/>
									<xsl:with-param name="dest-file"
									                select="if (empty($clips-for-dest-file)
									                            and (every $x in $level + 1 to $depth satisfies $dest-file[$x] eq 1))
									                        then $dest-file
									                        else (for $x in 1 to $level - 1 return $dest-file[$x],
									                              $dest-file[$level] + 1,
									                              for $x in $level + 1 to $depth return 1)"/>
									<xsl:with-param name="label" select="normalize-space(string(.))"/>
									<xsl:with-param name="clips-for-dest-file" select="()"/>
								</xsl:next-iteration>
							</xsl:when>
							<xsl:otherwise>
								<xsl:next-iteration>
									<xsl:with-param name="from-smil-doc" select="$to-smil-doc"/>
									<xsl:with-param name="from-smil-elem" select="$to-smil-elem"/>
									<xsl:with-param name="clips-for-dest-file" select="$clips-for-dest-file"/>
								</xsl:next-iteration>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:iterate>
		</d:fileset>
	</xsl:template>

	<xsl:template name="audio-clips-between" as="element(audio)*">
		<xsl:param name="from-smil-doc" as="xs:integer" required="yes"/>
		<xsl:param name="from-smil-elem" as="element()" required="yes"/>
		<xsl:param name="to-smil-doc" as="xs:integer" required="yes"/>
		<xsl:param name="to-smil-elem" as="element()?" required="yes"/>
		<xsl:choose>
			<xsl:when test="exists($to-smil-elem)">
				<xsl:choose>
					<xsl:when test="$from-smil-doc&gt;$to-smil-doc">
						<xsl:call-template name="pf:error">
							<xsl:with-param name="msg">NCC invalid: noncontinuous flow.</xsl:with-param>
						</xsl:call-template>
					</xsl:when>
					<xsl:when test="$from-smil-doc=$to-smil-doc">
						<xsl:if test="not($from-smil-elem/(following::*|descendant::*) intersect $to-smil-elem)">
							<xsl:call-template name="pf:error">
								<xsl:with-param name="msg">NCC invalid: noncontinuous flow.</xsl:with-param>
							</xsl:call-template>
						</xsl:if>
						<xsl:sequence select="$from-smil-elem/(following::*|descendant::*)
						                      intersect $to-smil-elem/preceding::audio"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:sequence select="$from-smil-elem/(following::audio|descendant::audio)"/>
						<xsl:sequence select="for $x in ($from-smil-doc + 1) to ($to-smil-doc - 1)
						                      return $smils[$x]//audio"/>
						<xsl:sequence select="$to-smil-elem/preceding::audio"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:sequence select="$from-smil-elem/(following::audio|descendant::audio)"/>
				<xsl:sequence select="for $x in ($from-smil-doc + 1) to $to-smil-doc
				                      return $smils[$x]//audio"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="add-clips-to-dest-file">
		<xsl:param name="audio-clips" as="element(audio)*" required="yes"/>
		<xsl:param name="dest-file" as="xs:string" required="yes"/>
		<xsl:variable name="clips" as="element(d:clip)*">
			<xsl:iterate select="$audio-clips">
				<xsl:param name="clipBegin" as="xs:decimal" select="0"/>
				<xsl:variable name="original-clipBegin" as="xs:decimal" select="pf:smil-clock-value-to-seconds(@clip-begin)"/>
				<xsl:variable name="original-clipEnd" as="xs:decimal" select="pf:smil-clock-value-to-seconds(@clip-end)"/>
				<xsl:variable name="clipEnd" as="xs:decimal" select="$clipBegin + $original-clipEnd - $original-clipBegin"/>
				<d:clip original-href="{pf:normalize-uri(resolve-uri(@src,base-uri(.)))}"
				        original-clipBegin="{$original-clipBegin}"
				        original-clipEnd="{$original-clipEnd}"
				        clipBegin="{$clipBegin}"
				        clipEnd="{$clipEnd}"/>
				<xsl:next-iteration>
					<xsl:with-param name="clipBegin" select="$clipEnd"/>
				</xsl:next-iteration>
			</xsl:iterate>
		</xsl:variable>
		<xsl:for-each-group select="$clips" group-adjacent="@original-href">
			<d:file href="{$dest-file}" original-href="{@original-href}" media-type="{$media-type}">
				<xsl:for-each select="current-group()">
					<xsl:copy>
						<xsl:sequence select="@clipBegin|@clipEnd|@original-clipBegin|@original-clipEnd"/>
					</xsl:copy>
				</xsl:for-each>
			</d:file>
		</xsl:for-each-group>
	</xsl:template>
	
	<xsl:template name="dest-file" as="xs:string">
		<xsl:param name="index" as="xs:integer*" required="yes"/>
		<xsl:param name="label" as="xs:string" required="yes"/>
		<xsl:variable name="folder-index" as="xs:integer*" select="$index[position()&lt;last()]"/>
		<xsl:variable name="file-index" as="xs:integer" select="$index[last()]"/>
		<xsl:sequence select="concat(
		                        string-join(for $x in $folder-index return
		                                      substring(
		                                        format-number($x,'000'),
		                                        1,
		                                        $max-folder-name-length),
		                                    '/'),
		                        '/',
		                        encode-for-uri(
		                          substring(
		                            concat(
		                              format-number($file-index,'000'),
		                              ' ',
		                              $label),
		                            1,
		                            $max-file-name-length)),
		                        '.',
		                        $file-extension)"/>
	</xsl:template>
	
</xsl:stylesheet>
