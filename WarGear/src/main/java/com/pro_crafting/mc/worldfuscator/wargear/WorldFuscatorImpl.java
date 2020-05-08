package com.pro_crafting.mc.worldfuscator.wargear;

import com.pro_crafting.mc.worldfuscator.WorldFuscator;
import com.pro_crafting.mc.worldfuscator.engine.WorldFuscatorGuard;
import net.myplayplanet.wargearfight.WarGear;
import net.myplayplanet.wargearfight.arena.Arena;
import net.myplayplanet.wargearfight.arena.ArenaPosition;
import net.myplayplanet.wargearfight.event.GroupUpdateEvent;
import net.myplayplanet.wargearfight.group.GroupMember;
import net.myplayplanet.wargearfight.group.PlayerRole;
import net.myplayplanet.wargearfight.model.WgRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.bukkit.event.EventPriority.MONITOR;

public class WorldFuscatorImpl extends WorldFuscator {

    private final WorldFuscatorGuardImpl guard = new WorldFuscatorGuardImpl();

    public void onEnable() {
        prepareDefaultConfiguration();

        super.onEnable();
        Bukkit.getPluginManager().registerEvents(new WarGearListener(), this);
    }

    @Override
    public WorldFuscatorGuard getWorldFuscatorGuard() {
        return guard;
    }

    private class WorldFuscatorGuardImpl extends WorldFuscatorGuard {

        WarGear warGear = WarGear.getPlugin(WarGear.class);

        @Override
        /**
         * Implementation of hasRights which checks if the player has rights to see all blocks, by checking the side and grop he is on.
         */
        public boolean hasRights(Player player, int x, int y, int z, World world) {
            Location location = new Location(world, x, y, z);
            Arena arenaAt = warGear.getArenaManager().getArenaAt(location);

            if (arenaAt == null) {
                return true;
            }

            ArenaPosition position = arenaAt.getPosition(location);

            if (position == ArenaPosition.Outside || position == ArenaPosition.Platform) {
                return true;
            }

            PlayerRole role = arenaAt.getGroupManager().getRole(player);

            if (role == PlayerRole.Team1 && (position == ArenaPosition.Team1PlayField
                    || position == ArenaPosition.Team1WG)) {
                return true;
            } else if (role == PlayerRole.Team2 && (position == ArenaPosition.Team2PlayField
                    || position == ArenaPosition.Team2WG)) {
                return true;
            }

            return false;
        }
    }

    private class WarGearListener implements Listener {

        @EventHandler(priority = MONITOR)
        /**
         * Handles updates to wargear fight groups.
         * On update of any group, a region of the world is refreshed for all old and new players
         */
        public void groupUpdateHandler(GroupUpdateEvent event) {
            List<UUID> oldPlayers = getPlayers(event.getOldMembers());
            List<UUID> newPlayers = getPlayers(event.getNewMembers());

            WgRegion region = event.getPlayerGroupKey().getRegion();
            getWorldRefresher().updateArea(
                    region.getWorld(),
                    region.getMin().toLocation(region.getWorld()),
                    region.getMax().toLocation(region.getWorld()),
                    oldPlayers,
                    newPlayers
            );
        }

        private List<UUID> getPlayers(Collection<GroupMember> members) {
            return members
                    .stream()
                    .map(m -> m.getOfflinePlayer().getUniqueId())
                    .collect(Collectors.toList());
        }
    }
}
