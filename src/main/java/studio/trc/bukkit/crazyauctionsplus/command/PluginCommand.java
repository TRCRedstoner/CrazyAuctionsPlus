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
                if (args[0].equalsIgnoreCase("Help")) {
                    if (!PluginControl.hasCommandPermission(sender, "Help", true)) return true;
                    for (String message : Messages.getMessageList("Help-Menu")) {
                        sender.sendMessage(message);
                    }
                    return true;
                }
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
                if (args[0].equalsIgnoreCase("Admin")) {
                    if (!PluginControl.hasCommandPermission(sender, "Admin", true)) return true;
                    if (args.length == 1) {
                        for (String message : Messages.getMessageList("Admin-Menu")) {
                            sender.sendMessage(message);
                        }
                        return true;
                    } else if (args.length >= 2) {
                        if (args[1].equalsIgnoreCase("backup")) {
                            if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Backup", true)) return true;
                            if (FileManager.isBackingUp()) {
                                sender.sendMessage(Messages.getMessage("Admin-Command.Backup.BackingUp"));
                                return true;
                            }
                            sender.sendMessage(Messages.getMessage("Admin-Command.Backup.Starting"));
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                player.closeInventory();
                            }
                            FileManager.backup(sender);
                            return true;
                        } else if (args[1].equalsIgnoreCase("rollback")) {
                            if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.RollBack", true)) return true;
                            if (FileManager.isRollingBack()) {
                                sender.sendMessage(Messages.getMessage("Admin-Command.RollBack.RollingBack"));
                                return true;
                            }
                            if (args.length == 2) {
                                sender.sendMessage(Messages.getMessage("Admin-Command.Info.Help"));
                                return true;
                            } else if (args.length >= 3) {
                                File backupFile = new File("plugins/CrazyAuctionsPlus/Backup/" + args[2]);
                                if (backupFile.exists()) {
                                    sender.sendMessage(Messages.getMessage("Admin-Command.RollBack.Starting"));
                                    for (Player player : Bukkit.getOnlinePlayers()) {
                                        player.closeInventory();
                                    }
                                    FileManager.rollBack(backupFile, sender);
                                    return true;
                                } else {
                                    sender.sendMessage(Messages.getMessage("Admin-Command.RollBack.Backup-Not-Exist").replace("%file%", args[2]));
                                    return true;
                                }
                            }
                        } else if (args[1].equalsIgnoreCase("info")) {
                            if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Info", true)) return true;
                            if (args.length == 2) {
                                sender.sendMessage(Messages.getMessage("Admin-Command.Info.Help"));
                                return true;
                            } else if (args.length >= 3) {
                                Player player = Bukkit.getPlayer(args[2]);
                                if (player == null) {
                                    OfflinePlayer offlineplayer = Bukkit.getOfflinePlayer(args[2]);
                                    if (offlineplayer != null) {
                                        int items = 0;
                                        String database;
                                        if (PluginControl.useSplitDatabase()) {
                                            switch (PluginControl.getItemMailStorageMethod()) {
                                                case MySQL: {
                                                    database = "[MySQL] [Database: " + MySQLEngine.getDatabaseName() + "] -> [Table: " + MySQLEngine.getItemMailTable() + "] -> [Colunm: UUID:" + offlineplayer.getUniqueId() + "]";
                                                    break;
                                                }
                                                case SQLite: {
                                                    database = "[SQLite] [" + SQLiteEngine.getFilePath() + SQLiteEngine.getFileName() + "] -> [Table: " + MySQLEngine.getItemMailTable() + "] -> [Colunm: UUID:" + offlineplayer.getUniqueId() + "]";
                                                    break;
                                                }
                                                default: {
                                                    database = new File("plugins/CrazyAuctionsPlus/Players/" + offlineplayer.getUniqueId() + ".yml").getPath();
                                                    break;
                                                }
                                            }
                                        } else if (PluginControl.useMySQLStorage()) {
                                            database = "[MySQL] [Database: " + MySQLEngine.getDatabaseName() + "] -> [Table: " + MySQLEngine.getItemMailTable() + "] -> [Colunm: UUID:" + offlineplayer.getUniqueId() + "]";
                                        } else if (PluginControl.useSQLiteStorage()) {
                                            database = "[SQLite] [" + SQLiteEngine.getFilePath() + SQLiteEngine.getFileName() + "] -> [Table: " + MySQLEngine.getItemMailTable() + "] -> [Colunm: UUID:" + offlineplayer.getUniqueId() + "]";
                                        } else {
                                            database = new File("plugins/CrazyAuctionsPlus/Players/" + offlineplayer.getUniqueId() + ".yml").getPath();
                                        }
                                        for (MarketGoods mg : GlobalMarket.getMarket().getItems()) {
                                            if (mg.getItemOwner().getUUID().equals(offlineplayer.getUniqueId())) {
                                                items++;
                                            }
                                        }
                                        for (String message : Messages.getMessageList("Admin-Command.Info.Info-Messages")) {
                                            sender.sendMessage(message.replace("%player%", offlineplayer.getName()).replace("%group%", Messages.getMessage("Admin-Command.Info.Unknown")).replace("%items%", String.valueOf(items)).replace("%database%", database));
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
                                                database = "[MySQL] [Database: " + MySQLEngine.getDatabaseName() + "] -> [Table: " + MySQLEngine.getItemMailTable() + "] -> [Colunm: UUID:" + player.getUniqueId() + "]";
                                                break;
                                            }
                                            case SQLite: {
                                                database = "[SQLite] [" + SQLiteEngine.getFilePath() + SQLiteEngine.getFileName() + "] -> [Table: " + MySQLEngine.getItemMailTable() + "] -> [Colunm: UUID:" + player.getUniqueId() + "]";
                                                break;
                                            }
                                            default: {
                                                database = new File("plugins/CrazyAuctionsPlus/Players/" + player.getUniqueId() + ".yml").getPath();
                                                break;
                                            }
                                        }
                                    } else if (PluginControl.useMySQLStorage()) {
                                        database = "[MySQL] [Database: " + MySQLEngine.getDatabaseName() + "] -> [Table: " + MySQLEngine.getItemMailTable() + "] -> [Colunm: UUID:" + player.getUniqueId() + "]";
                                    } else if (PluginControl.useSQLiteStorage()) {
                                        database = "[SQLite] [" + SQLiteEngine.getFilePath() + SQLiteEngine.getFileName() + "] -> [Table: " + MySQLEngine.getItemMailTable() + "] -> [Colunm: UUID:" + player.getUniqueId() + "]";
                                    } else {
                                        database = new File("plugins/CrazyAuctionsPlus/Players/" + player.getUniqueId() + ".yml").getPath();
                                    }
                                    for (MarketGoods mg : GlobalMarket.getMarket().getItems()) {
                                        if (mg.getItemOwner().getUUID().equals(player.getUniqueId())) {
                                            items++;
                                        }
                                    }
                                    for (String message : Messages.getMessageList("Admin-Command.Info.Info-Messages")) {
                                        sender.sendMessage(message.replace("%player%", player.getName()).replace("%group%", group).replace("%items%", String.valueOf(items)).replace("%database%", database));
                                    }
                                    return true;
                                }
                            }
                        } else if (args[1].equalsIgnoreCase("synchronize")) {
                            if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Synchronize", true)) return true;
                            if (FileManager.isSyncing()) {
                                sender.sendMessage(Messages.getMessage("Admin-Command.Synchronize.Syncing"));
                                return true;
                            }
                            sender.sendMessage(Messages.getMessage("Admin-Command.Synchronize.Starting"));
                            FileManager.synchronize(sender);
                        } else if (args[1].equalsIgnoreCase("itemcollection")) {
                            if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.ItemCollection", true)) return true;
                            if (args.length == 2) {
                                for (String message : Messages.getMessageList("Admin-Command.ItemCollection.Help")) {
                                    sender.sendMessage(message);
                                }
                            } else if (args.length >= 3) {
                                if (args[2].equalsIgnoreCase("add")) {
                                    if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.ItemCollection.SubCommands.Add", true)) return true;
                                    if (args.length <= 3) {
                                        sender.sendMessage(Messages.getMessage("Admin-Command.ItemCollection.Add.Help"));
                                        return true;
                                    } else {
                                        if (sender instanceof Player) {
                                            Player player = (Player) sender;
                                            if (player.getItemInHand() == null) {
                                                sender.sendMessage(Messages.getMessage("Admin-Command.ItemCollection.Add.Doesnt-Have-Item-In-Hand"));
                                                return true;
                                            }
                                            if (ItemCollection.addItem(player.getItemInHand(), args[3])) {
                                                Map<String, String> map = new HashMap<String, String>();
                                                map.put("%item%", args[3]);
                                                sender.sendMessage(Messages.getMessage("Admin-Command.ItemCollection.Add.Successfully", map));
                                            } else {
                                                sender.sendMessage(Messages.getMessage("Admin-Command.ItemCollection.Add.Already-Exist"));
                                            }
                                        } else {
                                            sender.sendMessage(Messages.getMessage("Players-Only"));
                                            return true;
                                        }
                                    }
                                } else if (args[2].equalsIgnoreCase("delete") || args[2].equalsIgnoreCase("remove")) {
                                    if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.ItemCollection.SubCommands.Delete", true)) return true;
                                    if (args.length <= 3) {
                                        sender.sendMessage(Messages.getMessage("Admin-Command.ItemCollection.Delete.Help"));
                                        return true;
                                    } else {
                                        try {
                                            long uid = Long.valueOf(args[3]);
                                            for (ItemCollection ic : ItemCollection.getCollection()) {
                                                if (ic.getUID() == uid) {
                                                    Map<String, String> map = new HashMap<String, String>();
                                                    map.put("%item%", ic.getDisplayName());
                                                    ItemCollection.deleteItem(uid);
                                                    sender.sendMessage(Messages.getMessage("Admin-Command.ItemCollection.Delete.Successfully", map));
                                                    return true;
                                                }
                                            }
                                            Map<String, String> map = new HashMap<String, String>();
                                            map.put("%item%", args[3]);
                                            sender.sendMessage(Messages.getMessage("Admin-Command.ItemCollection.Delete.Item-Not-Exist", map));
                                        } catch (NumberFormatException ex) {
                                            String displayName = args[3];
                                            for (ItemCollection ic : ItemCollection.getCollection()) {
                                                if (ic.getDisplayName().equalsIgnoreCase(displayName)) {
                                                    Map<String, String> map = new HashMap<String, String>();
                                                    map.put("%item%", ic.getDisplayName());
                                                    ItemCollection.deleteItem(displayName);
                                                    sender.sendMessage(Messages.getMessage("Admin-Command.ItemCollection.Delete.Successfully", map));
                                                    return true;
                                                }
                                            }
                                            Map<String, String> map = new HashMap<String, String>();
                                            map.put("%item%", args[3]);
                                            sender.sendMessage(Messages.getMessage("Admin-Command.ItemCollection.Delete.Item-Not-Exist", map));
                                        }
                                    }
                                } else if (args[2].equalsIgnoreCase("list")) {
                                    if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.ItemCollection.SubCommands.List", true)) return true;
                                    if (ItemCollection.getCollection().isEmpty()) {
                                        sender.sendMessage(Messages.getMessage("Admin-Command.ItemCollection.List.Empty-Collection"));
                                        return true;
                                    } else {
                                        String format = Messages.getMessage("Admin-Command.ItemCollection.List.List-Format");
                                        List<String> list = new ArrayList<String>();
                                        for (ItemCollection collection : ItemCollection.getCollection()) {
                                            list.add(format.replace("%uid%", String.valueOf(collection.getUID())).replace("%item%", collection.getDisplayName()));
                                        }
                                        for (String message : Messages.getMessageList("Admin-Command.ItemCollection.List.Messages")) {
                                            sender.sendMessage(message.replace("%list%", list.toString().substring(1, list.toString().length() - 1)));
                                        }
                                    }
                                } else if (args[2].equalsIgnoreCase("give")) {
                                    if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.ItemCollection.SubCommands.Give", true)) return true;
                                    if (args.length == 3) {
                                        sender.sendMessage(Messages.getMessage("Admin-Command.ItemCollection.Give.Help"));
                                        return true;
                                    } else if (args.length == 4) {
                                        if (sender instanceof Player) {
                                            Player player = (Player) sender;
                                            try {
                                                long uid = Long.valueOf(args[3]);
                                                for (ItemCollection ic : ItemCollection.getCollection()) {
                                                    if (ic.getUID() == uid) {
                                                        Map<String, String> map = new HashMap<String, String>();
                                                        map.put("%item%", ic.getDisplayName());
                                                        map.put("%player%", player.getName());
                                                        player.getInventory().addItem(ic.getItem());
                                                        sender.sendMessage(Messages.getMessage("Admin-Command.ItemCollection.Give.Successfully", map));
                                                        return true;
                                                    }
                                                }
                                                Map<String, String> map = new HashMap<String, String>();
                                                map.put("%item%", args[3]);
                                                sender.sendMessage(Messages.getMessage("Admin-Command.ItemCollection.Give.Item-Not-Exist", map));
                                            } catch (NumberFormatException ex) {
                                                String displayName = args[3];
                                                for (ItemCollection ic : ItemCollection.getCollection()) {
                                                    if (ic.getDisplayName().equalsIgnoreCase(displayName)) {
                                                        Map<String, String> map = new HashMap<String, String>();
                                                        map.put("%item%", ic.getDisplayName());
                                                        map.put("%player%", player.getName());
                                                        player.getInventory().addItem(ic.getItem());
                                                        sender.sendMessage(Messages.getMessage("Admin-Command.ItemCollection.Give.Successfully", map));
                                                        return true;
                                                    }
                                                }
                                                Map<String, String> map = new HashMap<String, String>();
                                                map.put("%item%", args[3]);
                                                sender.sendMessage(Messages.getMessage("Admin-Command.ItemCollection.Give.Item-Not-Exist", map));
                                            }
                                        } else {
                                            sender.sendMessage(Messages.getMessage("Admin-Command.ItemCollection.Give.Help"));
                                            return true;
                                        }
                                    } else if (args.length >= 5) {
                                        Player player = Bukkit.getPlayer(args[4]);
                                        if (player == null) {
                                            Map<String, String> map = new HashMap<String, String>();
                                            map.put("%player%", args[4]);
                                            sender.sendMessage(Messages.getMessage("Admin-Command.ItemCollection.Give.Player-Offline", map));
                                            return true;
                                        } else {
                                            try {
                                                long uid = Long.valueOf(args[3]);
                                                for (ItemCollection ic : ItemCollection.getCollection()) {
                                                    if (ic.getUID() == uid) {
                                                        Map<String, String> map = new HashMap<String, String>();
                                                        map.put("%item%", ic.getDisplayName());
                                                        map.put("%player%", player.getName());
                                                        player.getInventory().addItem(ic.getItem());
                                                        sender.sendMessage(Messages.getMessage("Admin-Command.ItemCollection.Give.Successfully", map));
                                                        return true;
                                                    }
                                                }
                                                Map<String, String> map = new HashMap<String, String>();
                                                map.put("%item%", args[3]);
                                                sender.sendMessage(Messages.getMessage("Admin-Command.ItemCollection.Give.Item-Not-Exist", map));
                                            } catch (NumberFormatException ex) {
                                                String displayName = args[3];
                                                for (ItemCollection ic : ItemCollection.getCollection()) {
                                                    if (ic.getDisplayName().equalsIgnoreCase(displayName)) {
                                                        Map<String, String> map = new HashMap<String, String>();
                                                        map.put("%item%", ic.getDisplayName());
                                                        map.put("%player%", player.getName());
                                                        player.getInventory().addItem(ic.getItem());
                                                        sender.sendMessage(Messages.getMessage("Admin-Command.ItemCollection.Give.Successfully", map));
                                                        return true;
                                                    }
                                                }
                                                Map<String, String> map = new HashMap<String, String>();
                                                map.put("%item%", args[3]);
                                                sender.sendMessage(Messages.getMessage("Admin-Command.ItemCollection.Give.Item-Not-Exist", map));
                                            }
                                        }
                                    }
                                } else {
                                    for (String message : Messages.getMessageList("Admin-Command.ItemCollection.Help")) {
                                        sender.sendMessage(message);
                                    }
                                }
                            }
                        } else {
                            for (String message : Messages.getMessageList("Admin-Menu")) {
                                sender.sendMessage(message);
                            }
                        }
                        return true;
                    }
                }               
                if (args[0].equalsIgnoreCase("View")) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(Messages.getMessage("Players-Only"));
                        return true;
                    }
                    Player player = (Player) sender;
                    if (PluginControl.isWorldDisabled(player)) {
                        sender.sendMessage(Messages.getMessage("World-Disabled"));
                        return true;
                    }
                    if (args.length == 1) {
                        if (!PluginControl.hasCommandPermission(sender, "View", true)) return true;
                        GUIAction.openViewer(player, player.getUniqueId(), 0);
                        return true;
                    }
                    if (args.length >= 2) {
                        if (!PluginControl.hasCommandPermission(sender, "View-Others-Player", true)) return true;
                        Player target = Bukkit.getPlayer(args[1]);
                        if (target != null) {
                            GUIAction.openViewer(player, target.getUniqueId(), 1);
                            return true;
                        } else {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    GUIAction.openViewer(player, Bukkit.getOfflinePlayer(args[1]).getUniqueId(), 1);
                                }
                            }.runTaskLater(Main.getInstance(), 1);
                            return true;
                        }
                    }
                    sender.sendMessage(Messages.getMessage("CrazyAuctions-View"));
                    return true;
                }                           
                if (args[0].equalsIgnoreCase("Buy")) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(Messages.getMessage("Players-Only"));
                        return true;
                    }
                    if (args.length == 1) {
                        sender.sendMessage(Messages.getMessage("CrazyAuctions-Buy"));
                        return true;
                    }
                    if (args.length >= 2) {
                        Player player = (Player) sender;
                        if (PluginControl.isWorldDisabled(player)) {
                            sender.sendMessage(Messages.getMessage("World-Disabled"));
                            return true;
                        }
                        if (!crazyAuctions.isBuyingEnabled()) {
                            player.sendMessage(Messages.getMessage("Buying-Disable"));
                            return true;
                        }
                        if (!PluginControl.hasCommandPermission(player, "Buy", true)) return true;
                        if (!PluginControl.isNumber(args[1])) {
                            Map<String, String> placeholders = new HashMap<String, String>();
                            placeholders.put("%Arg%", args[1]);
                            placeholders.put("%arg%", args[1]);
                            player.sendMessage(Messages.getMessage("Not-A-Valid-Number", placeholders));
                            return true;
                        }
                        double reward = Double.valueOf(args[1]);
                        double tax = 0;
                        if (!PluginControl.bypassTaxRate(player, ShopType.BUY)) {
                            tax = PluginControl.getTaxRate(player, ShopType.BUY);
                        }
                        if (CurrencyManager.getMoney(player) < reward) { 
                            HashMap<String, String> placeholders = new HashMap<String, String>();
                            placeholders.put("%Money_Needed%", String.valueOf((reward + tax) - CurrencyManager.getMoney(player)));
                            placeholders.put("%money_needed%", String.valueOf((reward + tax) - CurrencyManager.getMoney(player)));
                            player.sendMessage(Messages.getMessage("Need-More-Money", placeholders));
                            return true;
                        }
                        if (reward < FileManager.Files.CONFIG.getFile().getDouble("Settings.Minimum-Buy-Reward")) {
                            Map<String, String> placeholders = new HashMap<String, String>();
                            placeholders.put("%reward%", String.valueOf(FileManager.Files.CONFIG.getFile().getDouble("Settings.Minimum-Buy-Reward")));
                            player.sendMessage(Messages.getMessage("Buy-Reward-To-Low", placeholders));
                            return true;
                        }
                        if (reward > FileManager.Files.CONFIG.getFile().getDouble("Settings.Max-Beginning-Buy-Reward")) {
                            Map<String, String> placeholders = new HashMap<String, String>();
                            placeholders.put("%reward%", String.valueOf(FileManager.Files.CONFIG.getFile().getDouble("Settings.Max-Beginning-Buy-Reward")));
                            player.sendMessage(Messages.getMessage("Buy-Reward-To-High", placeholders));
                            return true;
                        }
                        if (!PluginControl.bypassLimit(player, ShopType.BUY)) {
                            int limit = PluginControl.getLimit(player, ShopType.BUY);
                            if (limit > -1) {
                                if (crazyAuctions.getNumberOfPlayerItems(player, ShopType.BUY) >= limit) {
                                    Map<String, String> placeholders = new HashMap<String, String>();
                                    placeholders.put("%number%", String.valueOf(limit));
                                    player.sendMessage(Messages.getMessage("Max-Buying-Items", placeholders));
                                    return true;
                                }
                            }
                        }
                        int amount = 1;
                        if (args.length >= 3) {
                            if (!PluginControl.isInt(args[2])) {
                                Map<String, String> placeholders = new HashMap<String, String>();
                                placeholders.put("%Arg%", args[1]);
                                placeholders.put("%arg%", args[1]);
                                player.sendMessage(Messages.getMessage("Not-A-Valid-Number", placeholders));
                                return true;
                            } else {
                                amount = Integer.valueOf(args[2]);
                            }
                        }
                        if (amount > 64) {
                            player.sendMessage(Messages.getMessage("Too-Many-Items"));
                            return true;
                        }
                        UUID owner = player.getUniqueId();
                        GlobalMarket market = GlobalMarket.getMarket();
                        ItemStack item;
                        if (args.length >= 4) {
                            try {
                                item = new ItemStack(Material.valueOf(args[3].toUpperCase()), amount);
                            } catch (IllegalArgumentException ex) {
                                Map<String, String> placeholders = new HashMap<String, String>();
                                placeholders.put("%Item%", args[3]);
                                placeholders.put("%item%", args[3]);
                                sender.sendMessage(Messages.getMessage("Unknown-Item", placeholders));
                                return true;
                            }
                        } else if (PluginControl.getItemInHand(player).getType() != Material.AIR) {
                            item = PluginControl.getItemInHand(player).clone();
                        } else {
                            sender.sendMessage(Messages.getMessage("CrazyAuctions-Buy"));
                            return true;
                        }
                        item.setAmount(amount);
                        MarketGoods goods = new MarketGoods(
                            market.makeUID(),
                            ShopType.BUY,
                            new ItemOwner(owner, player.getName()),
                            item,
                            PluginControl.convertToMill(FileManager.Files.CONFIG.getFile().getString("Settings.Buy-Time")),
                            PluginControl.convertToMill(FileManager.Files.CONFIG.getFile().getString("Settings.Full-Expire-Time")),
                            reward
                        );
                        market.addGoods(goods);
                        Bukkit.getPluginManager().callEvent(new AuctionListEvent(player, ShopType.BUY, item, reward, tax));
                        Map<String, String> placeholders = new HashMap<String, String>();
                        placeholders.put("%reward%", String.valueOf(reward));
                        placeholders.put("%tax%", String.valueOf(tax));
                        try {
                            placeholders.put("%Item%", item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : (String) item.getClass().getMethod("getI18NDisplayName").invoke(item));
                            placeholders.put("%item%", item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : (String) item.getClass().getMethod("getI18NDisplayName").invoke(item));
                        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                            placeholders.put("%Item%", item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().toString().toLowerCase().replace("_", " "));
                            placeholders.put("%item%", item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().toString().toLowerCase().replace("_", " "));
                        }
                        player.sendMessage(Messages.getMessage("Added-Item-For-Acquisition", placeholders));
                        CurrencyManager.removeMoney(player, reward + tax);
                    }
                    return true;
                }
                if (args[0].equalsIgnoreCase("Sell") || args[0].equalsIgnoreCase("Bid")) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(Messages.getMessage("Players-Only"));
                        return true;
                    }
                    if (args.length >= 2) {
                        Player player = (Player) sender;
                        if (PluginControl.isWorldDisabled(player)) {
                            sender.sendMessage(Messages.getMessage("World-Disabled"));
                            return true;
                        }
                        ShopType type = ShopType.SELL;
                        if (args[0].equalsIgnoreCase("Sell")) {
                            if (!crazyAuctions.isSellingEnabled()) {
                                player.sendMessage(Messages.getMessage("Selling-Disable"));
                                return true;
                            }
                            if (!PluginControl.hasCommandPermission(player, "Sell", true)) {
                                player.sendMessage(Messages.getMessage("No-Permission"));
                                return true;
                            }
                        } else if (args[0].equalsIgnoreCase("Bid")) {
                            type = ShopType.BID;
                            if (!crazyAuctions.isBiddingEnabled()) {
                                player.sendMessage(Messages.getMessage("Bidding-Disable"));
                                return true;
                            }
                            if (!PluginControl.hasCommandPermission(player, "Bid", true)) {
                                player.sendMessage(Messages.getMessage("No-Permission"));
                                return true;
                            }
                        }
                        ItemStack item = PluginControl.getItemInHand(player);
                        int amount = item.getAmount();
                        if (args.length >= 3) {
                            if (!PluginControl.isInt(args[2])) {
                                Map<String, String> placeholders = new HashMap<String, String>();
                                placeholders.put("%Arg%", args[2]);
                                placeholders.put("%arg%", args[2]);
                                player.sendMessage(Messages.getMessage("Not-A-Valid-Number", placeholders));
                                return true;
                            }
                            amount = Integer.parseInt(args[2]);
                            if (amount <= 0) amount = 1;
                            if (amount > item.getAmount()) amount = item.getAmount();
                        }
                        if (PluginControl.getItemInHand(player).getType() == Material.AIR) {
                            player.sendMessage(Messages.getMessage("Doesnt-Have-Item-In-Hand"));
                            return false;
                        }
                        if (!PluginControl.isNumber(args[1])) {
                            Map<String, String> placeholders = new HashMap<String, String>();
                            placeholders.put("%Arg%", args[1]);
                            placeholders.put("%arg%", args[1]);
                            player.sendMessage(Messages.getMessage("Not-A-Valid-Number", placeholders));
                            return true;
                        }
                        double price = Double.valueOf(args[1]);
                        double tax = 0;
                        if (args[0].equalsIgnoreCase("Sell")) {
                            if (!crazyAuctions.isSellingEnabled()) {
                                player.sendMessage(Messages.getMessage("Selling-Disable"));
                                return true;
                            }
                            if (!PluginControl.hasCommandPermission(player, "Sell", true)) {
                                player.sendMessage(Messages.getMessage("No-Permission"));
                                return true;
                            }
                            if (price < FileManager.Files.CONFIG.getFile().getDouble("Settings.Minimum-Sell-Price")) {
                                Map<String, String> placeholders = new HashMap<String, String>();
                                placeholders.put("%price%", String.valueOf(FileManager.Files.CONFIG.getFile().getDouble("Settings.Minimum-Sell-Price")));
                                player.sendMessage(Messages.getMessage("Sell-Price-To-Low", placeholders));
                                return true;
                            }
                            if (price > FileManager.Files.CONFIG.getFile().getDouble("Settings.Max-Beginning-Sell-Price")) {
                                Map<String, String> placeholders = new HashMap<String, String>();
                                placeholders.put("%price%", String.valueOf(FileManager.Files.CONFIG.getFile().getDouble("Settings.Max-Beginning-Sell-Price")));
                                player.sendMessage(Messages.getMessage("Sell-Price-To-High", placeholders));
                                return true;
                            }
                            if (!PluginControl.bypassLimit(player, ShopType.SELL)) {
                                int limit = PluginControl.getLimit(player, ShopType.SELL);
                                if (limit > -1) {
                                    if (crazyAuctions.getNumberOfPlayerItems(player, ShopType.SELL) >= limit) {
                                        Map<String, String> placeholders = new HashMap<String, String>();
                                        placeholders.put("%number%", String.valueOf(limit));
                                        player.sendMessage(Messages.getMessage("Max-Selling-Items", placeholders));
                                        return true;
                                    }
                                }
                            }
                            if (!PluginControl.bypassTaxRate(player, ShopType.SELL)) {
                                tax = price * PluginControl.getTaxRate(player, ShopType.SELL);
                                if (CurrencyManager.getMoney(player) < tax) { 
                                    HashMap<String, String> placeholders = new HashMap<String, String>();
                                    placeholders.put("%Money_Needed%", String.valueOf(tax - CurrencyManager.getMoney(player)));
                                    placeholders.put("%money_needed%", String.valueOf(tax - CurrencyManager.getMoney(player)));
                                    player.sendMessage(Messages.getMessage("Need-More-Money", placeholders));
                                    return true;
                                }
                            }
                        }
                        if (args[0].equalsIgnoreCase("Bid")) {
                            if (price < FileManager.Files.CONFIG.getFile().getDouble("Settings.Minimum-Bid-Price")) {
                                Map<String, String> placeholders = new HashMap<String, String>();
                                placeholders.put("%price%", String.valueOf(FileManager.Files.CONFIG.getFile().getDouble("Settings.Minimum-Bid-Price")));
                                player.sendMessage(Messages.getMessage("Bid-Price-To-Low", placeholders));
                                return true;
                            }
                            if (price > FileManager.Files.CONFIG.getFile().getDouble("Settings.Max-Beginning-Bid-Price")) {
                                Map<String, String> placeholders = new HashMap<String, String>();
                                placeholders.put("%price%", String.valueOf(FileManager.Files.CONFIG.getFile().getDouble("Settings.Max-Beginning-Bid-Price")));
                                player.sendMessage(Messages.getMessage("Bid-Price-To-High", placeholders));
                                return true;
                            }
                            if (!PluginControl.bypassLimit(player, ShopType.BID)) {
                                int limit = PluginControl.getLimit(player, ShopType.BID);
                                if (limit > -1) {
                                    if (crazyAuctions.getNumberOfPlayerItems(player, ShopType.BID) >= limit) {
                                        Map<String, String> placeholders = new HashMap<String, String>();
                                        placeholders.put("%number%", String.valueOf(limit));
                                        player.sendMessage(Messages.getMessage("Max-Bidding-Items", placeholders));
                                        return true;
                                    }
                                }
                            }
                            if (!PluginControl.bypassTaxRate(player, ShopType.BID)) {
                                tax = price * PluginControl.getTaxRate(player, ShopType.BID);
                                if (CurrencyManager.getMoney(player) < tax) { 
                                    HashMap<String, String> placeholders = new HashMap<String, String>();
                                    placeholders.put("%Money_Needed%", String.valueOf(tax - CurrencyManager.getMoney(player)));
                                    placeholders.put("%money_needed%", String.valueOf(tax - CurrencyManager.getMoney(player)));
                                    player.sendMessage(Messages.getMessage("Need-More-Money", placeholders));
                                    return true;
                                }
                            }
                        }
                        for (String id : FileManager.Files.CONFIG.getFile().getStringList("Settings.BlackList")) {
                            if (item.getType() == PluginControl.makeItem(id, 1).getType()) {
                                player.sendMessage(Messages.getMessage("Item-BlackListed"));
                                return true;
                            }
                        }
                        if (!FileManager.Files.CONFIG.getFile().getBoolean("Settings.Allow-Damaged-Items")) {
                            for (Material i : getDamageableItems()) {
                                if (item.getType() == i) {
                                    if (item.getDurability() > 0) {
                                        player.sendMessage(Messages.getMessage("Item-Damaged"));
                                        return true;
                                    }
                                }
                            }
                        }
                        UUID owner = player.getUniqueId();
                        ItemStack is = item.clone();
                        is.setAmount(amount);
                        GlobalMarket market = GlobalMarket.getMarket();
                        MarketGoods goods = new MarketGoods(
                            market.makeUID(),
                            type,
                            new ItemOwner(owner, player.getName()),
                            is,
                            type.equals(ShopType.BID) ? PluginControl.convertToMill(FileManager.Files.CONFIG.getFile().getString("Settings.Bid-Time")) : PluginControl.convertToMill(FileManager.Files.CONFIG.getFile().getString("Settings.Sell-Time")),
                            PluginControl.convertToMill(FileManager.Files.CONFIG.getFile().getString("Settings.Full-Expire-Time")),
                            price,
                            "None"
                        );
                        market.addGoods(goods);
                        Bukkit.getPluginManager().callEvent(new AuctionListEvent(player, type, is, price, tax));
                        CurrencyManager.removeMoney(player, tax);
                        Map<String, String> placeholders = new HashMap<String, String>();
                        placeholders.put("%Price%", String.valueOf(price));
                        placeholders.put("%price%", String.valueOf(price));
                        if (type.equals(ShopType.BID)) {
                            placeholders.put("%tax%", String.valueOf(tax));
                            player.sendMessage(Messages.getMessage("Added-Item-For-Bid", placeholders));
                        } else {
                            placeholders.put("%tax%", String.valueOf(tax));
                            player.sendMessage(Messages.getMessage("Added-Item-For-Sale", placeholders));
                        }
                        if (item.getAmount() <= 1 || (item.getAmount() - amount) <= 0) {
                            PluginControl.setItemInHand(player, new ItemStack(Material.AIR));
                        } else {
                            item.setAmount(item.getAmount() - amount);
                        }
                        return true;
                    }
                    if (args[0].equalsIgnoreCase("Sell")) {
                        sender.sendMessage(Messages.getMessage("CrazyAuctions-Sell"));
                    } else if (args[0].equalsIgnoreCase("Bid")) {
                        sender.sendMessage(Messages.getMessage("CrazyAuctions-Bid"));
                    }
                    return true;
                }
            }
        }
        sender.sendMessage(Messages.getMessage("CrazyAuctions-Help"));
        return false;
    }
    
    private ArrayList<Material> getDamageableItems() {
        ArrayList<Material> ma = new ArrayList<Material>();
        if (Version.getCurrentVersion().isNewer(Version.v1_12_R1)) {
            ma.add(Material.matchMaterial("GOLDEN_HELMET"));
            ma.add(Material.matchMaterial("GOLDEN_CHESTPLATE"));
            ma.add(Material.matchMaterial("GOLDEN_LEGGINGS"));
            ma.add(Material.matchMaterial("GOLDEN_BOOTS"));
            ma.add(Material.matchMaterial("WOODEN_SWORD"));
            ma.add(Material.matchMaterial("WOODEN_AXE"));
            ma.add(Material.matchMaterial("WOODEN_PICKAXE"));
            ma.add(Material.matchMaterial("WOODEN_AXE"));
            ma.add(Material.matchMaterial("WOODEN_SHOVEL"));
            ma.add(Material.matchMaterial("STONE_SHOVEL"));
            ma.add(Material.matchMaterial("IRON_SHOVEL"));
            ma.add(Material.matchMaterial("DIAMOND_SHOVEL"));
            ma.add(Material.matchMaterial("WOODEN_HOE"));
            ma.add(Material.matchMaterial("GOLDEN_HOE"));
            ma.add(Material.matchMaterial("CROSSBOW"));
            ma.add(Material.matchMaterial("TRIDENT"));
            ma.add(Material.matchMaterial("TURTLE_HELMET"));
        } else {
            ma.add(Material.matchMaterial("GOLD_HELMET"));
            ma.add(Material.matchMaterial("GOLD_CHESTPLATE"));
            ma.add(Material.matchMaterial("GOLD_LEGGINGS"));
            ma.add(Material.matchMaterial("GOLD_BOOTS"));
            ma.add(Material.matchMaterial("WOOD_SWORD"));
            ma.add(Material.matchMaterial("WOOD_AXE"));
            ma.add(Material.matchMaterial("WOOD_PICKAXE"));
            ma.add(Material.matchMaterial("WOOD_AXE"));
            ma.add(Material.matchMaterial("WOOD_SPADE"));
            ma.add(Material.matchMaterial("STONE_SPADE"));
            ma.add(Material.matchMaterial("IRON_SPADE"));
            ma.add(Material.matchMaterial("DIAMOND_SPADE"));
            ma.add(Material.matchMaterial("WOOD_HOE"));
            ma.add(Material.matchMaterial("GOLD_HOE"));
        }
        ma.add(Material.DIAMOND_HELMET);
        ma.add(Material.DIAMOND_CHESTPLATE);
        ma.add(Material.DIAMOND_LEGGINGS);
        ma.add(Material.DIAMOND_BOOTS);
        ma.add(Material.CHAINMAIL_HELMET);
        ma.add(Material.CHAINMAIL_CHESTPLATE);
        ma.add(Material.CHAINMAIL_LEGGINGS);
        ma.add(Material.CHAINMAIL_BOOTS);
        ma.add(Material.IRON_HELMET);
        ma.add(Material.IRON_CHESTPLATE);
        ma.add(Material.IRON_LEGGINGS);
        ma.add(Material.IRON_BOOTS);
        ma.add(Material.LEATHER_HELMET);
        ma.add(Material.LEATHER_CHESTPLATE);
        ma.add(Material.LEATHER_LEGGINGS);
        ma.add(Material.LEATHER_BOOTS);
        ma.add(Material.BOW);
        ma.add(Material.STONE_SWORD);
        ma.add(Material.IRON_SWORD);
        ma.add(Material.DIAMOND_SWORD);
        ma.add(Material.STONE_AXE);
        ma.add(Material.IRON_AXE);
        ma.add(Material.DIAMOND_AXE);
        ma.add(Material.STONE_PICKAXE);
        ma.add(Material.IRON_PICKAXE);
        ma.add(Material.DIAMOND_PICKAXE);
        ma.add(Material.STONE_AXE);
        ma.add(Material.IRON_AXE);
        ma.add(Material.DIAMOND_AXE);
        ma.add(Material.STONE_HOE);
        ma.add(Material.IRON_HOE);
        ma.add(Material.DIAMOND_HOE);
        ma.add(Material.FLINT_AND_STEEL);
        ma.add(Material.ANVIL);
        ma.add(Material.FISHING_ROD);
        return ma;
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
