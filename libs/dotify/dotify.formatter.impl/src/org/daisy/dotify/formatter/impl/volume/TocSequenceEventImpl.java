package org.daisy.dotify.formatter.impl.volume;

import org.daisy.dotify.api.formatter.Condition;
import org.daisy.dotify.api.formatter.Context;
import org.daisy.dotify.api.formatter.FormatterCore;
import org.daisy.dotify.api.formatter.SequenceProperties;
import org.daisy.dotify.api.formatter.TocEntryOnResumedRange;
import org.daisy.dotify.api.formatter.TocProperties;
import org.daisy.dotify.formatter.impl.common.FormatterCoreContext;
import org.daisy.dotify.formatter.impl.core.Block;
import org.daisy.dotify.formatter.impl.core.FormatterContext;
import org.daisy.dotify.formatter.impl.core.FormatterCoreImpl;
import org.daisy.dotify.formatter.impl.core.TableOfContentsImpl;
import org.daisy.dotify.formatter.impl.page.BlockSequence;
import org.daisy.dotify.formatter.impl.search.BlockAddress;
import org.daisy.dotify.formatter.impl.search.CrossReferenceHandler;
import org.daisy.dotify.formatter.impl.search.DefaultContext;
import org.daisy.dotify.formatter.impl.search.VolumeData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

class TocSequenceEventImpl implements VolumeSequence {
    private final TocProperties props;

    private final ArrayList<ConditionalBlock> tocStartEvents;
    private final ArrayList<ConditionalBlock> volumeStartEvents;
    private final ArrayList<ConditionalBlock> volumeEndEvents;
    private final ArrayList<ConditionalBlock> tocEndEvents;
    private final FormatterCoreContext fc;
    private final long groupNumber;
    private BlockAddress currentBlockAddress;

    TocSequenceEventImpl(FormatterCoreContext fc, TocProperties props) {
        this.fc = fc;
        this.props = props;
        this.tocStartEvents = new ArrayList<>();
        this.volumeStartEvents = new ArrayList<>();
        this.volumeEndEvents = new ArrayList<>();
        this.tocEndEvents = new ArrayList<>();
        this.groupNumber = BlockAddress.getNextGroupNumber();
    }

    FormatterCore addTocStart(Condition condition) {
        // we don't need a layout master here, because it will be replaced before rendering below
        FormatterCoreImpl f = new FormatterCoreImpl(fc);
        tocStartEvents.add(new ConditionalBlock(f, condition));
        return f;
    }

    FormatterCore addVolumeStartEvents(Condition condition) {
        FormatterCoreImpl f = new FormatterCoreImpl(fc);
        volumeStartEvents.add(new ConditionalBlock(f, condition));
        return f;
    }

    FormatterCore addVolumeEndEvents(Condition condition) {
        FormatterCoreImpl f = new FormatterCoreImpl(fc);
        volumeEndEvents.add(new ConditionalBlock(f, condition));
        return f;
    }

    FormatterCore addTocEnd(Condition condition) {
        // we don't need a layout master here, because it will be replaced before rendering below
        FormatterCoreImpl f = new FormatterCoreImpl(fc);
        tocEndEvents.add(new ConditionalBlock(f, condition));
        return f;
    }

    TocProperties.TocRange getRange() {
        return props.getRange();
    }

    private Iterable<Block> getCompoundIterableB(Iterable<ConditionalBlock> events, Context vars) {
        ArrayList<Block> it = new ArrayList<>();
        for (ConditionalBlock ev : events) {
            if (ev.appliesTo(vars)) {
                Iterable<Block> tmp = ev.getSequence();
                for (Block b : tmp) {
                    //always clone these blocks, as they may be placed in multiple contexts
                    Block bl = b.copy();
                    currentBlockAddress = new BlockAddress(
                        groupNumber,
                        currentBlockAddress.getBlockNumber() + 1
                    );
                    bl.setBlockAddress(currentBlockAddress);
                    it.add(bl);
                }
            }
        }
        return it;
    }

    private Iterable<Block> getVolumeStart(Context vars) throws IOException {
        return getCompoundIterableB(volumeStartEvents, vars);
    }

    private Iterable<Block> getVolumeEnd(Context vars) throws IOException {
        return getCompoundIterableB(volumeEndEvents, vars);
    }

    private Iterable<Block> getTocStart(Context vars) throws IOException {
        return getCompoundIterableB(tocStartEvents, vars);
    }

    private Iterable<Block> getTocEnd(Context vars) throws IOException {
        return getCompoundIterableB(tocEndEvents, vars);
    }

    @Override
    public SequenceProperties getSequenceProperties() {
        return props;
    }

    @Override
    public BlockSequence getBlockSequence(FormatterContext context, DefaultContext vars, CrossReferenceHandler crh) {
        TableOfContentsImpl data = context.getTocs().get(props.getTocName());
        currentBlockAddress = new BlockAddress(groupNumber, 0);
        try {
            BlockSequenceManipulator fsm = new BlockSequenceManipulator(
                    context.getMasters().get(getSequenceProperties().getMasterName()),
                    getSequenceProperties());
            fsm.appendGroup(getTocStart(vars));
            
            switch(getRange()) {
                
                case VOLUME: {
                    final int currentVolume = vars.getCurrentVolume();
                    Collection<Block> volumeToc = data.filter(
                            refToVolume(currentVolume, crh), rangeToVolume(currentVolume, crh), 
                            // It is important that this variable is only retrieved when a
                            // toc-entry-on-resumed is actually rendered because a volume could have
                            // no content pages, in which case the variable would have no value,
                            // which would result in the CrossReferenceHandler becoming dirty for no
                            // reason, which could in turn result in endless iterations.
                            () -> crh.getPageNumberOfFirstContentPageOfVolume(currentVolume)
                    );
                    if (volumeToc.isEmpty()) {
                        return null;
                    }
                    fsm.appendGroup(volumeToc);
                    break;
                }

                case DOCUMENT: {
                    for (int vol = 1; vol <= crh.getVolumeCount(); vol++) {
                        final int v = vol;
                        Collection<Block> volumeToc = data.filter(
                                refToVolume(vol, crh), rangeToVolume(vol, crh),
                                () -> crh.getPageNumberOfFirstContentPageOfVolume(v)
                        );
                        if (!volumeToc.isEmpty()) {
                            Context varsWithVolume = DefaultContext
                                    .from(vars)
                                    .metaVolume(vol)
                                    .build();
                            Iterable<Block> volumeStart = getVolumeStart(varsWithVolume);
                            for (Block b : volumeStart) {
                                b.setMetaVolume(vol);
                            }
                            Iterable<Block> volumeEnd = getVolumeEnd(varsWithVolume);
                            for (Block b : volumeEnd) {
                                b.setMetaVolume(vol);
                            }
                            fsm.appendGroup(volumeStart);
                            fsm.appendGroup(volumeToc);
                            fsm.appendGroup(volumeEnd);
                        }
                    }
                    Collection<Block> volumeToc = data.filter(refToVolume(null, crh), range -> false, () -> 0);
                    if (!volumeToc.isEmpty()) {
                        fsm.appendGroup(volumeToc);
                    }
                    break;
                }
                    
                default:
                    throw new RuntimeException("Coding error");
            }
            
            fsm.appendGroup(getTocEnd(vars));
            return fsm.newSequence();
        } catch (IOException e) {
            Logger.getLogger(this.getClass().getCanonicalName()).log(Level.WARNING, "Failed to assemble toc.", e);
        }
        return null;
    }

    private Predicate<String> refToVolume(Integer vol, CrossReferenceHandler crh) {
        return refId -> {
            VolumeData volumeData = crh.getVolumeData(refId);
            if (vol == null || volumeData == null) {
                return vol == null && volumeData == null;
            } else {
                return vol.equals(volumeData.getVolumeNumber());
            }
        };
    }

    /**
     * Determines whether a range is part of a volume.
     *
     * @param vol volume
     * @param crh cross-reference handler
     * @return
     */
    private Predicate<TocEntryOnResumedRange> rangeToVolume(int vol, CrossReferenceHandler crh) {
        return range -> {
            /* startVolumeData refers to the location where the range starts */
            VolumeData startVolumeData = crh.getVolumeData(range.getStartRefId());
            if (startVolumeData == null) {
                return false;
            }
            if (startVolumeData.getVolumeNumber() >= vol) {
                return false;
            }

            Optional<String> endRefId = range.getEndRefId();
            if (!endRefId.isPresent()) {
                return true;
            }

            /* endVolumeData refers to the location where the first block after the range starts */
            VolumeData endVolumeData = crh.getVolumeData(endRefId.get());
            if (endVolumeData == null) {
                return false;
            }
            if (endVolumeData.isAtStartOfVolumeContents()) {
                return vol < endVolumeData.getVolumeNumber();
            } else {
                return vol <= endVolumeData.getVolumeNumber();
            }
        };
    }

}
