<?php 
	require("authentication.php");
	class Rest {
		
		// return an associative array
		// ['success'] = did the request work
		// ['status-code'] = HTTP status code
		// ['data'] = response data
		public static function get_resource($uri) {
			$auth_uri = Authentication::prepare_authenticated_uri($uri);
			$ch = curl_init();
			curl_setopt($ch, CURLOPT_URL, $auth_uri); 
			curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
			curl_setopt($ch, CURLOPT_TIMEOUT, '10');
			$response = curl_exec($ch);
			$info = curl_getinfo($ch);
			curl_close($ch);
			
			if($info['http_code'] == 200) {
				return array("success" => true, "status-code" => $info['http_code'], "data" => $response);			
			}
			else {
				return array("success" => false, "status-code" => $info['http_code'], "data" => null);
			}
			
		}
		
		// return an associative array
		// ['success'] = did the request work
		// ['status-code'] = HTTP status code
		// ['data'] = response data as XML object
		public static function get_resource_as_xml($uri) {
			$resource = Rest::get_resource($uri);
			
			if ($resource['data'] == null) {
				// $resource[status-code] should give the reason for null data, so just pass it along
				return $resource;
			}
			$xml = new SimpleXMLElement($resource['data']);
			$resource['data'] = $xml;
			return $resource;
		}
	
		// to send raw data as the request, just pass in the raw data
		// return an associative array
		// ['success'] = did the request work
		// ['status-code'] = HTTP status code
		// ['data'] = location of new resource
		public static function post_resource($uri, $data, $content_type){
			$auth_uri = Authentication::prepare_authenticated_uri($uri);
			$ch = curl_init($auth_uri);
			curl_setopt($ch, CURLOPT_POST, 1);
			curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
			
			curl_setopt($ch, CURLOPT_HTTPHEADER, array('Content-Type: ' . $content_type));
			curl_setopt($ch, CURLOPT_POSTFIELDS, $data);
			curl_setopt($ch, CURLOPT_HEADER, true);
			$response = curl_exec($ch);
			$info = curl_getinfo($ch);
			curl_close($ch);
			
			// return the content location header
			if ($info['http_code'] == 201) {
				$parsed = Rest::parse_http_response($response);
				return array("success" => true, "status-code" => $info['http_code'], "data" => $parsed);
			}
			else {
				return array("success" => false, "status-code" => $info['http_code'], "data" => null);
			}
		}
		
		// to send a multipart request, send an array with field names and data 
		// in the form of data['field'] = [rawdata, content_type, encoding, is_file_attachment, filename]
		// return an associative array
		// ['success'] = did the request work
		// ['status-code'] = HTTP status code
		// ['data'] = response data
		public static function post_resource_multipart($uri, $data) {
			$auth_uri = Authentication::prepare_authenticated_uri($uri);
			$ch = curl_init($auth_uri);
			curl_setopt($ch, CURLOPT_POST, 1);
			curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
			
			// do a multipart upload
			$dataarr = Rest::create_multipart($data);
			$headers = $dataarr["headers"];
			$content = $dataarr["content"];
			
			curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
			curl_setopt($ch, CURLOPT_POSTFIELDS, $content);
			curl_setopt($ch, CURLOPT_HEADER, true);
			
			$response = curl_exec($ch);
			$info = curl_getinfo($ch);
			curl_close($ch);
			
			// return the content location header
			if ($info['http_code'] == 201) {
				$parsed = Rest::parse_http_response($response);
				return array("success" => true, "status-code" => $info['http_code'], "data" => $parsed[0]['content-location']);
			}
			else {
				return array("success" => false, "status-code" => $info['http_code'], "data" => null);
			}
			
		}
	
		// return an associative array
		// ['success'] = did the request work
		// ['status-code'] = HTTP status code
		// ['data'] = response data (always null for delete operations)
		public static function delete_resource($uri) {
			$auth_uri = Authentication::prepare_authenticated_uri($uri);
			
			$ch = curl_init();
			curl_setopt($ch, CURLOPT_URL, $auth_uri); 
			curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
			curl_setopt($ch, CURLOPT_TIMEOUT, '3');
			curl_setopt($ch, CURLOPT_CUSTOMREQUEST, 'DELETE');
			curl_exec($ch);
			$info = curl_getinfo($ch);
			
			curl_close($ch);
			if ($info['http_code'] == 204) {
				return array("success" => true, "status-code" => $info['http_code'], "data" => null);
			}
			else {
				return array("success" => false, "status-code" => $info['http_code'], "data" => null);
			}
		}
		
		// data_array is an associative array indexed by form field name
		// it contains associative arrays
		//$data_array['field'] = ("response" => raw data, 
		//						  "content-type" => type, 
		//						  "encoding" => character encoding, 
		// 						  "is-file-attachment" => true/false
		//						  "filename" => filename)
		private static function create_multipart($data_array) {

			$eol = "\r\n";
			
			$boundary = md5(time());
			$headers = array("Content-Type: multipart/form-data; boundary=" . $boundary);
			
			foreach ($data_array as $field => $data) {
				
				// the raw post data should be properly encoded according to its $encoding value
				$rawdata = $data['data'];
				
				// e.g. 
				// "text/plain; charset=iso-8859-1" or 
				// "application/zip"
				$type = $data['content-type'];
				
				// e.g. 
				// "7bit"
				// "base64"
				$encoding = $data['encoding'];
				
				// true or false
				$is_file_attachment = $data['is-file-attachment'];
				
				//e.g.
				// myfile.zip
				// null
				$filename = $data['filename'];
				
				$content .= "--" . $boundary . $eol;
				
				if ($is_file_attachment) {
					$content .= "Content-Disposition: form-data; name=\"" . $field . "\"; filename=\"" . $filename . "\"" . $eol;
				}
				else {
					$content .= "Content-Disposition: form-data; name=\"" . $field . "\"" . $eol;
				}
				
				$content .= "Content-Type: " . $type . $eol;
				$content .= "Content-Transfer-Encoding: " . $encoding . $eol;
				
				$content .= $eol;
				
				if ($encoding == "base64") {
					$content .= chunk_split($rawdata) . $eol;
				}
				else {
					$content .= $rawdata . $eol;
				}
			}
			$content .= "--" . $boundary . "--" .  $eol;
			$content .= $eol;
			
			return array("headers" => $headers, "content" => $content);
		}
		
		// from http://snipplr.com/view/17242/
		private static function parse_http_response($string)
	    {
	    	$headers = array();
	    	$content = '';
	    	$str = strtok($string, "\n");
	    	$h = null;
	    	while ($str !== false) {
	    		if ($h and trim($str) === '') {
	    			$h = false;
	    			continue;
	    		}
	    		if ($h !== false and false !== strpos($str, ':')) {
	    			$h = true;
		    		list($headername, $headervalue) = explode(':', trim($str), 2);
		    		$headername = strtolower($headername);
		    		$headervalue = ltrim($headervalue);
		    		if (isset($headers[$headername]))
		    			$headers[$headername] .= ',' . $headervalue;
		    		else
		    			$headers[$headername] = $headervalue;
		    	}
	    		if ($h === false) {
	    			$content .= $str."\n";
	    		}
	    		$str = strtok("\n");
	    	}
	    	return array($headers, trim($content));
	    }
	
		private static function array_implode($glue, $separator, $array) {
		    if (!is_array($array)) return $array;
		    $string = array();
		    foreach ($array as $key => $val) {
		        if (is_array($val))
		            $val = implode(',', $val);
		        $string[] = "{$key}{$glue}{$val}";
		    }
		    return implode($separator, $string);
		}
	}
?>
