package org.daisy.pipeline.audio.saxon.impl;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.daisy.common.transform.TransformerException;
import org.daisy.common.xpath.saxon.ExtensionFunctionProvider;
import org.daisy.common.xpath.saxon.ReflexiveExtensionFunctionProvider;
import org.daisy.pipeline.audio.AudioClip;
import org.daisy.pipeline.audio.AudioDecoder;
import org.daisy.pipeline.audio.AudioEncoder;
import org.daisy.pipeline.audio.AudioFileTypes;
import org.daisy.pipeline.audio.AudioServices;
import org.daisy.pipeline.audio.AudioUtils;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
	name = "TranscodeAudioFile",
	service = { ExtensionFunctionProvider.class }
)
public class TranscodeAudioFileDefinition extends ReflexiveExtensionFunctionProvider {

	public TranscodeAudioFileDefinition() {
		super(TranscodeAudioFile.class);
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

	public class TranscodeAudioFile {

		public Map<String,Object> run(URI href, Optional<String> oldMediaType, String newMediaType, URI outputDirURI) {
			File inputFile; {
				try {
					inputFile = new File(href);
				} catch (IllegalArgumentException e) {
					throw new IllegalArgumentException("href parameter is not a file URI: " + href, e);
				}
			}
			if (!inputFile.isFile())
				throw new IllegalArgumentException(new IOException("File does not exist: " + inputFile));
			Optional<AudioFileFormat.Type> oldFileType; {
				if (oldMediaType.isPresent()) {
					String mediaType = oldMediaType.get();
					AudioFileFormat.Type fileType = AudioFileTypes.fromMediaType(mediaType);
					if (fileType == null)
						throw new TransformerException(
							AudioErrors.ERR_AUDIO_001,
							new IllegalArgumentException("media-type is not a recognized mime type: " + mediaType));
					oldFileType = Optional.of(fileType);
				} else
					oldFileType = Optional.empty();
			}
			AudioFileFormat.Type newFileType = AudioFileTypes.fromMediaType(newMediaType);
			if (newFileType == null)
				throw new IllegalArgumentException("new-file-type parameter is not a recognized mime type: " + newMediaType);
			File outputDir; {
				try {
					outputDir = new File(outputDirURI);
				} catch (IllegalArgumentException e) {
					throw new IllegalArgumentException("output-dir parameter is not a file URI: " + outputDirURI, e);
				}
			}
			if (outputDir.isFile())
				throw new IllegalArgumentException(new IOException("File already exist and is not a directory: " + outputDir));
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
						throw new RuntimeException("No decoder found for file type " + oldFileType);
				} else
					decoder = audioServices.newDecoder(new HashMap<String,String>());
			}
			Optional<AudioEncoder> encoder = audioServices.newEncoder(newFileType, new HashMap<String,String>());
			if (!encoder.isPresent())
				throw new RuntimeException("No encoder found for file type " + newFileType);
			outputDir.mkdirs();
			AudioInputStream pcm; {
				try {
					pcm = decoder.get().decode(inputFile);
				} catch (UnsupportedAudioFileException e) {
					throw new UncheckedIOException(
						new IOException("Audio file could not be read (unsupported file type): " + inputFile, e));
				} catch (Throwable e) {
					throw new UncheckedIOException(new IOException("Audio file could not be read: " + inputFile, e));
				}
			}
			try {
				AudioClip clip = encoder.get().encode(pcm, newFileType, outputFile);
				Map<String,Object> rv = new HashMap<>();
				rv.put("href", outputFile.toURI());
				Duration originalDuration = AudioUtils.getDuration(pcm);
				if (!(clip.clipBegin.equals(Duration.ZERO) && clip.clipEnd.equals(originalDuration))) {
					rv.put("clipBegin", new BigDecimal(clip.clipBegin.toMillis()).movePointLeft(3));
					rv.put("clipEnd", new BigDecimal(clip.clipEnd.toMillis()).movePointLeft(3));
					rv.put("original-clipBegin", BigDecimal.ZERO);
					rv.put("original-clipEnd", new BigDecimal(originalDuration.toMillis()).movePointLeft(3));
				}
				return rv;
			} catch (Throwable e) {
				throw new UncheckedIOException(
					new IOException("Audio could not be written to file (type " + newFileType + "): " + outputFile, e));
			}
		}
	}
}
