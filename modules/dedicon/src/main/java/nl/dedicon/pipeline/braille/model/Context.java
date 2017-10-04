package nl.dedicon.pipeline.braille.model;

/**
 *
 * @author Paul Rambags
 */
public enum Context {
    Default,
    Formula,
    Code;
    
    public static Context get(String context) {
        if ("Default".equalsIgnoreCase(context)) return Default;
        if ("Formula".equalsIgnoreCase(context)) return Formula;
        if ("Code".equalsIgnoreCase(context)) return Code;
        return null;
    }
}
