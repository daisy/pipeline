package utils;

public class FileInfo {
	
	public String href;
	public String contentType;
	public Long size;
	
	public FileInfo(String href, String contentType, long size) {
		this.href = href;
		this.contentType = contentType;
		this.size = size;
	}
	
}