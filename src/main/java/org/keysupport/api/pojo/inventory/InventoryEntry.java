package org.keysupport.api.pojo.inventory;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "kid", "x5c", "crl" })
public class InventoryEntry {

	@JsonProperty("kid")
	private String kid;
	@JsonProperty("x5c")
	private List<String> x5c;
	@JsonProperty("crl")
	private String crl;

	@JsonProperty("kid")
	public String getKid() {
		return kid;
	}

	@JsonProperty("kid")
	public void setKid(String kid) {
		this.kid = kid;
	}

	@JsonProperty("x5c")
	public List<String> getX5c() {
		return x5c;
	}

	@JsonProperty("x5c")
	public void setX5c(List<String> x5c) {
		this.x5c = x5c;
	}

	@JsonProperty("crl")
	public String getCrl() {
		return crl;
	}

	@JsonProperty("crl")
	public void setCrl(String crl) {
		this.crl = crl;
	}

}
