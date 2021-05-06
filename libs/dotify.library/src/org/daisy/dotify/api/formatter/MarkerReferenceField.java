package org.daisy.dotify.api.formatter;

/**
 * A MarkerReferenceField is a place holder for a marker's value.
 * Its value is resolved by the LayoutPerformer when its location
 * in the flow is known.
 *
 * @author Joel Håkansson
 */
public class MarkerReferenceField extends MarkerReference implements Field {

    private final String textStyle;

    /**
     * Creates a new instance with the specified parameters.
     *
     * @param markerName the name of the marker
     * @param dir        the direction
     * @param scope      the scope
     */
    public MarkerReferenceField(String markerName, MarkerSearchDirection dir, MarkerSearchScope scope) {
        this(markerName, dir, scope, null);
    }

    /**
     * Creates a new instance with the specified parameters.
     *
     * @param markerName the name of the marker
     * @param dir        the direction
     * @param scope      the scope
     * @param textStyle  a text style name
     */
    public MarkerReferenceField(
        String markerName,
        MarkerSearchDirection dir,
        MarkerSearchScope scope,
        String textStyle
    ) {
        this(markerName, dir, scope, textStyle, 0);
    }

    /**
     * Creates a new instance with the specified parameters.
     *
     * @param markerName the name of the marker
     * @param dir        the direction
     * @param scope      the scope
     * @param textStyle  a text style name
     * @param offset     offsets the search by the specified amount, in pages
     */
    public MarkerReferenceField(
        String markerName,
        MarkerSearchDirection dir,
        MarkerSearchScope scope,
        String textStyle,
        int offset
    ) {
        super(markerName, dir, scope, offset);
        this.textStyle = textStyle;
    }

    @Override
    public String getTextStyle() {
        return textStyle;
    }
}
