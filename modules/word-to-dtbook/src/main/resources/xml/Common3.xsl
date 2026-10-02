<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
	xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture"
	xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing"
	xmlns:dcterms="http://purl.org/dc/terms/"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties"
	xmlns:dc="http://purl.org/dc/elements/1.1/"
	xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
	xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"
	xmlns:v="urn:schemas-microsoft-com:vml"
	xmlns:dcmitype="http://purl.org/dc/dcmitype/"
	xmlns:dgm="http://schemas.openxmlformats.org/drawingml/2006/diagram"
	xmlns:o="urn:schemas-microsoft-com:office:office"
	xmlns:d="org.daisy.pipeline.word_to_dtbook.impl.DaisyClass"
	xmlns="http://www.daisy.org/z3986/2005/dtbook/"
	exclude-result-prefixes="w pic wp dcterms xsi cp dc a r v dcmitype d o xsl dgm xs">
	
	<xsl:variable name="styles" select="$stylesXml//w:styles" />
	
	<xsl:variable name="ignorableCharacters" select="concat('[](){}=+-_;.~$*%&amp;&quot;,0123456789!?@#:&lt;&gt;/| ', &quot;&apos;&quot;)"/>
	<!-- Keys for note lookups : hash-based O(1) access by note id instead of a full scan of
	     footnotes.xml / endnotes.xml per note reference -->
	<xsl:key name="footnote-by-id" match="w:footnote" use="number(@w:id)"/>
	<xsl:key name="endnote-by-id" match="w:endnote" use="number(@w:id)"/>
	
	<xsl:variable name="defaultLatin">
		<xsl:choose>
			<xsl:when test="$styles/w:style[@w:type='paragraph' and (@w:styleId='Normal' or w:default='1')]/w:rPr/w:lang/@w:val">
				<xsl:value-of select="$styles/w:style[@w:type='paragraph' and (@w:styleId='Normal' or w:default='1')]/w:rPr/w:lang/@w:val"/>
			</xsl:when>
			<xsl:when test="$styles/w:docDefaults/w:rPrDefault/w:rPr/w:lang/@w:val">
				<xsl:value-of select="$styles/w:docDefaults/w:rPrDefault/w:rPr/w:lang/@w:val"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="''"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<xsl:variable name="defaultComplex">
		<xsl:choose>
			<xsl:when test="$styles/w:style[@w:type='paragraph' and (@w:styleId='Normal' or w:default='1')]/w:rPr/w:lang/@w:bidi">
				<xsl:value-of select="$styles/w:style[@w:type='paragraph' and (@w:styleId='Normal' or w:default='1')]/w:rPr/w:lang/@w:bidi"/>
			</xsl:when>
			<xsl:when test="$styles/w:docDefaults/w:rPrDefault/w:rPr/w:lang/@w:bidi">
				<xsl:value-of select="$styles/w:docDefaults/w:rPrDefault/w:rPr/w:lang/@w:bidi"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$defaultLatin"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<xsl:variable name="defaultEastAsia">
		<xsl:choose>
			<xsl:when test="$styles/w:style[@w:type='paragraph' and (@w:styleId='Normal' or w:default='1')]/w:rPr/w:lang/@w:eastAsia">
				<xsl:value-of select="$styles/w:style[@w:type='paragraph' and (@w:styleId='Normal' or w:default='1')]/w:rPr/w:lang/@w:eastAsia"/>
			</xsl:when>
			<xsl:when test="$styles/w:docDefaults/w:rPrDefault/w:rPr/w:lang/@w:eastAsia">
				<xsl:value-of select="$styles/w:docDefaults/w:rPrDefault/w:rPr/w:lang/@w:eastAsia"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$defaultLatin"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	
	<!--Declaring Global paramaters-->
	<xsl:param name="title" as="xs:string" select="''"/> <!--Holds Documents Title value-->
	<xsl:param name="creator" as="xs:string" select="''"/> <!--Holds Documents creator value-->
	<xsl:param name="publisher" as="xs:string" select="''"/> <!--Holds Documents Publisher value-->
	<xsl:param name="uid" as="xs:string" select="''"/> <!--Holds Document unique id value-->
	<xsl:param name="subject" as="xs:string" select="''"/> <!--Holds Documents Subject value-->
	<xsl:param name="acceptRevisions" as="xs:boolean" select="true()"/>
	<xsl:param name="version" as="xs:string" select="'14'"/> <!--Holds Documents version value-->
	<xsl:param name="pagination" as="xs:string" select="'custom'"/> <!-- Automatic|Custom -->
	<xsl:param name="MasterSub" as="xs:boolean" select="false()"/>
	<xsl:param name="ImageSizeOption" as="xs:string" select="'original'"/> <!-- resize|resample|original -->
	<xsl:param name="DPI" as="xs:integer" select="96"/>
	<xsl:param name="CharacterStyles" as="xs:boolean" select="false()" /> <!-- if true, also convert custom character styles to span with style attribute -->
	<xsl:param name="FootnotesPosition" as="xs:string" select="'end'" /> <!-- page|end| -->
	<xsl:param name="FootnotesLevel" as="xs:integer" select="0" />
	<xsl:param name="FootnotesNumbering" as="xs:string" select="'none'"  />
	<xsl:param name="FootnotesStartValue" as="xs:integer" select="1" />
	<xsl:param name="FootnotesNumberingPrefix" as="xs:string?" select="''"/>
	<xsl:param name="FootnotesNumberingSuffix" as="xs:string?" select="''"/>
	<xsl:param name="Language" as="xs:string?" select="''"/>
	
	<!--Template to create NoteReference for FootNote and EndNote
		It is taking two parameters varFootnote_Id and varNote_Class. varFootnote_Id 
		will contain the Reference id of either Footnote or Endnote.-->
	<xsl:template name="NoteReference">
		<xsl:param name="noteID" as="xs:integer"/>
		<xsl:param name="noteClass" as="xs:string"/>
		<xsl:variable name="parentLang">
			<xsl:call-template name="GetParagraphLanguage">
				<xsl:with-param name="paragraphNode" select=".." />
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="runLang">
			<xsl:call-template name="GetRunLanguage">
				<xsl:with-param name="runNode" select="." />
			</xsl:call-template>
		</xsl:variable>
		<xsl:if test="(exists($footnotesXml) and exists(key('footnote-by-id', $noteID, $footnotesXml)))
			or (exists($endnotesXml) and exists(key('endnote-by-id', $noteID, $endnotesXml)))">
			<xsl:variable name="idref">
				<!--If Note_Class is Footnotereference then it will have footnote id value -->
				<xsl:if test="$noteClass='FootnoteReference'">
					<xsl:value-of select="concat('#footnote-',$noteID)"/>
				</xsl:if>
				<!--If Note_Class is Footnotereference then it will have footnote id value -->
				<xsl:if test="$noteClass='EndnoteReference'">
					<xsl:value-of select="concat('#endnote-',$noteID)"/>
				</xsl:if>
			</xsl:variable>
			<noteref idref="{$idref}">
				<!--Creating the attribute idref for Noteref element and assining it a value.-->
				<!--Creating the attribute class for Noteref element and assinging it a value.-->
				<xsl:attribute name="class">
					<xsl:if test="$noteClass='FootnoteReference'">
						<xsl:value-of select="substring($noteClass,1,8)"/>
					</xsl:if>
					<!--Creating the attribute class for Noteref element and assinging it a value.-->
					<xsl:if test="$noteClass='EndnoteReference'">
						<xsl:value-of select="substring($noteClass,1,7)"/>
					</xsl:if>
				</xsl:attribute>
				<!--Checking if language differ from paragraph language -->
				<xsl:if test="not($parentLang = $runLang)">
					<xsl:attribute name="xml:lang">
						<xsl:value-of select="$runLang"/>
					</xsl:attribute>
				</xsl:if>
				<xsl:value-of select="$noteID"/>
			</noteref>
		</xsl:if>
	</xsl:template>
	
	<!--Template for Adding footnote-->
	<xsl:template name="InsertFootnotes">
		<xsl:param name="level"/>
		<xsl:param name="verfoot" as="xs:string"/>
		<xsl:param name="characterStyle" as="xs:boolean" select="false()"/>
		<xsl:param name="sOperators" as="xs:string"/>
		<xsl:param name="sMinuses" as="xs:string"/>
		<xsl:param name="sNumbers" as="xs:string"/>
		<xsl:param name="sZeros" as="xs:string"/>
		<!--Inserting default footnote id in the array list-->
		<xsl:variable name="checkid" as="xs:integer" select="d:FootNoteId($myObj,0, $level)"/>
		<!-- Checking for the matching Id and level returned from java code -->
		<xsl:if test="$checkid!=0">
			<!--Traversing through each footnote element in footnotes.xml file-->
			<xsl:for-each select="if (exists($footnotesXml)) then key('footnote-by-id', $checkid, $footnotesXml) else ()">
				<!--Checking if Id returned from C# is equal to the footnote Id in footnotes.xml file-->
				<xsl:if test="number(@w:id)=$checkid">
					<!-- <xsl:message terminate="no">progress:Insert footnote <xsl:value-of select="$checkid"/></xsl:message> -->
					<!--Creating note element and it's attribute values-->
					<note id="{concat('footnote-',$checkid)}" class="Footnote">
						<xsl:sequence select="d:sink(d:PushLevel($myObj, $level + 1))"/>
						<!--Travering each element inside w:footnote in footnote.xml file-->
						<xsl:for-each select="./node()">
							<!--Checking for Paragraph element-->
							<xsl:if test="self::w:p">
								<xsl:choose>
									<!--Checking for MathImage in Word2003/xp  footnotes-->
									<xsl:when test="(w:r/w:object/v:shape/v:imagedata/@r:id) and (not(w:r/w:object/o:OLEObject[@ProgID='Equation.DSMT4']))" >
										<p>
											<xsl:value-of select="$FootnotesNumberingPrefix"/>
											<xsl:choose>
												<xsl:when test="$FootnotesNumbering = 'number'">
													<xsl:value-of select="$checkid + number($FootnotesStartValue)"/>
												</xsl:when>
											</xsl:choose>
											<xsl:value-of select="$FootnotesNumberingSuffix"/>
											<imggroup>
												<!--Variable to hold r:id from document.xml-->
												<xsl:variable name="Math_id"  as="xs:string" select="w:r/w:object/v:shape/v:imagedata/@r:id" />
												<xsl:variable name="alt">
													<xsl:if test="not(w:r/w:object/v:shape/@alt)">
														<xsl:value-of select="'Math Equation'"/>
													</xsl:if>
												</xsl:variable>
												<img src="{d:MathImageFootnote($myObj,$Math_id)}" alt="{$alt}">
													<xsl:if test="w:r/w:object/v:shape/@alt">
														<xsl:sequence select="w:r/w:object/v:shape/@alt"/>
													</xsl:if>
												</img>
											</imggroup>
										</p>
									</xsl:when>
									<xsl:when test="w:r/w:object/o:OLEObject[@ProgID='Equation.DSMT4']">
										<xsl:variable name="Math_DSMT4" as="xs:string" select="d:GetMathML($myObj,'wdFootnotesStory')"/>
										<xsl:choose>
											<xsl:when test="$Math_DSMT4=''">
												<imggroup>
													<!--Creating variable mathimage for storing r:id value from document.xml-->
													<xsl:variable name="Math_rid" as="xs:string" select="w:r/w:object/v:shape/v:imagedata/@r:id"/>
													<xsl:variable name="alt">
														<xsl:if test="not(w:r/w:object/v:shape/@alt)">
															<xsl:value-of select="'Math Equation'"/>
														</xsl:if>
													</xsl:variable>
													<img src="{d:MathImageFootnote($myObj,$Math_rid)}" alt="{$alt}">
														<xsl:if test="w:r/w:object/v:shape/@alt">
															<xsl:sequence select="w:r/w:object/v:shape/@alt"/>
														</xsl:if>
													</img>
												</imggroup>
											</xsl:when>
											<xsl:otherwise>
												<xsl:value-of disable-output-escaping="yes" select="$Math_DSMT4"/>
											</xsl:otherwise>
										</xsl:choose>
									</xsl:when>
									<xsl:otherwise>
										<xsl:call-template name="ParagraphStyle">
											<xsl:with-param name="version" select="$verfoot"/>
											<xsl:with-param name="flagNote" select="'footnote'"/>
											<xsl:with-param name="checkid" select="$checkid + 1"/>
											<xsl:with-param name="sOperators" select="$sOperators"/>
											<xsl:with-param name="sMinuses" select="$sMinuses"/>
											<xsl:with-param name="sNumbers" select="$sNumbers"/>
											<xsl:with-param name="sZeros" select="$sZeros"/>
											<xsl:with-param name="characterparaStyle" select="$characterStyle"/>
										</xsl:call-template>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:if>
						</xsl:for-each>
						<xsl:sequence select="d:sink(d:PopLevel($myObj))"/>
					</note>
				</xsl:if>
				<xsl:sequence select="d:sink(d:InitializeNoteFlag($myObj))"/> <!-- empty -->
			</xsl:for-each>
			<!--Calling the template footnote recursively until the function returns 0-->
			<xsl:call-template name="InsertFootnotes">
				<xsl:with-param name="level" select="$level" />
				<xsl:with-param name="verfoot" select ="$verfoot"/>
				<xsl:with-param name="sOperators" select="$sOperators"/>
				<xsl:with-param name="sMinuses" select="$sMinuses"/>
				<xsl:with-param name="sNumbers" select="$sNumbers"/>
				<xsl:with-param name="sZeros" select="$sZeros"/>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>
	
	<!--Template for handling multiple Prodnotes and Captions applied to an image
	Used 4 time in this file in templates 
		- PictureHandler
		- Imagegroup2003
		- Object
		- tmpShape
	-->
	<xsl:template name="ProcessCaptionProdNote">
		<xsl:param name="followingnodes" as="node()*"/>
		<xsl:param name="imageId" as="xs:string"/>
		<xsl:param name="characterStyle" as="xs:boolean"/>
		<xsl:choose>
			<!--Checking for inbuilt caption and Image-CaptionDAISY custom paragraph style-->
			<xsl:when test="($followingnodes[1]/w:pPr/w:pStyle/@w:val='Caption') or ($followingnodes[1]/w:pPr/w:pStyle/@w:val='Image-CaptionDAISY')">
				<xsl:sequence select="d:sink(d:AddCaptionsProdnotes($myObj))"/> <!-- empty -->
				<caption>
					<!--attribute holds the value of the image id-->
					<xsl:attribute name="imgref" select="$imageId"/>
					<xsl:if test="($followingnodes[1]/w:r/w:rPr/w:lang) or ($followingnodes[1]/w:r/w:rPr/w:rFonts/@w:hint)">
						<!--attribute holds the id of the language-->
						<xsl:attribute name="xml:lang">
							<xsl:call-template name="PictureLanguage">
								<xsl:with-param name="CheckLang" select="'picture'"/>
							</xsl:call-template>
						</xsl:attribute>
					</xsl:if>
					<!--Checking if image is bidirectionally oriented-->
					<xsl:if test="($followingnodes[1]/w:pPr/w:bidi[not(@w:val=('0','false','off'))]) or ($followingnodes[1]/w:r/w:rPr/w:rtl)">
						<!--Variable holds the value which indicates that the image is bidirectionally oriented-->
						<xsl:variable name="Bd" as="xs:string">
							<!--calling the PictureLanguage template-->
							<xsl:call-template name="PictureLanguage">
								<xsl:with-param name="CheckLang" select="'picture'"/>
							</xsl:call-template>
						</xsl:variable>
						<xsl:value-of disable-output-escaping="yes" select="concat('&lt;p xml:lang=&quot;',$Bd,'&quot;&gt;')"/>
						<xsl:value-of disable-output-escaping="yes" select="concat('&lt;bdo dir= &quot;rtl&quot; xml:lang=&quot;',$Bd,'&quot;&gt;')"/>
					</xsl:if>
					<!--Looping through each of the node to print text to the output xml-->
					<xsl:for-each select="$followingnodes[1]/node()">
						<xsl:if test="self::w:r">
							<xsl:call-template name="TempCharacterStyle">
								<xsl:with-param name="characterStyle" select="$characterStyle"/>
							</xsl:call-template>
						</xsl:if>
						<xsl:if test="self::w:fldSimple">
							<xsl:value-of select="w:r/w:t"/>
						</xsl:if>
					</xsl:for-each>
					<!--Checking if image is bidirectionally oriented-->
					<xsl:if test="$followingnodes[1]/w:pPr/w:bidi[not(@w:val=('0','false','off'))]">
						<xsl:value-of disable-output-escaping="yes" select="'&lt;/bdo&gt;'"/>
						<xsl:value-of disable-output-escaping="yes" select="'&lt;/p&gt;'"/>
					</xsl:if>
				</caption>
				<!--Recursively calling the ProcessCaptionProdNote template till all the Captions are processed-->
				<xsl:call-template name="ProcessCaptionProdNote">
					<xsl:with-param name="followingnodes" select="$followingnodes[position() > 1]"/>
					<xsl:with-param name="imageId" select="$imageId"/>
					<xsl:with-param name="characterStyle" select="$characterStyle"/>
				</xsl:call-template>
			</xsl:when>
			<!--Checking for inbuilt caption and Prodnote-OptionalDAISY custom paragraph style-->
			<xsl:when test="($followingnodes[1]/w:pPr/w:pStyle/@w:val='Prodnote-OptionalDAISY')">
				<xsl:sequence select="d:sink(d:AddCaptionsProdnotes($myObj))"/> <!-- empty -->
				<xsl:value-of disable-output-escaping="yes" select="concat('&lt;prodnote render= &quot;optional&quot; imgref=&quot;',$imageId,'&quot;&gt;')"/>
				<!--Checking if image is bidirectionally oriented-->
				<xsl:if test="($followingnodes[1]/w:pPr/w:bidi[not(@w:val=('0','false','off'))]) or ($followingnodes[1]/w:r/w:rPr/w:rtl)">
					<!--Variable holds the value which indicates that the image is bidirectionally oriented-->
					<xsl:variable name="Bd" as="xs:string">
						<!--calling the PictureLanguage template-->
						<xsl:call-template name="PictureLanguage">
							<xsl:with-param name="CheckLang" select="'picture'"/>
						</xsl:call-template>
					</xsl:variable>
					<xsl:value-of disable-output-escaping="yes" select="concat('&lt;p xml:lang=&quot;',$Bd,'&quot;&gt;')"/>
					<xsl:value-of disable-output-escaping="yes" select="concat('&lt;bdo dir= &quot;rtl&quot; xml:lang=&quot;',$Bd,'&quot;&gt;')"/>
				</xsl:if>
				<!--Looping through each of the node to print text to the output xml-->
				<xsl:for-each select="$followingnodes[1]/node()">
					<xsl:if test="self::w:r">
						<xsl:call-template name="TempCharacterStyle">
							<xsl:with-param name="characterStyle" select="$characterStyle"/>
						</xsl:call-template>
					</xsl:if>
				</xsl:for-each>
				<!--Checking if image is bidirectionally oriented-->
				<xsl:if test="$followingnodes[1]/w:pPr/w:bidi[not(@w:val=('0','false','off'))]">
					<xsl:value-of disable-output-escaping="yes" select="'&lt;/bdo&gt;'"/>
					<xsl:value-of disable-output-escaping="yes" select="'&lt;/p&gt;'"/>
				</xsl:if>
				<xsl:value-of disable-output-escaping="yes" select="'&lt;/prodnote &gt;'"/>
				<!--Recursively calling the ProcessCaptionProdNote template till all the ProdNotes are processed-->
				<xsl:call-template name="ProcessCaptionProdNote">
					<xsl:with-param name="followingnodes" select="$followingnodes[position() > 1]"/>
					<xsl:with-param name="imageId" select="$imageId"/>
					<xsl:with-param name="characterStyle" select="$characterStyle"/>
				</xsl:call-template>
			</xsl:when>
			<!--Checking for inbuilt caption and Prodnote-RequiredDAISY custom paragraph style-->
			<xsl:when test="($followingnodes[1]/w:pPr/w:pStyle/@w:val='Prodnote-RequiredDAISY')">
				<xsl:sequence select="d:sink(d:AddCaptionsProdnotes($myObj))"/> <!-- empty -->
				<xsl:value-of disable-output-escaping="yes" select="concat('&lt;prodnote render=&quot;required&quot; imgref=&quot;', $imageId ,'&quot;&gt;')"/>
				<!--Checking if image is bidirectionally oriented-->
				<xsl:if test="($followingnodes[1]/w:pPr/w:bidi[not(@w:val=('0','false','off'))]) or ($followingnodes[1]/w:r/w:rPr/w:rtl)">
					<!--Variable holds the value which indicates that the image is bidirectionally oriented-->
					<xsl:variable name="Bd" as="xs:string">
						<!--calling the PictureLanguage template-->
						<xsl:call-template name="PictureLanguage">
							<xsl:with-param name="CheckLang" select="'picture'"/>
						</xsl:call-template>
					</xsl:variable>
					<xsl:value-of disable-output-escaping="yes" select="concat('&lt;p xml:lang=&quot;',$Bd,'&quot;&gt;')"/>
					<xsl:value-of disable-output-escaping="yes" select="concat('&lt;bdo dir= &quot;rtl&quot; xml:lang=&quot;',$Bd,'&quot;&gt;')"/>
				</xsl:if>
				<!--Looping through each of the node to print text to the output xml-->
				<xsl:for-each select="$followingnodes[1]/node()">
					<xsl:if test="self::w:r">
						<xsl:call-template name="TempCharacterStyle">
							<xsl:with-param name="characterStyle" select="$characterStyle"/>
						</xsl:call-template>
					</xsl:if>
				</xsl:for-each>
				<!--Checking if image is bidirectionally oriented-->
				<xsl:if test="$followingnodes[1]/w:pPr/w:bidi[not(@w:val=('0','false','off'))]">
					<xsl:value-of disable-output-escaping="yes" select="'&lt;/bdo&gt;'"/>
					<xsl:value-of disable-output-escaping="yes" select="'&lt;/p&gt;'"/>
				</xsl:if>
				<xsl:value-of disable-output-escaping="yes" select="'&lt;/prodnote &gt;'"/>
				<!--Recursively calling the ProcessCaptionProdNote template till all the ProdNotes are processed-->
				<xsl:call-template name="ProcessCaptionProdNote">
					<xsl:with-param name="followingnodes" select="$followingnodes[position() > 1]"/>
					<xsl:with-param name="imageId" select="$imageId"/>
					<xsl:with-param name="characterStyle" select="$characterStyle"/>
				</xsl:call-template>
			</xsl:when>
		</xsl:choose>
		<xsl:sequence select="d:ResetCaptionsProdnotes($myObj)"/> <!-- empty -->
	</xsl:template>
	
	<!--Template for implementing Simple Images i.e, ungrouped images
	Only used 1 time in Common.xsl:1391
	-->
	<xsl:template name="PictureHandler">
		<xsl:param name="imgOpt" as="xs:string"/>
		<xsl:param name="dpi" as="xs:float?"/>
		<xsl:param name="characterStyle" as="xs:boolean"/>
		<xsl:variable name="alttext" as="xs:string?">
			<xsl:choose>
				<xsl:when test="w:drawing/wp:inline/wp:docPr/@descr">
					<xsl:sequence select="w:drawing/wp:inline/wp:docPr/@descr"/>
				</xsl:when>
				<xsl:when test="w:drawing/wp:inline/a:graphic/a:graphicData/pic:pic/pic:nvPicPr/pic:cNvPr/@descr">
					<xsl:sequence select="w:drawing/wp:inline/a:graphic/a:graphicData/pic:pic/pic:nvPicPr/pic:cNvPr/@descr"/>
				</xsl:when>
				<xsl:when test="w:drawing/wp:anchor/a:graphic/a:graphicData/pic:pic/pic:nvPicPr/pic:cNvPr/@descr">
					<xsl:sequence select="w:drawing/wp:anchor/a:graphic/a:graphicData/pic:pic/pic:nvPicPr/pic:cNvPr/@descr"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="''"/> <!-- Not sure if this alt text needs to be pre-filled or not (like with a 'No description provided' text)-->
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<!--Variable holds the value of Image Id-->
		<xsl:variable name="Img_Id" as="xs:string?">
			<xsl:choose>
				<xsl:when  test="w:drawing/wp:inline/a:graphic/a:graphicData/pic:pic/pic:blipFill/a:blip/@r:embed">
					<xsl:sequence select="w:drawing/wp:inline/a:graphic/a:graphicData/pic:pic/pic:blipFill/a:blip/@r:embed"/>
				</xsl:when>
				<xsl:when test="w:drawing/wp:anchor/a:graphic/a:graphicData/pic:pic/pic:blipFill/a:blip/@r:embed">
					<xsl:sequence select="w:drawing/wp:anchor/a:graphic/a:graphicData/pic:pic/pic:blipFill/a:blip/@r:embed"/>
				</xsl:when>
				<xsl:when test="w:drawing/wp:inline/wp:docPr/@id">
					<xsl:sequence select="w:drawing/wp:inline/wp:docPr/@id"/>
				</xsl:when>
				<xsl:when test="w:drawing/wp:anchor/wp:docPr/@id">
					<xsl:sequence select="w:drawing/wp:anchor/wp:docPr/@id"/>
				</xsl:when>
			</xsl:choose>
		</xsl:variable>
		<!--Variable holds the filename of the image-->
		<xsl:variable name="imageName" as="xs:string">
			<xsl:choose>
				<xsl:when  test="w:drawing/wp:inline/a:graphic/a:graphicData/pic:pic/pic:nvPicPr/pic:cNvPr/@name">
					<xsl:sequence select="w:drawing/wp:inline/a:graphic/a:graphicData/pic:pic/pic:nvPicPr/pic:cNvPr/@name"/>
				</xsl:when>
				<xsl:when test="w:drawing/wp:anchor/a:graphic/a:graphicData/pic:pic/pic:nvPicPr/pic:cNvPr/@name">
					<xsl:sequence select="w:drawing/wp:anchor/a:graphic/a:graphicData/pic:pic/pic:nvPicPr/pic:cNvPr/@name"/>
				</xsl:when>
				<xsl:when test="w:drawing/wp:inline/wp:docPr/@name">
					<xsl:sequence select="w:drawing/wp:inline/wp:docPr/@name"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:sequence select="''"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<!--Variable holds the value of Image Id concatenated with some random number generated for Image Id-->
		<xsl:variable name="imageId" as="xs:string">
			<xsl:choose>
				<xsl:when  test="w:drawing/wp:inline/a:graphic/a:graphicData/pic:pic/pic:blipFill/a:blip/@r:embed">
					<xsl:sequence select="concat($Img_Id,d:GenerateImageId($myObj))"/>
				</xsl:when>
				<xsl:when test="w:drawing/wp:anchor/a:graphic/a:graphicData/pic:pic/pic:blipFill/a:blip/@r:embed">
					<xsl:sequence select="concat($Img_Id,d:GenerateImageId($myObj))"/>
				</xsl:when>
				<!--For chart and diagram, an inline/docPr/id is usually defined in the drawing and is used for ID resolution in export-->
				<xsl:when test="w:drawing/wp:inline/a:graphic/a:graphicData/@uri='http://schemas.openxmlformats.org/drawingml/2006/diagram'
							or w:drawing/wp:inline/a:graphic/a:graphicData/@uri='http://schemas.openxmlformats.org/drawingml/2006/chart'">
					<xsl:sequence select="d:CheckShapeId($myObj,concat('Shape',w:drawing/wp:inline/wp:docPr/@id))"/>
				</xsl:when>
				<xsl:when test="w:drawing/wp:inline/wp:docPr/@id">
					<xsl:sequence select="d:CheckShapeId($myObj,w:drawing/wp:inline/wp:docPr/@id)"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:sequence select="''"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<!--
							imageWidth and imageHeight are expressed in EMU
							- 914400 EMU in an inch
							- 9525 EMU in a pixel @ 96 dpi
							- https://en.wikipedia.org/wiki/Office_Open_XML_file_formats#DrawingML
		-->
		<xsl:variable name="imageWidth" as="xs:double">
			<xsl:choose>
				<xsl:when  test="w:drawing/wp:inline/wp:extent">
					<xsl:sequence select="w:drawing/wp:inline/wp:extent/@cx"/>
				</xsl:when>
				<xsl:when test="w:drawing/wp:anchor/wp:extent">
					<xsl:sequence select="w:drawing/wp:anchor/wp:extent/@cx"/>
				</xsl:when>
				<xsl:when test="w:drawing/wp:inline/wp:extent">
					<xsl:sequence select="w:drawing/wp:inline/wp:extent/@cx"/>
				</xsl:when>
				<xsl:when test="w:drawing/wp:anchor/wp:extent">
					<xsl:sequence select="w:drawing/wp:anchor/wp:extent/@cx"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:sequence select="number(())"/> <!-- NaN -->
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="imageHeight" as="xs:double">
			<xsl:choose>
				<xsl:when  test="w:drawing/wp:inline/wp:extent">
					<xsl:sequence select="w:drawing/wp:inline/wp:extent/@cy"/>
				</xsl:when>
				<xsl:when test="w:drawing/wp:anchor/wp:extent">
					<xsl:sequence select="w:drawing/wp:anchor/wp:extent/@cy"/>
				</xsl:when>
				<xsl:when test="w:drawing/wp:inline/wp:extent">
					<xsl:sequence select="w:drawing/wp:inline/wp:extent/@cy"/>
				</xsl:when>
				<xsl:when test="w:drawing/wp:anchor/wp:extent">
					<xsl:sequence select="w:drawing/wp:anchor/wp:extent/@cy"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:sequence select="number(())"/> <!-- NaN -->
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<!--Checking if Img_Id variable contains any Image Id-->
		<xsl:if test="exists($Img_Id)">
			<!--Checking if document is bidirectionally oriented-->
			<xsl:if test="(../w:pPr/w:bidi[not(@w:val=('0','false','off'))]) or (../w:pPr/w:jc/@w:val='right')">
				<!--Variable holds the value which indicates that the image is bidirectionally oriented-->
				<xsl:variable name="imgBd" as="xs:string">
					<!--calling the PictureLanguage template-->
					<xsl:call-template name="PictureLanguage">
						<xsl:with-param name="CheckLang" select="'picture'"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:value-of disable-output-escaping="yes" select="concat('&lt;bdo dir= &quot;rtl&quot; xml:lang=&quot;',$imgBd,'&quot;&gt;')"/>
			</xsl:if>
			<xsl:variable name="imageSrc" as="xs:string">
				<xsl:choose>
					<xsl:when test="contains($Img_Id,'rId') and ($imgOpt='resize')">
						<xsl:sequence select ="d:Image($myObj,$Img_Id,$imageName)"/>
					</xsl:when>
					<xsl:when test="contains($Img_Id,'rId') and ($imgOpt='resample')">
						<xsl:sequence select ="d:ResampleImage($myObj,$Img_Id,$imageName,$dpi)"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:choose>
							<xsl:when test="contains($Img_Id,'rId')">
								<xsl:sequence select ="d:Image($myObj,$Img_Id,$imageName)"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:sequence select="d:ShapeFileName($myObj,$imageId)"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:variable>
			<xsl:variable name="checkImage" as="xs:string" select="d:CheckImage($myObj,$imageSrc)"/>
			<xsl:choose>
				<xsl:when test="$checkImage='1'">
					<!--Creating Imagegroup element-->
					<imggroup>
						<img src="{$imageSrc}" alt="{$alttext}" id="{$imageId}">
							<xsl:if test="$imgOpt='resize'">
								<xsl:attribute name="width" select="round(($imageWidth) div (9525))"/> <!-- assuming 96 dpi -->
								<xsl:attribute name="height" select="round(($imageHeight) div (9525))"/> <!-- assuming 96 dpi -->
							</xsl:if>
						</img>
						<!--Handling Image-CaptionDAISY custom paragraph style applied above an image-->
						<xsl:if test="(../preceding-sibling::node()[1]/w:pPr/w:pStyle/@w:val='Image-CaptionDAISY') or (../w:pPr/w:pStyle/@w:val='Caption') or (../w:pPr/w:pStyle/@w:val='Image-CaptionDAISY')">
							<caption>
								<xsl:attribute name="imgref" select="$imageId"/>
								<xsl:if test="(../following-sibling::w:p[1]/w:r/w:rPr/w:lang) or (../following-sibling::w:p[1]/w:r/w:rPr/w:rFonts/@w:hint)">
									<xsl:attribute name="xml:lang">
										<xsl:call-template name="PictureLanguage">
											<xsl:with-param name="CheckLang" select="'picture'"/>
										</xsl:call-template>
									</xsl:attribute>
								</xsl:if>
								<xsl:if test="(../following-sibling::w:p[1]/w:pPr/w:bidi[not(@w:val=('0','false','off'))]) or (../following-sibling::w:p[1]/w:r/w:rPr/w:rtl)">
									<xsl:variable name="Bd" as="xs:string">
										<xsl:call-template name="PictureLanguage">
											<xsl:with-param name="CheckLang" select="'picture'"/>
										</xsl:call-template>
									</xsl:variable>
									<xsl:value-of disable-output-escaping="yes" select="concat('&lt;p  xml:lang=&quot;',$Bd,'&quot;&gt;')"/>
									<xsl:value-of disable-output-escaping="yes" select="concat('&lt;bdo dir= &quot;rtl&quot; xml:lang=&quot;',$Bd,'&quot;&gt;')"/>
								</xsl:if>
								<xsl:if test="(../preceding-sibling::node()[1]/w:pPr/w:pStyle/@w:val='Image-CaptionDAISY')">
									<xsl:for-each select="../preceding-sibling::node()[1]/node()">
										<!--Printing the Caption value-->
										<xsl:if test="self::w:r">
											<xsl:call-template name="TempCharacterStyle">
												<xsl:with-param name="characterStyle" select="$characterStyle"/>
											</xsl:call-template>
										</xsl:if>
										<xsl:if test="self::w:fldSimple">
											<xsl:value-of select="w:r/w:t"/>
										</xsl:if>

									</xsl:for-each>
									<xsl:text> </xsl:text>
								</xsl:if>
								<xsl:if test="(../w:pPr/w:pStyle/@w:val='Caption') or (../w:pPr/w:pStyle/@w:val='Image-CaptionDAISY')">
									<xsl:for-each select="../node()">
										<!--Printing the Caption value-->
										<xsl:if test="self::w:r">
											<xsl:call-template name="TempCharacterStyle">
												<xsl:with-param name="characterStyle" select="$characterStyle"/>
											</xsl:call-template>
										</xsl:if>
										<xsl:if test="self::w:fldSimple">
											<xsl:value-of select="w:r/w:t"/>
										</xsl:if>

									</xsl:for-each>
									<xsl:text> </xsl:text>
								</xsl:if>
								<xsl:if test="../following-sibling::w:p[1]/w:pPr/w:bidi[not(@w:val=('0','false','off'))]">
									<xsl:value-of disable-output-escaping="yes" select="'&lt;/bdo&gt;'"/>
									<xsl:value-of disable-output-escaping="yes" select="'&lt;/p&gt;'"/>
								</xsl:if>
							</caption>
						</xsl:if>
						<!--calling the template to handle multiple Prodnotes and Captions applied for an image-->
						<xsl:call-template name="ProcessCaptionProdNote">
							<xsl:with-param name="followingnodes" select="../following-sibling::node()"/>
							<xsl:with-param name="imageId" select="$imageId"/>
							<xsl:with-param name="characterStyle" select="$characterStyle"/>
						</xsl:call-template>
						<xsl:if test="(../preceding-sibling::node()[1]/w:pPr/w:pStyle/@w:val='Caption')">
							<xsl:message terminate="no">translation.oox2Daisy.ImageCaption</xsl:message>
						</xsl:if>
					</imggroup>
					<!--Checking if document is bidirectionally oriented-->
					<xsl:if test="(../w:pPr/w:bidi[not(@w:val=('0','false','off'))]) or (../w:pPr/w:jc/@w:val='right')">
						<xsl:value-of disable-output-escaping="yes" select="'&lt;/bdo&gt;'"/>
					</xsl:if>
				</xsl:when>
				<xsl:otherwise>
					<span>
						<xsl:choose>
							<xsl:when test="exists($imageName)">
								<xsl:sequence select="$imageName" />
							</xsl:when>
							<xsl:otherwise>
								Image <xsl:sequence select="$imageId" />
							</xsl:otherwise>
						</xsl:choose>
						<xsl:text>: </xsl:text>
						<xsl:choose>
							<xsl:when test="exists($alttext)">
								<xsl:value-of select="$alttext" />
							</xsl:when>
							<xsl:otherwise>No description provided</xsl:otherwise>
						</xsl:choose>
					</span>
				</xsl:otherwise>
			</xsl:choose>
			<!--Checking if Img_Id contains null value and returns the fidelity loss message-->
		</xsl:if>
	</xsl:template>
	
	<!--Template for handling multiple Prodnotes and Captions applied for grouped images
	Used only 1 time in Common3.xsl:897 in Imagegroups template
	(All other occurences are recursive calls)
	-->
	<xsl:template name="ProcessProdNoteImggroups">
		<xsl:param name="followingnodes" as="node()*"/>
		<xsl:param name="imageId" as="xs:string"/>
		<xsl:param name="characterStyle" as="xs:boolean"/>
		<xsl:choose>
			<!--Checking for Image-CaptionDAISY custom paragraph style-->
			<xsl:when test="($followingnodes[1]/w:pPr/w:pStyle/@w:val='Image-CaptionDAISY')">
				<xsl:sequence select="d:sink(d:AddCaptionsProdnotes($myObj))"/> <!-- empty -->
				<caption>
					<xsl:attribute name="imgref" select="$imageId"/>
					<!--Getting the language id by calling the PictureLanguage template-->
					<xsl:if test="($followingnodes[1]/w:r/w:rPr/w:lang) or ($followingnodes[1]/w:r/w:rPr/w:rFonts/@w:hint)">
						<!--attribute that holds language id-->
						<xsl:attribute name="xml:lang">
							<!--calling the PictureLanguage template-->
							<xsl:call-template name="PictureLanguage">
								<xsl:with-param name="CheckLang" select="'imagegroup'"/>
							</xsl:call-template>
						</xsl:attribute>
					</xsl:if>
					<!--Checking if image is bidirectionally oriented-->
					<xsl:if test="($followingnodes[1]/w:pPr/w:bidi[not(@w:val=('0','false','off'))]) or ($followingnodes[1]/w:r/w:rPr/w:rtl)">
						<!--Variable holds the value which indicates that the image is bidirectionally oriented-->
						<xsl:variable name="Bd" as="xs:string">
							<!--calling the PictureLanguage template-->
							<xsl:call-template name="PictureLanguage">
								<xsl:with-param name="CheckLang" select="'imagegroup'"/>
							</xsl:call-template>
						</xsl:variable>
						<xsl:value-of disable-output-escaping="yes" select="concat('&lt;p xml:lang=&quot;',$Bd,'&quot;&gt;')"/>
						<xsl:value-of disable-output-escaping="yes" select="concat('&lt;bdo dir= &quot;rtl&quot; xml:lang=&quot;',$Bd,'&quot;&gt;')"/>
					</xsl:if>
					<!--Looping through each of the node to print the text to the output xml-->
					<xsl:for-each select="$followingnodes[1]/node()">
						<xsl:if test="self::w:r">
							<xsl:call-template name="TempCharacterStyle">
								<xsl:with-param name="characterStyle" select="$characterStyle"/>
							</xsl:call-template>
						</xsl:if>
						<xsl:if test="self::w:fldSimple">
							<xsl:value-of select="w:r/w:t"/>
						</xsl:if>
						
					</xsl:for-each>
					<!--Checking for image is bidirectionally oriented-->
					<xsl:if test="$followingnodes[1]/w:pPr/w:bidi[not(@w:val=('0','false','off'))]">
						<xsl:value-of disable-output-escaping="yes" select="'&lt;/bdo&gt;'"/>
						<xsl:value-of disable-output-escaping="yes" select="'&lt;/p&gt;'"/>
					</xsl:if>
				</caption>
				<!--Recursively calling the ProcessCaptionProdNote template till all the Captions are processed-->
				<xsl:call-template name="ProcessProdNoteImggroups">
					<xsl:with-param name="followingnodes" select="$followingnodes[position() > 1]"/>
					<xsl:with-param name="imageId" select="$imageId"/>
					<xsl:with-param name="characterStyle" select="$characterStyle"/>
				</xsl:call-template>
			</xsl:when>
			<!--Checking for Prodnote-OptionalDAISY custom paragraph style-->
			<xsl:when test="($followingnodes[1]/w:pPr/w:pStyle/@w:val='Prodnote-OptionalDAISY')">
				<xsl:sequence select="d:sink(d:AddCaptionsProdnotes($myObj))"/> <!-- empty -->
				<xsl:value-of disable-output-escaping="yes" select="concat('&lt;prodnote render=&quot;optional&quot; imgref=&quot;',$imageId,'&quot;&gt;')"/>
				<!--Checking if image is bidirectionally oriented-->
				<xsl:if test="($followingnodes[1]/w:pPr/w:bidi[not(@w:val=('0','false','off'))]) or ($followingnodes[1]/w:r/w:rPr/w:rtl)">
					<!--Variable holds the value which indicates that the image is bidirectionally oriented-->
					<xsl:variable name="Bd" as="xs:string">
						<!--Calling the PictureLanguage template-->
						<xsl:call-template name="PictureLanguage">
							<xsl:with-param name="CheckLang" select="'imagegroup'"/>
						</xsl:call-template>
					</xsl:variable>
					<xsl:value-of disable-output-escaping="yes" select="concat('&lt;p xml:lang=&quot;',$Bd,'&quot;&gt;')"/>
					<xsl:value-of disable-output-escaping="yes" select="concat('&lt;bdo dir= &quot;rtl&quot; xml:lang=&quot;',$Bd,'&quot;&gt;')"/>
				</xsl:if>
				<!--Looping through each of the node to print the text to the output xml-->
				<xsl:for-each select="$followingnodes[1]/node()">
					<xsl:if test="self::w:r">
						<xsl:call-template name="TempCharacterStyle">
							<xsl:with-param name="characterStyle" select="$characterStyle"/>
						</xsl:call-template>
					</xsl:if>
				</xsl:for-each>
				<!--Checking if image is bidirectionally oriented-->
				<xsl:if test="$followingnodes[1]/w:pPr/w:bidi[not(@w:val=('0','false','off'))]">
					<xsl:value-of disable-output-escaping="yes" select="'&lt;/bdo&gt;'"/>
					<xsl:value-of disable-output-escaping="yes" select="'&lt;/p&gt;'"/>
				</xsl:if>
				<xsl:value-of disable-output-escaping="yes" select="'&lt;/prodnote &gt;'"/>
				<!--Recursively calling the ProcessCaptionProdNote template till all the prodnotes are processed-->
				<xsl:call-template name="ProcessProdNoteImggroups">
					<xsl:with-param name="followingnodes" select="$followingnodes[position() > 1]"/>
					<xsl:with-param name="imageId" select="$imageId"/>
					<xsl:with-param name="characterStyle" select="$characterStyle"/>
				</xsl:call-template>
			</xsl:when>
			<!--Checking for Prodnote-RequiredDAISY custom paragraph style-->
			<xsl:when test="($followingnodes[1]/w:pPr/w:pStyle/@w:val='Prodnote-RequiredDAISY')">
				<xsl:sequence select="d:sink(d:AddCaptionsProdnotes($myObj))"/> <!-- empty -->
				<xsl:value-of disable-output-escaping="yes" select="concat('&lt;prodnote render=&quot;required&quot; imgref=&quot;',$imageId,'&quot;&gt;')"/>
				<!--Getting the language id by calling the PictureLanguage template-->
				<xsl:if test="($followingnodes[1]/w:pPr/w:bidi[not(@w:val=('0','false','off'))]) or ($followingnodes[1]/w:r/w:rPr/w:rtl)">
					<!--attribute that holds language id-->
					<xsl:variable name="Bd" as="xs:string">
						<!--calling the PictureLanguage template-->
						<xsl:call-template name="PictureLanguage">
							<xsl:with-param name="CheckLang" select="'imagegroup'"/>
						</xsl:call-template>
					</xsl:variable>
					<xsl:value-of disable-output-escaping="yes" select="concat('&lt;p xml:lang=&quot;',$Bd,'&quot;&gt;')"/>
					<xsl:value-of disable-output-escaping="yes" select="concat('&lt;bdo dir= &quot;rtl&quot; xml:lang=&quot;',$Bd,'&quot;&gt;')"/>
				</xsl:if>
				<!--Looping through each of the node to print the text to the output xml-->
				<xsl:for-each select="$followingnodes[1]/node()">
					<xsl:if test="self::w:r">
						<xsl:call-template name="TempCharacterStyle">
							<xsl:with-param name="characterStyle" select="$characterStyle"/>
						</xsl:call-template>
					</xsl:if>
				</xsl:for-each>
				<!--Checking if image is bidirectionally oriented-->
				<xsl:if test="$followingnodes[1]/w:pPr/w:bidi[not(@w:val=('0','false','off'))]">
					<xsl:value-of disable-output-escaping="yes" select="'&lt;/bdo&gt;'"/>
					<xsl:value-of disable-output-escaping="yes" select="'&lt;/p&gt;'"/>
				</xsl:if>
				<xsl:value-of disable-output-escaping="yes" select="'&lt;/prodnote &gt;'"/>
				<!--Recursively calling the ProcessCaptionProdNote template till all the prodnotes are processed-->
				<xsl:call-template name="ProcessProdNoteImggroups">
					<xsl:with-param name="followingnodes" select="$followingnodes[position() > 1]"/>
					<xsl:with-param name="imageId" select="$imageId"/>
					<xsl:with-param name="characterStyle" select="$characterStyle"/>
				</xsl:call-template>
			</xsl:when>
		</xsl:choose>
		<xsl:sequence select="d:ResetCaptionsProdnotes($myObj)"/> <!-- empty -->
	</xsl:template>
	
	<!--Template for Implementing grouped images
	Only used 1 time in Common.xsl:1360
	-->
	<xsl:template name="Imagegroups">
		<xsl:param name="characterStyle" as="xs:boolean"/>
		<!--Handling Image-CaptionDAISY custom paragraph style applied above an image-->
		<xsl:if test="../preceding-sibling::node()[1]/w:pPr/w:pStyle/@w:val='Image-CaptionDAISY'">
			<xsl:variable name="caption" as="xs:string*">
				<xsl:for-each select="../preceding-sibling::node()[1]/node()">
					<xsl:if test="self::w:r">
						<xsl:call-template name="TempCharacterStyle">
							<xsl:with-param name="characterStyle" select="$characterStyle"/>
						</xsl:call-template>
					</xsl:if>
					<xsl:if test="self::w:fldSimple">
						<xsl:sequence select="w:r/w:t"/>
					</xsl:if>
					
				</xsl:for-each>
			</xsl:variable>
			<xsl:variable name="caption" as="xs:string" select="string-join($caption,'')"/>
			<xsl:sequence select="d:sink(d:InsertCaption($myObj,$caption))"/> <!-- empty -->
		</xsl:if>
		<!--Looping through each pict element and storing the caption value in the caption variable-->
		
		<xsl:if test="../w:r/w:pict/v:shape/v:textbox/w:txbxContent/w:p/w:pPr/w:pStyle[@w:val='Caption']">
			<xsl:variable name="caption" as="xs:string*">
				<xsl:for-each select="../w:r/w:pict/v:shape/v:textbox/w:txbxContent/w:p/node()">
					<xsl:if test="self::w:r">
						<xsl:call-template name="TempCharacterStyle">
							<xsl:with-param name="characterStyle" select="$characterStyle"/>
						</xsl:call-template>
					</xsl:if>
					<xsl:if test="self::w:fldSimple">
						<xsl:sequence select="w:r/w:t"/>
					</xsl:if>
					
				</xsl:for-each>
			</xsl:variable>
			<xsl:variable name="caption" as="xs:string" select="string-join($caption,'')"/>
			<!--Inserting the caption value in the Arraylist-->
			<xsl:sequence select="d:sink(d:InsertCaption($myObj,$caption))"/> <!-- empty -->
		</xsl:if>
		<xsl:variable name="Imageid" as="xs:string" select ="d:CheckShapeId($myObj,concat('Shape',substring-after(w:pict/v:group/@id,'s')))"/>
		<xsl:variable name="checkImage" as="xs:string" select="d:CheckImage($myObj,concat($Imageid,'.png'))"/>
		<xsl:choose>
			<xsl:when test="$checkImage='1'"> <!-- Image or shape found in output -->
				<imggroup>
					<img id="{$Imageid}" alt="{w:pict/v:group/@alt}" src="{concat($Imageid,'.png')}"/>

					<xsl:variable name="checkcaption" as="xs:string" select="d:ReturnCaption($myObj)"/>
					<!--Checking if checkcaption variables holds any value-->
					<xsl:if test="$checkcaption!='0'">
						<caption>
							<xsl:attribute name="imgref" select="$Imageid"/>
							<!--Creating xml:lang and assinging it the value returned by PictureLanguage template-->
							<xsl:if test="(../../w:r/w:pict/v:shape/v:textbox/w:txbxContent/w:p/w:r/w:rPr/w:lang) or ../../w:r/w:pict/v:shape/v:textbox/w:txbxContent/w:p/w:r/w:rPr/w:rFonts/@w:hint">
								<xsl:attribute name="xml:lang">
									<xsl:call-template name="PictureLanguage">
										<xsl:with-param name="CheckLang" select="'imagegroup'"/>
									</xsl:call-template>
								</xsl:attribute>
							</xsl:if>
							<!--Checking if image is bidirectionally oriented-->
							<xsl:if test="../../w:r/w:pict/v:shape/v:textbox/w:txbxContent/w:p/w:pPr/w:bidi[not(@w:val=('0','false','off'))] or (../../w:r/w:pict/v:shape/v:textbox/w:txbxContent/w:p/w:r/w:rPr/w:rtl)">
								<xsl:variable name="Bd" as="xs:string">
									<xsl:call-template name="PictureLanguage">
										<xsl:with-param name="CheckLang" select="'imagegroup'"/>
									</xsl:call-template>
								</xsl:variable>
								<xsl:value-of disable-output-escaping="yes" select="concat('&lt;p xml:lang=&quot;',$Bd,'&quot;&gt;')"/>
								<xsl:value-of disable-output-escaping="yes" select="concat('&lt;bdo dir= &quot;rtl&quot; xml:lang=&quot;',$Bd,'&quot;&gt;')"/>
							</xsl:if>
							<xsl:value-of select="$checkcaption"/>
							<xsl:if test="../../w:r/w:pict/v:shape/v:textbox/w:txbxContent/w:p/w:pPr/w:bidi[not(@w:val=('0','false','off'))]">
								<xsl:value-of disable-output-escaping="yes" select="'&lt;/bdo&gt;'"/>
								<xsl:value-of disable-output-escaping="yes" select="'&lt;/p&gt;'"/>
							</xsl:if>
						</caption>
					</xsl:if>
					<!--calling the template to handle multiple Prodnotes and Captions applied for image groups-->
					<xsl:call-template name="ProcessProdNoteImggroups">
						<xsl:with-param name="followingnodes" select="../following-sibling::node()"/>
						<xsl:with-param name="imageId" select="$Imageid"/>
						<xsl:with-param name="characterStyle" select="$characterStyle"/>
					</xsl:call-template>
				</imggroup>
			</xsl:when>
			<!-- Shape was exported but an error occured-->
			<!--<xsl:when test="$checkImage='0'">
				<span>Image <xsl:value-of select="$Imageid"/> was not copied to output</span>
			</xsl:when>-->
			<xsl:otherwise>
				<span>
					Image <xsl:sequence select="$Imageid" /> :
					<xsl:choose>
						<xsl:when test="w:pict/v:group/@alt">
							<xsl:value-of select="w:pict/v:group/@alt" />
						</xsl:when>
						<xsl:otherwise>No description provided</xsl:otherwise>
					</xsl:choose>
				</span>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>


	<!-- Only used 1 time in Common.xsl:1344 -->
	<xsl:template name="Imagegroup2003">
		<xsl:param name="characterStyle" as="xs:boolean"/>
		<!--Variable that holds the Image Id-->
		<xsl:variable name="imageId" as="xs:string" select="concat(w:pict/v:shape/v:imagedata/@r:id,d:GenerateImageId($myObj))"/>
		<!--Checking if image is bidirectionally oriented-->
		<xsl:if test="(../w:pPr/w:bidi[not(@w:val=('0','false','off'))]) or (../w:pPr/w:jc/@w:val='right')">
			<xsl:variable name="imgBd" as="xs:string">
				<xsl:call-template name="PictureLanguage">
					<xsl:with-param name="CheckLang" select="'picture'"/>
				</xsl:call-template>
			</xsl:variable>
			<xsl:value-of disable-output-escaping="yes" select="concat('&lt;bdo dir= &quot;rtl&quot; xml:lang=&quot;',$imgBd,'&quot;&gt;')"/>
		</xsl:if>
		<xsl:variable name="checkImage" as="xs:string" select="d:CheckImage($myObj,d:Image($myObj,w:pict/v:shape/v:imagedata/@r:id,w:pict/v:shape/v:imagedata/@o:title))"/>
		<xsl:if test="$checkImage='1'">
			<imggroup>
				<!--variable to store Image name-->
				<xsl:variable name="image2003Name" as="xs:string" select="w:pict/v:shape/v:imagedata/@o:title"/>
				<!--variable to store Image id-->
				<xsl:variable name="rid" as="xs:string" select="w:pict/v:shape/v:imagedata/@r:id"/>
				<img alt="{w:pict/v:shape/@alt}" src="{d:Image($myObj,$rid,$image2003Name)}" id="{$imageId}" />
				<!--Handling Image-CaptionDAISY custom paragraph style applied above an image-->
				<xsl:if test="(../preceding-sibling::node()[1]/w:pPr/w:pStyle/@w:val='Image-CaptionDAISY')or (../w:pPr/w:pStyle/@w:val='Caption') or (../w:pPr/w:pStyle/@w:val='Image-CaptionDAISY')">
					<caption>
						<xsl:attribute name="imgref">
							<xsl:value-of select="$imageId"/>
						</xsl:attribute>
						<xsl:if test="(../following-sibling::w:p[1]/w:r/w:rPr/w:lang) or (../following-sibling::w:p[1]/w:r/w:rPr/w:rFonts/@w:hint)">
							<xsl:attribute name="xml:lang">
								<xsl:call-template name="PictureLanguage">
									<xsl:with-param name="CheckLang" select="'picture'"/>
								</xsl:call-template>
							</xsl:attribute>
						</xsl:if>
						<xsl:if test="(../following-sibling::w:p[1]/w:pPr/w:bidi[not(@w:val=('0','false','off'))]) or (../following-sibling::w:p[1]/w:r/w:rPr/w:rtl)">
							<xsl:variable name="Bd" as="xs:string">
								<xsl:call-template name="PictureLanguage">
									<xsl:with-param name="CheckLang" select="'picture'"/>
								</xsl:call-template>
							</xsl:variable>
							<xsl:value-of disable-output-escaping="yes" select="concat('&lt;p  xml:lang=&quot;',$Bd,'&quot;&gt;')"/>
							<xsl:value-of disable-output-escaping="yes" select="concat('&lt;bdo dir= &quot;rtl&quot; xml:lang=&quot;',$Bd,'&quot;&gt;')"/>
						</xsl:if>
						<xsl:if test="(../preceding-sibling::node()[1]/w:pPr/w:pStyle/@w:val='Image-CaptionDAISY')">
							<xsl:for-each select="../preceding-sibling::node()[1]/node()">
								<!--Printing the Caption value-->
								<xsl:if test="self::w:r">
									<xsl:call-template name="TempCharacterStyle">
										<xsl:with-param name="characterStyle" select="$characterStyle"/>
									</xsl:call-template>
								</xsl:if>
								<xsl:if test="self::w:fldSimple">
									<xsl:value-of select="w:r/w:t"/>
								</xsl:if>
								
							</xsl:for-each>
							<xsl:text> </xsl:text>
						</xsl:if>
						<xsl:if test="(../w:pPr/w:pStyle/@w:val='Caption') or (../w:pPr/w:pStyle/@w:val='Image-CaptionDAISY')">
							<xsl:for-each select="../node()">
								<!--Printing the Caption value-->
								<xsl:if test="self::w:r">
									<xsl:call-template name="TempCharacterStyle">
										<xsl:with-param name="characterStyle" select="$characterStyle"/>
									</xsl:call-template>
								</xsl:if>
								<xsl:if test="self::w:fldSimple">
									<xsl:value-of select="w:r/w:t"/>
								</xsl:if>
								
							</xsl:for-each>
							<xsl:text> </xsl:text>
						</xsl:if>
						<xsl:if test="../following-sibling::w:p[1]/w:pPr/w:bidi[not(@w:val=('0','false','off'))]">
							<xsl:value-of disable-output-escaping="yes" select="'&lt;/bdo&gt;'"/>
							<xsl:value-of disable-output-escaping="yes" select="'&lt;/p&gt;'"/>
						</xsl:if>
					</caption>
				</xsl:if>
				<!--calling the template to handle multiple Prodnotes and Captions applied for an image-->
				<xsl:call-template name="ProcessCaptionProdNote">
					<xsl:with-param name="followingnodes" select="../following-sibling::node()"/>
					<xsl:with-param name="imageId" select="$imageId"/>
					<xsl:with-param name="characterStyle" select="$characterStyle"/>
				</xsl:call-template>
				<!--Capturing Fidelity loss for Captions above the image-->
				<xsl:if test="(../preceding-sibling::node()[1]/w:pPr/w:pStyle/@w:val='Caption')">
					<xsl:message terminate="no">translation.oox2Daisy.ImageCaption</xsl:message>
				</xsl:if>
			</imggroup>
			<!--Checking if image is bidirectionally oriented-->
			<xsl:if test="(../w:pPr/w:bidi[not(@w:val=('0','false','off'))]) or (../w:pPr/w:jc/@w:val='right')">
				<xsl:value-of disable-output-escaping="yes" select="'&lt;/bdo&gt;'"/>
			</xsl:if>
		</xsl:if>
		<xsl:if test="$checkImage='0'">
			<xsl:message terminate="no">translation.oox2Daisy.Image</xsl:message>
		</xsl:if>
	</xsl:template>
	
	<!-- Only used 1 time in Common.xsl:1457 -->
	<xsl:template name="Object">
		<xsl:param name="characterStyle" as="xs:boolean"/>
		<xsl:if test="not(contains(w:object/o:OLEObject/@ProgID,'Equation'))">
			<xsl:if test="(
					contains(w:object/o:OLEObject/@ProgID,'Excel')
					or contains(w:object/o:OLEObject/@ProgID,'Word')
					or contains(w:object/o:OLEObject/@ProgID,'PowerPoint')
				)">
				<xsl:variable name="href" as="xs:string" select="d:Object($myObj,w:object/o:OLEObject/@r:id)"/>
				<xsl:text disable-output-escaping="yes">&lt;a href=&quot;</xsl:text>
				<xsl:value-of select="$href"/>
				<xsl:text disable-output-escaping="yes">&quot; external=&quot;true&quot;&gt;</xsl:text>
				<!--<xsl:value-of disable-output-escaping="yes" select="concat('&lt;a href=&quot;',$href,'&quot; external=&quot;true&quot;&gt;')"/>-->
			</xsl:if>
			<xsl:variable name="ImageName" as="xs:string" select="d:MathImage($myObj,w:object/v:shape/v:imagedata/@r:id)"/>
			<xsl:variable name="id" as="xs:string" select="d:GenerateObjectId($myObj)"/>
			<xsl:variable name="ImageId" as="xs:string" select="concat($ImageName,$id)"/>
			<xsl:variable name="checkImage" as="xs:string" select="d:CheckImage($myObj,$ImageName)"/>
			<xsl:choose>
				<xsl:when test="$checkImage='1'">
					<imggroup>
						<xsl:variable name="alt">
							<xsl:choose>
								<xsl:when test="string-length(w:object/v:shape/@alt)!=0">
									<xsl:value-of select="w:object/v:shape/@alt"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="w:object/o:OLEObject/@ProgID"/>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:variable>
						<img id="{$ImageId}" alt="{$alt}" src="{$ImageName}" />
						<xsl:if test="(../preceding-sibling::node()[1]/w:pPr/w:pStyle/@w:val='Image-CaptionDAISY') or (../w:pPr/w:pStyle/@w:val='Caption') or (../w:pPr/w:pStyle/@w:val='Image-CaptionDAISY')">
							<caption>
								<xsl:attribute name="imgref">
									<xsl:value-of select="$ImageId"/>
								</xsl:attribute>
								<xsl:if test="(../following-sibling::w:p[1]/w:r/w:rPr/w:lang) or (../following-sibling::w:p[1]/w:r/w:rPr/w:rFonts/@w:hint)">
									<xsl:attribute name="xml:lang">
										<xsl:call-template name="PictureLanguage">
											<xsl:with-param name="CheckLang" select="'picture'"/>
										</xsl:call-template>
									</xsl:attribute>
								</xsl:if>
								<xsl:if test="(../following-sibling::w:p[1]/w:pPr/w:bidi[not(@w:val=('0','false','off'))]) or (../following-sibling::w:p[1]/w:r/w:rPr/w:rtl)">
									<xsl:variable name="Bd" as="xs:string">
										<xsl:call-template name="PictureLanguage">
											<xsl:with-param name="CheckLang" select="'picture'"/>
										</xsl:call-template>
									</xsl:variable>
									<xsl:value-of disable-output-escaping="yes" select="concat('&lt;p xml:lang=&quot;',$Bd,'&quot;&gt;')"/>
									<xsl:value-of disable-output-escaping="yes" select="concat('&lt;bdo dir= &quot;rtl&quot; xml:lang=&quot;',$Bd,'&quot;&gt;')"/>
								</xsl:if>
								<xsl:if test="(../preceding-sibling::node()[1]/w:pPr/w:pStyle/@w:val='Image-CaptionDAISY')">
									<xsl:for-each select="../preceding-sibling::node()[1]/node()">
										<!--Printing the Caption value-->
										<xsl:if test="self::w:r">
											<xsl:call-template name="TempCharacterStyle">
												<xsl:with-param name="characterStyle" select="$characterStyle"/>
											</xsl:call-template>
										</xsl:if>
										<xsl:if test="self::w:fldSimple">
											<xsl:value-of select="w:r/w:t"/>
										</xsl:if>

									</xsl:for-each>
								</xsl:if>
								<xsl:if test="(../w:pPr/w:pStyle/@w:val='Caption') or (../w:pPr/w:pStyle/@w:val='Image-CaptionDAISY')">
									<xsl:for-each select="../node()">
										<!--Printing the Caption value-->
										<xsl:if test="self::w:r">
											<xsl:call-template name="TempCharacterStyle">
												<xsl:with-param name="characterStyle" select="$characterStyle"/>
											</xsl:call-template>
										</xsl:if>
										<xsl:if test="self::w:fldSimple">
											<xsl:value-of select="w:r/w:t"/>
										</xsl:if>

									</xsl:for-each>
									<xsl:text> </xsl:text>
								</xsl:if>
								<xsl:if test="../following-sibling::w:p[1]/w:pPr/w:bidi[not(@w:val=('0','false','off'))]">
									<xsl:value-of disable-output-escaping="yes" select="'&lt;/bdo&gt;'"/>
									<xsl:value-of disable-output-escaping="yes" select="'&lt;/p&gt;'"/>
								</xsl:if>
								<!--Printing the field value of the Caption-->
							</caption>
						</xsl:if>
						<xsl:call-template name="ProcessCaptionProdNote">
							<xsl:with-param name="followingnodes" select="../following-sibling::node()"/>
							<xsl:with-param name="imageId" select="$ImageId"/>
							<xsl:with-param name="characterStyle" select="$characterStyle"/>
						</xsl:call-template>

					</imggroup>
					<xsl:if test="(../w:pPr/w:bidi[not(@w:val=('0','false','off'))]) or (../w:pPr/w:jc/@w:val='right')">
						<xsl:value-of disable-output-escaping="yes" select="'&lt;/bdo&gt;'"/>
					</xsl:if>
					<xsl:if test="contains(w:object/o:OLEObject/@ProgID,'Excel') or contains(w:object/o:OLEObject/@ProgID,'Word') or contains(w:object/o:OLEObject/@ProgID,'PowerPoint')">
						<xsl:value-of disable-output-escaping="yes" select="'&lt;/a&gt;'"/>
					</xsl:if>
				</xsl:when>
				<xsl:otherwise>
					<span>Image <xsl:value-of select="$ImageName"/> :
						<xsl:choose>
							<xsl:when test="string-length(w:object/v:shape/@alt)!=0">
								<xsl:value-of select="w:object/v:shape/@alt"/>
							</xsl:when>
							<xsl:when test="w:object/o:OLEObject/@ProgID">
								<xsl:value-of select="w:object/o:OLEObject/@ProgID"/>
							</xsl:when>
							<xsl:when test="w:pict/v:shape/@alt">
								<xsl:value-of select="w:pict/v:shape/@alt" />
							</xsl:when>
							<xsl:otherwise>No description provided</xsl:otherwise>
						</xsl:choose>
					</span>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="tmpShape">
		<xsl:param name="characterStyle" as="xs:boolean"/>
		<xsl:variable name="imageId" as="xs:string">
			<xsl:choose>
				<xsl:when test="(w:pict/v:shape/@id) and (w:pict/v:shape/@o:spid)">
					<xsl:sequence select="d:CheckShapeId($myObj,concat('Shape',substring-after(w:pict/v:shape/@o:spid,'s')))"/>
				</xsl:when>
				<xsl:when test="w:pict/v:shape/@id">
					<xsl:sequence select="d:CheckShapeId($myObj,concat('Shape',substring-after(w:pict/v:shape/@id,'s')))"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:sequence select="d:CheckShapeId($myObj,concat('Shape',substring-after(w:pict//@id,'s')))"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="checkImage" as="xs:string" select="d:CheckImage($myObj,concat($imageId,'.png'))"/>
		<xsl:choose>
			<xsl:when test="$checkImage='1'">
				<imggroup>
					<img alt="{w:pict/v:shape/@alt}" src="{concat($imageId,'.png')}" id="{$imageId}"/>
					<xsl:if test="(
						(../preceding-sibling::node()[1]/w:pPr/w:pStyle/@w:val='Image-CaptionDAISY')
						or (../w:pPr/w:pStyle/@w:val='Caption')
						or (../w:pPr/w:pStyle/@w:val='Image-CaptionDAISY')
					)">
						<caption>
							<xsl:attribute name="imgref">
								<xsl:value-of select="$imageId"/>
							</xsl:attribute>
							<xsl:if test="(../following-sibling::w:p[1]/w:r/w:rPr/w:lang) or (../following-sibling::w:p[1]/w:r/w:rPr/w:rFonts/@w:hint)">
								<xsl:attribute name="xml:lang">
									<xsl:call-template name="PictureLanguage">
										<xsl:with-param name="CheckLang" select="'picture'"/>
									</xsl:call-template>
								</xsl:attribute>
							</xsl:if>
							<xsl:if test="(../following-sibling::w:p[1]/w:pPr/w:bidi[not(@w:val=('0','false','off'))]) or (../following-sibling::w:p[1]/w:r/w:rPr/w:rtl)">
								<xsl:variable name="Bd" as="xs:string">
									<xsl:call-template name="PictureLanguage">
										<xsl:with-param name="CheckLang" select="'picture'"/>
									</xsl:call-template>
								</xsl:variable>
								<xsl:value-of disable-output-escaping="yes" select="concat('&lt;p  xml:lang=&quot;',$Bd,'&quot;&gt;')"/>
								<xsl:value-of disable-output-escaping="yes" select="concat('&lt;bdo dir= &quot;rtl&quot; xml:lang=&quot;',$Bd,'&quot;&gt;')"/>
							</xsl:if>
							<xsl:if test="(../preceding-sibling::node()[1]/w:pPr/w:pStyle/@w:val='Image-CaptionDAISY')">
								<xsl:for-each select="../preceding-sibling::node()[1]/node()">

									<!--Printing the Caption value-->

									<xsl:if test="self::w:r">
										<xsl:call-template name="TempCharacterStyle">
											<xsl:with-param name="characterStyle" select="$characterStyle"/>
										</xsl:call-template>
									</xsl:if>
									<xsl:if test="self::w:fldSimple">
										<xsl:value-of select="w:r/w:t"/>
									</xsl:if>

								</xsl:for-each>
								<xsl:text> </xsl:text>
							</xsl:if>
							<xsl:if test="(../w:pPr/w:pStyle/@w:val='Caption') or (../w:pPr/w:pStyle/@w:val='Image-CaptionDAISY')">
								<xsl:for-each select="../node()">

									<!--Printing the Caption value-->


									<xsl:if test="self::w:r">
										<xsl:call-template name="TempCharacterStyle">
											<xsl:with-param name="characterStyle" select="$characterStyle"/>
										</xsl:call-template>
									</xsl:if>
									<xsl:if test="self::w:fldSimple">
										<xsl:value-of select="w:r/w:t"/>
									</xsl:if>

								</xsl:for-each>
								<xsl:text> </xsl:text>
							</xsl:if>
							<xsl:if test="../following-sibling::w:p[1]/w:pPr/w:bidi[not(@w:val=('0','false','off'))]">
								<xsl:value-of disable-output-escaping="yes" select="'&lt;/bdo&gt;'"/>
								<xsl:value-of disable-output-escaping="yes" select="'&lt;/p&gt;'"/>
							</xsl:if>
						</caption>
						<xsl:if test="../following-sibling::w:p[1]/w:pPr/w:bidi[not(@w:val=('0','false','off'))]">
							<xsl:value-of disable-output-escaping="yes" select="'&lt;/bdo&gt;'"/>
							<xsl:value-of disable-output-escaping="yes" select="'&lt;/p&gt;'"/>
						</xsl:if>
					</xsl:if>
					<xsl:call-template name="ProcessCaptionProdNote">
						<xsl:with-param name="followingnodes" select="../following-sibling::node()"/>
						<xsl:with-param name="imageId" select="$imageId"/>
						<xsl:with-param name="characterStyle" select="$characterStyle"/>
					</xsl:call-template>
				</imggroup>
				<xsl:if test="(../w:pPr/w:bidi[not(@w:val=('0','false','off'))]) or (../w:pPr/w:jc/@w:val='right')">
					<xsl:value-of disable-output-escaping="yes" select="'&lt;/bdo&gt;'"/>
				</xsl:if>
			</xsl:when>
			<xsl:otherwise>
				<span>Image <xsl:value-of select="$imageId"/> :
					<xsl:choose>
						<xsl:when test="w:pict/v:shape/@alt">
							<xsl:value-of select="w:pict/v:shape/@alt" />
						</xsl:when>
						<xsl:otherwise>No description provided</xsl:otherwise>
					</xsl:choose>
				</span>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!--Template for checking section breaks for page numbers-->
	<xsl:template name="SectionBreak">
		<xsl:param name="count" as="xs:integer"/>
		<xsl:param name="node" as="xs:string"/>
		<xsl:sequence select="d:sink(d:InitalizeCheckSectionBody($myObj))"/> <!-- empty -->
		<xsl:sequence select="d:sink(d:ResetSetConPageBreak($myObj))"/> <!-- empty -->
		<xsl:choose>
			<!--if page number for front matter-->
			<xsl:when test="$node='front'">
				<!--incrementing the default page counter-->
				<xsl:sequence select="d:sink(d:IncrementPage($myObj))"/> <!-- empty -->
				<!--Traversing through each node-->
				<xsl:for-each select="following-sibling::node()">
					<xsl:choose>
						<!--Checking for paragraph section break-->
						<xsl:when test="w:pPr/w:sectPr">
							
							<xsl:if test="d:CheckSectionBody($myObj)=1">
								<xsl:choose>
									<!--Checking if page start and page format is present-->
									<xsl:when test="(w:pPr/w:sectPr/w:pgNumType/@w:fmt) and (w:pPr/w:sectPr/w:pgNumType/@w:start)">
										<!--Calling template for page number text-->
										<xsl:call-template name="PageNumber">
											<xsl:with-param name="pagetype" select="w:pPr/w:sectPr/w:pgNumType/@w:fmt"/>
											<xsl:with-param name="matter" select="$node"/>
											<xsl:with-param name="counter" select="$count"/>
										</xsl:call-template>
									</xsl:when>
									<!--Checking if page format is present and not page start-->
									<xsl:when test="(w:pPr/w:sectPr/w:pgNumType/@w:fmt) and not(w:pPr/w:sectPr/w:pgNumType/@w:start)">
										<!--Calling template for page number text-->
										<xsl:call-template name="PageNumber">
											<xsl:with-param name="pagetype" select="w:pPr/w:sectPr/w:pgNumType/@w:fmt"/>
											<xsl:with-param name="matter" select="$node"/>
											<xsl:with-param name="counter" select="$count"/>
										</xsl:call-template>
									</xsl:when>
									<!--Checking if page start is present and not page format-->
									<xsl:when test="not(w:pPr/w:sectPr/w:pgNumType/@w:fmt) and (w:pPr/w:sectPr/w:pgNumType/@w:start)">
										<!--Calling template for page number text-->
										<xsl:call-template name="PageNumber">
											<xsl:with-param name="pagetype" select="w:pPr/w:sectPr/w:pgNumType/@w:fmt"/>
											<xsl:with-param name="matter" select="$node"/>
											<xsl:with-param name="counter" select="$count"/>
										</xsl:call-template>
									</xsl:when>
									<!--If both are not present-->
									<xsl:otherwise>
										<!--Calling template for page number text-->
										<xsl:call-template name="PageNumber">
											<xsl:with-param name="pagetype" select="w:pPr/w:sectPr/w:pgNumType/@w:fmt"/>
											<xsl:with-param name="matter" select="$node"/>
											<xsl:with-param name="counter" select="$count"/>
										</xsl:call-template>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:if>
						</xsl:when>
						<!--Checking for Section in a document-->
						<xsl:when test="self::w:sectPr">
							<xsl:if test="d:CheckSectionBody($myObj)=1">
								<xsl:sequence select="d:sink(d:CheckSectionFront($myObj))"/> <!-- empty -->
								<xsl:choose>
									<!--Checking if page start and page format is present-->
									<xsl:when test="(w:pgNumType/@w:fmt) and (w:pgNumType/@w:start)">
										<!--Calling template for page number text-->
										<xsl:call-template name="PageNumber">
											<xsl:with-param name="pagetype" select="w:pgNumType/@w:fmt"/>
											<xsl:with-param name="matter" select="$node"/>
											<xsl:with-param name="counter" select="$count"/>
										</xsl:call-template>
									</xsl:when>
									<!--Checking if page format is present and not page start-->
									<xsl:when test="(w:pgNumType/@w:fmt) and not(w:pgNumType/@w:start)">
										<!--Calling template for page number text-->
										<xsl:call-template name="PageNumber">
											<xsl:with-param name="pagetype" select="w:pgNumType/@w:fmt"/>
											<xsl:with-param name="matter" select="$node"/>
											<xsl:with-param name="counter" select="$count"/>
										</xsl:call-template>
									</xsl:when>
									<!--Checking if page start is present and not page format-->
									<xsl:when test="not(w:pgNumType/@w:fmt) and (w:pgNumType/@w:start)">
										<!--Calling template for page number text-->
										<xsl:call-template name="PageNumber">
											<xsl:with-param name="pagetype" select="w:pgNumType/@w:fmt"/>
											<xsl:with-param name="matter" select="$node"/>
											<xsl:with-param name="counter" select="$count"/>
										</xsl:call-template>
									</xsl:when>
									<!--If both are not present-->
									<xsl:otherwise>
										<!--Calling template for page number text-->
										<xsl:call-template name="PageNumber">
											<xsl:with-param name="pagetype" select="w:pgNumType/@w:fmt"/>
											<xsl:with-param name="matter" select="$node"/>
											<xsl:with-param name="counter" select="$count"/>
										</xsl:call-template>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:if>
						</xsl:when>
					</xsl:choose>
				</xsl:for-each>
			</xsl:when>
			<!--if page number for body matter-->
            <xsl:when test="$node='body'">
                <xsl:if test="../preceding-sibling::node()[1]/w:pPr/w:sectPr">
                    <xsl:sequence select="d:sink(d:SetConPageBreak($myObj))"/> <!-- empty -->
                </xsl:if>
                <!--Traversing through each node-->
                <xsl:for-each select="../following-sibling::node()">
                    <xsl:choose>
                        <!--Checking for paragraph section break-->
                        <xsl:when test="w:pPr/w:sectPr and d:CheckSection($myObj)=1">
                            <xsl:call-template name="PageNumber">
                                <xsl:with-param name="pagetype">
                                    <xsl:choose>
                                        <xsl:when test="w:pPr/w:sectPr/w:pgNumType/@w:fmt">
                                            <xsl:value-of select="w:pPr/w:sectPr/w:pgNumType/@w:fmt"/>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:value-of select="d:GetPageFormat($myObj)"/>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:with-param>
                                <xsl:with-param name="matter" select="$node"/>
                                <xsl:with-param name="counter">
                                    <xsl:choose>
                                        <xsl:when test="w:pPr/w:sectPr/w:pgNumType/@w:start">
                                            <xsl:value-of select="w:pPr/w:sectPr/w:pgNumType/@w:start"/>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:value-of select="0"/>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:with-param>
                            </xsl:call-template>
                        </xsl:when>
                        <!--Checking for Section in a document-->
                        <xsl:when test="self::w:sectPr and d:CheckSectionBody($myObj)=1">
                            <xsl:call-template name="PageNumber">
                                <xsl:with-param name="pagetype">
                                    <xsl:choose>
                                        <xsl:when test="w:pgNumType/@w:fmt">
                                            <xsl:value-of select="w:pgNumType/@w:fmt"/>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:value-of select="d:GetPageFormat($myObj)"/>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:with-param>
                                <xsl:with-param name="matter" select="$node"/>
                                <xsl:with-param name="counter">
                                    <xsl:choose>
                                        <xsl:when test="w:pgNumType/@w:start">
                                            <xsl:value-of select="w:pgNumType/@w:start"/>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:value-of select="0"/>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:with-param>
                            </xsl:call-template>
                        </xsl:when>
                    </xsl:choose>
                </xsl:for-each>
            </xsl:when>
			<!--Checking for paragraph-->
			<xsl:when test="$node='Para'">
				<xsl:if test="preceding-sibling::node()[1]/w:pPr/w:sectPr">
					<xsl:sequence select="d:sink(d:SetConPageBreak($myObj))"/> <!-- empty -->
				</xsl:if>
				<!--Traversing through each node-->
				<xsl:for-each select="following-sibling::node()">
					<xsl:choose>
						<!--Checking for paragraph section break-->
						<xsl:when test="w:pPr/w:sectPr and d:CheckSectionBody($myObj)=1">
							<xsl:call-template name="PageNumber">
								<xsl:with-param name="pagetype" >
									<xsl:choose>
										<xsl:when test="w:pPr/w:sectPr/w:pgNumType/@w:fmt">
											<xsl:value-of select="w:pPr/w:sectPr/w:pgNumType/@w:fmt"/>
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of select="d:GetPageFormat($myObj)"/>
										</xsl:otherwise>
									</xsl:choose>
								</xsl:with-param>
								<xsl:with-param name="matter" select="$node"/>
								<xsl:with-param name="counter">
									<xsl:choose>
										<xsl:when test="w:pPr/w:sectPr/w:pgNumType/@w:start">
											<xsl:value-of select="w:pPr/w:sectPr/w:pgNumType/@w:start"/>
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of select="0"/>
										</xsl:otherwise>
									</xsl:choose>
								</xsl:with-param> 
							</xsl:call-template>
						</xsl:when>
						<!--Checking for Section in a document-->
						<xsl:when test="self::w:sectPr and d:CheckSectionBody($myObj)=1">
							<xsl:call-template name="PageNumber">
								<xsl:with-param name="pagetype" >
									<xsl:choose>
										<xsl:when test="w:pgNumType/@w:fmt">
											<xsl:value-of select="w:pgNumType/@w:fmt"/>
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of select="d:GetPageFormat($myObj)"/>
										</xsl:otherwise>
									</xsl:choose>
								</xsl:with-param>
								<xsl:with-param name="matter" select="$node"/>
								<xsl:with-param name="counter">
									<xsl:choose>
										<xsl:when test="w:pgNumType/@w:start">
											<xsl:value-of select="w:pgNumType/@w:start"/>
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of select="0"/>
										</xsl:otherwise>
									</xsl:choose>
								</xsl:with-param> 
							</xsl:call-template>
						</xsl:when>
					</xsl:choose>
				</xsl:for-each>
			</xsl:when>
			<!--Checking for bodysection-->
			<xsl:when test="$node='bodysection'">
				<xsl:if test="preceding-sibling::node()[1]/w:pPr/w:sectPr">
					<xsl:sequence select="d:sink(d:SetConPageBreak($myObj))"/> <!-- empty -->
				</xsl:if>
				<xsl:if test="w:pPr/w:sectPr">
					<xsl:call-template name="PageNumber">
						<xsl:with-param name="pagetype">
							<xsl:choose>
								<xsl:when test="w:pPr/w:sectPr/w:pgNumType/@w:fmt">
									<xsl:value-of select="w:pPr/w:sectPr/w:pgNumType/@w:fmt"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="d:GetPageFormat($myObj)"/>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:with-param>
						<xsl:with-param name="matter" select="$node"/>
						<xsl:with-param name="counter">
							<xsl:choose>
								<xsl:when test="w:pPr/w:sectPr/w:pgNumType/@w:start">
									<xsl:value-of select="w:pPr/w:sectPr/w:pgNumType/@w:start"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="0"/>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:with-param>
					</xsl:call-template>
				</xsl:if>
			</xsl:when>
			<!--Checking for Table-->
			<xsl:when test="$node='Table'">
				<xsl:if test="../../preceding-sibling::node()[1]/w:pPr/w:sectPr">
					<xsl:sequence select="d:sink(d:SetConPageBreak($myObj))"/> <!-- empty -->
				</xsl:if>
				<!--Traversing through each node-->
				<xsl:for-each select="../../following-sibling::node()">
					<xsl:choose>
						<!--Checking for paragraph section break-->
						<xsl:when test="w:pPr/w:sectPr">
							<xsl:call-template name="PageNumber">
								<xsl:with-param name="pagetype" >
									<xsl:choose>
										<xsl:when test="w:pPr/w:sectPr/w:pgNumType/@w:fmt">
											<xsl:value-of select="w:pPr/w:sectPr/w:pgNumType/@w:fmt"/>
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of select="d:GetPageFormat($myObj)"/>
										</xsl:otherwise>
									</xsl:choose>
								</xsl:with-param>
								<xsl:with-param name="matter" select="$node"/>
								<xsl:with-param name="counter">
									<xsl:choose>
										<xsl:when test="w:pPr/w:sectPr/w:pgNumType/@w:start">
											<xsl:value-of select="w:pPr/w:sectPr/w:pgNumType/@w:start"/>
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of select="0"/>
										</xsl:otherwise>
									</xsl:choose>
								</xsl:with-param> 
							</xsl:call-template>
						</xsl:when>
						<!--Checking for Section in a document-->
						<xsl:when test="self::w:sectPr and d:CheckSectionBody($myObj)=1">
							<xsl:call-template name="PageNumber">
								<xsl:with-param name="pagetype" >
									<xsl:choose>
										<xsl:when test="w:pgNumType/@w:fmt">
											<xsl:value-of select="w:pgNumType/@w:fmt"/>
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of select="d:GetPageFormat($myObj)"/>
										</xsl:otherwise>
									</xsl:choose>
								</xsl:with-param>
								<xsl:with-param name="matter" select="'body'"/>
								<xsl:with-param name="counter">
									<xsl:choose>
										<xsl:when test="w:pgNumType/@w:start">
											<xsl:value-of select="w:pgNumType/@w:start"/>
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of select="0"/>
										</xsl:otherwise>
									</xsl:choose>
								</xsl:with-param> 
							</xsl:call-template>
						</xsl:when>
					</xsl:choose>
				</xsl:for-each>
			</xsl:when>
		</xsl:choose>
	</xsl:template>
	
	<!--Template for counting number of pages before TOC-->
	<xsl:template name="countpageTOC">
		<xsl:for-each select="preceding-sibling::*">
			<xsl:choose>
				<!--Checking for page break in TOC-->
				<xsl:when test="(w:r/w:br/@w:type='page') or (w:r/w:lastRenderedPageBreak)">
					<xsl:sequence select="d:sink(d:PageForTOC($myObj))"/> <!-- empty -->
					<xsl:sequence select="d:sink(d:IncrementPage($myObj))"/> <!-- empty -->
					<xsl:if test="not(w:r/w:t)">
						<!--Calling template for initializing page number info-->
						<xsl:call-template name="SectionBreak">
							<xsl:with-param name="count" select="d:ReturnPageNum($myObj)"/>
							<xsl:with-param name="node" select="'front'"/>
						</xsl:call-template>
						<!--producer note for empty text-->
						<prodnote render="optional">Blank Page</prodnote>
					</xsl:if>
				</xsl:when>
				<xsl:when test="(w:sdtContent/w:p/w:r/w:br/@w:type='page') or (w:sdtContent/w:p/w:r/lastRenderedPageBreak)">
					<xsl:sequence select="d:sink(d:PageForTOC($myObj))"/> <!-- empty -->
					<xsl:sequence select="d:sink(d:IncrementPage($myObj))"/> <!-- empty -->
				</xsl:when>
			</xsl:choose>
		</xsl:for-each>
		<xsl:variable name="countPage" as="xs:integer" select="d:PageForTOC($myObj) - 1"/>
		<xsl:call-template name="SectionBreak">
			<xsl:with-param name="count" select="$countPage"/>
			<xsl:with-param name="node" select="'front'"/>
		</xsl:call-template>
	</xsl:template>
	
	<!--Template to translate page number information-->
	<xsl:template name="PageNumber">
		<xsl:param name="pagetype" as="xs:string"/>
		<xsl:param name="matter" as="xs:string"/>
		<xsl:param name="counter" as="xs:integer"/>
		<xsl:choose>
			<xsl:when test="d:GetCurrentMatterType($myObj)='Frontmatter'">
				<xsl:if test="not((d:SetConPageBreak($myObj)&gt;1) and (w:type/@w:val='continuous'))">
					<xsl:variable name="count" as="xs:integer" select="d:IncrementPageNo($myObj)-1"/>
					<xsl:choose>
						<!--LowerRoman page number-->
						<xsl:when test="$pagetype='lowerRoman'">
							<pagenum page="front" id="{concat('page',d:GeneratePageId($myObj))}">
								<xsl:value-of select="d:PageNumLowerRoman($count)"/>
							</pagenum>
						</xsl:when>
						<!--UpperRoman page number-->
						<xsl:when test="$pagetype='upperRoman'">
							<pagenum page="front" id="{concat('page',d:GeneratePageId($myObj))}">
								<xsl:value-of select="d:PageNumUpperRoman($count)"/>
							</pagenum>
						</xsl:when>
						<!--LowerLetter page number-->
						<xsl:when test="$pagetype='lowerLetter'">
							<pagenum page="front" id="{concat('page',d:GeneratePageId($myObj))}">
								<xsl:value-of select="d:PageNumLowerAlphabet($count)"/>
							</pagenum>
						</xsl:when>
						<!--UpperLetter page number-->
						<xsl:when test="$pagetype='upperLetter'">
							<pagenum page="front" id="{concat('page',d:GeneratePageId($myObj))}">
								<xsl:value-of select="d:PageNumUpperAlphabet($count)"/>
							</pagenum>
						</xsl:when>
						<!--Page number with dash-->
						<xsl:when test="$pagetype='numberInDash'">
							<pagenum page="front" id="{concat('page',d:GeneratePageId($myObj))}">
								<xsl:value-of select="concat('-',$count,'-')"/>
							</pagenum>
						</xsl:when>
						<!--Normal page number-->
						<xsl:otherwise>
							<xsl:choose>
								<xsl:when test="$counter=0 and d:GetSectionFront($myObj)=1">
									<pagenum page="front" id="{concat('page',d:GeneratePageId($myObj))}">
										<xsl:value-of select="$count"/>
									</pagenum>
								</xsl:when>
								<xsl:when test="$counter=0 and d:GetSectionFront($myObj)=0">
									<pagenum page="front" id="{concat('page',d:GeneratePageId($myObj))}">
										<xsl:value-of select="d:ReturnPageNum($myObj)"/>
									</pagenum>
								</xsl:when>
								<xsl:otherwise>
									<pagenum page="front" id="{concat('page',d:GeneratePageId($myObj))}">
										<xsl:value-of select="$count"/>
									</pagenum>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:if>
			</xsl:when>
			
			<xsl:when test="d:GetCurrentMatterType($myObj)='Bodymatter'">
				<xsl:if test="not((d:SetConPageBreak($myObj)&gt;1) and (w:type/@w:val='continuous'))">
					<xsl:variable name="count" as="xs:integer" select="d:IncrementPageNo($myObj)-1"/>
					<xsl:choose>
						<!--LowerRoman page number-->
						<xsl:when test="$pagetype='lowerRoman'">
							<pagenum page="special" id="{concat('page',d:GeneratePageId($myObj))}">
								<xsl:value-of select="d:PageNumLowerRoman($count)"/>
							</pagenum>
						</xsl:when>
						<!--UpperRoman page number-->
						<xsl:when test="$pagetype='upperRoman'">
							<pagenum page="special" id="{concat('page',d:GeneratePageId($myObj))}">
								<xsl:value-of select="d:PageNumUpperRoman($count)"/>
							</pagenum>
						</xsl:when>
						<!--LowerLetter page number-->
						<xsl:when test="$pagetype='lowerLetter'">
							<pagenum page="special" id="{concat('page',d:GeneratePageId($myObj))}">
								<xsl:value-of select="d:PageNumLowerAlphabet($count)"/>
							</pagenum>
						</xsl:when>
						<!--UpperLetter page number-->
						<xsl:when test="$pagetype='upperLetter'">
							<pagenum page="special" id="{concat('page',d:GeneratePageId($myObj))}">
								<xsl:value-of select="d:PageNumUpperAlphabet($count)"/>
							</pagenum>
						</xsl:when>
						<!--Page number with dash-->
						<xsl:when test="$pagetype='numberInDash'">
							<pagenum page="special" id="{concat('page',d:GeneratePageId($myObj))}">
								<xsl:value-of select="concat('-',$count,'-')"/>
							</pagenum>
						</xsl:when>
						<!--Normal page number-->
						<xsl:otherwise>
							<xsl:choose>
								<xsl:when test="$counter=0 and d:GetSectionFront($myObj)=1">
									<pagenum page="normal" id="{concat('page',d:GeneratePageId($myObj))}">
										<xsl:value-of select="$count"/>
									</pagenum>
								</xsl:when>
								<xsl:when test="$counter=0 and d:GetSectionFront($myObj)=0">
									<pagenum page="normal" id="{concat('page',d:GeneratePageId($myObj))}">
										<xsl:value-of select="d:ReturnPageNum($myObj)"/>
									</pagenum>
								</xsl:when>
								<xsl:otherwise>
									<pagenum page="normal" id="{concat('page',d:GeneratePageId($myObj))}">
										<xsl:value-of select="$count"/>
									</pagenum>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:if>
			</xsl:when>
			
			<xsl:when test="d:GetCurrentMatterType($myObj)='Rearmatter'">
				<xsl:if test="not((d:SetConPageBreak($myObj)&gt;1) and (w:type/@w:val='continuous'))">
					<xsl:variable name="count" as="xs:integer" select="d:IncrementPageNo($myObj)-1"/>
					<xsl:choose>
						<!--LowerRoman page number-->
						<xsl:when test="$pagetype='lowerRoman'">
							<pagenum page="special" id="{concat('page',d:GeneratePageId($myObj))}">
								<xsl:value-of select="d:PageNumLowerRoman($count)"/>
							</pagenum>
						</xsl:when>
						<!--UpperRoman page number-->
						<xsl:when test="$pagetype='upperRoman'">
							<pagenum page="special" id="{concat('page',d:GeneratePageId($myObj))}">
								<xsl:value-of select="d:PageNumUpperRoman($count)"/>
							</pagenum>
						</xsl:when>
						<!--LowerLetter page number-->
						<xsl:when test="$pagetype='lowerLetter'">
							<pagenum page="special" id="{concat('page',d:GeneratePageId($myObj))}">
								<xsl:value-of select="d:PageNumLowerAlphabet($count)"/>
							</pagenum>
						</xsl:when>
						<!--UpperLetter page number-->
						<xsl:when test="$pagetype='upperLetter'">
							<pagenum page="special" id="{concat('page',d:GeneratePageId($myObj))}">
								<xsl:value-of select="d:PageNumUpperAlphabet($count)"/>
							</pagenum>
						</xsl:when>
						<!--Page number with dash-->
						<xsl:when test="$pagetype='numberInDash'">
							<pagenum page="special" id="{concat('page',d:GeneratePageId($myObj))}">
								<xsl:value-of select="concat('-',$count,'-')"/>
							</pagenum>
						</xsl:when>
						<!--Normal page number-->
						<xsl:otherwise>
							<xsl:choose>
								<xsl:when test="$counter=0 and d:GetSectionFront($myObj)=1">
									<pagenum page="special" id="{concat('page',d:GeneratePageId($myObj))}">
										<xsl:value-of select="$count"/>
									</pagenum>
								</xsl:when>
								<xsl:when test="$counter=0 and d:GetSectionFront($myObj)=0">
									<pagenum page="special" id="{concat('page',d:GeneratePageId($myObj))}">
										<xsl:value-of select="d:ReturnPageNum($myObj)"/>
									</pagenum>
								</xsl:when>
								<xsl:otherwise>
									<pagenum page="special" id="{concat('page',d:GeneratePageId($myObj))}">
										<xsl:value-of select="$count"/>
									</pagenum>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:if>
			</xsl:when>
			<!--Frontmatter page number-->
			<xsl:when test="$matter='front'">
				<xsl:if test="d:GetSectionFront($myObj)=1">
					<xsl:sequence select="d:sink(d:IncrementPageNo($myObj))"/> <!-- empty -->
				</xsl:if>
				<xsl:choose>
					<!--LowerRoman page number-->
					<xsl:when test="$pagetype='lowerRoman'">
						<xsl:variable name="pageno" as="xs:string" select="d:PageNumLowerRoman($counter)"/>
						<pagenum page="front" id="{concat('page',d:GeneratePageId($myObj))}">
							<xsl:value-of select="$pageno"/>
						</pagenum>
					</xsl:when>
					<!--UpperRoman page number-->
					<xsl:when test="$pagetype='upperRoman'">
						<xsl:variable name="pageno" as="xs:string" select="d:PageNumUpperRoman($counter)"/>
						<pagenum page="front" id="{concat('page',d:GeneratePageId($myObj))}">
							<xsl:value-of select="$pageno"/>
						</pagenum>
					</xsl:when>
					<!--LowerLetter page number-->
					<xsl:when test="$pagetype='lowerLetter'">
						<xsl:variable name="pageno" as="xs:string" select="d:PageNumLowerAlphabet($counter)"/>
						<pagenum page="front" id="{concat('page',d:GeneratePageId($myObj))}">
							<xsl:value-of select="$pageno"/>
						</pagenum>
					</xsl:when>
					<!--UpperLetter page number-->
					<xsl:when test="$pagetype='upperLetter'">
						<xsl:variable name="pageno" as="xs:string" select="d:PageNumUpperAlphabet($counter)"/>
						<pagenum page="front" id="{concat('page',d:GeneratePageId($myObj))}">
							<xsl:value-of select="$pageno"/>
						</pagenum>
					</xsl:when>
					<!--Page number with dash-->
					<xsl:when test="$pagetype='numberInDash'">
						<pagenum page="front" id="{concat('page',d:GeneratePageId($myObj))}">
							<xsl:value-of select="concat('-',$counter,'-')"/>
						</pagenum>
					</xsl:when>
					<!--Normal page number-->
					<xsl:otherwise>
						<pagenum page="front" id="{concat('page',d:GeneratePageId($myObj))}">
							<xsl:value-of select="$counter"/>
						</pagenum>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<!--Bodymatter page number-->
			<xsl:when test="($matter='body') or ($matter='bodysection') or ($matter='Para')">
				<xsl:if test="not((d:SetConPageBreak($myObj)&gt;1) and (w:type/@w:val='continuous'))">
					<xsl:variable name="count" as="xs:integer" select="d:IncrementPageNo($myObj)-1"/>
					<xsl:choose>
						<!--LowerRoman page number-->
						<xsl:when test="$pagetype='lowerRoman'">
							<pagenum page="special" id="{concat('page',d:GeneratePageId($myObj))}">
								<xsl:value-of select="d:PageNumLowerRoman($count)"/>
							</pagenum>
						</xsl:when>
						<!--UpperRoman page number-->
						<xsl:when test="$pagetype='upperRoman'">
							<pagenum page="special" id="{concat('page',d:GeneratePageId($myObj))}">
								<xsl:value-of select="d:PageNumUpperRoman($count)"/>
							</pagenum>
						</xsl:when>
						<!--LowerLetter page number-->
						<xsl:when test="$pagetype='lowerLetter'">
							<pagenum page="special" id="{concat('page',d:GeneratePageId($myObj))}">
								<xsl:value-of select="d:PageNumLowerAlphabet($count)"/>
							</pagenum>
						</xsl:when>
						<!--UpperLetter page number-->
						<xsl:when test="$pagetype='upperLetter'">
							<pagenum page="special" id="{concat('page',d:GeneratePageId($myObj))}">
								<xsl:value-of select="d:PageNumUpperAlphabet($count)"/>
							</pagenum>
						</xsl:when>
						<!--Page number with dash-->
						<xsl:when test="$pagetype='numberInDash'">
							<pagenum page="special" id="{concat('page',d:GeneratePageId($myObj))}">
								<xsl:value-of select="concat('-',$count,'-')"/>
							</pagenum>
						</xsl:when>
						<!--Normal page number-->
						<xsl:otherwise>
							<xsl:choose>
								<xsl:when test="$counter=0 and d:GetSectionFront($myObj)=1">
									<pagenum page="normal" id="{concat('page',d:GeneratePageId($myObj))}">
										<xsl:value-of select="$count"/>
									</pagenum>
								</xsl:when>
								<xsl:when test="$counter=0 and d:GetSectionFront($myObj)=0">
									<pagenum page="normal" id="{concat('page',d:GeneratePageId($myObj))}">
										<xsl:value-of select="d:ReturnPageNum($myObj)"/>
									</pagenum>
								</xsl:when>
								<xsl:otherwise>
									<pagenum page="normal" id="{concat('page',d:GeneratePageId($myObj))}">
										<xsl:value-of select="$count"/>
									</pagenum>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:if>
			</xsl:when>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template name="GetParagraphLanguage">
		<xsl:param name="paragraphNode" /> <!-- Expects a w:p run node-->
		<xsl:variable name="paragraphStyleId" select="$paragraphNode/w:pPr/w:pStyle/@w:val" />
		<xsl:variable name="paragraphStyle">
			<xsl:choose>
				<xsl:when test="$paragraphStyleId">
					<xsl:value-of select="$styles/w:style[@w:type='paragraph' and @w:styleId=$paragraphStyleId]/w:rPr" />
				</xsl:when>
				<!-- Use the default Normal type (note : there might be style a more complexe style hierarchy not handled here) -->
				<xsl:when test="$styles/w:style[@w:type='paragraph' and (@w:styleId='Normal' or w:default='1')]/w:rPr">
					<xsl:value-of select="$styles/w:style[@w:type='paragraph' and (@w:styleId='Normal' or w:default='1')]/w:rPr" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$styles/w:docDefaults/w:rPrDefault/w:rPr" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="paragraphLatin">
			<xsl:choose>
				<xsl:when test="$paragraphStyleId and $styles/w:style[@w:type='paragraph' and @w:styleId=$paragraphStyleId]/w:rPr/w:lang/@w:val">
					<xsl:value-of select="$styles/w:style[@w:type='paragraph' and @w:styleId=$paragraphStyleId]/w:rPr/w:lang/@w:val" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$defaultLatin"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="paragraphEastAsia">
			<xsl:choose>
				<xsl:when test="$paragraphStyleId and $styles/w:style[@w:type='paragraph' and @w:styleId=$paragraphStyleId]/w:rPr/w:lang/@w:eastAsia">
					<xsl:value-of select="$styles/w:style[@w:type='paragraph' and @w:styleId=$paragraphStyleId]/w:rPr/w:lang/@w:eastAsia" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$defaultEastAsia"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="paragraphComplex">
			<xsl:choose>
				<xsl:when test="$paragraphStyleId and $styles/w:style[@w:type='paragraph' and @w:styleId=$paragraphStyleId]/w:rPr/w:lang/@w:bidi">
					<xsl:value-of select="$styles/w:style[@w:type='paragraph' and @w:styleId=$paragraphStyleId]/w:rPr/w:lang/@w:bidi" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$defaultComplex"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		
		<!-- deduce languages from runner first.
		     Languages are computed once per run and deduplicated / counted using
		     xsl:for-each-group (hash based, O(n)) instead of repeated preceding/following-sibling
		     scans over temporary trees (O(n²) on paragraphs with many runs). -->
		<xsl:variable name="runnerLanguages" as="xs:string*">
			<xsl:for-each select="$paragraphNode/w:r">
				<xsl:variable name="found" as="xs:string">
					<xsl:call-template name="GetRunLanguage">
						<xsl:with-param name="runNode" select="." />
					</xsl:call-template>
				</xsl:variable>
				<xsl:sequence select="$found"/>
			</xsl:for-each>
		</xsl:variable>
		<!-- Most frequent runner language (ties keep first appearance order) -->
		<xsl:variable name="topRunnerLanguage" as="xs:string?">
			<xsl:for-each-group select="$runnerLanguages" group-by=".">
				<xsl:sort select="count(current-group())" data-type="number" order="descending"/>
				<xsl:if test="position()=1">
					<xsl:sequence select="current-grouping-key()"/>
				</xsl:if>
			</xsl:for-each-group>
		</xsl:variable>
		<xsl:choose>
			<!-- Prioritize the language count (at least one run, even if its language is an empty string) -->
			<xsl:when test="exists($runnerLanguages)">
				<xsl:value-of select="$topRunnerLanguage"/>
			</xsl:when>
			<!-- then check if east asia is used as default -->
			<xsl:when test="w:rPr/w:eastAsianLayout or (w:rPr/w:rFonts/@w:hint='eastAsia') or (w:pPr/w:rPr/w:rFonts/@w:hint='eastAsia')">
				<xsl:value-of select="$paragraphEastAsia"/>
			</xsl:when>
			<!-- then check if complex is used as default -->
			<xsl:when test="w:rPr/w:cs or (w:rPr/w:rFonts/@w:hint='cs') or (w:pPr/w:rPr/w:rFonts/@w:hint='cs')">
				<xsl:value-of select="$paragraphComplex"/>
			</xsl:when>
			<!-- default as latin -->
			<xsl:otherwise>
				<xsl:value-of select="$paragraphLatin"/>
			</xsl:otherwise>
		</xsl:choose>
		
	</xsl:template>
	
	<!-- Port of the language detection for runner and paragraph that is used in WordToEPUB -->
	<xsl:template name="GetRunLanguage">
		<xsl:param name="runNode" />  <!-- Expects a w:r run node-->
		
		<!-- Run and Paragraph possible style id-->
		<xsl:variable name="characterStyleId" select="$runNode/w:rPr/w:rStyle/@w:val" />
		<xsl:variable name="paragraphStyleId" select="$runNode/../w:pPr/w:pStyle/@w:val" />
		
		<!--try to retrieve language not defined in the node itself : -->
		<!-- 1 - from runner character style-->
		<xsl:variable name="characterStyle">
			<xsl:choose>
				<xsl:when test="$characterStyleId">
					<xsl:value-of select="$styles/w:style[@w:type='character' and @w:styleId=$characterStyleId]/w:rPr" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$styles/w:style[@w:type='character' and @w:default='1']/w:rPr" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<!-- 2 - paragraph runners properties lang -->
		<xsl:variable name="paragraphRunProperties" select="$runNode/../w:pPr/w:rPr" />
		<!-- 3 - paragraph styles lang -->
		<xsl:variable name="paragraphStyle">
			<xsl:choose>
				<xsl:when test="$paragraphStyleId">
					<xsl:value-of select="$styles/w:style[@w:type='paragraph' and @w:styleId=$paragraphStyleId]/w:rPr" />
				</xsl:when>
				<!-- Use the default Normal type (note : there might be style a more complexe style hierarchy not handled here) -->
				<xsl:otherwise>
					<xsl:value-of select="$styles/w:style[@w:type='paragraph' and @w:styleId='Normal']/w:rPr" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<!-- Default document languages -->
		
		<!-- And now evaluate the possible language based on what was found -->
		<xsl:variable name="runLatin">
			<xsl:choose>
				<!-- 1 - Check current properties first -->
				<xsl:when test="$runNode/w:rPr/w:lang/@w:val">
					<xsl:value-of select="$runNode/w:rPr/w:lang/@w:val" />
				</xsl:when>
				<!-- 2 - Check custom or default character style properties -->
				<xsl:when test="$characterStyle/w:lang/@w:val">
					<xsl:value-of select="$characterStyle/w:lang/@w:val" />
				</xsl:when>
				<!-- 3 - Check paragraph properties -->
				<xsl:when test="$paragraphRunProperties/w:lang/@w:val">
					<xsl:value-of select="$paragraphRunProperties/w:lang/@w:val" />
				</xsl:when>
				<!-- 4 - Check paragraph styles -->
				<xsl:when test="$paragraphStyle/w:lang/@w:val">
					<xsl:value-of select="$paragraphStyle/w:lang/@w:val" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$defaultLatin" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="runEastAsia">
			<xsl:choose>
				<!-- 1 - Check current properties first -->
				<xsl:when test="$runNode/w:rPr/w:lang/@w:eastAsia">
					<xsl:value-of select="$runNode/w:rPr/w:lang/@w:eastAsia" />
				</xsl:when>
				<!-- 2 - Check custom or default character style properties -->
				<xsl:when test="$characterStyle/w:lang/@w:eastAsia">
					<xsl:value-of select="$characterStyle/w:lang/@w:eastAsia" />
				</xsl:when>
				<!-- 3 - Check paragraph properties -->
				<xsl:when test="$paragraphRunProperties/w:lang/@w:eastAsia">
					<xsl:value-of select="$paragraphRunProperties/w:lang/@w:eastAsia" />
				</xsl:when>
				<!-- 4 - Check paragraph styles -->
				<xsl:when test="$paragraphStyle/w:lang/@w:eastAsia">
					<xsl:value-of select="$paragraphStyle/w:lang/@w:eastAsia" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$defaultEastAsia" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="runComplex">
			<xsl:choose>
				<!-- 1 - Check current properties first -->
				<xsl:when test="$runNode/w:rPr/w:lang/@w:bidi">
					<xsl:value-of select="$runNode/w:rPr/w:lang/@w:bidi" />
				</xsl:when>
				<!-- 2 - Check custom or default character style properties -->
				<xsl:when test="$characterStyle/w:lang/@w:bidi">
					<xsl:value-of select="$characterStyle/w:lang/@w:bidi" />
				</xsl:when>
				<!-- 3 - Check paragraph properties -->
				<xsl:when test="$paragraphRunProperties/w:lang/@w:bidi">
					<xsl:value-of select="$paragraphRunProperties/w:lang/@w:bidi" />
				</xsl:when>
				<!-- 4 - Check paragraph styles -->
				<xsl:when test="$paragraphStyle/w:lang/@w:bidi">
					<xsl:value-of select="$paragraphStyle/w:lang/@w:bidi" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$defaultComplex" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<!-- Now resolve if we are in latin or east asia or complex -->
		<!-- Code used by richard check characters :
							Check for the first character in the run (that is not space or punctuation)
							- Consider Latin by default
							- if character starts by an East asian one (checked with regex and ranges of unicode characters)
							- else if character starts by an Bidirectionnal one (checked with regex and ranges of unicode characters)
							
							also checks runner style layout
							note : the older code also check (w:r/w:rPr/w:rFonts/@w:hint) to see if hint of the font used was cs or eastAsia
		-->
		
		<xsl:variable name="innerText" select="translate(normalize-space($runNode/w:t/text()), $ignorableCharacters, '')" />
		<xsl:choose>
			<!-- Not sure about the character test, also adding cs and layout check as backup-->
			<xsl:when test="string-length($innerText) &gt; 0">
				<xsl:choose>
					<xsl:when test="d:IsEastAsia($myObj, substring($innerText,1,1))
						or $runNode/w:rPr/w:eastAsianLayout
						or ($runNode/w:rPr/w:rFonts/@w:hint='eastAsia')">
						<xsl:value-of select="$runEastAsia"/>
					</xsl:when>
					<xsl:when test="d:IsBiDi($myObj, substring($innerText,1,1))
						or $runNode/w:rPr/w:cs
						or ($runNode/w:rPr/w:rFonts/@w:hint='cs')">
						<xsl:value-of select="$runComplex"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="$runLatin"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:choose>
					<!-- Required to avoid breaks in sentences that are written in complex script
					like including numbers in indi or east asian text
					Not sure if it is required or not -->
					<xsl:when test="$runNode/preceding-sibling::w:r[1]/w:t">
						<xsl:call-template name="GetRunLanguage">
							<xsl:with-param name="runNode" select="$runNode/preceding-sibling::w:r[1]" />
						</xsl:call-template>
					</xsl:when>
					<!--<xsl:when test="$runNode/following-sibling::w:r[1]/w:t">
										<xsl:call-template name="GetRunLanguage">
										<xsl:with-param name="runNode" select="$runNode/following-sibling::w:r[1]" />
										</xsl:call-template>
										</xsl:when>-->
					<!-- Check east asian layout -->
					<xsl:when test="$runNode/w:rPr/w:eastAsianLayout or ($runNode/w:rPr/w:rFonts/@w:hint='eastAsia')">
						<xsl:value-of select="$runEastAsia"/>
					</xsl:when>
					<!-- Check complex script -->
					<xsl:when test="$runNode/w:rPr/w:cs or ($runNode/w:rPr/w:rFonts/@w:hint='cs')">
						<xsl:value-of select="$runComplex"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="$runLatin"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!--Template to implement Languages-->
	<xsl:template name="PictureLanguage" as="xs:string">
		<xsl:param name="CheckLang" as="xs:string"/>
		<xsl:choose>
			<!--Checking languge for picture-->
			<xsl:when test="$CheckLang='picture'">
				<xsl:variable name="count_lang" as="xs:integer" select="xs:integer(string-join(('0',../following-sibling::w:p[1]/w:r[1]/w:rPr/w:lang/count(@*)),''))"/>
				<xsl:choose>
					<!--Checking for language type eastAsia-->
					<xsl:when test="../following-sibling::w:p[1]/w:r/w:rPr/w:rFonts/@w:hint='eastAsia'">
						<xsl:choose>
							<!--Getting value from eastasia attribute in lang tag-->
							<xsl:when test="../following-sibling::w:p[1]/w:r/w:rPr/w:lang/@w:eastAsia">
								<xsl:sequence select="(../following-sibling::w:p[1]/w:r/w:rPr/w:lang/@w:eastAsia)[1]"/>
							</xsl:when>
							<!--Assinging default eastAsia language-->
							<xsl:otherwise>
								<xsl:sequence select="$defaultEastAsia"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:when>
					<!--Checking for language type CS-->
					<xsl:when test="../following-sibling::w:p[1]/w:r/w:rPr/w:rFonts/@w:hint='cs'">
						<xsl:choose>
							<!--Checking for bidirectional language-->
							<xsl:when test="../following-sibling::w:p[1]/w:r/w:rPr/w:lang/@w:bidi">
								<!--Getting value from bidi attribute in lang tag-->
								<xsl:sequence select="(../following-sibling::w:p[1]/w:r/w:rPr/w:lang/@w:bidi)[1]"/>
							</xsl:when>
							<!--Assinging default bidirectional language-->
							<xsl:otherwise>
								<xsl:sequence select="$defaultComplex"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:when>
					<xsl:otherwise>
						<xsl:choose>
							<xsl:when test="$count_lang &gt;1">
								<xsl:message terminate="no"> In $count_lang &gt;1</xsl:message>
								<xsl:choose>
									<xsl:when test="../following-sibling::w:p[1]/w:r/w:rPr/w:lang/@w:val">
										<xsl:sequence select="(../following-sibling::w:p[1]/w:r/w:rPr/w:lang/@w:val)[1]"/>
									</xsl:when>
									<xsl:otherwise>
										<xsl:sequence select="$defaultLatin"/>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:when>
							<xsl:when test="$count_lang=1">
								<xsl:choose>
									<xsl:when test="../following-sibling::w:p[1]/w:r/w:rPr/w:lang/@w:val">
										<xsl:sequence select="(../following-sibling::w:p[1]/w:r/w:rPr/w:lang/@w:val)[1]"/>
									</xsl:when>
									<xsl:when test="../following-sibling::w:p[1]/w:r/w:rPr/w:lang/@w:eastAsia">
										<xsl:sequence select="(../following-sibling::w:p[1]/w:r/w:rPr/w:lang/@w:eastAsia)[1]"/>
									</xsl:when>
									<xsl:when test="../following-sibling::w:p[1]/w:r/w:rPr/w:lang/@w:bidi">
										<xsl:sequence select="(../following-sibling::w:p[1]/w:r/w:rPr/w:lang/@w:bidi)[1]"/>
									</xsl:when>
								</xsl:choose>
							</xsl:when>
							<xsl:otherwise>
								<xsl:sequence select="$defaultLatin"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<!--Checking language for image group-->
			<xsl:when test="$CheckLang='imagegroup'">
				<xsl:variable name="count_lang" as="xs:integer" select="xs:integer(string-join(('0',../../w:r/w:pict/v:shape/v:textbox/w:txbxContent/w:p/w:r/w:rPr/w:lang/count(@*)),''))"/>
				<xsl:choose>
					<!--Checking for language type CS-->
					<xsl:when test="../../w:r/w:pict/v:shape/v:textbox/w:txbxContent/w:p/w:r/w:rPr/w:rFonts/@w:hint='cs'">
						<xsl:choose>
							<!--Checking for bidirectional language-->
							<xsl:when test="(../../w:r/w:pict/v:shape/v:textbox/w:txbxContent/w:p/w:r/w:rPr/w:lang/@w:bidi)">
								<!--Getting value from bidi attribute in lang tag-->
								<xsl:sequence select="(../../w:r/w:pict/v:shape/v:textbox/w:txbxContent/w:p/w:r/w:rPr/w:lang/@w:bidi)[1]"/>
							</xsl:when>
							<!--Assinging default bidirectional language-->
							<xsl:otherwise>
								<xsl:sequence select="$defaultComplex"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:when>
					<!--Checking for language type eastAsia-->
					<xsl:when test="../../w:r/w:pict/v:shape/v:textbox/w:txbxContent/w:p/w:r/w:rPr/w:rFonts/@w:hint='eastAsia'">
						<xsl:choose>
							<!--Getting value from eastasia attribute in lang tag-->
							<xsl:when test="../../w:r/w:pict/v:shape/v:textbox/w:txbxContent/w:p/w:r/w:rPr/w:lang/@w:eastAsia">
								<xsl:sequence select="(../../w:r/w:pict/v:shape/v:textbox/w:txbxContent/w:p/w:r/w:rPr/w:lang/@w:eastAsia)[1]"/>
							</xsl:when>
							<!--Assinging default eastAsia language-->
							<xsl:otherwise>
								<xsl:sequence select="$defaultEastAsia"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:when>
					<xsl:otherwise>
						<xsl:choose>
							<xsl:when test="$count_lang &gt; 1">
								<xsl:choose>
									<xsl:when test="../../w:r/w:pict/v:shape/v:textbox/w:txbxContent/w:p/w:r/w:rPr/w:lang/@w:val">
										<xsl:sequence select="(../../w:r/w:pict/v:shape/v:textbox/w:txbxContent/w:p/w:r/w:rPr/w:lang/@w:val)[1]"/>
									</xsl:when>
									<xsl:otherwise>
										<xsl:sequence select="$defaultLatin"/>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:when>
							<xsl:when test="$count_lang=1">
								<xsl:choose>
									<xsl:when test="../../w:r/w:pict/v:shape/v:textbox/w:txbxContent/w:p/w:r/w:rPr/w:lang/@w:val">
										<xsl:sequence select="(../../w:r/w:pict/v:shape/v:textbox/w:txbxContent/w:p/w:r/w:rPr/w:lang/@w:val)[1]"/>
									</xsl:when>
									<xsl:when test="../../w:r/w:pict/v:shape/v:textbox/w:txbxContent/w:p/w:r/w:rPr/w:lang/@w:eastAsia">
										<xsl:sequence select="(../../w:r/w:pict/v:shape/v:textbox/w:txbxContent/w:p/w:r/w:rPr/w:lang/@w:eastAsia)[1]"/>
									</xsl:when>
									<xsl:when test="../../w:r/w:pict/v:shape/v:textbox/w:txbxContent/w:p/w:r/w:rPr/w:lang/@w:bidi">
										<xsl:sequence select="(../../w:r/w:pict/v:shape/v:textbox/w:txbxContent/w:p/w:r/w:rPr/w:lang/@w:bidi)[1]"/>
									</xsl:when>
								</xsl:choose>
							</xsl:when>
							<xsl:otherwise>
								<xsl:sequence select="$defaultLatin"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<!--Checking language for table-->
			<xsl:when test="$CheckLang='Table'">
				<xsl:variable name="count_lang" as="xs:integer" select="xs:integer(string-join(('0',preceding-sibling::w:p[1]/w:r/w:rPr/w:lang/count(@*)),''))"/>
				<xsl:choose>
					<!--Checking for language type eastAsia-->
					<xsl:when test="preceding-sibling::w:p[1]/w:r/w:rPr/w:rFonts/@w:hint='eastAsia'">
						<xsl:choose>
							<!--Getting value from eastasia attribute in lang tag-->
							
							<xsl:when test="preceding-sibling::w:p[1]/w:r/w:rPr/w:lang/@w:eastAsia">
								<xsl:sequence select="(preceding-sibling::w:p[1]/w:r/w:rPr/w:lang/@w:eastAsia)[1]"/>
							</xsl:when>
							<!--Assinging default eastAsia language-->
							
							<xsl:otherwise>
								<xsl:sequence select="$defaultEastAsia"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:when>
					<!--Checking for language type CS-->
					<xsl:when test="preceding-sibling::w:p[1]/w:r/w:rPr/w:rFonts/@w:hint='cs'">
						<xsl:choose>
							<!--Checking for bidirectional language-->
							<xsl:when test="preceding-sibling::w:p[1]/w:r/w:rPr/w:lang/@w:bidi">
								<!--Getting value from bidi attribute in lang tag-->
								<xsl:sequence select="(preceding-sibling::w:p[1]/w:r/w:rPr/w:lang/@w:bidi)[1]"/>
							</xsl:when>
							<!--Assinging default bidirectional language-->
							<xsl:otherwise>
								<xsl:sequence select="$defaultComplex"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:when>
					<xsl:otherwise>
						<xsl:choose>
							<xsl:when test="$count_lang &gt; 1">
								<xsl:choose>
									<xsl:when test="preceding-sibling::w:p[1]/w:r/w:rPr/w:lang/@w:val">
										<xsl:sequence select="(preceding-sibling::w:p[1]/w:r/w:rPr/w:lang/@w:val)[1]"/>
									</xsl:when>
									<xsl:otherwise>
										<xsl:sequence select="$defaultLatin"/>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:when>
							<xsl:when test="$count_lang = 1">
								<xsl:choose>
									<xsl:when test="preceding-sibling::w:p[1]/w:r/w:rPr/w:lang/@w:val">
										<xsl:sequence select="(preceding-sibling::w:p[1]/w:r/w:rPr/w:lang/@w:val)[1]"/>
									</xsl:when>
									<xsl:when test="preceding-sibling::w:p[1]/w:r/w:rPr/w:lang/@w:eastAsia">
										<xsl:sequence select="(preceding-sibling::w:p[1]/w:r/w:rPr/w:lang/@w:eastAsia)[1]"/>
									</xsl:when>
									<xsl:when test="preceding-sibling::w:p[1]/w:r/w:rPr/w:lang/@w:bidi">
										<xsl:sequence select="(preceding-sibling::w:p[1]/w:r/w:rPr/w:lang/@w:bidi)[1]"/>
									</xsl:when>
								</xsl:choose>
							</xsl:when>
							<xsl:otherwise>
								<xsl:sequence select="$defaultLatin"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="TempCharacterStyle">
		<xsl:param name="characterStyle" as="xs:boolean"/>
		<xsl:choose>
			<xsl:when test="$characterStyle">
				<xsl:choose>
					<xsl:when test="../w:pPr/w:ind[@w:left] and ../w:pPr/w:ind[@w:right] and w:rPr/w:u and w:rPr/w:strike and w:rPr/w:caps and w:rPr/w:color and w:t">
						<xsl:variable name="val" as="xs:integer" select="../w:pPr/w:ind/@w:left"/>
						<xsl:variable name="val_left" as="xs:integer" select="xs:integer(round($val div 1440))"/>
						<xsl:variable name="valright" as="xs:integer" select="../w:pPr/w:ind/@w:right"/>
						<xsl:variable name="val_right" as="xs:integer" select="xs:integer(round($valright div 1440))"/>
						<xsl:variable name="val_color" as="xs:string" select="w:rPr/w:color/@w:val"/>
						<span class="{concat('text:Underline line-through;color:#',$val_color,';text-transform:uppercase',';text-indent:','right=',$val_right,'in',';left=',$val_left,'in')}">
							<xsl:value-of select="w:t"/>
						</span>
					</xsl:when>
					
					<xsl:when test="../w:pPr/w:ind[@w:left] and w:rPr/w:u and w:rPr/w:strike and w:rPr/w:caps and w:rPr/w:color and w:t">
						<xsl:variable name="val" as="xs:integer" select="../w:pPr/w:ind/@w:left"/>
						<xsl:variable name="val_left" as="xs:integer" select="xs:integer(round($val div 1440))"/>
						<xsl:variable name="val_color" as="xs:string" select="w:rPr/w:color/@w:val"/>
						<span class="{concat('text:Underline line-through;color:#',$val_color,';text-transform:uppercase',';text-indent:',$val_left,'in')}">
							<xsl:value-of select="w:t"/>
						</span>
					</xsl:when>
					
					<xsl:when test="../w:pPr/w:ind[@w:right] and w:rPr/w:u and w:rPr/w:strike and w:rPr/w:caps and w:rPr/w:color and w:t">
						<xsl:variable name="val" as="xs:integer" select="../w:pPr/w:ind/@w:right"/>
						<xsl:variable name="val_right" as="xs:integer" select="xs:integer(round($val div 1440))"/>
						<xsl:variable name="val_color" as="xs:string" select="w:rPr/w:color/@w:val"/>
						<span class="{concat('text:Underline line-through;color:#',$val_color,';text-transform:uppercase',';text-indent:',$val_right,'in')}">
							<xsl:value-of select="w:t"/>
						</span>
					</xsl:when>
					
					<xsl:when test="../w:pPr/w:jc and w:rPr/w:u and w:rPr/w:strike and w:rPr/w:caps and w:rPr/w:color and w:t">
						<xsl:variable name="val" as="xs:string" select="../w:pPr/w:jc/@w:val"/>
						<xsl:variable name="val_color" as="xs:string" select="w:rPr/w:color/@w:val"/>
						<span class="{concat('text:Underline line-through;color:#',$val_color,';text-transform:uppercase',';text-align:',$val)}">
							<xsl:value-of select="w:t"/>
						</span>
					</xsl:when>
					
					<xsl:when test="w:rPr/w:u and w:rPr/w:strike and w:rPr/w:caps and w:rPr/w:color and w:t">
						<xsl:variable name="val_color" as="xs:string" select="w:rPr/w:color/@w:val"/>
						<span class="{concat('text:Underline line-through;color:#',$val_color,';text-transform:uppercase')}">
							<xsl:value-of select="w:t"/>
						</span>
					</xsl:when>
					
					
					<xsl:when test="../w:pPr/w:ind[@w:left] and ../w:pPr/w:ind[@w:right] and w:rPr/w:u and w:rPr/w:strike and w:rPr/w:smallCaps and w:rPr/w:color and w:t">
						<xsl:variable name="val" as="xs:integer" select="../w:pPr/w:ind/@w:left"/>
						<xsl:variable name="val_left" as="xs:integer" select="xs:integer(round($val div 1440))"/>
						<xsl:variable name="valright" as="xs:integer" select="../w:pPr/w:ind/@w:right"/>
						<xsl:variable name="val_right" as="xs:integer" select="xs:integer(round($valright div 1440))"/>
						<xsl:variable name="val_color" as="xs:string" select="w:rPr/w:color/@w:val"/>
						<span class="{concat('text:Underline line-through;color:#',$val_color,';font-variant:small-caps',';text-indent:','right=',$val_right,'in',';left=',$val_left,'in')}">
							<xsl:value-of select="w:t"/>
						</span>
					</xsl:when>
					
					<xsl:when test="../w:pPr/w:ind[@w:left] and w:rPr/w:u and w:rPr/w:strike and w:rPr/w:smallCaps and w:rPr/w:color and w:t">
						<xsl:variable name="val" as="xs:integer" select="../w:pPr/w:ind/@w:left"/>
						<xsl:variable name="val_left" as="xs:integer" select="xs:integer(round($val div 1440))"/>
						<xsl:variable name="val_color" as="xs:string" select="w:rPr/w:color/@w:val"/>
						<span class="{concat('text:Underline line-through;color:#',$val_color,';font-variant:small-caps',';text-indent:',$val_left,'in')}">
							<xsl:value-of select="w:t"/>
						</span>
					</xsl:when>
					
					<xsl:when test="../w:pPr/w:ind[@w:right] and w:rPr/w:u and w:rPr/w:strike and w:rPr/w:smallCaps and w:rPr/w:color and w:t">
						<xsl:variable name="val" as="xs:integer" select="../w:pPr/w:ind/@w:right"/>
						<xsl:variable name="val_right" as="xs:integer" select="xs:integer(round($val div 1440))"/>
						<xsl:variable name="val_color" as="xs:string" select="w:rPr/w:color/@w:val"/>
						<span class="{concat('text:Underline line-through;color:#',$val_color,';font-variant:small-caps',';text-indent:',$val_right,'in')}">
							<xsl:value-of select="w:t"/>
						</span>
					</xsl:when>
					
					<xsl:when test="../w:pPr/w:jc and w:rPr/w:u and w:rPr/w:strike and w:rPr/w:smallCaps and w:rPr/w:color and w:t">
						<xsl:variable name="val" as="xs:string" select="../w:pPr/w:jc/@w:val"/>
						<xsl:variable name="val_color" as="xs:string" select="w:rPr/w:color/@w:val"/>
						<span class="{concat('text:Underline line-through;color:#',$val_color,';font-variant:small-caps',';text-align:',$val)}">
							<xsl:value-of select="w:t"/>
						</span>
					</xsl:when>
					
					<xsl:when test="w:rPr/w:u and w:rPr/w:strike and w:rPr/w:smallCaps and w:rPr/w:color and w:t">
						<xsl:variable name="val_color" as="xs:string" select="w:rPr/w:color/@w:val"/>
						<span class="{concat('text:Underline line-through;color:#',$val_color,';font-variant:small-caps')}">
							<xsl:value-of select="w:t"/>
						</span>
					</xsl:when>
					
					<xsl:when test="w:rPr/w:u and w:t">
						<span class="text-decoration: underline">
							<xsl:value-of disable-output-escaping="yes" select="w:t"/>
						</span>
					</xsl:when>
					<xsl:when test="w:rPr/w:strike and w:t">
						<span class="text-decoration:line-through">
							<xsl:value-of disable-output-escaping="yes" select="concat(' ',w:t)"/>
						</span>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="w:t"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="w:t"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="OpenNode">
		<xsl:param name="qname" as="xs:string"/>
		<xsl:param name="attributes" as="xs:string?" select="''"/>
		<xsl:variable name="isOkToOpen" select="d:PushStructuralNode($myObj, $qname)" />
		<xsl:if test="$isOkToOpen">
			<xsl:value-of disable-output-escaping="yes" select="concat('&lt;', $qname)" />
			<xsl:if test="not($attributes = '')">
				<xsl:value-of disable-output-escaping="yes" select="concat(' ', $attributes)" />
			</xsl:if>
			<xsl:value-of disable-output-escaping="yes" select="'&gt;'" />
		</xsl:if>
	</xsl:template>

	<!-- If the node is found in the stack, it will close all nodes up to the specified one and then close it -->
	<xsl:template name="CloseNode">
		<xsl:param name="qname" as="xs:string"/>
		<xsl:variable name="toClose" select="d:PopStructuralNode($myObj, $qname)" />
		<xsl:if test="not($toClose = '')">
			<xsl:value-of disable-output-escaping="yes" select="concat('&lt;/', $toClose, '&gt;')" />
			<xsl:call-template name="CloseNode">
				<xsl:with-param name="qname" select="$qname"/>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>

	<!-- New templates for specific elements -->

	<!-- Parsing bookmark start -->
	<xsl:template name="ParseBookmarkStart">
		<xsl:call-template name="CloseAllStyleTag" />
		<xsl:variable name="aquote">"</xsl:variable>
		<!--Checking whether BookMarkStart is related to Abbreviations or not -->
		<xsl:if test="substring(@w:name,1,13)='Abbreviations'">
			<xsl:variable name="full" as="xs:string" select="d:FullAbbr($myObj,@w:name,$version)"/>
			<xsl:choose>
				<!--checking whether an Abbreviation is having Full Form or not-->
				<xsl:when test="not($full='')">
					<xsl:call-template name="CloseNode">
						<xsl:with-param name="qname" select="'abbr'"/>
					</xsl:call-template>
					<xsl:call-template name="OpenNode">
						<xsl:with-param name="qname" select="'abbr'"/>
						<xsl:with-param name="attributes" select="concat('title=',$aquote,$full,$aquote)"/>
					</xsl:call-template>
					<!-- <xsl:value-of disable-output-escaping="yes" select="concat('&lt;','abbr ','title=',$aquote,$full,$aquote,'&gt;')"/> -->
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="CloseNode">
						<xsl:with-param name="qname" select="'abbr'"/>
					</xsl:call-template>
					<xsl:call-template name="OpenNode">
						<xsl:with-param name="qname" select="'abbr'"/>
						<xsl:with-param name="attributes" select="''"/>
					</xsl:call-template>
					<!-- <xsl:value-of disable-output-escaping="yes" select="concat('&lt;','abbr','&gt;')"/> -->
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
		<!--Checking whether BookMarkStart is related to Acronyms or not -->
		<xsl:if test="substring(@w:name,1,11)='AcronymsYes'">
			<xsl:variable name="full" as="xs:string" select="d:FullAcr($myObj,@w:name,$version)"/>
			<xsl:choose>
				<!--checking whether an Acronym is having Full Form or not-->
				<xsl:when test="not($full='')">
					<xsl:call-template name="CloseNode">
						<xsl:with-param name="qname" select="'acronym'"/>
					</xsl:call-template>
					<xsl:call-template name="OpenNode">
						<xsl:with-param name="qname" select="'acronym'"/>
						<xsl:with-param name="attributes" select="concat('pronounce=',$aquote,'yes',$aquote,' title=',$aquote,$full,$aquote)"/>
					</xsl:call-template>
					<!-- <xsl:value-of disable-output-escaping="yes" select="concat('&lt;','acronym ','pronounce=',$aquote,'yes',$aquote,' title=',$aquote,$full,$aquote,'&gt;')"/> -->
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="CloseNode">
						<xsl:with-param name="qname" select="'acronym'"/>
					</xsl:call-template>
					<xsl:call-template name="OpenNode">
						<xsl:with-param name="qname" select="'acronym'"/>
						<xsl:with-param name="attributes" select="concat('pronounce=',$aquote,'yes',$aquote)"/>
					</xsl:call-template>
					<!-- <xsl:value-of disable-output-escaping="yes" select="concat('&lt;','acronym ','pronounce=',$aquote,'yes',$aquote,'&gt;')"/> -->
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
		<!--Checking whether BookMarkStart is related to Acronymss or not -->
		<xsl:if test="substring(@w:name,1,10)='AcronymsNo'">
			<xsl:call-template name="CloseAllStyleTag" />
			<xsl:variable name="full" as="xs:string" select="d:FullAcr($myObj,@w:name,$version)"/>
			<!--checking whether an Acronym is having Full Form or not-->
			<xsl:choose>
				<xsl:when test="not($full='')">
					<xsl:call-template name="CloseNode">
						<xsl:with-param name="qname" select="'acronym'"/>
					</xsl:call-template>
					<xsl:call-template name="OpenNode">
						<xsl:with-param name="qname" select="'acronym'"/>
						<xsl:with-param name="attributes" select="concat('pronounce=',$aquote,'no',$aquote,' title=',$aquote,$full,$aquote)"/>
					</xsl:call-template>
					<!-- <xsl:value-of disable-output-escaping="yes" select="concat('&lt;','acronym ','pronounce=',$aquote,'no',$aquote,' title=',$aquote,$full,$aquote,'&gt;')"/> -->
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="CloseNode">
						<xsl:with-param name="qname" select="'acronym'"/>
					</xsl:call-template>
					<xsl:call-template name="OpenNode">
						<xsl:with-param name="qname" select="'acronym'"/>
						<xsl:with-param name="attributes" select="concat('pronounce=',$aquote,'no',$aquote)"/>
					</xsl:call-template>
					<!-- <xsl:value-of disable-output-escaping="yes" select="concat('&lt;','acronym ','pronounce=',$aquote,'no',$aquote,'&gt;')"/> -->
				</xsl:otherwise>
			</xsl:choose>
			
		</xsl:if>
		<!--Checking for hyperlink-->
		<xsl:if test="d:GetHyperlinkName($myObj,@w:name)=1 and not(substring(@w:name,1,13)='Abbreviations') and not(substring(@w:name,1,11)='AcronymsYes') and not(substring(@w:name,1,10)='AcronymsNo')">
			<xsl:choose>
				<!--If hyperlink is not in Table of content-->
				<xsl:when test="not(contains(@w:name,'_Toc'))">
					<xsl:sequence select="d:sink(d:TestRun($myObj))"/> <!-- empty -->
					<xsl:variable name="initialize" as="xs:integer" select="d:SetHyperLinkFlag($myObj)"/>
					<xsl:call-template name="CloseAllStyleTag" />
					<a title="{@w:name}" id="_{@w:id}" />
					<xsl:sequence select="d:sink(d:StroreId($myObj,'_{@w:id}'))"/> <!-- empty -->
				</xsl:when>
			</xsl:choose>
		</xsl:if>
	</xsl:template>

	<xsl:variable name="BookmarksCache">
		<bookmarks>
			<xsl:for-each select="$documentXml//w:bookmarkStart">
				<bookmark id="{@w:id}" name="{@w:name}"/>
			</xsl:for-each>
			<xsl:for-each select="$footnotesXml//w:bookmarkStart">
				<bookmark id="{@w:id}" name="{@w:name}"/>
			</xsl:for-each>
			<xsl:for-each select="$endnotesXml//w:bookmarkStart">
				<bookmark id="{@w:id}" name="{@w:name}"/>
			</xsl:for-each>
		</bookmarks>
	</xsl:variable>

	<xsl:template name="ParseBookmarkEnd">
		<xsl:call-template name="CloseAllStyleTag" />
		<xsl:variable name="seperate" as="xs:string">
			<xsl:variable name="id" as="xs:string" select ="@w:id"/>
			<xsl:variable name="bookmarkName" as="xs:string">
				<xsl:choose>
					<xsl:when test="$BookmarksCache//*:bookmark[@*:id=$id]">
						<xsl:sequence select="$BookmarksCache//*:bookmark[@*:id=$id]/@*:name"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:sequence select="''"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:variable>
			<xsl:choose>
				<xsl:when test="starts-with($bookmarkName,'Abbreviations')">
					<xsl:sequence select="'AbbrTrue'"/>
				</xsl:when>
				<xsl:when test="starts-with($bookmarkName,'Acronyms')">
					<xsl:sequence select="'AcrTrue'"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:sequence select="'false'"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<!--Checking whether BookMarkEnd is related to Abbreviations or not -->
		<xsl:if test="$seperate='AbbrTrue'">
			<!--checking    condition to close abbr Tag -->
			<xsl:call-template name="CloseNode">
				<xsl:with-param name="qname" select="'abbr'"/>
			</xsl:call-template>
		</xsl:if>
		<!--Checking whether BookMarkEnd is related to Acronyms or not -->
		<xsl:if test="$seperate='AcrTrue'">
			<!--checking    condition to close acronym Tag -->
			<xsl:call-template name="CloseNode">
				<xsl:with-param name="qname" select="'acronym'"/>
			</xsl:call-template>
		</xsl:if>
		<!--Closing hyperlink if not heading-->
		<xsl:if test="not(d:GetBookmark($myObj)&gt;0)">
			<xsl:if test="d:CheckId($myObj,@w:id)=1">
				<xsl:sequence select="d:sink(d:SetTestRun($myObj))"/> <!-- empty -->
				<!-- <xsl:if test="not(../w:pPr/w:pStyle[substring(@w:val,1,7)='Heading'])">
					<xsl:value-of disable-output-escaping="yes" select="'&lt;/a&gt;'"/>
				</xsl:if> -->
				<xsl:if test="../w:pPr/w:pStyle[substring(@w:val,1,7)='Heading']">
					<xsl:sequence select="d:sink(d:SetHyperLink($myObj))"/> <!-- empty -->
				</xsl:if>
			</xsl:if>
		</xsl:if>
		<xsl:if test="d:GetBookmark($myObj)&gt;0">
			<xsl:sequence select="d:sink(d:SetTestRun($myObj))"/> <!-- empty -->
		</xsl:if>

	</xsl:template>
	
</xsl:stylesheet>
