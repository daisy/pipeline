using System;
using System.Xml;
using System.Collections.Generic;
using System.IO;
using NDesk.Options;

namespace PipelineWSClient
{
	/*
	 * Run the EXE from directly within Windows, e.g.: PipelineWSClient.exe jobs
	 * or
	 * From Mac/Linux via Mono, e.g.: mono PipelineWSClient.exe jobs
	 */ 
	class MainClass
	{
		private static List<string> commands = new List<string>{"scripts", "script", "jobs", "job", "log", "result", "delete-job", "new-job"};
		public static void Main (string[] args)
		{
			string id = null;
   			bool help = false;
			string jobRequestFile = null;
			string jobDataFile = null;
			OptionSet opts = new OptionSet () {
				{ "id=", "ID of Job or Script", v => id = v },
				{ "job-request=", "XML file representing the job request", v => jobRequestFile = v},
				{ "job-data=", "Zip file containing the job data", v => jobDataFile = v},
				{ "help", "Show this message", v => help = v != null },
				
			};
   			List<string> cmds = opts.Parse(args);

			//PostJob ("/Users/marisa/Projects/pipeline2/pipeline-framework/webservice/samples/clients/testdata/job3.request.localmode.xml", null);
			//return;

			if (help || cmds.Count == 0)
			{
				ShowUsage(opts);
				return;
			}
			
			string command = cmds[0];
			
			if (id == null && 
				(command == "script" || command == "job" || command == "log" || command == "result" || command == "delete-job"))
			{
				Console.WriteLine(String.Format("The command {0} must have an id parameter.", command));
				ShowUsage(opts);
				return;
			}
			
			if (jobRequestFile == null && command == "new-job")
			{
				Console.WriteLine(String.Format("The command {0} must have a job-request parameter, and may also have a job-data parameter.", command));
				ShowUsage(opts);
				return;
			}
		
			
			if (command == "scripts")
			{
				GetScripts();
			}
			else if (command == "script")
			{
				GetScript(id);
			}
			else if (command == "jobs")
			{
				GetJobs();
			}
			else if (command == "job")
			{
				GetJob(id);
			}
			else if (command == "log")
			{
				GetLog(id);
			}
			else if (command == "result")
			{
				GetResult(id);
			}
			else if (command == "delete-job")
			{
				DeleteJob(id);
			}
			else if (command == "new-job")
			{
				if (jobDataFile == null)
				{
					PostJob(jobRequestFile);
				}
				else 
				{
					PostJob(jobRequestFile, jobDataFile);
				}
			}
			else if (command == "alive") 
			{
				Alive();
			}
			else if (command == "halt")
			{
				Halt();
			}
			else
			{
				Console.WriteLine ("Command not recognized.");
				ShowUsage(opts);
				return;
			}
		}
		
		private static void ShowUsage(OptionSet opts)
		{
			Console.WriteLine("Usage: PipelineWSClient [COMMAND] [OPTIONS]+ ");
			Console.WriteLine();
			Console.WriteLine("Commands:");
			foreach (string s in commands)
			{
				Console.WriteLine(String.Format("\t{0}", s));
			}
			Console.WriteLine();
        	Console.WriteLine("Options:");
        	opts.WriteOptionDescriptions(Console.Out);
			
			Console.WriteLine();
			Console.WriteLine("Examples:");
			Console.WriteLine("Show all scripts: \n\tPipelineWSClient.exe scripts");
			Console.WriteLine("Show a specific script: \n\tPipelineWSClient.exe script --id=http://www.daisy.org/pipeline/modules/dtbook-to-zedai/dtbook-to-zedai.xpl");
			Console.WriteLine("Show a specific job: \n\tPipelineWSClient.exe job --id=873ce8d7-0b92-42f6-a2ed-b5e6a13b8cd7");
			Console.WriteLine("Create a job: \n\tPipelineWSClient.exe new-job --job-request=../../../testdata/job1.request.xml");
			Console.WriteLine("Create a job: \n\tPipelineWSClient.exe new-job --job-request=../../../testdata/job2.request.xml --job-data=../../../testdata/job2.data.zip");
		}
		
		
		public static void GetScripts()
		{
			XmlDocument doc = Resources.GetScripts();
			PrintDoc (doc);
		}
		
		public static void GetScript(string id)
		{
			XmlDocument doc = Resources.GetScript(id);
			PrintDoc (doc);
		}
		
		public static void PostJob(string jobRequestFilepath, string jobDataFilepath = null)
		{
			StreamReader reader = new StreamReader(jobRequestFilepath);
			string doc = reader.ReadToEnd();
			reader.Close();

			FileInfo data = null;
			if (jobDataFilepath !=  null)
			{
				data = new FileInfo(jobDataFilepath);
			}
			XmlDocument jobDoc = Resources.PostJob (doc, data);

			if (jobDoc == null)
			{
				Console.WriteLine("Job not created.");
				return;
			}
			Console.WriteLine("Job created:");
			PrettyPrint (jobDoc);
		}
		
		public static void GetJobs()
		{
			XmlDocument doc = Resources.GetJobs();
			PrintDoc (doc);			
		}
		
		public static void GetJob(string id)
		{
			XmlDocument doc = Resources.GetJob(id);
			PrintDoc(doc);
		}
		
		public static void GetLog(string id)
		{
			string status = Resources.GetJobStatus(id);
			if (status != "DONE")
			{
				Console.WriteLine (String.Format("Cannot get log until job is done.  Job status: {0}.", status));
				return;
			}
			
			string log = Resources.GetLog(id);
			if (log.Length == 0)
			{
				Console.WriteLine("No data returned.");
				return;
			}
			Console.WriteLine(log);
		}
		
		public static void GetResult(string id)
		{
			string status = Resources.GetJobStatus(id);
			if (status != "DONE")
			{
				Console.WriteLine (String.Format("Cannot get result until job is done.  Job status: {0}.", status));
				return;
			}
			string filepath = String.Format("/tmp/{0}.zip", id);
			Resources.GetResult(id, filepath);
			
			Console.WriteLine(String.Format("Saved to {0}", filepath));
		}
		
		public static void DeleteJob(string id)
		{
			string status = Resources.GetJobStatus(id);
			if (status != "DONE")
			{
				Console.WriteLine (String.Format("Cannot delete until job is done.  Job status: {0}.", status));
				return;
			}
			
			bool result = Resources.DeleteJob(id);
			if (!result)
			{
				Console.WriteLine("Error deleting job.");
			}
			else
			{
				Console.WriteLine ("Job deleted.");
			}
		}
		public static void Halt() 
		{
			Resources.Halt();
		}
		public static void Alive() 
		{
			XmlDocument doc = Resources.Alive ();
			PrintDoc (doc);
		}
		public static void PrintDoc(XmlDocument doc)
		{
			if (doc == null)
			{
				Console.WriteLine("No data.");
				return;
			}
			PrettyPrint(doc);
		}
		public static void PrettyPrint(XmlDocument doc)
		{
			using (StringWriter stringWriter = new StringWriter())
			{
				XmlNodeReader xmlReader = new XmlNodeReader(doc);
				XmlTextWriter xmlWriter = new XmlTextWriter(stringWriter);
				xmlWriter.Formatting = Formatting.Indented;
				xmlWriter.Indentation = 1;
				xmlWriter.IndentChar = '\t';
				xmlWriter.WriteNode(xmlReader, true);
				Console.WriteLine(stringWriter.ToString());
			}
		}
	}
}
