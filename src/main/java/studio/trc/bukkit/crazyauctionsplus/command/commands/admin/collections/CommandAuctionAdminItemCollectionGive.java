package studio.trc.bukkit.crazyauctionsplus.command.commands.admin.collections;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import studio.trc.bukkit.crazyauctionsplus.Main;
import studio.trc.bukkit.crazyauctionsplus.command.CommandType;
import studio.trc.bukkit.crazyauctionsplus.command.VCommand;
import studio.trc.bukkit.crazyauctionsplus.utils.ItemCollection;
import studio.trc.bukkit.crazyauctionsplus.utils.enums.Messages;

public class CommandAuctionAdminItemCollectionGive extends VCommand {

	public CommandAuctionAdminItemCollectionGive() {
		this.addSubCommand("give");
		this.setPermission("Admin.SubCommands.ItemCollection.SubCommands.Give");
		this.setConsoleCanUse(false);
		this.addRequireArg("uid/name");
		this.addOptionalArg("target");
	}

	@Override
	protected CommandType perform(Main plugin) {

		String arg = argAsString(0);
		Player player = argAsPlayer(1, this.player);

		try {
			long uid = Long.valueOf(arg);
			for (ItemCollection ic : ItemCollection.getCollection()) {
				if (ic.getUID() == uid) {
					Map<String, String> map = new HashMap<String, String>();
					map.put("%item%", ic.getDisplayName());
					map.put("%player%", player.getName());
					player.getInventory().addItem(ic.getItem());
					sender.sendMessage(Messages.getMessage("Admin-Command.ItemCollection.Give.Successfully", map));
					return CommandType.SUCCESS;
				}
			}
			Map<String, String> map = new HashMap<String, String>();
			map.put("%item%", arg);
			sender.sendMessage(Messages.getMessage("Admin-Command.ItemCollection.Give.Item-Not-Exist", map));
		} catch (NumberFormatException ex) {
			String displayName = arg;
			for (ItemCollection ic : ItemCollection.getCollection()) {
				if (ic.getDisplayName().equalsIgnoreCase(displayName)) {
					Map<String, String> map = new HashMap<String, String>();
					map.put("%item%", ic.getDisplayName());
					map.put("%player%", player.getName());
					player.getInventory().addItem(ic.getItem());
					sender.sendMessage(Messages.getMessage("Admin-Command.ItemCollection.Give.Successfully", map));
					return CommandType.SUCCESS;
				}
			}
			Map<String, String> map = new HashMap<String, String>();
			map.put("%item%", arg);
			sender.sendMessage(Messages.getMessage("Admin-Command.ItemCollection.Give.Item-Not-Exist", map));
		}

		return CommandType.SUCCESS;
	}

}
