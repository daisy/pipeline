# Text-To-Speech Configuration

The following scripts support the production of an aural presentation
of the document via speech synthesis, or "text-to-speech" (TTS):

- [dtbook-to-daisy3](http://daisy.github.io/pipeline/modules/dtbook-to-daisy3),
- [dtbook-to-epub3](http://daisy.github.io/pipeline/modules/dtbook-to-epub3) and
- [zedai-to-epub3](http://daisy.github.io/pipeline/modules/zedai-to-epub3)

The result is either an audio-only version of the document, or the
combination of a text layer and an audio layer synchronized with each
other.

The way text-to-speech is configured is common to all scripts. Speech
engines are configured with special properties, and the exact aural
rendering of the document (TTS voices, pronunciations, speech pitch,
speech rates, speech levels, etc.) is controlled with CSS style sheets
and PLS lexicons.

TTS configuration files may contain properties, (links to) aural CSS
style sheets, and (links to) PLS lexicons. The file format is as
follows:

~~~xml
<config>
  <property key="acapela.samplerate" value="44100"/>
  <property key="log" value="true"/>
  <voice engine="acapela" name="claire" lang="fr" gender="female-adult" priority="12"/>
  <css href="css/aural.css"/>
  <lexicon href="lexicons/fr.pls"/>
</config>
~~~

<!--
TODO: what about <annotations type="" href="">
                    ...
                 </annotations>
-->

Configuration files may either be specified "statically", through the
system property
[`tts.config`](http://daisy.github.io/pipeline/wiki/Configuration-Files#system-properties),
or "dynamically" through the optional script input.

Both relative and absolute paths are accepted as a value of the "href"
attributes. Relative paths are relative to the configuration file's
location.

<!-- Absolute paths work only when the Pipeline is running in "local
mode" (true by default, see
[Pipeline as a Service](http://daisy.github.io/pipeline/Get-Help/User-Guide/Pipeline-as-Service/)). -->

<!-- The elements can be put in any namespace since namespaces aren’t
checked. If there is any syntax error in the file, you will be
notified in the server’s logs. -->

<!-- TODO: invent a media type, for example:
`application/vnd.pipeline.tss-config+xml` -->

## Engine configuration

The audio encoder LAME must be installed.
<!-- LAME's location must be in the system PATH (i.e. $PATH on Unix,
%PATH% on Windows), unless it is provided via the 'lame.path' system
property. -->
In addition, one of the following text-to-speech processors must be
installed.

For Unix users,

- [Acapela][];
- [eSpeak][].

For Windows users,

- [eSpeak][];
- [SAPI][] with adequate voices.

For MacOS users,

- [say][].

It is strongly recommended to install eSpeak anyway, as it can handle
almost any language out there.
<!-- Just as Lame’s directory must be in the system PATH if it were
installed in a dedicated subdirectory, you may need to append eSpeak’s
installation directory to your PATH as well. On Unix systems, 'apt-get
install' already takes care of installing eSpeak to a known
location. On Windows, however, the PATH variable must be changed
manually using the 'environment variables' panel.-->

The audio encoder and the TTS processors are configured with
properties. The following properties are available:

### Common settings

`audio.tmpdir`
: Temporary directory used during audio synthesis
: Defaults to "${java.io.tmpdir}"

`threads.number`
: Number of threads for audio encoding and regular text-to-speech
: Defaults to the number of processors available to the JVM

`threads.encoding.number`
: Number of audio encoding threads
: Defaults to `threads.number` if not specified

`threads.speaking.number`
: Number of regular text-to-speech threads
: Defaults to `threads.number` if not specified

`threads.each.memlimit`
: Maximum amount of memory consumed by each text-to-speech thread (in Mb)
: Defaults to "20"

`encoding.speed`
: Maximum number of seconds of encoded audio per seconds of encoding
: Defaults to "2.0"

`log`
: If set to "true", will result in the Pipeline logging stuff in the
  output directory in a file named 'tts-log.xml'. The Pipeline will
  log a great deal of information to this file, which can be quite
  helpful for troubleshooting. Most of the log entries concern
  particular chunks of text of the input document. For more general
  errors, see the main server’s logs.
: Defaults to "false"
: dtbook-to-daisy3 only

### Acapela

`acapela.samplerate`
: Sample rate (in Hz)
: Defaults to "22050"

`acapela.threads.reserved`
: Number of reserved text-to-speech threads
: Defaults to "3"

`acapela.speed`
: Defaults to "300"

`acapela.servers`
: Defaults to "localhost:0"

`acapela.priority`
: This engine is chosen over another engine that serves the same voice
  if this one has a higher priority.
: Defaults to "15"

### AT&T

`att.servers`
: Defaults to "localhost:8888"

`att.priority`
: This engine is chosen over another engine that serves the same voice
  if this one has a higher priority.
: Defaults to "10"

### eSpeak

`espeak.priority`
: This engine is chosen over another engine that serves the same voice
  if this one has a higher priority.
: Defaults to "2"

In addition there is the
[`espeak.path`](http://daisy.github.io/pipeline/wiki/Configuration-Files#system-properties)
system property for setting the path to the eSpeak executable. If not
specified, Pipeline will automatically look for "espeak" in the
directories specified by the "PATH" environment variable.

### Mac OS

`osxspeech.priority`
: This engine is chosen over another engine that serves the same voice
  if this one has a higher priority.
: Defaults to "2"

In addition there is the
[`osxspeech.path`](http://daisy.github.io/pipeline/wiki/Configuration-Files#system-properties)
system property for setting an alternative path to OSX's command line
program "say" (default is "/usr/bin/say").

### SAPI

`sapi.samplerate`
: Sample rate (in Hz)
: Defaults to "22050"
: Can not be overridden at runtime. The server must be restarted to
  change this property.

`sapi.bytespersample`
: Defaults to "2"
: Can not be overridden at runtime. The server must be restarted to
  change this property.

`sapi.priority`
: This engine is chosen over another engine that serves the same voice
  if this one has a higher priority.
: Defaults to "7"

### LAME encoder

`lame.path`
: Path to LAME executable
: If not specified, will automatically look for "lame" in the
  directories specified by the environment variable "PATH".

`lame.cli.options`
: Additional command line options passed to lame


## CSS

The text-to-speech voices and prosody can be configured with Aural
CSS. To do so, add one or more "css" elements to the configuration
file. If the "href" attribute is missing, the CSS stylesheets will be
interpreted as inlined in the configuration file:

~~~xml
<config>
  <css>
    p {
      volume: soft;
      voice-family: female;
    }
  </css>
</config>
~~~

In addition to the configuration option, local CSS stylesheets
referenced in
['xml-stylesheet' processing instructions](https://www.w3.org/TR/xml-stylesheet)
and by ['links'](https://www.w3.org/Style/styling-XML#External) in the
header will be loaded too.

The CSS properties that are supported by DAISY Pipeline are a subset
of [Aural CSS 2.1](https://www.w3.org/TR/CSS2/aural.html) (and partly
inspired by [CSS 3 Speech](https://www.w3.org/TR/css3-speech)):

<!-- TODO: update completely to CSS 3 -->

[`voice-family`](https://www.w3.org/TR/CSS2/aural.html#propdef-voice-family)
: Used for selecting a voice based on gender, age, name and/or vendor.
: See text below.
<!-- see also <ssml:voice> -->

[`speak`](https://www.w3.org/TR/CSS2/aural.html#propdef-speak)
: "none" or "spell-out"
: Used for preventing certain text to be rendered aurally, or for
  spelling text one letter at a time.
<!-- see also CSS3 "speak" and "speak-as" properties -->
<!-- see also <ssml:say-as interpret-as="..."> -->

[`volume`](https://www.w3.org/TR/CSS2/aural.html#propdef-volume)
: A number, "silent", "x-soft", "soft", "medium", "loud" or "x-loud"
: Used for controlling the loudness of the speech.
<!-- see also CSS3 "voice-volume" property -->
<!-- see also <ssml:prosody volume="..."> -->

[`pitch`](https://www.w3.org/TR/CSS2/aural.html#propdef-pitch)
: "x-low", "low", "medium", "high" or "x-high"
: Used for controlling the average pitch of the speech.
<!-- see also CSS3 "voice-pitch" property -->
<!-- see also <ssml:prosody pitch="..."> -->

[`speech-rate`](https://www.w3.org/TR/CSS2/aural.html#propdef-speech-rate)
: A number, "x-slow", "slow", "medium", "fast" or "x-fast"
: Used for controlling the rate of the speech in terms of words per
  minute.
<!-- see also CSS3 "voice-rate" property -->
<!-- see also <ssml:prosody rate="..."> -->

[`pitch-range`](https://www.w3.org/TR/CSS2/aural.html#propdef-pitch-range)
: A number
: Used for controlling the variation in average pitch of the speech.
<!-- see also CSS3 "voice-range" property -->
<!-- see also <ssml:prosody range="..."> -->

[`speak-numeral`](https://www.w3.org/TR/CSS2/aural.html#propdef-speak-numeral)
: "digits" or "continuous"
: Used for speaking out numbers one digit at a time.
<!-- see also CSS3 "speak-as" property -->
<!-- see also <ssml:say-as interpret-as="..."> -->

[`pause-before`](https://www.w3.org/TR/CSS2/aural.html#propdef-pause-before), [`pause-after`](https://www.w3.org/TR/CSS2/aural.html#propdef-pause-after) and [`pause`](https://www.w3.org/TR/CSS2/aural.html#propdef-pause)
: A duration
: Used for specifying silences with a certain duration before or
  after an element.
<!-- see also <ssml:break time="..."> -->

[`cue-before`](https://www.w3.org/TR/CSS2/aural.html#propdef-cue-before), [`cue-after`](https://www.w3.org/TR/CSS2/aural.html#propdef-cue-after) and [`cue`](https://www.w3.org/TR/CSS2/aural.html#propdef-cue)
: A URL
: Used for playing pre-recorded sound clips before or after an
  element.
<!-- see also <ssml:audio src="..."> -->

<!--
- [`stress`](https://www.w3.org/TR/CSS2/aural.html#propdef-stress)
- [`richness`](https://www.w3.org/TR/CSS2/aural.html#propdef-richness)
- [`azimuth`](https://www.w3.org/TR/CSS2/aural.html#propdef-azimuth)
- [`play-during`](https://www.w3.org/TR/CSS2/aural.html#propdef-play-during)
- [`elevation`](https://www.w3.org/TR/CSS2/aural.html#propdef-elevation)
- [`speak-punctuation`](https://www.w3.org/TR/CSS2/aural.html#propdef-speak-punctuation)
- [`speak-header`](https://www.w3.org/TR/CSS2/aural.html#propdef-speak-header)
-->

`voice-family` is a comma-separated list of voice characteristics that
place conditions on the voice selection. It is inspired by (but not
the same as) the specification of the
[voice-family property in CSS 3](https://www.w3.org/TR/css3-speech#voice-family).

If a full voice name is provided, e.g. "acapela, alice", this voice
will be selected regardless of the document language. If this voice is
not available, a fallback voice will be chosen such that it will match
with the same characteristics as those of the requested voice: same
language, same engine, same gender. If none is available, the pipeline
broadens its search by relaxing the criteria: first the gender is
relaxed and then the engine.

If no voice name is provided, e.g. "acapela", "female" or "female,
old", the selection algorithm will take into consideration only the
voices that match the current language. It starts by looking for a
voice with the specified gender and supplied by the specified engine,
and will broaden to any gender if the first search yielded no
results. If neither the gender nor the engine match, language will be
the only criterion.

When multiple voices match the criteria, the algorithm chooses the
voice with the highest priority. Each voice has a default priority,
though they can be overridden via the "voice" entries of the
configuration file, as follows:

~~~xml
<config>
  <voice engine="sapi" name="Microsoft Todd" gender="male-adult" priority="100" lang="en"/>
</config>
~~~

<!-- TODO: "marks" attribute: you may set this attribute to "false" if
you have installed a new voice that can’t interpret SSML or mark-based
audio synchronization, which may compromise SAPI’s
initialization. Microsoft’s built-in voices do handle SSML and marks.
-->

Notice that it is also a convenient way to add voices that are not
natively supported by the Pipeline. In the example above, Todd is now
a registered voice and, as such, can be selected automatically by the
Pipeline when the document is written in English.

AT&T, eSpeak and Acapela's voice names can be found in their
corresponding documentation. For Windows users, SAPI voices are
enumerated in the system settings (Start > All Control Panel Items >
Speech Recognition > Advanced Speech Options). You will also need to
know the value of the "engine" attribute. This attribute must take as
value one of the following:

- "att" for AT&T voices;
- "espeak" for eSpeak voices;
- "acapela" for Acapela voices;
- "osx-speech" for Apple voices;
- "sapi" for Microsoft voices or for any other voice installed to work
  with the SAPI engine, including some versions of AT&T and Acapela’s
  products.

In case of any doubt, engines and voice names can be retrieved from
the server’s log in which all the voices are enumerated:

    Available voices:
    * {engine:'sapi', name:'NTMNTTS Voice (Male)'} by sapi-native
    * {engine:'acapela', name:'alice'} by acapela-jna

## PLS

PLS lexicons allow you to define custom pronunciations of words. It is
meant to help TTS processors deal with ambiguous abbreviations and
pronunciation of proper names. When a word is defined in a lexicon the
processor will use the provided pronunciation in place of the default
rendering.

Lexicons are configured using the "lexicon" elements in the
configuration file. If the "href" attribute is missing, the pipeline
will read the lexicons inside the config nodes, as in this example:

~~~xml
<config>
  <lexicon xmlns="http://www.w3.org/2005/01/pronunciation-lexicon" version="1.0"
           alphabet="ipa" xml:lang="en">
    <lexeme>
      ...
    </lexeme>
  </lexicon>
</config>
~~~

The syntax of a PLS lexicon is defined in
[Pronunciation Lexicon Specification Version 1.0](https://www.w3.org/TR/pronunciation-lexicon),
extended with regular expression matching. To enable regular
expression matching, add the "regex" attribute, as follows:

~~~xml
<lexicon xmlns="http://www.w3.org/2005/01/pronunciation-lexicon" version="1.0"
         alphabet="ipa" xml:lang="en">
  <lexeme regex="true">
    <grapheme>([0-9]+)-([0-9]+)</grapheme>
    <alias>between $1 and $2</alias>
  </lexeme>
</lexicon>
~~~

The regex feature works only with alias-based substitutions. The regex
syntax used is that from
[XQuery 1.0 and XPath 2.0](https://www.w3.org/TR/xpath-functions/#regex-syntax).

Whether or not the regex attribute is set to "true", the grapheme
matching can be made more accurate by specifying the
"positive-lookahead" and "negative-lookahead" attributes:

~~~xml
<lexicon version="1.0" xmlns="http://www.w3.org/2005/01/pronunciation-lexicon"
         alphabet="ipa" xml:lang="en">
  <lexeme>
    <grapheme positive-lookahead="[ ]+is">SB</grapheme>
    <alias>somebody</alias>
  </lexeme>
  <lexeme>
    <grapheme>SB</grapheme>
    <alias>should be</alias>
  </lexeme>
  <lexeme xml:lang="fr">
    <grapheme positive-lookahead="[ ]+[cC]ity">boston</grapheme>
    <phoneme>bɔstøn</phoneme>
  </lexeme>
</lexicon>
~~~

Graphemes with "positive-lookahead" will match if the beginning of
what follows matches the "position-lookahead" pattern. Graphemes with
"negative-lookahead" will match if the beginning of what follows does
not match the "negative-lookahead" pattern. The lookaheads are
case-sensitive while the grapheme contents are not.

The lexemes are reorganized so as to be matched in this order:

1. Graphemes with regex="false" come first, no matter if there is a lookahead or not;
2. Graphemes with regex="true" and no lookahead;
3. Graphemes with regex="true" and one or two lookaheads.

Within these categories, lexemes are matched in the same order as they
appear in the lexicons.



[Acapela]: http://www.acapela-group.com
[eSpeak]: http://espeak.sourceforge.net/
[SAPI]: https://en.wikipedia.org/wiki/Microsoft_Speech_API
[say]: https://developer.apple.com/legacy/library/documentation/Darwin/Reference/ManPages/man1/say.1.html
