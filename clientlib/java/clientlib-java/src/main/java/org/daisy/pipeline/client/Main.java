package org.daisy.pipeline.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.daisy.pipeline.client.models.Job;
import org.daisy.pipeline.client.models.Message;
import org.daisy.pipeline.client.utils.XML;
import org.w3c.dom.Document;

/**
 * Running the clientlib from the commandline, mainly for testing purposes.
 */
public class Main {
	
	public static void main(String[] args) {
		switch (args.length > 0 ? args[0] : "") {
		case "job-xml-to-progress-tree":
			jobXmlToProgressTree(args);
			break;
			
		default:
			if (args.length > 0 && !"".equals(args[0])) {
				System.out.println("'"+args[0]+" is not a valid command.\n\n");
			}
			System.out.println("Available commands:\n"
							   +"    job-xml-to-progress-tree infile?    # if infile is not specified, stdin is used\n");
			System.exit(1);
		}
	}
	
	public static void jobXmlToProgressTree(String[] args) {
		String infile = args.length > 1 ? args[1] : null;
		
		String input = "";
		if (infile == null) {
			// read from stdin
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				String line;
				while (br.ready() && (line=br.readLine()) != null) {
					input += line;
				}
					
			} catch (IOException e){
				e.printStackTrace();
				System.exit(1);
			}
			
		} else {
			// read from file
			try {
				List<String> lines = Files.readAllLines(new File(infile).toPath());
				for (String line : lines) {
					input += line;
				}
				
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		
		Document jobXml = XML.getXml(input);
		Job job = null;
		try {
			job = new Job(jobXml);
			
		} catch (Pipeline2Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		Job progressJob = new Job();
		List<Message> messages = new ArrayList<Message>();
		for (Message m : job.getMessages()) {
			messages.add(m);
			progressJob.setMessages(messages);
			String currentProgressName = progressJob.getProgressStack().peek().getName();
			String indent = "";
			for (int depth = 0; depth < m.depth; depth++) {
				indent += "    ";
			}
			if (m.text.contains("[progress")) {
				// Yes this is a bit ugly and could be done better. I tried formatting with %2.2f and %5.2f but I couldn't get it working properly so I split the from/to numbers manually instead 
				System.out.format("%2d.%2s - %2d.%2s  <%-20s>(%s)  "+indent+"%s\n",
						new Double(Math.floor(progressJob.getProgressFrom())).intValue(),
						((""+progressJob.getProgressFrom()).contains(".") || (""+progressJob.getProgressFrom()).contains(",")) ? (progressJob.getProgressFrom()+"00").split("[\\.,]", 2)[1].substring(0, 2) : "00",
						new Double(Math.floor(progressJob.getProgressTo())).intValue(),
						((""+progressJob.getProgressTo()).contains(".") || (""+progressJob.getProgressTo()).contains(",")) ? (progressJob.getProgressTo()+"00").split("[\\.,]", 2)[1].substring(0, 2) : "00",
						currentProgressName.length() > 20 ? currentProgressName.substring(0, 19)+"…" : currentProgressName,
						m.depth < 10 ? m.depth+"" : "+",
						m.text);
			} else {
				System.out.println("                                          "+indent+m.text);
			}
		}
	}

}
