package nl.dedicon.pipeline.braille.model;

/**
 *
 * @author Paul Rambags
 */
public class Replace {
    
    private Symbol parent;
    private Context context;
    private String braille;
    private String description;

    public Symbol getParent() {
        return parent;
    }

    public void setParent(Symbol parent) {
        this.parent = parent;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public String getBraille() {
        return braille;
    }

    public void setBraille(String braille) {
        this.braille = braille;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
