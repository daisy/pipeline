package org.daisy.dotify.formatter.impl.sheet;

import org.daisy.dotify.formatter.impl.common.Section;
import org.daisy.dotify.formatter.impl.common.Volume;
import org.daisy.dotify.formatter.impl.search.Overhead;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides a container for a physical volume of braille.
 *
 * @author Joel Håkansson
 */
public class VolumeImpl implements Volume {
    private List<Section> bodyVolData;
    private List<Section> preVolData;
    private List<Section> postVolData;
    /**
     * The number of sheets allocated for pre- and post-content, which may be more than the actual
     * number. This value is never decreased in order to avoid oscillation.
     */
    private Overhead overhead;
    /**
     * The actual number of pre-content sheets.
     */
    private int preVolSize;
    /**
     * The actual number of post-content sheets.
     */
    private int postVolSize;
    private int bodyVolSize;

    public VolumeImpl(Overhead overhead) {
        this.overhead = overhead;
        postVolSize = 0;
        bodyVolSize = 0;
    }

    public void setBody(SectionBuilder body) {
        bodyVolData = body.getSections();
        bodyVolSize = body.getSheetCount();
    }

    public void setPreVolData(SectionBuilder preVolData) {
        this.preVolData = preVolData.getSections();
        preVolSize = preVolData.getSheetCount();
        // use the highest value to avoid oscillation
        overhead = overhead.withPreContentSize(Math.max(overhead.getPreContentSize(), preVolSize));
    }

    public void setPostVolData(SectionBuilder postVolData) {
        this.postVolData = postVolData.getSections();
        postVolSize = postVolData.getSheetCount();
        // use the highest value to avoid oscillation
        overhead = overhead.withPostContentSize(Math.max(overhead.getPostContentSize(), postVolSize));
    }

    public Overhead getOverhead() {
        return overhead;
    }

    public int getBodySize() {
        return bodyVolSize;
    }

    /**
     * @return The actual size of the whole volume in sheets.
     */
    public int getVolumeSize() {
        return preVolSize + bodyVolSize + postVolSize;
    }

    @Override
    public Iterable<? extends Section> getSections() {
        List<Section> contents = new ArrayList<>();
        contents.addAll(preVolData);
        contents.addAll(bodyVolData);
        contents.addAll(postVolData);
        return contents;
    }

}
