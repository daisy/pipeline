package org.daisy.pipeline.modules.dtbook_utils.impl;

import java.util.Locale;
import java.util.Date;
import java.text.SimpleDateFormat;
import org.daisy.common.xpath.saxon.ExtensionFunctionProvider;
import org.daisy.common.xpath.saxon.ReflexiveExtensionFunctionProvider;
import org.osgi.service.component.annotations.Component;


public class DTBookCleanerLibrary {

    @Component(
            name = "DTBookCleanerLibrary",
            service = { ExtensionFunctionProvider.class }
    )
    public static class Provider extends ReflexiveExtensionFunctionProvider {
        public Provider() {
            super(DTBookCleanerLibrary.class);
        }
    }

    public String getDefaultLocale(){
        return Locale.getDefault().toString().replace('_', '-');
    }

    public String getDate(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date());
    }

}
