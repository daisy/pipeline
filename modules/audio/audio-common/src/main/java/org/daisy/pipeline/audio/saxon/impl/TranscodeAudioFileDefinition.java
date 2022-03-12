package org.daisy.pipeline.audio.saxon.impl;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Optional;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

import org.daisy.pipeline.audio.AudioDecoder;
import org.daisy.pipeline.audio.AudioEncoder;
import org.daisy.pipeline.audio.AudioFileTypes;
import org.daisy.pipeline.audio.AudioServices;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
	name = "pf:transcode-audio-file",
	service = { ExtensionFunctionDefinition.class }
)
public class TranscodeAudioFileDefinition extends ExtensionFunctionDefinition {

	private static final StructuredQName funcname = new StructuredQName(
		"pf",
		"http://www.daisy.org/ns/pipeline/functions",
		"transcode-audio-file");

	public StructuredQName getFunctionQName() {
		return funcname;
	}

	public SequenceType[] getArgumentTypes() {
		return new SequenceType[] {
			SequenceType.SINGLE_STRING,
			SequenceType.OPTIONAL_STRING,
			SequenceType.SINGLE_STRING,
			SequenceType.SINGLE_STRING
		};
	}

	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.SINGLE_STRING;
	}

	public ExtensionFunctionCall makeCallExpression() {
		return new ExtensionFunctionCall() {
			public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
				try {
					File inputFile; {
						URI uri = URI.create(arguments[0].head().getStringValue());
						try {
							inputFile = new File(uri);
						} catch (IllegalArgumentException e) {
							throw new XPathException("href parameter is not a file URI: " + uri, e);
						}
					}
					if (!inputFile.isFile())
						throw new XPathException("File does not exist: " + inputFile);
					Optional<AudioFileFormat.Type> oldFileType; {
						Item mediaType = arguments[1].head();
						if (mediaType != null) {
							oldFileType = Optional.ofNullable(AudioFileTypes.fromMediaType(mediaType.getStringValue()));
							if (!oldFileType.isPresent())
								throw new XPathException("new-file-type parameter is not a recognized mime type: " + mediaType);
						} else
							oldFileType = Optional.empty();
					}
					AudioFileFormat.Type newFileType; {
						String mediaType = arguments[2].head().getStringValue();
						newFileType = AudioFileTypes.fromMediaType(mediaType);
						if (newFileType == null)
							throw new XPathException("new-file-type parameter is not a recognized mime type: " + mediaType);
					}
					File outputDir; {
						URI uri = URI.create(arguments[3].head().getStringValue());
						try {
							outputDir = new File(uri);
						} catch (IllegalArgumentException e) {
							throw new XPathException("output-dir parameter is not a file URI: " + uri, e);
						}
					}
					if (outputDir.isFile())
						throw new XPathException("File already exist and is not a directory: " + outputDir);
					File outputFile; {
						String extension = newFileType.getExtension();
						String fileNameWithoutExtension = inputFile.getName().replaceAll("\\.[^\\.]+$", "");
						outputFile = new File(outputDir, fileNameWithoutExtension + "." + extension);
						if (outputFile.exists()) {
							int n = 2;
							do {
								outputFile = new File(outputDir, fileNameWithoutExtension + "_" + (n++) + "." + extension);
							} while (outputFile.exists());
						}
					}
					Optional<AudioDecoder> decoder; {
						if (oldFileType.isPresent()) {
							decoder = audioServices.newDecoder(oldFileType.get(), new HashMap<String,String>());
							if (!decoder.isPresent())
								throw new XPathException("No decoder found for file type " + oldFileType);
						} else
							decoder = audioServices.newDecoder(new HashMap<String,String>());
					}
					Optional<AudioEncoder> encoder = audioServices.newEncoder(newFileType, new HashMap<String,String>());
					if (!encoder.isPresent())
						throw new XPathException("No encoder found for file type " + newFileType);
					outputDir.mkdirs();
					AudioInputStream pcm; {
						try {
							pcm = decoder.get().decode(inputFile);
						} catch (IllegalArgumentException e) {
							throw new XPathException("Audio file could not be read (unsupported file type): " + inputFile, e);
						} catch (Throwable e) {
							throw new XPathException("Audio file could not be read: " + inputFile, e);
						}
					}
					try {
						encoder.get().encode(pcm, newFileType, outputFile);
					} catch (Throwable e) {
						throw new XPathException("Audio could not be written to file type: " + newFileType, e);
					}
					return new StringValue(outputFile.toURI().toASCIIString());
				} catch (XPathException e) {
					throw e;
				} catch (Throwable e) {
					throw new XPathException("Unexpected error in pf:transcode-audio-file", e);
				}
			}
		};
	}

	private AudioServices audioServices;

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
