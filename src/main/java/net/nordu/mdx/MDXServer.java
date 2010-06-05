package net.nordu.mdx;

import java.net.URL;
import java.security.KeyStore;
import java.security.Provider;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

public class MDXServer {
	
	private static final Log log = LogFactory.getLog(MDXServer.class);
	private KeyStore keyStore;
	private Server jetty;
	private Properties config;
	private Provider cryptoProvider;
	
	public Properties getConfig() {
		return config;
	}
	
	public Server getJetty() {
		return jetty;
	}
	
	public KeyStore getKeyStore() {
		return keyStore;
	}
	
	public Provider getCryptoProvider() {
		return cryptoProvider;
	}
	
	public MDXServer() 
		throws Exception 
	{
		jetty = new Server(8080);
		final URL warUrl = MDXServer.class.getClassLoader().getResource("webapp");
		final String warUrlString = warUrl.toExternalForm();
		jetty.setHandler(new WebAppContext(warUrlString, "/"));
	}
	
	public void run() throws Exception {
		jetty.start();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) 
		throws Exception 
	{
		MDXServer server = new MDXServer();
		server.run();
	}
	
}
