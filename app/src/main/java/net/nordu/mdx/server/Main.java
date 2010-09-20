package net.nordu.mdx.server;

import java.net.URL;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
	        Server jetty = new Server(8080);
	        final URL webXmlUrl = Thread.currentThread().getContextClassLoader().getResource("WEB-INF/web.xml");
	        WebAppContext context = new WebAppContext();
	        
	        String webXmlPath = webXmlUrl.toExternalForm();
	        context.setDescriptor(webXmlPath);
	        context.setContextPath("/");
	        context.setResourceBase(webXmlPath+"/../.."); // TODO Make this filesystem portable!
	        jetty.setHandler(context);
			jetty.start();
			jetty.join();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(255);
		}
	}

}
