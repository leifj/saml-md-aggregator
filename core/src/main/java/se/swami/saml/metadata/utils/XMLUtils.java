package se.swami.saml.metadata.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

public class XMLUtils {

	public static byte[] o2b(XmlObject o) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		XmlOptions opts = new XmlOptions();
		
		opts.setSaveOuter();
        opts.setSavePrettyPrint();
        opts.setSaveAggressiveNamespaces();
        o.save(out,opts);

        return out.toByteArray();
	}
	
	public static String o2s(XmlObject o) throws IOException {
		return new String(o2b(o));
	}
	
}
