package think.rpgitems.power;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.item.RPGItem;

import javax.annotation.CheckReturnValue;

/**
 * Triggers per tick
 */
public interface PowerTick extends Pimpl {
    /**
     * Calls per tick with {@code player} using {@code stack}
     *
     * @param player Player
     * @param item  RPGItem that triggered this power
     * @param stack  Item that triggered this power
     * @return PowerResult
     */
    @CheckReturnValue
    PowerResult<Void> tick(Player player, RPGItem item, ItemStack stack);
}
