<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:audio-rearrange"
                name="main">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Rearrange audio in files</p>
	</p:documentation>

	<p:input port="source" primary="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The source audio fileset</p>
			<p><code>d:fileset</code> document listing all the source audio files. Audio file types
			should be specified in <code>@media-type</code> attributes. Files that do not contain
			any audio needed to construct the desired audio fileset are ignored.</p>
		</p:documentation>
	</p:input>

	<p:input port="desired">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The desired audio fileset</p>
			<p><code>d:fileset</code> document with nested <code>d:clip</code> elements that maps
			audio clips in the source (<code>@original-href, @original-clipBegin</code> and
			<code>@original-clipEnd</code>) to audio clips in the result (<code>@href</code>,
			<code>@clipBegin</code> and <code>@clipEnd</code>). The desired audio file types should
			be specified in <code>@media-type</code> attributes. Source audio clips should have
			their file listed in the source audio fileset.</p>
		</p:documentation>
	</p:input>

	<p:option name="temp-dir">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Empty directory dedicated to this step. If set, the directory will be used to store
			audio files. If not set, the audio files will be stored in temporary directory that is
			automatically created.</p>
		</p:documentation>
	</p:option>

	<p:output port="result" primary="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The result audio fileset</p>
			<p><code>d:fileset</code> document listing the resulting audio files.</p>
		</p:documentation>
	</p:output>

	<p:output port="temp-files">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Temporary files. May be deleted when the result fileset is stored.</p>
		</p:documentation>
	</p:output>

	<!--
	    Implemented in ../../../java/org/daisy/pipeline/audio/calabash/impl/AudioRearrangeStep.java
	-->

</p:declare-step>
