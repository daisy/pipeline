package org.daisy.pipeline.tts.awsPolly.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;

import org.daisy.pipeline.audio.AudioBuffer;
import org.daisy.pipeline.audio.lame.impl.LameEncoder;
import org.daisy.pipeline.tts.StraightBufferAllocator;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.TTSService;
import org.daisy.pipeline.tts.Voice;
import org.junit.Test;

import junit.framework.Assert;

public class AWSPollyTTSServiceTest { // extends AbstractTest {

//	@Inject
	public TTSService ttsService = new AWSPollyTTSService();

	private static final Map<String, String> params = new HashMap<>();
	static {
		params.put("org.daisy.pipeline.tts.awsPolly.priority",
				System.getProperty("org.daisy.pipeline.tts.awsPolly.priority"));
	}

//	@Test
	public void testAvailableVoices() throws Throwable {
		TTSEngine engine = ttsService.newEngine(params);
		Collection<Voice> voices = engine.getAvailableVoices();
//		Assert.assertEquals(2, voices.size());
//		Assert.assertEquals(ImmutableSet.of("William", "Ylva"),
//		                    voices.stream().map(v -> v.name).collect(Collectors.toSet()));
	}

//	@Test
	public void testSpeak() throws Throwable {
		TTSEngine engine = ttsService.newEngine(params);
		Assert.assertTrue("polly".equals(ttsService.getName()) );
		TTSResource resource = engine.allocateThreadResources();
		try {
			Collection<AudioBuffer> audio = engine.synthesize(
					// con este texto falla. Es lo que le llega desde la aplicación para probar el engine y está fallando.
//					"<ssml:speak xmlns:ssml=\"http://www.w3.org/2001/10/synthesis\" version=\"1.0\"><s:s xmlns:tmp=\"http://\" xmlns:s=\"http://www.w3.org/2001/10/synthesis\" >small sentence</s:s><ssml:break time=\"250ms\"/></ssml:speak>",
					// con esto funciona. Parece que no le gustan los namespaces.
					"<ssml:speak xmlns:ssml=\"http://www.w3.org/2001/10/synthesis\" version=\"1.0\"><s xmlns=\"http://www.w3.org/2001/10/synthesis\">García, que de humor iba justito cuando era el inocente, entró una vez a un trapo que no ayudó a la fluidez de su relación tempestuosa con Gil.</s><ssml:break time=\"250ms\"/></ssml:speak>",
					null, // xml
					new Voice(null, "Lucia"), resource, null, // marks,
					null, // expectedMarks
					new StraightBufferAllocator(), false // retry
			);
			AudioFormat format = engine.getAudioOutputFormat();
			Assert.assertTrue(audio.stream().mapToInt(x -> x.size).sum() > 10000);
		} finally {
			engine.releaseThreadResources(resource);
		}
	}
	
//	@Test
	public void testSpeakLame() throws Throwable {
		TTSEngine engine = ttsService.newEngine(params);
		Assert.assertTrue("polly".equals(ttsService.getName()) );
		TTSResource resource = engine.allocateThreadResources();
		try {
			Collection<AudioBuffer> audio = engine.synthesize(
					// con este texto falla. Es lo que le llega desde la aplicación para probar el engine y está fallando.
//					"<ssml:speak xmlns:ssml=\"http://www.w3.org/2001/10/synthesis\" version=\"1.0\"><s:s xmlns:tmp=\"http://\" xmlns:s=\"http://www.w3.org/2001/10/synthesis\" >small sentence</s:s><ssml:break time=\"250ms\"/></ssml:speak>",
					// con esto funciona. Parece que no le gustan los namespaces.
					"<speak><s>Sin embargo, como muchos otros, estos escritos no fueron incorporados al canon de la Biblia eclesiástica.</s><break time=\"250ms\"/><s>Era de esperar.</s></speak>",
					null, // xml
					new Voice(null, "Lucia"), resource, null, // marks,
					null, // expectedMarks
					new StraightBufferAllocator(), false // retry
			);
			AudioFormat format = engine.getAudioOutputFormat();
//			Assert.assertTrue(audio.stream().mapToInt(x -> x.size).sum() > 10000);
			audio.forEach((ab) -> {
				try (FileOutputStream fos = new FileOutputStream("/temp/prueba-pausa.wav")) {
					   fos.write(ab.data);
					   //fos.close(); There is no more need for this line since you had created the instance of "fos" inside the try. And this will automatically close the OutputStream
					} catch (Exception e) {
						e.printStackTrace();
					}
			});
		} finally {
			engine.releaseThreadResources(resource);
		}
	}
	
//	@Test
	public void testSpeakLameConvertMp3() throws Throwable {
		AudioFormat AF_PCM_POLLY = new AudioFormat(Encoding.PCM_SIGNED, (float) 16000, 16, 1, 2, (float) 16000, false);
			LameEncoder encoder = new LameEncoder();
			Map<String, String> encoderProps =  new HashMap<>();
			encoderProps.put("org.daisy.pipeline.tts.lame.path", "c:/LAME/lame.exe");
			encoder.parseEncodingOptions(encoderProps);
			StraightBufferAllocator allocator = new StraightBufferAllocator();
			
			File file = new File("/temp/prueba-pausa.wav");
			File fileOut = new File("/temp/");
			byte[] fileContent = fullyReadFileToBytes(file);
			AudioBuffer ab = allocator.allocateBuffer(fileContent.length);
			ab.data = fileContent;
			ab.size = fileContent.length;
			Collection<AudioBuffer> abC = new ArrayList<>();
			abC.add(ab);
			encoder.encode(abC, AF_PCM_POLLY, fileOut, "prueba-pausa-convertido", encoder.parseEncodingOptions(encoderProps));
	}

//	@Test // test commented out until the files are in relative paths inside the project.
	public void testSpeakLameConvertMp3396() throws Throwable {
		AudioFormat AF_PCM_POLLY = new AudioFormat(Encoding.PCM_SIGNED, (float) 16000, 16, 1, 2, (float) 16000, false);
			LameEncoder encoder = new LameEncoder();
			Map<String, String> encoderProps =  new HashMap<>();
			encoderProps.put("org.daisy.pipeline.tts.lame.path", "/usr/bin/lame");
			encoder.parseEncodingOptions(encoderProps);
			StraightBufferAllocator allocator = new StraightBufferAllocator();
			
			File file = new File("temp/Quijote.pcm");
			File fileOut = new File("temp/");
			byte[] fileContent = fullyReadFileToBytes(file);
			AudioBuffer ab = allocator.allocateBuffer(fileContent.length);
			ab.data = fileContent;
			ab.size = fileContent.length;
			Collection<AudioBuffer> abC = new ArrayList<>();
			abC.add(ab);
			encoder.encode(abC, AF_PCM_POLLY, fileOut, "prueba-pausa-convertido-396", encoder.parseEncodingOptions(encoderProps));
	}
	
	byte[] fullyReadFileToBytes(File f) throws IOException {
		int size = (int) f.length();
		byte bytes[] = new byte[size];
		byte tmpBuff[] = new byte[size];
		FileInputStream fis = new FileInputStream(f);
		try {

			int read = fis.read(bytes, 0, size);
			if (read < size) {
				int remain = size - read;
				while (remain > 0) {
					read = fis.read(tmpBuff, 0, remain);
					System.arraycopy(tmpBuff, 0, bytes, size - remain, read);
					remain -= read;
				}
			}
		} catch (IOException e) {
			throw e;
		} finally {
			fis.close();
		}

		return bytes;
	}
}
