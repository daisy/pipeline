using System;
using System.Collections.Generic;
using System.IO;
using System.Web;
using System.Text;

namespace PipelineWSClient
{
	/* the following functions are from 
	 * http://www.paraesthesia.com/archive/2009/12/16/posting-multipartform-data-using-.net-webrequest.aspx
	 */
		
	public class MultipartBoundary
	{
		/// <summary>
		/// Creates a multipart/form-data boundary.
		/// </summary>
		/// <returns>
		/// A dynamically generated form boundary for use in posting multipart/form-data requests.
		/// </returns>
		public static string CreateFormDataBoundary()
		{
			return "---------------------------" + DateTime.Now.Ticks.ToString("x");
		}
	}
	/// <summary>
	/// Extension methods for generic dictionaries.
	/// </summary>
	public static class MultipartDictionaryExtensions
 	{
	    /// <summary>
	    /// Template for a multipart/form-data item.
	    /// </summary>
	    public const string FormDataTemplate = "--{0}\r\nContent-Disposition: form-data; name=\"{1}\"\r\n\r\n{2}\r\n";
	
	    /// <summary>
	    /// Writes a dictionary to a stream as a multipart/form-data set.
	    /// </summary>
	    /// <param name="dictionary">The dictionary of form values to write to the stream.</param>
	    /// <param name="stream">The stream to which the form data should be written.</param>
	    /// <param name="mimeBoundary">The MIME multipart form boundary string.</param>
	    /// <exception cref="System.ArgumentNullException">
	    /// Thrown if <paramref name="stream" /> or <paramref name="mimeBoundary" /> is <see langword="null" />.
	    /// </exception>
	    /// <exception cref="System.ArgumentException">
	    /// Thrown if <paramref name="mimeBoundary" /> is empty.
	    /// </exception>
	    /// <remarks>
	    /// If <paramref name="dictionary" /> is <see langword="null" /> or empty,
	    /// nothing wil be written to the stream.
	    /// </remarks>
	    public static void WriteMultipartFormData(this Dictionary<string, string> dictionary, Stream stream, string mimeBoundary)
	    {
			if (dictionary == null || dictionary.Count == 0)
			{
				return;
			}
			if (stream == null)
			{
				throw new ArgumentNullException("stream");
			}
			if (mimeBoundary == null)
			{
				throw new ArgumentNullException("mimeBoundary");
			}
			if (mimeBoundary.Length == 0)
			{
				throw new ArgumentException("MIME boundary may not be empty.", "mimeBoundary");
			}
			foreach (string key in dictionary.Keys)
			{
				string item = String.Format(FormDataTemplate, mimeBoundary, key, dictionary[key]);
				byte[] itemBytes = System.Text.Encoding.UTF8.GetBytes(item);
				stream.Write(itemBytes, 0, itemBytes.Length);
			}
		}
	}
	
	/// <summary>
	/// Extension methods for <see cref="System.IO.FileInfo"/>.
	/// </summary>
	public static class MultipartFileInfoExtensions
	{
		/// <summary>
		/// Template for a file item in multipart/form-data format.
		/// </summary>
		public const string HeaderTemplate = "--{0}\r\nContent-Disposition: form-data; name=\"{1}\"; filename=\"{2}\"\r\nContent-Type: {3}\r\n\r\n";

	    /// <summary>
	    /// Writes a file to a stream in multipart/form-data format.
	    /// </summary>
	    /// <param name="file">The file that should be written.</param>
	    /// <param name="stream">The stream to which the file should be written.</param>
	    /// <param name="mimeBoundary">The MIME multipart form boundary string.</param>
	    /// <param name="mimeType">The MIME type of the file.</param>
	    /// <param name="formKey">The name of the form parameter corresponding to the file upload.</param>
	    /// <exception cref="System.ArgumentNullException">
	    /// Thrown if any parameter is <see langword="null" />.
	    /// </exception>
	    /// <exception cref="System.ArgumentException">
	    /// Thrown if <paramref name="mimeBoundary" />, <paramref name="mimeType" />,
	    /// or <paramref name="formKey" /> is empty.
	    /// </exception>
	    /// <exception cref="System.IO.FileNotFoundException">
	    /// Thrown if <paramref name="file" /> does not exist.
	    /// </exception>
	    public static void WriteMultipartFormData(this FileInfo file, Stream stream, string mimeBoundary, string mimeType, string formKey)
	    {
			if (file == null)
			{
				throw new ArgumentNullException("file");
			}
			if (!file.Exists)
			{
				throw new FileNotFoundException("Unable to find file to write to stream.", file.FullName);
			}
			if (stream == null)
			{
				throw new ArgumentNullException("stream");
			}
			if (mimeBoundary == null)
			{
				throw new ArgumentNullException("mimeBoundary");
			}
			if (mimeBoundary.Length == 0)
			{
				throw new ArgumentException("MIME boundary may not be empty.", "mimeBoundary");
			}
			if (mimeType == null)
			{
				throw new ArgumentNullException("mimeType");
			}
			if (mimeType.Length == 0)
			{
				throw new ArgumentException("MIME type may not be empty.", "mimeType");
			}
			if (formKey == null)
			{
				throw new ArgumentNullException("formKey");
			}
			if (formKey.Length == 0)
			{
				throw new ArgumentException("Form key may not be empty.", "formKey");
			}
			string header = String.Format(HeaderTemplate, mimeBoundary, formKey, file.Name, mimeType);
			byte[] headerbytes = Encoding.UTF8.GetBytes(header);
			stream.Write(headerbytes, 0, headerbytes.Length);
			using (FileStream fileStream = new FileStream(file.FullName, FileMode.Open, FileAccess.Read))
			{
				byte[] buffer = new byte[1024];
				int bytesRead = 0;
				while ((bytesRead = fileStream.Read(buffer, 0, buffer.Length)) != 0)
				{
			  		stream.Write(buffer, 0, bytesRead);
				}
				fileStream.Close();
			}
			byte[] newlineBytes = Encoding.UTF8.GetBytes("\r\n");
			stream.Write(newlineBytes, 0, newlineBytes.Length);
		}
	}
}

