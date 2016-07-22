<?php
	header("Cache-Control: no-cache, must-revalidate"); // HTTP/1.1
	require("resources.php");

	function get_scripts() {
		$result = Resources::get_scripts();
		handle_xml_result($result);
	}

	function get_script($id) {

		$result = Resources::get_script($id);
		handle_xml_result($result);
	}

	function get_jobs() {
		$result = Resources::get_jobs();
		handle_xml_result($result);
	}

	function get_job($id) {
		$result = Resources::get_job($id);
		handle_xml_result($result);
	}

	function get_log($id) {
		$result = Resources::get_log($id);
		
		if ($result["success"] == false) {
			show_message("Operation failed with status code " . $result['status-code']);
			return;
		}
		
		download_file($result['data'], $id . ".log", "text/plain");
	}

	function get_result($id) {
		$result = Resources::get_result($id);
		
		if ($result["success"] == false) {
			show_message("Operation failed with status code " . $result['status-code']);
			return;
		}	
		
		download_file($result['data'], $id . ".zip", "application/zip");
	}

	function post_job($job_request_filename, $job_data_filename) {
	
		$job_request = file_get_contents($job_request_filename);
		
		$job_data = null;
		if ($job_data_filename != null) {
			$fh = fopen($job_data_filename, "rb");
			if ($fh) {
				$job_data = fread($fh, filesize($job_data_filename));
				fclose($fh);
			}
		}
		
	
		$result = Resources::post_job($job_request, $job_data);
		
		if ($result['success'] == false) {
			show_message("Operation failed with status code " . $result['status-code']);
			return;
		}
		
		show_message("New job created: " . implode("\n", $result['data']));
	}

	function delete($id) {
		$result = Resources::delete($id);
		
		if ($result["success"] == false) {
			show_message("Operation failed with status code " . $result['status-code']);
			return;
		}
		
		show_message("Job deleted.");
	}
	function handle_xml_result($result) {
		if ($result["success"] == false) {
			show_message("Operation failed with status code " . $result['status-code']);
			return;
		}	
		
		show_xml($result['data']);
	}
	
    function show_xml($xml) {
		if ($xml == NULL) {
			show_message("No data returned");
			return;
		} 
		// unfortunately not pretty-printed...
		echo $xml->asXML();
	}
	
	function download_file($file_contents, $filename, $content_type) {
		header("Content-Type: " . $content_type);  
		header('Content-Disposition: attachment; filename="' . $filename . '"');
		header("Content-Transfer-Encoding: binary");
		header('Expires: 0');
		header('Pragma: no-cache');
		header('Content-Length: ' . strlen($file_contents));
		echo $file_contents;
	}
	
	function show_message($text) {
		//header("Content-Type: text/html");
		//echo "<p>" . $text . "</p>";
		echo $text . "\n";
	}
	
	function show_usage() {
		echo "Examples \n";
		echo "List all scripts: \n\tmain.php scripts \n";
		echo "List one script: \n\tmain.php scripts <scriptID> \n";
		echo "List all jobs: \n\tmain.php jobs \n";
		echo "List one job: \n\tmain.php jobs <jobID>\n";
		echo "Create a job: \n\tmain.php new-job <job-request.xml> <job-data.zip>\n";
		echo "Get the job's log: \n\tmain.php log <jobID>\n";
		echo "Get the job's result: \n\tmain.php result <jobID>\n";
		echo "Delete a job: \n\tmain.php delete <jobID>\n";
		echo "Is the Pipeline2 alive: \n\tmain.php alive\n";
	}
	function alive() {
		$result = Resources::alive();
		handle_xml_result($result);
	}
	
	// ********************************************************
	// MAIN
	//*********************************************************
	if (count($argv) < 2) {
		echo "Command required.\n";
		show_usage();
		return;
	}

	$cmd = $argv[1];
	
	if ($cmd == "scripts") {
		if (count($argv) > 2) {
			get_script($argv[2]);
		}
		else {
			get_scripts();
		}
	}
	else if ($cmd == "jobs") {
		if (count($argv) > 2) {
			get_job($argv[2]);
		}
		else {
			get_jobs();
		}
	}
	else if ($cmd == "new-job") {
		if (count($argv) == 2) {
			echo "Error: more arguments required.";
			return;
		}
		if (count($argv) == 3) {
			post_job($argv[2], null);
		}
		else if (count($argv) == 4) {
			post_job($argv[2], $argv[3]);
		}
	}
	else if ($cmd == "log") {
		if (count($argv) == 2) {
			echo "Error: more arguments required.";
			return;	
		}
		get_log($argv[2]);
	}
	else if ($cmd == "result") {
		if (count($argv) == 2) {
			echo "Error: more arguments required.";
			return;	
		}
		get_result($argv[2]);
	}
	else if ($cmd == "delete") {
		if (count($argv) == 2) {
			echo "Error: more arguments required.";
			return;	
		}
		delete($argv[2]);
	}
	else if ($cmd == "alive") {
		alive();
	}
	else {
		echo "Command not recognized.\n";
		show_usage();
	}
	
?>
