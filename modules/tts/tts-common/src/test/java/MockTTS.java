import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.sf.saxon.s9api.XdmNode;

import org.daisy.common.file.URLs;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.TTSService;
import org.daisy.pipeline.tts.Voice;

import org.ops4j.pax.exam.util.PathUtils;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(
	name = "mock-tts",
	service = { TTSService.class }
)
public class MockTTS implements TTSService {
	
	final static File shortWaveOut = new File(PathUtils.getBaseDir(), "src/test/resources/mock-tts/mock_short.wav");
	final static File longWaveOut = new File(PathUtils.getBaseDir(), "src/test/resources/mock-tts/mock_long.wav");
	URL ssmlTransformer;
	
	@Activate
	protected void activate() {
		ssmlTransformer = URLs.getResourceFromJAR("/mock-tts/transform-ssml.xsl", MockTTS.class);
	}
	
	@Override
	public TTSEngine newEngine(Map<String,String> params) throws Throwable {
		return new TTSEngine(MockTTS.this) {
			
			@Override
			public SynthesisResult synthesize(XdmNode ssml, Voice voice, TTSResource threadResources)
					throws SynthesisException, InterruptedException {
				if (!"mock-en".equals(voice.getName())) {
					throw new SynthesisException("Voice " + voice.getName() + " not supported");
				}
				try {
					String sentence = transformSsmlNodeToString(ssml, ssmlTransformer, new TreeMap<String,Object>());
					File waveOut = sentence.length() < 50 ? MockTTS.shortWaveOut : MockTTS.longWaveOut;
					return new SynthesisResult(
						createAudioStream(
							new FileInputStream(waveOut)));
				} catch (Exception e) {
					throw new SynthesisException(e); }
			}
			
			@Override
			public Collection<Voice> getAvailableVoices() throws SynthesisException, InterruptedException {
				List<Voice> voices = new ArrayList<Voice>();
				voices.add(new Voice(getProvider().getName(), "mock-en"));
				voices.add(new Voice(getProvider().getName(), "mock-nl")); // don't put this first in the list because
				                                                           // the first item is used to test the engine
				return voices;
			}
			
			@Override
			public int getOverallPriority() {
				return 2;
			}
		};
	}
	
	@Override
	public String getName() {
		return "mock-tts";
	}
}
