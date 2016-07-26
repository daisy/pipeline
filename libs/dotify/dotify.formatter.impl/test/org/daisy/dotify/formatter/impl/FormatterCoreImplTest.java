package org.daisy.dotify.formatter.impl;

import org.junit.Test;

import static org.junit.Assert.*;

import org.daisy.dotify.api.formatter.BlockProperties;
import org.daisy.dotify.formatter.impl.Margin.Type;
public class FormatterCoreImplTest {

	@Test
	public void testBlockPropertiesHierarchy() {
		//Setup
		FormatterCoreImpl formatter = new FormatterCoreImpl(null);
		formatter.startBlock(new BlockProperties.Builder().rowSpacing(1.0f).firstLineIndent(1).orphans(2).widows(2).build());
		formatter.startBlock(new BlockProperties.Builder().rowSpacing(2.0f).firstLineIndent(2).orphans(3).widows(3).build());
		formatter.endBlock();
		formatter.endBlock();
		
		Margin left = (Margin)new Margin(Type.LEFT);
		Margin right = (Margin)new Margin(Type.RIGHT);
		left.add(new MarginComponent("", 0, 0));
		right.add(new MarginComponent("", 0, 0));
		Margin leftInner = (Margin)left.clone();
		leftInner.add(new MarginComponent("", 0, 0));
		Margin rightInner = (Margin)right.clone();
		rightInner.add(new MarginComponent("", 0, 0));
		
		RowDataProperties expectedOuter = new RowDataProperties.Builder().rowSpacing(1.0f).firstLineIndent(1).orphans(2).widows(2).leftMargin(left).rightMargin(right).build();
		RowDataProperties expectedInner = new RowDataProperties.Builder().rowSpacing(2.0f).firstLineIndent(2).orphans(3).widows(3).leftMargin(leftInner).rightMargin(rightInner).build();
		
		//Test
		assertEquals(3, formatter.size());
		assertEquals(expectedOuter, formatter.get(0).getRowDataProperties());
		assertEquals(expectedInner, formatter.get(1).getRowDataProperties());
		assertEquals(expectedOuter, formatter.get(2).getRowDataProperties());
	}
	
	@Test
	public void testVolumeKeepProperties_01() {
		//Setup
		FormatterCoreImpl formatter = new FormatterCoreImpl(null);
		formatter.startBlock(new BlockProperties.Builder().volumeKeepPriority(1).build());
		formatter.startBlock(new BlockProperties.Builder().volumeKeepPriority(2).build());
		formatter.startBlock(new BlockProperties.Builder().volumeKeepPriority(3).build());
		formatter.endBlock();
		formatter.endBlock();
		formatter.endBlock();
		
		//Test
		assertEquals(5, formatter.size());
		assertEquals(1, (int)formatter.get(0).getAvoidVolumeBreakInsidePriority());
		assertEquals(1, (int)formatter.get(0).getAvoidVolumeBreakAfterPriority());
		assertEquals(2, (int)formatter.get(1).getAvoidVolumeBreakInsidePriority());
		assertEquals(2, (int)formatter.get(1).getAvoidVolumeBreakAfterPriority());
		assertEquals(3, (int)formatter.get(2).getAvoidVolumeBreakInsidePriority());
		assertEquals(2, (int)formatter.get(2).getAvoidVolumeBreakAfterPriority());
		assertEquals(2, (int)formatter.get(3).getAvoidVolumeBreakInsidePriority());
		assertEquals(1, (int)formatter.get(3).getAvoidVolumeBreakAfterPriority());
		assertEquals(1, (int)formatter.get(4).getAvoidVolumeBreakInsidePriority());
		assertEquals(null, formatter.get(4).getAvoidVolumeBreakAfterPriority());
	}

	@Test
	public void testVolumeKeepProperties_02() {
		//Setup
		FormatterCoreImpl formatter = new FormatterCoreImpl(null);
		formatter.startBlock(new BlockProperties.Builder().volumeKeepPriority(1).build());
		formatter.endBlock();
		formatter.startBlock(new BlockProperties.Builder().volumeKeepPriority(2).build());
		formatter.endBlock();
		formatter.startBlock(new BlockProperties.Builder().volumeKeepPriority(3).build());
		formatter.endBlock();
		
		//Test
		assertEquals(3, formatter.size());
		assertEquals(1, (int)formatter.get(0).getAvoidVolumeBreakInsidePriority());
		assertEquals(null, formatter.get(0).getAvoidVolumeBreakAfterPriority());
		assertEquals(2, (int)formatter.get(1).getAvoidVolumeBreakInsidePriority());
		assertEquals(null, formatter.get(1).getAvoidVolumeBreakAfterPriority());
		assertEquals(3, (int)formatter.get(2).getAvoidVolumeBreakInsidePriority());
		assertEquals(null, formatter.get(2).getAvoidVolumeBreakAfterPriority());

	}
	
	@Test
	public void testVolumeKeepProperties_03() {
		//Setup
		FormatterCoreImpl formatter = new FormatterCoreImpl(null);
		formatter.startBlock(new BlockProperties.Builder().volumeKeepPriority(1).build());
		formatter.startBlock(new BlockProperties.Builder().volumeKeepPriority(2).build());
		formatter.startBlock(new BlockProperties.Builder().build());
		formatter.endBlock();
		formatter.startBlock(new BlockProperties.Builder().build());
		formatter.endBlock();
		formatter.endBlock();
		formatter.endBlock();
		
		//Test
		assertEquals(7, formatter.size());
		assertEquals(1, (int)formatter.get(0).getAvoidVolumeBreakInsidePriority());
		assertEquals(1, (int)formatter.get(0).getAvoidVolumeBreakAfterPriority());
		assertEquals(2, (int)formatter.get(1).getAvoidVolumeBreakInsidePriority());
		assertEquals(2, (int)formatter.get(1).getAvoidVolumeBreakAfterPriority());
		assertEquals(2, (int)formatter.get(2).getAvoidVolumeBreakInsidePriority());
		assertEquals(2, (int)formatter.get(2).getAvoidVolumeBreakAfterPriority());
		assertEquals(2, (int)formatter.get(3).getAvoidVolumeBreakInsidePriority());
		assertEquals(2, (int)formatter.get(3).getAvoidVolumeBreakAfterPriority());
		assertEquals(2, (int)formatter.get(4).getAvoidVolumeBreakInsidePriority());
		assertEquals(2, (int)formatter.get(4).getAvoidVolumeBreakAfterPriority());
		assertEquals(2, (int)formatter.get(5).getAvoidVolumeBreakInsidePriority());
		assertEquals(1, (int)formatter.get(5).getAvoidVolumeBreakAfterPriority());
		assertEquals(1, (int)formatter.get(6).getAvoidVolumeBreakInsidePriority());
		assertEquals(null, formatter.get(6).getAvoidVolumeBreakAfterPriority());
	}
}
