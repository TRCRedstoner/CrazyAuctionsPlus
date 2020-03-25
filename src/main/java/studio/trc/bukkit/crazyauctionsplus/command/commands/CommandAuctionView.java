package studio.trc.bukkit.crazyauctionsplus.command.commands;

import org.bukkit.entity.Player;

import studio.trc.bukkit.crazyauctionsplus.Main;
import studio.trc.bukkit.crazyauctionsplus.command.CommandType;
import studio.trc.bukkit.crazyauctionsplus.command.VCommand;
import studio.trc.bukkit.crazyauctionsplus.events.GUIAction;
import studio.trc.bukkit.crazyauctionsplus.utils.PluginControl;
import studio.trc.bukkit.crazyauctionsplus.utils.enums.Messages;

public class CommandAuctionView extends VCommand {

	public CommandAuctionView() {
		this.setConsoleCanUse(false);
		this.addSubCommand("view");
		this.setPermission("View");
		this.addOptionalArg("player");
	}

	@Override
	protected CommandType perform(Main plugin) {

		if (PluginControl.isWorldDisabled(player)) {
			sender.sendMessage(Messages.getMessage("World-Disabled"));
			return CommandType.DEFAULT;
		}

		Player target = argAsPlayer(0, null);

		if (target == null) {

			GUIAction.openViewer(player, player.getUniqueId(), 0);

		} else {
			if (!PluginControl.hasCommandPermission(sender, "View-Others-Player", true))
				return CommandType.DEFAULT;

			GUIAction.openViewer(player, target.getUniqueId(), 1);

		}

		return CommandType.SUCCESS;
	}

}
