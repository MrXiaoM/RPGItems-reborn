package think.rpgitems.utils.events;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;

public interface PlayerArmorChange {
    void run(PlayerEvent e, Player player, ItemStack item);
}
