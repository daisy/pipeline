package org.daisy.dotify.formatter.impl.sheet;

import java.util.ArrayList;
import java.util.List;

import org.daisy.dotify.formatter.impl.search.Overhead;
import org.daisy.dotify.formatter.impl.writer.Section;
import org.daisy.dotify.formatter.impl.writer.Volume;

/**
 * Provides a container for a physical volume of braille
 * @author Joel Håkansson
 */
public class VolumeImpl implements Volume {
	private List<Section> body;
	private List<Section> preVolData;
	private List<Section> postVolData;
	private Overhead overhead;
	private int bodyVolSize;
	
	public VolumeImpl(Overhead overhead) {
		this.overhead = overhead;
		this.bodyVolSize = 0;
	}

	public void setBody(SectionBuilder body) {
		bodyVolSize = body.getSheetCount();
		this.body = body.getSections();
	}
	
	public void setPreVolData(SectionBuilder preVolData) {
		//use the highest value to avoid oscillation
		overhead = overhead.withPreContentSize(Math.max(overhead.getPreContentSize(), preVolData.getSheetCount()));
		this.preVolData = preVolData.getSections();
	}

	public void setPostVolData(SectionBuilder postVolData) {
		//use the highest value to avoid oscillation
		overhead = overhead.withPostContentSize(Math.max(overhead.getPostContentSize(), postVolData.getSheetCount()));
		this.postVolData = postVolData.getSections();
	}
	
	public Overhead getOverhead() {
		return overhead;
	}
	
	public int getBodySize() {
		return bodyVolSize;
	}
	
	public int getVolumeSize() {
		return overhead.total() + bodyVolSize;
	}

	@Override
	public Iterable<? extends Section> getSections() {
		List<Section> contents = new ArrayList<>();
		contents.addAll(preVolData);
		contents.addAll(body);
		contents.addAll(postVolData);
		return contents;
	}

}