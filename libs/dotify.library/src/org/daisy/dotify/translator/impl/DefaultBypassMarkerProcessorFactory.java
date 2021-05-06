package org.daisy.dotify.translator.impl;

import org.daisy.dotify.api.translator.TranslatorType;
import org.daisy.dotify.translator.DefaultMarkerProcessor;
import org.daisy.dotify.translator.Marker;
import org.daisy.dotify.translator.MarkerStyleConstants;
import org.daisy.dotify.translator.SimpleMarkerDictionary;

class DefaultBypassMarkerProcessorFactory {

    public DefaultMarkerProcessor newMarkerProcessor(
        String locale,
        String mode
    ) throws DefaultBypassMarkerProcessorConfigurationException {
        if (mode.equals(TranslatorType.BYPASS.toString())) {
            SimpleMarkerDictionary dd = new SimpleMarkerDictionary(new Marker("* ", ""));

            DefaultMarkerProcessor sap = new DefaultMarkerProcessor.Builder()
                    .addDictionary(MarkerStyleConstants.EM, new SimpleMarkerDictionary(new Marker("", "")))
                    .addDictionary(MarkerStyleConstants.STRONG, new SimpleMarkerDictionary(new Marker("", "")))
                    .addDictionary(MarkerStyleConstants.SUB, new SimpleMarkerDictionary(new Marker("", "")))
                    .addDictionary(MarkerStyleConstants.SUP, new SimpleMarkerDictionary(new Marker("^", "")))
                    .addDictionary(MarkerStyleConstants.DD, dd)
                    .addDictionary("table-cell-continued", new SimpleMarkerDictionary(new Marker("--", "")))
                    .build();
            return sap;
        }
        throw new DefaultBypassMarkerProcessorConfigurationException("Factory does not support " + locale + "/" + mode);
    }

    class DefaultBypassMarkerProcessorConfigurationException extends Exception {

        /**
         *
         */
        private static final long serialVersionUID = 7831296813639319600L;

        private DefaultBypassMarkerProcessorConfigurationException(String message) {
            super(message);
        }

    }

}
