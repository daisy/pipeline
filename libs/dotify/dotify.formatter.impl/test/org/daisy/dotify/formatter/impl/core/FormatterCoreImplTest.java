package org.daisy.dotify.formatter.impl.core;

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

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * TODO: Write java doc.
 */
@SuppressWarnings("javadoc")
public class FormatterCoreImplTest {
    private static final TextProperties UND_TEXT_PROPERTIES = new TextProperties.Builder("und").build();
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
        formatter.startBlock(
            new BlockProperties.Builder().rowSpacing(1.0f).firstLineIndent(1).orphans(2).widows(2).build()
        );
        formatter.startBlock(
            new BlockProperties.Builder().rowSpacing(2.0f).firstLineIndent(2).orphans(3).widows(3).build()
        );
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

        RowDataProperties expectedOuter =
                new RowDataProperties.Builder().rowSpacing(1.0f).firstLineIndent(1).orphans(2).widows(2)
                .margins(new BlockMargin(left, right, ' '))
                .build();
        RowDataProperties expectedInner =
                new RowDataProperties.Builder().rowSpacing(2.0f).firstLineIndent(2).orphans(3).widows(3)
                .margins(new BlockMargin(leftInner, rightInner, ' '))
                .build();

        //Test
        assertEquals(3, formatter.size());
        assertEquals(expectedOuter, formatter.get(0).getRowDataProperties());
        assertEquals(expectedInner, formatter.get(1).getRowDataProperties());
        assertEquals(expectedOuter, formatter.get(2).getRowDataProperties());
    }

    // The following tests are to verify that:
    //
    // 1) Blocks inherit their volume-break-inside value from their parent
    //
    // 2) Blocks get their volume-break-after value from the following block with the
    //    volume-break-inside value and that does not come after the first following non-empty
    //    block. If there is no following non-empty block, the block's volume-break-after value will
    //    be null.
    //
    // Note that if an volume-break-inside is absent (null) it is as if the block has a high value.

    @Test
    public void testVolumeKeepProperties_01() {
        //Setup
        FormatterCoreImpl formatter = new FormatterCoreImpl(context);
        formatter.startBlock(new BlockProperties.Builder().volumeKeepPriority(1).build()); // start block 1
        formatter.startBlock(new BlockProperties.Builder().volumeKeepPriority(2).build()); // start block 2
        formatter.startBlock(new BlockProperties.Builder().volumeKeepPriority(3).build()); // start block 3
        formatter.addChars("  ", UND_TEXT_PROPERTIES);
        formatter.endBlock(); // end block 3
        formatter.addChars("  ", UND_TEXT_PROPERTIES);
        formatter.endBlock(); // end block 2
        formatter.addChars("  ", UND_TEXT_PROPERTIES);
        formatter.endBlock(); // end block 1

        //Test
        assertEquals(5, formatter.size());
        // in block 1 before block 2: empty
        assertEquals(1, (int) formatter.get(0).getAvoidVolumeBreakInsidePriority());
        assertEquals(3, (int) formatter.get(0).getAvoidVolumeBreakAfterPriority());
        // in block 2 before block 3: empty
        assertEquals(2, (int) formatter.get(1).getAvoidVolumeBreakInsidePriority());
        assertEquals(3, (int) formatter.get(1).getAvoidVolumeBreakAfterPriority());
        // in block 3
        assertEquals(3, (int) formatter.get(2).getAvoidVolumeBreakInsidePriority());
        assertEquals(2, (int) formatter.get(2).getAvoidVolumeBreakAfterPriority());
        // in block 2 after block 3
        assertEquals(2, (int) formatter.get(3).getAvoidVolumeBreakInsidePriority());
        assertEquals(1, (int) formatter.get(3).getAvoidVolumeBreakAfterPriority());
        // in block 1 after block 2
        assertEquals(1, (int) formatter.get(4).getAvoidVolumeBreakInsidePriority());
        assertEquals(null, formatter.get(4).getAvoidVolumeBreakAfterPriority());
    }

    @Test
    public void testVolumeKeepProperties_02() {
        //Setup
        FormatterCoreImpl formatter = new FormatterCoreImpl(context);
        formatter.startBlock(new BlockProperties.Builder().volumeKeepPriority(1).build()); // start block 1
        formatter.endBlock(); // end block 1
        formatter.startBlock(new BlockProperties.Builder().volumeKeepPriority(2).build()); // start block 2
        formatter.endBlock(); // end block 2
        formatter.startBlock(new BlockProperties.Builder().volumeKeepPriority(3).build()); // start block 3
        formatter.endBlock(); // end block 3

        //Test
        assertEquals(3, formatter.size());
        // in block 1: empty
        assertEquals(1, (int) formatter.get(0).getAvoidVolumeBreakInsidePriority());
        assertEquals(null, formatter.get(0).getAvoidVolumeBreakAfterPriority());
        // in block 2: empty
        assertEquals(2, (int) formatter.get(1).getAvoidVolumeBreakInsidePriority());
        assertEquals(null, formatter.get(1).getAvoidVolumeBreakAfterPriority());
        // in block 3: empty
        assertEquals(3, (int) formatter.get(2).getAvoidVolumeBreakInsidePriority());
        assertEquals(null, formatter.get(2).getAvoidVolumeBreakAfterPriority());

    }

    @Test
    public void testVolumeKeepProperties_03() {
        //Setup
        FormatterCoreImpl formatter = new FormatterCoreImpl(context);
        formatter.startBlock(new BlockProperties.Builder().volumeKeepPriority(1).build()); // start block 1
        formatter.startBlock(new BlockProperties.Builder().volumeKeepPriority(2).build()); // start block 2
        formatter.startBlock(new BlockProperties.Builder().build()); // start block 3
        formatter.endBlock(); // end block 3
        formatter.startBlock(new BlockProperties.Builder().build()); // start block 4
        formatter.endBlock(); // end block 4
        formatter.addChars("  ", UND_TEXT_PROPERTIES);
        formatter.endBlock(); // end block 2
        formatter.addChars("  ", UND_TEXT_PROPERTIES);
        formatter.endBlock(); // end block 1

        //Test
        assertEquals(7, formatter.size());
        // in block 1 before block 2: empty
        assertEquals(1, (int) formatter.get(0).getAvoidVolumeBreakInsidePriority());
        assertEquals(2, (int) formatter.get(0).getAvoidVolumeBreakAfterPriority());
        // in block 2 before block 3: empty
        assertEquals(2, (int) formatter.get(1).getAvoidVolumeBreakInsidePriority());
        assertEquals(2, (int) formatter.get(1).getAvoidVolumeBreakAfterPriority());
        // in block 3: empty
        assertEquals(2, (int) formatter.get(2).getAvoidVolumeBreakInsidePriority());
        assertEquals(2, (int) formatter.get(2).getAvoidVolumeBreakAfterPriority());
        // in block 2 between block 3 and 4: empty
        assertEquals(2, (int) formatter.get(3).getAvoidVolumeBreakInsidePriority());
        assertEquals(2, (int) formatter.get(3).getAvoidVolumeBreakAfterPriority());
        // in block 4: empty
        assertEquals(2, (int) formatter.get(4).getAvoidVolumeBreakInsidePriority());
        assertEquals(2, (int) formatter.get(4).getAvoidVolumeBreakAfterPriority());
        // in block 2 after block 4
        assertEquals(2, (int) formatter.get(5).getAvoidVolumeBreakInsidePriority());
        assertEquals(1, (int) formatter.get(5).getAvoidVolumeBreakAfterPriority());
        // in block 1 after block 2
        assertEquals(1, (int) formatter.get(6).getAvoidVolumeBreakInsidePriority());
        assertEquals(null, formatter.get(6).getAvoidVolumeBreakAfterPriority());
    }

    @Test
    public void testVolumeKeepProperties_04() {
        //Setup
        FormatterCoreImpl formatter = new FormatterCoreImpl(context);
        formatter.startBlock(new BlockProperties.Builder().volumeKeepPriority(1).build()); // start block 1
        formatter.startBlock(new BlockProperties.Builder().volumeKeepPriority(2).build()); // start block 2
        formatter.startBlock(new BlockProperties.Builder().build()); // start block 3
        formatter.endBlock(); // end block 3
        formatter.startBlock(new BlockProperties.Builder().build()); // start block 4
        formatter.endBlock(); // end block 4
        formatter.endBlock(); // end block 2
        formatter.endBlock(); // end block 1

        //Test
        assertEquals(7, formatter.size());
        // in block 1 before block 2: empty
        assertEquals(1, (int) formatter.get(0).getAvoidVolumeBreakInsidePriority());
        assertEquals(null, formatter.get(0).getAvoidVolumeBreakAfterPriority());
        // in block 2 before block 3: empty
        assertEquals(2, (int) formatter.get(1).getAvoidVolumeBreakInsidePriority());
        assertEquals(null, formatter.get(1).getAvoidVolumeBreakAfterPriority());
        // in block 3: empty
        assertEquals(2, (int) formatter.get(2).getAvoidVolumeBreakInsidePriority());
        assertEquals(null, formatter.get(2).getAvoidVolumeBreakAfterPriority());
        // in block 2 between block 3 and 4: empty
        assertEquals(2, (int) formatter.get(3).getAvoidVolumeBreakInsidePriority());
        assertEquals(null, formatter.get(3).getAvoidVolumeBreakAfterPriority());
        // in block 4: empty
        assertEquals(2, (int) formatter.get(4).getAvoidVolumeBreakInsidePriority());
        assertEquals(null, formatter.get(4).getAvoidVolumeBreakAfterPriority());
        // in block 2 after block 4: empty
        assertEquals(2, (int) formatter.get(5).getAvoidVolumeBreakInsidePriority());
        assertEquals(null, formatter.get(5).getAvoidVolumeBreakAfterPriority());
        // in block 1 after block 2: empty
        assertEquals(1, (int) formatter.get(6).getAvoidVolumeBreakInsidePriority());
        assertEquals(null, formatter.get(6).getAvoidVolumeBreakAfterPriority());
    }
}
