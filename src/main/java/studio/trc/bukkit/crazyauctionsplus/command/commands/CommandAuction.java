package studio.trc.bukkit.crazyauctionsplus.command.commands;

import studio.trc.bukkit.crazyauctionsplus.Main;
import studio.trc.bukkit.crazyauctionsplus.command.CommandType;
import studio.trc.bukkit.crazyauctionsplus.command.VCommand;
import studio.trc.bukkit.crazyauctionsplus.command.commands.admin.CommandAuctionAdmin;
import studio.trc.bukkit.crazyauctionsplus.utils.enums.Messages;

public class CommandAuction extends VCommand {

	public CommandAuction() {
		this.setPermission("Access");
		this.addSubCommand(new CommandAuctionGui());
		this.addSubCommand(new CommandAuctionMail());
		this.addSubCommand(new CommandAuctionListed());
		this.addSubCommand(new CommandAuctionSell());
		this.addSubCommand(new CommandAuctionBid());
		this.addSubCommand(new CommandAuctionView());
		this.addSubCommand(new CommandAuctionBuy());
		this.addSubCommand(new CommandAuctionHelp());
		this.addSubCommand(new CommandAuctionAdmin());
	}

	@Override
	protected CommandType perform(Main plugin) {

		sender.sendMessage(Messages.getMessage("CrazyAuctions-Main").replace("{version}",
				Main.getInstance().getDescription().getVersion()));

		return CommandType.SUCCESS;
	}

}
