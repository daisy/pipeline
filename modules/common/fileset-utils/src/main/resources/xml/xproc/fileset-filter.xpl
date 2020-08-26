<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                exclude-inline-prefixes="px" xpath-version="2.0"
                type="px:fileset-filter" name="main">

    <p:input port="source" primary="true"/>
    <p:input port="source.in-memory" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The input fileset</p>
        </p:documentation>
        <p:empty/>
    </p:input>

    <p:output port="result" primary="true">
        <p:pipe step="result" port="result"/>
    </p:output>
    <p:output port="result.in-memory" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The filtered fileset</p>
            <p>The "result.in-memory" port contains all the documents from the "source.in-memory"
            port that are listed in the "result" fileset manifest.</p>
        </p:documentation>
        <p:pipe step="result.in-memory" port="result"/>
    </p:output>
    <p:output port="not-matched">
        <p:pipe step="not-matched" port="result"/>
    </p:output>
    <p:output port="not-matched.in-memory" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Files from "source" that are not included in "result", i.e. the files that were
            filtered out.</p>
            <p>The "not-matched.in-memory" port contains all the documents from the
            "source.in-memory" port that are listed in the "not-matched" fileset manifest.</p>
        </p:documentation>
        <p:pipe step="not-matched.in-memory" port="result"/>
    </p:output>

    <p:option name="href" select="''">
        <!-- href to the file in the fileset you want to retrieve. suppports the glob characters '*' and '?', i.e. "*.txt" or "application/*+xml". -->
    </p:option>
    <p:option name="media-types" select="''">
        <!-- space separated list of whitelisted media types. suppports the glob characters '*' and '?', i.e. "image/*" or "application/*+xml". -->
    </p:option>
    <p:option name="not-media-types" select="''">
        <!-- space separated list of blacklisted media types. suppports the glob characters '*' and '?', i.e. "image/*" or "application/*+xml". -->
    </p:option>

    <p:import href="fileset-filter-in-memory.xpl"/>
    <p:import href="fileset-load.xpl"/>
    <p:import href="fileset-diff.xpl"/>

    <p:choose>
        <p:when test="$href=''">
            <p:identity/>
        </p:when>
        <p:otherwise>
            <p:variable name="href-regex" select="concat('^',replace(replace(replace($href,'([\[\^\.\\\+\{\}\(\)\|\^\$\]])','\\$1'),'\?','.'),'\*','.*'),'$')"/>
            <p:delete>
                <p:with-option name="match"
                    select="concat(&quot;//d:file[not(matches('&quot;,$href-regex,&quot;','^\w+:/') and matches(resolve-uri(@href,base-uri(.)),'&quot;,$href-regex,&quot;') or matches(replace(concat('/',@href),'^/+','/'),'&quot;,$href-regex,&quot;') or @href='&quot;,replace($href,'&quot;&quot;','&amp;quot;'),&quot;')]&quot;)"
                />
            </p:delete>
        </p:otherwise>
    </p:choose>

    <p:choose>
        <p:when test="$media-types=''">
            <p:identity/>
        </p:when>
        <p:otherwise>
            <p:variable name="media-types-regexes" select="if ($media-types='') then '' else replace(replace(replace($media-types,'\+','\\+'),'\?','.'),'\*','.*')"/>
            <p:delete>
                <p:with-option name="match"
                    select="concat(&quot;//d:file[@media-type='' or not(some $media-type-regex in tokenize('&quot;,$media-types-regexes,&quot;','\s+')[not(.='')] satisfies matches(@media-type,$media-type-regex))]&quot;)"
                />
            </p:delete>
        </p:otherwise>
    </p:choose>

    <p:choose>
        <p:when test="$not-media-types=''">
            <p:identity/>
        </p:when>
        <p:otherwise>
            <p:variable name="not-media-types-regexes" select="if ($not-media-types='') then '' else replace(replace(replace($not-media-types,'\+','\\+'),'\?','.'),'\*','.*')"/>
            <p:delete>
                <p:with-option name="match"
                    select="concat(&quot;//d:file[not(@media-type='') and (some $not-media-type-regex in tokenize('&quot;,$not-media-types-regexes,&quot;','\s+')[not(.='')] satisfies matches(@media-type,$not-media-type-regex))]&quot;)"
                />
            </p:delete>
        </p:otherwise>
    </p:choose>
    <p:identity name="result"/>

    <px:fileset-filter-in-memory>
        <p:input port="source.in-memory">
            <p:pipe step="main" port="source.in-memory"/>
        </p:input>
    </px:fileset-filter-in-memory>
    <px:fileset-load>
        <!-- this will just pick documents, everything is already loaded -->
        <p:input port="in-memory">
            <p:pipe step="main" port="source.in-memory"/>
        </p:input>
    </px:fileset-load>
    <p:identity name="result.in-memory"/>
    <p:sink/>

    <px:fileset-diff name="not-matched">
        <p:input port="source">
            <p:pipe step="main" port="source"/>
        </p:input>
        <p:input port="secondary">
            <p:pipe step="result" port="result"/>
        </p:input>
    </px:fileset-diff>
    <px:fileset-filter-in-memory>
        <p:input port="source.in-memory">
            <p:pipe step="main" port="source.in-memory"/>
        </p:input>
    </px:fileset-filter-in-memory>
    <px:fileset-load>
        <!-- this will just pick documents, everything is already loaded -->
        <p:input port="in-memory">
            <p:pipe step="main" port="source.in-memory"/>
        </p:input>
    </px:fileset-load>
    <p:identity name="not-matched.in-memory"/>
    <p:sink/>

</p:declare-step>
