package io.transpect.calabash.extensions;

import java.io.StringReader;
import java.io.IOException;

import javax.xml.transform.stream.StreamSource;

import org.jruby.embed.EvalFailedException;

import com.xmlcalabash.core.XMLCalabash;
import com.xmlcalabash.core.XProcConstants;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.runtime.XAtomicStep;
import com.xmlcalabash.util.TreeWriter;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;


@XMLCalabash(
        name = "tr:ole2xml",
        type = "{http://example.org/xmlcalabash/steps}ole2xml")

public class Mtef2Xml extends DefaultStep {
    private WritablePipe result = null;
	 private Ole2XmlConverter ole2xmlConverter;

    public Mtef2Xml(XProcRuntime runtime, XAtomicStep step) {
		  super(runtime,step);
		  this.ole2xmlConverter = new Ole2XmlConverter();
    }

    public void setOutput(String port, WritablePipe pipe) {
        result = pipe;
    }

    public void reset() {
        result.resetWriter();
    }

    private XdmNode createXMLError(String message, String file, XProcRuntime runtime){
        TreeWriter tree = new TreeWriter(runtime);
        tree.startDocument(step.getNode().getBaseURI());
        tree.addStartElement(XProcConstants.c_errors);
        tree.addAttribute(new QName("code"), "formula-error");
        tree.addAttribute(new QName("href"), file);
        tree.addStartElement(XProcConstants.c_error);
        tree.addAttribute(new QName("code"), "error");
        tree.addText(message);
        tree.addEndElement();
        tree.addEndElement();
        tree.endDocument();
        return tree.getResult();        
    }
	 
    public void run() throws SaxonApiException {
        super.run();

        String file = getOption(new QName("href")).getString();

        TreeWriter tree = new TreeWriter(runtime);
        tree.startDocument(step.getNode().getBaseURI());
        tree.startContent();
		  try {
		  		Processor proc = new Processor(false);
            DocumentBuilder builder = proc.newDocumentBuilder();
				this.ole2xmlConverter.convertFormula(file);
            StringReader reader = new StringReader(this.ole2xmlConverter.getFormula());
            XdmNode doc = builder.build(new StreamSource(reader));

		  		tree.addSubtree(doc);

		  } catch (Exception e) {
		  		System.err.println("[ERROR] Mtef2Xml: " + e.getMessage());
		  		result.write(createXMLError(e.getMessage(), file, runtime));
		  }
        tree.endDocument();
        result.write(tree.getResult());
    }
}
