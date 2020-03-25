package studio.trc.bukkit.crazyauctionsplus.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import studio.trc.bukkit.crazyauctionsplus.Main;
import studio.trc.bukkit.crazyauctionsplus.api.events.AuctionWinBidEvent;
import studio.trc.bukkit.crazyauctionsplus.currency.CurrencyManager;
import studio.trc.bukkit.crazyauctionsplus.database.GlobalMarket;
import studio.trc.bukkit.crazyauctionsplus.database.Storage;
import studio.trc.bukkit.crazyauctionsplus.utils.FileManager.Files;
import studio.trc.bukkit.crazyauctionsplus.utils.FileManager.ProtectedConfiguration;
import studio.trc.bukkit.crazyauctionsplus.utils.enums.Messages;
import studio.trc.bukkit.crazyauctionsplus.utils.enums.ShopType;

public class AuctionProcess
{
    public static void updateAuction() {
        if (FileManager.isBackingUp()) return;
        if (FileManager.isRollingBack()) return;
        if (FileManager.isSyncing()) return;
        Calendar cal = Calendar.getInstance();
        Calendar expireTime = Calendar.getInstance();
        Calendar fullExpireTime = Calendar.getInstance();
        ProtectedConfiguration config = Files.CONFIG.getFile();
        GlobalMarket market = GlobalMarket.getMarket();
        for (MarketGoods mg : market.getItems()) {
            if (mg.getItem() == null) {
                market.removeGoods(mg);
                continue;
            }
            if (mg.getShopType().equals(ShopType.BID)) {
                expireTime.setTimeInMillis(mg.getTimeTillExpire());
                fullExpireTime.setTimeInMillis(mg.getFullTime());
                if (cal.after(expireTime)) {
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
                        break;
                    } else {
                        Storage playerdata = Storage.getPlayer(mg.getItemOwner().getUUID());
                        ItemMail im = new ItemMail(playerdata.makeUID(), mg.getItemOwner().getUUID(), mg.getItem(), fullExpireTime.getTimeInMillis(), false);
                        playerdata.addItem(im);
                        market.removeGoods(mg.getUID());
                        if (mg.getItemOwner().getPlayer() != null) {
                            mg.getItemOwner().getPlayer().sendMessage(Messages.getMessage("Item-Has-Expired"));
                        }
                        break;
                    }
                } else {
                    if (config.getBoolean("Settings.Auction-Process-Settings.Countdown-Tips.Enabled")) {
                        long l = (mg.getTimeTillExpire() - System.currentTimeMillis()) / 1000;
                        if (config.get("Settings.Auction-Process-Settings.Countdown-Tips.Times." + l) != null) {
                            String item;
                            try {
                                item = mg.getItem().getItemMeta().hasDisplayName() ? mg.getItem().getItemMeta().getDisplayName() : (String) mg.getItem().getClass().getMethod("getI18NDisplayName").invoke(mg.getItem());
                            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                item = mg.getItem().getItemMeta().hasDisplayName() ? mg.getItem().getItemMeta().getDisplayName() : mg.getItem().getType().toString().toLowerCase().replace("_", " ");
                            }
                            for (String message : config.getStringList("Settings.Auction-Process-Settings.Countdown-Tips.Times." + l)) {
                                Bukkit.broadcastMessage(message.replace("%owner%", mg.getItemOwner().getName()).replace("%item%", item).replace("{prefix}", PluginControl.getPrefix()).replace("&", "§"));
                            }
                        }
                    }
                }
            }
        }
    }
    
    public static class AuctionUpdateThread extends Thread {
        
        public static AuctionUpdateThread thread;
        
        private final double updateDelay;
        
        public AuctionUpdateThread(double updateDelay) {
            this.updateDelay = updateDelay;
            thread = AuctionUpdateThread.this;
        }
        
        public double getUpdateDelay() {
            return updateDelay;
        }
        
        @Override
        public void run() {
            while (true) {
                try {
                    sleep((long) (updateDelay * 1000));
                    updateAuction();
                } catch (Exception ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
