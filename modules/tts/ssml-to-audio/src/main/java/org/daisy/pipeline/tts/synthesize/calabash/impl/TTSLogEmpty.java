package org.daisy.pipeline.tts.synthesize.calabash.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.sf.saxon.s9api.XdmNode;

import org.daisy.pipeline.tts.Voice;

public class TTSLogEmpty implements TTSLog {

	private Entry mUselessEntry = new Entry() {

		@Override
		public void addError(Error err) {
		}

		@Override
		public Collection<Error> getReadOnlyErrors() {
			return Collections.EMPTY_LIST;
		}

		@Override
		public void setSSML(XdmNode ssml) {
		}

		@Override
		public XdmNode getSSML() {

			return null;
		}

		@Override
		public void setSelectedVoice(Voice v) {
		}

		@Override
		public Voice getSelectedVoice() {
			return null;
		}

		@Override
		public void setActualVoice(Voice v) {

		}

		@Override
		public Voice getActualVoice() {
			return null;
		}

		@Override
		public void addTTSinput(String input) {
		}

		@Override
		public List<String> getTTSinput() {
			return Collections.EMPTY_LIST;
		}

		@Override
		public void setSoundfile(String soundfile) {
		}

		@Override
		public String getSoundFile() {
			return null;
		}

		@Override
		public void setPositionInFile(double begin, double end) {
		}

		@Override
		public double getBeginInFile() {
			return 0;
		}

		@Override
		public double getEndInFile() {
			return 0;
		}

		@Override
		public void resetTTSinput() {
		}

		@Override
		public void setTimeout(float secs) {
		}

		@Override
		public float getTimeout() {
			return 0;
		}

	};

	@Override
	public Entry getOrCreateEntry(String id) {
		return mUselessEntry;
	}

	@Override
	public Entry getWritableEntry(String id) {
		return mUselessEntry;
	}

	@Override
	public Set<java.util.Map.Entry<String, Entry>> getEntries() {
		return Collections.EMPTY_SET;
	}

	@Override
	public void addGeneralError(ErrorCode errcode, String message) {
	}

	@Override
	public Collection<Error> readonlyGeneralErrors() {
		return Collections.EMPTY_LIST;
	}

}
