package think.rpgitems.power;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.item.RPGItem;

import javax.annotation.CheckReturnValue;

/**
 * Triggers when player swap mainhand item to offhand
 */
public interface PowerMainhandItem extends Pimpl {

    /**
     * Calls when {@code player} swap mainhand item to offhand
     *
     * @param player Player
     * @param item  RPGItem that triggered this power
     * @param stack  Item that triggered this power
     * @param event  Event that triggered this power
     * @return PowerResult with proposed event continuation
     */
    @CheckReturnValue
    PowerResult<Boolean> swapToOffhand(Player player, RPGItem item, ItemStack stack, PlayerSwapHandItemsEvent event);

    /**
     * Calls when {@code player} place item to offhand in inventory
     *
     * @param player Player
     * @param item  RPGItem that triggered this power
     * @param stack  Item that triggered this power
     * @param event  Event that triggered this power
     * @return PowerResult with proposed event continuation
     */
    @CheckReturnValue
    default PowerResult<Boolean> placeOffhand(Player player, RPGItem item, ItemStack stack, InventoryClickEvent event) {
        return PowerResult.noop();
    }
}