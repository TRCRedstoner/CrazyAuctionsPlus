package studio.trc.bukkit.crazyauctionsplus.command.commands.admin.reload;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import studio.trc.bukkit.crazyauctionsplus.Main;
import studio.trc.bukkit.crazyauctionsplus.command.CommandType;
import studio.trc.bukkit.crazyauctionsplus.command.VCommand;
import studio.trc.bukkit.crazyauctionsplus.utils.PluginControl;
import studio.trc.bukkit.crazyauctionsplus.utils.PluginControl.ReloadType;
import studio.trc.bukkit.crazyauctionsplus.utils.enums.Messages;

public class CommandAuctionAdminReload extends VCommand {

	public CommandAuctionAdminReload() {
		this.addSubCommand("reload");
		this.setPermission("Reload");
		this.addSubCommand(new CommandAuctionAdminReloadDatabase());
		this.addSubCommand(new CommandAuctionAdminReloadConfig());
		this.addSubCommand(new CommandAuctionAdminReloadMarket());
		this.addSubCommand(new CommandAuctionAdminReloadMessages());
		this.addSubCommand(new CommandAuctionAdminReloadPlayerData());
		this.addSubCommand(new CommandAuctionAdminReloadCategory());
		this.addSubCommand(new CommandAuctionAdminReloadItemCollection());
	}

	@Override
	protected CommandType perform(Main plugin) {
		
		PluginControl.reload(ReloadType.ALL);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.closeInventory();
        }
        sender.sendMessage(Messages.getMessage("Reload"));
		
		return CommandType.SUCCESS;
	}

}
