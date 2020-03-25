package studio.trc.bukkit.crazyauctionsplus.command.commands.admin.collections;

import java.util.ArrayList;
import java.util.List;

import studio.trc.bukkit.crazyauctionsplus.Main;
import studio.trc.bukkit.crazyauctionsplus.command.CommandType;
import studio.trc.bukkit.crazyauctionsplus.command.VCommand;
import studio.trc.bukkit.crazyauctionsplus.utils.ItemCollection;
import studio.trc.bukkit.crazyauctionsplus.utils.enums.Messages;

public class CommandAuctionAdminItemCollectionList extends VCommand {

	public CommandAuctionAdminItemCollectionList() {
		this.addSubCommand("list");
		this.setPermission("Admin.SubCommands.ItemCollection.SubCommands.List");
		this.setConsoleCanUse(false);
	}

	@Override
	protected CommandType perform(Main plugin) {

		if (ItemCollection.getCollection().isEmpty()) {
			sender.sendMessage(Messages.getMessage("Admin-Command.ItemCollection.List.Empty-Collection"));
		} else {
			String format = Messages.getMessage("Admin-Command.ItemCollection.List.List-Format");
			List<String> list = new ArrayList<String>();
			for (ItemCollection collection : ItemCollection.getCollection()) {
				list.add(format.replace("%uid%", String.valueOf(collection.getUID())).replace("%item%",
						collection.getDisplayName()));
			}
			for (String message : Messages.getMessageList("Admin-Command.ItemCollection.List.Messages")) {
				sender.sendMessage(
						message.replace("%list%", list.toString().substring(1, list.toString().length() - 1)));
			}
		}

		return CommandType.SUCCESS;
	}

}
