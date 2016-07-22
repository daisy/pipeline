# takes care of all the rest calls
require 'net/http'
require 'uri'
require 'nokogiri'
require './multipart.rb'
require './authentication'

module Rest
  module_function

  def get_resource(uri)
    begin
      authUri = URI.parse(Authentication.prepare_authenticated_uri(uri))
      response = Net::HTTP.get_response(authUri)

      puts "Response was #{response}"

      case response
        when Net::HTTPSuccess
          return response.body
        when Net::HTTPNoContent
          return "success"
        when Net::HTTPInternalServerError
          return nil
        else
          return nil
      end

    rescue
      puts "Error: GET #{uri.to_s} failed."
      return nil
    end
  end

  def get_resource_as_xml(uri)
    resource = get_resource(uri)
    return xml_from_string(resource)
  end
  
  def xml_from_string(str)
    if str == nil
      return nil
    end
    doc = Nokogiri.XML(str) do |config|
      config.default_xml.noblanks
    end
    return doc
  end

  def post_resource(uri, data)
    begin
      authUri = URI.parse(Authentication.prepare_authenticated_uri(uri))
      request = Net::HTTP::Post.new(authUri.request_uri)

      # if this is a hash, treat it like a multipart request
      if data.respond_to?('keys') == true
        mp = Multipart::MultipartPost.new
        query, headers = mp.prepare_query(data)
        response = post_form(authUri, query, headers)

      # else upload the data by itself
      else
        request.body = data
        response = Net::HTTP.start(authUri.host, authUri.port) {|http| http.request(request)}
      end
      puts "Response was #{response}"

      case response
        when Net::HTTPCreated
          puts "success"
          return xml_from_string(response.body)
        when Net::HTTPInternalServerError
          return nil
        else
          return nil
      end
    rescue
      puts "Error: POST #{uri.to_s} failed."
      return nil
    end
  end
	
	def put_resource(uri, data)
		begin
			authUri = URI.parse(Authentication.prepare_authenticated_uri(uri))
	    request = Net::HTTP::Put.new(authUri.request_uri)

	    request.body = data
	    response = Net::HTTP.start(authUri.host, authUri.port) {|http| http.request(request)}
			puts "Response was #{response}"

    	case response
	      when Net::HTTPSuccess
	        return xml_from_string(response.body)
	      when Net::HTTPInternalServerError
	        return nil
	      else
	        return nil
	    end
  	rescue
    	puts "Error: POST #{uri.to_s} failed."
	    return nil
		end
	end

  def delete_resource(uri)
    begin
      authUri = URI.parse(Authentication.prepare_authenticated_uri(uri))
      request = Net::HTTP::Delete.new(authUri.request_uri)
      response = Net::HTTP.start(authUri.host, authUri.port) {|http| http.request(request)}

      puts "Response was #{response}"

      case response
        when Net::HTTPNoContent
          return true
        when Net::HTTPInternalServerError
          return false
        else
          return false
      end
    rescue
      error("Error: DELETE #{uri.to_s} failed.")
      return false
    end

  end

  def post_form(url, query, headers)
    Net::HTTP.start(url.host, url.port) {|con|
      con.read_timeout = Settings::TIMEOUT_SECONDS
      begin
        return con.post(url.request_uri, query, headers)
      rescue => e
        puts "POST Failed #{e}... #{Time.now}"
      end
    }
  end

end