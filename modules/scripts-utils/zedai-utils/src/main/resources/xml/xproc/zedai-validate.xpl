<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                exclude-inline-prefixes="#all"
                type="px:zedai-validate">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Validate a ZedAI (ANSI/NISO Z39.98-2012 Authoring and Interchange) document.</p>
		<p>Does not throw errors. Validation issues are reported through messages.</p>
	</p:documentation>

	<p:input port="source"/>
	<p:output port="result"/>

	<p:import href="http://www.daisy.org/pipeline/modules/validation-utils/library.xpl">
		<p:documentation>
			px:validate-with-relax-ng-and-report
		</p:documentation>
	</p:import>

	<px:validate-with-relax-ng-and-report assert-valid="false">
		<p:input port="schema">
			<p:document href="../schema/z3998-book-1.0-latest/z3998-book.rng"/>
		</p:input>
	</px:validate-with-relax-ng-and-report>

</p:declare-step>
