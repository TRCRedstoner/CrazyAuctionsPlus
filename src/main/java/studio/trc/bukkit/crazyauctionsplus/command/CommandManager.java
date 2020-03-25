package studio.trc.bukkit.crazyauctionsplus.command;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import studio.trc.bukkit.crazyauctionsplus.Main;
import studio.trc.bukkit.crazyauctionsplus.utils.PluginControl;
import studio.trc.bukkit.crazyauctionsplus.utils.enums.Messages;

public class CommandManager implements CommandExecutor {

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

	public void registerCommands() {

		plugin.log("Loading " + getUniqueCommand() + " commands");
		this.commandChecking();
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

		if (!(sender instanceof Player) && !command.isConsoleCanUse()) {
			sender.sendMessage(Messages.getMessage("Players-Only"));
			return CommandType.DEFAULT;
		}
		if (command.getPermission() == null
				|| PluginControl.hasCommandPermission(sender, command.getPermission(), true)) {
			CommandType returnType = command.prePerform(plugin, sender, strings);
			if (returnType == CommandType.SYNTAX_ERROR)
				sender.sendMessage(PluginControl.getPrefix() + command.getSyntaxe());
			return returnType;
		}
		return CommandType.DEFAULT;
	}

}
