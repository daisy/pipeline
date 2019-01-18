package com.xmlcalabash.util;

import net.sf.saxon.expr.parser.Location;

/**
 * Implementation of {@link Location} with System ID and line number.
 */
public class LineNumberLocation
        implements Location
{
    public LineNumberLocation(String sysid, int line) {
        this.sysid = sysid;
        this.line = line;
    }

    @Override
    public int getColumnNumber() {
        return -1;
    }

    @Override
    public int getLineNumber() {
        return line;
    }

    @Override
    public String getPublicId() {
        return null;
    }

    @Override
    public String getSystemId() {
        return sysid;
    }

    @Override
    public Location saveLocation() {
        return this;
    }

    private final String sysid;
    private final int line;
}
