# TTS Configuration

TTS supported in scripts
  - [dtbook-to-daisy3](http://daisy.github.io/pipeline/modules/dtbook-to-daisy3),
  - [dtbook-to-epub3](http://daisy.github.io/pipeline/modules/dtbook-to-epub3) and
  - [zedai-to-epub3](http://daisy.github.io/pipeline/modules/zedai-to-epub3)

## Engine configuration

Specify "static" configuration through system property
[`tts.config`](http://daisy.github.io/pipeline/wiki/Configuration-Files#system-properties)
(file path).

Specify "dynamic" configuration through script input.

### File media type

TODO: invent a media type, for example: `application/vnd.pipeline.tss-config+xml`

### File format

~~~xml
<config>
  <property key="x" value="..."/>
  <property key="y" value="..."/>
  <property key="z" value="..."/>
</config>
~~~

### Available properties

#### Common settings

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


#### Acapela

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

#### AT&T

`att.servers`
: Defaults to "localhost:8888"

`att.priority`
: This engine is chosen over another engine that serves the same voice
  if this one has a higher priority.
: Defaults to "10"

#### eSpeak

`espeak.priority`
: This engine is chosen over another engine that serves the same voice
  if this one has a higher priority.
: Defaults to "2"

#### Mac OS

`osxspeech.priority`
: This engine is chosen over another engine that serves the same voice
  if this one has a higher priority.
: Defaults to "2"

#### SAPI

`sapi.samplerate`
: Sample rate (in Hz)
: Defaults to "22050"

`sapi.bytespersample`
: Defaults to "2"

`sapi.priority`
: This engine is chosen over another engine that serves the same voice
  if this one has a higher priority.
: Defaults to "7"

#### LAME encoder

`lame.path`
: Path to LAME executable
: If not specified, will automatically look for "lame" in the
  directories specified by the environment variable "PATH"

`lame.cli.options`
: Additional command line options passed to lame


## Voice configuration

TBD

~~~xml
<voice xml:lang="..."
       lang="..."
       engine="..."
       name="..."
       priority="..."
       gender="..."
       marks="..."/>
~~~

## CSS

TBD

## PLS

TBD
