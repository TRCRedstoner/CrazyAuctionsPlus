package studio.trc.bukkit.crazyauctionsplus.command.commands;

import studio.trc.bukkit.crazyauctionsplus.Main;
import studio.trc.bukkit.crazyauctionsplus.command.CommandType;
import studio.trc.bukkit.crazyauctionsplus.command.VCommand;
import studio.trc.bukkit.crazyauctionsplus.events.GUIAction;
import studio.trc.bukkit.crazyauctionsplus.utils.Category;
import studio.trc.bukkit.crazyauctionsplus.utils.FileManager.Files;
import studio.trc.bukkit.crazyauctionsplus.utils.PluginControl;
import studio.trc.bukkit.crazyauctionsplus.utils.enums.Messages;
import studio.trc.bukkit.crazyauctionsplus.utils.enums.ShopType;

public class CommandAuctionGui extends VCommand {

	public CommandAuctionGui() {
		this.addSubCommand("gui");
		this.setPermission("Gui");
		this.setConsoleCanUse(false);
		this.addOptionalArg("type");
	}

	@Override
	protected CommandType perform(Main plugin) {

		if (PluginControl.isWorldDisabled(player)) {
			sender.sendMessage(Messages.getMessage("World-Disabled"));
			return CommandType.DEFAULT;
		}

		String gui = argAsString(0, "default");

		if (gui.equals("default")) {
			if (Files.CONFIG.getFile().getBoolean("Settings.Category-Page-Opens-First")) {
				GUIAction.setShopType(player, ShopType.ANY);
				GUIAction.setCategory(player, Category.getDefaultCategory());
				GUIAction.openCategories(player, ShopType.ANY);
			} else {
				GUIAction.openShop(player, ShopType.ANY, Category.getDefaultCategory(), 1);
			}
		} else if (gui.equalsIgnoreCase("sell")) {
			GUIAction.openShop(player, ShopType.SELL, Category.getDefaultCategory(), 1);
		} else if (gui.equalsIgnoreCase("buy")) {
			GUIAction.openShop(player, ShopType.BUY, Category.getDefaultCategory(), 1);
		} else if (gui.equalsIgnoreCase("bid")) {
			GUIAction.openShop(player, ShopType.BID, Category.getDefaultCategory(), 1);
		} else {
			GUIAction.openShop(player, ShopType.ANY, Category.getDefaultCategory(), 1);
		}

		return CommandType.SUCCESS;
	}

}
