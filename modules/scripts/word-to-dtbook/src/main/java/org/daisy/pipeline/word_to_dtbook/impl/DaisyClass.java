package org.daisy.pipeline.word_to_dtbook.impl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Stack;
import java.util.List;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.TargetMode;

import org.daisy.common.xpath.saxon.ExtensionFunctionProvider;
import org.daisy.common.xpath.saxon.ReflexiveExtensionFunctionProvider;

import org.daisy.pipeline.word_to_dtbook.shapes.OOShapesExporter;
import org.daisy.pipeline.word_to_dtbook.shapes.WordShapesExporter;
import org.osgi.service.component.annotations.Component;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import com.google.common.collect.ImmutableMap;

public class DaisyClass {

	private static final Logger LOGGER = LoggerFactory.getLogger(DaisyClass.class);
	private static final String wordRelationshipType = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument";
	private static final String footnotesRelationshipType = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/footnotes";
	private static final String endnotesRelationshipType = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/endnotes";
	private static final String numberRelationshipType = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/numbering";
	private static final String wordNamespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";
	private static final String CustomRelationshipType = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/customXml";
	private static final String customPropRelationshipType = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/custom-properties";
	private static final String docNamespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";
	private static final String emptyListCounter = "A";
	private static final Integer headingZeroLvl = 0;
	private static final Integer headingOneLvl = 1;
	private static final Integer headingSixLvl = 6;
	private static final String version2010 = "14.0", version2007 = "12.0", version2003 = "11.0", versionXP = "10.0";
	private static final List<String> bulletChar = new ArrayList<>();

	static {
		bulletChar.add("\u2605");
		bulletChar.add("\u25B6");
		bulletChar.add("\u25A3");
		bulletChar.add("\u25CF");
		bulletChar.add("\u25C6");
		bulletChar.add("\u25CB");
		bulletChar.add("\u25B2");
		bulletChar.add("\u25C8");
		bulletChar.add("\u25C7");
	}

	/** Destination folder */
	private final File outputFilename;
	/** Input file name without extension and without spaces */
	private final String inputName;
	private final OPCPackage pack;
	/** stack of document levels value */
	private final Stack<Integer> stackList = new Stack<>();
	/** Stack of abbrevations (and acronyms) */
	private final Stack<String> abbrstackList = new Stack<>();
	private final Stack<String> abbrparastackList = new Stack<>();
	private final Stack<String> abbrheadstackList = new Stack<>();
	private final Stack<String> masteSubstackList = new Stack<>();
	private final List<String> arrListLang = new ArrayList<>();
	private final List<String> startItem = new ArrayList<>();
	private final List<String> prevHeadId = new ArrayList<>();
	private final Hashtable<String,List<String>> startHeadingItem = new Hashtable<>();
	private final List<String> OverideNumList = new ArrayList<>();
        /**
         * Stack of character style ti apply on a groupe of letter or text
         * This is call by CustomCharStyle template to handle
         * italic (em), bold(strong), superscript(sup) and subscript(sub) groups of characters
         */
        Deque<String> characterStyle = new ArrayDeque<>();
	/** stack of levels value for lists */
	private Stack<String> lstackList = new Stack<>();
	/** Stack of lists headings */
	private Stack<String> listHeadingstackList = new Stack<>();
	private List<String> arrHyperlink = new ArrayList<>();
	/**
	 * Notes id
	 */
	private List<Integer> notesIdsQueue = new ArrayList<>();
	/**
	 * Notes level
	 */
	private List<Integer> notesLevelsQueue = new ArrayList<>();
	private List<Integer> arrCaptionProdnote = new ArrayList<>();
	private String strImageExt = "";
	private String sectionPagetype = "";
	private String getAuthor = "";
	private String getTitle = "";
	private String getYear = "";
	private String storeHyperId = "";
	private String caption;
	private String message = "";
	private int AbbrAcrflag = 0;
	private int listflag = 0;
	private int listHeadingFlag = 0;
	private int imgId = 0;
	private int pageNum = 0;
	private int setHyperLinkFlag = 0;
	private int listMasterSubFlag = 0;
	private int checkSectionFront = 0;
	private int chekTocOccur = 0;
	/** Number of pages found before the toc */
	private int pageToc = 1;
	private int checkSection = 0;
	private int checkSectionBody = 0;
	private int sectionCounter = 0;
	private int noteFlag = 0;
	private int incrementPage = 0;
	private int rowspan = 0;
	private int set_tabToc = 0;
	private int set_Toc = 0;
	private int bdoflag = 0;
	private int captionFlag = 0;
	private int testRun = 0;
	private int set = 0;
	private int setbookmark = 0;
	private int checkCverpage = 0;
	private int pageId = 0;
	private int sectionpageStart = 0;
	private int codeFlag = 0;
	private int conPageBreak = 0;
	private int flagRowspan = 0;
	private int linenumflag = 0;
	private int tmpcount = 0;
	private String prevHeadLvl = "";
	private String prevNumId = "";
	private String prevHeadNumId = "";
	private String baseNumId = "";
	private String baseAbsId = "";
	private Hashtable<String,List<String>> listCounters = new Hashtable<>();
	private Hashtable<String,List<String>> headingCounters = new Hashtable<>();
	private int objectId = 0;
	private String headingInfo = "";
	private boolean shapeIsExported = false;
	private PageStylesValidator _pageStylesValidator = new PageStylesValidator();
	private List<PageStyle> _currentParagraphStylse = new ArrayList<>();
	private StringBuilder _pageStylesErrors = new StringBuilder();
	private boolean _isAnyPageStyleApplied = false;
	private String _currentMatterType = "";

	/**
	 * DaisyClass constructor.
	 *
	 * Initialize the global variables to be modified by function calls from xslt stylesheets.
	 *
	 * @param input            Input .docx file
	 * @param output           Destination folder of .xml file
	 * @param extractShapes	   Try to export shapes from the daisyclass.
	 *                         Needs to be set to false for plugins that could block word from opening
	 */
	public DaisyClass(String input,
	                  String output,
					  Boolean extractShapes
	) throws InvalidFormatException {

		this.inputName = GetFileNameWithoutExtension(new File(URI.create(input))).replace(" ", "_");
		outputFilename = new File(URI.create(output));
		outputFilename.mkdirs();
		File inputFile = new File(URI.create(input));

		// first try to copy the current file in another location
		// as the file might be opened by the user in word
		OPCPackage copyOrOriginal;
		try{
			File tmpDirectory = Files.createTempDirectory("pipeline-").toFile();
			tmpDirectory.deleteOnExit();
			File copied = new File(tmpDirectory,inputFile.getName());
			try (
					InputStream in = new BufferedInputStream(
							new FileInputStream(inputFile));
					OutputStream out = new BufferedOutputStream(
							new FileOutputStream(copied))) {

				byte[] buffer = new byte[4096];
				int lengthRead;
				while ((lengthRead = in.read(buffer)) > 0) {
					out.write(buffer, 0, lengthRead);
					out.flush();
				}
			}
			if(extractShapes){
				try{
					//throw new java.lang.Exception("Disabling WordShapesExporter for test");
					WordShapesExporter.ProcessShapes(copied.toURI().toString(), outputFilename.toURI().toString());
				} catch (java.lang.Exception e){
					LOGGER.info(e.getMessage());
					LOGGER.info("Trying openoffice shapes export...");
					try {
						OOShapesExporter.ProcessShapes(copied.toURI().toString(), outputFilename.toURI().toString());
					} catch (Exception ex) {
						LOGGER.info("Could not export shapes with openoffice, shapes will be ignored");
					}
				}
			}
			copyOrOriginal = OPCPackage.open(copied);
		} catch (Exception e){
			LOGGER.info("Could not copy the input for shapes treatment");
			copyOrOriginal = OPCPackage.open(inputFile);
		}
		pack = copyOrOriginal;
		for (int i = 0; i < 9; i++) {
			startItem.add("");
		}
		for (int i = 0; i < 2; i++) {
			prevHeadId.add("");
		}
	}

	/**
	 * Function which returns Upper Roman letter with respect to an integer
	 */
	public static String PageNumUpperRoman(int counter) {
		int[] values = new int[] { 1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1 };
		String[] numerals = new String[] { "M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I" };
		StringBuilder result = new StringBuilder();
		int check = counter;
		if (check == 0) {
			check = 1;
		}
		for (int i = 0; i < values.length; i++) {
			// If the number being converted is less than the test value, append
			// the corresponding numeral or numeral pair to the resultant string
			while (check >= values[i]) {
				check -= values[i];
				result.append(numerals[i]);
			}
		}
		return result.toString();
	}

	/**
	 * Function which returns Lower Roman letter with respect to an integer
	 */
	public static String PageNumLowerRoman(int counter) {
		int[] values = new int[] { 1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1 };
		String[] numerals = new String[] { "m", "cm", "d", "cd", "c", "xc", "l", "xl", "x", "ix", "v", "iv", "i" };
		StringBuilder result = new StringBuilder();
		int check = counter;
		if (check == 0) {
			check = 1;
		}
		for (int i = 0; i < values.length; i++) {
			// If the number being converted is less than the test value, append
			// the corresponding numeral or numeral pair to the resultant string
			while (check >= values[i]) {
				check -= values[i];
				result.append(numerals[i]);
			}
		}
		return result.toString();
	}

	/**
	 * Function which returns Lower Alphabet with respect to an integer
	 */
	public static String PageNumLowerAlphabet(int counter) {
		String[] numerals = new String[] { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z" };
		String lowerAlpha;
		int check = counter;
		/*if counter value is greater than 26,then checking the difference and getting the proper alphabet*/
		if (check > 26) {
			check = check - 26;
			lowerAlpha = numerals[check - 1] + numerals[check - 1];
		} else if (check != 0) {
			lowerAlpha = numerals[check - 1];
		} else {
			lowerAlpha = numerals[0];
		}
		return lowerAlpha;
	}

	/**
	 * Function which returns Upper Alphabet with respect to an integer
	 */
	public static String PageNumUpperAlphabet(int counter) {
		String[] numerals = new String[] { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };
		String upperAlpha;
		int check = counter;
		/*if counter value is greater than 26,then checking the difference and getting the proper alphabet*/
		if (check > 26) {
			check = check - 26;
			upperAlpha = numerals[check - 1] + numerals[check - 1];
		} else if (check != 0) {
			upperAlpha = numerals[check - 1];
		} else {
			upperAlpha = numerals[0];
		}
		return upperAlpha;
	}

	/**
	 * Function to get Unique ID
	 */
	public static long GenerateId() {
		return new BigInteger(UUID.randomUUID().toString().substring(0, 8).getBytes()).longValue();
	}

	/**
	 * Function to return special character
	 */
	public static String EscapeSpecial(String id) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < id.length(); i++) {
			if ((id.charAt(i) >= '0' && id.charAt(i) <= '9') || (id.charAt(i) >= 'A' && id.charAt(i) <= 'z')) {
				sb.append(id.charAt(i));
			}
		}
		return sb.toString();
	}

	/**
	 * Function used to compare two Headings info
	 */
	public static int CompareHeading(String strA, String strB) {
		int value = 0;
		if (strA.substring(0, strA.length() - 1).equals(strB.substring(0, strB.length() - 1)))
			value = 1;
		return value;
	}

	private static String SpecificFormat(String lvlText, String numFormat, int iLvl) {
		String tempString = "";
		if (numFormat.equals("decimal") || numFormat.equals("decimalZero")) {
			tempString = lvlText;
		} else if (numFormat.equals("lowerLetter")) {
			tempString = PageNumLowerAlphabet(Integer.parseInt(lvlText));
		} else if (numFormat.equals("upperLetter")) {
			tempString = PageNumUpperAlphabet(Integer.parseInt(lvlText));
		} else if (numFormat.equals("upperRoman")) {
			tempString = PageNumUpperRoman(Integer.parseInt(lvlText));
		} else if (numFormat.equals("lowerRoman")) {
			tempString = PageNumLowerRoman(Integer.parseInt(lvlText));
		} else if (numFormat.equals("bullet")) {
			tempString = bulletChar.get(iLvl);
		} else if (numFormat.equals("none")) {
			tempString = "";
		} else {
			tempString = lvlText;
		}
		return tempString;
	}

	private static boolean IsOffice2007Or2010(String version) {
		return version.equals(version2007) || version.equals(version2010);
	}

	public static void sink(Object item) {}

	private static String GetFileNameWithoutExtension(File f) {
		String name = f.getName();
		if (name.lastIndexOf('.') >= 0)
			name = name.substring(0, name.lastIndexOf('.'));
		return name;
	}

	private static String GetExtension(File f) {
		if (f == null)
			return null;
		String name = f.getName();
		if (name.lastIndexOf('.') >= 0)
			return name.substring(name.lastIndexOf('.'));
		else
			return "";
	}

	private static XPathExpression compileXPathExpression(XPath xpath, String expression, final Map<String,String> namespaces)
			throws XPathExpressionException {
		if (namespaces != null)
			xpath.setNamespaceContext(
				new NamespaceContext() {
					public String getNamespaceURI(String prefix) {
						return namespaces.get(prefix); }
					public String getPrefix(String namespaceURI) {
						for (String prefix : namespaces.keySet())
							if (namespaces.get(prefix).equals(namespaceURI))
								return prefix;
						return null; }
					public Iterator<String> getPrefixes(String namespaceURI) {
						List<String> prefixes = new ArrayList<String>();
						for (String prefix : namespaces.keySet())
							if (namespaces.get(prefix).equals(namespaceURI))
								prefixes.add(prefix);
						return prefixes.iterator(); }});
		else
			xpath.setNamespaceContext(null);
		return xpath.compile(expression);
	}

	public void End(){
		if(!pack.isClosed()){
			try{
				pack.close();
			} catch (Exception e){

			}
		}
	}

	/**
	 * - In the input document name (inputName), replace all spaces by underscore
	 * - If the output is set to the %APPDATA%/SaveAsDaisy folder and if a png image ending by the "id"
	 *   parameter exists in it, copy it to the "output_pipeline" folder
	 * - If the output is not the temporary folder and if a png image ending by the "id" parameter
	 *   exists in %APPDATA%/SaveAsDAISY, move the image to the output folder
	 */
	public String CheckShapeId(String id) throws IOException {
		String fileName = inputName + "-" + id + ".png";
		shapeIsExported = false;
		File expectedShapeFile = new File(outputFilename, fileName);
		if(!expectedShapeFile.exists()){
			// Check for files exported by the SaveAsDAISY Addin
			File shapesDirectory = System.getProperty("os.name").startsWith("Windows")
					? new File(new File(System.getenv("APPDATA")), "SaveAsDAISY")
					: new File("/shapes");
			File shapeFile = new File(shapesDirectory, fileName);
			if (shapeFile.exists()) {
				shapeIsExported = true;
				Files.copy(shapeFile.toPath(), expectedShapeFile.toPath());
				shapeFile.delete();
			} else {
				File tempPath = System.getProperty("os.name").startsWith("Windows")
						? new File(new File(System.getenv("APPDATA")), "Local/Temp")
						: new File("/tmp");
				shapeFile = new File(tempPath, fileName);
				if (shapeFile.exists()) {
					shapeIsExported = true;
					Files.copy(shapeFile.toPath(), expectedShapeFile.toPath());
					shapeFile.delete();
				}
			}
		}
		if(!shapeIsExported){
			LOGGER.warn(fileName + " shape was not exported - manual conversion to an image is required");
		}
		id = id.replace(" ", "_");
		return id;
	}

	public String ShapeFileName(String id) {
		return ImageProcessing.UriEscape(inputName + "-" + id + ".png");
	}

	public String CheckImage(String img) {
		File tmp; {
			try {
				tmp = new File(outputFilename, URLDecoder.decode(img, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}
		if (tmp.exists()) { // shape or image is deployed in output
			return "1";
		} else if (shapeIsExported) // shape found but could not deploy it to output
			return "0";
		else
			return "2"; // shape or image not found
	}

	/**
	 * Retrieve the next mathml for a story type.
	 *
	 @param storyType wdTextFrameStory, wdFootnotesStory or wdMainTextStory
	*/
	public String GetMathML(String storyType) {
		String strMathMl = "(math type equations are not supported)";
		return strMathMl;
	}

	/**
	 * Function Copying 2003/xp MathML Images from footnotes to output folder
	 */
	public String MathImageFootnote(String inNum) {
		try {
			PackageRelationship relationship = null;
			for (PackageRelationship searchRelation : pack.getRelationshipsByType(wordRelationshipType)) {
				relationship = searchRelation;
				break;
			}
			PackagePart mainPartxml = pack.getPart(relationship);
			PackageRelationship footrelationship = null;
			for (PackageRelationship searchRelation : mainPartxml.getRelationshipsByType(footnotesRelationshipType)) {
				footrelationship = searchRelation;
				break;
			}
			PackagePart footPartxml = mainPartxml.getRelatedPart(footrelationship);
			PackageRelationship imgRelationship = footPartxml.getRelationship(inNum);
			PackagePart imgPartxml = footPartxml.getRelatedPart(imgRelationship);
			BufferedImage img = ImageIO.read(imgPartxml.getInputStream());
			String strImgName = imgPartxml.getPartName().getURI().toString()
			                              .substring(imgPartxml.getPartName().getURI().toString().lastIndexOf('/') + 1);
			File f = new File(outputFilename, inputName + "-" + strImgName);
			if (!GetExtension(f).equals(".jpeg")
			    && !GetExtension(f).equals(".png")) {
				ImageIO.write(
					img,
					ImageFormat.Png.getFormatName(),
					new File(f.getAbsolutePath().replace(GetExtension(f), ".png")));
				strImgName = strImgName.replace(GetExtension(f), ".png");
			} else if (GetExtension(f).equals(".jpeg")) {
				ImageIO.write(
					img,
					ImageFormat.Jpeg.getFormatName(),
					new File(f.getAbsolutePath().replace(GetExtension(f), ".jpg")));
				strImgName = strImgName.replace(GetExtension(f), ".jpg");
			} else {
				ImageIO.write(img, ImageFormat.Png.getFormatName(), f);
			}
			return ImageProcessing.UriEscape(inputName + "-" + strImgName);
		} catch (Throwable e) {
			return "translation.oox2Daisy.ImageContent";
		}
	}

	/**
	 * Function Copying 2003/xp MathML Images to output folder
	 */
	public String MathImage(String inNum) {
		try {
			PackageRelationship relationship = null;
			for (PackageRelationship searchRelation : pack.getRelationshipsByType(wordRelationshipType)) {
				relationship = searchRelation;
				break;
			}
			PackagePart mainPartxml = pack.getPart(relationship);
			PackageRelationship imgRelationship = mainPartxml.getRelationship(inNum);
			PackagePart imgPartxml = mainPartxml.getRelatedPart(imgRelationship);
			BufferedImage img = ImageIO.read(imgPartxml.getInputStream());
			String strImgName = imgPartxml.getPartName().getURI().toString()
			                              .substring(imgPartxml.getPartName().getURI().toString().lastIndexOf('/') + 1);
			File f = new File(outputFilename, inputName + "-" + strImgName);
			if (!GetExtension(f).equals(".jpeg")
			    && !GetExtension(f).equals(".png")) {
				ImageIO.write(
					img,
					ImageFormat.Png.getFormatName(),
					new File(f.getAbsolutePath().replace(GetExtension(f), ".png")));
				strImgName = strImgName.replace(GetExtension(f), ".png");
			} else {
				ImageIO.write(
					img,
					GetExtension(f).substring(1),
					f);
			}
			return ImageProcessing.UriEscape(inputName + "-" + strImgName);
		} catch (Throwable e) {
			return "translation.oox2Daisy.ImageContent";
		}
	}

	/**
	 * Function to get the image file names for Office Word 2007
	 * Function to get the image file names for Office Word 2003 and Grouped Images
	 */
	public String Image(String inNum, String imageName) {
		try {
			PackageRelationship relationship = null;
			for (PackageRelationship searchRelation : pack.getRelationshipsByType(wordRelationshipType)) {
				relationship = searchRelation;
				break;
			}
			PackagePart mainPartxml = pack.getPart(relationship);
			PackageRelationship imgRelationship = mainPartxml.getRelationship(inNum);
			PackagePart imgPartxml = mainPartxml.getRelatedPart(imgRelationship);
			ImageIO.read(imgPartxml.getInputStream());
			/* Checking if full filename (along with extn) of the image exists */
			if (imageName.lastIndexOf('.') >= 0) {
				String strImgName = imageName;
				String retimg2007Name = ImageExt(
					inNum,
					new File(outputFilename, inputName + "-" + strImgName),
					inputName + "-" + strImgName);
				return ImageProcessing.UriEscape(retimg2007Name);
			}
			/* Checking if full filename (along with extn) of the image does not exist */
			else if (!imageName.isEmpty()) {
				String strImgName = imgPartxml.getPartName().getURI().toString()
					                          .substring(imgPartxml.getPartName().getURI().toString().lastIndexOf('.') + 1);
				String img2007Name = imageName + "." + strImgName;
				String retimg2007Name = ImageExt(
					inNum,
					new File(outputFilename, inputName + "-" + img2007Name),
					inputName + "-" + img2007Name);
				return ImageProcessing.UriEscape(retimg2007Name);
			}
			/* Checking if entire filename of the image doesn't exist */
			else {
				String strImgName = imgPartxml.getPartName().getURI().toString()
					                          .substring(imgPartxml.getPartName().getURI().toString().lastIndexOf('/') + 1);
				String retimg2007Name = ImageExt(
					inNum,
					new File(outputFilename, inputName + "-" + strImgName),
					inputName + "-" + strImgName);
				return ImageProcessing.UriEscape(retimg2007Name);
			}
		} catch (Throwable e) {
			return "translation.oox2Daisy.ImageContent";
		}
	}

	/**
	 * Function to resample images in the document according to daisy settings
	 */
	public String ResampleImage(String inNum, String imageName, float resampleValue)
			throws IOException, InvalidFormatException {
		PackageRelationship relationship = null;
		for (PackageRelationship searchRelation : pack.getRelationshipsByType(wordRelationshipType)) {
			relationship = searchRelation;
			break;
		}
		PackagePart mainPartxml = pack.getPart(relationship);
		PackageRelationship imgRelationship = mainPartxml.getRelationship(inNum);
		PackagePart imgPartxml = mainPartxml.getRelatedPart(imgRelationship);
		String srcName = GetFileNameWithoutExtension(new File(outputFilename, imageName));
		String srcFormat = imgPartxml.getPartName().getURI().toString()
		                             .substring(imgPartxml.getPartName().getURI().toString().lastIndexOf('.') + 1);
		BufferedImage srcImg = ImageIO.read(imgPartxml.getInputStream());
		BufferedImage resampledImg = ImageProcessing.ResampleImage(srcImg, resampleValue);
		return ImageProcessing.SaveProcessedImage(resampledImg, outputFilename, srcName, srcFormat);
	}

	/**
	 * Function to Copy image to the Output/destination folder
	 */
	private String ImageExt(String inNum, File pathName, String imageName)
			throws IOException, InvalidFormatException {
		PackageRelationship relationship = null;
		for (PackageRelationship searchRelation : pack.getRelationshipsByType(wordRelationshipType)) {
			relationship = searchRelation;
			break;
		}
		PackagePart mainPartxml = pack.getPart(relationship);
		PackageRelationship imgRelationship = mainPartxml.getRelationship(inNum);
		PackagePart imgPartxml = mainPartxml.getRelatedPart(imgRelationship);
		BufferedImage img = ImageIO.read(imgPartxml.getInputStream());
		/* Checking if filename extension is not .jpeg, .jpg, .jpe,.png and converting it to filename with .png extn */
		if (!GetExtension(pathName).equals(".jpeg")
		    && !GetExtension(pathName).equals(".jpe")
		    && !GetExtension(pathName).equals(".jpg")
		    && !GetExtension(pathName).equals(".JPEG")
		    && !GetExtension(pathName).equals(".JPE")
		    && !GetExtension(pathName).equals(".JPG")
		    && !GetExtension(pathName).equals(".png")
		    && !GetExtension(pathName).equals(".PNG")) {
			ImageIO.write(
				img,
				ImageFormat.Png.getFormatName(),
				new File(pathName.getAbsolutePath().replace(GetExtension(pathName), ".png")));
			return imageName.replace(GetExtension(pathName), ".png");
		}
		/* Checking if filename extension is .jpeg, .jpg, .jpe and converting it to filename with .jpg extn */
		else if (GetExtension(pathName).equals(".jpeg")
		         || GetExtension(pathName).equals(".jpg")
		         || GetExtension(pathName).equals(".jpe")
		         || GetExtension(pathName).equals(".JPE")
		         || GetExtension(pathName).equals(".JPEG")
		         || GetExtension(pathName).equals(".JPG")) {
			ImageIO.write(
				img,
				ImageFormat.Jpeg.getFormatName(),
				new File(pathName.getAbsolutePath().replace(GetExtension(pathName), ".jpg")));
			return imageName.replace(GetExtension(pathName), ".jpg");
		}
		/* If filename extension is .png it is returned as it is */
		else {
			ImageIO.write(img, ImageFormat.Png.getFormatName(), pathName);
			return imageName;
		}
	}

	/**
	 * Function to check Document is having external images or not
	 */
	public String ExternalImage() throws InvalidFormatException {
		PackageRelationship relationship = null;
		for (PackageRelationship searchRelation : pack.getRelationshipsByType(wordRelationshipType)) {
			relationship = searchRelation;
			break;
		}
		PackagePart mainPartxml = pack.getPart(relationship);
		for (PackageRelationship searchRelation : mainPartxml.getRelationships()) {
			if ("http://schemas.openxmlformats.org/officeDocument/2006/relationships/image"
			    .equals(searchRelation.getRelationshipType())) {
				if (searchRelation.getTargetMode() == TargetMode.EXTERNAL) {
					strImageExt = "translation.oox2Daisy.ExternalImage";
					break;
				}
			}
			strImageExt = "no external image";
		}
		return strImageExt;
	}

	/**
	 * Function to Push current document level value to the general stack
	 */
	public int PushLevel(int level) {
		if (!stackList.empty()) {
			if (level > 6 && stackList.size() == 6) {
				stackList.push(headingSixLvl);
			} else if (level != stackList.peek() + 1)
			{
				level = stackList.peek() + 1;
				stackList.push(level);
			} else {
				stackList.push(level);
			}
		} else {
			if (level != headingOneLvl) {
				level = headingOneLvl;
				stackList.push(level);
			} else {
				stackList.push(level);
			}
		}
		return level;
	}

	/**
	 * peek (no suppresion) the last level of the document from the stack
	 */
	public int PeekLevel() {
		if (!stackList.empty()) {
			return stackList.peek();
		} else {
			return headingZeroLvl;
		}
	}

	/**
	 * pop (retrieve and delete) the last level of the document from the stack
	 */
	public int PopLevel() {
		return stackList.pop();
	}

	/**
	 * Increment and return as a string the pageToc counter (number of pages before the toc)
	 */
	public int PageForTOC() {
		pageToc++;
		return pageToc;
	}

	/**
	 * Function which returns the number of Sections in the document
	 */
	public String SectionCounter(String pageType, String pageStart) {
		if (pageType == null || "".equals(pageType)) {
			sectionPagetype = "decimal";
		} else {
			sectionPagetype = pageType;
		}
		if (pageStart == null || "".equals(pageStart)) {
			pageNum = 1;
		} else {
			pageNum = Integer.parseInt(pageStart);
		}
		sectionCounter++;
		return "" + sectionCounter;
	}

	/**
	 * Function which returns the type of page format
	 */
	public String GetPageFormat() {
		if (sectionPagetype == null || "".equals(sectionPagetype)) {
			return "decimal";
		} else {
			return sectionPagetype;
		}
	}

	/**
	 * Function which returns the Page Number
	 */
	public int GetPageNum() {
		return pageNum;
	}

	/**
	 * Function which returns the Section value for the first page
	 */
	public int GetSectionPageStart() {
		sectionpageStart++;
		return sectionpageStart;
	}

	/**
	 * Function which initializes the Section value for page Start
	 */
	public String InitalizeSectionPageStart() {
		sectionpageStart = 0;
		return "" + sectionpageStart;
	}

	/**
	 * Function which increments the TOC counter
	 */
	public int CheckTocOccur() {
		chekTocOccur++;
		return chekTocOccur;
	}

	/**
	 * Function which increments the Section counter
	 */
	public int CheckSection() {
		checkSection++;
		return checkSection;
	}

	/**
	 * Function which initializes the Section value
	 */
	public String InitalizeCheckSection() {
		checkSection = 0;
		return "" + checkSection;
	}

	/**
	 * Function which increments the Section counter for BODY
	 */
	public int CheckSectionBody() {
		checkSectionBody++;
		return checkSectionBody;
	}

	/**
	 * Function which initializes the Section counter for BODY
	 */
	public String InitalizeCheckSectionBody() {
		checkSectionBody = 0;
		return "" + checkSectionBody;
	}

	/**
	 * Function which increments the page number
	 */
	public int IncrementPageNo() {
		pageNum++;
		return pageNum;
	}

	/**
	 * Function which returns page Id
	 */
	public String GeneratePageId() {
		pageId++;
		return "" + pageId;
	}

	/**
	 * Function which increments page occurence counter
	 */
	public String IncrementPage() {
		incrementPage++;
		return "" + incrementPage;
	}

	/**
	 * Function which returns page number
	 */
	public int ReturnPageNum() {
		return incrementPage;
	}

	/**
	 * Function which increments Section counter in front matter
	 */
	public String CheckSectionFront() {
		checkSectionFront++;
		return "" + checkSectionFront;
	}

	/**
	 * Function which returns Section counter in front matter
	 */
	public int GetSectionFront() {
		return checkSectionFront;
	}

	/**
	 * Store a footnote id found in content.
     * Also store its current level.
	 */
	public int AddFootNote(int inNum) {
		notesIdsQueue.add(inNum);
		notesLevelsQueue.add(PeekLevel());
		return inNum;
	}

	/**
	 * Function to get Value of a particular Footnote.
	 * NP : Only used as FootNoteId(0) to get the first footnote to
	 * insert in content.
	 *
	 * @param i index of the footnote
	 * @param level level of the footnote
	 *
	 */
	public int FootNoteId(int i, int level) {
		if (notesIdsQueue.size() > 0) {
			// Search next footnote to include by checking if we are at the good level
			// (we could be in a level3 while having a level2 notes as first to insert
			// if we are inserting notes at end of levels)

			while (
				i < notesIdsQueue.size() &&
				(level > this.notesLevelsQueue.get(i))
			) {
				i++;
			}
			if (i >= notesIdsQueue.size()) return 0;
			int id = notesIdsQueue.get(i);
			notesIdsQueue.remove(i);
			notesLevelsQueue.remove(i);
			return id;
		} else {
			return 0;
		}
	}

	/**
	 * Function which returns the flag regarding Footnotes
	 */
	public int NoteFlag() {
		noteFlag++;
		return noteFlag;
	}

	/**
	 * Function which initializes the flag regarding Footnotes
	 */
	public String InitializeNoteFlag() {
		noteFlag = 0;
		return "" + noteFlag;
	}

	/**
	 * Function to add Caption and Prodnote to an Array
	 */
	public int AddCaptionsProdnotes() {
		tmpcount++;
		arrCaptionProdnote.add(tmpcount);
		return tmpcount;
	}

	/**
	 * Function to get Caption and Prodnote from an Array
	 */
	public int GetCaptionsProdnotes() {
		if (arrCaptionProdnote.size() > 0) {
			return arrCaptionProdnote.remove(0);
		} else {
			return 0;
		}
	}

	/**
	 * Function to reset the count value used in the AddCaptionsProdnotes function
	 */
	public void ResetCaptionsProdnotes() {
		tmpcount = 0;
	}

	/**
	 * Function to Push a level value to the stack
	 * (only if the level is superior to the last one in stack)
	 * Used in Common2.xsl:411
	 */
	public int ListPush(int i) {
		String j = "" + i;
		if (lstackList.size() > 0) {
			if (i > Integer.parseInt(lstackList.peek())) {
				j = lstackList.peek();
				lstackList.push("" + i);
			}
		} else {
			lstackList.push("" + i);
		}
		return Integer.parseInt(j);
	}

	/**
	 * Function to Peek the top value of the Stack
	 */
	public int ListPeekLevel() {
		if (lstackList.size() > 0) {
			return Integer.parseInt(lstackList.peek());
		} else {
			return 0;
		}
	}

	/**
	 * Function to PoP the top value of the Stack
	 */
	public int ListPopLevel() {
		if (lstackList.size() > 0)
			return Integer.parseInt(lstackList.pop());
		else
			return 0;
	}

	/**
	 * Function to generate a new ID for image
	 * (increment the id counter)
	 */
	public int GenerateImageId() {
	   return ++imgId;
	}

	/**
	 * Function to insert Caption for an Image
	 */
	public String InsertCaption(String captionImg) {
		caption = captionImg;
		return caption;
	}

	/**
	 * Function returs caption
	 */
	public String ReturnCaption() {
		if (caption == null || "".equals(caption)) {
			return "0";
		} else {
			return caption;
		}
	}

	public int GetCheckLvlInt(Iterator<Node> checkLvl) {
		if (checkLvl.hasNext()) {
			return Integer.parseInt(checkLvl.next().getTextContent());
		} else {
			return 0;
		}
	}

	/**
	 * Function which increments the counter value
	 */
	public void Increment(int rec) {
		for (int i = 0; i <= rec; i++)
			ListPush(i);
	}

	/**
	 * Function returns the target string of an anchor
	 */
	public String Anchor(String inNum, String flagNote) throws InvalidFormatException {
		PackageRelationship wordRelationship = null;
		for (PackageRelationship searchRelation : pack.getRelationshipsByType(wordRelationshipType)) {
			wordRelationship = searchRelation;
			break;
		}
		PackagePart mainPartxml = null;// = pack.getPart(relationship);
		PackageRelationship anchorRelationshipFile = null;// = mainPartxml.getRelationship(inNum);
		String uri;
		switch (flagNote){
			case "footnote":
				mainPartxml = pack.getPart(wordRelationship);
				try{
					for (PackageRelationship searchRelation : mainPartxml.getRelationshipsByType(footnotesRelationshipType)) {
						anchorRelationshipFile = searchRelation;
						break;
					}
				} catch (Exception e){
					return "";
				}
				break;
			case "endnote":
				mainPartxml = pack.getPart(wordRelationship);
				try{
					for (PackageRelationship searchRelation : mainPartxml.getRelationshipsByType(endnotesRelationshipType)) {
						anchorRelationshipFile = searchRelation;
						break;
					}
				} catch (Exception e){
					return "";
				}
				break;
			default:
				anchorRelationshipFile = wordRelationship;
				break;
		}

		if(anchorRelationshipFile != null){
			PackageRelationship anchorRelationship;
			mainPartxml = pack.getPart(anchorRelationshipFile);
			if(mainPartxml == null){
				// fallback for footnotes and endnotes where the pack does
				// resolve correctly the underlying file
				mainPartxml = anchorRelationshipFile.getSource().getRelatedPart(anchorRelationshipFile);
			}
			anchorRelationship = mainPartxml.getRelationship(inNum);

			uri = anchorRelationship.getTargetURI().toString();
			// don't encode apos
			uri = uri.replaceAll("%27", "'");

			// reencode ampersand if not already encoded
			if(uri.contains("&") && !uri.contains("&amp;")){
				uri = uri.replaceAll("&", "&amp;");
			}
			// normalize: change hex values to lowercase
			if (uri.contains("%")) {
				Matcher m = Pattern.compile("%..").matcher(uri);
				StringBuffer sb = new StringBuffer();
				while (m.find())
					m.appendReplacement(sb, m.group(0).toLowerCase());
				m.appendTail(sb);
				uri = sb.toString();
			}
			return uri;
		}
		return "";

	}

	/**
	 * Function which concatenate messages for TOC
	 */
	public void SetTOCMessage(String str) {
		message = message + " " + str;
	}

	/**
	 * Function which return message for TOC
	 */
	public String GetTOCMessage() {
		return message;
	}

	/**
	 * Function which returns Null message for TOC
	 */
	public void NullMsg() {
		message = "";
	}

	/**
	 * Function which increments the counter for TOC
	 */
	public int Set_tabToc() {
		set_tabToc++;
		return set_tabToc;
	}

	/**
	 * Function which resets the counter value for TOC
	 */
	public String Get_tabToc() {
		set_tabToc = 0;
		return "" + set_tabToc;
	}

	/**
	 * Function which increments the counter for TOC
	 */
	public int Set_Toc() {
		set_Toc++;
		return set_Toc;
	}

	/**
	 * Function which resets the counter value for TOC
	 */
	public int Get_Toc() {
		set_Toc = 0;
		return set_Toc;
	}

	/**
	 * Function which sets the counter value for Continuous page break
	 */
	public int SetConPageBreak() {
		conPageBreak++;
		return conPageBreak;
	}

	/**
	 * Function which Resetsets the counter value for Continuous page break
	 */
	public String ResetSetConPageBreak() {
		conPageBreak = 0;
		return "" + conPageBreak;
	}

	/**
	 * Functions to Implement Citation
	 */
	public String Citation(XPath xpath, DocumentBuilder docBuilder)
			throws InvalidFormatException, SAXException, IOException, XPathExpressionException {
		String indicator = " ";
		PackageRelationship relationship = null;
		for (PackageRelationship searchRelation : pack.getRelationshipsByType(wordRelationshipType)) {
			relationship = searchRelation;
			break;
		}
		PackagePart mainPartxml = pack.getPart(relationship);
		for (PackageRelationship searchRelation : mainPartxml.getRelationshipsByType(CustomRelationshipType)) {
			PackagePart customPartxml = mainPartxml.getRelatedPart(searchRelation);
			Document doc = docBuilder.parse(customPartxml.getInputStream());
			if ("http://schemas.openxmlformats.org/officeDocument/2006/bibliography"
			    .equals(doc.getDocumentElement().getNamespaceURI())) {
				NodeList node = (NodeList)compileXPathExpression(
					xpath,
					"//b:Sources/@StyleName",
					ImmutableMap.of("b", "http://schemas.openxmlformats.org/officeDocument/2006/bibliography"))
					.evaluate(doc, XPathConstants.NODESET);
				if (node != null && node.getLength() != 0) {
					indicator = node.item(0).getTextContent();
				} else
					indicator = " ";
			}
		}
		return indicator;
	}

	/**
	 * Function returns details(title,author,year) of a citation
	 */
	public String CitationDetails(String citeId, XPath xpath, DocumentBuilder docBuilder)
			throws InvalidFormatException, XPathExpressionException, DOMException, SAXException, IOException {
		String temp = "";
		PackageRelationship relationship = null;
		for (PackageRelationship searchRelation : pack.getRelationshipsByType(wordRelationshipType)) {
			relationship = searchRelation;
			break;
		}
		PackagePart mainPartxml = pack.getPart(relationship);
		for (PackageRelationship searchRelation : mainPartxml.getRelationshipsByType(CustomRelationshipType)) {
			PackagePart customPartxml = mainPartxml.getRelatedPart(searchRelation);
			Document doc = docBuilder.parse(customPartxml.getInputStream());
			if ("http://schemas.openxmlformats.org/officeDocument/2006/bibliography"
			    .equals(doc.getDocumentElement().getNamespaceURI())) {
				NodeList list = (NodeList)compileXPathExpression(
					xpath,
					"//b:Source/b:Tag",
					ImmutableMap.of("b", "http://schemas.openxmlformats.org/officeDocument/2006/bibliography"))
					.evaluate(doc, XPathConstants.NODESET);
				for (int i = 0; i < list.getLength(); i++) {
					if (citeId.contains(list.item(i).getTextContent())) {
						NodeList getTitle1 = (NodeList)compileXPathExpression(
							xpath,
							"b:Title",
							ImmutableMap.of("b", "http://schemas.openxmlformats.org/officeDocument/2006/bibliography"))
							.evaluate(list.item(i).getParentNode(), XPathConstants.NODESET);
						if (getTitle1 != null && getTitle1.getLength() != 0)
							getTitle = getTitle1.item(0).getTextContent();
						NodeList getYear1 = (NodeList)compileXPathExpression(
							xpath,
							"b:Year",
							ImmutableMap.of("b", "http://schemas.openxmlformats.org/officeDocument/2006/bibliography"))
							.evaluate(list.item(i).getParentNode(), XPathConstants.NODESET);
						if (getYear1 != null && getYear1.getLength() != 0)
							getYear = getYear1.item(0).getTextContent();
						NodeList listAuthor = (NodeList)compileXPathExpression(
							xpath,
							"b:Author/b:Author/b:NameList//b:Person/b:Last",
							ImmutableMap.of("b", "http://schemas.openxmlformats.org/officeDocument/2006/bibliography"))
							.evaluate(list.item(i).getParentNode(), XPathConstants.NODESET);
						if (listAuthor != null) {
							for (int j = 0; j < listAuthor.getLength(); j++) {
								temp = temp + " " + listAuthor.item(j).getTextContent();
							}
							getAuthor = temp;
						}
					}
				}
			}
		}
		return " ";
	}

	/**
	 * Function returns Author name of a citation
	 */
	public String GetAuthor() {
		return getAuthor;
	}

	/**
	 * Function returns Title of a citation
	 */
	public String GetTitle() {
		return getTitle;
	}

	/**
	 * Function returns Year of the citation
	 */
	public String GetYear() {
		return getYear;
	}

	/**
	 * Function to implement Language
	 */
	public int AddLanguage(String lang) {
		if (arrListLang.contains(lang))
			return 0;
		else {
			arrListLang.add(lang);
			return 1;
		}
	}

	/**
	 * Functions for HyperLink
	 */
	public String AddHyperlink(String name) {
		if (name == null || "".equals(name)) {
			name = "";
		} else {
			arrHyperlink.add(name);
		}
		return name;
	}

	/**
	 * Function to check whether given name is hyperlink or not
	 */
	public int GetHyperlinkName(String name) {
		int flag = 0;
		if ((name == null || "".equals(name)) && arrHyperlink.size() == 0) {
			flag = 0;
		} else if (arrHyperlink.size() > 0) {
			for (int count = 0; count <= arrHyperlink.size() - 1; count++) {
				if (name.equals(arrHyperlink.get(count))) {
					flag = 1;
				}
			}
		}
		return flag;
	}

	/**
	 * Function to increments flag value for Hyperlink
	 */
	public int SetHyperLinkFlag() {
		setHyperLinkFlag++;
		return setHyperLinkFlag;
	}

	/**
	 * Function to set flag for Hyperlink
	 */
	public String SetGetHyperLinkFlag() {
		setHyperLinkFlag = 0;
		return "" + setHyperLinkFlag;
	}

	/**
	 * Function to check whether hyperlink is coming is different runs or not
	 */
	public String TestRun() {
		testRun++;
		return "" + testRun;
	}

	/**
	 * Function to get the flag value for Hyperlink
	 */
	public String GetTestRun() {
		return "" + testRun;
	}

	/**
	 * Function to set the flag value for Hyperlink
	 */
	public String SetTestRun() {
		testRun = 0;
		return "" + testRun;
	}

	/**
	 * Function to store ID
	 */
	public String StroreId(String id) {
		storeHyperId = id;
		return id;
	}

	/**
	 * Function to check whether given Id is there or not
	 */
	public int CheckId(String id) {
		if (id.equals(storeHyperId))
			return 1;
		else
			return 0;
	}

	/**
	 * Function to increment flag for Hyperlink
	 */
	public int SetHyperLink() {
		set++;
		return set;
	}

	/**
	 * Function to set flag for Hyperlink
	 */
	public int GetHyperLink() {
		set = 0;
		return set;
	}

	/**
	 * Function to get flag value for Hyperlink
	 */
	public int GetFlag() {
		return set;
	}

	/**
	 * Function to increment flag value for Bookmark
	 */
	public int SetBookmark() {
		setbookmark++;
		return setbookmark;
	}

	/**
	 * Function to get flag value for Bookmark
	 */
	public int GetBookmark() {
		return setbookmark;
	}

	/*** Function to set flag value for Bookmark */
	public int AssingBookmark() {
		setbookmark = 0;
		return setbookmark;
	}

	/**
	 * Function which returns whether the BookmarkEnd related to Abbreviation or Acronym in document.xml
	 */
	public String Book(String id) {
		if (id.startsWith("Abbreviations")) {
			return "AbbrTrue";
		} else if (id.startsWith("Acronyms")) {
			return "AcrTrue";
		} else {
			return "false";
		}
	}

	/**
	 * Function which returns whether the BookmarkEnd related to Abbreviation or Acronym in footnote.xml/endnote.xml
	 */
	public String BookFootnote(String id, XPath xpath, DocumentBuilder docBuilder)
			throws InvalidFormatException, SAXException, IOException, XPathExpressionException {
		NodeList node;
		PackageRelationship relationship = null;
		for (PackageRelationship searchRelation : pack.getRelationshipsByType(wordRelationshipType)) {
			relationship = searchRelation;
			break;
		}
		PackagePart mainPartxml = pack.getPart(relationship);
		PackageRelationship footrelationship = null;
		for (PackageRelationship searchRelation : mainPartxml.getRelationshipsByType(footnotesRelationshipType)) {
			footrelationship = searchRelation;
			break;
		}
		PackagePart footPartxml = mainPartxml.getRelatedPart(footrelationship);
		Document doc = docBuilder.parse(footPartxml.getInputStream());
		node = (NodeList)compileXPathExpression(
			xpath,
			"//w:bookmarkStart[@w:id='" + id + "']",
			ImmutableMap.of("w", wordNamespace))
			.evaluate(doc, XPathConstants.NODESET);
		if (node.getLength() == 0) {
			PackageRelationship endrelationship = null;
			for (PackageRelationship searchRelation : mainPartxml.getRelationshipsByType(endnotesRelationshipType)) {
				endrelationship = searchRelation;
				break;
			}
			PackagePart endPartxml = mainPartxml.getRelatedPart(endrelationship);
			Document doc1 = docBuilder.parse(endPartxml.getInputStream());
			node = (NodeList)compileXPathExpression(
				xpath,
				"//w:bookmarkStart[@w:id='" + id + "']",
				ImmutableMap.of("w", wordNamespace))
				.evaluate(doc1, XPathConstants.NODESET);
		}
		if (node.getLength() != 0) {
			if (node.item(0).getAttributes().getNamedItemNS(wordNamespace, "name").getTextContent().startsWith("Abbreviations")) {
				return "AbbrTrue";
			} else if (node.item(0).getAttributes().getNamedItemNS(wordNamespace, "name").getTextContent().startsWith("Acronyms")) {
				return "AcrTrue";
			}
		}
		return "false";
	}

	/**
	 * Function which returns Full form of an Abbreviation
	 */
	public String FullAbbr(String abbrName, String version, XPath xpath, DocumentBuilder docBuilder)
			throws SAXException, IOException, XPathExpressionException, InvalidFormatException, DOMException {
		String indicator = "";
		PackageRelationship relationship = null;
		if (IsOffice2007Or2010(version)) {
			for (PackageRelationship searchRelation : pack.getRelationshipsByType(wordRelationshipType)) {
				relationship = searchRelation;
				break;
			}
			PackagePart mainPartxml = pack.getPart(relationship);
			for (PackageRelationship searchRelation : mainPartxml.getRelationshipsByType(CustomRelationshipType)) {
				PackagePart customPartxml = mainPartxml.getRelatedPart(searchRelation);
				Document doc = docBuilder.parse(customPartxml.getInputStream());
				if ("http://Daisy-OpenXML/customxml".equals(doc.getDocumentElement().getNamespaceURI())) {
					NodeList node = (NodeList)compileXPathExpression(
						xpath,
						"//a:Item[@AbbreviationName='" + abbrName + "']",
						ImmutableMap.of("a", "http://Daisy-OpenXML/customxml"))
						.evaluate(doc, XPathConstants.NODESET);
					if (node != null) {
						if (node.getLength() != 0) {
							if (!node.item(0).getAttributes().getNamedItem("FullAbbr").getTextContent().equals(""))
								indicator = node.item(0).getAttributes().getNamedItem("FullAbbr").getTextContent();
						} else
							indicator = "";
						break;
					}
				}
			}
		}
		if (version.equals(version2003) || version.equals(versionXP)) {
			for (PackageRelationship searchRelation : pack.getRelationshipsByType(customPropRelationshipType)) {
				relationship = searchRelation;
				break;
			}
			if (relationship != null) {
				PackagePart mainPartxml = pack.getPart(relationship);
				Document doc = docBuilder.parse(mainPartxml.getInputStream());
				NodeList node = doc.getFirstChild().getNextSibling().getChildNodes();
				if (node != null) {
					for (int i = 0; i < node.getLength(); i++) {
						if (node.item(i).getAttributes().getNamedItem("name").getTextContent().equals(abbrName)) {
							if (!node.item(i).getFirstChild().getTextContent().equals("")) {
								String temp = node.item(i).getFirstChild().getTextContent();
								String input = temp.replace("$#$", "-");
								String[] strKey = input.split("-");
								indicator = strKey[1];
							} else
								indicator = "";
						}
					}
				}
			}
		}
		return indicator;
	}

	/**
	 * Function which returns Full form of an Acronym
	 */
	public String FullAcr(String acrName, String version, XPath xpath, DocumentBuilder docBuilder)
			throws InvalidFormatException, XPathExpressionException, DOMException, SAXException, IOException {
		String indicator = "";
		PackageRelationship relationship = null;
		if (IsOffice2007Or2010(version)) {
			for (PackageRelationship searchRelation : pack.getRelationshipsByType(wordRelationshipType)) {
				relationship = searchRelation;
				break;
			}
			PackagePart mainPartxml = pack.getPart(relationship);
			for (PackageRelationship searchRelation : mainPartxml.getRelationshipsByType(CustomRelationshipType)) {
				PackagePart customPartxml = mainPartxml.getRelatedPart(searchRelation);
				Document doc = docBuilder.parse(customPartxml.getInputStream());
				if ("http://Daisy-OpenXML/customxml".equals(doc.getDocumentElement().getNamespaceURI())) {
					NodeList node = (NodeList)compileXPathExpression(
						xpath,
						"//a:Item[@AcronymName='" + acrName + "']",
						ImmutableMap.of("a", "http://Daisy-OpenXML/customxml"))
						.evaluate(doc, XPathConstants.NODESET);
					if (node.getLength() != 0) {
						if (!node.item(0).getAttributes().getNamedItem("FullAcr").getTextContent().equals(""))
							indicator = node.item(0).getAttributes().getNamedItem("FullAcr").getTextContent();
					} else
						indicator = "";
					break;
				}
			}
		}
		if (version == version2003 || version == versionXP) {
			for (PackageRelationship searchRelation : pack.getRelationshipsByType(customPropRelationshipType)) {
				relationship = searchRelation;
				break;
			}
			if (relationship != null) {
				PackagePart mainPartxml = pack.getPart(relationship);
				Document doc = docBuilder.parse(mainPartxml.getInputStream());
				NodeList node = doc.getFirstChild().getNextSibling().getChildNodes();
				if (node != null) {
					for (int i = 0; i < node.getLength(); i++) {
						if (node.item(i).getAttributes().getNamedItem("name").getTextContent().equals(acrName)) {
							if (!node.item(i).getFirstChild().getTextContent().equals("")) {
								String temp = node.item(i).getFirstChild().getTextContent();
								String input = temp.replace("$#$", "-");
								String[] strKey = input.split("-");
								indicator = strKey[1];
							} else
								indicator = "";
						}
					}
				}
			}
		}
		return indicator;
	}

	/**
	 * Function used to set the Abbreviations/Acronyms flag
	 */
	public String SetAbbrAcrFlag() {
		AbbrAcrflag = 1;
		return "1";
	}

	/**
	 * Function used to reset the Abbreviations/Acronyms flag
	 */
	public String ReSetAbbrAcrFlag() {
		AbbrAcrflag = 0;
		return "0";
	}

	/**
	 * Function used to return the Abbreviations/Acronyms flag value
	 */
	public int AbbrAcrFlag() {
		return AbbrAcrflag;
	}

	/**
	 * Function used to push an abbreviation to stack
	 */
	public String PushAbrAcr(String element) {
		abbrstackList.push(element);
		return "1";
	}

	/**
	 * Function used to push the given element into stack
	 */
	public String PushAbrAcrpara(String element) {
		abbrparastackList.push(element);
		return "1";
	}

	/**
	 * Function used to push the given element into stack
	 */
	public String PushAbrAcrhead(String element) {
		abbrheadstackList.push(element);
		return "1";
	}

	/**
	 * Function used to peek an element from the stack
	 */
	public String PeekAbrAcr() {
		return abbrstackList.pop();
	}

	/**
	 * Function used to peek an element from the stack
	 */
	public String PeekAbrAcrpara() {
		return abbrparastackList.pop();
	}

	/**
	 * Function used to peek an element from the stack
	 */
	public String PeekAbrAcrhead() {
		return abbrheadstackList.pop();
	}

	/**
	 * Function which returns the count value
	 */
	public int CountAbrAcr() {
		return abbrstackList.size();
	}

	/**
	 * Function which returns the count value
	 */
	public int CountAbrAcrpara() {
		return abbrparastackList.size();
	}

	/**
	 * Function which returns the count value
	 */
	public int CountAbrAcrhead() {
		return abbrheadstackList.size();
	}

	/**
	 * Function to return Rowspan value of a table
	 */
	public String Rowspan() {
		rowspan++;
		return "" + rowspan;
	}

	/**
	 * Function to set Rowspan value of a table
	 */
	public String SetRowspan() {
		rowspan = 0;
		return "" + rowspan;
	}

	/**
	 * Function to return Rowspan value of a table
	 */
	public int GetRowspan() {
		return rowspan;
	}

	public String GetFlagRowspan() {
		flagRowspan++;
		return "" + flagRowspan;
	}

	public String SetFlagRowspan() {
		flagRowspan = 0;
		return "" + flagRowspan;
	}

	public int ReturnFlagRowspan() {
		return flagRowspan;
	}

	/**
	 * Function to increment flag value for BDO
	 *
	 * @return the new value
	 */
	public int SetbdoFlag() {
		bdoflag++;
		return bdoflag;
	}

	/**
	 * Function to set flag value for BDO
	 *
	 * @return the previous value
	 */
	public int reSetbdoFlag() {
		int r = bdoflag;
		bdoflag = 0;
		return r;
	}

	/**
	 * Function to increment flag value for caption
	 */
	public int SetcaptionFlag() {
		captionFlag++;
		return captionFlag;
	}

	/**
	 * Function to set flag value for caption
	 */
	public String reSetcaptionFlag() {
		captionFlag = 0;
		return "" + captionFlag;
	}

	/**
	 * Function to increment flag value for Cover page
	 */
	public int CheckCoverPage() {
		checkCverpage++;
		return checkCverpage;
	}

	/**
	 * Function to increment flag value for cover page
	 */
	public String CodeFlag() {
		codeFlag++;
		return "" + codeFlag;
	}

	/**
	 * Function to returns flag value for cover page
	 */
	public String GetCodeFlag() {
		return "" + codeFlag;
	}

	/**
	 * Function to set flag value for cover page
	 */
	public String InitializeCodeFlag() {
		codeFlag = 0;
		return "" + codeFlag;
	}

	/**
	 * Function to set flag value for Heading
	 */
	public int ListHeadingFlag() {
		return listHeadingFlag;
	}

	/**
	 * Function used to push the given element into stack
	 */
	public String PushListHeading(String element) {
		listHeadingstackList.push(element);
		return "1";
	}

	/**
	 * Function used to peek an element from the stack
	 */
	public String PeekListHeading() {
		String temp = "";
		while (listHeadingstackList.size() > 0) {
			temp = listHeadingstackList.pop() + temp;
		}
		return temp;
	}

	/**
	 * Function used to set the Abbreviations/Acronyms flag
	 */
	public String SetListHeadingFlag() {
		listHeadingFlag = 1;
		return "1";
	}

	/**
	 * Function used to reset the Abbreviations/Acronyms flag
	 */
	public String ReSetListHeadingFlag() {
		listHeadingFlag = 0;
		return "0";
	}

	/**
	 * Function used to set the Linenumber flag
	 */
	public String Setlinenumflag() {
		linenumflag = 1;
		return "1";
	}

	/**
	 * Function used to get the Linenumber flag value
	 */
	public int Getlinenumflag() {
		return linenumflag;
	}

	/**
	 * Function used to reset the Linenumber flag
	 */
	public String Resetlinenumflag() {
		linenumflag = 0;
		return "0";
	}

	/**
	 * Function used to get the masterSub flag
	 */
	public int ListMasterSubFlag() {
		return listMasterSubFlag;
	}

	/**
	 * Function used to push the given element into stack
	 */
	public String PushMasterSubdoc(String element) {
		masteSubstackList.push(element);
		return "1";
	}

	/**
	 * Function used to peek an element from the stack
	 */
	public String PeekMasterSubdoc() {
		String temp = "";
		while (masteSubstackList.size() > 0) {
			temp = masteSubstackList.pop() + temp;
		}
		return temp;
	}

	/**
	 * Function used to set the MultipleOOXML flag
	 */
	public String MasterSubSetFlag() {
		listMasterSubFlag = 1;
		return "1";
	}

	/**
	 * Function used to reset the MultipleOOXML flag
	 */
	public String MasterSubResetFlag() {
		listMasterSubFlag = 0;
		return "0";
	}

	/**
	 * Function used to close levels for MultipleOOXML
	 */
	public String ClosingMasterSub(int value) {
		String output = "";
		for (int i = value; i >= 1; i--) {
			output = output + "</level" + i + ">";
		}
		output = "<p/>" + output;
		return output;
	}

	/**
	 * Function used to open levels for MultipleOOXML
	 */
	public String OpenMasterSub(int value) {
		String output = "";
		for (int i = 1; i <= value; i++) {
			output = output + "<level" + i + ">";
		}
		output = output + "<p/>";
		return output;
	}

	/**
	 * Function which increments the counter value
	 */
	public void Increment2(int rec, int pkLvl, String numId) {
		for (int i = 0; i <= rec; i++) {
			ListPush(i);
			if (i > pkLvl)
				IncrementListCounters(i, numId);
		}
	}

	public String TextList(String numFormat, String lvlText, String numId, int iLvl,
	                       XPath xpath, DocumentBuilder docBuilder)
			throws InvalidFormatException, XPathExpressionException, IOException, SAXException {
		String text = "";
		int index = lvlText.indexOf('%');
		if (index < 0) {
			if (numFormat.equals("bullet")) {
				text = bulletChar.get(iLvl);
			} else if (numFormat.equals("none")) {
				text = "";
			} else if (numFormat.equals("")) {
				text = "";
			} else {
				text = lvlText;
			}
			text = text + " ";
		} else {
			text = lvlText.substring(lvlText.indexOf('%'));
			List<String> absFormat = AbstractFormat(numId, xpath, docBuilder);
			int cntSymbol = 0;
			for (int j = 0; j < text.length(); j++) {
				if (text.charAt(j) == '%')
					cntSymbol++;
			}
			String[] chr = new String[text.length()];
			int cntCopy;
			if (cntSymbol == 1)
				cntCopy = NonZeroListCounter(numId);
			else
				cntCopy = 0;
			for (int i = 0; i < text.length(); i++) {
				chr[i] = "" + text.charAt(i);
				if (i > 0) {
					if (chr[i - 1].equals("%")) {
						if (numFormat.equals("decimalZero")) {
							if (cntSymbol == 1) {
								String valList = SpecificFormat("" + cntCopy, numFormat, iLvl);
								chr[i] = "0" + valList;
							} else {
								if (cntCopy > 0) {
									String valList = SpecificFormat(listCounters.get("List" + numId).get(cntCopy), numFormat, iLvl);
									if (Integer.parseInt(valList) < 9)
										chr[i] = "0" + valList;
									else
										chr[i] = valList;
								} else {
									String valList = SpecificFormat(listCounters.get("List" + numId).get(cntCopy), numFormat, iLvl);
									chr[i] = valList;
								}
							}
						} else {
							if (cntSymbol == 1) {
								String valList = SpecificFormat("" + cntCopy, numFormat, iLvl);
								chr[i] = valList;
							} else {
								String valList = SpecificFormat(listCounters.get("List" + numId).get(cntCopy), absFormat.get(cntCopy), iLvl);
								chr[i] = valList;
							}
						}
						cntCopy++;
					}
				}
			}
			String temp = "";
			for (int i = 0; i < chr.length; i++) {
				temp = temp + chr[i];
			}
			temp = temp.replace("%", "");
			text = lvlText.substring(0, lvlText.indexOf('%')) + temp;
		}
		return text + " ";
	}

	private List<String> AbstractFormat(String numId, XPath xpath, DocumentBuilder docBuilder)
			throws InvalidFormatException, IOException, SAXException, XPathExpressionException {
		PackageRelationship relationship = null;
		for (PackageRelationship searchRelation : pack.getRelationshipsByType(wordRelationshipType)) {
			relationship = searchRelation;
			break;
		}
		PackagePart mainPartxml = pack.getPart(relationship);
		PackageRelationship numberRelationship = null;
		for (PackageRelationship searchRelation : mainPartxml.getRelationshipsByType(numberRelationshipType)) {
			numberRelationship = searchRelation;
			break;
		}
		PackagePart numberPartxml = mainPartxml.getRelatedPart(numberRelationship);
		Document doc = docBuilder.parse(numberPartxml.getInputStream());
		NodeList list = (NodeList)compileXPathExpression(
			xpath,
			"w:numbering/w:num[@w:numId=" + numId + "]/w:abstractNumId",
			ImmutableMap.of("w", docNamespace))
			.evaluate(doc, XPathConstants.NODESET);
		List<String> absFormat = new ArrayList<>();
		if (list.getLength() != 0) {
			String absNumid = list.item(0).getAttributes().getNamedItemNS(docNamespace, "val").getTextContent();
			NodeList listAbs = (NodeList)compileXPathExpression(
				xpath,
				"w:numbering/w:abstractNum[@w:abstractNumId=" + absNumid + "]/w:lvl",
				ImmutableMap.of("w", docNamespace))
				.evaluate(doc, XPathConstants.NODESET);
			if (listAbs.getLength() != 0) {
				for (int j = 0; j < listAbs.getLength(); j++)
					absFormat.add(
						((NodeList)compileXPathExpression(
							xpath,
							"w:numFmt",
							ImmutableMap.of("w", docNamespace))
						 .evaluate(listAbs.item(j), XPathConstants.NODESET))
						.item(0)
						.getAttributes().getNamedItemNS(docNamespace, "val").getTextContent());
			}
		}
		return absFormat;
	}

	public String IsList(String numId) {
		if (listCounters.containsKey("List" + numId))
			return "ListTrue";
		else if (headingCounters.containsKey("List" + numId))
			return "HeadTrue";
		else
			return numId;
	}

	public void IncrementListCounters(int iLvl, String numId) {
		if (startItem.get(iLvl).equals(""))
			startItem.set(iLvl, "0");
		if (listCounters.get("List" + numId).get(iLvl).equals(emptyListCounter)) {
			if (Integer.parseInt(startItem.get(iLvl)) != 1)
				listCounters.get("List" + numId).set(iLvl, startItem.get(iLvl));
			else
				listCounters.get("List" + numId).set(iLvl, "1");
		} else
			listCounters.get("List" + numId).set(iLvl, "" + (Integer.parseInt(listCounters.get("List" + numId).get(iLvl)) + 1));
		for (int i = iLvl + 1; i < listCounters.get("List" + numId).size(); i++) {
			listCounters.get("List" + numId).set(i, emptyListCounter);
		}
		for (int i = 0; i <= iLvl; i++) {
			if (listCounters.get("List" + numId).get(i).equals(emptyListCounter))
				listCounters.get("List" + numId).set(i, startItem.get(i));
		}
	}

	public int GetListCounter(int iLvl, String numId) {
		String listCounter = listCounters.get("List" + numId).get(iLvl);
		if (listCounter.equals(emptyListCounter))
			return 1;
		else
			return Integer.parseInt(listCounter) + 1;
	}

	private int NonZeroListCounter(String numId) {
		int nonZeroValue = -1;
		int i = 0;
		for (i = 0; i < listCounters.get("List" + numId).size(); i++) {
			if (listCounters.get("List" + numId).get(i).equals(emptyListCounter)) {
				nonZeroValue = Integer.parseInt(listCounters.get("List" + numId).get(i - 1));
				break;
			}
		}
		if (nonZeroValue == -1)
			nonZeroValue = Integer.parseInt(listCounters.get("List" + numId).get(8));
		return nonZeroValue;
	}

	public String CheckNumID(String numId) {
		if (!prevNumId.equals(numId)) {
			prevNumId = numId;
			return "True";
		} else {
			return "False";
		}
	}

	public String StartNewListCounter(String numId) {
		if (!listCounters.containsKey("List" + numId)) {
			listCounters.put("List" + numId, new ArrayList<String>());
			for (int i = 0; i < 9; i++)
				listCounters.get("List" + numId).add(emptyListCounter);
		}
		return "1";
	}

	public String StartString(int iLvl, String strtItem) {
		startItem.set(iLvl, strtItem);
		return "1";
	}

	public void StoreHeadingPart(String headingInfo) {
		this.headingInfo = headingInfo;
	}

	public String RetrieveHeadingPart() {
		return this.headingInfo;
	}

	public String TextHeading(
		String numFormat,
		String lvlText,
		String numId,
		int iLvl,
		XPath xpath,
		DocumentBuilder docBuilder
	) throws InvalidFormatException, XPathExpressionException, IOException, SAXException {
		String text = "";
		int index = lvlText.indexOf('%');
		if (index < 0) {
			if (numFormat.equals("bullet")) {
				text = bulletChar.get(iLvl);
			} else if (numFormat.equals("none")) {
				text = "";
			} else if (numFormat.equals("")) {
				text = "";
			} else {
				text = lvlText;
			}
			text = text + " ";
		} else {
			text = lvlText.substring(index);
			List<String> absFormat = AbstractFormat(numId, xpath, docBuilder);
			int cntSymbol = 0;
			for (int j = 0; j < text.length(); j++) {
				if (text.charAt(j) == '%')
					cntSymbol++;
			}
			String[] chr = new String[text.length()];
			int cntCopy;
			if (cntSymbol == 1)
				cntCopy = NonZeroHeadingCounter(numId);
			else
				cntCopy = 0;
			for (int i = 0; i < text.length(); i++) {
				chr[i] = "" + text.charAt(i);
				if (i > 0) {
					if (chr[i - 1].equals("%")) {
						if ("decimalZero".equals(numFormat)) {
							if (cntSymbol == 1) {
								String valList = SpecificFormat("" + cntCopy, numFormat, iLvl);
								chr[i] = "0" + valList;
							} else {
								if (cntCopy > 0) {
									String valList = SpecificFormat(
										headingCounters.get("List" + numId).get(cntCopy), numFormat, iLvl);
									if (Integer.parseInt(valList) < 9)
										chr[i] = "0" + valList;
									else
										chr[i] = valList;
								} else {
									String valList = SpecificFormat(
										headingCounters.get("List" + numId).get(cntCopy), numFormat, iLvl);
									chr[i] = valList;
								}
							}
						} else {
							if (cntSymbol == 1) {
								String valList = SpecificFormat("" + cntCopy, numFormat, iLvl);
								chr[i] = valList;
							} else {
								String valList = SpecificFormat(
									headingCounters.get("List" + numId).get(cntCopy), absFormat.get(cntCopy), iLvl);
								chr[i] = valList;
							}
						}
						cntCopy++;
					}
				}
			}
			String temp = "";
			for (int i = 0; i < chr.length; i++) {
				temp = temp + chr[i];
			}
			temp = temp.replace("%", "");
			text = lvlText.substring(0, lvlText.indexOf('%')) + temp;
		}
		return text + " ";
	}

	public void IncrementHeadingCounters(String iLvlString, String numId, String absId) {
		int iLvl; {
			try {
				iLvl = Integer.parseInt(iLvlString);
			} catch (NumberFormatException e) {
				// FIXME: Convert.ToInt16 returns 0 for null argument, which probably means that in
				// the original code iLvlString is null and not the empty string when the input is
				// an empty node sequence
				iLvl = 0;
			}
		}
		if (!numId.equals("") && !absId.equals("")) {
			String tempId = "";
			tempId = CheckAbstCounter(numId, absId);
			switch (startHeadingItem.get("List" + tempId).get(iLvl)) {
			case "Inc":
				switch (headingCounters.get("List" + numId).get(iLvl)) {
				case emptyListCounter:
					headingCounters.get("List" + numId).set(iLvl, "" + 1);
					break;
				default:
					headingCounters.get("List" + numId)
					               .set(iLvl, "" + (Integer.parseInt((headingCounters.get("List" + numId)).get(iLvl)) + 1));
					break;
				}
				break;
			default:
				headingCounters.get("List" + numId).set(iLvl, startHeadingItem.get("List" + tempId).get(iLvl));
				break;
			}
			for (int i = iLvl + 1; i < headingCounters.get("List" + numId).size(); i++) {
				headingCounters.get("List" + numId).set(i, emptyListCounter);
			}
		}
	}

	private String CheckAbstCounter(String numId, String absId) {
		String tempId = "";
		if (!startHeadingItem.containsKey("List" + numId)) {
			if (absId.equals(""))
				absId = baseAbsId;
			if (baseAbsId.equals(absId))
				tempId = baseNumId;
		} else
			tempId = numId;
		return tempId;
	}

	private int NonZeroHeadingCounter(String numId) {
		int nonZeroValue = -1;
		int i = 0;
		for (i = 0; i < headingCounters.get("List" + numId).size(); i++) {
			if (headingCounters.get("List" + numId).get(i).equals(emptyListCounter)) {
				nonZeroValue = Integer.parseInt(headingCounters.get("List" + numId).get(i - 1));
				break;
			}
		}
		if (nonZeroValue == -1)
			nonZeroValue = Integer.parseInt(headingCounters.get("List" + numId).get(8));
		return nonZeroValue;
	}

	public String CheckHeadingNumID(String numId) {
		String diffNumId = "";
		if (!prevHeadNumId.equals(numId)) {
			if (!prevHeadNumId.equals("")) {
				diffNumId = "True";
				prevHeadNumId = numId;
			} else {
				diffNumId = "True";
				prevHeadNumId = numId;
			}
		} else {
			diffNumId = "False";
			prevHeadNumId = numId;
		}
		return diffNumId;
	}

	public String StartNewHeadingCounter(String numId, String absId) {
		if (headingCounters.size() == 0) {
			baseNumId = numId;
			baseAbsId = absId;
		}
		if (!headingCounters.containsKey("List" + numId)) {
			headingCounters.put("List" + numId, new ArrayList<String>());
			for (int i = 0; i < 9; i++)
				headingCounters.get("List" + numId).add(emptyListCounter);
		}
		return "1";
	}

	public String StartHeadingValueCtr(String numId, String absId) {
		if (!startHeadingItem.containsKey("List" + numId)) {
			if (baseAbsId != absId || !startHeadingItem.containsKey("List" + baseNumId)) {
				startHeadingItem.put("List" + numId, new ArrayList<String>());
				for (int i = 0; i < 9; i++)
					startHeadingItem.get("List" + numId).add("");
			}
		}
		return "1";
	}

	public String StartHeadingNewCtr(String numId, String absId) {
		if (!startHeadingItem.containsKey("List" + numId)) {
			if (baseAbsId != absId || !startHeadingItem.containsKey("List" + baseNumId)) {
				startHeadingItem.put("List" + numId, new ArrayList<String>());
				for (int i = 0; i < 9; i++)
					startHeadingItem.get("List" + numId).add("");
			}
		}
		return "1";
	}

	public String CopyToBaseCounter(String numId) {
		if (!numId.equals(baseNumId) && !numId.equals("") && !numId.equals("0")) {
			for (int i = 0; i < 9; i++) {
				headingCounters.get("List" + baseNumId).set(i, headingCounters.get("List" + numId).get(i));
			}
			return "1";
		} else
			return "2";
	}

	public String CopyToCurrCounter(String numId) {
		if (!numId.equals(baseNumId) && !numId.equals("") && !numId.equals("0")) {
			for (int i = 0; i < 9; i++) {
				if (!headingCounters.get("List" + baseNumId).get(i).equals(emptyListCounter))
					headingCounters.get("List" + numId).set(i, headingCounters.get("List" + baseNumId).get(i));
			}
			return "1";
		} else
			return "2";
	}

	/**
	 * Function which decrements the counter value
	 */
	public String StartHeadingString(int iLvl, String strtItem, String numId, String absId, String location, String overRide) {
		if (strtItem.equals(""))
			strtItem = "0";
		int overrideFlag = CheckOverRideFlag(numId);
		if (baseAbsId.equals(absId))
			numId = baseNumId;
		if (iLvl == 0) {
			for (int i = 1; i < 9; i++)
				startHeadingItem.get("List" + numId).set(i, "");
		}
		int val = iLvl + 1;
		for (int i = val; i < 9; i++)
			startHeadingItem.get("List" + numId).set(i, "");
		if (overRide.equals("Yes") && location.equals("Document") && overrideFlag == 1) {
			startHeadingItem.get("List" + numId).set(iLvl, strtItem);
		} else {
			if (startHeadingItem.get("List" + numId).get(iLvl).equals("")) {
				if ((baseAbsId.equals(absId) && !baseNumId.equals(numId) && overRide.equals("No")))
					startHeadingItem.get("List" + numId).set(iLvl, "Inc");
				else
					startHeadingItem.get("List" + numId).set(iLvl, strtItem);
			} else {
				startHeadingItem.get("List" + numId).set(iLvl, "Inc");
			}
		}
		return "0";
	}

	private int CheckOverRideFlag(String numId) {
		int overrideFlag = 0;
		if (!OverideNumList.contains(numId)) {
			OverideNumList.add(numId);
			overrideFlag = 1;
		} else {
			overrideFlag = 0;
		}
		return overrideFlag;
	}

	public String AddCurrHeadId(String currId) {
		if (!currId.equals("0") && !currId.equals("")) {
			if (prevHeadId.get(0).equals("")) {
				prevHeadId.set(0, currId);
				prevHeadId.set(1, currId);
			} else {
				prevHeadId.set(0, prevHeadId.get(1));
				prevHeadId.set(1, currId);
			}
		}
		return "1";
	}

	public String AddCurrHeadLevel(int currLvl, String location, String absId,
	                               XPath xpath, DocumentBuilder docBuilder)
			throws NumberFormatException, XPathExpressionException, InvalidFormatException, SAXException, IOException {
		String diffValue = "|" + prevHeadLvl;
		if (!absId.equals("")) {
			if (prevHeadLvl.equals("")) {
				if (currLvl > 0) {
					for (int i = 0; i < currLvl; i++) {
						StartHeadingValueCtr(baseNumId, baseAbsId);
						StartHeadingString(i, "1", baseNumId, baseAbsId, "Document", "No");
						IncrementHeadingCounters("" + i, baseNumId, baseAbsId);
					}
				}
				prevHeadLvl = "" + currLvl;
				diffValue = "|" + prevHeadLvl;
			} else {
				if (currLvl - Integer.parseInt(prevHeadLvl) > 1) {
					diffValue = currLvl - Integer.parseInt(prevHeadLvl) + "|" + prevHeadLvl;
					NumberHeadings(Integer.parseInt(prevHeadLvl), currLvl, location, absId,
					               xpath, docBuilder);
				}
				prevHeadLvl = "" + currLvl;
			}
		}
		return diffValue;
	}

	private void NumberHeadings(int prevHeadLvl, int currLvl, String location, String absId,
	                            XPath xpath, DocumentBuilder docBuilder)
			throws XPathExpressionException, InvalidFormatException, SAXException, IOException {
		PackageRelationship relationship = null;
		for (PackageRelationship searchRelation : pack.getRelationshipsByType(wordRelationshipType)) {
			relationship = searchRelation;
			break;
		}
		PackagePart mainPartxml = pack.getPart(relationship);
		PackageRelationship numberRelationship = null;
		for (PackageRelationship searchRelation : mainPartxml.getRelationshipsByType(numberRelationshipType)) {
			numberRelationship = searchRelation;
			break;
		}
		PackagePart numberPartxml = mainPartxml.getRelatedPart(numberRelationship);
		Document doc = docBuilder.parse(numberPartxml.getInputStream());
		StartNewHeadingCounter(prevHeadId.get(1), absId);
		CopyToCurrCounter(prevHeadId.get(1));
		int val = prevHeadLvl + 1;
		for (int i = val; i <= currLvl; i++) {
			NodeList listDel = (NodeList)compileXPathExpression(
				xpath,
				"w:numbering/w:num[@w:numId=" + prevHeadId.get(0) + "]/w:lvlOverride[@w:ilvl=" + i + "]/w:startOverride",
				ImmutableMap.of("w", docNamespace))
				.evaluate(doc, XPathConstants.NODESET);
			if (listDel.getLength() != 0) {
				StartHeadingValueCtr(prevHeadId.get(1), absId);
				String tempId = "";
				tempId = CheckAbstCounter(prevHeadId.get(1), absId);
				String valAbs = listDel.item(0).getAttributes().getNamedItemNS(docNamespace, "val").getTextContent();
				if (valAbs.equals(""))
					valAbs = "0";
				if (i == currLvl) {
					if (location.equals("Style")) {
						startHeadingItem.get("List" + tempId).set(i, "" + (Integer.parseInt(valAbs) - 1));
						headingCounters.get("List" + prevHeadId.get(1)).set(i, "" + (Integer.parseInt(valAbs) - 1));
					} else {
						startHeadingItem.get("List" + tempId).set(i, valAbs);
						headingCounters.get("List" + prevHeadId.get(1)).set(i, valAbs);
					}
				} else {
					startHeadingItem.get("List" + tempId).set(i, valAbs);
					headingCounters.get("List" + prevHeadId.get(1)).set(i, valAbs);
				}
			} else {
				NodeList listAbsDel = (NodeList)compileXPathExpression(
					xpath,
					"w:numbering/w:num[@w:numId=" + prevHeadId.get(0) + "]/w:abstractNumId",
					ImmutableMap.of("w", docNamespace))
					.evaluate(doc, XPathConstants.NODESET);
				NodeList list = (NodeList)compileXPathExpression(
					xpath,
					"w:numbering/w:abstractNum[@w:abstractNumId=" + listAbsDel.item(0).getAttributes().getNamedItemNS(docNamespace, "val").getTextContent() + "]/w:lvl[@w:ilvl=" + i + "]/w:start",
					ImmutableMap.of("w", docNamespace))
					.evaluate(doc, XPathConstants.NODESET);
				StartHeadingValueCtr(prevHeadId.get(1), absId);
				String tempId = "";
				tempId = CheckAbstCounter(prevHeadId.get(1), absId);
				if (list.getLength() != 0) {
					String valAbs = list.item(0).getAttributes().getNamedItemNS(docNamespace, "val").getTextContent();
					if (valAbs.equals(""))
						valAbs = "0";
					if (i == currLvl) {
						if (location.equals("Style")) {
							startHeadingItem.get("List" + tempId).set(i, "" + (Integer.parseInt(valAbs) - 1));
							headingCounters.get("List" + prevHeadId.get(1)).set(i, "" + (Integer.parseInt(valAbs) - 1));
						} else {
							startHeadingItem.get("List" + tempId).set(i, valAbs);
							headingCounters.get("List" + prevHeadId.get(1)).set(i, valAbs);
						}
					} else {
						startHeadingItem.get("List" + tempId).set(i, valAbs);
						headingCounters.get("List" + prevHeadId.get(1)).set(i, valAbs);
					}
				} else {
					String valAbs = "0";
					if (i == currLvl) {
						if (location.equals("Style")) {
							startHeadingItem.get("List" + tempId).set(i, "" + (Integer.parseInt(valAbs) - 1));
							headingCounters.get("List" + prevHeadId.get(1)).set(i, "" + (Integer.parseInt(valAbs) - 1));
						} else {
							startHeadingItem.get("List" + tempId).set(i, valAbs);
							headingCounters.get("List" + prevHeadId.get(1)).set(i, valAbs);
						}
					} else {
						startHeadingItem.get("List" + tempId).set(i, valAbs);
						headingCounters.get("List" + prevHeadId.get(1)).set(i, valAbs);
					}
				}
			}
		}
	}

	public String GenerateObjectId() {
		objectId++;
		return "" + objectId;
	}

	/**
	 * Object in destination folder
	 */
	public String Object(String inNum) throws IOException, InvalidFormatException {
		PackageRelationship relationship = null;
		for (PackageRelationship searchRelation : pack.getRelationshipsByType(wordRelationshipType)) {
			relationship = searchRelation;
			break;
		}
		PackagePart mainPartxml = pack.getPart(relationship);
		PackageRelationship imgRelationship = mainPartxml.getRelationship(inNum);
		PackagePart objPartxml = mainPartxml.getRelatedPart(imgRelationship);
		String strImgName = objPartxml.getPartName().getURI().toString()
		                              .substring(objPartxml.getPartName().getURI().toString().lastIndexOf('/') + 1);
		if (!strImgName.endsWith(".bin")) {
			InputStream stream = objPartxml.getInputStream();
			FileOutputStream objectFileStream = new FileOutputStream(new File(outputFilename, strImgName));
			int Length = 256;
			byte[] buffer = new byte[Length];
			int bytesRead = stream.read(buffer, 0, Length);
			while (bytesRead > 0) {
				objectFileStream.write(buffer, 0, bytesRead);
				bytesRead = stream.read(buffer, 0, Length);
			}
			stream.close();
			objectFileStream.close();
		}
		return strImgName;
	}

	public String DocPropSubject() throws InvalidFormatException {
		return pack.getPackageProperties().getSubjectProperty().orElse("");
	}

	public String DocPropDescription() throws InvalidFormatException {
		return pack.getPackageProperties().getDescriptionProperty().orElse("");
	}

	/**
	 * Function used to set the Abbreviations/Acronyms flag
	 */
	public String SetListFlag() {
		listflag = 1;
		return "1";
	}

	/**
	 * Function used to reset the Abbreviations/Acronyms flag
	 */
	public String ReSetListFlag() {
		listflag = 0;
		return "0";
	}

	/**
	 * Function used to return the Abbreviations/Acronyms flag value
	 */
	public int ListFlag() {
		return listflag;
	}

	public String PushPageStyle(String pageStyle) {
		_isAnyPageStyleApplied = true;
		PageStyle pushingStyle = PageStylesValidator.GetPageStyle(pageStyle);
		_currentParagraphStylse.add(pushingStyle);
		return pageStyle;
	}

	public void IncrementCheckingParagraph() {
		ValidationResult result = _pageStylesValidator.ValidateParagraph(_currentParagraphStylse);
		if (!result.IsValid())
			_pageStylesErrors.append(result.ErrorMessage + System.lineSeparator());
		_currentParagraphStylse = new ArrayList<PageStyle>();
	}

	public String IsInvalidPageStylesSequence() {
		ValidationResult result = _pageStylesValidator.ValidateLastStyle();
		if (!result.IsValid())
			_pageStylesErrors.append(result.ErrorMessage);
		if (_pageStylesErrors.length() > 0)
			return "true";
		else
			return "false";
	}

	public String GetPageStylesErrors() {
		return _pageStylesErrors.toString();
	}

	public String SetCurrentMatterType(String matterType) {
		_currentMatterType = matterType;
		return _currentMatterType;
	}

	public String GetCurrentMatterType() {
		if (!_isAnyPageStyleApplied)
			return "Bodymatter";
		if (_currentMatterType == null || "".equals(_currentMatterType))
			return "Frontmatter";
		return _currentMatterType;
	}

	public void ResetCurrentMatterType() {
		_currentMatterType = "";
	}

		/**
		 */
        public void PushCharacterStyle(String tag)
        {
            characterStyle.push(tag);
        }

		/**
		 */
        public boolean HasCharacterStyle(String tag)
        {
            return characterStyle.contains(tag);
        }

		/**
		 */
        public String PopCharacterStyle()
        {
            if(characterStyle.isEmpty())
            {
                return "";
            } else return characterStyle.pop();
        }

	@Component(
		name = "DaisyClass",
		service = { ExtensionFunctionProvider.class }
	)
	public static class Provider extends ReflexiveExtensionFunctionProvider {
		public Provider() {
			super(DaisyClass.class);
		}
	}
}
