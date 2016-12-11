package de.pro_crafting.worldfuscator.Plugin;

import de.pro_crafting.wg.WarGear;
import de.pro_crafting.wg.arena.Arena;
import de.pro_crafting.wg.arena.ArenaPosition;
import de.pro_crafting.wg.group.PlayerRole;
import de.pro_crafting.worldfuscator.Core.BlockTranslater;
import de.pro_crafting.worldfuscator.Core.WorldFuscator;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class WorldFuscatorImpl extends WorldFuscator {
    public void onEnable() {
        setTranslater(new Translater());
        super.onEnable();
    }

    private class Translater extends BlockTranslater {
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

            if (role == PlayerRole.Team1 && (position == ArenaPosition.Team1PlayField || position == ArenaPosition.Team1WG)) {
                return true;
            } else if (role == PlayerRole.Team2 && (position == ArenaPosition.Team2PlayField || position == ArenaPosition.Team2WG)) {
                return true;
            }

            return false;
        }
    }
}
