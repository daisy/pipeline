package nl.dedicon.pipeline.braille.symbolslist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmSequenceIterator;
import nl.dedicon.pipeline.braille.model.Context;
import nl.dedicon.pipeline.braille.model.Replace;
import nl.dedicon.pipeline.braille.model.Symbol;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * Replace symbols in a DTBook and insert a symbols list
 * Based on W3C DOM
 * 
 * @author Paul Rambags
 */
public class SymbolsReplacer  {
    private static final QName _braille = new QName("braille");
    private static final QName _char = new QName("char");
    private static final QName _context = new QName("context");
    private static final QName _description = new QName("description");
    private static final QName _language = new QName("language");
    private static final QName _replace = new QName("replace");
    private static final QName _symbol = new QName("symbol");
    private static final QName _symbols = new QName("symbols");

    private final Map<String, Symbol> symbolsMap;
    private final Integer[] symbolLengthsDescending;
    private final Set<Replace> replacesFound = new HashSet<>();
    
    /**
     * Constructor
     * 
     * @param symbolsCodeNode root document of the symbols code XML
     */
    public SymbolsReplacer(XdmNode symbolsCodeNode) {
        this.symbolsMap = filterSymbols(symbolsCodeNode);
        this.symbolLengthsDescending = determineSymbolLengths(this.symbolsMap);
    }
    
    /**
     * Generate a Symbols HashMap
     * 
     * @param symbolsCodeNode SymbolsCode root document of the symbols code file
     * @return Character -> Symbol map
     */
    private static Map<String, Symbol> filterSymbols(XdmNode symbolsCodeNode) {
        Map<String, Symbol> symbolsMap = new HashMap<>();

        XdmSequenceIterator symbolsIterator = symbolsCodeNode.axisIterator(Axis.CHILD, _symbols);
        while (symbolsIterator.hasNext()) {
            XdmNode symbolsNode = (XdmNode)symbolsIterator.next();
            XdmSequenceIterator symbolIterator = symbolsNode.axisIterator(Axis.CHILD, _symbol);
            while (symbolIterator.hasNext()) {

                XdmNode symbolNode = (XdmNode)symbolIterator.next();
                String character = Utils.getValue(symbolNode, _char);
                String language = Utils.getValue(symbolNode, _language);

                List<Replace> replaces = new ArrayList<>();
                XdmSequenceIterator replaceIterator = symbolNode.axisIterator(Axis.CHILD, _replace);
                while (replaceIterator.hasNext()) {

                    XdmNode replaceNode = (XdmNode)replaceIterator.next();
                    Context context = Context.get(replaceNode.getAttributeValue(_context));
                    String braille = Utils.getValue(replaceNode, _braille);
                    String description = Utils.getValue(replaceNode, _description);

                    if (context != null && braille != null && braille.length() > 0) {

                        Replace replace = new Replace();
                        replace.setContext(context);
                        replace.setBraille(DediconBrl.convert(braille));
                        replace.setDescription(description);
                        replaces.add(replace);

                    }
                }

                if (character != null && character.length() > 0 && !replaces.isEmpty()) {

                    Symbol symbol = new Symbol();
                    symbol.setCharacter(character);
                    symbol.setLanguage(language);
                    symbol.setReplaces(replaces);
                    replaces.stream().forEach(r -> r.setParent(symbol));
                    symbolsMap.put(character, symbol);

                }
            }
        }
        return symbolsMap;
    }

    /**
     * Determine the different symbol lengths in descending order
     * 
     * @param symbolsMap symbols map
     * @return symbol lengths in descending order
     */
    private static Integer[] determineSymbolLengths(Map<String, Symbol> symbolsMap) {
        Set<Integer> symbolLengths = new HashSet<>();
        symbolsMap.keySet()
                .stream()
                .map(String::length)
                .forEach(symbolLengths::add);
        Integer[] symbolLengthsDescending = symbolLengths.toArray(new Integer[symbolLengths.size()]);
        // sort descending
        Arrays.sort(symbolLengthsDescending, (i,j) -> j.compareTo(i));
        return symbolLengthsDescending;
    }

    /**
     * Get the set of replaces for this DTBook
     * 
     * @return the set of replaces for this DTBook
     */
    public Set<Replace> getReplacesFound() {
        return replacesFound;
    }

    private String replace(final String source) {
        if (source == null) {
            return null;
        }
        
        String target = source;
        int index = 0;
        while (index < target.length()) {
            for (Integer symbolLength : symbolLengthsDescending) {
                if (index + symbolLength > target.length()) {
                    continue;
                }
                String substring = target.substring(index, index + symbolLength);
                Symbol symbol = symbolsMap.get(substring);
                if (symbol == null) {
                    continue;
                }
                Replace replace = determineReplace(symbol);
                if (replace == null) {
                    continue;
                }

                // Replace found
                String braille = replace.getBraille();
                if (replace.getDescription() != null && replace.getDescription().length() > 0) {
                    target = target.substring(0, index).concat(braille).concat(target.substring(index + symbolLength));
                    replacesFound.add(replace);
                }
                index += braille.length() - 1;
                break;
            }
            index++;
        }
        return target;
    }

    private Replace determineReplace(Symbol symbol) {
        return symbol.getReplaces()
                .stream()
                .filter(r -> r.getContext() == Context.Default)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Recursively replace all symbols in text nodes with their braille representation
     * 
     * @param node DTBook node
     */
    public void replaceSymbols(Node node) {
        if (node.getNodeType() == Node.TEXT_NODE) {
            String text = node.getTextContent();
            if (text != null && text.length() > 0) {
                String replacement = replace(text);
                node.setTextContent(replacement);
            }
        } else {
            for (Node childNode = node.getFirstChild(); childNode != null; childNode = childNode.getNextSibling()) {
                // recursion
                replaceSymbols(childNode);
            }
        }
    }

    /**
     * Inserts the symbols list in a DTBook
     * 
     * @param document DTBook
     * @param header symbols list header
     */
    public void insertSymbolsList(Document document, String header) {
        // the header can contain symbols, too
        String headerWithSymbolsReplaced = replace(header);

        Element dtbook = document.getDocumentElement();
        Node book = Utils.getChild(dtbook, "book");

        if (book == null || getReplacesFound().isEmpty()) {
            return;
        }
        
        Node frontMatter = Utils.getChild(book, "frontmatter");
        if (frontMatter == null) {
            frontMatter = Utils.addChild(book, "frontmatter");
        }
        Element level1 = Utils.addChild(frontMatter, "level1");
        level1.setAttribute("class", "symbols_list");

        if (headerWithSymbolsReplaced != null && headerWithSymbolsReplaced.length() > 0) {
            Element h1 = Utils.addChild(level1, "h1");
            h1.setTextContent(headerWithSymbolsReplaced);
        }
        
        Element list = Utils.addChild(level1, "list");
        list.setAttribute("type", "pl");
        
        getReplacesFound().stream()
                .sorted((r1, r2) -> r1.getParent().getCharacter().compareTo(r2.getParent().getCharacter()))
                .forEachOrdered(r -> {
                    String text = String.format("\u283F%s\u00A0 %s", r.getBraille(), r.getDescription());
                    Element li = Utils.addChild(list, "li");
                    li.setTextContent(text);
                });
    }
}
