package org.daisy.dotify.formatter.impl.core;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.daisy.dotify.api.formatter.BlockProperties;
import org.daisy.dotify.api.formatter.FormatterConfiguration;
import org.daisy.dotify.api.formatter.TextProperties;
import org.daisy.dotify.formatter.impl.common.FormatterCoreContext;
import org.daisy.dotify.formatter.impl.row.BlockMargin;
import org.daisy.dotify.formatter.impl.row.Margin;
import org.daisy.dotify.formatter.impl.row.Margin.Type;
import org.daisy.dotify.formatter.impl.row.MarginComponent;
import org.daisy.dotify.formatter.impl.row.RowDataProperties;
import org.junit.Test;
import org.mockito.Mockito;
@SuppressWarnings("javadoc")
public class FormatterCoreImplTest {
	private final TextProperties UND_TEXT_PROPERTIES = new TextProperties.Builder("und").build();
	private final FormatterCoreContext context;
	
	public FormatterCoreImplTest() {
		context = Mockito.mock(FormatterCoreContext.class);
		Mockito.when(context.getSpaceCharacter()).thenReturn(' ');
		FormatterConfiguration conf = Mockito.mock(FormatterConfiguration.class);
		Mockito.when(conf.isMarkingCapitalLetters()).thenReturn(true);
		Mockito.when(context.getConfiguration()).thenReturn(conf);
	}

	@Test
	public void testBlockPropertiesHierarchy() {
		//Setup
		FormatterCoreContext context = Mockito.mock(FormatterCoreContext.class);
		Mockito.when(context.getSpaceCharacter()).thenReturn(' ');
		FormatterCoreImpl formatter = new FormatterCoreImpl(context);
		formatter.startBlock(new BlockProperties.Builder().rowSpacing(1.0f).firstLineIndent(1).orphans(2).widows(2).build());
		formatter.startBlock(new BlockProperties.Builder().rowSpacing(2.0f).firstLineIndent(2).orphans(3).widows(3).build());
		formatter.endBlock();
		formatter.endBlock();
		List<MarginComponent> leftComps = new ArrayList<>();
		List<MarginComponent> rightComps = new ArrayList<>();
		leftComps.add(new MarginComponent("", 0, 0));
		rightComps.add(new MarginComponent("", 0, 0));
		Margin left = new Margin(Type.LEFT, leftComps);
		Margin right = new Margin(Type.RIGHT, rightComps);

		leftComps.add(new MarginComponent("", 0, 0));
		rightComps.add(new MarginComponent("", 0, 0));
		Margin leftInner = new Margin(Type.LEFT, leftComps);
		Margin rightInner = new Margin(Type.RIGHT, rightComps);

		RowDataProperties expectedOuter = new RowDataProperties.Builder().rowSpacing(1.0f).firstLineIndent(1).orphans(2).widows(2)
				.margins(new BlockMargin(left, right, ' '))
				.build();
		RowDataProperties expectedInner = new RowDataProperties.Builder().rowSpacing(2.0f).firstLineIndent(2).orphans(3).widows(3)
				.margins(new BlockMargin(leftInner, rightInner, ' '))
				.build();
		
		//Test
		assertEquals(3, formatter.size());
		assertEquals(expectedOuter, formatter.get(0).getRowDataProperties());
		assertEquals(expectedInner, formatter.get(1).getRowDataProperties());
		assertEquals(expectedOuter, formatter.get(2).getRowDataProperties());
	}
	
	@Test
	public void testVolumeKeepProperties_01() {
		//Setup
		FormatterCoreImpl formatter = new FormatterCoreImpl(context);
		formatter.startBlock(new BlockProperties.Builder().volumeKeepPriority(1).build());
		formatter.startBlock(new BlockProperties.Builder().volumeKeepPriority(2).build());
		formatter.startBlock(new BlockProperties.Builder().volumeKeepPriority(3).build());
		formatter.addChars("  ", UND_TEXT_PROPERTIES); // adds a segment to this block to remain in sync with previous test result
		formatter.endBlock();
		formatter.addChars("  ", UND_TEXT_PROPERTIES); // adds a segment to this block to remain in sync with previous test result
		formatter.endBlock();
		formatter.addChars("  ", UND_TEXT_PROPERTIES); // adds a segment to this block to remain in sync with previous test result
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
		FormatterCoreImpl formatter = new FormatterCoreImpl(context);
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
		FormatterCoreImpl formatter = new FormatterCoreImpl(context);
		formatter.startBlock(new BlockProperties.Builder().volumeKeepPriority(1).build());
		formatter.startBlock(new BlockProperties.Builder().volumeKeepPriority(2).build());
		formatter.startBlock(new BlockProperties.Builder().build());
		formatter.endBlock();
		formatter.startBlock(new BlockProperties.Builder().build());
		formatter.endBlock();
		formatter.addChars("  ", UND_TEXT_PROPERTIES); // adds a segment to this block to remain in sync with previous test result
		formatter.endBlock();
		formatter.addChars("  ", UND_TEXT_PROPERTIES); // adds a segment to this block to remain in sync with previous test result
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
	
	@Test
	public void testVolumeKeepProperties_04() {
		//Setup
		FormatterCoreImpl formatter = new FormatterCoreImpl(context);
		formatter.startBlock(new BlockProperties.Builder().volumeKeepPriority(1).build());
		formatter.startBlock(new BlockProperties.Builder().volumeKeepPriority(2).build());
		formatter.startBlock(new BlockProperties.Builder().build());
		formatter.endBlock();
		formatter.startBlock(new BlockProperties.Builder().build());
		formatter.endBlock(); // this empty block will affect the volume keep priority
		formatter.endBlock(); // this empty block will affect the volume keep priority
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
		// this block gets its value from the following blocks break after priority, since it is empty 
		assertEquals(1, (int)formatter.get(4).getAvoidVolumeBreakAfterPriority()); 
		assertEquals(2, (int)formatter.get(5).getAvoidVolumeBreakInsidePriority());
		// this block gets its value from the following blocks break after priority, since it is empty
		assertEquals(null, formatter.get(5).getAvoidVolumeBreakAfterPriority());
		assertEquals(1, (int)formatter.get(6).getAvoidVolumeBreakInsidePriority());
		assertEquals(null, formatter.get(6).getAvoidVolumeBreakAfterPriority());
	}
}
