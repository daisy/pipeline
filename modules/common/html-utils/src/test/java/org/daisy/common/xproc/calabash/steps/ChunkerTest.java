package org.daisy.common.xproc.calabash.steps;

import java.util.SortedSet;
import java.util.TreeSet;

import org.daisy.common.xproc.calabash.steps.Chunker.BreakOpportunity;
import org.daisy.common.xproc.calabash.steps.Chunker.BreakOpportunity.Weight;
import org.daisy.common.xproc.calabash.steps.Chunker.BreakPosition;
import org.daisy.common.xproc.calabash.steps.Chunker.BreakPosition.Side;
import org.daisy.common.xproc.calabash.steps.Chunker.Path;

import org.junit.Assert;
import org.junit.Test;

public class ChunkerTest {
	
	@Test
	public void testComputeSplitPoints() {
		SortedSet<BreakOpportunity> opportunities = new TreeSet<>();
		SortedSet<BreakPosition> expected = new TreeSet<>();
		int bytes = 0;
		
		path.down().nextElement(); {                     //2
			path.down().nextElement(); {                 //2/2
				path.down().nextElement(); {             //2/2/2
					bytes += 100000; }
				opportunities.add(allow(here(), bytes)); // --------------
				path.nextElement(); {                    //2/2/4
					bytes += 100000; }
				path.up(); }
			opportunities.add(allow(here(), bytes));     // --------------
			path.nextElement(); {                        //2/4
				path.down().nextElement(); {             //2/4/2
					bytes += 100000; }
				opportunities.add(allow(here(), bytes)); // ==============
				expected.add(here());
				path.nextElement(); {                    //2/4/4
					bytes += 100000; }
				path.up(); }
			opportunities.add(allow(here(), bytes));     // --------------
			path.nextElement(); {                        //2/6
				path.down().nextElement(); {             //2/6/2
					bytes += 100000; }
				opportunities.add(allow(here(), bytes)); // --------------
				path.nextElement(); {                    //2/6/4
					bytes += 100000; }
				path.up(); }
			path.up(); }
		opportunities.add(allow(here(), bytes));         // ==============
		expected.add(here());
		path.nextElement(); {                            //4
			path.down().nextElement(); {                 //4/2
				path.down().nextElement(); {             //4/2/2
					bytes += 100000; }
				opportunities.add(allow(here(), bytes)); // --------------
				path.nextElement(); {                    //4/2/4
					bytes += 100000; }
				path.up(); }
			opportunities.add(allow(here(), bytes));     // --------------
			path.nextElement(); {                        //4/4
				path.down().nextElement(); {             //4/4/2
					bytes += 100000; }
				opportunities.add(allow(here(), bytes)); // ==============
				expected.add(here());
				path.nextElement(); {                    //4/4/4
					bytes += 100000; }
				path.up(); }
			opportunities.add(allow(here(), bytes));     // --------------
			path.nextElement(); {                        //4/6
				path.down().nextElement(); {             //4/6/2
					bytes += 100000; }
				opportunities.add(allow(here(), bytes)); // --------------
				path.nextElement(); {                    //4/6/4
					bytes += 100000; }}}
		
		int maxSize = 300000;
		SortedSet<BreakPosition> actual = Chunker.computeSplitPoints(opportunities, bytes, maxSize);
		Assert.assertEquals(expected, actual);
		
		opportunities.add(prefer(before("/2/4"), 200000));
		opportunities.add(prefer(before("/2/6"), 400000));
		opportunities.add(prefer(before("/4/4"), 800000));
		opportunities.add(prefer(before("/4/6"), 1000000));
		expected.clear();
		expected.add(before("/2/4"));
		expected.add(before("/2/6"));
		expected.add(after("/4/2/2"));
		expected.add(before("/4/6"));
		actual = Chunker.computeSplitPoints(opportunities, bytes, maxSize);
		Assert.assertEquals(expected, actual);
	}
	
	private Path.Builder path = new Path.Builder();
	
	private BreakPosition here() {
		return new BreakPosition(path.build(), Side.AFTER);
	}
	
	private BreakPosition before(String path) {
		return new BreakPosition(Path.parse(path), Side.BEFORE);
	}
	
	private BreakPosition after(String path) {
		return new BreakPosition(Path.parse(path), Side.AFTER);
	}
	
	private BreakOpportunity allow(BreakPosition pos, int bytes) {
		return new BreakOpportunity(pos, Weight.ALLOW, bytes);
	}
	
	private BreakOpportunity prefer(BreakPosition pos, int bytes) {
		return new BreakOpportunity(pos, Weight.PREFER, bytes);
	}
	
	private BreakOpportunity force(BreakPosition pos, int bytes) {
		return new BreakOpportunity(pos, Weight.ALWAYS, bytes);
	}
}
