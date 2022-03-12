package org.daisy.pipeline.tts.qfrency.impl;

import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.sound.sampled.AudioInputStream;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.common.file.URLs;
import org.daisy.common.shell.CommandRunner;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.TTSService.SynthesisException;
import org.daisy.pipeline.tts.Voice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QfrencyEngine extends TTSEngine {

	private final String mHostAddress;
	private final String mQfrencyPath;
	private final int mPriority;

	private final static URL ssmlTransformer = URLs.getResourceFromJAR("/transform-ssml.xsl", QfrencyEngine.class);
	private final static Logger mLogger = LoggerFactory.getLogger(QfrencyEngine.class);

	public QfrencyEngine(QfrencyService qfrencyService, String qfrencyPath, String address, int priority) {
		super(qfrencyService);
		mQfrencyPath = qfrencyPath;
		mPriority = priority;
		mHostAddress = address;
	}

	@Override
	public SynthesisResult synthesize(XdmNode ssml, Voice voice, TTSResource threadResources)
			throws SynthesisException, InterruptedException {

		File outFile = null;
		String outPath = null;
		String sentence; {
			Map<String,Object> xsltParams = new HashMap<>(); {
				xsltParams.put("voice", voice.name);
			}
			try {
				sentence = transformSsmlNodeToString(ssml, ssmlTransformer, xsltParams);
			} catch (IOException | SaxonApiException e) {
				throw new SynthesisException(e);
			}
		}
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
			new CommandRunner(lCmd)
				.consumeError(mLogger)
				.run();
			AudioInputStream audio = createAudioStream(
				new FileInputStream(outFile));
			outFile.delete();
			new File(outPath+".sutt").delete();
			new File(outPath+".TextGrid").delete();
			return new SynthesisResult(audio);
		} catch (InterruptedException e) {
			throw e;
		} catch (Throwable e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			throw new SynthesisException(e);
		}
	}

	public int reservedThreadNum() {
		return 1;
	}

	/**
	 * Need not be thread-safe. This method is called from the main thread.
	 */
	public Collection<Voice> getAvailableVoices() throws SynthesisException,
														 InterruptedException {
		ArrayList<Voice> list = new ArrayList<Voice>();
		try {
			new CommandRunner(mQfrencyPath, "-a", mHostAddress, "-l")
				.consumeOutput(stream -> {
						try (Scanner scanner = new Scanner(stream)) {
							while (scanner.hasNext())
								list.add(new Voice(getProvider().getName(), scanner.next()));
						}
					}
				)
				.consumeError(mLogger)
				.run();
			return list;
		} catch (Throwable x) {
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
