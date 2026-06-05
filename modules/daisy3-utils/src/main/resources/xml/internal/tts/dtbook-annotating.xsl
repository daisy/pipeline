<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:dt="http://www.daisy.org/z3986/2005/dtbook/"
		xmlns:ssml="http://www.w3.org/2001/10/synthesis"
		exclude-result-prefixes="#all"
		version="2.0">

  <xsl:template match="dt:docauthor[1]">
    <lang xml:lang="en">
      <before><ssml:token>By</ssml:token></before>
    </lang>
    <lang xml:lang="fr">
      <before><ssml:token>Par</ssml:token></before>
    </lang>
    <lang xml:lang="ja">
      <before><ssml:token>著者</ssml:token></before>
    </lang>
    <lang xml:lang="no">
      <before><ssml:token>Av</ssml:token></before>
    </lang>
    <lang xml:lang="sv">
      <before><ssml:token>Av</ssml:token></before>
    </lang>
  </xsl:template>

  <xsl:template match="dt:docauthor[position()=last() and last()!=1]">
    <lang xml:lang="en">
      <before><ssml:token>and</ssml:token></before>
    </lang>
    <lang xml:lang="fi">
      <before><ssml:token>ja</ssml:token></before>
    </lang>
    <lang xml:lang="fr">
      <before><ssml:token>et</ssml:token></before>
    </lang>
    <lang xml:lang="ja">
      <before><ssml:token>と</ssml:token></before>
    </lang>
    <lang xml:lang="no">
      <before><ssml:token>og</ssml:token></before>
    </lang>
    <lang xml:lang="sv">
      <before><ssml:token>och</ssml:token></before>
    </lang>
  </xsl:template>

  <xsl:template match="dt:note">
    <lang xml:lang="en">
      <before><ssml:token>Note</ssml:token></before>
      <after><ssml:token>End</ssml:token> <ssml:token>of</ssml:token> <ssml:token>note</ssml:token>.</after>
    </lang>
    <lang xml:lang="fi">
      <before><ssml:token>Viite</ssml:token></before>
      <after><ssml:token>Viitteen</ssml:token> <ssml:token>loppu</ssml:token>.</after>
    </lang>
    <lang xml:lang="fr">
      <before><ssml:token>Note</ssml:token></before>
    </lang>
    <lang xml:lang="ja">
      <before><ssml:token>注記</ssml:token>　<ssml:token>開始</ssml:token></before>
      <after><ssml:token>注記</ssml:token>　<ssml:token>終了</ssml:token></after>
    </lang>
    <lang xml:lang="no">
      <before><ssml:token>Note</ssml:token>.</before>
      <after><ssml:token>Slutt</ssml:token> <ssml:token>på</ssml:token> <ssml:token>noten</ssml:token>.</after>
    </lang>
    <lang xml:lang="sv">
      <before><ssml:token>Not</ssml:token>.</before>
      <after><ssml:token>Slut</ssml:token> <ssml:token>på</ssml:token> <ssml:token>noten</ssml:token>.</after>
    </lang>
  </xsl:template>

  <xsl:template match="dt:prodnote">
    <lang xml:lang="en">
      <before><ssml:token>Production</ssml:token> <ssml:token>note</ssml:token>.</before>
      <after><ssml:token>End</ssml:token> <ssml:token>of</ssml:token> <ssml:token>production</ssml:token> <ssml:token>note</ssml:token>.</after>
    </lang>
    <lang xml:lang="fi">
      <before><ssml:token>Toimittajan</ssml:token> <ssml:token>huomautus</ssml:token>.</before>
      <after><ssml:token>Huomautuksen</ssml:token> <ssml:token>loppu</ssml:token>.</after>
    </lang>
    <lang xml:lang="fr">
      <before><ssml:token>Note</ssml:token> <ssml:token>de</ssml:token> <ssml:token>production</ssml:token>.</before>
      <after><ssml:token>Fin</ssml:token> <ssml:token>de</ssml:token> <ssml:token>la</ssml:token> <ssml:token>note</ssml:token> <ssml:token>de</ssml:token> <ssml:token>production</ssml:token>.</after>
    </lang>
    <lang xml:lang="ja">
      <before><ssml:token>製作者ノート</ssml:token> <ssml:token>開始</ssml:token></before>
      <after><ssml:token>製作者ノート</ssml:token>　<ssml:token>終了</ssml:token></after>
    </lang>
    <lang xml:lang="no">
      <before><ssml:token>Produksjonsnote</ssml:token></before>
      <after><ssml:token>Slutt</ssml:token> <ssml:token>produksjonsnote</ssml:token>.</after>
    </lang>
    <lang xml:lang="sv">
      <before><ssml:token>Kommentar</ssml:token>.</before>
      <after><ssml:token>Slut</ssml:token> <ssml:token>kommentar</ssml:token>.</after>
    </lang>
  </xsl:template>

  <xsl:template match="dt:imggroup/dt:prodnote">
    <lang xml:lang="en">
      <before><ssml:token>Image</ssml:token> <ssml:token>description</ssml:token>.</before>
      <after><ssml:token>End</ssml:token> <ssml:token>of</ssml:token> <ssml:token>image</ssml:token> <ssml:token>description</ssml:token>.</after>
    </lang>
    <lang xml:lang="fi">
      <before><ssml:token>Kuvaselostus</ssml:token>.</before>
      <after><ssml:token>Kuvaselostuksen</ssml:token> <ssml:token>loppu</ssml:token>.</after>
    </lang>
    <lang xml:lang="fr">
      <before><ssml:token>Description</ssml:token> <ssml:token>d'</ssml:token><ssml:token>image</ssml:token>.</before>
      <after><ssml:token>Fin</ssml:token> <ssml:token>de</ssml:token> <ssml:token>la</ssml:token> <ssml:token>description</ssml:token> <ssml:token>d'</ssml:token><ssml:token>image</ssml:token>.</after>
    </lang>
    <lang xml:lang="ja">
      <before><ssml:token>画像の説明</ssml:token>　<ssml:token>開始</ssml:token></before>
      <after><ssml:token>画像の説明</ssml:token>　<ssml:token>終了</ssml:token></after>
    </lang>
    <lang xml:lang="no">
      <before><ssml:token>Bildebeskrivelse</ssml:token>.</before>
      <after><ssml:token>Bildebeskrivelse slutt</ssml:token>.</after>
    </lang>
    <lang xml:lang="sv">
      <before><ssml:token>Bildbeskrivning</ssml:token>.</before>
      <after><ssml:token>Slut</ssml:token> <ssml:token>bildbeskrivning</ssml:token>.</after>
    </lang>
  </xsl:template>

  <xsl:template match="dt:imggroup/dt:caption">
    <lang xml:lang="en">
      <before><ssml:token>Caption</ssml:token>.</before>
      <after><ssml:token>End</ssml:token> <ssml:token>of</ssml:token> <ssml:token>caption</ssml:token>.</after>
    </lang>
    <lang xml:lang="fi">
      <before><ssml:token>Kuvateksti</ssml:token>.</before>
      <after><ssml:token>Kuvatekstin</ssml:token> <ssml:token>loppu</ssml:token>.</after>
    </lang>
    <lang xml:lang="fr">
      <before><ssml:token>Légende</ssml:token>.</before>
      <after><ssml:token>Fin</ssml:token> <ssml:token>de</ssml:token> <ssml:token>la</ssml:token> <ssml:token>légende</ssml:token>.</after>
    </lang>
    <lang xml:lang="ja">
      <before><ssml:token>キャプション開始</ssml:token></before>
      <after><ssml:token>キャプション終了</ssml:token></after>
    </lang>
    <lang xml:lang="no">
      <before><ssml:token>Bildetekst</ssml:token>.</before>
      <after><ssml:token>Bildetekst</ssml:token> <ssml:token>slutt</ssml:token>.</after>
    </lang>
    <lang xml:lang="sv">
      <before><ssml:token>Bildtext</ssml:token></before>
      <after><ssml:token>Slut</ssml:token> <ssml:token>bildtext</ssml:token>.</after>
    </lang>
  </xsl:template>

  <xsl:template match="dt:sidebar">
    <lang xml:lang="en">
      <before><ssml:token>Side</ssml:token> <ssml:token>bar</ssml:token>.</before>
      <after><ssml:token>End</ssml:token> <ssml:token>of</ssml:token> <ssml:token>side</ssml:token> <ssml:token>bar</ssml:token>.</after>
    </lang>
    <lang xml:lang="fi">
      <before><ssml:token>Marginaaliteksti</ssml:token>.</before>
      <after><ssml:token>Marginaalitekstin</ssml:token> <ssml:token>loppu</ssml:token>.</after>
    </lang>
    <lang xml:lang="fr">
      <before><ssml:token>Encadré</ssml:token>.</before>
      <after><ssml:token>Fin</ssml:token> <ssml:token>de</ssml:token> <ssml:token>l'</ssml:token><ssml:token>encadré</ssml:token>.</after>
    </lang>
    <lang xml:lang="ja">
      <before><ssml:token>サイドバー開始</ssml:token></before>
      <after><ssml:token>サイドバー終了</ssml:token></after>
    </lang>
    <lang xml:lang="no">
      <before><ssml:token>Margtekst</ssml:token>.</before>
      <after><ssml:token>Margtekst</ssml:token> <ssml:token>slutt</ssml:token>.</after>
    </lang>
    <lang xml:lang="sv">
      <before><ssml:token>Parallell</ssml:token> <ssml:token>text</ssml:token>.</before>
      <after><ssml:token>Slut</ssml:token> <ssml:token>parallell</ssml:token> <ssml:token>text</ssml:token>.</after>
    </lang>
  </xsl:template>

  <xsl:template match="dt:poem">
    <lang xml:lang="en">
      <before><ssml:token>A</ssml:token> <ssml:token>poem</ssml:token> <ssml:token>follows</ssml:token>.</before>
      <after><ssml:token>End</ssml:token> <ssml:token>of</ssml:token> <ssml:token>poem</ssml:token>.</after>
    </lang>
    <lang xml:lang="fi">
      <before><ssml:token>Runo</ssml:token>.</before>
      <after><ssml:token>Runon</ssml:token> <ssml:token>loppu</ssml:token>.</after>
    </lang>
    <lang xml:lang="fr">
      <before><ssml:token>Un</ssml:token> <ssml:token>poème</ssml:token> <ssml:token>suit</ssml:token>.</before>
      <after><ssml:token>Fin</ssml:token> <ssml:token>du</ssml:token> <ssml:token>poème</ssml:token></after>
    </lang>
    <lang xml:lang="ja">
      <before><ssml:token>詩</ssml:token>　<ssml:token>開始</ssml:token>.</before>
      <after><ssml:token>詩</ssml:token>　<ssml:token>終了</ssml:token>.</after>
    </lang>
    <lang xml:lang="no">
      <before><ssml:token>Dikt</ssml:token>.</before>
      <after><ssml:token>Dikt</ssml:token> <ssml:token>slutt</ssml:token>.</after>
    </lang>
    <lang xml:lang="sv">
      <before><ssml:token>En</ssml:token> <ssml:token>dikt</ssml:token> <ssml:token>följer</ssml:token>.</before>
      <after><ssml:token>Slut</ssml:token> <ssml:token>på</ssml:token> <ssml:token>dikten</ssml:token>.</after>
    </lang>
  </xsl:template>

  <xsl:template match="dt:blockquote">
    <lang xml:lang="en">
      <before><ssml:token>Quote:</ssml:token></before>
      <after><ssml:token>End</ssml:token> <ssml:token>of</ssml:token> <ssml:token>quote</ssml:token>.</after>
    </lang>
    <lang xml:lang="fi">
      <before><ssml:token>Lainaus:</ssml:token></before>
      <after><ssml:token>Lainauksen</ssml:token> <ssml:token>loppu</ssml:token>.</after>
    </lang>
    <lang xml:lang="fr">
      <before><ssml:token>Une</ssml:token> <ssml:token>citation</ssml:token> <ssml:token>suit</ssml:token>: </before>
      <after><ssml:token>Fin</ssml:token> <ssml:token>de</ssml:token> <ssml:token>la</ssml:token> <ssml:token>citation</ssml:token>.</after>
    </lang>
    <lang xml:lang="ja">
      <before><ssml:token>引用</ssml:token>　<ssml:token>開始</ssml:token>: </before>
      <after><ssml:token>引用</ssml:token>　<ssml:token>終了</ssml:token></after>
    </lang>
    <lang xml:lang="no">
      <before><ssml:token>Sitat:</ssml:token></before>
      <after><ssml:token>Sitat</ssml:token> <ssml:token>slutt</ssml:token>.</after>
    </lang>
    <lang xml:lang="sv">
      <before><ssml:token>Citat:</ssml:token></before>
      <after><ssml:token>Slut</ssml:token> <ssml:token>citat</ssml:token>.</after>
    </lang>
  </xsl:template>

  <xsl:template match="dt:table">
    <lang xml:lang="en">
      <before><ssml:token>Table</ssml:token>.</before>
      <after><ssml:token>End</ssml:token> <ssml:token>of</ssml:token> <ssml:token>table</ssml:token>.</after>
    </lang>
    <lang xml:lang="fi">
      <before><ssml:token>Taulukko</ssml:token>.</before>
      <after><ssml:token>Taulukon</ssml:token> <ssml:token>loppu</ssml:token>.</after>
    </lang>
    <lang xml:lang="fr">
      <before><ssml:token>Tableau</ssml:token>.</before>
      <after><ssml:token>Fin</ssml:token> <ssml:token>du</ssml:token> <ssml:token>tableau</ssml:token>.</after>
    </lang>
    <lang xml:lang="ja">
      <before><ssml:token>表</ssml:token>　<ssml:token>開始</ssml:token>.</before>
      <after><ssml:token>表</ssml:token>　<ssml:token>終了</ssml:token>.</after>
    </lang>
    <lang xml:lang="no">
      <before><ssml:token>Tabell</ssml:token>.</before>
      <after><ssml:token>Tabell</ssml:token> <ssml:token>slutt</ssml:token>.</after>
    </lang>
    <lang xml:lang="sv">
      <before><ssml:token>Tabell</ssml:token>.</before>
      <after><ssml:token>Slut</ssml:token> <ssml:token>tabell</ssml:token>.</after>
    </lang>
  </xsl:template>

  <xsl:template match="dt:code">
    <lang xml:lang="en">
      <before><ssml:token>Code</ssml:token>.</before>
      <after><ssml:token>End</ssml:token> <ssml:token>of</ssml:token> <ssml:token>code</ssml:token>.</after>
    </lang>
    <lang xml:lang="fi">
      <before><ssml:token>Koodi</ssml:token></before>
      <after><ssml:token>Koodin</ssml:token> <ssml:token>loppu</ssml:token>.</after>
    </lang>
    <lang xml:lang="fr">
      <before><ssml:token>Code</ssml:token></before>
      <after><ssml:token>Fin</ssml:token> <ssml:token>du</ssml:token> <ssml:token>code</ssml:token>.</after>
    </lang>
    <lang xml:lang="ja">
      <before><ssml:token>コード開始</ssml:token>.</before>
      <after><ssml:token>コード終了</ssml:token>.</after>
    </lang>
    <lang xml:lang="no">
      <before><ssml:token>Kode</ssml:token>.</before>
      <after><ssml:token>Kode</ssml:token> <ssml:token>slutt</ssml:token>.</after>
    </lang>
    <lang xml:lang="sv">
      <before><ssml:token>Kod</ssml:token></before>
      <after><ssml:token>Slut</ssml:token> <ssml:token>kod</ssml:token></after>
    </lang>
  </xsl:template>

  <xsl:template match="dt:kbd">
    <lang xml:lang="en">
      <before><ssml:token>Keyboard</ssml:token> <ssml:token>instruction</ssml:token>.</before>
    </lang>
    <lang xml:lang="fi">
      <before><ssml:token>Näppäimistöohje</ssml:token>.</before>
    </lang>
    <lang xml:lang="fr">
      <before><ssml:token>Instructions</ssml:token> <ssml:token>clavier</ssml:token>.</before>
    </lang>
    <lang xml:lang="ja">
      <before><ssml:token>キーボード操作説明</ssml:token>.</before>
    </lang>
    <lang xml:lang="no">
      <before><ssml:token>Tastaturinstruksjon</ssml:token>.</before>
    </lang>
    <lang xml:lang="sv">
      <before><ssml:token>Tangentbordsinstruktion</ssml:token>.</before>
    </lang>
  </xsl:template>

  <!-- <xsl:template match="m:math"> -->
  <!--   <lang xml:lang="en"> -->
  <!--     <before>Matemathical expression:</before> -->
  <!--     <after>End of matemathical expression.</after> -->
  <!--   </lang> -->
  <!--   <lang xml:lang="fi"> -->
  <!--     <before>Matemaattinen kaava:</before> -->
  <!--     <after>Matemaattisen kaavan loppu.</after> -->
  <!--   </lang> -->
  <!--   <lang xml:lang="fr"> -->
  <!--     <before>Formule mathématique:</before> -->
  <!--     <after>Fin de la formule.</after> -->
  <!--   </lang> -->
  <!--   <lang xml:lang="ja"> -->
  <!--     <before>数式　開始:</before> -->
  <!--     <after>数式　終了。</after> -->
  <!--   </lang> -->
  <!--   <lang xml:lang="sv"> -->
  <!--     <before>Matematisk formel:</before> -->
  <!--     <after>Slut på matematisk formel.</after> -->
  <!--   </lang> -->
  <!-- </xsl:template> -->

</xsl:stylesheet>
