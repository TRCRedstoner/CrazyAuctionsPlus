package studio.trc.bukkit.crazyauctionsplus.command;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import studio.trc.bukkit.crazyauctionsplus.Main;
import studio.trc.bukkit.crazyauctionsplus.api.events.AuctionListEvent;
import studio.trc.bukkit.crazyauctionsplus.currency.CurrencyManager;
import studio.trc.bukkit.crazyauctionsplus.database.GlobalMarket;
import studio.trc.bukkit.crazyauctionsplus.database.engine.MySQLEngine;
import studio.trc.bukkit.crazyauctionsplus.database.engine.SQLiteEngine;
import studio.trc.bukkit.crazyauctionsplus.events.GUIAction;
import studio.trc.bukkit.crazyauctionsplus.utils.Category;
import studio.trc.bukkit.crazyauctionsplus.utils.CrazyAuctions;
import studio.trc.bukkit.crazyauctionsplus.utils.FileManager;
import studio.trc.bukkit.crazyauctionsplus.utils.FileManager.Files;
import studio.trc.bukkit.crazyauctionsplus.utils.ItemCollection;
import studio.trc.bukkit.crazyauctionsplus.utils.ItemOwner;
import studio.trc.bukkit.crazyauctionsplus.utils.MarketGoods;
import studio.trc.bukkit.crazyauctionsplus.utils.PluginControl;
import studio.trc.bukkit.crazyauctionsplus.utils.PluginControl.ReloadType;
import studio.trc.bukkit.crazyauctionsplus.utils.enums.Messages;
import studio.trc.bukkit.crazyauctionsplus.utils.enums.ShopType;
import studio.trc.bukkit.crazyauctionsplus.utils.enums.Version;

@SuppressWarnings("deprecation")
public class PluginCommand
    implements CommandExecutor, TabCompleter
{
    public static FileManager fileManager = FileManager.getInstance();
    public static CrazyAuctions crazyAuctions = CrazyAuctions.getInstance();
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String lable, String[] args) {
        if (lable.equalsIgnoreCase("CrazyAuctions") || lable.equalsIgnoreCase("CrazyAuction") || lable.equalsIgnoreCase("CA") || lable.equalsIgnoreCase("CAP") || lable.equalsIgnoreCase("CrazyAuctionsPlus")) {
            if (FileManager.isBackingUp()) {
                sender.sendMessage(Messages.getMessage("Admin-Command.Backup.BackingUp"));
                return true;
            }
            if (FileManager.isRollingBack()) {
                sender.sendMessage(Messages.getMessage("Admin-Command.RollBack.RollingBack"));
                return true;
            }
//            if (sender instanceof Player) {
//                Player player = (Player) sender;
//                if (PluginControl.isWorldDisabled(player)) {
//                    sender.sendMessage(Messages.getMessage("World-Disabled"));
//                    return true;
//                }
//            }
            if (args.length == 0) {
                if (!PluginControl.hasCommandPermission(sender, "Access", true)) return true;
                sender.sendMessage(Messages.getMessage("CrazyAuctions-Main").replace("{version}", Main.getInstance().getDescription().getVersion()));
                return true;
            }
            if (args.length >= 1) {              
                if (args[0].equalsIgnoreCase("Reload")) {
                    if (!PluginControl.hasCommandPermission(sender, "Reload", true)) return true;
                    if (args.length == 1) {
                        PluginControl.reload(ReloadType.ALL);
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            player.closeInventory();
                        }
                        sender.sendMessage(Messages.getMessage("Reload"));
                    } else if (args.length >= 2) {
                        if (args[1].equalsIgnoreCase("database")) {
                            if (!PluginControl.hasCommandPermission(sender, "Reload.SubCommands.Database", true)) return true;
                            PluginControl.reload(ReloadType.DATABASE);
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                player.closeInventory();
                            }
                            sender.sendMessage(Messages.getMessage("Reload-Database"));
                        } else if (args[1].equalsIgnoreCase("config")) {
                            if (!PluginControl.hasCommandPermission(sender, "Reload.SubCommands.Config", true)) return true;
                            PluginControl.reload(ReloadType.CONFIG);
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                player.closeInventory();
                            }
                            sender.sendMessage(Messages.getMessage("Reload-Config"));
                        } else if (args[1].equalsIgnoreCase("market")) {
                            if (!PluginControl.hasCommandPermission(sender, "Reload.SubCommands.Market", true)) return true;
                            PluginControl.reload(ReloadType.MARKET);
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                player.closeInventory();
                            }
                            sender.sendMessage(Messages.getMessage("Reload-Market"));
                        } else if (args[1].equalsIgnoreCase("messages")) {
                            if (!PluginControl.hasCommandPermission(sender, "Reload.SubCommands.Messages", true)) return true;
                            PluginControl.reload(ReloadType.MESSAGES);
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                player.closeInventory();
                            }
                            sender.sendMessage(Messages.getMessage("Reload-Messages"));
                        } else if (args[1].equalsIgnoreCase("playerdata")) {
                            if (!PluginControl.hasCommandPermission(sender, "Reload.SubCommands.PlayerData", true)) return true;
                            PluginControl.reload(ReloadType.PLAYERDATA);
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                player.closeInventory();
                            }
                            sender.sendMessage(Messages.getMessage("Reload-PlayerData"));
                        } else if (args[1].equalsIgnoreCase("category")) {
                            if (!PluginControl.hasCommandPermission(sender, "Reload.SubCommands.Category", true)) return true;
                            PluginControl.reload(ReloadType.CATEGORY);
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                player.closeInventory();
                            }
                            sender.sendMessage(Messages.getMessage("Reload-Category"));
                        } else if (args[1].equalsIgnoreCase("itemcollection")) {
                            if (!PluginControl.hasCommandPermission(sender, "Reload.SubCommands.ItemCollection", true)) return true;
                            PluginControl.reload(ReloadType.ITEMCOLLECTION);
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                player.closeInventory();
                            }
                            sender.sendMessage(Messages.getMessage("Reload-ItemCollection"));
                        } else {
                            PluginControl.reload(ReloadType.ALL);
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                player.closeInventory();
                            }
                            sender.sendMessage(Messages.getMessage("Reload"));
                        }
                    }
                    return true;
                }                                                    
            }
        }
        sender.sendMessage(Messages.getMessage("CrazyAuctions-Help"));
        return false;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (FileManager.isBackingUp()) return new ArrayList<String>();
        if (args.length == 1) {
            if (args[0].toLowerCase().startsWith("h") && PluginControl.hasCommandPermission(sender, "Help", false)) {
                return Arrays.asList("help");
            } else if (args[0].toLowerCase().startsWith("r") && PluginControl.hasCommandPermission(sender, "Reload", false)) {
                return Arrays.asList("reload");
            } else if (args[0].toLowerCase().startsWith("s") && PluginControl.hasCommandPermission(sender, "Sell", false)) {
                return Arrays.asList("sell");
            } else if (args[0].toLowerCase().startsWith("b")) {
                if (args[0].toLowerCase().startsWith("bi") && PluginControl.hasCommandPermission(sender, "Bid", false)) return Arrays.asList("bid");
                if (args[0].toLowerCase().startsWith("bu") && PluginControl.hasCommandPermission(sender, "Buy", false)) return Arrays.asList("buy");
                List<String> list = new ArrayList<String>();
                if (PluginControl.hasCommandPermission(sender, "Bid", false)) list.add("bid");
                if (PluginControl.hasCommandPermission(sender, "Buy", false)) list.add("buy");
                return list;
            } else if (args[0].toLowerCase().startsWith("l") && PluginControl.hasCommandPermission(sender, "Listed", false)) {
                return Arrays.asList("listed");
            } else if (args[0].toLowerCase().startsWith("m") && PluginControl.hasCommandPermission(sender, "Mail", false)) {
                return Arrays.asList("mail");
            } else if (args[0].toLowerCase().startsWith("v") && PluginControl.hasCommandPermission(sender, "View", false)) {
                return Arrays.asList("view");
            } else if (args[0].toLowerCase().startsWith("g") && PluginControl.hasCommandPermission(sender, "Gui", false)) {
                return Arrays.asList("gui");
            } else if (args[0].toLowerCase().startsWith("a") && PluginControl.hasCommandPermission(sender, "Admin", false)) {
                return Arrays.asList("admin");
            }
            List<String> list = new ArrayList<String>();
            if (PluginControl.hasCommandPermission(sender, "Help", false)) list.add("help");
            if (PluginControl.hasCommandPermission(sender, "Gui", false)) list.add("gui");
            if (PluginControl.hasCommandPermission(sender, "Sell", false)) list.add("sell");
            if (PluginControl.hasCommandPermission(sender, "Buy", false)) list.add("buy");
            if (PluginControl.hasCommandPermission(sender, "Bid", false)) list.add("bid");
            if (PluginControl.hasCommandPermission(sender, "View", false)) list.add("view");
            if (PluginControl.hasCommandPermission(sender, "Listed", false)) list.add("listed");
            if (PluginControl.hasCommandPermission(sender, "Mail", false)) list.add("mail");
            if (PluginControl.hasCommandPermission(sender, "Reload", false)) list.add("reload");
            if (PluginControl.hasCommandPermission(sender, "Admin", false)) list.add("admin");
            return list;
        } else if (args.length >= 2) {
            if (args[0].equalsIgnoreCase("reload") && PluginControl.hasCommandPermission(sender, "Reload", false)) {
                List<String> list = new ArrayList<String>();
                for (String text : new String[]{"all", "database", "config", "messages", "market", "playerdata", "category", "itemcollection"}) {
                    if (text.toLowerCase().startsWith(args[1].toLowerCase())) {
                        list.add(text);
                    }
                }
                return list;
            }
            if (args[0].equalsIgnoreCase("admin") && PluginControl.hasCommandPermission(sender, "Admin", false)) {
                if (args.length >= 3) {
                    if (args[1].equalsIgnoreCase("rollback") && PluginControl.hasCommandPermission(sender, "Admin.SubCommands.RollBack", false)) {
                        List<String> list = new ArrayList<String>();
                        for (String string : PluginControl.getBackupFiles()) {
                            if (string.toLowerCase().startsWith(args[2].toLowerCase())) {
                                list.add(string);
                            }
                        }
                        return list;
                    }
                    if (args[1].equalsIgnoreCase("info") && PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Info", false)) {
                        List<String> list = new ArrayList<String>();
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (p.getName().toLowerCase().startsWith(args[2].toLowerCase())) {
                                list.add(p.getName());
                            }
                        }
                        return list;
                    }
                    if (args[1].equalsIgnoreCase("itemcollection") && PluginControl.hasCommandPermission(sender, "Admin.SubCommands.ItemCollection", false)) {
                        if (args.length == 3) {
                            List<String> list = new ArrayList<String>();
                            for (String text : new String[]{"help", "add", "delete", "give", "list"}) {
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
                for (String text : new String[]{"backup", "rollback", "info", "synchronize", "itemcollection"}) {
                    if (text.toLowerCase().startsWith(args[1].toLowerCase())) {
                        list.add(text);
                    }
                }
                return list;
            }
            if (args[0].equalsIgnoreCase("view") && PluginControl.hasCommandPermission(sender, "View-Others-Player", false)) {
                List<String> players = new ArrayList<String>();
                for (Player ps : Bukkit.getOnlinePlayers()) {
                    if (ps.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        players.add(ps.getName());
                    }
                }
                return players;
            }
            if (args[0].equalsIgnoreCase("buy") && args.length == 4 && PluginControl.hasCommandPermission(sender, "Buy", false)) {
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
            if (args[0].equalsIgnoreCase("gui") && PluginControl.hasCommandPermission(sender, "Gui", false)) { // gui buy 
                if (args.length == 2) {
                    if (sender instanceof Player) {
                        if (args[1].toLowerCase().startsWith("s")) {
                            return Arrays.asList("sell");
                        } else if (args[1].toLowerCase().startsWith("b")) {
                            if (args[1].toLowerCase().startsWith("bu")) return Arrays.asList("buy");
                            if (args[1].toLowerCase().startsWith("bi")) return Arrays.asList("bid");
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
