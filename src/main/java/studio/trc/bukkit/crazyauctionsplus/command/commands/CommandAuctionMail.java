package studio.trc.bukkit.crazyauctionsplus.command.commands;

import studio.trc.bukkit.crazyauctionsplus.Main;
import studio.trc.bukkit.crazyauctionsplus.command.CommandType;
import studio.trc.bukkit.crazyauctionsplus.command.VCommand;
import studio.trc.bukkit.crazyauctionsplus.events.GUIAction;
import studio.trc.bukkit.crazyauctionsplus.utils.PluginControl;
import studio.trc.bukkit.crazyauctionsplus.utils.enums.Messages;

public class CommandAuctionMail extends VCommand {

	public CommandAuctionMail() {
		this.setPermission("Mail");
		this.setConsoleCanUse(false);
		this.addSubCommand("mail");
	}

	@Override
	protected CommandType perform(Main plugin) {

		if (PluginControl.isWorldDisabled(player)) {
			sender.sendMessage(Messages.getMessage("World-Disabled"));
			return CommandType.DEFAULT;
		}
		GUIAction.openPlayersMail(player, 1);

		return CommandType.SUCCESS;
	}

}
