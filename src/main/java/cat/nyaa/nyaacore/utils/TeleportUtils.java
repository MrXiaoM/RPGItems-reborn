package cat.nyaa.nyaacore.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;

public final class TeleportUtils {

    public static boolean Teleport(Player player, Location loc) {
        if (!player.isOnline() || loc == null || loc.getWorld() == null) {
            return false;
        }
        player.setFallDistance(0);
        player.teleport(loc, PlayerTeleportEvent.TeleportCause.PLUGIN);
        return true;
    }

    public static void Teleport(List<Player> players, Location loc) {
        for (Player p : players) {
            Teleport(p, loc);
        }
    }
}
