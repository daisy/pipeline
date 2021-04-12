package com.textalk;

import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.runtime.XAtomicStep;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;

import org.apache.commons.io.FileUtils;
import org.daisy.common.xproc.calabash.XProcStep;
import org.daisy.common.xproc.calabash.XProcStepProvider;

import org.osgi.service.component.annotations.Component;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class AppendLegal {
    private static final String EPUB_PATH = "EPUB/";
    private static final String NAV_DOCUMENT = "EPUB/nav.xhtml";
    private static final String PACKAGE_DOCUMENT = "EPUB/package.opf";
    private static final String FILE_POSTFIX = "-colophon.xhtml";

    private String prepareLegalDoc(
            String lang,
            String identifier,
            String title,
            String author,
            int pages,
            int levels,
            String voice,
            String guideline
    ) throws Exception {
        String langAbbreviation = "sv".equalsIgnoreCase(lang.substring(0, 2)) ? "sv" : "en";


        String filename = "/mtm-legal-" + langAbbreviation + "-" + guideline + ".xhtml";
        InputStream is = AppendLegal.class.getResourceAsStream(filename);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
            sb.append(System.getProperty("line.separator"));
        }

        String template = sb.toString();

        template = template.replace("[DOCUMENT_TITLE]", title);
        template = template.replace("[TITLE]", title);
        template = template.replace("[DOCUMENT_IDENTIFIER]", identifier);
        template = template.replace("[AUTHOR]", author);
        template = template.replace("[PAGES]", Integer.toString(pages));
        template = template.replace("[LEVELS]", Integer.toString(levels));
        template = template.replace("[VOICE]", voice);
        template = template.replace("[YEAR]", new SimpleDateFormat("yyyy").format(new Date()));

        return template;
    }

    public void appendLegalDoc(File input, File output) throws Exception {
        appendLegalDoc(input, output, "Ylva");
    }

    public void copyEntry(ZipFile input, ZipOutputStream output, ZipEntry ze) throws Exception {
        ZipEntry newEntry = new ZipEntry(ze.getName());
        output.putNextEntry(newEntry);
        BufferedInputStream bis = new BufferedInputStream(input.getInputStream(ze));
        byte[] buffer = new byte[4096];
        while(bis.available() > 0) {
            int bytes = bis.read(buffer);
            output.write(buffer, 0, bytes);
        }
        output.closeEntry();
        bis.close();
    }


    private Document readXML(ZipFile zipFile, ZipEntry zipEntry) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(zipFile.getInputStream(zipEntry));
        doc.getDocumentElement().normalize();
        return doc;
    }

    public void writeXML(ZipOutputStream zos, ZipEntry ze, Document doc) throws Exception {
        zos.putNextEntry(ze);
        Transformer tr = TransformerFactory.newInstance().newTransformer();
        tr.setOutputProperty(OutputKeys.INDENT, "yes");
        tr.setOutputProperty(OutputKeys.METHOD, "xml");
        tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        tr.transform(new DOMSource(doc), new StreamResult(zos));
        zos.closeEntry();
    }

    public String getElementValue(Document doc, String name) {
        NodeList nl = doc.getElementsByTagName(name);
        if(nl.getLength() < 1) return "";
        return nl.item(0).getTextContent();
    }

    public String getGuidelines(Document doc) {
        NodeList metaList = doc.getElementsByTagName("meta");
        String foundName = null;
        for (int i = 0; i < metaList.getLength(); i++) {
            Element item = (Element) metaList.item(i);
            if (item.hasAttribute("property")) {
                if (!"nordic:guidelines".equals(item.getAttribute("property"))) continue;
                foundName = item.getTextContent();
                break;
            }
            if (item.hasAttribute("name")) {
                if (!"nordic:guidelines".equals(item.getAttribute("name"))) continue;
                foundName = item.getAttribute("content");
                break;
            }
        }
        String guidelineName = "2020-1";
        if("2015-1".equalsIgnoreCase(foundName)) {
            guidelineName = "2015-1";
        }
        return guidelineName;
    }

    public void appendLegalDoc(File input, File output, String voice) throws Exception {
        File tempFile = File.createTempFile("epub3-input", "-tmpfile");
        tempFile.deleteOnExit();
        FileUtils.copyFile(input, tempFile);
        ZipFile zipFile = new ZipFile(tempFile);
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(output));
        Enumeration entryEnumeration = zipFile.entries();
        while(entryEnumeration.hasMoreElements()) {
            ZipEntry ze = (ZipEntry) entryEnumeration.nextElement();
            if(PACKAGE_DOCUMENT.equals(ze.getName()) || NAV_DOCUMENT.equals(ze.getName())) continue;
            copyEntry(zipFile, zos, ze);
        }

        /**
         * Here we make a BUNCH of assumptions on the document structure. This because we know that the files
         * processed with this tool will be validated against the 2020-1 nordic guidelines.
         */
        Document packageDoc = readXML(zipFile, zipFile.getEntry(PACKAGE_DOCUMENT));
        Document navDoc = readXML(zipFile, zipFile.getEntry(NAV_DOCUMENT));
        String title = getElementValue(packageDoc, "dc:title");
        String language = getElementValue(packageDoc, "dc:language");
        String identifier = getElementValue(packageDoc, "dc:identifier");
        String creator = getElementValue(packageDoc, "dc:creator");
        String guideline = getGuidelines(packageDoc);

        String fileCount = insertDocumentInPackage(packageDoc, identifier);
        int[] pageAndLevel = insertDocumentInNav(navDoc, title, language, identifier, fileCount);

        writeXML(zos, new ZipEntry(PACKAGE_DOCUMENT), packageDoc);
        writeXML(zos, new ZipEntry(NAV_DOCUMENT), navDoc);

        ZipEntry legalFile = new ZipEntry(EPUB_PATH + identifier + fileCount + FILE_POSTFIX);
        zos.putNextEntry(legalFile);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(zos));
        String legalInfo = prepareLegalDoc(
                language, identifier, title, creator, pageAndLevel[0], pageAndLevel[1], voice, guideline
        );
        bw.write(legalInfo);
        bw.flush();
        zos.finish();
        zos.close();
        zipFile.close();
    }

    private String insertDocumentInPackage(Document packageDoc, String identifier) {
        NodeList nl = packageDoc.getElementsByTagName("item");
        StringBuilder sb = new StringBuilder();
        sb.append("-0");

        for(int i = 0; i < nl.getLength(); i++) {
            Element el = (Element)nl.item(i);
            if (!el.hasAttribute("media-type")) continue;
            if (!el.hasAttribute("href")) continue;
            if (!"application/xhtml+xml".equalsIgnoreCase(el.getAttribute("media-type"))) continue;
            String[] addrParts = el.getAttribute("href").split("-");
            if (addrParts.length < 2) continue;
            for (int j = 1; j < addrParts[1].length(); j++) {
                sb.append("0");
            }
            Element newPage = packageDoc.createElement("item");
            newPage.setAttribute("id", "item_0");
            newPage.setAttribute("media-type", "application/xhtml+xml");
            newPage.setAttribute("href", identifier + sb.toString() + FILE_POSTFIX);
            el.getParentNode().insertBefore(newPage, el);
            break;
        }

        NodeList nlRef = packageDoc.getElementsByTagName("itemref");
        if (nlRef.getLength() == 0) return sb.toString();
        Element el = (Element) nlRef.item(0);

        Element newPageRef = packageDoc.createElement("itemref");
        newPageRef.setAttribute("id", "itemref_0");
        newPageRef.setAttribute("idref", "item_0");
        newPageRef.setAttribute("linear", "no");

        el.getParentNode().insertBefore(newPageRef, el);
        return sb.toString();
    }

    private int[] insertDocumentInNav(
        Document navDoc, String title, String language, String identifier, String fileCount
    ) {
        String informationHeader = "Information about Swedish Copyright Law and this talking book";
        if ("sv".equalsIgnoreCase(language.substring(0, 2))) {
            informationHeader = "Information om upphovsrättslagen och om talboken";
        }

        int numPages = 0;
        int numLevels = 0;

        NodeList nl = navDoc.getElementsByTagName("nav");
        Element pageListEl = null;
        Element tocEl = null;
        Element landmarksEl = null;
        for(int i = 0; i < nl.getLength(); i++) {
            Element el = (Element)nl.item(i);
            if (!el.hasAttribute("role")) continue;
            if ("doc-pagelist".equalsIgnoreCase(el.getAttribute("role"))) {
                pageListEl = el;
            }
            if ("doc-toc".equalsIgnoreCase(el.getAttribute("role"))) {
                tocEl = el;
            }
            if ("navigation".equalsIgnoreCase(el.getAttribute("role"))) {
                landmarksEl = el;
            }
        }

        if (pageListEl != null) {
            numPages = pageListEl.getElementsByTagName("li").getLength();
        }

        if (tocEl != null) {
            numLevels = findLevels(tocEl);
            NodeList tocList = tocEl.getElementsByTagName("ol");
            if (tocList.getLength() > 0) {
                Element firstTOCList = (Element) tocList.item(0);
                NodeList firstTOCListItems = firstTOCList.getElementsByTagName("li");
                if (firstTOCListItems.getLength() > 0) {
                    Element firstListItem = (Element) firstTOCListItems.item(0);
                    firstListItem.getParentNode()
                            .insertBefore(createPageListItem(navDoc, title, informationHeader, identifier, fileCount), firstListItem);
                }
            }
        }

        if (landmarksEl != null) {
            NodeList landmarksList = landmarksEl.getElementsByTagName("ol");
            if (landmarksList.getLength() > 0) {
                Element firstTOCList = (Element) landmarksList.item(0);
                NodeList firstTOCListItems = firstTOCList.getElementsByTagName("li");
                if (firstTOCListItems.getLength() > 0) {
                    Element firstListItem = (Element) firstTOCListItems.item(0);
                    firstListItem.getParentNode()
                            .insertBefore(createLandmarksItem(navDoc, informationHeader, identifier, fileCount), firstListItem);
                }
            }
        }

        return new int[] {numPages, numLevels};
    }

    private Node createLandmarksItem(Document doc, String informationHeader, String identifier, String fileCount) {
        /*
         <li>
            <a epub:type="colophon" href="DTB36215-000-mtminfo.xhtml#level1_000">[INFORMATION_HEADER]</a>
         </li>
         */

        String fileName = identifier + fileCount + FILE_POSTFIX;
        Element mainListItem = doc.createElement("li");
        Element mainAnchor = doc.createElement("a");

        mainAnchor.setAttribute("href", fileName + "#level1_000");
        mainAnchor.setAttribute("epub:type", "colophon");
        mainAnchor.setTextContent(informationHeader);

        mainListItem.appendChild(mainAnchor);
        return mainListItem;

    }

    private Element createPageListItem(Document doc, String title, String informationHeader, String identifier, String fileCount) {
        /*
        <li>
            <a href="DTB36215-000-mtminfo.xhtml#level1_000">[TITLE]</a>
            <ol>
                <li>
                    <a href="DTB36215-000-mtminfo.xhtml#level2_000">[INFORMATION_HEADER]</a>
                </li>
            </ol>
        </li>
        */

        String fileName = identifier + fileCount + FILE_POSTFIX;
        Element mainListItem = doc.createElement("li");
        Element mainAnchor = doc.createElement("a");
        Element subList = doc.createElement("ol");
        Element subListItem = doc.createElement("li");
        Element subAnchor = doc.createElement("a");

        mainAnchor.setAttribute("href", fileName + "#level1_000");
        mainAnchor.setTextContent(title);
        subAnchor.setAttribute("href", fileName + "#level2_000");
        subAnchor.setTextContent(informationHeader);

        mainListItem.appendChild(mainAnchor);
        mainListItem.appendChild(subList);
        subList.appendChild(subListItem);
        subListItem.appendChild(subAnchor);
        return mainListItem;
    }

    private int findLevels(Element el) {
        int levels = 1;
        NodeList listNodes = el.getChildNodes();
        for (int i = 0; i < listNodes.getLength(); i++) {
            if (!"ol".equalsIgnoreCase(listNodes.item(i).getNodeName())) continue;
            Element olEl = (Element) listNodes.item(i);
            NodeList listItemNodes = olEl.getChildNodes();
            for (int j = 0; j < listItemNodes.getLength(); j++) {
                if (!"li".equalsIgnoreCase(listItemNodes.item(j).getNodeName())) continue;
                int underLevels = findLevels((Element) listItemNodes.item(j)) + 1;
                if (levels < underLevels) {
                    levels = underLevels;
                }
            }
        }
        return levels;
    }

    public static void main(String[] args) {
        AppendLegal al = new AppendLegal();
        try {
            al.appendLegalDoc(new File(args[0]), new File(args[1]));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ////////// XProc interface //////////

    /**
     * @author bertfrees
     */
    public static class Step extends DefaultStep implements XProcStep {

        @Component(
            name = "pxi:add-legal-doc",
            service = { XProcStepProvider.class },
            property = { "type:String={http://www.daisy.org/ns/pipeline/xproc/internal}add-legal-doc" }
        )
        public static class Provider implements XProcStepProvider {
            @Override
            public XProcStep newStep(XProcRuntime runtime, XAtomicStep step) {
                return new Step(runtime, step);
            }
        }

        public Step(XProcRuntime runtime, XAtomicStep step) {
            super(runtime, step);
        }

        private final static QName INPUT = new QName("input");
        private final static QName OUTPUT = new QName("input");

        @Override
        public void run() throws SaxonApiException {
            super.run();
            AppendLegal al = new AppendLegal();
            File input; {
                String i = getOption(INPUT).getString();
                try {
                    input = new File(new URI(i));
                } catch (IllegalArgumentException|URISyntaxException e) {
                    throw new XProcException(step.getStep(), "Invalid input file specified: " + i);
                }
            }
            File output; {
                String o = getOption(OUTPUT).getString();
                try {
                    output = new File(new URI(o));
                } catch (IllegalArgumentException|URISyntaxException e) {
                    throw new XProcException(step.getStep(), "Invalid output file specified: " + o);
                }
            }
            try {
                al.appendLegalDoc(input, output);
            } catch (Exception e) {
                throw new XProcException(step.getNode(), e);
            }
        }

        @Override
        public void reset() {
        }
    }
}
