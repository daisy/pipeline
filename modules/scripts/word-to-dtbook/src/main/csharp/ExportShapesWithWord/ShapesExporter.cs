using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.InteropServices;
using System.Drawing.Imaging;
using System.Drawing;
using System.IO;
using MSword = Microsoft.Office.Interop.Word;
using System.Threading;
using System.Windows.Forms;
using System.Diagnostics;

namespace Daisy.SaveAsDAISY.Exporter
{
    [Guid("64de4a60-2e22-4e30-9ee3-8b2e6870fc8b")]
    public interface IShapesExporter
    {
        string[] processShapes(string inputPath, string outputPath);
    }

    [Guid("e221c30a-7683-4ca0-abdb-b42eceddfe88"), ClassInterface(ClassInterfaceType.None)]
    public class ShapesExporter : IShapesExporter
    {
        public string[] processShapes(string inputFile, string outputPath)
        {
            List<string> shapesPath = new List<string>();
            try
            {
                // Cleanup INETCACHE to avoid security notice
                DirectoryInfo inetcacheword = new DirectoryInfo(
                    Path.Combine(
                        System.Environment.GetEnvironmentVariable("LOCALAPPDATA"),
                        "Microsoft",
                        "Windows",
                        "INetCache",
                        "Content.Word"
                    )
                );
                if (inetcacheword.Exists) {
                    FileInfo[] cachedFiles = inetcacheword.GetFiles();
                    foreach (FileInfo file in cachedFiles) {
                        try {
                            File.Delete(file.FullName);
                        }
                        catch (Exception ex) {
                            Console.WriteLine("Warning - could not delete cached file " + file.Name);
                        }
                    }
                }
                // Note for users documentation
                // To avoid the raise of a security notice when launching the conversion
                // It is recommended to clean the word cache and to set word to clean its cache on
                // closing the Word application
                // See
                //System.Diagnostics.Process[] wordInstances = System.Diagnostics.Process.GetProcessesByName("WINWORD");
                MSword.Application app = null;
                try {
                    Console.WriteLine("Trying to retrieve word current instance if it is opened... ");
                    app = (MSword.Application)System.Runtime.InteropServices.Marshal.GetActiveObject("Word.Application");
                    //app.Activate();
                }
                catch (COMException ecom) {
                    try {
#if DEBUG
                        Console.WriteLine($"Could not retrieve word instance: {ecom.Message}");
#endif
                        Console.WriteLine($"Trying to open word...");
                        app = new MSword.Application()
                        {
                            Visible = false,
                            AutomationSecurity = Microsoft
                                .Office
                                .Core
                                .MsoAutomationSecurity
                                .msoAutomationSecurityForceDisable
                        };
                    }
                    catch (COMException e1) {
                        throw new Exception(
                            $"Could not retrieve or open Word application for export: {e1.Message}",
                            e1
                        );
                    }
                    catch (Exception e2) {
                        throw new Exception(
                            $"Could not retrieve or open Word application for export: {e2.Message}",
                            e2
                        );
                    }
                }
               
                if (app != null)
                {
                    Console.WriteLine($"Opening document in the background");
                    // Open document in read-only
                    MSword.Document currentDoc = app.Documents.Open(
                        Visible: false,
                        FileName: inputFile,
                        ReadOnly: true,
                        AddToRecentFiles: false
                    );
                    Exception threadEx = null;
                    Thread staThread = new Thread(
                        delegate()
                        {
                            try
                            {
                                Console.WriteLine($"Starting shapes export ...");
                                object missing = Type.Missing;

                                List<string> warnings = new List<string>();
                                string fileName = currentDoc.Name.ToString().Replace(" ", "_");
                                MSword.Application WordInstance = currentDoc.Application;
                                //WordInstance.Activate();

                                Process objProcess = Process.GetCurrentProcess();

                                foreach (MSword.Shape shape in currentDoc.Shapes)
                                {
                                    string name = shape.Name.ToString();
                                    if (!shape.Name.Contains("Text Box"))
                                    {
                                        shape.Select(ref missing);
                                        string shapeOutputPath = Path.Combine(
                                            outputPath,
                                            Path.GetFileNameWithoutExtension(fileName)
                                                + "-Shape"
                                                + shape.ID.ToString()
                                                + ".png"
                                        );
                                        WordInstance.Selection.CopyAsPicture();
                                        try
                                        {
                                            Image image = ClipboardEx.GetEMF(
                                                objProcess.MainWindowHandle
                                            );
                                            byte[] Ret;
                                            MemoryStream ms = new MemoryStream();
                                            image.Save(ms, ImageFormat.Png);
                                            Ret = ms.ToArray();
                                            FileStream fs = new FileStream(
                                                shapeOutputPath,
                                                FileMode.Create,
                                                FileAccess.Write
                                            );

                                            fs.Write(Ret, 0, Ret.Length);
                                            fs.Flush();
                                            fs.Dispose();
                                            Console.WriteLine("Exported shape " + shapeOutputPath);
                                            shapesPath.Add(shapeOutputPath);
                                            //objectShapes.Add(pathShape);
                                            //imageIds.Add(item.ID.ToString());
                                        }
                                        catch (ClipboardDataException cde)
                                        {
                                            warnings.Add(
                                                $"- Shape {shape.ID.ToString()} {name}: {cde.Message}"
                                            );
                                        }
                                        catch (Exception e)
                                        {
                                            warnings.Add(
                                                $"- Shape {shape.ID.ToString()} {name}: {e.Message}"
                                            );
                                        }
                                        finally
                                        {
                                            Clipboard.Clear();
                                        }
                                    }
                                }
                                MSword.Range rng;
                                foreach (MSword.Range tmprng in currentDoc.StoryRanges)
                                {
                                    rng = tmprng;
                                    while (rng != null)
                                    {
                                        foreach (MSword.InlineShape item in rng.InlineShapes)
                                        {
                                            try
                                            {
                                                string type = item.Type.ToString();
                                                if (
                                                    (
                                                        item.Type.ToString()
                                                        != "wdInlineShapeEmbeddedOLEObject"
                                                    )
                                                    && (
                                                        (
                                                            item.Type.ToString()
                                                            != "wdInlineShapePicture"
                                                        )
                                                    )
                                                )
                                                {
                                                    MSword.Shape shape = item.ConvertToShape();
                                                    string shapeOutputPath = Path.Combine(
                                                        outputPath,
                                                        Path.GetFileNameWithoutExtension(fileName)
                                                            + "-Shape"
                                                            + shape.ID.ToString()
                                                            + ".png"
                                                    );
                                                    try
                                                    {
                                                        byte[] buffer = (byte[])
                                                            item.Range.EnhMetaFileBits;
                                                        convertEmfBufferToPng(
                                                            buffer,
                                                            shapeOutputPath
                                                        );
                                                        Console.WriteLine(
                                                            "Exported inlined shape "
                                                                + shapeOutputPath
                                                        );
                                                        shapesPath.Add(shapeOutputPath);
                                                    }
                                                    catch (ClipboardDataException cde)
                                                    {
                                                        warnings.Add(
                                                            $"- InlineShape {shape.ID.ToString()} with AltText \"{item.AlternativeText.ToString()}\": {cde.Message}"
                                                        );
                                                    }
                                                    catch (Exception e)
                                                    {
                                                        warnings.Add(
                                                            $"- InlineShape {shape.ID.ToString()} with AltText \"{item.AlternativeText.ToString()}\": {e.Message}"
                                                        );
                                                        //throw e;
                                                    }
                                                    finally
                                                    {
                                                        Clipboard.Clear();
                                                    }
                                                }
                                            }
                                            catch (Exception e)
                                            {
                                                warnings.Add(
                                                    $"- InlineShape with AltText \"{item.AlternativeText.ToString()}\" could not be converted to shape: {e.Message}"
                                                );
                                            }
                                        }
                                        rng = rng.NextStoryRange;
                                    }
                                }
                                if (warnings.Count > 0)
                                {
                                    string warningMessage =
                                        "Some shapes could not be exported from the document "
                                        + currentDoc.Name;
                                    foreach (string warning in warnings)
                                    {
                                        warningMessage += "\r\n" + warning;
                                    }
                                    throw new Exception(warningMessage);
                                }
                            }
                            catch (Exception ex)
                            {
                                threadEx = ex;
                            }
                            Console.WriteLine("Done");
                        }
                    );
                    staThread.SetApartmentState(ApartmentState.STA);
                    staThread.Start();
                    staThread.Join();
                    Console.WriteLine("Closing document");
                    currentDoc.Close(SaveChanges: false);
                    if (!app.Visible) {
                        Console.WriteLine("Closing word in background");
                        app.Quit(SaveChanges: false);
                    }
                    if (threadEx != null)
                    {
                        throw threadEx;
                    }
                }
            }
            catch (Exception e)
            {
                string message =
                    "An error occured while preprocessing shapes and may prevent the rest of the conversion to success:";
                Exception t = e;
                while (t != null)
                {
                    message += "\r\n- " + t.Message;
                    t = t.InnerException;
                }
                Console.WriteLine($"{message}\r\n{e.StackTrace}");
            }
            return shapesPath.ToArray();
        }

        #region utils
        static object missing = Type.Missing;

        // to duplicate the current doc and use the copy
        static object doNotAddToRecentFiles = false;
        static object notReadOnly = false;

        // visibility
        // object visible = true;
        static object notVisible = false;
        static object originalFormat = MSword.WdOriginalFormat.wdOriginalDocumentFormat;
        static object format = MSword.WdSaveFormat.wdFormatXMLDocument;

        /// <summary>
        /// Saves the meta file. (source : https://keestalkstech.com/2016/06/rasterizing-emf-files-png-net-csharp/)
        /// </summary>
        /// <param name="source">The source.</param>
        /// <param name="destination">The destination.</param>
        /// <param name="scale">The scale. Default value is 4.</param>
        /// <param name="backgroundColor">Color of the background.</param>
        /// <param name="format">The format. Default is PNG.</param>
        /// <param name="parameters">The parameters.</param>
        public static void SaveMetaFile(
            Stream source,
            Stream destination,
            float scale = 1f,
            Color? backgroundColor = null,
            ImageFormat format = null,
            EncoderParameters parameters = null
        )
        {
            if (source == null)
            {
                throw new ArgumentNullException(nameof(source));
            }
            if (destination == null)
            {
                throw new ArgumentNullException(nameof(destination));
            }

            using (var img = new Metafile(source))
            {
                var f = format ?? ImageFormat.Png;

                //Determine default background color.
                //Not all formats support transparency.
                if (backgroundColor == null)
                {
                    var transparentFormats = new ImageFormat[]
                    {
                        ImageFormat.Gif,
                        ImageFormat.Png,
                        ImageFormat.Wmf,
                        ImageFormat.Emf
                    };
                    var isTransparentFormat = transparentFormats.Contains(f);

                    backgroundColor = isTransparentFormat ? Color.Transparent : Color.White;
                }

                //header contains DPI information
                var header = img.GetMetafileHeader();

                //calculate the width and height based on the scale
                //and the respective DPI
                var width = (int)
                    Math.Round((scale * img.Width / header.DpiX * 100), 0, MidpointRounding.ToEven);
                var height = (int)
                    Math.Round(
                        (scale * img.Height / header.DpiY * 100),
                        0,
                        MidpointRounding.ToEven
                    );

                using (var bitmap = new Bitmap(width, height))
                {
                    using (var g = System.Drawing.Graphics.FromImage(bitmap))
                    {
                        //fills the background
                        g.Clear(backgroundColor.Value);

                        //reuse the width and height to draw the image
                        //in 100% of the square of the bitmap
                        g.DrawImage(img, 0, 0, bitmap.Width, bitmap.Height);
                    }
                    // crop image
                    int xmin = bitmap.Width - 1,
                        xmax = 0;
                    // search min and max x
                    for (int y = 0; y < bitmap.Height; ++y)
                    {
                        for (int x = 0; x <= xmin; ++x)
                        {
                            if (bitmap.GetPixel(x, y).ToArgb() != backgroundColor.Value.ToArgb())
                            {
                                xmin = Math.Min(xmin, x);
                            }
                        }

                        for (int x = bitmap.Width - 1; x >= xmax; --x)
                        {
                            if (bitmap.GetPixel(x, y).ToArgb() != backgroundColor.Value.ToArgb())
                            {
                                xmax = Math.Max(xmax, x);
                            }
                        }
                    }
                    // search min y
                    int ymin = bitmap.Height - 1,
                        ymax = 0;
                    for (int x = 0; x < bitmap.Width; ++x)
                    {
                        for (int y = 0; y <= ymin; ++y)
                        {
                            if (bitmap.GetPixel(x, y).ToArgb() != backgroundColor.Value.ToArgb())
                            {
                                ymin = Math.Min(ymin, y);
                            }
                        }
                        for (int y = bitmap.Height - 1; y >= ymax; --y)
                        {
                            if (bitmap.GetPixel(x, y).ToArgb() != backgroundColor.Value.ToArgb())
                            {
                                ymax = Math.Max(ymax, y);
                            }
                        }
                    }

                    //get codec based on GUID
                    var codec = ImageCodecInfo
                        .GetImageEncoders()
                        .FirstOrDefault(c => c.FormatID == f.Guid);

                    //bitmap.Save(destination, codec, parameters);
                    // cropping result
                    bitmap
                        .Clone(
                            new System.Drawing.Rectangle(xmin, ymin, xmax - xmin, ymax - ymin),
                            bitmap.PixelFormat
                        )
                        .Save(destination, codec, parameters);
                }
            }
        }

        /// <summary>
        /// Save a metafile buffer (from inline shape range) to disk
        /// </summary>
        /// <param name="emfBuffer"></param>
        /// <param name="destinationFilePath"></param>
        public static void convertEmfBufferToPng(byte[] emfBuffer, string destinationFilePath)
        {
            using (var source = new MemoryStream(emfBuffer))
            {
                using (var destination = File.OpenWrite(destinationFilePath))
                {
                    SaveMetaFile(source, destination, 4, Color.White, ImageFormat.Png);
                }
            }
        }
        #endregion
    }
}
