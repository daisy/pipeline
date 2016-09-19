package org.daisy.pipeline.audio;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

public class AudioServices {

	private final Collection<AudioEncoder> encoders = new CopyOnWriteArrayList<AudioEncoder>();

	public AudioEncoder getEncoder() {
		Iterator<AudioEncoder> it = encoders.iterator();
		if (it.hasNext())
			return it.next();
		return null;
	}

	public void addEncoder(AudioEncoder encoder) {
		encoders.add(encoder);
	}

	public void removeEncoder(AudioEncoder encoder) {
		encoders.remove(encoder);
	}

}
