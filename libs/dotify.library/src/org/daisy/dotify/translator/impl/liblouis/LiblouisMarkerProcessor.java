package org.daisy.dotify.translator.impl.liblouis;

import org.daisy.dotify.translator.DefaultMarkerProcessor;
import org.daisy.dotify.translator.Marker;
import org.daisy.dotify.translator.MarkerStyleConstants;
import org.daisy.dotify.translator.SimpleMarkerDictionary;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>Provides marker processor definitions for the {@link LiblouisBrailleFilter}.
 * Some attributes can be supported through Liblouis "type form" feature
 * (these should be processed by Liblouis) and some attributes
 * are unique to the Dotify implementation (these should be processed
 * by the {@link LiblouisBrailleFilter}). Each attribute should
 * only be processed once. Therefore, specifications for both these systems
 * are provided by this implementation.</p>
 *
 * <p>Note that this is just a placeholder implementation.
 * It was simply copied from the Swedish marker processor. Needless to say will not
 * meet the requirement of any language supported by {@link LiblouisBrailleFilter}.</p>
 *
 * @author Joel Håkansson
 */
class LiblouisMarkerProcessor {
    private final DefaultMarkerProcessor sap;
    private final Map<String, Integer> typeform;

    private LiblouisMarkerProcessor(DefaultMarkerProcessor sap, Map<String, Integer> typeform) {
        this.sap = sap;
        this.typeform = typeform;
    }

    static LiblouisMarkerProcessor newInstance() {

        SimpleMarkerDictionary dd = new SimpleMarkerDictionary(new Marker("\u2820\u2804\u2800", ""));

        SimpleMarkerDictionary continuedTableCell = new SimpleMarkerDictionary(
            new Marker("\u283b\u283b", "")
        );

        DefaultMarkerProcessor sap = new DefaultMarkerProcessor.Builder().
            // em and strong are processed by the type form system.
            // They are defined here only to suppress warnings from the DefaultMarkerProcessor
            addDictionary(MarkerStyleConstants.STRONG, new SimpleMarkerDictionary(new Marker("", ""))).
            addDictionary(MarkerStyleConstants.EM, new SimpleMarkerDictionary(new Marker("", ""))).
            addDictionary(MarkerStyleConstants.SUB, new SimpleMarkerDictionary(new Marker("", ""))).
            addDictionary(MarkerStyleConstants.SUP, new SimpleMarkerDictionary(new Marker("", ""))).
            addDictionary(MarkerStyleConstants.DD, dd).
            addDictionary("table-cell-continued", continuedTableCell).
            build();

        return new LiblouisMarkerProcessor(sap, makeMarkersMap());
    }

    private static Map<String, Integer> makeMarkersMap() {
        Map<String, Integer> ret = new HashMap<>();
        ret.put("em", 1);
        ret.put("strong", 4);
        return Collections.unmodifiableMap(ret);
    }

    Map<String, Integer> getTypeForm() {
        return typeform;
    }

    DefaultMarkerProcessor getMarkerProcessor() {
        return sap;
    }
}
