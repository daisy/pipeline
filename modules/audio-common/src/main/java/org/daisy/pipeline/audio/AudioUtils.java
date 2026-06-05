package org.daisy.pipeline.audio;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.google.common.collect.AbstractIterator;
import com.google.common.io.ByteStreams;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public final class AudioUtils {

	private AudioUtils() {}

	/**
	 * Create an {@link AudioInputStream} from an {@link AudioFormat} and the audio data.
	 *
	 * @param format Expected to be PCM.
	 */
	public static AudioInputStream createAudioStream(AudioFormat format, byte[] data) {
		return createAudioStream(format, new ByteArrayInputStream(data));
	}

	public static AudioInputStream createAudioStream(AudioFormat format, ByteArrayInputStream data) {
		if (format.getFrameSize() == AudioSystem.NOT_SPECIFIED)
			throw new IllegalArgumentException("unknown frame size");
		return new AudioInputStream(data,
		                            format,
		                            // ByteArrayInputStream.available() returns
		                            // the total number of bytes
		                            data.available() / format.getFrameSize());
	}

	/**
	 * Create a {@link AudioInputStream} from a {@link InputStream}.
	 *
	 * This is to work around a bug in {@link javax.sound.sampled.AudioSystem}
	 * which may return {@link AudioInputStream} with a wrong {@link
	 * AudioInputStream#getFrameLength()}.
	 */
	public static AudioInputStream createAudioStream(InputStream stream)
			throws UnsupportedAudioFileException, IOException {
		AudioInputStream audio = AudioSystem.getAudioInputStream(new BufferedInputStream(stream));
		AudioFormat format = audio.getFormat();
		if (format.getFrameSize() != AudioSystem.NOT_SPECIFIED) {
			byte[] data = ByteStreams.toByteArray(audio);
			audio.close();
			audio = createAudioStream(format, data);
		}
		return audio;
	}

	/**
	 * Get the duration of a PCM encoded audio stream.
	 *
	 * @throws IllegalArgumentException if format is not PCM encoded with specified frame rate.
	 */
	public static Duration getDuration(AudioInputStream stream) {
		return getDurationFromFrames(PCMAudioFormat.of(stream.getFormat()), stream.getFrameLength());
	}

	/**
	 * @throws IllegalArgumentException if format is not PCM encoded with specified frame rate
	 *                                  and frame size.
	 */
	public static Duration getDuration(AudioFormat format, int bytes) {
		format = PCMAudioFormat.of(format);
		return getDurationFromFrames((PCMAudioFormat)format, (long)(bytes / format.getFrameSize()));
	}

	private static Duration getDurationFromFrames(PCMAudioFormat format, Long frames) {
		return Duration.ofMillis((long)(frames.floatValue() / (format.getFrameRate() / 1000)));
	}

	/**
	 * @return the number of frames needed to contain the given duration of audio.
	 */
	public static long getLengthInFrames(PCMAudioFormat format, Duration duration) {
		// rounding to the next natural number if needed
		return (long)Math.ceil((double)(format.getFrameRate() / 1000) * duration.toMillis());
	}

	/**
	 * Concatenate a sequence of {@link AudioInputStream} into a single {@link AudioInputStream}.
	 *
	 * @param streams Must be streams with the same (PCM) audio format.
	 */
	public static AudioInputStream concat(Iterable<AudioInputStream> streams) {
		int count = 0;
		long _totalLength = 0;
		PCMAudioFormat format = null;
		for (AudioInputStream s : streams) {
			count++;
			_totalLength += s.getFrameLength();
			if (format == null) {
				try {
					format = PCMAudioFormat.of(s.getFormat());
				} catch (IllegalArgumentException e) {
					throw new IllegalArgumentException("AudioInputStream must be PCM encoded, but got: "+ s.getFormat());
				}
			} else if (!format.matches(s.getFormat()))
				throw new IllegalArgumentException("Can not concatenate AudioInputStream with different audio formats");
		}
		if (count == 0)
			throw new IllegalArgumentException("At least one AudioInputStream expected");
		if (count == 1)
			return streams.iterator().next();
		long totalLength = _totalLength;
		int frameSize = format.getFrameSize();
		return new AudioInputStream(
			new InputStream() {
				Iterator<AudioInputStream> nextStreams = streams.iterator();
				AudioInputStream stream = null;
				byte[] frame = new byte[frameSize]; // always need to read an integral number of frames
				                                    // from an AudioInputStream
				long availableFrames = totalLength;
				int availableInFrame = 0;
				@Override
				public int read() throws IOException {
					if (availableFrames == 0 && availableInFrame == 0)
						return -1;
					if (availableInFrame > 0)
						return frame[frameSize - (availableInFrame--)] & 0xFF;
					if (stream != null) {
						availableInFrame = stream.read(frame);
						if (availableInFrame > 0) {
							availableFrames--;
							return frame[frameSize - (availableInFrame--)] & 0xFF;
						}
					}
					try {
						if (stream != null)
							stream.close();
						stream = nextStreams.next();
					} catch (NoSuchElementException e) {
						return -1;
					}
					return read();
				}
				@Override
				public int read(byte[] b, int off, int len) throws IOException {
					if (off < 0 || len < 0 || b.length - off < len)
						throw new IndexOutOfBoundsException();
					if (len == 0)
						return 0;
					if (availableFrames == 0 && availableInFrame == 0)
						return -1;
					int read = 0;
					while (true) {
						if (availableInFrame > 0) {
							System.arraycopy(frame, frameSize - availableInFrame, b, off, Math.min(len, availableInFrame));
							if (availableInFrame >= len) {
								availableInFrame -= len;
								read += len;
								len = 0;
							} else {
								len -= availableInFrame;
								off += availableInFrame;
								read += availableInFrame;
								availableInFrame = 0;
							}
						}
						if (len < frameSize)
							return read; // note that it is possible that we have read a number of bytes smaller
							             // than the initial value of `len'
						if (stream != null) {
							int l = stream.read(b, off, len); // this will read an integral number of frames, so it is
							                                  // possible that l < len even if the stream has more bytes
							                                  // available
							if (l > 0) {
								if (l % frameSize > 0)
									throw new IllegalStateException(); // should not happen
								len -= l;
								off += l;
								read += l;
								availableFrames -= (l / frameSize);
								continue;
							}
						}
						try {
							if (stream != null)
								stream.close();
							stream = nextStreams.next();
						} catch (NoSuchElementException e) {
							return read;
						}
					}
				}
				@Override
				public int available() {
					try {
						return Math.toIntExact(Math.multiplyExact(availableFrames, frameSize)) + availableInFrame;
					} catch (ArithmeticException e) {
						return Integer.MAX_VALUE;
					}
				}
				@Override
				public void close() throws IOException {
					if (stream != null)
						stream.close();
					while (nextStreams.hasNext())
						nextStreams.next().close();
				}
			},
			format,
			totalLength);
	}

	/**
	 * Split a {@link AudioInputStream} into a sequence of {@link AudioInputStream}.
	 *
	 * @param stream Must be PCM encoded. {@link AudioInputStream#getFrameLength()} must return the
	 *               actual number of available frames in the stream.
	 * @param splitPoints Are relative to the beginning of the stream.
	 * @return A sequence of streams. The number of streams is one more than the number of split points.
	 *         The durations are approximately (apart from rounding errors due to sampling) the time
	 *         between the split points.
	 * @throws IllegalArgumentException if a negative split point is provided, a split point exceeds
	 *                                  the total length of the input stream, or a split point is not
	 *                                  greater than or equal to the preceding split point ,and also
	 *                                  if the audio format is not PCM encoded with specified frame
	 *                                  rate and frame size.
	 */
	public static Iterable<AudioInputStream> split(AudioInputStream stream, Duration... splitPoints) {
		long[] splitPointsInFrames = new long[splitPoints.length];
		PCMAudioFormat format = PCMAudioFormat.of(stream.getFormat());
		for (int i = 0; i < splitPoints.length; i++)
			splitPointsInFrames[i] = getLengthInFrames(format, splitPoints[i]);
		return split(stream, splitPointsInFrames);
	}

	public static Iterable<AudioInputStream> split(AudioInputStream stream, long... splitPoints) {
		if (splitPoints.length == 0)
			return Collections.singleton(stream);
		PCMAudioFormat format = PCMAudioFormat.of(stream.getFormat());
		long totalFrames = stream.getFrameLength();
		{ // validate splitPoints
			long begin = 0;
			for (long end : splitPoints) {
				if (end < begin)
					throw new IllegalArgumentException();
				begin = end; }
			if (totalFrames < begin)
				// could be a rounding error due to sampling: provide margin of one frame
				if (totalFrames + 1 < begin)
					throw new IllegalArgumentException(
						"invalid split points: " + Arrays.toString(splitPoints)
						+ " (total number of frames: " + totalFrames + ")");
		}
		int frameSize = format.getFrameSize();
		return new Iterable<AudioInputStream>() {
			public Iterator<AudioInputStream> iterator() {
				return new AbstractIterator<AudioInputStream>() {
					long framesAvailable = totalFrames;
					long framesConsumed = 0;
					int i = 0;
					protected AudioInputStream computeNext() {
						if (i > splitPoints.length)
							return endOfData();
						long frames = i == splitPoints.length
							? framesAvailable
							: splitPoints[i] - framesConsumed;
						if (frames > framesAvailable) {
							if (i == splitPoints.length - 1 && frames == framesAvailable + 1)
								frames--;
							else
								throw new IllegalStateException("coding error"); // can not happen (see splitPoint validation above)
						}
						// In theory a byte array can hold 2 Gb of memory, which should be more than
						// enough for audio. Note that when a lot of AudioInputStreams are open,
						// there is still a risk for out-of-memory errors, but normally the streams
						// are consumed in order so that should be okay as well. An alternative
						// solution would be to not use byte arrays and enforce that streams are
						// fully consumed before the next is computed.
						byte[] bytes = new byte[Math.toIntExact(frames * frameSize)];
						if (frames > 0) {
							try {
								if (stream.read(bytes) < bytes.length)
									// if this happens it means that stream.getFrameLength() did not
									// return the actual number of available frames
									throw new IllegalStateException("coding error");
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
							framesConsumed += frames;
							framesAvailable -= frames;
						}
						i++;
						return createAudioStream(format, bytes);
					}
				};
			}
		};
	}

	/**
	 * @param duration must not be negative
	 * @return <code>null</code> if <code>duration</code> is too short
	 */
	public static AudioInputStream createSilence(PCMAudioFormat format, Duration duration) {
		if (duration.compareTo(Duration.ZERO) < 0)
			throw new IllegalArgumentException();
		long frames = getLengthInFrames(format, duration);
		if (frames == 0)
			return null;
		else
			return createSilence(format, frames);
	}

	/**
	 * @param format must be PCM encoded
	 * @param frames must not be negative
	 * @return <code>null</code> if <code>frames</code> is <code>0</code>
	 */
	public static AudioInputStream createSilence(AudioFormat format, long frames) {
		format = PCMAudioFormat.of(format);
		if (frames < 0)
			throw new IllegalArgumentException();
		if (frames == 0)
			return null;
		byte[] data = new byte[Math.multiplyExact(Math.toIntExact(frames), format.getFrameSize())]; // initialized to all zeros
		Encoding encoding = format.getEncoding();
		if (encoding == Encoding.PCM_SIGNED)
			return createAudioStream(format, data);
		else if (encoding == Encoding.PCM_UNSIGNED)
			return convertAudioStream(
				format,
				createAudioStream(new AudioFormat(Encoding.PCM_SIGNED,
				                                  format.getSampleRate(),
				                                  format.getSampleSizeInBits(),
				                                  format.getChannels(),
				                                  format.getFrameSize(),
				                                  format.getFrameRate(),
				                                  format.isBigEndian()),
				                  data));
		else // encoding == Encoding.PCM_FLOAT
			throw new UnsupportedOperationException("Not implemented"); // FIXME
	}

	/**
	 * Convert audio from one format to another.
	 */
	public static AudioInputStream convertAudioStream(AudioFormat newFormat, AudioInputStream stream) {
		AudioFormat format = stream.getFormat();
		if (format.getSampleSizeInBits() > 8
		    && newFormat.getSampleSizeInBits() > 8
		    && !format.isBigEndian()
		    && !newFormat.isBigEndian()
		    && ((format.getEncoding() == Encoding.PCM_UNSIGNED
		         && newFormat.getEncoding() == Encoding.PCM_SIGNED)
		        || (format.getEncoding() == Encoding.PCM_SIGNED
		            && newFormat.getEncoding() == Encoding.PCM_UNSIGNED))) {

			// com.sun.media.sound.PCMtoPCMCodec does not correctly convert unsigned little-endian
			// to signed little-endian or visa-versa. Work around this by first converting the
			// samples to big-endian.
			return convertAudioStream(
				newFormat,
				convertAudioStream(
					new AudioFormat(format.getSampleRate(),
					                format.getSampleSizeInBits(),
					                format.getChannels(),
					                format.getEncoding() == Encoding.PCM_SIGNED,
					                true), // big endian
					stream));
		} else if (format.getEncoding() == Encoding.PCM_FLOAT
		           && newFormat.getEncoding() == Encoding.PCM_FLOAT
		           && format.getSampleSizeInBits() == 64
		           && newFormat.getSampleSizeInBits() == 64
		           && format.isBigEndian() != newFormat.isBigEndian()) {

			// AudioSystem.getAudioInputStream() introduces a rounding error, so we do the byte
			// order conversion ourselves.
			AudioFormat otherEndian = new AudioFormat(format.getEncoding(),
			                                          format.getSampleRate(),
			                                          format.getSampleSizeInBits(),
			                                          format.getChannels(),
			                                          format.getFrameSize(),
			                                          format.getFrameRate(),
			                                          !format.isBigEndian());
			ByteBuffer bytes; {
				try {
					bytes = ByteBuffer.wrap(ByteStreams.toByteArray(stream));
				} catch (IOException e) {
					throw new RuntimeException(e); // should not happen
				}
			}
			ByteBuffer reorderedBytes  = ByteBuffer.wrap(new byte[bytes.array().length]);
			reorderedBytes = reorderedBytes.order(ByteOrder.LITTLE_ENDIAN);
			while (bytes.hasRemaining())
				reorderedBytes.putLong(bytes.getLong());
			stream = createAudioStream(otherEndian, reorderedBytes.array());
			if (otherEndian.matches(newFormat))
				return stream;
			else
				return convertAudioStream(newFormat, stream);
		}
		return AudioSystem.getAudioInputStream(newFormat, stream);
	}

	public static boolean isPCM(AudioFormat format) {
		Encoding encoding = format.getEncoding();
		return encoding == Encoding.PCM_SIGNED
			|| encoding == Encoding.PCM_UNSIGNED
			|| encoding == Encoding.PCM_FLOAT;
	}
}
