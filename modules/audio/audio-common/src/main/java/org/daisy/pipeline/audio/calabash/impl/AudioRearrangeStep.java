package org.daisy.pipeline.audio.calabash.impl;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.file.Files;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.END_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;

import com.google.common.collect.ImmutableMap;
import static com.google.common.io.Files.getFileExtension;

import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.model.RuntimeValue;
import com.xmlcalabash.runtime.XAtomicStep;

import net.sf.saxon.s9api.SaxonApiException;

import org.daisy.common.messaging.MessageAppender;
import org.daisy.common.messaging.MessageBuilder;
import org.daisy.common.stax.BaseURIAwareXMLStreamReader;
import org.daisy.common.transform.InputValue;
import org.daisy.common.transform.OutputValue;
import org.daisy.common.transform.TransformerException;
import org.daisy.common.transform.XMLInputValue;
import org.daisy.common.transform.XMLOutputValue;
import org.daisy.common.transform.XMLTransformer;
import org.daisy.common.xproc.calabash.XMLCalabashInputValue;
import org.daisy.common.xproc.calabash.XMLCalabashOutputValue;
import org.daisy.common.xproc.calabash.XProcStep;
import org.daisy.common.xproc.calabash.XProcStepProvider;
import org.daisy.common.xproc.XProcMonitor;
import static org.daisy.pipeline.file.FileUtils.normalizeURI;
import org.daisy.pipeline.fileset.Fileset;
import org.daisy.pipeline.audio.AudioClip;
import org.daisy.pipeline.audio.AudioDecoder;
import org.daisy.pipeline.audio.AudioEncoder;
import org.daisy.pipeline.audio.AudioFileTypes;
import org.daisy.pipeline.audio.AudioServices;
import org.daisy.pipeline.audio.AudioUtils;
import org.daisy.pipeline.audio.PCMAudioFormat;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

public class AudioRearrangeStep extends DefaultStep implements XProcStep {

	@Component(
		name = "px:audio-rearrange",
		service = { XProcStepProvider.class },
		property = { "type:String={http://www.daisy.org/ns/pipeline/xproc}audio-rearrange" }
	)
	public static class Provider implements XProcStepProvider {

		private AudioServices audioServices;

		@Override
		public XProcStep newStep(XProcRuntime runtime, XAtomicStep step, XProcMonitor monitor, Map<String,String> properties) {
			return new AudioRearrangeStep(runtime, step, audioServices);
		}

		@Reference(
			name = "AudioServices",
			unbind = "-",
			service = AudioServices.class,
			cardinality = ReferenceCardinality.MANDATORY,
			policy = ReferencePolicy.STATIC
		)
		protected void setAudioServices(AudioServices audioServices) {
			this.audioServices = audioServices;
		}
	}

	private final static QName _SOURCE = new QName("source");
	private final static QName _RESULT = new QName("result");
	private final static QName _DESIRED = new QName("desired");
	private final static QName _TEMP_FILES = new QName("temp-files");

	private ReadablePipe sourcePipe = null;
	private ReadablePipe desiredPipe = null;
	private WritablePipe resultPipe = null;
	private WritablePipe tempFilesPipe = null;

	private final AudioServices audioServices;
	private String tempDirOption = null;

	private AudioRearrangeStep(XProcRuntime runtime, XAtomicStep step, AudioServices audioServices) {
		super(runtime, step);
		this.audioServices = audioServices;
	}

	@Override
	public void setInput(String port, ReadablePipe pipe) {
		if (_SOURCE.getLocalPart().equals(port))
			sourcePipe = pipe;
		else
			desiredPipe = pipe;
	}

	@Override
	public void setOutput(String port, WritablePipe pipe) {
		if (_RESULT.getLocalPart().equals(port))
			resultPipe = pipe;
		else if (_TEMP_FILES.getLocalPart().equals(port))
			tempFilesPipe = pipe;
	}

	@Override
	public void setOption(net.sf.saxon.s9api.QName name, RuntimeValue value) {
		if ("temp-dir".equals(name.getLocalName()))
			tempDirOption = value.getString();
	}

	@Override
	public void reset() {
		sourcePipe.resetReader();
		desiredPipe.resetReader();
		resultPipe.resetWriter();
		tempFilesPipe.resetWriter();
	}

	@Override
	public void run() throws SaxonApiException {
		super.run();
		try {
			new AudioRearrange()
				.transform(
					ImmutableMap.of(_SOURCE, new XMLCalabashInputValue(sourcePipe),
					                _DESIRED, new XMLCalabashInputValue(desiredPipe)),
					ImmutableMap.of(_RESULT, new XMLCalabashOutputValue(resultPipe, runtime),
					                _TEMP_FILES, new XMLCalabashOutputValue(tempFilesPipe, runtime)))
				.run();
		} catch (Throwable e) {
			throw XProcStep.raiseError(e, step);
		}
	}

	private static final QName D_CLIP = new QName("http://www.daisy.org/ns/pipeline/data", "clip", "d");
	private static final QName _CLIPBEGIN = new QName("clipBegin");
	private static final QName _CLIPEND = new QName("clipEnd");
	private static final QName _ORIGINAL_CLIPBEGIN = new QName("original-clipBegin");
	private static final QName _ORIGINAL_CLIPEND = new QName("original-clipEnd");

	private class AudioRearrange implements XMLTransformer {

		public Runnable transform(Map<QName,InputValue<?>> input, Map<QName,OutputValue<?>> output) {
			input = XMLTransformer.validateInput(input, ImmutableMap.of(   _SOURCE,     InputType.MANDATORY_NODE_SINGLE,
			                                                               _DESIRED,    InputType.MANDATORY_NODE_SINGLE));
			output = XMLTransformer.validateOutput(output, ImmutableMap.of(_RESULT,     OutputType.NODE_SEQUENCE,
			                                                               _TEMP_FILES, OutputType.NODE_SEQUENCE));
			XMLInputValue<?> sourceXML = (XMLInputValue<?>)input.get(_SOURCE);
			XMLInputValue<?> desiredXML = (XMLInputValue<?>)input.get(_DESIRED);
			XMLOutputValue<?> resultXML = (XMLOutputValue<?>)output.get(_RESULT);
			XMLOutputValue<?> tempFilesXML = (XMLOutputValue<?>)output.get(_TEMP_FILES);
			File tempDir; {
				if (tempDirOption != null && !tempDirOption.isEmpty()) {
					try {
						tempDir = new File(new URI(tempDirOption));
					} catch (URISyntaxException e) {
						throw new RuntimeException("temp-dir option invalid: " + tempDirOption);
					}
					if (tempDir.exists() && tempDir.listFiles().length > 0)
						throw new RuntimeException("temp-dir option must be a non-existing directory: " + tempDirOption);
				} else {
					try {
						tempDir = Files.createTempDirectory("pipeline-").toFile();
						tempDir.deleteOnExit();
					} catch (IOException e) {
						throw new TransformerException(e);
					}
				}
			}
			return () -> {
				Map<URI,Fileset.File> source;
				SortedMap<AudioClip,AudioClip> desired;
				String resultMediaType;
				URI resultBaseURI;
				URI resultXmlBase; {
					try {
						source = new HashMap<>();
						for (Fileset.File f : Fileset.unmarshall(sourceXML.asXMLStreamReader()))
							if (!source.containsKey(f.href))
								source.put(f.href, f);
						desired = new TreeMap<>(new Comparator<AudioClip>() {
								public int compare(AudioClip c1, AudioClip c2) {
									int r = c1.src.compareTo(c2.src);
									if (r != 0) return r;
									r = c1.clipBegin.compareTo(c2.clipBegin);
									if (r != 0) return r;
									return c1.clipEnd.compareTo(c2.clipEnd); }});
						resultMediaType = null;
						BaseURIAwareXMLStreamReader xml = desiredXML.asXMLStreamReader();
						URI filesetBase = xml.getBaseURI();
						boolean hasXmlBase = false;
						int depth = 0;
						URI href = null;
						URI originalHref = null;
					  document: while (true) {
							try {
								int event = xml.next();
								switch (event) {
								case START_DOCUMENT:
									break;
								case END_DOCUMENT:
									break document;
								case START_ELEMENT:
									if (depth == 0 && Fileset.XMLConstants.D_FILESET.equals(xml.getName())) {
										for (int i = 0; i < xml.getAttributeCount(); i++)
											if (Fileset.XMLConstants.XML_BASE.equals(xml.getAttributeName(i))) {
												filesetBase = filesetBase.resolve(xml.getAttributeValue(i));
												hasXmlBase = true;
												break;
											}
										depth++;
										break;
									} else if (depth == 1 && Fileset.XMLConstants.D_FILE.equals(xml.getName())) {
										href = null;
										originalHref = null;
										String mediaType = null;
										String originalMediaType = null;
										for (int i = 0; i < xml.getAttributeCount(); i++)
											if (Fileset.XMLConstants._HREF.equals(xml.getAttributeName(i)))
												href = filesetBase.resolve(xml.getAttributeValue(i));
											else if (Fileset.XMLConstants._ORIGINAL_HREF.equals(xml.getAttributeName(i)))
												originalHref = filesetBase.resolve(xml.getAttributeValue(i));
											else if (Fileset.XMLConstants._MEDIA_TYPE.equals(xml.getAttributeName(i)))
												mediaType = xml.getAttributeValue(i);
											else if (Fileset.XMLConstants._MEDIA_TYPE.equals(xml.getAttributeName(i)))
												originalMediaType = xml.getAttributeValue(i);
										if (mediaType != null && href != null && originalHref != null) {
											originalHref = normalizeURI(originalHref);
											if (source.containsKey(originalHref)) {
												if (resultMediaType == null) {
													resultMediaType = mediaType;
													if (AudioFileTypes.fromMediaType(mediaType) == null)
														throw new TransformerException(
															new RuntimeException("media-type is not a recognized mime type: " + mediaType));
												} else if (!resultMediaType.equals(mediaType))
													throw new TransformerException(
														new RuntimeException("media-type of all files must be the same, but got '"
														                     + resultMediaType + "' and '" + mediaType + "'"));
												if (originalMediaType != null
												    && AudioFileTypes.fromMediaType(originalMediaType) == null)
													throw new TransformerException(
														new RuntimeException("media-type is not a recognized mime type: " + originalMediaType));
												href = normalizeURI(href);
												try {
													new File(href);
													depth++;
													break;
												} catch (IllegalArgumentException e) {
													// href does not point to a file
												}
											}
										}
									} else if (depth == 2 && D_CLIP.equals(xml.getName())) {
										BigDecimal clipBegin = null;
										BigDecimal clipEnd = null;
										BigDecimal originalClipBegin = null;
										BigDecimal originalClipEnd = null;
										for (int i = 0; i < xml.getAttributeCount(); i++)
											try {
												if (_CLIPBEGIN.equals(xml.getAttributeName(i)))
													clipBegin = new BigDecimal(xml.getAttributeValue(i));
												else if (_CLIPEND.equals(xml.getAttributeName(i)))
													clipEnd = new BigDecimal(xml.getAttributeValue(i));
												else if (_ORIGINAL_CLIPBEGIN.equals(xml.getAttributeName(i)))
													originalClipBegin = new BigDecimal(xml.getAttributeValue(i));
												else if (_ORIGINAL_CLIPEND.equals(xml.getAttributeName(i)))
													originalClipEnd = new BigDecimal(xml.getAttributeValue(i));
											} catch (NumberFormatException e) {
											}
										if (clipBegin != null && clipEnd != null && originalClipBegin != null && originalClipEnd != null)
											desired.put(
												new AudioClip(
													href,
													Duration.ofMillis(clipBegin.movePointRight(3).longValue()),
													Duration.ofMillis(clipEnd.movePointRight(3).longValue())),
												new AudioClip(
													originalHref,
													Duration.ofMillis(originalClipBegin.movePointRight(3).longValue()),
													Duration.ofMillis(originalClipEnd.movePointRight(3).longValue())));
									}
									{ // consume whole element
										int d = depth + 1;
									  element: while (true) {
											event = xml.next();
											switch (event) {
											case START_ELEMENT:
												d++;
												break;
											case END_ELEMENT:
												d--;
												if (d == depth) break element;
											default:
											}
										}
									}
									break;
								case END_ELEMENT:
									depth--;
									break;
								default:
								}
							} catch (NoSuchElementException e) {
								break;
							}
						}
						resultBaseURI = filesetBase;
						resultXmlBase = hasXmlBase ? resultBaseURI : null;
					} catch (XMLStreamException e) {
						throw new TransformerException(e);
					}
					// check that destination clips don't overlap (they are already in order)
					{
						AudioClip prevClip = null;
						for (AudioClip clip : desired.keySet()) {
							if (prevClip == null) {
								prevClip = clip;
								continue;
							}
							if (prevClip.src.equals(clip.src) && clip.clipBegin.compareTo(prevClip.clipEnd) < 0)
								throw new IllegalArgumentException("Invalid input: overlapping clips: " + prevClip + ", " + clip);
							prevClip = clip;
						}
					}
				}
				List<Fileset.File> tempFiles = new ArrayList<>();
				List<Fileset.File> result = new ArrayList<>(); {
					if (!desired.isEmpty()) {
						AudioFileFormat.Type resultFileType = AudioFileTypes.fromMediaType(resultMediaType);
						Optional<AudioEncoder> encoder = audioServices.newEncoder(resultFileType, new HashMap<String,String>());
						if (!encoder.isPresent())
							throw new RuntimeException("No encoder found for file type " + resultFileType);
						AudioClip prevSourceClip = null;
						Fileset.File currentSourceFile = null;
						PCMAudioFormat audioFormat = null;
						AudioInputStream currentSourcePCM = null;
						long currentSourceElapsed = 0; // in frames
						URI currentDestinationFile = null;
						List<AudioInputStream> currentDestinationPCM = new ArrayList<>();
						long currentDestinationElapsed = 0; // in frames
						int fileCounter = 0;
						MessageAppender progress = MessageAppender.getActiveBlock(); // px:audio-rearrange step
						BigDecimal progressInc = BigDecimal.ONE.divide(new BigDecimal(desired.size()), MathContext.DECIMAL128);
						int clipCounter = 0;
						for (Map.Entry<AudioClip,AudioClip> entry : desired.entrySet()) {
							AudioClip destinationClip = entry.getKey(); // destination clips are in order and don't overlap
							AudioClip sourceClip = entry.getValue();
							if (currentDestinationFile == null)
								currentDestinationFile = destinationClip.src;
							else if (!currentDestinationFile.equals(destinationClip.src)) {
								File resultFile = new File(currentDestinationFile);
								File tempFile = new File(tempDir, "tmp" + (++fileCounter) + "." + getFileExtension(resultFile.getName()));
								if (tempDirOption == null || tempDirOption.isEmpty())
									tempFile.deleteOnExit();
								try {
									tempFile.getParentFile().mkdirs();
									encoder.get().encode(AudioUtils.concat(currentDestinationPCM), resultFileType, tempFile);
								} catch (Throwable e) {
									throw new TransformerException(
										new IOException("Audio could not be written to file type: " + resultFileType, e));
								}
								Fileset.File f = Fileset.File.load(tempFile, Optional.of(resultMediaType));
								tempFiles.add(f);
								result.add(f.copy(resultFile.toURI()));
								currentDestinationFile = destinationClip.src;
								currentDestinationPCM = new ArrayList<>();
								currentDestinationElapsed = 0;
							}
							if (currentSourceFile == null                                     // first clip
							    || !currentSourceFile.href.equals(sourceClip.src)             // new file
							    || sourceClip.clipBegin.compareTo(prevSourceClip.clipEnd) < 0 // clips in same audio file overlap or are not in order
							) {
								currentSourceFile = source.get(sourceClip.src);
								Optional<AudioFileFormat.Type> sourceFileType; {
									Optional<String> sourceMediaType = currentSourceFile.mediaType;
									if (sourceMediaType.isPresent())
										sourceFileType = Optional.of(AudioFileTypes.fromMediaType(sourceMediaType.get()));
									else
										sourceFileType = Optional.empty();
								}
								Optional<AudioDecoder> decoder; {
									if (sourceFileType.isPresent()) {
										decoder = audioServices.newDecoder(sourceFileType.get(), new HashMap<String,String>());
										if (!decoder.isPresent())
											throw new TransformerException(
												new RuntimeException("No decoder found for file type " + sourceFileType));
									} else
										decoder = audioServices.newDecoder(new HashMap<String,String>());
								}
								try {
									currentSourcePCM = decoder.get().decode(currentSourceFile.read());
								} catch (UnsupportedAudioFileException e) {
									throw new TransformerException(
										new IOException("Audio file could not be read (unsupported file type): "
										                + currentSourceFile.href, e));
								} catch (Throwable e) {
									throw new TransformerException(
										new IOException("Audio file could not be read: " + currentSourceFile.href, e));
								}
								currentSourceElapsed = 0;
								if (audioFormat == null)
									audioFormat = PCMAudioFormat.of(currentSourcePCM.getFormat());
								else if (!audioFormat.matches(currentSourcePCM.getFormat()))
									throw new TransformerException(
										new RuntimeException("All input audio must have the same format"));
							}
							// convert clip begin/end times to frames
							long sourceClipBegin = AudioUtils.getLengthInFrames(audioFormat, sourceClip.clipBegin);
							long sourceClipEnd = AudioUtils.getLengthInFrames(audioFormat, sourceClip.clipEnd);
							long destinationClipBegin = AudioUtils.getLengthInFrames(audioFormat, destinationClip.clipBegin);
							long destinationClipEnd = AudioUtils.getLengthInFrames(audioFormat, destinationClip.clipEnd);
							if (sourceClipBegin < currentSourceElapsed)
								throw new IllegalStateException(); // can not happen because if clips overlap or are not in
								                                   // order, the audio file is read again
							long currentSourceRemaining = currentSourcePCM.getFrameLength();
							if (currentSourceRemaining == 0) {
								// audio missing in the input; will be missing in the output too
							} else {
								AudioInputStream clipPCM; {
									long sourceClipLength = sourceClipEnd - sourceClipBegin;
									long destinationClipLength = destinationClipEnd - destinationClipBegin;
									long clipLength = Math.min(sourceClipLength, destinationClipLength);
									if (clipLength > currentSourceRemaining) {
										// part of audio missing in the input; will be missing in the output too
										clipLength = currentSourceRemaining;
									}
									Iterator<AudioInputStream> chunks = AudioUtils.split(
										currentSourcePCM,
										sourceClipBegin - currentSourceElapsed,
										sourceClipBegin - currentSourceElapsed + clipLength
									).iterator();
									currentSourceElapsed += chunks.next().getFrameLength();
									clipPCM = chunks.next();
									currentSourcePCM = chunks.next();
									currentSourceElapsed += clipPCM.getFrameLength();
								}
								if (destinationClipBegin < currentDestinationElapsed)
									throw new IllegalStateException(); // can not happen because clips are in order and don't overlap
								AudioInputStream silence = AudioUtils.createSilence(
									clipPCM.getFormat(),
									destinationClipBegin - currentDestinationElapsed);
								if (silence != null) {
									currentDestinationPCM.add(silence);
									currentDestinationElapsed += silence.getFrameLength();
								}
								currentDestinationPCM.add(clipPCM);
								currentDestinationElapsed += clipPCM.getFrameLength();
							}
							prevSourceClip = sourceClip;
							if (++clipCounter == 10) {
								clipCounter = 0;
								progress.append(new MessageBuilder().withProgress(progressInc)).close();
							}
						}
						progress.append(new MessageBuilder().withProgress(progressInc)).close();
						File resultFile = new File(currentDestinationFile);
						File tempFile = new File(tempDir, "tmp" + (++fileCounter) + "." + getFileExtension(resultFile.getName()));
						if (tempDirOption == null || tempDirOption.isEmpty())
							tempFile.deleteOnExit();
						try {
							tempFile.getParentFile().mkdirs();
							encoder.get().encode(AudioUtils.concat(currentDestinationPCM), resultFileType, tempFile);
						} catch (Throwable e) {
							throw new TransformerException(
								new IOException("Audio could not be written to file (type " + resultFileType + "): " + tempFile, e));
						}
						Fileset.File f = Fileset.File.load(tempFile, Optional.of(resultMediaType));
						tempFiles.add(f);
						result.add(f.copy(resultFile.toURI()));
					}
				}
				resultXML.writeXMLStream(xml -> {
						try {
							xml.setBaseURI(resultBaseURI);
							Fileset.marshall(xml, resultXmlBase, result);
						} catch (XMLStreamException e) {
							throw new TransformerException(e);
						}
					});
				tempFilesXML.writeXMLStream(xml -> {
						try {
							URI base = tempDir.toURI();
							xml.setBaseURI(base);
							Fileset.marshall(xml, base, tempFiles);
						} catch (XMLStreamException e) {
							throw new TransformerException(e);
						}
					});
			};
		}
	}
}
