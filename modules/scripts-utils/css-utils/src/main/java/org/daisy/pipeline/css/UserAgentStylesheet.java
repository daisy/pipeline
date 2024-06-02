package org.daisy.pipeline.css;

import java.net.URL;
import java.util.Collection;

/**
 * Style sheet that should be applied by default for a given content
 * media type and output medium.
 */
public interface UserAgentStylesheet {

    /**
     * Get the resource
     */
    public URL getURL();

    /**
     * Get the style sheet type, e.g. "text/css" or "text/x-scss"
     */
    public String getType();

    /**
     * Whether this style sheet applies to the given content media type.
     *
     * @param contentType e.g. "application/x-dtbook+xml" or "application/xhtml+xml"
     */
    public boolean matchesContentType(String contentType);

    /**
     * Whether this style sheet matches the given medium
     */
    public boolean matchesMedium(Medium medium);

}
