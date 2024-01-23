<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="3.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:array="http://www.w3.org/2005/xpath-functions/array"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:html="http://www.w3.org/1999/xhtml">

	<xsl:param name="smils" as="document-node(element(smil))*" required="yes"/> <!-- in reading order -->
	<xsl:param name="file-limit" as="array(xs:integer)" required="no" select="[-1,-1,-1]"/>
	<xsl:param name="level-offset" as="xs:integer" required="no" select="0"/>
	<xsl:param name="max-folder-name-length" as="xs:integer" required="no" select="58"/>
	<xsl:param name="max-file-name-length" as="xs:integer" required="no" select="185"/>

	<xsl:include href="http://www.daisy.org/pipeline/modules/common-utils/library.xsl"/>
	<xsl:include href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>
	<xsl:include href="http://www.daisy.org/pipeline/modules/smil-utils/clock-functions.xsl"/>

	<xsl:variable name="depth" select="array:size($file-limit)">
		<!-- depth of folder structure; positive number (1 means no folders, 2 means 1 level of folders) -->
	</xsl:variable>
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
				<xsl:param name="dest-file" as="xs:integer*" select="(1,for $x in 1 to $depth return 0)"/> <!-- (1,0,0,0,...) -->
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
								<xsl:with-param name="msg">NCC invalid: "href" attribute contains a
								broken reference: {}</xsl:with-param>
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
						<xsl:variable name="level" as="xs:integer" select="xs:integer(substring-after(local-name(),'h')) + $level-offset"/>
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
								<xsl:variable name="dest-file" as="xs:integer*">
									<xsl:choose>
										<xsl:when test="empty($clips-for-dest-file)
										                and $level&lt;$depth
										                and (every $x in $level + 1 to $depth satisfies $dest-file[1 + $x] eq 1)">
											<xsl:sequence select="$dest-file"/>
										</xsl:when>
										<xsl:otherwise>
											<!-- split levels/volumes that are full -->
											<xsl:iterate select="reverse(0 to $level)">
												<xsl:variable name="level" as="xs:integer" select="."/>
												<xsl:choose>
													<xsl:when test="$level&gt;0 and
													                $file-limit[$level]&gt;0 and
													                $dest-file[1 + $level]=$file-limit[$level]">
														<xsl:next-iteration/>
													</xsl:when>
													<xsl:otherwise>
														<!-- level is number between 0 and $depth -->
														<!-- $level=0 means start a new volume -->
														<xsl:break select="(for $x in 0 to $level - 1 return max((1,$dest-file[1 + $x])),
														                    $dest-file[1 + $level] + 1,
														                    for $x in $level + 1 to $depth return 1)"/>
													</xsl:otherwise>
												</xsl:choose>
											</xsl:iterate>
										</xsl:otherwise>
									</xsl:choose>
								</xsl:variable>
								<xsl:next-iteration>
									<xsl:with-param name="from-smil-doc" select="$to-smil-doc"/>
									<xsl:with-param name="from-smil-elem" select="$to-smil-elem"/>
									<xsl:with-param name="dest-file" select="$dest-file"/>
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

	<xsl:template name="add-clips-to-dest-file" as="element(d:file)*">
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
		<xsl:variable name="volume" as="xs:integer" select="$index[1]"/>
		<xsl:variable name="folder-index" as="xs:integer*" select="$index[position()&gt;1 and position()&lt;last()]"/>
		<xsl:variable name="file-index" as="xs:integer" select="$index[last()]"/>
		<xsl:sequence select="concat(
		                        'volume%20',
		                        $volume,
		                        '/',
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
