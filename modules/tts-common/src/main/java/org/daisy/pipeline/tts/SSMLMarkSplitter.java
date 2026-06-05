package org.daisy.pipeline.tts;

import java.util.Collection;

import net.sf.saxon.s9api.XdmNode;

public interface SSMLMarkSplitter {

	/**
	 * @return a list of SSML chunks, XML document or otherwise.
	 */
	Collection<Chunk> split(XdmNode xdmNode);

	static class Chunk {
		public Chunk(XdmNode ssml) {
			this.ssml = ssml;
		}

		/**
		 * @param ssml the piece of SSML, XML document or otherwise.
		 * @param leftmark can be null if there is mark on the left of the chunk
		 */
		public Chunk(XdmNode ssml, String leftmark) {
			this.ssml = ssml;
			this.leftmark = leftmark;
		}

		public boolean mostLeftChunk() {
			return leftmark == null;
		}

		/**
		 * @return null if there is no mark on the left of the Chunk
		 */
		public String leftMark() {
			return leftmark;
		}

		public XdmNode ssml() {
			return ssml;
		}

		private String leftmark;
		private XdmNode ssml;

		@Override
		public String toString() {
			return "Chunk{" + leftmark + "}";
		}
	}

}
