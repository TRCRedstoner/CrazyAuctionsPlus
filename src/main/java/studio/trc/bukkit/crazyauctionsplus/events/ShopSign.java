package studio.trc.bukkit.crazyauctionsplus.events;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import studio.trc.bukkit.crazyauctionsplus.Main;
import studio.trc.bukkit.crazyauctionsplus.utils.FileManager;
import studio.trc.bukkit.crazyauctionsplus.utils.FileManager.Files;
import studio.trc.bukkit.crazyauctionsplus.utils.FileManager.ProtectedConfiguration;
import studio.trc.bukkit.crazyauctionsplus.utils.PluginControl;

public class ShopSign implements Listener {
	private static final String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

	@EventHandler(priority = EventPriority.LOWEST)
	public void click(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		if (FileManager.isBackingUp() || FileManager.isRollingBack() || PluginControl.isWorldDisabled(p)) {
			return;
		}
		ProtectedConfiguration config = Files.CONFIG.getFile();
		if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			if (!config.getBoolean("Settings.Shop-Sign.Enabled"))
				return;
			if (version.startsWith("v1_7") || version.startsWith("v1_8") || version.startsWith("v1_9")
					|| version.startsWith("v1_10") || version.startsWith("v1_11") || version.startsWith("v1_12")) {
				if (e.getClickedBlock().getType().equals(Material.valueOf("SIGN"))
						|| e.getClickedBlock().getType().equals(Material.valueOf("SIGN_POST"))) {
					Sign sign = (Sign) e.getClickedBlock().getState();
					if (sign.getLine(0) != null
							&& sign.getLine(0).equalsIgnoreCase(config.getString("Settings.Shop-Sign.Title-Format"))) {
						if (sign.getLine(1) != null) {
							new BukkitRunnable() {
								@SuppressWarnings("deprecation")
								@Override
								public void run() {
									GUIAction.openViewer(p, Bukkit.getOfflinePlayer(sign.getLine(1)).getUniqueId(), 1);
								}
							}.runTaskLater(Main.getInstance(), 1);
						}
					}
				}
			} else if (version.startsWith("v1_13")) {
				if (e.getClickedBlock().getType().equals(Material.valueOf("SIGN"))
						|| e.getClickedBlock().getType().equals(Material.valueOf("WALL_SIGN"))) {
					Sign sign = (Sign) e.getClickedBlock().getState();
					if (sign.getLine(0) != null
							&& sign.getLine(0).equalsIgnoreCase(config.getString("Settings.Shop-Sign.Title-Format"))) {
						if (sign.getLine(1) != null) {
							new BukkitRunnable() {
								@SuppressWarnings("deprecation")
								@Override
								public void run() {
									GUIAction.openViewer(p, Bukkit.getOfflinePlayer(sign.getLine(1)).getUniqueId(), 1);
								}
							}.runTaskLater(Main.getInstance(), 1);
						}
					}
				}
			} else {
				Material type = e.getClickedBlock().getType();
				if (type.equals(Material.valueOf("OAK_SIGN")) || type.equals(Material.valueOf("OAK_WALL_SIGN"))
						|| type.equals(Material.valueOf("SPRUCE_SIGN"))
						|| type.equals(Material.valueOf("SPRUCE_WALL_SIGN"))
						|| type.equals(Material.valueOf("BIRCH_SIGN"))
						|| type.equals(Material.valueOf("BIRCH_WALL_SIGN"))
						|| type.equals(Material.valueOf("JUNGLE_SIGN"))
						|| type.equals(Material.valueOf("JUNGLE_WALL_SIGN"))
						|| type.equals(Material.valueOf("ACACIA_SIGN"))
						|| type.equals(Material.valueOf("ACACIA_WALL_SIGN"))
						|| type.equals(Material.valueOf("DARK_OAK_SIGN"))
						|| type.equals(Material.valueOf("DARK_OAK_WALL_SIGN"))) {
					Sign sign = (Sign) e.getClickedBlock().getState();
					if (sign.getLine(0) != null
							&& sign.getLine(0).equalsIgnoreCase(config.getString("Settings.Shop-Sign.Title-Format"))) {
						if (sign.getLine(1) != null) {
							new BukkitRunnable() {
								@SuppressWarnings("deprecation")
								@Override
								public void run() {
									GUIAction.openViewer(p, Bukkit.getOfflinePlayer(sign.getLine(1)).getUniqueId(), 1);
								}
							}.runTaskLater(Main.getInstance(), 1);
						}
					}
				}
			}
		}
	}
}
