import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;

import org.daisy.pipeline.audio.AudioClip;
import org.daisy.pipeline.audio.AudioEncoder;
import org.daisy.pipeline.audio.AudioEncoderService;
import static org.daisy.pipeline.audio.AudioFileTypes.MP3;
import org.daisy.pipeline.audio.AudioUtils;

import org.ops4j.pax.exam.util.PathUtils;

import org.osgi.service.component.annotations.Component;

/**
 * Audio encoder that raises an exception when the audio data is too
 * long. This is used to test the handling of encoding errors.
 */
@Component(
	name = "mock-encoder",
	service = { AudioEncoderService.class }
)
public class MockEncoder implements AudioEncoderService {

	private final static File mp3Out = new File(PathUtils.getBaseDir(), "src/test/resources/mock-encoder/mock_short.mp3");

	@Override
	public boolean supportsFileType(AudioFileFormat.Type fileType) {
		return MP3.equals(fileType);
	}

	@Override
	public Optional<AudioEncoder> newEncoder(Map<String,String> params) {
		return Optional.of(
			new AudioEncoder() {
				@Override
				public AudioClip encode(AudioInputStream pcm, AudioFileFormat.Type outputFileType, File outputFile)
						throws Throwable {
					if (!MP3.equals(outputFileType))
						throw new IllegalArgumentException();
					long size = pcm.getFrameLength() * pcm.getFormat().getFrameSize(); // size in bytes
					if (size > 100000) {
						throw new RuntimeException("Encoding failed");
					}
					InputStream from = null;
					OutputStream to = null;
					try {
						from = new FileInputStream(mp3Out);
						to = new FileOutputStream(outputFile);
						byte[] buffer = new byte[1024];
						int length;
						while ((length = from.read(buffer)) > 0) {
							to.write(buffer, 0, length);
						}
					} finally {
						if (from != null) from.close();
						if (to != null) to.close();
					}
					return new AudioClip(outputFile, Duration.ZERO, AudioUtils.getDuration(pcm));
				}
			}
		);
	}
}
