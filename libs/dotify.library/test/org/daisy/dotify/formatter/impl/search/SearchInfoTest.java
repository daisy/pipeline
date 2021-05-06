package org.daisy.dotify.formatter.impl.search;

import org.daisy.dotify.api.formatter.Marker;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * TODO: Write java doc.
 */
@SuppressWarnings("javadoc")
public class SearchInfoTest {

    @Test
    public void testSearchInfo_01() {
        SearchInfo si = new SearchInfo();
        addPages(
            si,
            6,
            0,
            0,
            0,
            new DocumentSpace(Space.PRE_CONTENT, 0),
            null
        );
        addPages(
            si,
            3,
            0,
            0,
            0,
            DocumentSpace.BODY,
            0
        );
        addPages(
            si,
            3,
            3,
            0,
            0,
            DocumentSpace.BODY,
            0
        );
        addPages(
            si,
            6,
            0,
            0,
            0,
            new DocumentSpace(Space.POST_CONTENT, 0),
            null
        );

        si.setVolumeScope(1, 0, 3, DocumentSpace.BODY);
        si.setVolumeScope(2, 3, 6, DocumentSpace.BODY);
        si.setSequenceScope(new SequenceId(0, DocumentSpace.BODY, 0), 0, 6);
        View<PageDetails> vol1 = si.getContentsInVolume(1, DocumentSpace.BODY);
        assertEquals(3, vol1.size());

        View<PageDetails> vol2 = si.getContentsInVolume(2, DocumentSpace.BODY);
        assertEquals(3, vol2.size());

        View<PageDetails> seq = si.getContentsInSequence(new SequenceId(0, DocumentSpace.BODY, 0));
        assertEquals(6, seq.size());
    }

    private static void addPages(
        SearchInfo si,
        int count,
        int offset,
        int globalStartIndex,
        int sequenceId,
        DocumentSpace space,
        Integer volumeGroup
    ) {
        addPages(si, count, offset, globalStartIndex, sequenceId, space, volumeGroup, Collections.emptyMap());
    }

    private static void addPages(
        SearchInfo si,
        int count,
        int offset,
        int globalStartIndex,
        int sequenceId,
        DocumentSpace space,
        Integer volumeGroup,
        Map<Integer, List<Marker>> marker
    ) {
        for (int i = 0; i < count; i++) {
            PageDetails pd = new PageDetails(
                true,
                new PageId(i + offset, globalStartIndex, new SequenceId(sequenceId, space, volumeGroup)
                ),
                null,
                i + offset
            );
            List<Marker> m = marker.get(i + offset);
            if (m != null) {
                pd.getMarkers().addAll(m);
            }
            si.keepPageDetails(pd);
        }
        si.commitPageDetails();
    }

}
