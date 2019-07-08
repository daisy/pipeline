<?xml version="1.0"?>
<p:declare-step 
  xmlns:p="http://www.w3.org/ns/xproc" 
  xmlns:c="http://www.w3.org/ns/xproc-step"
  xmlns:tr-internal="http://transpect.io/internal" 
  version="1.0" 
  type="tr-internal:unzip">
  <p:documentation>Will extract a complete zip file or a single contained file to a specified destination
    folder.</p:documentation>
  <p:option name="zip" required="true">
    <p:documentation>The operating system’s file path (not a URL) to the zip file. On Windows, backward slashes should work, but
      it’s safer to use forward slashes for path separators. Not sure whether spaces are ok though. There also may be encoding
      issues related to OS specific encodings of file names that are not UTF-8 compliant.</p:documentation>
  </p:option>
  <p:option name="dest-dir" required="true">
    <p:documentation>An OS file system path, not a URL. Will be created if it does not exist (subject to the $overwrite
      option).</p:documentation>
  </p:option>
  <p:option name="overwrite" required="false" select="'no'">
    <p:documentation>Whether existing directories and files will be overwritten. The step will fail if set to 'no' and there is
      an existing file/dir with the target name.</p:documentation>
  </p:option>
  <p:option name="file" required="false">
    <p:documentation>Optionally, a specific relative path to a file within the zip file. Will be restored to its relative path
      below $dest-dir.</p:documentation>
  </p:option>
  <p:output port="result" primary="true">
    <p:documentation>A c:files document with the extracted files, as c:file elements (no hierarchy). There is an @xml:base
      attribute on /c:files that contains the $dest-dir location URL. A c:file element will contain a @name attribute with the
      corresponding file’s relative location.</p:documentation>
  </p:output>
</p:declare-step>
