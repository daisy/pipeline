package org.daisy.pipeline.tts.synthesize;

class SoundFileLink {
	//easy way to share a reference to a String
	public StringBuilder soundFileURIHolder;

	//time position where the text with id=xmlid starts within the sound file (in seconds)
	public double clipBegin;

	//time position where the node with id=xmlid ends within the sound file (in seconds)
	public double clipEnd;

	//id in the XML book
	public String xmlid;
}
