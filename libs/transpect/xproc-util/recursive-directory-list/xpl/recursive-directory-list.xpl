<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
  xmlns:cxf="http://xmlcalabash.com/ns/extensions/fileutils"
  xmlns:cx="http://xmlcalabash.com/ns/extensions"
  xmlns:c="http://www.w3.org/ns/xproc-step"
  xmlns:tr="http://transpect.io"
  type="tr:recursive-directory-list">
  
  <p:documentation xmlns="http://www.w3.org/1999/xhtml">
    <p>Copied from <a href="http://xproc.org/library/">http://xproc.org/library/recursive-directory-list.xpl</a></p>
    <p>Copyright situation unclear.</p>
    <p>Changed the namespace prefix from <code>l</code> to <code>tr</code> (and the namespaces accordingly).</p>
    <p>Prepended a <code>cxf:info</code> step because the step would fail sometimes with Calabash 1.1.4 even if there was a try/catch around it.</p>
    <p>In addition, in order to deal with a similar error, replaced <code>p:value-available()</code> with default values (empty strings) for include-filter and exclude-filter.</p>
  </p:documentation>

  <p:output port="result"/>
  <p:option name="path" required="true"/>
  <p:option name="include-filter" select="''"/>
  <p:option name="exclude-filter" select="''"/>
  <p:option name="depth" select="-1"/>

  <p:import href="http://xmlcalabash.com/extension/steps/library-1.0.xpl"/>

  <cxf:info fail-on-error="false">
    <p:with-option name="href" select="$path"/>
  </cxf:info>

  <p:choose>
    <p:when test="/c:directory">
<!--<cx:message>
<p:with-option name="message" select="/*/@*"/>
</cx:message>-->
      <p:choose>
        <p:when test="not($include-filter = '')
                      and not($exclude-filter = '')">
          <p:directory-list>
            <p:with-option name="path" select="$path"/>
            <p:with-option name="include-filter" select="$include-filter"/>
            <p:with-option name="exclude-filter" select="$exclude-filter"/>
          </p:directory-list>
        </p:when>
        <p:when test="not($include-filter = '')">
          <p:directory-list>
            <p:with-option name="path" select="$path"/>
            <p:with-option name="include-filter" select="$include-filter"/>
          </p:directory-list>
        </p:when>
        <p:when test="not($exclude-filter = '')">
          <p:directory-list>
            <p:with-option name="path" select="$path"/>
            <p:with-option name="exclude-filter" select="$exclude-filter"/>
          </p:directory-list>
        </p:when>
        <p:otherwise>
          <p:directory-list>
            <p:with-option name="path" select="$path"/>
          </p:directory-list>
        </p:otherwise>
      </p:choose>

      <p:viewport match="/c:directory/c:directory">
        <p:variable name="name" select="encode-for-uri(/*/@name)"/>
        <p:choose>
          <p:when test="$depth != 0">
            <p:choose>
              <p:when test="not($include-filter = '')
                            and not($exclude-filter = '')">
                <tr:recursive-directory-list>
                  <p:with-option name="path" select="concat($path,'/'[not(ends-with($path, '/'))],$name)"/>
                  <p:with-option name="include-filter" select="$include-filter"/>
                  <p:with-option name="exclude-filter" select="$exclude-filter"/>
                  <p:with-option name="depth" select="$depth - 1"/>
                </tr:recursive-directory-list>
              </p:when>

              <p:when test="not($include-filter = '')">
                <tr:recursive-directory-list>
                  <p:with-option name="path" select="concat($path,'/'[not(ends-with($path, '/'))],$name)"/>
                  <p:with-option name="include-filter" select="$include-filter"/>
                  <p:with-option name="depth" select="$depth - 1"/>
                </tr:recursive-directory-list>
              </p:when>

              <p:when test="not($exclude-filter = '')">
<!-- <cx:message> -->
<!-- <p:with-option name="message" select="'DDDD', concat($path,'/'[not(ends-with($path, '/'))],$name)"/> -->
<!-- </cx:message> -->
                <tr:recursive-directory-list>
                  <p:with-option name="path" select="concat($path,'/'[not(ends-with($path, '/'))],$name)"/>
                  <p:with-option name="exclude-filter" select="$exclude-filter"/>
                  <p:with-option name="depth" select="$depth - 1"/>
                </tr:recursive-directory-list>
              </p:when>

              <p:otherwise>
                <tr:recursive-directory-list>
                  <p:with-option name="path" select="concat($path,'/'[not(ends-with($path, '/'))],$name)"/>
                  <p:with-option name="depth" select="$depth - 1"/>
                </tr:recursive-directory-list>
              </p:otherwise>
            </p:choose>
          </p:when>
          <p:otherwise>
            <p:identity/>
          </p:otherwise>
        </p:choose>
      </p:viewport>
    </p:when>

    <p:otherwise>
      <p:string-replace match="href" >
        <p:with-option name="replace" select="concat('''', $path, '''')"/>
        <p:input port="source">
          <p:inline>
            <c:error code="l:NODIR">
              <href/>
            </c:error>
          </p:inline>
        </p:input>
      </p:string-replace>
    </p:otherwise>
  </p:choose>

</p:declare-step>
