package org.daisy.dotify.formatter.impl;

import java.util.ArrayList;
import java.util.List;

import org.daisy.dotify.api.formatter.SequenceProperties.SequenceBreakBefore;

class PageStructBuilder {

	private final FormatterContext context;
	private final Iterable<BlockSequence> fs;
	private final CrossReferenceHandler crh;
	private PageStruct struct;
	

	public PageStructBuilder(FormatterContext context, Iterable<BlockSequence> fs, CrossReferenceHandler crh) {
		this.context = context;
		this.fs = fs;
		this.crh = crh;
	}

	List<Sheet> paginate(DefaultContext rcontext) throws PaginatorException {
		try {
		restart:while (true) {
			crh.resetUniqueChecks();
			struct = new PageStruct();
			List<Sheet> ret = new ArrayList<>();
			boolean volBreakAllowed = true;
			for (BlockSequence bs : fs) {
				try {
					PageSequence seq = newSequence(bs, rcontext);
					LayoutMaster lm = seq.getLayoutMaster();
					Sheet.Builder s = null;
					SheetIdentity si = null;
					List<PageImpl> pages = seq.getPages();
					int sheetIndex = 0;
					for (int pageIndex = 0; pageIndex<pages.size(); pageIndex++) {
						PageImpl p = pages.get(pageIndex);
						if (!lm.duplex() || pageIndex % 2 == 0) {
							volBreakAllowed = true;
							if (s!=null) {
								ret.add(s.build());
							}
							s = new Sheet.Builder();
							si = new SheetIdentity(rcontext.getSpace(), rcontext.getCurrentVolume()==null?0:rcontext.getCurrentVolume(), ret.size());
							sheetIndex++;
						}
						s.avoidVolumeBreakAfterPriority(p.getAvoidVolumeBreakAfter());
						if (sheetIndex==1 && bs.getSequenceProperties().getBreakBeforeType()==SequenceBreakBefore.VOLUME) {
							s.startNewVolume(true);
						}
						if (pageIndex==pages.size()-1) {
							s.avoidVolumeBreakAfterPriority(null);
							//Don't get or store this value in crh as it is transient and not a property of the sheet context
							s.breakable(true);
						} else {
							boolean br = crh.getBreakable(si);
							//TODO: the following is a low effort way of giving existing uses of non-breakable units a high priority, but it probably shouldn't be done this way
							if (!br) {
								s.avoidVolumeBreakAfterPriority(1);
							}
							s.breakable(br);
						}

						setPreviousSheet(si.getSheetIndex()-1, Math.min(p.keepPreviousSheets(), sheetIndex-1), rcontext);
						volBreakAllowed &= p.allowsVolumeBreak();
						if (!lm.duplex() || pageIndex % 2 == 1) {
							crh.keepBreakable(si, volBreakAllowed);
						}
						s.add(p);
					}
					if (s!=null) {
						//Last page in the sequence doesn't need volume keep priority
						ret.add(s.build());
					}
				} catch (RestartPaginationException e) {
					continue restart;
				}
			}
			return ret;
		}
		} finally {
			crh.commitBreakable();
		}
	}

	private PageSequence newSequence(BlockSequence seq, DefaultContext rcontext) throws PaginatorException, RestartPaginationException {
		int offset = getCurrentPageOffset();
		PageSequence ps = new PageSequence(struct, seq.getLayoutMaster(), seq.getInitialPageNumber()!=null?seq.getInitialPageNumber() - 1:offset);
		PageSequenceBuilder2 psb = new PageSequenceBuilder2(ps.getLayoutMaster(), ps.getPageNumberOffset(), crh, seq, context, rcontext);
		struct.add(ps);
		while (psb.hasNext()) {
			PageImpl p = psb.nextPage();
			p.setSequenceParent(ps);
			//This is for pre/post volume contents, where the volume number is known
			if (rcontext.getCurrentVolume()!=null) {
				for (String id : p.getIdentifiers()) {
					crh.setVolumeNumber(id, rcontext.getCurrentVolume());
				}
			}
			ps.addPage(p);
		}
		return ps;
	}
	
	private int getCurrentPageOffset() {
		if (struct.size()>0) {
			PageSequence prv = (PageSequence)struct.peek();
			if (prv.getLayoutMaster().duplex() && (prv.getPageCount() % 2)==1) {
				return prv.getPageNumberOffset() + prv.getPageCount() + 1;
			} else {
				return prv.getPageNumberOffset() + prv.getPageCount();
			}
		} else {
			return 0;
		}
	}
	
	void setVolumeScope(int volumeNumber, int fromIndex, int toIndex) {
		struct.setVolumeScope(volumeNumber, fromIndex, toIndex);
	}

	private void setPreviousSheet(int start, int p, DefaultContext rcontext) {
		int i = 0;
		//TODO: simplify this?
		for (int x = start; i < p && x > 0; x--) {
			SheetIdentity si = new SheetIdentity(rcontext.getSpace(), rcontext.getCurrentVolume()==null?0:rcontext.getCurrentVolume(), x);
			crh.keepBreakable(si, false);
			i++;
		}
	}

}