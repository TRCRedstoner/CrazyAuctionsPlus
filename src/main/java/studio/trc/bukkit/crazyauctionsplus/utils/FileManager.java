package studio.trc.bukkit.crazyauctionsplus.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import studio.trc.bukkit.crazyauctionsplus.Main;
import studio.trc.bukkit.crazyauctionsplus.database.GlobalMarket;
import studio.trc.bukkit.crazyauctionsplus.database.Storage;
import studio.trc.bukkit.crazyauctionsplus.database.StorageMethod;
import studio.trc.bukkit.crazyauctionsplus.database.engine.MySQLEngine;
import studio.trc.bukkit.crazyauctionsplus.database.engine.SQLiteEngine;
import studio.trc.bukkit.crazyauctionsplus.utils.PluginControl.RollBackMethod;
import studio.trc.bukkit.crazyauctionsplus.utils.enums.Messages;
import studio.trc.bukkit.crazyauctionsplus.utils.enums.ShopType;
import studio.trc.bukkit.crazyauctionsplus.utils.enums.Version;

@SuppressWarnings("deprecation")
public class FileManager {
    
    private static final FileManager instance = new FileManager();
    private static boolean backingup = false;
    private static boolean rollingback = false;
    private static boolean syncing = false;
    private Main main;
    private String prefix = "[CrazyAuctionsPlus] ";
    private Boolean log = false;
    private final HashMap<Files, File> files = new HashMap<Files, File>();
    private final ArrayList<String> homeFolders = new ArrayList<String>();
    private final ArrayList<CustomFile> customFiles = new ArrayList<CustomFile>();
    private final HashMap<String, String> autoGenerateFiles = new HashMap<String, String>();
    private final HashMap<Files, FileConfiguration> configurations = new HashMap<Files, FileConfiguration>();
    
    public static FileManager getInstance() {
        return instance;
    }
    
    private static CommandSender[] syncSenders = {};
    
	public static Runnable synchronizeThread = () -> {
        syncing = true;
        
        // Old Data Files
        File database_File = new File("plugins/CrazyAuctionsPlus/Database.yml");
        File data_File = new File("plugins/CrazyAuctionsPlus/Data.yml");
        File data_File_On_CrazyAuctions = new File("plugins/CrazyAuctions/Data.yml");
        File[] files = {database_File, data_File, data_File_On_CrazyAuctions};
        
        GlobalMarket market = GlobalMarket.getMarket();
        
        try {
            for (File file : files) {
                if (file != null && file.exists()) {
                    YamlConfiguration databaseFile = new YamlConfiguration();
                    try (Reader reader = new InputStreamReader(new FileInputStream(file), "UTF-8")) {
                        databaseFile.load(reader);
                    }
                    if (databaseFile.get("Items") != null) {
                        for (String key : databaseFile.getConfigurationSection("Items").getKeys(false)) {
                            ShopType type = ShopType.SELL;
                            if (databaseFile.getBoolean("Items." + key + ".Biddable")) {
                                type = ShopType.BID;
                            } else if (databaseFile.getBoolean("Items." + key + ".Buyable")) {
                                type = ShopType.BUY;
                            }
                            ItemOwner owner;
                            if (databaseFile.get("Items." + key + ".Owner") != null) {
                                String[] info = databaseFile.getString("Items." + key + ".Owner").split(":");
                                owner = new ItemOwner(UUID.fromString(info[1]), info[0]);
                            } else if (databaseFile.get("Items." + key + ".Seller") != null) {
                                OfflinePlayer op = Bukkit.getOfflinePlayer(databaseFile.getString("Items." + key + ".Seller"));
                                if (op == null) continue;
                                owner = new ItemOwner(op.getUniqueId(), op.getName());
                            } else {
                                continue;
                            }
                            ItemStack is;
                            if (databaseFile.get("Items." + key + ".Item") != null) {
                                is = databaseFile.getItemStack("Items." + key + ".Item");
                            } else {
                                continue;
                            }
                            double money = databaseFile.getDouble("Items." + key + ".Price");
                            if (type.equals(ShopType.BUY)) {
                                money = databaseFile.getDouble("Items." + key + ".Reward");
                            }
                            MarketGoods mg;
                            if (type.equals(ShopType.BID)) {
                                String topBidder = databaseFile.get("Items." + key + ".TopBidder") != null ? databaseFile.getString("Items." + key + ".TopBidder") : "None";
                                mg = new MarketGoods(
                                    market.makeUID(),
                                    type,
                                    owner,
                                    is,
                                    databaseFile.getLong("Items." + key + ".Time-Till-Expire"),
                                    databaseFile.getLong("Items." + key + ".Full-Time"),
                                    money,
                                    topBidder
                                );
                            } else {
                                mg = new MarketGoods(
                                    market.makeUID(),
                                    type,
                                    owner,
                                    is,
                                    databaseFile.getLong("Items." + key + ".Time-Till-Expire"),
                                    databaseFile.getLong("Items." + key + ".Full-Time"),
                                    money
                                );
                            }
                            market.addGoods(mg);
                        }
                    }

                    if (databaseFile.get("OutOfTime/Cancelled") != null) {
                        for (String key : databaseFile.getConfigurationSection("OutOfTime/Cancelled").getKeys(false)) {
                            if (databaseFile.get("OutOfTime/Cancelled." + key + ".Item") != null) {
                                if (databaseFile.get("OutOfTime/Cancelled." + key + ".Owner") != null) {
                                    OfflinePlayer op = Bukkit.getOfflinePlayer(UUID.fromString(databaseFile.getString("OutOfTime/Cancelled." + key + ".Owner").split(":")[1]));
                                    if (op != null) {
                                        Storage playerdata = Storage.getPlayer(op);
                                        ItemMail im = new ItemMail(playerdata.makeUID(), Bukkit.getOfflinePlayer(playerdata.getUUID()), databaseFile.getItemStack("OutOfTime/Cancelled." + key + ".Item"), databaseFile.getLong("OutOfTime/Cancelled." + key + ".Full-Time"), databaseFile.getBoolean("OutOfTime/Cancelled." + key + ".Never-Expire"));
                                        playerdata.addItem(im);
                                    }
                                } else if (databaseFile.get("OutOfTime/Cancelled." + key + ".Seller") != null) {
                                    OfflinePlayer op = Bukkit.getOfflinePlayer(databaseFile.getString("OutOfTime/Cancelled." + key + ".Seller"));
                                    if (op != null) {
                                        Storage playerdata = Storage.getPlayer(op);
                                        ItemMail im = new ItemMail(playerdata.makeUID(), Bukkit.getOfflinePlayer(playerdata.getUUID()), databaseFile.getItemStack("OutOfTime/Cancelled." + key + ".Item"), databaseFile.getLong("OutOfTime/Cancelled." + key + ".Full-Time"), databaseFile.getBoolean("OutOfTime/Cancelled." + key + ".Never-Expire"));
                                        playerdata.addItem(im);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            for (CommandSender sender : FileManager.syncSenders) {
                if (sender != null) {
                    sender.sendMessage(Messages.getMessage("Admin-Command.Synchronize.Successfully"));
                }
            }
            syncing = false;
        } catch (Exception ex) {
            for (CommandSender sender : FileManager.syncSenders) {
                if (sender != null) {
                    sender.sendMessage(Messages.getMessage("Admin-Command.Synchronize.Failed").replace("%error%", ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : "null"));
                }
            }
            syncing = false;
        }
    };
    
    private static CommandSender[] backupSenders = {};
    
    public static Runnable backupThread = () -> {
        try {
            backingup = true;
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
            for (CommandSender sender : FileManager.backupSenders) {
                if (sender != null) {
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("%file%",  fileName);
                    sender.sendMessage(Messages.getMessage("Admin-Command.Backup.Successfully", map));
                }
            }
            backingup = false;
        } catch (Exception ex) {
            for (CommandSender sender : FileManager.backupSenders) {
                if (sender != null) {
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("%error%",  ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : "null");
                    sender.sendMessage(Messages.getMessage("Admin-Command.Backup.Failed", map));
                }
            }
            backingup = false;
        }
    };
    
    /**
     * Synchronize command.
     * @param sender 
     */
    public static void synchronize(CommandSender... sender) {
        syncSenders = sender;
        new Thread(synchronizeThread, "SynchronizeThread").start();
    }
    
    /**
     * Backup command.
     * @param sender 
     */
    public static void backup(CommandSender... sender) {
        backupSenders = sender;
        new Thread(backupThread, "BackupThread").start();
    }
    
    public static boolean isBackingUp() {
        return backingup;
    }
    
    public static boolean isSyncing() {
        return syncing;
    }
    
    public static boolean isRollingBack() {
        return rollingback;
    }
    
    /**
     * Roll Back command.
     * The rollback will cover all the current data,
     * and call a large number of IO read and write performance.
     * To ensure that the data is error-free,
     * it cannot be rolled back asynchronously.
     * @param backupFile
     * @param sender 
     */
    public static void rollBack(File backupFile, CommandSender... sender) {
        rollingback = true;
        new RollBackMethod(backupFile, instance, sender).rollBack(true);
        rollingback = false;
    }
    
    public void saveResource(Files file) {
        String lang = Locale.getDefault().toString();
        String path = "English";
        if (lang.equalsIgnoreCase("zh_cn")) {
            path = "Chinese";
        }
        File newFile = new File(main.getDataFolder(), file.getFileLocation());
        if (!newFile.exists()) {
            try {
                String fileLocation = file.getFileLocation();
                //Switch between 1.12.2- and 1.13+ config version.
                if (file == Files.CONFIG) {
                    if (Version.getCurrentVersion().isOlder(Version.v1_13_R2)) {
                        fileLocation = "Config1.12.2-Down.yml";
                    } else {
                        fileLocation = "Config1.13-Up.yml";
                    }
                }
                File serverFile = new File(main.getDataFolder(), file.getFileLocation());
                InputStream jarFile = getClass().getResourceAsStream("/Languages/" + path + "/" + fileLocation);
                saveFile(jarFile, serverFile); 
            } catch (IOException e) {
                if (Main.language.get("ConfigurationFileNotExist") != null) Main.getInstance().getServer().getConsoleSender().sendMessage(Main.language.getProperty("ConfigurationFileNotExist").replace("{file}", newFile.getName()).replace("{prefix}", prefix).replace("&", "§"));
                return;
            }
        }
        files.put(file, newFile);
    }
    
    private void saveFile(InputStream is, File file) throws IOException {
        if (!file.exists()) {
            file.createNewFile();
        }
        try (OutputStream out = new FileOutputStream(file)) {
            int b;
            while ((b = is.read()) != -1) {
                out.write((char) b);
            }
        }
    }
    
    public void reloadMessages() {
        Files file = Files.MESSAGES;
        saveResource(file);
        File newFile = new File(main.getDataFolder(), file.getFileLocation());
        try (Reader Config = new InputStreamReader(new FileInputStream(newFile), "UTF-8")) {
            FileConfiguration config = new YamlConfiguration();
            config.load(Config);
            configurations.put(file, config);
            if (Main.language.get("ConfigurationFileLoadedSuccessfully") != null) Main.getInstance().getServer().getConsoleSender().sendMessage(Main.language.getProperty("ConfigurationFileLoadedSuccessfully").replace("{file}", newFile.getName()).replace("{prefix}", prefix).replace("&", "§"));
        } catch (IOException | InvalidConfigurationException ex) {
            if (Main.language.get("ConfigurationFileLoadingError") != null) Main.getInstance().getServer().getConsoleSender().sendMessage(Main.language.getProperty("ConfigurationFileLoadingError").replace("{file}", newFile.getName()).replace("{prefix}", prefix).replace("&", "§"));
            File oldFile = new File(main.getDataFolder(), newFile.getName() + ".old");
            if (oldFile.exists()) {
                oldFile.delete();
            }
            newFile.renameTo(oldFile);
            saveResource(file);
            try (Reader newConfig = new InputStreamReader(new FileInputStream(newFile))) {
                FileConfiguration config = new YamlConfiguration();
                config.load(newConfig);
                configurations.put(file, config);
            } catch (IOException | InvalidConfigurationException ex1) {
                Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex1);
            }
            if (Main.language.get("ConfigurationFileRepair") != null) Main.getInstance().getServer().getConsoleSender().sendMessage(Main.language.getProperty("ConfigurationFileRepair").replace("{prefix}", prefix).replace("&", "§"));
        }
    }
    
    public void reloadConfig() {
        Files file = Files.CONFIG;
        File oldconfig = new File(main.getDataFolder(), "config.yml");
        if (oldconfig.exists()) {
            oldconfig.renameTo(new File(main.getDataFolder(), "Config.yml"));
        }
        saveResource(file);
        File newFile = new File(main.getDataFolder(), file.getFileLocation());
        try (Reader Config = new InputStreamReader(new FileInputStream(newFile), "UTF-8")) {
            FileConfiguration config = new YamlConfiguration();
            config.load(Config);
            configurations.put(file, config);
            if (Main.language.get("ConfigurationFileLoadedSuccessfully") != null) Main.getInstance().getServer().getConsoleSender().sendMessage(Main.language.getProperty("ConfigurationFileLoadedSuccessfully").replace("{file}", newFile.getName()).replace("{prefix}", prefix).replace("&", "§"));
        } catch (IOException | InvalidConfigurationException ex) {
            if (Main.language.get("ConfigurationFileLoadingError") != null) Main.getInstance().getServer().getConsoleSender().sendMessage(Main.language.getProperty("ConfigurationFileLoadingError").replace("{file}", newFile.getName()).replace("{prefix}", prefix).replace("&", "§"));
            File oldFile = new File(main.getDataFolder(), newFile.getName() + ".old");
            if (oldFile.exists()) {
                oldFile.delete();
            }
            newFile.renameTo(oldFile);
            saveResource(file);
            try (Reader newConfig = new InputStreamReader(new FileInputStream(newFile))) {
                FileConfiguration config = new YamlConfiguration();
                config.load(newConfig);
                configurations.put(file, config);
            } catch (IOException | InvalidConfigurationException ex1) {
                Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex1);
            }
            if (Main.language.get("ConfigurationFileRepair") != null) Main.getInstance().getServer().getConsoleSender().sendMessage(Main.language.getProperty("ConfigurationFileRepair").replace("{prefix}", prefix).replace("&", "§"));
        }
    }
    
    public void reloadDatabaseFile() {
        if ((PluginControl.useMySQLStorage() && PluginControl.useSplitDatabase() && PluginControl.getMarketStorageMethod().equals(StorageMethod.MySQL)) 
                || PluginControl.useSQLiteStorage() && PluginControl.useSplitDatabase() && PluginControl.getMarketStorageMethod().equals(StorageMethod.SQLite)) return;
        Files file = Files.DATABASE;
        saveResource(file);
        File newFile = new File(main.getDataFolder(), file.getFileLocation());
        try (Reader Config = new InputStreamReader(new FileInputStream(newFile), "UTF-8")) {
            FileConfiguration config = new YamlConfiguration();
            config.load(Config);
            configurations.put(file, config);
            if (Main.language.get("ConfigurationFileLoadedSuccessfully") != null) Main.getInstance().getServer().getConsoleSender().sendMessage(Main.language.getProperty("ConfigurationFileLoadedSuccessfully").replace("{file}", newFile.getName()).replace("{prefix}", prefix).replace("&", "§"));
        } catch (IOException | InvalidConfigurationException ex) {
            if (Main.language.get("ConfigurationFileLoadingError") != null) Main.getInstance().getServer().getConsoleSender().sendMessage(Main.language.getProperty("ConfigurationFileLoadingError").replace("{file}", newFile.getName()).replace("{prefix}", prefix).replace("&", "§"));
            File oldFile = new File(main.getDataFolder(), newFile.getName() + ".old");
            if (oldFile.exists()) {
                oldFile.delete();
            }
            newFile.renameTo(oldFile);
            saveResource(file);
            try (Reader newConfig = new InputStreamReader(new FileInputStream(newFile))) {
                FileConfiguration config = new YamlConfiguration();
                config.load(newConfig);
                configurations.put(file, config);
            } catch (IOException | InvalidConfigurationException ex1) {
                Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex1);
            }
            if (Main.language.get("ConfigurationFileRepair") != null) Main.getInstance().getServer().getConsoleSender().sendMessage(Main.language.getProperty("ConfigurationFileRepair").replace("{prefix}", prefix).replace("&", "§"));
        }
        GlobalMarket.getMarket().reloadData();
    }
    
    public void reloadCategoryFile() {
        Files file = Files.CATEGORY;
        saveResource(file);
        File newFile = new File(main.getDataFolder(), file.getFileLocation());
        try (Reader Config = new InputStreamReader(new FileInputStream(newFile), "UTF-8")) {
            FileConfiguration config = new YamlConfiguration();
            config.load(Config);
            configurations.put(file, config);
            if (Main.language.get("ConfigurationFileLoadedSuccessfully") != null) Main.getInstance().getServer().getConsoleSender().sendMessage(Main.language.getProperty("ConfigurationFileLoadedSuccessfully").replace("{file}", newFile.getName()).replace("{prefix}", prefix).replace("&", "§"));
        } catch (IOException | InvalidConfigurationException ex) {
            if (Main.language.get("ConfigurationFileLoadingError") != null) Main.getInstance().getServer().getConsoleSender().sendMessage(Main.language.getProperty("ConfigurationFileLoadingError").replace("{file}", newFile.getName()).replace("{prefix}", prefix).replace("&", "§"));
            File oldFile = new File(main.getDataFolder(), newFile.getName() + ".old");
            if (oldFile.exists()) {
                oldFile.delete();
            }
            newFile.renameTo(oldFile);
            saveResource(file);
            try (Reader newConfig = new InputStreamReader(new FileInputStream(newFile))) {
                FileConfiguration config = new YamlConfiguration();
                config.load(newConfig);
                configurations.put(file, config);
            } catch (IOException | InvalidConfigurationException ex1) {
                Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex1);
            }
            if (Main.language.get("ConfigurationFileRepair") != null) Main.getInstance().getServer().getConsoleSender().sendMessage(Main.language.getProperty("ConfigurationFileRepair").replace("{prefix}", prefix).replace("&", "§"));
        }
        GlobalMarket.getMarket().reloadData();
    }
    
    public void reloadItemCollectionFile() {
        Files file = Files.ITEMCOLLECTION;
        saveResource(file);
        File newFile = new File(main.getDataFolder(), file.getFileLocation());
        try (Reader Config = new InputStreamReader(new FileInputStream(newFile), "UTF-8")) {
            FileConfiguration config = new YamlConfiguration();
            config.load(Config);
            configurations.put(file, config);
            if (Main.language.get("ConfigurationFileLoadedSuccessfully") != null) Main.getInstance().getServer().getConsoleSender().sendMessage(Main.language.getProperty("ConfigurationFileLoadedSuccessfully").replace("{file}", newFile.getName()).replace("{prefix}", prefix).replace("&", "§"));
        } catch (IOException | InvalidConfigurationException ex) {
            if (Main.language.get("ConfigurationFileLoadingError") != null) Main.getInstance().getServer().getConsoleSender().sendMessage(Main.language.getProperty("ConfigurationFileLoadingError").replace("{file}", newFile.getName()).replace("{prefix}", prefix).replace("&", "§"));
            File oldFile = new File(main.getDataFolder(), newFile.getName() + ".old");
            if (oldFile.exists()) {
                oldFile.delete();
            }
            newFile.renameTo(oldFile);
            saveResource(file);
            try (Reader newConfig = new InputStreamReader(new FileInputStream(newFile))) {
                FileConfiguration config = new YamlConfiguration();
                config.load(newConfig);
                configurations.put(file, config);
            } catch (IOException | InvalidConfigurationException ex1) {
                Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex1);
            }
            if (Main.language.get("ConfigurationFileRepair") != null) Main.getInstance().getServer().getConsoleSender().sendMessage(Main.language.getProperty("ConfigurationFileRepair").replace("{prefix}", prefix).replace("&", "§"));
        }
        GlobalMarket.getMarket().reloadData();
    }
    
    public FileManager setup(Main main) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.closeInventory();
                }
            }
        }.runTask(main);
        prefix = "[" + main.getName() + "] ";
        this.main = main;
        if (!main.getDataFolder().exists()) {
            main.getDataFolder().mkdirs();
        }
        files.clear();
        customFiles.clear();
        //Loads all the normal static files.
        for (Files file : Files.values()) {
            if (file.equals(Files.DATABASE)) {
                if (PluginControl.useMySQLStorage()) {
                    if (PluginControl.useSplitDatabase()) {
                        if (PluginControl.getMarketStorageMethod().equals(StorageMethod.MySQL)) {
                            continue;
                        }
                    } else {
                        continue;
                    }
                }
                if (PluginControl.useSQLiteStorage()) {
                    if (PluginControl.useSplitDatabase()) {
                        if (PluginControl.getMarketStorageMethod().equals(StorageMethod.SQLite)) {
                            continue;
                        }
                    } else {
                        continue;
                    }
                }
            }
            File oldconfig = new File(main.getDataFolder(), "config.yml");
            if (oldconfig.exists()) {
                oldconfig.renameTo(new File(main.getDataFolder(), "Config.yml"));
            }
            saveResource(file);
            File newFile = new File(main.getDataFolder(), file.getFileLocation());
            try (Reader Config = new InputStreamReader(new FileInputStream(newFile), "UTF-8")) {
                FileConfiguration config = new YamlConfiguration();
                config.load(Config);
                configurations.put(file, config);
                if (Main.language.get("ConfigurationFileLoadedSuccessfully") != null) Main.getInstance().getServer().getConsoleSender().sendMessage(Main.language.getProperty("ConfigurationFileLoadedSuccessfully").replace("{file}", newFile.getName()).replace("{prefix}", prefix).replace("&", "§"));
            } catch (IOException | InvalidConfigurationException ex) {
                if (Main.language.get("ConfigurationFileLoadingError") != null) Main.getInstance().getServer().getConsoleSender().sendMessage(Main.language.getProperty("ConfigurationFileLoadingError").replace("{file}", newFile.getName()).replace("{prefix}", prefix).replace("&", "§"));
                File oldFile = new File(main.getDataFolder(), newFile.getName() + ".old");
                if (oldFile.exists()) {
                    oldFile.delete();
                }
                newFile.renameTo(oldFile);
                saveResource(file);
                try (Reader newConfig = new InputStreamReader(new FileInputStream(newFile))) {
                    FileConfiguration config = new YamlConfiguration();
                    config.load(newConfig);
                    configurations.put(file, config);
                } catch (IOException | InvalidConfigurationException ex1) {
                    Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex1);
                }
                if (Main.language.get("ConfigurationFileRepair") != null) Main.getInstance().getServer().getConsoleSender().sendMessage(Main.language.getProperty("ConfigurationFileRepair").replace("{prefix}", prefix).replace("&", "§"));
            }
        }
        //Starts to load all the custom files.
        if (homeFolders.size() > 0) {
            if (log) System.out.println(prefix + "Loading custom files.");
            for (String homeFolder : homeFolders) {
                File homeFile = new File(main.getDataFolder(), "/" + homeFolder);
                if (homeFile.exists()) {
                    String[] list = homeFile.list();
                    if (list != null) {
                        for (String name : list) {
                            if (name.endsWith(".yml")) {
                                CustomFile file = new CustomFile(name, homeFolder, main);
                                if (file.exists()) {
                                    customFiles.add(file);
                                    if (log) System.out.println(prefix + "Loaded new custom file: " + homeFolder + "/" + name + ".");
                                }
                            }
                        }
                    }
                    
                } else {
                    homeFile.mkdir();
                    if (log) System.out.println(prefix + "The folder " + homeFolder + "/ was not found so it was created.");
                    for (String fileName : autoGenerateFiles.keySet()) {
                        if (autoGenerateFiles.get(fileName).equalsIgnoreCase(homeFolder)) {
                            homeFolder = autoGenerateFiles.get(fileName);
                            try {
                                File serverFile = new File(main.getDataFolder(), homeFolder + "/" + fileName);
                                InputStream jarFile = getClass().getResourceAsStream(homeFolder + "/" + fileName);
                                saveFile(jarFile, serverFile);
                                if (fileName.toLowerCase().endsWith(".yml")) {
                                    customFiles.add(new CustomFile(fileName, homeFolder, main));
                                }
                                if (log) System.out.println(prefix + "Created new default file: " + homeFolder + "/" + fileName + ".");
                            } catch (Exception e) {
                                if (log) System.out.println(prefix + "Failed to create new default file: " + homeFolder + "/" + fileName + "!");
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            if (log) System.out.println(prefix + "Finished loading custom files.");
        }
        return this;
    }
    
    /**
     * Turn on the logger system for the FileManager.
     * @param log True to turn it on and false for it to be off.
     * @return 
     */
    public FileManager logInfo(Boolean log) {
        this.log = log;
        return this;
    }
    
    /**
     * Check if the logger is logging in console.
     * @return True if it is and false if it isn't.
     */
    public Boolean isLogging() {
        return log;
    }
    
    /**
     * Register a folder that has custom files in it. Make sure to have a "/" in front of the folder name.
     * @param homeFolder The folder that has custom files in it.
     * @return 
     */
    public FileManager registerCustomFilesFolder(String homeFolder) {
        homeFolders.add(homeFolder);
        return this;
    }
    
    /**
     * Unregister a folder that has custom files in it. Make sure to have a "/" in front of the folder name.
     * @param homeFolder The folder with custom files in it.
     * @return 
     */
    public FileManager unregisterCustomFilesFolder(String homeFolder) {
        homeFolders.remove(homeFolder);
        return this;
    }
    
    /**
     * Register a file that needs to be generated when it's home folder doesn't exist. Make sure to have a "/" in front of the home folder's name.
     * @param fileName The name of the file you want to auto-generate when the folder doesn't exist.
     * @param homeFolder The folder that has custom files in it.
     * @return 
     */
    public FileManager registerDefaultGenerateFiles(String fileName, String homeFolder) {
        autoGenerateFiles.put(fileName, homeFolder);
        return this;
    }
    
    /**
     * Unregister a file that doesn't need to be generated when it's home folder doesn't exist. Make sure to have a "/" in front of the home folder's name.
     * @param fileName The file that you want to remove from auto-generating.
     * @return 
     */
    public FileManager unregisterDefaultGenerateFiles(String fileName) {
        autoGenerateFiles.remove(fileName);
        return this;
    }
    
    /**
     * Gets the file from the system.
     * @param file
     * @return The file from the system.
     */
    public FileConfiguration getFile(Files file) {
        return configurations.get(file);
    }
    
    /**
     * Get a custom file from the loaded custom files instead of a hardcoded one.
     * This allows you to get custom files like Per player data files.
     * @param name Name of the crate you want. (Without the .yml)
     * @return The custom file you wanted otherwise if not found will return null.
     */
    public CustomFile getFile(String name) {
        for (CustomFile file : customFiles) {
            if (file.getName().toLowerCase().equalsIgnoreCase(name.toLowerCase())) {
                return file;
            }
        }
        return null;
    }
    
    /**
     * Saves the file from the loaded state to the file system.
     * @param file
     */
    public void saveFile(Files file) {
        try {
            configurations.get(file).save(files.get(file));
        } catch (IOException e) {
            System.out.println(prefix + "Could not save " + file.getFileName() + "!");
            e.printStackTrace();
        }
    }
    
    /**
     * Save a custom file.
     * @param name The name of the custom file.
     */
    public void saveFile(String name) {
        CustomFile file = getFile(name);
        if (file != null) {
            try {
                file.getFile().save(new File(main.getDataFolder(), file.getHomeFolder() + "/" + file.getFileName()));
                if (log) System.out.println(prefix + "Successfuly saved the " + file.getFileName() + ".");
            } catch (Exception e) {
                System.out.println(prefix + "Could not save " + file.getFileName() + "!");
                e.printStackTrace();
            }
        } else {
            if (log) System.out.println(prefix + "The file " + name + ".yml could not be found!");
        }
    }
    
    /**
     * Save a custom file.
     * @param file The custom file you are saving.
     * @return True if the file saved correct and false if there was an error.
     */
    public Boolean saveFile(CustomFile file) {
        return file.saveFile();
    }
    
    /**
     * Overrides the loaded state file and loads the file systems file.
     * @param file
     */
    public void reloadFile(Files file) {
        configurations.put(file, YamlConfiguration.loadConfiguration(files.get(file)));
    }
    
    /**
     * Overrides the loaded state file and loads the file systems file.
     * @param name
     */
    @Deprecated
    public void reloadFile(String name) {
        CustomFile file = getFile(name);
        if (file != null) {
            try {
                file.file = YamlConfiguration.loadConfiguration(new File(main.getDataFolder(), "/" + file.getHomeFolder() + "/" + file.getFileName()));
                if (log) System.out.println(prefix + "Successfuly reload the " + file.getFileName() + ".");
            } catch (Exception e) {
                System.out.println(prefix + "Could not reload the " + file.getFileName() + "!");
                e.printStackTrace();
            }
        } else {
            if (log) System.out.println(prefix + "The file " + name + ".yml could not be found!");
        }
    }
    
    /**
     * Overrides the loaded state file and loads the filesystems file.
     * @param file
     * @return True if it reloaded correct and false if the file wasn't found.
     */
    public Boolean reloadFile(CustomFile file) {
        return file.reloadFile();
    }
    
    public enum Files {
        
        //ENUM_NAME("FileName.yml", "FilePath.yml"),
        CONFIG("Config.yml", "Config.yml"),
        DATABASE("Database.yml", "Database.yml"),
        CATEGORY("Category.yml", "Category.yml"),
        ITEMCOLLECTION("ItemCollection.yml", "ItemCollection.yml"),
        MESSAGES("Messages.yml", "Messages.yml");
        
        private final String fileName;
        private final String fileLocation;
        
        /**
         * The files that the server will try and load.
         * @param fileName The file name that will be in the plugin's folder.
         * @param fileLocation The location the file is in while in the Jar.
         */
        private Files(String fileName, String fileLocation) {
            this.fileName = fileName;
            this.fileLocation = fileLocation;
        }
        
        /**
         * Get the name of the file.
         * @return The name of the file.
         */
        public String getFileName() {
            return fileName;
        }
        
        /**
         * The location the jar it is at.
         * @return The location in the jar the file is in.
         */
        public String getFileLocation() {
            return fileLocation;
        }

        public ProtectedConfiguration getFile() {
            return new ProtectedConfiguration(this);
        }
        
        /**
         * Saves the file from the loaded state to the file system.
         */
        public void saveFile() {
            getInstance().saveFile(this);
        }
        
        /**
         * Overrides the loaded state file and loads the file systems file.
         */
        public void relaodFile() {
            getInstance().reloadFile(this);
        }
    }
    
    public static class ProtectedConfiguration {
        private final FileConfiguration config;
        private final Files file;
        
        private static final Map<Files, FileConfiguration> defaultConfig = new HashMap<Files, FileConfiguration>();
        
        private ProtectedConfiguration(Files file) {
            this.file = file;
            config = getInstance().getFile(file);
        }
        
        public Object get(String path) {
            return config.get(path);
        }
        
        public String getString(String path) {
            if (file.equals(Files.DATABASE)) return config.getString(path);
            if (config.get(path) == null) {
                reset(path);
                return config.getString(path);
            } else {
                return config.getString(path);
            }
        }
        
        public int getInt(String path) {
            if (file.equals(Files.DATABASE)) return config.getInt(path);
            if (config.get(path) == null) {
                reset(path);
                return config.getInt(path);
            } else {
                return config.getInt(path);
            }
        }
        
        public double getDouble(String path) {
            if (file.equals(Files.DATABASE)) return config.getDouble(path);
            if (config.get(path) == null) {
                reset(path);
                return config.getDouble(path);
            } else {
                return config.getDouble(path);
            }
        }
        
        public long getLong(String path) {
            if (file.equals(Files.DATABASE)) return config.getLong(path);
            if (config.get(path) == null) {
                reset(path);
                return config.getLong(path);
            } else {
                return config.getLong(path);
            }
        }
        
        public boolean getBoolean(String path) {
            if (config.get(path) == null) {
                return false;
            } else {
                return config.getBoolean(path);
            }
        }
        
        public List<String> getStringList(String path) {
            if (file.equals(Files.DATABASE)) return config.getStringList(path);
            if (config.get(path) == null) {
                reset(path);
                return config.getStringList(path);
            } else {
                return config.getStringList(path);
            }
        }
        
        public ItemStack getItemStack(String path) {
            if (file.equals(Files.DATABASE)) return config.getItemStack(path);
            if (config.get(path) == null) {
                reset(path);
                return config.getItemStack(path);
            } else {
                return config.getItemStack(path);
            }
        }
        
        public ConfigurationSection getConfigurationSection(String path) {
            if (file.equals(Files.DATABASE)) return config.getConfigurationSection(path);
            if (config.get(path) == null) {
                reset(path);
                return config.getConfigurationSection(path);
            } else {
                return config.getConfigurationSection(path);
            }
        }
        
        public boolean contains(String path) {
            return config.contains(path);
        }
        
        public void set(String path, Object obj) {
            config.set(path, obj);
        }
        
        protected void reset(String path) {
            if (defaultConfig.get(file) == null) {
                loadDefaultConfigurations();
            } else if (file.equals(Files.DATABASE)) {
                return;
            }
            FileConfiguration defaultFile = defaultConfig.get(file);
            config.set(path, defaultFile.get(path) != null ? defaultFile.get(path) : "Null");
            getInstance().saveFile(file);
        }
        
        protected void loadDefaultConfigurations() {
            String lang = Locale.getDefault().toString();
            String jarPath = "English";
            if (lang.equalsIgnoreCase("zh_cn")) {
                jarPath = "Chinese";
            }
            String fileName = file.getFileName();
            if (file.equals(Files.CONFIG)) {
                if (Version.getCurrentVersion().isOlder(Version.v1_13_R2)) {
                    fileName = "Config1.12.2-Down.yml";
                } else {
                    fileName = "Config1.13-Up.yml";
                }
            }
            try (Reader Config = new InputStreamReader(Main.getInstance().getClass().getResource("/Languages/" + jarPath + "/" + fileName).openStream(), "UTF-8")) {
                FileConfiguration configFile = new YamlConfiguration();
                configFile.load(Config);
                defaultConfig.put(file, configFile);
            } catch (IOException | InvalidConfigurationException ex) {
                Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    @Deprecated
    public class CustomFile {
        
        private final String name;
        private final Main main;
        private final String fileName;
        private final String homeFolder;
        private FileConfiguration file;
        
        /**
         * A custom file that is being made.
         * @param name Name of the file.
         * @param homeFolder The home folder of the file.
         * @param main The plugin the files belong to.
         */
        public CustomFile(String name, String homeFolder, Main main) {
            this.name = name.replace(".yml", "");
            this.main = main;
            this.fileName = name;
            this.homeFolder = homeFolder;
            if (new File(main.getDataFolder(), "/" + homeFolder).exists()) {
                if (new File(main.getDataFolder(), "/" + homeFolder + "/" + name).exists()) {
                    file = YamlConfiguration.loadConfiguration(new File(main.getDataFolder(), "/" + homeFolder + "/" + name));
                } else {
                    file = null;
                }
            } else {
                new File(main.getDataFolder(), "/" + homeFolder).mkdir();
                if (log) System.out.println(prefix + "The folder " + homeFolder + "/ was not found so it was created.");
                file = null;
            }
        }
        
        /**
         * Get the name of the file without the .yml part.
         * @return The name of the file without the .yml.
         */
        public String getName() {
            return name;
        }
        
        /**
         * Get the full name of the file.
         * @return Full name of the file.
         */
        public String getFileName() {
            return fileName;
        }
        
        /**
         * Get the name of the home folder of the file.
         * @return The name of the home folder the files are in.
         */
        public String getHomeFolder() {
            return homeFolder;
        }
        
        /**
         * Get the plugin the file belongs to.
         * @return The plugin the file belongs to.
         */
        public Main getPlugin() {
            return main;
        }
        
        /**
         * Get the ConfigurationFile.
         * @return The ConfigurationFile of this file.
         */
        public FileConfiguration getFile() {
            return file;
        }
        
        /**
         * Check if the file actually exists in the file system.
         * @return True if it does and false if it doesn't.
         */
        public Boolean exists() {
            return file != null;
        }
        
        /**
         * Save the custom file.
         * @return True if it saved correct and false if something went wrong.
         */
        public Boolean saveFile() {
            if (file != null) {
                try {
                    file.save(new File(main.getDataFolder(), homeFolder + "/" + fileName));
                    if (log) System.out.println(prefix + "Successfuly saved the " + fileName + ".");
                    return true;
                } catch (Exception e) {
                    System.out.println(prefix + "Could not save " + fileName + "!");
                    e.printStackTrace();
                    return false;
                }
            } else {
                if (log) System.out.println(prefix + "There was a null custom file that could not be found!");
            }
            return false;
        }
        
        /**
         * Overrides the loaded state file and loads the filesystems file.
         * @return True if it reloaded correct and false if the file wasn't found or errored.
         */
        public Boolean reloadFile() {
            if (file != null) {
                try {
                    file = YamlConfiguration.loadConfiguration(new File(main.getDataFolder(), "/" + homeFolder + "/" + fileName));
                    if (log) System.out.println(prefix + "Successfuly reload the " + fileName + ".");
                    return true;
                } catch (Exception e) {
                    System.out.println(prefix + "Could not reload the " + fileName + "!");
                    e.printStackTrace();
                }
            } else {
                if (log) System.out.println(prefix + "There was a null custom file that was not found!");
            }
            return false;
        }
    }
}