package org.daisy.pipeline.job.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;

public final class StatusResultProvider implements Supplier<Result> {

	public StatusResultProvider(String port) {
		portName = port;
	}

	private final String portName;
	private List<ByteArrayOutputStream> results = new LinkedList<>();
	
	public Iterable<InputStream> read() {
		return Iterables.transform(results, o -> new ByteArrayInputStream(o.toByteArray()));
	}
	
	@Override
	public Result get() {
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		results.add(result);
		return new StreamResult(result);
	}
}
