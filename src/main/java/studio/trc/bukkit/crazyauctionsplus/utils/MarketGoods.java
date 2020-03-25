package studio.trc.bukkit.crazyauctionsplus.utils;

import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import studio.trc.bukkit.crazyauctionsplus.database.GlobalMarket;
import studio.trc.bukkit.crazyauctionsplus.utils.FileManager.Files;
import studio.trc.bukkit.crazyauctionsplus.utils.enums.ShopType;

/**
 * For processing goods on the market.
 */
public class MarketGoods {
	private String topBidder;
	private long timeTillExpire;
	private double price = 0;
	private double reward = 0;

	private final ShopType shoptype;
	private final long fullTime;
	private final ItemOwner owner;
	private final ItemStack item;
	private final long uid;

	@Override
	public String toString() {
		return "[MarketGoods] -> [UID=" + uid + ", Owner=" + owner + ", FullTime=" + fullTime + ", TimeTillExpire="
				+ timeTillExpire + ", ShopType=" + shoptype.getName() + ", Price=" + price + ", Reward=" + reward
				+ ", TopBidder=" + topBidder + ", Item=" + item + "]";
	}

	public MarketGoods(long uid, ShopType shoptype, ItemOwner owner, ItemStack item, long timeTillExpire,
			long fullTime) {
		this.shoptype = shoptype;
		this.owner = owner;
		this.item = item;
		this.timeTillExpire = timeTillExpire;
		this.fullTime = fullTime;
		this.uid = uid;
	}

	public MarketGoods(long uid, ShopType shoptype, ItemOwner owner, ItemStack item, long timeTillExpire, long fullTime,
			double money) {
		this.shoptype = shoptype;
		this.owner = owner;
		this.item = item;
		this.timeTillExpire = timeTillExpire;
		this.fullTime = fullTime;
		this.uid = uid;
		if (shoptype.equals(ShopType.SELL) || shoptype.equals(ShopType.BID)) {
			price = money;
		} else if (shoptype.equals(ShopType.BUY)) {
			reward = money;
		}
	}

	public MarketGoods(long uid, ShopType shoptype, ItemOwner owner, ItemStack item, long timeTillExpire, long fullTime,
			double price, String topBidder) {
		this.shoptype = shoptype;
		this.owner = owner;
		this.item = item;
		this.timeTillExpire = timeTillExpire;
		this.fullTime = fullTime;
		this.price = price;
		this.topBidder = topBidder;
		this.uid = uid;
	}

	public MarketGoods(long uid, ShopType shoptype, ItemOwner owner, ItemStack item, long timeTillExpire, long fullTime,
			double price, OfflinePlayer topBidder) {
		this.shoptype = shoptype;
		this.owner = owner;
		this.item = item;
		this.timeTillExpire = timeTillExpire;
		this.fullTime = fullTime;
		this.price = price;
		this.topBidder = topBidder.getName() + ":" + topBidder.getUniqueId();
		this.uid = uid;
	}

	/**
	 * Get the OfflinePlayer instance of the Top Bidder.
	 * 
	 * @return
	 */
	public String getTopBidder() {
		return topBidder;
	}

	/**
	 * Get product type
	 * 
	 * @return
	 */
	public ShopType getShopType() {
		return shoptype;
	}

	/**
	 * Get the price of an item
	 * 
	 * @return
	 */
	public double getPrice() {
		return price;
	}

	/**
	 * Get paid for goods
	 * 
	 * @return
	 */
	public double getReward() {
		return reward;
	}

	/**
	 * Get Time Till Expire.
	 * 
	 * @return
	 */
	public long getTimeTillExpire() {
		return timeTillExpire;
	}

	/**
	 * Get Full Time
	 * 
	 * @return
	 */
	public long getFullTime() {
		return fullTime;
	}

	/**
	 * Get added time
	 * 
	 * @return
	 */
	public long getAddedTime() {
		return fullTime - (PluginControl.convertToMill(Files.CONFIG.getFile().getString("Settings.Full-Expire-Time"))
				- System.currentTimeMillis());
	}

	/**
	 * Get the UID of an item.
	 * 
	 * @return
	 */
	public long getUID() {
		return uid;
	}

	/**
	 * Get item.
	 * 
	 * @return
	 */
	public ItemStack getItem() {
		return item;
	}

	/**
	 * Get Item's owner.
	 * 
	 * @return
	 */
	public ItemOwner getItemOwner() {
		return owner;
	}

	/**
	 * Repricing.
	 * 
	 * @param money
	 */
	public void repricing(double money) {
		switch (shoptype) {
		case BUY: {
			reward = money;
			break;
		}
		case SELL: {
			price = money;
			break;
		}
		default:
			break;
		}
	}

	/**
	 * @param topBidder
	 */
	public void setTopBidder(String topBidder) {
		this.topBidder = topBidder;
		GlobalMarket.getMarket().saveData();
	}

	/**
	 * @param player
	 */
	public void setTopBidder(OfflinePlayer player) {
		topBidder = player.getName() + ":" + player.getUniqueId();
		GlobalMarket.getMarket().saveData();
	}

	/**
	 * @param price
	 */
	public void setPrice(double price) {
		this.price = price;
		GlobalMarket.getMarket().saveData();
	}

	/**
	 * @param reward
	 */
	public void setReward(double reward) {
		this.reward = reward;
		GlobalMarket.getMarket().saveData();
	}

	/**
	 * @param timeTillExpire
	 */
	public void setTimeTillExpire(long timeTillExpire) {
		this.timeTillExpire = timeTillExpire;
		GlobalMarket.getMarket().saveData();
	}
}
