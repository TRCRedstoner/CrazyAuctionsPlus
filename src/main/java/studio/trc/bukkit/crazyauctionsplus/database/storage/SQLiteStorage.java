package studio.trc.bukkit.crazyauctionsplus.database.storage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import studio.trc.bukkit.crazyauctionsplus.database.Storage;
import studio.trc.bukkit.crazyauctionsplus.database.engine.SQLiteEngine;
import studio.trc.bukkit.crazyauctionsplus.utils.ItemMail;

public class SQLiteStorage extends SQLiteEngine implements Storage {
	public static final Map<UUID, SQLiteStorage> cache = new HashMap<UUID, SQLiteStorage>();

	private static long lastUpdateTime = System.currentTimeMillis();

	private final UUID uuid;
	private final YamlConfiguration yamlData = new YamlConfiguration();
	private final List<ItemMail> mailBox = new ArrayList<ItemMail>();

	public SQLiteStorage(UUID uuid) {
		this.uuid = uuid;

		try {
			ResultSet rs = super.executeQuery(super.getConnection()
					.prepareStatement("SELECT * FROM " + getItemMailTable() + " WHERE UUID = '" + uuid + "'"));
			if (rs.next()) {
				String yamldata = rs.getString("YamlData");
				yamlData.loadFromString(yamldata);
			} else {
				register(uuid);
			}
		} catch (SQLException | InvalidConfigurationException ex) {
			Logger.getLogger(SQLiteStorage.class.getName()).log(Level.SEVERE, null, ex);
			return;
		}

		loadData();
	}

	private void loadData() {
		if (yamlData.get("Name") == null
				|| !yamlData.getString("Name").equals(Bukkit.getOfflinePlayer(uuid).getName())) {
			yamlData.set("Name", Bukkit.getOfflinePlayer(uuid).getName());
			saveData();
		}

		if (yamlData.get("Items") != null) {
			for (String path : yamlData.getConfigurationSection("Items").getKeys(false)) {
				if (yamlData.get("Items." + path) != null) {
					ItemMail im;
					try {
						im = new ItemMail(
								yamlData.get("Items." + path + ".UID") != null
										? yamlData.getLong("Items." + path + ".UID") : Long.valueOf(path),
								uuid,
								yamlData.get("Items." + path + ".Item") != null
										? yamlData.getItemStack("Items." + path + ".Item")
										: new ItemStack(Material.AIR),
								yamlData.get("Items." + path + ".Full-Time") != null
										? yamlData.getLong("Items." + path + ".Full-Time") : 0,
								yamlData.get("Items." + path + ".Never-Expire") != null
										? yamlData.getBoolean("Items." + path + ".Never-Expire") : false);
						// im.setUID(yamlData.get("Items." + path + ".UID") !=
						// null ? yamlData.getLong("Items." + path + ".UID") :
						// Long.valueOf(path));
					} catch (Exception ex) {
						continue;
					}
					mailBox.add(im);
				}
			}
		}
	}

	@Override
	public String getName() {
		return yamlData.getString("Name");
	}

	@Override
	public UUID getUUID() {
		return uuid;
	}

	@Override
	public Player getPlayer() {
		return Bukkit.getPlayer(uuid);
	}

	@Override
	public YamlConfiguration getYamlData() {
		return yamlData;
	}

	@Override
	public void saveData() {
		try {
			long i = 1;
			yamlData.set("Items", null);
			for (ItemMail im : mailBox) {
				if (im.getItem() == null || im.getItem().getType().equals(Material.AIR))
					continue;
				yamlData.set("Items." + i + ".Item", im.getItem());
				yamlData.set("Items." + i + ".Full-Time", im.getFullTime());
				yamlData.set("Items." + i + ".Never-Expire", im.isNeverExpire());
				yamlData.set("Items." + i + ".UID", i);
				i++;
			}
			String yaml = yamlData.saveToString();
			PreparedStatement statement = getConnection().prepareStatement(
					"UPDATE " + getItemMailTable() + " SET " + "YamlData = ? " + "WHERE UUID = '" + uuid + "'");
			statement.setString(1, yaml);
			executeUpdate(statement);
		} catch (SQLException ex) {
			Logger.getLogger(SQLiteStorage.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public List<ItemMail> getMailBox() {
		return mailBox;
	}

	@Override
	public void addItem(ItemMail... is) {
		mailBox.addAll(Arrays.asList(is));
		saveData();
	}

	@Override
	public void removeItem(ItemMail... is) {
		mailBox.removeAll(Arrays.asList(is));
		saveData();
	}

	@Override
	public void clearMailBox() {
		mailBox.clear();
		yamlData.set("Items", null);
		saveData();
	}

	@Override
	public int getMailNumber() {
		return mailBox.size();
	}

	@Override
	public long makeUID() {
		long id = 0;
		while (true) { // 循环查找
			id++;
			boolean b = false;
			for (ItemMail im : mailBox) {
				if (im.getUID() == id) { // 如果有任何一个商品的UID等于变量id，即弃
					b = true; // 继续执行循环，不break
					break; // 停止，先+1再说
				}
			}
			if (b)
				continue;
			break;
		}
		return id;
	}

	private void register(UUID uuid) throws SQLException {
		String name = Bukkit.getOfflinePlayer(uuid) != null ? Bukkit.getOfflinePlayer(uuid).getName() : null;
		if (name == null) {
			name = "Null";
		}
		PreparedStatement statement = getConnection()
				.prepareStatement("INSERT INTO " + getItemMailTable() + "(UUID, Name, YamlData) " + "VALUES(?, ?, ?)");
		statement.setString(1, uuid.toString());
		statement.setString(2, name);
		statement.setString(3, "{}");
		executeUpdate(statement);
		yamlData.set("Name", name);
	}

	public static SQLiteStorage getPlayerData(Player player) {
		SQLiteStorage data = cache.get(player.getUniqueId());
		if (data != null) {
			if (!isItemMailReacquisition() || System.currentTimeMillis() - lastUpdateTime <= getUpdateDelay() * 1000) {
				return data;
			}
		}
		data = new SQLiteStorage(player.getUniqueId());
		cache.put(player.getUniqueId(), data);
		lastUpdateTime = System.currentTimeMillis();
		return data;
	}

	public static SQLiteStorage getPlayerData(OfflinePlayer player) {
		SQLiteStorage data = cache.get(player.getUniqueId());
		if (data != null) {
			if (!isItemMailReacquisition() || System.currentTimeMillis() - lastUpdateTime <= getUpdateDelay() * 1000) {
				return data;
			}
		}
		data = new SQLiteStorage(player.getUniqueId());
		cache.put(player.getUniqueId(), data);
		lastUpdateTime = System.currentTimeMillis();
		return data;
	}

	public static SQLiteStorage getPlayerData(UUID uuid) {
		SQLiteStorage data = cache.get(uuid);
		if (data != null) {
			if (!isItemMailReacquisition() || System.currentTimeMillis() - lastUpdateTime <= getUpdateDelay() * 1000) {
				return data;
			}
		}
		data = new SQLiteStorage(uuid);
		cache.put(uuid, data);
		lastUpdateTime = System.currentTimeMillis();
		return data;
	}
}