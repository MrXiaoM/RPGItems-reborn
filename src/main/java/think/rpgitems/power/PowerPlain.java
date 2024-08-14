package think.rpgitems.power;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.item.RPGItem;

import javax.annotation.CheckReturnValue;

/**
 * Plain triggers for powers that do not requires a event
 */
public interface PowerPlain extends Pimpl {
    /**
     * Simply trigger this power
     *
     * @param player Player
     * @param item  RPGItem that triggered this power
     * @param stack  Item that triggered this power
     * @return PowerResult
     */
    @CheckReturnValue
    PowerResult<Void> fire(Player player, RPGItem item, ItemStack stack);
}
