package org.daisy.dotify.api.embosser;

import java.io.File;
import javax.print.PrintException;

/**
 * Provides an interface to transmit a file to a device, typically a printer.
 *
 * @author Joel Håkansson
 */
public interface Device {

    /**
     * Transmits a file to the Device.
     *
     * @param file the file to transmit
     * @throws PrintException if the file could not be transmitted
     */
    public void transmit(File file) throws PrintException;

}
