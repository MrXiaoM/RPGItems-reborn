package think.rpgitems.power;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.item.RPGItem;

import javax.annotation.CheckReturnValue;

/**
 * Triggers when being hit
 */
public interface PowerHitTaken extends Pimpl {
    /**
     * Calls when {@code target} using {@code stack} being hit in {@code event}
     *
     * @param target Player been hit
     * @param item  RPGItem that triggered this power
     * @param stack  Item that triggered this power
     * @param damage Damage from this event
     * @param event  Event that triggered this power
     * @return PowerResult with proposed damage
     */
    @CheckReturnValue
    PowerResult<Double> takeHit(Player target, RPGItem item, ItemStack stack, double damage, EntityDamageEvent event);
}
