package org.daisy.dotify.formatter.impl.page;

import static org.junit.Assert.assertEquals;

import java.util.TreeSet;

import org.daisy.dotify.formatter.impl.page.TabStopString.Alignment;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class TabStopStringTest {

	@Test
	public void testTabStopStringComparableImpl() {
		//Setup
    	TreeSet<TabStopString> ts = new TreeSet<>();
    	ts.add(new TabStopString("Text1", 15));
    	ts.add(new TabStopString("Text2", 13));
    	ts.add(new TabStopString("Text3", 3));
    	ts.add(new TabStopString("Text4", 27));
    	ts.add(new TabStopString("Text", 11, Alignment.CENTER));
    	ts.add(new TabStopString("Text", 11));
    	ts.add(new TabStopString("Text", 11,  Alignment.LEFT, "1"));
    	
    	TabStopString[] tss = ts.toArray(new TabStopString[]{});
    	
    	//Test
    	assertEquals("Assert order and defaults", new TabStopString("Text3", 3, Alignment.LEFT, " "), tss[0]);
    	assertEquals("Assert order and defaults", new TabStopString("Text", 11, Alignment.LEFT, " "), tss[1]);
    	assertEquals("Assert order and defaults", new TabStopString("Text", 11,  Alignment.LEFT, "1"), tss[2]);
    	assertEquals("Assert order and defaults", new TabStopString("Text", 11, Alignment.CENTER, " "), tss[3]);
    	assertEquals("Assert order and defaults", new TabStopString("Text2", 13, Alignment.LEFT, " "), tss[4]);
    	assertEquals("Assert order and defaults", new TabStopString("Text1", 15, Alignment.LEFT, " "), tss[5]);
    	assertEquals("Assert order and defaults", new TabStopString("Text4", 27, Alignment.LEFT, " "), tss[6]);
    	
    	/*
    	{"Text3", 3, LEFT, " "}
    	{"Text", 11, LEFT, " "}
    	{"Text", 11, LEFT, "1"}
    	{"Text", 11, CENTER, " "}
    	{"Text2", 13, LEFT, " "}
    	{"Text1", 15, LEFT, " "}
    	{"Text4", 27, LEFT, " "}
    	
    	for (TabStopString tss : ts) {
    		System.out.println(tss.toString());
    	}*/
	}
	
	public void testTabStopStringEquals() {
		//Setup
		TabStopString tss1 = new TabStopString("Text", 1);
		TabStopString tss2 = new TabStopString("Text", 1);
		
		//Test
		assertEquals("Assert equal", tss1, tss2);
	}

}
