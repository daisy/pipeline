<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step version="1.0" type="px:fileset-filter" name="main" xmlns:p="http://www.w3.org/ns/xproc" xmlns:d="http://www.daisy.org/ns/pipeline/data"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc" xmlns:c="http://www.w3.org/ns/xproc-step" exclude-inline-prefixes="px" xpath-version="2.0">

    <p:input port="source"/>
    <p:output port="result"/>

    <p:option name="href" select="''">
        <!-- href to the file in the fileset you want to retrieve. suppports the glob characters '*' and '?', i.e. "*.txt" or "application/*+xml". -->
    </p:option>
    <p:option name="media-types" select="''">
        <!-- space separated list of whitelisted media types. suppports the glob characters '*' and '?', i.e. "image/*" or "application/*+xml". -->
    </p:option>
    <p:option name="not-media-types" select="''">
        <!-- space separated list of blacklisted media types. suppports the glob characters '*' and '?', i.e. "image/*" or "application/*+xml". -->
    </p:option>

    <p:import href="fileset-library.xpl"/>

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
                    select="concat(&quot;//d:file[@media-type='' or not(some $media-type-regex in tokenize('&quot;,$media-types-regexes,&quot;',' ') satisfies matches(@media-type,$media-type-regex))]&quot;)"
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
                    select="concat(&quot;//d:file[not(@media-type='') and (some $not-media-type-regex in tokenize('&quot;,$not-media-types-regexes,&quot;',' ') satisfies matches(@media-type,$not-media-type-regex))]&quot;)"
                />
            </p:delete>
        </p:otherwise>
    </p:choose>

</p:declare-step>
