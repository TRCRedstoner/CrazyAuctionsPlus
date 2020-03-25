package studio.trc.bukkit.crazyauctionsplus.command.commands;

import java.lang.reflect.InvocationTargetException;
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

public class CommandAuctionBuy extends VCommand {

	public CommandAuctionBuy() {
		this.setConsoleCanUse(false);
		this.addSubCommand("buy");
		this.setPermission("Buy");

		this.addRequireArg("reward");
		this.addOptionalArg("amount");
	}

	@SuppressWarnings("deprecation")
	@Override
	protected CommandType perform(Main plugin) {

		if (PluginControl.isWorldDisabled(player)) {
			sender.sendMessage(Messages.getMessage("World-Disabled"));
			return CommandType.DEFAULT;
		}

		if (!crazyAuctions.isBuyingEnabled()) {
			player.sendMessage(Messages.getMessage("Buying-Disable"));
			return CommandType.DEFAULT;
		}

		double reward = argAsDouble(0);

		double tax = 0;
		if (!PluginControl.bypassTaxRate(player, ShopType.BUY)) {
			tax = PluginControl.getTaxRate(player, ShopType.BUY);
		}

		if (CurrencyManager.getMoney(player) < reward) {
			HashMap<String, String> placeholders = new HashMap<String, String>();
			placeholders.put("%Money_Needed%", String.valueOf((reward + tax) - CurrencyManager.getMoney(player)));
			placeholders.put("%money_needed%", String.valueOf((reward + tax) - CurrencyManager.getMoney(player)));
			player.sendMessage(Messages.getMessage("Need-More-Money", placeholders));
			return CommandType.DEFAULT;
		}
		if (reward < FileManager.Files.CONFIG.getFile().getDouble("Settings.Minimum-Buy-Reward")) {
			Map<String, String> placeholders = new HashMap<String, String>();
			placeholders.put("%reward%",
					String.valueOf(FileManager.Files.CONFIG.getFile().getDouble("Settings.Minimum-Buy-Reward")));
			player.sendMessage(Messages.getMessage("Buy-Reward-To-Low", placeholders));
			return CommandType.DEFAULT;
		}
		if (reward > FileManager.Files.CONFIG.getFile().getDouble("Settings.Max-Beginning-Buy-Reward")) {
			Map<String, String> placeholders = new HashMap<String, String>();
			placeholders.put("%reward%",
					String.valueOf(FileManager.Files.CONFIG.getFile().getDouble("Settings.Max-Beginning-Buy-Reward")));
			player.sendMessage(Messages.getMessage("Buy-Reward-To-High", placeholders));
			return CommandType.DEFAULT;
		}
		if (!PluginControl.bypassLimit(player, ShopType.BUY)) {
			int limit = PluginControl.getLimit(player, ShopType.BUY);
			if (limit > -1) {
				if (crazyAuctions.getNumberOfPlayerItems(player, ShopType.BUY) >= limit) {
					Map<String, String> placeholders = new HashMap<String, String>();
					placeholders.put("%number%", String.valueOf(limit));
					player.sendMessage(Messages.getMessage("Max-Buying-Items", placeholders));
					return CommandType.DEFAULT;
				}
			}
		}

		int amount = argAsInteger(1, 1);
		if (amount > 64) {
			player.sendMessage(Messages.getMessage("Too-Many-Items"));
			return CommandType.DEFAULT;
		}

		UUID owner = player.getUniqueId();
		GlobalMarket market = GlobalMarket.getMarket();
		ItemStack item;
		if (args.length >= 4) {
			try {
				item = new ItemStack(Material.valueOf(args[3].toUpperCase()), amount);
			} catch (IllegalArgumentException ex) {
				Map<String, String> placeholders = new HashMap<String, String>();
				placeholders.put("%Item%", args[3]);
				placeholders.put("%item%", args[3]);
				sender.sendMessage(Messages.getMessage("Unknown-Item", placeholders));
				return CommandType.SUCCESS;
			}
		} else if (PluginControl.getItemInHand(player).getType() != Material.AIR) {
			item = PluginControl.getItemInHand(player).clone();
		} else {
			sender.sendMessage(Messages.getMessage("CrazyAuctions-Buy"));
			return CommandType.SUCCESS;
		}

		 item.setAmount(amount);
         MarketGoods goods = new MarketGoods(
             market.makeUID(),
             ShopType.BUY,
             new ItemOwner(owner, player.getName()),
             item,
             PluginControl.convertToMill(FileManager.Files.CONFIG.getFile().getString("Settings.Buy-Time")),
             PluginControl.convertToMill(FileManager.Files.CONFIG.getFile().getString("Settings.Full-Expire-Time")),
             reward
         );
         market.addGoods(goods);
         Bukkit.getPluginManager().callEvent(new AuctionListEvent(player, ShopType.BUY, item, reward, tax));
         Map<String, String> placeholders = new HashMap<String, String>();
         placeholders.put("%reward%", String.valueOf(reward));
         placeholders.put("%tax%", String.valueOf(tax));
         try {
             placeholders.put("%Item%", item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : (String) item.getClass().getMethod("getI18NDisplayName").invoke(item));
             placeholders.put("%item%", item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : (String) item.getClass().getMethod("getI18NDisplayName").invoke(item));
         } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
             placeholders.put("%Item%", item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().toString().toLowerCase().replace("_", " "));
             placeholders.put("%item%", item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().toString().toLowerCase().replace("_", " "));
         }
         player.sendMessage(Messages.getMessage("Added-Item-For-Acquisition", placeholders));
         CurrencyManager.removeMoney(player, reward + tax);
		
		return CommandType.SUCCESS;
	}

}
