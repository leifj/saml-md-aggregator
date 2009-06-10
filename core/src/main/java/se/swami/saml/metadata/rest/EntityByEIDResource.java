package se.swami.saml.metadata.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.springframework.stereotype.Component;

@Path("/id/{eid}")
@Component
public class EntityByEIDResource {

	@GET
	@Produces("application/samlmetadata+xml")
	public String getEntity(@PathParam("eid") String eid) {
		return "";
	}
	
}
