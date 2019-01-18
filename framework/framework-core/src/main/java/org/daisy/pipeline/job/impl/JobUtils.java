package org.daisy.pipeline.job.impl;

import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;

import com.google.common.base.Supplier;

import org.daisy.common.xproc.XProcOutput;
import org.daisy.common.xproc.XProcPortInfo;
import org.daisy.pipeline.job.impl.StatusResultProvider;
import org.daisy.pipeline.script.XProcScript;

import org.w3c.dom.Document;

public class JobUtils {
        /**
         * Checks the validation status from the status port and get it's value
         */
        public static boolean checkStatusPort(XProcScript script, XProcOutput outputs) {
                for (XProcPortInfo info : script.getXProcPipelineInfo().getOutputPorts()) {
                        Supplier<Result> provider = outputs.getResultProvider(info.getName());
                        if (provider != null && provider instanceof StatusResultProvider) {
                                boolean ok = true;
                                for (InputStream status : ((StatusResultProvider)provider).read()) {
                                        ok &= processStatus(status);
                                }
                                return ok;
                        }
                }
                return true;
        }

        /**Reads the xml file pointed by path to check that validation status is equal to ok.
         * if it's not it returns false
         */
        public static boolean processStatus(InputStream status){

                //check the contents of the xml and check if result is ok
                //<d:status xmlns:d="http://www.daisy.org/ns/pipeline/data" result="error"/>
                try{
                        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
                        docBuilderFactory.setNamespaceAware(true);
                        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
                        Document doc = docBuilder.parse(status);
                        String result = doc.getDocumentElement().getAttribute("result");
                        if (result==null || result.isEmpty()){
                                throw new RuntimeException("No result attribute was found in the status port");
                        }
                        return result.equalsIgnoreCase("ok");

                }catch (Exception e){
                        throw new RuntimeException("Error processing status file",e);
                }
        }
}
