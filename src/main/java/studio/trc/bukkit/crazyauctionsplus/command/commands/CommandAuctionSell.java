package studio.trc.bukkit.crazyauctionsplus.command.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import studio.trc.bukkit.crazyauctionsplus.Main;
import studio.trc.bukkit.crazyauctionsplus.api.events.AuctionListEvent;
import studio.trc.bukkit.crazyauctionsplus.command.CommandType;
import studio.trc.bukkit.crazyauctionsplus.command.VCommand;
import studio.trc.bukkit.crazyauctionsplus.currency.CurrencyManager;
import studio.trc.bukkit.crazyauctionsplus.database.GlobalMarket;
import studio.trc.bukkit.crazyauctionsplus.utils.FileManager;
import studio.trc.bukkit.crazyauctionsplus.utils.ItemOwner;
import studio.trc.bukkit.crazyauctionsplus.utils.MarketGoods;
import studio.trc.bukkit.crazyauctionsplus.utils.PluginControl;
import studio.trc.bukkit.crazyauctionsplus.utils.enums.Messages;
import studio.trc.bukkit.crazyauctionsplus.utils.enums.ShopType;

public class CommandAuctionSell extends VCommand {

	public CommandAuctionSell() {
		this.addSubCommand("sell");
		this.setConsoleCanUse(false);
		this.setPermission("Sell");
		
		this.addRequireArg("price");
		this.addOptionalArg("amount");
	}

	@SuppressWarnings("deprecation")
	@Override
	protected CommandType perform(Main plugin) {

		if (PluginControl.isWorldDisabled(player)) {
			sender.sendMessage(Messages.getMessage("World-Disabled"));
			return CommandType.SUCCESS;
		}

		if (!crazyAuctions.isSellingEnabled()) {
			player.sendMessage(Messages.getMessage("Selling-Disable"));
			return CommandType.DEFAULT;
		}

		ShopType type = ShopType.SELL;

		ItemStack item = PluginControl.getItemInHand(player);
		int amount = argAsInteger(1, item.getAmount());
		amount = amount <= 0 ? 1 : amount > item.getAmount() ? item.getAmount() : amount;

		if (PluginControl.getItemInHand(player).getType() == Material.AIR) {
			player.sendMessage(Messages.getMessage("Doesnt-Have-Item-In-Hand"));
			return CommandType.DEFAULT;
		}

		double price = argAsDouble(0);
		double tax = 0;

		if (price < FileManager.Files.CONFIG.getFile().getDouble("Settings.Minimum-Sell-Price")) {
			Map<String, String> placeholders = new HashMap<String, String>();
			placeholders.put("%price%",
					String.valueOf(FileManager.Files.CONFIG.getFile().getDouble("Settings.Minimum-Sell-Price")));
			player.sendMessage(Messages.getMessage("Sell-Price-To-Low", placeholders));
			return CommandType.DEFAULT;
		}

		if (price > FileManager.Files.CONFIG.getFile().getDouble("Settings.Max-Beginning-Sell-Price")) {
			Map<String, String> placeholders = new HashMap<String, String>();
			placeholders.put("%price%",
					String.valueOf(FileManager.Files.CONFIG.getFile().getDouble("Settings.Max-Beginning-Sell-Price")));
			player.sendMessage(Messages.getMessage("Sell-Price-To-High", placeholders));
			return CommandType.DEFAULT;
		}

		if (!PluginControl.bypassLimit(player, ShopType.SELL)) {
			int limit = PluginControl.getLimit(player, ShopType.SELL);
			if (limit > -1) {
				if (crazyAuctions.getNumberOfPlayerItems(player, ShopType.SELL) >= limit) {
					Map<String, String> placeholders = new HashMap<String, String>();
					placeholders.put("%number%", String.valueOf(limit));
					player.sendMessage(Messages.getMessage("Max-Selling-Items", placeholders));
					return CommandType.DEFAULT;
				}
			}
		}

		if (!PluginControl.bypassTaxRate(player, ShopType.SELL)) {
			tax = price * PluginControl.getTaxRate(player, ShopType.SELL);
			if (CurrencyManager.getMoney(player) < tax) {
				HashMap<String, String> placeholders = new HashMap<String, String>();
				placeholders.put("%Money_Needed%", String.valueOf(tax - CurrencyManager.getMoney(player)));
				placeholders.put("%money_needed%", String.valueOf(tax - CurrencyManager.getMoney(player)));
				player.sendMessage(Messages.getMessage("Need-More-Money", placeholders));
				return CommandType.DEFAULT;
			}
		}

		for (String id : FileManager.Files.CONFIG.getFile().getStringList("Settings.BlackList")) {
			if (item.getType() == PluginControl.makeItem(id, 1).getType()) {
				player.sendMessage(Messages.getMessage("Item-BlackListed"));
				return CommandType.DEFAULT;
			}
		}

		if (!FileManager.Files.CONFIG.getFile().getBoolean("Settings.Allow-Damaged-Items")) {
			for (Material i : getDamageableItems()) {
				if (item.getType() == i) {
					if (item.getDurability() > 0) {
						player.sendMessage(Messages.getMessage("Item-Damaged"));
						return CommandType.DEFAULT;
					}
				}
			}
		}

		UUID owner = player.getUniqueId();
		ItemStack is = item.clone();
		is.setAmount(amount);
		GlobalMarket market = GlobalMarket.getMarket();
		MarketGoods goods = new MarketGoods(market.makeUID(), type, new ItemOwner(owner, player.getName()), is,
				type.equals(ShopType.BID)
						? PluginControl.convertToMill(FileManager.Files.CONFIG.getFile().getString("Settings.Bid-Time"))
						: PluginControl
								.convertToMill(FileManager.Files.CONFIG.getFile().getString("Settings.Sell-Time")),
				PluginControl.convertToMill(FileManager.Files.CONFIG.getFile().getString("Settings.Full-Expire-Time")),
				price, "None");
		market.addGoods(goods);
		Bukkit.getPluginManager().callEvent(new AuctionListEvent(player, type, is, price, tax));
		CurrencyManager.removeMoney(player, tax);
		Map<String, String> placeholders = new HashMap<String, String>();
		placeholders.put("%Price%", String.valueOf(price));
		placeholders.put("%price%", String.valueOf(price));
		placeholders.put("%tax%", String.valueOf(tax));
		player.sendMessage(Messages.getMessage("Added-Item-For-Sale", placeholders));

		if (item.getAmount() <= 1 || (item.getAmount() - amount) <= 0)
			PluginControl.setItemInHand(player, new ItemStack(Material.AIR));
		else
			item.setAmount(item.getAmount() - amount);

		return CommandType.SUCCESS;
	}

}
