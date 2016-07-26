package org.daisy.dotify.formatter.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Provides a container for a physical volume of braille
 * @author Joel Håkansson
 */
class VolumeImpl implements Volume {
	private List<Section> body;
	private List<Section> preVolData;
	private List<Section> postVolData;
	private int preVolSize;
	private int postVolSize;
	private int bodyVolSize;
	private int targetVolSize;
	
	VolumeImpl() {
		this.preVolSize = 0;
		this.postVolSize = 0;
		this.bodyVolSize = 0;
		this.targetVolSize = 0;
	}

	public void setBody(List<Sheet> body) {
		bodyVolSize = body.size();
		this.body = sequencesFromSheets(body);
	}
	
	private static List<Section> sequencesFromSheets(List<Sheet> sheets) {
		Stack<Section> ret = new Stack<Section>();
		PageSequence currentSeq = null;
		for (Sheet s : sheets) {
			for (PageImpl p : s.getPages()) {
				if (ret.isEmpty() || currentSeq!=p.getSequenceParent()) {
					currentSeq = p.getSequenceParent();
					ret.add(
							new SectionImpl(currentSeq.getSectionProperties())
							//new PageSequence(ret, currentSeq.getLayoutMaster(), currentSeq.getPageNumberOffset())
							);
				}
				((SectionImpl)ret.peek()).addPage(p);
			}
		}
		return ret;
	}

	public void setPreVolData(List<Sheet> preVolData) {
		//use the highest value to avoid oscillation
		preVolSize = Math.max(preVolSize, preVolData.size());
		this.preVolData = sequencesFromSheets(preVolData);
	}

	public void setPostVolData(List<Sheet> postVolData) {
		//use the highest value to avoid oscillation
		postVolSize = Math.max(postVolSize, postVolData.size());
		this.postVolData = sequencesFromSheets(postVolData);
	}
	
	public int getOverhead() {
		return preVolSize + postVolSize;
	}
	
	public int getBodySize() {
		return bodyVolSize;
	}
	
	public int getVolumeSize() {
		return preVolSize + postVolSize + bodyVolSize;
	}

	public int getTargetSize() {
		return targetVolSize;
	}

	public void setTargetVolSize(int targetVolSize) {
		this.targetVolSize = targetVolSize;
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