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
	        final URL warUrl = Main.class.getClassLoader().getResource("webapp");
	        final String warUrlString = warUrl.toExternalForm();
	        jetty.setHandler(new WebAppContext(warUrlString, "/"));
			jetty.start();
			jetty.join();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(255);
		}
	}

}
