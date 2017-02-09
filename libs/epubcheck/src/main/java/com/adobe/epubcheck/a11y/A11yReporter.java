package com.adobe.epubcheck.a11y;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.idpf.epubcheck.util.saxon.LineNumberFunction;
import org.idpf.epubcheck.util.saxon.SystemIdFunction;

import com.adobe.epubcheck.api.Option;
import com.adobe.epubcheck.opf.OPFItem;
import com.adobe.epubcheck.opf.ValidationContext;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

public final class A11yReporter
{

  private final ValidationContext context;
  private final List<OPFItem> items;
  private final Processor processor = new Processor(false);
  private final XsltCompiler xsltCompiler;
  private final XsltExecutable xsltExecutable;

  public A11yReporter(ValidationContext context, Iterable<? extends OPFItem> items)
  {
    this.context = Preconditions.checkNotNull(context);
    this.items = ImmutableList.copyOf(items);
    processor.registerExtensionFunction(new LineNumberFunction());
    processor.registerExtensionFunction(new SystemIdFunction());
    processor.getUnderlyingConfiguration().setLineNumbering(true);
    xsltCompiler = processor.newXsltCompiler();
    try
    {
      xsltExecutable = xsltCompiler.compile(
          new StreamSource(getClass().getResourceAsStream("/com/adobe/epubcheck/a11y/a11y.xsl")));
    } catch (SaxonApiException e)
    {
      throw new IllegalStateException(e);
    }
  }

  public void check()
  {

    Option a11yOption = context.options.getOption(Option.Key.ACCESSIBILITY).get();

    // Prepare files
    Optional<File> a11yReport = Optional
        .<File> fromNullable((File) a11yOption.data.orNull());
    Optional<File> a11yReportDir = a11yReport.isPresent()
        ? Optional.<File> of(new File(a11yReport.get().getParentFile(),
            Files.getNameWithoutExtension(a11yReport.get().getName())))
        : Optional.<File> absent();

    // Apply a11y reporting XSLT
    try
    {

      // Apply the reporter XSLT
      XsltTransformer xslt = xsltExecutable.load();
      Source source = new StreamSource(context.resourceProvider.getInputStream(context.path));
      source.setSystemId("epub:/" + context.path);
      xslt.setSource(source);
      xslt.setParameter(new QName("resources-dir"),
          new XdmAtomicValue(a11yReportDir.or(new File("")).getName() + "/"));
      xslt.setDestination((a11yReport.isPresent())
          ? processor.newSerializer((File) a11yReport.get()) : processor.newSerializer(System.out));
      xslt.setURIResolver(new EpubUriResolver());
      xslt.transform();

      // Copy images if there is a report directory
      if (a11yReportDir.isPresent())
      {
        for (OPFItem item : items)
        {
          if (item.getMimeType().startsWith("image/"))
          {
            File itemFile = new File(a11yReportDir.get(), item.getPath());
            Files.createParentDirs(itemFile);
            ByteStreams.copy(context.resourceProvider.getInputStream(item.getPath()),
                new FileOutputStream(itemFile));
          }
        }
      }
    } catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  private class EpubUriResolver implements URIResolver
  {
    @Override
    public Source resolve(String href, String base)
      throws TransformerException
    {
      try
      {
        String path = new URI("epub:/").relativize(new URI(base).resolve(new URI(href))).toString();
        Source source = new StreamSource(context.resourceProvider.getInputStream(path));
        source.setSystemId("epub:/" + path);
        return source;
      } catch (Exception e)
      {
        return null;
      }
    }
  }
}
