package org.daisy.dotify.translator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * TODO: write java doc.
 */
public class RegexMarkerDictionaryTest {

    @Test
    public void testDefaultFilter() throws MarkerNotFoundException, MarkerNotCompatibleException {
        Marker m = new Marker("1", "2");
        RegexMarkerDictionary d = new RegexMarkerDictionary.Builder().addPattern(".*", m).build();
        Marker ret = d.getMarkersFor("test", null);
        assertEquals(m, ret);
    }

}
