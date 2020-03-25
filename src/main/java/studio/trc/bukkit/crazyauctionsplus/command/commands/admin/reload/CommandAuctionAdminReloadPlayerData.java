package studio.trc.bukkit.crazyauctionsplus.command.commands.admin.reload;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import studio.trc.bukkit.crazyauctionsplus.Main;
import studio.trc.bukkit.crazyauctionsplus.command.CommandType;
import studio.trc.bukkit.crazyauctionsplus.command.VCommand;
import studio.trc.bukkit.crazyauctionsplus.utils.PluginControl;
import studio.trc.bukkit.crazyauctionsplus.utils.PluginControl.ReloadType;
import studio.trc.bukkit.crazyauctionsplus.utils.enums.Messages;

public class CommandAuctionAdminReloadPlayerData extends VCommand {

	public CommandAuctionAdminReloadPlayerData() {
		this.setPermission("Reload.SubCommands.PlayerData");
		this.addSubCommand("playerdata");
	}

	@Override
	protected CommandType perform(Main plugin) {

		PluginControl.reload(ReloadType.PLAYERDATA);
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.closeInventory();
		}
		sender.sendMessage(Messages.getMessage("Reload-PlayerData"));

		return CommandType.SUCCESS;
	}

}
