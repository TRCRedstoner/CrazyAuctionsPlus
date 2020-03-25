package studio.trc.bukkit.crazyauctionsplus.utils;

import studio.trc.bukkit.crazyauctionsplus.utils.enums.ShopType;
import studio.trc.bukkit.crazyauctionsplus.utils.FileManager.*;
import studio.trc.bukkit.crazyauctionsplus.database.GlobalMarket;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CrazyAuctions {
    
    private static final CrazyAuctions instance = new CrazyAuctions();
    
    public static CrazyAuctions getInstance() {
        return instance;
    }
    
    public Boolean isSellingEnabled() {
        return Files.CONFIG.getFile().getBoolean("Settings.Feature-Toggle.Selling");
    }
    
    public Boolean isBiddingEnabled() {
        return Files.CONFIG.getFile().getBoolean("Settings.Feature-Toggle.Bidding");
    }
    
    public Boolean isBuyingEnabled() {
        return Files.CONFIG.getFile().getBoolean("Settings.Feature-Toggle.Buying");
    }
    
    public int getNumberOfPlayerItems(Player player, ShopType type) {
        int number = 0;
        GlobalMarket market = GlobalMarket.getMarket();
        if (market.getItems().isEmpty()) return number;
        switch (type) {
            case SELL: {
                for (MarketGoods mg : market.getItems()) {
                    if (mg.getItemOwner().getUUID().equals(player.getUniqueId())) {
                        if (mg.getShopType().equals(ShopType.SELL)) {
                            number++;
                        }
                    }
                }
                return number;
            }
            case BUY: {
                for (MarketGoods mg : market.getItems()) {
                    if (mg.getItemOwner().getUUID().equals(player.getUniqueId())) {
                        if (mg.getShopType().equals(ShopType.BUY)) {
                            number++;
                        }
                    }
                }
                return number;
            }
            case BID: {
                for (MarketGoods mg : market.getItems()) {
                    if (mg.getItemOwner().getUUID().equals(player.getUniqueId())) {
                        if (mg.getShopType().equals(ShopType.BID)) {
                            number++;
                        }
                    }
                }
                return number;
            }
		default:
			return number;
        }
    }
    
    public List<MarketGoods> getMarketItems(Player player) {
        List<MarketGoods> items = new ArrayList<MarketGoods>();
        GlobalMarket market = GlobalMarket.getMarket();
        if (!market.getItems().isEmpty()) {
            for (MarketGoods mg : market.getItems()) {
                if (mg.getItemOwner().getUUID().equals(player.getUniqueId())) {
                    items.add(mg);
                }
            }
        }
        return items;
    }
}