package de.pro_crafting.worldfuscator.Core;

import de.pro_crafting.region.events.RegionDomainChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;


public class WorldFuscatorListener implements Listener {

  private WorldFuscator plugin;

  public WorldFuscatorListener(WorldFuscator plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void handleDomainChange(RegionDomainChangeEvent event) {
    if (plugin.getConfiguration().isDebugEnabled()) {
      Bukkit.getLogger().info("Chunk refresh of region: " + event.getRegion().getId());
    }
    this.plugin.getWorldRefresher().updateArea(event.getRegion().getWorld(),
        event.getRegion().getMin(),
        event.getRegion().getMax(),
        event.getOldPlayers(),
        event.getNewPlayers()
    );
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void handlePlayerInteract(PlayerInteractEvent event) {
    if (event.getHand() != EquipmentSlot.HAND) {
      return;
    }

    if (!event.hasItem()) {
      return;
    }

    ItemStack item = event.getItem();

    if (item.getType() != Material.BLAZE_ROD) {
      return;
    }

    Player player = event.getPlayer();
    if (!player.hasPermission("worldfuscator.debug.toggle")) {
      return;
    }

    boolean debugEnabled = this.plugin.getConfiguration().isDebugEnabled();
    debugEnabled = !debugEnabled;
    this.plugin.getConfiguration().setDebugEnabled(debugEnabled);

    player.sendMessage("[WorldFuscator] Debug ist nun auf " + debugEnabled);
  }
}
