package org.daisy.pipeline.audio;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
	name = "audio-services",
	service = { AudioServices.class }
)
public class AudioServices {

	private final Collection<AudioEncoder> encoders = new CopyOnWriteArrayList<AudioEncoder>();

	public AudioEncoder getEncoder() {
		Iterator<AudioEncoder> it = encoders.iterator();
		if (it.hasNext())
			return it.next();
		return null;
	}

	@Reference(
		name = "audio-encoder",
		unbind = "removeEncoder",
		service = AudioEncoder.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC
	)
	public void addEncoder(AudioEncoder encoder) {
		encoders.add(encoder);
	}

	public void removeEncoder(AudioEncoder encoder) {
		encoders.remove(encoder);
	}

}
