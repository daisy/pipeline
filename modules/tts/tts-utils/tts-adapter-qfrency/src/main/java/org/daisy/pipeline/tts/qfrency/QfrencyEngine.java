package org.daisy.pipeline.tts.qfrency;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import net.sf.saxon.s9api.XdmNode;

import org.daisy.pipeline.audio.AudioBuffer;
import org.daisy.pipeline.tts.AudioBufferAllocator;
import org.daisy.pipeline.tts.AudioBufferAllocator.MemoryException;
import org.daisy.pipeline.tts.SoundUtil;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.TTSService.Mark;
import org.daisy.pipeline.tts.TTSService.SynthesisException;
import org.daisy.pipeline.tts.Voice;

public class QfrencyEngine extends TTSEngine {

	private AudioFormat mAudioFormat;
	private String mHostAddress;
	private String mQfrencyPath;
	private final static int MIN_CHUNK_SIZE = 2048;
	private int mPriority;

	public QfrencyEngine(QfrencyService qfrencyService, String qfrencyPath, String address, int priority) {
		super(qfrencyService);
		mQfrencyPath = qfrencyPath;
		mPriority = priority;
		mHostAddress = address;
	}

	@Override
	public Collection<AudioBuffer> synthesize(String sentence, XdmNode xmlSentence,
											  Voice voice, TTSResource threadResources, List<Mark> marks, List<String> oldMarks,
											  AudioBufferAllocator bufferAllocator, boolean retry) throws SynthesisException,
																										  InterruptedException, MemoryException {

		Collection<AudioBuffer> result = new ArrayList<AudioBuffer>();
		Process proc = null;
		File outFile = null;
		String outPath = null;
		sentence = stripSSML(sentence);
		try {
					outFile = File.createTempFile("dp2_qfrency_", ".wav");
		outFile.deleteOnExit();
		outPath = outFile.getPath();
			String [] lCmd = new String[7];
			lCmd[0]=mQfrencyPath;
			lCmd[1]="-a";
			lCmd[2]=mHostAddress;
			lCmd[3]="-s";
			lCmd[4]=outPath;
			lCmd[5]=voice.name;
			lCmd[6]="\'"+sentence+"\'";

			proc = Runtime.getRuntime().exec(lCmd);
			proc.waitFor();

			BufferedInputStream in = new BufferedInputStream(new FileInputStream(outFile));
			AudioInputStream fi = AudioSystem.getAudioInputStream(in);

			if (mAudioFormat == null)
				mAudioFormat = fi.getFormat();

			while (true) {
				AudioBuffer b = bufferAllocator
					.allocateBuffer(MIN_CHUNK_SIZE + fi.available());
				int ret = fi.read(b.data, 0, b.size);
				if (ret == -1) {
					bufferAllocator.releaseBuffer(b);
					break;
				}
				b.size = ret;
				result.add(b);
			}
			fi.close();


							} catch (MemoryException e) {
			SoundUtil.cancelFootPrint(result, bufferAllocator);
			proc.destroy();
			throw e;
		} catch (InterruptedException e) {
			SoundUtil.cancelFootPrint(result, bufferAllocator);
			if (proc != null)
				proc.destroy();
			throw e;
		} catch (Exception e) {
			SoundUtil.cancelFootPrint(result, bufferAllocator);
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			if (proc != null)
				proc.destroy();
			throw new SynthesisException(e);
		}
		outFile.delete();
		new File(outPath+".sutt").delete();
		new File(outPath+".TextGrid").delete();
		return result;
	}

	public int reservedThreadNum() {
		return 1;
	}

	
	@Override
	public AudioFormat getAudioOutputFormat() {
		return mAudioFormat;
	}


	/**
	 * Need not be thread-safe. This method is called from the main thread.
	 */
	public Collection<Voice> getAvailableVoices() throws SynthesisException,
														 InterruptedException {
		Scanner scanner = null;
		InputStream is = null;
		ArrayList<Voice> list = new ArrayList<Voice>();
		Process proc = null;
		try {
			proc = Runtime.getRuntime().exec(new String[] {mQfrencyPath, "-a", mHostAddress, "-l"});
			is = proc.getInputStream();
			scanner = new Scanner(is);
			while (scanner.hasNext())
				list.add(new Voice(getProvider().getName(), scanner.next()));
			scanner.close();
			return list;
		} catch(Exception x) {
			System.out.println("Error listing voices");
			x.printStackTrace();
		}
		return new ArrayList<Voice>();
	}

	
	@Override
	public int getOverallPriority() {
		return mPriority;
	}

	@Override
	public TTSResource allocateThreadResources() throws SynthesisException,
														InterruptedException {
		return new TTSResource();
	}

	
    private String stripSSML(String in) {
		String ret = "";
		boolean inTag=false;
		for (int i = 0; i < in.length(); i++) {
			if (in.charAt(i)=='<')
				inTag=true;
			else if (in.charAt(i)=='>')
				inTag=false;
			if (!inTag && in.charAt(i)!='>')
				ret+=in.charAt(i);
		}
		return ret;
    }
}
