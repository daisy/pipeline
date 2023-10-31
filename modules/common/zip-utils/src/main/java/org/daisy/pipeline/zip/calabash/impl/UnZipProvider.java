package org.daisy.pipeline.zip.calabash.impl;

/**
 * Unzip extension for non-XML files
 *
 * @author Lars Wittmar -- le-tex publishing services GmbH
 * @date   2012-03-08
 * 
 * A Java unzip for XProc, particularly useful 
 * if you want to access zip contents as extracted files
 * (and don't want to retrieve single files recursively, and
 * potentially base64 encoded).
 * 
 * Written by Lars Wittmar, le-tex publishing services GmbH
 * (small portions also by Gerrit Imsieke)
 * 
 * Configure letex:unzip step by passing options to it:
 * zip (required):      file name (not URI) of a zip file
 * dest-dir (required): directory (relative or absolute; not an URI)
 * overwrite ('yes'/'no', optional, default 'no'): overwrite individual files even if they already exist
 * file (optional):     file name relative to zip root. If omitted, extract complete zip file
 * 
 * The directory (and missing subdirectories) will be created if
 * missing. 
 * 
 * CAVEAT: If overwrite='yes' and, e.g., dest-dir='/', it will attempt to remove the root directory.
 * 
 * DO NOT USE THIS AS A USER WITH ADMIN PRIVILEGES.
 * 
 *
 * 2015-02-27, Jostein Austvik Jacobsen (NLB - Norwegian library of talking books and braille):
 *     - Wrapped into a org.daisy.common.xproc.calabash.XProcStepProvider
 *     - removed Apache Commons dependency
 *     - Use file URIs instead of OS-specific paths for options
 * 2016-02-10, Jostein Austvik Jacobsen (NLB - Norwegian library of talking books and braille):
 *     - slightly changed the meaning of the 'overwrite' option
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.model.RuntimeValue;
import com.xmlcalabash.runtime.XAtomicStep;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.QName;

import org.daisy.common.xproc.calabash.XProcStep;
import org.daisy.common.xproc.calabash.XProcStepProvider;
import org.daisy.common.xproc.XProcMonitor;

import org.osgi.service.component.annotations.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
	name = "letex:unzip",
	service = { XProcStepProvider.class },
	property = { "type:String={http://www.le-tex.de/namespace}unzip" }
)
public class UnZipProvider implements XProcStepProvider {

    /** The logger. */
    Logger logger = LoggerFactory.getLogger(this.getClass());

    /* (non-Javadoc)
     * @see org.daisy.common.xproc.calabash.XProcStepProvider#newStep(com.xmlcalabash.core.XProcRuntime, com.xmlcalabash.runtime.XAtomicStep)
     */
    @Override
    public XProcStep newStep(XProcRuntime runtime, XAtomicStep step, XProcMonitor monitor, Map<String,String> properties) {
        return new UnZip(runtime, step);
    }

    /**
     * Activate (OSGI)
     */
    public void activate() {
        logger.trace("Activating letex:unzip provider");
    }
    
    public static class UnZip
            extends DefaultStep implements XProcStep
    {
        public UnZip(XProcRuntime runtime, XAtomicStep step)
        {
            super(runtime,step);
        }

        @Override
        public void setOutput(String port, WritablePipe pipe)
        {
            myResult = pipe;
        }

        @Override
        public void reset()
        {
            myResult.resetWriter();
        }

        @Override
        public void run()
            throws SaxonApiException
        {
            super.run();
            
            String result = null;
            File folder = null, source = null;
            try {
                RuntimeValue href = getOption(new QName("zip"));
                URI sourceUri = href.getBaseURI().resolve(href.getString());
                href = getOption(new QName("dest-dir"));
                URI destinationUri = href.getBaseURI().resolve(href.getString());
    			
    			if (!"file".equals(sourceUri.getScheme())) {
    				throw new XProcException(step, "Only file: scheme URIs are supported by zip URI.");
    			} else {
    				source = new File(sourceUri.getPath());
    				if (!source.isFile()) {
    					throw new XProcException(step, "The zip URI must refer to a file.");
    				}
    			}
    			
    			if (!"file".equals(destinationUri.getScheme())) {
    				throw new XProcException(step, "Only file: scheme URIs are supported by dest-dir URI.");
    			} else {
    				folder = new File(destinationUri.getPath());
    				if (!folder.exists()) {
    					folder.mkdirs();
    				}
    				if (!folder.isDirectory()) {
    					throw new XProcException(step, "The dest-dir URI must refer to a directory.");
    				}
    				if (folder.getParentFile() == null || folder.getParentFile().getParentFile() == null) {
    					// Basic safety valve; should catch stuff like "/", "/home/", "C:\", "C:\Windows\" etc.
    					// Mainly aimed at catching typos or bugs in scripts during developent;
    					// running without admin-privileges is the only fairly safe option here 
    					throw new XProcException(step, "The dest-dir URI must not be the file system root directory (or any of its children) (was: \""+folder.getCanonicalPath()+"\").");
    				}
    			}
    			
                String overwriteS = getOption(new QName("overwrite")).getString();
                boolean overwrite = false; 
                if (overwriteS != null) {
                    if (overwriteS.matches("yes")) {
                        overwrite = true;
                    }
                }

    			String filename = "";
    			if (getOption(new QName("file"))!=null) {
                    filename = getOption(new QName("file")).getString();
    			}
    			
                if(! folder.exists()) { 
                    folder.mkdirs(); 
                }
    			
    			final int BUFFER = 2048;
    			BufferedOutputStream dest = null;
    			FileInputStream fis = new FileInputStream(source);
    			ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
    			ZipEntry entry;

                int numFiles = 0;
    			result = "<c:files xmlns:c=\"http://www.w3.org/ns/xproc-step\" xml:base=\"" + folder.toURI() + "\">";
    			//without given fileNode extract everything
    			if(filename.equals("")) {
                    while((entry = zis.getNextEntry()) != null) {
                        int count;
                        byte data[] = new byte[BUFFER];
                        
                        if(entry.isDirectory()) {
                        	File dir = new File(folder, entry.getName());
                            if (!dir.exists()) { dir.mkdirs(); }
                        } else {
                            numFiles++;
                            result += "<c:file name=\"" + entry.getName() + "\"/>";
                            File destFile = new File(folder, entry.getName());
                            if (destFile.exists()) {
                                if (overwrite) {
                                    deleteDirectory(destFile);
                                } else {
                                    continue;
                                }
                            }
                            File destdir = destFile.getParentFile();
                            if (!destdir.exists()) { destdir.mkdirs(); }
                            FileOutputStream fos = new FileOutputStream(destFile);
                            dest = new BufferedOutputStream(fos, BUFFER);
                            while ((count = zis.read(data, 0, BUFFER)) != -1) {
                                dest.write(data, 0, count);
                            }
                            dest.flush();
                            dest.close();
                        }
                    }
    			} else {
                    while((entry = zis.getNextEntry()) != null) {
                        if(filename.equals(entry.getName())) {
                            int count;
                            byte data[] = new byte[BUFFER];
                            
                            numFiles++;
                            result += "<c:file name=\"" + entry.getName() + "\"/>";
                            File destFile = new File(folder, entry.getName());
                            if (destFile.exists()) {
                                if (overwrite) {
                                    deleteDirectory(destFile);
                                } else {
                                    continue;
                                }
                            }
                            File destdir = destFile.getParentFile();
                            if (!destdir.exists()) { destdir.mkdirs(); }
                            FileOutputStream fos = new FileOutputStream(destFile);
                            dest = new BufferedOutputStream(fos, BUFFER);
                            while ((count = zis.read(data, 0, BUFFER)) != -1) {
                                dest.write(data, 0, count);
                            }
                            dest.flush();
                            dest.close();
                        }							 
                    }
    			}
    			
    			zis.close();
    			if (numFiles == 0) {
    			  result = "<c:error xmlns:c=\"http://www.w3.org/ns/xproc-step\" xmlns:letex=\"http://www.le-tex.de/namespace\" code=\"zip-error\" href=\""+folder.toURI()+source.toURI()+"\">No content processed. Zip file may empty or corrupted.</c:error>";
    			} else {
    			  result += "</c:files>";
    			}
                
    		  } catch(Exception e) {
    			  StringWriter stackTraceWriter = new StringWriter();
    			  e.printStackTrace(new PrintWriter(stackTraceWriter));
    			  String stackTrace = stackTraceWriter.toString().replaceAll("-", " - "); // just to be sure that the comment is valid
    			  result = "<c:error xmlns:c=\"http://www.w3.org/ns/xproc-step\" xmlns:letex=\"http://www.le-tex.de/namespace\" code=\"zip-error\" href=\""+folder.toURI()+source.toURI()+"\">Zip file seems to be corrupted: "+e.getMessage()+"<!-- "+stackTrace+" --></c:error>";
    		  }
            DocumentBuilder builder = runtime.getProcessor().newDocumentBuilder();
            Source src = new StreamSource(new StringReader(result));
            XdmNode doc = builder.build(src);

            myResult.write(doc);
        }
        
        /** http://stackoverflow.com/a/3775718/281065 */
        public static boolean deleteDirectory(File directory) {
            if(directory.exists()){
                File[] files = directory.listFiles();
                if(null!=files){
                    for(int i=0; i<files.length; i++) {
                        if(files[i].isDirectory()) {
                            deleteDirectory(files[i]);
                        }
                        else {
                            files[i].delete();
                        }
                    }
                }
            }
            return(directory.delete());
        }

        private WritablePipe myResult = null;
    }
    
}
