package studio.trc.bukkit.crazyauctionsplus.command.commands.admin;

import studio.trc.bukkit.crazyauctionsplus.Main;
import studio.trc.bukkit.crazyauctionsplus.command.CommandType;
import studio.trc.bukkit.crazyauctionsplus.command.VCommand;
import studio.trc.bukkit.crazyauctionsplus.command.commands.admin.collections.CommandAuctionAdminItemCollection;
import studio.trc.bukkit.crazyauctionsplus.command.commands.admin.reload.CommandAuctionAdminReload;
import studio.trc.bukkit.crazyauctionsplus.utils.enums.Messages;

public class CommandAuctionAdmin extends VCommand {

	public CommandAuctionAdmin() {
		this.setPermission("Admin");
		this.addSubCommand("admin");
		
		this.addSubCommand(new CommandAuctionAdminBackup());
		this.addSubCommand(new CommandAuctionAdminRollBack());
		this.addSubCommand(new CommandAuctionAdminInfo());
		this.addSubCommand(new CommandAuctionAdminSynchronize());
		this.addSubCommand(new CommandAuctionAdminItemCollection());
		this.addSubCommand(new CommandAuctionAdminReload());
	}

	@Override
	protected CommandType perform(Main plugin) {
		
		 for (String message : Messages.getMessageList("Admin-Menu")) {
             sender.sendMessage(message);
         }
		
		return CommandType.SUCCESS;
	}

}
