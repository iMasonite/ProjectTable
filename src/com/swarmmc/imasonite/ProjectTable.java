
package com.swarmmc.imasonite;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class ProjectTable extends JavaPlugin implements Listener {
	private static Logger log = Logger.getLogger("Minecraft");
	private Map<Block, String> inPt = new HashMap();
	private Map<String, Block> inPtPl = new HashMap();
	private Boolean clog = Boolean.valueOf(false);
	private String message = "Two players can't use same project table at the same time.";
	private Boolean allow_ignore = Boolean.valueOf(true);
	private int pt_id = 751;
	private int pt_subid = 3;
	
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		config_load();
		LogIt("Enabled!");
	}
	
	public void onDisable() {
		this.inPt.clear();
		this.inPtPl.clear();
		LogIt("Disabled and cleared hashmaps...");
	}
	
	public void LogIt(String x) {
		log.log(Level.INFO, "[" + getDescription().getName() + " " + getDescription().getVersion() + "] " + x);
	}
	
	public void config_load() {
		reloadConfig();
		getConfig().addDefault("log", Boolean.valueOf(false));
		getConfig().addDefault("message", "Two players can't use same project table at the same time.");
		getConfig().addDefault("allow_ignore", Boolean.valueOf(true));
		getConfig().addDefault("project_table.id", Integer.valueOf(751));
		getConfig().addDefault("project_table.subid", Integer.valueOf(3));
		
		getConfig().options().copyDefaults(true);
		saveConfig();
		
		this.clog = Boolean.valueOf(getConfig().getBoolean("log"));
		this.message = getConfig().getString("message");
		this.allow_ignore = Boolean.valueOf(getConfig().getBoolean("allow_ignore"));
		this.pt_id = getConfig().getInt("project_table.id");
		this.pt_subid = getConfig().getInt("project_table.subid");
		
		LogIt("config loaded.");
	}
	
	public Boolean canOpen(Player p) {
		if ((this.allow_ignore.booleanValue()) && ((p.isOp()) || (p.hasPermission("ptfix.ignore")))) {
			return Boolean.valueOf(true);
		}
		
		return Boolean.valueOf(false);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void playerQuit(PlayerQuitEvent e) {
		String player = e.getPlayer().getName();
		if (this.inPtPl.get(player) != null) {
			Block block = (Block) this.inPtPl.get(player);
			this.inPt.remove(block);
			this.inPtPl.remove(player);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void inventoryClose(InventoryCloseEvent e) {
		String player = e.getPlayer().getName();
		if (this.inPtPl.get(player) != null) {
			Block block = (Block) this.inPtPl.get(player);
			this.inPt.remove(block);
			this.inPtPl.remove(player);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void openPT(PlayerInteractEvent e) {
		if (e.getClickedBlock() == null) return;
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		
		if ((e.getClickedBlock().getTypeId() == this.pt_id) && (e.getClickedBlock().getData() == this.pt_subid)) {
			Player player = e.getPlayer();
			Block block = e.getClickedBlock();
			if (this.inPt.get(block) == null) {
				this.inPt.put(block, player.getName());
				this.inPtPl.put(player.getName(), block);
			} else {
				if (((String) this.inPt.get(block)).equals(player.getName())) return;
				if (!canOpen(player).booleanValue()) {
					player.sendMessage(ChatColor.RED + this.message);
					if (this.clog.booleanValue()) {
						String player_two = (String) this.inPt.get(block);
						Location l = block.getLocation();
						LogIt("Player " + player.getName() + " tried to open " + player_two + "'s Project Table at (" + l.getWorld().getName() + "; " + l.getX() + "; " + l.getY() + "; " + l.getZ() + ").");
					}
					e.setCancelled(true);
				} else
					if (this.clog.booleanValue()) {
						String player_two = (String) this.inPt.get(block);
						Location l = block.getLocation();
						LogIt("Operator " + player.getName() + " opened " + player_two + "'s Project Table at (" + l.getWorld().getName() + "; " + l.getX() + "; " + l.getY() + "; " + l.getZ() + ").");
					}
			}
		}
	}
}
