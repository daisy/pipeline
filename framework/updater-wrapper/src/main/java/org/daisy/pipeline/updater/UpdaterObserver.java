package org.daisy.pipeline.updater;

public interface UpdaterObserver {

        public void info(String msg);
        public void error(String msg);
        
}
