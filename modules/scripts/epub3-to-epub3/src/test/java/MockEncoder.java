import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.Optional;

import javax.sound.sampled.AudioFormat;

import org.daisy.pipeline.audio.AudioBuffer;
import org.daisy.pipeline.audio.AudioEncoder;

import org.ops4j.pax.exam.util.PathUtils;

import org.osgi.service.component.annotations.Component;

@Component(
	name = "mock-encoder",
	service = { AudioEncoder.class }
)
public class MockEncoder implements AudioEncoder {
	
	private final static File mp3 = new File(PathUtils.getBaseDir(), "src/test/resources/mock-encoder/mock.mp3");
	private final static File mp3Out = new File(PathUtils.getBaseDir(), "target/generated-test-resources/mock.mp3");
	
	@Override
	public EncodingOptions parseEncodingOptions(Map<String,String> params) {
		return new EncodingOptions(){};
	}
	
	@Override
	public void test(EncodingOptions options) throws Exception {
	}
	
	@Override
	public Optional<String> encode(Iterable<AudioBuffer> pcm, AudioFormat audioFormat,
	                               File outputDir, String filePrefix, EncodingOptions options) throws Throwable {
		if (!mp3Out.exists()) {
			mp3Out.getParentFile().mkdirs();
			mp3Out.createNewFile();
			FileInputStream reader = new FileInputStream(mp3);
			FileOutputStream writer = new FileOutputStream(mp3Out);
			byte[] buffer = new byte[153600];
			int bytesRead = 0;
			while ((bytesRead = reader.read(buffer)) > 0) {
				writer.write(buffer, 0, bytesRead);
				buffer = new byte[153600]; }
			writer.close();
			reader.close();
		}
		return Optional.of(mp3Out.toURI().toString());
	}
}
