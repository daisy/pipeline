<link rel="dp2:permalink" href="http://daisy.github.io/pipeline/Get-Help/User-Guide/Text-To-Speech/">

# Text-To-Speech Configuration

The following scripts support the production of an aural presentation
of the document via speech synthesis, or "text-to-speech" (TTS):

- [DTBook to DAISY 3](http://daisy.github.io/pipeline/Get-Help/User-Guide/Scripts/dtbook-to-daisy3),
- [DTBOOK to EPUB 3](http://daisy.github.io/pipeline/Get-Help/User-Guide/Scripts/dtbook-to-epub3),
- [ZedAI to EPUB 3](http://daisy.github.io/pipeline/Get-Help/User-Guide/Scripts/zedai-to-epub3),
- [EPUB 3 enhancer](http://daisy.github.io/pipeline/Get-Help/User-Guide/Scripts/epub3-to-epub3) and
- [EPUB to DAISY](http://daisy.github.io/pipeline/Get-Help/User-Guide/Scripts/epub-to-daisy)

The result is either an audio-only version of the document, or the
combination of a text layer and an audio layer synchronized with each
other.

The way text-to-speech is configured is common to all scripts. Speech
engines are configured with properties, and the exact aural rendering
of the document (TTS voices, pronunciations, speech pitch, speech
rates, speech levels, etc.) is controlled with CSS style sheets and
PLS lexicons.

The following two properties must be set through the
[pipeline.properties
file](http://daisy.github.io/pipeline/Configuration-Files#user-properties).

`org.daisy.pipeline.tts.config`
: File to load TTS configuration properties from at start-up
: Defaults to the file "tts-default-config.xml" located in the "etc/"
  directory in the base directory of the Pipeline installation, or
  "/etc/opt/daisy-pipeline2/tts-default-config.xml" on Debian/Ubuntu.

`org.daisy.pipeline.tts.host.protection`
: Allow dynamic setting of properties
: Defaults to "true"

All other TTS properties may be specified through either the
[pipeline.properties
file](http://daisy.github.io/pipeline/Configuration-Files#user-properties)
or special TTS configuration files. Aural CSS style sheets and PLS
lexicons must be specified in TTS configuration files.


Configuration files may either be specified "statically", through the
user property `org.daisy.pipeline.tts.config` or "dynamically" through
the optional script input. If `org.daisy.pipeline.tts.host.protection`
is true, properties in dynamic configuration files are ignored. The
configuration file format is as follows:

~~~xml
<config>
  <property key="org.daisy.pipeline.tts.acapela.samplerate" value="44100"/>
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

The following online text-to-speech processor can also be used for
all users, given they have an account for the service 
and the appropriate plan or license :

- [Google Cloud Text-To-Speech][]

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

`org.daisy.pipeline.tts.audio.tmpdir`
: Temporary directory used during audio synthesis
: Defaults to "${java.io.tmpdir}"

`org.daisy.pipeline.tts.mp3.bitrate`
: Bit rate of MP3 files

`org.daisy.pipeline.tts.maxmem`
: Maximum amount of memory in Mb to be used by TTS and audio encoding
: Defaults to 50% of the total amount of memory that the JVM will
  attempt to use, or 500 Mb if there is no such limit
: FIXME

`org.daisy.pipeline.tts.threads.number`
: Number of threads for audio encoding and regular text-to-speech
: Defaults to the number of processors available to the JVM

`org.daisy.pipeline.tts.threads.encoding.number`
: Number of audio encoding threads
: Defaults to "${org.daisy.pipeline.tts.threads.number}"

`org.daisy.pipeline.tts.threads.speaking.number`
: Number of regular text-to-speech threads
: Defaults to "${org.daisy.pipeline.tts.threads.number}"

`org.daisy.pipeline.tts.threads.each.memlimit`
: Maximum amount of memory consumed by each text-to-speech thread (in Mb)
: Defaults to "20"

`org.daisy.pipeline.tts.encoding.speed`
: Maximum number of seconds of encoded audio per seconds of encoding
: Defaults to "2.0"

### Acapela

`org.daisy.pipeline.tts.acapela.samplerate`
: Sample rate (in Hz)
: Defaults to "22050"

`org.daisy.pipeline.tts.acapela.threads.reserved`
: Number of reserved text-to-speech threads
: Defaults to "3"

`org.daisy.pipeline.tts.acapela.speed`
: Defaults to "300"

`org.daisy.pipeline.tts.acapela.servers`
: Defaults to "localhost:0"

`org.daisy.pipeline.tts.acapela.priority`
: This engine is chosen over another engine that serves the same voice
  if this one has a higher priority.
: Defaults to "15"

<!--
### AT&T

tts-adapter-attnative:

`org.daisy.pipeline.tts.att.servers`
: Address of the AT&T server
: Defaults to "localhost:8888"

`org.daisy.pipeline.tts.att.priority`
: Priority with which the AT&T engine is chosen over other TTS engines
  that serve the same voice.
: Defaults to "10"


tts-adapter-attbin:

`org.daisy.pipeline.tts.att.servers`
: Address of the AT&T server
: Defaults to "localhost:8888"

`org.daisy.pipeline.tts.att.client.path`
: Path to the "TTSClientFile" executable
: If not specified, Pipeline will automatically look for
  "TTSClientFile" in the directories specified by the "PATH"
  environment variable.

`org.daisy.pipeline.tts.att.bin.priority`
: Priority with which the AT&T engine is chosen over other TTS engines
  that serve the same voice.
: Defaults to "5"

-->

### eSpeak

`org.daisy.pipeline.tts.espeak.path`
: Path to eSpeak executable
: If not specified, Pipeline will automatically look for "espeak" in
  the directories specified by the "PATH" environment variable.

`org.daisy.pipeline.tts.espeak.priority`
: This engine is chosen over another engine that serves the same voice
  if this one has a higher priority.
: Defaults to "2"


### Mac OS

`org.daisy.pipeline.tts.osxspeech.path`
: Alternative path to OSX's command line program "say"
: Defaults to "/usr/bin/say"

`org.daisy.pipeline.tts.osxspeech.priority`
: This engine is chosen over another engine that serves the same voice
  if this one has a higher priority.
: Defaults to "2"

### SAPI

`org.daisy.pipeline.tts.sapi.samplerate`
: Sample rate (in Hz)
: Defaults to "22050"
: Can not be overridden at runtime. The server must be restarted to
  change this property.

`org.daisy.pipeline.tts.sapi.bytespersample`
: Defaults to "2"
: Can not be overridden at runtime. The server must be restarted to
  change this property.

`org.daisy.pipeline.tts.sapi.priority`
: This engine is chosen over another engine that serves the same voice
  if this one has a higher priority.
: Defaults to "7"

### Qfrency

`org.daisy.pipeline.tts.qfrency.path`
: Path to "synth" executable
: If not specified, will automatically look for "synth" in the
  directories specified by the environment variable "PATH".

`org.daisy.pipeline.tts.qfrency.address`
: Address of the Qfrency server
: Defaults to "localhost"

`org.daisy.pipeline.tts.qfrency.priority`
: This engine is chosen over another engine that serves the same voice
  if this one has a higher priority.
: Defaults to "2"

### Google Cloud Text-to-speech

`org.daisy.pipeline.tts.google.apikey`
: (Mandatory) API key to connect to Google Text-To-Speech service
: See [Google API key page](https://cloud.google.com/docs/authentication/api-keys)
  for more information.

`org.daisy.pipeline.tts.google.samplerate`
: Sample rate (in Hz)
: Defaults to "22050"

`org.daisy.pipeline.tts.google.priority`
: This engine is chosen over another engine that serves the same voice
  if this one has a higher priority.
: Defaults to "15"

### CereProc

`org.daisy.pipeline.tts.cereproc.server`
: Host address of CereProc server
: Defaults to "localhost"

`org.daisy.pipeline.tts.cereproc.port`
: Port of CereProc server for regular voices
: Mandatory

`org.daisy.pipeline.tts.cereproc.client`
: Location of client program for communicating with CereProc server
: Defaults to "/usr/bin/cspeechclient"

`org.daisy.pipeline.tts.cereproc.priority`
: This engine is chosen over another engine that serves the same voice
  if this one has a higher priority.
: Defaults to "15"

`org.daisy.pipeline.tts.cereproc.dnn.port`
: Port of CereProc server for DNN voices
: Mandatory

`org.daisy.pipeline.tts.cereproc.dnn.priority`
: This engine is chosen over another engine that serves the same voice
  if this one has a higher priority.
: Defaults to "15"

### LAME encoder

`org.daisy.pipeline.tts.lame.path`
: Path to LAME executable
: If not specified, will automatically look for "lame" in the
  directories specified by the environment variable "PATH".

`org.daisy.pipeline.tts.lame.cli.options`
: Additional command line options passed to lame
: Deprecated

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
[Google Cloud Text-To-Speech]: https://cloud.google.com/text-to-speech
