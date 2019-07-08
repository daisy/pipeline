<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step 
  xmlns:p="http://www.w3.org/ns/xproc" 
  xmlns:c="http://www.w3.org/ns/xproc-step" 
  xmlns:cx="http://xmlcalabash.com/ns/extensions"
  xmlns:tr="http://transpect.io"
  xmlns:tr-internal="http://transpect.io/internal"
  name="unzip"
  type="tr:unzip"
  version="1.0">

  <p:documentation>Provides an interface to tr-internal:unzip that prevents users from accidentally deleting
    precious directories and that provides a hierarchized representation of the archive contents.
    Attempts to create the hierarchized representation directly in Java from the archive member list have failed
    in that it could not reliably be detected which members are empty directories and which are files.
    Therefore we have reverted to the original unzip step implementation, 
    https://github.com/transpect/unzip-extension/commit/5438d48, which is now rebranded as tr-internal:unzip.
    See the discussion/spec at https://github.com/transpect/unzip-extension/issues/7#issuecomment-248242296
  </p:documentation>

  <p:output port="result" primary="true">
    <p:documentation>/c:files[@xml:base]/c:file, where @xml:base is the destination directory. It should have 
    a trailing slash on @xml:base.</p:documentation>
    <p:pipe port="result" step="group"/>
  </p:output>
  <p:serialization port="result" omit-xml-declaration="false" indent="true"/>
  
  <p:output port="list-with-directories">
    <p:documentation>/c:files/c:directory/c:file</p:documentation>
    <p:pipe port="list-with-directories" step="group"/>
  </p:output>
  <p:serialization port="list-with-directories" omit-xml-declaration="false" indent="true"/>

  <p:option name="zip" required="true">
    <p:documentation>file:, http:, https: URI or OS name of a zip archive</p:documentation>
  </p:option>
  <p:option name="dest-dir" required="true">
    <p:documentation>file: URI or OS name of a directory that does not need to exist beforehand. The zip archive will be 
      unzipped to this directory.</p:documentation>
  </p:option>
  <p:option name="overwrite" select="'yes'">
    <p:documentation>Whether to overwrite an existing destination directory. An existing directory will be deleted.
    This is less dangerous if you use the default option safe='yes'.</p:documentation>
  </p:option>
  <p:option name="safe" select="'yes'">
    <p:documentation>Whether to create a new directory below the specified dest-dir, in order to avoid inadvertent 
      deletion of precious directories. If dest-dir ends in '.tmp', it is assumed that it may safely be overwritten
    in any case.</p:documentation>
  </p:option>

  <p:import href="internal-unzip-declaration.xpl"/>
  <p:import href="http://transpect.io/xproc-util/file-uri/xpl/file-uri.xpl"/>
  <p:import href="http://transpect.io/xproc-util/recursive-directory-list/xpl/recursive-directory-list.xpl"/>

  <tr:file-uri name="unsafe-dest-dir-uri" fetch-http="false">
    <p:with-option name="filename" select="$dest-dir"/>
  </tr:file-uri>

  <tr:file-uri name="zip-file-uri" fetch-http="true" make-unique="true">
    <p:with-option name="filename" select="$zip"/>
  </tr:file-uri>
  
  <p:group name="group">
    <p:output port="result" primary="true">
      <p:pipe port="result" step="fix-xml-base"/>
    </p:output>
    <p:output port="list-with-directories">
      <p:pipe port="result" step="recursive-dirlist"/>
    </p:output>
    <p:variable name="notdir" select="/*/@lastpath"/>
    <p:variable name="actual-dest-dir" select="if ($safe = ('no', 'false') or ends-with(/*/@os-path, '.tmp')) then /*/@os-path
                                               else concat(/*/@os-path, '/', replace($notdir, '[^a-z0-9._-]', '', 'i'), '.tmp')">
      <p:pipe port="result" step="unsafe-dest-dir-uri"/>
    </p:variable>
    <tr-internal:unzip name="internal-unzip">
      <p:with-option name="zip" select="/*/@os-path">
        <p:pipe port="result" step="zip-file-uri"/>
      </p:with-option>
      <p:with-option name="dest-dir" select="$actual-dest-dir"/>
      <p:with-option name="overwrite" select="$overwrite"/>
    </tr-internal:unzip>
    <p:add-attribute name="fix-xml-base" match="/*" attribute-name="xml:base">
      <p:documentation>Sometimes the trailing slash was not included, but we need to make sure it’s there.</p:documentation>
      <p:with-option name="attribute-value" select="replace(/*/@xml:base, '([^/])$', '$1/')"/>
    </p:add-attribute>
    <tr:recursive-directory-list name="recursive-dirlist">
      <p:with-option name="path" select="/*/@xml:base"/>
    </tr:recursive-directory-list>
    <p:sink/>
  </p:group>

</p:declare-step>
