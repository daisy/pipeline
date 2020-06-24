package org.liblouis;

import com.sun.jna.NativeMapped;

interface WideChar extends NativeMapped {
	public static final int SIZE = Louis.getLibrary().lou_charSize();
}
