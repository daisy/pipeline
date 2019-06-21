<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" exclude-result-prefixes="#all" xmlns:x="http://www.daisy.org/ns/xprocspec" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:p="http://www.w3.org/ns/xproc"
    xmlns:xs="http://www.w3.org/2001/XMLSchema">

    <!--
        http://stackoverflow.com/a/9691131/281065
        https://svn.jenkins-ci.org/trunk/hudson/dtkit/dtkit-format/dtkit-junit-model/src/main/resources/com/thalesgroup/dtkit/junit/model/xsd/junit-4.xsd
    -->

    <xsl:param name="start-time" required="yes"/>
    <xsl:param name="end-time" required="yes"/>

    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>

    <xsl:template match="/*">
        <xsl:variable name="description" select=".[not(x:scenario/@pending)]"/>
        <xsl:variable name="pending-description" select=".[x:scenario/@pending]"/>
        <xsl:variable name="declaration" select="$description/x:step-declaration/*"/>
        <xsl:variable name="scenario" select="$description/x:scenario"/>
        <xsl:variable name="errors" select="self::c:errors"/>
        <xsl:variable name="tests" select="$description/x:test-result"/>

        <xsl:variable name="test-base-uri" select="string(@test-base-uri)"/>
        <xsl:variable name="script" select="distinct-values($description/@script)"/>
        <xsl:variable name="script-short" select="replace($script,'^.*/','')"/>
        <xsl:variable name="passed" select="count($tests[@result='passed'])"/>
        <xsl:variable name="pending" select="count($tests[@result='skipped'] | $pending-description)"/>
        <xsl:variable name="failed" select="count($tests[@result='failed'])"/>
        <xsl:variable name="error-count" select="count($errors)"/>
        <xsl:variable name="total" select="count($tests | $pending-description) + $error-count"/>

        <testsuite timestamp="{x:now()}" hostname="localhost" tests="{$total}" errors="{$error-count}" failures="{$failed}" skipped="{$pending}" id="{generate-id()}"
            temp-global-duration="{x:duration($start-time,$end-time)}" temp-global-name="{replace(replace(@test-base-uri,'\.xprocspec$','','i'),'^.*/([^/]*)$','$1')}">
            <!-- the @disabled attribute is not used (don't know what it means as opposed to @skipped...) -->

            <xsl:choose>
                <xsl:when test="$errors">
                    <xsl:choose>
                        <xsl:when test="$errors/@scenario-label">
                            <xsl:attribute name="name" select="$errors/@scenario-label"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:attribute name="name" select="'compilationError'"/>
                            <xsl:attribute name="package" select="'org.daisy.xprocspec'"/>
                        </xsl:otherwise>
                    </xsl:choose>

                    <properties>
                        <xsl:for-each select="@*">
                            <property name="{name()}" value="{.}"/>
                        </xsl:for-each>
                    </properties>

                    <!-- static error -->
                    <error message="{@error-location}" type="static"/>
                    <system-out>
                        <xsl:value-of select="concat(replace(@test-base-uri,'^.*/([^/]*)$','$1'),': ERROR')"/>
                    </system-out>
                    <xsl:for-each select="c:error">
                        <system-err>
                            <xsl:if test="@line">
                                <xsl:value-of select="concat('line: ',@line)"/>
                                <xsl:text>
</xsl:text>
                            </xsl:if>
                            <xsl:if test="@column">
                                <xsl:value-of select="concat('column: ',@column)"/>
                                <xsl:text>
</xsl:text>
                            </xsl:if>
                            <xsl:copy-of select="./text()"/>
                        </system-err>
                    </xsl:for-each>
                </xsl:when>

                <xsl:otherwise>
                    <xsl:choose>
                        <xsl:when test="$pending-description">
                            <xsl:attribute name="name" select="$pending-description/x:scenario/@label"/>
                            <xsl:if test="$pending-description/x:scenario[@start-time and @end-time]">
                                <xsl:attribute name="time" select="x:duration($pending-description/x:scenario/@start-time,$pending-description/x:scenario/@end-time)"/>
                            </xsl:if>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:attribute name="name" select="$scenario/@label"/>
                            <xsl:attribute name="package" select="replace($declaration/@x:type,'^\{(.*)\}.*','$1')"/>
                            <xsl:if test="$scenario[@start-time and @end-time]">
                                <xsl:attribute name="time" select="x:duration($scenario/@start-time,$scenario/@end-time)"/>
                            </xsl:if>
                        </xsl:otherwise>
                    </xsl:choose>

                    <xsl:text>
</xsl:text>
                    <properties>
                        <xsl:if test="$scenario/x:call/x:input">
                            <xsl:variable name="include-xml-base" select="if ($scenario/x:call/x:input/x:document/@xml:base) then true() else false()"/>
                            <xsl:for-each select="$scenario/x:call/x:input">
                                <property name="input: {@port}">
                                    <xsl:attribute name="value">
                                        <xsl:for-each select="*[position() &lt; 5 and *]">
                                            <xsl:value-of
                                                select="concat(
                                                '&lt;',*[1]/local-name(),' xmlns=&quot;',*[1]/namespace-uri(),'&quot;',
                                                if (.[@*]) then ' ...' else '',
                                                if (*[1]/node()) then concat('&gt;...&lt;/',*[1]/local-name(),'&gt;') else '/&gt;',
                                                if ($include-xml-base) then concat(' (Base URI: ',@xml:base,')') else ''
                                                )"/>
                                            <xsl:if test="following-sibling::*">
                                                <xsl:value-of select="'&#xA;'"/>
                                            </xsl:if>
                                        </xsl:for-each>
                                        <xsl:if test="count(*/*) &gt; 5">
                                            <xsl:value-of select="concat('... ',(count(*/*)-5),' more documents (a total of ',count(*/*),') ...')"/>
                                        </xsl:if>
                                    </xsl:attribute>
                                </property>
                            </xsl:for-each>
                        </xsl:if>
                        <xsl:text>
</xsl:text>
                        <xsl:if test="$scenario/x:call/x:option">
                            <xsl:for-each select="$scenario/x:call/x:option">
                                <xsl:text>
</xsl:text>
                                <property name="option: {@name}" value="{@value}"/>
                            </xsl:for-each>
                        </xsl:if>
                        <xsl:text>
</xsl:text>
                        <xsl:if test="$scenario/x:call/x:param">
                            <xsl:for-each select="$scenario/x:call/x:param">
                                <xsl:text>
</xsl:text>
                                <property name="parameter: {@name}" value="{@value}"/>
                            </xsl:for-each>
                        </xsl:if>
                        <xsl:text>
</xsl:text>
                    </properties>
                    <xsl:text>
</xsl:text>

                    <xsl:if test="$pending-description">
                        <testcase name="pendingScenario-{$pending-description/x:scenario/@label}" classname="pendingScenario" assertions="1" time="0">
                            <skipped type="xprocspec.skip" message="{string($pending-description/x:scenario/@pending)}">
                                <xsl:value-of select="string($pending-description/x:scenario/@pending)"/>
                            </skipped>
                            <system-out>
                                <xsl:value-of select="concat(string($pending-description/x:scenario/@label),': SKIPPED')"/>
                            </system-out>
                        </testcase>
                    </xsl:if>

                    <xsl:for-each select="$tests">
                        <testcase name="{@label}" classname="{tokenize($declaration/@type,':')[last()]}" assertions="1" time="0">
                            <!--
                                 TODO: @time: Time taken (in seconds) to execute the test
                             -->

                            <xsl:if test="not(@result='skipped')">
                                <xsl:attribute name="status" select="if (@result='passed') then 'SUCCESS' else 'FAILED'"/>
                            </xsl:if>

                            <xsl:variable name="message" select="string(@label)"/>

                            <xsl:choose>
                                <xsl:when test="@result='passed'">
                                    <!-- successful assertion -->
                                    <system-out>
                                        <xsl:value-of select="concat($message,': SUCCESS')"/>
                                    </system-out>
                                </xsl:when>
                                <xsl:when test="@result='skipped'">
                                    <!-- skipped assertion -->
                                    <skipped type="xprocspec.skip" message="{string(@pending)}">
                                        <xsl:value-of select="string(@pending)"/>
                                    </skipped>
                                    <system-out>
                                        <xsl:value-of select="concat($message,': SKIPPED')"/>
                                    </system-out>
                                </xsl:when>
                                <xsl:otherwise>
                                    <!-- failed assertion -->
                                    <failure message="{$message}" type="{(@type,'unknown')[1]}"/>
                                    <system-out>
                                        <xsl:value-of select="concat($message,': FAILED')"/>
                                    </system-out>
                                </xsl:otherwise>
                            </xsl:choose>
                            <xsl:text>
</xsl:text>

                            <system-err>
                                <xsl:if test="@result='failed'">
                                    <xsl:if test="./x:expected">
                                        <xsl:text>Expected:
</xsl:text>
                                        <xsl:value-of select="./x:expected"/>
                                    </xsl:if>
                                    <xsl:text>

</xsl:text>
                                    <xsl:if test="./x:was">
                                        <xsl:text>Was:
</xsl:text>
                                        <xsl:value-of select="./x:was"/>
                                    </xsl:if>
                                </xsl:if>
                            </system-err>
                        </testcase>
                    </xsl:for-each>
                </xsl:otherwise>
            </xsl:choose>
        </testsuite>
    </xsl:template>


    <xsl:function name="x:now" as="xs:string">
        <xsl:value-of select="adjust-dateTime-to-timezone(current-dateTime(),xs:dayTimeDuration('PT0H'))"/>
    </xsl:function>

    <xsl:function name="x:duration" as="xs:decimal">
        <xsl:param name="from"/>
        <xsl:param name="to"/>
        <xsl:variable name="duration" select="xs:dateTime($to) - xs:dateTime($from)"/>
        <xsl:variable name="seconds" select="days-from-duration($duration) * 3600 * 24 + 
            hours-from-duration($duration) * 3600 + 
            minutes-from-duration($duration) * 60 + 
            seconds-from-duration($duration)"/>
        <xsl:value-of select="$seconds"/>
        <!--<xsl:value-of select="adjust-dateTime-to-timezone(current-dateTime(),xs:dayTimeDuration('PT0H'))"/>-->
    </xsl:function>

    <xsl:function name="x:camelCase" as="xs:string">
        <xsl:param name="string"/>
        <xsl:variable name="string" select="replace(replace($string,'[\s\.]+',' '),'[^\w ]','')"/>
        <xsl:value-of
            select="string-join(
                            (for $i in 1 to count(tokenize($string,' ')),
                                $s in tokenize($string,' ')[$i],
                                $fl in substring($s,1,1),
                                $tail in substring($s,2)
                            return
                                if($i eq 1)
                                    then $s
                                    else concat(upper-case($fl), $tail)
                            ),
                            ''
            )"
        />
    </xsl:function>

</xsl:stylesheet>
