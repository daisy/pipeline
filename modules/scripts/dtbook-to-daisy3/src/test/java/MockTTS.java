import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import net.sf.saxon.s9api.XdmNode;

import org.daisy.pipeline.audio.AudioBuffer;
import org.daisy.pipeline.tts.AudioBufferAllocator;
import org.daisy.pipeline.tts.AudioBufferAllocator.MemoryException;
import org.daisy.pipeline.tts.MarklessTTSEngine;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.TTSService;
import org.daisy.pipeline.tts.TTSService.SynthesisException;
import org.daisy.pipeline.tts.Voice;

import org.ops4j.pax.exam.util.PathUtils;

import org.osgi.service.component.ComponentContext;

public class MockTTS implements TTSService {
	
	final static File waveOut = new File(PathUtils.getBaseDir(), "src/test/resources/mock-tts/mock.wav");
	URL ssmlTransformer;
	
	protected void activate(ComponentContext context) {
		ssmlTransformer = context.getBundleContext().getBundle().getEntry("/mock-tts/transform-ssml.xsl");
	}
	
	@Override
	public TTSEngine newEngine(Map<String,String> params) throws Throwable {
		return new MarklessTTSEngine(MockTTS.this) {
			
			AudioFormat audioFormat;
			
			@Override
			public Collection<AudioBuffer> synthesize(String sentence, XdmNode xmlSentence,
			                                          Voice voice, TTSResource threadResources,
			                                          AudioBufferAllocator bufferAllocator, boolean retry)
					throws SynthesisException, InterruptedException, MemoryException {
				try {
					Collection<AudioBuffer> result = new ArrayList<AudioBuffer>();
					BufferedInputStream in = new BufferedInputStream(new FileInputStream(MockTTS.waveOut));
					AudioInputStream fi = AudioSystem.getAudioInputStream(in);
					if (audioFormat == null)
						audioFormat = fi.getFormat();
					while (true) {
						AudioBuffer b = bufferAllocator.allocateBuffer(2048 + fi.available());
						int ret = fi.read(b.data, 0, b.size);
						if (ret == -1) {
							bufferAllocator.releaseBuffer(b);
							break; }
						b.size = ret;
						result.add(b); }
					fi.close();
					return result; }
				catch (Exception e) {
					throw new SynthesisException(e); }
			}
			
			@Override
			public AudioFormat getAudioOutputFormat() {
				return audioFormat;
			}
			
			@Override
			public Collection<Voice> getAvailableVoices() throws SynthesisException, InterruptedException {
				List<Voice> voices = new ArrayList<Voice>();
				voices.add(new Voice(getProvider().getName(), "mock"));
				return voices;
			}
			
			@Override
			public int getOverallPriority() {
				return 2;
			}
		};
	}
	
	@Override
	public URL getSSMLxslTransformerURL() {
		return ssmlTransformer;
	}
	
	@Override
	public String getName() {
		return "mock-tts";
	}
	
	@Override
	public String getVersion() {
		return "1";
	}
}
