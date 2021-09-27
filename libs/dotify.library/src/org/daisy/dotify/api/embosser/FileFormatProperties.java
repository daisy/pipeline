package org.daisy.dotify.api.embosser;

/**
 * @author Bert Frees
 */
public interface FileFormatProperties {

    /**
     * Returns true if 8-dot is supported, false otherwise.
     *
     * @return returns true if 8-dot is supported, false otherwise
     */
    public boolean supports8dot();

    /**
     * Returns true if duplex is supported, false otherwise.
     *
     * @return returns true if duplex is supported, false otherwise
     */
    public boolean supportsDuplex();

    /**
     * Returns true if a single file can contain multiple volumes, false otherwise.
     *
     * @return returns true if a single file can contain multiple volumes, false otherwise
     */
    public boolean supportsVolumes();

    /**
     * Gets the file extension.
     *
     * @return returns the file extension
     */
    public String getFileExtension();

}
