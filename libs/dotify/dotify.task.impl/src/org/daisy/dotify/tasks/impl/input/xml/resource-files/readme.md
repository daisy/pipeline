# Localizations #
This folder contains localizations for the `XMLInputManager`.

## localization_catalog.xml ##
The localization_catalog.xml file contains a mapping between a locale and a folder.
The folder is relative to the XMLL10nResourceLocator class.

## input\_format\_catalog.xml ##
The input\_format\_catalog.xml file contains a mapping between a qualified root element
and a properties file. The properties file contains the validation and transformation
files to be used when this qualified root element is encountered.

## Localization keys ##
The values in this file are inserted into the finished product where applicable.
Not all strings will be inserted into every product.

### l10nrearjacketcopy ###
Heading for the rear jacket copy text.

Example
<pre>&lt;entry key="l10nrearjacketcopy"&gt;Baksidestext&lt;/entry&gt;</pre>

### l10nimagedescription ###
Heading for image descriptions.

Example
<pre>&lt;entry key="l10nimagedescription"&gt;Bildbeskrivning&lt;/entry&gt;</pre>
	
### l10ncolophon ###
Heading for the colophon.

Example
<pre>&lt;entry key="l10ncolophon"&gt;Tryckuppgifter&lt;/entry&gt;</pre>
	 
### l10ncaption ###
Heading for captions.

Example
<pre>&lt;entry key="l10ncaption"&gt;Bildtext&lt;/entry&gt;</pre>
	
### l10ntable ###
Heading for tables.

Example
<pre>&lt;entry key="l10ntable"&gt;Tabell&lt;/entry&gt;</pre>
	
### l10ntablepart ###
Heading for a part of a table. If a table has to be split in to several
parts in order to fit within the available space, this heading is added
to each part.
 
Example
<pre>&lt;entry key="l10ntablepart"&gt;Tabelldel&lt;/entry&gt;</pre>

### l10nTocHeadline ####
Heading for the table of contents.

Example
<pre>&lt;entry key="l10nTocHeadline"&gt;Innehåll&lt;/entry&gt;</pre>
	
### l10nTocDescription ####
A preamble added to the full document table of contents.

Example
<pre>&lt;entry key="l10nTocDescription"&gt;Sid­hän­vis­ning­ar till svart­skrifts­bo­ken står in­om pa­ren­tes.&lt;/entry&gt;</pre>

### l10nTocVolumeStart ####
Heading for each new volume in the full document table of contents. The string `{0}` can be
used to insert the current volume number. 

Example
<pre>&lt;entry key="l10nTocVolumeStart"&gt;Volym {0}&lt;/entry&gt;</pre>
	
### l10nTocVolumeHeading ####
Heading for the volume table of contents. In other words, the table of contents
detailing the contents of the current volume only.
The string `{0}` can be
used to insert the current volume number. 

Example
<pre>&lt;entry key="l10nTocVolumeHeading"&gt;Innehåll volym {0}&lt;/entry&gt;</pre>


### l10nTocVolumeXofY ####
A string indicating the volume number and the total number of volumes. Currently used
on the cover page.
The string `{0}` is used to insert the current volume number. 
The string `{1}` is used to insert the total volume count.

Example
<pre>&lt;entry key="l10nTocVolumeXofY"&gt;Volym {0} av {1}&lt;/entry&gt;</pre>

### l10nTocOneVolume ####
A string indicating that the book consists of only one volume. Used instead of `l10nTocVolumeXofY`
when the total volume count is 1.

Example
<pre>&lt;entry key="l10nTocOneVolume"&gt;En volym&lt;/entry&gt;</pre>

### l10nEndnotesHeading ####
Heading for a section of footnotes or endnotes collected at the end of the volume.
Note that the type of notes in the original publication doesn't effect the presence
of this heading. It merely indicates that the notes are at the end of the volume produced.

Example
<pre>&lt;entry key="l10nEndnotesHeading"&gt;Fotnoter till denna volym&lt;/entry&gt;</pre>
	
### l10nEndnotesPageStart ####
Heading for a section of footnotes or endnotes originating from the same original page.
The string `{0}` is used to insert the original page number. 

Example
<pre>&lt;entry key="l10nEndnotesPageStart"&gt;Sida {0}&lt;/entry&gt;</pre>

### l10nEndnotesPageHeader ####
A string used in the page header to indicate that the content of the page is footnotes or endnotes.
This is comparable to including the chapter name in the header.

Example
<pre>&lt;entry key="l10nEndnotesPageHeader"&gt;Noter&lt;/entry&gt;</pre>

### l10nSequenceInterruptedMsg ###
This text is inserted when the last page/sheet in the volume is ended early and the
sequence (for example, a chapter) continues in the next volume. This text is only inserted
between paragraphs.

Example
<pre>&lt;entry key="l10nSequenceInterruptedMsg"&gt;Fortsättning i nästa volym.&lt;/entry&gt;</pre>

### l10nInstructionsHeading ###
This text is inserted when a special instruction is given to the reader before the regular
content begins. For example, if the source material doesn’t have page numbers, the user could be
informed about this in a customized message. The heading that precedes this message is
specified in this entry.

Example
<pre>&lt;entry key="l10nInstructionsHeading"&gt;Till punktskriftsläsaren&lt;/entry&gt;</pre>

### l10nLang ###
The value of this key is not inserted into the finished product, 
instead it informs the software which language the contents is in.

Example
<pre>&lt;entry key="l10nLang"&gt;sv&tl;/entry&gt;</pre>