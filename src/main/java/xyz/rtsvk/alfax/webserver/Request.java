package xyz.rtsvk.alfax.webserver;

import xyz.rtsvk.alfax.webserver.contentparsing.Content;

import java.util.HashMap;
import java.util.Map;

public class Request {

	private String requestMethod;
	private String path;
	private String protocolVersion;

	private Map<String, Object> requestHeader;
	private Map<String, Object> requestBody;

	public static Request parse(String raw, Map<String, Content> supportedContentTypes) {
		if (raw.length() == 0) return null;
		Request request = new Request();

		raw = raw.replace("\r", "");
		String[] sections = raw.split("\n\n");

		String[] lines = sections[0].split("\n");
		String[] head = lines[0].split(" ");

		request.setRequestMethod(head[0]);
		request.setPath(head[1]);
		request.setProtocolVersion(head[2]);

		request.requestHeader = new HashMap<>();

		for (int i = 1; i < lines.length; i++) {
			lines[i] = lines[i].replace(" ", "");
			lines[i] = lines[i].toLowerCase();

			int endIdx = lines[i].indexOf(";");
			endIdx = endIdx > 0 ? endIdx : lines[i].length();

			request.requestHeader.put(
					lines[i].substring(0, lines[i].indexOf(":")),
					lines[i].substring(lines[i].indexOf(":")+1, endIdx)
			);
		}

		String contentType = request.getHeaderProperty("content-type", "undefined").toString();
		System.out.println("debug content-type: " + contentType);
		Content c = supportedContentTypes.get(contentType);
		int length = Integer.parseInt(request.getHeaderProperty("content-length", 0).toString());
		System.out.println("debug content-length: " + length);

		if (c != null && length > 0 && sections.length > 1)
			request.setRequestBody(c.parse(sections[1].replace("\n", "").substring(0, length)));


		return request;
	}

	public String getRequestMethod() {
		return requestMethod;
	}

	private void setRequestMethod(String requestMethod) {
		this.requestMethod = requestMethod;
	}

	public String getPath() {
		return path;
	}

	private void setPath(String path) {
		this.path = path;
	}

	public String getProtocolVersion() {
		return protocolVersion;
	}

	private void setProtocolVersion(String protocolVersion) {
		this.protocolVersion = protocolVersion;
	}

	public Object getHeaderProperty(String key) {
		return this.requestHeader.get(key);
	}

	public Object getHeaderProperty(String key, Object def) {
		return this.requestHeader.getOrDefault(key, def);
	}

	public Map<String, Object> getHeaders() {
		return this.requestHeader;
	}

	public Object getProperty(String key) {
		return this.requestBody.get(key);
	}

	public Map<String, Object> getProperties() {
		return this.requestBody;
	}

	private void setRequestBody(Map<String, Object> requestBody) {
		this.requestBody = requestBody;
	}
}
