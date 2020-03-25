package studio.trc.bukkit.crazyauctionsplus.database.market;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import studio.trc.bukkit.crazyauctionsplus.Main;
import studio.trc.bukkit.crazyauctionsplus.database.GlobalMarket;
import studio.trc.bukkit.crazyauctionsplus.database.engine.SQLiteEngine;
import studio.trc.bukkit.crazyauctionsplus.utils.ItemOwner;
import studio.trc.bukkit.crazyauctionsplus.utils.MarketGoods;
import studio.trc.bukkit.crazyauctionsplus.utils.PluginControl;
import studio.trc.bukkit.crazyauctionsplus.utils.enums.ShopType;

public class SQLiteMarket extends SQLiteEngine implements GlobalMarket {
	private static final List<MarketGoods> marketgoods = new ArrayList<MarketGoods>();

	private static SQLiteMarket instance;
	private static long lastUpdateTime = System.currentTimeMillis();

	private final YamlConfiguration yamlMarket = new YamlConfiguration();

	private SQLiteMarket() {
		instance = SQLiteMarket.this;
	}

	public static SQLiteMarket getInstance() {
		if (instance == null) {
			SQLiteMarket market = new SQLiteMarket();
			return market;
		}
		return instance;
	}

	@Override
	public List<MarketGoods> getItems() {
		if (isMarketReacquisition() && System.currentTimeMillis() - lastUpdateTime >= getUpdateDelay() * 1000) {
			reloadData();
			lastUpdateTime = System.currentTimeMillis();
		}
		return marketgoods;
	}

	@Override
	public MarketGoods getMarketGoods(long uid) {
		if (isMarketReacquisition() && System.currentTimeMillis() - lastUpdateTime >= getUpdateDelay() * 1000) {
			reloadData();
			lastUpdateTime = System.currentTimeMillis();
		}
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
			if (isMarketReacquisition() && System.currentTimeMillis() - lastUpdateTime >= getUpdateDelay() * 1000) {
				reloadData();
				lastUpdateTime = System.currentTimeMillis();
			}
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
		if (isMarketReacquisition() && System.currentTimeMillis() - lastUpdateTime >= getUpdateDelay() * 1000) {
			reloadData();
			lastUpdateTime = System.currentTimeMillis();
		}
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
		if (isMarketReacquisition() && System.currentTimeMillis() - lastUpdateTime >= getUpdateDelay() * 1000) {
			reloadData();
			lastUpdateTime = System.currentTimeMillis();
		}
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
		try {
			yamlMarket.set("Items", null);
			for (MarketGoods mg : marketgoods) {
				long num = 1;
				for (; yamlMarket.contains("Items." + num); num++) {
				}
				yamlMarket.set("Items." + num + ".Owner", mg.getItemOwner().toString());
				switch (mg.getShopType()) {
				case SELL: {
					yamlMarket.set("Items." + num + ".Price", mg.getPrice());
					yamlMarket.set("Items." + num + ".ShopType", "SELL");
					yamlMarket.set("Items." + num + ".Time-Till-Expire", mg.getTimeTillExpire());
					yamlMarket.set("Items." + num + ".Full-Time", mg.getFullTime());
					yamlMarket.set("Items." + num + ".UID", mg.getUID());
					yamlMarket.set("Items." + num + ".Item", mg.getItem());
					break;
				}
				case BUY: {
					yamlMarket.set("Items." + num + ".Reward", mg.getReward());
					yamlMarket.set("Items." + num + ".ShopType", "BUY");
					yamlMarket.set("Items." + num + ".Time-Till-Expire", mg.getTimeTillExpire());
					yamlMarket.set("Items." + num + ".Full-Time", mg.getFullTime());
					yamlMarket.set("Items." + num + ".UID", mg.getUID());
					yamlMarket.set("Items." + num + ".Item", mg.getItem());
					break;
				}
				case BID: {
					yamlMarket.set("Items." + num + ".Price", mg.getPrice());
					yamlMarket.set("Items." + num + ".ShopType", "BID");
					yamlMarket.set("Items." + num + ".TopBidder", mg.getTopBidder());
					yamlMarket.set("Items." + num + ".Time-Till-Expire", mg.getTimeTillExpire());
					yamlMarket.set("Items." + num + ".Full-Time", mg.getFullTime());
					yamlMarket.set("Items." + num + ".UID", mg.getUID());
					yamlMarket.set("Items." + num + ".Item", mg.getItem());
					break;
				}
				default:
					break;
				}
			}
			PreparedStatement statement = getConnection()
					.prepareStatement("UPDATE " + getMarketTable() + " SET " + "YamlMarket = ?");
			statement.setString(1, yamlMarket.saveToString());
			executeUpdate(statement);
		} catch (SQLException ex) {
			if (Main.language.get("SQLite-DataSavingError") != null)
				Main.getInstance().getServer().getConsoleSender()
						.sendMessage(Main.language.getProperty("DataSaveingError")
								.replace("{error}", ex.getLocalizedMessage())
								.replace("{prefix}", PluginControl.getPrefix()).replace("&", "§"));
			try {
				if (getConnection().isClosed())
					repairConnection();
			} catch (SQLException ex1) {
			}
		}
	}

	@Override
	public void reloadData() {
		try {
			PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM " + getMarketTable());
			ResultSet rs = executeQuery(statement);
			marketgoods.clear();
			if (rs != null && rs.next()) {
				String stringYaml = rs.getString("YamlMarket");
				yamlMarket.loadFromString(stringYaml);
				if (yamlMarket.get("Items") == null) {
					return;
				}
				for (String path : yamlMarket.getConfigurationSection("Items").getKeys(false)) {
					String[] owner = yamlMarket.getString("Items." + path + ".Owner").split(":");
					ShopType shoptype = ShopType
							.valueOf(yamlMarket.getString("Items." + path + ".ShopType").toUpperCase());
					MarketGoods goods;
					switch (shoptype) {
					case SELL: {
						goods = new MarketGoods(yamlMarket.getLong("Items." + path + ".UID"), shoptype,
								new ItemOwner(UUID.fromString(owner[1]), owner[0]),
								yamlMarket.getItemStack("Items." + path + ".Item"),
								yamlMarket.getLong("Items." + path + ".Time-Till-Expire"),
								yamlMarket.getLong("Items." + path + ".Full-Time"),
								yamlMarket.getDouble("Items." + path + ".Price"));
						break;
					}
					case BUY: {
						goods = new MarketGoods(yamlMarket.getLong("Items." + path + ".UID"), shoptype,
								new ItemOwner(UUID.fromString(owner[1]), owner[0]),
								yamlMarket.getItemStack("Items." + path + ".Item"),
								yamlMarket.getLong("Items." + path + ".Time-Till-Expire"),
								yamlMarket.getLong("Items." + path + ".Full-Time"),
								yamlMarket.getDouble("Items." + path + ".Reward"));
						break;
					}
					case BID: {
						goods = new MarketGoods(yamlMarket.getLong("Items." + path + ".UID"), shoptype,
								new ItemOwner(UUID.fromString(owner[1]), owner[0]),
								yamlMarket.getItemStack("Items." + path + ".Item"),
								yamlMarket.getLong("Items." + path + ".Time-Till-Expire"),
								yamlMarket.getLong("Items." + path + ".Full-Time"),
								yamlMarket.getDouble("Items." + path + ".Price"),
								yamlMarket.getString("Items." + path + ".TopBidder"));
						break;
					}
					default: {
						continue;
					}
					}
					marketgoods.add(goods);
				}
			} else {
				PreparedStatement createMarket = getConnection()
						.prepareStatement("INSERT INTO " + getMarketTable() + " (YamlMarket) VALUES(?)");
				createMarket.setString(1, "{}");
				executeUpdate(createMarket);
			}
		} catch (SQLException | InvalidConfigurationException | NullPointerException ex) {
			if (Main.language.get("SQLite-DataReadingError") != null)
				Main.getInstance().getServer().getConsoleSender()
						.sendMessage(Main.language.getProperty("SQLite-DataReadingError")
								.replace("{error}", ex.getLocalizedMessage())
								.replace("{prefix}", PluginControl.getPrefix()).replace("&", "§"));
			try {
				if (getConnection().isClosed())
					repairConnection();
			} catch (SQLException ex1) {
			}
		}
	}

	@Override
	public YamlConfiguration getYamlData() {
		return yamlMarket;
	}
}
