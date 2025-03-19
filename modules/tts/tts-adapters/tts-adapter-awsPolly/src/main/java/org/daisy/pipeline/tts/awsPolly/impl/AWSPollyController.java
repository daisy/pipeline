package org.daisy.pipeline.tts.awsPolly.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.daisy.pipeline.audio.AudioBuffer;
import org.daisy.pipeline.tts.AudioBufferAllocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import javazoom.jl.decoder.JavaLayerException;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.polly.model.DescribeVoicesRequest;
import software.amazon.awssdk.services.polly.model.DescribeVoicesResponse;
import software.amazon.awssdk.services.polly.model.Engine;
import software.amazon.awssdk.services.polly.model.Gender;
import software.amazon.awssdk.services.polly.model.LanguageCode;
import software.amazon.awssdk.services.polly.model.OutputFormat;
import software.amazon.awssdk.services.polly.model.PollyException;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechRequest;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechResponse;
import software.amazon.awssdk.services.polly.model.TextType;
import software.amazon.awssdk.services.polly.model.Voice;
import software.amazon.awssdk.services.polly.model.VoiceId;

/**
 *
 * @author mmartida
 */
public final class AWSPollyController {

	private Voice voice;
	private final PollyClient polly;
//	private AudioInputStream ais;
	private static AudioFormat AF_PCM_POLLY = new AudioFormat(Encoding.PCM_SIGNED, (float) 16000, 16, 1, 2, (float) 16000, false);
	
	// revisar si esto puede ser variable de clase para que no se hagan llamadas innecesarias.
	// este proceso tarda unos 5 sg mínimo y está dando timeout en el contrutor. Se lanza
	// en un hilo, pero lo suyo sería que se espere a que ese hilo recupere la información
	// y usarlo donde haga falta.
	private DescribeVoicesResponse voicesResponse;
	
	private final static int MIN_CHUNK_SIZE = 2048;
	
	public static String getCurrentTime() {
		Date date = Calendar.getInstance().getTime();  
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");  
        return Thread.currentThread() + "-" + dateFormat.format(date); 
         
	}
	
	private final static Logger logger = LoggerFactory.getLogger(AWSPollyController.class);

	// constructor with custom region, lucia spanish voice and standard plan
	public AWSPollyController(String region) {
		logger.info("{} ********AWSPollyController.constructor", AWSPollyController.getCurrentTime());
		this.polly = PollyClient.builder().region(Region.of(region)).build();// 13- Lucia, 28- Enrique, 51- Conchita
		new Thread(() -> 
			  this.getVoices("standard")
		    ).start();
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("{} ********AWSPollyController.constructor fin", AWSPollyController.getCurrentTime());
	}

	// function to synthesize a text/ssml in a mp3 and store it on a file with the
	// given path
	public SynthesizeSpeechResponse talkPollyToFile(String chunk,  org.daisy.pipeline.tts.Voice voice, String path) {
		try {
			return synthesizeToFile(chunk, voice, OutputFormat.PCM, path);
		} catch (PollyException | IOException e) {
			System.err.println(e.getMessage());
			return null;
		}
	}

	// function to synthesize a text/ssml to a mp3 and send it through an audio
	// buffer
	public Collection<AudioBuffer> talkPolly(String chunk, org.daisy.pipeline.tts.Voice voice, String path, AudioBufferAllocator aba)
			throws UnsupportedAudioFileException, IOException, AudioBufferAllocator.MemoryException {
		return synthesize(chunk, voice, OutputFormat.PCM, aba);
	}

	// second part of function to save a synthesis of talk on a file
	private SynthesizeSpeechResponse synthesizeToFile(String text, org.daisy.pipeline.tts.Voice voice, OutputFormat format, String path)
			throws IOException {
		SynthesizeSpeechRequest synthReq = SynthesizeSpeechRequest.builder().engine(Engine.STANDARD).text(text)
				.textType(TextType.SSML).voiceId(getSelectedVoice(voice).id()).outputFormat(format).build();
		SynthesizeSpeechResponse synthRes = this.polly.synthesizeSpeech(synthReq, Paths.get(path));
		return synthRes;
	}

	// second part of function to synthesis of talk and send it through audio buffer
	private Collection<AudioBuffer> synthesize(String text, org.daisy.pipeline.tts.Voice voice, OutputFormat format, AudioBufferAllocator aba)
			throws IOException, UnsupportedAudioFileException, AudioBufferAllocator.MemoryException {
		UUID uuid = UUID.randomUUID();
		logger.info("{}-{} ********Controller.synthesize", uuid, AWSPollyController.getCurrentTime());
		logger.info("{}-{} ********Controller.synthesize.voice {}", uuid, AWSPollyController.getCurrentTime(), getSelectedVoice(voice));
		logger.info("{}-{} ********Controller.synthesize.voice.id {}", uuid, AWSPollyController.getCurrentTime(), getSelectedVoice(voice).idAsString());
		logger.info("{}-{} ********Controller.synthesize.text {}", uuid, AWSPollyController.getCurrentTime(), text);
		Collection<AudioBuffer> cab = new ArrayList<AudioBuffer>();
		SynthesizeSpeechRequest synthReq = SynthesizeSpeechRequest.builder().engine(Engine.STANDARD).text(text)
				.textType(TextType.SSML).voiceId(getSelectedVoice(voice).id()).outputFormat(format).build();
		ResponseInputStream<SynthesizeSpeechResponse> synRes = this.polly.synthesizeSpeech(synthReq);
		
		// esto no funciona
//		ais = AudioSystem.getAudioInputStream( synRes);
//		byte[] decodedBytes = ais.readAllBytes();
//		AudioBuffer ab = aba.allocateBuffer(decodedBytes.length);
//		ab.data = synRes.readAllBytes();
//		ab.size = ab.data.length;
//		cab.add(ab);
		
//		ais = AudioSystem.getAudioInputStream( synRes);
//		BufferedInputStream in = new BufferedInputStream(synRes);
//		AudioInputStream fi = AudioSystem.getAudioInputStream(in);
//		while (true) {
//		AudioBuffer b = aba
//				.allocateBuffer(MIN_CHUNK_SIZE + fi.available());
//			int ret = fi.read(b.data, 0, b.size);
//			if (ret == -1) {
//				// note: perhaps it would be better to call allocateBuffer()
//				// somewhere else in order to avoid this extra call:
//				aba.releaseBuffer(b);
//				break;
//			}
//			b.size = ret;
//			cab.add(b);
//		}
//		fi.close();
//		
//		return cab;
		
		// TODO
		// y con esto, no se lo que hará
		 
//		byte[] decodedBytesPcm = synRes.readAllBytes();
		byte[] decodedBytes = synRes.readAllBytes();
		synRes.close();
//		byte[] decodedBytes = pcmToWave(decodedBytesPcm);
		
				 
		AudioBuffer ab = aba.allocateBuffer(decodedBytes.length);
		
		ab.data = decodedBytes;
		ab.size = ab.data.length;
		//aba.releaseBuffer(ab);
		cab.add(ab);
		logger.info("{} ********Controller.synthesize ab.size {}", uuid, ab.size);
//		String outputFileName = "C:\\temp\\prueba.pcm";
//		 try (FileOutputStream outputStream = new FileOutputStream(new File(outputFileName))) {
//	                    outputStream.write(ab.data, 0, ab.size);
//	        } catch (Exception e) {
//	            System.err.println("Exception caught: " + e);
//	        }
		
		// prueba ... convierto el pcm a wav y lo guardo en fichero
//		String outputFileName = "C:\\temp\\prueba1.wav";
//		 try (FileOutputStream outputStream = new FileOutputStream(new File(outputFileName))) {
//	                    outputStream.write(ab.data, 0, ab.size);
//	        } catch (Exception e) {
//	            System.err.println("Exception caught: " + e);
//	        }
		
		
//		printStackTrace(uuid);
		logger.info("{} ********Controller.synthesize FIN RETURN", uuid);
		return cab;
	}
	
//	private void printStackTrace(UUID uuid) {
//		StackTraceElement[] elements = Thread.currentThread().getStackTrace();
//		logger.info("{}-{} ********STACK INI", uuid, AWSPollyController.getCurrentTime());
//        for(int i=0; i<elements.length; i++) {
//            logger.info("{}", elements[i]);
//        }
//        logger.info("{}-{} ********STACK FIN", uuid, AWSPollyController.getCurrentTime());
//	}

	// function to list the available voices
	// se sincroniza para que el hilo main y el hilo que se lanza en el constructor no se pisen
	public synchronized DescribeVoicesResponse getVoices(String type) {
		try {
			System.err.println("********Controller.getVoices");
			logger.info("{} ********Controller.getVoices", Thread.currentThread());
			if( voicesResponse == null ) {
				System.err.println("********Controller.getVoices no voices ");
				logger.info("{} ********Controller.getVoices no voices ", Thread.currentThread());
				voicesResponse = this.polly.describeVoices(DescribeVoicesRequest.builder().engine(type).build());
			} 
			
			return voicesResponse;
		} catch (AwsServiceException | SdkClientException e) {
			e.printStackTrace();
			logger.error("Error recovering available voices.", e);
			return null;
		} finally {
			logger.info("{} {} ********Controller.getVoices FIN", AWSPollyController.getCurrentTime(),Thread.currentThread());
		}
	}

	// function to select a certain voice
	public Voice selectVoice(VoiceId selection, DescribeVoicesResponse res) {
		try {
//			System.err.println("***res: " + res);
//			System.err.println("***res.voices: " + res.voices());
//			logger.info("{} ***res: " + res, AWSPollyController.getCurrentTime());
//			logger.info("{} ***res.voices: " + res.voices(), AWSPollyController.getCurrentTime());
			return res.voices().stream().filter(f -> f.id().equals(selection)).findFirst().orElse(defaultVoice());
//			this.voice = res.voices().get(selection);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		return defaultVoice();
	}

	// getter of the selected voice
	public Voice getSelectedVoice(org.daisy.pipeline.tts.Voice currentVoice) {
		if( currentVoice != null ) {
			logger.info("{} ***getSelectedVoice: currentVoice not null {}", AWSPollyController.getCurrentTime(), currentVoice);
			// si hay una voz seleccionada por la aplicación, se retorna esa
			Voice voice = this.selectVoice(VoiceId.fromValue(currentVoice.name), this.getVoices("standard"));
			logger.info("{} ***getSelectedVoice: currentVoice converted to pollyVoice {}", AWSPollyController.getCurrentTime(), voice);
			return voice;
		}
		// si no hay una voz actual, se mira a ver la que se tiene por defeto. Si es null
		// se pone la de lucía como voz por defecto
		if( this.voice == null ) {
			this.voice = this.selectVoice(VoiceId.LUCIA, this.getVoices("standard"));
		}
		logger.info("{} ***getSelectedVoice: currentVoice IS null {}", AWSPollyController.getCurrentTime(), voice);
		return this.voice;
	}

	// getter of the actual audio format specifications
	public AudioFormat getFormat() {
//		AudioFormat af = new AudioFormat((int) (ais.getFormat().getSampleRate() * 1.0),
//				ais.getFormat().getSampleSizeInBits(), ais.getFormat().getChannels(), true,
//				ais.getFormat().isBigEndian());
		// TODO - ais no se está generando, así que no se puede usar eso para generar el audioFormat
		// así que se pone algo como lo que hace google, y a ver.
		
//		public AudioFormat(Encoding encoding, float sampleRate, int sampleSizeInBits,
//                int channels, int frameSize, float frameRate, boolean bigEndian) {

//		AudioFormat af = new AudioFormat((float) 22050, 32, 1, true, true);
//		AudioFormat af = new AudioFormat(Encoding.PCM_SIGNED, (float) 22050, 32, 1, 48, (float) 22050, true);
		
//		getChannels: 1
//		getFrameRate: 16000.0
//		getFrameSize: 2
//		getSampleRate: 16000.0
//		getSampleSizeInBits: 16
//		getEncoding: PCM_SIGNED
		logger.info("*********Controller.format {}", AF_PCM_POLLY);
		return AF_PCM_POLLY;
	}
	
	private Voice defaultVoice() {
		System.err.println("***defaultVoice: ");
		return Voice.builder().gender(Gender.FEMALE).languageCode(LanguageCode.ES_ES).id(VoiceId.LUCIA).name(VoiceId.LUCIA.name()).languageName("Castilian Spanish").supportedEngines(Engine.STANDARD).build();
	}
	
	private byte[] pcmToWave(final byte[] rawData) throws IOException {
		
//		byte[] rawData = new byte[(int) rawFile.length()];
//		DataInputStream input = null;
//		try {
//			input = new DataInputStream(new FileInputStream(rawFile));
//			input.read(rawData);
//		} finally {
//			if (input != null) {
//				input.close();
//			}
//		}

		long initTime = System.currentTimeMillis();
		ByteArrayOutputStream output = null;
		try {
			output = new ByteArrayOutputStream();
			// WAVE header
			// see http://ccrma.stanford.edu/courses/422/projects/WaveFormat/
			writeString(output, "RIFF"); // chunk id
			writeInt(output, 36 + rawData.length); // chunk size
			writeString(output, "WAVE"); // format
			writeString(output, "fmt "); // subchunk 1 id
			writeInt(output, 16); // subchunk 1 size
			writeShort(output, (short) 1); // audio format (1 = PCM)
			writeShort(output, (short) 1); // number of channels
			writeInt(output, 16000); // sample rate
//		    writeInt(output, RECORDER_SAMPLERATE * 2); // byte rate
			writeInt(output, 1 * 2); // byte rate
			writeShort(output, (short) 2); // block align
			writeShort(output, (short) 16); // bits per sample
			writeString(output, "data"); // subchunk 2 id
			writeInt(output, rawData.length); // subchunk 2 size
			// Audio data (conversion big endian -> little endian)
			short[] shorts = new short[rawData.length / 2];
			ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
			ByteBuffer bytes = ByteBuffer.allocate(shorts.length * 2);
			for (short s : shorts) {
				bytes.putShort(s);
			}

			output.write(rawData);
			return output.toByteArray();
		} finally {
				output.close();
				long endTime = System.currentTimeMillis();
			logger.info("*********Controller.pcmToWave - tiempo en convertir pcm to wav: {}", (endTime - initTime));

		}
	}

//	byte[] fullyReadFileToBytes(File f) throws IOException {
//		int size = (int) f.length();
//		byte bytes[] = new byte[size];
//		byte tmpBuff[] = new byte[size];
//		FileInputStream fis = new FileInputStream(f);
//		try {
//
//			int read = fis.read(bytes, 0, size);
//			if (read < size) {
//				int remain = size - read;
//				while (remain > 0) {
//					read = fis.read(tmpBuff, 0, remain);
//					System.arraycopy(tmpBuff, 0, bytes, size - remain, read);
//					remain -= read;
//				}
//			}
//		} catch (IOException e) {
//			throw e;
//		} finally {
//			fis.close();
//		}
//
//		return bytes;
//	}

	private void writeInt(final OutputStream output, final int value) throws IOException {
		output.write(value >> 0);
		output.write(value >> 8);
		output.write(value >> 16);
		output.write(value >> 24);
	}

	private void writeShort(final OutputStream output, final short value) throws IOException {
		output.write(value >> 0);
		output.write(value >> 8);
	}

	private void writeString(final OutputStream output, final String value) throws IOException {
		for (int i = 0; i < value.length(); i++) {
			output.write(value.charAt(i));
		}
	}
}