<pattern id="package-doc-common" xmlns="http://purl.oclc.org/dsdl/schematron">
	<let name="uniqueid" value="//pkg:package/@unique-identifier"/> 
	
    <rule context="//pkg:package/pkg:metadata/pkg:dc-metadata">
        <assert test="dc:Identifier[@id=$uniqueid]">Value of unique-identifier attribute on package element does not match id value of any dc:Identifier.</assert>   					
    </rule>
    
    <rule context="//pkg:package/pkg:metadata/pkg:x-metadata">
        <assert test="count(pkg:meta[@name='dtb:sourcePublisher'])&lt;2">x-metadata element dtb:sourcePublisher occured more than one time.</assert>
        
        <assert test="count(pkg:meta[@name='dtb:sourceRights'])&lt;2">x-metadata element dtb:sourceRights occured more than one time.</assert>
        
        <assert test="count(pkg:meta[@name='dtb:sourceTitle'])&lt;2">x-metadata element dtb:sourceTitle occured more than one time.</assert>
        
    	<!-- DAISY requires this, not NIMAS. We follow NIMAS here. -->
        <!--<assert test="count(pkg:meta[@name='dtb:multimediaType'])=1">dtb:multimediaType is missing or duplicated in x-metadata.</assert>-->
        
        <!--<assert test="count(pkg:meta[@name='dtb:multimediaContent'])=1">dtb:multimediaContent is missing or duplicated in x-metadata.</assert>-->
        
        <assert test="count(pkg:meta[@name='dtb:producedDate'])&lt;2">x-metadata element dtb:producedDate occured more than one time.</assert>
        
        <assert test="count(pkg:meta[@name='dtb:revision'])&lt;2">x-metadata element dtb:revision occured more than one time.</assert>
        
        <assert test="count(pkg:meta[@name='dtb:revisionDate'])&lt;2">x-metadata element dtb:revisionDate occured more than one time.</assert>
        
        <assert test="count(pkg:meta[@name='dtb:revisionDescription'])&lt;2">x-metadata element dtb:revisionDescription occured more than one time.</assert>
    	<!-- DAISY requires this, not NIMAS. We follow NIMAS here. -->
        <!--<assert test="count(pkg:meta[@name='dtb:totalTime'])=1">dtb:totalTime is missing or duplicated in x-metadata.</assert>-->
    </rule>
    
    <rule context="//pkg:package/pkg:manifest">
        <report test="count(pkg:item[@media-type = 'application/x-dtbook+xml']) = 0"> 
            At least one document with media-type equal to 'application/x-dtbook+xml' is required in the manifest.
        </report>
    	
    	<!-- NIMAS doesn't require an NCX -->
    	<!--<assert test="count(pkg:item[@id='ncx' and @media-type='application/x-dtbncx+xml'])=1"> 
    		NCX manifest item in opf must have id="ncx".
		</assert>  
    	-->
    	<!-- NIMAS does not require the inclusion of SMIL files -->
    	<!--<assert test="count(pkg:item[@media-type='application/smil'])>0"> 
    		No SMIL file found.
    	</assert> -->
    	
    	<!-- NIMAS OPF does not require the package file to reference itself -->
    	<!--<assert test="count(pkg:item[@media-type='text/xml'])=1"> 
    		Zero or several package files listed in manifest.
    	</assert>--> 
    	
    	<!-- NIMAS OPF does not reference an NCX -->
    	<!--<assert test="count(pkg:item[@media-type='application/x-dtbncx+xml'])=1"> 
    		Zero or several NCX are listed in manifest.
    	</assert>--> 
    	
    	<assert test="count(pkg:item[@media-type='application/x-dtbresource+xml'])&lt;2"> 
    		Several resource files are listed in manifest.
    	</assert>  
    </rule>
    
	<rule context="//pkg:package/pkg:manifest/pkg:item[@media-type='application/x-dtbresource+xml']">
		<assert test="@id='resource'"> 
			Resource manifest item in opf must have id="resource".
		</assert>   					
	</rule>	
	
	<rule context="//pkg:package/pkg:manifest/pkg:item[@media-type='application/smil']">
		<assert test="//pkg:package/pkg:spine/pkg:itemref[@idref=current()/@id]"> 
			SMIL file in manifest not referenced in spine.
		</assert>   					
	</rule>	
	
	<!-- NIMAS doesn't care about SMIL -->
	<!--<rule context="//pkg:package/pkg:spine/pkg:itemref">
		<assert test="//pkg:item[@id=current()/@idref and @media-type='application/smil']"> 
			Manifest item referenced by itemref in spine is not a SMIL file.
		</assert>   					
	</rule>
-->
	<rule context="//pkg:package/pkg:metadata/pkg:x-metadata/pkg:meta[@name='dtb:multimediaContent' and contains(@content,'audio')]">
		<assert test="count(//pkg:package/pkg:manifest/pkg:item[@media-type='audio/mpeg4-generic' or @media-type='audio/mpeg' or @media-type='audio/x-wav' ])&gt;0"> 
			dtb:multimediaContent value 'audio' does not correspond to manifest.
		</assert>   					
	</rule>
	
	<rule context="//pkg:package/pkg:metadata/pkg:x-metadata/pkg:meta[@name='dtb:multimediaContent' and contains(@content,'image')]">
		<assert test="count(//pkg:package/pkg:manifest/pkg:item[@media-type='image/jpeg' or @media-type='image/png' or @media-type='image/svg+xml' ])&gt;0"> 
			dtb:multimediaContent value 'image' does not correspond to manifest.
		</assert>   					
	</rule>
	
	<rule context="//pkg:package/pkg:metadata/pkg:x-metadata/pkg:meta[@name='dtb:multimediaContent' and contains(@content,'text')]">
		<assert test="count(//pkg:package/pkg:manifest/pkg:item[@media-type='application/x-dtbook+xml'])&gt;0"> 
			dtb:multimediaContent value 'text' does not correspond to manifest.
		</assert>   					
	</rule>
	
	<!-- NIMAS additions -->
	<rule context="//pkg:package/pkg:metadata/pkg:dc-metadata">
		<assert test="count(dc:Format) >= 1">dc:Format metadata is required by NIMAS.</assert>
		<assert test="count(dc:Rights) >= 1">dc:Rights metadata is required by NIMAS.</assert>
		<assert test="count(dc:Source) >= 1">dc:Source metadata is required by NIMAS.</assert>
	</rule>
	
	<rule context="//pkg:package/pkg:metadata/pkg:x-metadata">
		<assert test="count(pkg:meta[@name = 'nimas-SourceEdition']) >= 1">nimas-SourceEdition metadata is required by NIMAS.</assert>
		<assert test="count(pkg:meta[@name = 'nimas-SourceDate']) >= 1">nimas-SourceDate metadata is required by NIMAS.</assert>
	</rule>
	
	<rule context="//pkg:package/pkg:manifest">
		<report test="count(pkg:item[@media-type = 'application/pdf']) = 0"> 
			NIMAS requires at least one document with media-type equal to 'application/pdf' in the manifest.
		</report>
	</rule>
	
	<rule context="//pkg:package/pkg:spine/pkg:itemref">
		<assert test="//pkg:item[@id=current()/@idref and @media-type='application/x-dtbook+xml']"> 
			Manifest item referenced by itemref in spine is not a DTBook file.
		</assert>   					
	</rule>
	
</pattern>
