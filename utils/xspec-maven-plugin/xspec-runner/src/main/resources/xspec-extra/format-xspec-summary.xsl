<?xml version='1.0' encoding='utf-8'?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:x="http://www.jenitennison.com/xslt/xspec"
                exclude-result-prefixes="#all">
	
	<xsl:param name="report-dir" as="xs:string" required="yes"/>
	<xsl:param name="test-names" as="xs:string*" required="yes"/>
	
	<xsl:template name="main">
		<xsl:variable name="sorted_test_names" as="xs:string*">
			<xsl:perform-sort select="$test-names">
				<xsl:sort select="."/>
			</xsl:perform-sort>
		</xsl:variable>
		<xsl:variable name="rows" as="element()*">
			<xsl:for-each select="$sorted_test_names">
				<xsl:variable name="name" as="xs:string" select="."/>
				<xsl:variable name="report-uri" as="xs:string"
				              select="concat($report-dir, '/XSPEC-', $name, '.xml')"/>
				<xsl:choose>
					<xsl:when test="doc-available($report-uri)">
						<xsl:variable name="report" select="doc($report-uri)/x:report"/>
						<xsl:variable name="pending" as="xs:integer"
						              select="count($report/x:scenario/x:test[@pending])"/>
						<xsl:variable name="passed" as="xs:integer"
						              select="count($report/x:scenario/x:test[not(@pending) and @successful='true'])"/>
						<xsl:variable name="failed" as="xs:integer"
						              select="count($report/x:scenario/x:test[not(@pending) and @successful='false'])"/>
						<xsl:variable name="total" as="xs:integer" select="count($report/x:scenario/x:test)"/>
						<tr class="{ if ($failed &gt; 0) then 'failed'
						             else if ($pending &gt; 0) then 'pending'
						             else 'successful' }">
							<th>
								<a href="{concat('HTML-', $name, '.html')}">
									<xsl:value-of select="$name"/>
								</a>
							</th>
							<th>
								<span>
									<xsl:value-of select="$passed"/>
								</span>
								<xsl:text>/</xsl:text>
								<span>
									<xsl:value-of select="$pending"/>
								</span>
								<xsl:text>/</xsl:text>
								<span>
									<xsl:value-of select="$failed"/>
								</span>
								<xsl:text>/</xsl:text>
								<span>
									<xsl:value-of select="$total"/>
								</span>
							</th>
						</tr>
					</xsl:when>
					<xsl:otherwise>
						<tr class="error">
							<th>
								<xsl:value-of select="$name"/>
							</th>
							<th> ERROR </th>
						</tr>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="passed" select="sum($rows/th[2]/span[1]/number(.))"/>
		<xsl:variable name="pending" select="sum($rows/th[2]/span[2]/number(.))"/>
		<xsl:variable name="failed" select="sum($rows/th[2]/span[3]/number(.))"/>
		<xsl:variable name="total" select="sum($rows/th[2]/span[4]/number(.))"/>
		<xsl:variable name="passed_pending_failed_total" as="xs:string"
		              select="concat($passed, '/', $pending, '/', $failed, '/', $total)"/>
		<html>
			<head>
				<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
				<title>
					Test Summary
					<xsl:value-of select="concat('(', $passed_pending_failed_total, ')')"/>
				</title>
				<link rel="stylesheet" type="text/css" href="xspec-report.css"/>
			</head>
			<body>
				<h1>
					Test Summary
					<span style="position:absolute; right:15">
						<xsl:value-of select="$passed_pending_failed_total"/>
					</span>
				</h1>
				<table class="xspec">
					<colgroup>
						<col width="85%"/>
						<col width="15%"/>
					</colgroup>
					<thead>
						<tr>
							<th style="text-align: right; font-weight: normal;">
								passed/pending/failed/total
							</th>
							<th>
								<xsl:value-of select="$passed_pending_failed_total"/>
							</th>
						</tr>
					</thead>
					<tbody>
						<xsl:sequence select="$rows"/>
					</tbody>
				</table>
			</body>
		</html>
	</xsl:template>
	
</xsl:stylesheet>
