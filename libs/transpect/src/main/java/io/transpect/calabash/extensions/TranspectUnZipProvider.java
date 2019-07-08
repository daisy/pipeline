package io.transpect.calabash.extensions;

import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.charset.Charset;

import java.net.URI;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.daisy.common.xproc.calabash.XProcStepProvider;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import com.xmlcalabash.core.XMLCalabash;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.core.XProcStep;
import com.xmlcalabash.core.XProcConstants;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.model.RuntimeValue;
import com.xmlcalabash.runtime.XAtomicStep;
import com.xmlcalabash.util.TreeWriter;

@Component(
		name = "tr-internal:unzip",
		service = { XProcStepProvider.class },
		property = { "type:String={http://transpect.io/internal}unzip" }
	)
public class TranspectUnZipProvider implements XProcStepProvider {

    /** The logger. */
    Logger logger = LoggerFactory.getLogger(this.getClass());

    /* (non-Javadoc)
     * @see org.daisy.common.xproc.calabash.XProcStepProvider#newStep(com.xmlcalabash.core.XProcRuntime, com.xmlcalabash.runtime.XAtomicStep)
     */
    @Override
    public XProcStep newStep(XProcRuntime runtime, XAtomicStep step) {
        return new UnZip(runtime, step);
    }

    /**
     * Activate (OSGI)
     */
    public void activate() {
        logger.trace("Activating tr-internal:unzip provider");
    }
	    
	public static class UnZip extends DefaultStep {
	    private WritablePipe result = null;
	    
	    public UnZip(XProcRuntime runtime, XAtomicStep step) {
	        super(runtime,step);
	    }
	    @Override
	    public void setOutput(String port, WritablePipe pipe) {
	        result = pipe;
	    }
	    @Override
	    public void reset() {
	        result.resetWriter();
	    }
	    @Override
	    public void run() throws SaxonApiException {
	        super.run();
	
	        RuntimeValue zip  = getOption(new QName("zip"));
	        RuntimeValue file = getOption(new QName("file"));
	        RuntimeValue path = getOption(new QName("dest-dir"));
	        RuntimeValue overwrite = getOption(new QName("overwrite"));
	
	        // submit empty string if attribute is not set
	        String zipString  = (zip  != null) ? zip.getString()  : "";
	        String fileString = (file != null) ? file.getString() : "";
	        String pathString = (path != null) ? path.getString() : "";
	        boolean overwriteBool = (overwrite != null && overwrite.getString().equals("yes")) ? true : false;
	
	        if(!zipString.equals("")) {
	            if(!pathString.equals("")) {
	                try {
	                    // main pipeline
	                    if(pathString.charAt(pathString.length()-1)!=File.separatorChar){
	                        pathString += File.separator;
	                    }
	                    URI baseuri = new File(pathString).getCanonicalFile().toURI();
	                    createDirectory(pathString, overwriteBool);
	                    try { // expect UTF-8 file names
	                        ArrayList<String> fileList = unzip(zipString, fileString, pathString, "UTF-8");
	                        XdmNode XMLFileList = createXMLFileList(fileList, baseuri, runtime);
	                        result.write(XMLFileList);
	                        System.out.println("[info] Unzip finished successfully.");
	                    } catch(Exception e) {
	                        try { // expect CP437 file names
	                            ArrayList<String> fileList = unzip(zipString, fileString, pathString, "cp437");
	                            XdmNode XMLFileList = createXMLFileList(fileList, baseuri, runtime);
	                            result.write(XMLFileList);
	                            System.out.println("[info] Unzip finished successfully.");
	                        }
	                        catch(Exception ee) {
	                            System.err.println("[ERROR] Unzip: " + ee.getMessage());
	                            result.write(createXMLError(e.getMessage(), zipString, runtime));
	                        }
	                    }
	                } catch(IOException ioe) {
	                    System.err.println("[ERROR] Unzip: " + ioe.getMessage());
	                    result.write(createXMLError(ioe.getMessage(), zipString, runtime));
	                }
	            } else {
	                result.write(createXMLError("The attribute path must not be an empty string.", zipString, runtime));
	            }
	        } else {
	            result.write(createXMLError("The attribute zip must not be an empty string.", zipString, runtime));
	        }
	    }
	    private static void createDirectory(String directory, boolean overwrite) throws IOException{
	        Path path = Paths.get(directory).toAbsolutePath();
	        File dir  = new File(path.toString());
	        if (Files.exists(path)) {
	            // delete directory recursively
	            if(overwrite) {
	                System.out.println("[info] Unzip: Deleting directory: " + path);
	                // see https://twitter.com/gimsieke/status/691323769445601281
	                if(path.getNameCount() != 0) { 
	                    FileUtils.deleteQuietly(dir);
	                    Files.createDirectories(path);
	                } else {
	                    System.out.println("[WARNING] Unzip: Directory not deleted. Seems to be your root directory. Index: " + path.getNameCount());
	                }
	            }
	       } else {
	           try {
	               Files.createDirectories(path);
	           } catch(IOException ioe) {
	               System.err.println("[ERROR] " + ioe.getMessage());
	           }
	        }
	    }
	    // create an ArrayList which contains the filenames
	    private static ArrayList<String> unzip(String zip, String file, String outputDirectory, String encoding) throws IOException {
	        final ZipFile zipFile = new ZipFile( zip, Charset.forName(encoding) );
	        ArrayList<String> fileList = new ArrayList<String>();
	        try {
	            final Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
	            System.out.println("[info] Unzipping: " + zip + " ==> " + outputDirectory);
	            if(!file.equals("")){
	                // unzip single file
	                final ZipEntry zipEntry = zipFile.getEntry(file);
	                String fileName = unzipSingleFile(zipFile, zipEntry, outputDirectory);
	                fileList.add(fileName);
	            } else {
	                // unzip all files
	                while (zipEntries.hasMoreElements()){
	                    final ZipEntry zipEntry = zipEntries.nextElement();
	                    String fileName = unzipSingleFile(zipFile, zipEntry, outputDirectory);
	                    fileList.add(fileName);
	                }
	            }
	        } finally {
	            zipFile.close();
	        }
	        System.out.println("[info] Unzipped " + fileList.size() + " files");
	        return fileList;
	    }
	    private static String unzipSingleFile(ZipFile zipFile, ZipEntry zipEntry, String outputDirectory) throws IOException {
	        String fileName = zipEntry.getName();
	        if(zipEntry.isDirectory()) {
	            createDirectory(outputDirectory + File.separator + fileName, false);
	        } else {
	            File newFile = new File(outputDirectory + File.separator + fileName).getAbsoluteFile();
	            //create directories on demand
	            new File(newFile.getParent()).mkdirs();
	            try(FileOutputStream fos = new FileOutputStream(newFile)){
	                byte[] buffer = new byte[4096];
	                InputStream in = zipFile.getInputStream(zipEntry);
	                int len = in.read(buffer);
	                while (len != -1) {
	                    fos.write(buffer, 0, len);
	                    len = in.read(buffer);
	                }
	            }
	        }
	        return fileName;
	    }
	    // create regular XML output with a list of files
	    private static XdmNode createXMLFileList(ArrayList<String> fileList, URI baseuri, XProcRuntime runtime) throws SaxonApiException {
	        QName xml_base = new QName("xml", "http://www.w3.org/XML/1998/namespace" ,"base");
	        QName c_files = new QName("c", "http://www.w3.org/ns/xproc-step" ,"files"); 
	        QName c_file = new QName("c", "http://www.w3.org/ns/xproc-step" ,"file");
	        QName c_dir = new QName("c", "http://www.w3.org/ns/xproc-step" ,"directory");
	        TreeWriter tree = new TreeWriter(runtime);
	        tree.startDocument(baseuri);
	        tree.addStartElement(c_files);
	        tree.addAttribute(xml_base, baseuri.toString());
	        for (String fileName: fileList) {
	            File file = new File(baseuri.getPath() + File.separator + fileName);
	            if(file.isDirectory()){
	                tree.addStartElement(c_dir);
	            } else {
	                tree.addStartElement(c_file);
	            }
	            tree.addAttribute(new QName("name"), fileName);
	            tree.addEndElement();
	        }
	        tree.addEndElement();
	        tree.endDocument();
	        return tree.getResult();
	    }
	    // in case of errors, present them as XML
	    private XdmNode createXMLError(String message, String zip, XProcRuntime runtime){
	        TreeWriter tree = new TreeWriter(runtime);
	        tree.startDocument(step.getNode().getBaseURI());
	        tree.addStartElement(XProcConstants.c_errors);
	        tree.addAttribute(new QName("code"), "zip-error");
	        tree.addAttribute(new QName("href"), zip);
	        tree.addStartElement(XProcConstants.c_error);
	        tree.addAttribute(new QName("code"), "error");
	        tree.addText(message);
	        tree.addEndElement();
	        tree.addEndElement();
	        tree.endDocument();
	        return tree.getResult();        
	    }
	}
}