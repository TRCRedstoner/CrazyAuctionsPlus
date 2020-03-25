package studio.trc.bukkit.crazyauctionsplus.command;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import studio.trc.bukkit.crazyauctionsplus.Main;
import studio.trc.bukkit.crazyauctionsplus.command.commands.CommandAuction;
import studio.trc.bukkit.crazyauctionsplus.utils.FileManager;
import studio.trc.bukkit.crazyauctionsplus.utils.ItemCollection;
import studio.trc.bukkit.crazyauctionsplus.utils.PluginControl;
import studio.trc.bukkit.crazyauctionsplus.utils.enums.Messages;

public class CommandManager implements CommandExecutor, TabCompleter {

	private final Main plugin;
	private final List<VCommand> commands = new ArrayList<VCommand>();

	/**
	 * 
	 * @param plugin
	 */
	public CommandManager(Main plugin) {
		super();
		this.plugin = plugin;
	}

	public void registerCommands() {

		this.registerCommand("ca", new CommandAuction(), "cap", "crazyauction", "crazyauctions", "crazyauctionsplus");

		this.plugin.log("Loading " + getUniqueCommand() + " commands");
		this.commandChecking();
	}

	/**
	 * 
	 * @param command
	 * @return
	 */
	public VCommand addCommand(VCommand command) {
		commands.add(command);
		return command;
	}

	/**
	 * @param string
	 * @param command
	 * @return
	 */
	public VCommand addCommand(String string, VCommand command) {
		commands.add(command.addSubCommand(string));
		plugin.getCommand(string).setExecutor(this);
		return command;
	}

	/**
	 * @param string
	 * @param vCommand
	 * @param aliases
	 */
	public void registerCommand(String string, VCommand vCommand, String... aliases) {
		try {
			Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
			bukkitCommandMap.setAccessible(true);

			CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());

			Class<? extends PluginCommand> class1 = PluginCommand.class;
			Constructor<? extends PluginCommand> constructor = class1.getDeclaredConstructor(String.class,
					Plugin.class);
			constructor.setAccessible(true);

			List<String> lists = Arrays.asList(aliases);

			PluginCommand command = constructor.newInstance(string, plugin);
			command.setExecutor(this);
			command.setAliases(lists);

			commands.add(vCommand.addSubCommand(string));
			vCommand.addSubCommand(aliases);

			commandMap.register(command.getName(), plugin.getDescription().getName(), command);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get the number of unique orders
	 * 
	 * @return
	 */
	private int getUniqueCommand() {
		return (int) commands.stream().filter(command -> command.getParent() == null).count();
	}

	/**
	 * Check if your commands is ready for use
	 */
	private void commandChecking() {
		commands.forEach(command -> {
			if (command.sameSubCommands()) {
				plugin.log(command.toString() + " command to an argument similar to its parent command !");
				plugin.getPluginLoader().disablePlugin(plugin);
			}
		});
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		for (VCommand command : commands) {
			if (command.getSubCommands().contains(cmd.getName().toLowerCase())) {
				if ((args.length == 0 || command.isIgnoreParent()) && command.getParent() == null) {
					CommandType type = processRequirements(command, sender, args);
					if (!type.equals(CommandType.CONTINUE))
						return true;
				}
			} else if (args.length >= 1 && command.getParent() != null
					&& canExecute(args, cmd.getName().toLowerCase(), command)) {
				CommandType type = processRequirements(command, sender, args);
				if (!type.equals(CommandType.CONTINUE))
					return true;
			}
		}
		sender.sendMessage(Messages.getMessage("CrazyAuctions-Help"));
		return true;
	}

	/**
	 * @param args
	 * @param cmd
	 * @param command
	 * @return true if can execute
	 */
	private boolean canExecute(String[] args, String cmd, VCommand command) {
		for (int index = args.length - 1; index > -1; index--) {
			if (command.getSubCommands().contains(args[index].toLowerCase())) {
				if (command.isIgnoreArgs()
						&& (command.getParent() != null ? canExecute(args, cmd, command.getParent(), index - 1) : true))
					return true;
				if (index < args.length - 1)
					return false;
				return canExecute(args, cmd, command.getParent(), index - 1);
			}
		}
		return false;
	}

	/**
	 * @param args
	 * @param cmd
	 * @param command
	 * @param index
	 * @return
	 */
	private boolean canExecute(String[] args, String cmd, VCommand command, int index) {
		if (index < 0 && command.getSubCommands().contains(cmd.toLowerCase()))
			return true;
		else if (index < 0)
			return false;
		else if (command.getSubCommands().contains(args[index].toLowerCase()))
			return canExecute(args, cmd, command.getParent(), index - 1);
		else
			return false;
	}

	/**
	 * @param command
	 * @param sender
	 * @param strings
	 * @return
	 */
	private CommandType processRequirements(VCommand command, CommandSender sender, String[] strings) {

		if (FileManager.isBackingUp()) {
			sender.sendMessage(Messages.getMessage("Admin-Command.Backup.BackingUp"));
			return CommandType.DEFAULT;
		}
		if (FileManager.isRollingBack()) {
			sender.sendMessage(Messages.getMessage("Admin-Command.RollBack.RollingBack"));
			return CommandType.DEFAULT;
		}

		if (!(sender instanceof Player) && !command.isConsoleCanUse()) {
			sender.sendMessage(Messages.getMessage("Players-Only"));
			return CommandType.DEFAULT;
		}

		if (command.getPermission() == null
				|| PluginControl.hasCommandPermission(sender, command.getPermission(), true)) {
			CommandType returnType = command.prePerform(plugin, sender, strings);
			if (returnType == CommandType.SYNTAX_ERROR)
				sender.sendMessage(PluginControl.color(PluginControl.getPrefix() + ChatColor.WHITE + command.getSyntaxe()));
			return returnType;
		}
		return CommandType.DEFAULT;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (FileManager.isBackingUp())
			return new ArrayList<String>();
		if (args.length == 1) {
			if (args[0].toLowerCase().startsWith("h") && PluginControl.hasCommandPermission(sender, "Help", false)) {
				return Arrays.asList("help");
			} else if (args[0].toLowerCase().startsWith("r")
					&& PluginControl.hasCommandPermission(sender, "Reload", false)) {
				return Arrays.asList("reload");
			} else if (args[0].toLowerCase().startsWith("s")
					&& PluginControl.hasCommandPermission(sender, "Sell", false)) {
				return Arrays.asList("sell");
			} else if (args[0].toLowerCase().startsWith("b")) {
				if (args[0].toLowerCase().startsWith("bi") && PluginControl.hasCommandPermission(sender, "Bid", false))
					return Arrays.asList("bid");
				if (args[0].toLowerCase().startsWith("bu") && PluginControl.hasCommandPermission(sender, "Buy", false))
					return Arrays.asList("buy");
				List<String> list = new ArrayList<String>();
				if (PluginControl.hasCommandPermission(sender, "Bid", false))
					list.add("bid");
				if (PluginControl.hasCommandPermission(sender, "Buy", false))
					list.add("buy");
				return list;
			} else if (args[0].toLowerCase().startsWith("l")
					&& PluginControl.hasCommandPermission(sender, "Listed", false)) {
				return Arrays.asList("listed");
			} else if (args[0].toLowerCase().startsWith("m")
					&& PluginControl.hasCommandPermission(sender, "Mail", false)) {
				return Arrays.asList("mail");
			} else if (args[0].toLowerCase().startsWith("v")
					&& PluginControl.hasCommandPermission(sender, "View", false)) {
				return Arrays.asList("view");
			} else if (args[0].toLowerCase().startsWith("g")
					&& PluginControl.hasCommandPermission(sender, "Gui", false)) {
				return Arrays.asList("gui");
			} else if (args[0].toLowerCase().startsWith("a")
					&& PluginControl.hasCommandPermission(sender, "Admin", false)) {
				return Arrays.asList("admin");
			}
			List<String> list = new ArrayList<String>();
			if (PluginControl.hasCommandPermission(sender, "Help", false))
				list.add("help");
			if (PluginControl.hasCommandPermission(sender, "Gui", false))
				list.add("gui");
			if (PluginControl.hasCommandPermission(sender, "Sell", false))
				list.add("sell");
			if (PluginControl.hasCommandPermission(sender, "Buy", false))
				list.add("buy");
			if (PluginControl.hasCommandPermission(sender, "Bid", false))
				list.add("bid");
			if (PluginControl.hasCommandPermission(sender, "View", false))
				list.add("view");
			if (PluginControl.hasCommandPermission(sender, "Listed", false))
				list.add("listed");
			if (PluginControl.hasCommandPermission(sender, "Mail", false))
				list.add("mail");
			if (PluginControl.hasCommandPermission(sender, "Reload", false))
				list.add("reload");
			if (PluginControl.hasCommandPermission(sender, "Admin", false))
				list.add("admin");
			return list;
		} else if (args.length >= 2) {
			if (args[0].equalsIgnoreCase("reload") && PluginControl.hasCommandPermission(sender, "Reload", false)) {
				List<String> list = new ArrayList<String>();
				for (String text : new String[] { "all", "database", "config", "messages", "market", "playerdata",
						"category", "itemcollection" }) {
					if (text.toLowerCase().startsWith(args[1].toLowerCase())) {
						list.add(text);
					}
				}
				return list;
			}
			if (args[0].equalsIgnoreCase("admin") && PluginControl.hasCommandPermission(sender, "Admin", false)) {
				if (args.length >= 3) {
					if (args[1].equalsIgnoreCase("rollback")
							&& PluginControl.hasCommandPermission(sender, "Admin.SubCommands.RollBack", false)) {
						List<String> list = new ArrayList<String>();
						for (String string : PluginControl.getBackupFiles()) {
							if (string.toLowerCase().startsWith(args[2].toLowerCase())) {
								list.add(string);
							}
						}
						return list;
					}
					if (args[1].equalsIgnoreCase("info")
							&& PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Info", false)) {
						List<String> list = new ArrayList<String>();
						for (Player p : Bukkit.getOnlinePlayers()) {
							if (p.getName().toLowerCase().startsWith(args[2].toLowerCase())) {
								list.add(p.getName());
							}
						}
						return list;
					}
					if (args[1].equalsIgnoreCase("itemcollection")
							&& PluginControl.hasCommandPermission(sender, "Admin.SubCommands.ItemCollection", false)) {
						if (args.length == 3) {
							List<String> list = new ArrayList<String>();
							for (String text : new String[] { "help", "add", "delete", "give", "list" }) {
								if (text.toLowerCase().startsWith(args[2].toLowerCase())) {
									list.add(text);
								}
							}
							return list;
						} else if (args.length >= 4) {
							if (args[2].equalsIgnoreCase("delete")) {
								List<String> list = new ArrayList<String>();
								for (ItemCollection ic : ItemCollection.getCollection()) {
									if (ic.getDisplayName().toLowerCase().startsWith(args[3].toLowerCase())) {
										list.add(ic.getDisplayName());
									}
								}
								return list;
							} else if (args[2].equalsIgnoreCase("give")) {
								if (args.length == 4) {
									List<String> list = new ArrayList<String>();
									for (ItemCollection ic : ItemCollection.getCollection()) {
										if (ic.getDisplayName().toLowerCase().startsWith(args[3].toLowerCase())) {
											list.add(ic.getDisplayName());
										}
									}
									return list;
								} else {
									List<String> list = new ArrayList<String>();
									for (Player p : Bukkit.getOnlinePlayers()) {
										if (p.getName().toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
											list.add(p.getName());
										}
									}
									return list;
								}
							} else {
								return new ArrayList<String>();
							}
						}
					}
				}
				List<String> list = new ArrayList<String>();
				for (String text : new String[] { "backup", "rollback", "info", "synchronize", "itemcollection" }) {
					if (text.toLowerCase().startsWith(args[1].toLowerCase())) {
						list.add(text);
					}
				}
				return list;
			}
			if (args[0].equalsIgnoreCase("view")
					&& PluginControl.hasCommandPermission(sender, "View-Others-Player", false)) {
				List<String> players = new ArrayList<String>();
				for (Player ps : Bukkit.getOnlinePlayers()) {
					if (ps.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
						players.add(ps.getName());
					}
				}
				return players;
			}
			if (args[0].equalsIgnoreCase("buy") && args.length == 4
					&& PluginControl.hasCommandPermission(sender, "Buy", false)) {
				if (sender instanceof Player) {
					List<String> list = new ArrayList<String>();
					for (Material m : Material.values()) {
						if (m.toString().toLowerCase().startsWith(args[3].toLowerCase())) {
							list.add(m.toString().toLowerCase());
						}
					}
					return list;
				}
			}
			if (args[0].equalsIgnoreCase("gui") && PluginControl.hasCommandPermission(sender, "Gui", false)) { // gui
																												// buy
				if (args.length == 2) {
					if (sender instanceof Player) {
						if (args[1].toLowerCase().startsWith("s")) {
							return Arrays.asList("sell");
						} else if (args[1].toLowerCase().startsWith("b")) {
							if (args[1].toLowerCase().startsWith("bu"))
								return Arrays.asList("buy");
							if (args[1].toLowerCase().startsWith("bi"))
								return Arrays.asList("bid");
							return Arrays.asList("buy", "bid");
						}
						return Arrays.asList("sell", "buy", "bid");
					}
				} else if (args.length == 3 && PluginControl.hasCommandPermission(sender, "Gui-Others-Player", false)) {
					if (sender instanceof Player) {
						List<String> list = new ArrayList<String>();
						for (Player p : Bukkit.getOnlinePlayers()) {
							if (p.getName().toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
								list.add(p.getName());
							}
						}
						return list;
					}
				}
			}
		}
		return new ArrayList<String>();
	}
	
}
