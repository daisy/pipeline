<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                exclude-inline-prefixes="#all"
                type="px:fileset-delete">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Delete files that were marked for removal by px:fileset-move.</p>
	</p:documentation>

	<p:input port="source">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>A d:fileset document that was returned on the "mapping" port of px:fileset-move.</p>
		</p:documentation>
	</p:input>

	<p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
		<p:documentation>
			px:delete
		</p:documentation>
	</p:import>

	<p:for-each>
		<p:iteration-source select="//d:file[@original-href and @to-delete]"/>
		<p:variable name="href" select="/*/@original-href"/>
		<p:choose>
			<p:when test="not(contains($href,'!/'))">
				<px:delete px:message="Deleting {$href}" px:message-severity="DEBUG">
					<p:with-option name="href" select="$href"/>
				</px:delete>
			</p:when>
			<p:otherwise>
				<p:sink px:message="Not deleting {$href}" px:message-severity="DEBUG"/>
			</p:otherwise>
		</p:choose>
	</p:for-each>

</p:declare-step>
