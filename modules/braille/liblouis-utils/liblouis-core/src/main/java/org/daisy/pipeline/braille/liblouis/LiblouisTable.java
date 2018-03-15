package org.daisy.pipeline.braille.liblouis;

import java.net.URI;

import com.google.common.base.Splitter;
import static com.google.common.collect.Iterables.toArray;
import static com.google.common.collect.Iterables.transform;

import static org.daisy.pipeline.braille.common.util.Strings.join;
import static org.daisy.pipeline.braille.common.util.URIs.asURI;

public class LiblouisTable {
	
	private final URI[] table;
	
	public LiblouisTable(URI[] table) {
		this.table = table;
	}
	
	public LiblouisTable(String table) {
		this(tokenizeTable(table));
	}
	
	public URI[] asURIs() {
		return table;
	}
	
	@Override
	public String toString() {
		return serializeTable(table);
	}
	
	public static URI[] tokenizeTable(String table) {
		return toArray(
			transform(
				Splitter.on(',').split(table),
				asURI),
			URI.class);
	}
	
	public static String serializeTable(URI[] table) {
		return join(table, ",");
	}
}
