package studio.trc.bukkit.crazyauctionsplus.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;

import studio.trc.bukkit.crazyauctionsplus.utils.FileManager.Files;
import studio.trc.bukkit.crazyauctionsplus.utils.FileManager.ProtectedConfiguration;

public class ItemCollection {
	private final ItemStack is;
	private final String displayName;
	private final long uid;

	public ItemCollection(ItemStack is, long uid, String displayName) {
		this.is = is;
		this.uid = uid;
		this.displayName = displayName;
	}

	public ItemStack getItem() {
		return is;
	}

	public String getDisplayName() {
		return displayName;
	}

	public long getUID() {
		return uid;
	}

	public static boolean addItem(ItemStack item, String displayName) {
		if (displayName == null)
			return false;
		ProtectedConfiguration ic = Files.ITEMCOLLECTION.getFile();
		if (ic.get("ItemCollection") != null) {
			for (String items : ic.getConfigurationSection("ItemCollection").getKeys(false)) {
				if (ic.get("ItemCollection." + items + ".UID") != null
						&& ic.get("ItemCollection." + items + ".Item") != null) {
					if (item.getItemMeta().equals(ic.getItemStack("ItemCollection." + items + ".Item").getItemMeta())) {
						return false;
					}
				}
			}
			if (ic.get("ItemCollection." + displayName) != null) {
				return false;
			} else {
				long uid = makeUID();
				ic.set("ItemCollection." + displayName + ".UID", uid);
				ic.set("ItemCollection." + displayName + ".Item", item);
				Files.ITEMCOLLECTION.saveFile();
				return true;
			}
		} else {
			ic.set("ItemCollection." + displayName + ".UID", 1L);
			ic.set("ItemCollection." + displayName + ".Item", item);
			Files.ITEMCOLLECTION.saveFile();
			return true;
		}
	}

	public static boolean deleteItem(ItemStack item) {
		ProtectedConfiguration ic = Files.ITEMCOLLECTION.getFile();
		if (ic.get("ItemCollection") != null) {
			for (String items : ic.getConfigurationSection("ItemCollection").getKeys(false)) {
				if (ic.get("ItemCollection." + items + ".UID") != null && ic
						.getItemStack("ItemCollection." + items + ".Item").getItemMeta().equals(item.getItemMeta())) {
					ic.set("ItemCollection." + items, null);
					Files.ITEMCOLLECTION.saveFile();
					return true;
				}
			}
			return false;
		} else {
			return false;
		}
	}

	public static boolean deleteItem(long uid) {
		ProtectedConfiguration ic = Files.ITEMCOLLECTION.getFile();
		if (ic.get("ItemCollection") != null) {
			for (String items : ic.getConfigurationSection("ItemCollection").getKeys(false)) {
				if (ic.get("ItemCollection." + items + ".UID") != null
						&& ic.getLong("ItemCollection." + items + ".UID") == uid) {
					ic.set("ItemCollection." + items, null);
					Files.ITEMCOLLECTION.saveFile();
					return true;
				}
			}
			return false;
		} else {
			return false;
		}
	}

	public static boolean deleteItem(String displayName) {
		if (displayName == null)
			return false;
		ProtectedConfiguration ic = Files.ITEMCOLLECTION.getFile();
		if (ic.get("ItemCollection") != null) {
			for (String items : ic.getConfigurationSection("ItemCollection").getKeys(false)) {
				if (items.equalsIgnoreCase(displayName)) {
					ic.set("ItemCollection." + items, null);
					Files.ITEMCOLLECTION.saveFile();
					return true;
				}
			}
			return false;
		} else {
			return false;
		}
	}

	public static long makeUID() {
		long id = 0;
		ProtectedConfiguration ic = Files.ITEMCOLLECTION.getFile();
		while (true) {
			id++;
			boolean b = false;
			for (String items : ic.getConfigurationSection("ItemCollection").getKeys(false)) {
				if (ic.get("ItemCollection." + items + ".UID") != null
						&& ic.get("ItemCollection." + items + ".Item") != null) {
					if (ic.getLong("ItemCollection." + items + ".UID") == id) {
						b = true;
						break;
					}
				}
			}
			if (b)
				continue;
			break;
		}
		return id;
	}

	public static List<ItemCollection> getCollection() {
		List<ItemCollection> list = new ArrayList<ItemCollection>();
		ProtectedConfiguration ic = Files.ITEMCOLLECTION.getFile();
		if (ic.get("ItemCollection") != null) {
			for (String items : ic.getConfigurationSection("ItemCollection").getKeys(false)) {
				if (ic.get("ItemCollection." + items + ".UID") != null
						&& ic.get("ItemCollection." + items + ".Item") != null) {
					list.add(new ItemCollection(ic.getItemStack("ItemCollection." + items + ".Item"),
							ic.getLong("ItemCollection." + items + ".UID"), items));
				}
			}
			return list;
		}
		return list;
	}

	public static ItemCollection getItemCollection(long uid) {
		ProtectedConfiguration ic = Files.ITEMCOLLECTION.getFile();
		if (ic.get("ItemCollection") != null) {
			for (String items : ic.getConfigurationSection("ItemCollection").getKeys(false)) {
				if (ic.get("ItemCollection." + items + ".UID") != null
						&& ic.get("ItemCollection." + items + ".Item") != null) {
					if (ic.getLong("ItemCollection." + items + ".UID") == uid) {
						return new ItemCollection(ic.getItemStack("ItemCollection." + items + ".Item"),
								ic.getLong("ItemCollection." + items + ".UID"), items);
					}
				}
			}
		}
		return null;
	}

	public static ItemCollection getItemCollection(String displayName) {
		ProtectedConfiguration ic = Files.ITEMCOLLECTION.getFile();
		if (ic.get("ItemCollection") != null) {
			for (String items : ic.getConfigurationSection("ItemCollection").getKeys(false)) {
				if (ic.get("ItemCollection." + items + ".UID") != null
						&& ic.get("ItemCollection." + items + ".Item") != null) {
					if (displayName.equalsIgnoreCase(items)) {
						return new ItemCollection(ic.getItemStack("ItemCollection." + items + ".Item"),
								ic.getLong("ItemCollection." + items + ".UID"), items);
					}
				}
			}
		}
		return null;
	}
}
