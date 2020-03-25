package studio.trc.bukkit.crazyauctionsplus.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import studio.trc.bukkit.crazyauctionsplus.Main;
import studio.trc.bukkit.crazyauctionsplus.api.events.AuctionExpireEvent;
import studio.trc.bukkit.crazyauctionsplus.api.events.AuctionWinBidEvent;
import studio.trc.bukkit.crazyauctionsplus.currency.CurrencyManager;
import studio.trc.bukkit.crazyauctionsplus.database.DatabaseEngine;
import studio.trc.bukkit.crazyauctionsplus.database.GlobalMarket;
import studio.trc.bukkit.crazyauctionsplus.database.Storage;
import studio.trc.bukkit.crazyauctionsplus.database.StorageMethod;
import studio.trc.bukkit.crazyauctionsplus.database.engine.MySQLEngine;
import studio.trc.bukkit.crazyauctionsplus.database.engine.SQLiteEngine;
import studio.trc.bukkit.crazyauctionsplus.database.market.MySQLMarket;
import studio.trc.bukkit.crazyauctionsplus.database.market.SQLiteMarket;
import studio.trc.bukkit.crazyauctionsplus.database.market.YamlMarket;
import studio.trc.bukkit.crazyauctionsplus.database.storage.MySQLStorage;
import studio.trc.bukkit.crazyauctionsplus.database.storage.SQLiteStorage;
import studio.trc.bukkit.crazyauctionsplus.database.storage.YamlStorage;
import studio.trc.bukkit.crazyauctionsplus.utils.AuctionProcess.AuctionUpdateThread;
import studio.trc.bukkit.crazyauctionsplus.utils.enums.Version;
import studio.trc.bukkit.crazyauctionsplus.utils.enums.ShopType;
import studio.trc.bukkit.crazyauctionsplus.utils.FileManager.*;
import studio.trc.bukkit.crazyauctionsplus.utils.enums.Messages;

@SuppressWarnings("deprecation")
public class PluginControl {
    
    public static String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
    
    public static String getPrefix() {
        return Files.CONFIG.getFile().getString("Settings.Prefix");
    }
    
    @Deprecated
    public static String getPrefix(String msg) {
        return color(Files.CONFIG.getFile().getString("Settings.Prefix") + msg);
    }
    
    @Deprecated
    public static String removeColor(String msg) {
        return ChatColor.stripColor(msg);
    }
    
    public static ItemStack makeItem(String type, int amount) {
        int ty = 0;
        if (type.contains(":")) {
            String[] b = type.split(":");
            type = b[0];
            ty = Integer.parseInt(b[1]);
        }
        Material m = Material.matchMaterial(type);
        ItemStack item;
        try {
            item = new ItemStack(m, amount, (short) ty);
        } catch (Exception e) {
            if (Version.getCurrentVersion().isNewer(Version.v1_12_R1)) {
                item = new ItemStack(Material.matchMaterial("RED_TERRACOTTA"), 1);
                
            } else {
                item = new ItemStack(Material.matchMaterial("STAINED_CLAY"), 1, (short) 14);
            }
        }
        return item;
    }
    
    public static ItemStack makeItem(String type, int amount, String name) {
        int ty = 0;
        if (type.contains(":")) {
            String[] b = type.split(":");
            type = b[0];
            ty = Integer.parseInt(b[1]);
        }
        Material m = Material.matchMaterial(type);
        ItemStack item;
        try {
            item = new ItemStack(m, amount, (short) ty);
        } catch (Exception e) {
            if (Version.getCurrentVersion().isNewer(Version.v1_12_R1)) {
                item = new ItemStack(Material.matchMaterial("RED_TERRACOTTA"), 1);
            } else {
                item = new ItemStack(Material.matchMaterial("STAINED_CLAY"), 1, (short) 14);
            }
        }
        ItemMeta me = item.getItemMeta();
        me.setDisplayName(color(name));
        item.setItemMeta(me);
        return item;
    }
    
    public static ItemStack makeItem(String type, int amount, String name, List<String> lore) {
        ArrayList<String> l = new ArrayList<String>();
        int ty = 0;
        if (type.contains(":")) {
            String[] b = type.split(":");
            type = b[0];
            ty = Integer.parseInt(b[1]);
        }
        Material m = Material.matchMaterial(type);
        ItemStack item;
        try {
            item = new ItemStack(m, amount, (short) ty);
        } catch (Exception e) {
            if (Version.getCurrentVersion().isNewer(Version.v1_12_R1)) {
                item = new ItemStack(Material.matchMaterial("RED_TERRACOTTA"), 1);
                
            } else {
                item = new ItemStack(Material.matchMaterial("STAINED_CLAY"), 1, (short) 14);
            }
        }
        ItemMeta me = item.getItemMeta();
        me.setDisplayName(color(name));
        for (String L : lore)
            l.add(color(L));
        me.setLore(l);
        item.setItemMeta(me);
        return item;
    }
    
    public static ItemStack makeItem(Material material, int amount, int type, String name) {
        ItemStack item = new ItemStack(material, amount, (short) type);
        ItemMeta m = item.getItemMeta();
        m.setDisplayName(color(name));
        item.setItemMeta(m);
        return item;
    }
    
    public static ItemStack makeItem(Material material, int amount, int type, String name, List<String> lore) {
        ArrayList<String> l = new ArrayList<String>();
        ItemStack item = new ItemStack(material, amount, (short) type);
        ItemMeta m = item.getItemMeta();
        m.setDisplayName(color(name));
        for (String L : lore)
            l.add(color(L));
        m.setLore(l);
        item.setItemMeta(m);
        return item;
    }
    
    public static ItemStack makeItem(Material material, int amount, int type, String name, List<String> lore, Map<Enchantment, Integer> enchants) {
        ItemStack item = new ItemStack(material, amount, (short) type);
        ItemMeta m = item.getItemMeta();
        m.setDisplayName(name);
        m.setLore(lore);
        item.setItemMeta(m);
        item.addUnsafeEnchantments(enchants);
        return item;
    }
    
    public static ItemStack addLore(ItemStack item, String i) {
        List<String> lore = new ArrayList<String>();
        ItemMeta m = item.getItemMeta();
        if (m == null) return item;
        if (item.getItemMeta().hasLore()) {
            lore.addAll(item.getItemMeta().getLore());
        }
        lore.add(i);
        m.setLore(lore);
        item.setItemMeta(m);
        return item;
    }
    
    public static ItemStack addLore(ItemStack item, List<String> list) {
        List<String> lore = new ArrayList<String>();
        ItemMeta m = item.getItemMeta();
        if (m == null) {
            return item;
        }
        if (item.getItemMeta().hasLore()) {
            lore.addAll(item.getItemMeta().getLore());
        }
        for (String i : list)
            lore.add(color(i));
        m.setLore(lore);
        item.setItemMeta(m);
        return item;
    }
    
    public static Integer getVersion() {
        String ver = Bukkit.getServer().getClass().getPackage().getName();
        ver = ver.substring(ver.lastIndexOf('.') + 1);
        ver = ver.replace("_", "").replace("R", "").replace("v", "");
        return Integer.parseInt(ver);
    }
    
    @Deprecated
    public static ItemStack getItemInHand(Player player) {
        if (getVersion() >= 191) {
            return player.getInventory().getItemInMainHand();
        } else {
            return player.getItemInHand();
        }
    }
    
    @Deprecated
    public static void setItemInHand(Player player, ItemStack item) {
        if (getVersion() >= 191) {
            player.getInventory().setItemInMainHand(item);
        } else {
            player.setItemInHand(item);
        }
    }
    
    public static boolean isNumber(String value) {
        try {
            Double.valueOf(value);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
    
    @Deprecated
    public static boolean isInt(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
    
    public static Player getPlayer(String name) {
        try {
            return Bukkit.getServer().getPlayer(name);
        } catch (Exception e) {
            return null;
        }
    }
    
    public static Player getPlayer(UUID uuid) {
        try {
            return Bukkit.getServer().getPlayer(uuid);
        } catch (Exception e) {
            return null;
        }
    }
    
    @Deprecated
    public static OfflinePlayer getOfflinePlayer(String name) {
        return Bukkit.getServer().getOfflinePlayer(name);
    }
    
    public static OfflinePlayer getOfflinePlayer(UUID uuid) {
        return Bukkit.getOfflinePlayer(uuid);
    }
    
    public static Location getLoc(Player player) {
        return player.getLocation();
    }
    
    public static void runCMD(Player player, String CMD) {
        player.performCommand(CMD);
    }
    
    @Deprecated
    public static boolean isOnline(String name) {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (player.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean isOnline(UUID uuid) {
        return Bukkit.getPlayer(uuid) != null;
    }
    
    public static boolean isOnline(String name, CommandSender p) {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (player.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        p.sendMessage(Messages.getMessage("Not-Online"));
        return false;
    }
    
    public static boolean hasCommandPermission(Player player, String perm, boolean message) {
        if (Files.CONFIG.getFile().getBoolean("Settings.Permissions.Commands." + perm + ".Default")) return true;
        if (!player.hasPermission(Files.CONFIG.getFile().getString("Settings.Permissions.Commands." + perm + ".Permission"))) {
            if (message) player.sendMessage(Messages.getMessage("No-Permission"));
            return false;
        }
        return true;
    }
    
    public static boolean hasCommandPermission(CommandSender sender, String perm, boolean message) {
        if (Files.CONFIG.getFile().getBoolean("Settings.Permissions.Commands." + perm + ".Default")) return true;
        if (!sender.hasPermission(Files.CONFIG.getFile().getString("Settings.Permissions.Commands." + perm + ".Permission"))) {
            if (message) sender.sendMessage(Messages.getMessage("No-Permission"));
            return false;
        }
        return true;
    }
    
    public static boolean bypassLimit(Player player, ShopType type) {
        ProtectedConfiguration config = Files.CONFIG.getFile();
        switch (type) {
            case SELL: {
                if (config.getBoolean("Settings.Permissions.Market.Sell-Bypass.Default")) return true;
                return player.hasPermission(config.getString("Settings.Permissions.Market.Sell-Bypass.Permission"));
            }
            case BUY: {
                if (config.getBoolean("Settings.Permissions.Market.Buy-Bypass.Default")) return true;
                return player.hasPermission(config.getString("Settings.Permissions.Market.Buy-Bypass.Permission"));
            }
            case BID: {
                if (config.getBoolean("Settings.Permissions.Market.Bid-Bypass.Default")) return true;
                return player.hasPermission(config.getString("Settings.Permissions.Market.Bid-Bypass.Permission"));
            }
            default: {
                return false;
            }
        }
    }
    
    public static boolean bypassTaxRate(Player player, ShopType type) {
        ProtectedConfiguration config = Files.CONFIG.getFile();
        switch (type) {
            case SELL: {
                if (config.getBoolean("Settings.Permissions.Market.Sell-Tax-Rate-Bypass.Default")) return true;
                return player.hasPermission(config.getString("Settings.Permissions.Market.Sell-Tax-Rate-Bypass.Permission"));
            }
            case BUY: {
                if (config.getBoolean("Settings.Permissions.Market.Buy-Tax-Rate-Bypass.Default")) return true;
                return player.hasPermission(config.getString("Settings.Permissions.Market.Buy-Tax-Rate-Bypass.Permission"));
            }
            case BID: {
                if (config.getBoolean("Settings.Permissions.Market.Bid-Tax-Rate-Bypass.Default")) return true;
                return player.hasPermission(config.getString("Settings.Permissions.Market.Bid-Tax-Rate-Bypass.Permission"));
            }
            default: {
                return false;
            }
        }
    }
    
    public static boolean hasMarketPermission(Player player, String perm) {
        ProtectedConfiguration config = Files.CONFIG.getFile();
        if (config.getBoolean("Settings.Permissions.Market." + perm + ".Default")) return true;
        return player.hasPermission(config.getString("Settings.Permissions.Market." + perm + ".Permission"));
    }
    
    public static int getLimit(Player player, ShopType type) {
        switch (type) {
            case SELL: {
                return getMarketGroup(player).getSellLimit();
            }
            case BUY: {
                return getMarketGroup(player).getBuyLimit();
            }
            case BID: {
                return getMarketGroup(player).getBidLimit();
            }
            default: {
                return 0;
            }
        }
    }
    
    public static double getTaxRate(Player player, ShopType type) {
        switch (type) {
            case SELL: {
                return getMarketGroup(player).getSellTaxRate();
            }
            case BUY: {
                return getMarketGroup(player).getBuyTaxRate();
            }
            case BID: {
                return getMarketGroup(player).getBidTaxRate();
            }
            default: {
                return 0;
            }
        }
    }
    
    public static MarketGroup getMarketGroup(Player player) {
        ProtectedConfiguration config = Files.CONFIG.getFile();
        for (String groups : config.getConfigurationSection("Settings.Permissions.Market.Permission-Groups").getKeys(false)) {
            if (config.getBoolean("Settings.Permissions.Market.Permission-Groups." + groups + ".Default")) return new MarketGroup(groups);
            if (player.hasPermission(config.getString("Settings.Permissions.Market.Permission-Groups." + groups + ".Permission"))) {
                return new MarketGroup(groups);
            }
        }
        return null;
    }
    
    public static List<ItemStack> getPage(List<ItemStack> list, Integer page) {
        List<ItemStack> items = new ArrayList<ItemStack>();
        if (page <= 0) page = 1;
        int max = 45;
        int index = page * max - max;
        int endIndex = index >= list.size() ? list.size() - 1 : index + max;
        for (; index < endIndex; index++) {
            if (index < list.size()) items.add(list.get(index));
        }
        for (; items.isEmpty(); page--) {
            if (page <= 0) break;
            index = page * max - max;
            endIndex = index >= list.size() ? list.size() - 1 : index + max;
            for (; index < endIndex; index++) {
                if (index < list.size()) items.add(list.get(index));
            }
        }
        return items;
    }
    
    public static List<Long> getMarketPageUIDs(List<Long> list, Integer page) {
        List<Long> items = new ArrayList<Long>();
        if (page <= 0) page = 1;
        int max = 45;
        int index = page * max - max;
        int endIndex = index >= list.size() ? list.size() - 1 : index + max;
        for (; index < endIndex; index++) {
            if (index < list.size()) items.add(list.get(index));
        }
        for (; items.isEmpty(); page--) {
            if (page <= 0) break;
            index = page * max - max;
            endIndex = index >= list.size() ? list.size() - 1 : index + max;
            for (; index < endIndex; index++) {
                if (index < list.size()) items.add(list.get(index));
            }
        }
        return items;
    }
    
    public static List<Long> getMailPageUIDs(List<Long> list, Integer page) {
        List<Long> items = new ArrayList<Long>();
        if (page <= 0) page = 1;
        int max = 45;
        int index = page * max - max;
        int endIndex = index >= list.size() ? list.size() - 1 : index + max;
        for (; index < endIndex; index++) {
            if (index < list.size()) items.add(list.get(index));
        }
        for (; items.isEmpty(); page--) {
            if (page <= 0) break;
            index = page * max - max;
            endIndex = index >= list.size() ? list.size() - 1 : index + max;
            for (; index < endIndex; index++) {
                if (index < list.size()) items.add(list.get(index));
            }
        }
        return items;
    }
    
    public static int getMaxPage(List<ItemStack> list) {
        int maxPage = 1;
        int amount = list.size();
        for (; amount > 45; amount -= 45, maxPage++) {}
        return maxPage;
    }
    
    public static String convertToTime(long time, boolean isExpire) {
        if (isExpire) {
            return Messages.getMessage("Date-Settings.Never");
        }
        Calendar C = Calendar.getInstance();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        int total = ((int) (cal.getTimeInMillis() / 1000) - (int) (C.getTimeInMillis() / 1000));
        int D = 0;
        int H = 0;
        int M = 0;
        int S = 0;
        for (; total > 86400; total -= 86400, D++) {}
        for (; total > 3600; total -= 3600, H++) {}
        for (; total > 60; total -= 60, M++) {}
        S += total;
        StringBuilder sb = new StringBuilder();
        if (D > 0) {
            sb.append(D).append(Messages.getMessage("Date-Settings.Day")).append(" ");
        }
        if (H > 0) {
            sb.append(H).append(Messages.getMessage("Date-Settings.Hour")).append(" ");
        }
        if (M > 0) {
            sb.append(M).append(Messages.getMessage("Date-Settings.Minute")).append(" ");
        }
        if (S > 0) {
            sb.append(S).append(Messages.getMessage("Date-Settings.Second"));
        }
        return sb.toString();
    }
    
    public static long convertToMill(String time) {
        Calendar cal = Calendar.getInstance();
        for (String i : time.split(" ")) {
            if (i.contains("D") || i.contains("d")) {
                cal.add(Calendar.DATE, Integer.parseInt(i.replace("D", "").replace("d", "")));
            }
            if (i.contains("H") || i.contains("h")) {
                cal.add(Calendar.HOUR, Integer.parseInt(i.replace("H", "").replace("h", "")));
            }
            if (i.contains("M") || i.contains("m")) {
                cal.add(Calendar.MINUTE, Integer.parseInt(i.replace("M", "").replace("m", "")));
            }
            if (i.contains("S") || i.contains("s")) {
                cal.add(Calendar.SECOND, Integer.parseInt(i.replace("S", "").replace("s", "")));
            }
        }
        return cal.getTimeInMillis();
    }
    
    public static boolean isInvFull(Player player) {
        return player.getInventory().firstEmpty() == -1;
    }
    
    public static boolean itemExists(Player player, ItemStack is) {
        Material material = is.getType();
        if (Files.CONFIG.getFile().getBoolean("Settings.Item-NBT-comparison")) {
            for (ItemStack items : player.getInventory().getContents()) {
                if (items == null) continue;
                if (items.getType().equals(material) && items.getItemMeta().equals(is.getItemMeta()) && items.getAmount() >= is.getAmount()) {
                    return true;
                }
            }
        } else {
            for (ItemStack items : player.getInventory().getContents()) {
                if (items == null) continue;
                if (items.getType().equals(material)) {
                    if (is.getAmount() <= items.getAmount()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    public static boolean isWorldDisabled(Player player) {
        if (player != null) {
            for (String worlds : Files.CONFIG.getFile().getStringList("Settings.Disabled-Worlds")) {
                if (worlds.equalsIgnoreCase(player.getWorld().getName())) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public static void takeItem(Player player, ItemStack item) {
        if (Files.CONFIG.getFile().getBoolean("Settings.Item-NBT-comparison")) {
            for (ItemStack is : player.getInventory().getContents()) {
                if (is != null) {
                    if (item.getType().equals(is.getType()) && item.getItemMeta().equals(is.getItemMeta()) && item.getAmount() <= is.getAmount()) {
                        is.setAmount(is.getAmount() - item.getAmount());
                        break;
                    }
                }
            }
        } else {
            for (ItemStack is : player.getInventory().getContents()) {
                if (is != null) {
                    if (item.getType().equals(is.getType()) && item.getAmount() <= is.getAmount()) {
                        is.setAmount(item.getAmount() - is.getAmount());
                    }
                }
            }
        }
    }
    
    public static void updateCacheData() {
        if (FileManager.isBackingUp()) return;
        if (FileManager.isRollingBack()) return;
        if (FileManager.isSyncing()) return;
        Calendar cal = Calendar.getInstance();
        Calendar expireTime = Calendar.getInstance();
        Calendar fullExpireTime = Calendar.getInstance();
        boolean shouldSave = false;
        GlobalMarket market = GlobalMarket.getMarket();
        if (!market.getItems().isEmpty()) {
            for (MarketGoods mg : new ArrayList<>(market.getItems())) {
                if (mg.getItem() == null) {
                    market.removeGoods(mg);
                    continue;
                }
                expireTime.setTimeInMillis(mg.getTimeTillExpire());
                fullExpireTime.setTimeInMillis(mg.getFullTime());
                if (cal.after(expireTime)) {
                    switch (mg.getShopType()) {
                        case BID: {
                            if (Files.CONFIG.getFile().getBoolean("Settings.Auction-Process-Settings.Countdown-Tips.Enabled")) {
                                continue;
                            }
                            if (!mg.getTopBidder().equalsIgnoreCase("None") && CurrencyManager.getMoney(mg.getItemOwner().getUUID()) >= mg.getPrice()) {
                                UUID owner = mg.getItemOwner().getUUID();
                                UUID winner = UUID.fromString(mg.getTopBidder().split(":")[1]);
                                double price = mg.getPrice();
                                CurrencyManager.addMoney(PluginControl.getOfflinePlayer(owner), price);
                                CurrencyManager.removeMoney(PluginControl.getOfflinePlayer(winner), price);
                                HashMap<String, String> placeholders = new HashMap<String, String>();
                                placeholders.put("%Price%", String.valueOf(mg.getPrice()));
                                placeholders.put("%price%", String.valueOf(mg.getPrice()));
                                placeholders.put("%Player%", PluginControl.getOfflinePlayer(winner).getName());
                                placeholders.put("%player%", PluginControl.getOfflinePlayer(winner).getName());
                                if (PluginControl.isOnline(winner) && PluginControl.getPlayer(winner) != null) {
                                    Player player = PluginControl.getPlayer(winner);
                                    AuctionWinBidEvent event = new AuctionWinBidEvent(player, mg, price);
                                    new BukkitRunnable() {
                                        @Override
                                        public void run() {
                                            Bukkit.getPluginManager().callEvent(event);
                                        }
                                    }.runTask(Main.getInstance());
                                    player.sendMessage(Messages.getMessage("Win-Bidding", placeholders));
                                }
                                if (PluginControl.isOnline(owner) && PluginControl.getPlayer(owner) != null) {
                                    Player player = PluginControl.getPlayer(owner);
                                    player.sendMessage(Messages.getMessage("Someone-Won-Players-Bid", placeholders));
                                }
                                Storage playerdata = Storage.getPlayer(winner);
                                ItemMail im = new ItemMail(playerdata.makeUID(), PluginControl.getOfflinePlayer(winner), mg.getItem(), fullExpireTime.getTimeInMillis(), false);
                                playerdata.addItem(im);
                                market.removeGoods(mg.getUID());
                            } else {
                                Storage playerdata = Storage.getPlayer(mg.getItemOwner().getUUID());
                                ItemMail im = new ItemMail(playerdata.makeUID(), mg.getItemOwner().getUUID(), mg.getItem(), fullExpireTime.getTimeInMillis(), false);
                                playerdata.addItem(im);
                                market.removeGoods(mg.getUID());
                                if (mg.getItemOwner().getPlayer() != null) {
                                    mg.getItemOwner().getPlayer().sendMessage(Messages.getMessage("Item-Has-Expired"));
                                }
                            }
                            break;
                        }
                        case BUY: {
                            UUID owner = mg.getItemOwner().getUUID();
                            Player player = getPlayer(owner);
                            if (player != null) {
                                player.sendMessage(Messages.getMessage("Item-Has-Expired"));
                            }
                            AuctionExpireEvent event = new AuctionExpireEvent(player, mg, ShopType.BUY);
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    Bukkit.getPluginManager().callEvent(event);
                                }
                            }.runTask(Main.getInstance());
                            CurrencyManager.addMoney(Bukkit.getOfflinePlayer(owner), mg.getReward());
                            market.removeGoods(mg.getUID());
                            break;
                        }
                        case SELL: {
                            UUID owner = mg.getItemOwner().getUUID();
                            Player player = getPlayer(owner);
                            if (player != null) {
                                player.sendMessage(Messages.getMessage("Item-Has-Expired"));
                            }
                            AuctionExpireEvent event = new AuctionExpireEvent(player, mg, ShopType.SELL);
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    Bukkit.getPluginManager().callEvent(event);
                                }
                            }.runTask(Main.getInstance());
                            Storage playerdata = Storage.getPlayer(getOfflinePlayer(owner));
                            ItemMail im = new ItemMail(playerdata.makeUID(), getOfflinePlayer(owner), mg.getItem(), fullExpireTime.getTimeInMillis(), false);
                            playerdata.addItem(im);
                            market.removeGoods(mg.getUID());
                            break;
                        }
					default:
						break;
                    }
                    shouldSave = true;
                }
            }
        }
        if (shouldSave) market.saveData();
    }
    
    public static boolean useMySQLStorage() {
        return Files.CONFIG.getFile().getBoolean("Settings.MySQL-Storage.Enabled");
    }
    
    public static boolean useSQLiteStorage() {
        return Files.CONFIG.getFile().getBoolean("Settings.SQLite-Storage.Enabled");
    }
    
    public static boolean useSplitDatabase() {
        return Files.CONFIG.getFile().getBoolean("Settings.Split-Database.Enabled");
    }
    
    public static boolean isGlobalMarketAutomaticUpdate() {
        return Files.CONFIG.getFile().getBoolean("Settings.Global-Market-Automatic-Update.Enabled");
    }
    
    public static boolean automaticBackup() {
        return Files.CONFIG.getFile().getBoolean("Settings.Automatic-Backup");
    }
    
    public static double getGlobalMarketAutomaticUpdateDelay() {
        return Files.CONFIG.getFile().getDouble("Settings.Global-Market-Automatic-Update.Update-Delay");
    }
    
    public static StorageMethod getItemMailStorageMethod() {
        if (!useSplitDatabase()) return StorageMethod.YAML;
        try {
            return StorageMethod.valueOf(Files.CONFIG.getFile().getString("Settings.Split-Database.Item-Mail").toUpperCase().replace("MYSQL", "MySQL").replace("SQLITE", "SQLite"));
        } catch (IllegalArgumentException ex) {
            return StorageMethod.YAML;
        }
    }
    
    public static StorageMethod getMarketStorageMethod() {
        if (!useSplitDatabase()) return StorageMethod.YAML;
        try {
            return StorageMethod.valueOf(Files.CONFIG.getFile().getString("Settings.Split-Database.Market").toUpperCase().replace("MYSQL", "MySQL").replace("SQLITE", "SQLite"));
        } catch (IllegalArgumentException ex) {
            return StorageMethod.YAML;
        }
    }
    
    private static long backupFilesAcquisitionTime = 0;
    private static List<String> backupFiles = new ArrayList<String>();
    
    public static List<String> getBackupFiles() {
        /**
         * Since TabComplete is obtained every time you enter it,
         * in order to prevent a large amount of useless IO performance from being wasted,
         * the default limit is to obtain it every 5 seconds.
         */
        if (System.currentTimeMillis() - backupFilesAcquisitionTime <= 5000) {
            return backupFiles;
        }
        List<String> list = new ArrayList<String>();
        File folder = new File("plugins/CrazyAuctionsPlus/Backup/");
        if (!folder.exists()) return list;
        File[] files = folder.listFiles();
        for (File f : files) {
            list.add(f.getName());
        }
        backupFiles = list;
        backupFilesAcquisitionTime = System.currentTimeMillis();
        return list;
    }
    
	public static boolean reload(ReloadType type) {
        FileManager fm = FileManager.getInstance();
        try {
            switch (type) {
                case ALL: {
                    fm.logInfo(true).setup(Main.getInstance());
                    if (AuctionUpdateThread.thread != null) AuctionUpdateThread.thread.stop();
                    if (PluginControl.useSplitDatabase()) {
                        boolean database_MySQL = false;
                        boolean database_SQLite = false;
                        switch (PluginControl.getItemMailStorageMethod()) {
                            case MySQL: {
                                MySQLStorage.cache.clear();
                                database_MySQL = true;
                                break;
                            }
                            case SQLite: {
                                SQLiteStorage.cache.clear();
                                database_SQLite = true;
                                break;
                            }
                            case YAML: {
                                YamlStorage.cache.clear();
                                break;
                            }
                        }
                        
                        switch (PluginControl.getMarketStorageMethod()) {
                            case MySQL: {
                                database_MySQL = true;
                                break;
                            }
                            case SQLite: {
                                database_SQLite = true;
                                break;
                            }
                            case YAML: {
                                fm.reloadDatabaseFile();
                                break;
                            }
                        }
                        
                        if (database_MySQL) {
                            MySQLEngine.getInstance().reloadConnectionParameters();
                        }
                        
                        if (database_SQLite) {
                            SQLiteEngine.getInstance().reloadConnectionParameters();
                        }
                        
                        GlobalMarket.getMarket().reloadData();
                    } else if (PluginControl.useMySQLStorage()) {
                        DatabaseEngine.getDatabase().reloadConnectionParameters();
                        MySQLStorage.cache.clear();
                        MySQLMarket.getInstance().reloadData();
                    } else if (PluginControl.useSQLiteStorage()) {
                        DatabaseEngine.getDatabase().reloadConnectionParameters();
                        SQLiteStorage.cache.clear();
                        SQLiteMarket.getInstance().reloadData();
                    } else {
                        YamlStorage.cache.clear();
                        YamlMarket.getInstance().reloadData();
                    }
                    if (Files.CONFIG.getFile().getBoolean("Settings.Auction-Process-Settings.Countdown-Tips.Enabled")) {
                        new AuctionUpdateThread(Files.CONFIG.getFile().getDouble("Settings.Auction-Process-Settings.Countdown-Tips.Update-Delay")).start();
                    }
                    return true;
                }
                case CONFIG: {
                    if (AuctionUpdateThread.thread != null) AuctionUpdateThread.thread.stop();
                    fm.reloadConfig();
                    if (Files.CONFIG.getFile().getBoolean("Settings.Auction-Process-Settings.Countdown-Tips.Enabled")) {
                        new AuctionUpdateThread(Files.CONFIG.getFile().getDouble("Settings.Auction-Process-Settings.Countdown-Tips.Update-Delay")).start();
                    }
                    return true;
                }
                case DATABASE: {
                    if (PluginControl.useSplitDatabase()) {
                        boolean database_MySQL = false;
                        boolean database_SQLite = false;
                        switch (PluginControl.getItemMailStorageMethod()) {
                            case MySQL: {
                                MySQLStorage.cache.clear();
                                database_MySQL = true;
                                break;
                            }
                            case SQLite: {
                                SQLiteStorage.cache.clear();
                                database_SQLite = true;
                                break;
                            }
                            case YAML: {
                                YamlStorage.cache.clear();
                                break;
                            }
                        }
                        
                        switch (PluginControl.getMarketStorageMethod()) {
                            case MySQL: {
                                MySQLMarket.getInstance().reloadData();
                                database_MySQL = true;
                                break;
                            }
                            case SQLite: {
                                SQLiteMarket.getInstance().reloadData();
                                database_SQLite = true;
                                break;
                            }
                            case YAML: {
                                fm.reloadDatabaseFile();
                                break;
                            }
                        }
                        
                        if (database_MySQL) {
                            MySQLEngine.getInstance().reloadConnectionParameters();
                        }
                        
                        if (database_SQLite) {
                            SQLiteEngine.getInstance().reloadConnectionParameters();
                        }
                        
                    } else if (PluginControl.useMySQLStorage()) {
                        DatabaseEngine.getDatabase().reloadConnectionParameters();
                        MySQLStorage.cache.clear();
                        MySQLMarket.getInstance().reloadData();
                    } else if (PluginControl.useSQLiteStorage()) {
                        DatabaseEngine.getDatabase().reloadConnectionParameters();
                        SQLiteStorage.cache.clear();
                        SQLiteMarket.getInstance().reloadData();
                    } else {
                        fm.reloadDatabaseFile();
                        YamlStorage.cache.clear();
                    }
                    return true;
                }
                case MARKET: {
                    if (PluginControl.useMySQLStorage()) {
                        MySQLMarket.getInstance().reloadData();
                    } else if (PluginControl.useSQLiteStorage()) {
                        SQLiteMarket.getInstance().reloadData();
                    } else {
                        fm.reloadDatabaseFile();
                    }
                    return true;
                }
                case MESSAGES: {
                    fm.reloadMessages();
                    return true;
                }
                case PLAYERDATA: {
                    if (PluginControl.useMySQLStorage()) {
                        MySQLStorage.cache.clear();
                    } else if (PluginControl.useSQLiteStorage()) {
                        SQLiteStorage.cache.clear();
                    } else {
                        YamlStorage.cache.clear();
                    }
                    return true;
                }
                case CATEGORY: {
                    fm.reloadCategoryFile();
                    return true;
                }
                case ITEMCOLLECTION: {
                    fm.reloadItemCollectionFile();
                    return true;
                }
                default: {
                    return false;
                }
            }
        } catch (Exception ex) {
            return false;
        }
    }
    
    public static enum ReloadType {
        
        /**
         * Config.yml
         */
        CONFIG,
        
        /**
         * Data for all players
         */
        PLAYERDATA,
        
        /**
         * Market Commodity Data (Database.yml, or related database data
         */
        MARKET,
        
        /**
         * Messages.yml
         */
        MESSAGES,
        
        /**
         * Category.yml
         */
        CATEGORY,
        
        /**
         * ItemCollection.yml
         */
        ITEMCOLLECTION,
        
        /**
         * Refers to MySQL, SQLite connections, including loaded cached data.
         */
        DATABASE,
        
        /**
         * All settings including database, language, etc.
         */
        ALL
    }
    
    public static class RollBackMethod {
        private final File rollBackFile;
        private final FileManager fm;
        private final CommandSender[] senders;
        
        public RollBackMethod(File rollBackFile, FileManager fm, CommandSender... senders) {
            this.rollBackFile = rollBackFile;
            this.fm = fm;
            this.senders = senders;
        }
        
        public static void backup() throws SQLException, IOException {
            String fileName = Messages.getMessage("Admin-Command.Backup.Backup-Name").replace("%date%", new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date())) + ".db";
            GlobalMarket market = GlobalMarket.getMarket();
            File folder = new File("plugins/CrazyAuctionsPlus/Backup");
            if (!folder.exists()) folder.mkdir();
            File file = new File(folder, fileName);
            if (!file.exists()) {
                file.createNewFile();
            } else {
                file.delete();
                file.createNewFile();
            }
            try (Connection DBFile = DriverManager.getConnection("jdbc:sqlite:plugins/CrazyAuctionsPlus/Backup/" + fileName)) {
                DBFile.prepareStatement("CREATE TABLE IF NOT EXISTS ItemMail" +
                    "("
                    + "UUID VARCHAR(36) NOT NULL PRIMARY KEY,"
                    + "Name VARCHAR(16) NOT NULL,"
                    + "YamlData LONGTEXT" +
                    ");").executeUpdate();
                DBFile.prepareStatement("CREATE TABLE IF NOT EXISTS Market" +
                    "("
                    + "YamlMarket LONGTEXT" +
                    ");").executeUpdate();
                PreparedStatement statement = DBFile.prepareStatement("INSERT INTO Market (YamlMarket) VALUES(?)");
                statement.setString(1, market.getYamlData().saveToString());
                statement.executeUpdate();
                if (PluginControl.useSplitDatabase()) {
                    switch (PluginControl.getItemMailStorageMethod()) {
                        case MySQL: {
                            MySQLEngine.backupPlayerData(DBFile);
                            break;
                        }
                        case SQLite: {
                            SQLiteEngine.backupPlayerData(DBFile);
                            break;
                        }
                        case YAML: {
                            File playerFolder = new File("plugins/CrazyAuctionsPlus/Players/");
                            if (playerFolder.exists()) {
                                File[] files = playerFolder.listFiles();
                                for (File f : files) {
                                    if (f.getName().endsWith(".yml")) {
                                        YamlConfiguration yaml = new YamlConfiguration();
                                        try {
                                            yaml.load(f);
                                        } catch (IOException | InvalidConfigurationException ex) {
                                            continue;
                                        }
                                        PreparedStatement pstatement = DBFile.prepareStatement("INSERT INTO ItemMail (Name, UUID, YamlData) VALUES(?, ?, ?)");
                                        pstatement.setString(1, yaml.get("Name") != null ? yaml.getString("Name") : "null");
                                        pstatement.setString(2, f.getName());
                                        pstatement.setString(3, yaml.get("Items") != null ? yaml.saveToString() : "{}");
                                        pstatement.executeUpdate();
                                    }
                                }
                            }
                            break;
                        }
                        default: {
                            File playerFolder = new File("plugins/CrazyAuctionsPlus/Players/");
                            if (playerFolder.exists()) {
                                File[] files = playerFolder.listFiles();
                                for (File f : files) {
                                    if (f.getName().endsWith(".yml")) {
                                        YamlConfiguration yaml = new YamlConfiguration();
                                        try {
                                            yaml.load(f);
                                        } catch (IOException | InvalidConfigurationException ex) {
                                            continue;
                                        }
                                        PreparedStatement pstatement = DBFile.prepareStatement("INSERT INTO ItemMail (Name, UUID, YamlData) VALUES(?, ?, ?)");
                                        pstatement.setString(1, yaml.get("Name") != null ? yaml.getString("Name") : "null");
                                        pstatement.setString(2, f.getName());
                                        pstatement.setString(3, yaml.get("Items") != null ? yaml.saveToString() : "{}");
                                        pstatement.executeUpdate();
                                    }
                                }
                            }
                            break;
                        }
                    }
                } else if (PluginControl.useMySQLStorage()) {
                    MySQLEngine.backupPlayerData(DBFile);
                } else if (PluginControl.useSQLiteStorage()) {
                    SQLiteEngine.backupPlayerData(DBFile);
                } else {
                    File playerFolder = new File("plugins/CrazyAuctionsPlus/Players/");
                    if (playerFolder.exists()) {
                        File[] files = playerFolder.listFiles();
                        for (File f : files) {
                            if (f.getName().endsWith(".yml")) {
                                YamlConfiguration yaml = new YamlConfiguration();
                                try {
                                    yaml.load(f);
                                } catch (IOException | InvalidConfigurationException ex) {
                                    continue;
                                }
                                PreparedStatement pstatement = DBFile.prepareStatement("INSERT INTO ItemMail (Name, UUID, YamlData) VALUES(?, ?, ?)");
                                pstatement.setString(1, yaml.get("Name") != null ? yaml.getString("Name") : "null");
                                pstatement.setString(2, f.getName());
                                pstatement.setString(3, yaml.get("Items") != null ? yaml.saveToString() : "{}");
                                pstatement.executeUpdate();
                            }
                        }
                    }
                }
            }
        }
        
        /**
         * 
         * Perform the rollback method
         * 
         * @param backup If an error occurs during the rollback process,
         *                there is a chance that data will be lost,
         *                so we recommend that you back up the current data before rolling back.
         */
        public void rollBack(boolean backup) {
            try {
                if (backup) {
                    backup();
                }
                if (rollBackFile.exists()) {
                    try (Connection sqlConnection = DriverManager.getConnection("jdbc:sqlite:plugins/CrazyAuctionsPlus/Backup/" + rollBackFile.getName())) {
                        
                        // Roll Back Market Database.
                        ResultSet marketRS = sqlConnection.prepareStatement("SELECT YamlMarket FROM Market").executeQuery();
                        if (marketRS.next()) {
                            if (PluginControl.useSplitDatabase()) {
                                switch (PluginControl.getMarketStorageMethod()) {
                                    case MySQL: {
                                        DatabaseEngine engine = MySQLEngine.getInstance();
                                        PreparedStatement statement = engine.getConnection().prepareStatement("UPDATE " + MySQLEngine.getDatabaseName() + "." + MySQLEngine.getMarketTable() + " SET " +
                                                "YamlMarket = ?");
                                        statement.setString(1, marketRS.getString("YamlMarket"));
                                        engine.executeQuery(statement);
                                        GlobalMarket.getMarket().reloadData();
                                        break;
                                    }
                                    case SQLite: {
                                        DatabaseEngine engine = SQLiteEngine.getInstance();
                                        PreparedStatement statement = engine.getConnection().prepareStatement("UPDATE " + SQLiteEngine.getMarketTable() + " SET " +
                                                "YamlMarket = ?");
                                        statement.setString(1, marketRS.getString("YamlMarket"));
                                        engine.executeQuery(statement);
                                        GlobalMarket.getMarket().reloadData();
                                        break;
                                    }
                                    case YAML: {
                                        String yamlData = marketRS.getString("YamlMarket");
                                        File databaseFile = new File("plugins/CrazyAuctionsPlus/Database.yml");
                                        if (!databaseFile.exists()) {
                                            databaseFile.createNewFile();
                                        }
                                        try (OutputStream out = new FileOutputStream(databaseFile)) {
                                            out.write(yamlData.getBytes());
                                        }
                                        fm.reloadDatabaseFile();
                                        break;
                                    }
                                    default: {
                                        String yamlData = marketRS.getString("YamlMarket");
                                        File databaseFile = new File("plugins/CrazyAuctionsPlus/Database.yml");
                                        if (!databaseFile.exists()) {
                                            databaseFile.createNewFile();
                                        }
                                        try (OutputStream out = new FileOutputStream(databaseFile)) {
                                            out.write(yamlData.getBytes());
                                        }
                                        fm.reloadDatabaseFile();
                                        break;
                                    }
                                }
                            } else if (PluginControl.useMySQLStorage()) {
                                DatabaseEngine engine = MySQLEngine.getInstance();
                                PreparedStatement statement = engine.getConnection().prepareStatement("UPDATE " + MySQLEngine.getDatabaseName() + "." + MySQLEngine.getMarketTable() + " SET " +
                                        "YamlMarket = ?");
                                statement.setString(1, marketRS.getString("YamlMarket"));
                                engine.executeUpdate(statement);
                                GlobalMarket.getMarket().reloadData();
                            } else if (PluginControl.useSQLiteStorage()) {
                                DatabaseEngine engine = SQLiteEngine.getInstance();
                                PreparedStatement statement = engine.getConnection().prepareStatement("UPDATE " + SQLiteEngine.getMarketTable() + " SET " +
                                        "YamlMarket = ?");
                                statement.setString(1, marketRS.getString("YamlMarket"));
                                engine.executeUpdate(statement);
                                GlobalMarket.getMarket().reloadData();
                            } else {
                                String yamlData = marketRS.getString("YamlMarket");
                                File databaseFile = new File("plugins/CrazyAuctionsPlus/Database.yml");
                                if (!databaseFile.exists()) {
                                    databaseFile.createNewFile();
                                }
                                try (OutputStream out = new FileOutputStream(databaseFile)) {
                                    out.write(yamlData.getBytes());
                                }
                                fm.reloadDatabaseFile();
                            }
                        }
                        
                        // Roll Back Item Mail
                        ResultSet itemMailRS = sqlConnection.prepareStatement("SELECT * FROM ItemMail").executeQuery();
                        if (PluginControl.useSplitDatabase()) {
                            switch (PluginControl.getMarketStorageMethod()) {
                                case MySQL: {
                                    DatabaseEngine engine = MySQLEngine.getInstance();
                                    engine.executeUpdate(engine.getConnection().prepareStatement("DELETE FROM " + MySQLEngine.getDatabaseName()+ "." + MySQLEngine.getItemMailTable()));
                                    while (itemMailRS.next()) {
                                        String uuid = itemMailRS.getString("UUID");
                                        String name = itemMailRS.getString("Name");
                                        String yaml = itemMailRS.getString("YamlData");
                                        PreparedStatement statement = engine.getConnection().prepareStatement("INSERT INTO " + MySQLEngine.getDatabaseName()+ "." + MySQLEngine.getItemMailTable() + " (Name, UUID, YamlData) VALUES(?, ?, ?)");
                                        statement.setString(1, name);
                                        statement.setString(2, uuid);
                                        statement.setString(3, yaml);
                                        engine.executeUpdate(statement);
                                    }
                                    MySQLStorage.cache.clear();
                                    break;
                                }
                                case SQLite: {
                                    DatabaseEngine engine = SQLiteEngine.getInstance();
                                    engine.executeUpdate(engine.getConnection().prepareStatement("DELETE FROM " + SQLiteEngine.getItemMailTable()));
                                    while (itemMailRS.next()) {
                                        String uuid = itemMailRS.getString("UUID");
                                        String name = itemMailRS.getString("Name");
                                        String yaml = itemMailRS.getString("YamlData");
                                        PreparedStatement statement = engine.getConnection().prepareStatement("INSERT INTO " + SQLiteEngine.getItemMailTable() + " (Name, UUID, YamlData) VALUES(?, ?, ?)");
                                        statement.setString(1, name);
                                        statement.setString(2, uuid);
                                        statement.setString(3, yaml);
                                        engine.executeUpdate(statement);
                                    }
                                    SQLiteStorage.cache.clear();
                                    break;
                                }
                                case YAML: {
                                    File path = new File("plugins/CrazyAuctionsPlus/Players/");
                                    if (!path.exists()) {
                                        path.mkdir();
                                    }
                                    while (itemMailRS.next()) {
                                        File dataFile = new File(path, itemMailRS.getString("UUID") + ".yml");
                                        if (dataFile.exists()) {
                                            try (OutputStream out = new FileOutputStream(dataFile)) {
                                                out.write(itemMailRS.getString("YamlData").getBytes());
                                            }
                                        } else {
                                            dataFile.createNewFile();
                                            try (OutputStream out = new FileOutputStream(dataFile)) {
                                                out.write(itemMailRS.getString("YamlData").getBytes());
                                            }
                                        }
                                    }
                                    YamlStorage.cache.clear();
                                    break;
                                }
                                default: {
                                    File path = new File("plugins/CrazyAuctionsPlus/Players/");
                                    if (!path.exists()) {
                                        path.mkdir();
                                    }
                                    while (itemMailRS.next()) {
                                        File dataFile = new File(path, itemMailRS.getString("UUID") + ".yml");
                                        if (dataFile.exists()) {
                                            try (OutputStream out = new FileOutputStream(dataFile)) {
                                                out.write(itemMailRS.getString("YamlData").getBytes());
                                            }
                                        } else {
                                            dataFile.createNewFile();
                                            try (OutputStream out = new FileOutputStream(dataFile)) {
                                                out.write(itemMailRS.getString("YamlData").getBytes());
                                            }
                                        }
                                    }
                                    YamlStorage.cache.clear();
                                    break;
                                }
                            }
                        } else if (PluginControl.useMySQLStorage()) {
                            DatabaseEngine engine = MySQLEngine.getInstance();
                            engine.executeUpdate(engine.getConnection().prepareStatement("DELETE FROM " + MySQLEngine.getDatabaseName()+ "." + MySQLEngine.getItemMailTable()));
                            while (itemMailRS.next()) {
                                String uuid = itemMailRS.getString("UUID");
                                String name = itemMailRS.getString("Name");
                                String yaml = itemMailRS.getString("YamlData");
                                PreparedStatement statement = engine.getConnection().prepareStatement("INSERT INTO " + MySQLEngine.getDatabaseName()+ "." + MySQLEngine.getItemMailTable() + " (Name, UUID, YamlData) VALUES(?, ?, ?)");
                                statement.setString(1, name);
                                statement.setString(2, uuid);
                                statement.setString(3, yaml);
                                engine.executeUpdate(statement);
                            }
                            MySQLStorage.cache.clear();
                        } else if (PluginControl.useSQLiteStorage()) {
                            DatabaseEngine engine = SQLiteEngine.getInstance();
                            engine.executeUpdate(engine.getConnection().prepareStatement("DELETE FROM " + SQLiteEngine.getItemMailTable()));
                            while (itemMailRS.next()) {
                                String uuid = itemMailRS.getString("UUID");
                                String name = itemMailRS.getString("Name");
                                String yaml = itemMailRS.getString("YamlData");
                                PreparedStatement statement = engine.getConnection().prepareStatement("INSERT INTO " + SQLiteEngine.getItemMailTable() + " (Name, UUID, YamlData) VALUES(?, ?, ?)");
                                statement.setString(1, name);
                                statement.setString(2, uuid);
                                statement.setString(3, yaml);
                                engine.executeUpdate(statement);
                            }
                            SQLiteStorage.cache.clear();
                        } else {
                            File path = new File("plugins/CrazyAuctionsPlus/Players/");
                            if (!path.exists()) {
                                path.mkdir();
                            }
                            while (itemMailRS.next()) {
                                File dataFile = new File(path, itemMailRS.getString("UUID") + ".yml");
                                if (dataFile.exists()) {
                                    try (OutputStream out = new FileOutputStream(dataFile)) {
                                        out.write(itemMailRS.getString("YamlData").getBytes());
                                    }
                                } else {
                                    dataFile.createNewFile();
                                    try (OutputStream out = new FileOutputStream(dataFile)) {
                                        out.write(itemMailRS.getString("YamlData").getBytes());
                                    }
                                }
                            }
                            YamlStorage.cache.clear();
                        }
                    }
                }
                for (CommandSender sender : senders) {
                    if (sender != null) {
                        sender.sendMessage(Messages.getMessage("Admin-Command.RollBack.Successfully").replace("%file%", rollBackFile.getName()));
                    }
                }
            } catch (Exception ex) {
                for (CommandSender sender : senders) {
                    sender.sendMessage(Messages.getMessage("Admin-Command.RollBack.Failed").replace("%error%", ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : "null"));
                }
            }
        }
    }
}