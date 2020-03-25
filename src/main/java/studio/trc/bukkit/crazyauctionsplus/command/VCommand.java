package studio.trc.bukkit.crazyauctionsplus.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import studio.trc.bukkit.crazyauctionsplus.Main;
import studio.trc.bukkit.crazyauctionsplus.utils.Arguments;

public abstract class VCommand extends Arguments {

	/**
	 * Permission used for the command, if it is a null then everyone can
	 * execute the command
	 */
	private String permission;
	

	/**
	 * Mother command of this command
	 */
	private VCommand parent;

	/**
	 * Are all sub commands used
	 */
	private List<String> subCommands = new ArrayList<String>();
	private List<VCommand> subVCommands = new ArrayList<VCommand>();

	private List<String> requireArgs = new ArrayList<String>();
	private List<String> optionalArgs = new ArrayList<String>();

	/**
	 * If this variable is false the command will not be able to use this
	 * command
	 */
	private boolean consoleCanUse = true;

	/**
	 * This variable allows to run the main class of the command even with
	 * arguments convenient for commands like /ban <player>
	 */
	private boolean ignoreParent = false;
	private boolean ignoreArgs = false;
	private String syntaxe;
	private String description;
	private int argsMinLength;
	private int argsMaxLength;

	/**
	 * This is the person who executes the command
	 */
	protected CommandSender sender;
	protected Player player;
	protected Main plugin;
	protected boolean DEBUG = false;

	/**
	 * @return the permission
	 */
	public String getPermission() {
		return permission;
	}

	/**
	 * @return the parent
	 */
	public VCommand getParent() {
		return parent;
	}

	/**
	 * @return the subCommand
	 */
	public List<String> getSubCommands() {
		return subCommands;
	}

	/**
	 * @return the consoleCanUse
	 */
	public boolean isConsoleCanUse() {
		return consoleCanUse;
	}

	/**
	 * @return the ignoreParent
	 */
	public boolean isIgnoreParent() {
		return ignoreParent;
	}

	public CommandSender getSender() {
		return sender;
	}

	/**
	 * @return the argsMinLength
	 */
	public int getArgsMinLength() {
		return argsMinLength;
	}

	/**
	 * @return the argsMaxLength
	 */
	public int getArgsMaxLength() {
		return argsMaxLength;
	}

	/**
	 * @return the player
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * @return the syntaxe
	 */
	public String getSyntaxe() {
		if (syntaxe == null) {
			syntaxe = generateDefaultSyntaxe("");
		}
		return syntaxe;
	}

	public boolean isIgnoreArgs() {
		return ignoreArgs;
	}

	public String getDescription() {
		return description == null ? "no description" : description;
	}

	//
	// SETTER
	//

	public void setIgnoreArgs(boolean ignoreArgs) {
		this.ignoreArgs = ignoreArgs;
	}

	public void setIgnoreParent(boolean ignoreParent) {
		this.ignoreParent = ignoreParent;
	}

	/**
	 * @param syntaxe
	 *            the syntaxe to set
	 */
	protected VCommand setSyntaxe(String syntaxe) {
		this.syntaxe = syntaxe;
		return this;
	}

	/**
	 * @param permission
	 *            the permission to set
	 */
	protected VCommand setPermission(String permission) {
		this.permission = permission;
		return this;
	}

	/**
	 * @param parent
	 *            the parent to set
	 */
	protected VCommand setParent(VCommand parent) {
		this.parent = parent;
		return this;
	}

	/**
	 * @param consoleCanUse
	 *            the consoleCanUse to set
	 */
	protected VCommand setConsoleCanUse(boolean consoleCanUse) {
		this.consoleCanUse = consoleCanUse;
		return this;
	}

	/**
	 * Mettre la description de la commande
	 * 
	 * @param description
	 * @return
	 */
	protected VCommand setDescription(String description) {
		this.description = description;
		return this;
	}

	/*
	 * Ajouter un argument obligatoire
	 */
	protected void addRequireArg(String message) {
		this.requireArgs.add(message);
		this.ignoreParent = parent == null ? true : false;
		this.ignoreArgs = true;
	}

	/**
	 * Ajouter un argument optionel
	 * 
	 * @param message
	 */
	protected void addOptionalArg(String message) {
		this.optionalArgs.add(message);
		this.ignoreParent = parent == null ? true : false;
		this.ignoreArgs = true;
	}

	//
	// OTHER
	//

	/**
	 * Adds sub orders
	 * 
	 * @param subCommand
	 * @return this
	 */
	public VCommand addSubCommand(String subCommand) {
		this.subCommands.add(subCommand);
		return this;
	}

	/**
	 * Adds sub orders
	 * 
	 * @param subCommand
	 * @return this
	 */
	public VCommand addSubCommand(VCommand command) {
		command.setParent(this);
		plugin.getCommandManager().addCommand(command);
		this.subVCommands.add(command);
		return this;
	}

	/**
	 * Adds sub orders
	 * 
	 * @param subCommand
	 * @return this
	 */
	public VCommand addSubCommand(String... subCommand) {
		this.subCommands.addAll(Arrays.asList(subCommand));
		return this;
	}

	/**
	 * Generate command syntax automatically
	 * 
	 * @param syntaxe
	 * @return generate syntaxe
	 */
	private String generateDefaultSyntaxe(String syntaxe) {

		String tmpString = subCommands.get(0);

		if (requireArgs.size() != 0 && syntaxe.equals(""))
			for (String requireArg : requireArgs) {
				requireArg = "<" + requireArg + ">";
				syntaxe += " " + requireArg;
			}
		if (optionalArgs.size() != 0 && syntaxe.equals(""))
			for (String optionalArg : optionalArgs) {
				optionalArg = "[<" + optionalArg + ">]";
				syntaxe += " " + optionalArg;
			}

		tmpString += syntaxe;

		if (parent == null)
			return "/" + tmpString;

		return parent.generateDefaultSyntaxe(" " + tmpString);
	}

	/**
	 * Get parent number
	 * 
	 * @param defaultParent
	 * @return
	 */
	private int parentCount(int defaultParent) {
		return parent == null ? defaultParent : parent.parentCount(defaultParent + 1);
	}

	public CommandType prePerform(Main main, CommandSender commandSender, String[] args) {

		this.plugin = main;
		
		parentCount = parentCount(0);
		argsMaxLength = requireArgs.size() + optionalArgs.size() + parentCount;
		argsMinLength = requireArgs.size() + parentCount;

		if (syntaxe == null)
			syntaxe = generateDefaultSyntaxe("");

		this.args = args;

		String defaultString = argAsString(0);

		if (defaultString != null) {
			for (VCommand subCommand : subVCommands) {
				if (subCommand.getSubCommands().contains(defaultString.toLowerCase()))
					return CommandType.CONTINUE;
			}
		}

		if (argsMinLength != 0 && argsMaxLength != 0
				&& !(args.length >= argsMinLength && args.length <= argsMaxLength)) {
			return CommandType.SYNTAX_ERROR;
		}

		this.sender = commandSender;
		if (sender instanceof Player)
			player = (Player) commandSender;

		try {
			return perform(main);
		} catch (Exception e) {
			if (DEBUG)
				e.printStackTrace();
			return CommandType.SYNTAX_ERROR;
		}
	}

	/**
	 * method that allows you to execute the command
	 */
	protected abstract CommandType perform(Main plugin);

	/**
	 * lets you know if the class girls have the same subcommands
	 * @return
	 */
	public boolean sameSubCommands() {
		if (parent == null)
			return false;
		for (String command : subCommands)
			if (parent.getSubCommands().contains(command))
				return true;
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "VCommand [permission=" + permission + ", subCommands=" + subCommands + ", consoleCanUse="
				+ consoleCanUse + ", description=" + description + "]";
	}

}
