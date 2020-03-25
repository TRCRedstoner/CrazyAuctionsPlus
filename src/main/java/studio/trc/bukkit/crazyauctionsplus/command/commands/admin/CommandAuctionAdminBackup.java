package studio.trc.bukkit.crazyauctionsplus.command.commands.admin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import studio.trc.bukkit.crazyauctionsplus.Main;
import studio.trc.bukkit.crazyauctionsplus.command.CommandType;
import studio.trc.bukkit.crazyauctionsplus.command.VCommand;
import studio.trc.bukkit.crazyauctionsplus.utils.FileManager;
import studio.trc.bukkit.crazyauctionsplus.utils.enums.Messages;

public class CommandAuctionAdminBackup extends VCommand {

	public CommandAuctionAdminBackup() {
		this.setPermission("Admin.SubCommands.Backup");
		this.addSubCommand("backup");
	}

	@Override
	protected CommandType perform(Main plugin) {
		
		if (FileManager.isBackingUp()) {
            sender.sendMessage(Messages.getMessage("Admin-Command.Backup.BackingUp"));
            return CommandType.DEFAULT;
        }
        sender.sendMessage(Messages.getMessage("Admin-Command.Backup.Starting"));
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.closeInventory();
        }
        FileManager.backup(sender);
		
		return CommandType.SUCCESS;
	}

}
