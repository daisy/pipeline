<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions">

    <!--
        EPUB 3 Structural Semantics Vocabulary
        
        This is the default vocabulary for use of unprefixed terms in epub:type attributes
        
        see https://www.w3.org/TR/epub-ssv-11/
    -->
    <xsl:variable name="vocab-structure-uri" select="'http://idpf.org/epub/vocab/structure/#'"/>
    <xsl:variable name="vocab-structure"
                  select="('acknowledgments',           'etymology',              'index-xref-preferred',    'qna',
                           'afterword',                 'example',                'index-xref-related',      'revision-history',
                           'antonym-group',             'figure',                 'introduction',            'sense-group',
                           'appendix',                  'footnote',               'keyword',                 'sense-list',
                           'aside',                     'footnotes',              'landmarks',               'sound-area',
                           'assessment',                'foreword',               'learning-objective',      'subtitle',
                           'backmatter',                'frontmatter',            'learning-resource',       'synonym-group',
                           'balloon',                   'fulltitle',              'list',                    'table',
                           'biblioentry',               'glossary',               'list-item',               'table-cell',
                           'bibliography',              'glossdef',               'loa',                     'table-row',
                           'bodymatter',                'glossterm',              'loi',                     'text-area',
                           'chapter',                   'gram-info',              'lot',                     'tip',
                           'colophon',                  'halftitle',              'lov',                     'title',
                           'concluding-sentence',       'halftitlepage',          'noteref',                 'titlepage',
                           'conclusion',                'idiom',                  'notice',                  'toc',
                           'condensed-entry',           'imprimatur',             'other-credits',           'topic-sentence',
                           'contributors',              'imprint',                'page-list',               'tran',
                           'copyright-page',            'index',                  'pagebreak',               'tran-info',
                           'cover',                     'index-editor-note',      'panel',                   'volume',
                           'covertitle',                'index-entry',            'panel-group',
                           'dedication',                'index-entry-list',       'part',
                           'def',                       'index-group',            'part-of-speech',
                           'dictentry',                 'index-headnotes',        'part-of-speech-group',
                           'dictionary',                'index-legend',           'part-of-speech-list',
                           'division',                  'index-locator',          'phonetic-transcription',
                           'endnote',                   'index-locator-list',     'phrase-group',
                           'endnotes',                  'index-locator-range',    'phrase-list',
                           'epigraph',                  'index-term',             'preamble',
                           'epilogue',                  'index-term-categories',  'preface',
                           'errata',                    'index-term-category',    'prologue',
                           
                           (:draft:)
                           
                           'abstract',                  'general-problem',        'multiple-choice-problem',
                           'answer',                    'glossref',               'ordinal',
                           'answers',                   'keywords',               'practice',
                           'assessments',               'label',                  'practices',
                           'backlink',                  'learning-objectives',    'pullquote',
                           'biblioref',                 'learning-outcome',       'question',
                           'case-study',                'learning-outcomes',      'seriespage',
                           'credit',                    'learning-resources',     'toc-brief',
                           'credits',                   'learning-standard',      'true-false-problem',
                           'feedback',                  'learning-standards',
                           'fill-in-the-blank-problem', 'match-problem',
                           
                           (:deprecated:)
                           
                           'annoref',
                           'annotation',
                           'bridgehead',
                           'help',
                           'marginalia',
                           'note',
                           'rearnote',
                           'rearnotes',
                           'sidebar',
                           'subchapter',
                           'warning'
                           )"/>
    
    <!--
        Z39.98-2012 Structural Semantics Vocabulary
        
        see http://www.daisy.org/z3998/2012/vocab/structure
    -->
    <xsl:variable name="vocab-z3998-structure-uri" select="'http://www.daisy.org/z3998/2012/vocab/structure/#'"/>
    <xsl:variable name="vocab-z3998-structure"
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
        Meta Properties Vocabulary
        
        This is the default vocabulary for use of unprefixed terms in package metadata, for the
        meta/@property attribute.
        
        see https://www.w3.org/TR/epub-33/#app-meta-property-vocab
    -->
    <xsl:variable name="vocab-package-meta-uri" as="xs:string" select="'http://idpf.org/epub/vocab/package/meta/#'"/>
    
    <!--
        Link Relationships Vocabulary
        
        This is the default vocabulary for use of unprefixed terms in the package metadata, for the
        link/@rel and link/@properties attributes.
        
        see https://www.w3.org/TR/epub/#app-link-vocab
    -->
    <xsl:variable name="vocab-package-link-uri" as="xs:string" select="'http://idpf.org/epub/vocab/package/link/#'"/>
    
    <!--
        Spine Properties Vocabulary
        
        This is the default vocabulary for use of unprefixed terms in the package metadata, for the
        item/@properties attribute.
        
        see https://www.w3.org/TR/epub-33/#app-item-properties-vocab
    -->
    <xsl:variable name="vocab-package-item-uri" as="xs:string" select="'http://idpf.org/epub/vocab/package/item/#'"/>
    
    
    <!--
        Spine Properties Vocabulary
        
        This is the default vocabulary for use of unprefixed terms in the package metadata, for the
        itemref/@properties attribute.
        
        see https://www.w3.org/TR/epub-33/#app-itemref-properties-vocab
    -->
    <xsl:variable name="vocab-package-itemref-uri" as="xs:string" select="'http://idpf.org/epub/vocab/package/itemref/#'"/>
    
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
        Parse prefix attribute
        
        Returns a sequence of f:vocab elements representing vocab declarations in a @prefix attribute where
        
        * @prefix contains the declared prefix
        * @uri contains the vocab URI
        
        see http://www.idpf.org/epub/301/spec/epub-publications.html#sec-prefix-attr
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
            no prefix may be mapped to a default vocabulary
        -->
        <xsl:variable name="mappings" as="element(f:vocab)*">
            <xsl:for-each select="$mappings">
                <xsl:choose>
                    <xsl:when test="@uri=($vocab-package-meta-uri,
                                          $vocab-package-link-uri,
                                          $vocab-package-item-uri,
                                          $vocab-package-itemref-uri,
                                          $vocab-structure-uri)">
                        <xsl:message select="concat('Warning: prefix attibute must not be used to define a prefix (',
                                                    @prefix, ') that maps to a default vocabulary: ''',
                                                    @uri,'''')"/>
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
