package studio.trc.bukkit.crazyauctionsplus.command.commands.admin.collections;

import java.util.HashMap;
import java.util.Map;

import studio.trc.bukkit.crazyauctionsplus.Main;
import studio.trc.bukkit.crazyauctionsplus.command.CommandType;
import studio.trc.bukkit.crazyauctionsplus.command.VCommand;
import studio.trc.bukkit.crazyauctionsplus.utils.ItemCollection;
import studio.trc.bukkit.crazyauctionsplus.utils.enums.Messages;

public class CommandAuctionAdminItemCollectionAdd extends VCommand {

	public CommandAuctionAdminItemCollectionAdd() {
		this.addSubCommand("add");
		this.setPermission("Admin.SubCommands.ItemCollection.SubCommands.Add");
		this.setConsoleCanUse(false);
		this.addRequireArg("name");
	}

	@SuppressWarnings("deprecation")
	@Override
	protected CommandType perform(Main plugin) {

		if (player.getItemInHand() == null) {
			sender.sendMessage(Messages.getMessage("Admin-Command.ItemCollection.Add.Doesnt-Have-Item-In-Hand"));
			return CommandType.DEFAULT;
		}
		if (ItemCollection.addItem(player.getItemInHand(), argAsString(0))) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("%item%", argAsString(0));
			sender.sendMessage(Messages.getMessage("Admin-Command.ItemCollection.Add.Successfully", map));
		} else {
			sender.sendMessage(Messages.getMessage("Admin-Command.ItemCollection.Add.Already-Exist"));
		}

		return CommandType.SUCCESS;
	}

}
