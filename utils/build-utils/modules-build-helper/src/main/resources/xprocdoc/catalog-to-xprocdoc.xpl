<p:declare-step version="1.0"
            xmlns:p="http://www.w3.org/ns/xproc"
            xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
            xmlns:catalog="urn:oasis:names:tc:entity:xmlns:xml:catalog"
            xmlns:xd="http://github.com/vojtechtoman/xprocdoc"
            type="px:catalog-to-xprocdoc"
            name="main"
            exclude-inline-prefixes="#all">
	
	<p:input port="source" sequence="false"/>
	<p:option name="input-base-uri" required="true"/>
	<p:option name="output-base-uri" required="true"/>
	
	<p:import href="xprocdoc.xpl"/>
	
	<p:for-each name="sources">
		<p:iteration-source select="/*/catalog:uri">
			<p:pipe step="main" port="source"/>
		</p:iteration-source>
		<p:choose>
			<p:when test="/*[ends-with(@uri,'.xpl')]">
				<p:load>
					<p:with-option name="href" select="/*/resolve-uri(@uri,base-uri(.))"/>
				</p:load>
				<p:add-attribute match="/*" attribute-name="px:public-name">
					<p:with-option name="attribute-value" select="/*/@name">
						<p:pipe step="sources" port="current"/>
					</p:with-option>
				</p:add-attribute>
			</p:when>
			<p:otherwise>
				<p:identity>
					<p:input port="source">
						<p:empty/>
					</p:input>
				</p:identity>
			</p:otherwise>
		</p:choose>
	</p:for-each>
	
	<xd:xprocdoc>
		<p:with-option name="input-base-uri" select="$input-base-uri">
			<p:empty/>
		</p:with-option>
		<p:with-option name="output-base-uri" select="$output-base-uri">
			<p:empty/>
		</p:with-option>
	</xd:xprocdoc>
	<p:sink/>
	
</p:declare-step>
