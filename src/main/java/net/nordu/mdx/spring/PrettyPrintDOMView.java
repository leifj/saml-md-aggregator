package net.nordu.mdx.spring;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.springframework.web.servlet.View;
import org.w3c.dom.Document;

public class PrettyPrintDOMView implements View {

	private String contentType;
	
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	public String getContentType() {
		return contentType;
	}

	public PrettyPrintDOMView() { }
	
	protected Document findDocument(Map<String, ?> map) {
		for (Map.Entry<String, ?> e : map.entrySet()) {
			if (e.getValue() instanceof Document) {
				return (Document)e.getValue();
			}
		}
		return null;
	}
	
	public void render(Map<String, ?> map, HttpServletRequest request,HttpServletResponse response) 
		throws Exception 
	{
		Document doc = findDocument(map);
		if (doc == null) {
			response.sendError(404);
		} else {
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer t = tf.newTransformer();
			response.setContentType(getContentType());
			t.transform(new DOMSource(doc.getDocumentElement()),new StreamResult(response.getOutputStream()));
		}
	}

}
