package se.swami.saml.metadata.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.springframework.stereotype.Component;

@Path("/tag/{tag}")
@Component
public class EntityByTagResource {

	@GET
	@Produces("application/samlmetadata+xml")
	public String getEntities(@PathParam("tag") String tag, @Context UriInfo uri) {
		StringBuffer buf = new StringBuffer();
		
		return buf.toString();
	}
	
}
