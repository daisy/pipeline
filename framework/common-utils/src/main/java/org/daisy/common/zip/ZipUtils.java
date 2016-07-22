package org.daisy.common.zip;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;

public class ZipUtils  {
	private static final Logger logger = LoggerFactory.getLogger(ZipUtils.class);
	/**
	 * Deflates the given string and returns it as a byte array
	 * This method is consistent with {@link #inflate(byte[] buffer)} 
	 * @param str
	 * @return the deflated version of str
	 */
	public static byte[] deflate(String str) throws IOException{
		//input str ==> ZipOutputStream ==> compressed bytes
		if(!str.isEmpty()){
			ByteArrayInputStream inByte= new ByteArrayInputStream(str.getBytes());
			ByteArrayOutputStream outByte= new ByteArrayOutputStream();
			GZIPOutputStream outZip=new GZIPOutputStream(outByte);
			long copied = ByteStreams.copy(inByte,outZip);
			outByte.close();
			outZip.close();
			inByte.close();
			logger.debug(String.format("Deflated %f%%",((double)str.length())/((double) copied)));
			return outByte.toByteArray();
		}else{
			return new byte[]{};
		}
	}

	/**
	 * Inflates the byte array assuming that is a string and returs it
	 * This method is consistent with {@link #deflate(String)} 
	 * @param bytes 
	 * @return the inflated string 
	 */
	public static String inflate(byte[] bytes) throws IOException{
		//compressed input bytes ==> ZipInputStream==> uncompressed String 
		if(bytes.length>0){
			ByteArrayInputStream inByte= new ByteArrayInputStream(bytes);
			GZIPInputStream inZip=new GZIPInputStream(inByte);
			ByteArrayOutputStream outByte= new ByteArrayOutputStream();
			long copied = ByteStreams.copy(inZip,outByte);
			outByte.close();
			inByte.close();
			inZip.close();
			logger.debug(String.format("Inflated %f%%",((double)bytes.length)/((double) copied)));
			return new String(outByte.toByteArray());
		}else{
			return new String("");
		}
	}

}
