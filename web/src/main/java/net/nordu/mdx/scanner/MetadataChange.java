package net.nordu.mdx.scanner;

import java.io.Serializable;
import java.util.Date;

public class MetadataChange implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6750830823194935907L;

	private String id;
	private String reason;
	private Date timeStamp;
	private MetadataChangeType type;
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getReason() {
		return reason;
	}
	
	public void setReason(String reason) {
		this.reason = reason;
	}
	
	public Date getTimeStamp() {
		return timeStamp;
	}
	
	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	public MetadataChangeType getType() {
		return type;
	}
	
	public void setType(MetadataChangeType type) {
		this.type = type;
	}
	
	public String toString() {
		return "EntityDescriptor["+id+"] changed: "+timeStamp+", "+type+(reason == null ? "" : ": "+reason);
	}
	
	public MetadataChange(String id, MetadataChangeType type, String reason, Date timeStamp) {
		this.id = id;
		this.type = type;
		this.reason = reason;
		this.timeStamp = timeStamp;
	}
	
	public MetadataChange(String id, MetadataChangeType type, String reason) {
		this(id,type,reason,new Date());
	}
	
	public MetadataChange(String id, MetadataChangeType type) {
		this(id,type,null,new Date());
	}
}
