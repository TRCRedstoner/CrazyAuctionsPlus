package studio.trc.bukkit.crazyauctionsplus.database.market;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import studio.trc.bukkit.crazyauctionsplus.database.GlobalMarket;
import studio.trc.bukkit.crazyauctionsplus.utils.FileManager.*;
import studio.trc.bukkit.crazyauctionsplus.utils.ItemOwner;
import studio.trc.bukkit.crazyauctionsplus.utils.MarketGoods;
import studio.trc.bukkit.crazyauctionsplus.utils.enums.ShopType;

public class YamlMarket implements GlobalMarket {
	private static final List<MarketGoods> marketgoods = new ArrayList<MarketGoods>();

	private static YamlMarket instance;

	private YamlMarket() {
		instance = YamlMarket.this;
	}

	public static YamlMarket getInstance() {
		if (instance == null) {
			YamlMarket market = new YamlMarket();
			market.reloadData();
			return market;
		}
		return instance;
	}

	@Override
	public List<MarketGoods> getItems() {
		return marketgoods;
	}

	@Override
	public MarketGoods getMarketGoods(long uid) {
		for (MarketGoods mg : marketgoods) {
			if (mg.getUID() == uid) {
				return mg;
			}
		}
		return null;
	}

	@Override
	public long makeUID() {
		long id = 0;
		while (true) {
			id++;
			boolean b = false;
			for (MarketGoods mgs : marketgoods) {
				if (mgs.getUID() == id) {
					b = true;
					break;
				}
			}
			if (b)
				continue;
			break;
		}
		return id;
	}

	@Override
	public void addGoods(MarketGoods goods) {
		marketgoods.add(goods);
		saveData();
	}

	@Override
	public void removeGoods(MarketGoods goods) {
		for (MarketGoods mg : marketgoods) {
			if (mg.equals(goods)) {
				marketgoods.remove(mg);
				break;
			}
		}
		saveData();
	}

	@Override
	public void removeGoods(long uid) {
		for (MarketGoods mg : marketgoods) {
			if (mg.getUID() == uid) {
				marketgoods.remove(mg);
				break;
			}
		}
		saveData();
	}

	@Override
	public void saveData() {
		ProtectedConfiguration data = Files.DATABASE.getFile();
		data.set("Items", null);
		for (MarketGoods mg : marketgoods) {
			long num = 1;
			for (; data.contains("Items." + num); num++) {
			}
			data.set("Items." + num + ".Owner", mg.getItemOwner().toString());
			switch (mg.getShopType()) {
			case SELL: {
				data.set("Items." + num + ".Price", mg.getPrice());
				data.set("Items." + num + ".ShopType", "SELL");
				data.set("Items." + num + ".Time-Till-Expire", mg.getTimeTillExpire());
				data.set("Items." + num + ".Full-Time", mg.getFullTime());
				data.set("Items." + num + ".UID", mg.getUID());
				data.set("Items." + num + ".Item", mg.getItem());
				break;
			}
			case BUY: {
				data.set("Items." + num + ".Reward", mg.getReward());
				data.set("Items." + num + ".ShopType", "BUY");
				data.set("Items." + num + ".Time-Till-Expire", mg.getTimeTillExpire());
				data.set("Items." + num + ".Full-Time", mg.getFullTime());
				data.set("Items." + num + ".UID", mg.getUID());
				data.set("Items." + num + ".Item", mg.getItem());
				break;
			}
			case BID: {
				data.set("Items." + num + ".Price", mg.getPrice());
				data.set("Items." + num + ".ShopType", "BID");
				data.set("Items." + num + ".TopBidder", mg.getTopBidder());
				data.set("Items." + num + ".Time-Till-Expire", mg.getTimeTillExpire());
				data.set("Items." + num + ".Full-Time", mg.getFullTime());
				data.set("Items." + num + ".UID", mg.getUID());
				data.set("Items." + num + ".Item", mg.getItem());
				break;
			}
			default:
				break;
			}
		}
		Files.DATABASE.saveFile();
	}

	@Override
	public void reloadData() {
		ProtectedConfiguration data = Files.DATABASE.getFile();
		marketgoods.clear();
		if (data.get("Items") != null) {
			for (String path : data.getConfigurationSection("Items").getKeys(false)) {
				String[] owner = data.getString("Items." + path + ".Owner").split(":");
				ShopType shoptype = ShopType.valueOf(data.getString("Items." + path + ".ShopType").toUpperCase());
				MarketGoods goods;
				switch (shoptype) {
				case SELL: {
					goods = new MarketGoods(data.getLong("Items." + path + ".UID"), shoptype,
							new ItemOwner(UUID.fromString(owner[1]), owner[0]),
							data.getItemStack("Items." + path + ".Item"),
							data.getLong("Items." + path + ".Time-Till-Expire"),
							data.getLong("Items." + path + ".Full-Time"), data.getDouble("Items." + path + ".Price"));
					break;
				}
				case BUY: {
					goods = new MarketGoods(data.getLong("Items." + path + ".UID"), shoptype,
							new ItemOwner(UUID.fromString(owner[1]), owner[0]),
							data.getItemStack("Items." + path + ".Item"),
							data.getLong("Items." + path + ".Time-Till-Expire"),
							data.getLong("Items." + path + ".Full-Time"), data.getDouble("Items." + path + ".Reward"));
					break;
				}
				case BID: {
					goods = new MarketGoods(data.getLong("Items." + path + ".UID"), shoptype,
							new ItemOwner(UUID.fromString(owner[1]), owner[0]),
							data.getItemStack("Items." + path + ".Item"),
							data.getLong("Items." + path + ".Time-Till-Expire"),
							data.getLong("Items." + path + ".Full-Time"), data.getDouble("Items." + path + ".Price"),
							data.getString("Items." + path + ".TopBidder"));
					break;
				}
				default: {
					continue;
				}
				}
				marketgoods.add(goods);
			}
		}
	}

	@Override
	public YamlConfiguration getYamlData() {
		YamlConfiguration config = new YamlConfiguration();
		try (Reader reader = new InputStreamReader(
				new FileInputStream(new File("plugins/CrazyAuctionsPlus/Database.yml")), "UTF-8")) {
			config.load(reader);
		} catch (IOException | InvalidConfigurationException ex) {
		}
		return config;
	}
}
