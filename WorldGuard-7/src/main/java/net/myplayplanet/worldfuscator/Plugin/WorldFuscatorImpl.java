package net.myplayplanet.worldfuscator.Plugin;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import net.myplayplanet.worldfuscator.Core.BlockTranslator;
import net.myplayplanet.worldfuscator.Core.WorldFuscator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;

public class WorldFuscatorImpl extends WorldFuscator {

    public void onEnable() {
        setTranslator(new Translator());
        super.onEnable();
    }

    private class Translator extends BlockTranslator {

        WorldGuardPlugin wgp = WorldGuardPlugin.inst();
        WorldGuard wg = com.sk89q.worldguard.WorldGuard.getInstance();

        @Override
        protected boolean hasRights(Player player, int x, int y, int z, World world) {
            LocalPlayer wgPlayer = wgp.wrapPlayer(player);
            RegionContainer container = wg.getPlatform().getRegionContainer();

            // TODO: Maybe use new Spatial Queries api for performance reasons?

            RegionManager manager = container.get(BukkitAdapter.adapt(world));
            ApplicableRegionSet ars = manager.getApplicableRegions(BlockVector3.at(x, y, z));

            for (ProtectedRegion rg : ars) {
                if (rg.isMember(wgPlayer)) {
                    return true;
                }
            }
            return ars.queryState(wgPlayer, Flags.ENDERDRAGON_BLOCK_DAMAGE) == StateFlag.State.ALLOW;
        }
    }

    private class WorldGuardListener implements Listener {

        @EventHandler(priority = EventPriority.LOW)
        public void playerMoveHandler(PlayerMoveEvent event) {
            if (event.getTo().equals(event.getTo())) {
                return;
            }
            event.setCancelled(this.fireRegionChangeEvent(event.getPlayer(),
                    event.getFrom(), event.getTo(), event.isCancelled()));
        }

        @EventHandler(priority = EventPriority.LOW)
        public void playerTeleportHandler(PlayerTeleportEvent event) {
            event.setCancelled(this.fireRegionChangeEvent(event.getPlayer(),
                    event.getFrom(), event.getTo(), event.isCancelled()));
        }

        private boolean fireRegionChangeEvent(Player player, Location from,
                                              Location to, boolean isCancelled) {
            List<Region> leaves = rMan.getRegions(from);
            List<Region> enters = rMan.getRegions(to);
            leaves.removeAll(enters);
            enters.removeAll(leaves);

            if (leaves.size() == enters.size()) {
                return isCancelled;
            }
            boolean isEntering = leaves.size() < enters.size();

            PlayerRegionChangeEvent regionChangeEvent = new PlayerRegionChangeEvent(
                    player, leaves, enters, isEntering);
            regionChangeEvent.setCancelled(isCancelled);
            Bukkit.getPluginManager().callEvent(regionChangeEvent);

            return regionChangeEvent.isCancelled();
        }

        public List<Region> getRegions(Location at) {
            RegionManager rm = container.get(at.getWorld());
            ApplicableRegionSet set = rm.getApplicableRegions(at);
            List<Region> ret = new ArrayList<Region>(set.size());
            for (ProtectedRegion rg : set) {
                ret.add(new Region(rg.getId(), at.getWorld(), this));
            }
            return ret;
        }

        // TODO: Fix this shit
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

        public Point getMinimumPoint(Region region) {
            RegionManager rm = container.get(region.getWorld());
            ProtectedRegion rg = rm.getRegion(region.getId());
            if (rg == null)
                return null;
            return new Point(rg.getMinimumPoint().getBlockX(), rg.getMinimumPoint()
                    .getBlockY(), rg.getMinimumPoint().getBlockZ());
        }


        public Point getMaximumPoint(Region region) {
            RegionManager rm = container.get(region.getWorld());
            ProtectedRegion rg = rm.getRegion(region.getId());
            if (rg == null)
                return null;
            return new Point(rg.getMaximumPoint().getBlockX(), rg.getMaximumPoint()
                    .getBlockY(), rg.getMaximumPoint().getBlockZ());
        }

    }
}
