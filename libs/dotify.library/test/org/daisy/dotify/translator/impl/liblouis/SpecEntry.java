package org.daisy.dotify.translator.impl.liblouis;

import org.daisy.dotify.api.translator.TranslatorSpecification;
import org.liblouis.Table;

/**
 * TODO: write java doc.
 */
class SpecEntry {
    private final TranslatorSpecification key;
    private final Table value;

    SpecEntry(TranslatorSpecification key, Table value) {
        this.key = key;
        this.value = value;
    }

    TranslatorSpecification getKey() {
        return key;
    }

    Table getValue() {
        return value;
    }

}
