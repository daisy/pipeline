<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions">

    <!--
        EPUB 3 Structural Semantics Vocabulary: https://idpf.github.io/epub-vocabs/structure
    -->
    <xsl:variable name="vocab-default"
                  select="('cover',                      'titlepage',                  'balloon',
                          'frontmatter',                 'halftitlepage',              'text-area',
                          'bodymatter',                  'copyright-page',             'sound-area',
                          'backmatter',                  'seriespage',                 'annotation',
                          'volume',                      'acknowledgments',            'note',
                          'part',                        'imprint',                    'footnote',
                          'chapter',                     'imprimatur',                 'rearnote',
                          'subchapter',                  'contributors',               'footnotes',
                          'division',                    'other-credits',              'rearnotes',
                          'abstract',                    'errata',                     'endnotes',
                          'foreword',                    'dedication',                 'annoref',
                          'preface',                     'revision-history',           'biblioref',
                          'prologue',                    'case-study',                 'glossref',
                          'introduction',                'help',                       'noteref',
                          'preamble',                    'marginalia',                 'referrer',
                          'conclusion',                  'notice',                     'credit',
                          'epilogue',                    'pullquote',                  'keyword',
                          'afterword',                   'sidebar',                    'topic-sentence',
                          'epigraph',                    'warning',                    'concluding-sentence',
                          'toc',                         'halftitle',                  'pagebreak',
                          'toc-brief',                   'fulltitle',                  'page-list',
                          'landmarks',                   'covertitle',                 'table',
                          'loa',                         'title',                      'table-row',
                          'loi',                         'subtitle',                   'table-cell',
                          'lot',                         'label',                      'list',
                          'lov',                         'ordinal',                    'list-item',
                          'appendix',                    'bridgehead',                 'figure',
                          'colophon',                    'learning-objective',         'antonym-group',
                          'credits',                     'learning-objectives',        'condensed-entry',
                          'keywords',                    'learning-outcome',           'def',
                          'index',                       'learning-outcomes',          'dictentry',
                          'index-headnotes',             'learning-resource',          'endnote',
                          'index-legend',                'learning-resources',         'etymology',
                          'index-group',                 'learning-standard',          'example',
                          'index-entry-list',            'learning-standards',         'gram-info',
                          'index-entry',                 'answer',                     'idiom',
                          'index-term',                  'answers',                    'part-of-speech',
                          'index-editor-note',           'assessment',                 'part-of-speech-group',
                          'index-locator',               'assessments',                'part-of-speech-list',
                          'index-locator-list',          'feedback',                   'phonetic-transcription',
                          'index-locator-range',         'fill-in-the-blank-problem',  'phrase-group',
                          'index-xref-preferred',        'general-problem',            'phrase-list',
                          'index-xref-related',          'qna',                        'sense-group',
                          'index-term-category',         'match-problem',              'sense-list',
                          'index-term-categories',       'multiple-choice-problem',    'synonym-group',
                          'glossary',                    'practice',                   'tran',
                          'glossterm',                   'practices',                  'tran-info',
                          'glossdef',                    'question',
                          'dictionary',                  'true-false-problem',
                          'bibliography',                'panel',
                          'biblioentry',                 'panel-group'
                          )"/>
    
    <!--
        Z39.98-2012 Structural Semantics Vocabulary: http://www.daisy.org/z3998/2012/vocab/structure
    -->
    <xsl:variable name="vocab-z3998-uri" select="'http://www.daisy.org/z3998/2012/vocab/structure/#'"/>
    <xsl:variable name="vocab-z3998"
                  select="('abbreviations',              'email',                      'measure',                   'recipient',
                          'acknowledgments',             'email-message',              'mixed',                     'recto',
                          'acronym',                     'epigraph',                   'morpheme',                  'reference',
                          'actor',                       'epilogue',                   'name-title',                'republisher',
                          'afterword',                   'errata',                     'nationality',               'resolving-reference',
                          'alteration',                  'essay',                      'non-fiction',               'result',
                          'annoref',                     'event',                      'nonresolving-citation',     'role-description',
                          'annotation',                  'example',                    'nonresolving-reference',    'roman',
                          'appendix',                    'family-name',                'note',                      'root',
                          'article',                     'fiction',                    'noteref',                   'salutation',
                          'aside',                       'figure',                     'notice',                    'scene',
                          'attribution',                 'filmography',                'orderedlist',               'secondary',
                          'author',                      'footnote',                   'ordinal',                   'section',
                          'award',                       'footnotes',                  'organization',              'sender',
                          'backmatter',                  'foreword',                   'other-credits',             'sentence',
                          'bcc',                         'fraction',                   'pagebreak',                 'sidebar',
                          'bibliography',                'from',                       'page-footer',               'signature',
                          'biographical-note',           'frontispiece',               'page-header',               'song',
                          'bodymatter',                  'frontmatter',                'part',                      'speech',
                          'cardinal',                    'ftp',                        'percentage',                'stage-direction',
                          'catalogue',                   'fulltitle',                  'persona',                   'stem',
                          'cc',                          'gallery',                    'personal-name',             'structure',
                          'chapter',                     'general-editor',             'pgroup',                    'subchapter',
                          'citation',                    'geographic',                 'phone',                     'subject',
                          'clarification',               'given-name',                 'phoneme',                   'subsection',
                          'collection',                  'glossary',                   'photograph',                'subtitle',
                          'colophon',                    'grant-acknowledgment',       'phrase',                    'suffix',
                          'commentary',                  'grapheme',                   'place',                     'surname',
                          'commentator',                 'halftitle',                  'plate',                     'taxonomy',
                          'compound',                    'halftitle-page',             'poem',                      'tertiary',
                          'concluding-sentence',         'help',                       'portmanteau',               'text',
                          'conclusion',                  'homograph',                  'postal',                    'textbook',
                          'continuation',                'http',                       'postal-code',               't-form',
                          'continuation-of',             'hymn',                       'postscript',                'timeline',
                          'contributors',                'illustration',               'practice',                  'title',
                          'coordinate',                  'image-placeholder',          'preamble',                  'title-page',
                          'correction',                  'imprimatur',                 'preface',                   'to',
                          'covertitle',                  'imprint',                    'prefix',                    'toc',
                          'currency',                    'index',                      'presentation',              'topic-sentence',
                          'decimal',                     'initialism',                 'primary',                   'translator',
                          'decorative',                  'introduction',               'product',                   'translator-note',
                          'dedication',                  'introductory-note',          'production',                'truncation',
                          'diary',                       'ip',                         'prologue',                  'unorderedlist',
                          'diary-entry',                 'isbn',                       'promotional-copy',          'valediction',
                          'discography',                 'keyword',                    'published-works',           'verse',
                          'division',                    'letter',                     'publisher-address',         'verso',
                          'drama',                       'loi',                        'publisher-logo',            'v-form',
                          'dramatis-personae',           'lot',                        'range',                     'volume',
                          'editor',                      'lyrics',                     'ratio',                     'warning',
                          'editorial-note',              'marginalia',                 'rearnote',                  'weight',
                                                                                       'rearnotes',                 'word'
                          )"/>
    
    <!--
        Package Metadata Vocabulary: http://www.idpf.org/epub/301/spec/epub-publications.html#sec-package-metadata-vocab
        
        This is the default vocabulary for use of unprefixed terms in package metadata
        
        see http://www.idpf.org/epub/301/spec/epub-publications.html#sec-metadata-default-vocab
    -->
    <xsl:variable name="vocab-package-uri" as="xs:string" select="'http://idpf.org/epub/vocab/package/#'"/>
    
    <!--
        Reserved prefix mappings in package document
        
        see https://www.w3.org/publishing/epub3/epub-packages.html#sec-metadata-reserved-prefixes
    -->
    <xsl:variable name="f:default-prefixes" as="element(f:vocab)*">
        <f:vocab prefix="a11y"      uri="http://www.idpf.org/epub/vocab/package/a11y/#"/>
        <f:vocab prefix="dcterms"   uri="http://purl.org/dc/terms/"/>
   <!-- <f:vocab prefix="epubsc"    uri="http://idpf.org/epub/vocab/sc/#"/> -->
        <f:vocab prefix="marc"      uri="http://id.loc.gov/vocabulary/"/>
        <f:vocab prefix="media"     uri="http://www.idpf.org/epub/vocab/overlays/#"/>
        <f:vocab prefix="onix"      uri="http://www.editeur.org/ONIX/book/codelists/current.html#"/>
        <f:vocab prefix="rendition" uri="http://www.idpf.org/vocab/rendition/#"/>
        <f:vocab prefix="schema"    uri="http://schema.org/"/>
        <f:vocab prefix="xsd"       uri="http://www.w3.org/2001/XMLSchema#"/>
    </xsl:variable>
    
    <!--
        Parse prefix attribute: http://www.idpf.org/epub/301/spec/epub-publications.html#sec-prefix-attr
        
        Returns a sequence of f:vocab elements representing vocab declarations in a @prefix attribute where
        
        * @prefix contains the declared prefix
        * @uri contains the vocab URI
    -->
    <xsl:function name="f:parse-prefix-decl" as="element(f:vocab)*">
        <xsl:param name="prefix-decl" as="xs:string?"/>
        <xsl:analyze-string select="$prefix-decl" regex="([^:\s\t\n\r]+):\s*([^\s\t\n\r]+)">
            <xsl:matching-substring>
                <f:vocab prefix="{regex-group(1)}" uri="{regex-group(2)}"/>
            </xsl:matching-substring>
            <xsl:non-matching-substring>
                <xsl:if test="not(matches(.,'[\s\t\n\r]+'))">
                    <xsl:message terminate="yes" select="concat('Error parsing prefix attribute: ',$prefix-decl)"/>
                </xsl:if>
            </xsl:non-matching-substring>
        </xsl:analyze-string>
    </xsl:function>
    
    <!--
        Merge a sequence of f:vocab elements. The result is a sequence of valid mappings where every
        prefix is unique, no prefix is mapped to the default vocabulary, no reserved prefixes are
        overridden, and duplicates are removed. URIs may be non-unique (different prefixes may map
        to the same URI).
    -->
    <xsl:function name="f:merge-prefix-decl" as="element(f:vocab)*">
        <xsl:param name="mappings" as="element(f:vocab)*"/>
        <xsl:param name="reserved-prefixes" as="element(f:vocab)*"/>
        <!--
            no prefix may be mapped to default vocabulary
        -->
        <xsl:variable name="mappings" as="element(f:vocab)*">
            <xsl:for-each select="$mappings">
                <xsl:choose>
                    <xsl:when test="@uri=$vocab-package-uri">
                        <xsl:message select="concat('Warning: prefix attibute must not be used to define a prefix (',
                                                    @prefix, ') that maps to the default vocabulary ''',
                                                    $vocab-package-uri,'''')"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:sequence select="."/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </xsl:variable>
        <!--
            make prefixes unique
        -->
        <xsl:variable name="mappings" as="element(f:vocab)*" select="f:unique-prefixes($mappings)"/>
        <!--
            reserved prefixes should not be overridden
        -->
        <xsl:variable name="mappings" as="element(f:vocab)*">
            <xsl:choose>
                <xsl:when test="some $m in $mappings satisfies
                                $reserved-prefixes[@prefix=$m/@prefix and not(@uri=$m/@uri)]">
                    <xsl:for-each select="$mappings">
                        <xsl:if test="$reserved-prefixes[@prefix=current()/@prefix and not(@uri=current()/@uri)]">
                            <xsl:message select="concat('Warning: reserved prefix ',@prefix,' was overridden to ''',@uri,'''')"/>
                        </xsl:if>
                    </xsl:for-each>
                    <xsl:sequence select="f:unique-prefixes(($reserved-prefixes,$mappings))
                                          [position()&gt;count($reserved-prefixes)]"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:sequence select="$mappings"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <!--
            remove duplicates
        -->
        <xsl:variable name="mappings" as="element(f:vocab)*"
                      select="for $p in distinct-values($mappings/@prefix) return $mappings[@prefix=$p][1]"/>
        <xsl:sequence select="$mappings"/>
    </xsl:function>
    
    <!--
        Rename prefixes so that the sequence of f:vocab elements becomes a sequence where a certain
        prefix always maps to the same URI.
    -->
    <xsl:function name="f:unique-prefixes" as="element(f:vocab)*">
        <xsl:param name="mappings" as="element(f:vocab)*"/>
        <xsl:sequence select="f:unique-prefixes((),$mappings)"/>
    </xsl:function>
    
    <xsl:function name="f:unique-prefixes" as="element(f:vocab)*">
        <xsl:param name="head" as="element(f:vocab)*"/>
        <xsl:param name="tail" as="element(f:vocab)*"/>
        <xsl:variable name="head" as="element(f:vocab)*">
            <xsl:sequence select="$head"/>
            <xsl:if test="exists($tail[1])">
                <xsl:choose>
                    <xsl:when test="$head[@prefix=$tail[1]/@prefix and not(@uri=$tail[1]/@uri)]">
                        <f:vocab prefix="{f:unique-prefix($tail[1]/@prefix,$head/@prefix)}"
                                 uri="{$tail[1]/@uri}"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:sequence select="$tail[1]"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:if>
        </xsl:variable>
        <xsl:variable name="tail" as="element(f:vocab)*" select="$tail[position()&gt;1]"/>
        <xsl:sequence select="if (exists($tail[1]))
                              then f:unique-prefixes($head, $tail)
                              else $head"/>
    </xsl:function>
    
    <!--
        Return a unique prefix from a given prefix and a sequence of existing prefixes. The unique
        prefix is generated by appending the needed amount of '_'.
    -->
    <xsl:function name="f:unique-prefix" as="xs:string">
        <xsl:param name="prefix" as="xs:string"/>
        <xsl:param name="existing" as="xs:string*"/>
        <xsl:sequence select="if (not($prefix=$existing))
                              then $prefix
                              else f:unique-prefix(concat($prefix,'_'),$existing)"/>
    </xsl:function>
    
    <!--
        Add a prefix mapping to a list of existing mappings
    -->
    <xsl:function name="pf:epub3-vocab-add-prefix">
        <xsl:param name="mappings" as="xs:string?"/> <!-- existing mappings -->
        <xsl:param name="prefix" as="xs:string"/> <!-- prefix for new mapping -->
        <xsl:param name="uri" as="xs:string"/> <!-- uri for new mapping -->
        <!--
            parse and add new mapping
        -->
        <xsl:variable name="new-mapping" as="element(f:vocab)">
            <f:vocab prefix="{$prefix}" uri="{$uri}"/>
        </xsl:variable>
        <xsl:variable name="mappings" as="element(f:vocab)*"
                      select="f:merge-prefix-decl((
                                  if (exists($mappings)) then f:parse-prefix-decl($mappings) else (),
                                  $new-mapping),
                                $f:default-prefixes)"/>
        <!--
            serialize
        -->
        <xsl:sequence select="string-join(for $m in $mappings return concat($m/@prefix,': ',$m/@uri),' ')"/>
    </xsl:function>
    
</xsl:stylesheet>
