package nl.dedicon.pipeline.braille.calabash.impl;

import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.core.XProcStep;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.runtime.XAtomicStep;
import java.io.IOException;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import nl.dedicon.pipeline.braille.model.Symbol;
import nl.dedicon.pipeline.braille.symbolslist.SymbolsReplacer;
import nl.dedicon.pipeline.braille.symbolslist.Utils;
import org.daisy.common.xproc.calabash.XProcStepProvider;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * XProc step for the symbols list
 * 
 * In a DTBook:
 *  - symbols are replaced with their braille representation
 *  - a symbols list is inserted
 * 
 * @author Paul Rambags
 */
public class SymbolsListStep extends DefaultStep {

    private static final Logger logger = LoggerFactory.getLogger(SymbolsListStep.class);

    private static final QName _symbols_code = new QName("symbols-code");
    private static final QName _symbols_list_header = new QName("symbols-list-header");

    private ReadablePipe source = null;
    private WritablePipe result = null;

    private SymbolsListStep(XProcRuntime runtime, XAtomicStep step) {
        super(runtime, step);
    }

    @Override
    public void setInput(String port, ReadablePipe pipe) {
        source = pipe;
    }

    @Override
    public void setOutput(String port, WritablePipe pipe) {
        result = pipe;
    }

    @Override
    public void reset() {
        source.resetReader();
        result.resetWriter();
    }

    @Override
    public void run() throws SaxonApiException {
        super.run();

        try {

            XdmNode book = source.read();

            info(book, "Including symbols list");

            String symbolsCode = getOption(_symbols_code, "");
            String header = getOption(_symbols_list_header, "");

            XdmNode symbolsCodeNode = runtime.parse(symbolsCode, runtime.getStaticBaseURI().toASCIIString());
            SymbolsReplacer symbolsReplacer = new SymbolsReplacer(symbolsCodeNode);

            // convert the immutable XdmNode to a modifiable Document
            Document document = Utils.convertToDocument(book);

            symbolsReplacer.replaceSymbols(document);
            symbolsReplacer.insertSymbolsList(document, header);

            XdmNode newBook = Utils.convertToXdmNode(document, runtime.getProcessor().newDocumentBuilder(), true);

            result.write(newBook);

        } catch (Exception e) {

            logger.error("dedicon:symbols-list failed", e);
            throw new XProcException(step.getNode(), e);

        }
    }

    @Component(
            name = "dedicon:symbols-list",
            service = {XProcStepProvider.class},
            property = {"type:String={http://www.dedicon.nl}symbols-list"}
    )
    public static class Provider implements XProcStepProvider {

        @Override
        public XProcStep newStep(XProcRuntime runtime, XAtomicStep step) {
            return new SymbolsListStep(runtime, step);
        }
    }
}
