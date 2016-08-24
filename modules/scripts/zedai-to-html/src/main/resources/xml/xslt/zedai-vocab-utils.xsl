<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:pf="http://www.daisy.org/ns/functions"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  exclude-result-prefixes="#all" version="2.0">

  <xsl:function name="pf:to-epub" as="xs:string">
    <xsl:param name="term" as="xs:string"/>
    <xsl:value-of select="$zedai-epub-map[@zedai=$term]/@epub"/>
  </xsl:function>

  <xsl:variable name="zedai-epub-map" as="element()*">
    <term zedai="fiction"/>
    <term zedai="non-fiction"/>
    <term zedai="article"/>
    <term zedai="essay"/>
    <term zedai="textbook"/>
    <term zedai="catalogue"/>
    <term zedai="frontmatter" epub="frontmatter"/>
    <term zedai="bodymatter" epub="bodymatter"/>
    <term zedai="backmatter" epub="backmatter"/>
    <term zedai="volume" epub="volume"/>
    <term zedai="part" epub="part"/>
    <term zedai="chapter" epub="chapter"/>
    <term zedai="subchapter" epub="subchapter"/>
    <term zedai="division" epub="division"/>
    <term zedai="section"/>
    <term zedai="subsection"/>
    <term zedai="foreword" epub="foreword"/>
    <term zedai="preface" epub="preface"/>
    <term zedai="prologue" epub="prologue"/>
    <term zedai="introduction" epub="introduction"/>
    <term zedai="preamble" epub="preamble"/>
    <term zedai="conclusion" epub="conclusion"/>
    <term zedai="epilogue" epub="epilogue"/>
    <term zedai="afterword" epub="afterword"/>
    <term zedai="toc" epub="toc"/>
    <term zedai="appendix" epub="appendix"/>
    <term zedai="glossary" epub="glossary"/>
    <term zedai="bibliography" epub="bibliography"/>
    <term zedai="discography" epub="bibliography"/><!--TODO refine-->
    <term zedai="filmography" epub="bibliography"/><!--TODO refine-->
    <term zedai="index" epub="index"/>
    <term zedai="colophon" epub="colophon"/>
    <term zedai="title" epub="title"/>
    <term zedai="halftitle" epub="halftitle"/>
    <term zedai="fulltitle" epub="fulltitle"/>
    <term zedai="subtitle" epub="subtitle"/>
    <term zedai="covertitle" epub="covertitle"/>
    <term zedai="published-works"/>
    <term zedai="title-page" epub="titlepage"/>
    <term zedai="halftitle-page" epub="halftitlepage"/>
    <term zedai="acknowledgments" epub="acknowledgments"/>
    <term zedai="imprint" epub="imprint"/>
    <term zedai="imprimatur" epub="imprimatur"/>
    <term zedai="loi" epub="loi"/>
    <term zedai="lot" epub="lot"/>
    <term zedai="publisher-address"/>
    <term zedai="publisher-address"/>
    <term zedai="editorial-note" epub="note"/><!--TODO refine-->
    <term zedai="grant-acknowledgment" epub="acknowledgments"/><!--TODO refine-->
    <term zedai="contributors" epub="contributors"/>
    <term zedai="other-credits" epub="other-credits"/>
    <term zedai="biographical-note" epub="note"/><!--TODO refine-->
    <term zedai="translator-note" epub="note"/><!--TODO refine-->
    <term zedai="errata" epub="errata"/>
    <term zedai="promotional-copy"/>
    <term zedai="dedication" epub="dedication"/>
    <term zedai="epigraph" epub="epigraph"/>
    <term zedai="example"/>
    <term zedai="pgroup"/>
    <term zedai="annotation" epub="annotation"/>
    <term zedai="introductory-note" epub="annotation"/><!--TODO refine-->
    <term zedai="commentary" epub="annotation"/><!--TODO refine-->
    <term zedai="clarification" epub="annotation"/><!--TODO refine-->
    <term zedai="correction" epub="annotation"/><!--TODO refine-->
    <term zedai="alteration" epub="annotation"/><!--TODO refine-->
    <term zedai="presentation" epub="annotation"/><!--TODO refine-->
    <term zedai="production" epub="annotation"/><!--TODO refine-->
    <term zedai="attribution" epub="annotation"/><!--TODO refine-->
    <term zedai="author"/>
    <term zedai="editor"/>
    <term zedai="general-editor"/>
    <term zedai="commentator"/>
    <term zedai="translator"/>
    <term zedai="republisher"/>
    <term zedai="structure"/>
    <term zedai="aside"/>
    <term zedai="sidebar" epub="sidebar"/>
    <term zedai="practice" epub="practice"/>
    <term zedai="notice" epub="notice"/>
    <term zedai="warning" epub="warning"/>
    <term zedai="marginalia" epub="marginalia"/>
    <term zedai="help" epub="help"/>
    <term zedai="drama"/>
    <term zedai="scene"/>
    <term zedai="stage-direction"/>
    <term zedai="dramatis-personae"/>
    <term zedai="persona"/>
    <term zedai="actor"/>
    <term zedai="role-description"/>
    <term zedai="speech"/>
    <term zedai="diary"/>
    <term zedai="diary-entry"/>
    <term zedai="figure"/>
    <term zedai="plate"/>
    <term zedai="gallery"/>
    <term zedai="letter"/>
    <term zedai="sender"/>
    <term zedai="recipient"/>
    <term zedai="salutation"/>
    <term zedai="valediction"/>
    <term zedai="postscript"/>
    <term zedai="email"/>
    <term zedai="to"/>
    <term zedai="from"/>
    <term zedai="cc"/>
    <term zedai="bcc"/>
    <term zedai="subject"/>
    <term zedai="collection"/>
    <term zedai="orderedlist"/>
    <term zedai="unorderedlist"/>
    <term zedai="abbreviations"/>
    <term zedai="timeline"/>
    <term zedai="note" epub="note"/>
    <term zedai="footnotes" epub="footnotes"/>
    <term zedai="footnote" epub="footnote"/>
    <term zedai="rearnote" epub="rearnote"/>
    <term zedai="rearnotes" epub="rearnotes"/>
    <term zedai="verse"/>
    <term zedai="poem"/>
    <term zedai="song"/>
    <term zedai="hymn"/>
    <term zedai="lyrics"/>
    <term zedai="text"/>
    <term zedai="phrase"/>
    <term zedai="keyword" epub="keyword"/>
    <term zedai="sentence"/>
    <term zedai="topic-sentence" epub="topic-sentence"/>
    <term zedai="concluding-sentence" epub="concluding-sentence"/>
    <term zedai="t-form"/>
    <term zedai="v-form"/>
    <term zedai="acronym"/>
    <term zedai="initialism"/>
    <term zedai="truncation"/>
    <term zedai="cardinal"/>
    <term zedai="ordinal"/>
    <term zedai="ratio"/>
    <term zedai="percentage"/>
    <term zedai="phone"/>
    <term zedai="isbn"/>
    <term zedai="currency"/>
    <term zedai="postal"/>
    <term zedai="result"/>
    <term zedai="fraction"/>
    <term zedai="mixed"/>
    <term zedai="decimal"/>
    <term zedai="roman"/>
    <term zedai="weight"/>
    <term zedai="measure"/>
    <term zedai="coordinate"/>
    <term zedai="range"/>
    <term zedai="result"/>
    <term zedai="place"/>
    <term zedai="nationality"/>
    <term zedai="organization"/>
    <term zedai="taxonomy"/>
    <term zedai="product"/>
    <term zedai="event"/>
    <term zedai="award"/>
    <term zedai="personal-name"/>
    <term zedai="given-name"/>
    <term zedai="surname"/>
    <term zedai="family-name"/>
    <term zedai="name-title"/>
    <term zedai="signature"/>
    <term zedai="word"/>
    <term zedai="compound"/>
    <term zedai="homograph"/>
    <term zedai="portmanteau"/>
    <term zedai="root"/>
    <term zedai="stem"/>
    <term zedai="prefix"/>
    <term zedai="suffix"/>
    <term zedai="morpheme"/>
    <term zedai="phoneme"/>
    <term zedai="grapheme"/>
    <term zedai="illustration"/>
    <term zedai="photograph"/>
    <term zedai="decorative"/>
    <term zedai="publisher-logo"/>
    <term zedai="frontispiece"/>
    <term zedai="reference"/>
    <term zedai="resolving-reference"/>
    <term zedai="nonresolving-reference"/>
    <term zedai="noteref" epub="noteref"/>
    <term zedai="annoref" epub="annoref"/>
    <term zedai="citation"/>
    <term zedai="nonresolving-citation"/>
    <term zedai="continuation"/>
    <term zedai="continuation-of"/>
    <term zedai="pagebreak" epub="pagebreak"/>
    <term zedai="page-header"/>
    <term zedai="page-footer"/>
    <term zedai="recto"/>
    <term zedai="verso"/>
    <term zedai="image-placeholder"/>
    <term zedai="primary"/>
    <term zedai="secondary"/>
    <term zedai="tertiary"/>

  </xsl:variable>
</xsl:stylesheet>
