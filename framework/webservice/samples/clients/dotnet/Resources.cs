using System;
using System.Collections.Generic;
using System.Xml;
using System.IO;
using System.Net;

namespace PipelineWSClient
{
	class Resources
	{
		private static string JOB_REQUEST = "job-request";
		private static string JOB_DATA = "job-data";
		
		public static string baseUri = "http://localhost:8181/ws";
		
		public static XmlDocument GetScript(string id)
		{
			string uri = String.Format("{0}/scripts/{1}", baseUri, id);
			return Rest.GetResourceAsXml(uri);
		}	
		
		public static XmlDocument GetScripts()
		{
			string uri = String.Format("{0}/scripts", baseUri);
			return Rest.GetResourceAsXml(uri);
		}
		
		public static XmlDocument GetJob(string id)
		{
			string uri = String.Format("{0}/jobs/{1}", baseUri, id);
			return Rest.GetResourceAsXml(uri);
		}
		
		public static XmlDocument GetJobs()
		{
			string uri = String.Format("{0}/jobs", baseUri);
			return Rest.GetResourceAsXml(uri);
		}
		
		public static string GetLog(string id)
		{
			string uri = String.Format("{0}/jobs/{1}/log", baseUri, id);
			return Rest.GetResource(uri);
		}
		
		public static void GetResult(string id, string filepath)
		{
			string uri = String.Format("{0}/jobs/{1}/result", baseUri, id);
			string authUri = Authentication.PrepareAuthenticatedUri(uri);
			using (var client = new WebClient())
			{
				client.DownloadFile(authUri, filepath);
			}
		}
		
		public static bool DeleteJob(string id)
		{
			string uri = String.Format("{0}/jobs/{1}", baseUri, id);
			return Rest.DeleteResource(uri);
		}
		
		public static XmlDocument PostJob(string request, FileInfo data = null)
		{
			string uri = String.Format("{0}/jobs", baseUri);
			if (data == null)
			{
				return Rest.PostResource(uri, request);
			}
			else
			{
				Dictionary<string, string> postData = new Dictionary<string, string>();
				postData.Add(JOB_REQUEST, request);
				string fileMimeType = "application/zip";
				string fileFormKey = JOB_DATA;
				return Rest.PostResource(uri, postData, data, fileMimeType, fileFormKey);			
			}
		}	
		public static XmlDocument Alive()
		{
			string uri = String.Format("{0}/alive", baseUri);
			return Rest.GetResourceAsXml(uri);
		}

		public static void Halt()
		{
			string path = String.Format ("{0}dp2key.txt", GetTempDir());
			string key = File.ReadAllText(path);
			string uri = String.Format("{0}/admin/halt/{1}", baseUri, key);
			Rest.GetResource(uri);
		}


		// returns "DONE", "IDLE", or "RUNNING"
		// status isn't a core pipeline resource, but it's useful nonetheless
		public static string GetJobStatus(string id)
		{
			
			XmlDocument doc = GetJob(id);
			XmlNamespaceManager manager = new XmlNamespaceManager(doc.NameTable);
			manager.AddNamespace("ns", "http://www.daisy.org/ns/pipeline/data");

			XmlNode node = doc.SelectSingleNode("//ns:job", manager);
			return node.Attributes.GetNamedItem("status").Value;
		}
		
		private static string XmlDocToString(XmlDocument doc)
		{
			StringWriter stringWriter = new StringWriter();
			XmlTextWriter textWriter = new XmlTextWriter(stringWriter);
			doc.WriteTo(textWriter);
			string retval = stringWriter.ToString();
			return retval;
		}

		private static string GetTempDir()
		{
            String dir;
			int p = (int) Environment.OSVersion.Platform;
			if ((p == 4) || (p == 6) || (p == 128)) 
			{
				// Running on Unix
				dir = System.Environment.GetEnvironmentVariable("TMPDIR");
			} 
			else 
			{
				// NOT running on Unix
				dir = System.Environment.GetEnvironmentVariable("TEMP");
			}
            if (!dir.EndsWith("/"))
            {
                dir += "/";
            }
            return dir;
		}
	}
}

