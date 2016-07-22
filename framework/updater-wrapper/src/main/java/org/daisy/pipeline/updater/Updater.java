package org.daisy.pipeline.updater;

import java.io.IOException;
import java.io.InputStream;

public class Updater {

        private static String UPDATER_BIN="org.pipeline.updater.bin";
        private static String DEPLOY_PATH="org.pipeline.updater.deployPath";
        private static String UPDATE_SITE="org.pipeline.updater.updateSite";
        private static String RELEASE_DESCRIPTOR="org.pipeline.updater.releaseDescriptor";
        private static String ERROR="Property %s not set";


        //launches the pipeline and waits it to be up
        public void update(UpdaterObserver obs) throws IOException {
                String bin=System.getProperty(UPDATER_BIN,"");
                String deployPath=System.getProperty(DEPLOY_PATH,"");
                String updateSite=System.getProperty(UPDATE_SITE,"");
                String releaseDescriptor=System.getProperty(RELEASE_DESCRIPTOR,"");
                if (bin.isEmpty()){
                        throw new IllegalArgumentException(String.format(ERROR,UPDATER_BIN));
                }
                if (deployPath.isEmpty()){
                        throw new IllegalArgumentException(String.format(ERROR,DEPLOY_PATH));
                }
                if (updateSite.isEmpty()){
                        throw new IllegalArgumentException(String.format(ERROR,UPDATE_SITE));
                }

                InputStream os=new Launcher(bin,
                                updateSite,
                                deployPath,
                                releaseDescriptor,
                                "current").launch();
                new OutputParser(os,obs).parse();

        }
}
