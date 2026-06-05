package org.daisy.pipeline.word_to_dtbook.impl;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.RenderingHints;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.imageio.ImageIO;

public class ImageProcessing {

	static BufferedImage ResampleImage(BufferedImage SrcImage, float resampleValue) {
		float scaleFactorX = resampleValue / 96; // FIXME: assuming 96 dpi
		float scaleFactorY = resampleValue / 96; // FIXME: assuming 96 dpi
		int Width = (int)(SrcImage.getWidth() * scaleFactorX);
		int Height = (int)(SrcImage.getHeight() * scaleFactorY);
		BufferedImage bmPhoto = new BufferedImage(Width, Height, BufferedImage.TYPE_INT_RGB);
		try {
			Graphics2D grPhoto = bmPhoto.createGraphics();
			grPhoto.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			grPhoto.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			grPhoto.drawImage(SrcImage, 0, 0, Width, Height, null);
			grPhoto.dispose();
		} catch (Exception e) {
			throw new RuntimeException("Unable to resample the image", e);
		}
		return bmPhoto;
	}

	static String SaveProcessedImage(BufferedImage ProcessedImage,
	                                 File OutPutFolder, String ImageName, String ImageFrmt) {
		ImageFormat Format = ImageFormat.from(ImageFrmt);
		try {
			File tempSave = new File(OutPutFolder, ImageName + Format.getExtension());
			ImageIO.write(ProcessedImage, Format.getFormatName(), tempSave);
			return UriEscape(tempSave.getName());
		} catch (Exception e) {
			throw new RuntimeException("Unable to save the image", e);
		}
	}

	static String UriEscape(String imgName) {
		try {
			return URLEncoder.encode(imgName, "UTF-8").replace("+", "%20");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
}
