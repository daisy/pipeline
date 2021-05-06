package org.daisy.dotify.api.formatter;

/**
 * Provides methods for building a volume template.
 *
 * @author Joel Håkansson
 */
public interface VolumeTemplateBuilder {

    /**
     * Gets the pre volume content builder.
     *
     * @return returns the pre volume content builder
     */
    public VolumeContentBuilder getPreVolumeContentBuilder();

    /**
     * Gets the post volume content builder.
     *
     * @return returns the post volume content builder
     */
    public VolumeContentBuilder getPostVolumeContentBuilder();

}
