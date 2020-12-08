package org.daisy.dotify.formatter.impl.core;

import org.daisy.dotify.api.writer.Row;
import org.daisy.dotify.formatter.impl.row.RowImpl;

import java.util.Collection;

/**
 * TODO: Write java doc.
 */
public class HeightCalculator {
    private final float defSpacing;
    private float ret;

    public HeightCalculator(float defSpacing) {
        this.defSpacing = defSpacing < 1 ? 1 : defSpacing;
        this.ret = 0;
    }

    HeightCalculator(HeightCalculator template) {
        this.defSpacing = template.defSpacing;
        this.ret = template.ret;
    }

    float getRowSpacing(Row r) {
        if (r.getRowSpacing() != null && r.getRowSpacing() >= 1) {
            return r.getRowSpacing();
        } else {
            return defSpacing;
        }
    }

    void addRow(RowImpl r) {
        if (r.isInvisible()) {
            return;
        }
        ret += getRowSpacing(r);
    }

    public void addRows(Collection<? extends Row> rows) {
        ret += rows.stream()
                .filter(row -> !(row instanceof RowImpl && ((RowImpl) row).isInvisible()))
                .mapToDouble(this::getRowSpacing).sum();
    }

    public float getCurrentHeight() {
        return ret;
    }
}
