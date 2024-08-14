package think.rpgitems.power;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.item.RPGItem;

import javax.annotation.CheckReturnValue;

/**
 * Triggers when being hurt
 */
public interface PowerHurt extends Pimpl {
    /**
     * Calls when {@code target} using {@code stack} being hurt in {@code event}
     *
     * @param target Player being hurt
     * @param item  RPGItem that triggered this power
     * @param stack  Item that triggered this power
     * @param event  Event that triggered this power
     * @return PowerResult
     */
    @CheckReturnValue
    PowerResult<Void> hurt(Player target, RPGItem item, ItemStack stack, EntityDamageEvent event);
}
