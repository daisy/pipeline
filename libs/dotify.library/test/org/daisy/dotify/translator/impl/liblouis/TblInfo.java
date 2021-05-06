package org.daisy.dotify.translator.impl.liblouis;

import org.daisy.dotify.api.translator.TranslatorMode;
import org.daisy.dotify.api.translator.TranslatorSpecification;
import org.daisy.dotify.api.translator.TranslatorType;
import org.liblouis.Table;
import org.liblouis.TableInfo;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * TODO: write java doc.
 */
class TblInfo {
    final Table table;
    final String locale;
    final String grade;
    final String type;
    final boolean eightDot;
    final String contraction;
    final TranslatorSpecification a;
    final TranslatorSpecification b;

    TblInfo(Table table) {
        this.table = table;
        TableInfo ti = table.getInfo();
        this.locale = ti.get("locale");
        this.grade = ti.get("grade");
        this.type = ti.get("type");
        this.eightDot = Optional.ofNullable(ti.get("dots")).map(v -> "8".equals(v)).orElse(false);
        this.contraction = ti.get("contraction");
        if ("full".equalsIgnoreCase(contraction)) {
            this.a = new TranslatorSpecification(locale, TranslatorMode.withType(TranslatorType.CONTRACTED));
        } else if ("no".equalsIgnoreCase(contraction)) {
            this.a = new TranslatorSpecification(locale, TranslatorMode.withType(TranslatorType.UNCONTRACTED));
        } else {
            this.a = null;
        }
        TranslatorSpecification t = null;
        if (grade != null) {
            try {
                double g = Double.parseDouble(grade);
                t = new TranslatorSpecification(locale, TranslatorMode.withGrade(g));
            } catch (NumberFormatException e) {
                //Do nothing
            }
        }
        this.b = t;
    }

    Stream<TranslatorSpecification> specStream() {
        if (a == null && b == null) {
            // This shouldn't happen, unless there's a number format exception
            return Stream.<TranslatorSpecification>empty();
        } else if (a != null && b != null) {
            return Stream.of(a, b);
        } else if (a != null) {
            return Stream.of(a);
        } else {
            return Stream.of(b);
        }
    }

    Stream<SpecEntry> entryStream() {
        return specStream().map(v -> new SpecEntry(v, table));
    }
}
