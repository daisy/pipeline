package org.daisy.dotify.formatter.impl;

import java.util.ArrayList;
import java.util.List;

import org.daisy.dotify.api.formatter.FormatterCore;
import org.daisy.dotify.api.formatter.ItemSequenceProperties;
import org.daisy.dotify.api.formatter.ReferenceListBuilder;

class ItemSequenceEventImpl implements ReferenceListBuilder, BlockGroup {
	private final String collectionID;
	private final ItemSequenceProperties.Range range;

	private final FormatterCoreImpl collectionStartEvents;
	private final FormatterCoreImpl pageStartEvents;
	private final FormatterCoreImpl pageEndEvents;
	private final FormatterCoreImpl collectionEndEvents;
	
	public ItemSequenceEventImpl(FormatterCoreContext fc, ItemSequenceProperties.Range range, String collectionID) {
		this.collectionID = collectionID;
		this.range = range;
		this.collectionStartEvents = new FormatterCoreImpl(fc);
		this.pageStartEvents = new FormatterCoreImpl(fc);
		this.pageEndEvents = new FormatterCoreImpl(fc);
		this.collectionEndEvents = new FormatterCoreImpl(fc);
	}

	@Override
	public FormatterCore newOnCollectionStart() {
		return collectionStartEvents;
	}

	@Override
	public FormatterCore newOnPageStart() {
		return pageStartEvents;
	}

	@Override
	public FormatterCore newOnPageEnd() {
		return pageEndEvents;
	}
	
	@Override
	public FormatterCore newOnCollectionEnd() {
		return collectionEndEvents;
	}

	@Override
	public List<Block> getBlocks(FormatterContext context, DefaultContext vars, CrossReferenceHandler crh) {
		ContentCollectionImpl c = context.getCollections().get(collectionID);
		ArrayList<Block> ret = new ArrayList<>();
		if (c==null) {
			return ret;
		}

		ret.addAll(collectionStartEvents);
		boolean hasContents = false;
		for (int i=0; i<crh.getVolumeCount(); i++) {
			Iterable<AnchorData> v = crh.getAnchorData(i+1);
			if (v!=null) {
				for (AnchorData ad : v) {
					ArrayList<String> refs = new ArrayList<>();
					for (String a : ad.getAnchors()) {
						if (c.containsItemID(a) && !refs.contains(a)) {
							refs.add(a);
						}
					}
					if (!refs.isEmpty() && (range == ItemSequenceProperties.Range.DOCUMENT || (i+1)==vars.getCurrentVolume())) {
						hasContents = true;
						{
							ArrayList<Block> b = new ArrayList<>();
							for (Block blk : pageStartEvents) {
								Block bl = (Block)blk.clone();
								bl.setMetaPage(ad.getPageIndex()+1);
								b.add(bl);
							}
							ret.addAll(b);
						}
						for (String key : refs) {
							ret.addAll(c.getBlocks(key));
						}
						{
							ArrayList<Block> b = new ArrayList<>();
							for (Block blk : pageEndEvents) {
								Block bl = (Block)blk.clone();
								bl.setMetaPage(ad.getPageIndex()+1);
								b.add(bl);
							}
							ret.addAll(b);
						}
					}
				}
			}
		}
		ret.addAll(collectionEndEvents);
		if (hasContents) {
			//only add a section if there are notes in it.
			return ret;
		}
		return new ArrayList<>();
	}

	@Override
	public boolean isGenerated() {
		return true;
	}


}
