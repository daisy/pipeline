<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:dp2="http://www.daisy.org/ns/pipeline/"
                xmlns:obfl="http://www.daisy.org/ns/2011/obfl"
                type="px:obfl-to-pef" name="main">

	<p:input port="source" sequence="false"/>
	<p:output port="result" sequence="false">
		<p:pipe step="pef" port="result"/>
	</p:output>
	<p:option name="identifier" required="false" select="''"/>
	<p:input port="parameters" kind="parameter" primary="false"/>

	<p:import href="http://www.daisy.org/pipeline/modules/braille/common-utils/library.xpl">
		<p:documentation>
			px:add-parameters
			px:merge-parameters
		</p:documentation>
	</p:import>

	<p:declare-step type="pxi:obfl-to-pef">
		<p:input port="source" sequence="false"/>
		<p:output port="result" sequence="false"/>
		<p:option name="locale" required="true"/>
		<p:option name="mode" required="true"/>
		<p:option name="braille-charset" required="true"/>
		<p:option name="identifier" required="false" select="''"/>
		<p:option name="style-type" required="false" select="''"/>
		<p:option name="css-text-transform-definitions" required="false" select="''"/>
		<p:option name="css-counter-style-definitions" required="false" select="''"/>
		<p:input port="parameters" kind="parameter" primary="false"/>
		<!--
		    Implemented in ../../java/org/daisy/pipeline/braille/dotify/calabash/impl/OBFLToPEFStep.java
		-->
	</p:declare-step>

	<p:add-attribute match="*[@translate='pre-translated-text-css']"
	                 attribute-name="translate"
	                 attribute-value="(input:braille)(input:text-css)(output:braille)"/>

	<p:choose>
		<p:when test="exists(/obfl:obfl/obfl:meta/dp2:braille-charset)">
			<p:label-elements match="*[@translate[not(.='')]]"
			                  attribute="translate"
			                  label="concat(@translate,'(braille-charset:&quot;',/obfl:obfl/obfl:meta/dp2:braille-charset,'&quot;)')"/>
		</p:when>
		<p:otherwise>
			<p:identity/>
		</p:otherwise>
	</p:choose>

	<p:delete match="/obfl:obfl/obfl:meta/dp2:style-type">
		<!-- We don't want this to end up in the PEF. We assume that the value is "text/css" and
		     that this is also what translators understand, meaning that $mode should contain
		     "(input:text-css)". -->
	</p:delete>
	<p:delete match="/obfl:obfl/obfl:meta/dp2:default-mode">
		<!-- We don't want this to end up in the PEF. -->
	</p:delete>
	<p:delete match="/obfl:obfl/obfl:meta/dp2:css-text-transform-definitions">
		<!-- We don't want this to end up in the PEF. -->
	</p:delete>
	<p:delete match="/obfl:obfl/obfl:meta/dp2:css-counter-style-definitions">
		<!-- We don't want this to end up in the PEF. -->
	</p:delete>
	<p:delete match="/obfl:obfl/obfl:meta/dp2:braille-charset">
		<!-- We don't want this to end up in the PEF. Another field "dp2:ascii-braille-charset"
		     will be added through the "braille-charset" option. -->
	</p:delete>

	<pxi:obfl-to-pef name="pef">
		<p:with-option name="locale" select="(/obfl:obfl/@xml:lang,'und')[1]"/>
		<p:with-option name="mode" select="/obfl:obfl/obfl:meta/dp2:default-mode">
			<p:pipe step="main" port="source"/>
		</p:with-option>
		<p:with-option name="braille-charset" select="/obfl:obfl/obfl:meta/dp2:braille-charset">
			<p:pipe step="main" port="source"/>
		</p:with-option>
		<p:with-option name="identifier" select="$identifier"/>
		<p:with-option name="style-type" select="/obfl:obfl/obfl:meta/dp2:style-type[1]">
			<p:pipe step="main" port="source"/>
		</p:with-option>
		<p:with-option name="css-text-transform-definitions" select="/obfl:obfl/obfl:meta/dp2:css-text-transform-definitions">
			<p:pipe step="main" port="source"/>
		</p:with-option>
		<p:with-option name="css-counter-style-definitions" select="/obfl:obfl/obfl:meta/dp2:css-counter-style-definitions">
			<p:pipe step="main" port="source"/>
		</p:with-option>
		<p:input port="parameters">
			<p:pipe step="parameters" port="result"/>
		</p:input>
	</pxi:obfl-to-pef>
	<p:sink/>

	<p:group name="parameters">
		<p:output port="result"/>
		<!--
		    Follow the OBFL standard which says that "when volume-transition is present, the last page
		    or sheet in each volume may be modified so that the volume break occurs earlier than usual:
		    preferably between two blocks, or if that is not possible, between words"
		    (http://braillespecs.github.io/obfl/obfl-specification.html#L8701). In other words, volumes
		    should by default not be allowed to end on a hyphen.
		-->
		<px:add-parameters name="allow-ending-volume-on-hyphen">
			<p:input port="source">
				<p:empty/>
			</p:input>
			<p:with-param name="allow-ending-volume-on-hyphen"
			              select="if (/*/obfl:volume-transition) then 'false' else 'true'">
				<p:pipe step="main" port="source"/>
			</p:with-param>
		</px:add-parameters>
		<px:merge-parameters>
			<p:input port="source">
				<p:pipe step="allow-ending-volume-on-hyphen" port="result"/>
				<p:pipe step="main" port="parameters"/>
			</p:input>
		</px:merge-parameters>
	</p:group>

</p:declare-step>
