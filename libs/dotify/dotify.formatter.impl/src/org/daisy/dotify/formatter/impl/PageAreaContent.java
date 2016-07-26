package org.daisy.dotify.formatter.impl;

import java.util.ArrayList;
import java.util.List;

class PageAreaContent {
	private List<RowImpl> before;
	private List<RowImpl> after;


	PageAreaContent(PageAreaBuilderImpl pab, BlockContext bc) {
		this.before = new ArrayList<>();
		this.after = new ArrayList<>();
		if (pab !=null) {
			//Assumes before is static
			for (Block b : pab.getBeforeArea()) {
				for (RowImpl r : b.getBlockContentManager(bc)) {
					before.add(r);
				}
			}

			//Assumes after is static
			for (Block b : pab.getAfterArea()) {
				for (RowImpl r : b.getBlockContentManager(bc)) {
					after.add(r);
				}
			}
		}
	}
	
	List<RowImpl> getBefore() {
		return before;
	}

	List<RowImpl> getAfter() {
		return after;
	}

}
