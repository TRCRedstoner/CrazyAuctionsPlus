package studio.trc.bukkit.crazyauctionsplus.command.commands.admin.collections;

import java.util.HashMap;
import java.util.Map;

import studio.trc.bukkit.crazyauctionsplus.Main;
import studio.trc.bukkit.crazyauctionsplus.command.CommandType;
import studio.trc.bukkit.crazyauctionsplus.command.VCommand;
import studio.trc.bukkit.crazyauctionsplus.utils.ItemCollection;
import studio.trc.bukkit.crazyauctionsplus.utils.enums.Messages;

public class CommandAuctionAdminItemCollectionRemove extends VCommand {

	public CommandAuctionAdminItemCollectionRemove() {
		this.addSubCommand("delete");
		this.addSubCommand("remove");
		this.setPermission("Admin.SubCommands.ItemCollection.SubCommands.Delete");
		this.setConsoleCanUse(false);
		this.addRequireArg("uid/name");
	}

	@Override
	protected CommandType perform(Main plugin) {

		String arg = argAsString(0);

		try {
			long uid = Long.valueOf(arg);
			for (ItemCollection ic : ItemCollection.getCollection()) {
				if (ic.getUID() == uid) {
					Map<String, String> map = new HashMap<String, String>();
					map.put("%item%", ic.getDisplayName());
					ItemCollection.deleteItem(uid);
					sender.sendMessage(Messages.getMessage("Admin-Command.ItemCollection.Delete.Successfully", map));
					return CommandType.SUCCESS;
				}
			}
			Map<String, String> map = new HashMap<String, String>();
			map.put("%item%", arg);
			sender.sendMessage(Messages.getMessage("Admin-Command.ItemCollection.Delete.Item-Not-Exist", map));
		} catch (NumberFormatException ex) {
			String displayName = arg;
			for (ItemCollection ic : ItemCollection.getCollection()) {
				if (ic.getDisplayName().equalsIgnoreCase(displayName)) {
					Map<String, String> map = new HashMap<String, String>();
					map.put("%item%", ic.getDisplayName());
					ItemCollection.deleteItem(displayName);
					sender.sendMessage(Messages.getMessage("Admin-Command.ItemCollection.Delete.Successfully", map));
					return CommandType.SUCCESS;
				}
			}
			Map<String, String> map = new HashMap<String, String>();
			map.put("%item%", arg);
			sender.sendMessage(Messages.getMessage("Admin-Command.ItemCollection.Delete.Item-Not-Exist", map));
		}

		return CommandType.SUCCESS;
	}

}
