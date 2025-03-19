package org.daisy.pipeline.tts.awsPolly.impl;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.daisy.pipeline.audio.AudioBuffer;
import org.daisy.pipeline.tts.AudioBufferAllocator;
import org.daisy.pipeline.tts.MarklessTTSEngine;
import org.daisy.pipeline.tts.TTSRegistry;
import org.daisy.pipeline.tts.TTSService;
import org.daisy.pipeline.tts.TTSService.SynthesisException;
import org.daisy.pipeline.tts.Voice;
import org.daisy.pipeline.tts.VoiceInfo;
import org.daisy.pipeline.tts.VoiceInfo.Gender;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import javazoom.jl.decoder.JavaLayerException;
import net.sf.saxon.s9api.XdmNode;
import software.amazon.awssdk.core.exception.AbortedException;
import software.amazon.awssdk.services.polly.model.Engine;


/**
 *
 * @author mmartida
 */

@Component(
	name = "polly-tts-service",
	service = { TTSService.class }
)
public class AWSPollyEngine extends MarklessTTSEngine{
    private final AWSPollyController polly;
    private int mPriority;
    private AudioFormat format;
    
    private final static Logger logger = LoggerFactory.getLogger(AWSPollyEngine.class);

	public AWSPollyEngine(AWSPollyTTSService service, int priority) {		
		super(service);
		logger.info("{} ********Engine.constructor ", AWSPollyController.getCurrentTime());
        this.polly = new AWSPollyController("eu-west-1"); 
        logger.info("{} ********Engine.constructor fin", AWSPollyController.getCurrentTime());
    }
    
    //public void synthesize(String sentence,String outputPath) throws JavaLayerException, UnsupportedAudioFileException {
    //    this.polly.talkPolly(sentence,outputPath);
	//}

    //Se quedara comentado hasta que sepamos si es necesario y como hacer el casteo sobre el
//	public AudioFormat getAudioOutputFormat() { return AudioFormat.Encoding.PCM_SIGNED; }

    @Override
	public Collection<Voice> getAvailableVoices() throws SynthesisException, InterruptedException {
    	logger.debug("********Engine.getvoices -> {} ", this.polly);
		try {
			Collection<software.amazon.awssdk.services.polly.model.Voice> arr = this.polly.getVoices(Engine.STANDARD.toString()).voices();
			return arr.stream()
					.map(i -> new Voice("polly",i.name(),new java.util.Locale(i.languageName()),Gender.of(i.genderAsString().toLowerCase())))
					.peek(v -> logger.debug("Obtained voice {} for locale {}, gender {}", v.name, v.getLocale(), v.getGender()))
					.collect(toList());
        } catch (Exception e) { throw e; }
	}
    
    @Override
	public int getOverallPriority() { return mPriority; }

	@Override
	public TTSRegistry.TTSResource allocateThreadResources() throws SynthesisException, InterruptedException {
		return new TTSRegistry.TTSResource();
	}

    @Override
    public Collection<AudioBuffer> synthesize(String sentence, XdmNode xmlSentence, Voice voice, TTSRegistry.TTSResource threadResources, AudioBufferAllocator bufferAllocator, boolean retry) throws SynthesisException, InterruptedException, AudioBufferAllocator.MemoryException {
    	//logger.debug("{} ********Engine.synthesize", AWSPollyController.getCurrentTime());
    	logger.info("synthesize -- VOICE: {}", voice);
    	if("<ssml:speak xmlns:ssml=\"http://www.w3.org/2001/10/synthesis\" version=\"1.0\"><s:s xmlns:tmp=\"http://\" xmlns:s=\"http://www.w3.org/2001/10/synthesis\" id=\"s1\">small sentence</s:s><ssml:break time=\"250ms\"/></ssml:speak>".equalsIgnoreCase(sentence)) {
    		logger.info("{} ********Engine.synthesize.TEST SENTENCE", AWSPollyController.getCurrentTime());
    		// cambiamos la sentence para ver si funciona... TODO eliminar
    		sentence = "<ssml:speak xmlns:ssml=\"http://www.w3.org/2001/10/synthesis\" version=\"1.0\"><s:s xmlns:tmp=\"http://\" xmlns:s=\"http://www.w3.org/2001/10/synthesis\" >small sentence</s:s><ssml:break time=\"250ms\"/></ssml:speak>";
    	}
    	try {
    		return this.polly.talkPolly(normalizeSSMLToPolly(sentence), voice, "", bufferAllocator);
    	} catch (UnsupportedAudioFileException | IOException ex) {
    		throw new SynthesisException("Could not synthesize sentence \"" + sentence + "\"", ex);
    	} catch (AbortedException e) {
    		if (Thread.currentThread().isInterrupted()) {
    			InterruptedException ex = new InterruptedException();
    			ex.initCause(e);
    			throw ex;
    		} else {
    			throw e;
    		}
    	}
    }

    @Override
	public AudioFormat getAudioOutputFormat() {
    	logger.debug("*********engine.format");
    	// TODO, como no funciona la lectura de la respuesta del método sinthetize, no 
    	// se genera el audio format. Hay que revisar eso y de momento
    	// se devuelve lo mismo que hace google, aunque puede que no sea lo mismo
        return this.polly.getFormat();
    	
    }
    
    // TODO - "workaround" para que funcione aws polly con los tags ssml que le llegan. No admite
    // <ssml:speak xmlns:ssml=\"http://www.w3.org/2001/10/synthesis\" version=\"1.0\"><s:s xmlns:tmp=\"http://\" xmlns:s=\"http://www.w3.org/2001/10/synthesis\" id=\"s1\">small sentence</s:s><ssml:break time=\"250ms\"/></ssml:speak>
    // y funciona si se le quitan los xmlns
    // de momento solo quito los ssml:speak porque con eso parece que no se queja y es lo sencillo. 
    private static final Pattern pSpeak = compile("ssml:speak", CASE_INSENSITIVE);
    private static final Pattern pBreak = compile("ssml:break", CASE_INSENSITIVE);
    public static String normalizeSSMLToPolly(String sentence) {
    	String result = pBreak.matcher(pSpeak.matcher(sentence).replaceAll("speak")).replaceAll("break");
    	
    	logger.info("Cadena SSML adaptada {}", result);
    	    return result;
    }
    
    @Override
    // copiado de la de google
	public int expectedMillisecPerWord() {
		// Worst case scenario with quotas:
		// the thread can wait for a bit more than a minute for a anwser
    	logger.debug("Entered expectedMillisecPerWord", AWSPollyController.getCurrentTime());
		return 10000;
	}
    
    public static void main(String[] args) {
		String toReplace = "<ssml:speak xmlns:ssml=\"http://www.w3.org/2001/10/synthesis\" version=\"1.0\"><s:s xmlns:tmp=\"http://\" xmlns:s=\"http://www.w3.org/2001/10/synthesis\" id=\"s1\">small sentence</s:s><ssml:break time=\"250ms\"/></ssml:speak>";
	}
}
