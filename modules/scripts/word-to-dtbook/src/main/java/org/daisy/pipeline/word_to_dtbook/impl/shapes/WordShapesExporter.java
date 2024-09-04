package org.daisy.pipeline.word_to_dtbook.impl.shapes;
import org.daisy.common.file.URLs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;

public class WordShapesExporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(WordShapesExporter.class);


    synchronized public static void ProcessShapes(String inputPath, String outputPath) throws Exception {
        String exeFileName = "ExportShapesWithWord.exe";
        // Write ExportShapesWithWord in a temp folder
        if (!System.getProperty("os.name").toLowerCase().startsWith("windows"))
            throw new IOException("Not on Windows");
        URL exePath = URLs.getResourceFromJAR("exe/" + exeFileName, WordShapesExporter.class);
        File exeFile; {
            try {
                exeFile = new File(URLs.asURI(exePath));
            } catch (IllegalArgumentException iae) {
                try {
                    File tmpDirectory = Files.createTempDirectory("pipeline-").toFile();
                    tmpDirectory.deleteOnExit();
                    exeFile = new File(tmpDirectory, exeFileName);
                    exeFile.deleteOnExit();
                    exeFile.getParentFile().mkdirs();
                    exeFile.createNewFile();
                    FileOutputStream writer = new FileOutputStream(exeFile);
                    exePath.openConnection();
                    InputStream reader = exePath.openStream();
                    byte[] buffer = new byte[153600];
                    int bytesRead;
                    while ((bytesRead = reader.read(buffer)) > 0) {
                        writer.write(buffer, 0, bytesRead);
                        buffer = new byte[153600];
                    }
                    writer.close();
                    reader.close();
                } catch (IOException e) {
                    throw new IOException("Could not unpack " + exeFileName, e);
                }
            }

        }
        // Launched the program with inputPath and outputPath as argument
        ProcessBuilder procbuilder;
        try{
            procbuilder = new ProcessBuilder(
                    exeFile.getAbsolutePath(),
                    new File(new URI(inputPath)).getAbsolutePath(),
                    new File(new URI(outputPath)).getAbsolutePath()
            );
            procbuilder.redirectErrorStream();

        } catch (Exception e){
            throw new Exception("Could not export shapes with word", e);
        }


        Process exporter = procbuilder.start();
        BufferedReader stdOut = new BufferedReader(new
                InputStreamReader(exporter.getInputStream()));

        BufferedReader stdError = new BufferedReader(new
                InputStreamReader(exporter.getErrorStream()));

        String s = null;
        while ((s = stdOut.readLine()) != null) {
            LOGGER.info(s);
        }

        while ((s = stdError.readLine()) != null) {
            LOGGER.error(s);
        }
        int returnCode = exporter.waitFor();
        if(returnCode != 0){
            throw new IOException("Word shapes exporter returned error code " + returnCode);
        }
    }
}
