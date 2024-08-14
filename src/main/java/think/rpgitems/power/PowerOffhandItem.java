package think.rpgitems.power;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.item.RPGItem;

import javax.annotation.CheckReturnValue;

/**
 * Triggers when player swap offhand item to main hand
 */
public interface PowerOffhandItem extends Pimpl {

    /**
     * Calls when {@code player} swap offhand item to mainhand
     *
     * @param player Player
     * @param item  RPGItem that triggered this power
     * @param stack  Item that triggered this power
     * @param event  Event that triggered this power
     * @return PowerResult with proposed event continuation
     */
    @CheckReturnValue
    PowerResult<Boolean> swapToMainhand(Player player, RPGItem item, ItemStack stack, PlayerSwapHandItemsEvent event);

    /**
     * Calls when {@code player} click offhand item in inventory
     *
     * @param player Player
     * @param item  RPGItem that triggered this power
     * @param stack  Item that triggered this power
     * @param event  Event that triggered this power
     * @return PowerResult with proposed event continuation
     */
    @CheckReturnValue
    default PowerResult<Boolean> pickupOffhand(Player player, RPGItem item, ItemStack stack, InventoryClickEvent event) {
        return PowerResult.noop();
    }
}