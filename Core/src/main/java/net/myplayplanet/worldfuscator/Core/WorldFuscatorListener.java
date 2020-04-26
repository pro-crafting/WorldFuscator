package net.myplayplanet.worldfuscator.Core;


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
