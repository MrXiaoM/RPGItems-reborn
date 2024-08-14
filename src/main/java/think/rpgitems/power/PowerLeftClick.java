package think.rpgitems.power;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.item.RPGItem;

import javax.annotation.CheckReturnValue;

/**
 * Triggers when left click
 */
public interface PowerLeftClick extends Pimpl {
    /**
     * Calls when {@code player} using {@code stack} left clicks {@code clicked}
     *
     * @param player Player
     * @param item  RPGItem that triggered this power
     * @param stack  Item that triggered this power
     * @param event  Event that triggered this power
     * @return PowerResult
     */
    @CheckReturnValue
    PowerResult<Void> leftClick(Player player, RPGItem item, ItemStack stack, PlayerInteractEvent event);
}
