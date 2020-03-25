package studio.trc.bukkit.crazyauctionsplus.database.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import studio.trc.bukkit.crazyauctionsplus.utils.ItemMail;
import studio.trc.bukkit.crazyauctionsplus.database.Storage;

public class YamlStorage implements Storage {
	public static final Map<UUID, YamlStorage> cache = new HashMap<UUID, YamlStorage>();

	private final UUID uuid;
	private final YamlConfiguration config = new YamlConfiguration();
	private final List<ItemMail> mailBox = new ArrayList<ItemMail>();

	public YamlStorage(Player player) {
		uuid = player.getUniqueId();

		File dataFolder = new File("plugins/CrazyAuctionsPlus/Players");
		if (!dataFolder.exists()) {
			dataFolder.mkdir();
		}

		File dataFile = new File("plugins/CrazyAuctionsPlus/Players/" + player.getUniqueId().toString() + ".yml");
		if (!dataFile.exists()) {
			try {
				dataFile.createNewFile();
			} catch (IOException ex) {
			}
		}
		try (InputStreamReader Config = new InputStreamReader(new FileInputStream(dataFile), "UTF-8")) {
			config.load(Config);
		} catch (IOException | InvalidConfigurationException ex) {
			dataFileRepair();
		}

		loadData();
	}

	public YamlStorage(UUID uuid) {
		this.uuid = uuid;

		File dataFolder = new File("plugins/CrazyAuctionsPlus/Players");
		if (!dataFolder.exists()) {
			dataFolder.mkdir();
		}

		File dataFile = new File("plugins/CrazyAuctionsPlus/Players/" + uuid.toString() + ".yml");
		if (!dataFile.exists()) {
			try {
				dataFile.createNewFile();
			} catch (IOException ex) {
			}
		}
		try (InputStreamReader Config = new InputStreamReader(new FileInputStream(dataFile), "UTF-8")) {
			config.load(Config);
		} catch (IOException | InvalidConfigurationException ex) {
			dataFileRepair();
		}

		loadData();
	}

	private void loadData() {
		if (config.get("Name") == null || !config.getString("Name").equals(Bukkit.getOfflinePlayer(uuid).getName())) {
			config.set("Name", Bukkit.getOfflinePlayer(uuid).getName());
			saveData();
		}

		if (config.get("Items") != null) {
			for (String path : config.getConfigurationSection("Items").getKeys(false)) {
				if (config.get("Items." + path) != null) {
					ItemMail im;
					try {
						im = new ItemMail(
								config.get("Items." + path + ".UID") != null ? config.getLong("Items." + path + ".UID")
										: Long.valueOf(path),
								Bukkit.getPlayer(uuid),
								config.get("Items." + path + ".Item") != null
										? config.getItemStack("Items." + path + ".Item") : new ItemStack(Material.AIR),
								config.get("Items." + path + ".Full-Time") != null
										? config.getLong("Items." + path + ".Full-Time") : 0,
								config.get("Items." + path + ".Never-Expire") != null
										? config.getBoolean("Items." + path + ".Never-Expire") : false);
						// im.setUID(config.get("Items." + path + ".UID") !=
						// null ? config.getLong("Items." + path + ".UID") :
						// Long.valueOf(path));
					} catch (Exception ex) {
						continue;
					}
					mailBox.add(im);
				}
			}
		}
	}

	private void dataFileRepair() {
		File dataFolder = new File("plugins/CrazyAuctionsPlus/Broken-Players");
		if (!dataFolder.exists()) {
			dataFolder.mkdir();
		}
		File dataFile = new File("plugins/CrazyAuctionsPlus/Players/" + uuid.toString() + ".yml");
		dataFile.renameTo(new File("plugins/CrazyAuctionsPlus/Broken-Players/" + uuid.toString() + ".yml"));
		try {
			dataFile.createNewFile();
		} catch (IOException ex) {
		}
		try (InputStreamReader Config = new InputStreamReader(new FileInputStream(dataFile), "UTF-8")) {
			config.load(Config);
		} catch (IOException | InvalidConfigurationException ex) {
		}
	}

	@Override
	public String getName() {
		return config.getString("Name");
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
	public List<ItemMail> getMailBox() {
		boolean save = false;
		for (int i = mailBox.size() - 1; i > -1; i--) {
			if (mailBox.get(i).getItem() == null || mailBox.get(i).getItem().getType().equals(Material.AIR)) {
				mailBox.remove(i);
				save = true;
			}
		}
		if (save)
			saveData();
		return mailBox;
	}

	@Override
	public int getMailNumber() {
		return mailBox.size();
	}

	@Override
	public YamlConfiguration getYamlData() {
		return config;
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
		config.set("Items", null);
		saveData();
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

	@Override
	public void saveData() {
		long i = 1;
		config.set("Items", null);
		for (ItemMail im : mailBox) {
			if (im.getItem() == null || im.getItem().getType().equals(Material.AIR))
				continue;
			config.set("Items." + i + ".Item", im.getItem());
			config.set("Items." + i + ".Full-Time", im.getFullTime());
			config.set("Items." + i + ".Never-Expire", im.isNeverExpire());
			config.set("Items." + i + ".UID", i);
			i++;
		}
		try {
			config.save("plugins/CrazyAuctionsPlus/Players/" + uuid + ".yml");
		} catch (IOException ex) {
		}
	}

	public static YamlStorage getPlayerData(Player player) {
		YamlStorage data = cache.get(player.getUniqueId());
		if (data != null) {
			return data;
		}
		data = new YamlStorage(player);
		cache.put(player.getUniqueId(), data);
		return data;
	}

	public static YamlStorage getPlayerData(OfflinePlayer player) {
		YamlStorage data = cache.get(player.getUniqueId());
		if (data != null) {
			return data;
		}
		data = new YamlStorage(player.getUniqueId());
		cache.put(player.getUniqueId(), data);
		return data;
	}

	public static YamlStorage getPlayerData(UUID uuid) {
		YamlStorage data = cache.get(uuid);
		if (data != null) {
			return data;
		}
		data = new YamlStorage(uuid);
		cache.put(uuid, data);
		return data;
	}
}
