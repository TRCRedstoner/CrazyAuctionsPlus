package studio.trc.bukkit.crazyauctionsplus.command.commands.admin;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import studio.trc.bukkit.crazyauctionsplus.Main;
import studio.trc.bukkit.crazyauctionsplus.command.CommandType;
import studio.trc.bukkit.crazyauctionsplus.command.VCommand;
import studio.trc.bukkit.crazyauctionsplus.utils.FileManager;
import studio.trc.bukkit.crazyauctionsplus.utils.enums.Messages;

public class CommandAuctionAdminRollBack extends VCommand {

	public CommandAuctionAdminRollBack() {
		this.setPermission("Admin.SubCommands.RollBack");
		this.addRequireArg("file");
		this.addSubCommand("rollback");
	}

	@Override
	protected CommandType perform(Main plugin) {

		String file = argAsString(0);

		File backupFile = new File("plugins/CrazyAuctionsPlus/Backup/" + file);
		if (backupFile.exists()) {
			sender.sendMessage(Messages.getMessage("Admin-Command.RollBack.Starting"));
			for (Player player : Bukkit.getOnlinePlayers()) {
				player.closeInventory();
			}
			FileManager.rollBack(backupFile, sender);
		} else {
			sender.sendMessage(Messages.getMessage("Admin-Command.RollBack.Backup-Not-Exist").replace("%file%", file));
		}

		return CommandType.SUCCESS;
	}

}
