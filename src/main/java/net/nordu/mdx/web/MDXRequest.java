package net.nordu.mdx.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.nordu.mdx.MDXServer;

public class MDXRequest {

	private String[] tags;
	private MDXServer server;
	private String baseURL;
	
	private String baseURL(HttpServletRequest req) {
		String scheme = req.getScheme(); 
		String serverName = req.getServerName();
		int serverPort = req.getServerPort();
		String contextPath = req.getContextPath();
		String servletPath = req.getServletPath();
		String pathInfo = req.getPathInfo(); 
		String queryString = req.getQueryString(); // d=789 // Reconstruct original requesting URL
		
		String port = scheme.equals("https") ? (serverPort == 443 ? "" : ":"+serverPort) : (serverPort == 80 ? "" : ":"+serverPort);
		
		String url = scheme+"://"+serverName+port+contextPath+servletPath;
		if (pathInfo != null) { url += pathInfo; } 
		if (queryString != null) { url += "?"+queryString; }
		
		return url;
	}
	
	public MDXRequest(MDXServer server, HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException
	{
		this.server = server;
		baseURL = baseURL(request);
		
		String pathInfo = request.getPathInfo();
		int pos = pathInfo.lastIndexOf('/');
		if (pos == -1) {
			response.sendError(404, "Bad url");
			throw new ServletException("Bad url");
		}
		
		tags = pathInfo.split("+");
	}
	
	public String[] getTags() {
		return tags;
	}
	
	public boolean isSingle() {
		return tags[0].startsWith("{") || tags[0].startsWith("http") || tags[0].startsWith("urn:");
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < tags.length - 1; i++) {
			buf.append(tags[i]).append("+");
		}
		buf.append(tags[tags.length-1]);
		return buf.toString();
	}
	
	public String getLocation() {
		return baseURL;
	}
}
