import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
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
	
	private final static File mp3Out = new File(PathUtils.getBaseDir(), "src/test/resources/mock-encoder/mock_short.mp3");
	
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
		int size = 0;
		for (AudioBuffer b : pcm)
			size += b.size;
		if (size > 100000) {
			throw new RuntimeException("Encoding failed");
		}
		File encodedFile = new File(outputDir, filePrefix + ".mp3");
		InputStream from = null;
		OutputStream to = null;
		try {
			from = new FileInputStream(mp3Out);
			to = new FileOutputStream(encodedFile);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = from.read(buffer)) > 0) {
				to.write(buffer, 0, length);
			}
		} finally {
			if (from != null) from.close();
			if (to != null) to.close();
		}
		return Optional.of(encodedFile.toURI().toString());
	}
}
