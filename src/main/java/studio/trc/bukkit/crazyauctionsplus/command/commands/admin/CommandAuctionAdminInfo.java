package studio.trc.bukkit.crazyauctionsplus.command.commands.admin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import studio.trc.bukkit.crazyauctionsplus.Main;
import studio.trc.bukkit.crazyauctionsplus.command.CommandType;
import studio.trc.bukkit.crazyauctionsplus.command.VCommand;
import studio.trc.bukkit.crazyauctionsplus.database.GlobalMarket;
import studio.trc.bukkit.crazyauctionsplus.database.engine.MySQLEngine;
import studio.trc.bukkit.crazyauctionsplus.database.engine.SQLiteEngine;
import studio.trc.bukkit.crazyauctionsplus.utils.MarketGoods;
import studio.trc.bukkit.crazyauctionsplus.utils.PluginControl;
import studio.trc.bukkit.crazyauctionsplus.utils.enums.Messages;

public class CommandAuctionAdminInfo extends VCommand {

	public CommandAuctionAdminInfo() {
		this.setPermission("Admin.SubCommands.Info");
		this.addSubCommand("info");
		this.addRequireArg("player");
	}

	@Override
	protected CommandType perform(Main plugin) {

		Player target = argAsPlayer(0);

		if (target == null) {

			@SuppressWarnings("deprecation")
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(argAsString(0));

			if (offlinePlayer != null) {
				int items = 0;
				String database;
				if (PluginControl.useSplitDatabase()) {
					switch (PluginControl.getItemMailStorageMethod()) {
					case MySQL: {
						database = "[MySQL] [Database: " + MySQLEngine.getDatabaseName() + "] -> [Table: "
								+ MySQLEngine.getItemMailTable() + "] -> [Colunm: UUID:" + offlinePlayer.getUniqueId()
								+ "]";
						break;
					}
					case SQLite: {
						database = "[SQLite] [" + SQLiteEngine.getFilePath() + SQLiteEngine.getFileName()
								+ "] -> [Table: " + MySQLEngine.getItemMailTable() + "] -> [Colunm: UUID:"
								+ offlinePlayer.getUniqueId() + "]";
						break;
					}
					default: {
						database = new File("plugins/CrazyAuctionsPlus/Players/" + offlinePlayer.getUniqueId() + ".yml")
								.getPath();
						break;
					}
					}
				} else if (PluginControl.useMySQLStorage()) {
					database = "[MySQL] [Database: " + MySQLEngine.getDatabaseName() + "] -> [Table: "
							+ MySQLEngine.getItemMailTable() + "] -> [Colunm: UUID:" + offlinePlayer.getUniqueId()
							+ "]";
				} else if (PluginControl.useSQLiteStorage()) {
					database = "[SQLite] [" + SQLiteEngine.getFilePath() + SQLiteEngine.getFileName() + "] -> [Table: "
							+ MySQLEngine.getItemMailTable() + "] -> [Colunm: UUID:" + offlinePlayer.getUniqueId()
							+ "]";
				} else {
					database = new File("plugins/CrazyAuctionsPlus/Players/" + offlinePlayer.getUniqueId() + ".yml")
							.getPath();
				}
				for (MarketGoods mg : GlobalMarket.getMarket().getItems()) {
					if (mg.getItemOwner().getUUID().equals(offlinePlayer.getUniqueId())) {
						items++;
					}
				}
				for (String message : Messages.getMessageList("Admin-Command.Info.Info-Messages")) {
					sender.sendMessage(message.replace("%player%", offlinePlayer.getName())
							.replace("%group%", Messages.getMessage("Admin-Command.Info.Unknown"))
							.replace("%items%", String.valueOf(items)).replace("%database%", database));
				}
			} else {
				Map<String, String> map = new HashMap<String, String>();
				map.put("%player%", args[2]);
				sender.sendMessage(Messages.getMessage("Admin-Command.Info.Unknown-Player", map));
			}

		} else {

			int items = 0;
			String group = PluginControl.getMarketGroup(player).getGroupName();
			String database;
			if (PluginControl.useSplitDatabase()) {
				switch (PluginControl.getItemMailStorageMethod()) {
				case MySQL: {
					database = "[MySQL] [Database: " + MySQLEngine.getDatabaseName() + "] -> [Table: "
							+ MySQLEngine.getItemMailTable() + "] -> [Colunm: UUID:" + player.getUniqueId() + "]";
					break;
				}
				case SQLite: {
					database = "[SQLite] [" + SQLiteEngine.getFilePath() + SQLiteEngine.getFileName() + "] -> [Table: "
							+ MySQLEngine.getItemMailTable() + "] -> [Colunm: UUID:" + player.getUniqueId() + "]";
					break;
				}
				default: {
					database = new File("plugins/CrazyAuctionsPlus/Players/" + player.getUniqueId() + ".yml").getPath();
					break;
				}
				}
			} else if (PluginControl.useMySQLStorage()) {
				database = "[MySQL] [Database: " + MySQLEngine.getDatabaseName() + "] -> [Table: "
						+ MySQLEngine.getItemMailTable() + "] -> [Colunm: UUID:" + player.getUniqueId() + "]";
			} else if (PluginControl.useSQLiteStorage()) {
				database = "[SQLite] [" + SQLiteEngine.getFilePath() + SQLiteEngine.getFileName() + "] -> [Table: "
						+ MySQLEngine.getItemMailTable() + "] -> [Colunm: UUID:" + player.getUniqueId() + "]";
			} else {
				database = new File("plugins/CrazyAuctionsPlus/Players/" + player.getUniqueId() + ".yml").getPath();
			}
			for (MarketGoods mg : GlobalMarket.getMarket().getItems()) {
				if (mg.getItemOwner().getUUID().equals(player.getUniqueId())) {
					items++;
				}
			}
			for (String message : Messages.getMessageList("Admin-Command.Info.Info-Messages")) {
				sender.sendMessage(message.replace("%player%", player.getName()).replace("%group%", group)
						.replace("%items%", String.valueOf(items)).replace("%database%", database));
			}

		}

		return CommandType.SUCCESS;
	}

}
