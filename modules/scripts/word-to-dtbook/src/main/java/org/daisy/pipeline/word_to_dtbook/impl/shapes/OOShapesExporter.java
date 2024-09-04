package org.daisy.pipeline.word_to_dtbook.impl.shapes;

import com.sun.star.beans.Property;
import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertySet;
import com.sun.star.beans.XPropertySetInfo;
import com.sun.star.chart2.XChartShape;
import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.comp.helper.BootstrapException;
import com.sun.star.container.XNameContainer;
import com.sun.star.container.XNamed;
import com.sun.star.drawing.*;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.graphic.XGraphic;
import com.sun.star.graphic.XGraphicProvider;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.presentation.XPresentation;
import com.sun.star.presentation.XPresentationPage;
import com.sun.star.presentation.XPresentationSupplier;
import com.sun.star.table.XTableCharts;
import com.sun.star.text.XTextDocument;
import com.sun.star.ucb.XContent;
import com.sun.star.ucb.XContentIdentifier;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.XCloseable;
import org.apache.commons.io.FilenameUtils;
import org.daisy.common.shell.BinaryFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class OOShapesExporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(OOShapesExporter.class);

    /**
     * Export a openoffice graphic object within a document to
     * @param xGraphicProvider
     * @param xGraphic
     * @param fileName
     * @throws Exception
     */
    public static void exportGraphicObject(XGraphicProvider xGraphicProvider, XGraphic xGraphic, String fileName) throws Exception {
        if (xGraphicProvider == null){
            throw new Exception("XGraphicProvider is null.");
        }

        if (xGraphic == null){
            throw new Exception("XGraphic is null.");
        }

        PropertyValue[] properties = new PropertyValue[2];
        properties[0] = new PropertyValue();
        properties[0].Name = "URL";
        properties[0].Value = fileName;
        properties[1] = new PropertyValue();
        properties[1].Name = "MimeType";
        properties[1].Value = "image/" + fileName.trim().substring(fileName.length() - 3);

        xGraphicProvider.storeGraphic(xGraphic, properties);
    }

    public static void exportAllEmbeddedGraphics(
            XGraphicProvider xGraphicProvider,
            XGraphicExportFilter xGraphicExportFilter,
            XTextDocument xTextDocument,
            String outDir,
            String outputPrefix
    ) throws Exception {
        if (xGraphicProvider == null){
            throw new Exception("XGraphicProvider is null.");
        }

        if (xGraphicExportFilter == null){
            throw new Exception("XGraphicExportFilter is null.");
        }

        if (xTextDocument == null){
            throw new Exception("XTextDocument is null.");
        }

        XServiceInfo xServiceInfoDocument = UnoRuntime.queryInterface(XServiceInfo.class, xTextDocument);
        if (xServiceInfoDocument != null) {
            String[] documentServices = xServiceInfoDocument.getSupportedServiceNames();
            xServiceInfoDocument = null;
        }

        XDrawPageSupplier xDrawPageSupplier = UnoRuntime.queryInterface(XDrawPageSupplier.class, xTextDocument);
        if(xDrawPageSupplier != null){
            XDrawPage xDrawPage = xDrawPageSupplier.getDrawPage();

            if (xDrawPage != null) {
                int count = xDrawPage.getCount();
                for(int i = 0; i < count; ++i){
                    Object elem = xDrawPage.getByIndex(i);
                    XPropertySet xPropertySet = UnoRuntime.queryInterface(XPropertySet.class, elem);
                    Property[] temp1 = xPropertySet.getPropertySetInfo().getProperties();

                    String desc = "";
                    List<String> propertiesNames = new ArrayList<>();
                    for (Property t: temp1 ) {
                        propertiesNames.add(t.Name);
                        if(t.Name == "Description"){
                            desc = xPropertySet.getPropertyValue(t.Name).toString();
                        }
                    }
                    XServiceInfo xServiceInfo = UnoRuntime.queryInterface(XServiceInfo.class, elem);
                    XNamed xNamed = UnoRuntime.queryInterface(XNamed.class, elem);
                    if (xServiceInfo != null){
                        String[] services = xServiceInfo.getSupportedServiceNames();
                        if(xServiceInfo.supportsService("com.sun.star.text.TextContent")
                                && xServiceInfo.supportsService("com.sun.star.text.TextGraphicObject")) {

                            try{
                                Property[] temp = xPropertySet.getPropertySetInfo().getProperties();
                                XGraphic xGraphic = UnoRuntime.queryInterface(
                                        XGraphic.class, xPropertySet.getPropertyValue("Graphic")
                                );
                                if (xGraphic != null){
                                    //String fileName = UUID.randomUUID() + ".png";
                                    String fileName = (xNamed != null ? xNamed.getName() : UUID.randomUUID()) + ".png";
                                    System.out.println("Filename: " + outDir + fileName);
                                    exportGraphicObject(xGraphicProvider, xGraphic, outDir + outputPrefix + fileName);

                                    xGraphic = null;
                                }
                            } catch (java.lang.Exception e){

                            }

                        } else if (xServiceInfo.supportsService("com.sun.star.drawing.Shape")){
                            XShape test = UnoRuntime.queryInterface(
                                    XShape.class, elem
                            );
                            String type = test.getShapeType();

                            XComponent xCompo = UnoRuntime.queryInterface(
                                    XComponent.class, elem
                            );

                            String name = xNamed != null ? xNamed.getName() : "";
                            String fileName = (name.length() > 0 ? name : UUID.randomUUID()) + ".png";
                            if(xCompo != null){
                                xGraphicExportFilter.setSourceDocument(xCompo);
                                //
                                PropertyValue[] properties = new PropertyValue[2];
                                properties[0] = new PropertyValue();
                                properties[0].Name = "URL";
                                properties[0].Value = outDir + outputPrefix + fileName;
                                properties[1] = new PropertyValue();
                                properties[1].Name = "MimeType";
                                properties[1].Value = "image/png";

                                xGraphicExportFilter.filter(properties);

                            } else {
                                System.out.println("no component ?");
                            }
                        } else {
                            String name = xNamed != null ? xNamed.getName() : "";
                            System.out.println("Undefined drawing ?");
                        }

                        xServiceInfo = null;
                    }
                }

            }
        }

        if (xDrawPageSupplier != null)
            xDrawPageSupplier = null;
    }


    public static void ProcessShapes(String inputPath, String outputPath) throws IOException, Exception {
        Optional<String> sofficePath = BinaryFinder.find("soffice");
        if(!sofficePath.isPresent()){
            throw new IOException("No openoffice compatible runtime found in system");
        }
        File oooExecFolder = new File(sofficePath.get()).getParentFile();
        XComponentContext xContext = null;
        XMultiComponentFactory xMultiComponentFactory = null;
        Object oDesktop = null;
        XComponentLoader xComponentLoader = null;
        // Images export
        XGraphicProvider xGraphicProvider = null;
        // Shapes export
        XGraphicExportFilter xGraphicExportFilter = null;

        try {
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
            method.setAccessible(true);
            method.invoke(Bootstrap.class.getClassLoader(), new Object[] {oooExecFolder.toURI().toURL()});
            List<String> temp = new ArrayList<>(Arrays.asList(Bootstrap.getDefaultOptions()));
            temp.add("--invisible");
            temp.add("--headless");
            xContext = Bootstrap.bootstrap(temp.toArray(new String[temp.size()]));
        } catch (BootstrapException e) {
            throw new IOException("Boostrap failed! Failed to start OpenOffice.", e);
        } catch (MalformedURLException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        try{
            xMultiComponentFactory  = (XMultiComponentFactory) xContext.getServiceManager();
            oDesktop = xMultiComponentFactory.createInstanceWithContext(
                    "com.sun.star.frame.Desktop", xContext);
            xComponentLoader = (XComponentLoader)UnoRuntime.queryInterface(
                    com.sun.star.frame.XComponentLoader.class, oDesktop);
            if (xComponentLoader == null) {
                throw new IOException("Failed to create openoffice XComponentLoader");
            }

            try {
                Object oGraphicProvider = xMultiComponentFactory.createInstanceWithContext(
                        "com.sun.star.graphic.GraphicProvider", xContext);
                xGraphicProvider = (XGraphicProvider) UnoRuntime.queryInterface(
                        XGraphicProvider.class, oGraphicProvider);

                Object oGraphicExportFilter = xMultiComponentFactory.createInstanceWithContext(
                        "com.sun.star.drawing.GraphicExportFilter", xContext);
                xGraphicExportFilter = UnoRuntime.queryInterface(
                        XGraphicExportFilter.class, oGraphicExportFilter);
            } catch (Exception e) {
                throw new IOException("Failed to create openoffice XGraphicProvider or xGraphicExportFilter", e);
            }
			/*try {
				Object oTextShapesSupplier = xMultiComponentFactory.createInstanceWithContext(
						"com.sun.star.text.XTextShapesSupplier", xContext);
				xTextShapesSupplier = (XTextShapesSupplier) UnoRuntime.queryInterface(
						XTextShapesSupplier.class, oTextShapesSupplier);
			} catch (Exception e) {
				System.out.println("Failed to create XGraphicProvider!");
				return;
			}*/

            PropertyValue[] properties = new PropertyValue[2];
            properties[0] = new PropertyValue();
            properties[0].Name = "Hidden";
            properties[0].Value = false; // put true to hide OpenOffice window
            properties[1] = new PropertyValue();
            properties[1].Name = "CharacterSet";
            properties[1].Value = "Unicode (UTF-8)";

            XComponent document = null;
            try {
                document = xComponentLoader.loadComponentFromURL(inputPath, "_blank", 0, properties);
            } catch (com.sun.star.io.IOException e) {
                e.printStackTrace();
                throw e;
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                throw e;
            }

            XTextDocument xTextDocument = (XTextDocument) UnoRuntime.queryInterface(
                    XTextDocument.class, document);
            // Exporting shapes
            String inputNameWithoutExtension = FilenameUtils.getName(inputPath);
            inputNameWithoutExtension = inputNameWithoutExtension.substring(0, inputNameWithoutExtension.length() - FilenameUtils.getExtension(inputNameWithoutExtension).length() - 1 );
            exportAllEmbeddedGraphics(xGraphicProvider, xGraphicExportFilter, xTextDocument, outputPath, inputNameWithoutExtension+"-Shape");

            XCloseable xCloseable = (XCloseable)UnoRuntime.queryInterface(
                    XCloseable.class, xContext);
            if(xCloseable != null)
            {
                xCloseable.close(true);
                xCloseable = null;
            }

            xGraphicProvider = null;
            xComponentLoader = null;
            oDesktop = null;
            xMultiComponentFactory = null;
            xContext = null;

        } catch (java.lang.Exception e){
            throw e;
        } finally {
            try {
                Runtime.getRuntime().exec("tskill soffice");
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }
    }

}
