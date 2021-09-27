package org.daisy.dotify.formatter.impl.core;

import org.daisy.dotify.api.formatter.BlockProperties;
import org.daisy.dotify.api.formatter.DynamicContent;
import org.daisy.dotify.api.formatter.Leader;
import org.daisy.dotify.api.formatter.Marker;
import org.daisy.dotify.api.formatter.MarkerReference;
import org.daisy.dotify.api.formatter.NumeralStyle;
import org.daisy.dotify.api.formatter.SpanProperties;
import org.daisy.dotify.api.formatter.TableOfContents;
import org.daisy.dotify.api.formatter.TextProperties;
import org.daisy.dotify.api.formatter.TocEntryOnResumedRange;
import org.daisy.dotify.formatter.impl.common.FormatterCoreContext;
import org.daisy.dotify.formatter.impl.row.RowDataProperties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Provides table of contents entries to be used when building a Table of Contents.
 *
 * @author Joel Håkansson
 */
public class TableOfContentsImpl extends FormatterCoreImpl implements TableOfContents {
    /**
     *
     */
    private static final long serialVersionUID = -2198713822437968076L;
    /* remember all the ref-id attributes in order to verify that they are unique */
    private final Set<String> refIds;
    /* every toc-entry maps exactly to one block in the resulting sequence of blocks */
    private final Map<Block, String> refIdForBlock;
    /* every toc-entry-on-resumed maps exactly to one block in the resulting sequence of blocks */
    private final Map<Block, TocEntryOnResumedRange> rangeForBlock;
    /* mapping from block in the resulting sequence of blocks to the toc-block element that it came from */
    private final Map<Block, TocBlock> tocBlockForBlock;
    /* parent-child relationships of toc-block elements */
    private final Map<TocBlock, TocBlock> parentTocBlockForTocBlock;
    /* current stack of ancestor toc-block elements */
    private final Stack<TocBlock> currentAncestorTocBlocks;
    /* whether we are currently inside an entry */
    private boolean inEntry = false;

    public TableOfContentsImpl(FormatterCoreContext fc) {
        super(fc);
        this.refIds = new HashSet<>();
        this.refIdForBlock = new IdentityHashMap<>();
        this.rangeForBlock = new IdentityHashMap<>();
        this.tocBlockForBlock = new IdentityHashMap<>();
        this.parentTocBlockForTocBlock = new LinkedHashMap<>();
        this.currentAncestorTocBlocks = new Stack<>();
    }

    @Override
    public void startBlock(BlockProperties p, String blockId) {
        TocBlock tocBlock = new TocBlock();
        if (!currentAncestorTocBlocks.isEmpty()) {
            parentTocBlockForTocBlock.put(tocBlock, currentAncestorTocBlocks.peek());
        }
        currentAncestorTocBlocks.push(tocBlock);

        super.startBlock(p, blockId);
    }

    @Override
    public void endBlock() {
        super.endBlock();
        currentAncestorTocBlocks.pop();
    }

    @Override
    public Block newBlock(String blockId, RowDataProperties rdp) {
        Block b = super.newBlock(blockId, rdp);
        if (!currentAncestorTocBlocks.isEmpty()) {
            tocBlockForBlock.put(b, currentAncestorTocBlocks.peek());
        }
        return b;
    }

    @Override
    public void startEntry(String refId) {
        if (inEntry) {
            throw new RuntimeException("Entries may not be nested");
        }
        inEntry = true;
        if (!refIds.add(refId)) {
            throw new RuntimeException("ref-id is not unique: " + refId);
        }
        Block currentBlock = getCurrentBlock();
        if (refIdForBlock.put(currentBlock, refId) != null || rangeForBlock.containsKey(currentBlock)) {
            // note that this is not strictly forbidden by OBFL, but it simplifies the implementation
            throw new RuntimeException("No two entries may be contained in the same block");
        }
    }

    @Override
    public void startEntryOnResumed(TocEntryOnResumedRange range) {
        if (inEntry) {
            throw new RuntimeException("Entries may not be nested");
        }
        inEntry = true;
        Block currentBlock = getCurrentBlock();
        if (rangeForBlock.put(currentBlock, range) != null || refIdForBlock.containsKey(currentBlock)) {
            // note that this is not strictly forbidden by OBFL, but it simplifies the implementation
            throw new RuntimeException("No two entries may be contained in the same block");
        }
    }

    @Override
    public void endEntry() {
        if (!inEntry) {
            throw new RuntimeException("Unexpected end of entry");
        }
        inEntry = false;
    }

    /**
     * <p>Filter out the toc-entry with an identification that does not satisfy the corresponding
     * predicate. This is used to create the volume range toc. toc-block that have all their
     * descendant toc-entry filtered out are also omitted.</p>
     *
     * <p>Note that, because this is implemented by filtering a fixed sequence of blocks, and because
     * of the way the sequence of blocks is constructed, we are potentially throwing away borders
     * and margins that should be kept. That said, the previous implementation did not handle
     * borders and margins correctly either, so fixing this issue can be seen as an optimization.</p>
     * 
     * <p>This method sets the meta page number of the range blocks that are filtered out to
     * the provided value.</p> 
     *
     * @param refIdFilter predicate that takes as argument a ref-id
     * @param rangeFilter predicate that takes as argument a range
     * @param rangeMetaPage meta page number of range blocks. Provided as a lazy value ({@link
     *     Supplier} object) so that it is not computed when not needed (when there are no matching
     *     toc-entry-on-resumed).
     * @param blockCloner block cloner. Range blocks in a document TOC must be cloned as
     *     they can be placed in multiple contexts.
     * @return collection of blocks
     */
    public Collection<Block> filter(
            Predicate<String> refIdFilter,
            Predicate<TocEntryOnResumedRange> rangeFilter,
            Supplier<Integer> rangeMetaPage,
            BlockCloner blockCloner
    ) {
        List<Block> filtered = new ArrayList<>();
        Set<TocBlock> tocBlocksWithDescendantTocEntry = new HashSet<>();
        for (Block b : this) {
            if (refIdForBlock.containsKey(b) || rangeForBlock.containsKey(b)) {
                if (refIdForBlock.containsKey(b) && !refIdFilter.test(refIdForBlock.get(b))) {
                    continue;
                }
                if (rangeForBlock.containsKey(b) && !rangeFilter.test(rangeForBlock.get(b))) {
                    continue;
                }
                if (tocBlockForBlock.containsKey(b)) {
                    TocBlock tocBlock = tocBlockForBlock.get(b);
                    tocBlocksWithDescendantTocEntry.add(tocBlock);
                    while (parentTocBlockForTocBlock.containsKey(tocBlock)) {
                        tocBlock = parentTocBlockForTocBlock.get(tocBlock);
                        tocBlocksWithDescendantTocEntry.add(tocBlock);
                    }
                }
            }
            filtered.add(b);
        }
        Iterator<Block> it = filtered.iterator();
        while (it.hasNext()) {
            Block b = it.next();
            if (refIdForBlock.containsKey(b)) {
                continue;
            }
            if (rangeForBlock.containsKey(b)) {
                b.setMetaPage(rangeMetaPage.get());
                continue;
            }
            if (tocBlockForBlock.containsKey(b) // this should always be true
                    && tocBlocksWithDescendantTocEntry.contains(tocBlockForBlock.get(b))) {
                continue;
            }
            it.remove();
        }
        if (blockCloner != null) {
            for (int i = 0; i < filtered.size(); ++i) {
                Block b = filtered.get(i);
                if (rangeForBlock.containsKey(b)) {
                    // clone these blocks, as they may be placed in multiple contexts
                    filtered.set(i, blockCloner.clone(b));
                }
            }
        }
        return filtered;
    }

    private void assertInEntry() {
        if (!inEntry) {
            throw new RuntimeException("Inline content is only allowed within an entry");
        }
    }

    @Override
    public void insertMarker(Marker marker) {
        assertInEntry();
        super.insertMarker(marker);
    }

    @Override
    public void insertAnchor(String ref) {
        assertInEntry();
        super.insertAnchor(ref);
    }

    @Override
    public void insertLeader(Leader leader, TextProperties props) {
        assertInEntry();
        super.insertLeader(leader, props);
    }

    @Override
    public void addChars(CharSequence chars, TextProperties props) {
        assertInEntry();
        super.addChars(chars, props);
    }

    @Override
    public void startStyle(String style) {
        assertInEntry();
        super.startStyle(style);
    }

    @Override
    public void endStyle() {
        assertInEntry();
        super.endStyle();
    }

    @Override
    public void startSpan(SpanProperties props) {
        assertInEntry();
        super.startSpan(props);
    }

    @Override
    public void endSpan() {
        assertInEntry();
        super.endSpan();
    }

    @Override
    public void newLine() {
        assertInEntry();
        super.newLine();
    }

    @Override
    public void insertPageReference(String identifier, NumeralStyle numeralStyle) {
        assertInEntry();
        super.insertPageReference(identifier, numeralStyle);
    }

    @Override
    public void insertExternalReference(Object reference) {
        assertInEntry();
        super.insertExternalReference(reference);
    }

    @Override
    public void insertMarkerReference(Iterable<? extends MarkerReference> ref, TextProperties t) {
        assertInEntry();
        super.insertMarkerReference(ref, t);
    }

    @Override
    public void insertEvaluate(DynamicContent exp, TextProperties t) {
        assertInEntry();
        super.insertEvaluate(exp, t);
    }

    private class TocBlock {
        // empty class
    }
}
