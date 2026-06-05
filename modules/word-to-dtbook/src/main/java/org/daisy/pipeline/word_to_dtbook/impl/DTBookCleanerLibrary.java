package org.daisy.pipeline.word_to_dtbook.impl;

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

    public static String getDefaultLocale() {
        return Locale.getDefault().toString().replace('_', '-');
    }

    public static String getDate() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }
}
