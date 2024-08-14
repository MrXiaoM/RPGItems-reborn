package think.rpgitems.power;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.item.RPGItem;

import javax.annotation.CheckReturnValue;

/**
 * Triggers when player sneaks
 */
public interface PowerSneak extends Pimpl {
    /**
     * Calls when {@code player} using {@code stack} sneaks
     *
     * @param player Player
     * @param item  RPGItem that triggered this power
     * @param stack  Item that triggered this power
     * @param event  Event that triggered this power
     * @return PowerResult
     */
    @CheckReturnValue
    PowerResult<Void> sneak(Player player, RPGItem item, ItemStack stack, PlayerToggleSneakEvent event);
}
