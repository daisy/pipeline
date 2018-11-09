package org.daisy.dotify.formatter.impl.sheet;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

class EvenSizeVolumeSplitter implements VolumeSplitter {
	private static final Logger logger = Logger.getLogger(EvenSizeVolumeSplitter.class.getCanonicalName());
	private EvenSizeVolumeSplitterCalculator sdc;
	private final SplitterLimit splitterMax;
	int volumeOffset = 0;
	
	/*
	 * This map keeps track of which split suggestions resulted in a successful split. We
	 * make use of this information in order to not get into an endless loop while looking
	 * for the optimal number of volumes.
	 */
	private Map<EvenSizeVolumeSplitterCalculator,Boolean> previouslyTried = new HashMap<>();
	
	EvenSizeVolumeSplitter(SplitterLimit splitterMax) {
		this.splitterMax = splitterMax;
	}
	
	@Override
	public void updateSheetCount(int sheets, int remainingSheets) {
		if (sdc == null) {
			sdc = new EvenSizeVolumeSplitterCalculator(sheets, splitterMax, volumeOffset);
		} else {
			boolean sheetsFitInVolumes = remainingSheets == 0;
			EvenSizeVolumeSplitterCalculator prvSdc = sdc;
			sdc = null;
			previouslyTried.put(prvSdc, sheetsFitInVolumes);
			if (!sheetsFitInVolumes) {
				EvenSizeVolumeSplitterCalculator esc = new EvenSizeVolumeSplitterCalculator(sheets, splitterMax, volumeOffset);
				
				// Try increasing the volume count
				if (remainingSheets >= sheets)
					throw new IllegalStateException();
				int volumeInc; {
					double inc = (1.0 * prvSdc.getVolumeCount() * remainingSheets) / (sheets - remainingSheets);
					// subtract increase in volume count due to increase in sheet count
					inc -= (esc.getVolumeCount() - prvSdc.getVolumeCount());
					// factor 3/4 because we don't want to adapt too fast
					inc *= .75;
					volumeInc = (int)Math.floor(inc);
				}
				if (volumeInc > 0) {
					volumeOffset += volumeInc;
					sdc = new EvenSizeVolumeSplitterCalculator(sheets, splitterMax, volumeOffset);
				} else {
					
					// Try with adjusted number of sheets
					if (!previouslyTried.containsKey(esc) || previouslyTried.get(esc)) {
						sdc = esc;
					} else {
						volumeOffset += 1;
						sdc = new EvenSizeVolumeSplitterCalculator(sheets, splitterMax, volumeOffset);
					}
				}
			} else {
				if (volumeOffset > 0) {
					
					// Try decreasing the volume count again
					EvenSizeVolumeSplitterCalculator esc = new EvenSizeVolumeSplitterCalculator(sheets, splitterMax, volumeOffset - 1);
					if (!previouslyTried.containsKey(esc)) {
						volumeOffset--;
						sdc = esc;
					}
				}
				if (sdc == null) {
					
					// Try with up to date sheet count
					EvenSizeVolumeSplitterCalculator esc = new EvenSizeVolumeSplitterCalculator(sheets, splitterMax, volumeOffset);
					if (!previouslyTried.containsKey(esc)) {
						sdc = esc;
					} else {
						sdc = prvSdc;
					}
				}
			}
		}
	}

	@Override
	public int sheetsInVolume(int volIndex) {
		return sdc.sheetsInVolume(volIndex);
	}

	@Override
	public int getVolumeCount() {
		return sdc.getVolumeCount();
	}
}
