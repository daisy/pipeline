package org.daisy.dotify.formatter.impl.core;

import org.daisy.dotify.api.formatter.RenderingScenario;
import org.daisy.dotify.formatter.impl.row.AbstractBlockContentManager;
import org.daisy.dotify.formatter.impl.row.BlockContentManager;
import org.daisy.dotify.formatter.impl.row.RowDataProperties;
import org.daisy.dotify.formatter.impl.search.DefaultContext;
import org.daisy.dotify.formatter.impl.segment.Segment;
import org.daisy.dotify.formatter.impl.segment.Style;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Stack;

class RegularBlock extends Block {
    private final Stack<Segment> segments;
    private final Deque<Style> styles;

    RegularBlock(String blockId, RowDataProperties rdp, RenderingScenario scenario) {
        super(blockId, rdp, scenario);
        this.segments = new Stack<>();
        this.styles = new ArrayDeque<>();
    }

    RegularBlock(RegularBlock template) {
        super(template);
        this.segments = template.segments;
        this.styles = new ArrayDeque<>(template.styles);
    }

    public RegularBlock copy() {
        return new RegularBlock(this);
    }

    @Override
    public void addSegment(Segment s) {
        super.addSegment(s);
        if (styles.isEmpty()) {
            segments.add(s);
        } else {
            styles.peek().add(s);
        }
    }

    @Override
    void startStyle(String style) {
        Style g = new Style(style);
        if (styles.isEmpty()) {
            segments.add(g);
        } else {
            styles.peek().add(g);
        }
        styles.push(g);
    }

    @Override
    void endStyle() {
        styles.pop();
    }

    @Override
    boolean isEmpty() {
        return segments.isEmpty();
    }

    @Override
    protected AbstractBlockContentManager newBlockContentManager(BlockContext context) {
        return new BlockContentManager(getIdentifier(), context.getFlowWidth(), segments, rdp, context.getRefs(),
                DefaultContext
                        .from(context)
                        .metaVolume(metaVolume)
                        .metaPage(metaPage)
                        .build(),
                context.getFcontext());
    }

}
