package com.pro_crafting.mc.worldfuscator.wargear;

import com.pro_crafting.mc.worldfuscator.Point;
import net.myplayplanet.wargearfight.WarGear;
import net.myplayplanet.wargearfight.arena.Arena;
import net.myplayplanet.wargearfight.arena.ArenaPosition;
import net.myplayplanet.wargearfight.event.GroupUpdateEvent;
import net.myplayplanet.wargearfight.group.GroupMember;
import net.myplayplanet.wargearfight.group.PlayerRole;
import net.myplayplanet.wargearfight.model.WgRegion;
import com.pro_crafting.mc.worldfuscator.BlockTranslator;
import com.pro_crafting.mc.worldfuscator.WorldFuscator;
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

    public void onEnable() {
        setTranslator(new Translator());
        super.onEnable();
        Bukkit.getPluginManager().registerEvents(new WarGearListener(), this);
    }

    private class Translator extends BlockTranslator {

        WarGear warGear = WarGear.getPlugin(WarGear.class);

        @Override
        protected boolean hasRights(Player player, int x, int y, int z, World world) {
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
        public void groupUpdateHandler(GroupUpdateEvent event) {
            List<UUID> oldPlayers = getPlayers(event.getOldMembers());
            List<UUID> newPlayers = getPlayers(event.getNewMembers());

            WgRegion region = event.getPlayerGroupKey().getRegion();
            getWorldRefresher().updateArea(
                    region.getWorld(),
                    new Point(region.getMin().getX(), region.getMin().getY(), region.getMin().getZ()),
                    new Point(region.getMax().getX(), region.getMax().getY(), region.getMax().getZ()),
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
