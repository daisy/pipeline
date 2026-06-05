package org.daisy.pipeline.word_to_dtbook.impl;

public enum ImageFormat {

	Bmp,
	Emf,
	Exif,
	Gif,
	Icon,
	Jpeg,
	MemoryBmp,
	Png,
	Tiff,
	Wmf;

	public String getFormatName() {
		return name().toLowerCase();
	}

	public String getExtension() {
		return "." + getFormatName();
	}

	public static ImageFormat from(String name) {
		try {
			name = name.replaceAll("^\\.", "");
			name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
			return ImageFormat.valueOf(name);
		} catch (IllegalArgumentException e) {
			return ImageFormat.Jpeg;
		}
	}
}
