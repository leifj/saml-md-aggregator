package net.nordu.saml.signer;

import java.io.File;
import java.lang.reflect.Constructor;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import java.util.Enumeration;
import java.util.Properties;

import net.nordu.saml.signer.servlets.MDXServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

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
	
	public MDXServer(Properties p) 
		throws Exception 
	{
		config = p;
		jetty = new Server(8080);
		Context root = new Context(jetty,"/",Context.SESSIONS);

		initCrypto(p.getProperty("mdx.pkcs11.config"),p.getProperty("mdx.pkcs11.pin"),p.getProperty("mdx.pkcs11.provider"));
		root.addServlet(new ServletHolder(new MDXServlet(this)), "/entity/*");
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
		Properties p  = new Properties();
		
		if (args.length > 0)
			p.setProperty("mdx.servlet.dir", args[0]);
		else
			p.setProperty("mdx.servlet.dir", System.getProperty("user.dir"));
		
		p.setProperty("mdx.pkcs11.config",p.getProperty("mdx.servlet.dir")+File.separator+"soft-token.cfg");
		p.setProperty("mdx.pkcs11.provider", "sun.security.pkcs11.SunPKCS11");
		p.setProperty("mdx.pkcs11.pin","swamid");
		
		log.info("Directory: "+p.getProperty("mdx.servlet.dir"));
		
		MDXServer server = new MDXServer(p);
		server.run();
	}

	@SuppressWarnings({ "unchecked" })
	private void initCrypto(String configName, String pin, String providerClassName) 
		throws Exception 
	{
		Class<Provider> providerClass = (Class<Provider>)Class.forName(providerClassName);
		Constructor<Provider> constructor = providerClass.getConstructor(new Class[] { String.class });
		cryptoProvider = constructor.newInstance(configName);
		int pos = Security.addProvider(cryptoProvider);
		if (pos == -1)
			throw new IllegalArgumentException("Unable to add crypto provider: "+providerClassName);
		
		char pinc[] = pin.toCharArray();
		keyStore = KeyStore.getInstance("PKCS11");
		keyStore.load(null,pinc);
		
		Enumeration<String> aliases = keyStore.aliases();
		while ( aliases.hasMoreElements() ) {
			System.err.println(aliases.nextElement());
		}	
	}
	
}
