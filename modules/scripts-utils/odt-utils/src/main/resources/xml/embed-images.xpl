<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step version="1.0"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:odt="urn:oasis:names:tc:opendocument:xmlns:text:1.0"
                xmlns:draw="urn:oasis:names:tc:opendocument:xmlns:drawing:1.0"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                exclude-inline-prefixes="#all"
                type="odt:embed-images"
                name="embed-images">
	
	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Embed externally linked images inside the ODT package.</p>
	</p:documentation>
	
	<p:input port="fileset.in"/>
	<p:input port="in-memory.in" sequence="true"/>
	<p:input port="original-fileset"/>
	<p:output port="fileset.out">
		<p:pipe step="update" port="result.fileset"/>
	</p:output>
	<p:output port="in-memory.out" sequence="true">
		<p:pipe step="update" port="result.in-memory"/>
	</p:output>
	
	<p:import href="get-file.xpl"/>
	<p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
	<p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
	
	<p:variable name="base" select="//d:file[starts-with(@media-type,'application/vnd.oasis.opendocument')]/resolve-uri(@href, base-uri(.))">
		<p:pipe step="embed-images" port="fileset.in"/>
	</p:variable>
	<p:variable name="numbering-offset"
	            select="max((0, for $x in (//d:file/substring-after(resolve-uri(@href, base-uri(.)), $base))
	                                      [matches(., '^Pictures/img_([0-9]+)\.[^/\.]*$')]
	                              return number(replace($x, '^Pictures/img_([0-9]+)\.[^/\.]*$', '$1'))))">
		<p:pipe step="embed-images" port="fileset.in"/>
	</p:variable>
	
	<odt:get-file href="content.xml" name="content">
		<p:input port="fileset.in">
			<p:pipe step="embed-images" port="fileset.in"/>
		</p:input>
		<p:input port="in-memory.in">
			<p:pipe step="embed-images" port="in-memory.in"/>
		</p:input>
	</odt:get-file>
	
	<px:message severity="DEBUG" message="[odt-utils] embedding images"/>
	<p:sink/>
	
	<px:fileset-create name="base">
		<p:with-option name="base" select="$base"/>
	</px:fileset-create>
	<p:sink/>
	
	<p:for-each>
		<p:iteration-source select="//draw:image[not(starts-with(resolve-uri(@xlink:href, base-uri(.)), $base))]">
			<p:pipe step="content" port="result"/>
		</p:iteration-source>
		<p:variable name="original-href" select="/*/resolve-uri(@xlink:href, base-uri(.))"/>
		<px:fileset-add-entry>
			<p:input port="source">
				<p:pipe step="base" port="result"/>
			</p:input>
			<p:with-option name="href" select="concat(
			                                     'Pictures/img_',
			                                     number($numbering-offset) + p:iteration-position(),
			                                     replace($original-href, '^.*(\.[^/\.]*)$', '$1'))"/>
			<p:with-option name="original-href" select="$original-href"/>
			<p:with-option name="media-type" select="//d:file[resolve-uri((@original-href,@href)[1], base-uri(.))=$original-href]/@media-type">
				<p:pipe step="embed-images" port="original-fileset"/>
			</p:with-option>
		</px:fileset-add-entry>
	</p:for-each>
	
	<px:fileset-join name="fileset.images"/>
	
	<px:fileset-join name="fileset.with-images">
		<p:input port="source">
			<p:pipe step="embed-images" port="fileset.in"/>
			<p:pipe step="fileset.images" port="result"/>
		</p:input>
	</px:fileset-join>
	<p:sink/>
	
	<p:viewport match="//draw:image" name="content.new">
		<p:viewport-source>
			<p:pipe step="content" port="result"/>
		</p:viewport-source>
		<p:variable name="original-href" select="/*/resolve-uri(@xlink:href, base-uri(.))"/>
		<p:add-attribute match="/*" attribute-name="xlink:href">
			<p:with-option name="attribute-value"
			               select="substring-after(
			                       (if (starts-with($original-href, $base))
			                         then $original-href
			                         else //d:file[resolve-uri(@original-href,base-uri(.))=$original-href]/resolve-uri(@href,base-uri(.))),
			                       $base)">
				<p:pipe step="fileset.images" port="result"/>
			</p:with-option>
		</p:add-attribute>
	</p:viewport>
	
	<px:fileset-update name="update">
		<p:input port="update">
			<p:pipe step="content.new" port="result"/>
		</p:input>
		<p:input port="source.fileset">
			<p:pipe step="fileset.with-images" port="result"/>
		</p:input>
		<p:input port="source.in-memory">
			<p:pipe step="embed-images" port="in-memory.in"/>
		</p:input>
	</px:fileset-update>
	
</p:declare-step>
