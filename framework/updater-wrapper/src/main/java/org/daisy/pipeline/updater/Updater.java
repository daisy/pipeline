package org.daisy.pipeline.updater;

import java.io.IOException;
import java.io.InputStream;

import org.daisy.common.properties.Properties;

public class Updater {

        private static String DEFAULT_UPDATE_SITE = "http://daisy.github.io/pipeline-assembly/releases/";

        //launches the pipeline and waits it to be up
        public void update(UpdaterObserver obs) throws IOException {
                String home = Properties.getProperty("org.daisy.pipeline.home");
                if (home == null)
                        throw new IllegalStateException(
                                "The property 'org.daisy.pipeline.home' is not set. Can not run updater.");
                // pipeline-assembly is responsible for placing the file at this location
                // FIXME: embed the binaries within this JAR
                String bin = home + "/updater/pipeline-updater";
                String deployPath = home + "/";
                // pipeline-assembly is responsible for placing the file at this location
                String releaseDescriptor = home + "/etc/releaseDescriptor.xml";
                String updateSite = Properties.getProperty("org.daisy.pipeline.updater.updateSite", DEFAULT_UPDATE_SITE);
                InputStream os=new Launcher(bin,
                                updateSite,
                                deployPath,
                                releaseDescriptor,
                                "current").launch();
                new OutputParser(os,obs).parse();

        }
}
