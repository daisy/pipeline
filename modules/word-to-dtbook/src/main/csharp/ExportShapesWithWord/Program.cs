using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Imaging;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Windows.Forms;
using MSword = Microsoft.Office.Interop.Word;

namespace Daisy.SaveAsDAISY.Exporter
{

    internal class Program
    {

        static int Main(string[] args)
        {
            FileInfo inputFile;
            DirectoryInfo outputFolder;
            try
            {
                if (args.Length == 0)
                {
                    return 1;
                }
                else
                {
                    string input = args[0];
                    if (input.StartsWith("file:/"))
                    {
                        input = new Uri(input).LocalPath;
                    }
                    inputFile = new FileInfo(input);
                    if (!inputFile.Exists)
                    {
                        Console.Error.WriteLine(string.Format("{0} not found", inputFile.FullName));
                        return 2;
                    }
                    if (args.Length == 1)
                    {
                        outputFolder = Directory.CreateDirectory(
                            Path.Combine(
                                inputFile.DirectoryName,
                                Path.GetFileNameWithoutExtension(inputFile.FullName)
                            )
                        );
                    }
                    else
                    {
                        string output = args[1];
                        if (output.StartsWith("file:/"))
                        {
                            output = new Uri(output).LocalPath;
                        }
                        outputFolder = Directory.CreateDirectory(output);
                    }
                }
                try
                {
                    IShapesExporter exporter = new ShapesExporter();
                    exporter.processShapes(inputFile.FullName, outputFolder.FullName);

                    return 0;
                }
                catch (Exception ex)
                {
                    Console.Error.WriteLine("An error occured while trying to export shapes");
                    Console.Error.WriteLine(ex.Message);
                    Console.Error.WriteLine(ex.StackTrace);
                    return 253;
                }
            } catch (Exception ex)
            {
                Console.Error.WriteLine("An error occured while parsing arguments");
                Console.Error.WriteLine(ex.Message);
                Console.Error.WriteLine(ex.StackTrace);
                return 254;
            }
            

        }
    }
}
