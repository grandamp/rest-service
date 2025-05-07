package org.keysupport.api.pojo.inventory;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Inventory {

	private List<InventoryEntry> inventory;

	public List<InventoryEntry> getInventory() {
		return inventory;
	}

	public InventoryEntry getEntry(String kid) {
		InventoryEntry entry = null;
		for (InventoryEntry currentEntry: inventory) {
			if (currentEntry.getKid().equalsIgnoreCase(kid)) {
				entry = currentEntry;
				return entry;
			}
		}
		return entry;
	}

	public void setEntry(List<InventoryEntry> inventory) {
		this.inventory = inventory;
	}
	
}
