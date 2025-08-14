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
				xmlns:m="http://schemas.openxmlformats.org/officeDocument/2006/math"
				xmlns:dcmitype="http://purl.org/dc/dcmitype/"
				xmlns:o="urn:schemas-microsoft-com:office:office"
				xmlns:d="org.daisy.pipeline.word_to_dtbook.impl.DaisyClass"
				xmlns="http://www.daisy.org/z3986/2005/dtbook/"
				exclude-result-prefixes="w pic wp dcterms xsi cp dc a r v dcmitype d xsl m o xs">
	<!--Parameter citation-->
	<xsl:param name="Cite_style" as="xs:string" select="d:Citation($myObj)"/>

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


	<!-- Template for content of front|body|rearmatter -->
	<xsl:template name="Matter">
		<!--Parameter trackchanges-->
		<xsl:param name="acceptRevisions" as="xs:boolean"/>
		<!--Parameter version of Office-->
		<xsl:param name="version" as="xs:string"/>
		<!--Parameter custom page number-->
		<xsl:param name="pagination" as="xs:string"/>
		<xsl:param name="masterSub" as="xs:boolean"/>
		<xsl:param name="sOperators" as="xs:string"/>
		<xsl:param name="sMinuses" as="xs:string"/>
		<xsl:param name="sNumbers" as="xs:string"/>
		<xsl:param name="sZeros" as="xs:string"/>
		<xsl:param name="imgOption" as="xs:string"/>
		<xsl:param name="dpi" as="xs:float"/>
		<xsl:param name="charStyles" as="xs:boolean"/>
		<xsl:param name="matterType" as="xs:string"/> <!-- Frontmatter|Bodymatter|Readmatter -->

		<!--Calling d:ExternalImage() to check external images-->
		<xsl:variable name="external" as="xs:string" select="d:ExternalImage($myObj)" />
		<!--Fedility loss External images-->
		<xsl:if test="$external='translation.oox2Daisy.ExternalImage'">
			<xsl:message terminate="no">translation.oox2Daisy.ExternalImage</xsl:message>
		</xsl:if>

		<!-- Searching for "heading 1" styleId in the document (usually the bodymatter title)-->
		<xsl:variable name="heading1StyleId" as="xs:string*">
			<xsl:for-each select="$stylesXml//w:styles/w:style/w:name[@w:val='heading 1']">
				<xsl:sequence select="../@w:styleId"/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="heading1StyleId" as="xs:string?" select="string-join($heading1StyleId,'')[not(.='')]"/>
		<!--Looping through each hyperlink-->
		<xsl:for-each select="$documentXml//w:document/w:body/w:p/w:hyperlink">
			<!--Calling d:AddHyperlink() for storing Anchor in Hyperlink-->
			<xsl:sequence select="d:sink(d:AddHyperlink($myObj,string(@w:anchor)))"/> <!-- empty -->
		</xsl:for-each>
		<xsl:variable name="ElementCountToConvert" select="count($documentXml//w:body/*)" />

		<!--If first paragraph is not Heading-->
		<xsl:if test="not($documentXml//w:document/w:body/w:p[1]/w:pPr/w:pStyle[substring(@w:val,1,7)='Heading']) or $matterType='Rearmatter'">
			<!--Calling Template to add level1-->
			<xsl:call-template name="AddLevel">
				<xsl:with-param name="levelValue" select="1"/>
				<xsl:with-param name="check" select="false()"/>
				<xsl:with-param name="verhead" select="$version"/>
				<xsl:with-param name="pagination" select="$pagination"/>
				<xsl:with-param name="sOperators" select="$sOperators"/>
				<xsl:with-param name="sMinuses" select="$sMinuses"/>
				<xsl:with-param name="sNumbers" select="$sNumbers"/>
				<xsl:with-param name="sZeros" select="$sZeros"/>
				<xsl:with-param name="mastersubhead" select="$masterSub"/>
				<xsl:with-param name="headingFormatAndTextAndID" select="'0'"/>
				<xsl:with-param name="lvlcharStyle" select="$charStyles"/>
			</xsl:call-template>
		</xsl:if>

		<xsl:sequence select="d:ResetCurrentMatterType($myObj)"/> <!-- empty -->
		<!--Traversing through each node of the document-->
		<xsl:for-each select="$documentXml//w:body/node()">
			<xsl:if test="not($masterSub) and self::w:p">
				<!-- First check if the paragraph has style with a "(Front|Body|Rear)matter" prefix,
									and set parsing context accordingly (stored in the java side) -->
				<xsl:choose>
					<xsl:when test="(
							count(w:pPr/w:pStyle[substring(@w:val,1,11)='Frontmatter'])=1
							or count(w:r/w:rPr/w:rStyle[substring(@w:val,1,11)='Frontmatter'])=1
						)">
						<xsl:sequence select="d:sink(d:SetCurrentMatterType($myObj, 'Frontmatter'))"/>
					</xsl:when>
					<xsl:when test="(
							count(w:pPr/w:pStyle[substring(@w:val,1,10)='Bodymatter'])=1
							or count(w:r/w:rPr/w:rStyle[substring(@w:val,1,10)='Bodymatter'])=1
						)">
						<xsl:sequence select="d:sink(d:SetCurrentMatterType($myObj, 'Bodymatter'))"/>
					</xsl:when>
					<xsl:when test="(
							count(w:pPr/w:pStyle[substring(@w:val,1,10)='Rearmatter'])=1
							or count(w:r/w:rPr/w:rStyle[substring(@w:val,1,10)='Rearmatter'])=1
						)">
						<xsl:sequence select="d:sink(d:SetCurrentMatterType($myObj, 'Rearmatter'))"/>
					</xsl:when>
				</xsl:choose>
			</xsl:if>
			<!-- If the node parsing context match the wanted matter context (i.e. node context is Bodymatter and requested matter type is Bodymatter ) -->
			<xsl:if test="d:GetCurrentMatterType($myObj)=$matterType">
				<!-- <xsl:message terminate="no">progress:Converting element <xsl:value-of select="name()"/> - <xsl:value-of select="position()"/> / <xsl:value-of select="$ElementCountToConvert"/></xsl:message> -->
				<xsl:choose>
					<!--Checking for Paragraph element-->
					<xsl:when test="(
							self::w:p
							and not(
								*/w:pStyle[
									substring(@w:val, 1, 11)='Frontmatter'
									or substring(@w:val, 1, 10)='Bodymatter'
									or substring(@w:val, 1, 10)='Rearmatter'
								]
							)
						)">
						<!-- check if current level is 0 in the stack 
											and if the paragraph is of not of heading type, make a level 1
						-->
						<xsl:variable name="paragraphStyleId" select="w:pPr/w:pStyle/@w:val"/>
						<xsl:variable name="paragraphStyleOutline" select="(
								$stylesXml/w:styles/w:style[@w:styleId=$paragraphStyleId]/w:pPr/w:outlineLvl/@w:val
							)"/>
						<xsl:variable name="currentLevel" select="d:PeekLevel($myObj)" />
						<xsl:if test="$currentLevel=0 and $paragraphStyleOutline=''">
							<xsl:call-template name="AddLevel">
								<xsl:with-param name="levelValue" select="1"/>
								<xsl:with-param name="check" select="false()"/>
								<xsl:with-param name="verhead" select="$version"/>
								<xsl:with-param name="pagination" select="$pagination"/>
								<xsl:with-param name="sOperators" select="$sOperators"/>
								<xsl:with-param name="sMinuses" select="$sMinuses"/>
								<xsl:with-param name="sNumbers" select="$sNumbers"/>
								<xsl:with-param name="sZeros" select="$sZeros"/>
								<xsl:with-param name="mastersubhead" select="$masterSub"/>
								<xsl:with-param name="headingFormatAndTextAndID" select="'0'"/>
								<xsl:with-param name="lvlcharStyle" select="$charStyles"/>
							</xsl:call-template>
						</xsl:if>
						<xsl:sequence select="d:sink(d:CheckCoverPage($myObj))"/> <!-- empty -->
						<xsl:call-template name="StyleContainer">
							<xsl:with-param name="acceptRevisions" select="$acceptRevisions"/>
							<xsl:with-param name="version" select="$version"/>
							<xsl:with-param name="pagination" select="$pagination"/>
							<xsl:with-param name="styleHeading" select="$heading1StyleId"/>
							<xsl:with-param name="mastersubstyle" select="$masterSub"/>
							<xsl:with-param name="imgOptionStyle" select="$imgOption"/>
							<xsl:with-param name="dpiStyle" select="$dpi"/>
							<xsl:with-param name="characterStyle" select="$charStyles"/>
						</xsl:call-template>
					</xsl:when>
					<!--Checking for Table element-->
					<xsl:when test="self::w:tbl">
						<!--If exists then calling TableHandler-->
						<xsl:sequence select="d:sink(d:CheckCoverPage($myObj))"/> <!-- empty -->
						<xsl:call-template name="TableHandler">
							<xsl:with-param name="parmVerTable" select="$version"/>
							<xsl:with-param name="pagination" select="$pagination"/>
							<xsl:with-param name="mastersubtbl" select="$masterSub"/>
							<xsl:with-param name="characterStyle" select="$charStyles"/>
						</xsl:call-template>
					</xsl:when>
					<!--Checking for section element-->
					<xsl:when test="self::w:sectPr">
						<xsl:if test="$FootnotesPosition='page'">
							<!--calling for foonote template and displaying footnote text at the end of the page-->
							<xsl:call-template name="InsertFootnotes">
								<xsl:with-param name="level" select="0"/>
								<xsl:with-param name="verfoot" select="$version"/>
								<xsl:with-param name="sOperators" select="$sOperators"/>
								<xsl:with-param name="sMinuses" select="$sMinuses"/>
								<xsl:with-param name="sNumbers" select="$sNumbers"/>
								<xsl:with-param name="sZeros" select="$sZeros"/>
							</xsl:call-template>
						</xsl:if>
					</xsl:when>
					<!--Checking for Structured document element-->
					<xsl:when test="self::w:sdt">
						<xsl:choose>
							<xsl:when test="w:sdtPr/w:docPartObj/w:docPartGallery/@w:val='Table of Contents'">
								<!--Save Level before closing all levels-->
								<xsl:variable name="PeekLevel" as="xs:integer" select="d:PeekLevel($myObj)"/>
								<!--Close all levels before Table Of Contents-->
								<xsl:call-template name="CloseLevel">
									<xsl:with-param name="CurrentLevel" select="1"/>
									<xsl:with-param name="verfoot" select="$version"/>
									<xsl:with-param name="characterStyle" select="$charStyles"/>
									<xsl:with-param name="sOperators" select="$sOperators"/>
									<xsl:with-param name="sMinuses" select="$sMinuses"/>
									<xsl:with-param name="sNumbers" select="$sNumbers"/>
									<xsl:with-param name="sZeros" select="$sZeros"/>
								</xsl:call-template>
								<!--Calling Template to add Table Of Contents-->
								<xsl:call-template name="TableOfContents">
									<xsl:with-param name="pagination" select="$pagination"/>
								</xsl:call-template>
								<!--Open $PeekLevel levels after Table Of Contents-->
								<xsl:call-template name="AddLevel">
									<xsl:with-param name="levelValue" select="$PeekLevel"/>
									<xsl:with-param name="check" select="true()"/>
									<xsl:with-param name="verhead" select="$version"/>
									<xsl:with-param name="pagination" select="$pagination"/>
									<xsl:with-param name="sOperators" select="$sOperators"/>
									<xsl:with-param name="sMinuses" select="$sMinuses"/>
									<xsl:with-param name="sNumbers" select="$sNumbers"/>
									<xsl:with-param name="sZeros" select="$sZeros"/>
									<xsl:with-param name="mastersubhead" select="$masterSub"/>
									<xsl:with-param name="headingFormatAndTextAndID" select="'0'"/>
									<xsl:with-param name="lvlcharStyle" select="$charStyles"/>
								</xsl:call-template>
							</xsl:when>
							<xsl:otherwise>
								<xsl:if test="w:sdtPr/w:docPartObj/w:docPartGallery">
									<!--Displaying fidelity loss message-->
									<xsl:message terminate="no">
										<xsl:value-of select="concat('translation.oox2Daisy.sdtElement|',w:sdtPr/w:docPartObj/w:docPartGallery/@w:val)"/>
									</xsl:message>
								</xsl:if>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:when>
					<!--Checking for BookmarkStart element-->
					<xsl:when test="self::w:bookmarkStart">
						<!--Checking Whether BookMarkStart is related to Abbreviations or not -->
						<xsl:if test="substring(@w:name,1,13)='Abbreviations'">
							<!--Storing the full form of Abbreviation in a variable-->
							<xsl:variable name="full" as="xs:string" select="d:FullAbbr($myObj,@w:name,$version)"/>
							<xsl:choose>
								<!--Checking whether all previous Abbreviations tags are closed or not before opening an new Abbreviation tag-->
								<xsl:when test ="not(d:AbbrAcrFlag($myObj)=1)">
									<xsl:choose>
										<!--checking whether an Abbreviation is having Full Form or not-->
										<xsl:when test="not($full='')">
											<xsl:value-of disable-output-escaping="yes" select="concat('&lt;abbr title=&quot;',$full,'&quot;&gt;')"/>
											<xsl:sequence select="d:sink(d:SetAbbrAcrFlag($myObj))"/> <!-- empty -->
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of disable-output-escaping="yes" select="'&lt;abbr&gt;'"/>
											<xsl:sequence select="d:sink(d:SetAbbrAcrFlag($myObj))"/> <!-- empty -->
										</xsl:otherwise>
									</xsl:choose>
								</xsl:when>
								<xsl:otherwise>
									<xsl:choose>
										<!--checking whether an Abbreviation is having Full Form or not-->
										<xsl:when test="not($full='')">
											<xsl:variable name="temp" as="xs:string" select="concat('&lt;abbr title=&quot;',$full,'&quot;&gt;')"/>
											<xsl:sequence select="d:sink(d:PushAbrAcr($myObj,$temp))"/> <!-- empty -->
										</xsl:when>
										<xsl:otherwise>
											<xsl:variable name="temp" as="xs:string" select="'&lt;abbr&gt;'"/>
											<xsl:sequence select="d:sink(d:PushAbrAcr($myObj,$temp))"/> <!-- empty -->
										</xsl:otherwise>
									</xsl:choose>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:if>
						<!--Checking Whether BookMarkStart is related to Acronyms or not -->
						<xsl:if test="substring(@w:name,1,11)='AcronymsYes'">
							<!--Storing the full form of Abbreviation in a variable-->
							<xsl:variable name="full" as="xs:string" select="d:FullAcr($myObj,@w:name,$version)"/>
							<xsl:choose>
								<!--Checking whether all previous Acronyms tags are closed or not before opening an new Acronyms tag-->
								<xsl:when test ="not(d:AbbrAcrFlag($myObj)=1)">
									<xsl:choose>
										<!--checking whether an Abbreviation is having Full Form or not-->
										<xsl:when test="not($full='')">
											<xsl:value-of disable-output-escaping="yes" select="concat('&lt;acronym pronounce=&quot;yes&quot; title=&quot;',$full,'&quot;&gt;')"/>
											<xsl:sequence select="d:sink(d:SetAbbrAcrFlag($myObj))"/> <!-- empty -->
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of disable-output-escaping="yes" select="'&lt;acronym pronounce=&quot;yes&quot;&gt;'"/>
											<xsl:sequence select="d:sink(d:SetAbbrAcrFlag($myObj))"/> <!-- empty -->
										</xsl:otherwise>
									</xsl:choose>
								</xsl:when>
								<xsl:otherwise>
									<xsl:choose>
										<!--checking whether an Abbreviation is having Full Form or not-->
										<xsl:when test="not($full='')">
											<xsl:variable name="temp" as="xs:string" select="concat('&lt;acronym pronounce=&quot;yes&quot; title=&quot;',$full,'&quot;&gt;')"/>
											<xsl:sequence select="d:sink(d:PushAbrAcr($myObj,$temp))"/> <!-- empty -->
										</xsl:when>
										<xsl:otherwise>
											<xsl:variable name="temp" as="xs:string" select="'&lt;acronym pronounce=&quot;yes&quot;&gt;'"/>
											<xsl:sequence select="d:sink(d:PushAbrAcr($myObj,$temp))"/> <!-- empty -->
										</xsl:otherwise>
									</xsl:choose>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:if>
						<!--Checking Whether BookMarkStart is related to Acronyms or not -->
						<xsl:if test="substring(@w:name,1,10)='AcronymsNo'">
							<!--Storing the full form of Abbreviation in a variable-->
							<xsl:variable name="full" as="xs:string" select="d:FullAcr($myObj,@w:name,$version)"/>
							<xsl:choose>
								<!--Checking whether all previous Acronyms tags are closed or not before opening an new Acronyms tag-->
								<xsl:when test ="not(d:AbbrAcrFlag($myObj)=1)">
									<xsl:choose>
										<!--checking whether an Abbreviation is having Full Form or not-->
										<xsl:when test="not($full='')">
											<xsl:value-of disable-output-escaping="yes" select="concat('&lt;acronym pronounce=&quot;no&quot; title=&quot;',$full,'&quot;&gt;')"/>
											<xsl:sequence select="d:sink(d:SetAbbrAcrFlag($myObj))"/> <!-- empty -->
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of disable-output-escaping="yes" select="'&lt;acronym pronounce=&quot;no&quot;&gt;'"/>
											<xsl:sequence select="d:sink(d:SetAbbrAcrFlag($myObj))"/> <!-- empty -->
										</xsl:otherwise>
									</xsl:choose>
								</xsl:when>
								<xsl:otherwise>
									<xsl:choose>
										<!--checking whether an Abbreviation is having Full Form or not-->
										<xsl:when test="not($full='')">
											<xsl:variable name="temp" as="xs:string" select="concat('&lt;acronym pronounce=&quot;no&quot; title=&quot;',$full,'&quot;&gt;')"/>
											<xsl:sequence select="d:sink(d:PushAbrAcr($myObj,$temp))"/> <!-- empty -->
										</xsl:when>
										<xsl:otherwise>
											<xsl:variable name="temp" as="xs:string" select="'&lt;acronym pronounce=&quot;no&quot;&gt;'"/>
											<xsl:sequence select="d:sink(d:PushAbrAcr($myObj,$temp))"/> <!-- empty -->
										</xsl:otherwise>
									</xsl:choose>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:if>
					</xsl:when>
					<!--Checking for BookMarkEnd -->
					<xsl:when test="self::w:bookmarkEnd">
						<xsl:variable name="seperate" as="xs:string">
							<xsl:variable name="id" as="xs:string" select="@w:id"/>
							<xsl:variable name="tempAbbr" as="xs:string" select="$documentXml//w:bookmarkStart[@w:id=$id]/@w:name"/>
							<xsl:sequence select="d:Book($myObj,$tempAbbr)"/>
						</xsl:variable>
						<!--Checking whether BookMarkEnd is related to Abbreviations or not -->
						<xsl:if test="$seperate='AbbrTrue'">
							<xsl:value-of disable-output-escaping="yes" select="'&lt;/abbr&gt;'"/>
							<xsl:sequence select="d:sink(d:ReSetAbbrAcrFlag($myObj))"/> <!-- empty -->
							<xsl:if test="d:CountAbrAcrpara($myObj) &gt; 0">
								<xsl:value-of disable-output-escaping="yes" select="d:PeekAbrAcrpara($myObj)"/>
							</xsl:if>
							<!-- FIXME NP : This might lead to an error as the heading is already closed-->
							<xsl:if test="d:CountAbrAcrhead($myObj) &gt; 0">
								<xsl:value-of disable-output-escaping="yes" select="d:PeekAbrAcrhead($myObj)"/>
							</xsl:if>
						</xsl:if>
						<!--Checking whether BookMarkEnd is related to Acronyms or not -->
						<xsl:if test="$seperate='AcrTrue'">
							<xsl:value-of disable-output-escaping="yes" select="'&lt;/acronym&gt;'"/>
							<xsl:sequence select="d:sink(d:ReSetAbbrAcrFlag($myObj))"/> <!-- empty -->
							<xsl:if test="d:CountAbrAcrpara($myObj) &gt; 0">
								<xsl:value-of disable-output-escaping="yes" select="d:PeekAbrAcrpara($myObj)"/>
							</xsl:if>
							<xsl:if test="d:CountAbrAcrhead($myObj) &gt; 0">
								<xsl:value-of disable-output-escaping="yes" select="d:PeekAbrAcrhead($myObj)"/>
							</xsl:if>
						</xsl:if>
					</xsl:when>
					<!--Checking for Pagebreaks and calling footnote template for displaying footnote text at the end of the page-->
					<xsl:otherwise>
						<!--Implementing fidelity loss for unhandled elements-->
						<xsl:message terminate="no">
							<xsl:value-of select="concat('translation.oox2Daisy.UncoveredElement|',name())"/>
						</xsl:message>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:if>
		</xsl:for-each>
		<!--Call a template to Close all the levels -->
		<xsl:call-template name="CloseLevel">
			<xsl:with-param name="CurrentLevel" select="1"/>
			<xsl:with-param name="verfoot" select="$version"/>
			<xsl:with-param name="characterStyle" select="$charStyles"/>
			<xsl:with-param name="sOperators" select="$sOperators"/>
			<xsl:with-param name="sMinuses" select="$sMinuses"/>
			<xsl:with-param name="sNumbers" select="$sNumbers"/>
			<xsl:with-param name="sZeros" select="$sZeros"/>
		</xsl:call-template>
	</xsl:template>


	<!--Template to implement different paragraph styles-->
	<!--Implementing all the paragraph styles and all other feature that appears inside the paragraph-->
	<xsl:template name="ParaHandler">
		<xsl:param name="flag" as="xs:string"/>
		<xsl:param name="acceptRevisions" as="xs:boolean" select="true()"/>
		<xsl:param name="version" as="xs:string" select="''"/>
		<xsl:param name="flagNote" as="xs:string" select="''"/>
		<xsl:param name="checkid" as="xs:integer?"/>
		<xsl:param name="sOperators" as="xs:string" select="''"/>
		<xsl:param name="sMinuses" as="xs:string" select="''"/>
		<xsl:param name="sNumbers" as="xs:string" select="''"/>
		<xsl:param name="sZeros" as="xs:string" select="''"/>
		<xsl:param name="pagination" as="xs:string"/>
		<xsl:param name="txt" as="xs:string" select="''"/>
		<xsl:param name="level" as="xs:integer?"/>
		<xsl:param name="mastersubpara" as="xs:boolean" select="false()"/>
		<xsl:param name="imgOptionPara" as="xs:string" select="''"/>
		<xsl:param name="dpiPara" as="xs:float?"/>
		<xsl:param name="charparahandlerStyle" as="xs:boolean"/>
		<!--Calling Footnote template when the page break is encountered.-->
		<xsl:if test="(
				( 
					w:r/w:lastRenderedPageBreak
					or (w:r/w:br/@w:type='page')
				) and not($flag='0')
			)">
			<!--If parent element is not table cell-->
			<xsl:if test="not(parent::w:tc)">

				<xsl:if test="$FootnotesPosition='page'">
					<xsl:call-template name="InsertFootnotes">
						<xsl:with-param name="level" select="0"/>
						<xsl:with-param name="verfoot" select="$version"/>
						<xsl:with-param name="sOperators" select="$sOperators"/>
						<xsl:with-param name="sMinuses" select="$sMinuses"/>
						<xsl:with-param name="sNumbers" select="$sNumbers"/>
						<xsl:with-param name="sZeros" select="$sZeros"/>
					</xsl:call-template>
				</xsl:if>

				<xsl:choose>
					<!--Checking for page break-->
					<xsl:when test="(
							(w:r/w:br/@w:type='page') 
							and not(
								(following-sibling::w:p[1]/w:pPr/w:sectPr) 
								or (following-sibling::w:p[2]/w:r/w:lastRenderedPageBreak) 
								or (following-sibling::w:p[1]/w:r/w:lastRenderedPageBreak) 
								or (following-sibling::w:sdt[1]/w:sdtPr/w:docPartObj/w:docPartGallery/@w:val='Table of Contents')
							)
						)">
						<xsl:if test="$FootnotesPosition='page'">
							<xsl:call-template name="InsertFootnotes">
								<xsl:with-param name="level" select="0"/>
								<xsl:with-param name="verfoot" select="$version"/>
								<xsl:with-param name="sOperators" select="$sOperators"/>
								<xsl:with-param name="sMinuses" select="$sMinuses"/>
								<xsl:with-param name="sNumbers" select="$sNumbers"/>
								<xsl:with-param name="sZeros" select="$sZeros"/>
							</xsl:call-template>
						</xsl:if>
					</xsl:when>
					<!--Checking for page break-->
					<xsl:when test="(w:r/w:lastRenderedPageBreak)">
						<xsl:if test="$FootnotesPosition='page'">
							<xsl:call-template name="InsertFootnotes">
								<xsl:with-param name="level" select="0"/>
								<xsl:with-param name="verfoot" select="$version"/>
								<xsl:with-param name="sOperators" select="$sOperators"/>
								<xsl:with-param name="sMinuses" select="$sMinuses"/>
								<xsl:with-param name="sNumbers" select="$sNumbers"/>
								<xsl:with-param name="sZeros" select="$sZeros"/>
							</xsl:call-template>
						</xsl:if>
					</xsl:when>
				</xsl:choose>
			</xsl:if>
		</xsl:if>

		<!--Initializing section Information if page number style is automatic-->
		<xsl:if test="(w:pPr/w:sectPr) and not($flag='2') and ($pagination='automatic')">
			<!-- <xsl:call-template name="SectionInfo"/> -->
			<xsl:for-each select="following-sibling::*">
				<xsl:choose>
					<!--Checking for section break-->
					<xsl:when test="w:pPr/w:sectPr">
						<xsl:if test="d:GetSectionPageStart($myObj)=1">
							<xsl:sequence select="d:sink(d:SectionCounter($myObj,w:pPr/w:sectPr/w:pgNumType/@w:fmt,w:pPr/w:sectPr/w:pgNumType/@w:start))"/> <!-- empty -->
						</xsl:if>
					</xsl:when>
					<!--Checking for section break-->
					<xsl:when test="self::w:sectPr">
						<xsl:if test="d:GetSectionPageStart($myObj)=1">
							<xsl:sequence select="d:sink(d:SectionCounter($myObj,w:pgNumType/@w:fmt,w:pgNumType/@w:start))"/> <!-- empty -->
						</xsl:if>
					</xsl:when>
				</xsl:choose>
			</xsl:for-each>
			<xsl:sequence select="d:sink(d:InitalizeSectionPageStart($myObj))"/> <!-- empty -->
		</xsl:if>

		<!-- Convert textboxes as <sidebar render="required"> element -->
		<xsl:if test="(w:r/w:pict//v:textbox/w:txbxContent) 
			and (
				($version='12.0' and not(w:r/w:pict/v:group))
				or (
					(($version='11.0') or ($version='10.0'))
					and not(w:r/w:pict/v:group[@editas='orgchart'])
				)
			)
			and not(w:r/w:pict//v:textbox/w:txbxContent/w:p/w:pPr/w:pStyle[@w:val='Caption'])"
		>
			<xsl:if test="$flag='0'">
				<xsl:value-of disable-output-escaping="yes" select="concat('&lt;/h',$level,'&gt;')"/>
			</xsl:if>
			<!--NP 2025/05/21 :
				I was not able to produce something that match this expression to create sidebar
				I'm replacing it by custom styles for now
				-->
			<xsl:for-each select="w:r/w:pict//v:textbox/w:txbxContent">
				<sidebar render="required">
					<xsl:for-each select="./node()">
						<xsl:choose>
							<!--Checking for Headings in sidebar-->
							<xsl:when test="(w:pPr/w:pStyle[substring(@w:val,1,7)='Heading']) or (w:pPr/w:pStyle/@w:val='BridgeheadDAISY')">
								<hd>
									<xsl:call-template name="ParaHandler">
										<xsl:with-param name="flag" select="'0'"/>
										<xsl:with-param name="txt" select="$txt"/>
										<xsl:with-param name="pagination" select="$pagination"/>
										<xsl:with-param name="charparahandlerStyle" select="$charparahandlerStyle"/>
									</xsl:call-template>
								</hd>
							</xsl:when>
							<!--Checking for lists in sidebar-->
							<xsl:when test="((w:pPr/w:numPr/w:ilvl) and (w:pPr/w:numPr/w:numId))">
								<xsl:call-template name="List">
									<xsl:with-param name="listcharStyle" select="$charparahandlerStyle"/>
								</xsl:call-template>
							</xsl:when>
							<!--Checking for Table in sidebar-->
							<xsl:when test="self::w:tbl">
								<xsl:call-template name="TableHandler">
									<xsl:with-param name="parmVerTable" select="$version"/>
									<xsl:with-param name="pagination" select="$pagination"/>
									<xsl:with-param name="mastersubtbl" select="$mastersubpara"/>
									<xsl:with-param name="characterStyle" select="$charparahandlerStyle"/>
								</xsl:call-template>
							</xsl:when>
							<!--Checking for Prodnote style in sidebar-->
							<xsl:when test="(w:pPr/w:pStyle/@w:val='Prodnote-RequiredDAISY') or (w:pPr/w:pStyle/@w:val='Prodnote-OptionalDAISY')">
								<xsl:call-template name="ParagraphStyle">
									<xsl:with-param name="acceptRevisions" select="$acceptRevisions"/>
									<xsl:with-param name="version" select="$version"/>
									<xsl:with-param name="pagination" select="$pagination"/>
									<xsl:with-param name="txt" select="$txt"/>
									<xsl:with-param name="characterparaStyle" select="$charparahandlerStyle"/>
								</xsl:call-template>
							</xsl:when>
							<xsl:otherwise>
								<xsl:if test="not(w:pPr/w:pStyle/@w:val='List-HeadingDAISY')">
									<!--Calling StyleContainer Template -->
									<xsl:call-template name="StyleContainer">
										<xsl:with-param name="version" select="$version"/>
										<xsl:with-param name="acceptRevisions" select="$acceptRevisions"/>
										<xsl:with-param name="pagination" select="$pagination"/>
										<xsl:with-param name="mastersubstyle" select="$mastersubpara"/>
										<xsl:with-param name="characterStyle" select="$charparahandlerStyle"/>
									</xsl:call-template>
								</xsl:if>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:for-each>
				</sidebar>
			</xsl:for-each>
		</xsl:if>

		<xsl:if test="not($flag='0')
			and not(d:AbbrAcrFlag($myObj)=1)
			and not($flagNote='hyper')"
		>
			<xsl:if test="not(d:GetTestRun($myObj)&gt;='1')
				and (d:GetCodeFlag($myObj)='0')"
			>
				<!--Setting a flag for linenumber-->
				<xsl:sequence select="d:sink(d:Setlinenumflag($myObj))"/> <!-- empty -->
				<xsl:variable name="paragraphLanguage">
					<xsl:call-template name="GetParagraphLanguage">
						<xsl:with-param name="paragraphNode" select="."/>
					</xsl:call-template>
				</xsl:variable>
				<!-- NP 2024/06/14 change language handling -->
				<xsl:variable name="LangAttribute">
					<xsl:if test="not($paragraphLanguage=$documentLanguages/*:lang[1]/@*:val)">
						<xsl:value-of select="concat(' xml:lang=&quot;',$paragraphLanguage,'&quot;')"/>
					</xsl:if>
				</xsl:variable>
				<xsl:value-of disable-output-escaping="yes" select="concat('&lt;','p',$LangAttribute,'&gt;')"/>
			</xsl:if>

			<!--Adding Note index-->
			<xsl:if test="$flagNote='footnote' or $flagNote='endnote'">
				<xsl:if test="d:NoteFlag($myObj)=1">
					<xsl:value-of select="$FootnotesNumberingPrefix"/>
					<xsl:choose>
						<xsl:when test="$FootnotesNumbering = 'number'">
							<xsl:value-of select="$checkid + number($FootnotesStartValue)"/>
						</xsl:when>
					</xsl:choose>
					<xsl:value-of select="$FootnotesNumberingSuffix"/>
				</xsl:if>
			</xsl:if>
		</xsl:if>

		<!--Traversing through each node inside a  word paragraph-->
		<xsl:for-each select="./node()">

			<xsl:if test="self::w:subDoc">
				<xsl:if test="$mastersubpara">
					<!-- <xsl:message terminate="no">progress:Found subdocument reference</xsl:message> -->
					<xsl:variable name="temp" as="xs:string" select="concat('&lt;subdoc rId=&quot;',@r:id,'&quot;&gt;&lt;/subdoc&gt;')"/>
					<xsl:sequence select="d:sink(d:PushMasterSubdoc($myObj,$temp))"/> <!-- empty -->
					<xsl:sequence select="d:sink(d:MasterSubSetFlag($myObj))"/> <!-- empty -->
				</xsl:if>
			</xsl:if>

			<!--Checking condition for MathEquations in Word2007-->
			<xsl:if test="m:oMathPara">
				<xsl:call-template name="ooml2mml">
					<xsl:with-param name="sOperators" select="$sOperators"/>
					<xsl:with-param name="sMinuses" select="$sMinuses"/>
					<xsl:with-param name="sNumbers" select="$sNumbers"/>
					<xsl:with-param name="sZeros" select="$sZeros"/>
				</xsl:call-template>
			</xsl:if>

			<!--Checking condition for MathEquations in Word2007-->
			<xsl:if test="m:oMath">
				<xsl:choose>
					<!--Checking for BDO Element in MathEquation-->
					<xsl:when test="../w:pPr/w:bidi">
						<xsl:value-of disable-output-escaping="yes" select="'&lt;bdo dir= &quot;rtl&quot;&gt;'"/>
						<!--Calling Mathml Template for Math Equations-->
						<xsl:call-template name="ooml2mml">
							<xsl:with-param name="sOperators" select="$sOperators"/>
							<xsl:with-param name="sMinuses" select="$sMinuses"/>
							<xsl:with-param name="sNumbers" select="$sNumbers"/>
							<xsl:with-param name="sZeros" select="$sZeros"/>
						</xsl:call-template>
						<!--Closing bdo Tag-->
						<xsl:value-of disable-output-escaping="yes" select="'&lt;/bdo&gt;'"/>
					</xsl:when>
					<xsl:otherwise>
						<!--Calling Mathml Template for Math Equations-->
						<xsl:call-template name="ooml2mml">
							<xsl:with-param name="sOperators" select="$sOperators"/>
							<xsl:with-param name="sMinuses" select="$sMinuses"/>
							<xsl:with-param name="sNumbers" select="$sNumbers"/>
							<xsl:with-param name="sZeros" select="$sZeros"/>
						</xsl:call-template>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:if>

			<!--Checking condition for MathEquations in Word2007-->
			<xsl:if test="../m:oMath">
				<!--Calling Mathml Template for Math Equations-->
				<xsl:call-template name="ooml2mml">
					<xsl:with-param name="sOperators" select="$sOperators"/>
					<xsl:with-param name="sMinuses" select="$sMinuses"/>
					<xsl:with-param name="sNumbers" select="$sNumbers"/>
					<xsl:with-param name="sZeros" select="$sZeros"/>
				</xsl:call-template>
			</xsl:if>

			<!--Checking for smartTag element-->
			<xsl:if test="self::w:smartTag">
				<xsl:call-template name="smartTag"/>
			</xsl:if>

			<!--Checking for fldSimple element-->
			<xsl:if test="self::w:fldSimple">
				<xsl:call-template name="fldSimple"/>
			</xsl:if>

			<!--Checking for Hyperlink element-->
			<xsl:if test="self::w:hyperlink
				and not(preceding-sibling::w:pPr/w:pStyle[substring(@w:val,1,3)='TOC'])
					">
				<xsl:if test="d:GetTestRun($myObj)&gt;='1'">
					<xsl:value-of disable-output-escaping="yes" select="'&lt;/a&gt;'"/>
					<xsl:sequence select="d:sink(d:SetBookmark($myObj))"/> <!-- empty -->
				</xsl:if>
				<xsl:variable name="href">
					<xsl:choose>
						<!--If both id and anchor attribute is present in hyperlink-->
						<xsl:when test="(@r:id) and (@w:anchor)">
							<xsl:value-of select="concat(d:Anchor($myObj, @r:id, $flagNote),'#',@w:anchor)"/>
						</xsl:when>
						<!--If only anchor for hyperlink is present-->
						<xsl:when test="@w:anchor">
							<xsl:text>#</xsl:text>
							<xsl:value-of select="d:EscapeSpecial(@w:anchor)"/>
						</xsl:when>
						<!--If only id for hyperlink is present-->
						<xsl:when test="@r:id">
							<xsl:value-of select="d:Anchor($myObj, @r:id,$flagNote)"/>
						</xsl:when>
					</xsl:choose>
				</xsl:variable>
				<xsl:variable name="isExternal" select="exists(@r:id) and not(@w:anchor)" />
				<!--Calling CustomCharStyle template with flagNote for hyperlink text-->
				<xsl:for-each select="w:r">
					<xsl:call-template name="CustomCharStyle">
						<xsl:with-param name="attributes">
							<xsl:if test="$href">
								<xsl:text>href="</xsl:text>
								<xsl:value-of select="$href"/>
								<xsl:text>" </xsl:text>
							</xsl:if>
							<xsl:if test="$isExternal">
								<xsl:text>external="true" </xsl:text>
							</xsl:if>
						</xsl:with-param>
					</xsl:call-template>
				</xsl:for-each>
			</xsl:if>

			<!--Checking for BookMarkStart-->

			<xsl:if test="self::w:bookmarkStart">
				<xsl:variable name="aquote">"</xsl:variable>
				<!--Checking whether BookMarkStart is related to Abbreviations or not -->
				<xsl:if test="substring(@w:name,1,13)='Abbreviations'">
					<xsl:call-template name="CloseAllStyleTag" />
					<xsl:variable name="full" as="xs:string" select="d:FullAbbr($myObj,@w:name,$version)"/>
					<xsl:choose>
						<!--Checking whether all previous Abbrevioations tags are closed or not before opening an new Abbreviation tag-->
						<xsl:when test="not(d:AbbrAcrFlag($myObj)=1)">
							<xsl:choose>
								<!--checking whether an Abbreviation is having Full Form or not-->
								<xsl:when test="not($full='')">
									<xsl:value-of disable-output-escaping="yes" select="concat('&lt;','abbr ','title=',$aquote,$full,$aquote,'&gt;')"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of disable-output-escaping="yes" select="concat('&lt;','abbr','&gt;')"/>
								</xsl:otherwise>
							</xsl:choose>
							<xsl:sequence select="d:sink(d:SetAbbrAcrFlag($myObj))"/> <!-- empty -->
						</xsl:when>
						<xsl:otherwise>
							<xsl:variable name="temp">
								<xsl:choose>
									<!--checking whether an Abbreviation is having Full Form or not-->
									<xsl:when test="not($full='')">
										<xsl:value-of  select="concat('&lt;','abbr ','title=',$aquote,$full,$aquote,'&gt;')"/>
									</xsl:when>
									<xsl:otherwise>
										<xsl:value-of select="concat('&lt;','abbr','&gt;')"/>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:variable>
							<xsl:sequence select="d:sink(d:PushAbrAcr($myObj,$temp))"/> <!-- empty -->
						</xsl:otherwise>
					</xsl:choose>
				</xsl:if>
				<!--Checking whether BookMarkStart is related to Acronyms or not -->
				<xsl:if test="substring(@w:name,1,11)='AcronymsYes'">
					<xsl:call-template name="CloseAllStyleTag" />
					<xsl:variable name="full" as="xs:string" select="d:FullAcr($myObj,@w:name,$version)"/>
					<xsl:choose>
						<!--Checking whether all previous Acronyms tags are closed or not before opening an new Acronym tag-->
						<xsl:when test ="not(d:AbbrAcrFlag($myObj)=1)">
							<xsl:choose>
								<!--checking whether an Acronym is having Full Form or not-->
								<xsl:when test="not($full='')">
									<xsl:value-of disable-output-escaping="yes" select="concat('&lt;','acronym ','pronounce=',$aquote,'yes',$aquote,' title=',$aquote,$full,$aquote,'&gt;')"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of disable-output-escaping="yes" select="concat('&lt;','acronym ','pronounce=',$aquote,'yes',$aquote,'&gt;')"/>
								</xsl:otherwise>
							</xsl:choose>
							<xsl:sequence select="d:sink(d:SetAbbrAcrFlag($myObj))"/> <!-- empty -->
						</xsl:when>
						<xsl:otherwise>
							<xsl:variable name="temp">
								<xsl:choose>
									<!--checking whether an Acronym is having Full Form or not-->
									<xsl:when test="not($full='')">
										<xsl:value-of select="concat('&lt;','acronym ','pronounce=',$aquote,'yes',$aquote,' title=',$aquote,$full,$aquote,'&gt;')"/>
									</xsl:when>
									<xsl:otherwise>
										<xsl:value-of  select="concat('&lt;','acronym ','pronounce=',$aquote,'yes',$aquote,'&gt;')"/>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:variable>
							<xsl:sequence select="d:sink(d:PushAbrAcr($myObj,$temp))"/> <!-- empty -->
						</xsl:otherwise>
					</xsl:choose>
				</xsl:if>
				<!--Checking whether BookMarkStart is related to Acronymss or not -->
				<xsl:if test="substring(@w:name,1,10)='AcronymsNo'">
					<xsl:call-template name="CloseAllStyleTag" />
					<xsl:variable name="full" as="xs:string" select="d:FullAcr($myObj,@w:name,$version)"/>
					<xsl:choose>
						<!--Checking whether all previous Acronyms tags are closed or not before opening an new Acronym tag-->
						<xsl:when test="not(d:AbbrAcrFlag($myObj)=1)">
							<xsl:choose>
								<!--checking whether an Acronym is having Full Form or not-->
								<xsl:when test="not($full='')">
									<xsl:value-of disable-output-escaping="yes" select="concat('&lt;','acronym ','pronounce=',$aquote,'no',$aquote,' title=',$aquote,$full,$aquote,'&gt;')"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of disable-output-escaping="yes" select="concat('&lt;','acronym ','pronounce=',$aquote,'no',$aquote,'&gt;')"/>

								</xsl:otherwise>
							</xsl:choose>
							<xsl:sequence select="d:sink(d:SetAbbrAcrFlag($myObj))"/> <!-- empty -->
						</xsl:when>
						<xsl:otherwise>
							<xsl:variable name="temp">
								<xsl:choose>
									<!--checking whether an Acronym is having Full Form or not-->
									<xsl:when test="not($full='')">
										<xsl:value-of select="concat('&lt;','acronym ','pronounce=',$aquote,'no',$aquote,' title=',$aquote,$full,$aquote,'&gt;')"/>
									</xsl:when>
									<xsl:otherwise>
										<xsl:value-of select="concat('&lt;','acronym ','pronounce=',$aquote,'no',$aquote,'&gt;')"/>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:variable>
							<xsl:sequence select="d:sink(d:PushAbrAcr($myObj,$temp))"/> <!-- empty -->
						</xsl:otherwise>
					</xsl:choose>
				</xsl:if>
				<!--Checking for hyperlink-->
				<xsl:if test="d:GetHyperlinkName($myObj,@w:name)=1 and not(substring(@w:name,1,13)='Abbreviations') and not(substring(@w:name,1,11)='AcronymsYes') and not(substring(@w:name,1,10)='AcronymsNo')">
					<xsl:choose>
						<!--If hyperling in Table of content-->
						<xsl:when test="not(contains(@w:name,'_Toc'))">
							<xsl:sequence select="d:sink(d:TestRun($myObj))"/> <!-- empty -->
							<xsl:variable name="initialize" as="xs:integer" select="d:SetHyperLinkFlag($myObj)"/>
							<xsl:call-template name="CloseAllStyleTag" />
							<xsl:if test="$initialize=1">
								<xsl:value-of disable-output-escaping="yes" select="concat('&lt;a id=&quot;',d:EscapeSpecial(@w:name),'&quot;&gt;')"/>
								<xsl:sequence select="d:sink(d:StroreId($myObj,@w:id))"/> <!-- empty -->
							</xsl:if>
							<xsl:if test="$initialize&gt;1">
								<xsl:value-of disable-output-escaping="yes" select="'&lt;/a&gt;'"/>
								<xsl:value-of disable-output-escaping="yes" select="concat('&lt;a id=&quot;',d:EscapeSpecial(@w:name),'&quot;&gt;')"/>
								<xsl:sequence select="d:sink(d:StroreId($myObj,@w:id))"/> <!-- empty -->
							</xsl:if>
						</xsl:when>
					</xsl:choose>
				</xsl:if>

			</xsl:if>

			<!--Checking for BookMarkEnd -->
			<xsl:if test="self::w:bookmarkEnd">
				<xsl:variable name="seperate" as="xs:string">
					<xsl:variable name="id" as="xs:string" select ="@w:id"/>
					<xsl:choose>
						<xsl:when test="$flagNote='footnote' or $flagNote='endnote'">
							<xsl:sequence select="d:BookFootnote($myObj,$id)"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:variable name="tempAbbr" as="xs:string" select="$documentXml//w:bookmarkStart[@w:id=$id]/@w:name"/>
							<xsl:sequence select="d:Book($myObj,$tempAbbr)"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:variable>
				<!--Checking whether BookMarkEnd is related to Abbreviations or not -->
				<xsl:if test="$seperate='AbbrTrue'">
					<!--checking    condition to close abbr Tag -->
					<xsl:call-template name="CloseAllStyleTag" />
					<xsl:value-of disable-output-escaping="yes" select="'&lt;/abbr&gt;'"/>
					<xsl:sequence select="d:sink(d:ReSetAbbrAcrFlag($myObj))"/> <!-- empty -->
					<xsl:if test="d:CountAbrAcr($myObj) &gt; 0">
						<xsl:value-of disable-output-escaping="yes" select="d:PeekAbrAcr($myObj)"/>
					</xsl:if>
				</xsl:if>
				<!--Checking whether BookMarkEnd is related to Acronyms or not -->
				<xsl:if test="$seperate='AcrTrue'">
					<!--checking    condition to close acronym Tag -->
					<xsl:call-template name="CloseAllStyleTag" />
					<xsl:value-of disable-output-escaping="yes" select="'&lt;/acronym&gt;'"/>
					<xsl:sequence select="d:sink(d:ReSetAbbrAcrFlag($myObj))"/> <!-- empty -->
					<xsl:if test="d:CountAbrAcr($myObj) &gt; 0">
						<xsl:value-of disable-output-escaping="yes" select="d:PeekAbrAcr($myObj)"/>
					</xsl:if>
				</xsl:if>
				<!--Closing hyperlink if not heading-->
				<xsl:if test="not(d:GetBookmark($myObj)&gt;0)">
					<xsl:if test="d:CheckId($myObj,@w:id)=1">
						<xsl:sequence select="d:sink(d:SetTestRun($myObj))"/> <!-- empty -->
						<xsl:if test="not(../w:pPr/w:pStyle[substring(@w:val,1,7)='Heading'])">
							<xsl:value-of disable-output-escaping="yes" select="'&lt;/a&gt;'"/>
						</xsl:if>
						<xsl:if test="../w:pPr/w:pStyle[substring(@w:val,1,7)='Heading']">
							<xsl:sequence select="d:sink(d:SetHyperLink($myObj))"/> <!-- empty -->
						</xsl:if>
					</xsl:if>
				</xsl:if>
				<xsl:if test="d:GetBookmark($myObj)&gt;0">
					<xsl:sequence select="d:sink(d:SetTestRun($myObj))"/> <!-- empty -->
				</xsl:if>
			</xsl:if>

			<!--checking sdt element for citation-->
			<xsl:if test="self::w:sdt">
				<!--Checking for Citation Element-->
				<xsl:if test="w:sdtContent/w:fldSimple/w:r">
					<cite>
						<!--Creating variable SupressAuthor for checking    value '\n'-->
						<xsl:variable name="SupressAuthor" as="xs:boolean">
							<xsl:choose>
								<xsl:when test="./w:sdtContent/w:fldSimple/@w:instr">
									<xsl:sequence select="contains(./w:sdtContent/w:fldSimple/@w:instr,'\n')"/>
								</xsl:when>
								<xsl:when test="./w:sdtContent/w:r/w:instrText">
									<xsl:sequence select="contains(./w:sdtContent/w:r/w:instrText,'\n')"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:sequence select="false()"/>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:variable>
						<!--Creating variable SupressTitle for checking    value '\t'-->
						<xsl:variable name="SupressTitle" as="xs:boolean">
							<xsl:choose>
								<xsl:when test="./w:sdtContent/w:fldSimple/@w:instr">
									<xsl:sequence select="contains(./w:sdtContent/w:fldSimple/@w:instr,'\t')"/>
								</xsl:when>
								<xsl:when test="./w:sdtContent/w:r/w:instrText">
									<xsl:sequence select="contains(./w:sdtContent/w:r/w:instrText,'\t')"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:sequence select="false()"/>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:variable>
						<!--Creating variable SupressYear for checking    value '\y'-->
						<xsl:variable name="SupressYear" as="xs:boolean">
							<xsl:choose>
								<xsl:when test="./w:sdtContent/w:fldSimple/@w:instr">
									<xsl:sequence select="contains(./w:sdtContent/w:fldSimple/@w:instr,'\y')"/>
								</xsl:when>
								<xsl:when test="./w:sdtContent/w:r/w:instrText">
									<xsl:sequence select="contains(./w:sdtContent/w:r/w:instrText,'\y')"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:sequence select="false()"/>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:variable>
						<!-- store CitationDetails -->
						<xsl:choose>
							<xsl:when test="./w:sdtContent/w:fldSimple/@w:instr">
								<xsl:sequence select="d:sink(d:CitationDetails($myObj,./w:sdtContent/w:fldSimple/@w:instr))"/> <!-- empty -->
							</xsl:when>
							<xsl:when test="./w:sdtContent/w:r/w:instrText">
								<xsl:sequence select="d:sink(d:CitationDetails($myObj,./w:sdtContent/w:r/w:instrText))"/> <!-- empty -->
							</xsl:when>
						</xsl:choose>
						<xsl:choose>

							<!--Checking for APA style-->
							<xsl:when test="$Cite_style='APA' or $Cite_style='GB7714' or $Cite_style='GOST - Name Sort' or $Cite_style='GOST - Title Sort' or $Cite_style='ISO 690 - First Element and Date' or $Cite_style='Turabian' or $Cite_style='Chicago'">
								<xsl:call-template name="styleCitation">
									<xsl:with-param name="supressAuthor" select="$SupressAuthor"/>
									<xsl:with-param name="supressTitle" select="$SupressTitle"/>
									<xsl:with-param name="supressYear" select="$SupressYear"/>
								</xsl:call-template>
							</xsl:when>
							<!--Checking for MLA style-->
							<xsl:when test="$Cite_style='MLA'">
								<xsl:call-template name="styleCitationMLA">
									<xsl:with-param name="supressAuthor" select="$SupressAuthor"/>
									<xsl:with-param name="supressTitle" select="$SupressTitle"/>
									<xsl:with-param name="supressYear" select="$SupressYear"/>
								</xsl:call-template>
							</xsl:when>
							<!--Checking forISO 690 - Numerical Reference style-->
							<xsl:when test="$Cite_style='ISO 690 - Numerical Reference'">
								<xsl:value-of select="./w:sdtContent//w:t"/>
							</xsl:when>
							<!--Checking for SIST02 style-->
							<xsl:when test="$Cite_style='SIST02'">
								<xsl:call-template name="styleCitationSIST02">
									<xsl:with-param name="supressAuthor" select="$SupressAuthor"/>
									<xsl:with-param name="supressTitle" select="$SupressTitle"/>
									<xsl:with-param name="supressYear" select="$SupressYear"/>
								</xsl:call-template>
							</xsl:when>
						</xsl:choose>
					</cite>
				</xsl:if>
			</xsl:if>

			<xsl:if test="self::w:del">
				<xsl:if test="$acceptRevisions = false()">
					<xsl:for-each select="w:r">
						<xsl:value-of select="w:delText"/>
					</xsl:for-each>
				</xsl:if>
			</xsl:if>

			<xsl:if test="self::w:ins">
				<xsl:if test="$acceptRevisions = true()">
					<xsl:for-each select="w:r">
						<xsl:value-of select="w:t"/>
					</xsl:for-each>
				</xsl:if>
			</xsl:if>

			<!-- Checking for a "run" group (
								that contains a <w:t> for text 
								and  a <w:rPr> for the run properties
								) -->
			<xsl:if test="self::w:r
				and not(
					preceding-sibling::w:pPr/w:pStyle[substring(@w:val,1,3)='TOC']
				)
					">
				<xsl:call-template name="RunHandler">
					<xsl:with-param name="flag" select="$flag"/>
					<xsl:with-param name="imgOptionPara" select="$imgOptionPara"/>
					<xsl:with-param name="dpiPara" select="$dpiPara"/>
					<xsl:with-param name="pagination" select="$pagination"/>
					<xsl:with-param name="txt" select="$txt"/>
					<xsl:with-param name="charparahandlerStyle" select="$charparahandlerStyle"/>
					<xsl:with-param name="version" select="$version"/>
					<xsl:with-param name="sOperators" select="$sOperators"/>
					<xsl:with-param name="sMinuses" select="$sMinuses"/>
					<xsl:with-param name="sNumbers" select="$sNumbers"/>
					<xsl:with-param name="sZeros" select="$sZeros"/>
				</xsl:call-template>
			</xsl:if>

			<!--Capturing Fidelity loss for Copy Right-->
			<xsl:if test="self::w:sdt">
				<xsl:if test="not(w:sdtPr/w:citation)">
					<xsl:message terminate="no">
						<xsl:value-of select="concat('translation.oox2Daisy.UncoveredElement|','Copy Right')"/>
					</xsl:message>
				</xsl:if>
			</xsl:if>

		</xsl:for-each>

		<!--checking    condition to close bdo Tag -->
		<xsl:if test="d:reSetbdoFlag($myObj)&gt;=1">
			<!--Closing BDO tag-->
			<xsl:value-of disable-output-escaping="yes" select="'&lt;/bdo&gt;'"/>
		</xsl:if>

		<xsl:sequence select="d:sink(d:AssingBookmark($myObj))"/> <!-- empty -->

		<!--closing paragraph tag-->
		<xsl:if test="not($flag='0') and not(d:AbbrAcrFlag($myObj)=1) and not($flagNote='hyper')">
			<xsl:if test="not(d:GetTestRun($myObj)&gt;='1') and (d:GetCodeFlag($myObj)='0') and (not(d:Getlinenumflag($myObj)=0))">
				<xsl:call-template name="CloseLevel">
					<xsl:with-param name="CurrentLevel" select="-1"/>
					<xsl:with-param name="verfoot" select="$version"/>
					<xsl:with-param name="characterStyle" select="$charparahandlerStyle"/>
					<xsl:with-param name="sOperators" select="$sOperators"/>
					<xsl:with-param name="sMinuses" select="$sMinuses"/>
					<xsl:with-param name="sNumbers" select="$sNumbers"/>
					<xsl:with-param name="sZeros" select="$sZeros"/>
				</xsl:call-template>
				<xsl:if test="(d:ListMasterSubFlag($myObj)=1) and $mastersubpara">
					<xsl:variable name="curLevel" as="xs:integer" select="d:PeekLevel($myObj)"/>
					<xsl:value-of disable-output-escaping="yes" select="d:ClosingMasterSub($myObj,$curLevel)"/>
					<xsl:value-of disable-output-escaping="yes" select="d:PeekMasterSubdoc($myObj)"/>
					<xsl:sequence select="d:sink(d:MasterSubResetFlag($myObj))"/> <!-- empty -->
					<xsl:value-of disable-output-escaping="yes" select="d:OpenMasterSub($myObj,$curLevel)"/>
				</xsl:if>
			</xsl:if>
		</xsl:if>

		<!--Checking for heading flag and abbr flag and closing paragraph tag-->
		<xsl:if test="not($flag='0') and d:AbbrAcrFlag($myObj)=1">
			<xsl:variable name="temp" as="xs:string" select="'&lt;/p&gt;'"/>
			<xsl:sequence select="d:sink(d:PushAbrAcrpara($myObj,$temp))"/> <!-- empty -->
		</xsl:if>
		<xsl:sequence select="d:sink(d:SetGetHyperLinkFlag($myObj))"/> <!-- empty -->
		<xsl:sequence select="d:sink(d:ReSetListFlag($myObj))"/> <!-- empty -->
		<!--code for hard return-->
		<!--<xsl:text>&#10;</xsl:text>-->

	</xsl:template>


	<!-- Parse paragraph styles and do : 
						- notes references creation 
						- line breaks
						- tab space replacement by space
						- page break checks
	-->
	<xsl:template name="RunHandler">
		<!-- Special flag parameter, with effect on values : 
							- 2 =  -->
		<xsl:param name="flag" as="xs:string"/>
		<xsl:param name="imgOptionPara" as="xs:string"/>
		<xsl:param name="dpiPara" as="xs:float?"/>
		<xsl:param name="pagination" as="xs:string"/>
		<xsl:param name="txt" as="xs:string"/>
		<xsl:param name="charparahandlerStyle" as="xs:boolean"/>
		<xsl:param name="version" as="xs:string"/>
		<!-- For footnote handling-->
		<xsl:param name="sOperators" as="xs:string"/>
		<xsl:param name="sMinuses" as="xs:string"/>
		<xsl:param name="sNumbers" as="xs:string"/>
		<xsl:param name="sZeros" as="xs:string"/>

		<!--Checking for line breaks-->
		<xsl:if test="((w:br/@w:type='textWrapping') or (w:br)) and (not(w:br/@w:type='page')) and not(w:rPr/w:rStyle/@w:val='PageNumberDAISY')">
			<br/>
		</xsl:if>
		<!--Checking for tabs-->
		<xsl:if test="w:tab">
			<xsl:text> </xsl:text>
		</xsl:if>

		<!--Checking for page breaks and populating page numbers.-->
		<!-- DB : if list skip this-->
		<xsl:if test="(
				(w:lastRenderedPageBreak) or (w:br/@w:type='page')
			) and not($flag='0')
			and ($pagination='automatic')
			and not(
				(
					(../w:pPr/w:numPr/w:ilvl)
					and (../w:pPr/w:numPr/w:numId)
					and not(../w:pPr/w:rPr/w:vanish)
				)
				and not(../w:pPr/w:pStyle[substring(@w:val,1,7)='Heading'])
			)">
			<xsl:if test="not($flag='2')">
				<xsl:choose>
					<!--runner with no text + pagebreak + not followed by TOC + not preceded by section-->
					<xsl:when test="not(w:t)
						and (w:lastRenderedPageBreak) 
						and (w:br/@w:type='page')
						and not(../following-sibling::w:sdt[1]/w:sdtPr/w:docPartObj/w:docPartGallery/@w:val='Table of Contents')
						and not(../preceding-sibling::node()[1]/w:pPr/w:sectPr)">
						<xsl:sequence select="d:sink(d:IncrementPage($myObj))"/> <!-- empty -->
						<!--Closing paragraph tag-->
						<xsl:call-template name="CloseLevel">
							<xsl:with-param name="CurrentLevel" select="-1"/>
							<xsl:with-param name="verfoot" select="$version"/>
							<xsl:with-param name="characterStyle" select="$charparahandlerStyle"/>
							<xsl:with-param name="sOperators" select="$sOperators"/>
							<xsl:with-param name="sMinuses" select="$sMinuses"/>
							<xsl:with-param name="sNumbers" select="$sNumbers"/>
							<xsl:with-param name="sZeros" select="$sZeros"/>
						</xsl:call-template>
						<xsl:if test="$flag='3'">
							<!--Closing paragraph tag-->
							<xsl:value-of disable-output-escaping="yes" select="'&lt;p&gt;'"/>
						</xsl:if>
						<!--calling template to initialize page number information-->
						<xsl:call-template name="SectionBreak">
							<xsl:with-param name="count" select="1"/>
							<xsl:with-param name="node" select="'body'"/>
						</xsl:call-template>
						<!--producer note for blank pages-->
						<prodnote render="optional">
							<xsl:value-of select="'Blank Page'"/>
						</prodnote>
						<xsl:if test="$flag='3'">
							<!--Closing paragraph tag-->
							<xsl:call-template name="CloseLevel">
								<xsl:with-param name="CurrentLevel" select="-1"/>
								<xsl:with-param name="verfoot" select="$version"/>
								<xsl:with-param name="characterStyle" select="$charparahandlerStyle"/>
								<xsl:with-param name="sOperators" select="$sOperators"/>
								<xsl:with-param name="sMinuses" select="$sMinuses"/>
								<xsl:with-param name="sNumbers" select="$sNumbers"/>
								<xsl:with-param name="sZeros" select="$sZeros"/>
							</xsl:call-template>
						</xsl:if>
						<!--Opening paragraph tag-->
						<xsl:value-of disable-output-escaping="yes" select="'&lt;p&gt;'"/>
					</xsl:when>
					<!--Checking for page breaks and populating page numbers.-->
					<xsl:when test="(
							(w:br/@w:type='page')
							and not(
								(../following-sibling::w:p[1]/w:pPr/w:sectPr)
								or (following-sibling::w:r[1]/w:lastRenderedPageBreak)
								or (../following-sibling::w:p[2]/w:r/w:lastRenderedPageBreak)
								or (../following-sibling::w:p[1]/w:r/w:lastRenderedPageBreak)
							)
						)">
						<!--Incrementing page numbers-->
						<xsl:sequence select="d:sink(d:IncrementPage($myObj))"/> <!-- empty -->
						<!--Closing paragraph tag-->
						<!--<xsl:value-of disable-output-escaping="yes" select="concat('&lt;','/p','&gt;')"/>-->
						<xsl:call-template name="CloseLevel">
							<xsl:with-param name="CurrentLevel" select="-1"/>
							<xsl:with-param name="verfoot" select="$version"/>
							<xsl:with-param name="characterStyle" select="$charparahandlerStyle"/>
							<xsl:with-param name="sOperators" select="$sOperators"/>
							<xsl:with-param name="sMinuses" select="$sMinuses"/>
							<xsl:with-param name="sNumbers" select="$sNumbers"/>
							<xsl:with-param name="sZeros" select="$sZeros"/>
						</xsl:call-template>
						<xsl:if test="$flag='3'">
							<!--Opening paragraph tag-->
							<xsl:value-of disable-output-escaping="yes" select="'&lt;p&gt;'"/>
						</xsl:if>
						<!--calling template to initialize page number information-->
						<xsl:call-template name="SectionBreak">
							<xsl:with-param name="count" select="1"/>
							<xsl:with-param name="node" select="'body'"/>
						</xsl:call-template>
						<xsl:if test="$flag='3'">
							<!--Closing paragraph tag-->
							<xsl:call-template name="CloseLevel">
								<xsl:with-param name="CurrentLevel" select="-1"/>
								<xsl:with-param name="verfoot" select="$version"/>
								<xsl:with-param name="characterStyle" select="$charparahandlerStyle"/>
								<xsl:with-param name="sOperators" select="$sOperators"/>
								<xsl:with-param name="sMinuses" select="$sMinuses"/>
								<xsl:with-param name="sNumbers" select="$sNumbers"/>
								<xsl:with-param name="sZeros" select="$sZeros"/>
							</xsl:call-template>
						</xsl:if>
						<!--Opening paragraph tag-->
						<xsl:value-of disable-output-escaping="yes" select="'&lt;p&gt;'"/>
					</xsl:when>
					<!-- Pagebreak not in section neither in Index-->
					<xsl:when test="(w:lastRenderedPageBreak)
						and not(
							../w:pPr/w:sectPr                                        
							or ../w:pPr/w:pStyle[substring(@w:val,1,5)='Index']
						)">
						<xsl:sequence select="d:sink(d:IncrementPage($myObj))"/> <!-- empty -->

						<!--calling template to initialize page number information-->
						<xsl:call-template name="SectionBreak">
							<xsl:with-param name="count" select="1"/>
							<xsl:with-param name="node" select="'body'"/>
						</xsl:call-template>
					</xsl:when>
				</xsl:choose>
			</xsl:if>
		</xsl:if>

		<xsl:choose>
			<!--Checking for Images-->
			<xsl:when test="w:pict">
				<xsl:choose>
					<!--checking for Images in word2003 version-->
					<xsl:when test="w:pict/v:shape/v:imagedata/@r:id">
						<xsl:call-template name="Imagegroup2003">
							<xsl:with-param name="characterStyle" select="$charparahandlerStyle"/>
						</xsl:call-template>
					</xsl:when>
					<!--checking image groups-->
					<xsl:when test="(
						w:pict/v:group
						and (
							$version='12.0'
							or (
								($version='11.0' or $version='10.0')
								and not(descendant::w:txbxContent)
								and not(w:pict/v:rect/v:textbox/w:txbxContent)
							)
						)
					)">
						<xsl:call-template name="Imagegroups">
							<xsl:with-param name="characterStyle" select="$charparahandlerStyle"/>
						</xsl:call-template>
					</xsl:when>
					<xsl:when test="(
							w:pict/v:shape/@o:spid
							and not(descendant::w:txbxContent)
							and (not(w:pict/v:rect/v:textbox/w:txbxContent))
						) or (
							w:pict/v:shape
							and not(contains(w:pict/v:shape/@id,'i'))
							and not(descendant::w:txbxContent)
							and not(w:pict/v:rect/v:textbox/w:txbxContent)
						) or (
							not(w:pict/v:shape/v:textbox)
							and not(contains(w:pict/v:shape/@id,'i'))
							and not(w:pict/v:rect/v:textbox/w:txbxContent)
							and (
								not(($version='11.0') or ($version='10.0'))
								or $version='12.0'
							)
					)">
						<xsl:call-template name="tmpShape">
							<xsl:with-param name="characterStyle" select="$charparahandlerStyle"/>
						</xsl:call-template>
					</xsl:when>
				</xsl:choose>
			</xsl:when>

			<!--checking for images in word2007 version-->
			<xsl:when test="w:drawing">
				<xsl:call-template name="PictureHandler">
					<xsl:with-param name="imgOpt" select="$imgOptionPara"/>
					<xsl:with-param name="dpi" select="$dpiPara"/>
					<xsl:with-param name="characterStyle" select="$charparahandlerStyle"/>
				</xsl:call-template>
			</xsl:when>

			<!--Checking for objects-->
			<xsl:when test="w:object/o:OLEObject">
				<xsl:choose>
					<!--Checking for Design science Math Equations-->
					<xsl:when test="w:object/o:OLEObject[@ProgID='Equation.DSMT4']">

						<xsl:variable name="Math_DSMT4" as="xs:string">
							<xsl:choose>
								<xsl:when test="ancestor::w:txbxContent">
									<xsl:sequence select="d:GetMathML($myObj,'wdTextFrameStory')"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:sequence select="d:GetMathML($myObj,'wdMainTextStory')"/>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:variable>

						<xsl:choose>
							<xsl:when test="$Math_DSMT4=''">
								<xsl:variable name="alttext">
									<xsl:choose>
										<xsl:when test="w:object/v:shape/@alt">
											<xsl:value-of select="w:object/v:shape/@alt"/>
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of select="'Math Equation'"/>
											<!--Hardcoding value 'Math Equation'if user donot provide alt text for Math Equations-->
										</xsl:otherwise>
									</xsl:choose>
								</xsl:variable>
								<!--Creating variable mathimage for storing r:id value from document.xml-->
								<xsl:variable name="Math_rid" as="xs:string" select="w:object/v:shape/v:imagedata/@r:id"/>
								<imggroup>
									<img alt="{$alttext}" src="{d:MathImage($myObj,$Math_rid)}" />
								</imggroup>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of disable-output-escaping="yes" select="$Math_DSMT4"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:when>
					<!--Checking condition for MathEquations in word 2003/xp-->
					<xsl:when test="contains(w:object/o:OLEObject/@ProgID,'Equation')and not(w:object/o:OLEObject[@ProgID='Equation.DSMT4'])">
						<xsl:variable name="mathimage" as="xs:string" select="w:object/v:shape/v:imagedata/@r:id"/>
						<xsl:variable name="alt">
							<xsl:choose>
								<xsl:when test="w:object/v:shape/@alt">
									<xsl:value-of select="w:object/v:shape/@alt"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="'Math Equation'"/>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:variable>
						<imggroup>
							<img alt="{$alt}" src="{d:MathImage($myObj,$mathimage)}"/>
						</imggroup>
					</xsl:when>
					<xsl:otherwise>
						<xsl:call-template name="Object">
							<xsl:with-param name="characterStyle" select="$charparahandlerStyle"/>
						</xsl:call-template>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>

			<!--Checking for footnotes in word2003 and word2007 -->
			<xsl:when test="w:footnoteReference">
				<xsl:variable name="footnoteid" as="xs:integer" select="w:footnoteReference/@w:id"/>
				<xsl:sequence select="d:sink(d:AddFootNote($myObj,$footnoteid))"/> <!-- empty -->
				<xsl:call-template name="NoteReference">
					<xsl:with-param name="noteID" select="$footnoteid"/>
					<xsl:with-param name="noteClass" select="'FootnoteReference'"/>
				</xsl:call-template>
			</xsl:when>

			<!--Checking for endnotes in word2003 and word2007-->
			<xsl:when test="w:endnoteReference">
				<xsl:call-template name="NoteReference">
					<xsl:with-param name="noteID" select="w:endnoteReference/@w:id"/>
					<xsl:with-param name="noteClass" select="'EndnoteReference'"/>
				</xsl:call-template>
			</xsl:when>

			<xsl:otherwise>
				<!--Calling Custom styles template if not caption-->
				<xsl:if test="not(
						(
							(
								(../w:pPr/w:pStyle/@w:val='Table-CaptionDAISY')
								or (../w:pPr/w:pStyle/@w:val='Caption')
							) and (
								(../following-sibling::node()[1][self::w:tbl])
								or (../preceding-sibling::node()[1][self::w:tbl])
							)
						) or (
							(../w:pPr/w:pStyle/@w:val='Image-CaptionDAISY')
							and (
								(../following-sibling::node()[1]/w:r/w:drawing)
								or (../following-sibling::node()[1]/w:r/w:pict)
								or (../following-sibling::node()[1]/w:r/w:object)
								or (../w:r/w:drawing)
								or (../w:r/w:pict)
								or (../w:r/w:object)
							)
						)
					)">
					<xsl:choose>
						<xsl:when test="d:ListFlag($myObj)=0">
							<xsl:sequence select="d:sink(d:SetListFlag($myObj))"/> <!-- empty -->
							<xsl:call-template name="CustomStyles">
								<xsl:with-param name="customTag" select="(w:rPr/w:rStyle/@w:val,'')[1]"/>
								<xsl:with-param name="pagination" select="$pagination"/>
								<xsl:with-param name="txt" select="$txt"/>
								<xsl:with-param name="customcharStyle" select="$charparahandlerStyle"/>
							</xsl:call-template>
						</xsl:when>
						<xsl:otherwise>
							<xsl:call-template name="CustomStyles">
								<xsl:with-param name="customTag" select="(w:rPr/w:rStyle/@w:val,'')[1]"/>
								<xsl:with-param name="pagination" select="$pagination"/>
								<xsl:with-param name="customcharStyle" select="$charparahandlerStyle"/>
							</xsl:call-template>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:if>
			</xsl:otherwise>
		</xsl:choose>

		<!--Initializing Hyperlink flag-->
		<xsl:sequence select="d:sink(d:SetGetHyperLinkFlag($myObj))"/> <!-- empty -->
		<!--Closing hyperlink tag for headings-->
		<xsl:if test="(d:GetFlag($myObj)&gt;=1) and (../w:pPr/w:pStyle[substring(@w:val,1,7)='Heading'])">
			<xsl:value-of disable-output-escaping="yes" select="'&lt;/a&gt;'"/>
			<xsl:sequence select="d:sink(d:GetHyperLink($myObj))"/> <!-- empty -->
		</xsl:if>

	</xsl:template>

	<!--Template for smartTag-->
	<xsl:template name="smartTag">
		<xsl:for-each select="w:r">
			<xsl:value-of select="w:t"/>
		</xsl:for-each>
		<xsl:for-each select="w:smartTag">
			<xsl:for-each select="w:r">
				<xsl:value-of select="w:t"/>
			</xsl:for-each>
		</xsl:for-each>
	</xsl:template>

	<!--Template for fldSimple-->
	<xsl:template name="fldSimple">
		<xsl:for-each select="w:r">
			<xsl:value-of select="w:t"/>
		</xsl:for-each>
	</xsl:template>


	<!--Template to implement citation styles-->
	<xsl:template name="styleCitation">
		<xsl:param name="supressAuthor" as="xs:boolean"/>
		<xsl:param name="supressTitle" as="xs:boolean"/>
		<xsl:param name="supressYear" as="xs:boolean"/>
		<xsl:choose>
			<!--Checking condition for supressing Author,Title,Year-->
			<xsl:when test="$supressAuthor and $supressTitle and $supressYear">
				<xsl:value-of select="./w:sdtContent//w:t"/>
			</xsl:when>
			<!--Checking condition for supressing Author,Title-->
			<xsl:when test="$supressAuthor and $supressTitle">
				<xsl:value-of select="d:GetYear($myObj)"/>
			</xsl:when>
			<!--Checking condition for supressing Author,Year-->
			<xsl:when test="$supressAuthor and $supressYear">
				<xsl:text>(</xsl:text>
				<title>
					<xsl:value-of select="d:GetTitle($myObj)"/>
				</title>
				<xsl:text>)</xsl:text>
			</xsl:when>
			<!--Checking condition for supressing Title,Year-->
			<xsl:when test="$supressTitle and $supressYear">
				<xsl:text>(</xsl:text>
				<author>
					<xsl:value-of select="d:GetAuthor($myObj)"/>
				</author>
				<xsl:text>)</xsl:text>
			</xsl:when>
			<!--Checking condition for supressing Author-->
			<xsl:when test="$supressAuthor">
				<xsl:text>(</xsl:text>
				<title>
					<xsl:value-of select="d:GetTitle($myObj)"/>
				</title>
				<xsl:text>,</xsl:text>
				<xsl:value-of select="d:GetYear($myObj)"/>
				<xsl:text>)</xsl:text>
			</xsl:when>
			<!--Checking condition for supressing Title-->
			<xsl:when test="$supressTitle">
				<xsl:text>(</xsl:text>
				<author>
					<xsl:value-of select="d:GetAuthor($myObj)"/>
				</author>
				<xsl:text>,</xsl:text>
				<xsl:value-of select="d:GetYear($myObj)"/>
				<xsl:text>)</xsl:text>
			</xsl:when>
			<!--Checking condition for supressing Year-->
			<xsl:when test="$supressYear">
				<xsl:text>(</xsl:text>
				<author>
					<!--Calling GetAuthor function to get the value of Author-->
					<xsl:value-of select="d:GetAuthor($myObj)"/>
				</author>
				<xsl:text>)</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:choose>
					<xsl:when test="d:GetAuthor($myObj)=''">
						<xsl:value-of select="./w:sdtContent//w:t"/>
					</xsl:when>
					<xsl:when test="(d:GetAuthor($myObj)='') and (d:GetYear($myObj)='') ">
						<title>
							<!--Calling GetTitle function to get the value of Title-->
							<xsl:value-of select="d:GetTitle($myObj)"/>
						</title>
					</xsl:when>
					<xsl:when test="(d:GetAuthor($myObj)='') and (d:GetTitle($myObj)='') and (d:GetYear($myObj)='')">
						<xsl:value-of select="./w:sdtContent//w:t"/>
					</xsl:when>
					<xsl:when test="(d:GetTitle($myObj)='') and (d:GetYear($myObj)='')">
						<author>
							<xsl:text>(</xsl:text>
							<!--Calling GetAuthor function to get the value of Author-->
							<xsl:value-of select="d:GetAuthor($myObj)"/>
							<xsl:text>)</xsl:text>
						</author>
					</xsl:when>
					<xsl:when test="(d:GetAuthor($myObj)='') and (d:GetTitle($myObj)='')">
						<!--Calling GetYear function to get the value of Year-->
						<xsl:value-of select="d:GetYear($myObj)"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:text>(</xsl:text>
						<author>
							<!--Calling GetAuthor function to get the value of Author-->
							<xsl:value-of select="d:GetAuthor($myObj)"/>
						</author>
						<xsl:text>,</xsl:text>
						<!--Calling GetYear function to get the value of the Year-->
						<xsl:value-of select="d:GetYear($myObj)"/>
						<xsl:text>)</xsl:text>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!--Template to implement citation styles MLA-->
	<xsl:template name="styleCitationMLA">
		<xsl:param name="supressAuthor" as="xs:boolean"/>
		<xsl:param name="supressTitle" as="xs:boolean"/>
		<xsl:param name="supressYear" as="xs:boolean"/>
		<xsl:choose>
			<!--Checking condition for supressing Author,Title,Year-->
			<xsl:when test="$supressAuthor and $supressTitle and $supressYear">
				<xsl:value-of select="./w:sdtContent//w:t"/>
			</xsl:when>
			<xsl:when test="$supressAuthor">
				<title>
					<!--Calling GetTitle function to get the value of the Title-->
					<xsl:value-of select="d:GetTitle($myObj)"/>
				</title>
			</xsl:when>
			<xsl:when test="$supressTitle">
				<author>
					<!--Calling GetAuthor function to get the value of the Author-->
					<xsl:value-of select="d:GetAuthor($myObj)"/>
				</author>
			</xsl:when>
			<xsl:otherwise>
				<xsl:choose>
					<xsl:when test="d:GetAuthor($myObj)=''">
						<xsl:value-of select="./w:sdtContent//w:t"/>
					</xsl:when>
					<xsl:when test="(d:GetAuthor($myObj)='') and (d:GetTitle($myObj)='') and (d:GetYear($myObj)='')">
						<xsl:value-of select="./w:sdtContent//w:t"/>
					</xsl:when>
					<xsl:when test="(d:GetTitle($myObj)='')">
						<author>
							<xsl:text>(</xsl:text>
							<xsl:value-of select="d:GetAuthor($myObj)"/>
							<xsl:text>)</xsl:text>
						</author>
					</xsl:when>
					<xsl:otherwise>
						<author>
							<xsl:text>(</xsl:text>
							<xsl:value-of select="d:GetAuthor($myObj)"/>
							<xsl:text>)</xsl:text>
						</author>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!--Template to implement citation styleSIST02-->
	<xsl:template name="styleCitationSIST02">
		<xsl:param name="supressAuthor" as="xs:boolean"/>
		<xsl:param name="supressTitle" as="xs:boolean"/>
		<xsl:param name="supressYear" as="xs:boolean"/>
		<xsl:choose>
			<!--Checking condition for supressing Author,Title,Year-->
			<xsl:when test="$supressAuthor and $supressTitle and $supressYear">
				<xsl:for-each select="./w:sdtContent/w:fldSimple/w:r">
					<xsl:value-of select="w:t"/>
				</xsl:for-each>
			</xsl:when>
			<xsl:when test="$supressAuthor">
				<xsl:text>(</xsl:text>
				<!--Calling GetYear function to get the value of the Year-->
				<xsl:value-of select="d:GetYear($myObj)"/>
				<xsl:text>)</xsl:text>
			</xsl:when>
			<!--Checking condition for supressing Author,Year-->
			<xsl:when test="$supressAuthor and $supressYear">
				<xsl:value-of select="./w:sdtContent//w:t"/>
			</xsl:when>
			<!--Checking condition for supressing Title,Year-->
			<xsl:when test="$supressTitle and $supressYear">
				<author>
					<xsl:text>(</xsl:text>
					<!--Calling GetAuthor function to get the value of the Author-->
					<xsl:value-of select="d:GetAuthor($myObj)"/>
					<xsl:text>)</xsl:text>
				</author>
			</xsl:when>
			<!--Checking condition for supressing Author-->
			<xsl:when test="$supressAuthor">
				<!--Calling GetYear function to get the value of the Year-->
				<xsl:value-of select="d:GetYear($myObj)"/>
			</xsl:when>
			<!--Checking condition for supressing Year-->
			<xsl:when test="$supressYear">
				<author>
					<xsl:text>(</xsl:text>
					<!--Calling GetAuthor function to get the value of the Author-->
					<xsl:value-of select="d:GetAuthor($myObj)"/>
					<xsl:text>)</xsl:text>
				</author>
			</xsl:when>
			<xsl:otherwise>
				<xsl:choose>
					<xsl:when test="d:GetAuthor($myObj)=''">
						<xsl:for-each select="./w:sdtContent/w:fldSimple/w:r">
							<xsl:value-of select="w:t"/>
						</xsl:for-each>
					</xsl:when>
					<xsl:when test="(d:GetAuthor($myObj)='') and (d:GetTitle($myObj)='') and (d:GetYear($myObj)='')">
						<xsl:for-each select="./w:sdtContent/w:fldSimple/w:r">
							<xsl:value-of select="w:t"/>
						</xsl:for-each>
					</xsl:when>
					<xsl:when test="(d:GetTitle($myObj)='') and (d:GetYear($myObj)='')">
						<author>
							<xsl:text>(</xsl:text>
							<!--Calling GetAuthor function to get the value of the Author-->
							<xsl:value-of select="d:GetAuthor($myObj)"/>
							<xsl:text>)</xsl:text>
						</author>
					</xsl:when>
					<xsl:otherwise>
						<xsl:text>(</xsl:text>
						<author>
							<!--Calling GetAuthor function to get the value of the Author-->
							<xsl:value-of select="d:GetAuthor($myObj)"/>
						</author>
						<xsl:text>,</xsl:text>
						<!--Calling GetYear function to get the value of the Year-->
						<xsl:value-of select="d:GetYear($myObj)"/>
						<xsl:text>)</xsl:text>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!--Template for Custom styles-->
	<xsl:template name="CustomStyles">
		<xsl:param name="customTag" as="xs:string"/>
		<xsl:param name="pagination" as="xs:string"/>
		<xsl:param name="txt" as="xs:string" select="''"/>
		<xsl:param name="customcharStyle" as="xs:boolean"/>
		<xsl:choose>
			<!--Checking for SampleDAISY/HTMLSample custom character style-->
			<xsl:when test="($customTag='SampleDAISY') or ($customTag='HTMLSample')" >
				<samp>
					<xsl:attribute name="xml:space">preserve</xsl:attribute>
					<xsl:call-template name="CustomCharStyle">
						<xsl:with-param name="characterStyle" select="$customcharStyle"/>
						<xsl:with-param name="txt" select="$txt"/>
					</xsl:call-template>
				</samp>
			</xsl:when>
			<!--Checking for QuotationDAISY custom character style-->
			<xsl:when test="$customTag='QuotationDAISY'" >
				<q>
					<xsl:call-template name="CustomCharStyle">
						<xsl:with-param name="characterStyle" select="$customcharStyle"/>
						<xsl:with-param name="txt" select="$txt"/>
					</xsl:call-template>
				</q>
			</xsl:when>
			<!--Checking for CodeDAISY/HTMLCode custom character style-->
			<xsl:when test="($customTag='CodeDAISY') or ($customTag='HTMLCode')">
				<xsl:if test="count(preceding-sibling::w:r[1]/w:rPr/w:rStyle[contains(@w:val,'Code')])=0">
					<xsl:value-of disable-output-escaping="yes" select="'&lt;code xml:space=&quot;preserve&quot;&gt;'"/>
					<xsl:sequence select="d:sink(d:CodeFlag($myObj))"/> <!-- empty -->
				</xsl:if>
				<xsl:call-template name="CustomCharStyle">
					<xsl:with-param name="characterStyle" select="$customcharStyle"/>
					<xsl:with-param name="txt" select="$txt"/>
				</xsl:call-template>
				<xsl:if test="count(following-sibling::w:r[1]/w:rPr/w:rStyle[contains(@w:val,'Code')])=0">
					<xsl:value-of disable-output-escaping="yes" select="'&lt;/code&gt;'"/>
					<xsl:sequence select="d:sink(d:InitializeCodeFlag($myObj))"/> <!-- empty -->
				</xsl:if>
			</xsl:when>
			<!--Checking for SentDAISY custom character style-->
			<xsl:when test="($customTag='SentDAISY')">
				<xsl:if test="count(preceding-sibling::w:r[1]/w:rPr/w:rStyle[contains(@w:val,'Sent')])=0">
					<xsl:value-of disable-output-escaping="yes" select="'&lt;sent&gt;'"/>
				</xsl:if>
				<xsl:call-template name="CustomCharStyle">
					<xsl:with-param name="characterStyle" select="$customcharStyle"/>
					<xsl:with-param name="txt" select="$txt"/>
				</xsl:call-template>
				<xsl:if test="count(following-sibling::w:r[1]/w:rPr/w:rStyle[contains(@w:val,'Sent')])=0">
					<xsl:value-of disable-output-escaping="yes" select="'&lt;/sent&gt;'"/>
				</xsl:if>
			</xsl:when>
			<!--Checking for SpanDAISY custom character style-->
			<xsl:when test="($customTag='SpanDAISY')">
				<xsl:if test="count(preceding-sibling::w:r[1]/w:rPr/w:rStyle[contains(@w:val,'Span')])=0">
					<xsl:value-of disable-output-escaping="yes" select="'&lt;span&gt;'"/>
				</xsl:if>
				<xsl:call-template name="CustomCharStyle">
					<xsl:with-param name="characterStyle" select="$customcharStyle"/>
					<xsl:with-param name="txt" select="$txt"/>
				</xsl:call-template>
				<xsl:if test="count(following-sibling::w:r[1]/w:rPr/w:rStyle[contains(@w:val,'Span')])=0">
					<xsl:value-of disable-output-escaping="yes" select="'&lt;/span&gt;'"/>
				</xsl:if>
			</xsl:when>
			<!--Checking for DefinitionDAISY/HTMLDefinition custom character style-->
			<xsl:when test="($customTag='DefinitionDAISY') or ($customTag='HTMLDefinition')">
				<dfn>
					<xsl:call-template name="CustomCharStyle">
						<xsl:with-param name="characterStyle" select="$customcharStyle"/>
						<xsl:with-param name="txt" select="$txt"/>
					</xsl:call-template>
				</dfn>
			</xsl:when>
			<!--Checking for CitationDAISY/HTMLCite custom character style-->
			<xsl:when test="($customTag='CitationDAISY')or ($customTag='HTMLCite')">
				<cite>
					<xsl:call-template name="CustomCharStyle">
						<xsl:with-param name="characterStyle" select="$customcharStyle"/>
						<xsl:with-param name="txt" select="$txt"/>
					</xsl:call-template>
				</cite>
			</xsl:when>
			<!--Checking for KeyboardInputDAISY/HTMLKeyboard custom character style-->
			<xsl:when test="($customTag='KeyboardInputDAISY') or ($customTag='HTMLKeyboard')">
				<kbd>
					<xsl:call-template name="CustomCharStyle">
						<xsl:with-param name="characterStyle" select="$customcharStyle"/>
						<xsl:with-param name="txt" select="$txt"/>
					</xsl:call-template>
				</kbd>
			</xsl:when>
			<!--Checking for Page number custom style-->
			<xsl:when test="($customTag='PageNumberDAISY') and ($pagination='custom')">
				<xsl:if test="count(preceding-sibling::w:r[1]/w:rPr/w:rStyle[@w:val='PageNumberDAISY'])=0">
					<xsl:variable name="page" as="xs:string">
						<xsl:choose>
							<xsl:when test="string(number(w:t))='NaN'">
								<xsl:sequence select="'special'"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:sequence select="'normal'"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:variable>
					<!--Opening page number tag-->
					<xsl:value-of disable-output-escaping="yes" select="concat('&lt;pagenum page=&quot;',$page,'&quot; id=&quot;page',d:GeneratePageId($myObj),'&quot;&gt;')"/>
				</xsl:if>
				<!-- pagenum only accept text-->
				<xsl:if test="not($txt='')">
					<xsl:value-of select="$txt"/>
				</xsl:if>
				<xsl:value-of select="normalize-space(w:t)"/>
				<xsl:if test="count(following-sibling::node()[1]/w:rPr/w:rStyle[@w:val='PageNumberDAISY'])=0">
					<xsl:value-of disable-output-escaping="yes" select="'&lt;/pagenum&gt;'"/>
				</xsl:if>
			</xsl:when>
			<xsl:when test="($customTag='LineNumberDAISY')">
				<xsl:choose>
					<xsl:when test="(../w:pPr/w:pStyle[@w:val='PoemDAISY']) or (../w:pPr/w:pStyle[@w:val='AddressDAISY'])">
						<linenum>
							<xsl:call-template name="CustomCharStyle">
								<xsl:with-param name="characterStyle" select="$customcharStyle"/>
								<xsl:with-param name="txt" select="$txt"/>
							</xsl:call-template>
						</linenum>
					</xsl:when>
					<xsl:when test="(../w:pPr/w:pStyle[@w:val='DefinitionDataDAISY'])">
						<line>
							<linenum>
								<xsl:call-template name="CustomCharStyle">
									<xsl:with-param name="characterStyle" select="$customcharStyle"/>
									<xsl:with-param name="txt" select="$txt"/>
								</xsl:call-template>
							</linenum>
						</line>
					</xsl:when>
					<xsl:when test="(d:Getlinenumflag($myObj)=1)">
						<!-- NP 20240109 : close all inlines before closing paragraph -->
						<xsl:call-template name="CloseAllStyleTag"/>
						<xsl:value-of disable-output-escaping="yes" select="'&lt;/p&gt;'"/>
						<line>
							<linenum>
								<xsl:call-template name="CustomCharStyle">
									<xsl:with-param name="characterStyle" select="$customcharStyle"/>
									<xsl:with-param name="txt" select="$txt"/>
								</xsl:call-template>
							</linenum>
						</line>
						<xsl:choose>
							<xsl:when test="(following-sibling::node()[1][self::w:r]) and (not(following-sibling::node()[1]/w:rPr/w:rStyle[contains(@w:val,'Line')]))">
								<xsl:value-of disable-output-escaping="yes" select="'&lt;p&gt;'"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:sequence select="d:sink(d:Resetlinenumflag($myObj))"/> <!-- empty -->
							</xsl:otherwise>
						</xsl:choose>
					</xsl:when>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="CustomCharStyle">
					<xsl:with-param name="characterStyle" select="$customcharStyle"/>
					<xsl:with-param name="txt" select="$txt"/>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- template to open a style tag (em,strong,sup or sub)-->
	<xsl:template name="OpenStyleTagIfNotOpened">
		<xsl:param name="styleTag"/>
		<xsl:param name="attributes" select="''"/>
		<xsl:if test="not(d:HasCharacterStyle($myObj, $styleTag))">
			<xsl:sequence select="d:PushCharacterStyle($myObj, $styleTag)" />
			<xsl:value-of disable-output-escaping="yes"
						  select="concat('&lt;', $styleTag, ' ', normalize-space($attributes), '&gt;')"/>
		</xsl:if>
	</xsl:template>

	<!-- template to close a style tag (and its children if needed recursively) -->
	<xsl:template name="CloseStyleTag">
		<xsl:param name="styleTag"/>
		<xsl:if test="d:HasCharacterStyle($myObj, $styleTag)">
			<xsl:variable name="currentTagInStack" select="d:PopCharacterStyle($myObj)" />
			<xsl:value-of disable-output-escaping="yes" select="concat('&lt;/',$currentTagInStack,'&gt;')"/>
			<xsl:if test="not($currentTagInStack = $styleTag)">
				<xsl:call-template name="CloseStyleTag">
					<xsl:with-param name="styleTag" select="$styleTag"/>
				</xsl:call-template>
			</xsl:if>
		</xsl:if>
	</xsl:template>

	<!-- Recursively close all remaining style tag -->
	<xsl:template name="CloseAllStyleTag">
		<xsl:variable name="currentTagInStack" select="d:PopCharacterStyle($myObj)" />
		<xsl:if test="$currentTagInStack">
			<xsl:value-of disable-output-escaping="yes" select="concat('&lt;/',$currentTagInStack,'&gt;')"/>
			<xsl:call-template name="CloseAllStyleTag" />
		</xsl:if>
	</xsl:template>

	<!--Template for inbuilt character styles applied on a block of letter (w:r) -->
	<!--Template for different inbuilt character styles applied on a block of letter (w:r) -->
	<xsl:template name="CustomCharStyle">
		<xsl:param name="characterStyle" as="xs:boolean" select="false()"/>
		<xsl:param name="attributes" select="''" /> <!-- To handle hyperlinks -->
		<xsl:param name="txt" as="xs:string" select="''"/>
		<!-- Group of Bidirectionnal / rtl text -->
		<xsl:variable name="isBidirectionnal" select="../w:pPr/w:bidi or w:rPr/w:rtl"/>
		<xsl:variable name="textLanguage">
			<xsl:call-template name="GetRunLanguage">
				<xsl:with-param name="runNode" select="." />
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="paragraphLanguage">
			<xsl:call-template name="GetParagraphLanguage">
				<xsl:with-param name="paragraphNode" select=".." />
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="previousLanguage">
			<xsl:choose>
				<xsl:when test="preceding-sibling::w:r[1]">
					<xsl:call-template name="GetRunLanguage">
						<xsl:with-param name="runNode" select="preceding-sibling::w:r[1]"/>
					</xsl:call-template>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$paragraphLanguage" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>



		<!-- Compute character group status -->
		<!-- Group of bold = strong characters -->
		<xsl:variable name="isStrong" select="w:rPr/w:b or (w:rPr/w:cs and w:rPr/w:bCs) or w:rPr/w:rStyle[@w:val='Strong']"/>
		<!-- Group of italic = em characters -->
		<xsl:variable name="isEmp" select="w:rPr/w:i or (w:rPr/w:cs and w:rPr/w:iCs) or w:rPr/w:rStyle[@w:val='Emphasis']"/>
		<!-- Group is a note reference -->
		<xsl:variable name="isNote" select="
			(w:rPr/w:rStyle[@w:val ='FootnoteReference'])
			or (w:rPr/w:rStyle[@w:val ='EndnoteReference'])
			or (w:footnoteReference)
			or (w:endnoteReference) " />
		<xsl:variable name="isHyperlink" select="parent::w:hyperlink" />
		<!-- Group of subscript = sub characters -->
		<xsl:variable name="isSubscript" select="w:rPr/w:vertAlign[@w:val='subscript']" />
		<!-- Group of superscript = sup characters -->
		<xsl:variable name="isSuperscript" select="w:rPr/w:vertAlign[@w:val='superscript']" />
		<xsl:variable name="lastGroup" select="not(following-sibling::w:r)" />
		<!-- Close removed status -->
		<xsl:if test="not($isHyperlink)">
			<xsl:call-template name="CloseStyleTag">
				<xsl:with-param name="styleTag" select="'a'"/>
			</xsl:call-template>
		</xsl:if>
		<xsl:if test="not($isEmp)">
			<xsl:call-template name="CloseStyleTag">
				<xsl:with-param name="styleTag" select="'em'"/>
			</xsl:call-template>
		</xsl:if>
		<xsl:if test="not($isStrong)">
			<xsl:call-template name="CloseStyleTag">
				<xsl:with-param name="styleTag" select="'strong'"/>
			</xsl:call-template>
		</xsl:if>
		<xsl:if test="not($isSuperscript)">
			<xsl:call-template name="CloseStyleTag">
				<xsl:with-param name="styleTag" select="'sup'"/>
			</xsl:call-template>
		</xsl:if>
		<xsl:if test="not($isSubscript)">
			<xsl:call-template name="CloseStyleTag">
				<xsl:with-param name="styleTag" select="'sub'"/>
			</xsl:call-template>
		</xsl:if>
		<!-- Close BDO element if not bidirectionnal anymore or lang has changed between BDO elements-->
		<xsl:if test="not($isBidirectionnal) or ($isBidirectionnal and $textLanguage != $previousLanguage)">
			<xsl:call-template name="CloseStyleTag">
				<xsl:with-param name="styleTag" select="'bdo'"/>
			</xsl:call-template>
		</xsl:if>
		<!-- testing language span addition for non-bdo element-->
		<xsl:if test="not($isBidirectionnal) and $textLanguage != $previousLanguage">
			<xsl:call-template name="CloseStyleTag">
				<xsl:with-param name="styleTag" select="'span'"/>
			</xsl:call-template>
		</xsl:if>
		<xsl:variable name="innerText" select="translate(normalize-space(w:t/text()), $ignorableCharacters, '')" />

		<xsl:choose>
			<!-- If group is not a notereference and has one of strong|em|sub|sup|a|bdo status -->
			<xsl:when test="$isBidirectionnal or (not($isBidirectionnal) and $textLanguage != $paragraphLanguage) or $isHyperlink or $isStrong or $isEmp or $isSuperscript or $isSubscript and not($isNote)">
				<!-- if not already in the style stack, Open new style tag and add it to the stack -->
				<xsl:if test="not($isBidirectionnal) and $textLanguage != $paragraphLanguage and string-length($innerText) &gt; 0">
					<xsl:call-template name="OpenStyleTagIfNotOpened">
						<xsl:with-param name="styleTag" select="'span'"/>
						<xsl:with-param name="attributes">xml:lang="<xsl:value-of select="$textLanguage"/>"</xsl:with-param>
					</xsl:call-template>
				</xsl:if>
				<xsl:if test="$isBidirectionnal">
					<xsl:call-template name="OpenStyleTagIfNotOpened">
						<xsl:with-param name="styleTag" select="'bdo'"/>
						<xsl:with-param name="attributes">dir="rtl" xml:lang="<xsl:value-of select="$textLanguage"/>"</xsl:with-param>
					</xsl:call-template>
				</xsl:if>
				<xsl:if test="$isSubscript">
					<xsl:call-template name="OpenStyleTagIfNotOpened">
						<xsl:with-param name="styleTag" select="'sub'"/>
					</xsl:call-template>
				</xsl:if>
				<xsl:if test="$isSuperscript">
					<xsl:call-template name="OpenStyleTagIfNotOpened">
						<xsl:with-param name="styleTag" select="'sup'"/>
					</xsl:call-template>
				</xsl:if>
				<xsl:if test="$isStrong">
					<xsl:call-template name="OpenStyleTagIfNotOpened">
						<xsl:with-param name="styleTag" select="'strong'"/>
					</xsl:call-template>
				</xsl:if>
				<xsl:if test="$isEmp">
					<xsl:call-template name="OpenStyleTagIfNotOpened">
						<xsl:with-param name="styleTag" select="'em'"/>
					</xsl:call-template>
				</xsl:if>
				<xsl:if test="$isHyperlink">
					<xsl:call-template name="OpenStyleTagIfNotOpened">
						<xsl:with-param name="styleTag" select="'a'"/>
						<xsl:with-param name="attributes" select="$attributes" />
					</xsl:call-template>
				</xsl:if>
				<xsl:call-template name="RunTextCustomCharacterStylesHandler">
					<xsl:with-param name="characterStyle" select="$characterStyle"/>
					<xsl:with-param name="txt" select="$txt"/>
				</xsl:call-template>
				<!-- Close style tag if no run with text is found afterward in the paragraph tag -->
				<xsl:if test="not(following-sibling::w:r/w:t)">
					<xsl:call-template name="CloseStyleTag">
						<xsl:with-param name="styleTag" select="'a'"/>
					</xsl:call-template>
					<xsl:call-template name="CloseStyleTag">
						<xsl:with-param name="styleTag" select="'em'"/>
					</xsl:call-template>
					<xsl:call-template name="CloseStyleTag">
						<xsl:with-param name="styleTag" select="'strong'"/>
					</xsl:call-template>
					<xsl:call-template name="CloseStyleTag">
						<xsl:with-param name="styleTag" select="'sup'"/>
					</xsl:call-template>
					<xsl:call-template name="CloseStyleTag">
						<xsl:with-param name="styleTag" select="'sub'"/>
					</xsl:call-template>
					<xsl:call-template name="CloseStyleTag">
						<xsl:with-param name="styleTag" select="'bdo'"/>
					</xsl:call-template>
				</xsl:if>
			</xsl:when>
			<!--Checking for WordDAISY custom character style-->
			<xsl:when test="(w:rPr/w:rStyle[contains(@w:val,'Word')])">
				<w>
					<xsl:call-template name="RunTextCustomCharacterStylesHandler">
						<xsl:with-param name="characterStyle" select="$characterStyle"/>
						<xsl:with-param name="txt" select="$txt"/>
					</xsl:call-template>
				</w>
			</xsl:when>
			<xsl:when test="w:instrText">
				<xsl:if test="not(preceding-sibling::w:r[1]/w:fldChar[@w:fldCharType='begin'])">
					<xsl:value-of select="w:instrText"/>
				</xsl:if>
			</xsl:when>
			<!--Checking conditions for Character styles-->
			<xsl:when test="(w:t) and (not(w:rPr/w:rStyle[@w:val='DefinitionTermDAISY']))">
				<!--Hyphen-->
				<xsl:if test="w:noBreakHyphen">
					<xsl:text>-</xsl:text>
				</xsl:if>
				<xsl:call-template name="RunTextCustomCharacterStylesHandler">
					<xsl:with-param name="characterStyle" select="$characterStyle"/>
					<xsl:with-param name="txt" select="$txt"/>
				</xsl:call-template>
			</xsl:when>
			<!--Fidility Loss Report-->
			<xsl:otherwise>
				<xsl:for-each select="./node()">
					<xsl:call-template name="RunTextCustomCharacterStylesHandler">
						<xsl:with-param name="characterStyle" select="$characterStyle"/>
						<xsl:with-param name="txt" select="$txt"/>
					</xsl:call-template>
					<xsl:choose>
						<xsl:when test="name()='w:commentReference'">
							<!--Capturing fidility loss-->
							<xsl:message terminate="no">translation.oox2Daisy.commentReference</xsl:message>
							<!--Capturing fidility loss-->
						</xsl:when>
						<xsl:when test="name()='w:object'">
							<xsl:message terminate="no">translation.oox2Daisy.object</xsl:message>
						</xsl:when>
						<xsl:otherwise>
							<!--Capturing fidility loss-->
							<xsl:if test="not((name()='w:rPr')or(name()='w:fldSimple') or (name()='w:lastRenderedPageBreak') or (name()='w:br') or (name()='w:tab')or(name()='w:fldChar') or (name()='w:t'))">
								<xsl:message terminate="no">
									<xsl:value-of select="concat('translation.oox2Daisy.UncoveredElement|',name())"/>
								</xsl:message>
							</xsl:if>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:for-each>
			</xsl:otherwise>
		</xsl:choose>
		<!-- If the current group is the last group of the parent element, close everything after handling the special cases-->
		<xsl:if test="$lastGroup">
			<xsl:call-template name="CloseAllStyleTag" />
		</xsl:if>
	</xsl:template>

	<xsl:template name="List">
		<xsl:param name="pagination" as="xs:string" select="''"/>
		<xsl:param name="listcharStyle" as="xs:boolean"/>
		<!--variable checkilvl holds level(w:ilvl) value of the List-->
		<!--NOTE:Use GetCheckLvlInt function that return 0 if node is not exists-->
		<xsl:variable name="checkilvl" as="xs:integer" select="d:GetCheckLvlInt($myObj,w:pPr/w:numPr/w:ilvl/@w:val)"/>
		<!--Variable checknumId holds the w:numId value which specifies the type of numbering in the list-->
		<xsl:variable name="checknumId">
			<xsl:choose>
				<xsl:when test="w:pPr/w:numPr/w:numId/@w:val">
					<xsl:value-of select="w:pPr/w:numPr/w:numId/@w:val" />
				</xsl:when>
				<xsl:otherwise>1</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<xsl:variable name="CheckNumId" as="xs:string" select="d:CheckNumID($myObj,$checknumId)"/>
		<xsl:if test="$CheckNumId ='True'">
			<xsl:sequence select="d:sink(d:StartNewListCounter($myObj,$checknumId))"/> <!-- empty -->
		</xsl:if>

		<xsl:if test="string-length(preceding-sibling::node()[1]/w:pPr/w:numPr/w:ilvl/@w:val)=0 or preceding-sibling::w:p[1]/w:pPr/w:rPr/w:vanish" >
			<xsl:if test="($checkilvl &gt; 0)">
				<xsl:call-template name="recursive">
					<xsl:with-param name="rec" select="$checkilvl"/>
				</xsl:call-template>
				<xsl:sequence select="d:Increment($myObj,$checkilvl)"/> <!-- empty -->
			</xsl:if>
		</xsl:if>

		<xsl:if test="(
				string-length(preceding-sibling::node()[1]/w:pPr/w:numPr/w:ilvl/@w:val) = 0
				or (
					preceding-sibling::node()[1]/w:pPr/w:numPr/w:ilvl/@w:val &lt; $checkilvl
					and not(preceding-sibling::node()[1]/w:pPr/w:numPr/w:ilvl/@w:val=$checkilvl)
				) or preceding-sibling::node()[1]/w:pPr/w:pStyle[substring(@w:val,1,7)='Heading']
				or preceding-sibling::node()[1]/w:pPr/w:rPr/w:vanish
			)">
			<xsl:variable name="val" as="xs:string" select="$numberingXml//w:numbering/w:num[@w:numId=$checknumId]/w:abstractNumId/@w:val"/>
			<!--Checking numbering.xml for the type of List-->
			<xsl:variable    name="type" as="xs:string" select="$numberingXml//w:numbering/w:abstractNum[@w:abstractNumId=$val]/w:lvl[@w:ilvl=$checkilvl]/w:numFmt/@w:val"/>
			<!--Checking for Ordered List type-->
			<xsl:choose>
				<xsl:when test="$type='decimal'">
					<xsl:value-of disable-output-escaping="yes" select="concat('&lt;list type=&quot;ol&quot; start=&quot;', d:GetListCounter($myObj,$checkilvl, $checknumId),'&quot;&gt;')"/>
				</xsl:when>
				<xsl:when test="($type='lowerLetter') or ($type='lowerRoman') or ($type='upperRoman') or ($type='upperLetter')or ($type='decimalZero')">
					<xsl:value-of disable-output-escaping="yes" select="'&lt;list type=&quot;pl&quot;&gt;'"/>
				</xsl:when>
				<!--Checking for Unordered list and Preformatted List type-->
				<xsl:when test="$type='bullet'">
					<xsl:choose>
						<xsl:when test ="$numberingXml//w:numbering/w:abstractNum[@w:abstractNumId=$val]/w:lvl[@w:ilvl=$checkilvl]/w:lvlPicBulletId">
							<xsl:value-of disable-output-escaping="yes" select="'&lt;list type=&quot;pl&quot;&gt;'"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of disable-output-escaping="yes" select="'&lt;list type=&quot;ul&quot;&gt;'"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:when>
				<xsl:when test="$type='none'">
					<xsl:value-of disable-output-escaping="yes" select="'&lt;list type=&quot;pl&quot;&gt;'"/>
				</xsl:when>
				<!--If in word/numbering.xml,numbering formatis having any style(means no w:numFmt element), we are taking default style as ordered list-->
				<xsl:when test="$type=''">
					<xsl:value-of disable-output-escaping="yes" select="'&lt;list type=&quot;ol&quot;&gt;'"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of disable-output-escaping="yes" select="'&lt;list type=&quot;pl&quot;&gt;'"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
		<xsl:if test="d:ListHeadingFlag($myObj)&gt;0">
			<xsl:value-of disable-output-escaping="yes" select="d:PeekListHeading($myObj)"/>
			<xsl:sequence select="d:sink(d:ReSetListHeadingFlag($myObj))"/> <!-- empty -->
		</xsl:if>

		<!--Checking the current level with the next level-->
		<xsl:variable name="PeekLevel" as="xs:integer" select="d:ListPeekLevel($myObj)"/>
		<xsl:if test="$PeekLevel - $checkilvl &gt; 1">
			<xsl:value-of disable-output-escaping="yes" select="'&lt;/li&gt;'"/>
			<xsl:call-template name="ComplexListClose">
				<xsl:with-param name="close" select="$checkilvl"/>
			</xsl:call-template>
		</xsl:if>

		<!--Closing the List-->
		<xsl:call-template name="closelist">
			<xsl:with-param name="close" select="$checkilvl"/>
		</xsl:call-template>

		<xsl:variable name="val" as="xs:string" select="$numberingXml//w:numbering/w:num[@w:numId=$checknumId]/w:abstractNumId/@w:val"/>
		<!--Checking numbering.xml for the type of List-->
		<xsl:variable    name="numFormat" as="xs:string" select="$numberingXml//w:numbering/w:abstractNum[@w:abstractNumId=$val]/w:lvl[@w:ilvl=$checkilvl]/w:numFmt/@w:val"/>
		<xsl:variable    name="lvlText" as="xs:string" select="$numberingXml//w:numbering/w:abstractNum[@w:abstractNumId=$val]/w:lvl[@w:ilvl=$checkilvl]/w:lvlText/@w:val"/>

		<xsl:call-template name="recStart">
			<xsl:with-param name="abstLevel" select="$val"/>
			<xsl:with-param name="level" select="$checkilvl"/>
		</xsl:call-template>

		<!--Opening the List-->
		<xsl:call-template name="addlist">
			<xsl:with-param name="openId" select="$checknumId"/>
			<xsl:with-param name="openlvl" select="$checkilvl"/>
			<xsl:with-param name="pagination" select="$pagination"/>
			<xsl:with-param name="numFmt" select="$numFormat"/>
			<xsl:with-param name="lText" select="$lvlText"/>
			<xsl:with-param name="lstcharStyle" select="$listcharStyle"/>
		</xsl:call-template>

		<!--Closing the current List item(li) element when there is no nested list-->
		<xsl:variable name="LPeekLevel" as="xs:integer" select="d:ListPeekLevel($myObj)"/>
		<xsl:if test="(
				$LPeekLevel = $checkilvl
				and following-sibling::node()[1][w:pPr/w:numPr/w:ilvl/@w:val = $checkilvl]
				and not(following-sibling::node()[1]/w:pPr/w:pStyle[substring(@w:val,1,7)='Heading'])
				and not(following-sibling::node()[1]/w:pPr/w:rPr/w:vanish)
			)">
			<xsl:value-of disable-output-escaping="yes" select="'&lt;/li&gt;'"/>
		</xsl:if>

		<!--Insert page break in list-->
		<xsl:call-template name="PageInList">
			<xsl:with-param name="pagination" select="$pagination"/>
		</xsl:call-template>

		<!--Closing all the nested Lists-->
		<xsl:if test="(
				count(following-sibling::node()[1][w:pPr/w:numPr/w:ilvl/@w:val])=0
				or following-sibling::w:p[1]/w:pPr/w:pStyle[substring(@w:val,1,7)='Heading']
				or (following-sibling::w:p[1]/w:pPr/w:rPr/w:vanish)
			)">
			<xsl:call-template name="CloseLastlist">
				<xsl:with-param name="close" select="0"/>
				<xsl:with-param name="pagination" select="$pagination"/>
			</xsl:call-template>
		</xsl:if>

	</xsl:template>

	<!--Template for implementing paragraph styles-->
	<xsl:template name="StyleContainer">
		<xsl:param name="acceptRevisions" as="xs:boolean" select="true()"/>
		<xsl:param name="version" as="xs:string"/>
		<xsl:param name="pagination" as="xs:string"/>
		<xsl:param name="styleHeading" as="xs:string?"/>
		<xsl:param name="sOperators" as="xs:string" select="''"/>
		<xsl:param name="sMinuses" as="xs:string" select="''"/>
		<xsl:param name="sNumbers" as="xs:string" select="''"/>
		<xsl:param name="sZeros" as="xs:string" select="''"/>
		<xsl:param name="mastersubstyle" as="xs:boolean"/>
		<xsl:param name="txt" as="xs:string" select="''"/>
		<xsl:param name="imgOptionStyle" as="xs:string" select="''"/>
		<xsl:param name="dpiStyle" as="xs:float?"/>
		<xsl:param name="characterStyle" as="xs:boolean"/>
		<xsl:choose>
			<!-- TOC starting paragraphe -->
			<xsl:when test="(
					w:pPr/w:pStyle[substring(@w:val,1,3)='TOC']
					and not(preceding::w:pPr/w:pStyle[substring(@w:val,1,3)='TOC'])
				)">
				<!--Save Level before closing all levels-->
				<xsl:variable name="PeekLevel" as="xs:integer" select="d:PeekLevel($myObj)"/>
				<!--Close all levels before Table Of Contents-->
				<xsl:call-template name="CloseLevel">
					<xsl:with-param name="CurrentLevel" select="1"/>
					<xsl:with-param name="verfoot" select="$version"/>
					<xsl:with-param name="characterStyle" select="$characterStyle"/>
					<xsl:with-param name="sOperators" select="$sOperators"/>
					<xsl:with-param name="sMinuses" select="$sMinuses"/>
					<xsl:with-param name="sNumbers" select="$sNumbers"/>
					<xsl:with-param name="sZeros" select="$sZeros"/>
				</xsl:call-template>
				<!--Calling Template to add Table Of Contents-->
				<xsl:call-template name="TableOfContents">
					<xsl:with-param name="pagination" select="$pagination"/>
				</xsl:call-template>
				<!--Open $PeekLevel levels after Table Of Contents-->
				<xsl:call-template name="AddLevel">
					<xsl:with-param name="levelValue" select="$PeekLevel"/>
					<xsl:with-param name="check" select="true()"/>
					<xsl:with-param name="verhead" select="$version"/>
					<xsl:with-param name="pagination" select="$pagination"/>
					<xsl:with-param name="sOperators" select="$sOperators"/>
					<xsl:with-param name="sMinuses" select="$sMinuses"/>
					<xsl:with-param name="sNumbers" select="$sNumbers"/>
					<xsl:with-param name="sZeros" select="$sZeros"/>
					<xsl:with-param name="mastersubhead" select="$mastersubstyle"/>
					<xsl:with-param name="headingFormatAndTextAndID" select="'0'"/>
					<xsl:with-param name="lvlcharStyle" select="$characterStyle"/>
				</xsl:call-template>
			</xsl:when>

			<xsl:when test="(
					w:pPr/w:pStyle[
						substring(@w:val,1,7)='Heading' 
						or (
							exists($styleHeading) 
							and @w:val/d:CompareHeading(.,$styleHeading)=1
						)
					] and not(parent::w:tc)
				)">
				<!--calling Close level template for closing all the higher levels-->
				<xsl:if test="(
						(
							(w:r/w:lastRenderedPageBreak)
							or (w:r/w:br/@w:type='page')
						) and $FootnotesPosition='page'
					)">
					<xsl:call-template name="InsertFootnotes">
						<xsl:with-param name="level" as="xs:integer" select="d:PeekLevel($myObj)"/>
						<xsl:with-param name="verfoot" select="$version"/>
						<xsl:with-param name="sOperators" select="$sOperators"/>
						<xsl:with-param name="sMinuses" select="$sMinuses"/>
						<xsl:with-param name="sNumbers" select="$sNumbers"/>
						<xsl:with-param name="sZeros" select="$sZeros"/>
					</xsl:call-template>
				</xsl:if>

				<xsl:choose>
					<xsl:when test="((w:pPr/w:numPr/w:ilvl) and (w:pPr/w:numPr/w:numId))">
						<xsl:variable name="text_heading" as="xs:string">
							<!--variable checkilvl holds level(w:ilvl) value of the List-->
							<!--NOTE:Use GetCheckLvlInt function that return 0 if node is not exists-->
							<xsl:variable name="checkilvl" as="xs:integer" select="d:GetCheckLvlInt($myObj,w:pPr/w:numPr/w:ilvl/@w:val)"/>
							<!--Variable checknumId holds the w:numId value which specifies the type of numbering in the list-->
							<xsl:variable name="checknumId" as="xs:string" select="w:pPr/w:numPr/w:numId/@w:val"/>
							<xsl:call-template name="HeadingsPart">
								<xsl:with-param name="checkilvl" select="$checkilvl"/>
								<xsl:with-param name="checknumId" select="$checknumId"/>
								<xsl:with-param name="doc" select="'Document'"/>
							</xsl:call-template>
							<xsl:sequence select="d:RetrieveHeadingPart($myObj)"/>
						</xsl:variable>

						<xsl:variable name="absValue" as="xs:string">
							<!--NOTE:Use GetCheckLvlInt function that return 0 if node is not exists-->
							<xsl:sequence select="d:sink(d:GetCheckLvlInt($myObj,w:pPr/w:numPr/w:ilvl/@w:val))"/> <!-- empty -->
							<!--Variable checknumId holds the w:numId value which specifies the type of numbering in the list-->
							<xsl:variable name="checknumId" as="xs:string" select="w:pPr/w:numPr/w:numId/@w:val"/>
							<xsl:sequence select="$numberingXml//w:numbering/w:num[@w:numId=$checknumId]/w:abstractNumId/@w:val"/>
						</xsl:variable>

						<xsl:call-template name="CloseLevel">
							<xsl:with-param name="CurrentLevel" select="number(substring(w:pPr/w:pStyle/@w:val,string-length(w:pPr/w:pStyle/@w:val)))"/>
							<xsl:with-param name="verfoot" select="$version"/>
							<xsl:with-param name="characterStyle" select="$characterStyle"/>
							<xsl:with-param name="sOperators" select="$sOperators"/>
							<xsl:with-param name="sMinuses" select="$sMinuses"/>
							<xsl:with-param name="sNumbers" select="$sNumbers"/>
							<xsl:with-param name="sZeros" select="$sZeros"/>
						</xsl:call-template>

						<!--calling AddLevel template for adding the levels-->
						<xsl:call-template name="AddLevel">
							<!--Passing parameter levelValue which holds the Heading type value-->
							<xsl:with-param name="levelValue" select="w:pPr/w:numPr/w:ilvl/@w:val"/>
							<xsl:with-param name="check" select="true()"/>
							<xsl:with-param name="verhead" select="$version"/>
							<xsl:with-param name="pagination" select="$pagination"/>
							<xsl:with-param name="sOperators" select="$sOperators"/>
							<xsl:with-param name="sMinuses" select="$sMinuses"/>
							<xsl:with-param name="sNumbers" select="$sNumbers"/>
							<xsl:with-param name="sZeros" select="$sZeros"/>
							<xsl:with-param name="mastersubhead" select="$mastersubstyle"/>
							<xsl:with-param name="abValue" select="$absValue"/>
							<xsl:with-param name="headingFormatAndTextAndID" select="concat($text_heading,'!',w:pPr/w:numPr/w:numId/@w:val)"/>
							<xsl:with-param name="lvlcharStyle" select="$characterStyle"/>
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						<xsl:variable name="text_heading" as="xs:string*">
							<xsl:variable name="nameHeading" as="xs:string" select="w:pPr/w:pStyle/@w:val"/>
							<xsl:for-each select="$stylesXml//w:styles/w:style[@w:styleId=$nameHeading]">
								<xsl:choose>
									<xsl:when test="(./w:pPr/w:outlineLvl) and (./w:pPr/w:numPr/w:numId)">
										<!--NOTE:Use GetCheckLvlInt function that return 0 if node is not exists-->
										<xsl:variable name="checkilvl" as="xs:integer" select="d:GetCheckLvlInt($myObj,./w:pPr/w:outlineLvl/@w:val)"/>
										<xsl:variable name="checknumId" as="xs:string" select="./w:pPr/w:numPr/w:numId/@w:val"/>
										<xsl:call-template name="HeadingsPart">
											<xsl:with-param name="checkilvl" select="$checkilvl"/>
											<xsl:with-param name="checknumId" select="$checknumId"/>
											<xsl:with-param name="doc" select="'Style'"/>
										</xsl:call-template>
										<xsl:sequence select="d:RetrieveHeadingPart($myObj)"/>
									</xsl:when>
									<xsl:when test="string-length(./w:pPr/w:numPr/w:numId)=0">
										<!--NOTE:Use GetCheckLvlInt function that return 0 if node is not exists-->
										<xsl:variable name="checkilvl" as="xs:integer" select="d:GetCheckLvlInt($myObj,./w:pPr/w:outlineLvl/@w:val)"/>
										<xsl:sequence select="d:sink(d:AddCurrHeadId($myObj,''))"/> <!-- empty -->
										<xsl:sequence select="d:sink(d:AddCurrHeadLevel($myObj,$checkilvl,'Style',''))"/> <!-- empty -->
										<xsl:sequence select="concat('','|',$checkilvl,'!','')"/>
									</xsl:when>
								</xsl:choose>
							</xsl:for-each>
						</xsl:variable>
						<xsl:variable name="text_heading" as="xs:string" select="string-join($text_heading,'')"/>

						<xsl:variable name="absValue" as="xs:string*">
							<xsl:variable name="nameHeading" as="xs:string" select="w:pPr/w:pStyle/@w:val"/>
							<xsl:for-each select="$stylesXml//w:styles/w:style[@w:styleId=$nameHeading]">
								<xsl:if test="(./w:pPr/w:outlineLvl) and (./w:pPr/w:numPr/w:numId)">
									<!--NOTE:Use GetCheckLvlInt function that return 0 if node is not exists-->
									<xsl:sequence select="d:sink(d:GetCheckLvlInt($myObj,./w:pPr/w:outlineLvl/@w:val))"/> <!-- empty -->
									<xsl:variable name="checknumId" as="xs:string" select="./w:pPr/w:numPr/w:numId/@w:val"/>
									<xsl:sequence select="$numberingXml//w:numbering/w:num[@w:numId=$checknumId]/w:abstractNumId/@w:val"/>
								</xsl:if>
							</xsl:for-each>
						</xsl:variable>
						<xsl:variable name="absValue" as="xs:string" select="string-join($absValue,'')"/>
						<xsl:variable name="ilvl" as="xs:string*">
							<xsl:variable name="nameHeading" as="xs:string" select="w:pPr/w:pStyle/@w:val"/>
							<xsl:for-each select="$stylesXml//w:styles/w:style[@w:styleId=$nameHeading]">
								<xsl:choose>
									<xsl:when test="(./w:pPr/w:outlineLvl) and (./w:pPr/w:numPr/w:numId)">
										<xsl:sequence    select="./w:pPr/w:outlineLvl/@w:val"/>
									</xsl:when>
									<xsl:when test="string-length(./w:pPr/w:numPr/w:numId)=0">
										<xsl:sequence    select="./w:pPr/w:outlineLvl/@w:val"/>
									</xsl:when>
								</xsl:choose>
							</xsl:for-each>
						</xsl:variable>
						<xsl:variable name="ilvl" as="xs:integer?">
							<xsl:if test="not(string-join($ilvl,'')='')">
								<xsl:sequence select="xs:integer(string-join($ilvl,''))"/>
							</xsl:if>
						</xsl:variable>

						<xsl:call-template name="CloseLevel">
							<xsl:with-param name="CurrentLevel" select="number(substring(w:pPr/w:pStyle/@w:val,string-length(w:pPr/w:pStyle/@w:val)))"/>
							<xsl:with-param name="verfoot" select="$version"/>
							<xsl:with-param name="characterStyle" select="$characterStyle"/>
							<xsl:with-param name="sOperators" select="$sOperators"/>
							<xsl:with-param name="sMinuses" select="$sMinuses"/>
							<xsl:with-param name="sNumbers" select="$sNumbers"/>
							<xsl:with-param name="sZeros" select="$sZeros"/>
						</xsl:call-template>

						<xsl:call-template name="AddLevel">
							<xsl:with-param name="levelValue" select="$ilvl"/>
							<xsl:with-param name="check" select="true()"/>
							<xsl:with-param name="verhead" select="$version"/>
							<xsl:with-param name="pagination" select="$pagination"/>
							<xsl:with-param name="sOperators" select="$sOperators"/>
							<xsl:with-param name="sMinuses" select="$sMinuses"/>
							<xsl:with-param name="sNumbers" select="$sNumbers"/>
							<xsl:with-param name="sZeros" select="$sZeros"/>
							<xsl:with-param name="mastersubhead" select="$mastersubstyle"/>
							<xsl:with-param name="abValue" select="$absValue"/>
							<xsl:with-param name="headingFormatAndTextAndID" select="$text_heading"/>
							<xsl:with-param name="lvlcharStyle" select="$characterStyle"/>
						</xsl:call-template>

					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>

			<!--Checking for Sidebarheader* custom style-->
			<xsl:when test="(starts-with(w:pPr/w:pStyle/@w:val,'Sidebarheader') and ends-with(w:pPr/w:pStyle/@w:val, 'DAISY')) and not(parent::w:tc)">
				<hd>
					<xsl:call-template name="ParaHandler">
						<xsl:with-param name="flag" select="'0'"/>
						<xsl:with-param name="version" select="$version"/>
						<xsl:with-param name="pagination" select="$pagination"/>
						<xsl:with-param name="imgOptionPara" select="$imgOptionStyle"/>
						<xsl:with-param name="dpiPara" select="$dpiStyle"/>
						<xsl:with-param name="txt" select="$txt"/>
						<xsl:with-param name="charparahandlerStyle" select="$characterStyle"/>
					</xsl:call-template>
				</hd>
			</xsl:when>
			<!--Checking for Bridgehead custom style-->
			<xsl:when test="(w:pPr/w:pStyle/@w:val='BridgeheadDAISY') and not(parent::w:tc)">
				<bridgehead>
					<xsl:call-template name="ParaHandler">
						<xsl:with-param name="flag" select="'0'"/>
						<xsl:with-param name="version" select="$version"/>
						<xsl:with-param name="pagination" select="$pagination"/>
						<xsl:with-param name="imgOptionPara" select="$imgOptionStyle"/>
						<xsl:with-param name="dpiPara" select="$dpiStyle"/>
						<xsl:with-param name="txt" select="$txt"/>
						<xsl:with-param name="charparahandlerStyle" select="$characterStyle"/>
					</xsl:call-template>
				</bridgehead>
			</xsl:when>
			<xsl:otherwise>
				<xsl:choose>
					<!--Cheaking for list heading-->
					<xsl:when test="(w:pPr/w:pStyle/@w:val='List-HeadingDAISY')">
						<xsl:variable name="tempListHeading" as="text()*">
							<xsl:text disable-output-escaping="yes">&lt;hd&gt;</xsl:text>
							<xsl:call-template name="ParaHandler">
								<xsl:with-param name="flag" select="'0'"/>
								<xsl:with-param name="version" select="$version"/>
								<xsl:with-param name="pagination" select="$pagination"/>
								<xsl:with-param name="imgOptionPara" select="$imgOptionStyle"/>
								<xsl:with-param name="dpiPara" select="$dpiStyle"/>
								<xsl:with-param name="charparahandlerStyle" select="$characterStyle"/>
								<xsl:with-param name="txt" select="$txt"/>
							</xsl:call-template>
							<xsl:text disable-output-escaping="yes">&lt;/hd&gt;</xsl:text>
						</xsl:variable>
						<xsl:variable name="tempListHeading" as="xs:string" select="string-join(tempListHeading,'')"/>
						<xsl:sequence select="d:sink(d:PushListHeading($myObj,$tempListHeading))"/> <!-- empty -->
						<xsl:sequence select="d:sink(d:SetListHeadingFlag($myObj))"/> <!-- empty -->
					</xsl:when>
					<!--calling list template for implementing lists-->
					<xsl:when test="(w:pPr/w:rPr/w:vanish) and (w:pPr/w:numPr/w:ilvl) and (w:pPr/w:numPr/w:numId)">
						<xsl:variable name="checkCounter" as="xs:string" select="d:IsList($myObj,w:pPr/w:numPr/w:numId/@w:val)"/>
						<xsl:choose>
							<xsl:when test="$checkCounter='ListTrue'">
								<xsl:sequence select="d:IncrementListCounters($myObj,w:pPr/w:numPr/w:ilvl/@w:val,w:pPr/w:numPr/w:numId/@w:val)"/> <!-- empty -->
							</xsl:when>
							<xsl:otherwise>
								<!--variable checkilvl holds level(w:ilvl) value of the List-->
								<!--NOTE:Use GetCheckLvlInt function that return 0 if node is not exists-->
								<xsl:variable name="checkilvl" as="xs:integer" select="d:GetCheckLvlInt($myObj,w:pPr/w:numPr/w:ilvl/@w:val)"/>
								<!--Variable checknumId holds the w:numId value which specifies the type of numbering in the list-->
								<xsl:variable name="checknumId" as="xs:string" select="w:pPr/w:numPr/w:numId/@w:val"/>
								<xsl:variable name="val" as="xs:string" select="$numberingXml//w:numbering/w:num[@w:numId=$checknumId]/w:abstractNumId/@w:val"/>
								<xsl:variable name="lStartOverride" as="xs:string" select="$numberingXml//w:numbering/w:num[@w:numId=$checknumId]/w:lvlOverride[@w:ilvl=$checkilvl]/w:startOverride/@w:val"/>
								<xsl:variable name="lStart" as="xs:string" select="$numberingXml//w:numbering/w:abstractNum[@w:abstractNumId=$val]/w:lvl[@w:ilvl=$checkilvl]/w:start/@w:val"/>
								<xsl:sequence select="d:sink(d:AddCurrHeadId($myObj,$checknumId))"/> <!-- empty -->
								<xsl:variable name="addCurrLvl" as="xs:string" select="d:AddCurrHeadLevel($myObj,$checkilvl,'Vanish',$val)"/>

								<xsl:choose>
									<xsl:when test="$checkCounter='HeadTrue'">
										<xsl:choose>
											<xsl:when test="string-length(substring-before($addCurrLvl,'|'))=0">
												<xsl:choose>
													<xsl:when test="not($lStartOverride='')">
														<xsl:sequence select="d:sink(d:StartHeadingValueCtr($myObj,$checknumId,$val))"/> <!-- empty -->
														<xsl:sequence select="d:sink(d:StartHeadingString($myObj,$checkilvl,$lStartOverride,$checknumId,$val,'Vanish','Yes'))"/> <!-- empty -->
														<xsl:sequence select="d:sink(d:CopyToCurrCounter($myObj,$checknumId))"/> <!-- empty -->
														<xsl:sequence select="d:IncrementHeadingCounters($myObj,w:pPr/w:numPr/w:ilvl/@w:val,$checknumId,$val)"/> <!-- empty -->
														<xsl:sequence select="d:sink(d:CopyToBaseCounter($myObj,$checknumId))"/> <!-- empty -->
													</xsl:when>
													<xsl:otherwise>
														<xsl:sequence select="d:sink(d:StartHeadingValueCtr($myObj,$checknumId,$val))"/> <!-- empty -->
														<xsl:sequence select="d:sink(d:StartHeadingString($myObj,$checkilvl,$lStart,$checknumId,$val,'Vanish','No'))"/> <!-- empty -->
														<xsl:sequence select="d:sink(d:CopyToCurrCounter($myObj,$checknumId))"/> <!-- empty -->
														<xsl:sequence select="d:IncrementHeadingCounters($myObj,w:pPr/w:numPr/w:ilvl/@w:val,$checknumId,$val)"/> <!-- empty -->
														<xsl:sequence select="d:sink(d:CopyToBaseCounter($myObj,$checknumId))"/> <!-- empty -->
													</xsl:otherwise>
												</xsl:choose>
											</xsl:when>
										</xsl:choose>
									</xsl:when>
									<xsl:otherwise>
										<xsl:choose>
											<xsl:when test="string-length(substring-before($addCurrLvl,'|'))=0">
												<xsl:variable name="CheckNumId" as="xs:string" select="d:CheckHeadingNumID($myObj,$checknumId)"/>
												<xsl:if test="$CheckNumId ='True'">
													<xsl:sequence select="d:sink(d:StartHeadingValueCtr($myObj,$checknumId,$val))"/> <!-- empty -->
													<xsl:sequence select="d:sink(d:StartNewHeadingCounter($myObj,$checknumId,$val))"/> <!-- empty -->
												</xsl:if>
												<xsl:choose>
													<xsl:when test="not($lStartOverride='')">
														<xsl:sequence select="d:sink(d:CopyToCurrCounter($myObj,$checknumId))"/> <!-- empty -->
														<xsl:sequence select="d:sink(d:StartHeadingValueCtr($myObj,$checknumId,$val))"/> <!-- empty -->
														<xsl:sequence select="d:sink(d:StartHeadingString($myObj,$checkilvl,$lStartOverride,$checkCounter,$val,'Vanish','Yes'))"/> <!-- empty -->
														<xsl:sequence select="d:IncrementHeadingCounters($myObj,w:pPr/w:numPr/w:ilvl/@w:val,$checkCounter,$val)"/> <!-- empty -->
														<xsl:sequence select="d:sink(d:CopyToBaseCounter($myObj,$checkCounter))"/> <!-- empty -->
													</xsl:when>
													<xsl:otherwise>
														<xsl:sequence select="d:sink(d:CopyToCurrCounter($myObj,$checknumId))"/> <!-- empty -->
														<xsl:sequence select="d:sink(d:StartHeadingValueCtr($myObj,$checknumId,$val))"/> <!-- empty -->
														<xsl:sequence select="d:sink(d:StartHeadingString($myObj,$checkilvl,$lStart,$checknumId,$val,'Vanish','No'))"/> <!-- empty -->
														<xsl:sequence select="d:IncrementHeadingCounters($myObj,w:pPr/w:numPr/w:ilvl/@w:val,$checkCounter,$val)"/> <!-- empty -->
														<xsl:sequence select="d:sink(d:CopyToBaseCounter($myObj,$checkCounter))"/> <!-- empty -->
													</xsl:otherwise>
												</xsl:choose>
											</xsl:when>
										</xsl:choose>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:when>

					<xsl:when test="(
							(w:pPr/w:numPr/w:ilvl) 
							and (w:pPr/w:numPr/w:numId) 
							and not(w:pPr/w:rPr/w:vanish)
						) and not(
							w:pPr/w:pStyle[
								substring(@w:val,1,7)='Heading' 
								or (
									exists($styleHeading) 
									and @w:val/d:CompareHeading(.,$styleHeading)=1
								)
							]
						)">
						<xsl:call-template name="List">
							<xsl:with-param name="pagination" select="$pagination"/>
							<xsl:with-param name="listcharStyle" select="$characterStyle"/>
						</xsl:call-template>
					</xsl:when>

					<xsl:otherwise>
						<!--calling template named ParagraphStyle-->
						<xsl:variable name="checkImageposition" as="xs:integer" select="d:GetCaptionsProdnotes($myObj)"/>
						<xsl:if test="not( 
								(preceding-sibling::node()[$checkImageposition]/w:r/w:drawing)
								or (preceding-sibling::node()[$checkImageposition]/w:r/w:pict)
								or (preceding-sibling::node()[$checkImageposition]/w:r/w:object)
								or ( 
									(
										(w:pPr/w:pStyle/@w:val='Table-CaptionDAISY')
										or (w:pPr/w:pStyle/@w:val='Caption')
										or (child::w:fldSimple)
									) and (
										(preceding-sibling::node()[1][self::w:tbl])
										or (following-sibling::node()[1][self::w:tbl])
									)
								)
							)">
							<xsl:call-template name="ParagraphStyle">
								<xsl:with-param name="acceptRevisions" select="$acceptRevisions"/>
								<xsl:with-param name="version" select="$version"/>
								<xsl:with-param name="pagination" select="$pagination"/>
								<xsl:with-param name="masterparastyle" select="$mastersubstyle"/>
								<xsl:with-param name="imgOptionPara" select="$imgOptionStyle"/>
								<xsl:with-param name="dpiPara" select="$dpiStyle"/>
								<xsl:with-param name="txt" select="$txt"/>
								<xsl:with-param name="characterparaStyle" select="$characterStyle"/>
							</xsl:call-template>
						</xsl:if>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="HeadingsPart">
		<xsl:param name="checkilvl" as="xs:integer"/>
		<xsl:param name="checknumId" as="xs:string"/>
		<xsl:param name="doc" as="xs:string"/>
		<xsl:variable name="val" as="xs:string" select="$numberingXml//w:numbering/w:num[@w:numId=$checknumId]/w:abstractNumId/@w:val"/>

		<xsl:variable name="CheckNumId" as="xs:string" select="d:CheckHeadingNumID($myObj,$checknumId)"/>
		<xsl:if test="$CheckNumId ='True'">
			<xsl:sequence select="d:sink(d:StartNewHeadingCounter($myObj,$checknumId,$val))"/> <!-- empty -->
		</xsl:if>

		<!--Checking numbering.xml for the type of List-->
		<xsl:variable    name="numFormat" as="xs:string" select="$numberingXml//w:numbering/w:abstractNum[@w:abstractNumId=$val]/w:lvl[@w:ilvl=$checkilvl]/w:numFmt/@w:val"/>
		<xsl:variable    name="lvlText" as="xs:string" select="$numberingXml//w:numbering/w:abstractNum[@w:abstractNumId=$val]/w:lvl[@w:ilvl=$checkilvl]/w:lvlText/@w:val"/>
		<xsl:variable    name="lStartOverride" as="xs:string?" select="$numberingXml//w:numbering/w:num[@w:numId=$checknumId]/w:lvlOverride[@w:ilvl=$checkilvl]/w:startOverride/@w:val"/>
		<xsl:variable    name="lStart" as="xs:string" select="$numberingXml//w:numbering/w:abstractNum[@w:abstractNumId=$val]/w:lvl[@w:ilvl=$checkilvl]/w:start/@w:val"/>

		<xsl:sequence select="d:sink(d:AddCurrHeadId($myObj,$checknumId))"/> <!-- empty -->
		<xsl:variable name="addCurrLvl" as="xs:string" select="d:AddCurrHeadLevel($myObj,$checkilvl,$doc,$val)"/>

		<xsl:choose>
			<xsl:when test="string-length(substring-before($addCurrLvl,'|'))=0">
				<xsl:choose>
					<xsl:when test="exists($lStartOverride)">
						<xsl:sequence select="d:sink(d:StartHeadingNewCtr($myObj,$checknumId,$val))"/> <!-- empty -->
						<xsl:sequence select="d:sink(d:StartHeadingString($myObj,$checkilvl,$lStartOverride,$checknumId,$val,$doc,'Yes'))"/> <!-- empty -->
						<xsl:sequence select="d:sink(d:CopyToCurrCounter($myObj,$checknumId))"/> <!-- empty -->
					</xsl:when>
					<xsl:when test="$numberingXml//w:numbering/w:num[@w:numId=$checknumId]/w:lvlOverride">
						<xsl:sequence select="d:sink(d:StartHeadingNewCtr($myObj,$checknumId,$val))"/> <!-- empty -->
						<xsl:sequence select="d:sink(d:StartHeadingString($myObj,$checkilvl,'0',$checknumId,$val,$doc,'Yes'))"/> <!-- empty -->
						<xsl:sequence select="d:sink(d:CopyToCurrCounter($myObj,$checknumId))"/> <!-- empty -->
					</xsl:when>
					<xsl:otherwise>
						<xsl:sequence select="d:sink(d:StartHeadingNewCtr($myObj,$checknumId,$val))"/> <!-- empty -->
						<xsl:sequence select="d:sink(d:StartHeadingString($myObj,$checkilvl,$lStart,$checknumId,$val,$doc,'No'))"/> <!-- empty -->
						<xsl:sequence select="d:sink(d:CopyToCurrCounter($myObj,$checknumId))"/> <!-- empty -->
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
		</xsl:choose>
		<xsl:choose>
			<xsl:when test="$doc='Document'">
				<xsl:sequence select="d:StoreHeadingPart($myObj,concat($numFormat,'|',$lvlText))"/> <!-- empty -->
			</xsl:when>
			<xsl:otherwise>
				<xsl:sequence select="d:StoreHeadingPart($myObj,concat($numFormat,'|',$lvlText,'!',$checknumId))"/> <!-- empty -->
			</xsl:otherwise>
		</xsl:choose>

	</xsl:template>

	<!-- Handles text with custom character styles : 
						if 
						convert the custom style as span with class (?) that contains style attributes
						TODO: Not sure this code is actually working, might need to check dtbook spec regarding span with class  -->
	<xsl:template name="RunTextCustomCharacterStylesHandler">
		<xsl:param name="characterStyle" as="xs:boolean"/>
		<xsl:param name="txt" as="xs:string"/>
		<xsl:choose>
			<xsl:when test="$characterStyle">
				<xsl:choose>
					<xsl:when test="( w:rPr/w:u and(not(w:rPr/w:u[@w:val='none']))) and w:rPr/w:strike and w:rPr/w:color and w:rPr/w:sz and w:rPr/w:spacing">
						<xsl:variable name="val_color" as="xs:string" select="w:rPr/w:color/@w:val"/>
						<xsl:variable name="val" as="xs:integer" select="w:rPr/w:sz/@w:val"/>
						<xsl:variable name="val_sz" as="xs:integer" select="xs:integer(round(($val) div 2))"/>
						<xsl:variable name="valspace" as="xs:integer" select="w:rPr/w:spacing/@w:val"/>
						<xsl:variable name="val_spacing" as="xs:integer" select="xs:integer(round(($valspace*0.1) div 2))"/>
						<span class="{concat('text-decoration:Underline line-through;color:#',$val_color ,';letter-spacing:',$val_spacing ,';font-size:',$val_sz)}">
							<xsl:if test="not($txt='')">
								<xsl:value-of select="$txt"/>
							</xsl:if>
							<xsl:value-of select="w:t"/>
						</span>
					</xsl:when>
					<xsl:when test="( w:rPr/w:u and(not(w:rPr/w:u[@w:val='none']))) and w:rPr/w:strike and w:rPr/w:color and w:rPr/w:sz">
						<xsl:variable name="val_color" as="xs:string" select="w:rPr/w:color/@w:val"/>
						<xsl:variable name="val" as="xs:integer" select="w:rPr/w:sz/@w:val"/>
						<xsl:variable name="val_sz" as="xs:integer" select="xs:integer(round(($val) div 2))"/>
						<span class="{concat('text-decoration:Underline line-through;color:#',$val_color ,';font-size:',$val_sz)}">
							<xsl:if test="not($txt='')">
								<xsl:value-of select="$txt"/>
							</xsl:if>
							<xsl:value-of select="w:t"/>
						</span>
					</xsl:when>
					<xsl:when test="w:rPr/w:strike and (w:rPr/w:u and(not(w:rPr/w:u[@w:val='none']))) and w:rPr/w:color">
						<xsl:variable name="val" as="xs:string" select="w:rPr/w:color/@w:val"/>
						<span class="{concat('text-decoration:Underline line-through;color:#',$val)}">
							<xsl:if test="not($txt='')">
								<xsl:value-of select="$txt"/>
							</xsl:if>
							<xsl:value-of select="w:t"/>
						</span>
					</xsl:when>
					<xsl:when test="( w:rPr/w:u and(not(w:rPr/w:u[@w:val='none']))) and w:rPr/w:color and w:rPr/w:sz">
						<xsl:variable name="val_color" as="xs:string" select="w:rPr/w:color/@w:val"/>
						<xsl:variable name="val" as="xs:integer" select="w:rPr/w:sz/@w:val"/>
						<xsl:variable name="val_sz" as="xs:integer" select="xs:integer(round(($val) div 2))"/>
						<span class="{concat('text-decoration:Underline;color:#',$val_color,';font-size:',$val_sz)}">
							<xsl:if test="not($txt='')">
								<xsl:value-of select="$txt"/>
							</xsl:if>
							<xsl:value-of select="w:t"/>
						</span>
					</xsl:when>
					<xsl:when test="w:rPr/w:strike and w:rPr/w:color and w:rPr/w:sz">
						<xsl:variable name="val_color" as="xs:string" select="w:rPr/w:color/@w:val"/>
						<xsl:variable name="val" as="xs:integer" select="w:rPr/w:sz/@w:val"/>
						<xsl:variable name="val_sz" as="xs:integer" select="xs:integer(round(($val) div 2))"/>
						<span class="{concat('text-decoration:line-through;color:#',$val_color,';font-size:',$val_sz)}">
							<xsl:if test="not($txt='')">
								<xsl:value-of select="$txt"/>
							</xsl:if>
							<xsl:value-of select="w:t"/>
						</span>
					</xsl:when>
					<xsl:when test="w:rPr/w:strike and ( w:rPr/w:u and(not(w:rPr/w:u[@w:val='none'])))">
						<span class="text-decoration:Underline line-through">
							<xsl:if test="not($txt='')">
								<xsl:value-of select="$txt"/>
							</xsl:if>
							<xsl:value-of select="w:t"/>
						</span>
					</xsl:when>
					<xsl:when test="( w:rPr/w:u and(not(w:rPr/w:u[@w:val='none']))) and w:rPr/w:sz">
						<xsl:variable name="val" as="xs:integer" select="w:rPr/w:sz/@w:val"/>
						<xsl:variable name="val_sz" as="xs:integer" select="xs:integer(round(($val) div 2))"/>
						<span class="{concat('text-decoration:Underline;font-size:',$val_sz)}">
							<xsl:if test="not($txt='')">
								<xsl:value-of select="$txt"/>
							</xsl:if>
							<xsl:value-of select="w:t"/>
						</span>
					</xsl:when>
					<xsl:when test="w:rPr/w:strike and w:rPr/w:sz">
						<xsl:variable name="val" as="xs:integer" select="w:rPr/w:sz/@w:val"/>
						<xsl:variable name="val_sz" as="xs:integer" select="xs:integer(round(($val) div 2))"/>
						<span class="{concat('text-decoration:line-through;font-size:',$val_sz)}">
							<xsl:if test="not($txt='')">
								<xsl:value-of select="$txt"/>
							</xsl:if>
							<xsl:value-of select="w:t"/>
						</span>
					</xsl:when>
					<xsl:when test="( w:rPr/w:u and(not(w:rPr/w:u[@w:val='none']))) and w:rPr/w:color">
						<xsl:variable name="val" as="xs:string" select="w:rPr/w:color/@w:val"/>
						<span class="{concat('text-decoration:Underline;color:#',$val)}">
							<xsl:if test="not($txt='')">
								<xsl:value-of select="$txt"/>
							</xsl:if>
							<xsl:value-of select="w:t"/>
						</span>
					</xsl:when>
					<xsl:when test="w:rPr/w:strike and w:rPr/w:color">
						<xsl:variable name="val" as="xs:string" select="w:rPr/w:color/@w:val"/>
						<span class="{concat('text-decoration:line-through;color:#',$val)}">
							<xsl:if test="not($txt='')">
								<xsl:value-of select="$txt"/>
							</xsl:if>
							<xsl:value-of select="w:t"/>
						</span>
					</xsl:when>
					<xsl:when test="w:rPr/w:color and w:rPr/w:sz">
						<xsl:variable name="val_color" as="xs:string" select="w:rPr/w:color/@w:val"/>
						<xsl:variable name="val" as="xs:integer" select="w:rPr/w:sz/@w:val"/>
						<xsl:variable name="val_sz" as="xs:integer" select="xs:integer(round(($val) div 2))"/>
						<span class="{concat('color:#',$val_color,';font-size:',$val_sz)}">
							<xsl:if test="not($txt='')">
								<xsl:value-of select="$txt"/>
							</xsl:if>
							<xsl:value-of select="w:t"/>
						</span>
					</xsl:when>
					<xsl:when test="w:rPr/w:color and w:rPr/w:caps">
						<xsl:variable name="val_color" as="xs:string" select="w:rPr/w:color/@w:val"/>
						<span class="{concat('color:#',$val_color,';text-transform:uppercase')}">
							<xsl:if test="not($txt='')">
								<xsl:value-of select="$txt"/>
							</xsl:if>
							<xsl:value-of select="w:t"/>
						</span>
					</xsl:when>
					<xsl:when test="( w:rPr/w:u and(not(w:rPr/w:u[@w:val='none']))) and w:rPr/w:caps">
						<span class="text-decoration:Underline;'text-transform:uppercase'">
							<xsl:if test="not($txt='')">
								<xsl:value-of select="$txt"/>
							</xsl:if>
							<xsl:value-of select="w:t"/>
						</span>
					</xsl:when>
					<xsl:when test="( w:rPr/w:u and(not(w:rPr/w:u[@w:val='none']))) and w:rPr/w:smallCaps">
						<span class="text-decoration:Underline;font-variant:small-caps">
							<xsl:if test="not($txt='')">
								<xsl:value-of select="$txt"/>
							</xsl:if>
							<xsl:value-of select="w:t"/>
						</span>
					</xsl:when>
					<xsl:when test="w:rPr/w:strike and w:rPr/w:caps">
						<span class="text-decoration:line-through;text-transform:uppercase">
							<xsl:if test="not($txt='')">
								<xsl:value-of select="$txt"/>
							</xsl:if>
							<xsl:value-of select="w:t"/>
						</span>
					</xsl:when>
					<xsl:when test="w:rPr/w:strike and w:rPr/w:smallCaps">
						<span class="text-decoration:line-through;font-variant:small-caps">
							<xsl:if test="not($txt='')">
								<xsl:value-of select="$txt"/>
							</xsl:if>
							<xsl:value-of select="w:t"/>
						</span>
					</xsl:when>
					<xsl:when test="w:rPr/w:color and w:rPr/w:smallCaps">
						<xsl:variable name="val_color" as="xs:string" select="w:rPr/w:color/@w:val"/>
						<span class="{concat('font-variant:small-caps,color:#',$val_color)}">
							<xsl:if test="not($txt='')">
								<xsl:value-of select="$txt"/>
							</xsl:if>
							<xsl:value-of select="w:t"/>
						</span>
					</xsl:when>

					<xsl:when test="( w:rPr/w:u and(not(w:rPr/w:u[@w:val='none'])))">
						<span class="text-decoration:underline">
							<xsl:if test="not($txt='')">
								<xsl:value-of select="$txt"/>
							</xsl:if>
							<xsl:value-of select="w:t"/>
						</span>
					</xsl:when>
					<xsl:when test="w:rPr/w:strike">
						<span class="text-decoration:line-through">
							<xsl:if test="not($txt='')">
								<xsl:value-of select="$txt"/>
							</xsl:if>
							<xsl:value-of select="w:t"/>
						</span>
					</xsl:when>
					<xsl:when test="w:rPr/w:smallCaps">
						<span class="font-variant:small-caps">
							<xsl:if test="not($txt='')">
								<xsl:value-of select="$txt"/>
							</xsl:if>
							<xsl:value-of select="w:t"/>
						</span>
					</xsl:when>
					<xsl:when test="w:rPr/w:spacing">
						<xsl:variable name="val" as="xs:integer" select="w:rPr/w:spacing/@w:val"/>
						<xsl:variable name="val_spacing" as="xs:integer" select="xs:integer(round(($val*0.1) div 2))"/>
						<span class="{concat('letter-spacing:',$val_spacing,'pt')}">
							<xsl:if test="not($txt='')">
								<xsl:value-of select="$txt"/>
							</xsl:if>
							<xsl:value-of select="w:t"/>
						</span>
					</xsl:when>
					<xsl:when test="w:rPr/w:color">
						<xsl:variable name="val" as="xs:string" select="w:rPr/w:color/@w:val"/>
						<span class="{concat('color:#',$val)}">
							<xsl:if test="not($txt='')">
								<xsl:value-of select="$txt"/>
							</xsl:if>
							<xsl:value-of select="w:t"/>
						</span>
					</xsl:when>
					<xsl:when test="w:rPr/w:caps">
						<span class="text-transform:uppercase">
							<xsl:if test="not($txt='')">
								<xsl:value-of select="$txt"/>
							</xsl:if>
							<xsl:value-of select="w:t"/>
						</span>
					</xsl:when>
					<xsl:when test="w:rPr/w:sz">
						<xsl:variable name="val" as="xs:integer" select="w:rPr/w:sz/@w:val"/>
						<xsl:variable name="val_sz" as="xs:integer" select="xs:integer(round(($val) div 2))"/>
						<span class="{concat('font-size:',$val_sz)}">
							<xsl:if test="not($txt='')">
								<xsl:value-of select="$txt"/>
							</xsl:if>
							<xsl:value-of select="w:t"/>
						</span>
					</xsl:when>
					<xsl:otherwise>
						<xsl:if test="not($txt='')">
							<xsl:value-of select="$txt"/>
						</xsl:if>
						<xsl:value-of select="w:t"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:if test="not($txt='')">
					<xsl:value-of select="$txt"/>
				</xsl:if>
				<xsl:value-of select="w:t"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!--Template to implement Custom and inbuilt paragraph styles.-->
	<xsl:template name="ParagraphStyle">
		<xsl:param name="acceptRevisions" as="xs:boolean" select="true()"/>
		<xsl:param name="version" as="xs:string" select="''"/>
		<xsl:param name="flagNote" as="xs:string" select="''"/>
		<xsl:param name="checkid" as="xs:integer?"/>
		<xsl:param name="pagination" as="xs:string" select="''"/>
		<xsl:param name="sOperators" as="xs:string" select="''"/>
		<xsl:param name="sMinuses" as="xs:string" select="''"/>
		<xsl:param name="sNumbers" as="xs:string" select="''"/>
		<xsl:param name="sZeros" as="xs:string" select="''"/>
		<xsl:param name="txt" as="xs:string" select="''"/>
		<xsl:param name="masterparastyle" as="xs:boolean" select="false()"/>
		<xsl:param name="imgOptionPara" as="xs:string" select="''"/>
		<xsl:param name="dpiPara" as="xs:float?"/>
		<xsl:param name="characterparaStyle" as="xs:boolean"/>

		<xsl:variable name="checkImageposition" as="xs:integer" select="d:GetCaptionsProdnotes($myObj)"/>
		<!-- Closing previously manually opened block before treating -->
		<!-- Optional sidebar -->
		<xsl:if test="not(w:pPr/w:pStyle[starts-with(@w:val,'Sidebar') and ends-with(@w:val,'OptionalDAISY')])
									and count(preceding-sibling::node()[1]/w:pPr/w:pStyle[starts-with(@w:val,'Sidebar') and ends-with(@w:val,'OptionalDAISY')])=1">
			<xsl:value-of disable-output-escaping="yes" select="'&lt;/sidebar&gt;'"/>
		</xsl:if>
		<!-- Required sidebar -->
		<xsl:if test="not(w:pPr/w:pStyle[starts-with(@w:val,'Sidebar') and ends-with(@w:val,'RequiredDAISY')])
									and count(preceding-sibling::node()[1]/w:pPr/w:pStyle[starts-with(@w:val,'Sidebar') and ends-with(@w:val,'RequiredDAISY')])=1">
			<xsl:value-of disable-output-escaping="yes" select="'&lt;/sidebar&gt;'"/>
		</xsl:if>
		<!-- epigraph -->
		<xsl:if test="not(w:pPr/w:pStyle[substring(@w:val,1,8)='Epigraph'])
									and count(preceding-sibling::node()[1]/w:pPr/w:pStyle[substring(@w:val,1,8)='Epigraph'])=1">
			<xsl:value-of disable-output-escaping="yes" select="'&lt;/epigraph&gt;'"/>
		</xsl:if>

		<!--<xsl:if test="not(w:pPr/w:pStyle[substring(@w:val,1,4)='PoemDAISY'])
									and count(preceding-sibling::node()[1]/w:pPr/w:pStyle[@w:val='PoemDAISY'])=1">
			<xsl:value-of disable-output-escaping="yes" select="'&lt;/linegroup&gt;'"/>
		</xsl:if>-->
		<!-- Poem -->
		<xsl:if test="not(w:pPr/w:pStyle[substring(@w:val,1,4)='Poem'])
									and count(preceding-sibling::node()[1]/w:pPr/w:pStyle[substring(@w:val,1,4)='Poem'])=1">
			<xsl:value-of disable-output-escaping="yes" select="'&lt;/poem&gt;'"/>
		</xsl:if>
		<xsl:if test="not(w:pPr/w:pStyle[substring(@w:val,1,5)='Block'])
									and count(preceding-sibling::node()[1]/w:pPr/w:pStyle[substring(@w:val,1,5)='Block'])=1">
			<xsl:value-of disable-output-escaping="yes" select="'&lt;/blockquote&gt;'"/>
		</xsl:if>
		<xsl:if test="not(w:pPr/w:pStyle[contains(@w:val,'Prodnote-RequiredDAISY')])
									and count(preceding-sibling::node()[1]/w:pPr/w:pStyle[contains(@w:val,'Prodnote-RequiredDAISY')])=1">
			<xsl:value-of disable-output-escaping="yes" select="'&lt;/prodnote &gt;'"/>
		</xsl:if>

		<!-- Optional prodnote-->
		<xsl:if test="not(w:pPr/w:pStyle[contains(@w:val,'Prodnote-OptionalDAISY')])
									and count(preceding-sibling::node()[1]/w:pPr/w:pStyle[contains(@w:val,'Prodnote-OptionalDAISY')])=1">
			<xsl:value-of disable-output-escaping="yes" select="'&lt;/prodnote &gt;'"/>
		</xsl:if>

		<xsl:choose>
			<!--Checking for Title/Subtitle paragraph style-->
			<xsl:when test="(w:pPr/w:pStyle/@w:val='Title') or (w:pPr/w:pStyle/@w:val='Subtitle')">
				<xsl:variable name="lang" as="xs:string">
					<xsl:call-template name="GetParagraphLanguage">
						<xsl:with-param name="paragraphNode" select="." />
					</xsl:call-template>
				</xsl:variable>
				<doctitle xml:lang="{$lang}">
					<xsl:call-template name="ParaHandler">
						<xsl:with-param name="flag" select="'0'"/>
						<xsl:with-param name="version" select="$version"/>
						<xsl:with-param name="pagination" select="$pagination"/>
						<xsl:with-param name="txt" select="$txt"/>
						<xsl:with-param name="charparahandlerStyle" select="$characterparaStyle"/>
					</xsl:call-template>
				</doctitle>
			</xsl:when>
			<!--Checking for AuthorDAISY custom paragraph style-->
			<xsl:when test="(w:pPr/w:pStyle/@w:val='AuthorDAISY')">
				<xsl:variable name="lang">
					<xsl:call-template name="GetParagraphLanguage">
						<xsl:with-param name="paragraphNode" select="." />
					</xsl:call-template>
				</xsl:variable>
				<author xml:lang="{$lang}">
					<xsl:if test="$flagNote='footnote' or $flagNote='endnote'">
						<xsl:if test="d:NoteFlag($myObj)=1">
							<p>
								<xsl:value-of select="$FootnotesNumberingPrefix"/>
								<xsl:choose>
									<xsl:when test="$FootnotesNumbering = 'number'">
										<xsl:value-of select="$checkid + number($FootnotesStartValue - 1)"/>
									</xsl:when>
								</xsl:choose>
								<xsl:value-of select="$FootnotesNumberingSuffix"/>
							</p>
						</xsl:if>
					</xsl:if>
					<xsl:call-template name="Paracharacterstyle">
						<xsl:with-param name="characterStyle" select="$characterparaStyle"/>
						<xsl:with-param name="txt" select="$txt"/>
						<xsl:with-param name="flag" select="'0'"/>
					</xsl:call-template>
				</author>
			</xsl:when>
			<!--Checking for CovertitleDAISY custom paragraph style-->
			<xsl:when test="(w:pPr/w:pStyle/@w:val='CovertitleDAISY')">
				<xsl:variable name="lang">
					<xsl:call-template name="GetParagraphLanguage">
						<xsl:with-param name="paragraphNode" select="." />
					</xsl:call-template>
				</xsl:variable>
				<covertitle xml:lang="{$lang}">
					<xsl:call-template name="Paracharacterstyle">
						<xsl:with-param name="characterStyle" select="$characterparaStyle"/>
						<xsl:with-param name="txt" select="$txt"/>
						<xsl:with-param name="flag" select="'0'"/>
					</xsl:call-template>
				</covertitle>
			</xsl:when>
			<!--Checking for BylineDAISY custom paragraph style-->
			<xsl:when test="(w:pPr/w:pStyle/@w:val='BylineDAISY')">
				<xsl:if test="$flagNote='footnote' or $flagNote='endnote'">
					<xsl:if test="d:NoteFlag($myObj)=1">
						<p>
							<xsl:value-of select="$FootnotesNumberingPrefix"/>
							<xsl:choose>
								<xsl:when test="$FootnotesNumbering = 'number'">
									<xsl:value-of select="$checkid + number($FootnotesStartValue)"/>
								</xsl:when>
							</xsl:choose>
							<xsl:value-of select="$FootnotesNumberingSuffix"/>
						</p>
					</xsl:if>
				</xsl:if>
				<xsl:variable name="lang">
					<xsl:call-template name="GetParagraphLanguage">
						<xsl:with-param name="paragraphNode" select="." />
					</xsl:call-template>
				</xsl:variable>
				<byline xml:lang="{$lang}">
					<xsl:call-template name="Paracharacterstyle">
						<xsl:with-param name="characterStyle" select="$characterparaStyle"/>
						<xsl:with-param name="txt" select="$txt"/>
						<xsl:with-param name="flag" select="'0'"/>
					</xsl:call-template>
				</byline>
			</xsl:when>
			<!--Checking for DatelineDAISY custom paragraph style-->
			<xsl:when test="(w:pPr/w:pStyle/@w:val='DatelineDAISY')">
				<xsl:if test="$flagNote='footnote' or $flagNote='endnote'">
					<xsl:if test="d:NoteFlag($myObj)=1">
						<p>
							<xsl:value-of select="$FootnotesNumberingPrefix"/>
							<xsl:choose>
								<xsl:when test="$FootnotesNumbering = 'number'">
									<xsl:value-of select="$checkid + number($FootnotesStartValue)"/>
								</xsl:when>
							</xsl:choose>
							<xsl:value-of select="$FootnotesNumberingSuffix"/>
						</p>
					</xsl:if>
				</xsl:if>
				<xsl:variable name="lang">
					<xsl:call-template name="GetParagraphLanguage">
						<xsl:with-param name="paragraphNode" select="." />
					</xsl:call-template>
				</xsl:variable>
				<xsl:value-of disable-output-escaping="yes" select="concat('&lt;dateline xml:lang=&quot;',$lang,'&quot;&gt;')"/>
				<xsl:call-template name="Paracharacterstyle">
					<xsl:with-param name="characterStyle" select="$characterparaStyle"/>
					<xsl:with-param name="txt" select="$txt"/>
					<xsl:with-param name="flag" select="'0'"/>
				</xsl:call-template>
				<xsl:value-of disable-output-escaping="yes" select="'&lt;/dateline&gt;'"/>
			</xsl:when>
			<!--Checking for Prodnote-OptionalDAISY custom paragraph style-->
			<xsl:when test="(w:pPr/w:pStyle/@w:val='Prodnote-OptionalDAISY')and (not((preceding-sibling::node()[$checkImageposition]/w:r/w:drawing) or (preceding-sibling::node()[$checkImageposition]/w:r/w:pict) or (preceding-sibling::node()[$checkImageposition]/w:r/w:object)))">
				<xsl:if test="count(preceding-sibling::node()[1]/w:pPr/w:pStyle[@w:val='Prodnote-OptionalDAISY'])=0">
					<xsl:variable name="lang">
						<xsl:call-template name="GetParagraphLanguage">
							<xsl:with-param name="paragraphNode" select="." />
						</xsl:call-template>
					</xsl:variable>
					<xsl:value-of disable-output-escaping="yes" select="concat('&lt;prodnote render=&quot;optional&quot; xml:lang=&quot;',$lang,'&quot;&gt;')"/>
				</xsl:if>

				<xsl:call-template name="Paracharacterstyle">
					<xsl:with-param name="characterStyle" select="$characterparaStyle"/>
					<xsl:with-param name="txt" select="$txt"/>
					<xsl:with-param name="flag" select="'1'"/>
				</xsl:call-template>
				<xsl:if test="not(following-sibling::w:p)">
					<xsl:value-of disable-output-escaping="yes" select="'&lt;/prodnote&gt;'"/>
				</xsl:if>
				<!--<xsl:if test="count(following-sibling::node()[1]/w:pPr/w:pStyle[contains(@w:val,'Prodnote-OptionalDAISY')])=0">
					<xsl:value-of disable-output-escaping="yes" select="'&lt;/prodnote &gt;'"/>
				</xsl:if>-->
			</xsl:when>
			<!--Checking for Prodnote-RequiredDAISY custom paragraph style-->
			<xsl:when test="(w:pPr/w:pStyle/@w:val='Prodnote-RequiredDAISY')and (not((preceding-sibling::node()[$checkImageposition]/w:r/w:drawing) or (preceding-sibling::node()[$checkImageposition]/w:r/w:pict) or (preceding-sibling::node()[$checkImageposition]/w:r/w:object)))">
				<xsl:if test="count(preceding-sibling::node()[1]/w:pPr/w:pStyle[@w:val='Prodnote-RequiredDAISY'])=0">
					<xsl:variable name="lang">
						<xsl:call-template name="GetParagraphLanguage">
							<xsl:with-param name="paragraphNode" select="." />
						</xsl:call-template>
					</xsl:variable>
					<xsl:value-of disable-output-escaping="yes" select="concat('&lt;prodnote render=&quot;required&quot; xml:lang=&quot;',$lang,'&quot;&gt;')"/>
				</xsl:if>

				<xsl:call-template name="Paracharacterstyle">
					<xsl:with-param name="characterStyle" select="$characterparaStyle"/>
					<xsl:with-param name="txt" select="$txt"/>
					<xsl:with-param name="flag" select="'1'"/>
				</xsl:call-template>
				<xsl:if test="not(following-sibling::w:p)">
					<xsl:value-of disable-output-escaping="yes" select="'&lt;/prodnote&gt;'"/>
				</xsl:if>
				<!--<xsl:if test="count(following-sibling::node()[1]/w:pPr/w:pStyle[contains(@w:val,'Prodnote-RequiredDAISY')])=0">
					<xsl:value-of disable-output-escaping="yes" select="'&lt;/prodnote&gt;'"/>
				</xsl:if>-->
			</xsl:when>
			<!--Checking for Blockquote/Blockquote-AuthorDAISY custom paragraph styles-->
			<xsl:when test="w:pPr/w:pStyle[substring(@w:val,1,5)='Block']">
				<xsl:if test="($flagNote='footnote' or $flagNote='endnote') and d:NoteFlag($myObj)=1">
					<p>
						<xsl:value-of select="$FootnotesNumberingPrefix"/>
						<xsl:choose>
								<xsl:when test="$FootnotesNumbering = 'number'">
										<xsl:value-of select="$checkid + number($FootnotesStartValue)"/>
								</xsl:when>
						</xsl:choose>
						<xsl:value-of select="$FootnotesNumberingSuffix"/>
					</p>
				</xsl:if>
				<xsl:if test="count(preceding-sibling::node()[1]/w:pPr/w:pStyle[substring(@w:val,1,5)='Block'])=0">
					<xsl:variable name="lang">
							<xsl:call-template name="GetParagraphLanguage">
									<xsl:with-param name="paragraphNode" select="." />
							</xsl:call-template>
					</xsl:variable>
					<xsl:value-of disable-output-escaping="yes" select="concat('&lt;blockquote xml:lang=&quot;',$lang,'&quot;&gt;')"/>
				</xsl:if>
				<xsl:choose>
						<!--Checking for 'Blockquote-AuthorDAISY' style-->
						<xsl:when test="w:pPr/w:pStyle[@w:val='Blockquote-AuthorDAISY']">
							<author>
								<xsl:call-template name="Paracharacterstyle">
										<xsl:with-param name="characterStyle" select="$characterparaStyle"/>
										<xsl:with-param name="txt" select="$txt"/>
										<xsl:with-param name="flag" select="'0'"/>
								</xsl:call-template>
							</author>
						</xsl:when>
						<!--Checking for List in BlockQuote Element-->
						<xsl:when test="((w:pPr/w:numPr/w:ilvl) and (w:pPr/w:numPr/w:numId))">
							<!--variable checkilvl holds level(w:ilvl) value of the List-->
							<xsl:call-template name="List">
									<xsl:with-param name="listcharStyle" select="$characterparaStyle"/>
							</xsl:call-template>
						</xsl:when>
						<xsl:otherwise>
							<xsl:call-template name="Paracharacterstyle">
									<xsl:with-param name="characterStyle" select="$characterparaStyle"/>
									<xsl:with-param name="txt" select="$txt"/>
									<xsl:with-param name="flag" select="'1'"/>
							</xsl:call-template>
							<!--<xsl:if test="not(w:pPr/pStyle/@w:val='List-HeadingDAISY')">
									<xsl:call-template name="Paracharacterstyle">
											<xsl:with-param name="characterStyle" select="$characterparaStyle"/>
											<xsl:with-param name="txt" select="$txt"/>
											<xsl:with-param name="flag" select="'0'"/>
									</xsl:call-template>
							</xsl:if>-->
						</xsl:otherwise>
				</xsl:choose>
				<xsl:if test="not(following-sibling::w:p)">
					<xsl:value-of disable-output-escaping="yes" select="'&lt;/blockquote&gt;'"/>
				</xsl:if>
			</xsl:when>
			<!--Checking for PoemDAISY/Poem-TitleDAISY/Poem-HeadingDAISY/Poem-AuthorDAISY/Poem-BylineDAISY custom paragraph styles-->
			<xsl:when test="w:pPr/w:pStyle[substring(@w:val,1,4)='Poem']">
				<xsl:if test="$flagNote='footnote' or $flagNote='endnote'">
					<xsl:if test="d:NoteFlag($myObj)=1">
						<p>
							<xsl:value-of select="$FootnotesNumberingPrefix"/>
							<xsl:choose>
								<xsl:when test="$FootnotesNumbering = 'number'">
									<xsl:value-of select="$checkid + number($FootnotesStartValue)"/>
								</xsl:when>
							</xsl:choose>
							<xsl:value-of select="$FootnotesNumberingSuffix"/>
						</p>
					</xsl:if>
				</xsl:if>
				<xsl:if test="count(preceding-sibling::node()[1]/w:pPr/w:pStyle[substring(@w:val,1,4)='Poem'])=0">
					<xsl:variable name="lang">
						<xsl:call-template name="GetParagraphLanguage">
							<xsl:with-param name="paragraphNode" select="." />
						</xsl:call-template>
					</xsl:variable>
					<xsl:value-of disable-output-escaping="yes" select="concat('&lt;poem xml:lang=&quot;',$lang,'&quot;&gt;')"/>
				</xsl:if>
				<xsl:if test="w:pPr/w:pStyle/@w:val='Poem-TitleDAISY'">
					<title>
						<xsl:call-template name="Paracharacterstyle">
							<xsl:with-param name="characterStyle" select="$characterparaStyle"/>
							<xsl:with-param name="txt" select="$txt"/>
							<xsl:with-param name="flag" select="'0'"/>
						</xsl:call-template>
					</title>
				</xsl:if>
				<xsl:if test="w:pPr/w:pStyle/@w:val='Poem-HeadingDAISY'">
					<hd>
						<xsl:call-template name="Paracharacterstyle">
							<xsl:with-param name="characterStyle" select="$characterparaStyle"/>
							<xsl:with-param name="txt" select="$txt"/>
							<xsl:with-param name="flag" select="'0'"/>
						</xsl:call-template>
					</hd>
				</xsl:if>
				<xsl:if test="(w:pPr/w:pStyle/@w:val='PoemDAISY')">
					<xsl:if test="count(preceding-sibling::node()[1]/w:pPr/w:pStyle[@w:val='PoemDAISY'])=0">
						<xsl:variable name="lang">
							<xsl:call-template name="GetParagraphLanguage">
								<xsl:with-param name="paragraphNode" select="." />
							</xsl:call-template>
						</xsl:variable>
						<xsl:value-of disable-output-escaping="yes" select="concat('&lt;linegroup xml:lang=&quot;',$lang,'&quot;&gt;')"/>
					</xsl:if>
					<line>
						<xsl:call-template name="Paracharacterstyle">
							<xsl:with-param name="characterStyle" select="$characterparaStyle"/>
							<xsl:with-param name="txt" select="$txt"/>
							<xsl:with-param name="flag" select="'0'"/>
						</xsl:call-template>
					</line>
					<xsl:if test="count(following-sibling::node()[1]/w:pPr/w:pStyle[@w:val='PoemDAISY'])=0">
						<xsl:value-of disable-output-escaping="yes" select="'&lt;/linegroup&gt;'"/>
					</xsl:if>
				</xsl:if>
				<xsl:if test="(w:pPr/w:pStyle/@w:val='Poem-AuthorDAISY')">
					<author>
						<xsl:call-template name="Paracharacterstyle">
							<xsl:with-param name="characterStyle" select="$characterparaStyle"/>
							<xsl:with-param name="txt" select="$txt"/>
							<xsl:with-param name="flag" select="'0'"/>
						</xsl:call-template>
					</author>
				</xsl:if>
				<xsl:if test="(w:pPr/w:pStyle/@w:val='Poem-BylineDAISY')">
					<byline>
						<xsl:call-template name="Paracharacterstyle">
							<xsl:with-param name="characterStyle" select="$characterparaStyle"/>
							<xsl:with-param name="txt" select="$txt"/>
							<xsl:with-param name="flag" select="'0'"/>
						</xsl:call-template>
					</byline>
				</xsl:if>
				<xsl:if test="not(following-sibling::w:p)">
					<xsl:value-of disable-output-escaping="yes" select="'&lt;/poem&gt;'"/>
				</xsl:if>
				<!--<xsl:if test="count(following-sibling::node()[1]/w:pPr/w:pStyle[substring(@w:val,1,4)='Poem'])=0">
					<xsl:value-of disable-output-escaping="yes" select="'&lt;/poem&gt;'"/>
				</xsl:if>-->
			</xsl:when>
			<!--Checking for EpigraphDAISY/Epigraph-AuthorDAISY custom paragraph styles-->
			<xsl:when test="(w:pPr/w:pStyle[substring(@w:val,1,8)='Epigraph'])">
				<xsl:if test="count(preceding-sibling::node()[1]/w:pPr/w:pStyle[substring(@w:val,1,8)='Epigraph'])=0">
					<xsl:variable name="lang">
						<xsl:call-template name="GetParagraphLanguage">
							<xsl:with-param name="paragraphNode" select="." />
						</xsl:call-template>
					</xsl:variable>
					<xsl:value-of disable-output-escaping="yes" select="concat('&lt;epigraph xml:lang=&quot;',$lang,'&quot;&gt;')"/>
				</xsl:if>
				<xsl:call-template name="Paracharacterstyle">
					<xsl:with-param name="characterStyle" select="$characterparaStyle"/>
					<xsl:with-param name="txt" select="$txt"/>
					<xsl:with-param name="flag" select="'1'"/>
				</xsl:call-template>
				<xsl:if test="not(following-sibling::w:p)">
					<xsl:value-of disable-output-escaping="yes" select="'&lt;/epigraph&gt;'"/>
				</xsl:if>
				<!--<xsl:if test="count(following-sibling::node()[1]/w:pPr/w:pStyle[substring(@w:val,1,8)='Epigraph'])=0">
					<xsl:value-of disable-output-escaping="yes" select="'&lt;/epigraph&gt;'"/>
				</xsl:if>-->
			</xsl:when>
			<!--Checking for Sidebar(header)?-OptionalDAISY custom paragraph styles-->
			<xsl:when test="w:pPr/w:pStyle[starts-with(@w:val,'Sidebar') and ends-with(@w:val,'OptionalDAISY')]">
				<xsl:if test="count(preceding-sibling::node()[1]/w:pPr/w:pStyle[starts-with(@w:val,'Sidebar') and ends-with(@w:val,'OptionalDAISY')])=0">
					<xsl:variable name="lang">
						<xsl:call-template name="GetParagraphLanguage">
							<xsl:with-param name="paragraphNode" select="." />
						</xsl:call-template>
					</xsl:variable>
					<xsl:value-of disable-output-escaping="yes" select="concat('&lt;sidebar xml:lang=&quot;',$lang,'&quot; render=&quot;optional&quot; &gt;')"/>
				</xsl:if>
				<xsl:call-template name="Paracharacterstyle">
					<xsl:with-param name="characterStyle" select="$characterparaStyle"/>
					<xsl:with-param name="txt" select="$txt"/>
					<xsl:with-param name="flag" select="'1'"/>
				</xsl:call-template>
				<xsl:if test="not(following-sibling::w:p)">
					<xsl:value-of disable-output-escaping="yes" select="'&lt;/sidebar&gt;'"/>
				</xsl:if>
				<!--<xsl:if test="count(following-sibling::node()[1]/w:pPr/w:pStyle[starts-with(@w:val,'Sidebar') and ends-with(@w:val,'OptionalDAISY')])=0">
					<xsl:value-of disable-output-escaping="yes" select="'&lt;/sidebar&gt;'"/>
				</xsl:if>-->
			</xsl:when>
			<!--Checking for Sidebar(header)?-RequiredDAISY custom paragraph styles-->
			<xsl:when test="w:pPr/w:pStyle[starts-with(@w:val,'Sidebar') and ends-with(@w:val,'RequiredDAISY')]">
				<xsl:if test="count(preceding-sibling::node()[1]/w:pPr/w:pStyle[starts-with(@w:val,'Sidebar') and ends-with(@w:val,'RequiredDAISY')])=0">
					<xsl:variable name="lang">
						<xsl:call-template name="GetParagraphLanguage">
							<xsl:with-param name="paragraphNode" select="." />
						</xsl:call-template>
					</xsl:variable>
					<xsl:value-of disable-output-escaping="yes" select="concat('&lt;sidebar xml:lang=&quot;',$lang,'&quot; render=&quot;required&quot; &gt;')"/>
				</xsl:if>
				<xsl:call-template name="Paracharacterstyle">
					<xsl:with-param name="characterStyle" select="$characterparaStyle"/>
					<xsl:with-param name="txt" select="$txt"/>
					<xsl:with-param name="flag" select="'1'"/>
				</xsl:call-template>
				<xsl:if test="not(following-sibling::w:p)">
					<xsl:value-of disable-output-escaping="yes" select="'&lt;/sidebar&gt;'"/>
				</xsl:if>
				<!--<xsl:if test="count(following-sibling::node()[1]/w:pPr/w:pStyle[starts-with(@w:val,'Sidebar') and ends-with(@w:val,'RequiredDAISY')])=0">
					<xsl:value-of disable-output-escaping="yes" select="'&lt;/sidebar&gt;'"/>
				</xsl:if>-->
			</xsl:when>
			<!--Checking for AddressDAISY custom paragraph style-->
			<xsl:when test="w:pPr/w:pStyle[@w:val='AddressDAISY']">
				<xsl:if test="$flagNote='footnote' or $flagNote='endnote'">
					<xsl:if test="d:NoteFlag($myObj)=1">
						<p>
							<xsl:value-of select="$FootnotesNumberingPrefix"/>
							<xsl:choose>
								<xsl:when test="$FootnotesNumbering = 'number'">
									<xsl:value-of select="$checkid + number($FootnotesStartValue)"/>
								</xsl:when>
							</xsl:choose>
							<xsl:value-of select="$FootnotesNumberingSuffix"/>
						</p>
					</xsl:if>
				</xsl:if>
				<xsl:choose>
					<!--Checking for occurence of Lists in AddressDAISY custom paragraph style-->
					<xsl:when test="(w:pPr/w:numPr/w:ilvl) and (w:pPr/w:numPr/w:numId)">
						<xsl:variable name="lang">
							<xsl:call-template name="GetParagraphLanguage">
								<xsl:with-param name="paragraphNode" select="." />
							</xsl:call-template>
						</xsl:variable>
						<!--Opening Address tag-->
						<xsl:value-of disable-output-escaping="yes" select="concat('&lt;address xml:lang=&quot;',$lang,'&quot;&gt;')"/>
					</xsl:when>
					<xsl:when test="count(preceding-sibling::node()[1]/w:pPr/w:pStyle[@w:val='AddressDAISY'])=0">
						<xsl:variable name="lang">
							<xsl:call-template name="GetParagraphLanguage">
								<xsl:with-param name="paragraphNode" select="." />
							</xsl:call-template>
						</xsl:variable>
						<!--Opening Address tag-->
						<xsl:value-of disable-output-escaping="yes" select="concat('&lt;address xml:lang=&quot;',$lang,'&quot;&gt;')"/>
					</xsl:when>
				</xsl:choose>
				<line>
					<xsl:call-template name="Paracharacterstyle">
						<xsl:with-param name="characterStyle" select="$characterparaStyle"/>
						<xsl:with-param name="txt" select="$txt"/>
						<xsl:with-param name="flag" select="'0'"/>
					</xsl:call-template>
				</line>
				<xsl:choose>
					<!--Checking for Address in a list-->
					<xsl:when test="(w:pPr/w:numPr/w:ilvl) and (w:pPr/w:numPr/w:numId)">
						<xsl:value-of disable-output-escaping="yes" select="'&lt;/address&gt;'"/>
					</xsl:when>
					<!--Checking for address style in the next sibling and closing the tag-->
					<xsl:when test="count(following-sibling::node()[1]/w:pPr/w:pStyle[@w:val='AddressDAISY'])=0">
						<xsl:value-of disable-output-escaping="yes" select="'&lt;/address&gt;'"/>
					</xsl:when>
				</xsl:choose>
			</xsl:when>
			<!--Checking for DivDAISY custom paragraph style-->
			<xsl:when test="w:pPr/w:pStyle[substring(@w:val,1,3)='Div']">
				<xsl:if test="count(preceding-sibling::node()[1]/w:pPr/w:pStyle[substring(@w:val,1,3)='Div'])=0">
					<xsl:value-of disable-output-escaping="yes" select="'&lt;div&gt;'"/>
				</xsl:if>

				<xsl:call-template name="Paracharacterstyle">
					<xsl:with-param name="characterStyle" select="$characterparaStyle"/>
					<xsl:with-param name="txt" select="$txt"/>
					<xsl:with-param name="flag" select="'1'"/>
				</xsl:call-template>

				<xsl:if test="count(following-sibling::node()[1]/w:pPr/w:pStyle[substring(@w:val,1,3)='Div'])=0">
					<xsl:value-of disable-output-escaping="yes" select="'&lt;/div&gt;'"/>
				</xsl:if>
			</xsl:when>

			<!--Checking for occurence of Lists in paragraph-->
			<xsl:when test="(w:pPr/w:numPr/w:ilvl) and (w:pPr/w:numPr/w:numId)">
				<xsl:call-template name="ParaHandler">
					<xsl:with-param name="flag" select="'3'"/>
					<xsl:with-param name="version" select="$version"/>
					<xsl:with-param name="flagNote" select="$flagNote"/>
					<xsl:with-param name="pagination" select="$pagination"/>
					<xsl:with-param name="sOperators" select="$sOperators"/>
					<xsl:with-param name="sMinuses" select="$sMinuses"/>
					<xsl:with-param name="sNumbers" select="$sNumbers"/>
					<xsl:with-param name="sZeros" select="$sZeros"/>
					<xsl:with-param name="txt" select="$txt"/>
					<xsl:with-param name="mastersubpara" select="$masterparastyle"/>
					<xsl:with-param name="charparahandlerStyle" select="$characterparaStyle"/>
				</xsl:call-template>
			</xsl:when>

			<!--Checking for DefinitionTermDAISY custom character style and DefinitionDataDAISY    custom paragraph style-->
			<xsl:when test="(w:r/w:rPr/w:rStyle/@w:val='DefinitionTermDAISY') or (w:pPr/w:pStyle/@w:val='DefinitionDataDAISY')">
				<xsl:if test="(count(preceding-sibling::node()[1]/w:pPr/w:pStyle[@w:val='DefinitionDataDAISY'])=0)
					and (count(preceding-sibling::node()[1]/w:r/w:rPr/w:rStyle[@w:val='DefinitionTermDAISY'])=0)">
					<xsl:value-of disable-output-escaping="yes" select="'&lt;dl&gt;'"/>
				</xsl:if>
				<!--Checking for DefinitionTermDAISY custom character style-->
				<xsl:if test="w:r/w:rPr/w:rStyle/@w:val='DefinitionTermDAISY'">
					<dt>
						<!--Checking if image is bidirectionally oriented-->
						<xsl:if test="(w:pPr/w:bidi) or (w:r/w:rPr/w:rtl)">
							<!--Variable holds the value which indicates that the image is bidirectionally oriented-->
							<xsl:variable name="definitionlistBd" as="xs:string">
								<xsl:call-template name="GetParagraphLanguage">
									<xsl:with-param name="paragraphNode" select="."/>
								</xsl:call-template>
							</xsl:variable>
							<xsl:value-of disable-output-escaping="yes" select="concat('&lt;bdo dir= &quot;rtl&quot; xml:lang=&quot;',$definitionlistBd,'&quot;&gt;')"/>
						</xsl:if>
						<xsl:value-of select="w:r/w:t"/>
						<xsl:if test="(w:pPr/w:bidi) or (w:r/w:rPr/w:rtl)">
							<xsl:value-of disable-output-escaping="yes" select="'&lt;/bdo&gt;'"/>
						</xsl:if>
					</dt>
				</xsl:if>
				<!--Checking for DefinitionData-DAISY custom character style-->
				<xsl:if test="(w:pPr/w:pStyle/@w:val='DefinitionDataDAISY')">
					<dd>
						<xsl:call-template name="Paracharacterstyle">
							<xsl:with-param name="characterStyle" select="$characterparaStyle"/>
							<xsl:with-param name="txt" select="$txt"/>
							<xsl:with-param name="flag" select="'0'"/>
						</xsl:call-template>
					</dd>
				</xsl:if>
				<xsl:if test="(count(following-sibling::node()[1]/w:pPr/w:pStyle[@w:val='DefinitionDataDAISY'])=0)
					and (count(following-sibling::node()[1]/w:r/w:rPr/w:rStyle[@w:val='DefinitionTermDAISY'])=0)">
					<xsl:value-of disable-output-escaping="yes" select="'&lt;/dl&gt;'"/>
				</xsl:if>
			</xsl:when>

			<xsl:when test="$characterparaStyle">
				<xsl:choose>
					<xsl:when test="w:pPr/w:ind[@w:left] and w:pPr/w:ind[@w:right]">
						<p>
							<xsl:variable name="val" as="xs:integer" select="w:pPr/w:ind/@w:left"/>
							<xsl:variable name="val_left" as="xs:integer" select="xs:integer(round($val div 1440))"/>
							<xsl:variable name="valright" as="xs:integer" select="w:pPr/w:ind/@w:right"/>
							<xsl:variable name="val_right" as="xs:integer" select="xs:integer(round($valright div 1440))"/>
							<span class="{concat('text-indent:', 'right=',$val_right,'in',';left=',$val_left,'in')}">
								<xsl:call-template name="ParaHandler">
									<xsl:with-param name="flag" select="'0'"/>
									<xsl:with-param name="version" select="$version"/>
									<xsl:with-param name="pagination" select="$pagination"/>
									<xsl:with-param name="txt" select="$txt"/>
									<xsl:with-param name="charparahandlerStyle" select="$characterparaStyle"/>
								</xsl:call-template>
							</span>
						</p>
					</xsl:when>
					<xsl:when test="w:pPr/w:ind[@w:left] and    w:pPr/w:jc">
						<p>
							<xsl:variable name="val" as="xs:integer" select="w:pPr/w:ind/@w:left"/>
							<xsl:variable name="val_left" as="xs:integer" select="xs:integer(round($val div 1440))"/>
							<xsl:variable name="val1" as="xs:string" select="w:pPr/w:jc/@w:val"/>
							<span class="{concat('text-indent:',';left=',$val_left,'in',';text-align:',$val1)}">
								<xsl:call-template name="ParaHandler">
									<xsl:with-param name="flag" select="'0'"/>
									<xsl:with-param name="version" select="$version"/>
									<xsl:with-param name="pagination" select="$pagination"/>
									<xsl:with-param name="txt" select="$txt"/>
									<xsl:with-param name="charparahandlerStyle" select="$characterparaStyle"/>
								</xsl:call-template>
							</span>
						</p>
					</xsl:when>
					<xsl:when test="w:pPr/w:ind[@w:left]">
						<p>
							<xsl:variable name="val" as="xs:integer" select="w:pPr/w:ind/@w:left"/>
							<xsl:variable name="val_left" as="xs:integer" select="xs:integer(round($val div 1440))"/>
							<span class="{concat('text-indent:',$val_left,'in')}">
								<xsl:call-template name="ParaHandler">
									<xsl:with-param name="flag" select="'0'"/>
									<xsl:with-param name="version" select="$version"/>
									<xsl:with-param name="pagination" select="$pagination"/>
									<xsl:with-param name="charparahandlerStyle" select="$characterparaStyle"/>
								</xsl:call-template>
							</span>
						</p>
					</xsl:when>
					<xsl:when test="w:pPr/w:ind[@w:right]">
						<p>
							<xsl:variable name="val" as="xs:integer" select="w:pPr/w:ind/@w:right"/>
							<xsl:variable name="val_right" as="xs:integer" select="xs:integer(round($val div 1440))"/>
							<span class="{concat('text-indent:',$val_right,'in')}">
								<xsl:call-template name="ParaHandler">
									<xsl:with-param name="flag" select="'0'"/>
									<xsl:with-param name="version" select="$version"/>
									<xsl:with-param name="pagination" select="$pagination"/>
									<xsl:with-param name="txt" select="$txt"/>
									<xsl:with-param name="charparahandlerStyle" select="$characterparaStyle"/>
								</xsl:call-template>
							</span>
						</p>
					</xsl:when>
					<xsl:when test="w:pPr/w:jc">
						<p>
							<xsl:variable name="val" as="xs:string" select="w:pPr/w:jc/@w:val"/>
							<span class="{concat('text-align:',$val)}">
								<xsl:call-template name="ParaHandler">
									<xsl:with-param name="flag" select="'0'"/>
									<xsl:with-param name="version" select="$version"/>
									<xsl:with-param name="pagination" select="$pagination"/>
									<xsl:with-param name="txt" select="$txt"/>
									<xsl:with-param name="charparahandlerStyle" select="$characterparaStyle"/>
								</xsl:call-template>
							</span>
						</p>
					</xsl:when>
					<xsl:otherwise>
						<xsl:call-template name="ParaHandler">
							<xsl:with-param name="flag" select="'1'"/>
							<xsl:with-param name="acceptRevisions" select="$acceptRevisions"/>
							<xsl:with-param name="version" select="$version"/>
							<xsl:with-param name="flagNote" select="$flagNote"/>
							<xsl:with-param name="checkid" select="$checkid"/>
							<xsl:with-param name="pagination" select="$pagination"/>
							<xsl:with-param name="mastersubpara" select="$masterparastyle"/>
							<xsl:with-param name="imgOptionPara" select="$imgOptionPara"/>
							<xsl:with-param name="dpiPara" select="$dpiPara"/>
							<xsl:with-param name="txt" select="$txt"/>
							<xsl:with-param name="charparahandlerStyle" select="$characterparaStyle"/>
						</xsl:call-template>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>

			<!--Checking if table occurs in the document and implementing all the styles applied on it-->
			<xsl:when test="(
					(parent::w:tc)
					and (
						not(
							(w:pPr/w:pStyle/@w:val='Prodnote-OptionalDAISY')
							or (w:pPr/w:pStyle/@w:val='Prodnote-RequiredDAISY')
							or (w:pPr/w:pStyle/@w:val='Image-CaptionDAISY')
						)
					)
				)">
				<xsl:call-template name="ParaHandler">
					<xsl:with-param name="flag" select="'2'"/>
					<xsl:with-param name="version" select="$version"/>
					<xsl:with-param name="mastersubpara" select="$masterparastyle"/>
					<xsl:with-param name="pagination" select="$pagination"/>
					<xsl:with-param name="charparahandlerStyle" select="$characterparaStyle"/>
					<xsl:with-param name="sOperators" select="$sOperators"/>
					<xsl:with-param name="sMinuses" select="$sMinuses"/>
					<xsl:with-param name="sNumbers" select="$sNumbers"/>
					<xsl:with-param name="sZeros" select="$sZeros"/>
				</xsl:call-template>
			</xsl:when>
			<!--Checking if no style exist and then calling the template named ParaHandler-->
			<xsl:when test="not(
					(
						(
							(w:pPr/w:pStyle/@w:val='Table-CaptionDAISY')
							or (w:pPr/w:pStyle/@w:val='Caption')
							or child::w:fldSimple
						) and (
							(preceding-sibling::node()[1][self::w:tbl])
							or (following-sibling::node()[1][self::w:tbl])
						)
					) or (w:pPr/w:pStyle[substring(@w:val,1,3)='TOC'])
					or (preceding-sibling::node()[$checkImageposition]/w:r/w:drawing)
					or (preceding-sibling::node()[$checkImageposition]/w:r/w:pict)
					or (
						(w:pPr/w:pStyle[@w:val='Image-CaptionDAISY'])
						and (
							(following-sibling::node()[1]/w:r/w:drawing)
							or (following-sibling::node()[1]/w:r/w:pict)
						)
					)
				)">
				<xsl:call-template name="ParaHandler">
					<xsl:with-param name="flag" select="'1'"/>
					<xsl:with-param name="acceptRevisions" select="$acceptRevisions"/>
					<xsl:with-param name="version" select="$version"/>
					<xsl:with-param name="flagNote" select="$flagNote"/>
					<xsl:with-param name="checkid" select="$checkid"/>
					<xsl:with-param name="pagination" select="$pagination"/>
					<xsl:with-param name="mastersubpara" select="$masterparastyle"/>
					<xsl:with-param name="imgOptionPara" select="$imgOptionPara"/>
					<xsl:with-param name="dpiPara" select="$dpiPara"/>
					<xsl:with-param name="txt" select="$txt"/>
					<xsl:with-param name="charparahandlerStyle" select="$characterparaStyle"/>
					<xsl:with-param name="sOperators" select="$sOperators"/>
					<xsl:with-param name="sMinuses" select="$sMinuses"/>
					<xsl:with-param name="sNumbers" select="$sNumbers"/>
					<xsl:with-param name="sZeros" select="$sZeros"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<!--Other elements are considered as fidelity loss-->
				<!--Capturing fidility loss elements-->
				<xsl:if test="not(
						self::w:pPr
						| self::w:p
						| self::w:r
						| self::w:fldSimple
						| self::w:fldChar
						| self::w:proofErr
						| self::w:lastRenderedPageBreak
						| self::w:br
						| self::w:tab
					)">
					<xsl:message terminate="no">
						<xsl:value-of select="concat('translation.oox2Daisy.UncoveredElement|', name())"/>
					</xsl:message>
				</xsl:if>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="Paracharacterstyle">
		<xsl:param name="version" as="xs:string" select="''"/>
		<xsl:param name="pagination" as="xs:string" select="''"/>
		<xsl:param name="characterStyle" as="xs:boolean"/>
		<xsl:param name="flag" as="xs:string"/>
		<xsl:param name="txt" as="xs:string"/>
		<xsl:choose>
			<xsl:when test="$characterStyle">
				<xsl:choose>
					<xsl:when test="w:pPr/w:ind[@w:left] and w:pPr/w:ind[@w:right]">
						<xsl:variable name="val" as="xs:integer" select="w:pPr/w:ind/@w:left"/>
						<xsl:variable name="val_left" as="xs:integer" select="xs:integer(round($val div 1440))"/>
						<xsl:variable name="valright" as="xs:integer" select="w:pPr/w:ind/@w:right"/>
						<xsl:variable name="val_right" as="xs:integer" select="xs:integer(round($valright div 1440))"/>
						<span class="{concat('text-indent:', 'right=',$val_right,'in',';left=',$val_left,'in')}">
							<xsl:call-template name="ParaHandler">
								<xsl:with-param name="flag" select="'0'"/>
								<xsl:with-param name="version" select="$version"/>
								<xsl:with-param name="pagination" select="$pagination"/>
								<xsl:with-param name="txt" select="$txt"/>
								<xsl:with-param name="charparahandlerStyle" select="$characterStyle"/>
							</xsl:call-template>
						</span>
					</xsl:when>
					<xsl:when test="w:pPr/w:ind[@w:left] and    w:pPr/w:jc">
						<xsl:variable name="val" as="xs:integer" select="w:pPr/w:ind/@w:left"/>
						<xsl:variable name="val_left" as="xs:integer" select="xs:integer(round($val div 1440))"/>
						<xsl:variable name="val1" as="xs:string" select="w:pPr/w:jc/@w:val"/>
						<span class="{concat('text-indent:',';left=',$val_left,'in',';text-align:',$val1)}">
							<xsl:call-template name="ParaHandler">
								<xsl:with-param name="flag" select="'0'"/>
								<xsl:with-param name="version" select="$version"/>
								<xsl:with-param name="pagination" select="$pagination"/>
								<xsl:with-param name="txt" select="$txt"/>
								<xsl:with-param name="charparahandlerStyle" select="$characterStyle"/>
							</xsl:call-template>
						</span>
					</xsl:when>
					<xsl:when test="w:pPr/w:ind[@w:left]">
						<xsl:variable name="val" as="xs:integer" select="w:pPr/w:ind/@w:left"/>
						<xsl:variable name="val_left" as="xs:integer" select="xs:integer(round($val div 1440))"/>
						<span class="{concat('text-indent:',$val_left,'in')}">
							<xsl:call-template name="ParaHandler">
								<xsl:with-param name="flag" select="'0'"/>
								<xsl:with-param name="version" select="$version"/>
								<xsl:with-param name="pagination" select="$pagination"/>
								<xsl:with-param name="txt" select="$txt"/>
								<xsl:with-param name="charparahandlerStyle" select="$characterStyle"/>
							</xsl:call-template>
						</span>
					</xsl:when>
					<xsl:when test="w:pPr/w:ind[@w:right]">
						<xsl:variable name="val" as="xs:integer" select="w:pPr/w:ind/@w:right"/>
						<xsl:variable name="val_right" as="xs:integer" select="xs:integer(round($val div 1440))"/>
						<span class="{concat('text-indent:',$val_right,'in')}">
							<xsl:call-template name="ParaHandler">
								<xsl:with-param name="flag" select="'0'"/>
								<xsl:with-param name="version" select="$version"/>
								<xsl:with-param name="pagination" select="$pagination"/>
								<xsl:with-param name="txt" select="$txt"/>
								<xsl:with-param name="charparahandlerStyle" select="$characterStyle"/>
							</xsl:call-template>
						</span>
					</xsl:when>
					<xsl:when test="w:pPr/w:jc">
						<xsl:variable name="val" as="xs:string" select="w:pPr/w:jc/@w:val"/>
						<span class="{concat('text-align:',$val)}">
							<xsl:call-template name="ParaHandler">
								<xsl:with-param name="flag" select="'0'"/>
								<xsl:with-param name="version" select="$version"/>
								<xsl:with-param name="pagination" select="$pagination"/>
								<xsl:with-param name="txt" select="$txt"/>
								<xsl:with-param name="charparahandlerStyle" select="$characterStyle"/>
							</xsl:call-template>
						</span>
					</xsl:when>
					<xsl:otherwise>
						<xsl:call-template name="ParaHandler">
							<xsl:with-param name="flag" select="$flag"/>
							<xsl:with-param name="version" select="$version"/>
							<xsl:with-param name="pagination" select="$pagination"/>
							<xsl:with-param name="txt" select="$txt"/>
							<xsl:with-param name="charparahandlerStyle" select="$characterStyle"/>
						</xsl:call-template>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="ParaHandler">
					<xsl:with-param name="flag" select="$flag"/>
					<xsl:with-param name="version" select="$version"/>
					<xsl:with-param name="pagination" select="$pagination"/>
					<xsl:with-param name="txt" select="$txt"/>
					<xsl:with-param name="charparahandlerStyle" select="$characterStyle"/>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- Change the matter type based on style encountered during parsing paragraph (w:p) nodes-->
	<xsl:template name="SetCurrentMatterType">
		<xsl:if test="self::w:p">
			<xsl:choose>
				<xsl:when test="(
						count(w:pPr/w:pStyle[substring(@w:val,1,11)='Frontmatter'])=1
						or count(w:r/w:rPr/w:rStyle[substring(@w:val,1,11)='Frontmatter'])=1
					)">
					<xsl:if test="d:SetCurrentMatterType($myObj, 'Frontmatter')"/>
				</xsl:when>
				<xsl:when test="(
						count(w:pPr/w:pStyle[substring(@w:val,1,10)='Bodymatter'])=1
						or count(w:r/w:rPr/w:rStyle[substring(@w:val,1,10)='Bodymatter'])=1
					)">
					<xsl:if test="d:SetCurrentMatterType($myObj, 'Bodymatter')"/>
				</xsl:when>
				<xsl:when test="(
						count(w:pPr/w:pStyle[substring(@w:val,1,10)='Rearmatter'])=1
						or count(w:r/w:rPr/w:rStyle[substring(@w:val,1,10)='Rearmatter'])=1
					)">
					<xsl:if test="d:SetCurrentMatterType($myObj, 'Rearmatter')"/>
				</xsl:when>
			</xsl:choose>
		</xsl:if>
	</xsl:template>

</xsl:stylesheet>
