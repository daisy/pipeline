package org.daisy.dotify.text;


public class TestNSH {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/*
		BreakPointHandler bph = new BreakPointHandler.Builder("til\u00adlåta").
				addHyphenationInfo(2, 3, "ll\u00adl").build();
		BreakPoint bp = bph.nextRow(4, false);*/
		
		BreakPointHandler bph = new BreakPointHandler.Builder("Det ska vara til\u00adlåtet.").
				addHyphenationInfo(15, 3, "ll\u00adl").build();
		bph.nextRow(7, false);
		BreakPoint bp = bph.nextRow(11, false);
		
		System.out.println(bp.getHead());
		System.out.println( bp.getTail());
		System.out.println(!bp.isHardBreak());
	}

}
