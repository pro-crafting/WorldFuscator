package de.pro_crafting.worldfuscator.Plugin;

import static org.bukkit.event.EventPriority.MONITOR;

import net.myplayplanet.wg.WarGear;
import de.pro_crafting.wg.arena.Arena;
import de.pro_crafting.wg.arena.ArenaPosition;
import de.pro_crafting.wg.event.GroupUpdateEvent;
import de.pro_crafting.wg.group.GroupMember;
import de.pro_crafting.wg.group.PlayerRole;
import de.pro_crafting.wg.model.WgRegion;
import de.pro_crafting.worldfuscator.Core.BlockTranslator;
import de.pro_crafting.worldfuscator.Core.WorldFuscator;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class WorldFuscatorImpl extends WorldFuscator {

  public void onEnable() {
    setTranslater(new Translator());
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
          region.getMin(),
          region.getMax(),
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
