package studio.trc.bukkit.crazyauctionsplus.command.commands;

import studio.trc.bukkit.crazyauctionsplus.Main;
import studio.trc.bukkit.crazyauctionsplus.command.CommandType;
import studio.trc.bukkit.crazyauctionsplus.command.VCommand;
import studio.trc.bukkit.crazyauctionsplus.events.GUIAction;
import studio.trc.bukkit.crazyauctionsplus.utils.PluginControl;
import studio.trc.bukkit.crazyauctionsplus.utils.enums.Messages;

public class CommandAuctionListed extends VCommand {

	public CommandAuctionListed() {
		this.setPermission("Listed");
		this.setConsoleCanUse(false);
		this.addSubCommand("listed");
	}

	@Override
	protected CommandType perform(Main plugin) {

		if (PluginControl.isWorldDisabled(player)) {
			sender.sendMessage(Messages.getMessage("World-Disabled"));
			return CommandType.DEFAULT;
		}
		GUIAction.openPlayersCurrentList(player, 1);

		return CommandType.SUCCESS;
	}

}
