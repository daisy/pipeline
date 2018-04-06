<link rel="rdf:type" href="http://www.daisy.org/ns/pipeline/userdoc"/>
<meta property="dc:title" content="DTBook to ZedAI Development Notes"/>

<!-- 
summary: DTBook to ZedAI development notes
-->
# DTBook to ZedAI Development Notes

The DTBook2ZedAI module will convert one or more DTBook XML files to a single ZedAI file.

This module is written in XProc and XSLT. It relies on the DTBookUtils and MetadataUtils modules within the Pipeline 2.

## Steps

* Upgrade. Migrate a single DTBook file from version 1.1.0, 2005-1, or 2005-2 to 2005-3, using the upgrade-dtbook utility.
* If there are multiple input files, merge them into one DTBook document, using the merge-dtbook utility.
* Create external metadata records, using the metadata utilities.
* Transform DTBook 2005-3 to ZedAI. DTBook2ZedAI_TransformationRules lists each DTBook element and gives its ZedAI translation. The sub-steps are documented in the source code and include content model normalization as well as direct translation of elements and attributes.
* In ZedAI, visual attributes are expressed in a separate CSS file. The following DTBook attributes are extracted into a separate CSS file: height, width, align, valign, abbr, axis, headers, scope, rowspan, colspan, border, frame, cellspacing, cellpadding
* Copy all referenced material (e.g. images) to the output directory.
