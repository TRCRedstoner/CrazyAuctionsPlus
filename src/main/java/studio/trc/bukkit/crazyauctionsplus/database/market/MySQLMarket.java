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
import studio.trc.bukkit.crazyauctionsplus.database.engine.MySQLEngine;
import studio.trc.bukkit.crazyauctionsplus.utils.ItemOwner;
import studio.trc.bukkit.crazyauctionsplus.utils.MarketGoods;
import studio.trc.bukkit.crazyauctionsplus.utils.PluginControl;
import studio.trc.bukkit.crazyauctionsplus.utils.enums.ShopType;

public class MySQLMarket extends MySQLEngine implements GlobalMarket {
	private static final List<MarketGoods> marketgoods = new ArrayList<MarketGoods>();

	private static MySQLMarket instance;
	private static long lastUpdateTime = System.currentTimeMillis();

	private final YamlConfiguration yamlMarket = new YamlConfiguration();

	private MySQLMarket() {
		instance = MySQLMarket.this;
	}

	public static MySQLMarket getInstance() {
		if (instance == null) {
			MySQLMarket market = new MySQLMarket();
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
		while (true) { // 循环查找
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
			PreparedStatement statement = getConnection().prepareStatement(
					"UPDATE " + getDatabaseName() + "." + getMarketTable() + " SET " + "YamlMarket = ?");
			statement.setString(1, yamlMarket.saveToString());
			executeUpdate(statement);
		} catch (SQLException ex) {
			if (Main.language.get("MySQL-DataSavingError") != null)
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
			PreparedStatement statement = getConnection()
					.prepareStatement("SELECT * FROM " + getDatabaseName() + "." + getMarketTable());
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
				PreparedStatement createMarket = getConnection().prepareStatement(
						"INSERT INTO " + getDatabaseName() + "." + getMarketTable() + " (YamlMarket) VALUES(?)");
				createMarket.setString(1, "{}");
				executeUpdate(createMarket);
			}
		} catch (SQLException | InvalidConfigurationException | NullPointerException ex) {
			if (Main.language.get("MySQL-DataReadingError") != null)
				Main.getInstance().getServer().getConsoleSender()
						.sendMessage(Main.language.getProperty("MySQL-DataReadingError")
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

	/**
	 * Here is the obsolete storage method:
	 *
	 * Data table construction: "(" + "UID BIGINT NOT NULL PRIMARY KEY," +
	 * "Owner VARCHAR(60) NOT NULL," + "FullTime BIGINT NOT NULL," +
	 * "TimeTillExpire BIGINT NOT NULL," + "ShopType VARCHAR(3) NOT NULL," +
	 * "Price DOUBLE DEFAULT 0," + "Reward DOUBLE DEFAULT 0" + "TopBidder
	 * VARCHAR(16) DEFAULT 'None'," + "Item LONGTEXT NOT NULL" + ")"
	 *
	 * I originally planned to store the data of each market commodity into the
	 * database one by one, but found a problem during the test. If I clear the
	 * database and upload a new complete data package, the connection is
	 * unexpectedly disconnected (such as MySQL Is an external server), which
	 * may cause data loss in the entire market. Therefore, after discussing
	 * with my friends, I finally adopted the method of loading the entire Yaml
	 * into MySQL, which is safer.
	 * 
	 * Do you have a better idea? Please tell me by private message!
	 * (IMPORTANT!)
	 * 
	 * Method: (public void reloadData()) while (rs.next()) { try { String[]
	 * owner = rs.getString("Owner").split(":"); ShopType shoptype =
	 * ShopType.valueOf(rs.getString("ShopType")); MarketGoods goods; switch
	 * (shoptype) { case SELL: { goods = new MarketGoods( rs.getLong("UID"),
	 * shoptype, new ItemOwner(UUID.fromString(owner[1]), owner[0]),
	 * ItemStack.deserialize(rs.getObject("Item", Map.class)),
	 * rs.getLong("TimeTillExpire"), rs.getLong("FullTime"),
	 * rs.getDouble("Price") ); break; } case BUY: { goods = new MarketGoods(
	 * rs.getLong("UID"), shoptype, new ItemOwner(UUID.fromString(owner[1]),
	 * owner[0]), ItemStack.deserialize(rs.getObject("Item", Map.class)),
	 * rs.getLong("TimeTillExpire"), rs.getLong("FullTime"),
	 * rs.getDouble("Reward") ); break; } case BID: { goods = new MarketGoods(
	 * rs.getLong("UID"), shoptype, new ItemOwner(UUID.fromString(owner[1]),
	 * owner[0]), ItemStack.deserialize(rs.getObject("Item", Map.class)),
	 * rs.getLong("TimeTillExpire"), rs.getLong("FullTime"),
	 * rs.getDouble("Price"), rs.getString("TopBidder") ); break; } default: {
	 * continue; } } marketgoods.add(goods); } catch (Exception ex) { if
	 * (Main.language.get("MySQL-DataReadingError") != null)
	 * Main.getInstance().getServer().getConsoleSender().sendMessage(Main.language.getProperty("MySQL-DataReadingError").replace("{prefix}",
	 * PluginControl.getPrefix().replace("&", "§")); try { if
	 * (getConnection().isClosed()) repairConnection(); } catch (SQLException
	 * ex1) {} } }
	 */
}
