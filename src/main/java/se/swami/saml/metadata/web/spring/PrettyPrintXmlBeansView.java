package se.swami.saml.metadata.web.spring;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.xmlbeans.XmlObject;
import org.springframework.web.servlet.View;

import se.swami.saml.metadata.utils.XMLUtils;

public class PrettyPrintXmlBeansView implements View {

	private String contentType;
	
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	public String getContentType() {
		return contentType;
	}

	public PrettyPrintXmlBeansView() { }
	
	protected XmlObject findXmlObject(Map<String, ?> map) {
		for (Map.Entry<String, ?> e : map.entrySet()) {
			if (e.getValue() instanceof XmlObject) {
				return (XmlObject)e.getValue();
			}
		}
		
		return null;
	}
	
	public void render(Map<String, ?> map, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		XmlObject o = findXmlObject(map);
		if (o != null)
			XMLUtils.writeObject(o, response.getOutputStream());
	}

}
