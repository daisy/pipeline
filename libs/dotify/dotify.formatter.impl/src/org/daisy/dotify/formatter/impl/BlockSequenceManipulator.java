package org.daisy.dotify.formatter.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import org.daisy.dotify.api.formatter.SequenceProperties;

/**
 * Provides methods for manipulating a flow sequence.
 * @author Joel Håkansson
 */
class BlockSequenceManipulator {
	private HashMap<String, Integer> taggedEntries;
	private final Stack<Block> sequence;
	private final SequenceProperties props;
	private final LayoutMaster master;
	
	public BlockSequenceManipulator(BlockSequence b) {
		this.sequence = new Stack<>();
		for (Block bb : b) {
			this.sequence.add(bb);
		}
		this.master = b.getLayoutMaster();
		this.props = b.getSequenceProperties();
		this.taggedEntries = tagSequence(this.sequence);
	}
	
	public BlockSequenceManipulator(LayoutMaster master, SequenceProperties props) {
		this.sequence = new Stack<>();
		this.master = master;
		this.props = props;
		this.taggedEntries = tagSequence(this.sequence);
	}

	private BlockSequence newSequence(List<Block> c) {
		//FIXME: this will break if the returned sequence attempts to use some methods 
		BlockSequence ret = new BlockSequence(null, props, master);
		ret.addAll(c);
		return ret;
	}
	
	public BlockSequence newSequence() {
		return newSequence(sequence);
	}
	
	/*
	public BlockSequence newSubSequence(String fromId) {
		Integer fromIndex = taggedEntries.get(fromId);
		if (fromIndex==null) {
			throw new IllegalArgumentException("Cannot find identifier " + fromId);
		}
		return newSequence(sequence.subList(fromIndex, sequence.size()));
	}*/
	
	public void insertGroup(Iterable<Block> blocks, String beforeId) {
		ArrayList<Block> call = new ArrayList<>();
		for (Block b : blocks) {
			call.add(b);
		}
		insertGroup(call, beforeId);
	}
	public void appendGroup(Iterable<Block> blocks) {
		ArrayList<Block> call = new ArrayList<>();
		for (Block b : blocks) {
			call.add(b);
		}
		sequence.addAll(call);
		taggedEntries = tagSequence(sequence);
	}
	
	public void insertGroup(Collection<Block> seq, String beforeId) {
		Integer beforeIndex = taggedEntries.get(beforeId);
		if (beforeIndex==null) {
			throw new IllegalArgumentException("Cannot find identifier " + beforeId);
		}
		sequence.addAll(beforeIndex, seq);
		taggedEntries = tagSequence(sequence);
	}

	public void removeGroup(String id) {
		Integer index = taggedEntries.get(id);
		if (index==null) {
			throw new IllegalArgumentException("Cannot find identifier " + id);
		}
		sequence.removeElementAt(index);
		taggedEntries = tagSequence(sequence);
	}
	
	public void removeRange(String fromId, String toId) {
		Integer fromIndex = taggedEntries.get(fromId);
		Integer toIndex = taggedEntries.get(toId);
		if (fromIndex==null || toIndex==null) {
			throw new IllegalArgumentException("Cannot find identifier " + fromId + "/" + toId);
		}
		for (int i=0; i<toIndex-fromIndex; i++) {
			sequence.remove((int)fromIndex);
		}
		taggedEntries = tagSequence(sequence);
	}
	
	public void removeTail(String fromId) {
		Integer fromIndex = taggedEntries.get(fromId);
		fromIndex++;
		int count = sequence.size()-fromIndex;
		for (int i=0; i<count; i++) {
			sequence.remove((int)fromIndex);
		}
		taggedEntries = tagSequence(sequence);
	}

	/*
	public BlockSequence newSubSequence(String fromId, String toId) {
		Integer fromIndex = taggedEntries.get(fromId);
		Integer toIndex = taggedEntries.get(toId);
		if (fromIndex==null || toIndex==null) {
			throw new IllegalArgumentException("Cannot find identifier " + fromId + "/" + toId);
		}
		toIndex++;
		// subList handles checking of fromIndex>toIndex
		return newSequence(sequence.subList(fromIndex, toIndex));
	}*/
	/*
	public BlockSequence newFromItem(String id) {
		return newSubSequence(id, id);
	}
	*/
	private static HashMap<String, Integer> tagSequence(List<Block> seq) {
		HashMap<String, Integer> entries = new HashMap<>();
		int i = 0;
		for (Block group : seq) {
			if (group.getBlockIdentifier()!=null && !group.getBlockIdentifier().equals("")) {
				if (entries.put(group.getBlockIdentifier(), i)!=null) {
					throw new IllegalArgumentException("Duplicate id " + group.getBlockIdentifier());
				}
				//System.out.println("GROUP! " + fg.getIdentifier());
			}
			i++;
		}
		return entries;
	}
	
	public List<Block> getBlocks() {
		return sequence;
	}
	


}
