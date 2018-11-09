package org.daisy.dotify.formatter.impl.row;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daisy.dotify.api.formatter.Leader;
import org.daisy.dotify.api.translator.BrailleTranslator;
import org.daisy.dotify.api.translator.Translatable;
import org.daisy.dotify.api.translator.TranslationException;
import org.daisy.dotify.common.text.StringTools;

class LeaderManager {
	private static final Logger logger = Logger.getLogger(LeaderManager.class.getCanonicalName());
	private Deque<Leader> leaders;

	LeaderManager() {
		this.leaders = new ArrayDeque<>();
	}

	LeaderManager(LeaderManager template) {
		this.leaders = new ArrayDeque<>(template.leaders);
	}

	void addLeader(Leader leader) {
		this.leaders.addLast(leader);
	}

	boolean hasLeader() {
		return !leaders.isEmpty();
	}

	void removeLeader() {
		leaders.pollFirst();
	}
	
	void discardAllLeaders() {
		leaders.clear();
	}
	
	Leader getCurrentLeader() {
		return leaders.getFirst();
	}

	int getLeaderPosition(int width) {
		if (hasLeader()) {
			return getCurrentLeader().getPosition().makeAbsolute(width);
		} else {
			return 0;
		}
	}

	int getLeaderAlign(int length) {
		if (hasLeader()) {
			switch (getCurrentLeader().getAlignment()) {
				case LEFT:
					return 0;
				case RIGHT:
					return length;
				case CENTER:
					return length/2;
			}
		}
		return 0;
	}

	String getLeaderPattern(BrailleTranslator translator, int len) {
		if (!hasLeader()) {
			return "";
		} else if (len > 0) {
			String leaderPattern;
			try {
				leaderPattern = translator.translate(Translatable.text(getCurrentLeader().getPattern()).build()).getTranslatedRemainder();
			} catch (TranslationException e) {
				throw new RuntimeException(e);
			}
			return StringTools.fill(leaderPattern, len);
		} else {
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Leader position has been passed on an empty row or text does not fit on an empty row, ignoring...");
			}
			return "";
		}
	}

}
