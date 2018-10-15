package ignite.examples.model;

import java.io.Serializable;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 0L;
	private Long id;
	private JsonNode data;
	
	public JsonData(Long id, JsonNode data) {
		this.id = id;
		this.data = data;
	}
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public JsonNode getData() {
		return data;
	}
	
	public void setData(JsonNode data) {
		this.data = data;
	}
	
	public String toString() {
		return String.format("[JsonData] id = %d\n data[%s] = %s\n", id, data.getClass().getSimpleName(), data.toString());
	}

}
