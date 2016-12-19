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

TTS configuration files may contain properties, links to aural CSS
style sheets, and links to PLS lexicons. The file format is as
follows:

~~~xml
<config>
  <property key="acapela.samplerate" value="44100"/>
  <css href="css/aural.css"/>
  <lexicon href="lexicons/fr.pls"/>
</config>
~~~

<!--
TODO: what about <voice engine="acapela"
                        name="claire"
                        lang="fr"
                        gender="female-adult"
                        priority="12"/>
TODO: what about <annotations type="" href="">
                    ...
                 </annotations>
-->

Configuration files may either be specified "statically", through the
system property
[`tts.config`](http://daisy.github.io/pipeline/wiki/Configuration-Files#system-properties),
or "dynamically" through a script input.

<!--
TODO: invent a media type, for example: `application/vnd.pipeline.tss-config+xml`
-->

## Engine configuration

TTS engines are configured with global properties. The following
properties are available:

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

### Mac OS

`osxspeech.priority`
: This engine is chosen over another engine that serves the same voice
  if this one has a higher priority.
: Defaults to "2"

### SAPI

`sapi.samplerate`
: Sample rate (in Hz)
: Defaults to "22050"

`sapi.bytespersample`
: Defaults to "2"

`sapi.priority`
: This engine is chosen over another engine that serves the same voice
  if this one has a higher priority.
: Defaults to "7"

### LAME encoder

`lame.path`
: Path to LAME executable
: If not specified, will automatically look for "lame" in the
  directories specified by the environment variable "PATH"

`lame.cli.options`
: Additional command line options passed to lame


## CSS

<!-- TODO: update to CSS 3: https://www.w3.org/TR/css3-speech -->

The CSS properties that are supported by DAISY Pipeline are:

[`voice-family`](https://www.w3.org/TR/CSS2/aural.html#propdef-voice-family)
: Used for selecting a voice based on gender, age, name and/or vendor.
<!-- see also CSS3 "voice-family" property -->
<!-- see also <ssml:voice> -->

[`speak`](https://www.w3.org/TR/CSS2/aural.html#propdef-speak)
: Used for preventing certain text to be rendered aurally, or for
  spelling text one letter at a time.
<!-- see also CSS3 "speak" and "speak-as" properties -->
<!-- see also <ssml:say-as interpret-as="..."> -->

[`volume`](https://www.w3.org/TR/CSS2/aural.html#propdef-volume)
: Used for controlling the loudness of the speech.
<!-- see also CSS3 "voice-volume" property -->
<!-- see also <ssml:prosody volume="..."> -->

[`pitch`](https://www.w3.org/TR/CSS2/aural.html#propdef-pitch)
: Used for controlling the average pitch of the speech.
<!-- see also CSS3 "voice-pitch" property -->
<!-- see also <ssml:prosody pitch="..."> -->

[`speech-rate`](https://www.w3.org/TR/CSS2/aural.html#propdef-speech-rate)
: Used for controlling the rate of the speech in terms of words per
  minute.
<!-- see also CSS3 "voice-rate" property -->
<!-- see also <ssml:prosody rate="..."> -->

[`pitch-range`](https://www.w3.org/TR/CSS2/aural.html#propdef-pitch-range)
: Used for controlling the variation in average pitch of the speech.
<!-- see also CSS3 "voice-range" property -->
<!-- see also <ssml:prosody range="..."> -->

[`speak-numeral`](https://www.w3.org/TR/CSS2/aural.html#propdef-speak-numeral)
: Used for speaking out numbers one digit at a time.
<!-- see also CSS3 "speak-as" property -->
<!-- see also <ssml:say-as interpret-as="..."> -->

[`pause-before`](https://www.w3.org/TR/CSS2/aural.html#propdef-pause-before), [`pause-after`](https://www.w3.org/TR/CSS2/aural.html#propdef-pause-after) and [`pause`](https://www.w3.org/TR/CSS2/aural.html#propdef-pause)
: Used for specifying silences with a certain duration before or
  after an element.
<!-- see also <ssml:break time="..."> -->

[`cue-before`](https://www.w3.org/TR/CSS2/aural.html#propdef-cue-before), [`cue-after`](https://www.w3.org/TR/CSS2/aural.html#propdef-cue-after) and [`cue`](https://www.w3.org/TR/CSS2/aural.html#propdef-cue)
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

## PLS

PLS lexicons allow you to define the pronunciations of words. When a
word is defined in a lexicon the TTS engine will use the provided
pronunciation in place of the default rendering. A PLS lexicon is an
XML file. The syntax is defined in
[https://www.w3.org/TR/pronunciation-lexicon](https://www.w3.org/TR/pronunciation-lexicon).
