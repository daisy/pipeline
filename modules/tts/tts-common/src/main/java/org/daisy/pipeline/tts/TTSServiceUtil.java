package org.daisy.pipeline.tts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.daisy.pipeline.audio.AudioBuffer;
import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.TTSService.Mark;
import org.daisy.pipeline.tts.TTSService.SynthesisException;

public class TTSServiceUtil {
	public static String displayName(TTSService service) {
		return service.getName() + "-" + service.getVersion();
	}
}
