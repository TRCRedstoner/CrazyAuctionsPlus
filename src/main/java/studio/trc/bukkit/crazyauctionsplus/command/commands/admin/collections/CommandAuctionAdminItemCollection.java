package studio.trc.bukkit.crazyauctionsplus.command.commands.admin.collections;

import studio.trc.bukkit.crazyauctionsplus.Main;
import studio.trc.bukkit.crazyauctionsplus.command.CommandType;
import studio.trc.bukkit.crazyauctionsplus.command.VCommand;
import studio.trc.bukkit.crazyauctionsplus.utils.enums.Messages;

public class CommandAuctionAdminItemCollection extends VCommand {

	public CommandAuctionAdminItemCollection() {
		this.setPermission("Admin.SubCommands.ItemCollection");
		this.addSubCommand("itemcollection");
		this.addSubCommand(new CommandAuctionAdminItemCollectionAdd());
		this.addSubCommand(new CommandAuctionAdminItemCollectionRemove());
		this.addSubCommand(new CommandAuctionAdminItemCollectionList());
		this.addSubCommand(new CommandAuctionAdminItemCollectionGive());
	}

	@Override
	protected CommandType perform(Main plugin) {
		
		for (String message : Messages.getMessageList("Admin-Command.ItemCollection.Help")) {
            sender.sendMessage(message);
        }
		
		return CommandType.SUCCESS;
	}

}
